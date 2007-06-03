package com.application.areca.postprocess;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.myJava.util.EqualsHelper;
import com.myJava.util.HashHelper;
import com.myJava.util.PublicClonable;

/**
 * Merge archives
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2162742295696737000
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
public class MergePostProcessor extends AbstractPostProcessor {

    private int delay = 0;

    /**
     * @param target
     */
    public MergePostProcessor() {
        super();
    }

    public int getDelay() {
        return delay;
    }
    
    public void setDelay(int delay) {
        this.delay = delay;
    }
    
    public void run(ProcessContext context) throws ApplicationException {
        AbstractRecoveryTarget target = context.getReport().getTarget();
        target.getProcess().processCompactOnTargetImpl(target, delay, new ProcessContext(target));
    }
    
    public boolean requiresProcessReport() {
        return true;
    }
    
    public String getParametersSummary() {
        return "" + delay;
    }
    
    public PublicClonable duplicate() {
        MergePostProcessor pro = new MergePostProcessor();
        pro.delay = this.delay;
        return pro;
    }

    public void validate() throws PostProcessorValidationException {
        if (delay <= 0) {
            throw new PostProcessorValidationException("The merge delay must be above 0");
        }
    }
    
    public boolean equals(Object obj) {
        if (obj == null || (! (obj instanceof MergePostProcessor)) ) {
            return false;
        } else {
            MergePostProcessor other = (MergePostProcessor)obj;
            return EqualsHelper.equals(this.delay, other.delay);
        }
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, this.delay);
        return h;
    }
}
