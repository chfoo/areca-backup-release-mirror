package com.application.areca.launcher.gui;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.application.areca.ArchiveMedium;
import com.application.areca.RecoveryEntry;
import com.application.areca.Utils;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.metadata.manifest.Manifest;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2162742295696737000
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
public class ArchiveWindow
extends AbstractWindow {

    protected Manifest manifest;
    protected Set entries;
    protected ArchiveMedium medium;
    protected RecoveryEntry currentEntry;
    
    public ArchiveWindow(Manifest manifest, Set entries, ArchiveMedium medium) {
        super();
        this.manifest = manifest;
        this.entries = entries;
        this.medium = medium;
    }

    protected Control createContents(Composite parent) {
        Composite ret = new Composite(parent, SWT.NONE);
        try {
            GridLayout layout = new GridLayout(1, false);
            ret.setLayout(layout);

            CTabFolder tabs = new CTabFolder(ret, SWT.BORDER);
            tabs.setSimple(Application.SIMPLE_SUBTABS);
            tabs.setLayout(new FillLayout());
            GridData dt = new GridData(SWT.FILL, SWT.FILL, true, true);
            dt.heightHint = computeHeight(450);
            dt.widthHint = computeWidth(700);
            tabs.setLayoutData(dt);

            CTabItem itm1 = new CTabItem(tabs, SWT.NONE);
            itm1.setText(RM.getLabel("archivedetail.manifest.label") + "    ");
            itm1.setImage(ArecaImages.ICO_TARGET_NEW);
            itm1.setControl(getDataPanel(tabs));

            CTabItem itm2 = new CTabItem(tabs, SWT.NONE);
            itm2.setText(RM.getLabel("archivedetail.archivecontent.label") + "    ");
            itm2.setImage(ArecaImages.ICO_REF_TARGET);
            itm2.setControl(getContentPanel(tabs));

            if (currentEntry == null) {
                tabs.setSelection(0);
            } else {
                tabs.setSelection(1);
            }
        } finally {
            application.disableWaitCursor();
        }
        return ret;
    }

    private Composite getContentPanel(Composite parent) {
        ArchiveExplorer explorer = new ArchiveExplorer(parent);
        explorer.setEntries(entries);
        explorer.setSelectedEntry(this.currentEntry);
        return explorer;
    }
    
    private Composite getDataPanel(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 30;
        composite.setLayout(layout);
        
        // TITLE
        Label lblTitle = new Label(composite, SWT.NONE);
        lblTitle.setText(RM.getLabel("archivedetail.titlefield.label"));
        
        Text txtTitle = new Text(composite, SWT.BORDER);
        GridData ldTitle = new GridData();
        ldTitle.grabExcessHorizontalSpace = true;
        ldTitle.horizontalAlignment = SWT.FILL;
        txtTitle.setLayoutData(ldTitle);
        txtTitle.setEditable(false);
        
        // DATE
        Label lblDate = new Label(composite, SWT.NONE);
        lblDate.setText(RM.getLabel("archivedetail.datefield.label"));
        
        Text txtDate = new Text(composite, SWT.BORDER);
        GridData ldDate = new GridData();
        ldDate.grabExcessHorizontalSpace = true;
        ldDate.horizontalAlignment = SWT.FILL;
        txtDate.setLayoutData(ldDate);
        txtDate.setEditable(false);

        // DESCRIPTION
        Label lblDescription = new Label(composite, SWT.NONE);
        lblDescription.setText(RM.getLabel("archivedetail.descriptionfield.label"));
        
        Text txtDescription = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData ldDescription = new GridData(SWT.FILL, SWT.FILL, true, true);
        ldDescription.minimumHeight = computeHeight(100);
        txtDescription.setLayoutData(ldDescription);
        txtDescription.setEditable(false);
        
        // PROPERTIES
        Label lblProperties = new Label(composite, SWT.NONE);
        lblProperties.setText(RM.getLabel("archivedetail.propertiesfield.label"));
        
        TableViewer viewer = new TableViewer(composite, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
        Table table = viewer.getTable();
        table.setLinesVisible(false);
        table.setHeaderVisible(true);
        GridData ldProperties = new GridData();
        ldProperties.grabExcessHorizontalSpace = true;
        ldProperties.horizontalAlignment = SWT.FILL;
        ldProperties.grabExcessVerticalSpace = true;
        ldProperties.verticalAlignment = SWT.FILL;
        ldProperties.minimumHeight = computeHeight(100);
        table.setLayoutData(ldProperties);
        TableColumn col1 = new TableColumn (table, SWT.NONE);
        col1.setWidth(300);
        col1.setText(RM.getLabel("archivedetail.propertycolumn.label"));
        col1.setMoveable(true);
        TableColumn col2 = new TableColumn (table, SWT.NONE);
        col2.setWidth(100);
        col2.setText(RM.getLabel("archivedetail.valuecolumn.label"));
        col2.setMoveable(true);
        
        // INIT DATA
        if (manifest != null) {
            txtTitle.setText(manifest.getTitle() == null ? "" : manifest.getTitle());
            txtDate.setText(manifest.getDate() == null ? "" : Utils.formatDisplayDate(manifest.getDate()));
            txtDescription.setText(manifest.getDescription() == null ? "" : manifest.getDescription());
            
            Iterator iter = this.manifest.propertyIterator();
            while (iter.hasNext()) {
                String key = (String)iter.next();
                TableItem item = new TableItem(table, SWT.NONE);
                item.setText(0, key + " : ");
                item.setText(1, manifest.getProperty(key));
            }
        }
        
        txtTitle.forceFocus();
        
        composite.pack();
        return composite;
    }

    public void setCurrentEntry(RecoveryEntry currentEntry) {
        this.currentEntry = currentEntry;
    }

    protected boolean checkBusinessRules() {
        return true;
    }

    public String getTitle() {
        return RM.getLabel("archivedetail.archive.title");
    }

    protected void saveChanges() {
    }

    protected void updateState(boolean rulesSatisfied) {
    }
}
