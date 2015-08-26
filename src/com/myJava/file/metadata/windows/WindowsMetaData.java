package com.myJava.file.metadata.windows;

import com.myJava.file.metadata.FileMetaData;
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
public class WindowsMetaData implements FileMetaData {
    private boolean canRead;
    private boolean canWrite;
    private long lastmodified = -1;

    public WindowsMetaData() {
    }
    
    public boolean isCanRead() {
        return canRead;
    }
    
    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }
    
    public boolean isCanWrite() {
        return canWrite;
    }
    
    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }

    public long getLastmodified() {
		return lastmodified;
	}

	public void setLastmodified(long lastmodified) {
		this.lastmodified = lastmodified;
	}

	public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("canRead", this.canRead, sb);
        ToStringHelper.append("lastmodified", this.lastmodified, sb);
        ToStringHelper.append("canWrite", this.canWrite, sb);       
        return ToStringHelper.close(sb);
    }

	public boolean equals(Object obj) {
		if (! EqualsHelper.checkClasses(obj, this)) {
			return false;
		} else {
			WindowsMetaData other = (WindowsMetaData)obj;
			return
				EqualsHelper.equals(other.canRead, this.canRead)
				&& EqualsHelper.equals(other.lastmodified, this.lastmodified)				
				&& EqualsHelper.equals(other.canWrite, this.canWrite)
			;
		}
	}

	public int hashCode() {
		int h = HashHelper.initHash(this);
		h = HashHelper.hash(h, canRead);
		h = HashHelper.hash(h, lastmodified);
		h = HashHelper.hash(h, canWrite);
		return h;
	}
}
