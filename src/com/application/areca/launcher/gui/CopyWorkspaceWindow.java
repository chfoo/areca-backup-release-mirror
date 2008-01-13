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

import com.application.areca.ResourceManager;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ArecaPreferences;
import com.application.areca.launcher.gui.common.SavePanel;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2367131098465853703
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
public class CopyWorkspaceWindow 
extends AbstractWindow {
    private static final ResourceManager RM = ResourceManager.instance();
    
    private Text location;
    private Button removeEncryption;
    private Button saveButton;

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.verticalSpacing = 20;
        composite.setLayout(layout);
        
        Label lblAdvise = new Label(composite, SWT.NONE);
        lblAdvise.setText(RM.getLabel("cpws.intro.label"));
        GridData mainData1 = new GridData();
        mainData1.grabExcessHorizontalSpace = true;
        mainData1.horizontalAlignment = SWT.FILL;
        lblAdvise.setLayoutData(mainData1);
        
        Group grpLocation = new Group(composite, SWT.NONE);
        grpLocation.setText(RM.getLabel("cpws.location.label"));
        GridLayout grpLayout = new GridLayout(2, false);
        grpLocation.setLayout(grpLayout);
        grpLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        location = new Text(grpLocation, SWT.BORDER);
        GridData mainData2 = new GridData();
        mainData2.grabExcessHorizontalSpace = true;
        mainData2.horizontalAlignment = SWT.FILL;
        location.setLayoutData(mainData2);
        location.setText(ArecaPreferences.getLastWorkspaceCopyLocation());
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
        GridData mainData3 = new GridData();
        mainData3.horizontalAlignment = SWT.FILL;
        btnBrowse.setLayoutData(mainData3);

        removeEncryption = new Button(composite, SWT.CHECK);
        removeEncryption.setText(RM.getLabel("cpws.removeencryption.label"));
        GridData mainData4 = new GridData();
        removeEncryption.setLayoutData(mainData4);
        removeEncryption.setSelection(ArecaPreferences.getLastWorkspaceCopyMask());
        monitorControl(SWT.Selection, removeEncryption);

        SavePanel pnlSave = new SavePanel(this);
        GridData saveData = new GridData();
        saveData.grabExcessHorizontalSpace = true;
        saveData.horizontalAlignment = SWT.FILL;
        pnlSave.buildComposite(composite).setLayoutData(saveData);        
        saveButton = pnlSave.getBtnSave();
        
        composite.pack();
        return composite;
    }
    
    public String getTitle() {
        return RM.getLabel("cpws.dialog.title");
    }

    protected boolean checkBusinessRules() {
        // Nom obligatoire
        this.resetErrorState(location);     
        if (this.location.getText() == null || this.location.getText().length() == 0) {
            this.setInError(location);
            return false;
        }
        return true;
    }

    protected void saveChanges() {       
        ArecaPreferences.setLastWorkspaceCopyLocation(location.getText());
        ArecaPreferences.setLastWorkspaceCopyMask(removeEncryption.getSelection());
        
        application.createWorkspaceCopy(new File(location.getText()), removeEncryption.getSelection());
        
        this.hasBeenUpdated = false;
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
        this.saveButton.setEnabled(rulesSatisfied);
    }
}
