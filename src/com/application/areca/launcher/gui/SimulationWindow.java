package com.application.areca.launcher.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ApplicationException;
import com.application.areca.EntryArchiveData;
import com.application.areca.RecoveryEntry;
import com.application.areca.Utils;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.metadata.manifest.Manifest;
import com.myJava.util.log.Logger;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4945525256658487980
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
public class SimulationWindow 
extends AbstractWindow
implements Listener {
    
    protected RecoveryEntry[] entries;
    
    protected Table table;
    protected Button btnBackup;
    protected Button btnBackupManifest;
    protected Label lblCreated;
    protected Label lblModified;
    protected Label lblTotalSize;
    
    public SimulationWindow(RecoveryEntry[] entries) {
        super();
        this.entries = entries;
    }

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        composite.setLayout(layout);
        
        GridData mainData1 = new GridData(SWT.FILL, SWT.FILL, true, true);
        mainData1.widthHint = computeWidth(900);
        mainData1.heightHint = computeHeight(600);
        createTopComposite(composite).setLayoutData(mainData1);
        
        GridData mainData2 = new GridData(SWT.FILL, SWT.FILL, true, false);
        createBottomComposite(composite).setLayoutData(mainData2);
        
        GridData saveData = new GridData(SWT.FILL, SWT.FILL, true, false);
        createSaveComposite(composite).setLayoutData(saveData);
        
        initContent();
        composite.pack();
        
        return composite;
    }
    
    private Table createTopComposite(Composite parent) {
        table = new Table(parent, SWT.BORDER);   
        TableColumn col1 = new TableColumn(table, SWT.NONE);
        col1.setWidth(600);
        col1.setMoveable(true);
        col1.setText(RM.getLabel("simulation.filecolumn.label"));
        TableColumn col2 = new TableColumn(table, SWT.NONE);
        col2.setWidth(150);
        col2.setMoveable(true);
        col2.setText(RM.getLabel("simulation.sizecolumn.label"));

        table.setHeaderVisible(true);
        table.setLinesVisible(AbstractWindow.getTableLinesVisible());
        
        GridData dt1 = new GridData();
        dt1.grabExcessHorizontalSpace = true;
        dt1.grabExcessVerticalSpace = true;
        dt1.horizontalAlignment = SWT.FILL;
        dt1.verticalAlignment = SWT.FILL;
        
        table.setLayoutData(dt1);
        
        return table;
    }
    
    private Composite createBottomComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 6;
        composite.setLayout(layout);
        
        Label lblTCreated = new Label(composite, SWT.NONE); 
        lblTCreated.setText(RM.getLabel("simulation.newfilesfield.label"));
        lblCreated = new Label(composite, SWT.NONE);
        GridData dt1 = new GridData();
        dt1.grabExcessHorizontalSpace = true;
        lblCreated.setLayoutData(dt1);
        
        Label lblTModified = new Label(composite, SWT.NONE);
        lblTModified.setText(RM.getLabel("simulation.modifiedfilesfield.label"));
        lblModified = new Label(composite, SWT.NONE);
        GridData dt2 = new GridData();
        dt2.grabExcessHorizontalSpace = true;
        lblModified.setLayoutData(dt2);
        
        Label lblTTotalSize = new Label(composite, SWT.NONE); 
        lblTTotalSize.setText(RM.getLabel("simulation.estimatedsizefield.label"));
        lblTotalSize = new Label(composite, SWT.NONE);
        GridData dt3 = new GridData();
        dt3.grabExcessHorizontalSpace = true;
        lblTotalSize.setLayoutData(dt3);
        
        return composite;
    }
    
    private Composite createSaveComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        
        GridData dt1 = new GridData();
        dt1.grabExcessHorizontalSpace = true;
        dt1.horizontalAlignment = SWT.RIGHT;
        
        // BACKUP           
        btnBackup = new Button(composite, SWT.PUSH);
        btnBackup.setText(RM.getLabel("simulation.backupaction.label"));
        btnBackup.addListener(SWT.Selection, this);
        btnBackup.setLayoutData(dt1);
     
        GridData dt2 = new GridData();
        dt2.horizontalAlignment = SWT.RIGHT;
        
        // BACKUP MANIFEST       
        btnBackupManifest = new Button(composite, SWT.PUSH);
        btnBackupManifest.setText(RM.getLabel("simulation.backupwmaction.label"));
        btnBackupManifest.addListener(SWT.Selection, this);
        btnBackupManifest.setLayoutData(dt2);
        
        return composite;
    }
    
    private void initContent() {        
        long totalSize = 0;
        long nbNew = 0;
        long nbModified = 0;
        long nbDeleted = 0;
        
        for (int i=0; i<entries.length; i++) {
            TableItem item = new TableItem(table, SWT.NONE);

            if (entries[i].getStatus() == EntryArchiveData.STATUS_CREATED) {
                nbNew++;
                totalSize += entries[i].getSize();
                item.setImage(0, ArecaImages.ICO_HISTO_NEW);
            } else if (entries[i].getStatus() == EntryArchiveData.STATUS_DELETED) {
                nbDeleted++;     
                item.setImage(0, ArecaImages.ICO_HISTO_DELETE); 
            } else {
                nbModified++;
                totalSize += entries[i].getSize();
                item.setImage(0, ArecaImages.ICO_HISTO_EDIT); 
            }

            item.setText(0, entries[i].getName());
            item.setText(1, Utils.formatFileSize(entries[i].getSize()));
        }
        
        this.lblCreated.setText("" + nbNew);
        this.lblModified.setText("" + nbModified);
        this.lblTotalSize.setText(Utils.formatFileSize(totalSize));
    }

    public String getTitle() {
        AbstractRecoveryTarget target = (AbstractRecoveryTarget)this.application.getCurrentObject();   
        return RM.getLabel("simulation.dialog.title", new Object[] {target.getTargetName()});
    }

    protected boolean checkBusinessRules() {
        return true;
    }

    protected void saveChanges() {
    }

    protected void updateState(boolean rulesSatisfied) {
    }

    public void handleEvent(Event event) {
        if (event.widget == this.btnBackup) {
            this.close();
            Manifest mf = new Manifest();
            mf.setType(Manifest.TYPE_BACKUP);
            this.application.launchBackupOnTarget(mf);
        } else {
            this.close();
            Manifest mf;
            try {
                mf = ((AbstractIncrementalFileSystemMedium)this.application.getCurrentTarget().getMedium()).buildDefaultBackupManifest();
            } catch (ApplicationException e1) {
                Logger.defaultLogger().error(e1);
                mf = new Manifest();
                mf.setType(Manifest.TYPE_BACKUP);
            }
            
            this.application.showManifestEditionFrame(false, mf);
        }
    }
}
