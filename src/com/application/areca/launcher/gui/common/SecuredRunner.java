package com.application.areca.launcher.gui.common;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

import com.application.areca.launcher.gui.Application;

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
public class SecuredRunner 
implements Runnable {
    
    private Widget parent;
    private Display parent2;
    private RuntimeException exception;
    private Runnable runnable;

    private SecuredRunner(Widget parent, Runnable runnable) {
        this.parent = parent;
        this.runnable = runnable;
    }
    
    private SecuredRunner(Display parent, Runnable runnable) {
        this.parent2 = parent;
        this.runnable = runnable;
    }

    public RuntimeException getException() {
		return exception;
	}

	public void run() {
        if (
                (parent != null && ! parent.isDisposed())
                || (parent2 != null && ! parent2.isDisposed())
        ) {
            try {
				runnable.run();
			} catch (RuntimeException e) {
				exception = e;
			}
        }
    }
    
    public static void execute(ApplicationWindow window, Runnable runnable) {
        execute(window.getShell(), runnable);
    }
    
    /**
     * Return a status flag (ok / nok)
     */
    public static void execute(Widget parent, Runnable runnable) {
        if (parent != null && ! parent.isDisposed()) {
        	SecuredRunner rn = new SecuredRunner(parent, runnable);
            parent.getDisplay().syncExec(rn);
            if (rn.getException() != null) {
            	throw rn.getException();
            }
        }
    }
    
    public static void execute(Runnable runnable) {
    	execute(runnable, false);
    }
    
    public static void execute(Runnable runnable, boolean async) {
        execute(Application.getInstance().getDisplay(), runnable, async);
    }
    
    public static void execute(Display parent, Runnable runnable, boolean async) {
        if (parent != null && ! parent.isDisposed()) {
        	SecuredRunner rn = new SecuredRunner(parent, runnable);
        	if (async) {
        		parent.asyncExec(rn);        		
        	} else {
        		parent.syncExec(rn);
        	}
            if (rn.getException() != null) {
            	throw rn.getException();
            }
        }
    }
    
    public static void execute(Display parent, Runnable runnable) {
    	execute(parent, runnable, false);
    }
}