package com.application.areca.launcher.gui;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.SavePanel;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.myJava.file.FileSystemManager;

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
public class AskBeforeOverwriteWindow 
extends AbstractWindow {
    private static final ResourceManager RM = ResourceManager.instance();
    
    private Button chkRemember;
    private File file;
    
    private boolean remembered = false;
    private boolean overwrite = false;

	public AskBeforeOverwriteWindow(File file) {
		super();
		this.file = file;
	}

	protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.verticalSpacing = 20;
        layout.horizontalSpacing = 20;
        layout.marginWidth = 20;
        composite.setLayout(layout);

        Label lblImage = new Label(composite, SWT.NONE);
        lblImage.setImage(this.getShell().getDisplay().getSystemImage(SWT.ICON_QUESTION));
        
        Label lblMessage = new Label(composite, SWT.NONE);
        lblMessage.setText(RM.getLabel("common.message.overwrite", new Object[] {FileSystemManager.getDisplayPath(file)}));
        lblMessage.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

        new Label(composite, SWT.NONE);
        
        chkRemember = new Button(composite, SWT.CHECK);
        chkRemember.setText(RM.getLabel("common.message.remember"));
        chkRemember.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

        SavePanel pnlSave = new SavePanel(RM.getLabel("common.yes.label"), RM.getLabel("common.no.label"), this);
        pnlSave.setShowCancel(true);
        pnlSave.buildComposite(composite).setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));        
        
        composite.pack();
        return composite;
    }
    
    public String getTitle() {
        return FileSystemManager.getDisplayPath(file);
    }

    protected boolean checkBusinessRules() {
        return true;
    }

    protected void saveChanges() {
    	this.remembered = chkRemember.getSelection();
        this.overwrite = true;
        this.close();
    }

    protected void cancelChanges() {
    	this.remembered = chkRemember.getSelection();
        this.overwrite = false;
        this.close();
	}

	protected void updateState(boolean rulesSatisfied) {
    }

	public boolean isRemembered() {
		return remembered;
	}

	public void setRemembered(boolean remembered) {
		this.remembered = remembered;
	}

	public boolean isOverwrite() {
		return overwrite;
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}
}
