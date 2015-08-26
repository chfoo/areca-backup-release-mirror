package com.application.areca.filter;

import java.io.File;
import java.io.IOException;

import com.application.areca.Utils;
import com.myJava.file.FileSystemManager;
import com.myJava.file.metadata.FileMetaData;
import com.myJava.file.metadata.posix.PosixMetaData;
import com.myJava.object.Duplicable;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.util.log.Logger;

/**
 * @author Olivier PETRUCCI <BR>
 * 
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
public class FileOwnerArchiveFilter extends AbstractArchiveFilter {
	private static final long serialVersionUID = 6961712009168838158L;

	private static final char SEPARATOR = ':';

	private String owner;
	private String group;

	public void acceptParameters(String parameters) {
		if (Utils.isEmpty(parameters)) {
			throw new IllegalArgumentException("Invalid parameters : " + parameters);
		}

		this.owner = null;
		this.group = null;

		String params = parameters.trim();
		int index = params.indexOf(SEPARATOR);
		if (index == -1) {
			this.owner = params;
		} else {
			if (index != 0) {
				this.owner = params.substring(0, index).trim();
			}
			this.group = params.substring(index + 1).trim();
		}
	}
	
	public short acceptIteration(File entry, File data) {
		if (! acceptElement(entry, data)) {
			return WILL_MATCH_FALSE;
		} else {
			return WILL_MATCH_PERHAPS;
		}
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getOwner() {
		return owner;
	}

	public String getGroup() {
		return group;
	}

	public boolean acceptElement(File entry, File data) {
		if (entry == null) {
			return false;
		} else {
			try {
				FileMetaData atts = FileSystemManager.getMetaData(data, true);
				boolean match;
				if (atts instanceof PosixMetaData) {
					PosixMetaData pmtd = (PosixMetaData)atts;
					match = (owner == null || owner.equals(pmtd.getOwner()));
					match = match && (group == null || group.equals(pmtd.getGroup()));
				} else {
					match = true;
				}
				return match ? !logicalNot : logicalNot;
			} catch (IOException e) {
				String msg = "Error reading file permissions for "+ FileSystemManager.getDisplayPath(data);
				Logger.defaultLogger().info(msg);
				throw new IllegalArgumentException(msg);
			}
		}
	}

	public Duplicable duplicate() {
		FileOwnerArchiveFilter filter = new FileOwnerArchiveFilter();
		filter.logicalNot = this.logicalNot;
		filter.owner = this.owner;
		filter.group = this.group;
		return filter;
	}

	public String getStringParameters() {
		return (owner == null ? "" : owner)
				+ (group == null ? "" : (SEPARATOR + group));
	}

	public boolean equals(Object obj) {
		if (obj == null || (!(obj instanceof FileOwnerArchiveFilter))) {
			return false;
		} else {
			FileOwnerArchiveFilter other = (FileOwnerArchiveFilter) obj;
			return EqualsHelper.equals(this.logicalNot, other.logicalNot)
					&& EqualsHelper.equals(this.owner, other.owner)
					&& EqualsHelper.equals(this.group, other.group);
		}
	}

	public int hashCode() {
		int h = HashHelper.initHash(this);
		h = HashHelper.hash(h, this.owner);
		h = HashHelper.hash(h, this.logicalNot);
		h = HashHelper.hash(h, this.group);
		return h;
	}
}
