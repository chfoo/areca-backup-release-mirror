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

import com.application.areca.impl.FileSystemTarget;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.SavePanel;
import com.application.areca.launcher.gui.processors.AbstractProcessorComposite;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.application.areca.processor.AbstractProcessor;
import com.application.areca.processor.Processor;

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
public class ProcessorEditionWindow 
extends AbstractWindow {
    private static final ResourceManager RM = ResourceManager.instance();
    private static final String TITLE = RM.getLabel("procedition.dialog.title");
    
    protected Combo cboProcessorType;
    protected List procKeys;
    protected AbstractProcessorComposite pnlParams;
    protected Group pnlParamsContainer;
    
    protected boolean preprocess;
    
    protected Button chkError;
    protected Button chkWarning;
    protected Button chkOK;
    
    protected Button chkBackup;
    protected Button chkMerge;
    protected Button chkCheck;

    protected AbstractProcessor proc;  
    protected FileSystemTarget currentTarget;
    protected Button btnSave;

    public ProcessorEditionWindow(Processor proc, FileSystemTarget currentTarget, boolean preprocess) {
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
        
        Group pnlRunContainer = new Group(composite, SWT.NONE);
        pnlRunContainer.setText(RM.getLabel("procedition.run.label"));
        pnlRunContainer.setLayout(new GridLayout(1, false));
        pnlRunContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    	
        chkOK = new Button(pnlRunContainer, SWT.CHECK);
        chkOK.setText(RM.getLabel("procedition.run.ok.label"));
        chkOK.setToolTipText(RM.getLabel("procedition.run.ok.tt"));
        this.monitorControl(chkOK);
        
        chkWarning = new Button(pnlRunContainer, SWT.CHECK);
        chkWarning.setText(RM.getLabel("procedition.run.warning.label"));
        chkWarning.setToolTipText(RM.getLabel("procedition.run.warning.tt"));
        this.monitorControl(chkWarning);
        
        chkError = new Button(pnlRunContainer, SWT.CHECK);
        chkError.setText(RM.getLabel("procedition.run.error.label"));
        chkError.setToolTipText(RM.getLabel("procedition.run.error.tt"));
        this.monitorControl(chkError);
        
        Group pnlActionContainer = new Group(composite, SWT.NONE);
        pnlActionContainer.setText(RM.getLabel("procedition.context.label"));
        pnlActionContainer.setLayout(new GridLayout(1, false));
        pnlActionContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    	
        chkBackup = new Button(pnlActionContainer, SWT.CHECK);
        chkBackup.setText(RM.getLabel("procedition.run.backup.label"));
        chkBackup.setToolTipText(RM.getLabel("procedition.run.backup.tt"));
        this.monitorControl(chkBackup);
        
        chkMerge = new Button(pnlActionContainer, SWT.CHECK);
        chkMerge.setText(RM.getLabel("procedition.run.merge.label"));
        chkMerge.setToolTipText(RM.getLabel("procedition.run.merge.tt"));
        this.monitorControl(chkMerge);
        
        chkCheck = new Button(pnlActionContainer, SWT.CHECK);
        chkCheck.setText(RM.getLabel("procedition.run.check.label"));
        chkCheck.setToolTipText(RM.getLabel("procedition.run.check.tt"));
        this.monitorControl(chkCheck);
        
        if (proc != null) {
        	chkOK.setSelection(proc.isRunIfOK());
        	chkWarning.setSelection(proc.isRunIfWarning());
        	chkError.setSelection(proc.isRunIfError());
        	
        	chkBackup.setSelection(proc.isRunBackup());
        	chkMerge.setSelection(proc.isRunMerge());
        	chkCheck.setSelection(proc.isRunCheck());
        } else {
        	chkOK.setSelection(true);
        	chkWarning.setSelection(true);
        	chkError.setSelection(true);
        	
        	chkBackup.setSelection(true);
        	chkMerge.setSelection(false);
        	chkCheck.setSelection(false);
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

    	this.proc.setRunIfError(chkError.getSelection());
    	this.proc.setRunIfWarning(chkWarning.getSelection());
    	this.proc.setRunIfOK(chkOK.getSelection());
    	
    	this.proc.setRunBackup(chkBackup.getSelection());
    	this.proc.setRunMerge(chkMerge.getSelection());
    	this.proc.setRunCheck(chkCheck.getSelection());
        
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
