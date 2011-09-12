package com.application.areca.impl.copypolicy;

import java.io.File;
import java.io.IOException;

import com.application.areca.metadata.trace.ArchiveTraceAdapter;
import com.application.areca.metadata.trace.TraceFileIterator;
import com.myJava.file.FileSystemManager;
import com.myJava.file.copypolicy.CopyPolicy;
import com.myJava.file.copypolicy.CopyPolicyException;
import com.myJava.util.log.Logger;

/**
 * Caution : this implementation is stateful !!
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
public class TraceFileFilter implements CopyPolicy {
	private File referenceTrace;
	private String root;
	private TraceFileIterator iterator;
	private boolean initialized = false;

	public TraceFileFilter(File referenceTrace, String root) {
		if (root == null) {
			throw new IllegalArgumentException("Null root directory.");
		}
		
		this.referenceTrace = referenceTrace;
		if (! root.endsWith("/")) {
			root += "/";
		}
		this.root = root;
	}

	public void reset() throws IOException {
		if (this.iterator != null) {
			this.iterator.close();
		}
		this.iterator = null;
		initialized = false;
	}

	public boolean accept(File file) throws CopyPolicyException {
		try {
			if (!initialized) {
				if (referenceTrace != null) {
					this.iterator = ArchiveTraceAdapter.buildIterator(referenceTrace);
				}
				initialized = true;
			}
			
			String path = FileSystemManager.getAbsolutePath(file);
			if (! path.startsWith(root)) {
				throw new CopyPolicyException("Error filtering " + file + " : path should start with " + root);
			}
			path = path.substring(root.length());

			return this.iterator == null || this.iterator.fetch(path);
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw new CopyPolicyException(e);
		}
	}
}
