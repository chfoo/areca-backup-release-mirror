package com.application.areca.metadata.trace;

import java.io.File;
import java.io.IOException;

import com.application.areca.context.ProcessContext;
import com.application.areca.metadata.MetadataConstants;
import com.myJava.file.FileSystemManager;
import com.myJava.file.metadata.FileMetaData;
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
 Copyright 2005-2010, Olivier PETRUCCI.

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
public class UpdateMetaDataTraceHandler implements TraceHandler {
	private File destination;
	private long version;

	public File getDestination() {
		return destination;
	}

	public void setDestination(File destination) {
		this.destination = destination;
	}

	public void close() {
	}

	public void newRow(char type, String key, String hash, ProcessContext context) 
	throws FileMetaDataSerializationException, TaskCancelledException {
		context.getTaskMonitor().checkTaskState();
		
		File target = new File(destination, key);
		if (FileSystemManager.exists(target)) {
			FileMetaData atts;
			if (type == MetadataConstants.T_DIR) {
				// Directory
				atts = ArchiveTraceParser.extractDirectoryAttributesFromTrace(hash, version);
			} else if (type == MetadataConstants.T_FILE) {
				// File
				atts = ArchiveTraceParser.extractFileAttributesFromTrace(hash, version);
			} else if (type == MetadataConstants.T_SYMLINK) {
				// Symlink
				atts = ArchiveTraceParser.extractSymLinkAttributesFromTrace(hash, version);
			} else if (type == MetadataConstants.T_PIPE) {
				// Pipe
				atts = ArchiveTraceParser.extractPipeAttributesFromTrace(hash, version);
			} else {
				throw new FileMetaDataSerializationException("Unsupported type for " + key + " : " + type + " / " + hash);
			}
			
			if (atts != null) {
				try {
					FileSystemManager.applyMetaData(atts, target);
				} catch (IOException e) {
					Logger.defaultLogger().error("Unable to apply metadata.", e);
				}
			}
		}
	}

	public void setVersion(long version) {
		this.version = version;
	}
}
