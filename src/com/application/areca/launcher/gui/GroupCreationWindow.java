package com.application.areca.launcher.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.application.areca.TargetGroup;
import com.application.areca.Utils;
import com.application.areca.impl.AbstractFileSystemMedium;
import com.application.areca.impl.FileSystemTarget;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.SavePanel;

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
public class GroupCreationWindow 
extends AbstractWindow 
implements FocusListener {
    protected Text txtName;
    protected Button btnSave;
   
    protected TargetGroup group;

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        
        Group grpName = new Group(composite, SWT.NONE);
        grpName.setText(RM.removeDots(RM.getLabel("archivedetail.titlefield.label")));
        grpName.setLayout(new GridLayout(1, false));
        grpName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        
        txtName = new Text(grpName, SWT.BORDER);
        txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        txtName.addFocusListener(this);
        monitorControl(txtName);
        
        SavePanel pnlSave = new SavePanel(this);
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

    public String getTitle() {
        return RM.getLabel("groupedition.dialog.title");
    }

    protected boolean checkBusinessRules() {
        this.resetErrorState(txtName);   
        String name = Utils.normalizeFileName(this.txtName.getText());
        // This rules are a little too conservative ... but there is no harm
        if (
        		this.txtName.getText() == null 
        		|| this.txtName.getText().length() == 0 
        		|| this.txtName.getText().startsWith(".")
        		|| this.txtName.getText().endsWith(FileSystemTarget.CONFIG_FILE_EXT)
        		|| this.txtName.getText().endsWith(FileSystemTarget.CONFIG_FILE_EXT_DEPRECATED)
        		|| this.txtName.getText().endsWith(".properties")  
        		|| this.txtName.getText().endsWith(AbstractFileSystemMedium.DATA_DIRECTORY_SUFFIX)  
        		|| this.txtName.getText().endsWith(AbstractFileSystemMedium.MANIFEST_FILE)  
        		|| application.getCurrentTargetGroup().getItem(name) != null
        ) {
            this.setInError(txtName, RM.getLabel("error.reserved.words"));
            return false;
        }
        return true;
    }

    protected void saveChanges() {
        String name = Utils.normalizeFileName(this.txtName.getText());
        this.group = new TargetGroup(name);
        
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

    public TargetGroup getGroup() {
        return group;
    }
}
