package com.application.areca.processor;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.object.PublicClonable;

/**
 * Delete archives
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 6668125177615540854
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
    
    public void runImpl(ProcessContext context) throws ApplicationException {
        AbstractRecoveryTarget target = context.getReport().getTarget();
        target.getProcess().processDeleteOnTargetImpl(target, delay, new ProcessContext(target, context.getInfoChannel()));
    }
    
    public boolean requiresFilteredEntriesListing() {
        return false;
    }
    
    public String getParametersSummary() {
        String ret = "";
        ret += "[-" + delay;
        ret +=  "; 0]";
        
        return ret;
    }
    
    public PublicClonable duplicate() {
        DeleteProcessor pro = new DeleteProcessor();
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
                EqualsHelper.equals(this.delay, other.delay);
        }
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, this.delay);
        return h;
    }
}
