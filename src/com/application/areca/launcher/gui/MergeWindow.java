package com.application.areca.launcher.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.application.areca.AbstractRecoveryTarget;
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
 Copyright 2005-2009, Olivier PETRUCCI.

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
    protected AbstractRecoveryTarget target;

    protected Text txtTitle;
    protected Text txtDescription;
    protected Button btnKeepDeletedEntries;

    public MergeWindow(Manifest manifest, AbstractRecoveryTarget target) {
        super();
        this.manifest = manifest;
        this.target = target;
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

        new Label(composite, SWT.NONE);
        btnKeepDeletedEntries = new Button(composite, SWT.CHECK);
        btnKeepDeletedEntries.setSelection(false);
        btnKeepDeletedEntries.setText(RM.getLabel("archivedetail.keepdeletedentries.label"));
        btnKeepDeletedEntries.setToolTipText(RM.getLabel("archivedetail.keepdeletedentries.tt"));
        btnKeepDeletedEntries.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
        monitorControl(btnKeepDeletedEntries);
        
        String saveLabel = RM.getLabel("archivedetail.startmergeaction.label");

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
        return RM.getLabel("archivedetail.merge.title");
    }

    protected boolean checkBusinessRules() {
        return true;
    }

    protected void saveChanges() {
        this.manifest.setDescription(this.txtDescription.getText());
        this.manifest.setTitle(this.txtTitle.getText());        
        this.application.launchMergeOnTarget(btnKeepDeletedEntries.getSelection(), this.manifest);
        this.hasBeenUpdated = false;
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
    }
}
