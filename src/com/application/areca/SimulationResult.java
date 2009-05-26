package com.application.areca;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.application.areca.impl.FileSystemRecoveryEntry;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

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
public class SimulationResult {
	private long maxEntries = (long)(0.5 * MemoryHelper.getMaxManageableEntries());
	private boolean maxEntriesReached = false;
	private List entries = new ArrayList();
	private long globalSize;
	private long newFiles;
	private long modifiedFiles;
	
	public boolean isMaxEntriesReached() {
		return maxEntriesReached;
	}
	
	public void addEntry(FileSystemRecoveryEntry entry) {
        if (entry.getStatus() == EntryStatus.STATUS_CREATED) {
        	newFiles++;
        } else if (entry.getStatus() == EntryStatus.STATUS_DELETED) {
        	// shall not happen anymore : deletions are not tracked by the simulation window   
        } else {
        	modifiedFiles++;
        }

        if (! entry.isLink()) {
        	globalSize += entry.getSize();
        }
        
		if (entries.size() < maxEntries) {
			entries.add(entry);
		} else {
			maxEntriesReached = true;
		}
	}
	
	public Iterator iterator() {
		return entries.iterator();
	}

	public long getGlobalSize() {
		return globalSize;
	}

	public long getNewFiles() {
		return newFiles;
	}

	public long getModifiedFiles() {
		return modifiedFiles;
	}
}
