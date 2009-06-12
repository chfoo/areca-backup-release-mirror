package com.application.areca.processor;

import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.util.log.Logger;

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
public abstract class AbstractProcessor implements Processor {
	private short runScheme;

    public AbstractProcessor() {
    	runScheme = RUN_SCHEME_ALWAYS;
    }

    public short getRunScheme() {
    	return runScheme;
	}

    public void setRunScheme(short runScheme) {
		this.runScheme = runScheme;
	}
    
    public boolean shallRun(ProcessContext context) {
    	return
    		runScheme == Processor.RUN_SCHEME_ALWAYS
    		|| (runScheme == Processor.RUN_SCHEME_FAILURE && context.getReport().getStatus().hasError())
    		|| (runScheme == Processor.RUN_SCHEME_SUCCESS && (! context.getReport().getStatus().hasError()));
    }

	protected void copyAttributes(AbstractProcessor proc) {
    	proc.runScheme = runScheme;
    }

	public void run(ProcessContext context) throws ApplicationException {
        try {
            Logger.defaultLogger().info("Validating and running processor : " + this.getName() + " - " + this.getParametersSummary());
            this.validate();
            if (shallRun(context)) {
            	this.runImpl(context);
            } else {
            	Logger.defaultLogger().info("The processor will not be run (rule = \"" + getSchemeAsString() + "\" and state = \"" + (context.getReport().getStatus().hasError() ? "Failure":"Success") + "\")");
            }
        } catch (ProcessorValidationException e) {
            throw new ApplicationException("Error during processor validation : " + e.getMessage(), e);
        }
    }
	
    public boolean equals(Object obj) {
        if (obj == null || (! (obj instanceof AbstractProcessor)) ) {
            return false;
        } else {
        	AbstractProcessor other = (AbstractProcessor)obj;
            return 
                EqualsHelper.equals(this.runScheme, other.runScheme);
        }
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, this.runScheme);
        return h;
    }
    
    public String getName() {
        return this.getClass().getName();
    }
    
    protected abstract void runImpl(ProcessContext context) throws ApplicationException;

    public int compareTo(Object o) {
        if (o == null) {
            return 1;
        } else {
            Processor other = (Processor)o;
            return (this.getClass().getName() + this.getParametersSummary()).compareTo(other.getClass().getName() + other.getParametersSummary());
        }
    }
    
    private String getSchemeAsString() {
    	if (runScheme == RUN_SCHEME_ALWAYS) {
    		return "Always";
    	} else if (runScheme == RUN_SCHEME_FAILURE) {
    		return "Only in case of failure";
    	} else {
    		return "Only in case of success";
    	}
    }
}
