package com.application.areca.launcher.gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.application.areca.AbstractTarget;
import com.application.areca.AbstractWorkspaceItem;
import com.application.areca.ActionProxy;
import com.application.areca.ApplicationException;
import com.application.areca.ArchiveMedium;
import com.application.areca.ArecaRawFileList;
import com.application.areca.ArecaURLs;
import com.application.areca.CheckParameters;
import com.application.areca.EntryArchiveData;
import com.application.areca.EntryStatus;
import com.application.areca.MergeParameters;
import com.application.areca.SimulationResult;
import com.application.areca.TargetGroup;
import com.application.areca.UserInformationChannel;
import com.application.areca.Utils;
import com.application.areca.Workspace;
import com.application.areca.WorkspaceItem;
import com.application.areca.adapters.ConfigurationHandler;
import com.application.areca.adapters.ConfigurationListener;
import com.application.areca.cache.ArchiveManifestCache;
import com.application.areca.context.ProcessContext;
import com.application.areca.context.ReportingConfiguration;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.impl.FileSystemTarget;
import com.application.areca.impl.copypolicy.AbstractCopyPolicy;
import com.application.areca.impl.copypolicy.AlwaysOverwriteCopyPolicy;
import com.application.areca.impl.copypolicy.AskBeforeOverwriteCopyPolicy;
import com.application.areca.impl.copypolicy.NeverOverwriteCopyPolicy;
import com.application.areca.impl.copypolicy.OverwriteIfNewerCopyPolicy;
import com.application.areca.launcher.ArecaUserPreferences;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ActionConstants;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.launcher.gui.common.CTabFolderManager;
import com.application.areca.launcher.gui.common.SecuredRunner;
import com.application.areca.launcher.gui.composites.GUIInformationChannel;
import com.application.areca.launcher.gui.composites.LogComposite;
import com.application.areca.launcher.gui.confimport.ImportConfigurationWindow;
import com.application.areca.launcher.gui.menus.AppActionReferenceHolder;
import com.application.areca.launcher.gui.menus.MenuBuilder;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.application.areca.launcher.gui.wizards.BackupShortcutWizardWindow;
import com.application.areca.launcher.gui.wizards.BackupStrategyWizardWindow;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.manifest.ManifestKeys;
import com.application.areca.metadata.trace.TraceEntry;
import com.application.areca.metadata.transaction.GUITransactionHandler;
import com.application.areca.search.SearchResultItem;
import com.application.areca.version.VersionChecker;
import com.application.areca.version.VersionInfos;
import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.system.NoBrowserFoundException;
import com.myJava.system.OSTool;
import com.myJava.system.viewer.ViewerHandlerHelper;
import com.myJava.util.Util;
import com.myJava.util.log.FileLogProcessor;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;
import com.myJava.util.taskmonitor.TaskMonitor;
import com.myJava.util.version.VersionData;
import com.myJava.util.xml.AdapterException;

/**
 * <BR>
 * 
 * @author Olivier PETRUCCI <BR>
 *         
 *
 */

 /*
 Copyright 2005-2015, Olivier PETRUCCI.

This file is part of Areca.

    Areca is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Areca is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Areca; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 */
public class Application implements ActionConstants, Window.IExceptionHandler, ArecaURLs {
	public static String[] STATUS_LABELS;
	public static Image[] STATUS_ICONS;
	private static final ResourceManager RM = ResourceManager.instance();
	private static Application instance = new Application();
	public static boolean SIMPLE_MAINTABS = true;
	public static String TEXT_EDITOR_PLACE_HOLDER = "%f";

	public static Application getInstance() {
		return instance;
	}

	static {
		STATUS_LABELS = new String[8];
		STATUS_ICONS = new Image[8];
		STATUS_LABELS[EntryStatus.STATUS_CREATED + 1] = ResourceManager.instance().getLabel("archivecontent.statuscreation.label");
		STATUS_LABELS[EntryStatus.STATUS_MODIFIED + 1] = ResourceManager.instance().getLabel("archivecontent.statusmodification.label");
		STATUS_LABELS[EntryStatus.STATUS_DELETED + 1] = ResourceManager.instance().getLabel("archivecontent.statusdeletion.label");
		STATUS_LABELS[EntryStatus.STATUS_MISSING + 1] = ResourceManager.instance().getLabel("archivecontent.statusmissing.label");
		STATUS_LABELS[EntryStatus.STATUS_FIRST_BACKUP + 1] = ResourceManager.instance().getLabel("archivecontent.statusfirstbackup.label");
		STATUS_LABELS[EntryStatus.STATUS_UNKNOWN + 1] = ResourceManager.instance().getLabel("archivecontent.statusunknown.label");
		STATUS_ICONS[EntryStatus.STATUS_CREATED + 1] = ArecaImages.ICO_HISTO_NEW;
		STATUS_ICONS[EntryStatus.STATUS_MODIFIED + 1] = ArecaImages.ICO_HISTO_EDIT;
		STATUS_ICONS[EntryStatus.STATUS_DELETED + 1] = ArecaImages.ICO_HISTO_DELETE;
		STATUS_ICONS[EntryStatus.STATUS_FIRST_BACKUP + 1] = ArecaImages.ICO_HISTO_NEW;
	}

	private CTabFolderManager folderMonitor = new CTabFolderManager();

	// Keep a reference on the display used by the swt thread.
	private Display display;
	private Clipboard clipboard;
	private Menu archiveContextMenu;
	private Menu archiveContextMenuLogical;
	private Menu actionContextMenu;
	private Menu targetContextMenu;
	private Menu groupContextMenu;
	private Menu workspaceContextMenu;
	private Menu logContextMenu;
	private Menu historyContextMenu;
	private Menu searchContextMenu;

	private Workspace workspace;
	private MainWindow mainWindow;

	private WorkspaceItem currentObject; // Objet en cours de selection; il peut
	// s'agir d'un workspace, groupe ou
	// target
	private GregorianCalendar currentFromDate; // Debut de l'intervalle de dates
	// en cours de selection
	private GregorianCalendar currentToDate; // Fin de l'intervalle de dates en
	// cours de selection
	private TraceEntry currentEntry; // Entree en cours de selection (utile pour
	// le detail d'une archive)
	private EntryArchiveData currentEntryData; // En cas d'affichage de
	// l'historique d'une entree,
	// date en cours de selection
	private UIRecoveryFilter currentFilter; // En cas de selection d'un noeud sur
	// le panel de detail d'une archive
	// (repertoire ou Entry reelle), nom
	// de celui ci.
	private boolean latestVersionRecoveryMode; // Indique si la recovery se fera
	// en derniere version ou non

	private Set channels = new HashSet();

	private FileTool fileTool = FileTool.getInstance();

	public Cursor CURSOR_WAIT;

	public Application() {
		Window.setExceptionHandler(this);
	}

	public void show(String workspacePath) {
		mainWindow = new MainWindow();
		mainWindow.setWorkspacePath(workspacePath);
		display = Display.getCurrent();
		clipboard = new Clipboard(display);

		CURSOR_WAIT = new Cursor(display, SWT.CURSOR_WAIT);
		AppActionReferenceHolder.refresh();
		mainWindow.show();
	}

	public void initMenus(Shell shell) {
		this.archiveContextMenu = MenuBuilder.buildArchiveContextMenu(shell);
		this.archiveContextMenuLogical = MenuBuilder
				.buildArchiveContextMenuLogical(shell);
		this.actionContextMenu = MenuBuilder.buildActionContextMenu(shell);
		this.targetContextMenu = MenuBuilder.buildTargetContextMenu(shell);
		this.groupContextMenu = MenuBuilder.buildGroupContextMenu(shell);
		this.workspaceContextMenu = MenuBuilder
				.buildWorkspaceContextMenu(shell);
		this.logContextMenu = MenuBuilder.buildLogContextMenu(shell);
		this.historyContextMenu = MenuBuilder.buildHistoryContextMenu(shell);
		this.searchContextMenu = MenuBuilder.buildSearchContextMenu(shell);
	}

