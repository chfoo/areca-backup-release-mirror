package com.application.areca.launcher.gui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.application.areca.ArecaTechnicalConfiguration;
import com.application.areca.MemoryHelper;
import com.application.areca.ResourceManager;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.plugins.StoragePlugin;
import com.application.areca.plugins.StoragePluginRegistry;
import com.application.areca.version.VersionInfos;
import com.myJava.file.FileTool;
import com.myJava.util.log.Logger;
import com.myJava.util.os.OSTool;
import com.myJava.util.version.VersionData;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2162742295696737000
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
public class AboutWindow
extends AbstractWindow {
    private static final int widthHint = computeWidth(450);
    private static final int heightHint = computeHeight(140);
    
    private static final ResourceManager RM = ResourceManager.instance();
    private static final String CREDITS_TXT_WIN = RM.getLabel("about.creditswin.label", new Object[] {VersionInfos.APP_NAME});

    protected Control createContents(Composite parent) {
        application.enableWaitCursor();
        Composite ret = new Composite(parent, SWT.NONE);
        try {
            FillLayout layout = new FillLayout();
            ret.setLayout(layout);

            CTabFolder tabs = new CTabFolder(ret, SWT.BORDER);
            tabs.setSimple(Application.SIMPLE_SUBTABS);

            initAboutContent(addTab(tabs, RM.getLabel("about.abouttab.label", new Object[] {VersionInfos.APP_NAME})));
            initCreditsContent(addTab(tabs, RM.getLabel("about.creditstab.label")));
            initHistoryContent(addTab(tabs, RM.getLabel("about.historytab.label")));
            initLicenseContent(addTab(tabs, RM.getLabel("about.licensetab.label")));
            initSystemContent(addTab(tabs, RM.getLabel("about.systemtab.label")));

            tabs.setSelection(0);
            
            tabs.pack();
        } finally {
            application.disableWaitCursor();
        }
        return ret;
    }
    
    private Composite addTab(CTabFolder tabs, String titleKey) {
        CTabItem itm = new CTabItem(tabs, SWT.NONE);
        itm.setText(titleKey + "    ");
        
        Composite tab = new Composite(tabs, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        tab.setLayout(layout);
        layout.horizontalSpacing = 30;
        layout.verticalSpacing = 30;
        layout.marginHeight = 30;
        layout.marginWidth = 30;
        itm.setControl(tab);
        Label icon = new Label(tab, SWT.NONE);
        icon.setImage(ArecaImages.ICO_BIG);
        GridData dt = new GridData();
        dt.horizontalAlignment = SWT.CENTER;
        dt.verticalAlignment = SWT.TOP;
        icon.setLayoutData(dt);
        
        Composite content = new Composite(tab, SWT.NONE);
        GridData dt2 = new GridData();
        dt2.grabExcessVerticalSpace = true;
        dt2.grabExcessHorizontalSpace = true;
        dt2.verticalAlignment = SWT.FILL;
        dt2.horizontalAlignment = SWT.FILL;
        dt2.verticalSpan = 2;
        content.setLayoutData(dt2);
        
        GridData dt3 = new GridData();
        dt3.grabExcessVerticalSpace = true;
        dt3.grabExcessHorizontalSpace = true;
        dt3.verticalAlignment = SWT.BOTTOM;
        dt3.horizontalAlignment = SWT.CENTER;
        Link lnk = new Link(tab, SWT.NONE);
        lnk.addListener (SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                try {
                    OSTool.launchBrowser(event.text);
                } catch (Exception e) {
                    Logger.defaultLogger().error(e);
                }
            }
        });
        lnk.setText("<A HREF=\"http://areca.sourceforge.net\">areca.sf.net</A>");
        lnk.setLayoutData(dt3);
        
        return content;        
    }

    private void initAboutContent(Composite composite) {
        Text content = configurePanel(composite, SWT.WRAP);
        content.setText(
                VersionInfos.APP_NAME + " - " + RM.getLabel("about.appdescription.label") +
                "\n" + RM.getLabel("about.version.label") + " " + VersionInfos.getLastVersion().getVersionId() + " - " + VersionInfos.formatVersionDate(VersionInfos.getLastVersion().getVersionDate()) + "." +
                "\n\n" + RM.getLabel("about.copyright.label")
        );

        content.forceFocus();
    }
    
    private void initCreditsContent(Composite composite) {
        Text content = configurePanel(composite, SWT.WRAP);
        String txtCredits = RM.getLabel("about.credits.label");
        if (OSTool.isSystemWindows() ) {
            txtCredits += "\n\n" + CREDITS_TXT_WIN;
        }               
        content.setText(txtCredits);
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
            sb.append("\t\t\t").append(data.getDescription()).append("\n");
        }
        
        content.setText(sb.toString());
    }
    
    private void initLicenseContent(Composite composite) {      
        Text content = configurePanel(composite, SWT.H_SCROLL);
        try {
            URL url = ClassLoader.getSystemClassLoader().getResource("license.txt");
            InputStream in = url.openStream();
            FileTool tool = new FileTool();
            content.setText(tool.getInputStreamContent(in, true));
        } catch (IOException e) {
            application.handleException("Error reading license file", e);
        }     
    }
    
    private void initSystemContent(Composite composite) {
        Text content = configurePanel(composite, SWT.H_SCROLL);
        
        Properties prps = System.getProperties();
        System.gc();
        prps.put("system.free.memory", "" + OSTool.getFreeMemory());
        prps.put("system.memory.usage", "" + OSTool.getMemoryUsage());
        prps.put("system.total.memory", "" + OSTool.getTotalMemory());
        prps.put("system.max.available.memory", "" + OSTool.getMaxMemory());
        prps.put("file.encoding.iana", OSTool.getIANAFileEncoding());
        prps.put("areca.max.manageable.entries", "" + MemoryHelper.getMaxManageableEntries());
        prps.put("areca.version", VersionInfos.getLastVersion().getVersionId());
        prps.put("areca.build.id", "" + VersionInfos.getBuildId());
        
        URL url = ClassLoader.getSystemClassLoader().getResource("languages.txt");
        prps.put("areca.languages.url", url.toExternalForm());
        
        prps.putAll(ArecaTechnicalConfiguration.get().getProperties());
        
        // Plugins
        Iterator iter = StoragePluginRegistry.getInstance().getAll().iterator();
        String plugins = "";
        while (iter.hasNext()) {
            StoragePlugin plugin = (StoragePlugin)iter.next();
            if (plugins.length() != 0) {
                plugins += ", ";
            }
            plugins += plugin.getFullName();
        }
        prps.put("areca.plugins", plugins);
        
        String[] keys = (String[])prps.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<keys.length; i++) {
            String key = keys[i];
            String value = prps.getProperty(key).replace('\n', ' ').replace('\r', ' ');
            sb.append(key).append(" : ").append(value).append("\n");
        }
        content.setText(sb.toString());
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
