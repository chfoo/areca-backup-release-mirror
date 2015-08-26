package com.application.areca.launcher.gui.composites;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;

import com.application.areca.launcher.ArecaUserPreferences;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.launcher.gui.common.Colors;
import com.application.areca.launcher.gui.common.Refreshable;
import com.application.areca.launcher.gui.common.SecuredRunner;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.myJava.file.driver.remote.RemoteConnectionMonitor;
import com.myJava.system.OSTool;
import com.myJava.util.Util;
import com.myJava.util.log.FileLogProcessor;
import com.myJava.util.log.LogHelper;
import com.myJava.util.log.LogProcessor;
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
public class LogComposite 
extends AbstractTabComposite 
implements LogProcessor, Refreshable {
	private static final long MAX_SIZE = (long)(OSTool.getMaxMemory() * 0.10 * 0.25); // Max memory * 10% / 4
	private static final int MAX_LEVEL = 1000;

	private final ResourceManager RM = ResourceManager.instance();	
	private Application application = Application.getInstance();
	private int position = 0;
	private Set displayedMessages = new HashSet();
	private int logLevel = Math.min(ArecaUserPreferences.getLogLevel(), Logger.defaultLogger().getLogLevel());
	private boolean lockScroll = false;

	private StyledText txtLog;
	private Button btnClear;
	private Button btnThreadDump;
	private Button btnLock;
	private Combo cboLogLevel;
	private Composite panel;
	private Link lblPath;

	private Font warningFont;
	private int currentMinLevel = MAX_LEVEL;

	public LogComposite(Composite parent) {
		super(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		setLayout(layout);

		txtLog = new StyledText(this, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		txtLog.setEditable(false);
		txtLog.setMenu(Application.getInstance().getLogContextMenu());
		txtLog.setForeground(Colors.C_GREY);

		GridData dt1 = new GridData();
		dt1.grabExcessHorizontalSpace = true;
		dt1.grabExcessVerticalSpace = true;
		dt1.horizontalAlignment = SWT.FILL;
		dt1.verticalAlignment = SWT.FILL;
		txtLog.setLayoutData(dt1);

		Composite bottom = buildBottomComposite(this);
		GridData dt2 = new GridData();
		dt2.grabExcessHorizontalSpace = true;
		dt2.horizontalAlignment = SWT.FILL;
		dt2.verticalAlignment = SWT.FILL;
		bottom.setLayoutData(dt2);

		Logger.defaultLogger().remove(this.getClass());
		Logger.defaultLogger().addProcessor(this);
	}

	private void switchLockScroll() {
		if (lockScroll) {
			btnLock.setText(RM.getLabel("app.locklog.label"));
		} else {
			btnLock.setText(RM.getLabel("app.unlocklog.label"));
		}
		lockScroll = ! lockScroll;
		panel.layout(true);
	}

	private Composite buildBottomComposite(Composite parent) {
		panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new GridLayout(5, false));

		lblPath = new Link(panel, SWT.NONE);
		lblPath.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1));

		Label lblLevel = new Label(panel, SWT.NONE);
		lblLevel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblLevel.setText(RM.getLabel("app.loglevel.label"));

		cboLogLevel = new Combo(panel, SWT.READ_ONLY);
		cboLogLevel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		if (Logger.LEVELS[0] <= Logger.defaultLogger().getLogLevel()) {
			cboLogLevel.add(RM.getLabel("app.log.error.label"));
		}

		if (Logger.LEVELS[1] <= Logger.defaultLogger().getLogLevel()) {
			cboLogLevel.add(RM.getLabel("app.log.warning.label"));
		}

		if (Logger.LEVELS[2] <= Logger.defaultLogger().getLogLevel()) {
			cboLogLevel.add(RM.getLabel("app.log.info.label"));
		}

		if (Logger.LEVELS[3] <= Logger.defaultLogger().getLogLevel()) {
			cboLogLevel.add(RM.getLabel("app.log.detail.label"));
		}
		
		if (Logger.LEVELS[4] <= Logger.defaultLogger().getLogLevel()) {
			cboLogLevel.add(RM.getLabel("app.log.finest.label"));
		}
		
		for (int i=0; i<Logger.LEVELS.length; i++) {
			if (Logger.LEVELS[i] == logLevel) {
				cboLogLevel.select(i);
			}
		}
		cboLogLevel.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				LogComposite.this.logLevel = Logger.LEVELS[cboLogLevel.getSelectionIndex()];
				ArecaUserPreferences.setLogLevel(LogComposite.this.logLevel);
			}
		});

		btnClear = new Button(panel, SWT.PUSH);
		btnClear.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		btnClear.setText(RM.getLabel("app.clearlog.label"));
		btnClear.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				application.clearLog();
			}
		});

		btnLock = new Button(panel, SWT.PUSH);
		btnLock.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		btnLock.setText(RM.getLabel("app.locklog.label"));
		btnLock.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				switchLockScroll();
			}
		});

		btnThreadDump = new Button(panel, SWT.PUSH);
		btnThreadDump.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		btnThreadDump.setText(RM.getLabel("app.logtd.label"));
		btnThreadDump.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				Util.logAllThreadInformations();

				Logger.defaultLogger().info("Connection data :");
				Logger.defaultLogger().fine(RemoteConnectionMonitor.getInstance().toString());
			}
		});

		return panel;
	}

	public void getFocus() {
		super.getFocus();
		markRead();
	}

	public void markRead() {
		SecuredRunner.execute(this, new Runnable() {
			public void run() {
				setCurrentLevel(MAX_LEVEL);
			}});
	}

	private void setCurrentLevel(final int l) {

		SecuredRunner.execute(this, new Runnable() {
			public void run() {
				if (l <= Logger.LOG_LEVEL_WARNING) {
					if (! hasFocus()) {
						getTab().setFont(deriveWarningFont());

						if (l < currentMinLevel) {
							if (l <= Logger.LOG_LEVEL_ERROR) {
								getTab().setImage(ArecaImages.ICO_TAB_LOG_ERR);
								//Application.setTabLabel(getTab(), RM.getLabel("mainpanel.log.label") + " (" + RM.getLabel("app.log.error.label") + ")");
							} else {
								getTab().setImage(ArecaImages.ICO_TAB_LOG_WARN);
								//Application.setTabLabel(getTab(), RM.getLabel("mainpanel.log.label") + " (" + RM.getLabel("app.log.warning.label") + ")");
							}
						}
					}
				} else {
					getTab().setFont(null);
					getTab().setImage(ArecaImages.ICO_TAB_LOG);
					//Application.setTabLabel(getTab(), RM.getLabel("mainpanel.log.label"));
				}
			}});
		currentMinLevel = l;
	}

	private Font deriveWarningFont() {
		if (this.warningFont == null) {
			FontData dt = this.getTab().getFont().getFontData()[0];
			FontData dt2 = new FontData(dt.getName(), dt.getHeight(), SWT.BOLD);
			warningFont = new Font(this.getTab().getDisplay(), new FontData[] { dt2 });
		}
		return warningFont;
	}

	private void doClearLog() {
		SecuredRunner.execute(this, new Runnable() {
			public void run() {
				txtLog.setText("");
				position = 0;
				txtLog.setStyleRange(null);	// Clear all styles
			}
		});
	}

	public boolean clearLog() {
		doClearLog();
		return true;
	}

	public void unmount() {
	}

	public void log(final int level, String message, Throwable e, String source) {
		if (level < currentMinLevel) {
			setCurrentLevel(level);
		}

		try {
			if (level <= logLevel) {			
				StringBuffer txt = LogHelper.format(level, message, source, true);
				if (e != null) {
					txt.append("\n").append(LogHelper.formatException(e));
				}
				txt.append("\n");
				final String fTxt = txt.toString();
				SecuredRunner.execute(this, new Runnable() {
					public void run() {
						int l = fTxt.length();
						txtLog.append(fTxt);
						StyleRange rg = resolveStyle(level);
						if (rg != null) {
							rg.start = position;
							rg.length = l;
							txtLog.setStyleRange(rg);
						}
						position += l;
						if (! lockScroll) {
							txtLog.setSelection(position, position);
							txtLog.showSelection();
						}

						if (txtLog.getCharCount() > MAX_SIZE) {
							doClearLog();
							log(Logger.LOG_LEVEL_WARNING, "Log memory limit reached : the log has been cleared.", null, null);
						}
					}
				});
			}
		} catch (Throwable ignored) {
			ignored.printStackTrace();
		}
	}

	public Object getRefreshableKey() {
		return this.getClass().getName();
	}

	public void refresh() {
		FileLogProcessor fileProc = (FileLogProcessor)Logger.defaultLogger().find(FileLogProcessor.class);
		if (fileProc != null) {
			final String path = fileProc.getCurrentLogFile();
			lblPath.setText("<A HREF=\"\">" + path + "</A>");
			Listener[] listeners = lblPath.getListeners(SWT.Selection);
			for (int i=0; i<listeners.length; i++) {
				lblPath.removeListener(SWT.Selection, listeners[i]);
			}
			lblPath.addListener (SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					try {
						Application.getInstance().secureOpenFile(path);
					} catch (Exception e) {
						Logger.defaultLogger().error(e);
					}
				}
			});
		}
	}

	public void displayApplicationMessage(final String messageKey, final String title, final String message) {
		if (! hasMessageBeenDisplayed(messageKey)) {
			registerMessage(messageKey);

			SecuredRunner.execute(Application.getInstance().getMainWindow().getShell(), new Runnable() {
				public void run() {
					MessageBox msg = new MessageBox(Application.getInstance().getMainWindow().getShell(), SWT.OK | SWT.CANCEL | SWT.ICON_INFORMATION);
					msg.setText(title);
					msg.setMessage(message);

					int ret = msg.open();
					if (ret != SWT.OK) {
						throw new IllegalStateException("Task canceled by user.");
					}
				}
			});
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

	private static StyleRange resolveStyle(int level) {
		if (level > Logger.LOG_LEVEL_INFO) {
			return null;
		} 

		StyleRange style = new StyleRange();
		if (level == Logger.LOG_LEVEL_ERROR) {
			style.foreground = Colors.C_RED;
		} else if (level == Logger.LOG_LEVEL_WARNING) {
			style.foreground = Colors.C_ORANGE;
		} else {
			style.foreground = Colors.C_BLUE;
		}

		return style;
	}
}