	public void checkSystem() {
		if (!VersionInfos.checkJavaVendor()) {
			Logger.defaultLogger().warn(VersionInfos.VENDOR_MSG);

			showDoNotShowAgainWindow(RM.getLabel("common.java.vendor.title"),
					RM.getLabel("common.java.vendor.message",
							new Object[] { OSTool.getJavaVendor() }),
							ArecaUserPreferences.DISPLAY_JAVA_VENDOR_MESSAGE);
		}
	}

	public void showDoNotShowAgainWindow(String title, String message,
			String key) {
		if (ArecaUserPreferences.isDisplayMessage(key)) {
			DoNotShowAgainWindow frm = new DoNotShowAgainWindow(title, message,
					key);
			showDialog(frm);
		}
	}

	public UIRecoveryFilter getCurrentFilter() {
		return currentFilter;
	}

	public Clipboard getClipboard() {
		return clipboard;
	}

	public Menu getArchiveContextMenu() {
		return archiveContextMenu;
	}

	public Menu getArchiveContextMenuLogical() {
		return archiveContextMenuLogical;
	}

	public Menu getActionContextMenu() {
		return actionContextMenu;
	}

	public Menu getGroupContextMenu() {
		return groupContextMenu;
	}

	public Menu getTargetContextMenu() {
		return targetContextMenu;
	}

	public Menu getWorkspaceContextMenu() {
		return workspaceContextMenu;
	}

	public Menu getLogContextMenu() {
		return logContextMenu;
	}

	public Menu getHistoryContextMenu() {
		return historyContextMenu;
	}

	public Menu getSearchContextMenu() {
		return searchContextMenu;
	}

	public Display getDisplay() {
		return display;
	}

	public CTabFolderManager getFolderMonitor() {
		return folderMonitor;
	}

	public boolean isLatestVersionRecoveryMode() {
		return latestVersionRecoveryMode;
	}

	public void setLatestVersionRecoveryMode(boolean latestVersionRecoveryMode) {
		this.latestVersionRecoveryMode = latestVersionRecoveryMode;
		AppActionReferenceHolder.refresh();
	}

