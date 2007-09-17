package com.application.areca.launcher.gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ApplicationException;
import com.application.areca.ArchiveMedium;
import com.application.areca.EntryArchiveData;
import com.application.areca.Identifiable;
import com.application.areca.RecoveryEntry;
import com.application.areca.RecoveryProcess;
import com.application.areca.ResourceManager;
import com.application.areca.UserInformationChannel;
import com.application.areca.Utils;
import com.application.areca.adapters.AdapterException;
import com.application.areca.adapters.ProcessXMLWriter;
import com.application.areca.cache.ArchiveManifestCache;
import com.application.areca.context.ProcessContext;
import com.application.areca.context.ReportingConfiguration;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.impl.FileSystemRecoveryTarget;
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
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.search.SearchResultItem;
import com.application.areca.version.VersionInfos;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.system.OSTool;
import com.myJava.util.Utilitaire;
import com.myJava.util.log.FileLogProcessor;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 3732974506771028333
 */
 
 /*
 Copyright 2005-2007, Olivier PETRUCCI.
 
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
implements ActionConstants, Window.IExceptionHandler {

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
        STATUS_LABELS = new String[6];
        STATUS_ICONS = new Image[6];
        STATUS_LABELS[EntryArchiveData.STATUS_CREATED + 1] = ResourceManager.instance().getLabel("archivecontent.statuscreation.label");
        STATUS_LABELS[EntryArchiveData.STATUS_MODIFIED + 1] = ResourceManager.instance().getLabel("archivecontent.statusmodification.label");
        STATUS_LABELS[EntryArchiveData.STATUS_DELETED + 1] = ResourceManager.instance().getLabel("archivecontent.statusdeletion.label");
        STATUS_LABELS[EntryArchiveData.STATUS_MISSING + 1] = ResourceManager.instance().getLabel("archivecontent.statusmissing.label");
        STATUS_LABELS[EntryArchiveData.STATUS_FIRST_BACKUP + 1] = ResourceManager.instance().getLabel("archivecontent.statusfirstbackup.label");
        STATUS_LABELS[EntryArchiveData.STATUS_UNKNOWN + 1] = ResourceManager.instance().getLabel("archivecontent.statusunknown.label");
        STATUS_ICONS[EntryArchiveData.STATUS_CREATED + 1] = ArecaImages.ICO_HISTO_NEW;
        STATUS_ICONS[EntryArchiveData.STATUS_MODIFIED + 1] = ArecaImages.ICO_HISTO_EDIT;
        STATUS_ICONS[EntryArchiveData.STATUS_DELETED + 1] = ArecaImages.ICO_HISTO_DELETE;
        STATUS_ICONS[EntryArchiveData.STATUS_FIRST_BACKUP + 1] = ArecaImages.ICO_HISTO_NEW;
    }

    private CTabFolderManager folderMonitor = new CTabFolderManager();

    // Keep a reference on the display used by the swt thread.
    private Display display;

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

    private Identifiable currentObject;							// Objet en cours de sélection; il peut s'agir d'un workspace, groupe ou target
    private GregorianCalendar currentFromDate;			// Début de l'intervalle de dates en cours de sélection
    private GregorianCalendar currentToDate;				// Fin de l'intervalle de dates en cours de sélection
    private RecoveryEntry currentEntry;						// Entrée en cours de sélection (utile pour le détail d'une archive)
    private GregorianCalendar currentHistoryDate; 		// En cas d'affichage de l'historique d'une entrée, date en cours de sélection
    private String[] currentFilter;									// En cas de sélection d'un noeud sur le panel de détail d'une archive (répertoire ou Entry réelle), nom de celui ci.
    private boolean latestVersionRecoveryMode;         // Indique si la recovery se fera en dernière version ou non
    
    private Set channels = new HashSet();

    private FileTool fileTool = FileTool.getInstance();

    public Cursor CURSOR_WAIT;

    public Application() {
        Window.setExceptionHandler(this);
    }

    public void show() {
        mainWindow = new MainWindow();
        display = Display.getCurrent();

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

    public void loadWorkspace(String path) {
        try {
            this.setWorkspace(new Workspace(path, this), false);
        } catch (AdapterException e) {
            this.handleException(
                    ResourceManager.instance().getLabel("error.loadworkspace.message", new Object[] {e.getMessage(), e.getSource()}),
                    e
            );
        }       
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
            launchBackupOnWorkspace();
        } else if (command.equals(CMD_BACKUP)) {            
            // BACKUP
            if (RecoveryProcess.class.isAssignableFrom(this.getCurrentObject().getClass())) {
                RecoveryProcess process = (RecoveryProcess)this.getCurrentObject();
                launchBackupOnProcess(process);
            } else if (FileSystemRecoveryTarget.class.isAssignableFrom(this.getCurrentObject().getClass())) {
                launchBackupOnTarget(this.getCurrentTarget(), null);
            }
        } else if (command.equals(CMD_COMPACT)) {
            // COMPACT
            AbstractRecoveryTarget target = this.getCurrentTarget();
            ArchiveMedium medium = target.getMedium();
            if (! ((AbstractIncrementalFileSystemMedium)medium).isOverwrite()) {
                try {
                    Manifest manifest = this.getCurrentTarget().buildDefaultMergeManifest(this.getCurrentFromDate(), this.getCurrentToDate());
                    this.showManifestEditionFrame(true, target, manifest);
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
                if (FileSystemRecoveryTarget.class.isAssignableFrom(this.getCurrentObject().getClass())) {
                    FileSystemRecoveryTarget target = (FileSystemRecoveryTarget)this.getCurrentObject();
                    RecoveryProcess process = target.getProcess();
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
            showEditTarget((AbstractRecoveryTarget)this.getCurrentObject());
        } else if (command.equals(CMD_DEL_TARGET)) {            
            // DELETE TARGET
            showDeleteTarget();
        } else if (command.equals(CMD_NEW_PROCESS)) {
            // NEW PROCESS
            showEditProcess(null);            
        } else if (command.equals(CMD_DUPLICATE_TARGET)) {
            // DUPLICATE TARGET
            try {
                duplicateTarget(this.getCurrentTarget());
            } catch (ApplicationException e1) {
                this.handleException(RM.getLabel("error.duplicatetarget.message", new Object[] {e1.getMessage()}), e1);
            } 
        } else if (command.equals(CMD_EDIT_PROCESS)) {
            // EDIT PROCESS
            showEditProcess(this.getCurrentProcess());            
        } else if (command.equals(CMD_DEL_PROCESS)) {
            // DELETE PROCESS
            showDeleteProcess();
        } else if (command.equals(CMD_SIMULATE)) {
            // SIMULATE
            ProcessRunner rn = new ProcessRunner(this.getCurrentTarget()) {
                private RecoveryEntry[] entries;

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
            rn.rProcess = this.getCurrentProcess();
            rn.rName = RM.getLabel("app.simulateaction.process.message");
            rn.refreshAfterProcess = false;
            rn.launch();
        } else if (command.equals(CMD_EXIT)) {
            // EXIT
            this.processExit();            
        } else if (command.equals(CMD_OPEN)) {
            // OPEN WORKSPACE
            String path = showDirectoryDialog(this.workspace.path, this.mainWindow);
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
                || command.equals(CMD_RECOVER_FROM_LOGICAL)
        ) {
            // RECOVER
            String path;
            final boolean recoverDeletedEntries;
            if (command.equals(CMD_RECOVER_FROM_LOGICAL)) {
                RecoverWindow window = new RecoverWindow();
                this.showDialog(window);
                path = window.getLocation();
                recoverDeletedEntries = window.isRecoverDeletedEntries();
            } else {
                path = showDirectoryDialog(null, this.mainWindow);
                recoverDeletedEntries = false;
            }

            if (path != null) {
                if (FileSystemRecoveryTarget.class.isAssignableFrom(this.getCurrentObject().getClass())) {
                    FileSystemRecoveryTarget target = (FileSystemRecoveryTarget)this.getCurrentObject();
                    RecoveryProcess process = target.getProcess();
                    ProcessRunner rn = new ProcessRunner(target) {
                        public void runCommand() throws ApplicationException {
                            rProcess.processRecoverOnTarget(rTarget, (String[])argument, rPath, rFromDate, recoverDeletedEntries, context);
                        }
                    };
                    rn.rProcess = process;
                    rn.refreshAfterProcess = false;
                    rn.rName = RM.getLabel("app.recoverfilesaction.process.message");
                    rn.rPath = FileSystemManager.getAbsolutePath(new File(path));
                    if (command.equals(CMD_RECOVER) || command.equals(CMD_RECOVER_WITH_FILTER)) {
                        rn.rFromDate = getCurrentDate();
                    }
                    if (command.equals(CMD_RECOVER_WITH_FILTER) || command.equals(CMD_RECOVER_FROM_LOGICAL)) {
                        rn.argument = this.currentFilter;
                    }
                    rn.launch();                    
                }  
            }  
        } else if (command.equals(CMD_BUILD_BATCH)) {
            buildBatch();
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
        } else if (command.equals(CMD_RECOVER_ENTRY) || command.equals(CMD_EDIT_FILE)) {
            // RECOVER ENTRY
            final String path;
            if (command.equals(CMD_RECOVER_ENTRY)) {
                path= showDirectoryDialog(null, this.mainWindow);
            } else {
                path= OSTool.getTempDirectory();
            }

            if (path != null) {
                if (FileSystemRecoveryTarget.class.isAssignableFrom(this.getCurrentObject().getClass())) {
                    FileSystemRecoveryTarget target = (FileSystemRecoveryTarget)this.getCurrentObject();
                    RecoveryProcess process = target.getProcess();
                    ProcessRunner rn = new ProcessRunner(target) {
                        public void runCommand() throws ApplicationException {
                            rProcess.processRecoverOnTarget(rTarget, rPath, rFromDate, rEntry, context);
                        }
                        
                        protected void finishCommand() {
                            if (command.equals(CMD_EDIT_FILE)) {
                                File entry = new File(path, rEntry.getName());
                                File f = new File(path, FileSystemManager.getName(entry));
                                FileSystemManager.deleteOnExit(f);
                                try {
                                    String editCommand = ArecaPreferences.getEditionCommand();
                                    String targetFile = FileSystemManager.getAbsolutePath(f).replace('\\', '/');
                                    Logger.defaultLogger().info("Launching '" + editCommand + "' on file '"  + targetFile + "'");
                                    Runtime.getRuntime().exec(new String[] {editCommand, targetFile});
                                } catch (IOException e) {
                                    Application.getInstance().handleException("Error attempting to edit " + FileSystemManager.getAbsolutePath(f) + " - Text edition command = " + ArecaPreferences.getEditionCommand(), e);
                                }
                            }
                        }                        
                    };
                    rn.rProcess = process;
                    rn.refreshAfterProcess = false;
                    rn.rEntry = this.currentEntry;
                    rn.rName = RM.getLabel("app.recoverfileaction.process.message");
                    rn.rPath = FileSystemManager.getAbsolutePath(new File(path));
                    rn.rFromDate = this.currentHistoryDate;
                    rn.launch();                    
                }  
            }        
        } else if (command.equals(CMD_BACKUP_MANIFEST)) {
            // BACKUP WITH MANIFEST
            Manifest mf;
            try {
                mf = ((AbstractIncrementalFileSystemMedium)this.getCurrentTarget().getMedium()).buildDefaultBackupManifest();
            } catch (ApplicationException e1) {
                Logger.defaultLogger().error(e1);
                mf = new Manifest();
                mf.setType(Manifest.TYPE_BACKUP);
            }
            this.showManifestEditionFrame(false, this.getCurrentTarget(), mf);
        }  else if (command.equals(CMD_VIEW_MANIFEST)) {
            this.showArchiveDetail(null);
        } 
    }

    public void showEditTarget(AbstractRecoveryTarget target) {
        TargetEditionWindow frmEdit = new TargetEditionWindow(target);
        showDialog(frmEdit);
        AbstractRecoveryTarget newTarget = frmEdit.getTargetIfValidated();
        if (newTarget != null) {
            RecoveryProcess process = newTarget.getProcess();
            process.addTarget(newTarget);
            this.currentObject = newTarget;
            this.saveProcess(process);
            this.mainWindow.refresh(true, true);
        }
    }

    public void showEditProcess(RecoveryProcess process) {
        ProcessEditionWindow frmEdit = new ProcessEditionWindow(process);
        showDialog(frmEdit);
        RecoveryProcess newProcess = frmEdit.getProcess();
        if (newProcess != null) {
            this.getWorkspace().addProcess(newProcess);
            this.currentObject = newProcess;
            this.saveProcess(newProcess);
            this.mainWindow.refresh(true, true);
        }
    }   
    
    private void openWorkspace(String path) {
        if (path != null) {
            try {
                this.setWorkspace(new Workspace(FileSystemManager.getAbsolutePath(new File(path)), this), true);
            } catch (AdapterException e) {
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
    
    public void buildBatch() {
        String fileNameSelected = "backup_" + Utilitaire.replace(this.getCurrentProcess().getSource(), ".xml", "");
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
        
        //String path = showFileDialog(OSTool.getUserHome(), this.mainWindow, fileName, RM.getLabel("app.buildbatch.label"), SWT.SAVE);
        CreateShortcutWindow win = new CreateShortcutWindow(OSTool.getUserHome(), fileNameSelected, fileNameAll);
        showDialog(win);
        String path = win.getSelectedPath();
        boolean forSelectedOnly = win.isForSelectedOnly();
        
        if (path != null) {
            String content = commentPrefix + "Backup script generated by Areca v" + VersionInfos.getLastVersion().getVersionId() + " on " + Utils.formatDisplayDate(new GregorianCalendar()) + "\n\n";

            File applicationRoot = Utils.getApplicationRoot();
            File executable;
            if (OSTool.isSystemWindows()) {
                executable = new File(applicationRoot, "areca_cl.exe");
            } else {
                executable = new File(applicationRoot, "/bin/run_tui.sh");
            }
            
            if (forSelectedOnly) {
                content += generateShortcutScript(
                        executable, 
                        this.getCurrentProcess(), 
                        isCurrentObjectTarget() ? getCurrentTarget() : null, 
                        commentPrefix, 
                        commandPrefix);
            } else {
                Iterator iter = this.workspace.getProcessIterator();
                while (iter.hasNext()) {
                    RecoveryProcess process = (RecoveryProcess)iter.next();
                    content += generateShortcutScript(
                            executable, 
                            process, 
                            null, 
                            commentPrefix, 
                            commandPrefix);
                }
            }

            try {
                Logger.defaultLogger().info("Creating shell script : " + path);
                this.fileTool.createFile(new File(path), content);

                if (! OSTool.isSystemWindows()) {
                    String[] chmod = new String[] {"chmod", "750", path};
                    Process p = Runtime.getRuntime().exec(chmod);
                    int retValue = p.waitFor();
                    Logger.defaultLogger().info("Executed chmod command - got the following return code : " + retValue);
                    if (retValue != 0) {
                        String errorMsg = fileTool.getInputStreamContent(p.getErrorStream(), false);
                        Logger.defaultLogger().warn("Got the following error message : " + errorMsg);
                    }
                }
            } catch (Exception e) {
                handleException("Error during shortcut creation", e);
            }
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
                FileTool.getInstance().copy(f, new File(workspace.getPath()));
                this.openWorkspace(this.workspace.getPath());
            }
        } catch (Throwable e) {
            handleException(RM.getLabel("error.importgrp.message"), e);
        }
    }
    
    private String generateShortcutScript(
            File executable,
            RecoveryProcess process, 
            AbstractRecoveryTarget target,
            String commentPrefix,
            String commandPrefix        
        ) {
        
        String comments = commentPrefix + "Target Group : \"" + process.getName() + "\"\n";
        String configPath = FileSystemManager.getAbsolutePath(process.getSourceFile());
        String command = "backup -config \"" + configPath + "\"";
        if (target != null) {
            command += " -target " + target.getId();
            comments += commentPrefix + "Target : \"" + target.getTargetName() + "\"\n";
        }
        
        command = commandPrefix + "\"" + FileSystemManager.getAbsolutePath(executable) + "\" " + command;
        
        return comments + command + "\n\n";
    }

    public void createWorkspaceCopy(File root, boolean removeEncryptionData) {
        try {
            if (this.workspace != null) {
                File location = new File(root, "areca_workspace_copy");

                if (! FileSystemManager.exists(location)) {
                    fileTool.createDir(location);
                }

                Iterator iter = this.workspace.getProcessIterator();
                while (iter.hasNext()) {
                    RecoveryProcess process = (RecoveryProcess)iter.next();
                    ProcessXMLWriter writer = new ProcessXMLWriter(removeEncryptionData);
                    File targetFile = new File(location, FileSystemManager.getName(process.getSourceFile()));
                    writer.serializeProcess(process, targetFile);
                }
            }
        } catch (Throwable e) {
            handleException(RM.getLabel("error.cpws.message"), e);
        }
    }

    public void clearLog() {
        Logger.defaultLogger().clearLog(LogComposite.class);
    }

    private void duplicateTarget(AbstractRecoveryTarget target) throws ApplicationException {
        try {
            AbstractRecoveryTarget clone = (AbstractRecoveryTarget)target.duplicate();
            this.getCurrentProcess().addTarget(clone);
            clone.getMedium().install();

            this.setCurrentObject(clone, true);
            this.saveProcess(this.getCurrentProcess());
            this.mainWindow.refresh(true, true);
        } catch (Exception shallNotHappen) {
            handleException(shallNotHappen);
        }
    }

    public void showDeleteProcess() {       
        DeleteWindow window = new DeleteWindow(this.getCurrentProcess());
        showDialog(window);

        if (window.isOk()) {
            if (window.isDeleteContent()) {
                try {
                    Iterator iter = this.getCurrentProcess().getTargetIterator();
                    while (iter.hasNext()) {
                        ((AbstractRecoveryTarget)iter.next()).getMedium().destroyRepository();
                    }
                } catch (Exception e) {
                    handleException(e);
                }
            }
            this.getWorkspace().removeProcess(this.getCurrentProcess());
            FileSystemManager.delete(this.getCurrentProcess().getSourceFile());
            this.currentObject = null;
            this.mainWindow.refresh(true, true);                   
        }
    }

    public void showDeleteTarget() {
        try {
            DeleteWindow window = new DeleteWindow((FileSystemRecoveryTarget)this.getCurrentTarget());
            showDialog(window);

            if (window.isOk()) {
                if (window.isDeleteContent()) {
                    this.getCurrentTarget().getMedium().destroyRepository();
                }
                this.getCurrentProcess().removeTarget(this.getCurrentTarget());
                this.saveProcess(this.getCurrentProcess());
                this.currentObject = null;
                this.mainWindow.refresh(true, true);                   
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void showArchiveDetail(RecoveryEntry entry) {
        this.enableWaitCursor();

        // VIEW ARCHIVE DETAIL
        FileSystemRecoveryTarget target = (FileSystemRecoveryTarget)this.getCurrentObject();

        try {
            AbstractIncrementalFileSystemMedium fsMedium = (AbstractIncrementalFileSystemMedium)target.getMedium();
            Manifest mf = ArchiveManifestCache.getInstance().getManifest(fsMedium, fsMedium.getLastArchive(currentFromDate));

            if (mf == null) {
                mf = new Manifest();
                mf.setDate(currentFromDate);
            }

            ArchiveWindow frm = new ArchiveWindow (
                    mf, 
                    fsMedium.getEntries(currentFromDate),
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
    
    public void showLogicalView(RecoveryEntry entry) {
        this.mainWindow.focusOnLogicalView(entry);
    }

    /**
     * Indique si la VM doit être stoppée au moyen de System.exit(0) ou
     * Si le thread en cours doit simplement être stoppé, ce qui laisse la possibilité
     * aux threads non daemon de continuer à s'exécuter.
     * 
     * @return
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
                System.exit(0);  
                return true;
            } else {
                return false;
            }
        }
    } 

    public void launchBackupOnProcess(RecoveryProcess process) {
        Iterator iter = process.getTargetIterator();
        while (iter.hasNext()) {
            AbstractRecoveryTarget tg = (AbstractRecoveryTarget)iter.next();
            this.launchBackupOnTarget(tg, null);
        }
    }
    
    public void launchBackupOnWorkspace() {
        Iterator iter = this.workspace.getProcessIterator();
        while (iter.hasNext()) {
            RecoveryProcess process = (RecoveryProcess)iter.next();
            this.launchBackupOnProcess(process);
        }
    }
    
    public void launchBackupOnTarget(AbstractRecoveryTarget target, Manifest manifest) {
        RecoveryProcess process = target.getProcess();
        ProcessRunner rn = new ProcessRunner(target) {
            public void runCommand() throws ApplicationException {
                rProcess.processBackupOnTarget(rTarget, rManifest, context);
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

    public void launchCompactOnTarget(final boolean keepDeletedEntries, Manifest manifest) {
        // COMPACT
        FileSystemRecoveryTarget target = (FileSystemRecoveryTarget)this.getCurrentObject();
        RecoveryProcess process = target.getProcess();
        ProcessRunner rn = new ProcessRunner(target) {
            public void runCommand() throws ApplicationException {
                rProcess.processCompactOnTarget(rTarget, rFromDate, rToDate, keepDeletedEntries, rManifest, context);
            }
        };
        rn.rProcess = process;
        rn.rFromDate = currentFromDate;    
        rn.rName = RM.getLabel("app.mergearchivesaction.process.message");
        rn.rToDate = currentToDate;     
        rn.rManifest = manifest;
        rn.launch();                    
    }

    public void showManifestEditionFrame(boolean isCompact, AbstractRecoveryTarget target, Manifest manifest) {
        ManifestWindow frm = new ManifestWindow(
                manifest, 
                target,
                isCompact);
        showDialog(frm);
    }
    
    public void showVendorDialog() {
        JavaVendorWindow frm = new JavaVendorWindow();
        showDialog(frm);
    }

    public void showHelpFrame() {
        try {
            URL url = new URL("http://areca.sourceforge.net/help.php?fromApplication=1&currentVersion=" + VersionInfos.getLastVersion().getVersionId());
            OSTool.launchBrowser(url);
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void showDialog(AbstractWindow window) {
        try {
            window.setModal(this.getMainWindow());
            window.setBlockOnOpen(true);
            window.open();
        } catch (Exception e) {
            handleException(e);
        }
    }

    public Object retrieveMissingEncryptionData(AbstractRecoveryTarget tg) {
        MissingEncryptionDataWindow frm = new MissingEncryptionDataWindow(tg);
        showDialog(frm);

        return new Object[] {frm.getAlgo(), frm.getPassword()};
    }

    public void handleException(final String msg, final Throwable e) {
        SecuredRunner.execute(new Runnable() {
            public void run() {
                disableWaitCursor();

                if (e != null) {
                    Logger.defaultLogger().error(e);
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


    public RecoveryProcess getCurrentProcess() {
        if (this.currentObject == null) {
            return null;
        }

        if (RecoveryProcess.class.isAssignableFrom(this.currentObject.getClass())) {
            return (RecoveryProcess)this.currentObject;
        } else if (AbstractRecoveryTarget.class.isAssignableFrom(this.currentObject.getClass())) {
            return ((AbstractRecoveryTarget)this.currentObject).getProcess();
        } else {
            return null;
        }
    }

    public void saveProcess(RecoveryProcess process) {
        try {
            ProcessXMLWriter writer = new ProcessXMLWriter();
            writer.serializeProcess(process);
        } catch (Throwable e1) {
            this.handleException(RM.getLabel("error.groupupdate.message"), e1);
        }
    }

    public AbstractRecoveryTarget getCurrentTarget() {
        if (this.currentObject == null) {
            return null;
        }

        return (AbstractRecoveryTarget)this.currentObject;
    }

    public boolean isCurrentObjectProcess() {
        return (currentObject != null && RecoveryProcess.class.isAssignableFrom(currentObject.getClass()));
    }

    public boolean isCurrentObjectTarget() {
        return (currentObject != null && FileSystemRecoveryTarget.class.isAssignableFrom(currentObject.getClass()));
    }

    public void setCurrentEntry(RecoveryEntry currentEntry) {
        this.currentEntry = currentEntry;
        AppActionReferenceHolder.refresh();
    }

    public RecoveryEntry getCurrentEntry() {
        return currentEntry;
    }

    public String[] getCurrentFilter() {
        return currentFilter;
    }
    
    public void setCurrentFilter(String[] argCurrentFilter) {
        boolean containsRoot = false;
        if (argCurrentFilter != null) {
            for (int i=0; i<argCurrentFilter.length; i++) {
                if (argCurrentFilter[i].equals("/") || argCurrentFilter[i].equals("\\")) {
                    containsRoot = true;
                    break;
                }
            }
        } 
        if (! containsRoot) {
            this.currentFilter = argCurrentFilter;
        } else {
            this.currentFilter = null;
        }
    }
    
    public GregorianCalendar getCurrentFromDate() {
        return currentFromDate;
    }

    public GregorianCalendar getCurrentHistoryDate() {
        return currentHistoryDate;
    }
    public void setCurrentHistoryDate(GregorianCalendar currentHistoryDate) {
        this.currentHistoryDate = currentHistoryDate;
    }
    public Identifiable getCurrentObject() {
        return currentObject;
    }
    public void setCurrentObject(Identifiable currentObject, boolean refreshTree) {
        if (this.currentObject != currentObject) { // Yes, we DO use reference compaisison
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
        this.currentHistoryDate = null;
        this.currentObject = null;
        this.currentToDate = null;

        // Refresh the gui
        if (refreshInterface) {
            this.mainWindow.refresh(true, true);
        }

        ArecaPreferences.setLastWorkspace(workspace.getPath());
    }

    public void enableWaitCursor() {
        if (mainWindow != null) {
            mainWindow.getShell().setCursor(CURSOR_WAIT); // Always set the cursor on the whole window
        }
    }

    public void disableWaitCursor() {
        if (mainWindow != null) {
            mainWindow.getShell().setCursor(null); // Always set the cursor on the whole window
        }
    }

    public void showInformationDialog(
            String message,
            String title,
            boolean longMessage
    ) {
        showDialog(message, title, SWT.OK, SWT.ICON_INFORMATION, longMessage);
    }

    public void showWarningDialog(
            String message,
            String title,
            boolean longMessage
    ) {
        showDialog(message, title, SWT.OK, SWT.ICON_WARNING, longMessage);
    }

    public void showErrorDialog(
            String message,
            String title,
            boolean longMessage
    ) {
        showDialog(message, title, SWT.OK, SWT.ICON_ERROR, longMessage);
    }

    public int showConfirmDialog(
            String message,
            String title,
            int buttons
    ) {
        MessageBox msg = new MessageBox(this.mainWindow.getShell(), buttons | SWT.ICON_QUESTION);
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
            int buttons,
            int type,
            boolean longMessage
    ) {
        if (mainWindow != null) {
            if (longMessage) {
                LongMessageWindow msg = new LongMessageWindow(title, message, type);
                return msg.open();
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
            item.setText("   " + label + "   ");
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

    public void enforceSelectedTarget(AbstractRecoveryTarget target) {
        this.setCurrentObject(target, false);
        this.mainWindow.enforceSelectedTarget(target);
    }

    public void setCurrentDates(GregorianCalendar currentFromDate, GregorianCalendar currentToDate) {
        this.currentFromDate = currentFromDate;
        this.currentToDate = currentToDate;
        AppActionReferenceHolder.refresh();
    }

    private abstract class ProcessRunner implements Runnable {
        public RecoveryProcess rProcess;
        public String rName;
        public AbstractRecoveryTarget rTarget;
        public String rPath;
        public GregorianCalendar rFromDate;
        public GregorianCalendar rToDate;
        public Manifest rManifest;
        public RecoveryEntry rEntry;
        public boolean refreshAfterProcess = true;
        protected ProcessContext context;
        public Object argument;
        protected InfoChannel channel;

        public abstract void runCommand() throws ApplicationException;

        public ProcessRunner(AbstractRecoveryTarget target) {
            this.rTarget = target;
            channel = new InfoChannel(rTarget, mainWindow.getProgressContainer());
            
            GridData infoData = new GridData();
            infoData.grabExcessHorizontalSpace = true;
            infoData.horizontalAlignment = SWT.FILL;
            channel.setLayoutData(infoData);
            mainWindow.getProgressContainer().layout();
            mainWindow.focusOnProgress();
        }

        // Called in the AWT event thread, to update GUI after the command execution
        protected void finishCommand() {
        }

        public void run() {
            addChannel(channel);
            
            try {
                this.context = new ProcessContext(rTarget, channel);

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
                } else {
                    AppActionReferenceHolder.refresh();
                }
                finishCommand();
            } catch (Exception e) {
                registerState(false);
                try {
                    SecuredRunner.execute(mainWindow, new Runnable() {
                        public void run() {
                            mainWindow.refresh(false, false);  
                        }
                    });
                } finally {
                    if (! TaskCancelledException.isTaskCancellation(e)) {
                        handleException(e);
                    } else {
                        channel.print(RM.getLabel("common.processcancelled.label"));
                        context.getTaskMonitor().enforceCompletion();
                    }
                }
            } finally {
                channel.stopRunning(); 
                removeChannel(channel);
            }
        }
        
        private void registerState(boolean running) {
            rTarget.setRunning(running);
        }

        public void launch() {
            Thread th = new Thread(this);
            th.setName("Command Runner : [" + rName + "]");

            // Le thread doit se terminer normalement
            th.setDaemon(false);
            th.start();
        }
    }   
}
