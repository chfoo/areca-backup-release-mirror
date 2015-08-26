package com.application.areca.metadata.trace;

import java.io.File;
import java.io.IOException;

import com.application.areca.impl.AbstractFileSystemMedium;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.myJava.file.FileTool;

/**
 * This class manages accesses to archives' traces.
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
public class ArchiveTraceManager {
    protected static FileTool tool = FileTool.getInstance();
    
    public static File resolveTraceFileForArchive(AbstractIncrementalFileSystemMedium medium, File archive) 
    throws IOException {
        if (! medium.checkArchiveCompatibility(archive, true)) {
            return null;
        }
        File dataDir = AbstractFileSystemMedium.getDataDirectory(archive);
        return new File(dataDir, medium.getTraceFileName());
    }
    
    public static TraceFileIterator buildIteratorForArchive(AbstractIncrementalFileSystemMedium medium, File archive) throws IOException {
    	File traceFile = resolveTraceFileForArchive(medium, archive);
    	return ArchiveTraceAdapter.buildIterator(traceFile);
    }
}
