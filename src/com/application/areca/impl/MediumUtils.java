package com.application.areca.impl;

import java.io.File;
import java.io.IOException;

import com.application.areca.Utils;
import com.application.areca.metadata.trace.ArchiveTraceAdapter;
import com.application.areca.metadata.trace.TraceEntry;
import com.application.areca.metadata.trace.TraceFileIterator;
import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemManager;
import com.myJava.file.iterator.FilePathComparator;
import com.myJava.file.iterator.FileSystemIterator;
import com.myJava.file.metadata.FileMetaDataSerializationException;
import com.myJava.util.taskmonitor.TaskCancelledException;

/**
 * Utility class for ArchiveMedium implementations
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
public class MediumUtils {
	/**
	 * Delete unwanted files (ie files that have been recovered but that do not appear in the trace file)
	 */
	protected static void cleanObsoleteFiles(
			File targetFile, 
			File traceFile,
			boolean cancelSensitive
	) throws IOException, TaskCancelledException, FileMetaDataSerializationException {   
		FileSystemIterator targetIterator = new FileSystemIterator(targetFile, false, true, true, true);
		TraceFileIterator traceIterator = ArchiveTraceAdapter.buildIterator(traceFile);

		try {
			FilePathComparator comparator = new FilePathComparator();

			File toCheck = fetchNextFile(targetIterator);				// Ignore the recovery root
			toCheck = fetchNextFile(targetIterator);
			TraceEntry entry = fetchNextTraceEntry(traceIterator);
			while (true) {	
				if (toCheck == null) {
					break;
				}
				String shortPath = Utils.extractShortFilePath(toCheck, targetFile);

				// Compare the file paths
				int result = entry == null ? -1 : comparator.compare(shortPath, entry.getKey());

				if (result == 0) {
					// Found among recovered files and in trace -> ok
					toCheck = fetchNextFile(targetIterator);
					entry = fetchNextTraceEntry(traceIterator);
				} else if (result < 0) {
					// File found in recovered files but not found in trace -> destroy it
					deleteRecur(targetFile, toCheck);
					toCheck = fetchNextFile(targetIterator);
				} else {
					// File found in trace but not among recovered files -> ignore it
					entry = fetchNextTraceEntry(traceIterator);
				}
			}
		} finally {
			if (traceIterator != null) {
				traceIterator.close();
			}
		}
	}
	
	private static File fetchNextFile(FileSystemIterator iter) {
		if (iter.hasNext()) {
			return iter.nextFile();
		} else {
			return null;
		}
	}

	private static TraceEntry fetchNextTraceEntry(TraceFileIterator iter) throws IOException {
		if (iter.hasNext()) {
			return iter.next();
		} else {
			return null;
		}
	} 
	
	/**
	 * Try to delete the file and its parent(s)
	 */
	private static void deleteRecur(File root, File current) {
		if (FileSystemManager.delete(current)) {
			File parent = FileSystemManager.getParentFile(current);
			if (FileNameUtil.normalizePath(FileSystemManager.getAbsolutePath(parent)).startsWith(FileNameUtil.normalizePath(FileSystemManager.getAbsolutePath(root)))) {
				String[] children = FileSystemManager.list(parent);
				if (children == null || children.length == 0) { // The parent will be deleted only if it is empty
					deleteRecur(root, parent); 
				}
			}
		}
	} 
}
