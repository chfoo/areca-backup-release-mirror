package com.application.areca.launcher.gui.filters;

import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.application.areca.ArecaURLs;
import com.application.areca.filter.ArchiveFilter;
import com.application.areca.filter.RegexArchiveFilter;
import com.application.areca.launcher.gui.FilterEditionWindow;
import com.application.areca.launcher.gui.FilterRepository;
import com.myJava.system.viewer.ViewerHandlerHelper;
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
public class RegexFilterComposite 
extends AbstractFilterComposite 
implements ArecaURLs {
    private static final String EXAMPLE_REGEX = RM.getLabel("filteredition.exampleregex.label");    
    
    protected Button radWholePath;
    protected Button radFileName;
    protected Button radDirectory;    
    protected Text txtRegex;
    protected Button chkMatch;
    
    public RegexFilterComposite(Composite composite, ArchiveFilter filter, FilterEditionWindow window) {
        super(composite, FilterRepository.getIndex(RegexArchiveFilter.class), filter, window);
        
        this.setLayout(new GridLayout(2, false));
        
        txtRegex = new Text(this, SWT.BORDER);
        txtRegex.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        window.monitorControl(txtRegex);
        
        Label lblExample = new Label(this, SWT.NONE);
        lblExample.setText(EXAMPLE_REGEX);
        Link lnk = new Link(this, SWT.NONE);
        lnk.addListener (SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                try {
                    ViewerHandlerHelper.getViewerHandler().browse(new URL(event.text));
                } catch (Exception e) {
                    Logger.defaultLogger().error(e);
                }
            }
        });
        lnk.setText("<A HREF=\"" + REGEX_URL + "\">" + RM.getLabel("menu.help.label") + "</A>");
        lnk.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        
        new Label(this, SWT.NONE);
        new Label(this, SWT.NONE);
        
        radWholePath = addButton("filteredition.regex.path", SWT.RADIO);
        radWholePath.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
        radFileName = addButton("filteredition.regex.filename", SWT.RADIO);
        radFileName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
        radDirectory = addButton("filteredition.regex.directory", SWT.RADIO);
        radDirectory.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
        
        new Label(this, SWT.NONE);
        new Label(this, SWT.NONE);
        
        chkMatch = addButton("filteredition.regex.match", SWT.CHECK);

        RegexArchiveFilter sff = (RegexArchiveFilter)currentFilter;
        if (sff != null) {
        	txtRegex.setText(sff.getRegex());
        	if (sff.getScheme().equals(RegexArchiveFilter.SCHEME_NAME)) {
        		radFileName.setSelection(true);
        	} else if (sff.getScheme().equals(RegexArchiveFilter.SCHEME_FULLPATH)){
        		radWholePath.setSelection(true);
        	} else {
        		radDirectory.setSelection(true);
        	}
        	chkMatch.setSelection(sff.isMatch());
        } else {
    		radFileName.setSelection(true);
    		chkMatch.setSelection(true);
        }
    }
    
    private Button addButton(String label, int style) {
        Button btn = new Button(this, style);
        btn.setText(RM.getLabel(label + ".label"));
        btn.setToolTipText(RM.getLabel(label + ".tt"));
        btn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
        window.monitorControl(btn);
        return btn;
    }
    
    public void initFilter(ArchiveFilter filter) {
    	RegexArchiveFilter sff = (RegexArchiveFilter)filter;
    	
    	sff.setRegex(txtRegex.getText());
    	if (radFileName.getSelection()) {
    		sff.setScheme(RegexArchiveFilter.SCHEME_NAME);
    	} else if (radWholePath.getSelection()) {
    		sff.setScheme(RegexArchiveFilter.SCHEME_FULLPATH);
    	} else {
    		sff.setScheme(RegexArchiveFilter.SCHEME_PARENTDIR);    		
    	}
    	sff.setMatch(chkMatch.getSelection());
    }
    
    public boolean validateParams() {
        window.resetErrorState(txtRegex);  
        boolean result = FilterRepository.checkParameters(txtRegex.getText(), this.filterIndex);
        if (! result) {
            window.setInError(txtRegex, RM.getLabel("error.invalid.parameters"));
        }
        return result;
    }
}
