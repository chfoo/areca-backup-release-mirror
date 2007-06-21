package com.application.areca.postprocess;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.myJava.util.DuplicateHelper;
import com.myJava.util.PublicClonable;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
 * Collection of PostProcessors
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
public class PostProcessorList implements PublicClonable {

    protected List postProcessors = new ArrayList();
    protected boolean shallGenerateReport = false;

    public PublicClonable duplicate() {
        PostProcessorList other = new PostProcessorList();
        other.postProcessors = DuplicateHelper.duplicate(postProcessors);
        other.shallGenerateReport = shallGenerateReport;
        return other;
    }
    
    private void updateReportRequired() {
        Iterator iter = this.postProcessors.iterator();
        while (iter.hasNext()) {
            PostProcessor processor = (PostProcessor)iter.next();
            if (processor.requiresProcessReport()) {
                shallGenerateReport = true;
                return;
            }
        }
    }
    
    public boolean requiresProcessReport() {
        return shallGenerateReport;
    }
    
    public void addPostProcessor(PostProcessor postProcessor) {
        this.postProcessors.add(postProcessor);
        updateReportRequired();
    }

    /**
     * Calls the post processors 
     */
    public void postProcess(ProcessContext context) throws ApplicationException {
        if (! this.isEmpty()) {
            double taskShare = 1.0 / (double)this.getSize();
            
	        Iterator iter = this.postProcessors.iterator();
	        StringBuffer exceptions = new StringBuffer();
	        while (iter.hasNext()) {
	            context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(taskShare);
	            TaskMonitor itemMonitor = context.getTaskMonitor().getCurrentActiveSubTask();
	            PostProcessor processor = (PostProcessor)iter.next();
	            try {
	                processor.postProcess(context);
	            } catch (Throwable e) {
	                Logger.defaultLogger().error("Error during post-processing.", e);
	                exceptions.append("\n").append(e.getMessage());
	            } finally {
	                itemMonitor.enforceCompletion();
	            }
	        }
	        
	        String errorMsg = exceptions.toString();
	        if (errorMsg.length() != 0) {
	            throw new ApplicationException("The following errors occured during the post-processing : " + errorMsg);
	        }
        }
    }
    
    public Iterator iterator() {
        return this.postProcessors.iterator();
    }
    
    public int getSize() {
        return this.postProcessors.size();
    }
    
    public boolean isEmpty() {
        return this.postProcessors.isEmpty();
    }
}
