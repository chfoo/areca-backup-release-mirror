package com.application.areca.launcher.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.SavePanel;
import com.application.areca.metadata.manifest.Manifest;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -6307890396762748969
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
public class ManifestWindow 
extends AbstractWindow {
    
    protected Manifest manifest;
    protected boolean mergeMode;
    
    protected Text txtTitle;
    protected Text txtDescription;
    
    public ManifestWindow(Manifest manifest, boolean mergeMode) {
        super();
        this.manifest = manifest;
        this.mergeMode = mergeMode;
    }

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        
        Label lblTitle = new Label(composite, SWT.NONE);
        lblTitle.setText(RM.getLabel("archivedetail.titlefield.label"));
        
        txtTitle = new Text(composite, SWT.BORDER);
        GridData ldTitle = new GridData();
        ldTitle.grabExcessHorizontalSpace = true;
        ldTitle.horizontalAlignment = SWT.FILL;
        txtTitle.setLayoutData(ldTitle);
        monitorControl(txtTitle);
        Label lblDescription = new Label(composite, SWT.NONE);
        lblDescription.setText(RM.getLabel("archivedetail.descriptionfield.label"));
        
        txtDescription = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData ldDescription = new GridData(SWT.FILL, SWT.FILL, true, true);
        ldDescription.widthHint = computeWidth(400);
        ldDescription.heightHint = computeHeight(100);
        txtDescription.setLayoutData(ldDescription);
        monitorControl(txtDescription);
        
        String saveLabel;
        if (mergeMode) {
            saveLabel = RM.getLabel("archivedetail.startmergeaction.label");
        } else {
            saveLabel = RM.getLabel("archivedetail.startbackupaction.label");
        }
        SavePanel pnlSave = new SavePanel(saveLabel, this);
        Composite pnl = pnlSave.buildComposite(composite);
        GridData ldPnl = new GridData();
        ldPnl.grabExcessHorizontalSpace = true;
        ldPnl.horizontalAlignment = SWT.FILL;
        ldPnl.horizontalSpan = 2;
        pnl.setLayoutData(ldPnl);
        
        if (manifest != null) {
            txtTitle.setText(manifest.getTitle() == null ? "" : manifest.getTitle());
            txtDescription.setText(manifest.getDescription() == null ? "" : manifest.getDescription());
        }
        
        composite.pack();
        return composite;
    }

    public String getTitle() {
        if (mergeMode) {
            return RM.getLabel("archivedetail.merge.title");
        } else {
            return RM.getLabel("archivedetail.backup.title");
        }
    }

    protected boolean checkBusinessRules() {
        return true;
    }

    protected void saveChanges() {
        this.manifest.setDescription(this.txtDescription.getText());
        this.manifest.setTitle(this.txtTitle.getText());        
        
        if (mergeMode) {
            this.application.launchCompactOnTarget(this.manifest);
        } else {
            this.application.launchBackupOnTarget(this.manifest);            
        }
        
        this.hasBeenUpdated = false;
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
    }
}
