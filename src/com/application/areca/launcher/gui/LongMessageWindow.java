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
import com.application.areca.launcher.gui.resources.ResourceManager;

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
public class LongMessageWindow 
extends AbstractWindow {
    private static final ResourceManager RM = ResourceManager.instance();

    private String title;
    private boolean closeOnly;
    private String message;
    private int image;
    
    private boolean validated = false;
    
    public LongMessageWindow(String title, String message, int image) {
        this(title, message, true, image);
    }
    
    public LongMessageWindow(String title, String message, boolean closeOnly, int image) {
        super();

        this.title = title;
        this.closeOnly = closeOnly;
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

        SavePanel pnlSave;
        if (closeOnly) {
            pnlSave = new SavePanel(RM.getLabel("common.close.label"), this);
            pnlSave.setShowCancel(false);
        } else {
            pnlSave = new SavePanel(RM.getLabel("common.yes.label"), RM.getLabel("common.no.label"), this);    
            pnlSave.setShowCancel(true);
        }

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
        validated = true;
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
    }

    public boolean isValidated() {
        return validated;
    }
}