	public void processCommand(final String command) {
		if (command == null) {
			return;
		} else if (command.equals(CMD_ABOUT)) {
			// ABOUT
			AboutWindow about = new AboutWindow(0);
			showDialog(about);
		} else if (command.equals(CMD_PLUGINS)) {
			// ABOUT
			AboutWindow win = new AboutWindow(4);
			showDialog(win);
		} else if (command.equals(CMD_HELP)) {
			// HELP
			showWebPage(HELP_ROOT
					+ VersionInfos.getLastVersion().getVersionId());
		} else if (command.equals(CMD_TUTORIAL)) {
			// TUTORIAL
			showWebPage(TUTORIAL_ROOT
					+ VersionInfos.getLastVersion().getVersionId());
		} else if (command.equals(CMD_BACKUP_ALL)) {
			this.showBackupWindow(null, workspace.getContent());
		} else if (command.equals(CMD_BACKUP)) {
			// BACKUP
			if (TargetGroup.class.isAssignableFrom(this.getCurrentObject()
					.getClass())) {
				this.showBackupWindow(null, getCurrentTargetGroup());
			} else if (FileSystemTarget.class.isAssignableFrom(this
					.getCurrentObject().getClass())) {
				// BACKUP WITH MANIFEST
				Manifest mf;
				try {
					mf = ((AbstractIncrementalFileSystemMedium) this
							.getCurrentTarget().getMedium())
							.buildDefaultBackupManifest();
				} catch (ApplicationException e1) {
					Logger.defaultLogger().error(e1);
					mf = null;
				}
				this.showBackupWindow(mf, getCurrentTarget());
			}
		} else if (command.equals(CMD_MERGE)) {
			// MERGE
			AbstractTarget target = this.getCurrentTarget();
			ArchiveMedium medium = target.getMedium();
			if (!((AbstractIncrementalFileSystemMedium) medium).isImage()) {
				try {
					Manifest manifest = this.getCurrentTarget()
							.buildDefaultMergeManifest(
									this.getCurrentFromDate(),
									this.getCurrentToDate());
					this.showMergeWindow(target, manifest);
				} catch (ApplicationException e1) {
					handleException(e1);
				}
			}
		} else if (command.equals(CMD_DELETE_ARCHIVES)) {
			// DELETE ARCHIVES
			int result = showConfirmDialog(RM.getLabel(
					"app.deletearchivesaction.confirmverbose.message",
					new Object[] { Utils
							.formatDisplayDate(this.currentFromDate) }),
							RM.getLabel("app.deletearchivesaction.confirm.title"));

			if (result == SWT.YES) {
				if (FileSystemTarget.class.isAssignableFrom(this
						.getCurrentObject().getClass())) {
					FileSystemTarget target = (FileSystemTarget) this
							.getCurrentObject();
					TargetGroup process = target.getParent();
					ProcessRunner rn = new ProcessRunner(target) {
						public void runCommand() throws ApplicationException {
							ActionProxy.processDeleteOnTarget(rTarget,
									rFromDate, context);
						}
					};
					rn.rProcess = process;
					rn.rName = RM
							.getLabel("app.deletearchivesaction.process.message");
					rn.rFromDate = currentFromDate;
					rn.rToDate = currentToDate;
					rn.launch();
					resetCurrentDates();
				}
			}
		} else if (command.equals(CMD_NEW_TARGET)) {
			// NEW TARGET
			showEditTarget(null);
		} else if (command.equals(CMD_EDIT_TARGET)) {
			// EDIT TARGET
			showEditTarget((AbstractTarget) this.getCurrentObject());
		} else if (command.equals(CMD_DEL_TARGET)
				|| command.equals(CMD_DEL_GROUP)) {
			// DELETE TARGET / GROUP
			showDeleteItem();
		} else if (command.equals(CMD_NEW_GROUP)) {
			// NEW GROUP
			showEditGroup();
		} else if (command.equals(CMD_DUPLICATE_TARGET)) {
			// DUPLICATE TARGET
			try {
				duplicateTarget(this.getCurrentTarget());
			} catch (ApplicationException e1) {
				this.handleException(RM.getLabel(
						"error.duplicatetarget.message",
						new Object[] { e1.getMessage() }), e1);
			}
		} else if (command.equals(CMD_EDIT_XML)) {
			// EDIT XML CONFIGURATION
			showEditTargetXML(this.getCurrentTarget());
		} else if (command.equals(CMD_SUPPORT)) {
			showWebPage(DONATION_URL);
		} else if (command.equals(CMD_SIMULATE)) {
			// SIMULATE
			ProcessRunner rn = new ProcessRunner(this.getCurrentTarget()) {
				private SimulationResult entries;

				public void runCommand() throws ApplicationException {
					entries = ActionProxy.processSimulateOnTarget(this.rTarget,
							this.context);
				}

				protected void finishCommand() {
					SecuredRunner.execute(new Runnable() {
						public void run() {
							SimulationWindow frm = new SimulationWindow(
									entries, rTarget);
							showDialog(frm);
						}
					});
				}
			};
			rn.rProcess = this.getCurrentTargetGroup();
			rn.rName = RM.getLabel("app.simulateaction.process.message");
			rn.refreshAfterProcess = false;
			rn.launch();
		} else if (command.equals(CMD_EXIT)) {
			// EXIT
			this.processExit();
		} else if (command.equals(CMD_OPEN)) {
			// OPEN WORKSPACE
			String initPath = this.workspace != null ? this.workspace.getPath()
					: OSTool.getUserHome();
			String path = showDirectoryDialog(initPath, this.mainWindow);
			openWorkspace(path);
		} else if (command.equals(CMD_IMPORT_CONF)) {
			// IMPORT GROUP
			ImportConfigurationWindow frm = new ImportConfigurationWindow();
			showDialog(frm);
		} else if (command.equals(CMD_PREFERENCES)) {
			// PREFERENCES
			PreferencesWindow frm = new PreferencesWindow();
			showDialog(frm);
		} else if (command.equals(ACTION_CLEAR_LOG)) {
			clearLog();
		} else if (command.equals(CMD_BACKUP_WORKSPACE)) {
			// BACKUP WORKSPACE
			CopyWorkspaceWindow frm = new CopyWorkspaceWindow();
			showDialog(frm);
		} else if (command.equals(CMD_RECOVER)
				|| command.equals(CMD_RECOVER_WITH_FILTER)
				|| command.equals(CMD_RECOVER_WITH_FILTER_LATEST)) {
			// RECOVER
			RecoverWindow window = new RecoverWindow(true);
			window.setRecoverDeletedEntries(this.currentFilter != null && this.currentFilter.isContainsDeletedDirectory());
			this.showDialog(window);
			String path = window.getLocation();
			final boolean checkRecoveredFiles = window.isCheckRecoveredFiles();
			final boolean addSubdirectory = window.isAppendSubdirectory();
			final boolean recoverDeletedEntries = window.isRecoverDeletedEntries();

			if (path != null) {
				if (FileSystemTarget.class.isAssignableFrom(this.getCurrentObject().getClass())) {
					FileSystemTarget target = (FileSystemTarget) this.getCurrentObject();
					TargetGroup process = target.getParent();
					final AbstractCopyPolicy policy;
					if (window.isNeverOverwrite()) {
						policy = new NeverOverwriteCopyPolicy();
					} else if (window.isAskBeforeOverwrite()) {
						policy = new AskBeforeOverwriteCopyPolicy();
					} else if (window.isOverwriteIfNewer()) {
						policy = new OverwriteIfNewerCopyPolicy(FileSystemManager.getAbsolutePath(new File(path)));
					} else {
						policy = new AlwaysOverwriteCopyPolicy();
					}

					ProcessRunner rn = new ProcessRunner(target) {
						public void runCommand() throws ApplicationException {
							policy.setContext(context);

							ActionProxy.processRecoverOnTarget(
									rTarget,
									argument == null ? null : new ArecaRawFileList(((UIRecoveryFilter) argument).getFilter()), 
											policy,
											rPath,
											addSubdirectory,
											rFromDate, 
											recoverDeletedEntries,
											checkRecoveredFiles, 
											context);
						}

						protected void finishCommand() {
							showRecoveryResultWindow(context);
						}
					};
					rn.rProcess = process;
					rn.refreshAfterProcess = false;
					rn.rName = RM.getLabel("app.recoverfilesaction.process.message");
					rn.rPath = FileSystemManager.getAbsolutePath(new File(path));
					if (command.equals(CMD_RECOVER) || command.equals(CMD_RECOVER_WITH_FILTER)) {
						rn.rFromDate = getCurrentDate();
					}
					if (command.equals(CMD_RECOVER_WITH_FILTER) || command.equals(CMD_RECOVER_WITH_FILTER_LATEST)) {
						rn.argument = this.currentFilter;
					}
					rn.launch();
				}
			}
		} else if (command.equals(CMD_CHECK_ARCHIVES)) {
			// CHECK ARCHIVES
			CheckWindow window = new CheckWindow(this.getCurrentTarget());
			this.showDialog(window);
		} else if (command.equals(CMD_BUILD_BATCH)) {
			// BUILD BATCH
			buildBatch();
		} else if (command.equals(CMD_CHECK_VERSION)) {
			// CHECK VERSION
			checkVersion(true);
		} else if (command.equals(CMD_BUILD_STRATEGY)) {
			// BUILD STRATEGY
			buildStrategy();
		} else if (command.equals(CMD_SEARCH_LOGICAL)
				|| command.equals(CMD_SEARCH_PHYSICAL)) {
			SearchResultItem item = this.mainWindow.getSearchView()
					.getSelectedItem();

			this.enforceSelectedTarget(item.getTarget());
			this.setCurrentDates(item.getCalendar(), item.getCalendar());

			if (command.equals(CMD_SEARCH_PHYSICAL)) {
				// Archive detail
				this.showArchiveDetail(item.getEntry());
			} else {
				// Logical view
				this.showLogicalView(item.getEntry());
			}
		} else if (command.equals(CMD_COPY_FILENAMES)) {
			String[] filter = this.currentFilter.getFilter();
			StringBuffer cp = new StringBuffer();
			if (filter != null) {
				for (int i = 0; i < filter.length; i++) {
					cp.append(filter[i]).append(OSTool.getLineSeparator());
				}
			}
			copyString(cp.toString());
		} else if (command.equals(CMD_RECOVER_ENTRY_HISTO)
				|| command.equals(CMD_VIEW_FILE_AS_TEXT_HISTO)
				|| command.equals(CMD_VIEW_FILE_HISTO)
				|| command.equals(CMD_VIEW_FILE_AS_TEXT)
				|| command.equals(CMD_VIEW_FILE)) {
			// RECOVER ENTRY
			final String path;
			final boolean checkRecoveredFiles;
			if (command.equals(CMD_RECOVER_ENTRY_HISTO)) {
				RecoverWindow window = new RecoverWindow(false);
				this.showDialog(window);
				path = window.getLocation();
				checkRecoveredFiles = window.isCheckRecoveredFiles();
			} else {
				path = OSTool.getTempDirectory();
				checkRecoveredFiles = false;
			}

			if (path != null) {
				if (FileSystemTarget.class.isAssignableFrom(this
						.getCurrentObject().getClass())) {
					FileSystemTarget target = (FileSystemTarget) this
							.getCurrentObject();
					TargetGroup process = target.getParent();
					final AbstractCopyPolicy policy = new AskBeforeOverwriteCopyPolicy();
					ProcessRunner rn = new ProcessRunner(target) {
						private File recoveredFile;

						public void runCommand() throws ApplicationException {
							File entry = new File(path, rEntry.getKey());
							recoveredFile = new File(path,
									FileSystemManager.getName(entry));
							if (FileSystemManager.exists(recoveredFile)) {
								FileSystemManager.delete(recoveredFile);
							}
							ActionProxy.processRecoverOnTarget(
									rTarget, 
									rPath,
									rFromDate, 
									rEntry.getKey(),
									policy,
									checkRecoveredFiles, 
									context);
						}

						protected void finishCommand() {
							showRecoveryResultWindow(context);

							if (command.equals(CMD_VIEW_FILE_AS_TEXT_HISTO)
									|| command.equals(CMD_VIEW_FILE_HISTO)
									|| command.equals(CMD_VIEW_FILE_AS_TEXT)
									|| command.equals(CMD_VIEW_FILE)) {
								File entry = new File(path, rEntry.getKey());
								final File f = new File(path,
										FileSystemManager.getName(entry));
								FileSystemManager.deleteOnExit(f);

								if (command.equals(CMD_VIEW_FILE_AS_TEXT_HISTO)
										|| command
										.equals(CMD_VIEW_FILE_AS_TEXT)) {
									launchFileEditor(
											FileSystemManager
											.getAbsolutePath(f),
											true);
								} else {
									SecuredRunner.execute(new Runnable() {
										public void run() {
											try {
												ViewerHandlerHelper
												.getViewerHandler()
												.open(f);
											} catch (Throwable e) {
												if (ArecaUserPreferences.hasEditionCommand()) {
													Logger.defaultLogger().fine("No default viewer found for "+ FileSystemManager.getDisplayPath(f)+ ". Launching text viewer.");
													launchFileEditor(FileSystemManager.getAbsolutePath(f),true);
												} else {
													Application.instance.showErrorDialog(
															"An error occured while launching default viewer for "+ FileSystemManager.getDisplayPath(f),
															"Error viewing " + FileSystemManager.getDisplayPath(f),
															false);
													Logger.defaultLogger().error("Error viewing file "+ FileSystemManager.getDisplayPath(f)+ " : "+ e.getMessage());
												}
											}
										}
									});
								}
							}
						}
					};
					rn.rProcess = process;
					rn.refreshAfterProcess = false;
					rn.rEntry = this.currentEntry;
					rn.rName = RM
							.getLabel("app.recoverfileaction.process.message");
					rn.rPath = FileSystemManager
							.getAbsolutePath(new File(path));
					rn.rFromDate = command.equals(CMD_RECOVER_ENTRY_HISTO)
							|| command.equals(CMD_VIEW_FILE_AS_TEXT_HISTO)
							|| command.equals(CMD_VIEW_FILE_HISTO) ? this.currentEntryData
									.getManifest().getDate() : null;
									rn.launch();
				}
			}
		} else if (command.equals(CMD_VIEW_MANIFEST)) {
			this.showArchiveDetail(null);
		}
	}

