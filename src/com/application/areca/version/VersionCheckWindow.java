package com.application.areca.version;

import java.io.IOException;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.launcher.gui.common.SecuredRunner;
import com.myJava.system.NoBrowserFoundException;
import com.myJava.system.viewer.ViewerHandlerHelper;
import com.myJava.util.Util;
import com.myJava.util.log.Logger;
import com.myJava.util.version.VersionData;

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
public class VersionCheckWindow 
extends ApplicationWindow {
   
    private Text txt;
    private Label lbl;
    private Button btnOK;
    private Button btnCancel;
    
    private boolean initialized = false;
    
    private VersionData data = null;
    
    public VersionCheckWindow() {
        super(null);
    }
    
    protected void configureShell(final Shell shell) {
        super.configureShell(shell);
        shell.setText(VersionInfos.APP_NAME + " - Version check module.");
        shell.setImage(ArecaImages.ICO_SMALL);
        shell.addShellListener(new ShellListener() {
            public void shellActivated(ShellEvent e) {
                if (! initialized) {
                    initialized = true;
                    launchCheck();
                }
            }
            public void shellClosed(ShellEvent e) {}
            public void shellDeactivated(ShellEvent e) {}
            public void shellDeiconified(ShellEvent e) {}
            public void shellIconified(ShellEvent e) {}
        });
    }
    
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
  
        lbl = new Label(composite, SWT.NONE);
        GridData dt = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
        lbl.setLayoutData(dt);
        
        txt = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
        GridData dt2 = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        dt2.heightHint = 250;
        dt2.widthHint = 400;
        txt.setLayoutData(dt2);

        btnOK = new Button(composite, SWT.PUSH);
        btnOK.setText("Open download location");
        btnOK.setEnabled(false);
        btnOK.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, false));
        btnOK.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                try {
                	ViewerHandlerHelper.getViewerHandler().browse(data.getDownloadUrl());
                    close();
                } catch (IOException e1) {
                    Logger.defaultLogger().error(e1);
                    lbl.setText("Error connecting to : " + data.getDownloadUrl());
                } catch (NoBrowserFoundException e1) {
                    Logger.defaultLogger().error(e1);
                    lbl.setText("Error connecting to : " + data.getDownloadUrl() + " - No web browser could be found.");
                }
            }
        });

        btnCancel = new Button(composite, SWT.PUSH);
        btnCancel.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false));
        btnCancel.setText("Close");
        btnCancel.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                close();
            }
        });
        
        composite.pack();        
        return composite;
    }
    
    private void launchCheck() {
        Runnable rn = new CheckRunner();
        Thread thr = new Thread(rn);
        thr.setDaemon(true);
        thr.setName("Version Check");
        thr.start();
    }
    
    private class CheckRunner implements Runnable {
        public void run() {
            try {
                SecuredRunner.execute(VersionCheckWindow.this, new Runnable(){
                    public void run() {
                        lbl.setText("Connecting to " + VersionChecker.getInstance().getCheckHost() + " ...");
                    }
                });
                data = VersionChecker.getInstance().checkForNewVersion();
                VersionData currentVersion = VersionInfos.getLastVersion();
                
                if (currentVersion.equals(data)) {
                    SecuredRunner.execute(VersionCheckWindow.this, new Runnable(){
                        public void run() {
                            lbl.setText("No new version found : Your version (v" + VersionInfos.getLastVersion().getVersionId() + ") is the latest version.");
                        }
                    });
                } else {
                    SecuredRunner.execute(VersionCheckWindow.this, new Runnable(){
                        public void run() {
                            lbl.setText("A new version of " + VersionInfos.APP_NAME + " has been found.");
                            btnOK.setEnabled(true);
                        }
                    });
                }
                
                SecuredRunner.execute(VersionCheckWindow.this, new Runnable(){
                    public void run() {
                        txt.setText(
                                "Version : " + data.getVersionId() + 
                                " (" + VersionInfos.formatVersionDate(data.getVersionDate()) + ")" +
                                "\n\nDescription :\n" + ((data.getDescription() == null || data.getDescription().trim().length() == 0) ? "No description provided for this version." : Util.replace(data.getDescription(), "<BR>", "\n")) +
                                "\n\nDownload URL :\n" + (data.getDownloadUrl() == null ? "" : data.getDownloadUrl().toExternalForm())
                        );
                    }
                });
                

            } catch (final Throwable e) {
                Logger.defaultLogger().error(e);
                SecuredRunner.execute(VersionCheckWindow.this, new Runnable(){
                    public void run() {
                        lbl.setText("An error occurred during the remote informations retrieval ("+ e.getMessage() + "). Please try again later.");
                    }
                });
            }            
        }
    }
}
