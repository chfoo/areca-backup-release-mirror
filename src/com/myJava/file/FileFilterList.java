package com.myJava.file;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.myJava.object.ToStringHelper;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

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
public class FileFilterList {
	private List content = new ArrayList();
	private boolean containsDirectories = false;
	private boolean containsFiles = false;

	public void add(String item) {
		if (item.endsWith("/")) {
			containsDirectories = true;
		} else {
			containsFiles = true;
		}
    	content.add(item);
    }
    
    public Iterator iterator() {
    	return content.iterator();
    }
	
	public boolean containsDirectories() {
		return containsDirectories;
	}
	
	public boolean containsFiles() {
		return containsFiles;
	}
	
	public int size() {
		return content.size();
	}
	
	public String get(int i) {
		return (String)content.get(i);
	}
	
	public String[] toArray() {
		return (String[])content.toArray(new String[content.size()]);
	}

	public String toString() {
		StringBuffer sb = ToStringHelper.init(this);
		ToStringHelper.append("ContainsDirs", containsDirectories, sb);
		ToStringHelper.append("ContainsFiles", containsFiles, sb);
		ToStringHelper.append("Filters", content.toString(), sb);
		return ToStringHelper.close(sb);
	}
}
