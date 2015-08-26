package com.application.areca.launcher.gui.composites;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.application.areca.AbstractTarget;
import com.application.areca.Workspace;
import com.application.areca.impl.FileSystemTarget;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.confimport.ImportConfigurationWindow;
import com.application.areca.launcher.gui.resources.ResourceManager;

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
public class ImportTargetTreeComposite 
extends AbstractTargetTreeComposite {
	private final ResourceManager RM = ResourceManager.instance();
	private Workspace workspace;

    public ImportTargetTreeComposite(Composite parent, final ImportConfigurationWindow window) {
        super(parent, true, false, true);
        
		TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
		column1.setText(RM.getLabel("property.element.label"));
		column1.setWidth(AbstractWindow.computeWidth(260));
		
		TreeColumn column2 = new TreeColumn(tree, SWT.LEFT);
		column2.setText(RM.getLabel("property.conf.label"));
		column2.setWidth(AbstractWindow.computeWidth(160));
		
		TreeColumn column3 = new TreeColumn(tree, SWT.LEFT);
		column3.setText(RM.getLabel("property.source.label"));
		column3.setWidth(AbstractWindow.computeWidth(200));
		
		TreeColumn column4 = new TreeColumn(tree, SWT.LEFT);
		column4.setText(RM.getLabel("targetedition.storagedirfield.label"));
		column4.setWidth(AbstractWindow.computeWidth(360));
		
		tree.setLinesVisible(AbstractWindow.getTableLinesVisible());
		tree.setHeaderVisible(true);
        
        tree.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				window.treeItemSelected();
			}
        });
        
        tree.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                TreeItem ti = tree.getItem(new Point(e.x, e.y));
                if (ti == null) {
                    tree.deselectAll();
    				window.treeItemSelected();
                }
            }
        });
    }
    
    protected String getCurrentId() {
    	return null;
    }

	protected Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
		this.refresh();
	}
	
    protected void fillTargetData(TreeItem targetNode, AbstractTarget target, String currentObjectId) {
    	super.fillTargetData(targetNode, target, currentObjectId);
        
    	FileSystemTarget fTarget = (FileSystemTarget)target;
    	
    	targetNode.setText(1, fTarget.computeConfigurationFileName());
        targetNode.setText(2, fTarget.getSourcesRoot());
        targetNode.setText(3, fTarget.getMedium().getDescription());       
    }
}
