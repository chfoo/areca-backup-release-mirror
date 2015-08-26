package com.application.areca.launcher.gui.menus;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.application.areca.ArecaConfiguration;
import com.application.areca.launcher.gui.resources.ResourceManager;

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
public class MenuBuilder 
extends AppActionReferenceHolder {
    private static final ResourceManager RM = ResourceManager.instance();
    
    public static Menu buildMainMenu(Shell parent) {
        Menu menu = new Menu(parent, SWT.BAR);
        
        // WORKSPACE
        Menu mnWorkspace = buildSubMenu("menu.workspace", menu);
        add(AC_OPEN, mnWorkspace);
        add(AC_BACKUP_WS, mnWorkspace);
        add(AC_IMPORT_GROUP, mnWorkspace);
        addSeparator(mnWorkspace);
        add(AC_PREFERENCES, mnWorkspace);
        addSeparator(mnWorkspace);
        add(AC_EXIT, mnWorkspace);

        // EDIT
        Menu mnEdit = buildSubMenu("menu.edit", menu);
        add(AC_NEW_GROUP, mnEdit);
        //add(AC_EDIT_GROUP, mnEdit);
        add(AC_DEL_GROUP, mnEdit);
        addSeparator(mnEdit);
        add(AC_NEW_TARGET, mnEdit);
        add(AC_EDIT_TARGET, mnEdit);
        add(AC_DEL_TARGET, mnEdit);
        addSeparator(mnEdit);
        add(AC_DUP_TARGET, mnEdit);
        addSeparator(mnEdit);
        
        // ASSISTANTS
        Menu mnAssist = buildSubMenu("menu.assist", mnEdit);
        add(AC_BUILD_BATCH, mnAssist);
        add(AC_BUILD_STRATEGY, mnAssist);
        
        // ACTION
        Menu mnRun = buildSubMenu("menu.run", menu);
        add(AC_SIMULATE, mnRun);
        add(AC_BACKUP, mnRun);
        addSeparator(mnRun);
        add(AC_MERGE, mnRun);
        add(AC_DELETE_ARCHIVES, mnRun);
        addSeparator(mnRun);
        add(AC_RECOVER, mnRun);
        add(AC_CHECK_ARCHIVES, mnRun);
        addSeparator(mnRun);
        add(AC_BACKUP_ALL, mnRun);
        
        // HELP
        Menu mnHelp = buildSubMenu("menu.help", menu);
        add(AC_HELP, mnHelp);
        add(AC_TUTORIAL, mnHelp);
        addSeparator(mnHelp);
        add(AC_CHECK_VERSION, mnHelp);
        add(AC_PLUGINS, mnHelp); 
        add(AC_SUPPORT, mnHelp);  
        addSeparator(mnHelp);
        add(AC_ABOUT, mnHelp);    
        
        return menu;
    }
    
    public static Menu buildActionContextMenu(Shell parent) {
        Menu menu = new Menu(parent, SWT.POP_UP);
        
        add(AC_MERGE, menu);
        add(AC_DELETE_ARCHIVES, menu);
        addSeparator(menu);
        add(AC_RECOVER, menu);
        add(AC_CHECK_ARCHIVES, menu);
        addSeparator(menu);
        add(AC_VIEW_MANIFEST, menu);  

        return menu;
    }
    
    public static Menu buildGroupContextMenu(Shell parent) {
        Menu menu = new Menu(parent, SWT.POP_UP);
        
        add(AC_BACKUP, menu);
        addSeparator(menu);
        add(AC_NEW_GROUP, menu);      
        //add(AC_EDIT_GROUP, menu);
        add(AC_DEL_GROUP, menu);
        addSeparator(menu);
        add(AC_NEW_TARGET, menu);  
        addSeparator(menu);
        add(AC_BUILD_BATCH, menu);  
        
        return menu;
    }
    
    public static Menu buildTargetContextMenu(Shell parent) {
        Menu menu = new Menu(parent, SWT.POP_UP);

        add(AC_SIMULATE, menu);
        add(AC_BACKUP, menu);     
        addSeparator(menu);
        add(AC_NEW_GROUP, menu);   
        addSeparator(menu);
        add(AC_NEW_TARGET, menu);    
        add(AC_EDIT_TARGET, menu);
        add(AC_EDIT_XML, menu);
        add(AC_DEL_TARGET, menu);
        addSeparator(menu);
        add(AC_DUP_TARGET, menu);   
        addSeparator(menu);
        
        // ASSISTANTS
        Menu mnAssist = buildSubMenu("menu.assist", menu);
        add(AC_BUILD_BATCH, mnAssist);
        add(AC_BUILD_STRATEGY, mnAssist);

        return menu;
    }
    
    public static Menu buildWorkspaceContextMenu(Shell parent) {
        Menu menu = new Menu(parent, SWT.POP_UP);
        
        add(AC_NEW_GROUP, menu);
        
        return menu;
    }
    
    public static Menu buildLogContextMenu(Shell parent) {
        Menu menu = new Menu(parent, SWT.POP_UP);
        
        add(AC_CLEAR_LOG, menu);
        
        return menu;
    }
    
    public static Menu buildArchiveContextMenu(Shell parent) {
        Menu menu = new Menu(parent, SWT.POP_UP);
        
        add(AC_RECOVER_FILTER, menu);
        addSeparator(menu);
        add(AC_COPY_FILENAMES, menu);
        
        return menu;
    }
    
    public static Menu buildArchiveContextMenuLogical(Shell parent) {
        Menu menu = new Menu(parent, SWT.POP_UP);
        
        add(AC_RECOVER_FILTER_LATEST, menu);
        addSeparator(menu);
        add(AC_COPY_FILENAMES, menu);
        addSeparator(menu);
        add(AC_VIEW, menu);
        add(AC_VIEW_TEXT, menu);
        
        return menu;
    }
    
    public static Menu buildHistoryContextMenu(Shell parent) {
        Menu menu = new Menu(parent, SWT.POP_UP);
        
        add(AC_RECOVER_HISTORY, menu);
        addSeparator(menu);
        add(AC_VIEW_HISTORY, menu);
        add(AC_VIEW_TEXT_HISTORY, menu);
        
        return menu;
    }
    
    public static Menu buildSearchContextMenu(Shell parent) {
        Menu menu = new Menu(parent, SWT.POP_UP);
        
        add(AC_SEARCH_PHYSICAL, menu);
        add(AC_SEARCH_LOGICAL, menu);
        
        return menu;
    }
    
    private static Menu buildSubMenu(String resourceKeyPrefix, Menu parent) {
        MenuItem menuItem = new MenuItem(parent, SWT.CASCADE);
        
        String str = RM.getLabel(resourceKeyPrefix + ".label");
        char mnemonic = RM.getChar(resourceKeyPrefix + ".mnemonic");
        
        str = AppAction.addMnemonic(str, mnemonic);

        menuItem.setText(str);
        
        Menu menu = new Menu(menuItem);
        menuItem.setMenu(menu);
        return menu;
    }
    
    private static void addSeparator(Menu menu) {
        new MenuItem(menu, SWT.SEPARATOR);
    }
    
    private static void add(AppAction action, Menu parent) {
        action.addToMenu(parent);
    }
}
