package com.myJava.util.taskmonitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.myJava.object.ToStringHelper;
import com.myJava.util.log.Logger;

/**
 * This task allows to monitor a task completion.
 * It also holds multiple listeners for event management.
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
public class TaskMonitor implements Serializable {
	private static final long serialVersionUID = -223768891867633062L;

	public static final long PAUSE_CHECK_DELAY_MS = 500;
	
	/**
	 * Completion rate of the task : between 0 and 1
	 */
    protected double currentCompletionRate;

    /**
     * Current subtask
     */
    protected TaskMonitor currentSubTask;
    
    /**
     * Part of the subtask in the global task completion (between 0 and 1)
     */
    protected double currentSubTaskShare;
    
    /**
     * Parent task (null if no parent task)
     */
    protected TaskMonitor parentTask;   
    
    /**
     * Task's listeners
     */
    protected transient List listeners; 
    
    /**
     * Tells whether a "pause" has been requested by the user
     */
    protected boolean pauseRequested = false;
    
    /**
     * Boolean telling whether the current tack can be canceled or not
     */
    protected boolean cancellable = true;
    
    /**
     * Tells whether a "cancel" has been requested by the user
     */
    protected boolean cancelRequested = false;
    
    /**
     * Task name
     */
    protected String name = "";
    
    private TaskMonitor() {
        this.currentCompletionRate = 0;
        this.currentSubTaskShare = 0;
        this.currentSubTask = null;
        this.parentTask = null; 
        this.clearAllListeners();  
    }

    public boolean isCancelRequested() {
		return cancelRequested;
	}

	public TaskMonitor(String name) {
    	this();
        this.name = name;
    }
    
    /**
     * Add a new listener
     * @param listener
     */
    public void addListener(TaskMonitorListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        } else {
            this.listeners.add(listener);
        }
    }
    
    /**
     * remove all listeners
     */
    public void clearAllListeners() {
        this.listeners = new ArrayList();
    }
    
    /**
     * Return the global completion rate of the task
     * <BR>Task completion rate + subtask share * subtask global completion rate
     * @return
     */
    public double getGlobalCompletionRate() {
        if (currentSubTask == null) {
            return this.currentCompletionRate;
        } else {
            return Math.min(1., this.currentCompletionRate + this.currentSubTaskShare * this.currentSubTask.getGlobalCompletionRate());
        }
    }
    
    public void overrideCurrentSubTask(TaskMonitor subTask) {
        this.currentSubTask = subTask;
        subTask.parentTask = this;
    }
    
    /**
     * Set the current sub task.
     * @param subTask
     * @param subTaskShare is the share of the subtask in the task's global completion (between 0 and 1)
     */
    public void setCurrentSubTask(TaskMonitor subTask, double subTaskShare) {
        if (subTask.getGlobalCompletionRate() > 0) {
            //throw new IllegalArgumentException("Illegal attempt to add a subTask which has already been started.");
        }
        
        overrideCurrentSubTask(subTask);
        this.currentSubTaskShare = subTaskShare;
    }
    
    public void addNewSubTask(double subTaskShare, String name) {
        this.setCurrentSubTask(new TaskMonitor(name), subTaskShare);
    }

    /**
     * Enforce the task's global completion rate.
     * It can be done only if the current sub task is completed  
     * @param completion
     */ 
    public void setCurrentCompletion(double completion) {
        
        // Do nothing if the completion rate remains unchanged. 
    	// Except if the completion is 0 : in this case, we enforce the rate's update
        if (this.currentCompletionRate == completion && completion != 0) {
            return;
        }
        
        if (this.currentSubTask != null) {
        	this.currentSubTask.setCurrentCompletion(1);
            //throw new IllegalArgumentException("Illegal attempt to enforce the current task's completion while a subtask is pending");
        }
        
        if (completion < this.currentCompletionRate) {
            //throw new IllegalArgumentException("Illegal Argument : the current completion rate is above the completion rate passed in argument");
        } else {
        	// Update completion rate
        	this.currentCompletionRate = completion;
        }
        // Raise event
        this.completionChanged();
    }
    
    public void addCompletion(double completionStep) {
        this.setCurrentCompletion(this.currentCompletionRate + completionStep);
    }
    
    public void setCurrentCompletion(long numerator, long denominator) {
        if (denominator == numerator) {
            this.setCurrentCompletion(1.); // "0/0" special case
        } else {
            this.setCurrentCompletion(((double)numerator) / ((double)denominator));
        }
    }    
    
    /**
     * Enforces the task's completion.
     * <BR>All subtasks are deleted.
     * <BR>Since no checks are made, it must be used VERY carefully.
     */
    public void enforceCompletion() {
        this.currentSubTask = null;
        this.setCurrentCompletion(1.0);
    }
    
    /**
     * Returns the really active subtask of the current task.
     * <BR>It is the last subtask which has been added in the subtask tree.
     * <BR>It is the only task whose completion rate can be set (by calling setCurrentCompletion)
     * @return
     */
    public TaskMonitor getCurrentActiveSubTask() {
        if (this.currentSubTask == null) {
            return this;
        } else {
            return this.currentSubTask.getCurrentActiveSubTask();
        }
    }
    
    /**
     * Call all listeners
     */
    protected void completionChanged() {
        if (this.currentSubTask != null && this.currentSubTask.getGlobalCompletionRate() >= 1.) {
            this.completeCurrentSubTask();
        }
        
        if (listeners != null) {
	        for (int i=0; i<this.listeners.size(); i++) {
	            ((TaskMonitorListener)listeners.get(i)).completionChanged(this);
	        }
        }
        
        if (this.parentTask != null) {
            this.parentTask.completionChanged();
        }
    }
    
    /**
     * Remove the current sub task and integrates its completion rate in the global task's completion rate
     */
    private void completeCurrentSubTask() {
        this.currentSubTask.clearAllListeners();
        this.currentSubTask.parentTask = null;
        this.currentSubTask = null;
        this.currentCompletionRate += currentSubTaskShare;
        this.currentSubTaskShare = 0;
    }    
    
    public synchronized boolean isCancellable() {
        return cancellable;
    }

	public boolean isPauseRequested() {
		return pauseRequested;
	}

	public void setPauseRequested(boolean pauseRequested) {
        synchronized(this) {
            this.pauseRequested = pauseRequested;
        }
        
        if (listeners != null) {
        for (int i=0; i<this.listeners.size(); i++) {
            ((TaskMonitorListener)listeners.get(i)).pauseRequested(this);
        }
        }
	}

	public void setCancelRequested() {
        if (cancellable) {
            synchronized(this) {
                this.cancelRequested = true;
                this.setCancellable(false);
                if (this.pauseRequested) {
                	this.setPauseRequested(false);
                }
            }
            
            if (listeners != null) {
            for (int i=0; i<this.listeners.size(); i++) {
                ((TaskMonitorListener)listeners.get(i)).cancelRequested(this);
            }
            }
        }
    }
    
    public void resetCancellationState() {
        this.cancelRequested = false;
        this.setCancellable(true);
    }
    
    public void setCancellable(boolean cancellable) {
        synchronized(this) {
            this.cancellable = cancellable;
        }
        
        if (listeners != null) {
        for (int i=0; i<this.listeners.size(); i++) {
            ((TaskMonitorListener)listeners.get(i)).cancellableChanged(this);
        }
        }
    }
    
    /**
     * Checks whether the task has been canceled or paused.
     * @throws TaskCancelledException
     */
    public void checkTaskState() throws TaskCancelledException {
        if (this.cancelRequested) {
            throw new TaskCancelledException("The [" + this.name + "] task has been cancelled.");
        }
        
    	try {
    		if (pauseRequested) {
    			Logger.defaultLogger().info("The [" + this.name + "] task has been paused.");
    			while (this.pauseRequested) {
    				Thread.sleep(PAUSE_CHECK_DELAY_MS);
    			}
    			Logger.defaultLogger().info("The [" + this.name + "] task has been resumed.");
    		}
		} catch (InterruptedException e) {
			Logger.defaultLogger().error("Interrupted", e);
		}
    }

    public String toOldString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("Name", this.name, sb);
        ToStringHelper.append("Child", this.currentSubTask, sb);
        ToStringHelper.append("ChildShare", this.currentSubTaskShare, sb);
        ToStringHelper.append("Completion", this.currentCompletionRate, sb);
        return ToStringHelper.close(sb);
    }
    
    public String toString() {
    	return toStringTree("", "");
    }
    
    private String toStringTree(String h1, String h2) {
    	String tab = "    ";
    	String ret = h1 + this.name + " : " + this.currentCompletionRate;
    	if (this.currentSubTask != null) {
    		String sh1 = h2 + this.currentSubTaskShare + tab;
    		String sh2 = h2 + tab + tab;
    		ret += "\n" + currentSubTask.toStringTree(sh1, sh2); 
    	}
    	return ret;
    }
}