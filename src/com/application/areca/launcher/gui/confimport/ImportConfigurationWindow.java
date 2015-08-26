package com.application.areca.launcher.gui.confimport;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.application.areca.Workspace;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.SavePanel;
import com.application.areca.launcher.gui.common.SecuredRunner;
import com.application.areca.launcher.gui.composites.ImportTargetTreeComposite;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.myJava.file.FileSystemManager;
import com.myJava.util.log.Logger;
import com.myJava.util.xml.AdapterException;

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
public class ImportConfigurationWindow
extends AbstractWindow {
	private static final ResourceManager RM = ResourceManager.instance();

	private Text location;
	private Button saveButton;
	private ImportTargetTreeComposite view;

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);

		Label lblAdvise = new Label(composite, SWT.NONE);
		lblAdvise.setText(RM.getLabel("importconf.intro.label"));
		lblAdvise.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(composite, SWT.NONE);

		Group grpLocation = new Group(composite, SWT.NONE);
		grpLocation.setText(RM.getLabel("importconf.location.label"));
		GridLayout grpLayout = new GridLayout(2, false);
		grpLocation.setLayout(grpLayout);
		grpLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		location = new Text(grpLocation, SWT.BORDER);
		location.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				File f= new File(location.getText());
				final String path;
				if (FileSystemManager.exists(f)) {
					if (FileSystemManager.isFile(f)) {
						path = Application.getInstance().showDirectoryDialog(FileSystemManager.getParent(f), ImportConfigurationWindow.this);
					} else {
						path = Application.getInstance().showDirectoryDialog(FileSystemManager.getAbsolutePath(f), ImportConfigurationWindow.this);
					}
				} else {
					path = Application.getInstance().showDirectoryDialog(ImportConfigurationWindow.this);
				}

				Runnable rn = new Runnable() {
					public void run() {
						if (path != null) {
							try {
								application.enableWaitCursor(ImportConfigurationWindow.this);

								SecuredRunner.execute(new Runnable() {
									public void run() {
										location.setText(path);
									}
								});

								Workspace workspace = null;

								try {
									workspace = Workspace.open(path, application, false);
								} catch (AdapterException e) {
									Logger.defaultLogger().warn("Some files will be ignored : " + e.getMessage());
								}

								view.setWorkspace(workspace);
							} finally {
								application.disableWaitCursor(ImportConfigurationWindow.this);
							}
						}
					}
				};
				
				Thread th = new Thread(rn);
				th.setDaemon(true);
				th.start();
			}
		};

		Button btnBrowse = new Button(grpLocation, SWT.PUSH);
		btnBrowse.setText(RM.getLabel("common.browseaction.label"));
		btnBrowse.addListener(SWT.Selection, listener);
		btnBrowse.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		view = new ImportTargetTreeComposite(composite, this);
		GridData dt = new GridData(SWT.FILL, SWT.FILL, true, true);
		dt.heightHint = computeHeight(200);
		view.setLayoutData(dt);

		SavePanel pnlSave = new SavePanel(RM.getLabel("common.import.label"), this);
		pnlSave.buildComposite(composite).setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
		saveButton = pnlSave.getBtnSave();

		composite.pack();
		return composite;
	}

	public void treeItemSelected() {
		registerUpdate();
	}

	public String getTitle() {
		return RM.getLabel("importconf.dialog.title");
	}

	protected boolean checkBusinessRules() {
		boolean ok = true;

		if (view.getSelectedItems().length == 0) {
			ok = false;
		}

		return ok;
	}

	protected void saveChanges() {
		WorkspaceItemImportHelper.importWorkspaceItems(view.getSelectedItems());
		this.hasBeenUpdated = false;
		this.close();
	}

	protected void updateState(boolean rulesSatisfied) {
		this.saveButton.setEnabled(rulesSatisfied);
	}
}
