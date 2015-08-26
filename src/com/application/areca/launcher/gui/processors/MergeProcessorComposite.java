package com.application.areca.launcher.gui.processors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.application.areca.CheckParameters;
import com.application.areca.MergeParameters;
import com.application.areca.launcher.gui.ProcessorEditionWindow;
import com.application.areca.launcher.gui.processors.AbstractProcessorComposite;
import com.application.areca.processor.MergeProcessor;
import com.application.areca.processor.Processor;
import com.application.areca.processor.ProcessorValidationException;
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
public class MergeProcessorComposite extends AbstractProcessorComposite {

    private Text txtFromDelay;
    private Text txtToDelay;
    private Button btnKeepDeletedEntries;
    private Button chkCheckArchive;
    
    public MergeProcessorComposite(Composite composite, Processor proc, ProcessorEditionWindow window) {
        super(composite, proc, window);
        this.setLayout(new GridLayout(2, false));
        
        Label lblFrom = new Label(this, SWT.NONE);
        lblFrom.setText(RM.getLabel("procedition.delay.from.label"));
        
        txtFromDelay = new Text(this, SWT.BORDER);
        txtFromDelay.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        window.monitorControl(txtFromDelay);
        
        Label lblTo = new Label(this, SWT.NONE);
        lblTo.setText(RM.getLabel("procedition.delay.to.label"));
        
        txtToDelay = new Text(this, SWT.BORDER);
        txtToDelay.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        window.monitorControl(txtToDelay);
        
        btnKeepDeletedEntries = new Button(this, SWT.CHECK);
        btnKeepDeletedEntries.setText(RM.getLabel("archivedetail.keepdeletedentries.label"));
        btnKeepDeletedEntries.setToolTipText(RM.getLabel("archivedetail.keepdeletedentries.tt"));
        btnKeepDeletedEntries.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        window.monitorControl(btnKeepDeletedEntries);
        
        // Check
        chkCheckArchive = new Button(this, SWT.CHECK);
        chkCheckArchive.setSelection(false);
		chkCheckArchive.setText(RM.getLabel("archivedetail.checkmerged.label"));
		chkCheckArchive.setToolTipText(RM.getLabel("archivedetail.checkmerged.tt"));
        chkCheckArchive.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
        window.monitorControl(chkCheckArchive);
        
        if (proc != null) {
            MergeProcessor fProc = (MergeProcessor)proc;
            txtFromDelay.setText("" + fProc.getFromDelay());
            txtToDelay.setText("" + fProc.getToDelay());
            btnKeepDeletedEntries.setSelection(fProc.getParams().isKeepDeletedEntries());
            chkCheckArchive.setSelection(fProc.getCheckParams().isCheck());
        } else {
        	chkCheckArchive.setSelection(true);
        }
    }

    public void initProcessor(Processor proc) {
        MergeProcessor fProc = (MergeProcessor)proc;
        int from = 0;
        if (txtFromDelay.getText() != null && txtFromDelay.getText().trim().length() > 0) {
        	from = Integer.parseInt(txtFromDelay.getText());
        }
        int to = Integer.parseInt(txtToDelay.getText());
        
        if (from != 0 && from < to) {
        	int tmp = to;
        	to = from;
        	from = tmp;
        }
        fProc.setFromDelay(from);
        fProc.setToDelay(to);
        fProc.setParams(new MergeParameters(btnKeepDeletedEntries.getSelection(), false, null));
        fProc.setCheckParams(new CheckParameters(chkCheckArchive.getSelection(), true, true, false, null));
    }
    
    public boolean validateParams() {       
        // From
        window.resetErrorState(txtFromDelay);
        if (
                txtFromDelay.getText() != null
                && txtFromDelay.getText().trim().length() > 0
                && (! CommonRules.checkInteger(txtFromDelay.getText(), true))
        ) {
            window.setInError(txtFromDelay, RM.getLabel("error.numeric.value.expected"));
            return false;
        }
        
        // To
        window.resetErrorState(txtToDelay);
        if (
                ! CommonRules.checkInteger(txtToDelay.getText(), true)
        ) {
            window.setInError(txtToDelay, RM.getLabel("error.numeric.value.expected"));
            return false;
        }
        
        // Consistency
        MergeProcessor p = new MergeProcessor();
        initProcessor(p);
        try {
            p.validate();
        } catch (ProcessorValidationException e) {
            window.setInError(txtFromDelay, e.getMessage());
            window.setInError(txtToDelay, e.getMessage());
            return false;
        }

        return true;
    }
}
