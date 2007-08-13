package com.application.areca.metadata.content;

import java.io.File;
import java.io.IOException;

import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.impl.FileSystemRecoveryEntry;
import com.application.areca.impl.IncrementalDirectoryMedium;
import com.application.areca.impl.IncrementalZipMedium;
import com.myJava.file.FileSystemManager;

/**
 * This class manages accesses to archives' metadata.
 * <BR>It includes backward-compatibility management.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -4899974077672581254
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
public class ArchiveContentManager {

    public static ArchiveContent getContentForArchive(AbstractIncrementalFileSystemMedium medium, File archive) throws IOException {
        File metadataDir = medium.getDataDirectory(archive);
        File contentFile = new File(metadataDir, medium.getContentFileName(false));
        if (! FileSystemManager.exists(contentFile)) {
            contentFile = new File(metadataDir, medium.getContentFileName(true));
        }
        
        if (FileSystemManager.exists(contentFile)) {
            ArchiveContentAdapter contentAdapter = new ArchiveContentAdapter(contentFile);
            return contentAdapter.readContent();
        } else {
            // Manage Backward compatibility : read from dir/zip content
            ArchiveContent content;
            if (medium  instanceof IncrementalDirectoryMedium) {
                content = backwardCompatibleGetContent((IncrementalDirectoryMedium)medium, archive);
            } else {
                content = backwardCompatibleGetContent((IncrementalZipMedium)medium, archive);                
            }
            
            // Write the content (use a temporary file name which is renamed afterwards)
            File tmpCtn = new File(FileSystemManager.getParentFile(contentFile), FileSystemManager.getName(contentFile) + ".tmp");
            FileSystemManager.delete(tmpCtn);
            ArchiveContentAdapter adapter = new ArchiveContentAdapter(tmpCtn);
            adapter.writeContent(content);
            adapter.close();
            
            FileSystemManager.renameTo(tmpCtn, contentFile);
            
            return content;
        }
    }
    
    private static ArchiveContent backwardCompatibleGetContent(IncrementalDirectoryMedium medium, File archive) throws IOException {
        ArchiveContent content = new ArchiveContent();
        addStoredDirectoryToSet(content, archive, archive);
        return content;
    }
    
    /**
     * This method returns the archive's content in a way which is compatible with older versions of areca.
     */
    private static ArchiveContent backwardCompatibleGetContent(IncrementalZipMedium medium, File archive) throws IOException {       
        throw new UnsupportedOperationException("This version of Areca is not compatible with archives that have been created with versions anterior to v3.5. Use Areca v4.2.3 to read them.");
    }

	private static void addStoredDirectoryToSet(ArchiveContent content, File directory, File root) throws IOException {
		File[] files = FileSystemManager.listFiles(directory);
		for (int i=0; i<files.length; i++) {
			if (FileSystemManager.isFile(files[i])) {
			    content.add(new FileSystemRecoveryEntry(
			            root, 
			            files[i],
			            FileSystemRecoveryEntry.STATUS_STORED,
			            FileSystemManager.length(files[i])
			    ));
			} else {
				addStoredDirectoryToSet(content, files[i], root);
			}
		}
	}
}
