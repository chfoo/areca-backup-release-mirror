package com.application.areca.launcher.gui.composites;

import java.lang.reflect.Field;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.application.areca.AbstractTarget;
import com.application.areca.ApplicationException;
import com.application.areca.indicator.Indicator;
import com.application.areca.indicator.IndicatorMap;
import com.application.areca.indicator.IndicatorTypes;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.Refreshable;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.myJava.util.taskmonitor.TaskCancelledException;

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
public class IndicatorsComposite 
extends AbstractTabComposite 
implements Refreshable { 
    protected final ResourceManager RM = ResourceManager.instance();
    
    private Table table;
    private TableViewer viewer;
    private Application application = Application.getInstance();
    
    public IndicatorsComposite(Composite parent) {
        super(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.numColumns = 1;
        setLayout(layout);

        viewer = new TableViewer(this, 
                SWT.BORDER 
                | SWT.SINGLE 
                | SWT.FULL_SELECTION 
                | SWT.WRAP 
                | SWT.H_SCROLL 
                | SWT.V_SCROLL);
        
        table = viewer.getTable();
        table.setLinesVisible(AbstractWindow.getTableLinesVisible());
        table.setHeaderVisible(true);
        
        new org.eclipse.swt.widgets.TableColumn(table, SWT.NONE);
        new org.eclipse.swt.widgets.TableColumn(table, SWT.NONE);
        new org.eclipse.swt.widgets.TableColumn(table, SWT.NONE | SWT.WRAP);

        table.getColumn(0).setWidth(AbstractWindow.computeWidth(300));
        table.getColumn(0).setMoveable(true);
        table.getColumn(1).setWidth(AbstractWindow.computeWidth(400));
        table.getColumn(1).setMoveable(true);
        table.getColumn(2).setWidth(AbstractWindow.computeWidth(100));
        table.getColumn(2).setAlignment(SWT.RIGHT);
        table.getColumn(2).setMoveable(true);

        GridData dt1 = new GridData();
        dt1.grabExcessHorizontalSpace = true;
        dt1.grabExcessVerticalSpace = true;
        dt1.horizontalAlignment = SWT.FILL;
        dt1.verticalAlignment = SWT.FILL;
        table.setLayoutData(dt1);
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

    private void fillTargetData(AbstractTarget target) {
        try {
            IndicatorMap indicators = target.getMedium().computeIndicators();
            Integer[] keys = indicators.getSortedIndicatorKeys();

            for (int i=0; i<keys.length; i++) {
                Integer key = keys[i];
                Indicator indicator = indicators.getIndicator(key);
                String resourceBaseKey = getResourceKey(indicator);
                
                TableItem item = new TableItem(table, SWT.NONE);
                item.setText(0, RM.getLabel("indicators." + resourceBaseKey + ".name") + " : ");
                item.setText(2, indicator.getStringValue());
                item.setText(1, AbstractWindow.configureForTable(RM.getLabel("indicators." + resourceBaseKey + ".description")));
            }
        } catch (ApplicationException e) {
            application.handleException(e);
        } catch (TaskCancelledException e) {
            application.handleException(e);
        }
    }
    
    private String getResourceKey(Indicator indicator) {
        try {
            Field[] fields = IndicatorTypes.class.getDeclaredFields();
            for (int i=0; i<fields.length; i++) {
                if (fields[i].getType().equals(Integer.class)) {
                    if (indicator.getId().intValue() == ((Integer)fields[i].get(null)).intValue()) {
                        return fields[i].getName().substring(2);
                    }
                }
            }
        } catch (Exception ex) {
        }
        return null;
    }
}
