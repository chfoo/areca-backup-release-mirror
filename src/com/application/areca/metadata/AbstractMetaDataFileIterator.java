package com.application.areca.metadata;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import com.application.areca.metadata.AbstractMetadataAdapter.MetadataHeader;
import com.myJava.file.iterator.FilePathComparator;

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
public abstract class AbstractMetaDataFileIterator {
	/**
	 * The reader from which data will be read
	 */
	private BufferedReader in;
	
	/**
	 * The current entry
	 */
	private AbstractMetaDataEntry pointer = null;
	
	/**
	 * The AbstractMetadataAdapter that will be used to decode entries read from the Reader
	 */
	private AbstractMetadataAdapter adapter;
	
	protected boolean closed = false;

	protected AbstractMetaDataFileIterator(BufferedReader reader, AbstractMetadataAdapter adapter) throws IOException {
		this.in = reader;
		this.adapter = adapter;
		
		this.fetchNext();
	}
	
	/**
	 * Close the iterator
	 */
	public void close() throws IOException {
    	if (closed) {
    		return;
    	}
    	closed = true;
    	
		in.close();
	}
	
	public MetadataHeader getHeader() throws IOException {
		return adapter.getMetaData();
	}

	public boolean hasNext() {
		return (pointer != null);
	}
	
	public File getSource() {
		return adapter.file;
	}
	
	private void fetchNext() throws IOException {
		while (true) {
			String line = in.readLine();
			if (line == null) {
				pointer = null;
				break;
			} else if (line.trim().length() != 0) {
				pointer = adapter.decodeEntry(line);
				break;
			}
		}
	}
	
	/**
	 * Iterates until the key provided as argument is found or passed
	 */
	public boolean fetch(String key) throws IOException {
		while (true) {
			if (this.pointer == null) {
				// End of file
				return false;
			} else if (key != null && key.length() == 0) {
				return true;
			} else {
				int result = FilePathComparator.instance().compare(this.pointer.getKey(), key);
				if (result == 0) {
					// found
					return true;
				} else if (result < 0) {
					// key > pointer --> not reached yet
					this.nextEntry();
				} else {
					// key < pointer --> not found in context
					return false;
				}
			}
		}
	}
	
	public AbstractMetaDataEntry nextEntry() throws IOException {
		AbstractMetaDataEntry ret = this.pointer;
		fetchNext();
		return ret;
	}
	
	public AbstractMetaDataEntry currentEntry() {
		return this.pointer;
	}
}
