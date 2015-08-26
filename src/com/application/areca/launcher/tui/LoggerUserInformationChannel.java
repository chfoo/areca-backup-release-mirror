package com.application.areca.launcher.tui;

import com.application.areca.UserInformationChannel;
import com.application.areca.launcher.AbstractInformationChannel;
import com.myJava.util.log.ConsoleLogProcessor;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
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
public class LoggerUserInformationChannel
extends AbstractInformationChannel
implements UserInformationChannel {
    protected boolean running;
    protected int previousProgress = -1;
    protected boolean displayThreadName = false;
    
    private Logger logger;  
    
    public LoggerUserInformationChannel(boolean displayThreadName) {
        this.displayThreadName = displayThreadName;
        this.logger = new Logger();
    	((ConsoleLogProcessor)this.logger.find(ConsoleLogProcessor.class)).setFullLog(false);
    }

    public void print(String info) {
        if (displayThreadName) {
            logger.info(info, Thread.currentThread().getName());
            Logger.defaultLogger().info(info, Thread.currentThread().getName());
        } else {
            logger.info(info);
            Logger.defaultLogger().info(info);
        }
    }

    public void warn(String info) {
        if (displayThreadName) {
            logger.warn(info, Thread.currentThread().getName());
            Logger.defaultLogger().warn(info, Thread.currentThread().getName());
        } else {
            logger.warn(info);
            Logger.defaultLogger().warn(info);
        }
	}
    
    public void error(String info) {
        if (displayThreadName) {
            logger.error(info, Thread.currentThread().getName());
            Logger.defaultLogger().error(info, Thread.currentThread().getName());
        } else {
            logger.error(info);
            Logger.defaultLogger().error(info);
        }
	}

	public void updateProgress(double percent) {
        int pc = (int)(0.1*percent);
        if (pc != previousProgress) {
            previousProgress = pc;
            String info = "" + (10*pc) + "%";
            if (displayThreadName) {
                logger.info(info, Thread.currentThread().getName());
                Logger.defaultLogger().info(info, Thread.currentThread().getName());
            } else {
                logger.info(info);
                Logger.defaultLogger().info(info);
            }
        }
    }
    
    public void startRunning() {
        running = true;
    }
    
    public void stopRunning() {
        running = false;
    }
    
    public void reset() {
        // does nothing        
    }
    
    public void updateCurrentTask(long taskindex, long taskCount, String taskDescription) {
        if (taskCount != 0) {
            if (displayThreadName) {
                logger.info(taskDescription, Thread.currentThread().getName());
                Logger.defaultLogger().info(taskDescription, Thread.currentThread().getName());
            } else {
                logger.info(taskDescription);
                Logger.defaultLogger().info(taskDescription);
            }
        }
    }
    
	public void pauseRequested(TaskMonitor task) {
		// does nothing
	}

	public void completionChanged(TaskMonitor task) {
        try {
			this.updateProgress(100 * task.getGlobalCompletionRate());
		} catch (Exception e) {
			// Non blocking error - just log it
			Logger.defaultLogger().error(e);
		}
    }
    
    public void cancellableChanged(TaskMonitor task) {
        // does nothing
    }
    
    public void cancelRequested(TaskMonitor task) {
        // does nothing
    }
}
