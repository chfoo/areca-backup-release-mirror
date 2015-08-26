package com.application.areca.launcher.gui.composites;

import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.application.areca.ApplicationException;
import com.application.areca.ArchiveMedium;
import com.application.areca.Utils;
import com.application.areca.impl.AggregatedViewContext;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.UIRecoveryFilter;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.launcher.gui.common.Colors;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.application.areca.metadata.MetadataConstants;
import com.application.areca.metadata.trace.TraceEntry;
import com.myJava.util.log.Logger;

/**
 * Expansive logs have been temporarily added to this class to diagnose application crashes which have been
 * reported by some users.<BR>
 * 
 * @author Olivier PETRUCCI <BR>
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
public class ArchiveExplorer 
extends Composite 
implements MouseListener, Listener {
	
	private static int ITEM_STYLE = SWT.NONE;
	private final ResourceManager RM = ResourceManager.instance();

	private Tree tree;
	private boolean displayNonStoredItemsSize = false;
	private boolean logicalView = false;
	private Font italic;
	private ArchiveMedium medium;
	private AggregatedViewContext context = new AggregatedViewContext();
	private GregorianCalendar fromDate;
	private boolean aggregated = false;

	public ArchiveExplorer(Composite parent, boolean aggregated) {
		super(parent, SWT.NONE);
		try {
			this.aggregated = aggregated;
			
			setLayout(new FillLayout());
			TreeViewer viewer = new TreeViewer(this, SWT.BORDER | SWT.MULTI);
			tree = viewer.getTree();
			tree.setLinesVisible(AbstractWindow.getTableLinesVisible());
			tree.setHeaderVisible(true);
			tree.addMouseListener(this);
			tree.addListener(SWT.Selection, this);

			TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
			column1.setText(RM.getLabel("mainpanel.name.label"));
			column1.setWidth(AbstractWindow.computeWidth(400));
			TreeColumn column2 = new TreeColumn(tree, SWT.LEFT);
			column2.setText(RM.getLabel("mainpanel.size.label"));
			column2.setWidth(AbstractWindow.computeWidth(120));

			viewer.addDoubleClickListener(new IDoubleClickListener() {
				public void doubleClick(DoubleClickEvent event) {
					TreeItem item = tree.getSelection()[0];
					if (item.getExpanded()) {
						item.setExpanded(false);
						handleExpansion(item, false);
					} else {
						handleExpansion(item, true);
						item.setExpanded(true);
					}
				}
			});

			TreeListener listener = new TreeListener() {
				public void treeCollapsed(TreeEvent arg0) {
					TreeItem item = (TreeItem) arg0.item;
					handleExpansion(item, false);
				}

				public void treeExpanded(TreeEvent event) {
					TreeItem item = (TreeItem) event.item;
					handleExpansion(item, true);
				}
			};
			tree.addTreeListener(listener);
		} catch (RuntimeException e) {
			Application.getInstance().handleException(e);
			throw e;
		}
	}
	
	private void handleExpansion(TreeItem item, boolean expanded) {
		try {
			if (expanded) {
				try {
					refreshNode(item, (TraceEntry) item.getData(), null);
				} catch (ApplicationException e) {
					Logger.defaultLogger().error(e);
				}
			} else {
				item.removeAll();
				new TreeItem(item, ITEM_STYLE);
			}
		} catch (RuntimeException e) {
			Application.getInstance().handleException("Error while displaying " + item, e);
			throw e;
		}
	}

	public GregorianCalendar getFromDate() {
		return fromDate;
	}

	public void setFromDate(GregorianCalendar fromDate) {
		this.fromDate = fromDate;
	}

	public ArchiveMedium getMedium() {
		return medium;
	}

	public void setMedium(ArchiveMedium medium) {
		this.medium = medium;
	}

	public void setDisplayNonStoredItemsSize(boolean displayNonStoredItemsSize) {
		this.displayNonStoredItemsSize = displayNonStoredItemsSize;
	}

	public void setLogicalView(boolean logicalView) {
		this.logicalView = logicalView;
	}

	public void refresh(boolean aggregated) throws ApplicationException {
		try {
			this.aggregated = aggregated;
			reset();
			if (medium != null) {
				TraceEntry entry = new TraceEntry();
				entry.setKey("");
				entry.setType(MetadataConstants.T_DIR);

				refreshNode(null, entry, tree);
			}
		} catch (RuntimeException e) {
			Application.getInstance().handleException(e);
			throw e;
		}
	}

	private void refreshNode(TreeItem item, TraceEntry entry, Tree tree) 
	throws ApplicationException {
		// Get data to display
		List entries;
		if (logicalView) {
			entries = this.medium.getLogicalView(context, entry.getKey(), aggregated);
		} else {
			entries = this.medium.getEntries(context, entry.getKey(), fromDate);
		}

		// Remove existing items
		if (item != null) {
			item.removeAll();
		}
		if (tree != null) {
			tree.removeAll();
		}

		// Add new items
		Iterator iter = entries.iterator();
		while (iter.hasNext()) {
			TreeItem chld;
			if (tree == null) {
				chld = new TreeItem(item, ITEM_STYLE);
			} else {
				chld = new TreeItem(tree, ITEM_STYLE);
			}
			chld.setData(iter.next());
			configure(chld);
		}
	}

	private void configure(final TreeItem item) {
		TraceEntry data = (TraceEntry) item.getData();
		long length = 0;
		boolean stored = true;
		if (data.getType() != MetadataConstants.T_SYMLINK && data.getType() != MetadataConstants.T_PIPE) {
			length = Math.max(0, Long.parseLong(data.getData().substring(1)));
			char c = data.getData().charAt(0);
			if (c == '1') {
				stored = true;
			} else if (c == '0') {
				stored = false;
			} else {
				Logger.defaultLogger().warn("Caution : inconsistency in logical view for entry " + data.getKey() + " / " + data.getType() + " / " + data.getData());
			}
		}

		if (stored) {
			item.setForeground(Colors.C_BLACK);
		} else {
			item.setForeground(Colors.C_LIGHT_GRAY);
		}

		if (data.getType() == MetadataConstants.T_SYMLINK || data.getType() == MetadataConstants.T_PIPE) {
			// SymLinks
			item.setFont(deriveItalicFont(item));
		}

		if (
				data.getType() == MetadataConstants.T_DIR 
				|| (data.getType() == MetadataConstants.T_SYMLINK && data.getData().equals("0"))
		) {
			item.setImage(ArecaImages.ICO_FS_FOLDER);
		} else if (data.getType() == MetadataConstants.T_PIPE) {
			item.setImage(ArecaImages.ICO_FS_PIPE);
		} else {
			item.setImage(ArecaImages.ICO_FS_FILE);
		}

		int idx = data.getKey().lastIndexOf('/');
		String label = data.getKey();
		if (idx != -1) {
			label = data.getKey().substring(idx + 1);
		}
		item.setText(0, label);
		if (((!stored) && (!displayNonStoredItemsSize))) {
			item.setText(1, " ");
		} else {
			item.setText(1, Utils.formatFileSize(length));
		}

		if (data.getType() == MetadataConstants.T_DIR) {
			new TreeItem(item, ITEM_STYLE);
		}
	}

	private Font deriveItalicFont(TreeItem item) {
		if (this.italic == null) {
			FontData dt = item.getFont().getFontData()[0];
			FontData dtItalic = new FontData(dt.getName(), dt.getHeight(), SWT.ITALIC);
			this.italic = new Font(item.getDisplay(), new FontData[] { dtItalic });
		}
		return italic;
	}

	public void reset() {
		try {
			this.tree.removeAll();
			this.context.setData(null);
		} catch (RuntimeException e) {
			Application.getInstance().handleException(e);
			throw e;
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void setSelectedEntry(TraceEntry entry) {
		try {
			if (entry != null) {
				StringTokenizer stt = new StringTokenizer(entry.getKey(), "/");
				TreeItem parent = null;
				String element = null;
				while (stt.hasMoreTokens()) {
					element = element == null ? stt.nextToken() : element + "/"
							+ stt.nextToken();
					parent = getElement(parent, element);
					if (parent == null) {
						Logger.defaultLogger().warn("No tree item found for key : [" + element + "]. Initial entry : " + entry.toString());
						break;
					}
					try {
						refreshNode(parent, (TraceEntry) parent.getData(), null);
						parent.setExpanded(true);
					} catch (ApplicationException e) {
						Logger.defaultLogger().error(e);
					}
				}
				if (parent != null) {
					this.tree.setSelection(parent);
				}

				Application.getInstance().setCurrentEntry(entry);
				Application.getInstance().setCurrentFilter(buildFilter(entry));
			}
		} catch (RuntimeException e) {
			Application.getInstance().handleException(e);
			throw e;
		}
	}

	private TreeItem getElement(TreeItem parent, String name) {
		TreeItem[] items = parent == null ? tree.getItems() : parent.getItems();
		for (int i = 0; i < items.length; i++) {
			TreeItem child = items[i];
			TraceEntry data = (TraceEntry) child.getData();
			if (name.equals(data.getKey())) {
				return child;
			}
		}
		return null;
	}

	private UIRecoveryFilter buildFilter(TreeItem[] nodes) {
        UIRecoveryFilter ret = new UIRecoveryFilter();
        
        String[] filter = new String[nodes.length];
        for (int i=0; i<nodes.length; i++) {
            TreeItem current = nodes[i];
            TraceEntry data = (TraceEntry)current.getData();
            ret.initViewable(data);
            filter[i] = data.getKey() + (data.getType() == MetadataConstants.T_DIR ? "/" : "");
            
            if (
            		data.getType() != MetadataConstants.T_SYMLINK 
            		&& data.getType() != MetadataConstants.T_PIPE             		
            		&& data.getData() != null 
            		&& data.getData().length() > 0 
            		&& data.getData().charAt(0) == '0'
            ) {
                ret.setContainsDeletedDirectory(true);
            }
        }
        
        ret.setFilter(filter);
        return ret;
    }

	private UIRecoveryFilter buildFilter(TraceEntry entry) {
		UIRecoveryFilter filter = new UIRecoveryFilter();
		filter.setContainsDeletedDirectory(false);
		filter.initViewable(entry);
		filter.setFilter(new String[] { entry.getKey() });
		return filter;
	}

	public void mouseDoubleClick(MouseEvent e) {
	}

	public void mouseUp(MouseEvent e) {
	}

	public void mouseDown(MouseEvent e) {
		try {
			showMenu(e, logicalView ? Application.getInstance()
					.getArchiveContextMenuLogical() : Application.getInstance()
					.getArchiveContextMenu());
		} catch (RuntimeException e1) {
			Application.getInstance().handleException(e1);
			throw e1;
		}
	}

	private void showMenu(MouseEvent e, Menu m) {
		if (e.button == 3) {
			m.setVisible(true);
		}
	}

	public void handleEvent(Event event) {
		try {
			TreeItem[] selection = tree.getSelection();

			if (selection.length == 1) {
				TraceEntry data = (TraceEntry) (selection[0].getData());
				Application.getInstance().setCurrentEntry(data);
			} else {
				Application.getInstance().setCurrentEntry(null);
			}

			Application.getInstance().setCurrentFilter(buildFilter(selection));
		} catch (RuntimeException e) {
			Application.getInstance().handleException(e);
			throw e;
		}
	}

	public Tree getTree() {
		return tree;
	}
}
