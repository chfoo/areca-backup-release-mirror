package com.application.areca.postprocess;

import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.myJava.util.log.Logger;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 3274863990151426915
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
public abstract class AbstractPostProcessor implements PostProcessor {

    public AbstractPostProcessor() {
    }
    
    public void postProcess(ProcessContext context) throws ApplicationException {
        try {
            Logger.defaultLogger().info("Validating and running post-processor : " + this.getName() + " - " + this.getParametersSummary());
            this.validate();
            this.run(context);
        } catch (PostProcessorValidationException e) {
            throw new ApplicationException("Error during post-processor validation : " + e.getMessage(), e);
        }
    }
    
    public String getName() {
        return this.getClass().getName();
    }
    
    protected abstract void run(ProcessContext context) throws ApplicationException;

    public int compareTo(Object o) {
        if (o == null) {
            return 1;
        } else {
            PostProcessor other = (PostProcessor)o;
            return (this.getClass().getName() + this.getParametersSummary()).compareTo(other.getClass().getName() + other.getParametersSummary());
        }
    }
}
