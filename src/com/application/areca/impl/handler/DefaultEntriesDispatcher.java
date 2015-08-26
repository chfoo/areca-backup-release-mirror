package com.application.areca.impl.handler;

import java.io.File;
import java.io.IOException;

import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.metadata.content.ArchiveContentAdapter;
import com.application.areca.metadata.content.ArchiveContentManager;
import com.application.areca.metadata.content.ContentFileIterator;
import com.myJava.file.FileTool;
import com.myJava.system.OSTool;

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
public class DefaultEntriesDispatcher extends AbstractEntriesDispatcher {
	protected ContentFileIterator[] iters;
	protected boolean initialized = false;
	
	public DefaultEntriesDispatcher(File[] archives, AbstractIncrementalFileSystemMedium medium) {
		super(archives, medium);
		
		// Build content iterators
		iters = new ContentFileIterator[archives.length];
	}
	
	private void initialize() throws IOException {
		if (! initialized) {
			initialized = true;

			for (int i=0; i<archives.length; i++) {
				iters[i] = ArchiveContentAdapter.buildIterator(
						ArchiveContentManager.resolveContentFileForArchive(medium, archives[i])
				);
			}
		}
	}
	
	public void dispatchEntry(String entry) throws IOException {
		initialize();
		int index = -1;
		for (int i=archives.length-1; i>=0; i--) {
			boolean found = iters[i].fetch(entry);
			if (found) {
				index = i;
				break;
			}
		}
		
		if (index == -1) {
			// This can happen if the user tries to recover a symbolic link
			// throw new IllegalStateException(entriesToRecover[e] + " was not found in ANY archive.");
		} else {
			this.incrementEntries();
			result.add(archives[index], entry);
		}
	}

	public void close() throws IOException {
		for (int i=0; i<archives.length; i++) {
			if (iters[i] != null) {
				iters[i].close();
			}
		}
	}
	/*
	public static void main(String[] args) {
		try {
			for (int i=0; i<2000; i++) {
				System.out.println("File " + i);
				File f = new File("C:\\Users\\Olivier\\Desktop\\src", "File_" + i + ".txt");
				FileTool.getInstance().createFile(f, "File number " + i);
				String cmd = "C:/Users/Olivier/Desktop/backup_1012077951.bat";
				OSTool.execute(new String[] {cmd});
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
}