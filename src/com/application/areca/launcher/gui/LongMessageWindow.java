package com.application.areca.launcher.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.application.areca.ResourceManager;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.SavePanel;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 6892146605129115786
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
public class LongMessageWindow 
extends AbstractWindow {
    private static final ResourceManager RM = ResourceManager.instance();

    private String title;
    private String message;
    private int image;
    
    public LongMessageWindow(String title, String message, int image) {
        super();
        
        this.title = title;
        this.message = message;
        this.image = image;
    }

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.verticalSpacing = 10;
        composite.setLayout(layout);

        Label lblImage = new Label(composite, SWT.NONE);
        lblImage.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        lblImage.setImage(this.getShell().getDisplay().getSystemImage(image));
        
        Text content = new Text(composite, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER | SWT.H_SCROLL);
        content.setEditable(false);
        content.setText(message);
        GridData dt = new GridData(SWT.FILL, SWT.FILL, true, true);
        dt.widthHint = AbstractWindow.computeWidth(450);
        dt.heightHint = AbstractWindow.computeWidth(150);
        content.setLayoutData(dt);

        SavePanel pnlSave = new SavePanel(RM.getLabel("common.close.label"), this);
        pnlSave.setShowCancel(false);
        pnlSave.buildComposite(composite).setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));        
        
        composite.pack();
        return composite;
    }
    
    public String getTitle() {
        return title;
    }

    protected boolean checkBusinessRules() {
        return true;
    }

    protected void saveChanges() {    
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
    }
}
