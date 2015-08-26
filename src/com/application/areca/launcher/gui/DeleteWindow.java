package com.application.areca.launcher.gui;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.application.areca.AbstractTarget;
import com.application.areca.TargetGroup;
import com.application.areca.impl.AbstractFileSystemMedium;
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
public class DeleteWindow 
extends AbstractWindow {
    private static final ResourceManager RM = ResourceManager.instance();

    private boolean deleteContent = false;
    private Button chkDeleteContent;
    private boolean ok = false;
    private AbstractTarget target;
    private TargetGroup group;
    
    public DeleteWindow(AbstractTarget target) {
        super();
        this.target = target;
    }
    
    public DeleteWindow(TargetGroup process) {
        super();
        this.group = process;
    }

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.verticalSpacing = 20;
        composite.setLayout(layout);
        
        Label lblImage = new Label(composite, SWT.NONE);
        lblImage.setImage(this.getShell().getDisplay().getSystemImage(SWT.ICON_QUESTION));
        
        Label lblAdvise = new Label(composite, SWT.NONE);
        if (target != null) {
            lblAdvise.setText(RM.getLabel("app.deletetargetaction.confirm.message", new Object[] {target.getName()}));
        } else {
            lblAdvise.setText(RM.getLabel("app.deletegroupactionverbose.confirm.message", new Object[] {group.getName(), group.getLoadedFrom().getSource()}));
        }
        GridData mainData1 = new GridData();
        mainData1.grabExcessHorizontalSpace = true;
        mainData1.horizontalAlignment = SWT.FILL;
        lblAdvise.setLayoutData(mainData1);

        Group grp = new Group(composite, SWT.NONE);
        grp.setText(RM.getLabel("app.deletetargetaction.content.label"));
        GridData mainData4 = new GridData(SWT.FILL, SWT.FILL, true, true);
        mainData4.horizontalSpan = 2;
        grp.setLayoutData(mainData4);
        grp.setLayout(new RowLayout());
        
        chkDeleteContent = new Button(grp, SWT.CHECK);
        chkDeleteContent.setSelection(false);
        if (target != null) {
            AbstractFileSystemMedium medium = (AbstractFileSystemMedium)target.getMedium();
            File tgDir = medium.getFileSystemPolicy().getArchiveDirectory();
            chkDeleteContent.setText(RM.getLabel("app.deletetargetaction.deletecontent.label", new Object[] {FileSystemManager.getDisplayPath(tgDir)}));
        } else {
            chkDeleteContent.setText(RM.getLabel("app.deletegroupaction.deletecontent.label"));            
        }

        SavePanel pnlSave = new SavePanel(RM.getLabel("common.yes.label"), RM.getLabel("common.no.label"), this);
        GridData saveData = new GridData();
        saveData.horizontalSpan = 2;
        saveData.grabExcessHorizontalSpace = true;
        saveData.horizontalAlignment = SWT.FILL;
        pnlSave.buildComposite(composite).setLayoutData(saveData);        
        
        composite.pack();
        return composite;
    }
    
    public String getTitle() {
        if (target != null) {
            return RM.getLabel("app.deletetargetaction.confirm.title");
        } else {
            return RM.getLabel("app.deletegroupaction.confirm.title");
        }
    }

    protected boolean checkBusinessRules() {
        return true;
    }

    protected void saveChanges() {       
        this.deleteContent = chkDeleteContent.getSelection();
        this.ok = true;
        this.hasBeenUpdated = false;
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
    }

    public boolean isDeleteContent() {
        return deleteContent;
    }

    public boolean isOk() {
        return ok;
    }
}
