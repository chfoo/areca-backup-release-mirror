package com.application.areca.launcher.gui.composites;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.application.areca.ApplicationException;
import com.application.areca.EntryArchiveData;
import com.application.areca.RecoveryEntry;
import com.application.areca.ResourceManager;
import com.application.areca.Utils;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.launcher.gui.common.Refreshable;
import com.application.areca.metadata.manifest.Manifest;
import com.myJava.util.log.Logger;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -4899974077672581254
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
public class LogicalViewComposite 
extends Composite 
implements MouseListener, Refreshable, Listener { 
   
    private ArchiveExplorer explorer;
    private Table history;
    private Text manifest;
    
    private Application application = Application.getInstance();
    private static ResourceManager RM = ResourceManager.instance();
    
    public LogicalViewComposite(Composite parent) {
        super(parent, SWT.NONE);
        this.setLayout(new FillLayout());

        SashForm sashMain = new SashForm(this, SWT.HORIZONTAL);
        sashMain.setLayout(new FillLayout());

        buildExplorerComposite(sashMain);
        
        SashForm sashHisto = new SashForm(sashMain, SWT.VERTICAL);
        sashHisto.setLayout(new FillLayout());

        buildHistoryComposite(sashHisto);
        buildManifestComposite(sashHisto);

        sashMain.setWeights(new int[] {60, 40});
        sashHisto.setWeights(new int[] {60, 40});
    }
    
    private void buildHistoryComposite(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.numColumns = 2;
        content.setLayout(layout);
    
        Label lblHistory = new Label(content, SWT.NONE);
        lblHistory.setImage(ArecaImages.ICO_HISTORY);
        GridData dt1 = new GridData();
        lblHistory.setLayoutData(dt1);
        
        Label lblHistoryText = new Label(content, SWT.NONE);
        lblHistoryText.setText(RM.getLabel("archivecontent.filehistoryfield.label"));
        GridData dt1Text = new GridData();
        lblHistoryText.setLayoutData(dt1Text);
        
        history = new Table(content, SWT.BORDER);
        TableColumn col1 = new TableColumn(history, SWT.NONE);
        col1.setMoveable(true);
        TableColumn col2 = new TableColumn(history, SWT.NONE);
        col2.setMoveable(true);
        col1.setText(RM.getLabel("archivecontent.actioncolumn.label"));
        col2.setText(RM.getLabel("archivecontent.datecolumn.label"));
        col1.setWidth(150);
        col2.setWidth(150);
        history.setHeaderVisible(true);
        history.setLinesVisible(AbstractWindow.getTableLinesVisible());
        history.addMouseListener(this);
        history.addListener(SWT.Selection, this);
        
        GridData dt2 = new GridData();
        dt2.grabExcessVerticalSpace = true;
        dt2.verticalAlignment = SWT.FILL;
        dt2.grabExcessHorizontalSpace = true;
        dt2.horizontalAlignment = SWT.FILL;
        dt2.horizontalSpan = 2;
        history.setLayoutData(dt2);
    }
    
    private void buildExplorerComposite(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        content.setLayout(layout);
        GridData dt = new GridData();
        dt.grabExcessVerticalSpace = true;
        dt.verticalAlignment = SWT.FILL;
        dt.grabExcessHorizontalSpace = true;
        dt.horizontalAlignment = SWT.FILL;    
        explorer = new ArchiveExplorer(content);
        explorer.setDisplayNonStoredItemsSize(true);
        explorer.setLogicalView(true);
        explorer.getTree().addListener(SWT.Selection, this);
        explorer.setLayoutData(dt);
    }
    
    private void buildManifestComposite(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.numColumns = 2;
        content.setLayout(layout);
    
        Label lblManifest = new Label(content, SWT.NONE);
        lblManifest.setImage(ArecaImages.ICO_FS_FILE);
        GridData dt1 = new GridData();
        lblManifest.setLayoutData(dt1);
        
        Label lblManifestText = new Label(content, SWT.NONE);
        lblManifestText.setText(RM.getLabel("archivecontent.descriptionfield.label"));
        GridData dt1Text = new GridData();
        dt1Text.grabExcessHorizontalSpace = true;
        lblManifestText.setLayoutData(dt1Text);
        
        manifest = new Text(content, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData dt2 = new GridData();
        dt2.grabExcessVerticalSpace = true;
        dt2.verticalAlignment = SWT.FILL;
        dt2.grabExcessHorizontalSpace = true;
        dt2.horizontalAlignment = SWT.FILL;
        dt2.horizontalSpan = 2;
        manifest.setLayoutData(dt2);
    }
    
    public void refresh() {
        try {
            if (explorer != null) {
                if (Application.getInstance().isCurrentObjectTarget()) {
                    explorer.setEntries(Application.getInstance().getCurrentTarget().getMedium().getLogicalView());
                } else {
                    explorer.reset();
                }
                resetHistoryContent();
            }
        } catch (ApplicationException e) {
            application.handleException(e);
        }
    }

    public void mouseDoubleClick(MouseEvent e) {}
    public void mouseUp(MouseEvent e) {}
    public void mouseDown(MouseEvent e) {
        TableItem item = history.getItem(new Point(e.x, e.y));
        if (item != null) {
            showMenu(e, Application.getInstance().getHistoryContextMenu());
        }
    }
    
    public void handleEvent(Event e) {
        if (e.widget instanceof Tree) {
            TreeItem[] selection = explorer.getTree().getSelection();
            
            if (selection.length == 1) {
                TreeItem item = selection[0];               
                ArchiveExplorer.NodeData data = (ArchiveExplorer.NodeData)(item.getData());
                RecoveryEntry currentEntry = data.entry;
                
                if (currentEntry != null) {
                    this.initHistoryContent(currentEntry);
                } else {
                    resetHistoryContent();
                }
            } else {
                resetHistoryContent();
            }
        } else {
            TableItem[] items = history.getSelection();
            if (items != null && items.length == 1) {
                EntryArchiveData data = (EntryArchiveData)items[0].getData();
                refreshManifest(data);
            }
        }
    }

    private void refreshManifest(EntryArchiveData data) {
        // Affichage manifeste et enregistrement date courante
        this.application.setCurrentHistoryDate(data.getManifest().getDate());
        
        Manifest mf = data.getManifest();
        String txt = "";
        if (mf != null) {
            txt = mf.getTitle() + "\n\n" + mf.getDescription();
        }
        this.manifest.setText(txt);
        this.manifest.setSelection(0);
    }
    
    private void showMenu(MouseEvent e, Menu m) {
        if (e.button == 3) {
            m.setVisible(true);
        }
    }
    
    private void resetHistoryContent() {
        this.history.removeAll();
        this.manifest.setText("");
    }
    
    private void initHistoryContent(RecoveryEntry entry) {
        try {
            application.enableWaitCursor();
            
            EntryArchiveData[] currentEntryData = application.getCurrentTarget().getMedium().getHistory(entry);
            resetHistoryContent();

            for (int i=currentEntryData.length - 1; i>=0; i--) {
                Manifest mf = currentEntryData[i].getManifest();                
                TableItem item = new TableItem(history, SWT.NONE);
                
                item.setText(0, Application.STATUS_LABELS[currentEntryData[i].getStatus() + 1]); 
                item.setImage(0, Application.STATUS_ICONS[currentEntryData[i].getStatus() + 1]); 
                item.setData(currentEntryData[i]);
                if (mf != null) {
                    item.setText(1, Utils.formatDisplayDate(mf.getDate()));
                } else {
                    item.setText(1, " ");
                }      
            }
            
            if (currentEntryData.length != 0) {
                history.setSelection(0);
                refreshManifest(currentEntryData[currentEntryData.length - 1]);
            }
        } catch (ApplicationException e) {
            Logger.defaultLogger().error(e);
        } finally {
            application.disableWaitCursor();
        }
    }
    
    public Object getRefreshableKey() {
        return this.getClass().getName();
    }
}
