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
 * Collection of PostProcessors
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
public class ProcessorList implements Duplicable {

    protected List processors = new ArrayList();

    public Duplicable duplicate() {
        ProcessorList other = new ProcessorList();
        other.processors = DuplicateHelper.duplicate(processors);
        return other;
    }
    
    public void addProcessor(Processor processor) {
        this.processors.add(processor);
    }

    /**
     * Calls the post processors 
     */
    public void run(ProcessContext context) throws ApplicationException {
        if (! this.isEmpty()) {
            double taskShare = 1 / (double)this.getSize();
            
	        Iterator iter = this.processors.iterator();
	        StringBuffer exceptions = new StringBuffer();
	        while (iter.hasNext()) {
                context.reset(true);
                
                Processor processor = (Processor)iter.next();
                String key = processor.getKey() + " (" + processor.getParametersSummary() + ")";
                context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(taskShare, processor.getClass().getName());
	            TaskMonitor itemMonitor = context.getTaskMonitor().getCurrentActiveSubTask();
	            try {
                    processor.run(context);
	                context.getReport().getStatus().addItem(key);
	            } catch (Throwable e) {
	                Logger.defaultLogger().error("Error during processor.", e);
	                exceptions.append("\n").append(e.getMessage());
	                context.getReport().getStatus().addItem(key, e.getMessage());
	            } finally {
	                itemMonitor.enforceCompletion();
	            }
	        }
	        
	        String errorMsg = exceptions.toString();
	        if (errorMsg.length() != 0) {
	            throw new ApplicationException("The following errors occurred during processor : " + errorMsg);
	        }
        }
    }
    
    public Iterator iterator() {
        return this.processors.iterator();
    }
    
    public int getSize() {
        return this.processors.size();
    }
    
    public boolean isEmpty() {
        return this.processors.isEmpty();
    }
}
