package com.application.areca.metadata.content;

import java.io.File;
import java.io.IOException;

import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.impl.handler.DeltaArchiveHandler;

/**
 * This class manages accesses to archives' metadata.
 * <BR>It includes backward-compatibility management.
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
public class ArchiveContentManager {
    
    public static File resolveContentFileForArchive(AbstractIncrementalFileSystemMedium medium, File archive) throws IOException {
        if (! medium.checkArchiveCompatibility(archive, true)) {
            return null;
        }
    	File metadataDir = medium.getDataDirectory(archive);
        return new File(metadataDir, medium.getContentFileName());
    }
    
    public static File resolveHashFileForArchive(AbstractIncrementalFileSystemMedium medium, File archive) throws IOException {
        if (! medium.checkArchiveCompatibility(archive, true)) {
            return null;
        }
    	File metadataDir = medium.getDataDirectory(archive);
        return new File(metadataDir, medium.getHashFileName());
    }
    
    public static File resolveSequenceFileForArchive(AbstractIncrementalFileSystemMedium medium, File archive) throws IOException {
        if (! medium.checkArchiveCompatibility(archive, true)) {
            return null;
        }
    	File metadataDir = medium.getDataDirectory(archive);
        DeltaArchiveHandler handler = (DeltaArchiveHandler)medium.getHandler();
        return new File(metadataDir, handler.getSequenceFileName());
    }
    
    public static ContentFileIterator buildIteratorForArchive(AbstractIncrementalFileSystemMedium medium, File archive) throws IOException {
    	File file = resolveContentFileForArchive(medium, archive);
    	return ArchiveContentAdapter.buildIterator(file);
    }
}
