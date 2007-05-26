package com.application.areca.launcher.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.application.areca.ArchiveFilter;
import com.application.areca.impl.FileSystemRecoveryTarget;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ResourceManager;
import com.application.areca.launcher.gui.common.SavePanel;
import com.myJava.file.FileSystemManager;
import com.myJava.util.os.OSTool;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4945525256658487980
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
public class FilterEditionWindow 
extends AbstractWindow {
    private static final ResourceManager RM = ResourceManager.instance();
    private static final String TITLE = RM.getLabel("filteredition.dialog.title");
    
    private static final String EXAMPLE_DIR_WIN = RM.getLabel("filteredition.exampledirwin.label");
    private static final String EXAMPLE_DIR_LINUX = RM.getLabel("filteredition.exampledirlinux.label");
    private static final String EXAMPLE_FILEEXTENSION = RM.getLabel("filteredition.examplefileext.label");    
    private static final String EXAMPLE_REGEX = RM.getLabel("filteredition.exampleregex.label");    
    private static final String EXAMPLE_DATE = RM.getLabel("filteredition.exampledate.label");    
    private static final String EXAMPLE_SIZE = RM.getLabel("filteredition.examplesize.label");
    
    protected Combo cboFilterType;
    protected Text txtFilterParameters;
    protected Button chkExclude;
    protected Label lblExample;
    protected Label lblFilterParameters;
    protected Button btnSave;
    
    protected ArchiveFilter currentFilter;  
    protected FileSystemRecoveryTarget currentTarget;
    
    public FilterEditionWindow(ArchiveFilter currentFilter, FileSystemRecoveryTarget currentTarget) {
        super();
        this.currentFilter = currentFilter;
        this.currentTarget = currentTarget;
    }

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        
        // TYPE
        Label lblFilterType = new Label(composite, SWT.NONE);
        lblFilterType.setText(RM.getLabel("filteredition.filtertypefield.label"));
        cboFilterType = new Combo(composite, SWT.READ_ONLY);
        cboFilterType.add(RM.getLabel("filteredition.fileextensionfilter.label"));
        cboFilterType.add(RM.getLabel("filteredition.regexfilter.label"));
        cboFilterType.add(RM.getLabel("filteredition.directoryfilter.label"));
        cboFilterType.add(RM.getLabel("filteredition.filesizefilter.label"));
        cboFilterType.add(RM.getLabel("filteredition.filedatefilter.label"));  
        cboFilterType.add(RM.getLabel("filteredition.linkfilter.label"));       
        cboFilterType.add(RM.getLabel("filteredition.lockedfilefilter.label"));    
        cboFilterType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        monitorControl(cboFilterType);
        
        // EXCLUDE
        new Label(composite, SWT.NONE);
        chkExclude = new Button(composite, SWT.CHECK);
        chkExclude.setText(RM.getLabel("filteredition.exclusionfilterfield.label"));
        chkExclude.setToolTipText(RM.getLabel("filteredition.exclusionfilterfield.tooltip"));
        monitorControl(chkExclude);
        
        // SEPARATOR
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        
        // PARAMS
        lblFilterParameters = new Label(composite, SWT.NONE);
        lblFilterParameters.setText(RM.getLabel("filteredition.parametersfield.label"));
        txtFilterParameters = new Text(composite, SWT.BORDER);
        txtFilterParameters.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        monitorControl(txtFilterParameters);
        
        // EXAMPLE
        new Label(composite, SWT.NONE);
        lblExample = new Label(composite, SWT.NONE);
        lblExample.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // INIT
        if (this.currentFilter != null) {
            txtFilterParameters.setText(currentFilter.getStringParameters() == null ? "" : currentFilter.getStringParameters());
            this.cboFilterType.setEnabled(false);
            cboFilterType.select(FilterRepository.getIndex(currentFilter));   
            chkExclude.setSelection(currentFilter.isExclude());
        } else {
            cboFilterType.select(0);
            chkExclude.setSelection(true);
        }
        
        // SAVE
        SavePanel sv = new SavePanel(this);
        Composite pnlSave = sv.buildComposite(composite);
        btnSave = sv.getBtnSave();
        pnlSave.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, false, 2, 1));
        
        composite.pack();
        return composite;
    }

    public String getTitle() {
        return TITLE;
    }
    
    public ArchiveFilter getCurrentFilter() {
        return currentFilter;
    }

    protected boolean checkBusinessRules() {
        updateExample();
        this.resetErrorState(txtFilterParameters);    
        boolean result = FilterRepository.checkParameters(txtFilterParameters.getText(), this.cboFilterType.getSelectionIndex());
        if (! result) {
            this.setInError(txtFilterParameters);
        }
        return result;
    }

    protected void saveChanges() {
        if (this.currentFilter == null) {
            this.currentFilter = FilterRepository.buildFilter(this.cboFilterType.getSelectionIndex());
        }
         
        this.currentFilter.setExclude(this.chkExclude.getSelection());
        this.currentFilter.acceptParameters(this.txtFilterParameters.getText());
        
        this.hasBeenUpdated = false;
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
        btnSave.setEnabled(rulesSatisfied);
    }
    
    protected void updateExample() {
        if (this.cboFilterType.getSelectionIndex() == 0) {
            // FICHIER
            this.lblExample.setText(EXAMPLE_FILEEXTENSION);
        } else if (this.cboFilterType.getSelectionIndex() == 1) {
            // REGEX
            this.lblExample.setText(EXAMPLE_REGEX);
        } else if (this.cboFilterType.getSelectionIndex() == 2) {
            // REPERTOIRE
            if (OSTool.isSystemWindows()) {
                this.lblExample.setText(EXAMPLE_DIR_WIN);
            } else {
                this.lblExample.setText(EXAMPLE_DIR_LINUX);                
            }
            
            if (
                    this.currentTarget != null
                    && currentTarget.getSourcePath() != null
                    && (this.txtFilterParameters.getText() == null || this.txtFilterParameters.getText().length() == 0)
            ) {
                this.txtFilterParameters.setText(FileSystemManager.getAbsolutePath(currentTarget.getSourcePath()));
            }      
        } else if (this.cboFilterType.getSelectionIndex() == 3) {
            // SIZE
            this.lblExample.setText(EXAMPLE_SIZE);
        } else if (this.cboFilterType.getSelectionIndex() == 4) {   
            // DATE
            this.lblExample.setText(EXAMPLE_DATE);
        } else {
            this.lblExample.setText("");
        }
        
        boolean paramEnabled = FilterRepository.buildFilter(this.cboFilterType.getSelectionIndex()).requiresParameters();
        this.lblFilterParameters.setEnabled(paramEnabled);
        this.txtFilterParameters.setEnabled(paramEnabled);
    }
}
