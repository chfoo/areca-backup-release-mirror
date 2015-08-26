package com.application.areca.impl.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.myJava.file.FileList.FileListIterator;

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
public class RecoveryFilterMap {
	private Map content = new HashMap();
	
	public void add(File archive, String entry) throws IOException {
		com.application.areca.metadata.FileList entries = (com.application.areca.metadata.FileList)get(archive);
		if (entries == null) {
			entries = new com.application.areca.metadata.FileList();
			content.put(archive, entries);
		}
		entries.add(entry);
    }

	public com.application.areca.metadata.FileList get(File archive) {
    	return (com.application.areca.metadata.FileList)content.get(archive);
    }
	
	/**
	 * Return the list of archives containing the given entry
	 * @param entry
	 * @return
	 * @throws IOException
	 */
	public File[] lookupEntry(String entry) throws IOException {
		Iterator iter = content.keySet().iterator();
		List ret = new ArrayList();
		while (iter.hasNext()) {
			File archive = (File)iter.next();
			com.application.areca.metadata.FileList list = get(archive);
			FileListIterator listIter = list.iterator();
			try {
				if (listIter.fetch(entry)) {
					ret.add(archive);
				}
			} finally {
				listIter.close();
			}
		}
		return (File[])ret.toArray(new File[ret.size()]);
	}
}
