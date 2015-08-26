package com.application.areca.metadata.content;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import com.application.areca.metadata.AbstractMetaDataFileIterator;

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
public class ContentFileIterator extends AbstractMetaDataFileIterator {
	private File referenceArchive = null;		// optional field - used to maintain a link with the source archive

	protected ContentFileIterator(BufferedReader reader, ArchiveContentAdapter adapter) 
	throws IOException {
		super(reader, adapter);
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
