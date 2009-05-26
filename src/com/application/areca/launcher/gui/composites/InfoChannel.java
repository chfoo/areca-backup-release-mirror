package com.application.areca.launcher.gui.composites;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ResourceManager;
import com.application.areca.UserInformationChannel;
import com.application.areca.launcher.gui.common.ArecaPreferences;
import com.application.areca.launcher.gui.common.Colors;
import com.application.areca.launcher.gui.common.SecuredRunner;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskMonitor;

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
public class InfoChannel 
extends Composite
implements UserInformationChannel, Colors, Listener {

    private static final ResourceManager RM = ResourceManager.instance();

    private Composite parent;
    private String currentMessage = "";
    private AbstractRecoveryTarget target;
    
    private Label lblMessage;
    private ProgressBar pgbProgress;
    private Button btnCancel;
    private Button btnPause;
    
    protected TaskMonitor taskMonitor;
    protected boolean running;
    
    protected String stateBeforePause = "";
    
    protected boolean synthetic = ArecaPreferences.isInformationSynthetic();

    /**
     * @param parent
     * @param style
     */
    public InfoChannel(AbstractRecoveryTarget target, Composite parent) {
        super(parent, SWT.NONE);
        this.parent = parent;
        this.target = target;
        this.setLayout(new FillLayout());
        this.setToolTipText(target.getTargetName());
        Composite grp = this;
        
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.horizontalSpacing = 20;
        layout.verticalSpacing = 1;
        layout.marginHeight = 5;
        layout.marginWidth = 0;
        grp.setLayout(layout);

        lblMessage = new Label(grp, SWT.NONE);
        GridData gdMessage = new GridData();
        gdMessage.grabExcessHorizontalSpace = true;
        gdMessage.horizontalAlignment = SWT.FILL;
        lblMessage.setLayoutData(gdMessage);
        lblMessage.setForeground(C_INFO);
        
        btnPause = new Button(grp, SWT.PUSH);
        btnPause.setText(RM.getLabel("mainpanel.pause.label"));
        GridData gdPause = new GridData();
        gdPause.grabExcessHorizontalSpace = false;
        gdPause.horizontalAlignment = SWT.CENTER;
        gdPause.verticalAlignment = SWT.BOTTOM;
        gdPause.verticalSpan = 2;
        btnPause.setLayoutData(gdPause);
        btnPause.addListener(SWT.Selection, this);   
        btnPause.setForeground(C_INFO);
        
        btnCancel = new Button(grp, SWT.PUSH);
        btnCancel.setText(RM.getLabel("common.cancel.label"));
        GridData gdCancel = new GridData();
        gdCancel.grabExcessHorizontalSpace = false;
        gdCancel.horizontalAlignment = SWT.CENTER;
        gdCancel.verticalAlignment = SWT.BOTTOM;
        gdCancel.verticalSpan = 2;
        btnCancel.setLayoutData(gdCancel);
        btnCancel.addListener(SWT.Selection, this);   
        btnCancel.setForeground(C_INFO);

        pgbProgress = new ProgressBar(grp, SWT.NONE);
        pgbProgress.setMinimum(0);
        pgbProgress.setMaximum(100);
        GridData gdProgress = new GridData();
        gdProgress.grabExcessHorizontalSpace = true;
        gdProgress.horizontalAlignment = SWT.FILL;
        pgbProgress.setLayoutData(gdProgress);
    }

    public void setSynthetic(boolean synthetic) {
        this.synthetic = synthetic;
    }

	public void pauseRequested(TaskMonitor task) {
		if (task.isPauseRequested()) {
			this.stateBeforePause = lblMessage.getText();
	        SecuredRunner.execute(parent, new Runnable() {
	            public void run() {
	                lblMessage.setText("Paused.");
	            }
	        });
		} else {
	        SecuredRunner.execute(parent, new Runnable() {
	            public void run() {
	                lblMessage.setText(stateBeforePause);
	            }
	        });
		}
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

    public void print(final String info) {
        SecuredRunner.execute(parent, new Runnable() {
            public void run() {
                if (synthetic) {
                    lblMessage.setText(format(target.getTargetName(), info));
                } else {
                    lblMessage.setText(info);
                    currentMessage = info;
                }
            }
        });
        Logger.defaultLogger().info(info);
    }

    public void warn(String info) {
        Logger.defaultLogger().warn(info);
	}
    
    public void error(String info) {
        Logger.defaultLogger().error(info);
	}

	public void startRunning() {
        SecuredRunner.execute(parent, new Runnable() {
            public void run() {
                running = true;
            }
        });
    }

    public void stopRunning() {
        SecuredRunner.execute(parent, new Runnable() {
            public void run() {
                dispose();
                parent.layout();
                running = false;
                
                // send a message to the progress tab
                ((ProgressComposite)parent).taskFinished();
            }
        });
    }

    public void updateCurrentTask(final long taskIndex, final long taskCount, final String taskDescription) {
        if (taskCount != 0) {
            Logger.defaultLogger().info(taskDescription);
        }
        
        if (! synthetic) {
            SecuredRunner.execute(parent, new Runnable() {
                public void run() {
                    lblMessage.setText(format(currentMessage, taskDescription));
                }
            });
        }
    }

    public void handleEvent(Event event) {
        if (event.widget == this.btnCancel) {
            taskMonitor.setCancelRequested();
        } else if (event.widget == this.btnPause) {
        	taskMonitor.setPauseRequested(! taskMonitor.isPauseRequested());
        	if (taskMonitor.isPauseRequested()) {
        		btnPause.setText(ResourceManager.instance().getLabel("mainpanel.resume.label"));
        	} else {
        		btnPause.setText(ResourceManager.instance().getLabel("mainpanel.pause.label"));        		
        	}
            this.layout();
        }
    }

    public boolean isRunning() {
		return running;
	}

	public void setTaskMonitor(TaskMonitor taskMonitor) {
        this.taskMonitor = taskMonitor;
        taskMonitor.addListener(this);
    }

    public TaskMonitor getTaskMonitor() {
        return taskMonitor;
    }
    
    private String format(String t1, String t2) {
        return t1 + "\t\t" + t2;
    }
}
