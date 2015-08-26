package com.application.areca.launcher.gui;

import java.io.File;

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

import com.application.areca.impl.FileSystemTarget;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.SavePanel;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.myJava.file.FileSystemManager;
import com.myJava.system.OSTool;

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
public class RecoverWindow 
extends AbstractWindow {
    private static final ResourceManager RM = ResourceManager.instance();
    
    private Text txtLocation;
    private Group grpLocation;
    private Group grpOptions;
    private Button chkCheckRecoveredFiles;
    private Button chkRecoverDeletedEntries;
    private Button btnSave;
    private Button radRecoverInSubdirectory;
    private Button radOverwrite;
    private Button radDoNotOverwrite;
    private Button radAskBeforeOverwrite;
    private Button radOverwriteIfNewer;
    private Label lblExisting;
    
    private String location;
    private boolean checkRecoveredFiles;
    private boolean recoverDeletedEntries;
    private boolean appendSubdirectory;
    private boolean alwaysOverwrite;
    private boolean neverOverwrite;
    private boolean askBeforeOverwrite;
    private boolean overwriteIfNewer;
    private boolean fullMode;

    /**
     * FullMode controls the display : 
     * <BR>true for full options (when recovering an entire archive)
     * <BR>false for simple mode (when recovering a single entry)
     * @param fullMode
     */
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

        grpLocation = new Group(composite, SWT.NONE);
        grpLocation.setText(RM.getLabel("recover.location.label"));
        grpLocation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        grpLocation.setLayout(new GridLayout(2, false));
        
        grpOptions =  new Group(composite, SWT.NONE);
        grpOptions.setText(RM.getLabel("recover.options.label"));
        grpOptions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        grpOptions.setLayout(new GridLayout(1, false));
        
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
	        txtLocation.addModifyListener(new ModifyListener() {
	            public void modifyText(ModifyEvent e) {
	            	updateRecoveryLocation(checkExistingFiles());
	            }
	        });
	        
	        lblExisting = new Label(grpLocation, SWT.NONE);
	        
        	radRecoverInSubdirectory = new Button(grpLocation, SWT.RADIO);
        	radRecoverInSubdirectory.setText(RM.getLabel("recover.appenddir.label"));
        	radRecoverInSubdirectory.setToolTipText(RM.getLabel("recover.appenddir.tt"));
        	radRecoverInSubdirectory.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
        	radRecoverInSubdirectory.setSelection(true);
	        monitorControl(SWT.Selection, radRecoverInSubdirectory);
	        radRecoverInSubdirectory.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
	            	updateRecoveryLocation(true);
				}
			});

        	radOverwrite = new Button(grpLocation, SWT.RADIO);
        	radOverwrite.setText(RM.getLabel("recover.alwaysoverride.label"));
        	radOverwrite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
	        monitorControl(SWT.Selection, radOverwrite);
	        
        	radOverwriteIfNewer = new Button(grpLocation, SWT.RADIO);
        	radOverwriteIfNewer.setText(RM.getLabel("recover.overwriteifnewer.label"));
        	radOverwriteIfNewer.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
	        monitorControl(SWT.Selection, radOverwriteIfNewer);
	        
        	radAskBeforeOverwrite = new Button(grpLocation, SWT.RADIO);
        	radAskBeforeOverwrite.setText(RM.getLabel("recover.askoverride.label"));
        	radAskBeforeOverwrite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
	        monitorControl(SWT.Selection, radAskBeforeOverwrite);
	        
        	radDoNotOverwrite = new Button(grpLocation, SWT.RADIO);
        	radDoNotOverwrite.setText(RM.getLabel("recover.neveroverride.label"));
        	radDoNotOverwrite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
	        monitorControl(SWT.Selection, radDoNotOverwrite);
	        
	        chkRecoverDeletedEntries = new Button(grpOptions, SWT.CHECK);
	        chkRecoverDeletedEntries.setText(RM.getLabel("recover.recoverdeleted.label"));
	        chkRecoverDeletedEntries.setToolTipText(RM.getLabel("recover.recoverdeleted.tt"));
	        chkRecoverDeletedEntries.setLayoutData(new GridData());
	        chkRecoverDeletedEntries.setSelection(recoverDeletedEntries);

	        monitorControl(SWT.Selection, chkRecoverDeletedEntries);
        }
        
        chkCheckRecoveredFiles = new Button(grpOptions, SWT.CHECK);
        chkCheckRecoveredFiles.setText(RM.getLabel("recover.check.label"));
        chkCheckRecoveredFiles.setToolTipText(RM.getLabel("recover.check.tt"));
        chkCheckRecoveredFiles.setLayoutData(new GridData());
        chkCheckRecoveredFiles.setSelection(true);
        monitorControl(SWT.Selection, chkCheckRecoveredFiles);

        SavePanel pnlSave = new SavePanel(RM.getLabel("common.ok.label"), RM.getLabel("common.cancel.label"), this);
        pnlSave.buildComposite(composite).setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));        
        btnSave = pnlSave.getBtnSave();
        
        // INIT DATA
        txtLocation.setText(OSTool.getUserHome());
        
        composite.pack();
        return composite;
    }
	
	protected boolean checkExistingFiles() {
		try {
			String[] children = FileSystemManager.list(new File(txtLocation.getText()));
			return (children != null && children.length != 0);
		} catch (Exception e) {
			com.myJava.util.log.Logger.defaultLogger().warn("Error while checking recovery directory", e);
			return true;
		}
	}
	
	protected void updateRecoveryLocation(boolean existingFiles) {
		radAskBeforeOverwrite.setEnabled(existingFiles);
		radDoNotOverwrite.setEnabled(existingFiles);
		radOverwrite.setEnabled(existingFiles);
		radRecoverInSubdirectory.setEnabled(existingFiles);
		
		String txt = "";
		if (existingFiles) {
			lblExisting.setText(RM.getLabel("recover.existingfiles.label"));
			if (radRecoverInSubdirectory.getSelection()) {
				txt = " (" + FileSystemTarget.buildRecoveryFile(txtLocation.getText(), radRecoverInSubdirectory.getSelection()) + ")";
			}
		} else {
			lblExisting.setText(RM.getLabel("recover.empty.label"));
		}

    	radRecoverInSubdirectory.setText(RM.getLabel("recover.appenddir.label") + txt);
        grpLocation.layout();
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

	public boolean isAppendSubdirectory() {
		return appendSubdirectory;
	}
	
	public boolean isAlwaysOverwrite() {
		return alwaysOverwrite;
	}

	public boolean isNeverOverwrite() {
		return neverOverwrite;
	}

	public boolean isAskBeforeOverwrite() {
		return askBeforeOverwrite;
	}

	public boolean isOverwriteIfNewer() {
		return overwriteIfNewer;
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
        this.appendSubdirectory = this.radRecoverInSubdirectory != null && this.radRecoverInSubdirectory.isEnabled() && this.radRecoverInSubdirectory.getSelection();
        this.alwaysOverwrite = this.radOverwrite != null && this.radOverwrite.isEnabled() && this.radOverwrite.getSelection();
        this.neverOverwrite = this.radDoNotOverwrite == null || (!this.radDoNotOverwrite.isEnabled()) || this.radDoNotOverwrite.getSelection();
        this.askBeforeOverwrite = this.radAskBeforeOverwrite != null && this.radAskBeforeOverwrite.isEnabled() && this.radAskBeforeOverwrite.getSelection();
        this.overwriteIfNewer = this.radOverwriteIfNewer != null && this.radOverwriteIfNewer.isEnabled() && this.radOverwriteIfNewer.getSelection();
        this.hasBeenUpdated = false;
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
        this.btnSave.setEnabled(rulesSatisfied);
    }
}
