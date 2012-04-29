package com.application.areca.launcher.gui.processors;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.application.areca.ApplicationException;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.ProcessorEditionWindow;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.processor.AbstractMailSendProcessor;
import com.application.areca.processor.Processor;
import com.application.areca.processor.SendReportByMailProcessor;
import com.application.areca.version.VersionInfos;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2011, Olivier PETRUCCI.

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
public class SendMailProcessorComposite extends AbstractProcessorComposite {

    private Text txtRecipients;
    private Text txtSmtp;
    private Text txtPort;
    private Text txtUser;
    private Text txtPassword;
    private Text txtTitle;
    private Text txtMessage;
    private Text txtFrom;
    private Button btnTest;
    private Button btnSMTPS;
    private Button chkAppendStatistics;
    private boolean appendReport;
    
    public SendMailProcessorComposite(Composite composite, Processor proc, ProcessorEditionWindow window, boolean appendReport) {
        super(composite, proc, window);
        this.appendReport = appendReport;
        
        this.setLayout(new GridLayout(4, false));
        
        // Title
        Label lblTitle = new Label(this, SWT.NONE);
        lblTitle.setText(RM.getLabel("procedition.mailtitle.label"));
        
        txtTitle = new Text(this, SWT.BORDER);
        txtTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        window.monitorControl(txtTitle);
        
        // Intro
        Label lblMessage = new Label(this, SWT.NONE);
        if (appendReport) {
            lblMessage.setText(RM.getLabel("procedition.mailintro.label"));
        } else {
            lblMessage.setText(RM.getLabel("procedition.mailmessage.label"));
        }
        txtMessage = new Text(this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
        GridData dt = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
        dt.heightHint = AbstractWindow.computeHeight(50);
        dt.widthHint = AbstractWindow.computeWidth(200);
        txtMessage.setLayoutData(dt);
        window.monitorControl(txtMessage);
        
        // Example
        new Label(this, SWT.NONE);
        Label lblExample = new Label(this, SWT.NONE);
        lblExample.setText(RM.getLabel("procedition.dynparams.label"));
        lblExample.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        
        if (appendReport) {
	        // Append Statistics
	        new Label(this, SWT.NONE);
	        chkAppendStatistics = new Button(this, SWT.CHECK);
	        chkAppendStatistics.setText(RM.getLabel("procedition.appendstats.label"));
	        chkAppendStatistics.setToolTipText(RM.getLabel("procedition.appendstats.tt"));
	        chkAppendStatistics.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
	        window.monitorControl(chkAppendStatistics);
        }

        // Recipients
        Label lblRecipients = new Label(this, SWT.NONE);
        lblRecipients.setText(RM.getLabel("procedition.recipients.label"));
        
        txtRecipients = new Text(this, SWT.BORDER);
        txtRecipients.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        window.monitorControl(txtRecipients);
        
        // From
        Label lblFrom = new Label(this, SWT.NONE);
        lblFrom.setText(RM.getLabel("procedition.mailfrom.label"));
        
        txtFrom = new Text(this, SWT.BORDER);
        txtFrom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        window.monitorControl(txtFrom);
        
        // SMTP
        Label lblSmtp = new Label(this, SWT.NONE);
        lblSmtp.setText(RM.getLabel("procedition.smtp.label"));
        
        txtSmtp = new Text(this, SWT.BORDER);
        txtSmtp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        window.monitorControl(txtSmtp);
        
        Label lblPort = new Label(this, SWT.NONE);
        lblPort.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        lblPort.setText(RM.getLabel("procedition.port.label"));
        
        txtPort = new Text(this, SWT.BORDER);
        txtPort.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        window.monitorControl(txtPort);
        
        // SMTPS
        new Label(this, SWT.NONE);
        btnSMTPS = new Button(this, SWT.CHECK);
        btnSMTPS.setText(RM.getLabel("procedition.smtps.label"));
        btnSMTPS.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
        window.monitorControl(btnSMTPS);
        
        // User
        Label lblUser = new Label(this, SWT.NONE);
        lblUser.setText(RM.getLabel("procedition.user.label"));
        
        txtUser = new Text(this, SWT.BORDER);
        txtUser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        window.monitorControl(txtUser);
        
        // Password
        Label lblPassword = new Label(this, SWT.NONE);
        lblPassword.setText(RM.getLabel("procedition.password.label"));
        
        txtPassword = new Text(this, SWT.BORDER);
		txtPassword.setEchoChar('*');
        txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        window.monitorControl(txtPassword);
        
		final Button btnReveal = new Button(this, SWT.PUSH);
		btnReveal.setText(RM.getLabel("targetedition.reveal.label"));
		btnReveal.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		btnReveal.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (txtPassword.getEchoChar() == '*') {
					txtPassword.setEchoChar('\0');
					btnReveal.setText(RM.getLabel("targetedition.mask.label"));
					layout();
				} else {
					txtPassword.setEchoChar('*');
					btnReveal.setText(RM.getLabel("targetedition.reveal.label"));
					layout();
				}
			}
		});

        // Test
        new Label(this, SWT.NONE);
        btnTest = new Button(this, SWT.PUSH);
        btnTest.setText(RM.getLabel("procedition.smtp.test"));
        btnTest.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        btnTest.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                
                try {
                    SendReportByMailProcessor testProc = new SendReportByMailProcessor();
                    initProcessor(testProc);
                    testProc.sendMail("" + VersionInfos.APP_SHORT_NAME + " mail report test", "" + VersionInfos.APP_SHORT_NAME + " mail report test successfull !", ps, null);
                    
                    Application.getInstance().showInformationDialog(baos.toString(), RM.getLabel("procedition.ok.label"), true);                
                } catch (ApplicationException e1) {
                    Application.getInstance().showErrorDialog(e1.getMessage() + "\n\n" + baos.toString(), RM.getLabel("procedition.error.label"), true);
                }
            }
        });

        if (proc != null) {
            SendReportByMailProcessor mProc = (SendReportByMailProcessor)proc;
            txtRecipients.setText(mProc.getRecipients());
            txtSmtp.setText(mProc.getSmtpServerName());
            txtPort.setText("" + mProc.getSmtpServerPort());
            txtUser.setText(mProc.getUser());
            txtPassword.setText(mProc.getPassword());
            txtTitle.setText(mProc.getTitle());
            btnSMTPS.setSelection(mProc.isSmtps());
            if (mProc.getMessage() != null) {
                txtMessage.setText(mProc.getMessage());
            }
            if (mProc.getFrom() != null) {
                txtFrom.setText(mProc.getFrom());
            }
            if (appendReport) {
            	chkAppendStatistics.setSelection(mProc.isAppendStatistics());
            }
        }
    }

    public void initProcessor(Processor proc) {
        AbstractMailSendProcessor mProc = (AbstractMailSendProcessor)proc;
        mProc.setRecipients(txtRecipients.getText());
        
        String smtp = txtSmtp.getText();
        if ((! smtp.contains(":")) && txtPort.getText() != null && txtPort.getText().length() != 0) {
        	smtp += ":" + txtPort.getText();
        }
        mProc.setSmtpServer(smtp);
        mProc.setUser(txtUser.getText());
        mProc.setPassword(txtPassword.getText());
        mProc.setTitle(txtTitle.getText());
        mProc.setSmtps(btnSMTPS.getSelection());
        mProc.setMessage(txtMessage.getText());
        mProc.setFrom(txtFrom.getText());
        
        if (appendReport) {
            ((SendReportByMailProcessor)mProc).setAppendStatistics(chkAppendStatistics.getSelection());
        }
    }
    
    public boolean validateParams() {
        this.window.resetErrorState(txtRecipients);
        this.window.resetErrorState(txtSmtp);
        this.window.resetErrorState(txtTitle);
        
        if (txtTitle.getText() == null || txtTitle.getText().trim().length() == 0) {
            this.window.setInError(txtTitle, RM.getLabel("error.field.mandatory"));
            return false;
        }
        
        if (txtRecipients.getText() == null || txtRecipients.getText().trim().length() == 0) {
            this.window.setInError(txtRecipients, RM.getLabel("error.field.mandatory"));
            this.btnTest.setEnabled(false);
            return false;
        }
        
        if (txtSmtp.getText() == null || txtSmtp.getText().trim().length() == 0) {
            this.window.setInError(txtSmtp, RM.getLabel("error.field.mandatory"));
            this.btnTest.setEnabled(false);
            return false;
        }

        this.btnTest.setEnabled(true);
        return true;
    }
}
