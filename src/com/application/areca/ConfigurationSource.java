package com.application.areca;

import java.io.File;

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
public class ConfigurationSource {
	private boolean deprecated = false;
	private File source;
	private boolean backupCopy = false;

	public ConfigurationSource(boolean deprecated, File source) {
		this.deprecated = deprecated;
		this.source = source;
	}

	public boolean isDeprecated() {
		return deprecated;
	}
	
	public File getSource() {
		return source;
	}
	
	public String toString() {
		return source.toString();
	}

	public boolean isBackupCopy() {
		return backupCopy;
	}

	public void setBackupCopy(boolean backupCopy) {
		this.backupCopy = backupCopy;
	}
}
