package com.application.areca.launcher.gui.filters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.application.areca.filter.ArchiveFilter;
import com.application.areca.filter.FilterGroup;
import com.application.areca.launcher.gui.FilterEditionWindow;
import com.application.areca.launcher.gui.FilterRepository;

/**
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
public class FilterGroupComposite extends AbstractFilterComposite {
    
    protected Button rdAnd;
    protected Button rdOr;
    
    public FilterGroupComposite(Composite composite, ArchiveFilter filter, FilterEditionWindow window) {
        super(composite, FilterRepository.getIndex(FilterGroup.class), filter, window);
        
        this.setLayout(new GridLayout(1, false));
        
        rdAnd = new Button(this, SWT.RADIO);
        rdAnd.setText(RM.getLabel("common.operator.and"));
        rdAnd.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
        window.monitorControl(rdAnd);
        
        rdOr = new Button(this, SWT.RADIO);
        rdOr.setText(RM.getLabel("common.operator.or"));
        rdOr.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
        window.monitorControl(rdOr);
        
        FilterGroup fg = (FilterGroup)currentFilter;
        if (this.currentFilter != null) {
            if (fg.isAnd()) {
                rdAnd.setSelection(true);
            } else {
                rdOr.setSelection(true);                
            }
        } else {
            rdAnd.setSelection(true);
        }
    }
    
    public void initFilter(ArchiveFilter filter) {
        FilterGroup fg = (FilterGroup)filter;
        fg.setAnd(rdAnd.getSelection());
    }
    
    public boolean validateParams() {
        return true;
    }

	public boolean allowTest() {
		return false;
	}
}
