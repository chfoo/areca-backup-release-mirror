package com.application.areca;

import com.myJava.object.Duplicable;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;

/**
 * 
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
public class CheckParameters implements Duplicable {
	private boolean check = true;
	private boolean useSpecificLocation = false;
	private boolean simulateRecovery = false;
	private String specificLocation = null;
	private boolean checkLastArchiveOnly = true;

	public CheckParameters(
			boolean check, 
			boolean checkLastArchiveOnly, 
			boolean simulateRecovery,
			boolean useSpecificLocation,
			String specificLocation) {
		
		super();
		this.check = check;
		this.useSpecificLocation = useSpecificLocation;
		this.specificLocation = specificLocation;
		this.simulateRecovery = simulateRecovery;
		this.checkLastArchiveOnly = checkLastArchiveOnly;
	}
	public boolean isCheck() {
		return check;
	}
	public void setCheck(boolean check) {
		this.check = check;
	}
	public boolean isUseSpecificLocation() {
		return useSpecificLocation;
	}
	public void setUseSpecificLocation(boolean useSpecificLocation) {
		this.useSpecificLocation = useSpecificLocation;
	}
	public String getSpecificLocation() {
		return specificLocation;
	}
	public void setSpecificLocation(String specificLocation) {
		this.specificLocation = specificLocation;
	}
	public boolean isCheckLastArchiveOnly() {
		return checkLastArchiveOnly;
	}
	public void setCheckLastArchiveOnly(boolean checkLastArchiveOnly) {
		this.checkLastArchiveOnly = checkLastArchiveOnly;
	}
	public boolean isSimulateRecovery() {
		return simulateRecovery;
	}
	public void setSimulateRecovery(boolean simulateRecovery) {
		this.simulateRecovery = simulateRecovery;
	}
	
    public Duplicable duplicate() {
    	CheckParameters p = new CheckParameters(this.check, this.checkLastArchiveOnly, this.simulateRecovery, this.useSpecificLocation, this.specificLocation);
        return p;
    }
    
	public boolean equals(Object obj) {
        if (obj == null || (! (obj instanceof CheckParameters)) ) {
            return false;
        } else {
        	CheckParameters other = (CheckParameters)obj;
            return 
            	super.equals(other)
            	&& EqualsHelper.equals(this.specificLocation, other.specificLocation)
                && EqualsHelper.equals(this.useSpecificLocation, other.useSpecificLocation)
                && EqualsHelper.equals(this.check, other.check)
                && EqualsHelper.equals(this.checkLastArchiveOnly, other.checkLastArchiveOnly)
                && EqualsHelper.equals(this.simulateRecovery, other.simulateRecovery);
        }
	}

	public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, super.hashCode());
        h = HashHelper.hash(h, this.specificLocation);
        h = HashHelper.hash(h, this.useSpecificLocation);
        h = HashHelper.hash(h, this.check);
        h = HashHelper.hash(h, this.checkLastArchiveOnly);
        h = HashHelper.hash(h, this.simulateRecovery);
        return h;
	}
}
