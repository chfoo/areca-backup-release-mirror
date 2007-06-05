package com.application.areca.launcher.gui;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.application.areca.RecoveryEntry;
import com.application.areca.ResourceManager;
import com.application.areca.Utils;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.launcher.gui.common.Colors;
import com.myJava.file.FileNameUtil;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -6307890396762748969
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
public class ArchiveExplorer 
extends Composite 
implements MouseListener, Listener
{
    private static int ITEM_STYLE = SWT.NONE;
    private final ResourceManager RM = ResourceManager.instance();
    
    private Tree tree;
    private RecoveryEntry[] entries;
    private boolean displayNonStoredItemsSize = false;
    private boolean logicalView = false;
    
    public ArchiveExplorer(Composite parent) {
        super(parent, SWT.NONE);
        
        setLayout(new FillLayout());
        TreeViewer viewer = new TreeViewer(this, SWT.BORDER | SWT.MULTI); 
        tree = viewer.getTree();
        tree.setLinesVisible(AbstractWindow.getTableLinesVisible());
        tree.setHeaderVisible(true);
        tree.addMouseListener(this);
        tree.addListener(SWT.Selection, this);
        
        TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
        column1.setText(RM.getLabel("mainpanel.name.label"));
        column1.setWidth(400);
        TreeColumn column2 = new TreeColumn(tree, SWT.LEFT);
        column2.setText(RM.getLabel("mainpanel.size.label"));
        column2.setWidth(200);
        
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                TreeItem item = tree.getSelection()[0];
                item.setExpanded(! item.getExpanded());
            }
        });
    }

    public void setDisplayNonStoredItemsSize(boolean displayNonStoredItemsSize) {
        this.displayNonStoredItemsSize = displayNonStoredItemsSize;
    }
    
    public void setLogicalView(boolean logicalView) {
        this.logicalView = logicalView;
    }

    private void initContent() {
        tree.removeAll();
        TreeItem root = new TreeItem(tree, ITEM_STYLE);
        root.setText("/");
        root.setData(new NodeData("/", RecoveryEntry.STATUS_STORED, 0, null));
        root.setImage(ArecaImages.ICO_FS_FOLDER);

        Hashtable directoryMap = new Hashtable();
        directoryMap.put("/", root);
        
        for (int i=0; i<entries.length; i++) {
            addNode("/" + entries[i].getName(), entries[i].getStatus(), entries[i].getSize(), entries[i], directoryMap);
        }
        
        Iterator iter = directoryMap.values().iterator();
        while (iter.hasNext()) {
            TreeItem item = (TreeItem)iter.next();
            configure(item);
        }
        
        root.setExpanded(true);
    }
    
    private void configure(TreeItem item) {
        NodeData data = (NodeData)item.getData();
        
        if (data.status == RecoveryEntry.STATUS_STORED) {
            item.setForeground(Colors.C_BLACK);
        } else {
            item.setForeground(Colors.C_LIGHT_GRAY);
        }
        
        String str = data.name;
        if (isDirectory(str)) {
            str = data.name.substring(0, data.name.length() - 1);
            if (str.length() == 0) {
                str = "/";
            }
            item.setImage(ArecaImages.ICO_FS_FOLDER);
        } else {
            item.setImage(ArecaImages.ICO_FS_FILE);           
        }

        item.setText(0, str);
        if (data.status == RecoveryEntry.STATUS_NOT_STORED && (! displayNonStoredItemsSize)) {
            item.setText(1, " ");
        } else {
            item.setText(1, Utils.formatFileSize(data.size));
        }
    }
    
    private TreeItem addNode(String fullPath, short status, long size, RecoveryEntry entry, Hashtable table) {
        int i= resolveParentIndex(fullPath);
        String parentKey = fullPath.substring(0, i + 1);
        String name = fullPath.substring(i + 1);
        
        // Parent node lookup
        TreeItem parent = (TreeItem)table.get(parentKey);
        if (parent == null) {
            parent = addNode(parentKey, RecoveryEntry.STATUS_NOT_STORED, 0, null, table);
        }
        
        if (status == RecoveryEntry.STATUS_STORED || displayNonStoredItemsSize){
            // Update the node's parents' status
            TreeItem tmpParent = parent;
            TreeItem tmpParentBck = null;
            while (tmpParent != null && ! tmpParent.equals(tmpParentBck)) {
                NodeData parentData = (NodeData)tmpParent.getData();
                parentData.status = (parentData.status == RecoveryEntry.STATUS_STORED || status == RecoveryEntry.STATUS_STORED) ? RecoveryEntry.STATUS_STORED : RecoveryEntry.STATUS_NOT_STORED;
                parentData.size += size;
                tmpParentBck = tmpParent;
                tmpParent = (TreeItem)tmpParent.getParentItem();
            }
        }
        
        TreeItem child = new TreeItem(parent, ITEM_STYLE);
        child.setData(new NodeData(name, status, size, entry));
        
        if (! isDirectory(name)) {
            configure(child);
        }
        
        if (FileNameUtil.endsWithSeparator(fullPath)) {
            // If it's a directory, add it to the map of parents
            table.put(fullPath, child);
        }
        
        return child;
    }
    
    private boolean isDirectory(String userObject) {
        return (userObject != null && FileNameUtil.endsWithSeparator(userObject));
    }
    
    private int resolveParentIndex(String fullPath) {
        if (FileNameUtil.endsWithSeparator(fullPath)) {
            return fullPath.lastIndexOf("/", fullPath.length() - 2);
        } else {
            return fullPath.lastIndexOf("/");
        }
    }

    private static class RecoveryEntryComparator implements Comparator {
        public int compare(Object arg0, Object arg1) {
            RecoveryEntry en0 = (RecoveryEntry)arg0;
            RecoveryEntry en1 = (RecoveryEntry)arg1;
            
            return en0.getName().toLowerCase().compareTo(en1.getName().toLowerCase());
        }
    }
    
    public static class NodeData {
        public String name;
        public short status;
        public long size;
        public RecoveryEntry entry;

        public NodeData(String name, short status, long size, RecoveryEntry entry) {
            super();
            this.name = name;
            this.status = status;
            this.size = size;
            this.entry = entry;
        }
    }

    public void setEntries(Set entries) {
        this.entries = (RecoveryEntry[])entries.toArray(new RecoveryEntry[0]);
        Arrays.sort(this.entries, new RecoveryEntryComparator());
        initContent();
    }
    
    public void reset() {
        this.tree.removeAll();
    }

    public void widgetDefaultSelected(SelectionEvent e) {
    }

    public void setSelectedEntry(RecoveryEntry entry) {
        if (entry != null) {
            StringTokenizer stt = new StringTokenizer(entry.getName(), "/");
            TreeItem parent = tree.getItem(0);
            while (stt.hasMoreTokens()) {
                String element = stt.nextToken();
                if (stt.hasMoreTokens()) {
                    element += "/";
                }
                parent = getElement(parent, element);
            }
            this.tree.setSelection(parent);
        }
    }
    
    private TreeItem getElement(TreeItem parent, String name) {
        if (parent == null) {
            return null;
        }
        
        TreeItem[] items = parent.getItems();
        for (int i=0; i<items.length; i++) {
            TreeItem child = items[i];
            NodeData data = (NodeData)child.getData();
            if (name.equals(data.name)) {
                return child;
            }
        }
        
        return null;
    }

    private String[] buildFilter(TreeItem[] nodes) {
        String[] filter = new String[nodes.length];
        for (int i=0; i<nodes.length; i++) {
            TreeItem current = nodes[i];
            while (current != null) {
                NodeData data = (NodeData)current.getData();
                filter[i] = data.name + (filter[i] == null ? "" : filter[i]); 
                current = (TreeItem)current.getParentItem();
            }
        }
        return filter;
    }
    

    public void mouseDoubleClick(MouseEvent e) {}
    public void mouseUp(MouseEvent e) {}
    
    public void mouseDown(MouseEvent e) {
        showMenu(e, logicalView ? Application.getInstance().getArchiveContextMenuLogical() : Application.getInstance().getArchiveContextMenu());
    }
    
    private void showMenu(MouseEvent e, Menu m) {
        if (e.button == 3) {
            m.setVisible(true);
        }
    }

    public void handleEvent(Event event) {
        TreeItem[] selection = tree.getSelection();
        
        if (selection.length == 1) {
            ArchiveExplorer.NodeData data = (ArchiveExplorer.NodeData)(selection[0].getData());
            Application.getInstance().setCurrentEntry(data.entry);
        } else {
            Application.getInstance().setCurrentEntry(null);
        }
        
        Application.getInstance().setCurrentFilter(buildFilter(selection));
    }

    public Tree getTree() {
        return tree;
    }
}
