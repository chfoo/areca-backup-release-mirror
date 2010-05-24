package com.application.areca.launcher.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.application.areca.ResourceManager;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ArecaPreferences;
import com.application.areca.launcher.gui.common.SavePanel;
import com.myJava.system.OSTool;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2010, Olivier PETRUCCI.

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
public class JavaVendorWindow 
extends AbstractWindow {
    private static final ResourceManager RM = ResourceManager.instance();
    
    private Button chkDoNotShowAgain;

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.verticalSpacing = 20;
        composite.setLayout(layout);

        Label lblImage = new Label(composite, SWT.NONE);
        lblImage.setImage(this.getShell().getDisplay().getSystemImage(SWT.ICON_WARNING));
        
        Label lblMessage = new Label(composite, SWT.NONE);
        lblMessage.setText(RM.getLabel("common.java.vendor.message", new Object[] {OSTool.getJavaVendor()}));
        lblMessage.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

        chkDoNotShowAgain = new Button(composite, SWT.CHECK);
        chkDoNotShowAgain.setText(RM.getLabel("common.message.donotshow"));
        chkDoNotShowAgain.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));

        SavePanel pnlSave = new SavePanel(RM.getLabel("common.close.label"), this);
        pnlSave.setShowCancel(false);
        pnlSave.buildComposite(composite).setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));        
        
        composite.pack();
        return composite;
    }
    
    public String getTitle() {
        return RM.getLabel("common.java.vendor.title");
    }

    protected boolean checkBusinessRules() {
        return true;
    }

    protected void saveChanges() {    
        ArecaPreferences.setDisplayJavaVendorMessage(! this.chkDoNotShowAgain.getSelection());
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
    }
}
