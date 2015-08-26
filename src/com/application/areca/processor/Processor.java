package com.application.areca.processor;

import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.myJava.object.Duplicable;

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
public interface Processor extends Comparable, Duplicable {
	public static final int ACTION_BACKUP = 1;
	public static final int ACTION_MERGE = 2;
	public static final int ACTION_CHECK = 3;
	
	public String getKey();
	
    public void run(ProcessContext context) throws ApplicationException;
    public String getParametersSummary();
    public boolean requireStatistics();
    public void validate() throws ProcessorValidationException;
    
    public boolean isRunIfOK();
	public void setRunIfOK(boolean runIfOK);
	public boolean isRunIfWarning();
	public void setRunIfWarning(boolean runIfWarning);
	public boolean isRunIfError();
	public void setRunIfError(boolean runIfError);
	public boolean shallRun(int action);
	
	public boolean isRunBackup();
	public boolean isRunMerge();
	public boolean isRunCheck();
}
