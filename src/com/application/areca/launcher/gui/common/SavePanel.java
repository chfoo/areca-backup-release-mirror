package com.application.areca.launcher.gui.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

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
public class SavePanel implements Listener {

    private static final int MIN_BUTTON_WIDTH = 80;
    
    protected String saveLabel;
    protected String cancelLabel;
    protected String optionLabel;
    protected AbstractWindow parentWindow;
    protected boolean showCancel =true;
    protected boolean showOptionButton = false;
    
    protected Button btnSave;
    protected Button btnCancel;
    protected Button btnAdd;

    public SavePanel(String saveLabel, String cancelLabel, AbstractWindow parentWindow) {
        this.saveLabel = saveLabel;
        this.cancelLabel = cancelLabel;
        this.parentWindow = parentWindow;
    }
    
    public SavePanel(String saveLabel, AbstractWindow parentWindow) {
        this(
                saveLabel,
                ResourceManager.instance().getLabel("common.cancel.label"),
                parentWindow
        );
    }
    
    public SavePanel(AbstractWindow parentWindow) {
        this(
                ResourceManager.instance().getLabel("common.save.label"),
                parentWindow
        );
    }
    
    public void addOptionButton(String label) {
    	this.optionLabel = ResourceManager.instance().getLabel(label);
    	this.showOptionButton = true;
    }
    
    public Composite buildComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        
        GridLayout layout = new GridLayout(showOptionButton ? 2 : 1, false);
        layout.verticalSpacing = 0;
        layout.marginBottom = 0;
        layout.marginTop = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        
        if (showOptionButton) {
            btnAdd = new Button(composite, SWT.PUSH);
            btnAdd.setText(this.optionLabel);
            GridData ld = new GridData(SWT.LEFT, SWT.CENTER, true, false);
            ld.minimumWidth = AbstractWindow.computeWidth(MIN_BUTTON_WIDTH);
            btnAdd.setLayoutData(ld);
            btnAdd.addListener(SWT.Selection, this);
        }
        
        
        Composite content = buildInnerComposite(composite);
        content.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, false));
        return composite;
    }
    
    public Composite buildInnerComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        
        GridLayout layout = new GridLayout(showCancel ? 2 : 1, true);
        layout.verticalSpacing = 0;
        layout.marginBottom = 0;
        layout.marginTop = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        
        btnSave = new Button(composite, SWT.PUSH);
        btnSave.setText(this.saveLabel);
        GridData ldSave = new GridData(SWT.FILL, SWT.FILL, true, false);
        ldSave.minimumWidth = AbstractWindow.computeWidth(MIN_BUTTON_WIDTH);
        btnSave.setLayoutData(ldSave);
        btnSave.addListener(SWT.Selection, this);
        
        parentWindow.getShell().setDefaultButton(btnSave);
        
        if (showCancel) {
            btnCancel = new Button(composite, SWT.PUSH);        
            btnCancel.setText(this.cancelLabel);
            GridData ldCancel = new GridData(SWT.FILL, SWT.FILL, false, false);
            ldCancel.minimumWidth = AbstractWindow.computeWidth(MIN_BUTTON_WIDTH);
            btnCancel.setLayoutData(ldCancel);
            btnCancel.addListener(SWT.Selection, this);
        }
        
        return composite;
    }

    public Button getBtnCancel() {
        return btnCancel;
    }

    public void setBtnCancel(Button btnCancel) {
        this.btnCancel = btnCancel;
    }

    public Button getBtnSave() {
        return btnSave;
    }
 
    public Button getOptionButton() {
		return btnAdd;
	}

	public void setBtnSave(Button btnSave) {
        this.btnSave = btnSave;
    }
    
    public void handleEvent(Event event) {
        if (event.widget == this.btnCancel) {
            this.parentWindow.cancelChanges();
        } else if (event.widget == this.btnSave) {
            this.parentWindow.saveChanges();
        }
    }

    public void setShowCancel(boolean showCancel) {
        this.showCancel = showCancel;
    }
}
