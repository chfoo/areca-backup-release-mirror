package com.myJava.file.attributes;

import com.myJava.object.HashHelper;
import com.myJava.object.ToStringHelper;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 8363716858549252512
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
public class PermissionScope {
    public static PermissionScope OWNER = new PermissionScope("Owner", 2);
    public static PermissionScope GROUP = new PermissionScope("Group", 1);
    public static PermissionScope OTHER = new PermissionScope("Other", 0);    
    
    private String id;
    private int order;
    
    private PermissionScope(String id, int order) {
        this.id = id;
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
    
    public boolean equals(Object obj) {
        if (obj == null || (! (obj instanceof PermissionScope))) {
            return false;
        } else {
            return this.id.equals(((PermissionScope)obj).id);
        }
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, id);
        return h;
    }
    
    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("ID", this.id, sb);
        return ToStringHelper.close(sb);
    }
}
