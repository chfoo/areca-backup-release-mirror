package com.application.areca.launcher.gui;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.application.areca.ResourceManager;
import com.application.areca.impl.FileSystemRecoveryTarget;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.SavePanel;
import com.myJava.file.FileSystemManager;

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
public class SourceEditionWindow 
extends AbstractWindow {
    private static final ResourceManager RM = ResourceManager.instance();
    private static final String TITLE = RM.getLabel("sourceedition.dialog.title");
    
    private Text location;
    
    protected File source;  
    protected FileSystemRecoveryTarget currentTarget;
    protected Button btnSave;

    public SourceEditionWindow(File source, FileSystemRecoveryTarget currentTarget) {
        super();
        this.source = source;
        this.currentTarget = currentTarget;
    }

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.verticalSpacing = 20;
        composite.setLayout(layout);
        
        Group grpLocation = new Group(composite, SWT.NONE);
        grpLocation.setText(RM.getLabel("targetedition.sourcedirfield.label"));
        GridLayout grpLayout = new GridLayout(2, false);
        grpLayout.verticalSpacing = 2;
        grpLocation.setLayout(grpLayout);
        grpLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        location = new Text(grpLocation, SWT.BORDER);
        GridData mainData2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        mainData2.widthHint = AbstractWindow.computeWidth(350);
        location.setLayoutData(mainData2);
        monitorControl(location);
        
        Button btnBrowsed = new Button(grpLocation, SWT.PUSH);
        btnBrowsed.setText(RM.getLabel("common.browsedirectoryaction.label"));
        btnBrowsed.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        btnBrowsed.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
               String str = location.getText();
               if (str != null && str.trim().length() != 0) {
                   File f = new File(str);
                   if (FileSystemManager.isFile(f)) {
                       str = FileSystemManager.getAbsolutePath(FileSystemManager.getParentFile(f));
                   }
               }
                String path = Application.getInstance().showDirectoryDialog(str, SourceEditionWindow.this);
                if (path != null) {
                    location.setText(path);
                }
            }
        });
        
        new Label(grpLocation, SWT.NONE);
        
        Button btnBrowsef = new Button(grpLocation, SWT.PUSH);
        btnBrowsef.setText(RM.getLabel("common.browsefileaction.label"));
        btnBrowsef.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        btnBrowsef.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                String path = Application.getInstance().showFileDialog(location.getText(), SourceEditionWindow.this);
                if (path != null) {
                    location.setText(path);
                }
            }
        });

        SavePanel pnlSave = new SavePanel(this);
        GridData saveData = new GridData();
        saveData.grabExcessHorizontalSpace = true;
        saveData.horizontalAlignment = SWT.FILL;
        pnlSave.buildComposite(composite).setLayoutData(saveData);        
        btnSave = pnlSave.getBtnSave();
        
        if (source != null) {
            location.setText(FileSystemManager.getAbsolutePath(source));
        }
        
        composite.pack();
        return composite;
    }

    public String getTitle() {
        return TITLE;
    }

    protected boolean checkBusinessRules() {
        // - REPERTOIRE SOURCE + valider qu'il existe
        this.resetErrorState(location);
        if (this.location.getText() == null || this.location.getText().length() == 0) {
            this.setInError(location);
            return false;
        } else {
            if (! FileSystemManager.exists(new File(location.getText()))) {
                this.setInError(location);
                return false;
            }
        }
        
        return true;
    }

    protected void saveChanges() {
        this.source = new File(this.location.getText());
        this.hasBeenUpdated = false;
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
        btnSave.setEnabled(rulesSatisfied);
    }

    public File getSource() {
        return source;
    }
}
