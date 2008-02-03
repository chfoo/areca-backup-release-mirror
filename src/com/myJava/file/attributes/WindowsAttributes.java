package com.myJava.file.attributes;

import com.myJava.object.ToStringHelper;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 8290826359148479344
 */
 
 /*
 Copyright 2005-2007, Olivier PETRUCCI.
 
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
public class WindowsAttributes implements Attributes {

    private boolean canRead;
    private boolean canWrite;

    public WindowsAttributes() {
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

    public int getPermissions(PermissionScope type) {
        if (canRead && canWrite) {
            return 7;   //rwx
        } else if (canRead) {
            return 5;	// rx
        } else {
            return 0;	// nothing
        }
    }
    
    public String getOwner() {
        return null;
    }
    
    public String getOwnerGroup() {
        return null;
    }
    
    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("canRead", this.canRead, sb);
        ToStringHelper.append("canWrite", this.canWrite, sb);       
        return ToStringHelper.close(sb);
    }
}
