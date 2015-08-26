package com.application.areca.launcher.gui.menus;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.application.areca.launcher.ArecaUserPreferences;

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
public class ToolBarBuilder
extends AppActionReferenceHolder {

    public static void buildMainToolBar(Composite parent) {
        ToolBar bar = new ToolBar(parent, SWT.FLAT);
        
        addOpenItems(bar);
        addSeparator(bar);
        addEditItems(bar);       
        addSeparator(bar);
        addActionItems(bar);
        addSeparator(bar);
        addHelpItems(bar);
        
        bar.pack();
    }
    
    public static void addOpenItems(ToolBar bar) {
        if (! ArecaUserPreferences.isDisplayWSAddress()) {
        	buildToolItem(AC_OPEN, bar);
        }
        buildToolItem(AC_PREFERENCES, bar);       
    }
    
    public static void addEditItems(ToolBar bar) {
        buildToolItem(AC_NEW_TARGET, bar);
        buildToolItem(AC_EDIT_TARGET, bar);   
    }
    
    public static void addActionItems(ToolBar bar) {
        buildToolItem(AC_BACKUP, bar);
        buildToolItem(AC_MERGE, bar);
        buildToolItem(AC_DELETE_ARCHIVES, bar);
        buildToolItem(AC_RECOVER, bar);      
    }
    
    public static void addHelpItems(ToolBar bar) {
        buildToolItem(AC_HELP, bar);     
    }

    private static void buildToolItem(AppAction action, ToolBar parent) {
        action.addToToolBar(parent);
    }
    
    private static void addSeparator(ToolBar parent) {
        new ToolItem(parent, SWT.SEPARATOR);
    }
}
