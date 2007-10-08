package com.application.areca.launcher.gui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ResourceManager;
import com.application.areca.launcher.gui.postprocessors.AbstractProcessorComposite;
import com.application.areca.launcher.gui.postprocessors.FileDumpProcessorComposite;
import com.application.areca.launcher.gui.postprocessors.MailSendProcessorComposite;
import com.application.areca.launcher.gui.postprocessors.MergeProcessorComposite;
import com.application.areca.launcher.gui.postprocessors.ShellScriptProcessorComposite;
import com.application.areca.processor.CustomAction;
import com.application.areca.processor.FileDumpAction;
import com.application.areca.processor.MailSendAction;
import com.application.areca.processor.MergeAction;
import com.application.areca.processor.ShellScriptAction;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 6222835200985278549
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
public class ProcessorRepository {
    
    private static final ResourceManager RM = ResourceManager.instance();
    
    public static List getProcessors(boolean preProcess) {
        ArrayList list = new ArrayList();
        list.add(RM.getLabel("procedition.shell.label"));
        
        if (! preProcess) {
            list.add(RM.getLabel("procedition.dump.label"));
            list.add(RM.getLabel("procedition.mail.label"));
            list.add(RM.getLabel("procedition.merge.label"));
        }
        return list;
    }
    
    public static AbstractProcessorComposite buildProcessorComposite(
            int index, 
            Composite composite,
            CustomAction proc, 
            ProcessorEditionWindow frm
    ) {
        AbstractProcessorComposite pnl = null;
        if (index == 1) {
            pnl = new FileDumpProcessorComposite(composite, proc, frm);
        } else if (index == 2){
            pnl = new MailSendProcessorComposite(composite, proc, frm);
        } else if (index == 0){
            pnl = new ShellScriptProcessorComposite(composite, proc, frm);
        } else if (index == 3){
            pnl = new MergeProcessorComposite(composite, proc, frm);
        }
        
        return pnl;
    }
    
    public static CustomAction buildProcessor(int index, AbstractRecoveryTarget target) {
        CustomAction proc = null;
        if (index == 1) {
            proc = new FileDumpAction();
        } else if (index == 2){
            proc = new MailSendAction();
        } else if (index == 0){
            proc = new ShellScriptAction();
        } else if (index == 3){
            proc = new MergeAction();
        }
        
        return proc;
    }
    
    public static int getIndex(CustomAction proc) {
        if (proc == null) {
            return 0;
        }
        
    	if (ShellScriptAction.class.isAssignableFrom(proc.getClass())) {
    		return 0;
    	} else if (MailSendAction.class.isAssignableFrom(proc.getClass())) {
    		return 2;
    	} else if (FileDumpAction.class.isAssignableFrom(proc.getClass())) {
    		return 1;         
    	} else if (MergeAction.class.isAssignableFrom(proc.getClass())) {
    		return 3;         
    	}
    	
    	return 0;
    }
    
    public static String getName(Class proc) {
        if (FileDumpAction.class.isAssignableFrom(proc)) {
            return RM.getLabel("procedition.dump.label");
        } else if (MailSendAction.class.isAssignableFrom(proc)) {
            return RM.getLabel("procedition.mail.label");
        } else if (MergeAction.class.isAssignableFrom(proc)) {
            return RM.getLabel("procedition.merge.label");
        } else {
            return RM.getLabel("procedition.shell.label");          
        }        
    }
}
