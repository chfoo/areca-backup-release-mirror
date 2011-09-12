package com.application.areca.metadata.trace;

import java.io.File;
import java.io.IOException;

import com.application.areca.ArecaFileList;
import com.application.areca.context.ProcessContext;
import com.application.areca.metadata.MetadataConstants;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.metadata.FileMetaDataSerializationException;
import com.myJava.util.Util;
import com.myJava.util.taskmonitor.TaskCancelledException;

/**
 * Rebuilds directories and symlinks
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2011, Olivier PETRUCCI.

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
public class RebuildOtherFilesTraceHandler implements TraceHandler {
	
	private File directory;
	private ArecaFileList filters;
	private ArecaFileList normalizedFilters;

	public RebuildOtherFilesTraceHandler(File directory, ArecaFileList filters) {
		this.directory = directory;
		this.filters = filters;
		
		if (filters != null) {
			normalizedFilters = (ArecaFileList)this.filters.duplicate();
			normalizedFilters.normalize(directory);
		}
	}

	public void close() {
	}

	public void newRow(char type, String key, String trace, ProcessContext context) 
	throws IOException, FileMetaDataSerializationException, TaskCancelledException {
		if (context != null) {
			context.getTaskMonitor().checkTaskState();
		}
		
		if (type == MetadataConstants.T_DIR) {
			File dir = new File(directory, key);
			if (matchFilters(dir, normalizedFilters)) {
				if (! FileSystemManager.exists(dir)) {
					FileTool.getInstance().createDir(dir);
				}
			}
		} else if (type == MetadataConstants.T_SYMLINK) {
			if (filters == null || Util.passFilter(key, filters.asArray())) {
				File symLink = new File(directory, key);
				File parent = symLink.getParentFile();
				if (! FileSystemManager.exists(parent)) {
					FileTool.getInstance().createDir(parent);
				}
				FileSystemManager.createSymbolicLink(symLink, ArchiveTraceParser.extractSymLinkPathFromTrace(trace));
			}
		} else if (type == MetadataConstants.T_PIPE) {
			if (filters == null || Util.passFilter(key, filters.asArray())) {
				FileSystemManager.createNamedPipe(new File(directory, key));
			}
		}
	}

	public void setVersion(long version) {		
	}

	// To refactor / clean
	private boolean matchFilters(File dir, ArecaFileList normalizedFilters) {
		if (normalizedFilters == null) {
			return true;
		} else {
			String tested = FileSystemManager.getAbsolutePath(dir);
			for (int i=0; i<normalizedFilters.length(); i++) {
				if (tested.equals(normalizedFilters.get(i)) || tested.startsWith(normalizedFilters.get(i) + "/")) {
					return true;
				}
			}

			return false;
		}
	}
}
