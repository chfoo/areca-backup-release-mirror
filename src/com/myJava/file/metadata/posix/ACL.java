package com.myJava.file.metadata.posix;

import java.util.ArrayList;
import java.util.Iterator;

import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
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
public class ACL {
	private java.util.List content;
	
	public ACL() {
		content = new ArrayList();
	}

	public void addEntry(int tag, int identifier, boolean r, boolean w, boolean x) {
		ACLEntry entry = new ACLEntry();
		entry.setR(r);
		entry.setW(w);
		entry.setX(x);
		entry.setIdentifier(identifier);
		entry.setTag(tag);
		content.add(entry);
	}
	
	public void addEntry(int tag, int identifier, int perms) {
		ACLEntry entry = new ACLEntry();
		entry.setIdentifier(identifier);
		entry.setTag(tag);
		entry.setPermissions(perms);
		content.add(entry);
	}
	
	public int size() {
		return content.size();
	}
	
	public Iterator iterator() {
		return content.iterator();
	}
	
	public ACLEntry getEntryAt(int i) {
		return (ACLEntry)content.get(i);
	}
	
	public boolean isEmpty() {
		return content.isEmpty();
	}
	
	public String toString() {
		StringBuffer sb = ToStringHelper.init(this);
		ToStringHelper.append("content", content, sb);
		return ToStringHelper.close(sb);
	}
	
	public boolean equals(Object obj) {
		if (! EqualsHelper.checkClasses(obj, this)) {
			return false;
		} else {
			ACL other = (ACL)obj;
			return EqualsHelper.equals(other.content, this.content);
		}
	}

	public int hashCode() {
		int h = HashHelper.initHash(this);
		h = HashHelper.hash(h, content);
		return h;
	}
}
