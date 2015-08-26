package com.application.areca.launcher.gui.processors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.application.areca.launcher.gui.ProcessorEditionWindow;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.application.areca.processor.Processor;

/**
 * Abstract implementation for all Post Processor parameters panel
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
public abstract class AbstractProcessorComposite extends Composite {

    protected static final ResourceManager RM = ResourceManager.instance();
    protected Processor currentProcessor;
    protected ProcessorEditionWindow window;
    
    public AbstractProcessorComposite(Composite parent, Processor proc, ProcessorEditionWindow window) {
        super(parent, SWT.NONE);
        this.window = window;
        this.currentProcessor = proc;
    }
    
    public abstract void initProcessor(Processor proc);
    public abstract boolean validateParams();
}
