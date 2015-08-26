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
public abstract class AbstractProcessor implements Processor {
	private boolean runIfOK = true;
	private boolean runIfWarning = true;
	private boolean runIfError = true;
	
	private boolean runBackup = true;
	private boolean runMerge = false;
	private boolean runCheck = false;

    public AbstractProcessor() {
    }

    public boolean isRunBackup() {
		return runBackup;
	}

	public void setRunBackup(boolean runBackup) {
		this.runBackup = runBackup;
	}

	public boolean isRunMerge() {
		return runMerge;
	}

	public void setRunMerge(boolean runMerge) {
		this.runMerge = runMerge;
	}

	public boolean isRunIfOK() {
		return runIfOK;
	}

	public boolean isRunCheck() {
		return runCheck;
	}

	public void setRunCheck(boolean runCheck) {
		this.runCheck = runCheck;
	}

	public void setRunIfOK(boolean runIfOK) {
		this.runIfOK = runIfOK;
	}

	public boolean isRunIfWarning() {
		return runIfWarning;
	}

	public void setRunIfWarning(boolean runIfWarning) {
		this.runIfWarning = runIfWarning;
	}

	public boolean isRunIfError() {
		return runIfError;
	}

	public void setRunIfError(boolean runIfError) {
		this.runIfError = runIfError;
	}
	
	public void setRunAlways() {
		this.runIfError = true;
		this.runIfWarning = true;
		this.runIfOK = true;
	}

    public boolean shallRun(int action) {
    	return (
    			action == Processor.ACTION_BACKUP && runBackup
    			|| action == Processor.ACTION_MERGE && runMerge
    			|| action == Processor.ACTION_CHECK && runCheck
    	);
	}

	private boolean shallRun(ProcessContext context) {
    	if (context.getReport().hasError() && this.runIfError) {
    		return true;
    	} else if (context.getReport().hasWarnings() && this.runIfWarning) {
    		return true;
    	} else if (!context.getReport().hasError() && !context.getReport().hasWarnings() && this.runIfOK) {
    		return true;
    	} else {
    		return false;
    	}
    }

	protected void copyAttributes(AbstractProcessor proc) {
    	proc.runIfOK = runIfOK;
    	proc.runIfError = runIfError;
    	proc.runIfWarning = runIfWarning;
    	
    	proc.runBackup = runBackup;
    	proc.runMerge = runMerge;
    	proc.runCheck = runCheck;
    }

	public void run(ProcessContext context) throws ApplicationException {
        try {
            Logger.defaultLogger().info("Validating and running processor : " + this.getName() + " - " + this.getParametersSummary());
            this.validate();
            if (shallRun(context)) {
            	this.runImpl(context);
            } else {
            	Logger.defaultLogger().info("The processor will not be run (rule = \"" + getSchemeAsString() + "\" and state = \"" + (context.getReport().hasError() ? "Error":(context.getReport().hasWarnings() ? "Warning":"Success")) + "\")");
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
                EqualsHelper.equals(this.runIfOK, other.runIfOK)
            	&& EqualsHelper.equals(this.runIfError, other.runIfError)
            	&& EqualsHelper.equals(this.runIfWarning, other.runIfWarning)
            	&& EqualsHelper.equals(this.runMerge, other.runMerge)
            	&& EqualsHelper.equals(this.runBackup, other.runBackup)
            	&& EqualsHelper.equals(this.runCheck, other.runCheck)
            	;            
        }
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, this.runIfOK);
        h = HashHelper.hash(h, this.runIfError);
        h = HashHelper.hash(h, this.runIfWarning);
        h = HashHelper.hash(h, this.runMerge);
        h = HashHelper.hash(h, this.runBackup);
        h = HashHelper.hash(h, this.runCheck);
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
    	if (runIfOK && runIfError && runIfWarning) {
    		return "Always";
    	} else if (!runIfOK && !runIfError && !runIfWarning) {
    		return "Never";
    	} else {
    		String ret = "In case of ";
    		if (runIfOK) {
    			ret += "success or ";
    		}
    		if (runIfError) {
    			ret += "error or ";
    		}
    		if (runIfWarning) {
    			ret += "warning or ";
    		}
    		return ret.substring(0, ret.length() - 4);
    	}
    }
}
