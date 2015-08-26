package com.application.areca;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	
	public void sortBySize(boolean asc) {
		sort(new SizeComparator(asc));
	}
	
	public void sortByPath(boolean asc) {
		sort(new PathComparator(asc));
	}
	
	private void sort(Comparator comparator) {
		Collections.sort(entries, comparator);
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
	
	private static class SizeComparator implements Comparator {
		private boolean asc;
		
		public SizeComparator(boolean asc) {
			this.asc = asc;
		}
		
		public int compare(Object arg0, Object arg1) {
			FileSystemRecoveryEntry e0 = (FileSystemRecoveryEntry)arg0;
			FileSystemRecoveryEntry e1 = (FileSystemRecoveryEntry)arg1;
			
			if (e0.getSize() == e1.getSize()) {
				return 0;
			} else if (e0.getSize() < e1.getSize()) {
				return asc ? -1 : 1;
			} else {
				return asc ? 1 : -1;
			}
		}
	}
	
	private static class PathComparator implements Comparator {
		private boolean asc;
		
		public PathComparator(boolean asc) {
			this.asc = asc;
		}
		
		public int compare(Object arg0, Object arg1) {
			FileSystemRecoveryEntry e0 = (FileSystemRecoveryEntry)arg0;
			FileSystemRecoveryEntry e1 = (FileSystemRecoveryEntry)arg1;
			
			String k0 = e0.getKey().toLowerCase();
			String k1 = e1.getKey().toLowerCase();
	
			int result = k0.compareTo(k1);
			if (result == 0) {
				return 0;
			} else if (result < 0) {
				return asc ? -1 : 1;
			} else {
				return asc ? 1 : -1;
			}
		}
	}
}
