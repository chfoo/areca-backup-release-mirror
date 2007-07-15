package com.application.areca.launcher.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ResourceManager;
import com.application.areca.impl.EncryptionConfiguration;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.SavePanel;

/**
 * <BR>
 * @author Stephane BRUNEL
 * <BR>
 * <BR>Areca Build ID : -1628055869823963574
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
public class MissingEncryptionDataWindow 
extends AbstractWindow {
    private static final ResourceManager RM = ResourceManager.instance();
    
    private String algo;
    private String password;
    private AbstractRecoveryTarget target;
    private List encryptionAlgorithms = new ArrayList();    
    
    private Text txtPassword;
    protected Combo cboEncryptionAlgorithm;
    private Button saveButton;

    public MissingEncryptionDataWindow(AbstractRecoveryTarget target) {
        super();
        this.target = target;
    }
    
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        
        Label lblIntro = new Label(composite, SWT.WRAP);
        lblIntro.setText(RM.getLabel("med.intro.label", new Object[] {target.getTargetName()}));
        GridData mainData0 = new GridData();
        mainData0.grabExcessHorizontalSpace = true;
        mainData0.widthHint = computeWidth(600);
        mainData0.horizontalAlignment = SWT.FILL;
        mainData0.horizontalSpan = 2;
        lblIntro.setLayoutData(mainData0);
        
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        
        Label lblAlgo = new Label(composite, SWT.NONE);
        lblAlgo.setText(RM.getLabel("med.algo.label"));
        
        this.cboEncryptionAlgorithm = new Combo(composite, SWT.READ_ONLY);
        GridData mainData1 = new GridData();
        mainData1.grabExcessHorizontalSpace = true;
        mainData1.horizontalAlignment = SWT.FILL;
        cboEncryptionAlgorithm.setLayoutData(mainData1);
        
        Iterator algIter = EncryptionConfiguration.getAvailableAlgorithms().iterator();
        while (algIter.hasNext()) {
            String id = (String)algIter.next();
            EncryptionConfiguration conf = EncryptionConfiguration.getParameters(id);
            encryptionAlgorithms.add(conf);
            cboEncryptionAlgorithm.add(conf.getFullName());
        }
        monitorControl(SWT.Selection, cboEncryptionAlgorithm);
        
        Label lblPassword = new Label(composite, SWT.NONE);
        lblPassword.setText(RM.getLabel("med.password.label"));
        
        txtPassword = new Text(composite, SWT.BORDER);
        GridData mainData2 = new GridData();
        mainData2.grabExcessHorizontalSpace = true;
        mainData2.horizontalAlignment = SWT.FILL;
        txtPassword.setLayoutData(mainData2);
        monitorControl(txtPassword);

        SavePanel pnlSave = new SavePanel(this);
        GridData saveData = new GridData();
        saveData.grabExcessHorizontalSpace = true;
        saveData.grabExcessVerticalSpace = true;
        saveData.verticalAlignment = SWT.BOTTOM;
        saveData.horizontalAlignment = SWT.RIGHT;        
        saveData.horizontalSpan = 2;
        pnlSave.buildComposite(composite).setLayoutData(saveData);        
        saveButton = pnlSave.getBtnSave();
        
        composite.pack();
        return composite;
    }
    
    public String getTitle() {
        return RM.getLabel("med.dialog.title");
    }

    protected boolean checkBusinessRules() {
        // ALGO
        this.resetErrorState(cboEncryptionAlgorithm);
        if (this.cboEncryptionAlgorithm.getSelectionIndex() == -1) {
            this.setInError(cboEncryptionAlgorithm);
            return false;
        }    
        
        // PWD
        this.resetErrorState(txtPassword);     
        if (this.txtPassword.getText() == null || this.txtPassword.getText().length() == 0) {
            this.setInError(txtPassword);
            return false;
        }
        
        return true;
    }

    protected void saveChanges() {
        this.algo = ((EncryptionConfiguration)encryptionAlgorithms.get(this.cboEncryptionAlgorithm.getSelectionIndex())).getId();
        this.password = this.txtPassword.getText();
        
        this.hasBeenUpdated = false;
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
        this.saveButton.setEnabled(rulesSatisfied);
    }

    public String getAlgo() {
        return algo;
    }

    public String getPassword() {
        return password;
    }
}
