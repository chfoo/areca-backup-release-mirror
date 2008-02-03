package com.application.areca.metadata.trace;

import java.io.File;
import java.io.IOException;

import com.application.areca.cache.ObjectPool;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.impl.IncrementalDirectoryMedium;
import com.application.areca.impl.IncrementalZipMedium;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;

/**
 * This class manages accesses to archives' traces.
 * <BR>It includes backward-compatibility management.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 8290826359148479344
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
public class ArchiveTraceManager {
    protected static FileTool tool = FileTool.getInstance();

    public static ArchiveTrace readTraceForArchive(AbstractIncrementalFileSystemMedium medium, File archive, ObjectPool pool) throws IOException {
        if (! medium.checkArchiveCompatibility(archive)) {
            return null;
        }
        
        File dataDir = medium.getDataDirectory(archive);
        File traceFile = new File(dataDir, medium.getTraceFileName(false));
        if (FileSystemManager.exists(traceFile)) {
            // CurrentFormat
            return currentReadTrace(medium, traceFile, pool);
        } else {
            File traceFileOldFormat = new File(dataDir, medium.getTraceFileName(true));
            if (FileSystemManager.exists(traceFileOldFormat)) {
                // Older format : trace stored with old file name
                return currentReadTrace(medium, traceFileOldFormat, pool);
            } else {
                // Oldest format : trace stored in archive with old file name
	            // Read from dir/zip content
	            ArchiveTrace trace;
	            if (medium  instanceof IncrementalDirectoryMedium) {
	                trace = backwardCompatibleReadTrace((IncrementalDirectoryMedium)medium, archive, pool);
	            } else {
	                trace = backwardCompatibleReadTrace((IncrementalZipMedium)medium, archive, pool);                
	            }
	            
	            // Write the trace (use a temporary file name which is renamed afterwards)
	            File tmpTrc = new File(FileSystemManager.getParentFile(traceFileOldFormat), FileSystemManager.getName(traceFileOldFormat) + ".tmp");
	            FileSystemManager.delete(tmpTrc);
	            ArchiveTraceAdapter adapter = new ArchiveTraceAdapter(medium, tmpTrc);
	            adapter.writeTrace(trace);
	            adapter.close();
	            
	            FileSystemManager.renameTo(tmpTrc, traceFile);
	            
	            return trace;
	        }
        }
    }
    
    private static ArchiveTrace currentReadTrace(AbstractIncrementalFileSystemMedium medium, File traceFile, ObjectPool pool) throws IOException {
        ArchiveTraceAdapter adapter = new ArchiveTraceAdapter(medium, traceFile);
        adapter.setObjectPool(pool);
        return adapter.readTrace();
    }
    
    private static ArchiveTrace backwardCompatibleReadTrace(IncrementalDirectoryMedium medium, File archive, ObjectPool pool) throws IOException {
        File traceFile = new File(archive, medium.getTraceFileName(true));
        if (FileSystemManager.exists(traceFile)) {
            ArchiveTraceAdapter adapter = new ArchiveTraceAdapter(medium, traceFile);
            adapter.setObjectPool(pool);
            adapter.setCompressed(false); // older versions are not compressed
	        return adapter.readTrace();
        } else {
            return new ArchiveTrace();
        }
    }
    
    /**
     * This method reads the archive's trace in a way which is compatible with older versions of areca.
     */
    private static ArchiveTrace backwardCompatibleReadTrace(IncrementalZipMedium medium, File archive, ObjectPool pool) throws IOException {       
        throw new UnsupportedOperationException("This version of Areca is not compatible with archives that have been created with versions anterior to v3.5. Use Areca v4.2.3 to read them.");
    }
}
