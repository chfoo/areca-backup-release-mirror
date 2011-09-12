package com.application.areca.launcher.gui;

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

import com.application.areca.ResourceManager;
import com.application.areca.impl.policy.SFTPFileSystemPolicy;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ListPane;
import com.application.areca.launcher.gui.common.SecuredRunner;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyAccessor;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.myJava.util.Util;
import com.myJava.util.log.Logger;

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

	protected Button btnReveal;

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

		Composite itm2 = tabs.addElement("sftpedition.hostkey.title", RM.getLabel("sftpedition.hostkey.title"));
		initFGPanel(itm2);

		buildSaveComposite(ret);
		initValues();

		tabs.setSelection(0);
		ret.pack();
		return ret;
	}

	private void initValues() {
		if (this.currentPolicy != null) {            
			this.txtHost.setText(currentPolicy.getRemoteServer());
			this.txtPort.setText("" + currentPolicy.getRemotePort());
			this.txtLogin.setText(currentPolicy.getLogin());
			this.txtPassword.setText(currentPolicy.getPassword());
			this.txtRemoteDir.setText(currentPolicy.getRemoteDirectory());
			if (currentPolicy.getHostKey() != null) {
				this.txtHostKey.setText(currentPolicy.getHostKey());
			}
			this.chkCheckHostKey.setSelection(currentPolicy.isCheckHostKey());
		}
		handleCheckHostKeyModification();
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
		grpServer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label lblHost = new Label(grpServer, SWT.NONE);
		lblHost.setText(RM.getLabel("ftpedition.host.label"));
		txtHost = new Text(grpServer, SWT.BORDER);
		GridData dt = new GridData(SWT.FILL, SWT.CENTER, true, false);
		dt.widthHint = computeWidth(300);
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

		new Label(composite, SWT.NONE);

		final Group grpAuthent = new Group(composite, SWT.NONE);
		grpAuthent.setText(RM.getLabel("ftpedition.authentgroup.label"));
		grpAuthent.setLayout(new GridLayout(3, false));
		grpAuthent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label lblLogin = new Label(grpAuthent, SWT.NONE);
		lblLogin.setText(RM.getLabel("ftpedition.login.label"));
		txtLogin = new Text(grpAuthent, SWT.BORDER);
		txtLogin.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		monitorControl(txtLogin);

		Label lblPassword = new Label(grpAuthent, SWT.NONE);
		lblPassword.setText(RM.getLabel("ftpedition.password.label"));
		txtPassword = new Text(grpAuthent, SWT.BORDER);
		txtPassword.setEchoChar('*');
		txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		monitorControl(txtPassword);

		btnReveal = new Button(grpAuthent, SWT.PUSH);
		btnReveal.setText(RM.getLabel("targetedition.reveal.label"));
		btnReveal.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		btnReveal.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (txtPassword.getEchoChar() == '*') {
					txtPassword.setEchoChar('\0');
					btnReveal.setText(RM.getLabel("targetedition.mask.label"));
					grpAuthent.layout();
				} else {
					txtPassword.setEchoChar('*');
					btnReveal.setText(RM.getLabel("targetedition.reveal.label"));
					grpAuthent.layout();
				}
			}
		});

		return composite;
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

		if (! check(txtPassword)) {
			return false;
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
		policy.setPassword(txtPassword.getText());
		policy.setRemoteDirectory(txtRemoteDir.getText());
		policy.setRemotePort(Integer.parseInt(txtPort.getText()));
		policy.setRemoteServer(txtHost.getText());
		policy.setHostKey(txtHostKey.getText());
		policy.setCheckHostKey(chkCheckHostKey.getSelection());
	}

	public SFTPFileSystemPolicy getCurrentPolicy() {
		return currentPolicy;
	}
}
