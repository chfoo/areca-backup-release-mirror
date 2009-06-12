package com.application.areca.launcher.gui.composites;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;

import com.application.areca.ResourceManager;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.common.ArecaPreferences;
import com.application.areca.launcher.gui.common.Refreshable;
import com.application.areca.launcher.gui.common.SecuredRunner;
import com.myJava.system.OSTool;
import com.myJava.util.Util;
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
public class LogComposite 
extends Composite 
implements LogProcessor, Refreshable {
	private static Color RED = new Color(Application.getInstance().getDisplay(), 250, 0, 0);
	private static Color ORANGE = new Color(Application.getInstance().getDisplay(), 250, 120, 0);
	private static Color BLUE = new Color(Application.getInstance().getDisplay(), 0, 0, 250);
	private static Color GREY = new Color(Application.getInstance().getDisplay(), 150, 150, 150);

	private static final long MAX_SIZE = (long)(OSTool.getMaxMemory() * 0.10 * 0.25); // Max memory * 10% / 4

	private final ResourceManager RM = ResourceManager.instance();	
	private Application application = Application.getInstance();
	private int position = 0;
	private Set displayedMessages = new HashSet();
	private int logLevel = Math.min(ArecaPreferences.getLogLevel(), Logger.defaultLogger().getLogLevel());

	private StyledText txtLog;
	private Button btnClear;
	private Button btnThreadDump;
	private Combo cboLogLevel;

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
		txtLog.setForeground(GREY);

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

	private Composite buildBottomComposite(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new GridLayout(4, false));

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
		for (int i=0; i<Logger.LEVELS.length; i++) {
			if (Logger.LEVELS[i] == logLevel) {
				cboLogLevel.select(i);
			}
		}
		cboLogLevel.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				LogComposite.this.logLevel = Logger.LEVELS[cboLogLevel.getSelectionIndex()];
				ArecaPreferences.setLogLevel(LogComposite.this.logLevel);
			}
		});
		
		btnThreadDump = new Button(panel, SWT.PUSH);
		btnThreadDump.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		btnThreadDump.setText(RM.getLabel("app.logtd.label"));
		btnThreadDump.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				Util.logAllThreadInformations();
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

		return panel;
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
		try {
			if (level <= logLevel) {			
				String txt = LogHelper.format(level, message, source, true);
				if (e != null) {
					txt += "\n" + LogHelper.formatException(e);
				}
				txt += "\n";
				final String fTxt = txt;
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
						txtLog.setSelection(position, position);
						txtLog.showSelection();
						
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
		// Does nothing
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
		if (level > 7) {
			return null;
		} 

		StyleRange style = new StyleRange();
		if (level == 1) {
			style.foreground = RED;
		} else if (level <= 3) {
			style.foreground = ORANGE;
		} else {
			style.foreground = BLUE;
		}

		return style;
	}
}
