package com.application.areca.launcher.gui.composites;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Iterator;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ApplicationException;
import com.application.areca.RecoveryEntry;
import com.application.areca.ResourceManager;
import com.application.areca.Utils;
import com.application.areca.cache.ArchiveManifestCache;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.impl.IncrementalDirectoryMedium;
import com.application.areca.impl.IncrementalZipMedium;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.launcher.gui.common.Colors;
import com.application.areca.launcher.gui.common.Refreshable;
import com.application.areca.metadata.content.ArchiveContent;
import com.application.areca.metadata.content.ArchiveContentManager;
import com.application.areca.metadata.manifest.Manifest;
import com.myJava.file.FileSystemDriver;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -2622785387388097396
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
public class PhysicalViewComposite 
extends Composite 
implements SelectionListener, Refreshable { 
    
    private static final FileTool TOOL = FileTool.getInstance();
    
    private Table table;
    private TableViewer viewer;
    private Application application = Application.getInstance();
    
    public PhysicalViewComposite(final CTabFolder parent) {
        super(parent, SWT.NONE);
        setLayout(new FillLayout());

        viewer = new TableViewer(this, SWT.BORDER | SWT.MULTI);
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                application.showArchiveDetail(null);
            }
        });
       
       
        table = viewer.getTable();
        table.setLinesVisible(AbstractWindow.getTableLinesVisible());
        table.setHeaderVisible(true);
        String[] titles = new String[] {
                ResourceManager.instance().getLabel("mainpanel.description.label"),
                ResourceManager.instance().getLabel("mainpanel.date.label"),
                ResourceManager.instance().getLabel("mainpanel.size.label")
        };
        
        for (int i=0; i<titles.length; i++) {
            TableColumn column = new TableColumn (table, SWT.NONE);
            column.setText(titles[i]);
            column.setMoveable(true);
        }

        table.getColumn(1).setWidth(200);
        table.getColumn(0).setWidth(500);
        table.getColumn(2).setWidth(150);

        table.getColumn(2).setAlignment(SWT.RIGHT);
        table.addSelectionListener(this);

        table.setMenu(Application.getInstance().getActionContextMenu());
        
        parent.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                Application.getInstance().setLatestVersionRecoveryMode(parent.getSelectionIndex() != 0);
            }
        });
    }
    
    public void refresh() {
        table.removeAll();
        if (application.isCurrentObjectTarget()) {
            fillTargetData(application.getCurrentTarget());
        }
    }

    public Object getRefreshableKey() {
        return this.getClass().getName();
    }

    private void fillTargetData(AbstractRecoveryTarget target) {
        AbstractIncrementalFileSystemMedium medium = (AbstractIncrementalFileSystemMedium)target.getMedium();
        File[] archives = new File[0];
        try {
            archives = medium.listArchives(null, null);
        } catch (Throwable e) {
            this.application.handleException(e);
        }
        
        if (archives == null) {
            archives = new File[0];
        }
        
        for (int i=archives.length-1; i>=0; i--) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setData(archives[i]);
            
            try {
                Manifest manifest = ArchiveManifestCache.getInstance().getManifest(
                        medium, 
                        archives[i]
                );

                item.setText(1, Utils.formatDisplayDate(manifest.getDate()));
                item.setImage(0, ArecaImages.ICO_FS_FOLDER);
                initText(item, 0, manifest);
                initSize(item, 2, archives[i], medium);
            } catch (ApplicationException e) {
                application.handleException(e);
            }
        }
    }
    
    private void initText(TableItem item, int column, Manifest manifest) {
        if (manifest == null || manifest.getTitle() == null || manifest.getTitle().trim().length() == 0) {
            item.setForeground(column, Colors.C_LIGHT_GRAY);
            //item.setFont(column, italic);
            item.setText(column, ResourceManager.instance().getLabel("mainpanel.nodesc.label"));
        } else {
            item.setForeground(column, Colors.C_BLACK);
            //item.setFont(column, normal);
            item.setText(column, manifest.getTitle());
        }   
    }
    
    private void initSize(TableItem item, int column, File archive, AbstractIncrementalFileSystemMedium medium) {
        if (FileSystemManager.isFile(archive)) {
            item.setForeground(column, Colors.C_BLACK);
            item.setText(column, Utils.formatFileSize(FileSystemManager.length(archive)));
        } else if (FileSystemManager.getAccessEfficiency(archive) > FileSystemDriver.ACCESS_EFFICIENCY_POOR) {
            if (medium instanceof IncrementalDirectoryMedium) {
                try {
                    ArchiveContent ctn = ArchiveContentManager.getContentForArchive(medium, archive);
                    Iterator iter = ctn.getContent();
                    long size = 0;
                    while (iter.hasNext()) {
                        RecoveryEntry entry = (RecoveryEntry)iter.next();
                        size += entry.getSize();
                    }
                    item.setForeground(column, Colors.C_BLACK);
                    item.setText(column, Utils.formatFileSize(size));
                } catch (IOException e) {
                    item.setText(column, e.getMessage());
                }
            } else if (medium instanceof IncrementalZipMedium) {
                item.setForeground(column, Colors.C_BLACK);
                try {
                    item.setText(column, Utils.formatFileSize(TOOL.getSize(archive)));
                } catch (FileNotFoundException e) {
                    item.setText(column, Utils.formatFileSize(0));
                }
            } else {
                throw new IllegalArgumentException("Unsupported medium implementation");
            }
        } else {
            item.setForeground(column, Colors.C_LIGHT_GRAY);
            item.setText(column, ResourceManager.instance().getLabel("mainpanel.nosize.label"));
        }
    }

    public void widgetDefaultSelected(SelectionEvent e) {
    }

    public void widgetSelected(SelectionEvent e) {
        TableItem[] items = table.getSelection();
        GregorianCalendar first = null;
        GregorianCalendar last = null;
        for (int i=0; i<items.length; i++) {
            File f = (File)items[i].getData();
            Manifest mf = null;
            try {
                mf = ArchiveManifestCache.getInstance().getManifest((AbstractIncrementalFileSystemMedium)Application.getInstance().getCurrentTarget().getMedium(), f);
            } catch (ApplicationException e1) {
                Application.getInstance().handleException(e1);
            }
            if (mf != null && mf.getDate() != null) {
                GregorianCalendar date = mf.getDate();
                if (first == null || date.before(first)) {
                    first = date;
                }
                if (last == null || date.after(last)) {
                    last = date;
                }
            }
        }
        
        Application.getInstance().setCurrentDates(first, last);
    }
}
