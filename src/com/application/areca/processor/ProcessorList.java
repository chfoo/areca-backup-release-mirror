package com.application.areca.processor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.myJava.object.Duplicable;
import com.myJava.object.DuplicateHelper;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
 * Collection of Pre/PostProcessors
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
public class ProcessorList implements Duplicable {

    protected List processors = new ArrayList();
    protected boolean forwardErrors = true;

    public Duplicable duplicate() {
        ProcessorList other = new ProcessorList();
        other.processors = DuplicateHelper.duplicate(processors);
        return other;
    }
    
    public void addProcessor(Processor processor) {
        this.processors.add(processor);
    }

    public boolean isForwardErrors() {
		return forwardErrors;
	}

	public void setForwardErrors(boolean forwardErrors) {
		this.forwardErrors = forwardErrors;
	}
	
	/**
	 * 
	 * @param action : see constants declared in the Processor interface
	 * @return
	 */
	public boolean requireStatistics(int action) {
        Iterator iter = this.processors.iterator();
        while (iter.hasNext()) {
            Processor processor = (Processor)iter.next();
            if (processor.shallRun(action) && processor.requireStatistics()) {
            	return true;
            }
        }
        return false;
	}

	/**
     * Calls the processors 
     */
    public void run(int action, ProcessContext context) throws ApplicationException {
        if (! this.isEmpty(action)) {
            double taskShare = 1 / (double)this.getSize(action);
            
	        Iterator iter = this.processors.iterator();
	        StringBuffer exceptions = new StringBuffer();
	        while (iter.hasNext()) {
                context.reset(true);
                
                Processor processor = (Processor)iter.next();
                
                if (processor.shallRun(action)) {
	                String key = processor.getKey() + " (" + processor.getParametersSummary() + ")";
	                context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(taskShare, processor.getClass().getName());
		            TaskMonitor itemMonitor = context.getTaskMonitor().getCurrentActiveSubTask();
		            try {
	                    processor.run(context);
		                context.getReport().getStatus().addItem(key);
		            } catch (Throwable e) {
		                Logger.defaultLogger().error("Error while executing " + key, e);
		                exceptions.append("\n").append(e.getMessage());
		                context.getReport().getStatus().addItem(key, e.getMessage());
		            } finally {
		                itemMonitor.enforceCompletion();
		            }
                }
	        }
	        
	        String errorMsg = exceptions.toString();
	        if (errorMsg.length() != 0 && forwardErrors) {
	            throw new ApplicationException("The following errors occurred while runnings processors : " + errorMsg);
	        }
        }
    }
    
    public Iterator iterator() {
        return this.processors.iterator();
    }
    
    public int getSize(int action) {
    	int size = 0;
        Iterator iter = this.processors.iterator();
        
        while (iter.hasNext()) {
            Processor processor = (Processor)iter.next();
            if (processor.shallRun(action)) {
            	size++;
            }
        }
        
        return size;
    }
    
    public boolean isEmpty(int action) {
        return this.getSize(action) == 0;
    }
}
