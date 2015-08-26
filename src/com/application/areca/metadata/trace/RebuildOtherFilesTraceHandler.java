package com.application.areca.metadata.trace;

import java.io.File;
import java.io.IOException;

import com.application.areca.ArecaRawFileList;
import com.application.areca.context.ProcessContext;
import com.application.areca.metadata.MetadataConstants;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.copypolicy.CopyPolicy;
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
public class RebuildOtherFilesTraceHandler implements TraceHandler {

	private File directory;
	private ArecaRawFileList filters;
	private CopyPolicy policy;
	private ArecaRawFileList normalizedFilters;

	public RebuildOtherFilesTraceHandler(File directory, ArecaRawFileList filters, CopyPolicy policy) {
		this.directory = directory;
		this.filters = filters;
		this.policy = policy;

		if (filters != null) {
			normalizedFilters = (ArecaRawFileList)this.filters.duplicate();
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
		File target = new File(directory, key);

		if (type == MetadataConstants.T_DIR) {
			if (matchFilters(target, normalizedFilters)) {
				if (! FileSystemManager.exists(target)) {
					if (policy.accept(target)) {
						FileTool.getInstance().createDir(target);
					}
				}
			}
		} else if (type == MetadataConstants.T_SYMLINK) {
			if (filters == null || Util.passFilter(key, filters.asArray())) {
				if (policy.accept(target)) {
					File parent = target.getParentFile();
					if (! FileSystemManager.exists(parent)) {
						FileTool.getInstance().createDir(parent);
					}
					FileSystemManager.createSymbolicLink(target, ArchiveTraceParser.extractSymLinkPathFromTrace(trace));
				}
			}
		} else if (type == MetadataConstants.T_PIPE) {
			if (filters == null || Util.passFilter(key, filters.asArray())) {
				if (policy.accept(target)) {
					FileSystemManager.createNamedPipe(target);
				}
			}
		}
	}

	public void setVersion(long version) {		
	}

	// To refactor / clean
	private boolean matchFilters(File dir, ArecaRawFileList normalizedFilters) {
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
