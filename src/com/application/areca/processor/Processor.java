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
public interface Processor extends Comparable, Duplicable {
	public static short RUN_SCHEME_ALWAYS = 0;
	public static short RUN_SCHEME_FAILURE = 1;
	public static short RUN_SCHEME_SUCCESS = 2;
	
	public String getKey();
	
    public void run(ProcessContext context) throws ApplicationException;
    public String getParametersSummary();
    public void validate() throws ProcessorValidationException;
    
    /**
     * Tells when the processor must be run (always, in case of failure, or for successfull backups)
     * @return
     */
    public short getRunScheme();
}
