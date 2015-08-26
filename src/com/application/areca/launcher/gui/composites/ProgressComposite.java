package com.application.areca.launcher.gui.composites;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.common.Refreshable;
import com.application.areca.launcher.gui.resources.ResourceManager;

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
public class ProgressComposite 
extends AbstractTabComposite 
implements Refreshable {
    protected final ResourceManager RM = ResourceManager.instance();
    protected Composite mainPane;
	private Button btnClear;
    
    public ProgressComposite(Composite parent) {
        super(parent, SWT.NONE);
        
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginHeight = 0;
        mainLayout.marginWidth = 0;
        setLayout(mainLayout);
        ScrolledComposite sc = new ScrolledComposite(this, SWT.H_SCROLL | SWT.V_SCROLL);
        sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        mainPane = new Composite(sc, SWT.NONE);
        sc.setContent(mainPane);

        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);
        
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 1;
		mainPane.setLayout(layout);
		
        Composite panel = new Composite(this, SWT.NONE);
        panel.setLayout(new GridLayout(1, false));
        panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		btnClear = new Button(panel, SWT.PUSH);
		btnClear.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		btnClear.setText(RM.getLabel("progress.removeall.label"));
		btnClear.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				Control[] children = ProgressComposite.this.getMainPane().getChildren();
				for (int i=0; i<children.length; i++) {
					((GUIInformationChannel)children[i]).removeIfPossible();
				}
			}
		});
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
		Control[] children = ProgressComposite.this.getMainPane().getChildren();
		boolean rn = false;
		for (int i=0; i<children.length; i++) {
			if (((GUIInformationChannel)children[i]).isRunning()) {
				rn = true;
				break;
			}
		}
    	
        if (! rn) {
            Application.getInstance().getMainWindow().goBackToLastTab();
        }
    }
}
