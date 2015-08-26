package com.myJava.util.log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import com.myJava.configuration.FrameworkConfiguration;

/**
 * 
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
public class LogMessagesContainer implements Serializable {
	private static final long serialVersionUID = 2548990974492892206L;
	private static int MAX_SIZE = FrameworkConfiguration.getInstance().getMaxInlineLogMessages();

	private ArrayList content = new ArrayList();
	private boolean maxSizeReached = false;
	private boolean hasWarnings = false;
	private boolean hasErrors = false;
	
	public void addLogMessage(LogMessage message) {
		if (message.getLevel() <= LogLevels.LOG_LEVEL_ERROR) {
			hasErrors = true;
		} else if (message.getLevel() <= LogLevels.LOG_LEVEL_WARNING) {
			hasWarnings = true;
		}
		
		if (! maxSizeReached && content.size() < MAX_SIZE) {
			content.add(message);
		} else {
			maxSizeReached = true;
		}
	}
	
	public Iterator iterator() {
		return content.iterator();
	}
	
	public boolean clear() {
		content.clear();
		maxSizeReached = false;
		return true;
	}
	
	public boolean isEmpty() {
		return content.isEmpty();
	}

	public boolean isMaxSizeReached() {
		return maxSizeReached;
	}
	
	public boolean hasWarnings() {
		return hasWarnings;
	}
	
	public boolean hasErrors() {
		return hasErrors;
	}
}
