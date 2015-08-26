package com.application.areca.metadata.trace;

import java.io.IOException;

import com.application.areca.context.ProcessContext;
import com.myJava.file.iterator.FilePathComparator;
import com.myJava.file.metadata.FileMetaDataSerializationException;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;

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
public class CheckTraceHandler implements TraceHandler {
	private String previousEntryKey = null;

	public CheckTraceHandler() {
	}

	public void close() {
	}

	public void newRow(char type, String key, String trace, ProcessContext context) 
	throws IOException, FileMetaDataSerializationException, TaskCancelledException {
		if (key != null) {
			if (previousEntryKey != null) {
				if (FilePathComparator.instance().compare(key, previousEntryKey) <= 0) {
					Logger.defaultLogger().error("Error detected in trace file: invalid entry order. " + previousEntryKey + " / " + key);
					throw new IllegalStateException("Error detected while checking archive metadata.");
				}
			}
			
			previousEntryKey = key;
		}
	}

	public void setVersion(long version) {		
	}
}
