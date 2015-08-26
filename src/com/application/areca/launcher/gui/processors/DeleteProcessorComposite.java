package com.application.areca.launcher.gui.processors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.application.areca.launcher.gui.ProcessorEditionWindow;
import com.application.areca.launcher.gui.processors.AbstractProcessorComposite;
import com.application.areca.processor.DeleteProcessor;
import com.application.areca.processor.Processor;
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
public class DeleteProcessorComposite extends AbstractProcessorComposite {

    private Text txtDelay;
    
    public DeleteProcessorComposite(Composite composite, Processor proc, ProcessorEditionWindow window) {
        super(composite, proc, window);
        this.setLayout(new GridLayout(2, false));
        
        Label lblDelay = new Label(this, SWT.NONE);
        lblDelay.setText(RM.getLabel("procedition.delay.label"));
        
        txtDelay = new Text(this, SWT.BORDER);
        txtDelay.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        window.monitorControl(txtDelay);
        
        if (proc != null) {
            DeleteProcessor fProc = (DeleteProcessor)proc;
            txtDelay.setText("" + fProc.getDelay());
        }
    }

    public void initProcessor(Processor proc) {
        DeleteProcessor fProc = (DeleteProcessor)proc;
        if (txtDelay.getText() != null && txtDelay.getText().trim().length() > 0) {
            fProc.setDelay(Integer.parseInt(txtDelay.getText()));
        }
    }
    
    public boolean validateParams() {       
        window.resetErrorState(txtDelay);
        if (
                txtDelay.getText() == null
                || txtDelay.getText().trim().length() == 0
                || (! CommonRules.checkInteger(txtDelay.getText(), true))
        ) {
            window.setInError(txtDelay, RM.getLabel("error.numeric.value.expected"));
            return false;
        }

        return true;
    }
}
