package com.application.areca.impl.handler;

import java.io.File;

import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.impl.tools.RecoveryFilterMap;

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
public abstract class AbstractEntriesDispatcher implements EntriesDispatcher {
	protected File[] archives;
	protected AbstractIncrementalFileSystemMedium medium;
	protected RecoveryFilterMap result = new RecoveryFilterMap();
	protected long entries = 0;
	
	protected AbstractEntriesDispatcher(File[] archives, AbstractIncrementalFileSystemMedium medium) {
		this.archives = archives;
		this.medium = medium;
	}

	public RecoveryFilterMap getResult() {
		return result;
	}
	
	protected void incrementEntries() {
		this.entries++;
	}

	public long getEntriesCount() {
		return entries;
	}
}
