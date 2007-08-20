package com.application.areca.launcher.gui;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.application.areca.RecoveryProcess;
import com.application.areca.Utils;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.SavePanel;
import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemManager;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4438212685798161280
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
public class ProcessEditionWindow 
extends AbstractWindow 
implements FocusListener
{
    
    protected Text txtName;
    protected Text txtDescription;
    protected Button btnSave;
    
    protected RecoveryProcess process;
    
    public ProcessEditionWindow(RecoveryProcess process) {
        super();
        this.process = process;
    }

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        
        Label lblName = new Label(composite, SWT.NONE);
        lblName.setText(RM.getLabel("archivedetail.titlefield.label"));
        
        txtName = new Text(composite, SWT.BORDER);
        GridData ldTitle = new GridData();
        ldTitle.grabExcessHorizontalSpace = true;
        ldTitle.horizontalAlignment = SWT.FILL;
        txtName.setLayoutData(ldTitle);
        monitorControl(txtName);
        Label lblDescription = new Label(composite, SWT.NONE);
        lblDescription.setText(RM.getLabel("archivedetail.descriptionfield.label"));
        txtName.addFocusListener(this);
        
        if (this.process != null) {
            this.txtName.setEnabled(false);
            lblName.setEnabled(false);            
        }
        
        txtDescription = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData ldDescription = new GridData(SWT.FILL, SWT.FILL, true, true);
        ldDescription.widthHint = computeWidth(400);
        ldDescription.heightHint = computeHeight(100);
        txtDescription.setLayoutData(ldDescription);
        monitorControl(txtDescription);
        
        SavePanel pnlSave = new SavePanel(this);
        Composite pnl = pnlSave.buildComposite(composite);
        btnSave = pnlSave.getBtnSave();
        GridData ldPnl = new GridData();
        ldPnl.grabExcessHorizontalSpace = true;
        ldPnl.horizontalAlignment = SWT.FILL;
        ldPnl.horizontalSpan = 2;
        pnl.setLayoutData(ldPnl);
        
        if (process != null) {
            this.txtDescription.setText(process.getComments() == null ? "" : process.getComments());
            this.txtName.setText(FileSystemManager.getName(process.getSourceFile()).substring(0, FileSystemManager.getName(process.getSourceFile()).length() - 4));
        }
        
        composite.pack();
        return composite;
    }

    public String getTitle() {
        return RM.getLabel("groupedition.dialog.title");
    }

    protected boolean checkBusinessRules() {
        // Nom obligatoire
        this.resetErrorState(txtName);     
        if (this.txtName.getText() == null || this.txtName.getText().length() == 0) {
            this.setInError(txtName);
            return false;
        }
        return true;
    }

    protected void saveChanges() {
        String suffix = this.txtName.getText();
        if (suffix.toLowerCase().endsWith(".xml")) {
            suffix = suffix.substring(0, suffix.length() - 4);
        }
        if (FileNameUtil.startsWithSeparator(suffix)) {
            suffix = suffix.substring(1);
        }
        suffix = Utils.normalizeFileName(suffix);
        
        if (this.process == null) {
            this.process = new RecoveryProcess(
                    new File(application.getWorkspace().getPath(), suffix + ".xml")
            );
        }
        this.process.setComments(this.txtDescription.getText());
        
        this.hasBeenUpdated = false;
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
        btnSave.setEnabled(rulesSatisfied);
    }

    public void focusGained(FocusEvent e) {
    }

    public void focusLost(FocusEvent e) {
        this.txtName.setText(Utils.normalizeFileName(
                this.txtName.getText()
        ));
    }

    public RecoveryProcess getProcess() {
        return process;
    }
}
