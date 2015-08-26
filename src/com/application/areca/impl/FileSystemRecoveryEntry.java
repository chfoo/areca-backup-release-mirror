package com.application.areca.impl;

import java.io.File;

import com.application.areca.EntryStatus;
import com.application.areca.RecoveryEntry;
import com.application.areca.Utils;
import com.myJava.object.Duplicable;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;

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
public class FileSystemRecoveryEntry implements RecoveryEntry, Duplicable {
    private File file;
    private String rootDirectory;
    private short status = EntryStatus.STATUS_NOT_STORED;
    private long size = 0;
    private boolean isLink = false;
    private String key;
    
    public FileSystemRecoveryEntry() {
    }
    
    public void init(String rootDirectory, File file) {
        this.rootDirectory = rootDirectory;            
        this.file = file;
        this.key = Utils.extractShortFilePath(this.getFile(), this.rootDirectory);
    }

    public boolean isLink() {
		return isLink;
	}

	public void setLink(boolean isLink) {
		this.isLink = isLink;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public void setStatus(short status) {
		this.status = status;
	}
	
	public void copyAttributes(FileSystemRecoveryEntry other) {
		other.file = this.file;
		other.isLink = this.isLink;
		other.key = this.key;
		other.rootDirectory = this.rootDirectory;
		other.size = this.size;
		other.status = this.status;
	}

	public Duplicable duplicate() {
		FileSystemRecoveryEntry other = new FileSystemRecoveryEntry();
		this.copyAttributes(other);
		return other;
	}

	public short getStatus() {
		return status;
	}

	public File getFile() {
        return this.file;
    }
    
    public String toString() {
        return getKey();
    }

	public String getKey() {
	    return key;
	}
	
	public String getName() {
		int idx = key.lastIndexOf('/');
		if (idx == -1) {
			return key;
		} else {
			return key.substring(idx+1);
		}
	}
	
    public boolean equals(Object obj) {
        if (obj == null || ! (obj instanceof FileSystemRecoveryEntry)) {
            return false;
        } else {
            FileSystemRecoveryEntry other = (FileSystemRecoveryEntry)obj;
            return
            	EqualsHelper.equals(other.key, this.key);
        }
    }
    
    public int hashCode() {
        int hash = HashHelper.initHash(this);
        hash = HashHelper.hash(hash, this.key);
        return hash;
    }
}