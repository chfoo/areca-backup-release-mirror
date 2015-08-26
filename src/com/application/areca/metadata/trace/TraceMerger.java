package com.application.areca.metadata.trace;

import java.io.File;
import java.io.IOException;

import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.impl.FileSystemTarget;
import com.application.areca.metadata.AbstractMetaDataFileIterator;
import com.application.areca.metadata.MetadataConstants;
import com.myJava.file.FileTool;
import com.myJava.file.iterator.FilePathComparator;
import com.myJava.file.metadata.FileMetaDataSerializationException;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;

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
public class TraceMerger {
	private ArchiveTraceAdapter writer = null;
	private File[] archives;
	private AbstractIncrementalFileSystemMedium medium;
	private AbstractMetaDataFileIterator referenceIterator;

	/**
	 * Aggregates the traces provided as argument
	 */
	public static File buildAggregatedTraceFile(AbstractIncrementalFileSystemMedium medium, File[] archives) 
	throws IOException, TaskCancelledException, FileMetaDataSerializationException {
		return buildAggregatedTraceFile(medium, archives, null);
	}

	/**
	 * Aggregates the traces provided as argument
	 */
	public static File buildAggregatedTraceFile(
			AbstractIncrementalFileSystemMedium medium, 
			File[] archives, 
			AbstractMetaDataFileIterator referenceIterator) 
	throws IOException, TaskCancelledException, FileMetaDataSerializationException {
		Logger.defaultLogger().info("Building aggregated archive trace ...");

		File tmpFile = FileTool.getInstance().generateNewWorkingFile(null, "areca", "mtrc", true);
		ArchiveTraceAdapter writer = new ArchiveTraceAdapter(tmpFile, ((FileSystemTarget)medium.getTarget()).getSourceDirectory(), false);
		TraceMerger merger = new TraceMerger(medium, writer, archives, referenceIterator);
		merger.merge();
		
		// Make sure that the trace file was written in the right order. 
		// This step should not be necessary, but some users reported entries to appear in the wrong order in merged archives
		TraceMerger.checkTrace(tmpFile);

		Logger.defaultLogger().info("Aggregated archive trace built.");
		return tmpFile;
	}
	
	// Checks the merged trace for consistency
	public static void checkTrace(File file) throws TaskCancelledException, FileMetaDataSerializationException, IOException {
		Logger.defaultLogger().fine("Checking trace file consistency: " + file);
		CheckTraceHandler handler = new CheckTraceHandler();
		ArchiveTraceAdapter.traverseTraceFile(handler, file, null);
	}

	private TraceMerger(AbstractIncrementalFileSystemMedium medium, ArchiveTraceAdapter writer, File[] archives, AbstractMetaDataFileIterator referenceIterator) {
		this.writer = writer;
		this.archives = archives;
		this.medium = medium;
		this.referenceIterator = referenceIterator;
	}

	public ArchiveTraceAdapter getWriter() {
		return writer;
	}

	public File[] getArchives() {
		return archives;
	}

	public AbstractIncrementalFileSystemMedium getMedium() {
		return medium;
	}

	public void merge() throws IOException {
		TraceFileIterator[] iters = new TraceFileIterator[archives.length];
		try {
			// Build iterators
			for (int i=0; i<iters.length; i++) {
				File traceFile = ArchiveTraceManager.resolveTraceFileForArchive(medium, archives[i]);
				iters[i] = ArchiveTraceAdapter.buildIterator(traceFile);
			}

			String previousKey = null;
			while (true) {
				// Look for minimum key
				int minIndex = -1;
				for (int i=iters.length-1; i>=0; i--) {
					if (iters[i].current() != null) {
						int result = minIndex == -1 ? -1 : FilePathComparator.instance().compare(iters[i].current().getKey(), iters[minIndex].current().getKey());
						if (result < 0) {
							minIndex = i;
						}
					}
				}

				// Nothing more to read
				if (minIndex == -1) {
					break;
				}

				TraceEntry current = iters[minIndex].current();

				// Once the key has been found, compare it to the previous entry
				int result = previousKey == null ? -1 : FilePathComparator.instance().compare(previousKey, current.getKey());
				if (result == 0) {
					// already written -> do nothing
				} else if (result < 0) {
					// minKey > previousEntry -> new entry -> write it
					String rawData;
					if (referenceIterator == null) {
						rawData = current.getData(); // No provided reference iterator -> keep data unchanged 
					} else {
						boolean foundInReference = referenceIterator.fetch(current.getKey());
						if (current.getType() == MetadataConstants.T_FILE) {
							rawData = (foundInReference ? "1":"0") + ArchiveTraceParser.extractFileSizeFromTrace(current.getData());
						} else if (current.getType() == MetadataConstants.T_SYMLINK) {
							rawData = ArchiveTraceParser.extractSymLinkFileFromTrace(current.getData()) ? "1":"0";
						} else {
							rawData = "";
						}
					}

					previousKey = current.getKey();
					writer.writeEntry(current.getType(), current.getKey(), rawData);
				} else {
					// minKey < previousEntry -> error !
					throw new IllegalStateException("" + current.getKey() + "<" + previousKey);
				}

				// Fetch next entry
				iters[minIndex].next();
			}
		} finally {
			try {
				writer.close();
			} finally {
				try {
					if (referenceIterator != null) {
						referenceIterator.close();
					}
				} finally {
					for (int i=iters.length-1; i>=0; i--) {
						if (iters[i] != null) {
							iters[i].close();
						}
					}
				}
			}
		}
	}
}
