package com.application.areca.launcher.gui.menus;

import com.application.areca.EntryStatus;
import com.application.areca.TargetGroup;
import com.application.areca.Workspace;
import com.application.areca.impl.FileSystemTarget;
import com.application.areca.launcher.ArecaUserPreferences;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.common.ActionConstants;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.launcher.gui.common.SecuredRunner;
import com.myJava.system.viewer.ViewerHandlerHelper;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
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
public class AppActionReferenceHolder implements ActionConstants{
    public static AppAction AC_OPEN = new AppAction("app.openworkspaceaction", ArecaImages.ICO_WORKSPACE_OPEN, ArecaImages.ICO_WORKSPACE_OPEN_B, CMD_OPEN);
    public static AppAction AC_BACKUP_WS = new AppAction("app.backupworkspace", CMD_BACKUP_WORKSPACE);
    public static AppAction AC_IMPORT_GROUP = new AppAction("app.importconfaction", CMD_IMPORT_CONF);
    public static AppAction AC_PREFERENCES = new AppAction("app.openpreferencesaction", ArecaImages.ICO_CONFIGURE, ArecaImages.ICO_CONFIGURE_B, CMD_PREFERENCES);
    public static AppAction AC_EXIT= new AppAction("app.quitaction", CMD_EXIT);

    public static AppAction AC_NEW_GROUP = new AppAction("app.newgroupaction", ArecaImages.ICO_PROCESS_NEW, CMD_NEW_GROUP);
    //public static AppAction AC_EDIT_GROUP = new AppAction("app.editgroupaction", ArecaImages.ICO_PROCESS_EDIT, CMD_EDIT_GROUP);
    public static AppAction AC_EDIT_XML = new AppAction("app.editgroupxmlaction", null, CMD_EDIT_XML);
    public static AppAction AC_DEL_GROUP = new AppAction("app.deletegroupaction", CMD_DEL_GROUP);
    public static AppAction AC_NEW_TARGET = new AppAction("app.newtargetaction", ArecaImages.ICO_TARGET_NEW, ArecaImages.ICO_TARGET_NEW_B, CMD_NEW_TARGET);
    public static AppAction AC_EDIT_TARGET = new AppAction("app.edittargetaction", ArecaImages.ICO_TARGET_EDIT, ArecaImages.ICO_TARGET_EDIT_B, CMD_EDIT_TARGET);
    public static AppAction AC_DEL_TARGET =  new AppAction("app.deletetargetaction", CMD_DEL_TARGET);
    public static AppAction AC_DUP_TARGET = new AppAction("app.duplicatetargetaction", CMD_DUPLICATE_TARGET);
    public static AppAction AC_SEARCH = new AppAction("app.searchaction", ArecaImages.ICO_FIND, CMD_SEARCH);

    public static AppAction AC_HISTORY = new AppAction("app.historyaction", ArecaImages.ICO_HISTORY, CMD_HISTORY);
    public static AppAction AC_INDICATORS = new AppAction("app.indicatorsaction", CMD_INDICATORS);
    public static AppAction AC_SIMULATE = new AppAction("app.simulateaction", ArecaImages.ICO_ACT_ARCHIVE, ArecaImages.ICO_ACT_ARCHIVE_B, CMD_SIMULATE);
    public static AppAction AC_BACKUP = new AppAction("app.backupaction", ArecaImages.ICO_ACT_ARCHIVE, ArecaImages.ICO_ACT_ARCHIVE_B, CMD_BACKUP);
    public static AppAction AC_BACKUP_ALL = new AppAction("app.backupallaction", null, null, CMD_BACKUP_ALL);    
    public static AppAction AC_BUILD_BATCH = new AppAction("app.buildbatch", ArecaImages.ICO_FILTER, CMD_BUILD_BATCH);
    public static AppAction AC_BUILD_STRATEGY = new AppAction("app.strategy", ArecaImages.ICO_FILTER, CMD_BUILD_STRATEGY);    
    public static AppAction AC_MERGE = new AppAction("app.mergearchivesaction", ArecaImages.ICO_ACT_MERGE, ArecaImages.ICO_ACT_MERGE_B, CMD_MERGE);
    public static AppAction AC_DELETE_ARCHIVES = new AppAction("app.deletearchivesaction", ArecaImages.ICO_ACT_DELETE, ArecaImages.ICO_ACT_DELETE_B, CMD_DELETE_ARCHIVES);
    public static AppAction AC_RECOVER = new AppAction("app.recoverfilesaction", ArecaImages.ICO_ACT_RESTAURE, ArecaImages.ICO_ACT_RESTAURE_B, CMD_RECOVER);
    public static AppAction AC_CHECK_ARCHIVES = new AppAction("app.checkfilesaction", null, null, CMD_CHECK_ARCHIVES);    

