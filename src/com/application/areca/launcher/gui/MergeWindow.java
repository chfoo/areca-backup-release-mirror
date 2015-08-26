package com.application.areca.launcher.gui;

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

import com.application.areca.AbstractTarget;
import com.application.areca.CheckParameters;
import com.application.areca.MergeParameters;
import com.application.areca.launcher.ArecaUserPreferences;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.SavePanel;
import com.application.areca.metadata.manifest.Manifest;

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
public class MergeWindow 
extends AbstractWindow {

    protected Manifest manifest;
    protected AbstractTarget target;

    protected Text txtTitle;
    protected Text txtDescription;
    protected Button btnKeepDeletedEntries;
    
    private Text txtLocation;
    private Button btnBrowse;
    private Button radUseDefaultLocation;
    private Button radUseSpecificLocation;
    
    private Button chkCheckArchive;

    public MergeWindow(Manifest manifest, AbstractTarget target) {
        super();
        this.manifest = manifest;
        this.target = target;
    }

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        
        // Keep deleted entries
        btnKeepDeletedEntries = new Button(composite, SWT.CHECK);
        btnKeepDeletedEntries.setSelection(false);
        btnKeepDeletedEntries.setText(RM.getLabel("archivedetail.keepdeletedentries.label"));
        btnKeepDeletedEntries.setToolTipText(RM.getLabel("archivedetail.keepdeletedentries.tt"));
        btnKeepDeletedEntries.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
        monitorControl(btnKeepDeletedEntries);
        
        // Check
        chkCheckArchive = new Button(composite, SWT.CHECK);
        chkCheckArchive.setSelection(false);
		chkCheckArchive.setText(RM.getLabel("archivedetail.checkmerged.label"));
		chkCheckArchive.setToolTipText(RM.getLabel("archivedetail.checkmerged.tt"));
        chkCheckArchive.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
        chkCheckArchive.setSelection(true);
        monitorControl(chkCheckArchive);
        
		new Label(composite, SWT.NONE);
        
        // Manifest
        Group grpManifest = new Group(composite, SWT.NONE);
        grpManifest.setText(RM.getLabel("archivedetail.manifest.label"));
        GridData ldManifest = new GridData(SWT.FILL, SWT.FILL, true, true);
        grpManifest.setLayoutData(ldManifest);
		grpManifest.setLayout(new GridLayout(1, false));
		
        txtTitle = new Text(grpManifest, SWT.BORDER);
        txtTitle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        monitorControl(txtTitle);

        txtDescription = new Text(grpManifest, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData ldDescription = new GridData(SWT.FILL, SWT.FILL, true, true);
		ldDescription.widthHint = computeWidth(350);
		ldDescription.heightHint = computeHeight(70);
		ldDescription.horizontalSpan = 1;
		txtDescription.setLayoutData(ldDescription);
        monitorControl(txtDescription);
        
		new Label(composite, SWT.NONE);
        
        // Location
        final Group grpLocation = new Group(composite, SWT.NONE);
        grpLocation.setText(RM.getLabel("check.location.label"));
        grpLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        GridLayout grpLayout = new GridLayout(3, false);
        grpLayout.verticalSpacing = 0;
        grpLocation.setLayout(grpLayout);
        
        radUseDefaultLocation = new Button(grpLocation, SWT.RADIO);
        radUseDefaultLocation.setText(RM.getLabel("check.default.label"));
        radUseDefaultLocation.setToolTipText(RM.getLabel("check.default.tt"));
        radUseDefaultLocation.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
        monitorControl(SWT.Selection, radUseDefaultLocation);
        
        radUseSpecificLocation = new Button(grpLocation, SWT.RADIO);
        radUseSpecificLocation.setText(RM.getLabel("check.specific.label"));
        radUseSpecificLocation.setToolTipText(RM.getLabel("check.specific.tt"));
        radUseSpecificLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        monitorControl(SWT.Selection, radUseSpecificLocation);

        txtLocation = new Text(grpLocation, SWT.BORDER);
        GridData mainData2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        mainData2.widthHint = computeWidth(200);
        txtLocation.setLayoutData(mainData2);       
        monitorControl(txtLocation);
        
        btnBrowse = new Button(grpLocation, SWT.PUSH);
        btnBrowse.setText(RM.getLabel("common.browseaction.label"));
        btnBrowse.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                String path = Application.getInstance().showDirectoryDialog(txtLocation.getText(), MergeWindow.this);
                if (path != null) {
                    txtLocation.setText(path);
                }
            }
        });
        btnBrowse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        
        radUseDefaultLocation.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
            	switchLocation();
            }
        });
        
        radUseSpecificLocation.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
            	switchLocation();
            }
        });
        
        String saveLabel = RM.getLabel("archivedetail.startmergeaction.label");

        SavePanel pnlSave = new SavePanel(saveLabel, this);
        Composite pnl = pnlSave.buildComposite(composite);
        GridData ldPnl = new GridData();
        ldPnl.grabExcessHorizontalSpace = true;
        ldPnl.horizontalAlignment = SWT.FILL;
        ldPnl.horizontalSpan = 2;
        pnl.setLayoutData(ldPnl);

        // INIT DATA
        txtLocation.setText(ArecaUserPreferences.getMergeSpecificLocation(application.getCurrentWorkspaceItem().getUid()));
        if (ArecaUserPreferences.getMergeUseSpecificLocation(application.getCurrentWorkspaceItem().getUid())) {
            radUseSpecificLocation.setSelection(true);
        } else {
            radUseDefaultLocation.setSelection(true);
        }
        
        if (manifest != null) {
            txtTitle.setText(manifest.getTitle() == null ? "" : manifest.getTitle());
            txtDescription.setText(manifest.getDescription() == null ? "" : manifest.getDescription());
        }
        
        switchLocation();
        composite.pack();
        return composite;
    }
    
    private void switchLocation() {
    	boolean defaultLocation = radUseDefaultLocation.getSelection();
    	txtLocation.setEnabled(! defaultLocation);
    	btnBrowse.setEnabled(! defaultLocation);
    	checkBusinessRules();
    }

    public String getTitle() {
        return RM.getLabel("archivedetail.merge.title");
    }

    protected boolean checkBusinessRules() {
        return true;
    }

    protected void saveChanges() {
		ArecaUserPreferences.setMergeUseSpecificLocation(radUseSpecificLocation.getSelection(), application.getCurrentWorkspaceItem().getUid());
		ArecaUserPreferences.setMergeSpecificLocation(txtLocation.getText(), application.getCurrentWorkspaceItem().getUid());
		
        this.manifest.setDescription(this.txtDescription.getText());
        this.manifest.setTitle(this.txtTitle.getText());  
        MergeParameters params = new MergeParameters(
        		btnKeepDeletedEntries.getSelection(), 
        		radUseSpecificLocation.getSelection(), 
        		txtLocation.getText()
        );
        
        CheckParameters checkParams = new CheckParameters(
        		chkCheckArchive.getSelection(),  
        		true, 
        		true, 
        		radUseSpecificLocation.getSelection(), 
        		txtLocation.getText()
        );
        
        this.application.launchMergeOnTarget(params, checkParams, this.manifest);
        this.hasBeenUpdated = false;
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
    }
}
