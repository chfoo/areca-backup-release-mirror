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
public class PosixMetaDataImpl implements PosixMetaData {
	public static final int UNDEF_MODE = -1;
	
    private int mode = UNDEF_MODE;
    private String owner;
    private String group;
    private long lastmodified = UNDEF_DATE;
    private ACL accessAcl;
    private ACL defaultAcl;
    private ExtendedAttributeList xattrList;

    public PosixMetaDataImpl() {
    }

    public String getOwner() {
        return owner;
    }

	public void setOwner(String owner) {
        this.owner = owner;
    }
    
    public String getGroup() {
        return group;
    }

	public void setGroup(String group) {
        this.group = group;
    }
    
    public int getMode() {
        return mode;
    }
    
    public int getModeBase10() {
		int o = mode%8;
		int g = ((mode-o)/8)%8;
		int u = (mode-8*g-o)/64;
		
		return o+10*g+100*u;
	}
    
    public void setModeBase10(int m) {
		int o = m%10;
		int g = ((m-o)/10)%10;
		int u = (m-10*g-o)/100;
		
		mode = o+8*g+64*u;
	}
    
    public void setMode(int mode) {
        this.mode = mode;
    }

    public ACL getAccessAcl() {
		return accessAcl;
	}

	public void setAccessAcl(ACL acl) {
		this.accessAcl = acl;
	}

	public ACL getDefaultAcl() {
		return defaultAcl;
	}

	public void setDefaultAcl(ACL defaultAcl) {
		this.defaultAcl = defaultAcl;
	}

	public ExtendedAttributeList getXattrList() {
		return xattrList;
	}

	public void setXattrList(ExtendedAttributeList xattrList) {
		this.xattrList = xattrList;
	}

	public long getLastmodified() {
		return lastmodified;
	}

	public void setLastmodified(long lastmodified) {
		this.lastmodified = lastmodified;
	}

	public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("mode", this.mode, sb);
        ToStringHelper.append("owner", this.owner, sb);       
        ToStringHelper.append("group", this.group, sb); 
        ToStringHelper.append("lastmodified", this.lastmodified, sb);    
        ToStringHelper.append("access acl", this.accessAcl, sb);
        ToStringHelper.append("default acl", this.defaultAcl, sb);
        ToStringHelper.append("xattr list", this.xattrList, sb);
        return ToStringHelper.close(sb);
    }
	
	public boolean equals(Object obj) {
		if (! EqualsHelper.checkClasses(obj, this)) {
			return false;
		} else {
			PosixMetaDataImpl other = (PosixMetaDataImpl)obj;
			return
				EqualsHelper.equals(other.mode, this.mode)
				&& EqualsHelper.equals(other.owner, this.owner)
				&& EqualsHelper.equals(other.group, this.group)
				&& EqualsHelper.equals(other.lastmodified, this.lastmodified)				
				&& EqualsHelper.equals(other.defaultAcl, this.defaultAcl)
				&& EqualsHelper.equals(other.accessAcl, this.accessAcl)
				&& EqualsHelper.equals(other.xattrList, this.xattrList)		
			;
		}
	}

	public int hashCode() {
		int h = HashHelper.initHash(this);
		h = HashHelper.hash(h, mode);
		h = HashHelper.hash(h, owner);
		h = HashHelper.hash(h, group);
		h = HashHelper.hash(h, lastmodified);
		h = HashHelper.hash(h, defaultAcl);
		h = HashHelper.hash(h, accessAcl);
		h = HashHelper.hash(h, xattrList);
		return h;
	}
}