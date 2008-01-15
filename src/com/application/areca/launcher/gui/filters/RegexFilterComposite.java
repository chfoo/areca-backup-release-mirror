package com.application.areca.launcher.gui.filters;

import org.eclipse.swt.widgets.Composite;

import com.application.areca.filter.ArchiveFilter;
import com.application.areca.filter.RegexArchiveFilter;
import com.application.areca.launcher.gui.FilterEditionWindow;
import com.application.areca.launcher.gui.FilterRepository;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 1926729655347670856
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
public class RegexFilterComposite extends AbstractSimpleParamFilterComposite {
    private static final String EXAMPLE_REGEX = RM.getLabel("filteredition.exampleregex.label");    
    
    public RegexFilterComposite(Composite composite, ArchiveFilter filter, FilterEditionWindow window) {
        super(composite, FilterRepository.getIndex(RegexArchiveFilter.class), filter, window);
    }

    public String getParamExample() {
        return EXAMPLE_REGEX;
    }
}
