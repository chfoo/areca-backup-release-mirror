package com.application.areca.launcher.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.application.areca.ResourceManager;
import com.application.areca.impl.policy.FTPFileSystemPolicy;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.SecuredRunner;
import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.util.Utilitaire;
import com.myJava.util.log.Logger;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 3274863990151426915
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
public class FTPEditionWindow 
extends AbstractWindow {
    
    private static final ResourceManager RM = ResourceManager.instance();
    private static final String TITLE = RM.getLabel("ftpedition.dialog.title");
    private static String[] PROTOCOLS;
    
    static {
        String[] protocols = FrameworkConfiguration.getInstance().getSSEProtocols();
        PROTOCOLS = new String[protocols.length + 1];
        PROTOCOLS[0] = "";
        for (int i=0; i<protocols.length; i++) {
            PROTOCOLS[i+1] = protocols[i];
        }
    }
    
    private FTPFileSystemPolicy currentPolicy;  
    private Thread currentRunningTest = null;
    
    protected Text txtHost;
    protected Text txtPort;
    protected Button chkPassiv;
    protected Combo cboProtocol;
    protected Text txtLogin;
    protected Text txtPassword;
    protected Text txtRemoteDir;
    protected Button chkImplicit;
    protected Button btnTest;
    protected Button btnSave;
    protected Button btnCancel;
    
    public FTPEditionWindow(FTPFileSystemPolicy currentPolicy) {
        super();
        this.currentPolicy = currentPolicy;
    }

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(4, false));
        
        Label lblHost = new Label(composite, SWT.NONE);
        lblHost.setText(RM.getLabel("ftpedition.host.label"));
        txtHost = new Text(composite, SWT.BORDER);
        txtHost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        monitorControl(txtHost);
        
        Label lblPort = new Label(composite, SWT.NONE);
        lblPort.setText(RM.getLabel("ftpedition.port.label"));
        txtPort = new Text(composite, SWT.BORDER);
        GridData dt = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        dt.widthHint = 100;
        txtPort.setLayoutData(dt);
        monitorControl(txtPort);
        
        chkPassiv = new Button(composite, SWT.CHECK);
        chkPassiv.setText(RM.getLabel("ftpedition.passiv.label"));
        chkPassiv.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        monitorControl(chkPassiv);
        
        Label lblFTPs = new Label(composite, SWT.NONE);
        lblFTPs.setText(RM.getLabel("ftpedition.secured.label"));
        cboProtocol = new Combo(composite, SWT.READ_ONLY);
        cboProtocol.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        for (int i=0; i<PROTOCOLS.length; i++) {
            cboProtocol.add(PROTOCOLS[i]);
        }
        monitorControl(cboProtocol);
        
        chkImplicit = new Button(composite, SWT.CHECK);
        chkImplicit.setText(RM.getLabel("ftpedition.implicit.label"));
        chkImplicit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        monitorControl(chkImplicit);
        
        Label lblLogin = new Label(composite, SWT.NONE);
        lblLogin.setText(RM.getLabel("ftpedition.login.label"));
        txtLogin = new Text(composite, SWT.BORDER);
        txtLogin.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        monitorControl(txtLogin);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        
        Label lblPassword = new Label(composite, SWT.NONE);
        lblPassword.setText(RM.getLabel("ftpedition.password.label"));
        txtPassword = new Text(composite, SWT.BORDER);
        txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        monitorControl(txtPassword);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        
        Label lblRemoteDir = new Label(composite, SWT.NONE);
        lblRemoteDir.setText(RM.getLabel("ftpedition.dir.label"));
        txtRemoteDir = new Text(composite, SWT.BORDER);
        txtRemoteDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        monitorControl(txtRemoteDir);
        
        btnTest = new Button(composite, SWT.PUSH);
        btnTest.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        btnTest.setText(RM.getLabel("ftpedition.test.label"));
        btnTest.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                final FTPFileSystemPolicy policy = new FTPFileSystemPolicy();
                initPolicy(policy);
                
                Runnable rn = new Runnable() {
                    public void run() {
                        testFTP(policy);    
                    }
                };
                Thread th = new Thread(rn, "FTP Test #" + Utilitaire.getRndLong());
                th.setDaemon(true);
                registerCurrentRunningTest(th);
                th.start();
            }
        });
        
        btnSave = new Button(composite, SWT.PUSH);
        btnSave.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        btnSave.setText(RM.getLabel("common.save.label"));
        btnSave.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                registerCurrentRunningTest(null);
                saveChanges();
            }
        });
        
        btnCancel = new Button(composite, SWT.PUSH);
        btnCancel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        btnCancel.setText(RM.getLabel("common.cancel.label"));
        btnCancel.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                registerCurrentRunningTest(null);
                hasBeenUpdated = false;
                close();
            }
        });
        
        // INIT
        if (this.currentPolicy != null) {            
            this.txtHost.setText(currentPolicy.getRemoteServer());
            this.txtPort.setText("" + currentPolicy.getRemotePort());
            this.chkPassiv.setSelection(currentPolicy.isPassivMode());
            this.chkImplicit.setSelection(currentPolicy.isImplicit());
            
            int index = -1;
            for (int i=0; i<PROTOCOLS.length; i++) {
                if (PROTOCOLS[i].equals(currentPolicy.getProtocol())) {
                    index = i;
                    break;
                }
            }
            this.cboProtocol.select(index);
            this.txtLogin.setText(currentPolicy.getLogin());
            this.txtPassword.setText(currentPolicy.getPassword());
            this.txtRemoteDir.setText(currentPolicy.getRemoteDirectory());
        } else {
            this.txtPort.setText("" + FTPFileSystemPolicy.DEFAULT_PORT);
        }
        
        composite.pack();
        return composite;
    }
    
    public String getTitle() {
        return TITLE;
    }
    
    private void registerCurrentRunningTest(Thread th) {
        try {
            if (this.currentRunningTest != null) {
                this.currentRunningTest.interrupt();
            }
        } catch (Exception e) {
            Logger.defaultLogger().error(e);
        }
        this.currentRunningTest = th;
    }
    
    protected void testFTP(FTPFileSystemPolicy policy) {
        SecuredRunner.execute(new Runnable() {
            public void run() {
                btnTest.setEnabled(false);
            }
        });

        try {
            policy.validate(true);
            SecuredRunner.execute(new Runnable() {
                public void run() {
                    application.showInformationDialog(RM.getLabel("ftpedition.test.success"), RM.getLabel("ftpedition.test.title"));
                }
            });
        } catch (final Throwable e) {
            SecuredRunner.execute(new Runnable() {
                public void run() {
                    application.showWarningDialog(RM.getLabel("ftpedition.test.failure", new Object[] {e.getMessage()}), RM.getLabel("ftpedition.test.title"));
                }
            });
        } finally {
            SecuredRunner.execute(new Runnable() {
                public void run() {
                    btnTest.setEnabled(true);
                }
            });
        }
    }

    protected boolean checkBusinessRules() {
        boolean result = true;
        
        result = check(txtHost, result);
        result = check(txtLogin, result);
        result = check(txtPassword, result);
        result = check(txtPort, result);
        result = check(txtRemoteDir, result);
        
        try {
            Integer.parseInt(txtPort.getText());
        } catch (Throwable e) {
            this.setInError(txtPort);
            result = false;
        }
        
        return result;
    }

    private boolean check(Text fld, boolean b) {
        this.resetErrorState(fld);
        if (fld.getText() == null || fld.getText().trim().length() == 0) {
            this.setInError(fld);
            return false;
        } else {
            return b;
        }
    }
    
    protected void saveChanges() {
        if (this.currentPolicy == null) {
            this.currentPolicy = new FTPFileSystemPolicy();
        }
        initPolicy(this.currentPolicy);
        
        this.hasBeenUpdated = false;
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
        btnSave.setEnabled(rulesSatisfied);
        btnTest.setEnabled(rulesSatisfied);
    }
    
    protected void initPolicy(FTPFileSystemPolicy policy) {
        policy.setLogin(txtLogin.getText());
        policy.setPassivMode(chkPassiv.getSelection());
        policy.setImplicit(chkImplicit.getSelection());

        if (cboProtocol.getSelectionIndex() != -1) {
            String protocol = (String)cboProtocol.getItem(cboProtocol.getSelectionIndex());
            policy.setProtocol(protocol);
        } else {
            policy.setProtocol(null);
        }
        policy.setPassword(txtPassword.getText());
        policy.setRemoteDirectory(txtRemoteDir.getText());
        policy.setRemotePort(Integer.parseInt(txtPort.getText()));
        policy.setRemoteServer(txtHost.getText());
    }

    public FTPFileSystemPolicy getCurrentPolicy() {
        return currentPolicy;
    }
}