	public ProcessRunner launchArchiveCheck(final CheckParameters checkParams, final AbstractTarget target, final CheckWindow window) {
			TargetGroup process = target.getParent();
			ProcessRunner rn = new ProcessRunner(target) {
			public void runCommand() throws ApplicationException {
				ActionProxy.processCheckOnTarget(rTarget, checkParams,
						rFromDate, context);
			}

			protected void finishCommand() {
				window.setResult(context.getReport().getInvalidRecoveredFiles(),
						context.getReport().getUncheckedRecoveredFiles(),
						context.getReport().getUnrecoveredFiles(), context.getReport().getNbChecked());
			}

			protected void finishCommandInError(Exception e) {
				window.closeInError(e);
			}
		};
		rn.rProcess = process;
		rn.refreshAfterProcess = false;
		rn.rName = RM.getLabel("app.checkfilesaction.process.message");
		rn.rFromDate = getCurrentDate();
		rn.launch();
		return rn;
	}

	/**
	 * Show the files with errors
	 */
	private void showRecoveryResultWindow(final ProcessContext context) {
		SecuredRunner.execute(new Runnable() {
			public void run() {
				if (context.getReport().hasRecoveryIssues()) {
					showWarningDialog(
							RM.getLabel("recover.check.invalid.label"),
							RM.getLabel("recover.check.result.title"), false);
				}
			}
		});
	}

	public void copyString(String s) {
		TextTransfer textTransfer = TextTransfer.getInstance();
		clipboard.setContents(new Object[] { s },
				new Transfer[] { textTransfer });
	}

	public void showEditTarget(AbstractTarget target) {
		TargetEditionWindow frmEdit = new TargetEditionWindow(target);
		showDialog(frmEdit);
		AbstractTarget newTarget = frmEdit.getTargetIfValidated();
		if (newTarget != null) {
			this.getCurrentTargetGroup().linkChild(newTarget);
			this.currentObject = newTarget;
			try {
				if (target == null) {
					ConfigurationListener.getInstance().targetCreated(newTarget, workspace.getPathFile());
				} else {
					ConfigurationListener.getInstance().targetModified(newTarget, workspace.getPathFile());
				}
			} catch (Exception e) {
				handleException(e);
			}
			this.mainWindow.refresh(true, true);
		}
	}

	/**
	 * Try to open the file with the system editor.
	 * <br>Switch to the configured text editor in case of failure
	 * @param path
	 */
	public void secureOpenFile(String path) {
		try {
			ViewerHandlerHelper.getViewerHandler().open(new File(path));
		} catch (Exception e) {
			launchFileEditor(path, true);
		}
	}

	public void launchFileEditor(String path, boolean async) {
		path = path.replace('\\', '/');
		String editCommand = ArecaUserPreferences.getEditionCommand();
		try {
			Logger.defaultLogger().info("Launching '" + editCommand + "' on file '" + path + "'");
			String[] cmd;
			if (editCommand.indexOf(TEXT_EDITOR_PLACE_HOLDER) != -1) {
				String replaced = Util.replace(editCommand, TEXT_EDITOR_PLACE_HOLDER, "\"" + path + "\"") + " ";
				String token = "";
				ArrayList list = new ArrayList();
				boolean inQuote = false;
				for (int i=0; i<replaced.length(); i++) {
					if (replaced.charAt(i) == '\"') {
						inQuote = ! inQuote;
					} else if (replaced.charAt(i) == ' ' && ! inQuote) {
						if (token.length() != 0) {
							list.add(token);
							token = "";
						}
					} else {
						token += replaced.charAt(i);
					}
				}
				cmd = (String[])list.toArray(new String[list.size()]);
			} else {
				cmd = new String[] {editCommand, path};
			}

			OSTool.execute(cmd, async);
		} catch (Exception e) {
			Application.getInstance().handleException("Error attempting to edit " + path + " - Text editor = " + editCommand, e);
		}
	}

	public void showEditTargetXML(final AbstractTarget target) {
		if (target != null) {
			Runnable rn = new Runnable() {
				public void run() {
					try {
						File configFile = ConfigurationListener.getInstance().ensureConfigurationFileAvailability(target,workspace.getPathFile());
						String path = FileSystemManager.getAbsolutePath(configFile);
						launchFileEditor(path, false);
					} catch (Exception e) {
						Application.getInstance().handleException(e);
					}
				}
			};

			Thread th = new Thread(rn);
			th.setDaemon(true);
			th.setName("Target XML edition");
			th.start();
		}
	}

	public void showEditGroup() {
		GroupCreationWindow frmEdit = new GroupCreationWindow();
		showDialog(frmEdit);
		TargetGroup newGroup = frmEdit.getGroup();
		if (newGroup != null) {
			this.getCurrentTargetGroup().linkChild(newGroup);
			this.currentObject = newGroup;
			try {
				ConfigurationListener.getInstance().groupCreated(newGroup,
						workspace.getPathFile());
			} catch (Exception e) {
				handleException(e);
			}
			this.mainWindow.refresh(true, true);
		}
	}

	public void refreshWorkspace() {
		this.openWorkspaceImpl(this.workspace.getPath());
	}

	public void openWorkspace(String path) {
		if (path != null && ((this.workspace == null) || (! path.equals(this.workspace.getPath())))) {
			this.openWorkspaceImpl(path);
		}
	}

	private void openWorkspaceImpl(String path) {
		if (path != null) {
			try {
				enableWaitCursor();
				Workspace w = Workspace.open(
						FileSystemManager.getAbsolutePath(new File(path)),
						this, true);

				Stack s = ArecaUserPreferences.getWorkspaceHistory();
				String normalizedPath = FileNameUtil.normalizePath(path);
				if (!s.contains(normalizedPath)) {
					s.add(0, normalizedPath);
				}
				while (s.size() > ArecaUserPreferences.MAX_HISTORY_SIZE) {
					s.remove(s.size() - 1);
				}
				ArecaUserPreferences.setWorkspaceHistory(s);

				this.setWorkspace(w, true);
			} catch (AdapterException e) {
				Logger.defaultLogger().error(
						"Error detected in " + e.getSource());
				this.handleException(RM.getLabel("error.loadworkspace.message",
						new Object[] { e.getMessage(), e.getSource() }), e);
			} catch (Throwable e) {
				this.handleException(RM.getLabel("error.loadworkspace.message",
						new Object[] { e.getMessage(), path }), e);
			} finally {
				disableWaitCursor();
			}
		}
	}

