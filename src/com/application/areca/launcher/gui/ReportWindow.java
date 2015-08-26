package com.application.areca.launcher.gui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import com.application.areca.context.ProcessReport;
import com.application.areca.context.ProcessReportWriter;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.SavePanel;
import com.myJava.util.log.Logger;

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
public class ReportWindow 
extends AbstractWindow {

	protected Text txtContent;
	protected ProcessReport report;

	public ReportWindow(ProcessReport report) {
		super();
		this.report = report;
	}

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		txtContent = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        GridData dt = new GridData(SWT.FILL, SWT.FILL, true, true);
        dt.widthHint = computeWidth(500);
        dt.heightHint = computeHeight(200);
		txtContent.setLayoutData(dt);
		txtContent.setEditable(false);

		ProcessReportWriter writer = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			writer = new ProcessReportWriter(new OutputStreamWriter(baos), false, false, -1);
			writer.writeReport(report);
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				Logger.defaultLogger().error(e);
			}            
		}
		txtContent.setText(baos.toString());

        SavePanel pnlSave = new SavePanel(RM.getLabel("common.close.label"), this);
        pnlSave.setShowCancel(false);
        pnlSave.buildComposite(composite).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        
		composite.pack();
		
		return composite;
	}

	public String getTitle() {
		return RM.getLabel("report.dialog.title");
	}

	protected boolean checkBusinessRules() {
		return true;
	}

	protected void saveChanges() {
		this.cancelChanges();
	}

	protected void updateState(boolean rulesSatisfied) {
	}
}
