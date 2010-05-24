package com.application.areca.launcher.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.application.areca.ResourceManager;
import com.application.areca.TranslationData;
import com.application.areca.Utils;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ArecaPreferences;
import com.application.areca.launcher.gui.common.ListPane;
import com.application.areca.launcher.gui.common.SavePanel;

/**
 * <BR>
 * @author Stephane BRUNEL
 * <BR>
 *
 */

 /*
 Copyright 2005-2010, Olivier PETRUCCI.

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
public class PreferencesWindow 
extends AbstractWindow {
    private static final ResourceManager RM = ResourceManager.instance();
    
    private Combo langCombo;
    private Button openLastWorkspace;
    private Button openDefaultWorkspace;
    private Button informationSynthetic;
    private Text defaultWorkspace;
    private Text defaultArchiveStorage;
    private Button displayReport;
    private Text editor;
    private Text dateFormat;
    private Button btnSave;
    private Button checkNewVersions;
    private Button showWSPath;
    private Button showToolBar;

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        
        ListPane pane = new ListPane(composite, SWT.BORDER, true);
        GridData dt = new GridData(SWT.FILL, SWT.FILL, true, true);
        dt.heightHint = computeHeight(200);
        dt.widthHint = computeWidth(650);
        pane.setLayoutData(dt);
        Composite grpDisp = pane.addElement("appearence", RM.getLabel("preferences.appearence.title"));
        Composite grpStart = pane.addElement("startup", RM.getLabel("preferences.startup.title"));
        Composite grpArchives = pane.addElement("workspace", RM.getLabel("preferences.workspace.title"));

        grpDisp.setLayout(new GridLayout(2, false));
        buildAppearenceComposite(grpDisp);
             
        grpStart.setLayout(new GridLayout(3, false));
        buildStartupComposite(grpStart);
       
        grpArchives.setLayout(new GridLayout(3, false));
        buildArchivesComposite(grpArchives);
        
        SavePanel pnlSave = new SavePanel(this);
        pnlSave.buildComposite(composite).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        btnSave = pnlSave.getBtnSave();
        
        pane.setSelection(0);
        composite.pack();
        return composite;
    }
    
    private void buildAppearenceComposite(Composite parent) {
        Label lblLang = new Label(parent, SWT.NONE);
        lblLang.setText(RM.getLabel("preferences.lang.label"));
        langCombo = new Combo(parent, SWT.READ_ONLY);
        fillLangCombo();
        monitorControl(langCombo);
        
        Label lblDate = new Label(parent, SWT.NONE);
        lblDate.setText(RM.getLabel("preferences.dateformat.label"));
        lblDate.setToolTipText(RM.getLabel("preferences.dateformat.tt"));
        dateFormat = new Text(parent, SWT.BORDER);
        dateFormat.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        dateFormat.setText(ArecaPreferences.getDateFormat() == null ? "" : ArecaPreferences.getDateFormat());
        dateFormat.setToolTipText(RM.getLabel("preferences.dateformat.tt"));
        monitorControl(dateFormat);
        
        new Label(parent, SWT.NONE).setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        
        informationSynthetic = new Button(parent, SWT.CHECK);
        informationSynthetic.setText(RM.getLabel("preferences.synthetic.label"));
        informationSynthetic.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
        informationSynthetic.setSelection(ArecaPreferences.isInformationSynthetic());
        monitorControl(informationSynthetic);
        
        showToolBar = new Button(parent, SWT.CHECK);
        showToolBar.setText(RM.getLabel("preferences.show.tb.label"));
        showToolBar.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
        showToolBar.setSelection(ArecaPreferences.isDisplayToolBar());
        monitorControl(showToolBar);
        
        showWSPath = new Button(parent, SWT.CHECK);
        showWSPath.setText(RM.getLabel("preferences.show.ws.path.label"));
        showWSPath.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
        showWSPath.setSelection(ArecaPreferences.isDisplayWSAddress());
        monitorControl(showWSPath);
        
    }
    
    private void buildStartupComposite(Composite parent) {
        GridData dtLast = new GridData();
        dtLast.horizontalSpan = 3;
        openLastWorkspace = new Button(parent, SWT.RADIO);
        openLastWorkspace.setText(RM.getLabel("preferences.lastworkspace.label"));
        openLastWorkspace.setLayoutData(dtLast);
        
        openDefaultWorkspace = new Button(parent, SWT.RADIO);
        openDefaultWorkspace.setText(RM.getLabel("preferences.opendefaultworkspace.label"));
        int startupMode = ArecaPreferences.getStartupMode();
        if (startupMode == ArecaPreferences.UNDEFINED || startupMode == ArecaPreferences.LAST_WORKSPACE_MODE) {
            openLastWorkspace.setSelection(true);
        } else {
            openDefaultWorkspace.setSelection(true);
        }
        monitorControl(openDefaultWorkspace);
        
        defaultWorkspace = new Text(parent, SWT.BORDER);
        defaultWorkspace.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        defaultWorkspace.setText(ArecaPreferences.getDefaultWorkspace());
        monitorControl(defaultWorkspace);
        
        Button btnBrowse = new Button(parent, SWT.PUSH);
        btnBrowse.setText(RM.getLabel("common.browseaction.label"));
        btnBrowse.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                String path = Application.getInstance().showDirectoryDialog(defaultWorkspace.getText(), PreferencesWindow.this);
                if (path != null) {
                    defaultWorkspace.setText(path);
                }
            }
        });
        
        checkNewVersions = new Button(parent, SWT.CHECK);
        checkNewVersions.setText(RM.getLabel("preferences.checkversions.label"));
        checkNewVersions.setToolTipText(RM.getLabel("preferences.checkversions.tt"));    
        checkNewVersions.setSelection(ArecaPreferences.isCheckNewVersions());
        checkNewVersions.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
        monitorControl(checkNewVersions);   
    }
    
    private void buildArchivesComposite(Composite parent) {
        Label lblDefaultStorage = new Label(parent, SWT.NONE);
        lblDefaultStorage.setText(RM.getLabel("preferences.defaultstorage.label"));
        
        defaultArchiveStorage = new Text(parent, SWT.BORDER);
        defaultArchiveStorage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        defaultArchiveStorage.setText(ArecaPreferences.getDefaultArchiveStorage());
        monitorControl(defaultArchiveStorage);
        
        Button btnBrowse = new Button(parent, SWT.PUSH);
        btnBrowse.setText(RM.getLabel("common.browseaction.label"));
        btnBrowse.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                String path = Application.getInstance().showDirectoryDialog(defaultArchiveStorage.getText(), PreferencesWindow.this);
                if (path != null) {
                    defaultArchiveStorage.setText(path);
                }
            }
        });
        
        Label lblEditor = new Label(parent, SWT.NONE);
        lblEditor.setText(RM.getLabel("preferences.editor.label"));
        editor = new Text(parent, SWT.BORDER);
        editor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        editor.setText(ArecaPreferences.getEditionCommand());
        monitorControl(editor);
        
        displayReport = new Button(parent, SWT.CHECK);
        displayReport.setText(RM.getLabel("preferences.displayreport.label"));
        displayReport.setSelection(ArecaPreferences.getDisplayReport());
        displayReport.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
        monitorControl(displayReport);
    }
    
    private void fillLangCombo() {
        try {
        	String currentLg = ArecaPreferences.resolveLanguage();

        	TranslationData[] lges = Utils.getTranslations();
            for (int i=0; i<lges.length; i++) {
            	String lg = lges[i].getLanguage();
            	if (lges[i].isDeprecated()) {
            		lg += " (" + RM.getLabel("common.deprecated.label") + ")";
            	}
                langCombo.add(lg);
                langCombo.setData(lg, lges[i]);
                
                if (lges[i].getLanguage().equals(currentLg)) {
                    langCombo.select(i);
                }
            }
        } catch (Exception ex) {
            this.application.handleException(ex);
        }
    }

    public String getTitle() {
        return RM.getLabel("preferences.dialog.title");
    }

    protected boolean checkBusinessRules() {
        resetErrorState(langCombo);
        if (langCombo.getSelectionIndex() == -1) {
            setInError(langCombo, RM.getLabel("error.field.mandatory"));
            return false;
        }
        return true;
    }

    protected void saveChanges() {       
        if (langCombo.getSelectionIndex() != -1) {
            String lang = (String)langCombo.getItem(langCombo.getSelectionIndex());
            ArecaPreferences.setLang(((TranslationData)langCombo.getData(lang)).getLanguage());
        }
        ArecaPreferences.setStartupMode(openLastWorkspace.getSelection() ? ArecaPreferences.LAST_WORKSPACE_MODE : ArecaPreferences.DEFAULT_WORKSPACE_MODE);
        ArecaPreferences.setDefaultWorkspace(defaultWorkspace.getText());
        ArecaPreferences.setDefaultArchiveStorage(defaultArchiveStorage.getText());
        ArecaPreferences.setDisplayReport(displayReport.getSelection());
        ArecaPreferences.setEditionCommand(editor.getText());
        ArecaPreferences.setInformationSynthetic(informationSynthetic.getSelection());
        ArecaPreferences.setDateFormat(dateFormat.getText());
        ArecaPreferences.setCheckNewVersion(checkNewVersions.getSelection());
        ArecaPreferences.setDisplayToolBar(showToolBar.getSelection());
        ArecaPreferences.setDisplayWSAddress(showWSPath.getSelection());
        
        this.hasBeenUpdated = false;
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
        btnSave.setEnabled(rulesSatisfied);
    }
}
