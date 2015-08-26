package com.application.areca.impl;

import java.io.File;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import com.application.areca.ApplicationException;
import com.application.areca.Utils;
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
public class DateArchiveScope implements ArchiveScope {
	private GregorianCalendar fromDate;          		// Recovery from date
	private GregorianCalendar toDate;             		// Recovery to date
	private Set ignoreList = new HashSet();

	public DateArchiveScope(GregorianCalendar fromDate, GregorianCalendar toDate) {
		this.fromDate = fromDate;
		this.toDate = toDate;
	}

	public String displayScope() {
		return "from " + Utils.formatDisplayDate(fromDate) + " to " + Utils.formatDisplayDate(toDate);
	}
	
	public void setIgnoredArchives(Set ignoredArchives) {
		this.ignoreList = ignoredArchives;
	}

	public File[] buildArchiveList(AbstractIncrementalFileSystemMedium medium) throws ApplicationException {
		GregorianCalendar fd=null, td=null;
		
		if (toDate != null) {
			td = (GregorianCalendar)toDate.clone();
			td.add(GregorianCalendar.MILLISECOND, 1);
		}
		if (fromDate != null) {
			fd = (GregorianCalendar)fromDate.clone();
			fd.add(GregorianCalendar.MILLISECOND, -1);
		}
		
		return medium.listArchives(null, fd, td, ignoreList, true);
	}
}