	public void checkVersion(final boolean explicit) {
		if ((explicit || ArecaUserPreferences.isCheckNewVersions())) {
			Runnable rn = new Runnable() {
				public void run() {
					try {
						Logger.defaultLogger().info(
								"Checking new version of " + VersionInfos.APP_SHORT_NAME + " ...");
						final VersionData data = VersionChecker.getInstance()
								.checkForNewVersion();
						VersionData currentVersion = VersionInfos
								.getLastVersion();

						if (currentVersion.equals(data)) {
							Logger.defaultLogger().info(
									"No new version found : v"
											+ data.getVersionId()
											+ " is the latest version.");
							if (explicit) {
								SecuredRunner.execute(new Runnable() {
									public void run() {
										NewVersionWindow win = new NewVersionWindow(
												RM.getLabel(
														"common.versionok.message",
														new Object[] {
																data.getVersionId(),
																VersionInfos
																.formatVersionDate(data
																		.getVersionDate()),
																		data.getDownloadUrl(),
																		data.getDescription() }),
																		false);
										showDialog(win);
									}
								});
							}
						} else {
							Logger.defaultLogger().info(
									"New version found : " + data.toString());
							SecuredRunner.execute(new Runnable() {
								public void run() {
									NewVersionWindow win = new NewVersionWindow(
											RM.getLabel(
													"common.newversion.message",
													new Object[] {
															data.getVersionId(),
															VersionInfos
															.formatVersionDate(data
																	.getVersionDate()),
																	data.getDownloadUrl(),
																	data.getDescription() }),
																	true);
									showDialog(win);
									if (win.isValidated()) {
										try {
											ViewerHandlerHelper
											.getViewerHandler()
											.browse(data
													.getDownloadUrl());
										} catch (IOException e1) {
											Logger.defaultLogger().error(e1);
										} catch (NoBrowserFoundException e1) {
											Logger.defaultLogger()
											.error("Error connecting to : "
													+ data.getDownloadUrl()
													+ " - No web browser could be found.",
													e1);
										}

										Application.this.processExit();
									}
								}
							});
						}
					} catch (Throwable e) {
						handleException(
								"An error occurred during " + VersionInfos.APP_SHORT_NAME + "'s version verification : "
										+ e.getMessage(), e);
					}
				}
			};

			Thread th = new Thread(rn);
			th.start();
		}
	}

	public void buildStrategy() {
		// Dialog
		String prefix = this.getCurrentTarget().getUid() + "_every_";

		BackupStrategyWizardWindow win = new BackupStrategyWizardWindow(OSTool.getUserHome());
		showDialog(win);

		String path = win.getSelectedPath();
		boolean check = true; // not used yet ...

		if (path != null && win.getTimes() != null && win.getTimes().size() != 0) {
			String files = "";

			// Init
			String commentPrefix;
			String commandPrefix;
			String extension;
			String precommands = "";
			File executable = Utils.buildExecutableFile();
			if (OSTool.isSystemWindows()) {
				extension = ".bat";
				commentPrefix = "@REM ";
				commandPrefix = "@";
				if (OSTool.getCodePage() != -1) {
					precommands = "@chcp " + OSTool.getCodePage() + "\n";
				}
			} else {
				extension = ".sh";
				commentPrefix = "# ";
				commandPrefix = "";
			}
			String content = commentPrefix + "Script generated by " + VersionInfos.APP_SHORT_NAME + " v"
					+ VersionInfos.getLastVersion().getVersionId() + " on "
					+ Utils.formatDisplayDate(new GregorianCalendar()) + "\n\n";

			content += commentPrefix + "Target Group : \""
					+ this.getCurrentTargetGroup().getName() + "\"\n";
			content += commentPrefix + "Target : \""
					+ this.getCurrentTarget().getName() + "\"\n\n";

			content += precommands;

			File config = this.getCurrentTarget().computeConfigurationFile(new File(workspace.getPath()), true);
			String configPath = FileSystemManager.getAbsolutePath(config);
			String command = commandPrefix + "\""
					+ FileSystemManager.getAbsolutePath(executable)
					+ "\" merge -c -config \"" + configPath + "\"";

			// Script generation
			List parameters = win.getTimes();
			int unit = 1;
			for (int i = 0; i < parameters.size(); i++) {
				int repetition = ((Integer) parameters.get(i)).intValue();
				String fileName = prefix + unit + "_days" + extension;
				String fileContent = content;
				fileContent += commentPrefix + "This script must be run every ";
				if (unit == 1) {
					fileContent += "day.\n";
				} else {
					fileContent += unit + " days.\n";
				}

				if (i == 0) {
					// Backup
					fileContent += "\n" + commentPrefix + "Daily backup\n";

					String strCheck = "";
					if (check) {
						strCheck = "-c -wdir \"" + getWorkingDirectoryWithoutTrailingSlash() + "\" ";
					}

					fileContent += commandPrefix + "\""
							+ FileSystemManager.getAbsolutePath(executable)
							+ "\" backup " + strCheck + "-config \""
							+ configPath + "\"\n";
					unit *= repetition;
				} else {
					// Intermediate merge
					int to = unit;
					int from = 2 * unit;
					fileContent += "\n" + commentPrefix + "Merge between day " + to + " and day " + from + " \n";
					fileContent += command + " -from " + from + " -to " + to+ "\n";
					unit *= repetition + 1;
				}

				// Final merge
				if (i == parameters.size() - 1) {
					fileContent += "\n" + commentPrefix + "Merge after day "+ unit + " \n";
					fileContent += command + " -delay " + unit + "\n";
				}

				// File generation
				File tgFile = new File(path, fileName);
				files += "\n- " + FileSystemManager.getName(tgFile);
				buildExecutableFile(tgFile, fileContent);
			}

			this.showInformationDialog(
					RM.getLabel("shrtc.confirm.message", new Object[] { path,
							files }), RM.getLabel("shrtc.confirm.title"), false);
		}
	}

	public void buildBatch() {
		String fileNameSelected = "backup_";
		String fileNameAll = "backup";
		if (this.isCurrentObjectTarget()) {
			fileNameSelected += this.getCurrentTarget().getUid();
		} else {
			fileNameSelected += this.getCurrentTargetGroup().getName()
					.toLowerCase().replace(' ', '_');
		}
		String commentPrefix;
		String commandPrefix;
		String precommands = "";
		if (OSTool.isSystemWindows()) {
			fileNameSelected += ".bat";
			fileNameAll += ".bat";
			commentPrefix = "@REM ";
			commandPrefix = "@";
			if (OSTool.getCodePage() != -1) {
				precommands = "@chcp " + OSTool.getCodePage() + "\n";
			}
		} else {
			fileNameSelected += ".sh";
			fileNameAll += ".sh";
			commentPrefix = "# ";
			commandPrefix = "";
		}

		BackupShortcutWizardWindow win = new BackupShortcutWizardWindow(OSTool.getUserHome(), fileNameSelected, fileNameAll);
		showDialog(win);
		String path = win.getSelectedPath();
		boolean forSelectedOnly = win.isForSelectedOnly();
		boolean full = win.isFull();
		boolean check = win.isCheckArchive();
		boolean differential = win.isDifferential();

		if (path != null) {
			String content = commentPrefix
					+ "Backup script generated by " + VersionInfos.APP_SHORT_NAME + " v"
					+ VersionInfos.getLastVersion().getVersionId() + " on "
					+ Utils.formatDisplayDate(new GregorianCalendar()) + "\n\n";
			File executable = Utils.buildExecutableFile();

			content += precommands;

			if (forSelectedOnly) {
				content += generateShortcutScript(executable,
						this.getCurrentTargetGroup(),
						isCurrentObjectTarget() ? getCurrentTarget() : null,
								commentPrefix, commandPrefix, check, full, differential);
			} else {
				Iterator iter = this.workspace.getIterator();
				while (iter.hasNext()) {
					AbstractWorkspaceItem item = (AbstractWorkspaceItem) iter.next();
					if (item instanceof TargetGroup) {
						content += generateShortcutScript(executable, (TargetGroup)item,
								null, commentPrefix, commandPrefix, check, full,
								differential);
					} else {
						FileSystemTarget target = (FileSystemTarget)item;
						content += generateShortcutScript(executable, target.getParent(),
								target, commentPrefix, commandPrefix, check, full,
								differential);
					}
				}
			}

			buildExecutableFile(new File(path), content);
		}
	}

