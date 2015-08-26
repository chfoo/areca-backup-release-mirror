package com.application.areca.launcher.gui;

import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.application.areca.ArecaURLs;
import com.application.areca.launcher.ArecaUserPreferences;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.launcher.gui.common.SavePanel;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.myJava.system.viewer.ViewerHandlerHelper;
import com.myJava.util.log.Logger;

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
public class NewVersionWindow
extends AbstractWindow {
    private static final int widthHint = computeWidth(400);
    private static final int heightHint = computeHeight(150);
    
    private static final ResourceManager RM = ResourceManager.instance();

    private String message;
    private String title;
    private boolean locked;
    private boolean validated= false;
    private Button checkNewVersions;
    
    public NewVersionWindow(String message, boolean newVersion) {
        super();
        this.message = message;
        
        if (newVersion) {
            this.title = RM.getLabel("common.newversion.title");      
            locked = false;
        } else {
            this.title = RM.getLabel("common.versionok.title");
            locked = true;            
        }
    }

    protected Control createContents(Composite parent) {
        application.enableWaitCursor();
        Composite ret = new Composite(parent, SWT.NONE);
        try {
            GridLayout layout = new GridLayout(2, false);
            ret.setLayout(layout);
            
            Label icon = new Label(ret, SWT.NONE);
            icon.setImage(ArecaImages.ICO_BIG);
            GridData dt = new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 2);
            icon.setLayoutData(dt);

            Composite content = new Composite(ret, SWT.NONE);
            content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            Text txt = configurePanel(content, SWT.WRAP);
            txt.setText(message);
            
            checkNewVersions = new Button(ret, SWT.CHECK);
            checkNewVersions.setText(RM.getLabel("preferences.checkversions.label"));
            checkNewVersions.setToolTipText(RM.getLabel("preferences.checkversions.tt"));    
            checkNewVersions.setSelection(ArecaUserPreferences.isCheckNewVersions());
            checkNewVersions.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false)); 
            
            GridData dt3 = new GridData(SWT.CENTER, SWT.BOTTOM, false, false);
            Link lnk = new Link(ret, SWT.NONE);
            lnk.addListener (SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    try {
                    	ViewerHandlerHelper.getViewerHandler().browse(new URL(event.text));
                    } catch (Exception e) {
                        Logger.defaultLogger().error(e);
                    }
                }
            });
            lnk.setText("<A HREF=\"" + ArecaURLs.ARECA_URL + "\">areca-backup.org</A>");
            lnk.setLayoutData(dt3);
            
            SavePanel pnlSave;
            if (! locked) {
                pnlSave = new SavePanel(RM.getLabel("common.yes.label"), RM.getLabel("common.no.label"), this);    
                pnlSave.setShowCancel(true);
            } else {
                pnlSave = new SavePanel(RM.getLabel("common.close.label"), this);    
                pnlSave.setShowCancel(false);
            }
            pnlSave.buildComposite(ret).setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));    
            
            ret.pack();
        } finally {
            application.disableWaitCursor();
        }
        return ret;
    }

    private Text configurePanel(Composite composite, int style) {
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);
        
        GridData dt = new GridData();
        dt.grabExcessHorizontalSpace = true;
        dt.grabExcessVerticalSpace = true;
        dt.verticalAlignment = SWT.FILL;
        dt.horizontalAlignment = SWT.FILL;
        dt.heightHint = heightHint;
        dt.widthHint = widthHint;
        
        Text content = new Text(composite, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER | style);
        content.setEditable(false);
        content.setLayoutData(dt);
        
        return content;
    }

    protected boolean checkBusinessRules() {
        return true;
    }

    public String getTitle() {
        return title;
    }

    protected void cancelChanges() {
        ArecaUserPreferences.setCheckNewVersion(checkNewVersions.getSelection());
        super.cancelChanges();
    }

    protected void saveChanges() {
        ArecaUserPreferences.setCheckNewVersion(checkNewVersions.getSelection());
        validated = ! locked;
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
    }

    public boolean isValidated() {
        return validated;
    }
}
