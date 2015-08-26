package com.application.areca.launcher.gui;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.application.areca.launcher.ArecaUserPreferences;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.SavePanel;
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
public class CopyWorkspaceWindow 
extends AbstractWindow {
    private static final ResourceManager RM = ResourceManager.instance();
    
    private Text location;
    private Button removeEncryption;
    private Button saveButton;

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        composite.setLayout(layout);
        
        Label lblAdvise = new Label(composite, SWT.NONE);
        lblAdvise.setText(RM.getLabel("cpws.intro.label"));
        lblAdvise.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        new Label(composite, SWT.NONE);
        
        Group grpLocation = new Group(composite, SWT.NONE);
        grpLocation.setText(RM.getLabel("cpws.location.label"));
        GridLayout grpLayout = new GridLayout(2, false);
        grpLocation.setLayout(grpLayout);
        grpLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        location = new Text(grpLocation, SWT.BORDER);
        location.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        location.setText(ArecaUserPreferences.getLastWorkspaceCopyLocation());
        monitorControl(location);
        
        Button btnBrowse = new Button(grpLocation, SWT.PUSH);
        btnBrowse.setText(RM.getLabel("common.browseaction.label"));
        btnBrowse.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                String path = Application.getInstance().showDirectoryDialog(location.getText(), CopyWorkspaceWindow.this);
                if (path != null) {
                    location.setText(path);
                }
            }
        });
        btnBrowse.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

        removeEncryption = new Button(composite, SWT.CHECK);
        removeEncryption.setText(RM.getLabel("cpws.removeencryption.label"));
        removeEncryption.setSelection(ArecaUserPreferences.getLastWorkspaceCopyMask());
        monitorControl(SWT.Selection, removeEncryption);

        SavePanel pnlSave = new SavePanel(this);
        pnlSave.buildComposite(composite).setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true));        
        saveButton = pnlSave.getBtnSave();
        
        composite.pack();
        return composite;
    }
    
    public String getTitle() {
        return RM.getLabel("cpws.dialog.title");
    }

    protected boolean checkBusinessRules() {
        this.resetErrorState(location);     
        if (this.location.getText() == null || this.location.getText().length() == 0) {
            this.setInError(location, RM.getLabel("error.field.mandatory"));
            return false;
        }
        return true;
    }

    protected void saveChanges() {       
        ArecaUserPreferences.setLastWorkspaceCopyLocation(location.getText());
        ArecaUserPreferences.setLastWorkspaceCopyMask(removeEncryption.getSelection());
        
        application.createWorkspaceCopy(new File(location.getText()), removeEncryption.getSelection());
        
        this.hasBeenUpdated = false;
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
        this.saveButton.setEnabled(rulesSatisfied);
    }
}
