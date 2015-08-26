package com.application.areca.processor;

import com.application.areca.AbstractTarget;
import com.application.areca.ActionProxy;
import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.myJava.object.Duplicable;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;

/**
 * Delete archives
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
public class DeleteProcessor extends AbstractProcessor {

    private int delay = 0;

    /**
     * @param target
     */
    public DeleteProcessor() {
        super();
    }
    
    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public boolean requireStatistics() {
		return false;
	}

	public void runImpl(ProcessContext context) throws ApplicationException {
        AbstractTarget target = context.getReport().getTarget();
        ActionProxy.processDeleteOnTarget(target, delay, context.createSubContext());
    }
    
    public String getParametersSummary() {
        String ret = "";
        ret += "[-" + delay;
        ret +=  "; 0]";
        
        return ret;
    }
    
    public Duplicable duplicate() {
        DeleteProcessor pro = new DeleteProcessor();
        copyAttributes(pro);
        pro.delay = this.delay;
        return pro;
    }

    public void validate() throws ProcessorValidationException {
        if (delay < 0) {
            throw new ProcessorValidationException("The deletion delay must be above or equal to 0");
        }
    }
    
    public boolean equals(Object obj) {
        if (obj == null || (! (obj instanceof DeleteProcessor)) ) {
            return false;
        } else {
            DeleteProcessor other = (DeleteProcessor)obj;
            return 
                super.equals(other)
            	&& EqualsHelper.equals(this.delay, other.delay);
        }
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, super.hashCode());
        h = HashHelper.hash(h, this.delay);
        return h;
    }

	public String getKey() {
		return "Delete Archives";
	}
}
