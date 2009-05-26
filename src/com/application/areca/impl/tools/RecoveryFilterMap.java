package com.application.areca.impl.tools;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.myJava.file.FileFilterList;

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
public class RecoveryFilterMap {
	private Map content = new HashMap();
	private boolean optimized = false;

    public RecoveryFilterMap(boolean optimized) {
		this.optimized = optimized;
	}

	public void put(File archive, FileFilterList filters) {
    	content.put(archive, filters);
    }
    
    public FileFilterList get(File archive) {
    	return (FileFilterList)content.get(archive);
    }

	public boolean isOptimized() {
		return optimized;
	}

	public void setOptimized(boolean isOptimized) {
		this.optimized = isOptimized;
	}
	
	public boolean containsDirectories() {
		Iterator iter = content.keySet().iterator();
		while (iter.hasNext()) {
			File archive = (File)iter.next();
			FileFilterList list = get(archive);
			if (list.containsDirectories()) {
				return true;
			}
		}
		return false;
	}
}
