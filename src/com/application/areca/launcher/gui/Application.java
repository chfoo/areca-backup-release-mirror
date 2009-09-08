package com.application.areca.launcher.gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import com.application.areca.ApplicationException;
import com.application.areca.ArchiveMedium;
import com.application.areca.ArecaURLs;
import com.application.areca.EntryArchiveData;
import com.application.areca.EntryStatus;
import com.application.areca.Identifiable;
import com.application.areca.ResourceManager;
import com.application.areca.SimulationResult;
import com.application.areca.TargetGroup;
import com.application.areca.UserInformationChannel;
import com.application.areca.Utils;
import com.application.areca.adapters.AdapterException;
import com.application.areca.adapters.ProcessXMLWriter;
import com.application.areca.cache.ArchiveManifestCache;
import com.application.areca.context.ProcessContext;
import com.application.areca.context.ReportingConfiguration;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.impl.FileSystemTarget;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ActionConstants;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.launcher.gui.common.ArecaPreferences;
import com.application.areca.launcher.gui.common.CTabFolderManager;
import com.application.areca.launcher.gui.common.SecuredRunner;
import com.application.areca.launcher.gui.composites.InfoChannel;
import com.application.areca.launcher.gui.composites.LogComposite;
import com.application.areca.launcher.gui.menus.AppActionReferenceHolder;
import com.application.areca.launcher.gui.menus.MenuBuilder;
import com.application.areca.launcher.gui.wizards.BackupShortcutWizardWindow;
import com.application.areca.launcher.gui.wizards.BackupStrategyWizardWindow;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.trace.TraceEntry;
import com.application.areca.search.SearchResultItem;
import com.application.areca.version.VersionChecker;
import com.application.areca.version.VersionInfos;
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

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

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
public class Application 
implements ActionConstants, Window.IExceptionHandler, ArecaURLs {

	public static String[] STATUS_LABELS;
	public static Image[] STATUS_ICONS;  
	private static final ResourceManager RM = ResourceManager.instance();
	private static Application instance = new Application();
	public static boolean SIMPLE_SUBTABS = true;
	public static boolean SIMPLE_MAINTABS = true; 

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
	private Menu processContextMenu;
	private Menu workspaceContextMenu;
	private Menu logContextMenu;
	private Menu historyContextMenu;
	private Menu searchContextMenu;

	private Workspace workspace;
	private MainWindow mainWindow;

	private Identifiable currentObject;					// Objet en cours de selection; il peut s'agir d'un workspace, groupe ou target
	private GregorianCalendar currentFromDate;			// Debut de l'intervalle de dates en cours de selection
	private GregorianCalendar currentToDate;			// Fin de l'intervalle de dates en cours de selection
	private TraceEntry currentEntry;					// Entree en cours de selection (utile pour le detail d'une archive)
	private EntryArchiveData currentEntryData;   		// En cas d'affichage de l'historique d'une entree, date en cours de selection
	private RecoveryFilter currentFilter;				// En cas de selection d'un noeud sur le panel de detail d'une archive (repertoire ou Entry reelle), nom de celui ci.
	private boolean latestVersionRecoveryMode;        	// Indique si la recovery se fera en derniere version ou non

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
		this.archiveContextMenuLogical = MenuBuilder.buildArchiveContextMenuLogical(shell);
		this.actionContextMenu = MenuBuilder.buildActionContextMenu(shell);
		this.targetContextMenu = MenuBuilder.buildTargetContextMenu(shell);
		this.processContextMenu = MenuBuilder.buildProcessContextMenu(shell);
		this.workspaceContextMenu = MenuBuilder.buildWorkspaceContextMenu(shell);
		this.logContextMenu = MenuBuilder.buildLogContextMenu(shell);
		this.historyContextMenu = MenuBuilder.buildHistoryContextMenu(shell);
		this.searchContextMenu = MenuBuilder.buildSearchContextMenu(shell);
	}

	public void checkSystem() {
		if (! VersionInfos.checkJavaVendor()) {
			Logger.defaultLogger().warn(VersionInfos.VENDOR_MSG);

			if (ArecaPreferences.isDisplayJavaVendorMessage()) {
				this.showVendorDialog();
			}
		}
	}

	public RecoveryFilter getCurrentFilter() {
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

	public Menu getProcessContextMenu() {
		return processContextMenu;
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
			AboutWindow about = new AboutWindow();
			showDialog(about);         
		} else if (command.equals(CMD_HELP)) {
			// HELP
			showHelpFrame();
		} else if (command.equals(CMD_BACKUP_ALL)) {
			this.showBackupWindow(null, workspace, false); 
		} else if (command.equals(CMD_BACKUP)) {            
			// BACKUP
			if (TargetGroup.class.isAssignableFrom(this.getCurrentObject().getClass())) {
				this.showBackupWindow(null, getCurrentTargetGroup(), false); 
			} else if (FileSystemTarget.class.isAssignableFrom(this.getCurrentObject().getClass())) {
				// BACKUP WITH MANIFEST
				Manifest mf;
				try {
					mf = ((AbstractIncrementalFileSystemMedium)this.getCurrentTarget().getMedium()).buildDefaultBackupManifest();
				} catch (ApplicationException e1) {
					Logger.defaultLogger().error(e1);
					mf = null;
				}
				this.showBackupWindow(mf, getCurrentTarget(), false);   
			}
		} else if (command.equals(CMD_MERGE)) {
			// MERGE
			AbstractTarget target = this.getCurrentTarget();
			ArchiveMedium medium = target.getMedium();
			if (! ((AbstractIncrementalFileSystemMedium)medium).isOverwrite()) {
				try {
					Manifest manifest = this.getCurrentTarget().buildDefaultMergeManifest(this.getCurrentFromDate(), this.getCurrentToDate());
					this.showMergeWindow(target, manifest);
				} catch (ApplicationException e1) {
					handleException(e1);
				}                
			}     
		} else if (command.equals(CMD_DELETE_ARCHIVES)) {
			// DELETE ARCHIVES
			int result = showConfirmDialog(
					RM.getLabel("app.deletearchivesaction.confirm.message", new Object[] {Utils.formatDisplayDate(this.currentFromDate)}),
					RM.getLabel("app.deletearchivesaction.confirm.title"));

			if (result == SWT.YES) {
				if (FileSystemTarget.class.isAssignableFrom(this.getCurrentObject().getClass())) {
					FileSystemTarget target = (FileSystemTarget)this.getCurrentObject();
					TargetGroup process = target.getGroup();
					ProcessRunner rn = new ProcessRunner(target) {
						public void runCommand() throws ApplicationException {
							rProcess.processDeleteOnTarget(rTarget, rFromDate, context);
						}
					};
					rn.rProcess = process;
					rn.rName = RM.getLabel("app.deletearchivesaction.process.message");
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
			showEditTarget((AbstractTarget)this.getCurrentObject());
		} else if (command.equals(CMD_DEL_TARGET)) {            
			// DELETE TARGET
			showDeleteTarget();
		} else if (command.equals(CMD_NEW_PROCESS)) {
			// NEW PROCESS
			showEditGroup(null);            
		} else if (command.equals(CMD_DUPLICATE_TARGET)) {
			// DUPLICATE TARGET
			try {
				duplicateTarget(this.getCurrentTarget());
			} catch (ApplicationException e1) {
				this.handleException(RM.getLabel("error.duplicatetarget.message", new Object[] {e1.getMessage()}), e1);
			} 
		} else if (command.equals(CMD_EDIT_PROCESS_XML)) {
			// EDIT XML CONFIGURATION
			showEditGroupXML(this.getCurrentTargetGroup());  
		} else if (command.equals(CMD_SUPPORT)) {
			try {
				ViewerHandlerHelper.getViewerHandler().browse(new URL(DONATION_URL));
			} catch (Exception e) {
				handleException(e);
			}
		} else if (command.equals(CMD_EDIT_PROCESS)) {
			// EDIT PROCESS
			showEditGroup(this.getCurrentTargetGroup());            
		} else if (command.equals(CMD_DEL_PROCESS)) {
			// DELETE PROCESS
			showDeleteProcess();
		} else if (command.equals(CMD_SIMULATE)) {
			// SIMULATE
			ProcessRunner rn = new ProcessRunner(this.getCurrentTarget()) {
				private SimulationResult entries;

				public void runCommand() throws ApplicationException {
					entries = this.rProcess.processSimulateOnTarget(this.rTarget, this.context);
				}

				protected void finishCommand() {
					SecuredRunner.execute(new Runnable() {
						public void run() {
							SimulationWindow frm = new SimulationWindow(entries, rTarget);
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
			String initPath = this.workspace != null ? this.workspace.path : OSTool.getUserHome();
			String path = showDirectoryDialog(initPath, this.mainWindow);
			openWorkspace(path);
		} else if (command.equals(CMD_IMPORT_GROUP)) {
			// IMPORT GROUP
			ImportGroupWindow frm = new ImportGroupWindow();
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
		} else if (
				command.equals(CMD_RECOVER) || command.equals(CMD_RECOVER_WITH_FILTER)
				|| command.equals(CMD_RECOVER_WITH_FILTER_LATEST)
		) {
			// RECOVER
			RecoverWindow window = new RecoverWindow(true);
			window.setRecoverDeletedEntries(this.currentFilter != null && this.currentFilter.isContainsDeletedDirectory());
			this.showDialog(window);
			String path = window.getLocation();
			final boolean checkRecoveredFiles = window.isCheckRecoveredFiles();
			final boolean recoverDeletedEntries = window.isRecoverDeletedEntries();

			if (path != null) {
				if (FileSystemTarget.class.isAssignableFrom(this.getCurrentObject().getClass())) {
					FileSystemTarget target = (FileSystemTarget)this.getCurrentObject();
					TargetGroup process = target.getGroup();
					ProcessRunner rn = new ProcessRunner(target) {
						public void runCommand() throws ApplicationException {
							rProcess.processRecoverOnTarget(
									rTarget, 
									argument == null ? null : ((RecoveryFilter)argument).getFilter(), 
											rPath, 
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
			CheckWindow window = new CheckWindow();
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
		} else if (command.equals(CMD_SEARCH_LOGICAL) || command.equals(CMD_SEARCH_PHYSICAL)) {
			SearchResultItem item = this.mainWindow.getSearchView().getSelectedItem();

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
				for (int i=0; i< filter.length; i++) {
					cp.append(filter[i]).append(OSTool.getLineSeparator());
				}
			}
			copyString(cp.toString());
		} else if (
				command.equals(CMD_RECOVER_ENTRY_HISTO) 
				|| command.equals(CMD_VIEW_FILE_AS_TEXT_HISTO) 
				|| command.equals(CMD_VIEW_FILE_HISTO)
				|| command.equals(CMD_VIEW_FILE_AS_TEXT) 
				|| command.equals(CMD_VIEW_FILE)
		) {
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
				if (FileSystemTarget.class.isAssignableFrom(this.getCurrentObject().getClass())) {
					FileSystemTarget target = (FileSystemTarget)this.getCurrentObject();
					TargetGroup process = target.getGroup();
					ProcessRunner rn = new ProcessRunner(target) {
						private File recoveredFile;

						public void runCommand() throws ApplicationException {
							File entry = new File(path, rEntry.getKey());
							recoveredFile = new File(path, FileSystemManager.getName(entry));
							if (FileSystemManager.exists(recoveredFile)) {
								FileSystemManager.delete(recoveredFile);
							}
							rProcess.processRecoverOnTarget(
									rTarget, 
									rPath, 
									rFromDate, 
									rEntry.getKey(), 
									checkRecoveredFiles,
									context);
						}

						protected void finishCommand() {
							showRecoveryResultWindow(context);

							if (
									command.equals(CMD_VIEW_FILE_AS_TEXT_HISTO) 
									|| command.equals(CMD_VIEW_FILE_HISTO)
									|| command.equals(CMD_VIEW_FILE_AS_TEXT) 
									|| command.equals(CMD_VIEW_FILE)	
							) {
								File entry = new File(path, rEntry.getKey());
								final File f = new File(path, FileSystemManager.getName(entry));
								FileSystemManager.deleteOnExit(f);

								if (command.equals(CMD_VIEW_FILE_AS_TEXT_HISTO) || command.equals(CMD_VIEW_FILE_AS_TEXT)) {
									launchFileEditor(FileSystemManager.getAbsolutePath(f), true);
								} else {
									SecuredRunner.execute(new Runnable(){
										public void run() {
											try {
												ViewerHandlerHelper.getViewerHandler().open(f);
											} catch (Throwable e) {
												if (ArecaPreferences.getEditionCommand() != null && ArecaPreferences.getEditionCommand().trim().length() != 0) {
													Logger.defaultLogger().fine("No default viewer found for " + FileSystemManager.getAbsolutePath(f) + ". Launching text viewer.");
													launchFileEditor(FileSystemManager.getAbsolutePath(f), true);
												} else {
													Application.instance.showErrorDialog("An error occured while launching default viewer for " + f.getAbsolutePath(), "Error viewing " + f.getAbsolutePath(), false);
													Logger.defaultLogger().error("Error viewing file " + FileSystemManager.getAbsolutePath(f) + " : " + e.getMessage());
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
					rn.rName = RM.getLabel("app.recoverfileaction.process.message");
					rn.rPath = FileSystemManager.getAbsolutePath(new File(path));
					rn.rFromDate = command.equals(CMD_RECOVER_ENTRY_HISTO) || command.equals(CMD_VIEW_FILE_AS_TEXT_HISTO) || command.equals(CMD_VIEW_FILE_HISTO) ? this.currentEntryData.getManifest().getDate() : null;
					rn.launch();  
				}  
			}        
		}  else if (command.equals(CMD_VIEW_MANIFEST)) {
			this.showArchiveDetail(null);
		} 
	}

	public ProcessRunner launchArchiveCheck(final String path, final boolean checkSelectedEntries, final CheckWindow window) {
		if (FileSystemTarget.class.isAssignableFrom(this.getCurrentObject().getClass())) {
			FileSystemTarget target = (FileSystemTarget)this.getCurrentObject();
			TargetGroup process = target.getGroup();
			ProcessRunner rn = new ProcessRunner(target) {
				public void runCommand() throws ApplicationException {
					rProcess.processCheckOnTarget(
							rTarget, 
							path,
							checkSelectedEntries, 
							rFromDate, 
							context);
				}

				protected void finishCommand() {
					window.setResult(
							context.getInvalidRecoveredFiles(), 
							context.getUncheckedRecoveredFiles(), 
							context.getUnrecoveredFiles(),
							context.getNbChecked());
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
		} else {
			return null;
		}
	}

	/**
	 * Show the files with errors
	 */
	private void showRecoveryResultWindow(final ProcessContext context) {
		SecuredRunner.execute(new Runnable() {
			public void run() {
				if (context.hasRecoveryProblem()) {
					showWarningDialog(
							RM.getLabel("recover.check.invalid.label"),
							RM.getLabel("recover.check.result.title"), 
							false);
				}
			}
		});
	}

	public void copyString(String s) {
		TextTransfer textTransfer = TextTransfer.getInstance();
		clipboard.setContents(new Object[] {s}, new Transfer[] {textTransfer});
	}

	public void showEditTarget(AbstractTarget target) {
		TargetEditionWindow frmEdit = new TargetEditionWindow(target);
		showDialog(frmEdit);
		AbstractTarget newTarget = frmEdit.getTargetIfValidated();
		if (newTarget != null) {
			TargetGroup process = newTarget.getGroup();
			process.addTarget(newTarget);
			this.currentObject = newTarget;
			this.saveProcess(process);
			this.mainWindow.refresh(true, true);
		}
	}

	private void launchFileEditor(String path, boolean async) {
		path = path.replace('\\', '/');
		try {
			String editCommand = ArecaPreferences.getEditionCommand();
			Logger.defaultLogger().info("Launching '" + editCommand + "' on file '"  + path + "'");
			String[] cmd = new String[] {editCommand, path};
			OSTool.execute(cmd, async);
		} catch (Exception e) {
			Application.getInstance().handleException("Error attempting to edit " + path + " - Text editor = " + ArecaPreferences.getEditionCommand(), e);
		}
	}

	public void showEditGroupXML(final TargetGroup process) {
		if (process != null) {
			Runnable rn = new Runnable() {
				public void run() {
					launchFileEditor(process.getSourceFile().getAbsolutePath(), false);
				}
			};

			Thread th = new Thread(rn);
			th.setDaemon(true);
			th.setName("Group XML edition");
			th.start();
		}
	}   

	public void showEditGroup(TargetGroup process) {
		GroupEditionWindow frmEdit = new GroupEditionWindow(process);
		showDialog(frmEdit);
		TargetGroup newProcess = frmEdit.getProcess();
		if (newProcess != null) {
			this.getWorkspace().addGroup(newProcess);
			this.currentObject = newProcess;
			this.saveProcess(newProcess);
			this.mainWindow.refresh(true, true);
		}
	}   

	public void openWorkspace(String path) {
		if (path != null) {
			try {
				this.setWorkspace(new Workspace(FileSystemManager.getAbsolutePath(new File(path)), this), true);
			} catch (AdapterException e) {
				Logger.defaultLogger().error("Error detected in " + e.getSource());
				this.handleException(
						RM.getLabel("error.loadworkspace.message", new Object[] {e.getMessage(), e.getSource()}),
						e
				);
			} catch (Throwable e) {
				this.handleException(
						RM.getLabel("error.loadworkspace.message", new Object[] {e.getMessage(), path}),
						e
				);
			}                  
		}
	}

	public void checkVersion(final boolean explicit) {
		if (explicit || ArecaPreferences.isCheckNewVersions()) {
			Runnable rn = new Runnable() {
				public void run() {
					try {
						Logger.defaultLogger().info("Checking new version of Areca ...");
						final VersionData data = VersionChecker.getInstance().checkForNewVersion();
						VersionData currentVersion = VersionInfos.getLastVersion();

						if (currentVersion.equals(data)) {
							Logger.defaultLogger().info("No new version found : v" + data.getVersionId() + " is the latest version.");
							if (explicit) {
								SecuredRunner.execute(new Runnable() {
									public void run() {
										NewVersionWindow win = new NewVersionWindow(
												RM.getLabel("common.versionok.message", new Object[] {data.getVersionId(), VersionInfos.formatVersionDate(data.getVersionDate()), data.getDownloadUrl(), data.getDescription()}),
												false   
										);                                    
										showDialog(win);
									}
								});
							}
						} else {
							Logger.defaultLogger().info("New version found : " + data.toString());
							SecuredRunner.execute(new Runnable() {
								public void run() {
									NewVersionWindow win = new NewVersionWindow(
											RM.getLabel("common.newversion.message", new Object[] {data.getVersionId(), VersionInfos.formatVersionDate(data.getVersionDate()), data.getDownloadUrl(), data.getDescription()}), 
											true   
									);                                    
									showDialog(win);
									if (win.isValidated()) {
										try {
											ViewerHandlerHelper.getViewerHandler().browse(data.getDownloadUrl());
										} catch (IOException e1) {
											Logger.defaultLogger().error(e1);
										} catch (NoBrowserFoundException e1) {
											Logger.defaultLogger().error("Error connecting to : " + data.getDownloadUrl() + " - No web browser could be found.", e1);
										}

										Application.this.processExit();
									}
								}
							});
						}
					} catch (Throwable e) {
						handleException("An error occurred during Areca's version verification : " + e.getMessage(), e);
					}  
				}
			};

			Thread th = new Thread(rn);
			th.start();
		}
	}

	public void buildStrategy() {
		// Dialog
		String prefix = Util.replace(this.getCurrentTargetGroup().getSource(), ".xml", "").toLowerCase().replace(' ', '_');
		prefix += "_" + this.getCurrentTarget().getId()+ "_every_";

		BackupStrategyWizardWindow win = new BackupStrategyWizardWindow(OSTool.getUserHome());
		showDialog(win);

		String path = win.getSelectedPath();
		boolean check = false; // not used yet ...

		if (path != null && win.getTimes() != null && win.getTimes().size() != 0) {
			String files = "";

			// Init
			String commentPrefix;
			String commandPrefix;
			String extension;
			File executable = Utils.buildExecutableFile();
			if (OSTool.isSystemWindows()) {
				extension = ".bat";
				commentPrefix = "@REM ";
				commandPrefix = "@";
			} else {
				extension = ".sh";
				commentPrefix = "# ";
				commandPrefix = "";
			}
			String content = commentPrefix + "Script generated by Areca v" + VersionInfos.getLastVersion().getVersionId() + " on " + Utils.formatDisplayDate(new GregorianCalendar()) + "\n\n";
			content += commentPrefix + "Target Group : \"" + this.getCurrentTargetGroup().getName() + "\"\n";
			content += commentPrefix + "Target : \"" + this.getCurrentTarget().getTargetName() + "\"\n\n";

			String configPath = FileSystemManager.getAbsolutePath(this.getCurrentTargetGroup().getSourceFile());
			String command = commandPrefix + "\"" + FileSystemManager.getAbsolutePath(executable) + "\" merge -config \"" + configPath + "\" -target " + this.getCurrentTarget().getId();

			// Script generation
			List parameters = win.getTimes();
			int unit = 1;
			for (int i=0; i<parameters.size(); i++) {                
				int repetition = ((Integer)parameters.get(i)).intValue();
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
					String strCheck = check ? "-c " : "";
					fileContent += commandPrefix + "\"" + FileSystemManager.getAbsolutePath(executable) + "\" backup " + strCheck + "-config \"" + configPath + "\" -target " + this.getCurrentTarget().getId() + "\n";
					unit *= repetition;
				} else {
					// Intermediate merge
					int to = unit;
					int from = 2*unit;
					fileContent += "\n" + commentPrefix + "Merge between day " + to + " and day " + from + " \n";
					fileContent += command + " -from " + from + " -to " + to + "\n";
					unit *= repetition + 1;
				}

				// Final merge
				if (i == parameters.size() - 1) {
					fileContent += "\n" + commentPrefix + "Merge after day " + unit + " \n";
					fileContent += command + " -delay " + unit + "\n";
				}

				// File generation
				File tgFile = new File(path, fileName);
				files += "\n- " + FileSystemManager.getName(tgFile);
				buildExecutableFile(tgFile, fileContent);
			}

			this.showInformationDialog(RM.getLabel("shrtc.confirm.message", new Object[]{path, files}), RM.getLabel("shrtc.confirm.title"), false);
		}
	}

	public void buildBatch() {
		String fileNameSelected = "backup_" + Util.replace(this.getCurrentTargetGroup().getSource(), ".xml", "").toLowerCase().replace(' ', '_');
		String fileNameAll = "backup";
		if (this.isCurrentObjectTarget()) {
			fileNameSelected += "_" + this.getCurrentTarget().getId();
		}
		String commentPrefix;
		String commandPrefix;
		if (OSTool.isSystemWindows()) {
			fileNameSelected += ".bat";
			fileNameAll += ".bat";
			commentPrefix = "@REM ";
			commandPrefix = "@";
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
			String content = commentPrefix + "Backup script generated by Areca v" + VersionInfos.getLastVersion().getVersionId() + " on " + Utils.formatDisplayDate(new GregorianCalendar()) + "\n\n";
			File executable = Utils.buildExecutableFile();

			if (forSelectedOnly) {
				content += generateShortcutScript(
						executable, 
						this.getCurrentTargetGroup(), 
						isCurrentObjectTarget() ? getCurrentTarget() : null, 
								commentPrefix, 
								commandPrefix,
								check,
								full,
								differential);
			} else {
				Iterator iter = this.workspace.getGroupIterator();
				while (iter.hasNext()) {
					TargetGroup process = (TargetGroup)iter.next();
					content += generateShortcutScript(
							executable, 
							process, 
							null, 
							commentPrefix, 
							commandPrefix,
							check,
							full,
							differential);
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
			if (! OSTool.isSystemWindows()) {
				String[] chmod = new String[] {"chmod", "750", strTgFile};
				OSTool.execute(chmod);
			}
		} catch (Throwable e) {
			handleException("Error during command file creation", e);
		}
	}

	public void importGroup(File f) {
		try {
			if (
					this.workspace != null
					&& FileSystemManager.exists(f) 
					&& FileSystemManager.isFile(f) 
					&& FileSystemManager.getName(f).toLowerCase().endsWith(".xml")              
			) {
				FileTool.getInstance().copy(f, new File(workspace.getPath()), null, null);
				this.openWorkspace(this.workspace.getPath());
			}
		} catch (Throwable e) {
			handleException(RM.getLabel("error.importgrp.message"), e);
		}
	}

	private String generateShortcutScript(
			File executable,
			TargetGroup process, 
			AbstractTarget target,
			String commentPrefix,
			String commandPrefix,
			boolean check,
			boolean full,
			boolean differential
	) {
		String type = full ? AbstractTarget.BACKUP_SCHEME_FULL : (differential ? AbstractTarget.BACKUP_SCHEME_DIFFERENTIAL : AbstractTarget.BACKUP_SCHEME_INCREMENTAL);

		String comments = commentPrefix + type + "\n" + commentPrefix + "Target Group : \"" + process.getName() + "\"\n";
		String configPath = FileSystemManager.getAbsolutePath(process.getSourceFile());
		String command = "backup ";
		if (full) {
			command += "-f ";
		} else if (differential) {
			command += "-d ";
		}
		if (check) {
			command += "-c ";
		}

		command += "-config \"" + configPath + "\"";
		if (target != null) {
			command += " -target " + target.getId();
			comments += commentPrefix + "Target : \"" + target.getTargetName() + "\"\n";
		}

		command = commandPrefix + "\"" + FileSystemManager.getAbsolutePath(executable) + "\" " + command;

		return comments + command + "\n\n";
	}

	public void createWorkspaceCopy(File root, boolean removeEncryptionData) {
		String removeStr = removeEncryptionData ? " (Encryption data will be removed)" : "";
		Logger.defaultLogger().info("Creating a backup copy of current workspace (" + this.workspace.path + ") in " + FileSystemManager.getAbsolutePath(root) + removeStr);
		try {
			if (this.workspace != null) {
				if (! FileSystemManager.exists(root)) {
					fileTool.createDir(root);
				}

				Iterator iter = this.workspace.getGroupIterator();
				while (iter.hasNext()) {
					TargetGroup process = (TargetGroup)iter.next();
					ProcessXMLWriter writer = new ProcessXMLWriter(removeEncryptionData);
					File targetFile = new File(root, FileSystemManager.getName(process.getSourceFile()));
					Logger.defaultLogger().info("Creating a backup copy of \"" + process.getName() + "\" : " + FileSystemManager.getAbsolutePath(targetFile));
					writer.serializeProcess(process, targetFile);
				}
			}

			Logger.defaultLogger().info("Backup copy of " + this.workspace.path + " successfully created.");
		} catch (Throwable e) {
			handleException(RM.getLabel("error.cpws.message"), e);
		}
	}

	public void clearLog() {
		Logger.defaultLogger().clearLog(LogComposite.class);
	}

	private void duplicateTarget(AbstractTarget target) throws ApplicationException {
		try {
			AbstractTarget clone = (AbstractTarget)target.duplicate();
			this.getCurrentTargetGroup().addTarget(clone);
			clone.getMedium().install();

			this.setCurrentObject(clone, true);
			this.saveProcess(this.getCurrentTargetGroup());
			this.mainWindow.refresh(true, true);
		} catch (Exception shallNotHappen) {
			handleException(shallNotHappen);
		}
	}

	public void showDeleteProcess() {       
		DeleteWindow window = new DeleteWindow(this.getCurrentTargetGroup());
		showDialog(window);

		if (window.isOk()) {
			if (window.isDeleteContent()) {
				try {
					Iterator iter = this.getCurrentTargetGroup().getTargetIterator();
					while (iter.hasNext()) {
						((AbstractTarget)iter.next()).getMedium().destroyRepository();
					}
				} catch (Exception e) {
					handleException(e);
				}
			}
			this.getWorkspace().removeGroup(this.getCurrentTargetGroup());
			FileSystemManager.delete(this.getCurrentTargetGroup().getSourceFile());
			this.currentObject = null;
			this.mainWindow.refresh(true, true);                   
		}
	}

	public void showDeleteTarget() {
		try {
			DeleteWindow window = new DeleteWindow((FileSystemTarget)this.getCurrentTarget());
			showDialog(window);

			if (window.isOk()) {
				if (window.isDeleteContent()) {
					this.getCurrentTarget().getMedium().destroyRepository();
				}
				this.getCurrentTargetGroup().removeTarget(this.getCurrentTarget());
				this.saveProcess(this.getCurrentTargetGroup());
				this.currentObject = null;
				this.mainWindow.refresh(true, true);                   
			}
		} catch (Exception e) {
			handleException(e);
		}
	}

	public void showArchiveDetail(TraceEntry entry) {
		this.enableWaitCursor();

		// VIEW ARCHIVE DETAIL
		FileSystemTarget target = (FileSystemTarget)this.getCurrentObject();

		try {
			AbstractIncrementalFileSystemMedium fsMedium = (AbstractIncrementalFileSystemMedium)target.getMedium();
			Manifest mf = ArchiveManifestCache.getInstance().getManifest(fsMedium, fsMedium.getLastArchive(null, currentFromDate));

			if (mf == null) {
				mf = new Manifest(Manifest.TYPE_BACKUP);
				mf.setDate(currentFromDate);
			}

			ArchiveWindow frm = new ArchiveWindow (
					mf, 
					currentFromDate,
					target.getMedium());

			frm.setCurrentEntry(entry);

			showDialog(frm);
		} catch (ApplicationException e1) {
			this.handleException(
					RM.getLabel("error.archiveloading.message", new Object[] {e1.getMessage()}),
					e1
			);
		} finally {
			this.disableWaitCursor();
		}
	}

	public void showLogicalView(TraceEntry entry) {
		this.mainWindow.focusOnLogicalView(entry);
	}

	/**
	 * Tells whether the virtual machine must be killed or 
	 * whether current non daemon threads must be kept alive.
	 */
	public boolean processExit() {
		this.mainWindow.savePreferences();
		if (this.channels.size() == 0) {
			return this.mainWindow.close(true);            
		} else {
			int result = showConfirmDialog(
					RM.getLabel("appdialog.confirmexit.message"),
					RM.getLabel("appdialog.confirmexit.title"),
					SWT.YES | SWT.NO | SWT.CANCEL);

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
	private String resolveBackupScheme(AbstractTarget target, String backupScheme) {
		if (target.supportsBackupScheme(backupScheme)) {
			return backupScheme;
		} else if (AbstractTarget.BACKUP_SCHEME_INCREMENTAL.equals(backupScheme)) {
			return resolveBackupScheme(target, AbstractTarget.BACKUP_SCHEME_DIFFERENTIAL);
		} else if (AbstractTarget.BACKUP_SCHEME_DIFFERENTIAL.equals(backupScheme)) {
			return resolveBackupScheme(target, AbstractTarget.BACKUP_SCHEME_FULL);
		} else {
			throw new IllegalStateException("Unable to resolve backup scheme for target " + target.getTargetName());
		}
	}

	public void launchBackupOnTarget(
			AbstractTarget target, 
			Manifest manifest, 
			String backupScheme, 
			final boolean disablePreCheck,
			final boolean disableArchiveCheck
	) {
		TargetGroup process = target.getGroup();
		final String resolvedBackupScheme = resolveBackupScheme(target, backupScheme);
		ProcessRunner rn = new ProcessRunner(target) {
			public void runCommand() throws ApplicationException {
				rProcess.processBackupOnTarget(rTarget, rManifest, resolvedBackupScheme, disablePreCheck, disableArchiveCheck, context);
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

	public void launchBackupOnGroup(
			TargetGroup group, 
			Manifest mf,
			String backupScheme, 
			boolean disableArchiveCheck
	) {
		Iterator iter = group.getTargetIterator();
		while (iter.hasNext()) {
			AbstractTarget tg = (AbstractTarget)iter.next();
			Manifest clone = mf == null ? null : (Manifest)mf.duplicate(); 
			this.launchBackupOnTarget(tg, clone, backupScheme, false, disableArchiveCheck);
		}
	}

	public void launchBackupOnWorkspace(
			Manifest mf,
			String backupScheme, 
			boolean disableArchiveCheck
	) {
		Iterator iter = this.workspace.getGroupIterator();
		while (iter.hasNext()) {
			TargetGroup grp = (TargetGroup)iter.next();
			this.launchBackupOnGroup(grp, mf, backupScheme, disableArchiveCheck);
		}
	}

	public void launchMergeOnTarget(final boolean keepDeletedEntries, Manifest manifest) {
		// MERGE
		FileSystemTarget target = (FileSystemTarget)this.getCurrentObject();
		TargetGroup process = target.getGroup();
		ProcessRunner rn = new ProcessRunner(target) {
			public void runCommand() throws ApplicationException {
				rProcess.processMergeOnTarget(rTarget, rFromDate, rToDate, rManifest, keepDeletedEntries, context);
			}
		};
		rn.rProcess = process;
		rn.rFromDate = currentFromDate;    
		rn.rName = RM.getLabel("app.mergearchivesaction.process.message");
		rn.rToDate = currentToDate;     
		rn.rManifest = manifest;
		rn.launch();                    
	}

	public void showBackupWindow(Manifest manifest, Object scope, boolean disableCheck) {
		BackupWindow frm = new BackupWindow(
				manifest, 
				scope,
				disableCheck);
		showDialog(frm);
	}

	public void showMergeWindow(AbstractTarget target, Manifest manifest) {
		MergeWindow frm = new MergeWindow(
				manifest, 
				target);
		showDialog(frm);
	}

	public void showVendorDialog() {
		JavaVendorWindow frm = new JavaVendorWindow();
		showDialog(frm);
	}

	public void showHelpFrame() {
		try {      	
			URL url = new URL(HELP_ROOT + VersionInfos.getLastVersion().getVersionId());
			ViewerHandlerHelper.getViewerHandler().browse(url);
		} catch (Exception e) {
			handleException(e);
		}
	}

	public void showDialog(final AbstractWindow window) {
		try {
			window.setModal(getMainWindow());
			window.setBlockOnOpen(true);
			window.open();
		} catch (Exception e) {
			handleException(e);
		}
	}

	public Object retrieveMissingEncryptionData(AbstractTarget tg) {
		MissingEncryptionDataWindow frm = new MissingEncryptionDataWindow(tg);
		showDialog(frm);
		return new Object[] {frm.getAlgo(), frm.getPassword(), new Boolean(frm.isEncryptFileNames())};
	}

	public Object retrieveMissingFTPData(AbstractTarget tg) {
		MissingFTPDataWindow frm = new MissingFTPDataWindow(tg);
		showDialog(frm);
		return new Object[] {frm.getPassword()};
	}

	public void handleException(final String msg, final Throwable e) {
		SecuredRunner.execute(new Runnable() {
			public void run() {
				disableWaitCursor();

				if (e != null) {
					if (!(e instanceof ApplicationException)) {
						Logger.defaultLogger().error(e); // Unexpected exception ... that may not have been logged.
					}
					e.printStackTrace(System.err);
				}

				showErrorDialog(
						msg,
						ResourceManager.instance().getLabel("error.dialog.title"),
						false);
			}
		});
	}

	public void handleException(Throwable e) {
		handleException(
				RM.getLabel("error.process.message", new Object[] {getExceptionMessage(e), ((FileLogProcessor)Logger.defaultLogger().find(FileLogProcessor.class)).getCurrentLogFile()}), 
				e
		);
	}

	private String getExceptionMessage(Throwable e) {
		return e.getMessage() == null ? "Unexpected error (" + e.getClass().getName() + ")" : e.getMessage();
	}


	public TargetGroup getCurrentTargetGroup() {
		if (this.currentObject == null) {
			return null;
		}

		if (TargetGroup.class.isAssignableFrom(this.currentObject.getClass())) {
			return (TargetGroup)this.currentObject;
		} else if (AbstractTarget.class.isAssignableFrom(this.currentObject.getClass())) {
			return ((AbstractTarget)this.currentObject).getGroup();
		} else {
			return null;
		}
	}

	public void saveProcess(TargetGroup process) {
		try {
			ProcessXMLWriter writer = new ProcessXMLWriter();
			boolean ok = writer.serializeProcess(process, process.getSourceFile());
			if (! ok) {
				throw new IOException("Error detected while writing configuration on disk");
			}
		} catch (Throwable e1) {
			this.handleException(RM.getLabel("error.groupupdate.message"), e1);
		}
	}

	public AbstractTarget getCurrentTarget() {
		if (this.currentObject == null) {
			return null;
		}

		return (AbstractTarget)this.currentObject;
	}

	public boolean isCurrentObjectTargetGroup() {
		return (currentObject != null && TargetGroup.class.isAssignableFrom(currentObject.getClass()));
	}

	public boolean isCurrentObjectTarget() {
		return (currentObject != null && FileSystemTarget.class.isAssignableFrom(currentObject.getClass()));
	}

	public void setCurrentEntry(TraceEntry currentEntry) {
		this.currentEntry = currentEntry;
		AppActionReferenceHolder.refresh();
	}

	public TraceEntry getCurrentEntry() {
		return currentEntry;
	}

	public void setCurrentFilter(RecoveryFilter argCurrentFilter) {
		if (argCurrentFilter != null && argCurrentFilter.getFilter() != null) {
			for (int i=0; i<argCurrentFilter.getFilter().length; i++) {
				if (argCurrentFilter.getFilter()[i].equals("/") || argCurrentFilter.getFilter()[i].equals("\\")) {
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

	public Identifiable getCurrentObject() {
		return currentObject;
	}
	public void setCurrentObject(Identifiable currentObject, boolean refreshTree) {
		if (this.currentObject != currentObject) { // Yes, we DO use reference comparison
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

		ArecaPreferences.setLastWorkspace(workspace.getPath());
	}

	public void enableWaitCursor(AbstractWindow window) {
		if (window != null) {
			window.getShell().setCursor(CURSOR_WAIT);
		}
	}

	public void disableWaitCursor(AbstractWindow window) {
		if (window != null && window.getShell() != null) {
			window.getShell().setCursor(null);
		}
	}

	public void enableWaitCursor() {
		enableWaitCursor(mainWindow);
	}

	public void disableWaitCursor() {
		disableWaitCursor(mainWindow);
	}

	public void showInformationDialog(
			String message,
			String title,
			boolean longMessage
	) {
		showDialog(message, title, true, SWT.ICON_INFORMATION, longMessage);
	}

	public void showWarningDialog(
			String message,
			String title,
			boolean longMessage
	) {
		showDialog(message, title, true,SWT.ICON_WARNING, longMessage);
	}

	public void showErrorDialog(
			String message,
			String title,
			boolean longMessage
	) {
		showDialog(message, title, true, SWT.ICON_ERROR, longMessage);
	}

	public int showConfirmDialog(
			final String message,
			final String title,
			final int buttons
	) {
		MessageBox msg = new MessageBox(Application.this.mainWindow.getShell(), buttons | SWT.ICON_QUESTION);
		msg.setText(title);
		msg.setMessage(message);
		return msg.open();
	}

	public int showConfirmDialog(
			String message,
			String title
	) {
		return showConfirmDialog(message, title, SWT.YES | SWT.NO);
	}

	private int showDialog(
			String message,
			String title,
			boolean closeOnly,
			int type,
			boolean longMessage
	) {
		if (mainWindow != null) {
			if (longMessage) {
				LongMessageWindow msg = new LongMessageWindow(title, message, closeOnly, type);
				showDialog(msg);
				if (msg.isValidated()) {
					return SWT.YES;
				} else {
					return SWT.NO;
				}
			} else {
				MessageBox msg = new MessageBox(this.mainWindow.getShell(), SWT.OK | type);
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
		if (this.getCurrentFromDate() != null && this.getCurrentToDate() != null && this.getCurrentFromDate().equals(this.getCurrentToDate())) {
			return this.getCurrentFromDate();
		} else {
			return null;
		}
	}

	public boolean areMultipleDatesSelected() {
		if (this.getCurrentFromDate() != null && this.getCurrentToDate() != null && (! this.getCurrentFromDate().equals(this.getCurrentToDate()))) {
			return true;
		} else {
			return false;
		}
	}

	public String showDirectoryDialog(String dir, AbstractWindow parent) {
		DirectoryDialog fileChooser = new DirectoryDialog(parent.getShell(), SWT.OPEN);
		if (dir != null) {
			fileChooser.setFilterPath(dir);
		}
		fileChooser.setText(RM.getLabel("common.choosedirectory.title"));
		fileChooser.setMessage(RM.getLabel("common.choosedirectory.message"));

		return fileChooser.open();
	}

	public String showFileDialog(String dir, AbstractWindow parent, String fileName, String title, int style) {
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

	public static void setTabLabel(CTabItem item, String label, boolean hasImage) {
		if (hasImage) {
			item.setText(label + "    ");
		} else {
			item.setText("  " + label + "  ");
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

	public void setCurrentDates(GregorianCalendar currentFromDate, GregorianCalendar currentToDate) {
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
		protected InfoChannel channel;

		public abstract void runCommand() throws ApplicationException;

		public ProcessRunner(AbstractTarget target) {
			this.rTarget = target;
			channel = new InfoChannel(rTarget, mainWindow.getProgressContainer().getMainPane());
			mainWindow.focusOnProgress();
		}

		public InfoChannel getChannel() {
			return channel;
		}

		// Called in the AWT event thread, to update GUI after the command execution
		protected void finishCommand() {
		}

		// Called in the AWT event thread, to update GUI after the command execution
		protected void finishCommandInError(Exception e) {
		}

		public void run() {           
			addChannel(channel);
			channel.setAction(rName);

			try {
				String taskName = "Unnamed-Task";
				if (rTarget != null) {
					taskName = rTarget.getTargetName();
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
						if (! TaskCancelledException.isTaskCancellation(e)) {
							handleException(e);
						} else {
							channel.print(RM.getLabel("common.processcancelled.label"));
							context.getTaskMonitor().enforceCompletion();
						}
					}
				}
			} finally {
				channel.stopRunning(); 
				removeChannel(channel);
				registerState(false);						// Enforce menu refresh
				AppActionReferenceHolder.refresh();			// Enforce menu refresh
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
