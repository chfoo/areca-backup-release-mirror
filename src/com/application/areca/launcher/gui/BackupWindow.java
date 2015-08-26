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
import com.application.areca.SupportedBackupTypes;
import com.application.areca.TargetGroup;
import com.application.areca.WorkspaceItem;
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
public class BackupWindow 
extends AbstractWindow {
	protected Manifest manifest;

	protected Text txtTitle;
	protected Text txtDescription;
	protected Button radFull;
	protected Button radIncremental;
	protected Button radDifferential;
	protected Button chkManifest;
	protected Button chkCheckArchive;
	private Text txtLocation;
	private Button btnBrowse;
	private Button radUseDefaultLocation;
	private Button radUseSpecificLocation;

	protected WorkspaceItem scope;
	protected boolean isTarget;
	protected boolean isGroup;

	public BackupWindow(Manifest manifest, WorkspaceItem scope) {
		super();
		this.manifest = manifest;
		this.scope = scope;
		isTarget = scope instanceof AbstractTarget;
		isGroup = scope instanceof TargetGroup;
	}

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		boolean incrOk = false;
		boolean diffOk = false;    
		boolean fullOk = false;

		SupportedBackupTypes types = scope.getSupportedBackupSchemes();

		incrOk = types.isSupported(AbstractTarget.BACKUP_SCHEME_INCREMENTAL);
		diffOk = types.isSupported(AbstractTarget.BACKUP_SCHEME_DIFFERENTIAL);        
		fullOk = types.isSupported(AbstractTarget.BACKUP_SCHEME_FULL);

		radIncremental = new Button(composite, SWT.RADIO);
		radIncremental.setText(RM.getLabel("archivedetail.incremental.label"));
		radIncremental.setToolTipText(RM.getLabel("archivedetail.incremental.tooltip"));
		radIncremental.setSelection(incrOk);
		radIncremental.setEnabled(incrOk);

		radDifferential = new Button(composite, SWT.RADIO);
		radDifferential.setText(RM.getLabel("archivedetail.differential.label"));
		radDifferential.setToolTipText(RM.getLabel("archivedetail.differential.tooltip"));
		radDifferential.setEnabled(diffOk);

		radFull = new Button(composite, SWT.RADIO);
		radFull.setText(RM.getLabel("archivedetail.full.label"));
		radFull.setToolTipText(RM.getLabel("archivedetail.full.tooltip"));
		radFull.setEnabled(fullOk);
		radFull.setSelection(fullOk && (! incrOk));

		new Label(composite, SWT.NONE);

		Group grpCheckArchive = new Group(composite, SWT.NONE);
		grpCheckArchive.setText(RM.getLabel("archivedetail.checkgroup.label"));
		grpCheckArchive.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		grpCheckArchive.setLayout(new GridLayout(3, false));

		chkCheckArchive = new Button(grpCheckArchive, SWT.CHECK);
		chkCheckArchive.setText(RM.getLabel("archivedetail.checkarchive.label"));
		chkCheckArchive.setToolTipText(RM.getLabel("archivedetail.checkarchive.tooltip"));
		chkCheckArchive.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
		monitorControl(chkCheckArchive);

		radUseDefaultLocation = new Button(grpCheckArchive, SWT.RADIO);
		radUseDefaultLocation.setText(RM.getLabel("check.default.label"));
		radUseDefaultLocation.setToolTipText(RM.getLabel("check.default.tt"));
		radUseDefaultLocation.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
		monitorControl(SWT.Selection, radUseDefaultLocation);

		radUseSpecificLocation = new Button(grpCheckArchive, SWT.RADIO);
		radUseSpecificLocation.setText(RM.getLabel("check.specific.label"));
		radUseSpecificLocation.setToolTipText(RM.getLabel("check.specific.tt"));
		radUseSpecificLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		monitorControl(SWT.Selection, radUseSpecificLocation);

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
        
        chkCheckArchive.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
            	switchLocation();
            }
        });
		
		txtLocation = new Text(grpCheckArchive, SWT.BORDER);
		GridData mainData2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		mainData2.widthHint = computeWidth(200);
		txtLocation.setLayoutData(mainData2);       
		monitorControl(txtLocation);

		btnBrowse = new Button(grpCheckArchive, SWT.PUSH);
		btnBrowse.setText(RM.getLabel("common.browseaction.label"));
		btnBrowse.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				String path = Application.getInstance().showDirectoryDialog(txtLocation.getText(), BackupWindow.this);
				if (path != null) {
					txtLocation.setText(path);
				}
			}
		});
		btnBrowse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		new Label(composite, SWT.NONE);

		Group grpManifest = new Group(composite, SWT.NONE);
		grpManifest.setText(RM.getLabel("archivedetail.manifest.label"));
		grpManifest.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		grpManifest.setLayout(new GridLayout(1, false));

		chkManifest = new Button(grpManifest, SWT.CHECK);
		chkManifest.setText(RM.getLabel("archivedetail.addmanifest.label"));
		chkManifest.setToolTipText(RM.getLabel("archivedetail.addmanifest.tooltip"));
		chkManifest.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		chkManifest.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				updateManifestState();
			}
		});

		txtTitle = new Text(grpManifest, SWT.BORDER);
		GridData ldTitle = new GridData();
		ldTitle.grabExcessHorizontalSpace = true;
		ldTitle.horizontalAlignment = SWT.FILL;
		txtTitle.setLayoutData(ldTitle);
		monitorControl(txtTitle);

		txtDescription = new Text(grpManifest, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		GridData ldDescription = new GridData(SWT.FILL, SWT.FILL, true, true);
		ldDescription.widthHint = computeWidth(350);
		ldDescription.heightHint = computeHeight(70);
		ldDescription.horizontalSpan = 1;
		txtDescription.setLayoutData(ldDescription);
		monitorControl(txtDescription);

		String saveLabel = RM.getLabel("archivedetail.startbackupaction.label");
		SavePanel pnlSave = new SavePanel(saveLabel, this);
		Composite pnl = pnlSave.buildComposite(composite);
		GridData ldPnl = new GridData();
		ldPnl.grabExcessHorizontalSpace = true;
		ldPnl.horizontalAlignment = SWT.FILL;
		pnl.setLayoutData(ldPnl);

		if (manifest != null) {
			txtTitle.setText(manifest.getTitle() == null ? "" : manifest.getTitle());
			txtDescription.setText(manifest.getDescription() == null ? "" : manifest.getDescription());
		}

		// Default parameters
		chkCheckArchive.setSelection(true);
        txtLocation.setText(ArecaUserPreferences.getCheckSpecificLocation(scope.getUid()));
        if (ArecaUserPreferences.isCheckForceDefaultLocation(scope.getUid())) {
            radUseDefaultLocation.setSelection(true);
        } else {
            radUseSpecificLocation.setSelection(true);
        }

        switchLocation();
		updateManifestState();

		composite.pack();
		return composite;
	}
	
    private void switchLocation() {
    	if (chkCheckArchive.getSelection()) {
    		radUseDefaultLocation.setEnabled(true);
    		radUseSpecificLocation.setEnabled(true);
        	boolean defaultLocation = radUseDefaultLocation.getSelection();
        	txtLocation.setEnabled(! defaultLocation);
        	btnBrowse.setEnabled(! defaultLocation);
    	} else {
    		radUseDefaultLocation.setEnabled(false);
    		radUseSpecificLocation.setEnabled(false);
        	txtLocation.setEnabled(false);
        	btnBrowse.setEnabled(false);
    	}

    	checkBusinessRules();
    }

	public String getTitle() {
		return RM.getLabel("archivedetail.backup.title");
	}

	protected boolean checkBusinessRules() {
		return true;
	}

	protected void updateManifestState() {
		boolean enabled = chkManifest.getSelection();
		txtTitle.setEnabled(enabled);
		txtDescription.setEnabled(enabled);
	}

	protected void saveChanges() {
		ArecaUserPreferences.setCheckForceDefaultLocation(radUseDefaultLocation.getSelection(), scope.getUid());
		ArecaUserPreferences.setCheckSpecificLocation(txtLocation.getText(), scope.getUid());
		
		if (chkManifest != null && chkManifest.getSelection()) {
			if (this.manifest == null) {
				this.manifest = new Manifest(Manifest.TYPE_BACKUP);
			}
			this.manifest.setDescription(this.txtDescription.getText());
			this.manifest.setTitle(this.txtTitle.getText());
		} else {
			this.manifest = null;
		}

		String backupScheme;
		if (radDifferential.getSelection()) {
			backupScheme = AbstractTarget.BACKUP_SCHEME_DIFFERENTIAL;
		} else if (radFull.getSelection()) {
			backupScheme = AbstractTarget.BACKUP_SCHEME_FULL;
		} else {
			backupScheme = AbstractTarget.BACKUP_SCHEME_INCREMENTAL;
		}
		
		CheckParameters checkParams = new CheckParameters(
				chkCheckArchive.getSelection(),
				true,
				true,
				radUseSpecificLocation.getSelection(),
				txtLocation.getText()
		);

		if (isTarget) {
			this.application.launchBackupOnTarget(
					(AbstractTarget)scope, 
					this.manifest, 
					backupScheme, 
					checkParams
			);     
		} else {
			this.application.launchBackupOnGroup(
					(TargetGroup)scope, 
					this.manifest, 
					backupScheme, 
					checkParams
			);  
		}
		this.hasBeenUpdated = false;
		this.close();
	}

	protected void updateState(boolean rulesSatisfied) {
	}
}
