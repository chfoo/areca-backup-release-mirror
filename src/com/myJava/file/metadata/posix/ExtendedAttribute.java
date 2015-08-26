package com.myJava.file.metadata.posix;

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
public class ExtendedAttribute {
	private String name;
	private byte[] data;

	public ExtendedAttribute(String name, byte[] data) {
		this.name = name;
		this.data = data;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	public String toString() {
		StringBuffer sb = ToStringHelper.init(this);
		ToStringHelper.append("name", name, sb);
		ToStringHelper.append("data", getAsString(), sb);
		return ToStringHelper.close(sb);
	}

	public String getAsString() {
		if (data == null) {
			return "<null>";
		} else {
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<data.length; i++) {
				sb.append((char)data[i]);
			}
			return sb.toString();
		}
	}
	
	public boolean equals(Object obj) {
		if (! EqualsHelper.checkClasses(obj, this)) {
			return false;
		} else {
			ExtendedAttribute other = (ExtendedAttribute)obj;
			return
				EqualsHelper.equals(other.name, this.name)
				&& EqualsHelper.equals(other.data, this.data)	
			;
		}
	}

	public int hashCode() {
		int h = HashHelper.initHash(this);
		h = HashHelper.hash(h, name);
		h = HashHelper.hash(h, data);
		return h;
	}
}
