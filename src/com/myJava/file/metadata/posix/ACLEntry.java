package com.myJava.file.metadata.posix;

import com.myJava.file.metadata.posix.jni.wrapper.FileAccessWrapper;
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
public class ACLEntry {
	// Tag values
	public static final int ACL_UNDEFINED_TAG = 0;
	public static final int ACL_USER_OBJ	= 1;
	public static final int ACL_USER	= 2;
	public static final int ACL_GROUP_OBJ = 4;
	public static final int ACL_GROUP = 8;
	public static final int ACL_MASK	= 16;
	public static final int ACL_OTHER = 32;
	
	// Instance attributes
	private boolean r;
	private boolean w;
	private boolean x;
	private int tag;
	private int identifier;
	
	public String toString() {
		StringBuffer sb = ToStringHelper.init(this);
		ToStringHelper.append("tag", getStringTag(), sb);
		if (identifier != -1) {
			String name;
			if (tag == ACL_USER) {
				name = FileAccessWrapper.getUserName(identifier);
			} else if (tag == ACL_GROUP) {
				name = FileAccessWrapper.getGroupName(identifier);
			} else {
				name = "ERROR";
			}
			ToStringHelper.append("identifier", name + " (" + identifier + ")", sb);
		}
		ToStringHelper.append("r", r, sb);
		ToStringHelper.append("w", w, sb);
		ToStringHelper.append("x", x, sb);
		
		return ToStringHelper.close(sb);
	}

	public boolean isR() {
		return r;
	}

	public void setR(boolean r) {
		this.r = r;
	}

	public boolean isW() {
		return w;
	}

	public void setW(boolean w) {
		this.w = w;
	}

	public boolean isX() {
		return x;
	}

	public void setX(boolean x) {
		this.x = x;
	}

	public int getTag() {
		return tag;
	}

	public void setTag(int tag) {
		this.tag = tag;
	}

	public int getIdentifier() {
		return identifier;
	}

	public void setIdentifier(int identifier) {
		this.identifier = identifier;
	}
	
	public void setPermissions(int p) {
		x = (p%2 == 1);
		w = (p%4 >= 2);
		r = (p >= 4);
	}
	
	public int getPermissions() {
		return (r?4:0) + (w?2:0) + (x?1:0);
	}

	public String getStringTag() {
		switch (tag) {
		case ACL_UNDEFINED_TAG:
			return "undefined";
		case ACL_USER_OBJ:
			return "owner";
		case ACL_USER:
			return "user";
		case ACL_GROUP_OBJ:
			return "owner_group";
		case ACL_GROUP:
			return "group";
		case ACL_MASK:
			return "mask";
		case ACL_OTHER:
			return "other";
		default:
			return "unknown [" + tag + "]";
		}
	}
	
	public boolean equals(Object obj) {
		if (! EqualsHelper.checkClasses(obj, this)) {
			return false;
		} else {
			ACLEntry other = (ACLEntry)obj;
			return
				EqualsHelper.equals(other.identifier, this.identifier)
				&& EqualsHelper.equals(other.r, this.r)
				&& EqualsHelper.equals(other.w, this.w)
				&& EqualsHelper.equals(other.x, this.x)
				&& EqualsHelper.equals(other.tag, this.tag)		
			;
		}
	}

	public int hashCode() {
		int h = HashHelper.initHash(this);
		h = HashHelper.hash(h, identifier);
		h = HashHelper.hash(h, r);
		h = HashHelper.hash(h, w);
		h = HashHelper.hash(h, x);
		h = HashHelper.hash(h, tag);
		return h;
	}
}
