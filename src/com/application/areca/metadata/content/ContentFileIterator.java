package com.application.areca.metadata.content;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import com.application.areca.metadata.AbstractMetaDataFileIterator;
import com.myJava.file.FileTool;

/**
 * 
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
public class ContentFileIterator extends AbstractMetaDataFileIterator {
	private boolean deleteOnClose = false;
	private File fileToDelete = null;
	private File referenceArchive = null;		// optional field - used to maintain a link with the source archive

	protected ContentFileIterator(BufferedReader reader, ArchiveContentAdapter adapter) 
	throws IOException {
		super(reader, adapter);
	}
	
	/**
	 * Close the iterator
	 */
	public void close() throws IOException {
		try {
			super.close();
		} finally {
			if (deleteOnClose) {
				FileTool.getInstance().delete(fileToDelete, true);
			}
		}
	}

	public void setDeleteOnClose(File fileToDelete) {
		this.fileToDelete = fileToDelete;
		this.deleteOnClose = true;
	}
	
	public ContentEntry next() throws IOException {
		return (ContentEntry)this.nextEntry();
	}
	
	public ContentEntry current() {
		return (ContentEntry)this.currentEntry();
	}

	public File getReferenceArchive() {
		return referenceArchive;
	}

	public void setReferenceArchive(File referenceArchive) {
		this.referenceArchive = referenceArchive;
	}
}
