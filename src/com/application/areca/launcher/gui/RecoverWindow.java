package com.application.areca.launcher.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import com.application.areca.impl.FileSystemTarget;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.SavePanel;
import com.myJava.system.OSTool;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2010, Olivier PETRUCCI.

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
public class RecoverWindow 
extends AbstractWindow {
    private static final ResourceManager RM = ResourceManager.instance();
    
    private Text txtLocation;
    private Button chkCheckRecoveredFiles;
    private Button chkRecoverDeletedEntries;
    private Button btnSave;
    
    private String location;
    private boolean checkRecoveredFiles;
    private boolean recoverDeletedEntries;
    private boolean fullMode;

    public RecoverWindow(boolean fullMode) {
		this.fullMode = fullMode;
	}

	public void setRecoverDeletedEntries(boolean recoverDeletedEntries) {
		this.recoverDeletedEntries = recoverDeletedEntries;
	}

	protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        composite.setLayout(layout);

        final Group grpLocation = new Group(composite, SWT.NONE);
        grpLocation.setText(RM.getLabel("recover.location.label"));
        grpLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        GridLayout grpLayout = new GridLayout(2, false);
        grpLayout.verticalSpacing = 0;
        grpLocation.setLayout(grpLayout);
        
        txtLocation = new Text(grpLocation, SWT.BORDER);
        GridData mainData2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        mainData2.widthHint = computeWidth(350);
        txtLocation.setLayoutData(mainData2);       
        monitorControl(txtLocation);
        
        Button btnBrowse = new Button(grpLocation, SWT.PUSH);
        btnBrowse.setText(RM.getLabel("common.browseaction.label"));
        btnBrowse.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                String path = Application.getInstance().showDirectoryDialog(txtLocation.getText(), RecoverWindow.this);
                if (path != null) {
                    txtLocation.setText(path);
                }
            }
        });
        btnBrowse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        
        if (fullMode) {
	        final Label lblLocation = new Label(grpLocation, SWT.NONE);
	        GridData dtLocation = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
	        lblLocation.setLayoutData(dtLocation);
	        txtLocation.addModifyListener(new ModifyListener() {
	            public void modifyText(ModifyEvent e) {
	                lblLocation.setText("(" + txtLocation.getText() + "/" + FileSystemTarget.RECOVERY_LOCATION_SUFFIX + ")");
	                grpLocation.layout();
	            }
	        });
	        
	        chkRecoverDeletedEntries = new Button(composite, SWT.CHECK);
	        chkRecoverDeletedEntries.setText(RM.getLabel("recover.recoverdeleted.label"));
	        chkRecoverDeletedEntries.setToolTipText(RM.getLabel("recover.recoverdeleted.tt"));
	        chkRecoverDeletedEntries.setLayoutData(new GridData());
	        chkRecoverDeletedEntries.setSelection(recoverDeletedEntries);
	        monitorControl(SWT.Selection, chkRecoverDeletedEntries);
        }
        
        chkCheckRecoveredFiles = new Button(composite, SWT.CHECK);
        chkCheckRecoveredFiles.setText(RM.getLabel("recover.check.label"));
        chkCheckRecoveredFiles.setToolTipText(RM.getLabel("recover.check.tt"));
        chkCheckRecoveredFiles.setLayoutData(new GridData());
        chkCheckRecoveredFiles.setSelection(checkRecoveredFiles);
        monitorControl(SWT.Selection, chkCheckRecoveredFiles);

        SavePanel pnlSave = new SavePanel(RM.getLabel("common.ok.label"), RM.getLabel("common.cancel.label"), this);
        pnlSave.buildComposite(composite).setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));        
        btnSave = pnlSave.getBtnSave();
        
        // INIT DATA
        txtLocation.setText(OSTool.getUserHome());
        
        composite.pack();
        return composite;
    }
    
    public String getTitle() {
        return RM.getLabel("recover.dialog.title");
    }
    
    public String getLocation() {
        return location;
    }
    
    public boolean isCheckRecoveredFiles() {
		return checkRecoveredFiles;
	}
    
    public boolean isRecoverDeletedEntries() {
		return recoverDeletedEntries;
	}

	public void setCheckRecoveredFiles(boolean checkRecoveredFiles) {
		this.checkRecoveredFiles = checkRecoveredFiles;
	}

	protected boolean checkBusinessRules() {
        this.resetErrorState(txtLocation);     
        if (this.txtLocation.getText() == null || this.txtLocation.getText().length() == 0) {
            this.setInError(txtLocation, RM.getLabel("error.field.mandatory"));
            return false;
        }
        return true;
    }

    protected void saveChanges() {    
        this.location = this.txtLocation.getText();
        this.recoverDeletedEntries = this.chkRecoverDeletedEntries == null ? false : this.chkRecoverDeletedEntries.getSelection();
        this.checkRecoveredFiles = this.chkCheckRecoveredFiles.getSelection();
        this.hasBeenUpdated = false;
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
        this.btnSave.setEnabled(rulesSatisfied);
    }
}
