package com.application.areca.launcher.gui.menus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.resources.ResourceManager;

/**
 * <BR>
 * @author Stephane BRUNEL
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
public class AppAction implements SelectionListener {
    private static final ResourceManager RM = ResourceManager.instance();

    private String label;
    private Image icon;
    private Image bigIcon;
    private char mnemonic;
    private int accel = -1;
    private String toolTip;
    private String command;
    private boolean enabled = true;
    
    private List menuItems = new ArrayList();
    private List toolItems = new ArrayList();
    
    public AppAction(String resPrefixKey, String actionCommand) {
        this(resPrefixKey, null, actionCommand);
    }
    
    public AppAction(String resPrefixKey, Image image, String actionCommand) {
        this(resPrefixKey, image, null, actionCommand);
    }

    public AppAction(String resPrefixKey, Image image, Image bigImage, String actionCommand) {
        // Mnemonic
        mnemonic = RM.getChar(resPrefixKey + ".mnemonic");
        
        // Accelerator
        String strAccel = RM.getLabel(resPrefixKey + ".accel", (String)null);
        if (strAccel != null) {
            accel = LegacyActionTools.convertAccelerator(strAccel);
        } 
        
        // Label
        label = normalizeMenuLabel(RM.getLabel(resPrefixKey + ".label"), mnemonic, accel);
        
        // Icon
        if (image != null) {
            icon = image;
        }

        if (bigImage != null) {
            bigIcon = bigImage;
        }

        // Tooltip
        toolTip = RM.getLabel(resPrefixKey + ".tooltip", (String)null);
        
        command = actionCommand;
    }
    
    public static String addMnemonic(String label, char mnemonic) {
        int i = label.toLowerCase().indexOf(Character.toLowerCase(mnemonic));
        if (i != -1) {
            label = label.substring(0, i) + "&" + label.substring(i);
        }
        return label;
    }
    
    private static String normalizeMenuLabel(String label, char mnemonic, int accel) {
        label = addMnemonic(label, mnemonic);
        
        int defaultSize = 30;
        int size = Math.max(label.length(), defaultSize);
        StringBuffer sb = new StringBuffer(label);
        while (sb.length() < size) {
            sb.append(' ');
        }
        
        if (accel != -1) {
            sb.append("\t").append(LegacyActionTools.convertAccelerator(accel));
        }
        
        return sb.toString();
    }
    
    public String getCommand() {
        return command;
    }
    
    public Image getIcon() {
        return icon;
    }
    
    public String getLabel() {
        return label;
    }
    
    public char getMnemonic() {
        return mnemonic;
    }
    
    public String getToolTip() {
        return toolTip;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Image getBigIcon() {
        return bigIcon;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        refreshItems();
    }
    
    private void refreshMenuItem(MenuItem item) {
        item.setEnabled(enabled);
    }
    
    private void refreshToolItem(ToolItem item) {
        item.setEnabled(enabled);
    }
    
    private void refreshItems() {
        Iterator iter = menuItems.iterator();
        while (iter.hasNext()) {
            MenuItem item = (MenuItem)iter.next();
            refreshMenuItem(item);
        }
        
        iter = toolItems.iterator();
        while (iter.hasNext()) {
            ToolItem item = (ToolItem)iter.next();
            refreshToolItem(item);
        }
    }
    
    public void addToMenu(Menu menu) {
        MenuItem item = new MenuItem(menu, SWT.CASCADE);
        item.addSelectionListener(this);
        
        item.setImage(icon);
        item.setText(label);
        if (accel != -1) {
            item.setAccelerator(accel);
        }
        
        refreshMenuItem(item);
        this.menuItems.add(item);
    }
    
    public void addToToolBar(ToolBar parent) {
        ToolItem item = new ToolItem(parent, SWT.PUSH);
        item.addSelectionListener(this);
        
        if (bigIcon != null) {
            item.setImage(bigIcon);
        } else if (icon != null){
            item.setImage(icon);
        }
        item.setToolTipText(toolTip);
        refreshToolItem(item);
        this.toolItems.add(item);
    }

    public void widgetDefaultSelected(SelectionEvent e) {
    }
    
    public void widgetSelected(SelectionEvent e) {
        Application.getInstance().processCommand(this.command);
    }
}
