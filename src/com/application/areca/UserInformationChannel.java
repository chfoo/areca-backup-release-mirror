package com.application.areca;

import com.application.areca.context.ProcessContext;
import com.myJava.util.taskmonitor.TaskMonitor;
import com.myJava.util.taskmonitor.TaskMonitorListener;

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
public interface UserInformationChannel extends TaskMonitorListener {
    public void print(String info);
    public void warn(String info);
    public void error(String info);
    public void updateCurrentTask(long taskIndex, long taskCount, String taskDescription);
    
    public void startRunning();
    public void stopRunning();
    
    public void setTaskMonitor(TaskMonitor tm);
    public TaskMonitor getTaskMonitor();
    public void setContext(ProcessContext context);
}
