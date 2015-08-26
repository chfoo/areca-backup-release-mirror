package com.application.areca.launcher.gui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.application.areca.ArecaURLs;
import com.application.areca.Utils;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.launcher.gui.common.ListPane;
import com.application.areca.launcher.gui.composites.DonationLink;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.application.areca.plugins.Plugin;
import com.application.areca.plugins.PluginRegistry;
import com.application.areca.version.VersionInfos;
import com.myJava.file.FileTool;
import com.myJava.system.OSTool;
import com.myJava.system.viewer.ViewerHandlerHelper;
import com.myJava.util.log.Logger;
import com.myJava.util.version.VersionData;

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
public class AboutWindow
extends AbstractWindow
implements ArecaURLs {
	private static final int CURRENT_YEAR = VersionInfos.getLastVersion().getYear();
	private static final int widthHint = computeWidth(400);
	private static final int heightHint = computeHeight(250);

	private static final ResourceManager RM = ResourceManager.instance();
	private static final String CREDITS_TXT_WIN = RM.getLabel("about.creditswin2.label", new Object[] {VersionInfos.APP_NAME});

	private ListPane tabs;
	private int selectedIndex = 0;

	public AboutWindow(int selectedIndex) {
		super();
		this.selectedIndex = selectedIndex;
	}

	protected Control createContents(Composite parent) {
		application.enableWaitCursor();
		Composite ret = new Composite(parent, SWT.NONE);
		try {
			GridLayout layout = new GridLayout(2, false);
			ret.setLayout(layout);

			Label icon = new Label(ret, SWT.NONE);
			icon.setImage(ArecaImages.ICO_BIG);
			icon.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));

			tabs = new ListPane(ret, SWT.NONE, true);
			tabs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
			initAboutContent(addTab(tabs, RM.getLabel("about.abouttab.label", new Object[] {VersionInfos.APP_NAME})));
			initCreditsContent(addTab(tabs, RM.getLabel("about.creditstab.label")));
			initHistoryContent(addTab(tabs, RM.getLabel("about.historytab.label")));
			initLicenseContent(addTab(tabs, RM.getLabel("about.licensetab.label")));
			initPluginsContent(addTab(tabs, RM.getLabel("about.pluginstab.label")));
			initSystemContent(addTab(tabs, RM.getLabel("about.systemtab.label")));

			GridData dt3 = new GridData(SWT.CENTER, SWT.BOTTOM, false, true);
			Link lnk = new Link(ret, SWT.NONE);
			lnk.addListener (SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					try {
						ViewerHandlerHelper.getViewerHandler().browse(new URL(event.text));
					} catch (Exception e) {
						Logger.defaultLogger().error(e);
					}
				}
			});
			lnk.setText("<A HREF=\"" + ARECA_URL + "\">areca-backup.org</A>");
			lnk.setLayoutData(dt3);

			tabs.setSelection(selectedIndex);
			ret.pack();
		} finally {
			application.disableWaitCursor();
		}
		return ret;
	}
	
	public void setSelection(int tab) {
		tabs.setSelection(tab);
	}

	private Composite addTab(ListPane tabs, String titleKey) {
		Composite tab = tabs.addElement(titleKey, titleKey);
		return tab;        
	}

	private void initAboutContent(Composite composite) {
		Text content = configurePanel(composite, SWT.WRAP);
		String txt =
				VersionInfos.APP_NAME +
				"\n" + RM.getLabel("about.version.label") + " " + VersionInfos.getLastVersion().getVersionId() + " - " + VersionInfos.formatVersionDate(VersionInfos.getLastVersion().getVersionDate()) +
				"\n\n" + RM.getLabel("about.copyright.label", new Object[] {""+CURRENT_YEAR});

		content.setText(txt);
	}

	private void initCreditsContent(Composite composite) {
		Text content = configurePanel(composite, SWT.WRAP);
		String txt =  RM.getLabel("about.translators");

		String contribs = RM.getLabel("about.contributors");
		if (contribs != null && contribs.trim().length() != 0) {
			txt += "\n\n" + contribs;
		}

		txt += "\n\n" + RM.getLabel("about.credits.label");

		if (OSTool.isSystemWindows()) {
			txt += "\n\n" + CREDITS_TXT_WIN;
		}

		content.setText(txt);
	}

	private void initPluginsContent(Composite composite) {
		Link lnk0 = new Link(composite, SWT.NONE);
		lnk0.addListener (SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				try {
					ViewerHandlerHelper.getViewerHandler().browse(new URL(event.text));
				} catch (Exception e) {
					Logger.defaultLogger().error(e);
				}
			}
		});
		lnk0.setText("<A HREF=\"" + ArecaURLs.PLUGINS_URL + "\">" + RM.getLabel("plugins.getmore.label") + "</A>");
		lnk0.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, false));
		
		TableViewer viewer = new TableViewer(composite, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		Table tblProc = viewer.getTable();

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);

		GridData dt = new GridData();
		dt.grabExcessHorizontalSpace = true;
		dt.grabExcessVerticalSpace = true;
		dt.verticalAlignment = SWT.FILL;
		dt.horizontalAlignment = SWT.FILL;
		dt.heightHint = heightHint;
		dt.widthHint = widthHint;
		
		tblProc.setLayoutData(dt);

		TableColumn col1 = new TableColumn(tblProc, SWT.NONE);
		col1.setText(RM.getLabel("plugins.name.label"));
		col1.setWidth(AbstractWindow.computeWidth(130));
		col1.setMoveable(true);

		TableColumn col2 = new TableColumn(tblProc, SWT.NONE);
		col2.setText(RM.getLabel("plugins.version.label"));
		col2.setWidth(AbstractWindow.computeWidth(70));
		col2.setMoveable(true);
		
		TableColumn col3 = new TableColumn(tblProc, SWT.NONE);
		col3.setText(RM.getLabel("plugins.location.label"));
		col3.setWidth(AbstractWindow.computeWidth(140));
		col3.setMoveable(true);
		
		TableColumn col4 = new TableColumn(tblProc, SWT.NONE);
		col4.setText(RM.getLabel("plugins.description.label"));
		col4.setWidth(AbstractWindow.computeWidth(200));
		col4.setMoveable(true);

		tblProc.setHeaderVisible(true);
		tblProc.setLinesVisible(AbstractWindow.getTableLinesVisible());

		Iterator iter = PluginRegistry.getInstance().getAll(Plugin.class, true).iterator();
		while (iter.hasNext()) {
			Plugin plg = (Plugin)iter.next();

			TableItem item = new TableItem(tblProc, SWT.NONE);
			item.setText(0, plg.getFullName());
			item.setText(1, plg.getVersionData().getVersionId());
			
			String path = plg.getPath() != null ? plg.getPath() : RM.getLabel("plugins.internal.label");
			item.setText(2, path);
			item.setText(3, plg.getDescription() == null ? plg.getFullName() : plg.getDescription());
		}

		Link lnk = DonationLink.build(composite);
		lnk.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
	}

	private void initHistoryContent(Composite composite) {
		Text content = configurePanel(composite, SWT.H_SCROLL);
		StringBuffer sb = new StringBuffer();
		Iterator iter = VersionInfos.getVersions().iterator();
		while (iter.hasNext()) {
			VersionData data = (VersionData)iter.next();
			sb.append(data.getVersionId());
			if (data.getVersionId().length() <= 3) {
				sb.append("   ");
			}
			sb.append("\t\t\t").append(data.getDescription());
			if (data.getAdditionalNotes() != null) {
				sb.append(" - ").append(data.getAdditionalNotes());
			}
			sb.append("\n");
		}

		content.setText(sb.toString());
	}

	private void initLicenseContent(Composite composite) {      
		Text content = configurePanel(composite, SWT.H_SCROLL);
		try {
			URL url = ClassLoader.getSystemClassLoader().getResource("COPYING");
			if (url != null) {
				InputStream in = url.openStream();
				FileTool tool = FileTool.getInstance();
				content.setText(tool.getInputStreamContent(in, true));
			} else {
				content.setText("No license file found !");
			}
		} catch (IOException e) {
			application.handleException("Error reading license file", e);
		}     
	}

	private void initSystemContent(Composite composite) {
		Text content = configurePanel(composite, SWT.H_SCROLL);
		content.setText(Utils.getPropertiesAndPreferences());
	}

	private Text configurePanel(Composite composite, int style) {
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);

		GridData dt = new GridData();
		dt.grabExcessHorizontalSpace = true;
		dt.grabExcessVerticalSpace = true;
		dt.verticalAlignment = SWT.FILL;
		dt.horizontalAlignment = SWT.FILL;
		dt.heightHint = heightHint;
		dt.widthHint = widthHint;

		Text content = new Text(composite, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER | style);
		content.setEditable(false);
		content.setLayoutData(dt);

		Link lnk = DonationLink.build(composite);
		lnk.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		return content;
	}

	protected boolean checkBusinessRules() {
		return true;
	}

	public String getTitle() {
		return ResourceManager.instance().getLabel("about.dialog.title");
	}

	protected void saveChanges() {
	}

	protected void updateState(boolean rulesSatisfied) {
	}
}
