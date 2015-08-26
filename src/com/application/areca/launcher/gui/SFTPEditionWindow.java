package com.application.areca.launcher.gui;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.application.areca.impl.policy.SFTPFileSystemPolicy;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ListPane;
import com.application.areca.launcher.gui.common.SecuredRunner;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyAccessor;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.myJava.file.FileSystemManager;
import com.myJava.util.Util;
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
public class SFTPEditionWindow 
extends AbstractWindow {

	private static final ResourceManager RM = ResourceManager.instance();
	private static final String TITLE = RM.getLabel("ftpedition.dialog.title");

	private SFTPFileSystemPolicy currentPolicy;  
	private Thread currentRunningTest = null;

	protected Group grpHostKey;
	protected Label lblFingerprint;

	protected Text txtHost;
	protected Text txtPort;

	protected Text txtLogin;
	protected Text txtPassword;
	protected Text txtRemoteDir;
	protected Text txtHostKey;

	protected Button btnTest;
	protected Button btnSave;
	protected Button btnCancel;
	protected Button btnHostKey;
	protected Button chkCheckHostKey;
	
	protected Button radPassword;
	protected Button radCertificate;
	
	protected Group grpPassword;
	protected Group grpCert;
	
	protected Label lblLogin;
	protected Label lblPrivateKey;
	protected Text txtPrivateKey;
	protected Button btnBrowse;
	protected Text txtPassphrase;
	protected Text lblCertHint;
	protected Button chkEncrypedtCert;

	protected Button btnReveal;
	protected Button btnReveal2;

	public SFTPEditionWindow(SFTPFileSystemPolicy currentPolicy) {
		super();
		this.currentPolicy = currentPolicy;
	}

	protected Control createContents(Composite parent) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout(1, false));

		ListPane tabs = new ListPane(ret, SWT.NONE, false);
		GridData dt = new GridData(SWT.FILL, SWT.FILL, true, true);
		tabs.setLayoutData(dt);

		Composite itm1 = tabs.addElement("ftpedition.main.title", RM.getLabel("ftpedition.main.title"));
		initMainPanel(itm1);

		Composite itm2 = tabs.addElement("ftpedition.authentgroup.label", RM.getLabel("ftpedition.authentgroup.label"));
		initAuthPanel(itm2);
		
		Composite itm3 = tabs.addElement("sftpedition.hostkey.title", RM.getLabel("sftpedition.hostkey.title"));
		initFGPanel(itm3);

		buildSaveComposite(ret);
		initValues();
		
		tabs.setSelection(0);
		ret.pack(true);
		ret.layout(true);
		
		return ret;
	}

	private void initValues() {
		if (this.currentPolicy != null) {            
			this.txtHost.setText(currentPolicy.getRemoteServer());
			this.txtPort.setText("" + currentPolicy.getRemotePort());
			this.txtLogin.setText(currentPolicy.getLogin());
			this.txtRemoteDir.setText(currentPolicy.getRemoteDirectory());
			if (currentPolicy.getHostKey() != null) {
				this.txtHostKey.setText(currentPolicy.getHostKey());
			}
			this.chkCheckHostKey.setSelection(currentPolicy.isCheckHostKey());
			
			if (currentPolicy.isUseCertificateAuth()) {
				this.radCertificate.setSelection(true);
				this.txtPassphrase.setText(currentPolicy.getPassword());
				this.txtPrivateKey.setText(currentPolicy.getCertificateFileName());
				this.chkEncrypedtCert.setSelection(currentPolicy.isEncryptedCert());
			} else {
				this.radPassword.setSelection(true);
				this.txtPassword.setText(currentPolicy.getPassword());
			}
		} else {
			radPassword.setSelection(true);
			txtPort.setText("22");
		}
		handleCheckHostKeyModification();
		handleAuthModification();
		updateFingerPrint();
	}

	private GridLayout initLayout(int nbCols) {
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.numColumns = nbCols;
		layout.marginHeight = 0;
		layout.verticalSpacing = 10;
		layout.horizontalSpacing = 10;
		return layout;
	}

	private Composite initMainPanel(Composite parent) {
		parent.setLayout(new FillLayout());
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(initLayout(1));

		Group grpServer = new Group(composite, SWT.NONE);
		grpServer.setText(RM.getLabel("ftpedition.servergroup.label"));
		grpServer.setLayout(new GridLayout(2, false));
		GridData dt2 = new GridData(SWT.FILL, SWT.FILL, true, false);
		dt2.widthHint = computeWidth(450);
		dt2.heightHint = computeHeight(350);
		grpServer.setLayoutData(dt2);

		Label lblHost = new Label(grpServer, SWT.NONE);
		lblHost.setText(RM.getLabel("ftpedition.host.label"));
		txtHost = new Text(grpServer, SWT.BORDER);
		GridData dt = new GridData(SWT.FILL, SWT.CENTER, true, false);
		txtHost.setLayoutData(dt);
		monitorControl(txtHost);

		Label lblPort = new Label(grpServer, SWT.NONE);
		lblPort.setText(RM.getLabel("ftpedition.port.label"));
		txtPort = new Text(grpServer, SWT.BORDER);
		txtPort.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		monitorControl(txtPort);

		Label lblRemoteDir = new Label(grpServer, SWT.NONE);
		lblRemoteDir.setText(RM.getLabel("ftpedition.dir.label"));
		txtRemoteDir = new Text(grpServer, SWT.BORDER);
		txtRemoteDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		monitorControl(txtRemoteDir);

		return composite;
	}
	
	private Composite initAuthPanel(Composite parent) {
		parent.setLayout(new FillLayout());
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(initLayout(2));
		
		lblLogin = new Label(composite, SWT.NONE);
		lblLogin.setText(RM.getLabel("ftpedition.login.label"));
		lblLogin.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		txtLogin = new Text(composite, SWT.BORDER);
		txtLogin.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		monitorControl(txtLogin);
		
		radPassword = new Button(composite, SWT.RADIO);
		radPassword.setText(RM.getLabel("sftpedition.passauth.label"));
		radPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		monitorControl(radPassword);
		
		radPassword.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event) {
				handleAuthModification();
			}
		});

		grpPassword= new Group(composite, SWT.NONE);
		grpPassword.setText(RM.getLabel("sftpedition.passwordgroup.label"));
		grpPassword.setLayout(new GridLayout(2, false));
		grpPassword.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		txtPassword = new Text(grpPassword, SWT.BORDER);
		txtPassword.setEchoChar('*');
		txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		monitorControl(txtPassword);

		btnReveal = new Button(grpPassword, SWT.PUSH);
		btnReveal.setText(RM.getLabel("targetedition.reveal.label"));
		btnReveal.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		btnReveal.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (txtPassword.getEchoChar() == '*') {
					txtPassword.setEchoChar('\0');
					btnReveal.setText(RM.getLabel("targetedition.mask.label"));
					grpPassword.layout();
				} else {
					txtPassword.setEchoChar('*');
					btnReveal.setText(RM.getLabel("targetedition.reveal.label"));
					grpPassword.layout();
				}
			}
		});
		
		
		radCertificate = new Button(composite, SWT.RADIO);
		radCertificate.setText(RM.getLabel("sftpedition.certauth.label"));
		radCertificate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		monitorControl(radCertificate);

		grpCert= new Group(composite, SWT.NONE);
		grpCert.setText(RM.getLabel("sftpedition.certificategroup.label"));
		grpCert.setLayout(new GridLayout(3, false));
		grpCert.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		lblPrivateKey = new Label(grpCert, SWT.NONE);
		lblPrivateKey.setText(RM.getLabel("sftpedition.privkey.label"));
		txtPrivateKey = new Text(grpCert, SWT.BORDER);
		txtPrivateKey.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		monitorControl(txtPrivateKey);
		
		btnBrowse = new Button(grpCert, SWT.PUSH);
		btnBrowse.setText(RM.getLabel("common.browseaction.label"));
		btnBrowse.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				String dir = txtPrivateKey.getText();
				String path = Application.getInstance().showFileDialog(dir, SFTPEditionWindow.this);
				if (path != null) {
					txtPrivateKey.setText(path);
				}
			}
		});

		chkEncrypedtCert = new Button(grpCert, SWT.CHECK);
		chkEncrypedtCert.setText(RM.getLabel("sftpedition.passphrase.label"));
		chkEncrypedtCert.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event) {
				handleEncryptedCertModification();
			}
		});
		
		txtPassphrase = new Text(grpCert, SWT.BORDER);
		txtPassphrase.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtPassphrase.setEchoChar('*');
		monitorControl(txtPassphrase);
		
		btnReveal2 = new Button(grpCert, SWT.PUSH);
		btnReveal2.setText(RM.getLabel("targetedition.reveal.label"));
		btnReveal2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		btnReveal2.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (txtPassphrase.getEchoChar() == '*') {
					txtPassphrase.setEchoChar('\0');
					btnReveal2.setText(RM.getLabel("targetedition.mask.label"));
					grpCert.layout();
				} else {
					txtPassphrase.setEchoChar('*');
					btnReveal2.setText(RM.getLabel("targetedition.reveal.label"));
					grpCert.layout();
				}
			}
		});
		
		lblCertHint = new Text(grpCert, SWT.MULTI | SWT.WRAP);
		lblCertHint.setText(RM.getLabel("sftpedition.cert.hint.label"));
		lblCertHint.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		lblCertHint.setEnabled(false);
		
		composite.pack(true);
		composite.layout(true);
		
		return composite;
	}
	
	private void handleEncryptedCertModification() {
		btnReveal2.setEnabled(chkEncrypedtCert.getSelection());
		txtPassphrase.setEnabled(chkEncrypedtCert.getSelection());
	}
	
	private void handleAuthModification() {
		grpPassword.setEnabled(radPassword.getSelection());
		txtPassword.setEnabled(radPassword.getSelection());
		btnReveal.setEnabled(radPassword.getSelection());
		
		grpCert.setEnabled(radCertificate.getSelection());
		chkEncrypedtCert.setEnabled(radCertificate.getSelection());
		txtPassphrase.setEnabled(radCertificate.getSelection());
		lblPrivateKey.setEnabled(radCertificate.getSelection());
		txtPrivateKey.setEnabled(radCertificate.getSelection());
		btnBrowse.setEnabled(radCertificate.getSelection());
		btnReveal2.setEnabled(radCertificate.getSelection());
		
		if (chkEncrypedtCert.isEnabled()) {
			handleEncryptedCertModification();
		}
	}

	private void handleCheckHostKeyModification() {
		grpHostKey.setEnabled(chkCheckHostKey.getSelection());
		btnHostKey.setEnabled(chkCheckHostKey.getSelection());
		txtHostKey.setEnabled(chkCheckHostKey.getSelection());
		lblFingerprint.setEnabled(chkCheckHostKey.getSelection());
	}

	private void updateFingerPrint() {
		if (txtHost.getText() != null && txtHostKey.getText() != null && txtHostKey.getText().length() != 0) {
			byte[] keybytes = Util.base64Decode(txtHostKey.getText());
			String fg = "<" + RM.getLabel("sftpedition.invalid.key.label") + ">";
			if (keybytes != null) {
				try {
					HostKey key = new HostKey(txtHost.getText(), keybytes);
					fg = key.getFingerPrint(new JSch());
				} catch (JSchException ignored) {
				}
			}

			lblFingerprint.setText(RM.getLabel("sftpedition.fg.label") + " " + fg); 
		}
	}

	private Composite initFGPanel(Composite parent) {
		parent.setLayout(new FillLayout());
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(initLayout(1));

		chkCheckHostKey = new Button(composite, SWT.CHECK);
		chkCheckHostKey.setText(RM.getLabel("sftpedition.hostkey.check.label"));
		chkCheckHostKey.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		chkCheckHostKey.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event) {
				handleCheckHostKeyModification();
			}
		});

		grpHostKey = new Group(composite, SWT.NONE);
		grpHostKey.setText(RM.getLabel("sftpedition.hostkeygroup.label"));
		grpHostKey.setLayout(new GridLayout(2, false));
		grpHostKey.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		txtHostKey = new Text(grpHostKey, SWT.BORDER);
		txtHostKey.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		monitorControl(txtHostKey);
		txtHostKey.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
            	updateFingerPrint();
            }
        });

		btnHostKey = new Button(grpHostKey, SWT.PUSH);
		btnHostKey.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		btnHostKey.setText(RM.getLabel("sftpedition.retrieve.label"));
		btnHostKey.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event) {
				final String host = txtHost.getText(); 
				final int port = Integer.parseInt(txtPort.getText());

				Runnable rn = new Runnable() {
					public void run() {
						JSch jsch = new JSch();
						Session session = null;
						try {
							Logger.defaultLogger().info("Retrieving hostkey from " + host + " ...");
							session = jsch.getSession("Dummy", host, port);
							session.setConfig("StrictHostKeyChecking", "no");
							session.setDaemonThread(true);
							session.connect();
						} catch (Exception e) {
							if (session == null || session.getHostKey() == null) {
								Logger.defaultLogger().error("Error retrieving hostkey", e);
							}
						} finally {
							if (session != null) {
								if (session.getHostKey() != null) {
									HostKey key = session.getHostKey();
									final String keyStr = Util.base64Encode(HostKeyAccessor.getKeyAsByteArray(key));
									Logger.defaultLogger().info("Host key retrieved : " + keyStr);

									SecuredRunner.execute(new Runnable() {
										public void run() {
											txtHostKey.setText(keyStr);
							            	updateFingerPrint();
										}
									});
								}

								session.disconnect();
							}
						}
					}
				};
				Thread th = new Thread(rn, "Retrieve hostkey");
				th.setDaemon(true);
				th.start();
			}
		});

		lblFingerprint = new Label(grpHostKey, SWT.NONE);
		lblFingerprint.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		/*
        // Old J2SSH implementation
        btnFG.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                final SshClient clt = new SshClient();
                final HostKeyVerification verif = new HostKeyVerification() {
					public boolean verifyHost(String host, final SshPublicKey pk) throws TransportProtocolException {						
						Logger.defaultLogger().info("Fingerprint retrieved : " + pk.getFingerprint());
						SecuredRunner.execute(new Runnable() {
							public void run() {
								txtFG.setText(pk.getFingerprint());
							}
						});
						return true;
					}
                };

                final String host = txtHost.getText(); 
                final int port = Integer.parseInt(txtPort.getText());

                Runnable rn = new Runnable() {
                    public void run() {
        				try {
        					Logger.defaultLogger().info("Retrieving fingerprint from " + host + " ...");
        					clt.connect(host, port, verif);
        				} catch (Exception e) {
        					Logger.defaultLogger().error("Error retrieving fingerprint", e);
        				} finally {
        					clt.disconnect();
        				}
                    }
                };
                Thread th = new Thread(rn, "Retrieve fingerprint");
                th.setDaemon(true);
                th.start();
            }
        });
		 */
		return composite;
	}

	private void buildSaveComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		composite.setLayout(new GridLayout(3, false));

		btnTest = new Button(composite, SWT.PUSH);
		btnTest.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		btnTest.setText(RM.getLabel("ftpedition.test.label"));
		btnTest.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event) {
				final SFTPFileSystemPolicy policy = new SFTPFileSystemPolicy();
				initPolicy(policy);

				Runnable rn = new Runnable() {
					public void run() {
						testSFTP(policy);    
					}
				};
				Thread th = new Thread(rn, "FTP Test #" + Util.getRndLong());
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
				cancelChanges();
			}
		});
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

	protected void testSFTP(SFTPFileSystemPolicy policy) {   	
		SecuredRunner.execute(new Runnable() {
			public void run() {
				btnTest.setEnabled(false);
			}
		});

		try {
			policy.validate(true);
			SecuredRunner.execute(new Runnable() {
				public void run() {
					application.showInformationDialog(RM.getLabel("ftpedition.test.success"), RM.getLabel("ftpedition.test.title"), false);
				}
			});
		} catch (final Throwable e) {
			SecuredRunner.execute(new Runnable() {
				public void run() {
					application.showWarningDialog(RM.getLabel("ftpedition.test.failure", new Object[] {e.getMessage()}), RM.getLabel("ftpedition.test.title"), false);
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
		this.resetErrorState(txtHost);
		this.resetErrorState(txtPort);
		this.resetErrorState(txtRemoteDir);
		this.resetErrorState(txtLogin);
		this.resetErrorState(txtPassword);
		this.resetErrorState(txtHostKey);
		this.resetErrorState(txtPrivateKey);
		this.resetErrorState(txtPassphrase);
		
		if (! check(txtHost)) {
			return false;
		}

		if (! check(txtPort)) {
			return false;
		}

		try {
			Integer.parseInt(txtPort.getText());
		} catch (Throwable e) {
			this.setInError(txtPort, RM.getLabel("error.numeric.value.expected"));
			return false;
		}

		if (! check(txtRemoteDir)) {
			return false;
		}

		if (! check(txtLogin)) {
			return false;
		}

		if (radCertificate.getSelection()) {
			if (! check(txtPrivateKey)) {
				return false;
			} else if (! FileSystemManager.exists(new File(txtPrivateKey.getText()))) {
				this.setInError(txtPrivateKey, RM.getLabel("error.file.does.not.exist"));
				return false;
			}
			
			if (chkEncrypedtCert.getSelection() && ! check(txtPassphrase)) {
				return false;
			}
		} else {
			if (! check(txtPassword)) {
				return false;
			}
		}

		if (chkCheckHostKey.getSelection() && (! check(txtHostKey))) {
			return false;
		}

		return true;
	}

	private boolean check(Text fld) {
		if (fld.getText() == null || fld.getText().trim().length() == 0) {
			this.setInError(fld, RM.getLabel("error.field.mandatory"));
			return false;
		}
		return true;
	}

	protected void saveChanges() {
		if (this.currentPolicy == null) {
			this.currentPolicy = new SFTPFileSystemPolicy();
		}
		initPolicy(this.currentPolicy);

		this.hasBeenUpdated = false;
		this.close();
	}

	protected void updateState(boolean rulesSatisfied) {
		btnSave.setEnabled(rulesSatisfied);
		btnTest.setEnabled(rulesSatisfied);
	}

	protected void initPolicy(SFTPFileSystemPolicy policy) {
		policy.setLogin(txtLogin.getText());
		policy.setRemoteDirectory(txtRemoteDir.getText());
		policy.setRemotePort(Integer.parseInt(txtPort.getText()));
		policy.setRemoteServer(txtHost.getText());
		policy.setHostKey(txtHostKey.getText());
		policy.setCheckHostKey(chkCheckHostKey.getSelection());
		policy.setUseCertificateAuth(radCertificate.getSelection());
		if (policy.isUseCertificateAuth()) {
			policy.setCertificateFileName(txtPrivateKey.getText());
			policy.setEncryptedCert(chkEncrypedtCert.getSelection());
			policy.setPassword(txtPassphrase.getText());
		} else {
			policy.setPassword(txtPassword.getText());
		}
	}

	public SFTPFileSystemPolicy getCurrentPolicy() {
		return currentPolicy;
	}
}
