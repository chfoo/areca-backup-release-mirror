package com.application.areca.launcher.gui.composites;

import java.util.GregorianCalendar;
import java.util.HashMap;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.HistoryEntryTypes;
import com.application.areca.ResourceManager;
import com.application.areca.Utils;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.launcher.gui.common.Refreshable;
import com.myJava.util.history.History;
import com.myJava.util.history.HistoryEntry;

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
public class HistoryComposite 
extends Composite 
implements Listener, Refreshable, HistoryEntryTypes { 
    protected final ResourceManager RM = ResourceManager.instance();
    
    private Table table;
    private TableViewer viewer;
    private Application application = Application.getInstance();
    
    protected Button btnClear;
    
    public HistoryComposite(Composite parent) {
        super(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.numColumns = 1;
        setLayout(layout);

        viewer = new TableViewer(this, SWT.BORDER| SWT.SINGLE);
        
        table = viewer.getTable();
        table.setLinesVisible(AbstractWindow.getTableLinesVisible());
        table.setHeaderVisible(true);
        
        TableColumn col1 = new org.eclipse.swt.widgets.TableColumn(table, SWT.NONE);
        col1.setMoveable(true);
        TableColumn col2 = new org.eclipse.swt.widgets.TableColumn(table, SWT.NONE);
        col2.setMoveable(true);
        
        table.getColumn(1).setText(RM.getLabel("history.datecolumn.label"));
        table.getColumn(0).setText(RM.getLabel("history.actioncolumn.label"));
        table.getColumn(1).setWidth(AbstractWindow.computeWidth(300));
        table.getColumn(0).setWidth(AbstractWindow.computeWidth(400));

        GridData dt1 = new GridData();
        dt1.grabExcessHorizontalSpace = true;
        dt1.grabExcessVerticalSpace = true;
        dt1.horizontalAlignment = SWT.FILL;
        dt1.verticalAlignment = SWT.FILL;
        table.setLayoutData(dt1);
        
        Composite bottom = buildBottomComposite(this);
        GridData dt2 = new GridData();
        dt2.grabExcessHorizontalSpace = true;
        dt2.horizontalAlignment = SWT.FILL;
        dt2.verticalAlignment = SWT.FILL;
        bottom.setLayoutData(dt2);
    }
    
    private Composite buildBottomComposite(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        panel.setLayout(layout);

        GridData data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = SWT.RIGHT;
        btnClear = new Button(panel, SWT.PUSH);
        btnClear.setLayoutData(data);
        btnClear.setText(RM.getLabel("history.clearbutton.label"));
        btnClear.addListener(SWT.Selection, this);
        
        return panel;
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
        History h = target.getHistory();
        if (h != null) {
            HashMap content = h.getContent();
            GregorianCalendar[] keys = h.getOrderedKeys();

            for (int i=keys.length - 1; i>=0; i--) {
                HistoryEntry entry = (HistoryEntry)content.get(keys[i]);
                int type = entry.getType();

                TableItem item = new TableItem(table, SWT.NONE);

                if (type == HISTO_BACKUP_CANCEL) {
                    item.setImage(0, ArecaImages.ICO_ACT_CANCEL);
                } else if (type == HISTO_BACKUP) {
                    item.setImage(0, ArecaImages.ICO_ACT_ARCHIVE);
                } else if (type == HISTO_DELETE) {
                    item.setImage(0, ArecaImages.ICO_ACT_DELETE);
                } else if (type == HISTO_RECOVER) {
                    item.setImage(0, ArecaImages.ICO_ACT_RESTAURE);
                } else {
                    item.setImage(0, ArecaImages.ICO_ACT_MERGE);
                }

                item.setText(1, Utils.formatDisplayDate(keys[i]));
                item.setText(0, entry.getDescription());
            }
        }
    }

    public void handleEvent(Event event) {
        if (application.isCurrentObjectTarget()) {
            int result = application.showConfirmDialog(RM.getLabel("history.clear.confirm.question"), RM.getLabel("history.clear.confirm.title"));
            if (result == SWT.YES) {
                this.application.getCurrentTarget().clearHistory();   
                this.refresh();
            }
        }
    }
}
