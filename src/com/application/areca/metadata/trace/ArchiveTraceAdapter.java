package com.application.areca.metadata.trace;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import com.application.areca.ArchiveMedium;
import com.application.areca.MemoryHelper;
import com.application.areca.impl.FileSystemRecoveryEntry;
import com.application.areca.metadata.AbstractMetadataAdapter;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.util.log.Logger;

/**
 * <BR>File adapter for ArchiveTrace read/write operations.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -3366468978279844961
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
public class ArchiveTraceAdapter extends AbstractMetadataAdapter {

    /**
     * Maximum number of entries that can be securely managed by Areca given the allocated memory
     */
    private static final long MAX_SIZE = MemoryHelper.getMaxManageableEntries();
    
    /**
     * Boolean that sets wether directories must be read or not
     */
    protected boolean trackDirectories;
    protected boolean trackSymlinks;
    protected boolean trackPermissions;
    
    protected ArchiveMedium medium;
    
    public ArchiveTraceAdapter(ArchiveMedium medium, File traceFile) throws IOException {
        this(medium, traceFile, false, false);
    }
    
    public ArchiveTraceAdapter(ArchiveMedium medium, File traceFile, boolean trackDirectories, boolean trackSymlinks) {
        this.medium = medium;
        this.trackDirectories = trackDirectories;
        this.trackSymlinks = trackSymlinks;
        this.file = traceFile;
    }
   
    public void setTrackPermissions(boolean trackPermissions) {
        this.trackPermissions = trackPermissions;
    }
    
    public void writeEntry(FileSystemRecoveryEntry entry) throws IOException {
        initWriter();
        if (FileSystemManager.isFile(entry.getFile()) || trackDirectories) {
            this.writer.write("\r\n" + ArchiveTrace.serialize(entry, trackPermissions, trackSymlinks));
            this.written++;
        }
    }
    
    public void writeTrace(ArchiveTrace traceToWrite) throws IOException {
        initWriter();
        Iterator iter = traceToWrite.fileEntrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            this.writer.write("\r\n" + traceToWrite.buildFileTraceFileString((String)entry.getKey()));
        }
        
        iter = traceToWrite.directoryEntrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            this.writer.write("\r\n" + traceToWrite.buildDirectoryTraceFileString((String)entry.getKey()));
        }
        
        iter = traceToWrite.symLinkEntrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            this.writer.write("\r\n" + traceToWrite.buildSymLinkTraceFileString((String)entry.getKey()));
        }
    }
    
    /**
     * Parses each line initializes the trace content
     * <BR>-Key = file path
     * <BR>-Value = FilePath TRACE_SEP FileSize - FileModificationDate
     */    
    public ArchiveTrace readTrace() throws IOException {
        FileTool tool = FileTool.getInstance();
        String encoding = resolveEncoding();
        String[] traceContent = tool.getInputStreamRows(this.getInputStream(), encoding, true);
        // Check memory usage
        long entries = traceContent.length;
        if (entries > MAX_SIZE) {
            Logger.defaultLogger().displayApplicationMessage(
                    "" + this.medium.getTarget().getUid(),
                   MemoryHelper.getMemoryTitle(this.medium.getTarget(), entries),
                   MemoryHelper.getMemoryMessage(this.medium.getTarget(), entries)
            );
        }
        
        // Parse trace
        ArchiveTrace trace = new ArchiveTrace();
        if (traceContent.length >= 1) {
            traceContent[0] = null; // The first line holds version data
        }
        for (int i=0; i<traceContent.length; i++) {
            if (traceContent[i] != null) {
                trace.deserialize(traceContent[i], this.objectPool);
            }
        }
        Logger.defaultLogger().info("" + traceContent.length + " traces read for " + FileSystemManager.getAbsolutePath(file));
        return trace;
    }
}