    public static AppAction AC_HELP = new AppAction("app.helpaction", ArecaImages.ICO_HELP, ArecaImages.ICO_HELP_B, CMD_HELP);
    public static AppAction AC_TUTORIAL = new AppAction("app.tutorialaction", null, null, CMD_TUTORIAL);
    public static AppAction AC_ABOUT = new AppAction("app.aboutaction", ArecaImages.ICO_SMALL, null, CMD_ABOUT);
    public static AppAction AC_SUPPORT = new AppAction("app.supportaction", CMD_SUPPORT);
    public static AppAction AC_PLUGINS = new AppAction("app.pluginsaction", CMD_PLUGINS);
    public static AppAction AC_CHECK_VERSION = new AppAction("app.checkversionaction", CMD_CHECK_VERSION);

    public static AppAction AC_VIEW_MANIFEST = new AppAction("app.archivedetailaction", ArecaImages.ICO_ARCHIVE_DETAIL, CMD_VIEW_MANIFEST);
    public static AppAction AC_CLEAR_LOG = new AppAction("app.clearlog", ACTION_CLEAR_LOG);    

    public static AppAction AC_RECOVER_FILTER = new AppAction("app.recoverfilesaction", ArecaImages.ICO_ACT_RESTAURE, CMD_RECOVER_WITH_FILTER);
    public static AppAction AC_RECOVER_FILTER_LATEST = new AppAction("app.recoverfilesaction", ArecaImages.ICO_ACT_RESTAURE, CMD_RECOVER_WITH_FILTER_LATEST);
    public static AppAction AC_RECOVER_HISTORY = new AppAction("app.recoverfilesaction", ArecaImages.ICO_ACT_RESTAURE, CMD_RECOVER_ENTRY_HISTO);
    public static AppAction AC_VIEW_TEXT_HISTORY = new AppAction("app.texteditaction", CMD_VIEW_FILE_AS_TEXT_HISTO);   
    public static AppAction AC_VIEW_HISTORY = new AppAction("app.editaction", CMD_VIEW_FILE_HISTO);  
    public static AppAction AC_VIEW_TEXT = new AppAction("app.texteditaction", CMD_VIEW_FILE_AS_TEXT);   
    public static AppAction AC_VIEW = new AppAction("app.editaction", CMD_VIEW_FILE);  
    public static AppAction AC_COPY_FILENAMES = new AppAction("app.copyfilenames", CMD_COPY_FILENAMES);  
    
    public static AppAction AC_SEARCH_PHYSICAL = new AppAction("mainpanel.physical", CMD_SEARCH_PHYSICAL);    
    public static AppAction AC_SEARCH_LOGICAL = new AppAction("mainpanel.logical", CMD_SEARCH_LOGICAL);    
    

