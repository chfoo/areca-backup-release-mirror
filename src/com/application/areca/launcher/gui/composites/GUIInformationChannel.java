package com.application.areca.launcher.gui.composites;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;

import com.application.areca.AbstractTarget;
import com.application.areca.UserInformationChannel;
import com.application.areca.context.ProcessContext;
import com.application.areca.launcher.ArecaUserPreferences;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.ReportWindow;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.launcher.gui.common.Colors;
import com.application.areca.launcher.gui.common.SecuredRunner;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskMonitor;

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
public class GUIInformationChannel 
extends Composite
implements UserInformationChannel, Colors, Listener {
	private static final ResourceManager RM = ResourceManager.instance();

	private Composite parent;
	private Label lblMessage;
	private ProgressBar pgbProgress;
	private Button btnCancel;
	private Button btnPause;

	private AbstractTarget target;
	private ProcessContext context;
	private TaskMonitor taskMonitor;
	private boolean running;
	private String stateBeforePause = "";
	private String currentMessage = "";
	private boolean synthetic = ArecaUserPreferences.isInformationSynthetic();
	private String action;

	public GUIInformationChannel(AbstractTarget target, Composite parent) {
		super(parent, SWT.BORDER);
		this.parent = parent;
		this.target = target;
		this.setLayout(new FillLayout());
		this.setToolTipText(target.getName());
		Composite grp = this;
		this.setBackground(Colors.C_WHITE);

		GridLayout layout = new GridLayout(4, false);
		layout.horizontalSpacing = 20;
		layout.verticalSpacing = 1;
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		grp.setLayout(layout);

		Label lblIco = new Label(grp, SWT.NONE);
		lblIco.setImage(ArecaImages.ICO_CHANNEL);
		lblIco.setBackground(Colors.C_WHITE);
		
		lblMessage = new Label(grp, SWT.NONE);
		lblMessage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		lblMessage.setForeground(C_INFO);
		lblMessage.setBackground(Colors.C_WHITE);

		btnPause = new Button(grp, SWT.PUSH);
		btnPause.setText(RM.getLabel("mainpanel.pause.label"));
		btnPause.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, true, 1, 2));
		btnPause.addListener(SWT.Selection, this);   
		btnPause.setForeground(C_INFO);

		btnCancel = new Button(grp, SWT.PUSH);
		btnCancel.setText(RM.getLabel("common.cancel.label"));
		btnCancel.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, true, 1, 2));
		btnCancel.addListener(SWT.Selection, this);   
		btnCancel.setForeground(C_INFO);

		pgbProgress = new ProgressBar(grp, SWT.NONE);
		pgbProgress.setMinimum(0);
		pgbProgress.setMaximum(100);
		pgbProgress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		this.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		this.setSize(this.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		recomputeParentMinimumSize(true);
	}

	public void setAction(String action) {
		this.action = action;
	}

	private void recomputeParentMinimumSize(boolean includeThis) {
		int h = 0;
		for (int i=0; i<parent.getChildren().length; i++) {
			h += parent.getChildren()[i].getSize().y;
		}
		((ScrolledComposite)parent.getParent()).setMinHeight(h + (includeThis ? this.computeSize(SWT.DEFAULT, SWT.DEFAULT).y : 0));
		parent.layout(true);
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
				try {
					int pc = (int)(100 * task.getGlobalCompletionRate());
					pgbProgress.setSelection(pc);
				} catch (Exception e) {
					// Non blocking error - just log it
					Logger.defaultLogger().error(e);
				}
			}
		});
	}

	public void print(final String info) {
		SecuredRunner.execute(parent, new Runnable() {
			public void run() {
				if (synthetic) {
					lblMessage.setText(format(target.getName(), info));
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
				running = false;
				lblMessage.setText(format(target.getName(), action + " (" + RM.getLabel("progress.finished.label") + ")"));
				if (context.getReport().hasError()) {
					lblMessage.setForeground(Colors.C_RED);
				} else if (context.getReport().getLogMessagesContainer().hasWarnings()) {
					lblMessage.setForeground(Colors.C_ORANGE);
				} 

				btnPause.dispose();
				btnCancel.dispose();
				pgbProgress.dispose();

				Link lnkShowReport = new Link(GUIInformationChannel.this, SWT.NONE);
				lnkShowReport.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
				lnkShowReport.addListener (SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						SecuredRunner.execute(new Runnable() {
							public void run() {
								ReportWindow frm = new ReportWindow(context.getReport());
								Application.getInstance().showDialog(frm, false);
							}
						});
					}
				});
				lnkShowReport.setText("<A>" + RM.getLabel("progress.report.label") + "</A>");
				lnkShowReport.setBackground(Colors.C_WHITE);
				

				Link lnkRemove = new Link(GUIInformationChannel.this, SWT.NONE);
				lnkRemove.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
				lnkRemove.addListener (SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						removeIfPossible();
					}
				});
				lnkRemove.setText("<A>" + RM.getLabel("progress.remove.label") + "</A>");
				lnkRemove.setBackground(Colors.C_WHITE);
				
				recomputeParentMinimumSize(true);

				// send a message to the progress tab
				((ProgressComposite)(parent.getParent().getParent())).taskFinished();
			}
		});
	}
	
	public void removeIfPossible() {
		if (! this.running) {
			this.dispose();
			recomputeParentMinimumSize(false);
		}
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

	public ProcessContext getContext() {
		return context;
	}

	public void setContext(ProcessContext context) {
		this.context = context;
	}
}
