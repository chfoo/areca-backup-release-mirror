package com.application.areca.metadata.transaction;

import java.util.GregorianCalendar;

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
public class TransactionPointHeader {
    private GregorianCalendar date;
    private String arecaVersion;
    private String sourcesRoot;
    private String backupScheme;

	public GregorianCalendar getDate() {
		return date;
	}

	public void setDate(GregorianCalendar date) {
		this.date = date;
	}

	public String getArecaVersion() {
		return arecaVersion;
	}

	public void setArecaVersion(String arecaVersion) {
		this.arecaVersion = arecaVersion;
	}

	public String getSourcesRoot() {
		return sourcesRoot;
	}

	public void setSourcesRoot(String sourcesRoot) {
		this.sourcesRoot = sourcesRoot;
	}

	public String getBackupScheme() {
		return backupScheme;
	}

	public void setBackupScheme(String backupScheme) {
		this.backupScheme = backupScheme;
	}
}
