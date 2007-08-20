package com.myJava.file.attributes;

import com.myJava.util.ToStringHelper;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4438212685798161280
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
public class UnixAttributes implements Attributes {

    private int permissions;
    private String owner;
    private String ownerGroup;

    public UnixAttributes() {
    }

    public String getOwner() {
        return owner;
    }

    public int getPermissions(PermissionScope type) {
        return (int)(permissions / Math.pow(10, type.getOrder()) % 10);
    }
    
    public void setOwner(String owner) {
        this.owner = owner;
    }
    
    public String getOwnerGroup() {
        return ownerGroup;
    }
    
    public void setOwnerGroup(String ownerGroup) {
        this.ownerGroup = ownerGroup;
    }
    
    public int getPermissions() {
        return permissions;
    }
    
    public void setPermissions(int permissions) {
        this.permissions = permissions;
    }
    
    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("permissions", this.permissions, sb);
        ToStringHelper.append("owner", this.owner, sb);       
        ToStringHelper.append("ownerGroup", this.ownerGroup, sb);               
        return ToStringHelper.close(sb);
    }
}
