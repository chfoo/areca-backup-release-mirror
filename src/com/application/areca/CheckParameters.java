package com.application.areca;

/**
 * 
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2010, Olivier PETRUCCI.

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
public class CheckParameters {
	private boolean check = true;
	private boolean useSpecificLocation = false;
	private String specificLocation = null;
	private boolean checkLastArchiveOnly = true;

	public CheckParameters(
			boolean check, 
			boolean checkLastArchiveOnly, 
			boolean useSpecificLocation,
			String specificLocation) {
		
		super();
		this.check = check;
		this.useSpecificLocation = useSpecificLocation;
		this.specificLocation = specificLocation;
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
}
