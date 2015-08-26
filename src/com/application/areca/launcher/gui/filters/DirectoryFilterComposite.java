package com.application.areca.launcher.gui.filters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.application.areca.Utils;
import com.application.areca.filter.ArchiveFilter;
import com.application.areca.filter.DirectoryArchiveFilter;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.FilterEditionWindow;
import com.application.areca.launcher.gui.FilterRepository;
import com.myJava.system.OSTool;

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
public class DirectoryFilterComposite extends AbstractStringParamFilterComposite {
    private static final String EXAMPLE_DIR_WIN = RM.getLabel("filteredition.exampledirwin.label");
    private static final String EXAMPLE_DIR_LINUX = RM.getLabel("filteredition.exampledirlinux.label");
    
    public DirectoryFilterComposite(Composite composite, ArchiveFilter filter, final FilterEditionWindow window) {
        super(composite, FilterRepository.getIndex(DirectoryArchiveFilter.class), filter, window);
        
        this.setLayout(new GridLayout(2, false));
        
        txt = new Text(this, SWT.BORDER);
        GridData dt = new GridData(SWT.FILL, SWT.CENTER, true, false);
        txt.setLayoutData(dt);
        window.monitorControl(txt);
        
        Button btnBrowse = new Button(this, SWT.PUSH);
        btnBrowse.setText(RM.getLabel("common.browseaction.label"));
        btnBrowse.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                String path = Application.getInstance().showDirectoryDialog(txt.getText(), window);
                if (path != null) {
                    txt.setText(path);
                }
            }
        });
        
        Label lblExample = new Label(this, SWT.NONE);
        lblExample.setText(getParamExample());
        lblExample.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        
        postInit();
    }

    public String getDefaultParameters() {
        if (
                window.getCurrentTarget() != null
                &&  window.getCurrentTarget().getSourcesRoot() != null
        ) {
            return window.getCurrentTarget().getSourcesRoot();
        } else {
            return super.getDefaultParameters();
        }
    }

    public String getParamExample() {
        if (OSTool.isSystemWindows()) {
            return EXAMPLE_DIR_WIN;
        } else {
            return EXAMPLE_DIR_LINUX;
        }
    }
    
    public void initFilter(ArchiveFilter filter) {
    	txt.setText(Utils.normalizePath(txt.getText()));
        super.initFilter(filter);
    }
}
