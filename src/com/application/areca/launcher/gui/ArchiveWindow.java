package com.application.areca.launcher.gui;

import java.io.File;
import java.util.GregorianCalendar;
import java.util.Iterator;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.application.areca.ApplicationException;
import com.application.areca.ArchiveMedium;
import com.application.areca.Utils;
import com.application.areca.cache.ArchiveManifestCache;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.impl.FileSystemTarget;
import com.application.areca.impl.handler.DeltaArchiveHandler;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ListPane;
import com.application.areca.launcher.gui.composites.ArchiveExplorer;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.trace.TraceEntry;
import com.myJava.file.FileSystemManager;
import com.myJava.file.delta.DeltaLayer;
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
public class ArchiveWindow
extends AbstractWindow {

	protected Manifest manifest;
	protected ArchiveMedium medium;
	protected TraceEntry currentEntry;
	protected GregorianCalendar date;

	public ArchiveWindow(Manifest manifest, GregorianCalendar date, ArchiveMedium medium) {
		super();
		this.manifest = manifest;
		this.medium = medium;
		this.date = date;
	}

	protected Control createContents(Composite parent) {
		Composite ret = new Composite(parent, SWT.NONE);
		try {
			GridLayout layout = new GridLayout(1, false);
			ret.setLayout(layout);

			ListPane tabs = new ListPane(ret, SWT.NONE, false);
			GridData dt = new GridData(SWT.FILL, SWT.FILL, true, true);
			dt.heightHint = computeHeight(400);
			dt.widthHint = computeWidth(750);
			tabs.setLayoutData(dt);

			Composite itm1 = tabs.addElement("archivedetail.manifest.label", RM.getLabel("archivedetail.manifest.label"));
			initDataPanel(itm1);

			Composite itm2 = tabs.addElement("archivedetail.archivecontent.label", RM.getLabel("archivedetail.archivecontent.label"));
			initContentPanel(itm2);

			if (currentEntry == null) {
				tabs.setSelection(0);
			} else {
				tabs.setSelection(1);
			}

			// Add debugging listener
			ret.addKeyListener(new org.eclipse.swt.events.KeyAdapter() {
				public void keyPressed(KeyEvent event) {
					if ((event.stateMask & SWT.CTRL) != 0 && (event.stateMask & SWT.ALT) != 0 && event.keyCode == 100) {
						Runnable rn = new Runnable() {
							public void run() {
								try {
									FileSystemTarget target = (FileSystemTarget)Application.getInstance().getCurrentTarget();
									AbstractIncrementalFileSystemMedium medium = (AbstractIncrementalFileSystemMedium)target.getMedium();
									File archive = medium.getLastArchive(null, date);

									if (archive != null) {
										Logger.defaultLogger().fine("Creating debugging informations for target " + target.getUid() + " and archive " + archive);
										application.enableWaitCursor(ArchiveWindow.this);

										Manifest mf = ArchiveManifestCache.getInstance().getManifest(medium, archive);
										if (mf != null) {
											Logger.defaultLogger().fine("Manifest : ");
											Logger.defaultLogger().fine(mf.toString());
										}
									}
								} catch (Exception e) {
									Logger.defaultLogger().error("Error caught while creating debugging informations", e);
									application.handleException("Error caught while creating debugging informations", e);
								} finally {
									application.disableWaitCursor(ArchiveWindow.this);
								}
							}
						};

						Thread th = new Thread(rn);
						th.setName("Create debugging data for archive");
						th.start();
					}
				}
			});
		} finally {
			application.disableWaitCursor();
		}
		return ret;
	}

	private void initContentPanel(Composite parent) {
		parent.setLayout(new FillLayout());
		ArchiveExplorer explorer = new ArchiveExplorer(parent, false);
		explorer.setMedium(Application.getInstance().getCurrentTarget().getMedium());
		explorer.setFromDate(date);
		explorer.setDisplayNonStoredItemsSize(false);
		explorer.setLogicalView(false);

		try {
			explorer.refresh(false);
		} catch (ApplicationException e) {
			Logger.defaultLogger().error(e);
		}

		if (currentEntry != null) {
			explorer.setSelectedEntry(this.currentEntry);
		}
	}

	private void initDataPanel(Composite parent) {
		parent.setLayout(new FillLayout());
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		// TITLE
		Text txtTitle = null;
		if (manifest != null && manifest.getTitle() != null && !manifest.getTitle().equals("")) {
			Group grpTitle = new Group(composite, SWT.NONE);
			grpTitle.setText(RM.removeDots(RM.getLabel("archivedetail.titlefield.label")));
			grpTitle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			grpTitle.setLayout(new GridLayout());

			txtTitle = new Text(grpTitle, SWT.BORDER);
			txtTitle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			txtTitle.setEditable(false);
		}

		// DATE
		Group grpDate = new Group(composite, SWT.NONE);
		grpDate.setText(RM.removeDots(RM.getLabel("archivedetail.datefield.label")));
		grpDate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		grpDate.setLayout(new GridLayout());

		Text txtDate = new Text(grpDate, SWT.BORDER);
		txtDate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		txtDate.setEditable(false);

		// DESCRIPTION
		Text txtDescription = null;
		if (manifest != null && manifest.getDescription() != null && !manifest.getDescription().equals("")) {
			Group grpDescription = new Group(composite, SWT.NONE);
			grpDescription.setText(RM.removeDots(RM.getLabel("archivedetail.descriptionfield.label")));
			grpDescription.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			grpDescription.setLayout(new GridLayout());

			txtDescription = new Text(grpDescription, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
			GridData ldDescription = new GridData(SWT.FILL, SWT.FILL, true, true);
			ldDescription.heightHint = computeHeight(50);
			ldDescription.minimumHeight = computeHeight(50);
			txtDescription.setLayoutData(ldDescription);
			txtDescription.setEditable(false);
		}

		// PROPERTIES
		Group grpProperties = new Group(composite, SWT.NONE);
		grpProperties.setText(RM.removeDots(RM.getLabel("archivedetail.propertiesfield.label")));
		grpProperties.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		grpProperties.setLayout(new GridLayout());

		TableViewer viewer = new TableViewer(grpProperties, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		Table table = viewer.getTable();
		table.setLinesVisible(AbstractWindow.getTableLinesVisible());
		table.setHeaderVisible(true);
		GridData ldProperties = new GridData(SWT.FILL, SWT.FILL, true, true);
		ldProperties.minimumHeight = computeHeight(100);
		table.setLayoutData(ldProperties);
		TableColumn col1 = new TableColumn (table, SWT.NONE);
		col1.setWidth(AbstractWindow.computeWidth(300));
		col1.setText(RM.getLabel("archivedetail.propertycolumn.label"));
		col1.setMoveable(true);
		TableColumn col2 = new TableColumn (table, SWT.NONE);
		col2.setWidth(AbstractWindow.computeWidth(150));
		col2.setText(RM.getLabel("archivedetail.valuecolumn.label"));
		col2.setMoveable(true);

		// INIT DATA
		if (manifest != null) {
			if (txtTitle != null) {
				txtTitle.setText(manifest.getTitle() == null ? "" : manifest.getTitle());
			}
			txtDate.setText(manifest.getDate() == null ? "" : Utils.formatDisplayDate(manifest.getDate()));
			if (txtDescription != null) {
				txtDescription.setText(manifest.getDescription() == null ? "" : manifest.getDescription());
			}

			TableItem item0 = new TableItem(table, SWT.NONE);
			item0.setText(0, " ** Type ** ");
			item0.setText(1, manifest.getType() == Manifest.TYPE_BACKUP ? "Backup" : "Merge");

			Iterator iter = this.manifest.propertyIterator();
			while (iter.hasNext()) {
				String key = (String)iter.next();
				if (! key.equals("Source")) { // Hide old properties
					TableItem item = new TableItem(table, SWT.NONE);
					item.setText(0, key + " : ");
					item.setText(1, manifest.getStringProperty(key));
				}
			}
		}

		/*
		if (txtTitle != null) {
			txtTitle.forceFocus();
		} else {
			txtDate.forceFocus();
		}
		*/

		composite.pack();
	}

	public void setCurrentEntry(TraceEntry currentEntry) {
		this.currentEntry = currentEntry;
	}

	protected boolean checkBusinessRules() {
		return true;
	}

	public String getTitle() {
		return RM.getLabel("archivedetail.archive.title");
	}

	protected void saveChanges() {
	}

	protected void updateState(boolean rulesSatisfied) {
	}
}
