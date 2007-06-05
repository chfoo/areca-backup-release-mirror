package com.application.areca.launcher.tui;

import com.application.areca.UserInformationChannel;
import com.application.areca.launcher.AbstractInformationChannel;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -6307890396762748969
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
public class LoggerUserInformationChannel
extends AbstractInformationChannel
implements UserInformationChannel {
    
    protected Logger userLogger = new Logger();
    protected boolean running;
    protected int previousProgress = -1;
    
    public void logInfo(String title, String info) {
        this.userLogger.info(info, title);
    }

    public void logWarning(String title, String warning) {
        this.userLogger.warn(warning, title);
    }
    
    public void logError(String title, String error, Throwable e) {
        this.userLogger.error(error, e, title);
    }
    
    public void updateProgress(double percent) {
        int pc = (int)(0.1*percent);
        if (pc != previousProgress) {
            previousProgress = pc;
            userLogger.info("" + (10*pc) + "%");
        }
    }
    
    public boolean isRunning() {
        return running;
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
            this.userLogger.info(taskDescription);
        }
    }

    public void completionChanged(TaskMonitor task) {
        this.updateProgress(100 * task.getGlobalCompletionRate());
    }
    
    public void cancellableChanged(TaskMonitor task) {
        // does nothing
    }
    
    public void cancelRequested(TaskMonitor task) {
        // does nothing
    }
    
    public void displayApplicationMessage(String messageKey, String title, String message) {
        if (! hasMessageBeenDisplayed(messageKey)) {
            userLogger.warn(Launcher.SEPARATOR);        
            userLogger.warn(title);
            userLogger.warn(message);
            userLogger.warn(Launcher.SEPARATOR);   
	        
	        registerMessage(messageKey);
        }
    }
}
