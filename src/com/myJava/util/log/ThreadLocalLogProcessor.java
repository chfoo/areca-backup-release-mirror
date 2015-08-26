package com.myJava.util.log;

import com.myJava.configuration.FrameworkConfiguration;

/**
 * Log processor that stores all error and warning messages which are emitted in the current thread and its children.
 * <BR>This processor is not active by default and must be activated explicitly by calling the "activateMessageTracking" method. 
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
public class ThreadLocalLogProcessor implements LogProcessor {
	private static final int THRESHOLD = FrameworkConfiguration.getInstance().getInlineLogLevel();
	
	private InheritableThreadLocal threadLocal = new InheritableThreadLocal() {
		protected Object initialValue() {
			return null;
		}
	};

	/**
	 * Register a new message container for the current thread.
	 * <BR>Caution : if a container was inherited from the parent thread, it will be overriden
	 */
	public LogMessagesContainer activateMessageTracking() {
		LogMessagesContainer ctn = new LogMessagesContainer(); 
		threadLocal.set(ctn);
		return ctn;
	}
	
	public LogMessagesContainer getMessageContainer() {
		return (LogMessagesContainer)threadLocal.get();
	}
	
	public boolean clearLog() {
		LogMessagesContainer ctn = getMessageContainer();
		if (ctn != null) {
			return ctn.clear();
		} else {
			return true;
		}
	}

	public void displayApplicationMessage(String messageKey, String title, String message) {
	}

	public void log(int level, String message, Throwable e, String source) {
		if (level <= THRESHOLD) {
			LogMessagesContainer ctn = getMessageContainer();
			if (ctn != null) {
				LogMessage msg = new LogMessage();
				msg.init(level, message, source, e);
				ctn.addLogMessage(msg);	
			}
		}
	}

	public void unmount() {	
	}
}
