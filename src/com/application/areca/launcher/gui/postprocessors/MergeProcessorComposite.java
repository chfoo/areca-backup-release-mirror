package com.application.areca.launcher.gui.postprocessors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.application.areca.launcher.gui.ProcessorEditionWindow;
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
public class MergeProcessorComposite extends AbstractProcessorComposite {

    private Text txtFromDelay;
    private Text txtToDelay;
    private Button btnKeepDeletedEntries;
    
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
        
        if (proc != null) {
            MergeProcessor fProc = (MergeProcessor)proc;
            txtFromDelay.setText("" + fProc.getFromDelay());
            txtToDelay.setText("" + fProc.getToDelay());
            btnKeepDeletedEntries.setSelection(fProc.isKeepDeletedEntries());
        }
    }

    public void initProcessor(Processor proc) {
        MergeProcessor fProc = (MergeProcessor)proc;
        if (txtFromDelay.getText() != null && txtFromDelay.getText().trim().length() > 0) {
            fProc.setFromDelay(Integer.parseInt(txtFromDelay.getText()));
        }
        fProc.setToDelay(Integer.parseInt(txtToDelay.getText()));
        fProc.setKeepDeletedEntries(btnKeepDeletedEntries.getSelection());
    }
    
    public boolean validateParams() {       
        // From
        window.resetErrorState(txtFromDelay);
        if (
                txtFromDelay.getText() != null
                && txtFromDelay.getText().trim().length() > 0
                && (! CommonRules.checkInteger(txtFromDelay.getText(), true))
        ) {
            window.setInError(txtFromDelay);
            return false;
        }
        
        // To
        window.resetErrorState(txtToDelay);
        if (
                ! CommonRules.checkInteger(txtToDelay.getText(), true)
        ) {
            window.setInError(txtToDelay);
            return false;
        }
        
        // Consistency
        MergeProcessor p = new MergeProcessor();
        initProcessor(p);
        try {
            p.validate();
        } catch (ProcessorValidationException e) {
            window.setInError(txtFromDelay);
            window.setInError(txtToDelay);
            return false;
        }

        return true;
    }
}
