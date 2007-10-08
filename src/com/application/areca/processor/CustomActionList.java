package com.application.areca.processor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.myJava.object.DuplicateHelper;
import com.myJava.object.PublicClonable;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
 * Collection of PostProcessors
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
public class CustomActionList implements PublicClonable {

    protected List actions = new ArrayList();
    protected boolean requiresFilteredEntriesListing = false;

    public PublicClonable duplicate() {
        CustomActionList other = new CustomActionList();
        other.actions = DuplicateHelper.duplicate(actions);
        other.requiresFilteredEntriesListing = requiresFilteredEntriesListing;
        return other;
    }
    
    private void updateReportRequired() {
        Iterator iter = this.actions.iterator();
        while (iter.hasNext()) {
            CustomAction processor = (CustomAction)iter.next();
            if (processor.requiresFilteredEntriesListing()) {
                requiresFilteredEntriesListing = true;
                return;
            }
        }
    }
    
    public boolean requiresFilteredEntriesListing() {
        return requiresFilteredEntriesListing;
    }
    
    public void addAction(CustomAction action) {
        this.actions.add(action);
        updateReportRequired();
    }

    /**
     * Calls the post processors 
     */
    public void run(ProcessContext context) throws ApplicationException {
        if (! this.isEmpty()) {
            double taskShare = 1.0 / (double)this.getSize();
            
	        Iterator iter = this.actions.iterator();
	        StringBuffer exceptions = new StringBuffer();
	        while (iter.hasNext()) {
	            context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(taskShare);
	            TaskMonitor itemMonitor = context.getTaskMonitor().getCurrentActiveSubTask();
	            CustomAction action = (CustomAction)iter.next();
	            try {
                    action.run(context);
	            } catch (Throwable e) {
	                Logger.defaultLogger().error("Error during processor.", e);
	                exceptions.append("\n").append(e.getMessage());
	            } finally {
	                itemMonitor.enforceCompletion();
	            }
	        }
	        
	        String errorMsg = exceptions.toString();
	        if (errorMsg.length() != 0) {
	            throw new ApplicationException("The following errors occured during processor : " + errorMsg);
	        }
        }
    }
    
    public Iterator iterator() {
        return this.actions.iterator();
    }
    
    public int getSize() {
        return this.actions.size();
    }
    
    public boolean isEmpty() {
        return this.actions.isEmpty();
    }
}
