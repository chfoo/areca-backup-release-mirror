package com.application.areca.launcher.gui.composites;

import java.net.URL;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;

import com.application.areca.AbstractTarget;
import com.application.areca.ArecaURLs;
import com.application.areca.TargetGroup;
import com.application.areca.Workspace;
import com.application.areca.WorkspaceItem;
import com.application.areca.adapters.ConfigurationListener;
import com.application.areca.launcher.ArecaUserPreferences;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.common.Colors;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.myJava.system.viewer.ViewerHandlerHelper;
import com.myJava.util.log.Logger;

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
public class TargetTreeComposite 
extends AbstractTargetTreeComposite {
	protected Application application = Application.getInstance();
	protected Label warning;
	protected Link more;

    public TargetTreeComposite(Composite parent) {
        super(parent, false, ArecaUserPreferences.isDisplayWSAddress(), false);
        
        
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                if (
                		application.isCurrentObjectTarget() 
                		&& (
                				application.getWorkspace() == null
                				|| (! application.getWorkspace().isBackupWorkspace())
                    	)
                ) {
                    application.showEditTarget(application.getCurrentTarget());
                }
            }
        });
        
        tree.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                TreeItem ti = tree.getItem(new Point(e.x, e.y));
                if (ti == null) {
                    tree.deselectAll();
                    Application.getInstance().setCurrentObject(null, false);
                }
            }
        });

        Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
        final int operation = DND.DROP_MOVE;

        final DragSource source = new DragSource(tree, operation);
        source.setTransfer(types);
        source.addDragListener(
            new DragSourceAdapter() {
                public void dragStart(DragSourceEvent event) {   
                    TreeItem[] selection = tree.getSelection();
                    if (selection.length > 0) {
                    	event.doit = true;
                    } else {
                        event.doit = false;
                    }
                };
                public void dragSetData(DragSourceEvent event) {
                    event.data = "dummy data";
                }
            }
        );

        DropTarget target = new DropTarget(tree, operation);
        target.setTransfer(types);
        target.addDropListener(new DropTargetAdapter() {
            public void dragEnter(DropTargetEvent event) {
                event.detail = DND.DROP_NONE;
                event.feedback = DND.FEEDBACK_NONE;
            }

            public void dragOver(DropTargetEvent event) {
                event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
                if (isValidTreeItem((TreeItem)event.item)) {
                    event.feedback |= DND.FEEDBACK_SELECT;
                    event.detail = operation;
                } else {
                    event.feedback |= DND.FEEDBACK_NONE;
                    event.detail = DND.DROP_NONE;
                }
            }

            public void drop(DropTargetEvent event) {
                TreeItem item = (TreeItem)event.item;
                WorkspaceItem draggedItem = Application.getInstance().getCurrentObject();
                TargetGroup sourceGroup = draggedItem.getParent();
                TargetGroup destinationGroup = extractRecoveryGroup(item);

                if (! destinationGroup.equals(sourceGroup)) {
                    
                    sourceGroup.remove(draggedItem.getUid());
                    destinationGroup.linkChild(draggedItem);
                    
                    // Update files
                	try {
                		ConfigurationListener.getInstance().itemMoved(
                				draggedItem, 
                				sourceGroup, 
                				Application.getInstance().getWorkspace().getPathFile());
					} catch (Exception e) {
						Logger.defaultLogger().error(e);
						Application.getInstance().refreshWorkspace();
					}
                }
                refresh();
                Application.getInstance().getMainWindow().refreshProperties();
            }
        });
    }
    
    private void setBackupWorkspace() {
    	if (warning == null) {
    		warning = new Label(this, SWT.WRAP);
    		warning.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false, 2, 1));
    		warning.setText(ResourceManager.instance().getLabel("targettree.isbackup.warning"));
    		warning.setForeground(Colors.C_RED);
    		
    		
    		more = new Link(this, SWT.NONE);
            more.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 2, 1));
    		more.setText("<A HREF=\"" + ArecaURLs.BACKUP_COPY_URL + "\">" + ResourceManager.instance().getLabel("targettree.isbackup.more") + "</A>");
    		more.addListener (SWT.Selection, new Listener () {
    			public void handleEvent(Event event) {
                    try {
                        ViewerHandlerHelper.getViewerHandler().browse(new URL(event.text));
                    } catch (Exception e) {
                        Logger.defaultLogger().error(e);
                    }
    			}
    		});
    		
    		this.layout(true);
    	}
    }
    
    private void unsetBackupWorkspace() {
    	if (warning != null) {
    		warning.dispose();
    		more.dispose();
    		more = null;
    		warning = null;
    		this.layout(true);
    	}
    }
    
	public void refresh() {
		super.refresh();
		if (Application.getInstance().getWorkspace() != null && txtPath != null) {
			txtPath.setText(Application.getInstance().getWorkspace().getPath());
		}
		if (Application.getInstance().getWorkspace() != null 
				&& Application.getInstance().getWorkspace().isBackupWorkspace()) {
			setBackupWorkspace();
		} else {
			unsetBackupWorkspace();	
		}
	}

	protected Workspace getWorkspace() {
    	return Application.getInstance().getWorkspace();
    }

    private boolean isValidTreeItem(TreeItem item) {
        TargetGroup targetGroup = extractRecoveryGroup(item);
    	WorkspaceItem currentGroup = Application.getInstance().getCurrentTargetGroup();
    	WorkspaceItem currentItem = Application.getInstance().getCurrentWorkspaceItem();

    	if (targetGroup == null) {
    		return false;
    	} else {
    		return 
	    		(! targetGroup.isChildOf(currentItem)) 
	    		&& (! targetGroup.getUid().equals(currentGroup.getUid()))
	    		&& (! targetGroup.getUid().equals(Application.getInstance().getCurrentWorkspaceItem().getParent().getUid())
	    		);
    	}
    }
    
    public void mouseDown(MouseEvent e) {
        TreeItem item = tree.getItem(new Point(e.x, e.y));

        if (item != null) {
            if (item.getData() instanceof AbstractTarget) {
                showMenu(e, Application.getInstance().getTargetContextMenu());
            } else if (item.getData() instanceof TargetGroup) {
                showMenu(e, Application.getInstance().getGroupContextMenu());            
            } else {
                showMenu(e, Application.getInstance().getWorkspaceContextMenu());
            }
        } else {
            showMenu(e, Application.getInstance().getGroupContextMenu()); 
        }
    }
    
    protected String getCurrentId() {
        return Application.getInstance().getCurrentObject() != null ? Application.getInstance().getCurrentObject().getUid() : null;
    }
    
    public void handleEvent(Event event) {
        Application.getInstance().setCurrentObject((WorkspaceItem)event.item.getData(), false);
    }
    
    protected TargetGroup extractRecoveryGroup(TreeItem item) {
        if (item == null) {
            return Application.getInstance().getWorkspace().getContent();
        } else {
            Object data = item.getData();
            if (data instanceof TargetGroup) {
                return (TargetGroup)data;
            } else {
                AbstractTarget tg = (AbstractTarget)data;
                return tg.getParent();
            }
        }
    }
}
