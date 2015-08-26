package com.application.areca.launcher.gui.composites;

import java.io.File;
import java.util.GregorianCalendar;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.application.areca.AbstractTarget;
import com.application.areca.ApplicationException;
import com.application.areca.Utils;
import com.application.areca.cache.ArchiveManifestCache;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.launcher.gui.common.Colors;
import com.application.areca.launcher.gui.common.Refreshable;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.manifest.ManifestKeys;
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
public class PhysicalViewComposite 
extends AbstractTabComposite 
implements SelectionListener, Refreshable {    
	private Table table;
	private TableViewer viewer;
	private Application application = Application.getInstance();
	//private Composite messageBox;
	//private Composite messageMainContainer;
	//private Label lblMessage;
	//private Text txtPath;

	public PhysicalViewComposite(final CTabFolder parent) {
		super(parent, SWT.NONE);
		GridLayout lyt = new GridLayout(6, false);
		lyt.marginHeight = 0;
		lyt.marginBottom = 2;
		lyt.verticalSpacing = 2;
		lyt.marginWidth = 0;
		setLayout(lyt);

		viewer = new TableViewer(this, SWT.BORDER | SWT.MULTI);
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				application.showArchiveDetail(null);
			}
		});

		table = viewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1));
		table.setLinesVisible(AbstractWindow.getTableLinesVisible());
		table.setHeaderVisible(true);
		String[] titles = new String[] {
				ResourceManager.instance().getLabel("mainpanel.description.label"),
				ResourceManager.instance().getLabel("mainpanel.date.label"),
				ResourceManager.instance().getLabel("mainpanel.size.label")
		};

		for (int i=0; i<titles.length; i++) {
			TableColumn column = new TableColumn (table, SWT.NONE);
			column.setText(titles[i]);
			column.setMoveable(true);
		}

		table.getColumn(1).setWidth(AbstractWindow.computeWidth(200));
		table.getColumn(0).setWidth(AbstractWindow.computeWidth(400));
		table.getColumn(2).setWidth(AbstractWindow.computeWidth(150));

		table.getColumn(2).setAlignment(SWT.RIGHT);
		table.addSelectionListener(this);

		table.setMenu(Application.getInstance().getActionContextMenu());

		/*
		messageMainContainer = new Composite(this, SWT.NONE);
		messageMainContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 6, 1));
		GridLayout lytMsg = new GridLayout(1, false);
		lytMsg.marginHeight = 1;
		lytMsg.marginWidth = 1;
		messageMainContainer.setLayout(lytMsg);
		 */

		Label lblIncrementalImg = new Label(this, SWT.NONE);
		lblIncrementalImg.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		lblIncrementalImg.setImage(ArecaImages.ICO_FS_FOLDER);

		Label lblIncremental = new Label(this, SWT.NONE);
		lblIncremental.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		lblIncremental.setText(ResourceManager.instance().getLabel("archivedetail.incremental.label"));

		Label lblDifferentialImg = new Label(this, SWT.NONE);
		lblDifferentialImg.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		lblDifferentialImg.setImage(ArecaImages.ICO_FS_FOLDER_DIFFERENTIAL);

		Label lblDifferential = new Label(this, SWT.NONE);
		lblDifferential.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		lblDifferential.setText(ResourceManager.instance().getLabel("archivedetail.differential.label"));

		Label lblFullImg = new Label(this, SWT.NONE);
		lblFullImg.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		lblFullImg.setImage(ArecaImages.ICO_FS_FOLDER_FULL);

		Label lblFull = new Label(this, SWT.NONE);
		lblFull.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		lblFull.setText(ResourceManager.instance().getLabel("archivedetail.full.label"));

		parent.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Application.getInstance().setLatestVersionRecoveryMode(parent.getSelectionIndex() != 0);
			}
		});
	}
	
	/*
	public void removeViewMessage() {
		if (messageBox != null && (! messageBox.isDisposed())) {
			messageBox.dispose();
		}
		layout(true);
	}
	
	public void showViewMessage() {
		if (messageBox == null || messageBox.isDisposed()) {
			messageBox = new Composite(messageMainContainer, SWT.NONE);
			messageBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			messageBox.setLayout(new GridLayout(2, false));
			
			lblMessage = new Label(messageBox, SWT.NONE);
			lblMessage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			Button btnSearchArchives = new Button(messageBox, SWT.PUSH);
			btnSearchArchives.setText(ResourceManager.instance().getLabel("mainpanel.lookuparchives.label"));
			btnSearchArchives.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			btnSearchArchives.addListener(SWT.Selection, new Listener() {
				
				public void handleEvent(Event arg0) {
					System.out.println("haha");
					PhysicalViewComposite.this.removeViewMessage();
				}
			});
		}
		
		lblMessage.setText(ResourceManager.instance().getLabel("mainpanel.noarchive.label", new Object[] {application.getCurrentTarget().getName(), ((AbstractFileSystemMedium)application.getCurrentTarget().getMedium()).getFileSystemPolicy().getDisplayableParameters(true)}));	
		layout(true);
	}
	
	public void searchArchives() {
		AbstractIncrementalFileSystemMedium medium = (AbstractIncrementalFileSystemMedium)application.getCurrentTarget().getMedium();
		if (txtPath != null && ! txtPath.isDisposed() && txtPath.getText() != null) {
			File found = medium.lookupArchives(txtPath.getText());
			if (found != null) {
				application.getc
				refresh();
			}
		}
	}
	*/

	public void refresh() {
		table.removeAll();

		if (application.isCurrentObjectTarget()) {
			fillTargetData(application.getCurrentTarget());
		} else {
			//removeViewMessage();
		}
	}

	public Object getRefreshableKey() {
		return this.getClass().getName();
	}

	private void fillTargetData(AbstractTarget target) {
		AbstractIncrementalFileSystemMedium medium = (AbstractIncrementalFileSystemMedium)target.getMedium();
		File[] archives = new File[0];
		try {
			Logger.defaultLogger().info("Looking for archives in " + medium.getFileSystemPolicy().getDisplayableParameters(true), "Physical View");
			archives = medium.listArchives(null, null, true);
		} catch (Throwable e) {
			this.application.handleException(e);
		}

		if (archives == null) {
			archives = new File[0];
		}
		
		medium.checkArchivesEncoding(archives);

		for (int i=archives.length-1; i>=0; i--) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setData(archives[i]);

			try {
				Manifest manifest = ArchiveManifestCache.getInstance().getManifest(
						medium, 
						archives[i]
				);

				String prp = null;
				if (manifest != null) {
					item.setText(1, Utils.formatDisplayDate(manifest.getDate()));
					initText(item, 0, manifest);
					prp = manifest.getStringProperty(ManifestKeys.OPTION_BACKUP_SCHEME);
				}

				if (
						(prp != null && prp.equals(AbstractTarget.BACKUP_SCHEME_FULL))
						|| i == 0
				) {
					item.setImage(0, ArecaImages.ICO_FS_FOLDER_FULL);
				} else if (prp != null && prp.equals(AbstractTarget.BACKUP_SCHEME_DIFFERENTIAL)) {
					item.setImage(0, ArecaImages.ICO_FS_FOLDER_DIFFERENTIAL);
				} else {
					item.setImage(0, ArecaImages.ICO_FS_FOLDER);   
				}
				initSize(item, 2, archives[i], medium);
			} catch (ApplicationException e) {
				application.handleException(e);
			}
		}
		
		/*
		if (archives.length == 0) {
			showViewMessage();
		} else {
			removeViewMessage();
		}
		*/
	}

	private void initText(TableItem item, int column, Manifest manifest) {
		if (manifest == null || manifest.getTitle() == null || manifest.getTitle().trim().length() == 0) {
			item.setForeground(column, Colors.C_LIGHT_GRAY);
			item.setText(column, ResourceManager.instance().getLabel("mainpanel.nodesc.label"));
		} else {
			item.setForeground(column, Colors.C_BLACK);
			item.setText(column, manifest.getTitle());
		}   
	}

	private void initSize(TableItem item, int column, File archive, AbstractIncrementalFileSystemMedium medium) throws ApplicationException {
		long prp = medium.getArchiveSize(archive, false);

		if (prp >= 0) {
			item.setForeground(column, Colors.C_BLACK);
			item.setText(column, Utils.formatFileSize(prp));
		} else {
			item.setForeground(column, Colors.C_LIGHT_GRAY);
			item.setText(column, ResourceManager.instance().getLabel("mainpanel.nosize.label"));
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		TableItem[] items = table.getSelection();
		GregorianCalendar first = null;
		GregorianCalendar last = null;
		for (int i=0; i<items.length; i++) {
			File f = (File)items[i].getData();
			Manifest mf = null;
			try {
				mf = ArchiveManifestCache.getInstance().getManifest((AbstractIncrementalFileSystemMedium)Application.getInstance().getCurrentTarget().getMedium(), f);
			} catch (ApplicationException e1) {
				Application.getInstance().handleException(e1);
			}
			if (mf != null && mf.getDate() != null) {
				GregorianCalendar date = mf.getDate();
				if (first == null || date.before(first)) {
					first = date;
				}
				if (last == null || date.after(last)) {
					last = date;
				}
			}
		}

		Application.getInstance().setCurrentDates(first, last);
	}
}
