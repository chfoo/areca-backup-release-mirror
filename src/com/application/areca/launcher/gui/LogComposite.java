package com.application.areca.launcher.gui;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.application.areca.ResourceManager;
import com.application.areca.launcher.gui.common.Refreshable;
import com.application.areca.launcher.gui.common.SecuredRunner;
import com.myJava.util.log.LogHelper;
import com.myJava.util.log.LogProcessor;
import com.myJava.util.log.Logger;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -6307890396762748969
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
    private Text txtLog;
    private int position = 0;
    protected Button btnClear;
    private Application application = Application.getInstance();
    
    public LogComposite(Composite parent) {
        super(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.numColumns = 1;
        setLayout(layout);
        
        txtLog = new Text(this, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        txtLog.setEditable(false);
        txtLog.setMenu(Application.getInstance().getLogContextMenu());
        
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
                synchronized(this) {
                    txtLog.setText("");
                    position = 0;
                }
            }
        });
        return true;
    }

    public void log(int level, String message, Throwable e, String source) {
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
                    synchronized(this) {
                        position += fTxt.length();
                        txtLog.append(fTxt);
                        txtLog.update();
                    }
                }
            });
        } catch (Throwable ignored) {
            System.out.println(ignored);
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
}
