package com.application.areca.launcher.gui.confimport;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
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
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.SavePanel;
import com.myJava.file.FileSystemManager;

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
public class ImportAdjustmentWindow 
extends AbstractWindow {
	protected FileSystemTarget target;
	protected String initialLocation;
	protected String proposedLocation;
	
	protected Button radLeaveUnchanged;
	protected Button radPickProposed;
	protected Button radChooseOther;	
	
	protected Text txtPath;
	protected Button btnSelect;
	
	protected Group grpChooseOther;

    protected Button btnSave;
    
    protected boolean saved = false;
    protected boolean savedModifyLocation;
    protected String savedProposedLocation;

    public ImportAdjustmentWindow(FileSystemTarget target, String initialLocation, String proposedLocation) {
    	this.target = target;
    	this.initialLocation = initialLocation;
    	this.proposedLocation = proposedLocation;
    }

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout lyt = new GridLayout(1, false);
        lyt.verticalSpacing = 20;
        composite.setLayout(lyt);
        
		Label lblAdvise = new Label(composite, SWT.NONE|SWT.WRAP);
		lblAdvise.setText(RM.getLabel("adjustconf.intro.label", new Object[] {initialLocation, proposedLocation, target.getName(), target.getUid()}));
		GridData labelGridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		labelGridData.widthHint = computeWidth(500);
		lblAdvise.setLayoutData(labelGridData);
		
		Group grpChooseOptions = new Group(composite, SWT.NONE);
		grpChooseOptions.setText(RM.getLabel("adjustconf.question.label"));
		grpChooseOptions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        GridLayout lytOptions = new GridLayout(1, false);
        lytOptions.verticalSpacing = 10;
		grpChooseOptions.setLayout(lytOptions);
       
        radLeaveUnchanged = new Button(grpChooseOptions, SWT.RADIO);
        radLeaveUnchanged.setText(RM.getLabel("adjustconf.leaveunchanged.label", new Object[] {initialLocation}));
        radLeaveUnchanged.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        monitorControl(radLeaveUnchanged);
		
        radPickProposed = new Button(grpChooseOptions, SWT.RADIO);
        radPickProposed.setText(RM.getLabel("adjustconf.pickproposed.label", new Object[] {proposedLocation}));
        radPickProposed.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        monitorControl(radPickProposed);
        
        radChooseOther = new Button(grpChooseOptions, SWT.RADIO);
        radChooseOther.setText(RM.getLabel("adjustconf.chooseother.label"));
        radChooseOther.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        monitorControl(radChooseOther);
		radChooseOther.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event) {
				handleSelection();
			}
		});
		
		grpChooseOther = new Group(grpChooseOptions, SWT.NONE);
		grpChooseOther.setText(RM.getLabel("adjustconf.otherloc.label"));
		grpChooseOther.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		grpChooseOther.setLayout(new GridLayout(2, false));
        
        txtPath = new Text(grpChooseOther, SWT.BORDER);
		GridData dt = new GridData(SWT.FILL, SWT.CENTER, true, false);
		dt.minimumWidth = computeWidth(450);
		txtPath.setLayoutData(dt);
        monitorControl(txtPath);
		txtPath.addFocusListener(new FocusListener() {
			
			public void focusLost(FocusEvent arg0) {
				verifyProposedPath();
			}
			
			public void focusGained(FocusEvent arg0) {
				verifyProposedPath();
			}
		});
		
		btnSelect = new Button(grpChooseOther, SWT.PUSH);
		btnSelect.setText(RM.getLabel("common.browseaction.label"));
		btnSelect.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				String dir = btnSelect.getText();
				String path = Application.getInstance().showDirectoryDialog(dir, ImportAdjustmentWindow.this);
				if (path != null) {
					txtPath.setText(path);
				}
			}
		});
		
		handleSelection();
        
        SavePanel pnlSave = new SavePanel(RM.getLabel("common.import.label"), this);
        pnlSave.setShowCancel(false);
        Composite pnl = pnlSave.buildComposite(composite);
        btnSave = pnlSave.getBtnSave();
        GridData ldPnl = new GridData();
        ldPnl.grabExcessHorizontalSpace = true;
        ldPnl.horizontalAlignment = SWT.FILL;
        ldPnl.horizontalSpan = 2;
        pnl.setLayoutData(ldPnl);
        
        composite.pack();
        return composite;
    }
    
	private void verifyProposedPath() {
		String path = txtPath.getText();
		if (path != null) {
			path = path.trim();
			if (path.length() != 0) {
				path = path.replace('\\', '/');
				while (path.endsWith("/")) {
					path = path.substring(0, path.length() - 2);
				}
				
				if (! path.endsWith("/" + target.getUid())) {
					File newFile = new File(path, target.getUid());
					if (FileSystemManager.exists(newFile)) {
						txtPath.setText(FileSystemManager.getAbsolutePath(newFile));
					}
				}
			}
		}
	}

    public String getTitle() {
        return RM.getLabel("adjustconf.dialog.title");
    }
    
    private void handleSelection() {
    	boolean selected = radChooseOther.getSelection();
    	grpChooseOther.setEnabled(selected);
    	txtPath.setEnabled(selected);
    	btnSelect.setEnabled(selected);
    	verifyProposedPath();
    }

    protected boolean checkBusinessRules() {
        this.resetErrorState(txtPath);   

    	if (radLeaveUnchanged.getSelection()) {
    		return true;
    	} else if (radPickProposed.getSelection()) {
    		return true;
    	} else {
            if (txtPath.getText() == null || txtPath.getText().trim().length() == 0) {
                this.setInError(txtPath, RM.getLabel("error.field.mandatory"));
                return false;
            } else {
            	File path = new File(txtPath.getText());
            	if (! FileSystemManager.exists(path) || ! FileSystemManager.isDirectory(path)) {
                    this.setInError(txtPath, RM.getLabel("error.directory.expected"));
                	return false;
                }
            }
        	
            return true;
    	}
    }

    protected void saveChanges() {
    	saved = true;
    	
    	if (radLeaveUnchanged.getSelection()) {
    		this.savedModifyLocation = false;
    		this.savedProposedLocation = this.initialLocation;
    	} else if (radPickProposed.getSelection()) {
    		this.savedModifyLocation = true;
    		this.savedProposedLocation = this.proposedLocation;
    	} else {
    		this.savedModifyLocation = true;
    		this.savedProposedLocation = this.txtPath.getText();
    	}
        
        this.hasBeenUpdated = false;
        this.close();
    }
    
    public boolean isSavedModifyLocation() {
		return savedModifyLocation;
	}

	public String getSavedProposedLocation() {
		return savedProposedLocation;
	}

	protected void updateState(boolean rulesSatisfied) {
        btnSave.setEnabled(rulesSatisfied);
    }
	
	protected boolean isSaved() {
		return saved;
	}
}
