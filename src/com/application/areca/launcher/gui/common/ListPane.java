package com.application.areca.launcher.gui.common;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

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
public class ListPane {

    private List elements = new ArrayList();
    private Composite parent; 
    private Composite panes;
    private int style;
    private boolean showTitles;
    private org.eclipse.swt.widgets.List menu;
    
    public ListPane(Composite p, int style, boolean showTitles) {
        parent = new Composite(p, SWT.NONE);
        this.style = style;
        this.showTitles = showTitles;
        
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        parent.setLayout(layout);
        
        menu = new org.eclipse.swt.widgets.List(parent, SWT.BORDER);
        menu.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
        
        panes = new Composite(parent, SWT.NONE);
        panes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        RowLayout lyt = new RowLayout();
        lyt.marginWidth = 0;
        lyt.marginHeight = 0;
        lyt.fill = true;
        panes.setLayout(lyt);
        
        menu.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                refreshSelected();
            }

        });
        
        panes.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event event) {
                adjustSelectedElement();
            }
        });
    }
    
    private void refreshSelected() {
        int selected = menu.getSelectionIndex();
        adjustSelectedElement();
        
        for (int i=0; i<elements.size(); i++) {
            if (i !=  selected) {
                ListPaneElement element =(ListPaneElement)elements.get(i);
                element.getComposite().setVisible(false);
                RowData dt = new RowData();
                dt.exclude = true;
                element.getComposite().setLayoutData(dt);
            }
        }
        
       panes.layout(true);
    }
    
    public void setSelection(int selection) {
        menu.setSelection(selection);
        refreshSelected();
    }
    
    public void setLayoutData(Object layoutData) {
        this.parent.setLayoutData(layoutData);
    }
    
    private void adjustSelectedElement() {
        int selected = menu.getSelectionIndex();
        if (selected != -1) {
            ListPaneElement selectedElement =(ListPaneElement)elements.get(selected);
            selectedElement.getComposite().setVisible(true);
            RowData dt = (RowData)selectedElement.getComposite().getLayoutData();
            dt.exclude = false;
            if (panes.getSize().x > 3 && panes.getSize().y > 3) {
                dt.width = panes.getSize().x - 3;
                dt.height = panes.getSize().y - 3;
            }
            selectedElement.getComposite().setLayoutData(dt);
        }
    }

    public Composite addElement(String key, String label) {
        Composite composite = new Composite(panes, SWT.NONE);
        RowData dt = new RowData();
        dt.exclude = elements.size() != 0;
        composite.setLayoutData(dt);
        
        GridLayout lyt = new GridLayout(1, false);
        lyt.marginHeight = 0;
        lyt.marginWidth = 0;
        composite.setLayout(lyt);
        
        if (showTitles) {
            Label lbl = new Label(composite, SWT.NONE);
            lbl.setText(label);
            lbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        }
        
        menu.add(label + "        ");
        
        Composite content = new Composite(composite, style);
        content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        elements.add(new ListPaneElement(key, label, composite));
        
        return content;
    }
}
