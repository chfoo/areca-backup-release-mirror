package com.application.areca.launcher.gui.composites;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.application.areca.ResourceManager;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.common.Refreshable;

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
public class ProgressComposite 
extends Composite 
implements Refreshable {
    protected final ResourceManager RM = ResourceManager.instance();
    protected Composite mainPane;
    
    public ProgressComposite(Composite parent) {
        super(parent, SWT.NONE);
        this.setLayout(new GridLayout(1, false));
    }
    
    public Composite getMainPane() {
    	return mainPane;
    }

    public Object getRefreshableKey() {
        return this.getClass().getName();
    }

    public void refresh() {
        // Does nothing
    }
    
    public void taskFinished() {
        if (this.getChildren().length == 0) {
            Application.getInstance().getMainWindow().goBackToLastTab();
        }
    }
}
