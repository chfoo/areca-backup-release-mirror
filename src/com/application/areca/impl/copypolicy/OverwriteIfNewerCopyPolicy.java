package com.application.areca.impl.copypolicy;

import java.io.File;
import java.io.IOException;

import com.application.areca.metadata.trace.ArchiveTraceAdapter;
import com.application.areca.metadata.trace.ArchiveTraceParser;
import com.application.areca.metadata.trace.TraceEntry;
import com.application.areca.metadata.trace.TraceFileIterator;
import com.myJava.file.FileSystemManager;
import com.myJava.file.metadata.FileMetaDataSerializationException;
import com.myJava.util.log.Logger;

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
public class OverwriteIfNewerCopyPolicy extends OverwriteCopyPolicy {
	private String root;
	private TraceFileIterator trace;
	private boolean initialized = false;

	public OverwriteIfNewerCopyPolicy(String root) {
		this.root = root;
	}
	
	private void reset() throws IOException {
		this.trace = ArchiveTraceAdapter.buildIterator(context.getTraceFile());	
		initialized = true;
	}
	
	protected boolean overrideExistingFile(File file) {
		return overrideExistingFile(file, 0);
	}

	protected boolean overrideExistingFile(File file, int iterationCtrl) {
		try {
			if (! initialized) {
				reset();
			}
			
			String key = FileSystemManager.getAbsolutePath(file).substring(root.length());
			
			// Locate the entry
			boolean found = trace.fetch(key);
			if (! found) {
				
				// Not found : probably because the trace iterator needs to be reset (new archive)
				if (iterationCtrl >= 1) {
					// Trace iterator already reset --> should not occur --> raise a warning
					Logger.defaultLogger().warn("Trying to recover an entry that should not be (current : " + file + " / " + root + ")");
					return false;
				} else {
					// Reset the trace iterator
					Logger.defaultLogger().fine("Resetting trace iterator (current : " + file + " / " + root + ")");
					reset();
					return overrideExistingFile(file, iterationCtrl + 1);
				}
			} else {
				// Entry found : check the last modification date
				TraceEntry entry = (TraceEntry)trace.currentEntry();
				long traceLastModified = ArchiveTraceParser.extractAttributesFromEntry(key, entry.getType(), entry.getData(), trace.getHeader().getVersion()).getLastmodified();
				long existingLastModified = FileSystemManager.lastModified(file);
				return (traceLastModified > existingLastModified);
			}
		} catch (IOException e) {
			Logger.defaultLogger().error("Error while attempting to retrieve trace informations for " + file + " / " + root, e);
			return false;
		} catch (FileMetaDataSerializationException e) {
			Logger.defaultLogger().error("Error while attempting to retrieve trace informations for " + file + " / " + root, e);
			return false;
		}
	}
}
