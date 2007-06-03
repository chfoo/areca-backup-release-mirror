package com.application.areca.launcher.gui;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.GregorianCalendar;
import java.util.Iterator;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
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
import com.application.areca.launcher.gui.common.CTabFolderManager;
import com.application.areca.launcher.gui.common.SecuredRunner;
import com.application.areca.launcher.gui.menus.AppActionReferenceHolder;
import com.application.areca.launcher.gui.menus.MenuBuilder;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.version.VersionInfos;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.util.Utilitaire;
import com.myJava.util.log.FileLogProcessor;
import com.myJava.util.log.Logger;
import com.myJava.util.os.OSTool;
import com.myJava.util.taskmonitor.TaskCancelledException;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2162742295696737000
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

    protected static String[] STATUS_LABELS;
    protected static Image[] STATUS_ICONS;  
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

    private Workspace workspace;
    private MainWindow mainWindow;

    private Identifiable currentObject;							// Objet en cours de sélection; il peut s'agir d'un workspace, groupe ou target
    private GregorianCalendar currentFromDate;			// Début de l'intervalle de dates en cours de sélection
    private GregorianCalendar currentToDate;				// Fin de l'intervalle de dates en cours de sélection
    private RecoveryEntry currentEntry;						// Entrée en cours de sélection (utile pour le détail d'une archive)
    private GregorianCalendar currentHistoryDate; 		// En cas d'affichage de l'historique d'une entrée, date en cours de sélection
    private String[] currentFilter;									// En cas de sélection d'un noeud sur le panel de détail d'une archive (répertoire ou Entry réelle), nom de celui ci.
    private boolean latestVersionRecoveryMode;         // Indique si la recovery se fera en dernière version ou non
    
    private UserInformationChannel channel;

    private FileTool fileTool = new FileTool();

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

    public void processCommand(String command) {
        if (command == null) {
            return;
        } else if (command.equals(CMD_ABOUT)) {
            // ABOUT
            AboutWindow about = new AboutWindow();
            showDialog(about);         
        } else if (command.equals(CMD_HELP)) {
            // HELP
            showHelpFrame();
        } else if (command.equals(CMD_BACKUP)) {
            // BACKUP
            if (RecoveryProcess.class.isAssignableFrom(this.getCurrentObject().getClass())) {
                RecoveryProcess process = (RecoveryProcess)this.getCurrentObject();
                ProcessRunner rn = new ProcessRunner() {
                    public void runCommand() throws ApplicationException {
                        rProcess.launchBackup();
                    }
                };
                rn.rProcess = process;
                rn.rName = RM.getLabel("app.backupaction.process.message");
                rn.launch();

            } else if (FileSystemRecoveryTarget.class.isAssignableFrom(this.getCurrentObject().getClass())) {
                launchBackupOnTarget(null);
            }
        } else if (command.equals(CMD_COMPACT)) {
            // COMPACT
            AbstractRecoveryTarget target = this.getCurrentTarget();
            ArchiveMedium medium = target.getMedium();
            if (((AbstractIncrementalFileSystemMedium)medium).isOverwrite()) {
                int result = showConfirmDialog(
                        RM.getLabel("app.mergearchivesaction.confirm.message", new Object[] {Utils.formatDisplayDate(this.currentFromDate), Utils.formatDisplayDate(this.currentToDate)}),
                        RM.getLabel("app.mergearchivesaction.confirm.title"));

                if (result == SWT.YES) {
                    this.launchCompactOnTarget(null);
                }
            } else {
                try {
                    Manifest manifest = this.getCurrentTarget().buildDefaultMergeManifest(this.getCurrentFromDate(), this.getCurrentToDate());
                    this.showManifestEditionFrame(true, manifest);
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
                    ProcessRunner rn = new ProcessRunner() {
                        public void runCommand() throws ApplicationException {
                            rProcess.processDeleteOnTarget(rTarget, rFromDate, context);
                        }
                    };
                    rn.rProcess = process;
                    rn.rTarget = target;
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
            ProcessRunner rn = new ProcessRunner() {
                private RecoveryEntry[] entries;

                public void runCommand() throws ApplicationException {
                    entries = this.rProcess.processSimulateOnTarget(this.rTarget, this.context);
                }

                protected void finishCommand() {
                    SecuredRunner.execute(new Runnable() {
                        public void run() {
                            SimulationWindow frm = new SimulationWindow(entries);
                            showDialog(frm);     
                        }
                    });
                }
            };
            rn.rProcess = this.getCurrentProcess();
            rn.rName = RM.getLabel("app.simulateaction.process.message");
            rn.rTarget = this.getCurrentTarget();
            rn.refreshAfterProcess = false;
            rn.launch();
        } else if (command.equals(CMD_EXIT)) {
            // EXIT
            this.processExit();            
        } else if (command.equals(CMD_OPEN)) {
            // OPEN WORKSPACE
            String path = showDirectoryDialog(null, this.mainWindow);
            if (path != null) {
                try {
                    this.setWorkspace(new Workspace(FileSystemManager.getAbsolutePath(new File(path)), this), true);
                } catch (AdapterException e1) {
                    this.handleException(
                            RM.getLabel("error.loadworkspace.message", new Object[] {e1.getMessage(), e1.getSource()}),
                            e1
                    );
                }                
            }
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
                    ProcessRunner rn = new ProcessRunner() {
                        public void runCommand() throws ApplicationException {
                            rProcess.processRecoverOnTarget(rTarget, (String[])argument, rPath, rFromDate, recoverDeletedEntries, context);
                        }
                    };
                    rn.rProcess = process;
                    rn.rTarget = target;
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
        } else if (command.equals(CMD_RECOVER_ENTRY)) {
            // RECOVER ENTRY
            String path = showDirectoryDialog(null, this.mainWindow);

            if (path != null) {
                if (FileSystemRecoveryTarget.class.isAssignableFrom(this.getCurrentObject().getClass())) {
                    FileSystemRecoveryTarget target = (FileSystemRecoveryTarget)this.getCurrentObject();
                    RecoveryProcess process = target.getProcess();
                    ProcessRunner rn = new ProcessRunner() {
                        public void runCommand() throws ApplicationException {
                            rProcess.processRecoverOnTarget(rTarget, rPath, rFromDate, rEntry, context);
                        }
                    };
                    rn.rProcess = process;
                    rn.rTarget = target;
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
            this.showManifestEditionFrame(false, mf);
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
    
    public void buildBatch() {
        String fileName = "backup_" + Utilitaire.replace(this.getCurrentProcess().getSource(), ".xml", "");
        if (this.isCurrentObjectTarget()) {
            fileName += "_" + this.getCurrentTarget().getId();
        }
        String commentPrefix;
        String commandPrefix;
        if (OSTool.isSystemWindows()) {
            fileName += ".bat";
            commentPrefix = "@REM ";
            commandPrefix = "@";
        } else {
            fileName += ".sh";
            commentPrefix = "# ";
            commandPrefix = "";
        }
        
        String path = showFileDialog(OSTool.getUserHome(), this.mainWindow, fileName, RM.getLabel("app.buildbatch.label"), SWT.SAVE);
        if (path != null) {
            String configPath = FileSystemManager.getAbsolutePath(this.getCurrentProcess().getSourceFile());
            String command = "backup -config \"" + configPath + "\"";
            String comments = commentPrefix + "Backup shortcut generated by Areca v" + VersionInfos.getLastVersion().getVersionId() + " on " + Utils.formatDisplayDate(new GregorianCalendar()) + "\n";
            comments += commentPrefix + "Target Group : \"" + this.getCurrentProcess().getName() + "\"\n";
            if (this.isCurrentObjectTarget()) {
                command += " -target " + this.getCurrentTarget().getId();
                comments += commentPrefix + "Target : \"" + this.getCurrentTarget().getTargetName() + "\"\n";
            }

            File applicationRoot = Utils.getApplicationRoot();

            File executable;
            if (OSTool.isSystemWindows()) {
                executable = new File(applicationRoot, "areca_cl.exe");
            } else {
                executable = new File(applicationRoot, "/bin/run_tui.sh");
            }
            command = commandPrefix + "\"" + FileSystemManager.getAbsolutePath(executable) + "\" " + command;

            try {
                Logger.defaultLogger().info("Creating shell script : " + path);
                this.fileTool.createFile(new File(path), comments + command);

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
        int result = showConfirmDialog(
                RM.getLabel("app.deletegroupaction.confirm.message"),
                RM.getLabel("app.deletegroupaction.confirm.title"),
                SWT.YES | SWT.NO);

        if (result == SWT.YES) {
            this.getWorkspace().removeProcess(this.getCurrentProcess());
            FileSystemManager.delete(this.getCurrentProcess().getSourceFile());
            this.currentObject = null;
            this.mainWindow.refresh(true, true);                   
        }    
    }

    public void showDeleteTarget() {
        int result = showConfirmDialog(
                RM.getLabel("app.deletetargetaction.confirm.message"),
                RM.getLabel("app.deletetargetaction.confirm.title"),
                SWT.YES | SWT.NO);

        if (result == SWT.YES) {
            this.getCurrentProcess().removeTarget(this.getCurrentTarget());
            this.saveProcess(this.getCurrentProcess());
            this.currentObject = null;
            this.mainWindow.refresh(true, true);                   
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

    /**
     * Indique si la VM doit être stoppée au moyen de System.exit(0) ou
     * Si le thread en cours doit simplement être stoppé, ce qui laisse la possibilité
     * aux threads non daemon de continuer à s'exécuter.
     * 
     * @return
     */
    public boolean processExit() {
        this.mainWindow.savePreferences();
        if (! this.channel.isRunning()) {
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

    public void launchBackupOnTarget(Manifest manifest) {
        FileSystemRecoveryTarget target = (FileSystemRecoveryTarget)this.getCurrentObject();
        RecoveryProcess process = target.getProcess();
        ProcessRunner rn = new ProcessRunner() {
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
        rn.rTarget = target;
        rn.launch();           
    }

    public void launchCompactOnTarget(Manifest manifest) {
        // COMPACT
        FileSystemRecoveryTarget target = (FileSystemRecoveryTarget)this.getCurrentObject();
        RecoveryProcess process = target.getProcess();
        ProcessRunner rn = new ProcessRunner() {
            public void runCommand() throws ApplicationException {
                rProcess.processCompactOnTarget(rTarget, rFromDate, rToDate, rManifest, context);
            }
        };
        rn.rProcess = process;
        rn.rTarget = target;
        rn.rFromDate = currentFromDate;    
        rn.rName = RM.getLabel("app.mergearchivesaction.process.message");
        rn.rToDate = currentToDate;     
        rn.rManifest = manifest;
        rn.launch();                    
    }

    public void showManifestEditionFrame(boolean isCompact, Manifest manifest) {
        ManifestWindow frm = new ManifestWindow(
                manifest, 
                isCompact);
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

    public void handleException(String msg, Throwable e) {
        disableWaitCursor();

        if (e != null) {
            Logger.defaultLogger().error(e);
            e.printStackTrace(System.err);
        }

        showErrorDialog(
                msg,
                ResourceManager.instance().getLabel("error.dialog.title"));
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

        // Set the user information channel
        Iterator iter = workspace.getProcessIterator();
        while (iter.hasNext()) {
            RecoveryProcess process = (RecoveryProcess)iter.next();
            process.setInfoChannel(this.channel);
        }

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
            String title
    ) {
        showDialog(message, title, SWT.OK, SWT.ICON_INFORMATION);
    }

    public void showWarningDialog(
            String message,
            String title
    ) {
        showDialog(message, title, SWT.OK, SWT.ICON_WARNING);
    }

    public void showErrorDialog(
            String message,
            String title
    ) {
        showDialog(message, title, SWT.OK, SWT.ICON_ERROR);
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
            int type
    ) {
        if (mainWindow != null) {
            MessageBox msg = new MessageBox(this.mainWindow.getShell(), SWT.OK | type);
            msg.setText(title);
            msg.setMessage(message);
            return msg.open();
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
    
    public String showFileDialog(String dir, AbstractWindow parent) {
        return showFileDialog(dir, parent, null, null, SWT.OPEN);
    }

    public UserInformationChannel getChannel() {
        return channel;
    }

    public void setChannel(UserInformationChannel channel) {
        this.channel = channel;

        // Set the user information channel
        Iterator iter = workspace.getProcessIterator();
        while (iter.hasNext()) {
            RecoveryProcess process = (RecoveryProcess)iter.next();
            process.setInfoChannel(this.channel);
        }
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

        public abstract void runCommand() throws ApplicationException;

        // Called in the AWT event thread, to update GUI after the command execution
        protected void finishCommand() {
        }

        public void run() {
            try {
                this.context = new ProcessContext(rTarget);
                channel.startRunning();
                AppActionReferenceHolder.refresh();
                runCommand();
                channel.stopRunning();  
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
                channel.stopRunning();  
                try {
                    SecuredRunner.execute(mainWindow, new Runnable() {
                        public void run() {
                            mainWindow.refresh(false, false);  
                        }
                    });
                } finally {
                    if (! TaskCancelledException.isTaskCancellation(e)) {
                        channel.logError(RM.getLabel("error.processerror.message"), getExceptionMessage(e), e);
                        handleException(e);
                    } else {
                        channel.logInfo(null, RM.getLabel("common.processcancelled.label"));
                        rProcess.getTaskMonitor().enforceCompletion();
                    }
                }
            }
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
