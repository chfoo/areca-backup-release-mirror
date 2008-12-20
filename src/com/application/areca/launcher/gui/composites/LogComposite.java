package com.application.areca.launcher.gui.composites;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;

import com.application.areca.ResourceManager;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.common.Refreshable;
import com.application.areca.launcher.gui.common.SecuredRunner;
import com.myJava.util.log.LogHelper;
import com.myJava.util.log.LogProcessor;
import com.myJava.util.log.Logger;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 8785459451506899793
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
public class LogComposite 
extends Composite 
implements LogProcessor, Refreshable, Listener {
    protected final ResourceManager RM = ResourceManager.instance();
    private StyledText txtLog;
    private int position = 0;
    protected Button btnClear;
    private Application application = Application.getInstance();
    private Set displayedMessages = new HashSet();
    
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
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        panel.setLayout(layout);

        GridData data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = SWT.RIGHT;
        btnClear = new Button(panel, SWT.PUSH);
        btnClear.setLayoutData(data);
        btnClear.setText(RM.getLabel("app.clearlog.label"));
        btnClear.addListener(SWT.Selection, this);
        
        return panel;
    }
    
    public boolean clearLog() {
        SecuredRunner.execute(this, new Runnable() {
            public void run() {
                txtLog.setText("");
                position = 0;
                txtLog.setStyleRange(null);	// Clear all styles
            }
        });
        return true;
    }
    
	public void unmount() {
	}

    public void log(final int level, String message, Throwable e, String source) {
        try {
            String txt = LogHelper.format(level, message, source);
            if (e != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                e.printStackTrace(ps);
                ps.close();
                txt += "\n" + new String(baos.toByteArray());
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
                }
            });
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
    
    public void handleEvent(Event event) {
        application.clearLog();
    }

    public void displayApplicationMessage(final String messageKey, final String title, final String message) {
        if (! hasMessageBeenDisplayed(messageKey)) {
            registerMessage(messageKey);

            SecuredRunner.execute(Application.getInstance().getMainWindow().getShell(), new Runnable() {
                public void run() {
                    MessageBox msg = new MessageBox(Application.getInstance().getMainWindow().getShell(), SWT.OK | SWT.ICON_INFORMATION);
                    msg.setText(title);
                    msg.setMessage(message);
                    
                    msg.open();
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
    
    private static Color RED = new Color(Application.getInstance().getDisplay(), 250, 0, 0);
    private static Color ORANGE = new Color(Application.getInstance().getDisplay(), 250, 120, 0);
    private static Color BLUE = new Color(Application.getInstance().getDisplay(), 0, 0, 250);
    private static Color GREY = new Color(Application.getInstance().getDisplay(), 150, 150, 150);
    
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
