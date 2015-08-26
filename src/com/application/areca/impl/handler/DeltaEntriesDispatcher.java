package com.application.areca.impl.handler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.impl.IncrementalZipMedium;
import com.application.areca.metadata.content.ArchiveContentAdapter;
import com.application.areca.metadata.content.ArchiveContentManager;
import com.application.areca.metadata.content.ContentFileIterator;
import com.application.areca.metadata.trace.ArchiveTraceAdapter;
import com.application.areca.metadata.trace.ArchiveTraceManager;
import com.application.areca.metadata.trace.TraceFileIterator;

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
public class DeltaEntriesDispatcher extends AbstractEntriesDispatcher {
	protected boolean initialized = false;
	protected ContentFileIterator[] citers;
	protected TraceFileIterator[] titers;
	protected boolean rangeDispatch;

	public DeltaEntriesDispatcher(File[] archives, AbstractIncrementalFileSystemMedium medium) {
		super(archives, medium);

		// Build content iterators
		citers = new ContentFileIterator[archives.length];
		titers = new TraceFileIterator[archives.length];

		// - ZipMedium : All copies have to be explicitly listed in the files to recover (they will be handled by the "ensurelocalcopy" method)
		// - DirectoryMedium : Only the first instance has to be referenced : all further copies will be found automatically
		rangeDispatch = (medium instanceof IncrementalZipMedium);
	}

	private void initialize() throws IOException {
		if (! initialized) {
			initialized = true;

			for (int i=0; i<archives.length; i++) {
				citers[i] = ArchiveContentAdapter.buildIterator(ArchiveContentManager.resolveContentFileForArchive(this.medium, archives[i]));
				titers[i] = ArchiveTraceAdapter.buildIterator(ArchiveTraceManager.resolveTraceFileForArchive(this.medium, archives[i]));
			}
		}
	}

	public void dispatchEntry(String entry) throws IOException {
		initialize();

		List indexes = new ArrayList();
		for (int i=archives.length-1; i>=0; i--) {
			boolean found = titers[i].fetch(entry);
			if ((! found) && (!indexes.isEmpty())) {
				// Found in previously processed archives but not found in trace anymore -> stop searching
				break;
			}

			found = citers[i].fetch(entry);
			if (found) {
				indexes.add(new Integer(i));
			}
		}

		// if indexes is empty, the file will not be recovered.
		// this can happen during archive merges (partial recoveries)
		if (! indexes.isEmpty()) {
			this.incrementEntries();

			if (rangeDispatch) {
				Iterator iter = indexes.iterator();
				while (iter.hasNext()) {
					int index = ((Integer)iter.next()).intValue();
					result.add(archives[index], entry);
				}
			} else {
				result.add(archives[((Integer)indexes.get(indexes.size() - 1)).intValue()], entry);
			}
		}
	}

	public void close() throws IOException {
		for (int i=0; i<archives.length; i++) {
			if (citers[i] != null) {
				citers[i].close();
			}
			if (titers[i] != null) {
				titers[i].close();
			}
		}
	}
}
