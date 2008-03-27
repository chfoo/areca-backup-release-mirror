package com.application.areca.impl.handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.application.areca.impl.FileSystemRecoveryEntry;
import com.application.areca.metadata.content.ArchiveContent;
import com.application.areca.metadata.content.ArchiveContentManager;
import com.myJava.file.FileTool;
import com.myJava.object.PublicClonable;
import com.myJava.util.taskmonitor.TaskCancelledException;

/**
 * Default handler : handles standard files.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 7289397627058093710
 */
 
 /*
 Copyright 2005-2007, Olivier PETRUCCI.
 
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
public class DefaultArchiveHandler
extends AbstractArchiveHandler {

    public void store(FileSystemRecoveryEntry entry, InputStream in, OutputStream out, ProcessContext context)
    throws ApplicationException, IOException, TaskCancelledException {    
        FileTool.getInstance().copy(in, out, true, false, context.getTaskMonitor());
    }

    public void recoverRawData(
            File[] archivesToRecover, 
            Map filtersByArchive, 
            short mode,
            ProcessContext context
    ) throws IOException, ApplicationException {
        // Simply recover the files
        medium.ensureLocalCopy(
        		archivesToRecover, 
        		true, 
        		context.getRecoveryDestination(), 
        		filtersByArchive, 
        		context
        );
    }

    public PublicClonable duplicate() {
        return new DefaultArchiveHandler();
    }

	public Map dispatchEntries(File[] archives, Set entriesToRecover) throws ApplicationException, IOException {
		Map entriesByArchive = new HashMap();
		
		// Build a list of entries to recover indexed by archive
		for (int i=archives.length - 1; i>=0 && entriesToRecover.size() > 0; i--) {
			ArchiveContent content = ArchiveContentManager.getContentForArchive(this.medium, archives[i]);
			Iterator iter = entriesToRecover.iterator();
			Set toRemove = new HashSet();
			while (iter.hasNext()) {
				FileSystemRecoveryEntry entry = (FileSystemRecoveryEntry)iter.next();
				if (content.contains(entry)) {
					List entries = (List)entriesByArchive.get(archives[i]);
					if (entries == null) {
						entries = new ArrayList();
						entriesByArchive.put(archives[i], entries);
					}
					entries.add(entry.getName());
					toRemove.add(entry);
				}
			}
			entriesToRecover.removeAll(toRemove);
		}
		
		return entriesByArchive;
	}
}
