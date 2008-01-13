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

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.SavePanel;
import com.application.areca.metadata.manifest.Manifest;

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
public class BackupWindow 
extends AbstractWindow {

    protected Manifest manifest;
    protected AbstractRecoveryTarget target;

    // MarieB was here !!

    protected Label lblTitle;
    protected Text txtTitle;
    protected Text txtDescription;
    protected Button radFull;
    protected Button radIncremental;
    protected Button radDifferential;
    protected Button chkManifest;

    public BackupWindow(Manifest manifest, AbstractRecoveryTarget target) {
        super();
        this.manifest = manifest;
        this.target = target;
    }

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout lyt = new GridLayout(1, false);
        composite.setLayout(lyt);
        
        boolean incrOk = target.supportsBackupScheme(AbstractRecoveryTarget.BACKUP_SCHEME_INCREMENTAL);
        boolean diffOk = target.supportsBackupScheme(AbstractRecoveryTarget.BACKUP_SCHEME_DIFFERENTIAL);        
        boolean fullOk = target.supportsBackupScheme(AbstractRecoveryTarget.BACKUP_SCHEME_FULL);
        
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

        Group grpManifest = new Group(composite, SWT.NONE);
        grpManifest.setText(RM.getLabel("archivedetail.manifest.label"));
        grpManifest.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        grpManifest.setLayout(new GridLayout(2, false));

        chkManifest = new Button(grpManifest, SWT.CHECK);
        chkManifest.setText(RM.getLabel("archivedetail.addmanifest.label"));
        chkManifest.setToolTipText(RM.getLabel("archivedetail.addmanifest.tooltip"));
        chkManifest.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
        chkManifest.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                updateManifestState();
            }
        });

        lblTitle = new Label(grpManifest, SWT.NONE);
        lblTitle.setText(RM.getLabel("archivedetail.titlefield.label"));

        txtTitle = new Text(grpManifest, SWT.BORDER);
        GridData ldTitle = new GridData();
        ldTitle.grabExcessHorizontalSpace = true;
        ldTitle.horizontalAlignment = SWT.FILL;
        txtTitle.setLayoutData(ldTitle);
        monitorControl(txtTitle);

        new Label(grpManifest, SWT.NONE);

        txtDescription = new Text(grpManifest, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
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

        updateManifestState();

        composite.pack();
        return composite;
    }

    public String getTitle() {
        return RM.getLabel("archivedetail.backup.title");
    }

    protected boolean checkBusinessRules() {
        return true;
    }

    protected void updateManifestState() {
        boolean enabled = chkManifest.getSelection();
        lblTitle.setEnabled(enabled);
        txtTitle.setEnabled(enabled);
        txtDescription.setEnabled(enabled);
    }

    protected void saveChanges() {
        if (chkManifest.getSelection()) {
            this.manifest.setDescription(this.txtDescription.getText());
            this.manifest.setTitle(this.txtTitle.getText());
        } else {
            this.manifest = null;
        }
        
        String backupScheme;
        if (radDifferential.getSelection()) {
            backupScheme = AbstractRecoveryTarget.BACKUP_SCHEME_DIFFERENTIAL;
        } else if (radFull.getSelection()) {
            backupScheme = AbstractRecoveryTarget.BACKUP_SCHEME_FULL;
        } else {
            backupScheme = AbstractRecoveryTarget.BACKUP_SCHEME_INCREMENTAL;
        }

        this.application.launchBackupOnTarget(
                target, 
                this.manifest, 
                backupScheme
        );            
        this.hasBeenUpdated = false;
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
    }
}
