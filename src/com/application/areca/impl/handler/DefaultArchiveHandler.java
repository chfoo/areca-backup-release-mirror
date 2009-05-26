package com.application.areca.impl.handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.application.areca.impl.FileSystemRecoveryEntry;
import com.application.areca.impl.tools.RecoveryFilterMap;
import com.application.areca.metadata.content.ArchiveContentAdapter;
import com.application.areca.metadata.content.ArchiveContentManager;
import com.application.areca.metadata.content.ContentFileIterator;
import com.myJava.file.FileFilterList;
import com.myJava.file.FileTool;
import com.myJava.object.Duplicable;
import com.myJava.util.taskmonitor.TaskCancelledException;

/**
 * Default handler : handles standard files.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

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
public class DefaultArchiveHandler
extends AbstractArchiveHandler {

	public void store(FileSystemRecoveryEntry entry, InputStream in, OutputStream out, ProcessContext context)
	throws ApplicationException, IOException, TaskCancelledException {    
		FileTool.getInstance().copy(in, out, true, false, context.getTaskMonitor());
	}

	public void recoverRawData(
			File[] archivesToRecover, 
			RecoveryFilterMap filtersByArchive, 
			short mode,
			ProcessContext context
	) throws IOException, ApplicationException, TaskCancelledException {
		// Simply recover the files
		medium.ensureLocalCopy(
				archivesToRecover, 
				true, 
				context.getRecoveryDestination(), 
				filtersByArchive, 
				context
		);
	}

	public void archiveDeleted(File archive) 
	throws IOException {
	}

	public void close(ProcessContext context) 
	throws IOException {
	}

	public void init(ProcessContext context) 
	throws IOException {
	}

	public Duplicable duplicate() {
		return new DefaultArchiveHandler();
	}

	public RecoveryFilterMap dispatchEntries(File[] archives, String[] entriesToRecover) 
	throws ApplicationException, IOException {
		RecoveryFilterMap entriesByArchive = new RecoveryFilterMap(true);

		// Build content iterators
		ContentFileIterator[] iters = new ContentFileIterator[archives.length];

		try {
			for (int i=0; i<archives.length; i++) {
				ArchiveContentAdapter adapter = new ArchiveContentAdapter(ArchiveContentManager.resolveContentFileForArchive(this.medium, archives[i]));
				iters[i] = adapter.buildIterator();
			}

			// Build a list of entries to recover indexed by archive
			for (int e=0; e<entriesToRecover.length; e++) {
				int index = -1;
				for (int i=archives.length-1; i>=0; i--) {
					boolean found = iters[i].fetchUntil(entriesToRecover[e]);
					if (found) {
						index = i;
						break;
					}
				}
				
				if (index == -1) {
					// This can happen if the user tries to recover a symbolic link
					// throw new IllegalStateException(entriesToRecover[e] + " was not found in ANY archive.");
				} else {
					FileFilterList entries = (FileFilterList)entriesByArchive.get(archives[index]);
					if (entries == null) {
						entries = new FileFilterList();
						entriesByArchive.put(archives[index], entries);
					}
					entries.add(entriesToRecover[e]);
				}
			}
		} finally {
			for (int i=0; i<archives.length; i++) {
				if (iters[i] != null) {
					iters[i].close();
				}
			}
		}

		return entriesByArchive;
	}

	public boolean supportsImageBackup() {
		return true;
	}

	public File getContentFile(File archive) {
		return null;
	}

	public boolean autonomousArchives() {
		return true;
	}
}
