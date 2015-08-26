package com.application.areca.launcher.gui.composites;

import java.util.GregorianCalendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
import com.application.areca.Utils;
import com.application.areca.impl.AbstractFileSystemMedium;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.launcher.gui.common.Refreshable;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.application.areca.metadata.MetadataConstants;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.trace.ArchiveTraceParser;
import com.application.areca.metadata.trace.TraceEntry;
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
public class LogicalViewComposite 
extends AbstractTabComposite 
implements MouseListener, Refreshable, Listener { 

	private ArchiveExplorer explorer;
	private Table history;
	private Text manifest;
	private Button btnMode;
	private boolean aggregated;
	private Composite pnlView; 
	
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

	public void setSelectedEntry(TraceEntry entry) {
		this.explorer.setSelectedEntry(entry);
		this.initHistoryContent(entry);
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
		TableColumn col3 = new TableColumn(history, SWT.NONE);
		col3.setMoveable(true);
		TableColumn col4 = new TableColumn(history, SWT.NONE);
		col4.setMoveable(true);
		col1.setText(RM.getLabel("archivecontent.actioncolumn.label"));
		col2.setText(RM.getLabel("mainpanel.size.label"));
		col3.setText(RM.getLabel("archivecontent.filedatecolumn.label"));
		col4.setText(RM.getLabel("archivecontent.backupdatecolumn.label"));
		col1.setWidth(AbstractWindow.computeWidth(150));
		col2.setWidth(AbstractWindow.computeWidth(110));
		col3.setWidth(AbstractWindow.computeWidth(150));
		col4.setWidth(AbstractWindow.computeWidth(150));
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
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		content.setLayout(layout);
		GridData dt = new GridData();
		dt.grabExcessVerticalSpace = true;
		dt.verticalAlignment = SWT.FILL;
		dt.grabExcessHorizontalSpace = true;
		dt.horizontalAlignment = SWT.FILL;    
		explorer = new ArchiveExplorer(content, false);
		explorer.setDisplayNonStoredItemsSize(true);
		explorer.setLogicalView(true);
		explorer.getTree().addListener(SWT.Selection, this);
		explorer.setLayoutData(dt);
		
		pnlView = new Composite(content, SWT.NONE);
		pnlView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		pnlView.setLayout(new GridLayout(1, false));
		btnMode = new Button(pnlView, SWT.PUSH);
		btnMode.setText(RM.getLabel("logical.aggregated.label"));
		btnMode.setToolTipText(RM.getLabel("logical.aggregated.tt"));
		btnMode.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		btnMode.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				LogicalViewComposite.this.aggregated = ! LogicalViewComposite.this.aggregated;
				if (LogicalViewComposite.this.aggregated) {
					btnMode.setText(RM.getLabel("logical.current.label"));
					btnMode.setToolTipText(RM.getLabel("logical.current.tt"));
				} else {
					btnMode.setText(RM.getLabel("logical.aggregated.label"));
					btnMode.setToolTipText(RM.getLabel("logical.aggregated.tt"));
				}
				pnlView.layout();
				try {
					Application.getInstance().enableWaitCursor();
					LogicalViewComposite.this.refresh();
				} finally {
					Application.getInstance().disableWaitCursor();
				}
			}
		});
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

		manifest = new Text(content, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		GridData dt2 = new GridData();
		dt2.grabExcessVerticalSpace = true;
		dt2.verticalAlignment = SWT.FILL;
		dt2.grabExcessHorizontalSpace = true;
		dt2.horizontalAlignment = SWT.FILL;
		dt2.horizontalSpan = 2;
		manifest.setLayoutData(dt2);
	}

	public void refresh() {
		if (explorer != null) {
			if (Application.getInstance().isCurrentObjectTarget()) {
			    AbstractFileSystemMedium medium = (AbstractFileSystemMedium)Application.getInstance().getCurrentTarget().getMedium();
	        	Logger.defaultLogger().info("Looking for archives in " + medium.getFileSystemPolicy().getDisplayableParameters(true), "Logical View");
				explorer.setMedium(medium);
				btnMode.setEnabled(! medium.isImage());
			} else {
				explorer.setMedium(null);
			}
			try {
				explorer.refresh(aggregated);
			} catch (ApplicationException e) {
				Logger.defaultLogger().error(e);
			}
			resetHistoryContent();
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
				TraceEntry data = (TraceEntry)(item.getData());

				if (data != null && data.getType() == MetadataConstants.T_FILE) {
					this.initHistoryContent(data);
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
		this.application.setCurrentEntryData(data);

		Manifest mf = data.getManifest();
		String txt = "";
		if (mf != null) {
			String title = mf.getTitle() == null ? "" : mf.getTitle();
			String content = mf.getDescription() == null ? "" : mf.getDescription();
			txt = title + "\n\n" + content;
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

	private void initHistoryContent(TraceEntry entry) {
		try {
			application.enableWaitCursor();

			EntryArchiveData[] currentEntryData = application.getCurrentTarget().getMedium().getHistory(entry.getKey());
			resetHistoryContent();

			for (int i=currentEntryData.length - 1; i>=0; i--) {
				Manifest mf = currentEntryData[i].getManifest();                
				TableItem item = new TableItem(history, SWT.NONE);

				item.setText(0, Application.STATUS_LABELS[currentEntryData[i].getStatus() + 1]); 
				item.setImage(0, Application.STATUS_ICONS[currentEntryData[i].getStatus() + 1]); 
				item.setData(currentEntryData[i]);
				if (mf != null) {
					item.setText(3, Utils.formatDisplayDate(mf.getDate()));
				} else {
					item.setText(3, " ");
				}  	
				if (currentEntryData[i].getHash() != null) {
					try {
						long lastModified = ArchiveTraceParser.extractFileAttributesFromTrace(currentEntryData[i].getHash(), currentEntryData[i].getMetadataVersion()).getLastmodified();
						GregorianCalendar cal = new GregorianCalendar();
						cal.setTimeInMillis(lastModified);
						item.setText(2, Utils.formatDisplayDate(cal));
						
						long size = ArchiveTraceParser.extractFileSizeFromTrace(currentEntryData[i].getHash());
						item.setText(1, Utils.formatFileSize(size));
					} catch (Exception e) {
						Logger.defaultLogger().warn("Error reading data for " + entry.getKey() + " : " + entry.getData(), e);
					}
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