    public static void refresh() {
        SecuredRunner.execute(Application.getInstance().getDisplay(), new Runnable() {
            public void run() {
                Application application = Application.getInstance();
                boolean hasEditionCmd = ArecaUserPreferences.hasEditionCommand();

                if (application.getCurrentObject() == null || Workspace.class.isAssignableFrom(application.getCurrentObject().getClass())) {
                    enableCommands(false);
                    AC_NEW_GROUP.setEnabled(! isBackupWorkspace());
                    AC_NEW_TARGET.setEnabled(! isBackupWorkspace());
                    AC_BACKUP_ALL.setEnabled(true);
                } else if (TargetGroup.class.isAssignableFrom(application.getCurrentObject().getClass())) {
                    boolean available = ! application.getCurrentTargetGroup().isRunning();
                    
                    AC_BUILD_BATCH.setEnabled(true);
                    AC_BUILD_STRATEGY.setEnabled(false);
                    AC_NEW_GROUP.setEnabled(! isBackupWorkspace());
                    AC_NEW_TARGET.setEnabled(! isBackupWorkspace());  
                    
                    AC_BACKUP_ALL.setEnabled(available);
                    AC_DEL_GROUP.setEnabled(available && (! isBackupWorkspace()));
                    AC_BACKUP.setEnabled(available);
                    
                    AC_SIMULATE.setEnabled(false);        
                    AC_MERGE.setEnabled(false);
                    AC_DELETE_ARCHIVES.setEnabled(false);
                    AC_RECOVER.setEnabled(false);
                    AC_CHECK_ARCHIVES.setEnabled(false);
                    AC_INDICATORS.setEnabled(false);
                    AC_HISTORY.setEnabled(false);
                    AC_EDIT_TARGET.setEnabled(false);
                    AC_DUP_TARGET.setEnabled(false);     
                    AC_DEL_TARGET.setEnabled(false);      
                    AC_VIEW_MANIFEST.setEnabled(false);
                    AC_COPY_FILENAMES.setEnabled(false);
                } else if (FileSystemTarget.class.isAssignableFrom(application.getCurrentObject().getClass())) {
                    boolean available = ! application.getCurrentTarget().isRunning();
                    
                    AC_EDIT_XML.setEnabled(available && hasEditionCmd);
                    AC_BUILD_BATCH.setEnabled(true);
                    AC_BUILD_STRATEGY.setEnabled(true);
                    AC_HISTORY.setEnabled(true);   
                    AC_DUP_TARGET.setEnabled(! isBackupWorkspace()); 
                    AC_NEW_GROUP.setEnabled(! isBackupWorkspace());
                    AC_NEW_TARGET.setEnabled(! isBackupWorkspace());   
                    
                    AC_SIMULATE.setEnabled(available);
                    AC_BACKUP.setEnabled(available);
                    AC_BACKUP_ALL.setEnabled(available); 
                    AC_INDICATORS.setEnabled(available);         
                    AC_MERGE.setEnabled(
                            available 
                            && application.areMultipleDatesSelected()
                            && ! application.isLatestVersionRecoveryMode()      
                    );
                    AC_DELETE_ARCHIVES.setEnabled(
                            available 
                            && application.getCurrentDate() != null
                            && ! application.isLatestVersionRecoveryMode()        
                    );
                    AC_RECOVER.setEnabled(
                            available 
                            && application.getCurrentDate() != null
                            && ! application.isLatestVersionRecoveryMode()
                     );
                    AC_CHECK_ARCHIVES.setEnabled(
                            available 
                            && application.getCurrentDate() != null
                            && ! application.isLatestVersionRecoveryMode()
                     );
                    AC_VIEW_MANIFEST.setEnabled(
                            application.getCurrentDate() != null
                    );
                    AC_EDIT_TARGET.setEnabled(available && (! isBackupWorkspace()));
                    AC_DEL_GROUP.setEnabled(available && (! isBackupWorkspace()));
                    AC_DEL_TARGET.setEnabled(available && (! isBackupWorkspace()));
                    AC_RECOVER_FILTER.setEnabled(available);
                    AC_RECOVER_HISTORY.setEnabled(
                            available
                            && application.getCurrentEntryData() != null
                            && application.getCurrentEntryData().getStatus() != EntryStatus.STATUS_DELETED
                            && application.getCurrentEntryData().getStatus() != EntryStatus.STATUS_MISSING                    
                    );

                    AC_VIEW_TEXT_HISTORY.setEnabled(
                            available 
                            && hasEditionCmd
                            && application.getCurrentEntryData() != null
                            && application.getCurrentFilter().isViewable()
                            && application.getCurrentEntryData().getStatus() != EntryStatus.STATUS_DELETED
                            && application.getCurrentEntryData().getStatus() != EntryStatus.STATUS_MISSING
                    );
                    AC_VIEW_TEXT.setEnabled(AC_VIEW_TEXT_HISTORY.isEnabled());
                    
                    AC_VIEW_HISTORY.setEnabled(
                            available 
                            && ViewerHandlerHelper.getViewerHandler().isOpenSupported()  
                            && application.getCurrentEntryData() != null
                            && application.getCurrentFilter().isViewable()
                            && application.getCurrentEntryData().getStatus() != EntryStatus.STATUS_DELETED
                            && application.getCurrentEntryData().getStatus() != EntryStatus.STATUS_MISSING
                    );
                    AC_VIEW.setEnabled(AC_VIEW_HISTORY.isEnabled());
                    
                    AC_COPY_FILENAMES.setEnabled(
                    		application.getCurrentFilter() != null 
                    		&& application.getCurrentFilter().getFilter().length > 0
                    );
                } else {
                    enableCommands(false);
                }
            }
        }
        );
    }

    private static void enableCommands(boolean enabled) {
        AC_SIMULATE.setEnabled(enabled);
        AC_BACKUP.setEnabled(enabled);
        AC_BACKUP_ALL.setEnabled(enabled);
        AC_BUILD_BATCH.setEnabled(enabled);
        AC_BUILD_STRATEGY.setEnabled(enabled);    
        AC_MERGE.setEnabled(enabled);
        AC_DELETE_ARCHIVES.setEnabled(enabled);        
        AC_RECOVER.setEnabled(enabled);
        AC_CHECK_ARCHIVES.setEnabled(enabled);
        AC_INDICATORS.setEnabled(enabled);
        AC_EDIT_XML.setEnabled(enabled);
        AC_EDIT_TARGET.setEnabled(enabled);
        AC_DUP_TARGET.setEnabled(enabled);
        AC_DEL_GROUP.setEnabled(enabled);
        AC_DEL_TARGET.setEnabled(enabled);  
        AC_NEW_TARGET.setEnabled(enabled);
        AC_NEW_GROUP.setEnabled(enabled);          
        AC_HISTORY.setEnabled(enabled);
        AC_VIEW_MANIFEST.setEnabled(enabled);
        AC_COPY_FILENAMES.setEnabled(enabled);
    }
    
    private static boolean isBackupWorkspace() {
    	return 
    	Application.getInstance().getWorkspace() != null
    	&& Application.getInstance().getWorkspace().isBackupWorkspace();
    }
}
