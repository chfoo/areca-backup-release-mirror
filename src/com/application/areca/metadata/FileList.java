package com.application.areca.metadata;

import java.io.File;
import java.io.IOException;

import com.application.areca.metadata.content.ArchiveContentAdapter;
import com.application.areca.metadata.content.ContentFileIterator;
import com.myJava.file.FileTool;
import com.myJava.object.ToStringHelper;

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
public class FileList implements com.myJava.file.FileList{
	private boolean containsDirectories = false;
	private boolean containsFiles = false;
	private int size = 0;
	private File file = null;
	private boolean initialized = false;
	private boolean locked = false;
	private ArchiveContentAdapter adapter;

	private void initialize() throws IOException {
		if (! initialized) {
			file = FileTool.getInstance().generateNewWorkingFile(null, null, "tmp", true);
			adapter = new ArchiveContentAdapter(file, null);
			initialized = true;
		}
	}

	public void add(String item) throws IOException {
		if (locked) {
			throw new IllegalStateException();
		}
		initialize();
		if (item.endsWith("/")) {
			containsDirectories = true;
		} else {
			containsFiles = true;
		}
		size++;
		adapter.writeGenericEntry(item, "");
    }
	
	public void lock() throws IOException {
		if (! locked) {
			initialize();
			adapter.close();
			locked = true;
		}
	}
	
	public FileListIterator iterator() throws IOException {
		lock();
		final ContentFileIterator iter = ArchiveContentAdapter.buildIterator(file);
		
		return new FileListIterator() {
			public boolean fetch(String key) throws IOException {
				return iter.fetch(key);
			}
			
			public String current() {
				return iter.current().getKey();
			}

			public boolean hasNext() {
				return iter.hasNext();
			}

			public String next() throws IOException {
				return iter.next().getKey();
			}

			public void close() throws IOException {
				iter.close();
			}
		};
	}



	public int size() {
		return size;
	}
	
	public boolean containsDirectories() {
		return containsDirectories;
	}
	
	public boolean containsFiles() {
		return containsFiles;
	}

	public String toString() {
		StringBuffer sb = ToStringHelper.init(this);
		ToStringHelper.append("ContainsDirs", containsDirectories, sb);
		ToStringHelper.append("ContainsFiles", containsFiles, sb);
		ToStringHelper.append("Size", size, sb);
		ToStringHelper.append("File", file, sb);
		return ToStringHelper.close(sb);
	}
}
