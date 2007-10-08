package com.application.areca.launcher.gui;

import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.application.areca.Utils;
import com.application.areca.context.ProcessReport;
import com.application.areca.impl.FileSystemRecoveryEntry;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.myJava.file.FileSystemManager;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 6222835200985278549
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
public class ReportWindow 
extends AbstractWindow {
    
    protected ProcessReport report;
    
    protected Tree tree;
    
    protected Label lblProcessed;
    protected Label lblFiltered;
    protected Label lblUnfilteredDirs;
    protected Label lblUnfilteredFiles;
    protected Label lblIgnored;
    protected Label lblSaved;
    protected Label lblTime;    
    
    protected Label lblTProcessed;
    protected Label lblTFiltered;
    protected Label lblTUnfilteredDirs;
    protected Label lblTUnfilteredFiles;
    protected Label lblTIgnored;
    protected Label lblTSaved;
    protected Label lblTTime;    
    
    public ReportWindow(ProcessReport report) {
        super();
        this.report = report;
    }

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        composite.setLayout(layout);
        
        Label lblTop = new Label(composite, SWT.NONE);
        lblTop.setText(RM.getLabel("report.filtered.label"));
        
        GridData mainData1 = new GridData(SWT.FILL, SWT.FILL, true, true);
        mainData1.widthHint = computeWidth(900);
        mainData1.heightHint = computeHeight(600);
        createTopComposite(composite).setLayoutData(mainData1);
        
        GridData mainData2 = new GridData(SWT.FILL, SWT.FILL, true, false);
        createBottomComposite(composite).setLayoutData(mainData2);
        
        initContent();
        composite.pack();
        
        return composite;
    }
    
    private Tree createTopComposite(Composite parent) {
        tree = new Tree(parent, SWT.BORDER);   
        tree.setLinesVisible(AbstractWindow.getTableLinesVisible());
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        return tree;
    }
    
    private Composite createBottomComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(6, false);
        layout.horizontalSpacing = 20;
        composite.setLayout(layout);

        // LIGNE 1:
        lblTProcessed = new Label(composite, SWT.NONE);
        lblTProcessed.setText(RM.getLabel("report.processed.label"));
        lblProcessed = new Label(composite, SWT.NONE);
        GridData dt1 = new GridData();
        dt1.grabExcessHorizontalSpace = true;
        lblProcessed.setLayoutData(dt1);
        
        lblTFiltered = new Label(composite, SWT.NONE);
        lblTFiltered.setText(RM.getLabel("report.filtered.label"));
        lblFiltered = new Label(composite, SWT.NONE);
        
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        
        // LIGNE 2 :
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        
        lblTUnfilteredDirs = new Label(composite, SWT.NONE);
        lblTUnfilteredDirs.setText(RM.getLabel("report.dircount.label"));
        lblUnfilteredDirs = new Label(composite, SWT.NONE);
        GridData dt2 = new GridData();
        dt2.grabExcessHorizontalSpace = true;
        lblUnfilteredDirs.setLayoutData(dt2);
        
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        
        // LIGNE 3 :
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        
        lblTUnfilteredFiles = new Label(composite, SWT.NONE);
        lblTUnfilteredFiles.setText(RM.getLabel("report.filecount.label"));
        lblUnfilteredFiles = new Label(composite, SWT.NONE);
        
        lblTSaved = new Label(composite, SWT.NONE);
        lblTSaved.setText(RM.getLabel("report.saved.label"));
        lblSaved = new Label(composite, SWT.NONE);
        GridData dt3 = new GridData();
        dt3.grabExcessHorizontalSpace = true;
        lblSaved.setLayoutData(dt3);
        
        // LIGNE 4 :
        lblTTime = new Label(composite, SWT.NONE);
        lblTTime.setText(RM.getLabel("report.duration.label"));    
        lblTime = new Label(composite, SWT.NONE);
        
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        
        lblTIgnored = new Label(composite, SWT.NONE);
        lblTIgnored.setText(RM.getLabel("report.ignored.label"));
        lblIgnored = new Label(composite, SWT.NONE);
        
        return composite;
    }
    
    private void initContent() {        
        Iterator iter = report.getFilteredEntriesData().getKeyIterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            
            TreeItem keyNode =new TreeItem(tree, SWT.NONE); 
            keyNode.setData(key);
            keyNode.setText(RM.getLabel("report.filtered.label"));
            keyNode.setImage(ArecaImages.ICO_FILTER);
            
            Iterator entries = report.getFilteredEntriesData().getFilteredEntries(key).iterator();
            while (entries.hasNext()) {
                TreeItem item = new TreeItem(keyNode, SWT.NONE);
                
                FileSystemRecoveryEntry entry = (FileSystemRecoveryEntry)entries.next();
                item.setText(entry.getName());
                if (FileSystemManager.isFile(entry.getFile())) {
                    item.setImage(ArecaImages.ICO_FS_FILE);
                } else {
                    item.setImage(ArecaImages.ICO_FS_FOLDER);                        
                }

                item.setText(entry.getName());
                item.setData(entry);
            }
        }
        
        this.lblUnfilteredDirs.setText("" + report.getUnfilteredDirectories());
        this.lblUnfilteredFiles.setText("" + report.getUnfilteredFiles());
        this.lblFiltered.setText("" + report.getFilteredEntries());
        this.lblIgnored.setText("" + report.getIgnoredFiles());
        this.lblSaved.setText("" + report.getSavedFiles());
        this.lblTime.setText(Utils.formatDuration(System.currentTimeMillis() - report.getStartMillis()));
        this.lblProcessed.setText("" + report.getProcessedEntries());
    }

    public String getTitle() {
        return RM.getLabel("report.dialog.title");
    }

    protected boolean checkBusinessRules() {
        return true;
    }

    protected void saveChanges() {
    }

    protected void updateState(boolean rulesSatisfied) {
    }
}