	private void buildExecutableFile(File path, String content) {
		try {
			this.fileTool.createFile(path, content);
			String strTgFile = FileSystemManager.getAbsolutePath(path);
			Logger.defaultLogger().info("Creating shell script : " + strTgFile);
			if (!OSTool.isSystemWindows()) {
				String[] chmod = new String[] { "chmod", "750", strTgFile };
				OSTool.execute(chmod);
			}
		} catch (Throwable e) {
			handleException("Error during command file creation", e);
		}
	}

	private String generateShortcutScript(File executable, TargetGroup targetGroup,
			AbstractTarget target, String commentPrefix, String commandPrefix,
			boolean check, boolean full, boolean differential) {
		String type = full ? AbstractTarget.BACKUP_SCHEME_FULL: (differential ? AbstractTarget.BACKUP_SCHEME_DIFFERENTIAL: AbstractTarget.BACKUP_SCHEME_INCREMENTAL);

		String comments = commentPrefix + type + "\n" + commentPrefix + "Target Group : \"" + targetGroup.getName() + "\"\n";
		File config = new File(workspace.getPath(), targetGroup.getFullPath());
		if (target != null) {
			config = target.computeConfigurationFile(config, false);
		}

		String command = "backup ";
		if (full) {
			command += "-f ";
		} else if (differential) {
			command += "-d ";
		}
		if (check) {
			command += "-c -wdir \"" + getWorkingDirectoryWithoutTrailingSlash() + "\" ";
		}

		command += "-config \"" + FileSystemManager.getAbsolutePath(config) + "\"";
		if (target != null) {
			comments += commentPrefix + "Target : \"" + target.getName()+ "\"\n";
		}

		command = commandPrefix + "\""+ FileSystemManager.getAbsolutePath(executable) + "\" "+ command;

		return comments + command + "\n\n";
	}

	private static String getWorkingDirectoryWithoutTrailingSlash() {
		String wdir = OSTool.getTempDirectory();
		if (wdir.endsWith("\\")) {
			wdir = wdir.substring(0, wdir.length()-1);
		}
		return wdir;
	}

	public void createWorkspaceCopy(File root, boolean removeEncryptionData) {
		String removeStr = removeEncryptionData ? " (Encryption data will be removed)" : "";
		Logger.defaultLogger().info("Creating a backup copy of current workspace (" + this.workspace.getPath() + ") in " + FileSystemManager.getDisplayPath(root) + removeStr);
		try {
			if (this.workspace != null) {
				if (!FileSystemManager.exists(root)) {
					fileTool.createDir(root);
				}

				Logger.defaultLogger().info("Creating a backup copy of \"" + workspace.getPath() + "\" : " + FileSystemManager.getDisplayPath(root));
				ConfigurationHandler.getInstance().serialize(workspace, root, removeEncryptionData, true);
			}

			Logger.defaultLogger().info("Backup copy of " + this.workspace.getPath()+ " successfully created.");
		} catch (Throwable e) {
			handleException(RM.getLabel("error.cpws.message"), e);
		}
	}

	public void clearLog() {
		Logger.defaultLogger().clearLog(LogComposite.class);
	}

	private void duplicateTarget(AbstractTarget target)
			throws ApplicationException {
		try {
			AbstractTarget clone = (AbstractTarget) target.duplicate();
			this.getCurrentTargetGroup().linkChild(clone);
			clone.getMedium().install();

			ConfigurationListener.getInstance().targetCreated(clone,
					workspace.getPathFile());
			this.setCurrentObject(clone, true);
			this.mainWindow.refresh(true, true);
		} catch (Exception e) {
			handleException(e);
		}
	}

	public void showDeleteItem() {
		DeleteWindow window;
		WorkspaceItem item;
		if (isCurrentObjectTarget()) {
			window = new DeleteWindow(this.getCurrentTarget());
			item = this.getCurrentTarget();
		} else {
			window = new DeleteWindow(this.getCurrentTargetGroup());
			item = this.getCurrentTargetGroup();
		}
		showDialog(window);

		if (window.isOk()) {
			try {
				if (window.isDeleteContent()) {
					item.destroyRepository();
				}

				item.getParent().remove(item.getUid());
				ConfigurationListener.getInstance().itemDeleted(this.getCurrentWorkspaceItem(),workspace.getPathFile());

				this.currentObject = null;
				this.mainWindow.refresh(true, true);
			} catch (Exception e) {
				handleException(e);
			}
		}
	}

	public void showArchiveDetail(TraceEntry entry) {
		this.enableWaitCursor();

		// VIEW ARCHIVE DETAIL
		FileSystemTarget target = (FileSystemTarget) this.getCurrentObject();

		try {
			AbstractIncrementalFileSystemMedium fsMedium = (AbstractIncrementalFileSystemMedium) target
					.getMedium();
			File archive = fsMedium.getLastArchive(null, currentFromDate);
			Manifest mf = ArchiveManifestCache.getInstance().getManifest(
					fsMedium, archive);

			if (mf == null) {
				mf = new Manifest(Manifest.TYPE_BACKUP);
				mf.setDate(currentFromDate);
			}

			mf.addProperty(ManifestKeys.CURRENT_ARCHIVE_PATH,FileSystemManager.getDisplayPath(archive));
			ArchiveWindow frm = new ArchiveWindow(mf, currentFromDate,target.getMedium());

			frm.setCurrentEntry(entry);

			showDialog(frm);
		} catch (Exception e1) {
			this.handleException(RM.getLabel("error.archiveloading.message",
					new Object[] { e1.getMessage() }), e1);
		} finally {
			this.disableWaitCursor();
		}
	}

	public void showLogicalView(TraceEntry entry) {
		this.mainWindow.focusOnLogicalView(entry);
	}

