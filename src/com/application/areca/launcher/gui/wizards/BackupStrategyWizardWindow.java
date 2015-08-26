package com.application.areca.launcher.gui.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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

import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.SavePanel;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.myJava.file.FileSystemManager;
import com.myJava.util.CommonRules;

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
public class BackupStrategyWizardWindow 
extends AbstractWindow {
    private static final ResourceManager RM = ResourceManager.instance();
    private static final int ITERATIONS = 6;
    
    private String initialDirectory;
    
    private String selectedPath = null;
    private boolean refresh = true;
    
    private Text location;
    private Button saveButton;
    private List times = new ArrayList();
    private List lTxtDelays = new ArrayList();
    private List lTxtTimes = new ArrayList();
    

    public BackupStrategyWizardWindow(String initialDirectory) {
        super();
        this.initialDirectory = initialDirectory;
    }

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        composite.setLayout(layout);
               
        Group grpLocation = new Group(composite, SWT.NONE);
        grpLocation.setText(RM.getLabel("shrtc.location.label"));
        GridLayout grpLayout = new GridLayout(2, false);
        grpLocation.setLayout(grpLayout);
        grpLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        location = new Text(grpLocation, SWT.BORDER);
        GridData mainData2 = new GridData();
        mainData2.grabExcessHorizontalSpace = true;
        mainData2.horizontalAlignment = SWT.FILL;
        mainData2.widthHint = AbstractWindow.computeWidth(400);
        location.setLayoutData(mainData2);
        location.setText(FileSystemManager.getAbsolutePath(new File(initialDirectory)));
        monitorControl(location);
        
        Button btnBrowse = new Button(grpLocation, SWT.PUSH);
        btnBrowse.setText(RM.getLabel("common.browseaction.label"));
        btnBrowse.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                File f = new File(location.getText());
                String path = application.showDirectoryDialog(FileSystemManager.getParent(f), BackupStrategyWizardWindow.this);
                if (path != null) {
                    location.setText(path);
                }
            }
        });
        GridData mainData3 = new GridData();
        mainData3.horizontalAlignment = SWT.FILL;
        btnBrowse.setLayoutData(mainData3);

        Group grpParams = new Group(composite, SWT.NONE);
        grpParams.setText(RM.getLabel("shrtc.params.label"));
        grpParams.setLayout(new GridLayout(5, false));
        grpParams.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label lblIntro = new Label(grpParams, SWT.NONE);
        lblIntro.setText(RM.getLabel("shrtc.intro.label"));
        lblIntro.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1));

        for (int i=0; i<ITERATIONS; i++) {
            Label lblEach = new Label(grpParams, SWT.NONE);
            lblEach.setText(i == 0 ? RM.getLabel("shrtc.lbl1.first.label") : RM.getLabel("shrtc.lbl1.label"));
            
            final Text txtDelay = new Text(grpParams, SWT.BORDER);
            txtDelay.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            txtDelay.setEnabled(false);
            if (i == 0) {
                txtDelay.setText("1");
            }
            lTxtDelays.add(txtDelay);
            
            Label lblDays = new Label(grpParams, SWT.NONE);
            lblDays.setText(RM.getLabel("shrtc.lbl2.label"));

            Text txtTimes = new Text(grpParams, SWT.BORDER);
            txtTimes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            monitorControl(txtTimes);
            txtTimes.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    if (refresh) {
                        refresh = false;
                        resetValues();
                        refresh = true;
                    }
                }
            });
            lTxtTimes.add(txtTimes);
            
            Label lblTimes = new Label(grpParams, SWT.NONE);
            lblTimes.setText(RM.getLabel("shrtc.lbl3.label"));
        }

        SavePanel pnlSave = new SavePanel(this);
        GridData saveData = new GridData();
        saveData.grabExcessHorizontalSpace = true;
        saveData.horizontalAlignment = SWT.FILL;
        pnlSave.buildComposite(composite).setLayoutData(saveData);        
        saveButton = pnlSave.getBtnSave();
        
        composite.pack();
        return composite;
    }

    protected void resetValues() {
        Iterator iDelays = lTxtDelays.iterator();
        Iterator iTimes = lTxtTimes.iterator();

        long multiplier = 1;
        boolean reset = false;
        boolean first = true;
        while (iDelays.hasNext()) {
            Text txtDelay = (Text)iDelays.next();
            Text txtTimes = (Text)iTimes.next();
            
            if (reset) {
                txtDelay.setText("");
                txtTimes.setText("");
            } else {
                txtDelay.setText("" + multiplier);
                if (! CommonRules.checkInteger(txtTimes.getText(), true)) {
                    reset = true;
                    txtTimes.setText("");
                } else {
                    multiplier *= (Integer.parseInt(txtTimes.getText()) + (first? 0 : 1));
                }
            }
            first = false;
        }
    }
    
    public String getTitle() {
        return RM.getLabel("app.strategy.label");
    }

    protected boolean checkBusinessRules() {
        this.resetErrorState(location);     
        if (this.location.getText() == null || this.location.getText().length() == 0) {
            this.setInError(location, RM.getLabel("error.field.mandatory"));
            return false;
        }
        return true;
    }

    protected void saveChanges() {       
        Iterator iTimes = lTxtTimes.iterator();
        for (int i=0; iTimes.hasNext(); i++) {
            Text txtTimes = (Text)iTimes.next();
            if (CommonRules.checkInteger(txtTimes.getText(), true)) {
                this.times.add(new Integer(txtTimes.getText()));
            }
        }
        
        this.selectedPath = location.getText();        
        this.hasBeenUpdated = false;
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
        this.saveButton.setEnabled(rulesSatisfied);
    }

    public String getSelectedPath() {
        return selectedPath;
    }

    public List getTimes() {
        return times;
    }
}
