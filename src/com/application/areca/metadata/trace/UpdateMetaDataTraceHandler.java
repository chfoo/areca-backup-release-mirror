package com.application.areca.metadata.trace;

import java.io.File;
import java.io.IOException;

import com.application.areca.context.ProcessContext;
import com.application.areca.metadata.FileList;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileList.FileListIterator;
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
public class UpdateMetaDataTraceHandler implements TraceHandler {
	private File destination;
	private FileList excludedFiles;
	private FileListIterator excludedFilesIterator;
	
	private long version;

	public File getDestination() {
		return destination;
	}

	public void setDestination(File destination) {
		this.destination = destination;
	}

	public FileList getExcludedFiles() {
		return excludedFiles;
	}

	public void setExcludedFiles(FileList excludedFiles) throws IOException {
		this.excludedFiles = excludedFiles;
		this.excludedFilesIterator = excludedFiles == null ? null : excludedFiles.iterator();
	}

	public void close() throws IOException {
		if (excludedFilesIterator != null) {
			excludedFilesIterator.close();
		}
	}

	public void newRow(char type, String key, String hash, ProcessContext context) 
	throws FileMetaDataSerializationException, TaskCancelledException, IOException {
		context.getTaskMonitor().checkTaskState();
		File target = new File(destination, key);
		
		if (excludedFilesIterator == null || (! excludedFilesIterator.fetch(FileSystemManager.getAbsolutePath(target)))) {
			if (FileSystemManager.exists(target)) {
				FileMetaData atts = ArchiveTraceParser.extractAttributesFromEntry(key, type, hash, version);
				
				if (atts == null) {
					Logger.defaultLogger().warn("Unable to retrieve metadata for '" + key + "'. This is probably because you are trying to read an archive that was created on a different operating system.");
				} else {
					try {
						FileSystemManager.applyMetaData(atts, target);
					} catch (IOException e) {
						Logger.defaultLogger().error("Unable to apply metadata.", e);
					}
				}
			}
		}
	}

	public void setVersion(long version) {
		this.version = version;
	}
}