	/**
	 * Tells whether the virtual machine must be killed or whether current non
	 * daemon threads must be kept alive.
	 */
	public boolean processExit() {
		this.mainWindow.savePreferences();
		if (this.channels.size() == 0) {
			return this.mainWindow.close(true);
		} else {
			int result = showConfirmDialog(
					RM.getLabel("appdialog.confirmexit.message"),
					RM.getLabel("appdialog.confirmexit.title"), SWT.YES
					| SWT.NO | SWT.CANCEL);

			if (result == SWT.YES) {
				// on ferme la fenetre
				return this.mainWindow.close(true);
			} else if (result == SWT.NO) {
				// Kill violent
				Launcher.getInstance().exit(true);
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Find a supported backup scheme for the target.
	 */
	private String resolveBackupScheme(AbstractTarget target,
			String backupScheme) {
		if (target.getSupportedBackupSchemes().isSupported(backupScheme)) {
			return backupScheme;
		} else if (AbstractTarget.BACKUP_SCHEME_INCREMENTAL
				.equals(backupScheme)) {
			return resolveBackupScheme(target,
					AbstractTarget.BACKUP_SCHEME_DIFFERENTIAL);
		} else if (AbstractTarget.BACKUP_SCHEME_DIFFERENTIAL
				.equals(backupScheme)) {
			return resolveBackupScheme(target,
					AbstractTarget.BACKUP_SCHEME_FULL);
		} else {
			throw new IllegalStateException(
					"Unable to resolve backup scheme for target "
							+ target.getName());
		}
	}

	public void launchBackupOnTarget(AbstractTarget target, Manifest manifest, String backupScheme, final CheckParameters checkParams) {
		TargetGroup process = target.getParent();
		final String resolvedBackupScheme = resolveBackupScheme(target, backupScheme);
		ProcessRunner rn = new ProcessRunner(target) {
			public void runCommand() throws ApplicationException {
				ActionProxy.processBackupOnTarget(rTarget, rManifest,
						resolvedBackupScheme, checkParams,
						new GUITransactionHandler(), context);
			}

			protected void finishCommandInError(Exception e) {
				finishCommand();
			}

			protected void finishCommand() {
				if (ReportingConfiguration.getInstance().isReportingEnabled()) {
					SecuredRunner.execute(new Runnable() {
						public void run() {
							ReportWindow frm = new ReportWindow(context.getReport());
							showDialog(frm);
						}
					});
				}
			}
		};
		rn.rProcess = process;
		rn.rManifest = manifest;
		rn.rName = RM.getLabel("app.backupaction.process.message");
		rn.launch();
	}

	public void launchBackupOnGroup(TargetGroup group, Manifest mf,
			String backupScheme, CheckParameters checkParams) {
		Iterator iter = group.getIterator();
		while (iter.hasNext()) {
			WorkspaceItem item = (WorkspaceItem) iter.next();

			if (item instanceof TargetGroup) {
				this.launchBackupOnGroup((TargetGroup) item, mf, backupScheme,
						checkParams);
			} else {
				Manifest clone = mf == null ? null : (Manifest) mf.duplicate();
				this.launchBackupOnTarget((AbstractTarget) item, clone,
						backupScheme, checkParams);
			}
		}
	}

	public void launchMergeOnTarget(
			final MergeParameters params,
			final CheckParameters checkParams,
			Manifest manifest) {
		// MERGE
		FileSystemTarget target = (FileSystemTarget) this.getCurrentObject();
		TargetGroup process = target.getParent();
		ProcessRunner rn = new ProcessRunner(target) {
			public void runCommand() throws ApplicationException {
				ActionProxy.processMergeOnTarget(rTarget, rFromDate, rToDate,
						rManifest, params, checkParams, context);
			}
		};
		rn.rProcess = process;
		rn.rFromDate = currentFromDate;
		rn.rName = RM.getLabel("app.mergearchivesaction.process.message");
		rn.rToDate = currentToDate;
		rn.rManifest = manifest;
		rn.launch();
	}

	public void showBackupWindow(Manifest manifest, WorkspaceItem scope) {
		BackupWindow frm = new BackupWindow(manifest, scope);
		showDialog(frm);
	}

	public void showMergeWindow(AbstractTarget target, Manifest manifest) {
		MergeWindow frm = new MergeWindow(manifest, target);
		showDialog(frm);
	}

	public void showWebPage(String location) {
		try {
			URL url = new URL(location);
			ViewerHandlerHelper.getViewerHandler().browse(url);
		} catch (Exception e) {
			handleException(e);
		}
	}

	public void showDialog(final AbstractWindow window, boolean block) {
		try {
			window.setModal(getMainWindow());
			window.setBlockOnOpen(block);
			window.open();
		} catch (Exception e) {
			handleException(e);
		}
	}

	public void showDialog(final AbstractWindow window) {
		showDialog(window, true);
	}

	public void handleException(final String msg, final Throwable e) {
		SecuredRunner.execute(new Runnable() {
			public void run() {
				disableWaitCursor();

				if (e != null) {
					if (!(e instanceof ApplicationException)) {
						Logger.defaultLogger().error(e); // Unexpected exception
						// ... that may not
						// have been logged.
					}
					e.printStackTrace(System.err);
				}

				showErrorDialog(
						msg,
						ResourceManager.instance().getLabel(
								"error.dialog.title"), false);
			}
		});
	}

	public void handleException(Throwable e) {
		FileLogProcessor processor = (FileLogProcessor) Logger.defaultLogger().find(FileLogProcessor.class);
		String logFile = "<null>";
		if (processor != null) {
			logFile = processor.getCurrentLogFile();
		}
		handleException(RM.getLabel("error.process.message", new Object[] {getExceptionMessage(e), logFile }), e);
	}

	private String getExceptionMessage(Throwable e) {
		if (e.getMessage() == null || e.getMessage().trim().length() == 0) {
			return "Unexpected error";
		} else {
			String ret = e.getMessage();

			try {
				int idxExcep = ret.indexOf("Exception");
				int l = "Exception".length();
				if (idxExcep == -1) {
					idxExcep = ret.indexOf("Error");
					l = "Error".length();
				}
				
				if (idxExcep != -1) {
					int idxSpace = ret.indexOf(' ');
					if (idxSpace == -1 || idxSpace > idxExcep) {
						int idxDot = ret.indexOf('.');
						if (idxDot < idxExcep) {
							ret = ret.substring(idxExcep + l).trim();
							if (ret.length() != 0) {
								int i=0;
								while (i<ret.length()) {
									char c = ret.charAt(i);
									if (c != ' ' && c != ':' && c != ',' && c != ';' && c != '.' && c != ',' && c != '-' && c != '_') {
										break;
									}
									i++;
								}
								if (i == ret.length()) {
									ret = "";
								} else {
									ret = ret.substring(i);
								}
							}
						}
					}
				}
			} catch (Throwable ignored) {
				ret = e.getMessage();
			}
			return ret;
		}
	}

	public TargetGroup getCurrentTargetGroup() {
		TargetGroup defaultGroup = workspace.getContent();
		if (this.currentObject == null) {
			return defaultGroup;
		}

		if (TargetGroup.class.isAssignableFrom(this.currentObject.getClass())) {
			return (TargetGroup) this.currentObject;
		} else if (AbstractTarget.class.isAssignableFrom(this.currentObject
				.getClass())) {
			return ((AbstractTarget) this.currentObject).getParent();
		} else {
			return defaultGroup;
		}
	}

	public WorkspaceItem getCurrentWorkspaceItem() {
		return (WorkspaceItem) this.currentObject;
	}

	public AbstractTarget getCurrentTarget() {
		return (AbstractTarget) this.currentObject;
	}

	public boolean isCurrentObjectTargetGroup() {
		return (currentObject != null && TargetGroup.class
				.isAssignableFrom(currentObject.getClass()));
	}

	public boolean isCurrentObjectTarget() {
		return (currentObject != null && FileSystemTarget.class
				.isAssignableFrom(currentObject.getClass()));
	}

	public void setCurrentEntry(TraceEntry currentEntry) {
		this.currentEntry = currentEntry;
		AppActionReferenceHolder.refresh();
	}

	public TraceEntry getCurrentEntry() {
		return currentEntry;
	}

	public void setCurrentFilter(UIRecoveryFilter argCurrentFilter) {
		if (argCurrentFilter != null && argCurrentFilter.getFilter() != null) {
			for (int i = 0; i < argCurrentFilter.getFilter().length; i++) {
				if (argCurrentFilter.getFilter()[i].equals("/")
						|| argCurrentFilter.getFilter()[i].equals("\\")) {
					argCurrentFilter.setFilter(null);
					break;
				}
			}
		}

		this.currentFilter = argCurrentFilter;
		AppActionReferenceHolder.refresh();
	}

	public GregorianCalendar getCurrentFromDate() {
		return currentFromDate;
	}

	public EntryArchiveData getCurrentEntryData() {
		return currentEntryData;
	}

	public void setCurrentEntryData(EntryArchiveData currentEntryData) {
		this.currentEntryData = currentEntryData;
		AppActionReferenceHolder.refresh();
	}

	public GregorianCalendar getCurrentHistoryDate() {
		if (currentEntryData == null || currentEntryData.getManifest() == null) {
			return null;
		} else {
			return currentEntryData.getManifest().getDate();
		}
	}

	public WorkspaceItem getCurrentObject() {
		return currentObject;
	}

	public void setCurrentObject(WorkspaceItem currentObject,
			boolean refreshTree) {
		if (this.currentObject != currentObject) { // Yes, we DO use reference
			// comparison
			this.enableWaitCursor();
			this.currentObject = currentObject;

			this.resetCurrentDates();
			if (this.mainWindow != null) {
				this.mainWindow.refresh(refreshTree, true);
			}
			this.disableWaitCursor();
		}
	}

	public GregorianCalendar getCurrentToDate() {
		return currentToDate;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Workspace workspace, boolean refreshInterface) {
		this.workspace = workspace;

		this.currentEntry = null;
		this.currentFilter = null;
		this.currentFromDate = null;
		this.currentEntryData = null;
		this.currentObject = null;
		this.currentToDate = null;

		// Refresh the gui
		if (refreshInterface) {
			this.mainWindow.refresh(true, true);
		}

		ArecaUserPreferences.setLastWorkspace(workspace.getPath());
	}

	public void enableWaitCursor(final AbstractWindow window) {
		SecuredRunner.execute(window, new Runnable() {
			public void run() {
				if (window != null) {
					window.getShell().setCursor(CURSOR_WAIT);
				}
			}
		});
	}

	public void disableWaitCursor(final AbstractWindow window) {
		SecuredRunner.execute(window, new Runnable() {
			public void run() {
				if (window != null && window.getShell() != null) {
					window.getShell().setCursor(null);
				}
			}
		});
	}

	public void enableWaitCursor() {
		enableWaitCursor(mainWindow);
	}

	public void disableWaitCursor() {
		disableWaitCursor(mainWindow);
	}

	public void showInformationDialog(String message, String title,
			boolean longMessage) {
		showDialog(message, title, true, SWT.ICON_INFORMATION, longMessage);
	}

	public void showWarningDialog(String message, String title,
			boolean longMessage) {
		showDialog(message, title, true, SWT.ICON_WARNING, longMessage);
	}

	public void showErrorDialog(String message, String title,
			boolean longMessage) {
		showDialog(message, title, true, SWT.ICON_ERROR, longMessage);
	}

	public int showConfirmDialog(final String message, final String title,
			final int buttons) {
		MessageBox msg = new MessageBox(Application.this.mainWindow.getShell(),
				buttons | SWT.ICON_QUESTION);
		msg.setText(title);
		msg.setMessage(message);
		return msg.open();
	}

	public int showConfirmDialog(String message, String title) {
		return showConfirmDialog(message, title, SWT.YES | SWT.NO);
	}

	private int showDialog(String message, String title, boolean closeOnly,
			int type, boolean longMessage) {
		if (mainWindow != null) {
			if (longMessage) {
				LongMessageWindow msg = new LongMessageWindow(title, message,
						closeOnly, type);
				showDialog(msg);
				if (msg.isValidated()) {
					return SWT.YES;
				} else {
					return SWT.NO;
				}
			} else {
				MessageBox msg = new MessageBox(this.mainWindow.getShell(),
						SWT.OK | type);
				msg.setText(title);
				msg.setMessage(message);
				return msg.open();
			}
		} else {
			return SWT.OK;
		}
	}

	public void resetCurrentDates() {
		this.currentToDate = null;
		this.currentFromDate = null;
	}

	public MainWindow getMainWindow() {
		return mainWindow;
	}

	public GregorianCalendar getCurrentDate() {
		if (this.getCurrentFromDate() != null
				&& this.getCurrentToDate() != null
				&& this.getCurrentFromDate().equals(this.getCurrentToDate())) {
			return this.getCurrentFromDate();
		} else {
			return null;
		}
	}

	public boolean areMultipleDatesSelected() {
		if (this.getCurrentFromDate() != null
				&& this.getCurrentToDate() != null
				&& (!this.getCurrentFromDate().equals(this.getCurrentToDate()))) {
			return true;
		} else {
			return false;
		}
	}

	public String showDirectoryDialog(AbstractWindow parent) {
		return showDirectoryDialog(OSTool.getUserDir(), parent);
	}

	public String showDirectoryDialog(String dir, AbstractWindow parent) {
		DirectoryDialog fileChooser = new DirectoryDialog(parent.getShell(),
				SWT.OPEN);
		if (dir != null) {
			fileChooser.setFilterPath(dir);
		}
		fileChooser.setText(RM.getLabel("common.choosedirectory.title"));
		fileChooser.setMessage(RM.getLabel("common.choosedirectory.message"));

		return fileChooser.open();
	}

	public String showFileDialog(String dir, AbstractWindow parent,
			String fileName, String title, int style) {
		FileDialog fileChooser = new FileDialog(parent.getShell(), style);
		if (dir != null) {
			fileChooser.setFilterPath(dir);
		}
		if (title != null) {
			fileChooser.setText(title);
		} else {
			fileChooser.setText(RM.getLabel("common.choosefile.title"));
		}
		if (fileName != null) {
			fileChooser.setFileName(fileName);
		}
		return fileChooser.open();
	}

	public static void setTabLabel(CTabItem item, String label) {
		if (item.getImage() != null) {
			item.setText(label + "  ");
		} else {
			item.setText(" " + label + " ");
		}
	}

	public String showFileDialog(AbstractWindow parent) {
		return showFileDialog(OSTool.getUserDir(), parent);
	}

	public String showFileDialog(String dir, AbstractWindow parent) {
		return showFileDialog(dir, parent, null, null, SWT.OPEN);
	}

	public void addChannel(UserInformationChannel channel) {
		this.channels.add(channel);
	}

	public void removeChannel(UserInformationChannel channel) {
		this.channels.remove(channel);
	}

	public void enforceSelectedTarget(AbstractTarget target) {
		this.setCurrentObject(target, false);
		this.mainWindow.enforceSelectedTarget(target);
	}

	public void setCurrentDates(GregorianCalendar currentFromDate,
			GregorianCalendar currentToDate) {
		this.currentFromDate = currentFromDate;
		this.currentToDate = currentToDate;
		AppActionReferenceHolder.refresh();
	}

	public abstract class ProcessRunner implements Runnable {
		protected TargetGroup rProcess;
		protected String rName;
		protected AbstractTarget rTarget;
		protected String rPath;
		protected GregorianCalendar rFromDate;
		protected GregorianCalendar rToDate;
		protected Manifest rManifest;
		protected TraceEntry rEntry;
		protected boolean refreshAfterProcess = true;
		protected ProcessContext context;
		protected Object argument;
		protected GUIInformationChannel channel;

		public abstract void runCommand() throws ApplicationException;

		public ProcessRunner(AbstractTarget target) {
			this.rTarget = target;
			channel = new GUIInformationChannel(rTarget, mainWindow.getProgressContainer().getMainPane());
			mainWindow.focusOnProgress();
		}

		public GUIInformationChannel getChannel() {
			return channel;
		}

		// Called in the AWT event thread, to update GUI after the command
		// execution
		protected void finishCommand() {
		}

		// Called in the AWT event thread, to update GUI after the command
		// execution
		protected void finishCommandInError(Exception e) {
		}

		public void run() {
			addChannel(channel);
			channel.setAction(rName);

			try {
				String taskName = "Unnamed-Task";
				if (rTarget != null) {
					taskName = rTarget.getName();
				}
				this.context = new ProcessContext(rTarget, channel, new TaskMonitor(taskName));

				// Activate message tracking for current thread.
				this.context.getReport().setLogMessagesContainer(Logger.defaultLogger().getTlLogProcessor().activateMessageTracking());

				channel.startRunning();
				registerState(true);
				AppActionReferenceHolder.refresh();
				runCommand();
				registerState(false);
				if (refreshAfterProcess) {
					SecuredRunner.execute(mainWindow, new Runnable() {
						public void run() {
							mainWindow.refresh(false, false);
						}
					});
				}
				finishCommand();
			} catch (Exception e) {
				registerState(false);
				try {
					finishCommandInError(e);
				} finally {
					try {
						if (refreshAfterProcess) {
							SecuredRunner.execute(mainWindow, new Runnable() {
								public void run() {
									mainWindow.refresh(false, false);
								}
							});
						}
					} finally {
						if (!TaskCancelledException.isTaskCancellation(e)) {
							handleException(e);
						} else {
							channel.print(RM
									.getLabel("common.processcancelled.label"));
							context.getTaskMonitor().enforceCompletion();
						}
					}
				}
			} finally {
				channel.stopRunning();
				removeChannel(channel);
				registerState(false); // Enforce menu refresh
				AppActionReferenceHolder.refresh(); // Enforce menu refresh
			}
		}

		private void registerState(boolean running) {
			rTarget.setRunning(running);
		}

		public void launch() {
			Thread th = new Thread(this);
			th.setName("Command Runner : [" + rName + "]");

			// The thread shall stop normally
			th.setDaemon(false);
			th.start();
		}
	}
}
