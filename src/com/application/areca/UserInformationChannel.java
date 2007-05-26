package com.application.areca;

import com.myJava.util.taskmonitor.TaskMonitorListener;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4945525256658487980
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
public interface UserInformationChannel extends TaskMonitorListener {
    public void logInfo(String title, String info);
    public void logWarning(String title, String warning);
    public void logError(String title, String error, Throwable e);    
    public void updateCurrentTask(long taskIndex, long taskCount, String taskDescription);
    public void startRunning();
    public void stopRunning();
    public boolean isRunning(); 
    public void reset();    
    public void displayApplicationMessage(String messageKey, String title, String message);
}
