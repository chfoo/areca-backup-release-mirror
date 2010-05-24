package com.application.areca.metadata;

import java.io.BufferedReader;
import java.io.IOException;

import com.myJava.file.iterator.FilePathComparator;

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
public abstract class AbstractMetaDataFileIterator {
	private BufferedReader in;
	private AbstractMetaDataEntry pointer = null;
	private AbstractMetadataAdapter adapter;
	private boolean header = true;

	protected AbstractMetaDataFileIterator(BufferedReader reader, AbstractMetadataAdapter adapter) throws IOException {
		this.in = reader;
		this.adapter = adapter;
		this.fetchNext();
	}
	
	/**
	 * Close the iterator
	 */
	public void close() throws IOException {
		in.close();
	}

	public boolean hasNext() {
		return (pointer != null);
	}
	
	private void fetchNext() throws IOException {
		String line = in.readLine();
		if (header) {
			header = false;
			fetchNext();
		} else if (line == null) {
			pointer = null;
		} else {
	        line = line.trim();
			if (line.length() == 0) {
				fetchNext();
			} else {
				pointer = adapter.decodeEntry(line);
			}
		}
	}
	
	/**
	 * Iterates until the key provided as argument is found or passed
	 */
	public boolean fetchUntil(String key) throws IOException {
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
