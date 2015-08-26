package com.myJava.util.log;

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
public class ConsoleLogProcessor 
implements LogProcessor {
	
	private boolean fullLog = true;
    
    public ConsoleLogProcessor(boolean fullLog) {
    	this.fullLog = fullLog;
    }

	public boolean isFullLog() {
		return fullLog;
	}

	public void setFullLog(boolean fullLog) {
		this.fullLog = fullLog;
	}

	public void log(int level, String message, Throwable e, String source) {
        // build log string
        String logCt = LogHelper.format(level, message, source, fullLog).toString();
        
        // write log
        System.out.println(logCt);
        if (e != null) {      
            System.out.println("");
            e.printStackTrace();
        }
    }

    public boolean clearLog() {
        return true;
    }

    public void displayApplicationMessage(String messageKey, String title, String message) {
        log(LogLevels.LOG_LEVEL_WARNING, message, null, title);
    }
    
	public void unmount() {
	}
}
