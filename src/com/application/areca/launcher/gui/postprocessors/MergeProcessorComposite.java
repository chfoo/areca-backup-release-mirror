package com.application.areca.launcher.gui.postprocessors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.application.areca.launcher.gui.ProcessorEditionWindow;
import com.application.areca.postprocess.MergePostProcessor;
import com.application.areca.postprocess.PostProcessor;
import com.myJava.util.CommonRules;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -6307890396762748969
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
public class MergeProcessorComposite extends AbstractProcessorComposite {

    private Text txtDelay;
    
    public MergeProcessorComposite(Composite composite, PostProcessor proc, ProcessorEditionWindow window) {
        super(composite, proc, window);
        this.setLayout(new GridLayout(2, false));
        
        Label lbl = new Label(this, SWT.NONE);
        lbl.setText(RM.getLabel("procedition.delay.label"));
        
        txtDelay = new Text(this, SWT.BORDER);
        GridData dt = new GridData(SWT.FILL, SWT.TOP, true, false);
        txtDelay.setLayoutData(dt);
        window.monitorControl(txtDelay);
        
        if (proc != null) {
            MergePostProcessor fProc = (MergePostProcessor)proc;
            txtDelay.setText("" + fProc.getDelay());
        }
    }

    public void initProcessor(PostProcessor proc) {
        MergePostProcessor fProc = (MergePostProcessor)proc;
        fProc.setDelay(Integer.parseInt(txtDelay.getText()));
    }
    
    public boolean validateParams() {
        window.resetErrorState(txtDelay);
        
        if (txtDelay.getText() == null || txtDelay.getText().trim().length() == 0 || ! CommonRules.checkInteger(txtDelay.getText())) {
            window.setInError(txtDelay);
            return false;
        }

        return true;
    }
}
