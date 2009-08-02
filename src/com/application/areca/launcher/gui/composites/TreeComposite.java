package com.application.areca.launcher.gui.composites;

import java.util.Iterator;

import javax.swing.event.TreeSelectionEvent;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.application.areca.AbstractTarget;
import com.application.areca.Identifiable;
import com.application.areca.TargetGroup;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.common.ArecaImages;

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
public class TreeComposite 
extends Composite 
implements MouseListener, Listener {

    protected Tree tree;
    protected Application application = Application.getInstance();

    public TreeComposite(Composite parent) {
        super(parent, SWT.NONE);
        setLayout(new FillLayout());

        
        TreeViewer viewer = new TreeViewer(this, SWT.BORDER);
        tree = viewer.getTree();
        tree.addMouseListener(this);
        tree.addListener(SWT.Selection, this);

        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                if (application.isCurrentObjectTarget()) {
                    application.showEditTarget(application.getCurrentTarget());
                } else if (application.isCurrentObjectProcess()) {
                    application.showEditGroup(application.getCurrentTargetGroup());
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
                    if (selection.length > 0 && selection[0].getData() instanceof AbstractTarget) {
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
                if (event.item != null && isValidTreeItem((TreeItem)event.item)) {
                    event.feedback |= DND.FEEDBACK_SELECT;
                    event.detail = operation;
                } else {
                    event.feedback |= DND.FEEDBACK_NONE;
                    event.detail = DND.DROP_NONE;
                }
            }

            public void drop(DropTargetEvent event) {
                if (event.item != null) {
                    TreeItem item = (TreeItem) event.item;
                    AbstractTarget draggedTarget = Application.getInstance().getCurrentTarget();
                    TargetGroup sourceProcess = draggedTarget.getGroup();
                    TargetGroup destinationProcess = extractRecoveryProcess(item);

                    if ( ! destinationProcess.equals(sourceProcess)) {
                        sourceProcess.removeTarget(draggedTarget);

                        if (destinationProcess.getTargetById(draggedTarget.getId()) != null) {
                            // A target with same ID already exists
                            draggedTarget.setId(destinationProcess.getNextFreeTargetId());
                        }

                        destinationProcess.addTarget(draggedTarget);
                        draggedTarget.setGroup(destinationProcess);
                    }

                    application.saveProcess(sourceProcess);
                    application.saveProcess(destinationProcess);
                    refresh();
                }
            }
        });

        refresh();
    }

    private TargetGroup extractRecoveryProcess(TreeItem item) {
        if (item == null) {
            return null;
        } else {
            Object data = item.getData();
            if (data instanceof TargetGroup) {
                return (TargetGroup)data;
            } else {
                AbstractTarget tg = (AbstractTarget)data;
                return tg.getGroup();
            }
        }
    }

    private boolean isValidTreeItem(TreeItem item) {
        TargetGroup process = extractRecoveryProcess(item);
        return
        process != null
        && ! (process.getUid().equals(Application.getInstance().getCurrentTargetGroup().getUid()));
    }

    public void refresh() {
        tree.removeAll();
        String currentObjectId = Application.getInstance().getCurrentObject() != null ? Application.getInstance().getCurrentObject().getUid() : null;

        if (Application.getInstance().getWorkspace() != null) {
        	Iterator iter = Application.getInstance().getWorkspace().getSortedGroupIterator();
        	while (iter.hasNext()) {
        		TreeItem processNode = new TreeItem(tree, SWT.NONE);
        		TargetGroup process = (TargetGroup)iter.next();
        		fillProcessData(processNode, process, currentObjectId);
        		processNode.setExpanded(true);
        	}
        }
    }

    private void fillProcessData(TreeItem processNode, TargetGroup process, String currentObjectId) {
        processNode.setText(" " + process.getName());
        processNode.setImage(ArecaImages.ICO_REF_PROCESS);
        processNode.setData(process);

        Iterator iter = process.getSortedTargetIterator();
        while (iter.hasNext()) {
            TreeItem targetNode = new TreeItem(processNode, SWT.NONE);
            AbstractTarget target = (AbstractTarget)iter.next();

            targetNode.setText(" " + target.getTargetName());
            targetNode.setImage(ArecaImages.ICO_REF_TARGET);
            targetNode.setData(target);

            if (target.getUid().equals(currentObjectId)) {
                tree.setSelection(targetNode);
            }
        }

        if (process.getUid().equals(currentObjectId)) {
            tree.setSelection(processNode);
        }
    }

    public void setSelectedTarget(AbstractTarget target) {
        if (target != null) {
            TreeItem processNode = null;
            TargetGroup process = target.getGroup();
            TreeItem[] processes = tree.getItems();
            for (int i=0; i<processes.length; i++) {
                TreeItem child = processes[i];
                TargetGroup cProcess = (TargetGroup)child.getData();
                if (cProcess.getSource().equals(process.getSource())) {
                    processNode = child;
                    break;
                }
            }

            TreeItem[] targets = processNode.getItems();
            for (int i=0; i<targets.length; i++) {
                TreeItem child = targets[i];
                AbstractTarget cTarget = (AbstractTarget)child.getData();
                if (cTarget.equals(target)) {
                    tree.setSelection(child);
                    break;
                }
            }
        }
    }

    public void mouseDoubleClick(MouseEvent e) {
    }

    public void mouseDown(MouseEvent e) {
        TreeItem item = tree.getItem(new Point(e.x, e.y));

        if (item != null) {
            if (item.getData() instanceof AbstractTarget) {
                showMenu(e, Application.getInstance().getTargetContextMenu());
            } else if (item.getData() instanceof TargetGroup) {
                showMenu(e, Application.getInstance().getProcessContextMenu());            
            } else {
                showMenu(e, Application.getInstance().getWorkspaceContextMenu());
            }
        }
    }

    private void showMenu(MouseEvent e, Menu m) {
        if (e.button == 3) {
            m.setVisible(true);
        }
    }

    public void mouseUp(MouseEvent e) {
    }

    public void valueChanged(TreeSelectionEvent e) {
        Application.getInstance().setCurrentObject((Identifiable)e.getSource(), false);
    }

    public void handleEvent(Event event) {
        Application.getInstance().setCurrentObject((Identifiable)event.item.getData(), false);
    }
}
