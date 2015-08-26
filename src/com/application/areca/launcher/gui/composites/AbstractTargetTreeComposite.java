package com.application.areca.launcher.gui.composites;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.application.areca.AbstractTarget;
import com.application.areca.AbstractWorkspaceItem;
import com.application.areca.TargetGroup;
import com.application.areca.Workspace;
import com.application.areca.WorkspaceItem;
import com.application.areca.launcher.ArecaUserPreferences;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.launcher.gui.common.SecuredRunner;
import com.application.areca.launcher.gui.menus.AppActionReferenceHolder;
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
public abstract class AbstractTargetTreeComposite 
extends Composite 
implements MouseListener, Listener {
    protected Tree tree;
    protected TreeViewer viewer;
	protected Combo txtPath;
	protected Button btnWsp;
	protected int width;
	protected boolean filterEmptyGroups = false;

    public AbstractTargetTreeComposite(Composite parent, boolean multi, boolean addPath, boolean filterEmptyGroups) {
        super(parent, SWT.NONE);
        this.filterEmptyGroups = filterEmptyGroups;
        
        width = addPath ? 2:1;
        	
        GridLayout layout = new GridLayout(width, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        setLayout(layout);
        
        int style = SWT.BORDER;
        if (multi) {
        	style |= SWT.MULTI;
        }
        
        if (addPath) {
            txtPath = new Combo(this, SWT.DROP_DOWN);
            txtPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            txtPath.setToolTipText(ResourceManager.instance().getLabel("mainpanel.wspath.tt"));
            txtPath.addKeyListener(new KeyListener() {
				public void keyReleased(KeyEvent arg0) {
					if (arg0.character == '\r') {
						Application.getInstance().openWorkspace(txtPath.getText());
					}
				}
				
				public void keyPressed(KeyEvent arg0) {
				}
			});
            txtPath.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					Application.getInstance().openWorkspace(txtPath.getText());
				}
			});
            
            btnWsp = new Button(this, SWT.PUSH);
            btnWsp.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
            btnWsp.setText(ResourceManager.instance().getLabel("common.browseaction.label"));
            btnWsp.setToolTipText(AppActionReferenceHolder.AC_OPEN.getToolTip());
            btnWsp.addSelectionListener(AppActionReferenceHolder.AC_OPEN);
        }
        
        viewer = new TreeViewer(this, style);
        tree = viewer.getTree();
        tree.addMouseListener(this);
        tree.addListener(SWT.Selection, this);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, width, 1));
        
        refresh();
    }
    
    public void synchronizeHistory() {
    	if (txtPath != null){
	    	txtPath.removeAll();
	        Stack h = ArecaUserPreferences.getWorkspaceHistory();
	        for (int i=0; i<h.size(); i++) {
	        	txtPath.add((String)h.get(i));
	        }
	        txtPath.setText(Application.getInstance().getWorkspace().getPath());
    	}
    }
    
    protected abstract Workspace getWorkspace();
    protected abstract String getCurrentId();

    public void refresh() {
    	SecuredRunner.execute(new Runnable() {
			public void run() {
		        tree.removeAll();
		        
		        String currentObjectId = getCurrentId();

		        if (getWorkspace() != null) {
		        	Iterator iter = getWorkspace().getContent().getSortedIterator(filterEmptyGroups);
		        	while (iter.hasNext()) {
		        		TreeItem node = new TreeItem(tree, SWT.NONE);
		        		Object o = iter.next();
		        		
		        		if (o instanceof TargetGroup) {
		            		fillGroupData(node, (TargetGroup)o, currentObjectId);
		        		} else {
		        			fillTargetData(node, (AbstractTarget)o, currentObjectId);
		        		}
		        	}
		        }
			}
		});
    }

    private void fillGroupData(TreeItem groupNode, TargetGroup group, String currentObjectId) {
    	groupNode.setText(" " + group.getName());
    	groupNode.setImage(ArecaImages.ICO_REF_PROCESS);
    	groupNode.setData(group);

        Iterator iter = group.getSortedIterator(filterEmptyGroups);
        while (iter.hasNext()) {
            TreeItem targetNode = new TreeItem(groupNode, SWT.NONE);
            groupNode.setExpanded(true);
            Object o = iter.next();
            if (o instanceof AbstractTarget) {
	            fillTargetData(targetNode, (AbstractTarget)o, currentObjectId);
            } else {
	            fillGroupData(targetNode, (TargetGroup)o, currentObjectId);
            }
        }

        if (group.getUid().equals(currentObjectId)) {
            tree.setSelection(groupNode);
        }
    }
    
    protected void fillTargetData(TreeItem targetNode, AbstractTarget target, String currentObjectId) {
        targetNode.setText(" " + target.getName());
        targetNode.setImage(ArecaImages.ICO_REF_TARGET);
        targetNode.setData(target);

        if (target.getUid().equals(currentObjectId)) {
            tree.setSelection(targetNode);
        }
    }
    

    public void setSelectedTarget(AbstractTarget target) {
    	List path = new ArrayList();
    	AbstractWorkspaceItem itm = target;
    	while (itm != null) {
    		path.add(itm);
    		itm = itm.getParent();
    	}
    	
    	if (path.size() != 0) {
        	path.remove(path.size() - 1);
    		TreeItem toSelect = null;
    		TreeItem[] searchRange = tree.getItems();
    		for (int i=path.size() - 1; i >=0; i--) {
    			itm = (AbstractWorkspaceItem)path.get(i);
    			for (int j=0; j<searchRange.length; j++) {
    				AbstractWorkspaceItem candidate = (AbstractWorkspaceItem)searchRange[j].getData();
                    if (candidate.getUid().equals(itm.getUid())) {
                    	toSelect = searchRange[j];
                    	searchRange = toSelect.getItems();
                        break;
                    }
    			}
    		}
    		
    		tree.setSelection(toSelect);
    		tree.showSelection();
    	}
    }

    public void mouseDoubleClick(MouseEvent e) {
    }

    public void mouseDown(MouseEvent e) {
    }
    
    public void mouseUp(MouseEvent e) {
    }

    public void handleEvent(Event event) {
    }

    protected void showMenu(MouseEvent e, Menu m) {
        if (e.button == 3) {
            m.setVisible(true);
        }
    }

    public WorkspaceItem[] getSelectedItems() {
    	TreeItem[] items = tree.getSelection();
    	WorkspaceItem[] ret = new WorkspaceItem[items.length];
    	for (int i=0; i<items.length; i++) {
    		ret[i] = (WorkspaceItem)items[i].getData();
    	}
    	return ret;
    }
}
