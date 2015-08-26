package com.application.areca;

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
public class MergeParameters implements Duplicable {
	private boolean keepDeletedEntries = false;
	private boolean useSpecificLocation = false;
	private String specificLocation = null;

	public MergeParameters(boolean keepDeletedEntries, boolean useSpecificLocation, String specificLocation) {
		this.keepDeletedEntries = keepDeletedEntries;
		this.useSpecificLocation = useSpecificLocation;
		this.specificLocation = specificLocation;
	}
	
	public boolean isKeepDeletedEntries() {
		return keepDeletedEntries;
	}

	public boolean isUseSpecificLocation() {
		return useSpecificLocation;
	}

	public String getSpecificLocation() {
		return specificLocation;
	}

	public boolean equals(Object obj) {
        if (obj == null || (! (obj instanceof MergeParameters)) ) {
            return false;
        } else {
        	MergeParameters other = (MergeParameters)obj;
            return 
            	super.equals(other)
            	&& EqualsHelper.equals(this.specificLocation, other.specificLocation)
                && EqualsHelper.equals(this.useSpecificLocation, other.useSpecificLocation)
                && EqualsHelper.equals(this.keepDeletedEntries, other.keepDeletedEntries);
        }
	}

	public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, super.hashCode());
        h = HashHelper.hash(h, this.specificLocation);
        h = HashHelper.hash(h, this.useSpecificLocation);
        h = HashHelper.hash(h, this.keepDeletedEntries);
        return h;
	}
	
    public Duplicable duplicate() {
        MergeParameters p = new MergeParameters(keepDeletedEntries, useSpecificLocation, specificLocation);
        return p;
    }
}
