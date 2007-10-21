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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.application.areca.ResourceManager;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.SavePanel;
import com.myJava.file.FileSystemManager;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 5653799526062900358
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
public class CreateBackupShortcutWindow 
extends AbstractWindow {
    private static final ResourceManager RM = ResourceManager.instance();
   
    private String initialFileNameSelected;
    private String initialFileNameAll;
    private String initialDirectory;
    
    private String selectedPath = null;
    private boolean forSelectedOnly = true;
    
    private Text location;
    private Button radSelectedOnly;
    private Button radAll;
    private Button saveButton;

    public CreateBackupShortcutWindow(String initialDirectory, String initialFileNameSelected, String initialFileNameAll) {
        super();
        this.initialFileNameSelected = initialFileNameSelected;
        this.initialFileNameAll = initialFileNameAll;
        this.initialDirectory = initialDirectory;
    }

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        composite.setLayout(layout);
               
        Group grpLocation = new Group(composite, SWT.NONE);
        grpLocation.setText(RM.getLabel("shrtc.location.label"));
        GridLayout grpLayout = new GridLayout(2, false);
        grpLocation.setLayout(grpLayout);
        grpLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        location = new Text(grpLocation, SWT.BORDER);
        GridData mainData2 = new GridData();
        mainData2.grabExcessHorizontalSpace = true;
        mainData2.horizontalAlignment = SWT.FILL;
        mainData2.widthHint = AbstractWindow.computeWidth(300);
        location.setLayoutData(mainData2);
        location.setText(FileSystemManager.getAbsolutePath(new File(initialDirectory, initialFileNameSelected)));
        monitorControl(location);
        
        Button btnBrowse = new Button(grpLocation, SWT.PUSH);
        btnBrowse.setText(RM.getLabel("common.browseaction.label"));
        btnBrowse.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                File f = new File(location.getText());
                String path = application.showFileDialog(FileSystemManager.getParent(f), CreateBackupShortcutWindow.this, FileSystemManager.getName(f), RM.getLabel("app.buildbatch.label"), SWT.SAVE);
                if (path != null) {
                    location.setText(path);
                }
            }
        });
        GridData mainData3 = new GridData();
        mainData3.horizontalAlignment = SWT.FILL;
        btnBrowse.setLayoutData(mainData3);

        radSelectedOnly = new Button(composite, SWT.RADIO);
        radSelectedOnly.setText(RM.getLabel("shrtc.forselected.label"));
        radSelectedOnly.setLayoutData(new GridData());
        radSelectedOnly.setSelection(true);
        radSelectedOnly.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                File loc = new File(location.getText());
                if (FileSystemManager.getName(loc).equals(initialFileNameAll)) {
                    location.setText(FileSystemManager.getAbsolutePath(new File(FileSystemManager.getParentFile(loc), initialFileNameSelected)));
                }
            }
        });
        monitorControl(SWT.Selection, radSelectedOnly);

        radAll = new Button(composite, SWT.RADIO);
        radAll.setText(RM.getLabel("shrtc.forall.label"));
        radAll.setLayoutData(new GridData());
        radAll.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                File loc = new File(location.getText());
                if (FileSystemManager.getName(loc).equals(initialFileNameSelected)) {
                    location.setText(FileSystemManager.getAbsolutePath(new File(FileSystemManager.getParentFile(loc), initialFileNameAll)));
                }
            }
        });
        monitorControl(SWT.Selection, radAll);
        
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
        return RM.getLabel("app.buildbatch.label");
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
        this.selectedPath = location.getText();
        this.forSelectedOnly = radSelectedOnly.getSelection();
        
        this.hasBeenUpdated = false;
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
        this.saveButton.setEnabled(rulesSatisfied);
    }

    public String getSelectedPath() {
        return selectedPath;
    }

    public boolean isForSelectedOnly() {
        return forSelectedOnly;
    }
}
