package com.application.areca.launcher.gui;

import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.application.areca.ResourceManager;
import com.application.areca.impl.FileSystemRecoveryTarget;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.SavePanel;
import com.application.areca.launcher.gui.postprocessors.AbstractProcessorComposite;
import com.application.areca.processor.AbstractProcessor;
import com.application.areca.processor.Processor;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

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
public class ProcessorEditionWindow 
extends AbstractWindow {
    private static final ResourceManager RM = ResourceManager.instance();
    private static final String TITLE = RM.getLabel("procedition.dialog.title");
    
    protected Combo cboProcessorType;
    protected List procKeys;
    protected AbstractProcessorComposite pnlParams;
    protected Group pnlParamsContainer;
    
    protected boolean preprocess;
    
    protected Button radAlways;
    protected Button radFailure;
    protected Button radSuccess;
    
    protected AbstractProcessor proc;  
    protected FileSystemRecoveryTarget currentTarget;
    protected Button btnSave;

    public ProcessorEditionWindow(Processor proc, FileSystemRecoveryTarget currentTarget, boolean preprocess) {
        super();
        this.proc = (AbstractProcessor)proc;
        this.currentTarget = currentTarget;       
        this.preprocess = preprocess;
        procKeys = ProcessorRepository.getProcessors(preprocess);
    }

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.verticalSpacing = 10;
        composite.setLayout(layout);
        
        // TYPE
        Label lblProcType = new Label(composite, SWT.NONE);
        lblProcType.setText(RM.getLabel("procedition.type.label"));
        cboProcessorType = new Combo(composite, SWT.READ_ONLY);
        Iterator iter = procKeys.iterator();
        while (iter.hasNext()) {
            cboProcessorType.add(ProcessorRepository.getName((String)iter.next()));
        }
        GridData dt1 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        dt1.widthHint = computeWidth(400);
        cboProcessorType.setLayoutData(dt1);
        cboProcessorType.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }
            public void widgetSelected(SelectionEvent e) {
                refreshParamPnl();
                registerUpdate();
            }
        });
        cboProcessorType.select(ProcessorRepository.getIndex(proc, procKeys));  
        if (this.proc != null) {
            this.cboProcessorType.setEnabled(false);
        }
        
        if (! preprocess) {
            Group pnlRunContainer = new Group(composite, SWT.NONE);
            pnlRunContainer.setText(RM.getLabel("procedition.run.label"));
            pnlRunContainer.setLayout(new GridLayout(1, false));
            pnlRunContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        	
            radAlways = new Button(pnlRunContainer, SWT.RADIO);
            radAlways.setText(RM.getLabel("procedition.run.always.label"));
            radAlways.setToolTipText(RM.getLabel("procedition.run.always.tt"));
            this.monitorControl(radAlways);
            
            radSuccess = new Button(pnlRunContainer, SWT.RADIO);
            radSuccess.setText(RM.getLabel("procedition.run.success.label"));
            radSuccess.setToolTipText(RM.getLabel("procedition.run.success.tt"));
            this.monitorControl(radSuccess);
            
            radFailure = new Button(pnlRunContainer, SWT.RADIO);
            radFailure.setText(RM.getLabel("procedition.run.failure.label"));
            radFailure.setToolTipText(RM.getLabel("procedition.run.failure.tt"));
            this.monitorControl(radFailure);
            
            if (proc != null) {
            	if (proc.getRunScheme() == Processor.RUN_SCHEME_ALWAYS) {
            		radAlways.setSelection(true);
            	} else if (proc.getRunScheme() == Processor.RUN_SCHEME_SUCCESS) {
            		radSuccess.setSelection(true);
            	} else {
            		radFailure.setSelection(true);
            	}
            } else {
        		radAlways.setSelection(true);
            }
        }
        
        // CONTAINER
        pnlParamsContainer = new Group(composite, SWT.NONE);
        pnlParamsContainer.setText(RM.getLabel("procedition.params.label"));
        GridLayout lt = new GridLayout();
        pnlParamsContainer.setLayout(lt);
        pnlParamsContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        buildParamPnl();
        
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

    public Processor getCurrentProcessor() {
        return this.proc;
    }

    protected boolean checkBusinessRules() {
        boolean result = this.pnlParams == null ||  this.pnlParams.validateParams();   
        return result;
    }

    protected void saveChanges() {
        if (this.proc == null) {
            this.proc = (AbstractProcessor)ProcessorRepository.buildProcessor(this.cboProcessorType.getSelectionIndex(), this.procKeys, this.currentTarget);
        }
        this.pnlParams.initProcessor(proc);

        if (! this.preprocess) {
        	if (radAlways.getSelection()) {
        		this.proc.setRunScheme(Processor.RUN_SCHEME_ALWAYS);
        	} else if (radSuccess.getSelection()) {
        		this.proc.setRunScheme(Processor.RUN_SCHEME_SUCCESS);
        	} else if (radFailure.getSelection()) {
        		this.proc.setRunScheme(Processor.RUN_SCHEME_FAILURE);
        	}
        }
        
        this.hasBeenUpdated = false;
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
        btnSave.setEnabled(rulesSatisfied);
    }
    
    private void buildParamPnl() {
        this.pnlParams = ProcessorRepository.buildProcessorComposite(
                this.cboProcessorType.getSelectionIndex(), 
                this.procKeys, 
                this.pnlParamsContainer, 
                proc, 
                this);
        
        GridData dt = new GridData(SWT.FILL, SWT.FILL, true, true);
        this.pnlParams.setLayoutData(dt);
    }
    
    private void refreshParamPnl() {      
        if (pnlParams != null) {
            this.pnlParams.dispose();
            this.getShell().pack(true);
        }

        buildParamPnl();
        this.getShell().pack(true);
    }
}
