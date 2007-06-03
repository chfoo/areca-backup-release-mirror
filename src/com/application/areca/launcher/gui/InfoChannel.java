package com.application.areca.launcher.gui;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;

import com.application.areca.ResourceManager;
import com.application.areca.UserInformationChannel;
import com.application.areca.launcher.gui.common.Colors;
import com.application.areca.launcher.gui.common.SecuredRunner;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2162742295696737000
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
public class InfoChannel 
extends Composite
implements UserInformationChannel, Colors, Listener {

    private static final ResourceManager RM = ResourceManager.instance();

    private Composite parent;
    private Set displayedMessages = new HashSet();
    private String currentMessage = "";
    
    private Label lblMessage;
    private ProgressBar pgbProgress;
    private Button btnCancel;
    
    protected boolean running;

    /**
     * @param parent
     * @param style
     */
    public InfoChannel(Composite parent) {
        super(parent, SWT.NONE);
        this.parent = parent;
        
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.horizontalSpacing = 30;
        layout.verticalSpacing = 1;
        layout.marginHeight = 3;
        layout.marginWidth = 0;
        this.setLayout(layout);

        lblMessage = new Label(this, SWT.NONE);
        GridData gdMessage = new GridData();
        gdMessage.grabExcessHorizontalSpace = true;
        gdMessage.horizontalAlignment = SWT.FILL;
        lblMessage.setLayoutData(gdMessage);
        
        btnCancel = new Button(this, SWT.PUSH);
        btnCancel.setText(RM.getLabel("common.cancel.label"));
        GridData gdCancel = new GridData();
        gdCancel.grabExcessHorizontalSpace = false;
        gdCancel.horizontalAlignment = SWT.RIGHT;
        gdCancel.verticalSpan = 2;
        btnCancel.setLayoutData(gdCancel);
        btnCancel.addListener(SWT.Selection, this);
        
        pgbProgress = new ProgressBar(this, SWT.NONE);
        pgbProgress.setMinimum(0);
        pgbProgress.setMaximum(100);
        GridData gdProgress = new GridData();
        gdProgress.grabExcessHorizontalSpace = true;
        gdProgress.horizontalAlignment = SWT.FILL;
        pgbProgress.setLayoutData(gdProgress);

        reset();
    }

    public void cancellableChanged(final TaskMonitor task) {
        SecuredRunner.execute(parent, new Runnable() {
            public void run() {
                btnCancel.setEnabled(task.isCancellable());
            }
        });
    }

    public void cancelRequested(final TaskMonitor task) {
        SecuredRunner.execute(parent, new Runnable() {
            public void run() {
                lblMessage.setText(ResourceManager.instance().getLabel("mainpanel.cancelling.label"));
            }
        });
    }

    public void completionChanged(final TaskMonitor task) {
        SecuredRunner.execute(parent, new Runnable() {
            public void run() {
                int pc = (int)(100 * task.getGlobalCompletionRate());
                pgbProgress.setSelection(pc);
            }
        });
    }

    public void displayApplicationMessage(final String messageKey, final String title, final String message) {
        if (! hasMessageBeenDisplayed(messageKey)) {
            registerMessage(messageKey);

            Logger.defaultLogger().warn(title);
            Logger.defaultLogger().warn(message);

            SecuredRunner.execute(parent, new Runnable() {
                public void run() {
                    MessageBox msg = new MessageBox(parent.getShell(), SWT.OK | SWT.ICON_INFORMATION);
                    msg.setText(title);
                    msg.setMessage(message);
                    
                    msg.open();
                }
            });
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void logError(String title, String error, Throwable e) {
        log(getText(title, error), C_ERROR);
        Logger.defaultLogger().error(error, e, title);
    }

    public void logInfo(String title, String info) {
        log(getText(title, info), C_INFO);
        Logger.defaultLogger().info(info, title);
    }

    public void logWarning(String title, String warning) {
        log(getText(title, warning), C_WARNING);
        Logger.defaultLogger().warn(warning, title);
    }

    private void log(final String msg, final org.eclipse.swt.graphics.Color color) {
        SecuredRunner.execute(parent, new Runnable() {
            public void run() {
                lblMessage.setForeground(color);    
                btnCancel.setForeground(color);
                lblMessage.setText(msg);
                currentMessage = msg;
            }
        });
    }

    public void reset() {
        SecuredRunner.execute(parent, new Runnable() {
            public void run() {
                pgbProgress.setVisible(false);
                btnCancel.setVisible(false);
                lblMessage.setVisible(false);
                pgbProgress.setSelection(0);
                lblMessage.setText(" ");
            }
        });
    }

    public void startRunning() {
        SecuredRunner.execute(parent, new Runnable() {
            public void run() {
                btnCancel.setEnabled(true);
                pgbProgress.setVisible(true);
                btnCancel.setVisible(true);
                lblMessage.setVisible(true);
                running = true;
            }
        });
    }

    public void stopRunning() {
        SecuredRunner.execute(parent, new Runnable() {
            public void run() {
                btnCancel.setEnabled(false);
                running = false;
            }
        });
    }

    public void updateCurrentTask(final long taskIndex, final long taskCount, final String taskDescription) {
        SecuredRunner.execute(parent, new Runnable() {
            public void run() {
                lblMessage.setText(currentMessage + "\t" + taskDescription);
                if (taskCount != 0) {
                    Logger.defaultLogger().info(taskDescription);
                }
            }
        });
    }

    private String getText(String title, String message) {
        if (title == null && message == null) {
            return "";
        } else if (title == null) {
            return message;
        } else if (message == null) {
            return title;            
        } else {
            return title + " - " + message;
        }
    }

    protected void registerMessage(Object messageKey) {
        if (messageKey != null) {
            this.displayedMessages.add(messageKey);
        }
    }

    protected boolean hasMessageBeenDisplayed(Object messageKey) {
        return (
                messageKey != null
                && displayedMessages.contains(messageKey)
        );
    }

    public void handleEvent(Event event) {
        if (event.widget == this.btnCancel) {
            Application.getInstance().getCurrentProcess().getTaskMonitor().setCancelRequested();
        }
    }
}
