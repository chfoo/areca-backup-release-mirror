package com.application.areca.metadata.trace;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.application.areca.cache.ObjectPool;
import com.application.areca.impl.FileSystemRecoveryEntry;
import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemManager;
import com.myJava.file.attributes.Attributes;
import com.myJava.file.attributes.AttributesHelper;
import com.myJava.util.log.Logger;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4945525256658487980
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
public class ArchiveTrace {
    
    /**
     * File trace separator
     */
    private static final String TRACE_SEP = "#-#";
    
    /**
     * Internal separator
     */
    private static final char INTERNAL_SEP = '-';
    
    /**
     * Approximation of the size in the memory
     */
    private long approximateMemorySize = 0;
    
    /**
     * Directory marker
     */
    private static final String DIRECTORY_MARKER = "!D!";
    
    private Map files;
    private Map directories;
    
    public ArchiveTrace() {
        files = new HashMap();
        directories = new HashMap();
    }
    
    /**
     * Parses the entry's trace and extract its size.
     */
    public static long extractFileSizeFromTrace(String trace) {
        int idx = trace.indexOf(INTERNAL_SEP);
        return Long.parseLong(trace.substring(0, idx));
    }
    
    /**
     * Parses the entry's trace and extract its modification time.
     */
    public static long extractFileModificationDateFromTrace(String trace) {
        int idx1 = trace.indexOf(INTERNAL_SEP);
        int idx2 = trace.indexOf(INTERNAL_SEP, idx1 + 1);
        
        if (idx2 < 0) {
            return Long.parseLong(trace.substring(idx1 + 1));
        } else {
            return Long.parseLong(trace.substring(idx1 + 1, idx2));
        }
    }
    
    public static Attributes extractFileAttributesFromTrace(String trace) {
        int idx1 = trace.indexOf(INTERNAL_SEP);
        int idx2 = trace.indexOf(INTERNAL_SEP, idx1 + 1);
        
        if (idx2 < 0) {
            return null;
        } else {
            return AttributesHelper.deserialize(trace.substring(idx2 + 1));
        }
    }
    
    public static long extractDirectoryModificationDateFromTrace(String trace) {
        int idx1 = trace.indexOf(INTERNAL_SEP);
        if (idx1 < 0) {
            return -1;
        } else {
            return Long.parseLong(trace.substring(0, idx1));
        }
    }
    
    public static Attributes extractDirectoryAttributesFromTrace(String trace) {
        int idx1 = trace.indexOf(INTERNAL_SEP);
        if (idx1 < 0) {
            return null;
        } else {
            return AttributesHelper.deserialize(trace.substring(idx1 + 1));            
        }
    }
    
    public Set fileEntrySet() {
        return this.files.entrySet();
    }
    
    public Set directoryEntrySet() {
        return this.directories.entrySet();
    }
    
    public Set fileKeySet() {
        return this.files.keySet();
    }
    
    public int fileSize() {
        return files.size();
    }
    
    public Set getDirectoryList() {
        return directories.keySet();
    }
    
    public Map getFileMap() {
        return this.files;
    }
    
    public void merge(ArchiveTrace other, boolean mergeDirectories) {
        this.files.putAll(other.files);
        if (mergeDirectories) {
            this.directories.putAll(other.directories);
        }
    }
    
    /**
     * Builds the key + hash
     */
    protected static String serialize(FileSystemRecoveryEntry fEntry, boolean trackPermissions) {
        try {
            if (fEntry == null) {
                return null;
            } else if (FileSystemManager.isFile(fEntry.getFile())) {
                StringBuffer sb = new StringBuffer()
                .append(fEntry.getName())
                .append(TRACE_SEP)
                .append(hash(fEntry));
                
                if (trackPermissions) {
                    sb.append(INTERNAL_SEP)
                    .append(AttributesHelper.serialize(FileSystemManager.getAttributes(fEntry.getFile())));
                }
                return sb.toString();
            } else {
                StringBuffer sb = new StringBuffer()
                .append(DIRECTORY_MARKER)
                .append(fEntry.getName())
                .append(TRACE_SEP)
                .append(FileSystemManager.lastModified(fEntry.getFile()));
                
                if (trackPermissions) {
                    sb.append(INTERNAL_SEP)
                    .append(AttributesHelper.serialize(FileSystemManager.getAttributes(fEntry.getFile())));
                }
                return sb.toString();
            }
        } catch (IOException e) {
            Logger.defaultLogger().error(e);
            throw new IllegalArgumentException(fEntry.getName());
        }
    }  
    
    /**
     * Builds the hash key
     */
    protected static String hash(FileSystemRecoveryEntry fEntry) {
        if (fEntry == null) {
            return null;
        } else if (FileSystemManager.isFile(fEntry.getFile())) {
            return new StringBuffer()
            .append(fEntry.getSize())
            .append(INTERNAL_SEP)
            .append(FileSystemManager.lastModified(fEntry.getFile()))
            .toString();
        } else {
            throw new IllegalArgumentException("Only files are accepted");
        }
    }  
    
    protected void deserialize(String str, ObjectPool pool) {       
        // Format : <Path><TRACE_SEP><hash>     
        int index = str.indexOf(ArchiveTrace.TRACE_SEP);
        String key;
        String hash;
        if (index == -1) {
            key = str;
            hash = null;
        } else {
            key = str.substring(0, index);
            hash = str.substring(index + ArchiveTrace.TRACE_SEP.length());
        }
        
        key = FileNameUtil.normalizePath(key);
        
        if (str.startsWith(ArchiveTrace.DIRECTORY_MARKER)) {
            // CASE 1 : DIRECTORY
            registerDirectory(
                    key.substring(ArchiveTrace.DIRECTORY_MARKER.length()),
                    hash,
                    pool);
        } else {
            // CASE 2 : FILE
            
            // Backward compatibility (older versions store a trailing "/" for each row) 
            if (FileNameUtil.startsWithSeparator(str)) {
                str= str.substring(1);
            }
            registerFile(key, hash, pool);
        }       
    }
    
    private void registerFile(String key, String hash, ObjectPool pool) {    
        if (pool != null) {
            key = (String)pool.intern(key);
            hash = (String)pool.intern(hash);
        }
        this.files.put(key, hash);
        this.approximateMemorySize += measureString(key) + measureString(hash);
    }
    
    private void registerDirectory(String key, String hash, ObjectPool pool) {
        if (pool != null) {
            key = (String)pool.intern(key);
            hash = (String)pool.intern(hash);
        }
        this.directories.put(key, hash);
    }
    
    private long measureString(String s) {
        return s == null ? 0 : s.length();
    }
    
    /**
     * Retrieves the trace file content to be serialized
     */
    protected String buildTraceFileString(String entryKey) {
        if (entryKey == null) {
            return null;
        } else if (this.files.containsKey(entryKey)) {
            return entryKey + TRACE_SEP + (String)this.files.get(entryKey);
        } else {
            return DIRECTORY_MARKER + entryKey;
        }
    }  
    
    public boolean containsKey(String key) {
        return this.files.containsKey(key);
    }
    
    public long getApproximateMemorySize() {
        double ratio = ((double) (this.files.size() + this.directories.size()) / ((double)Math.max(this.files.size(), 1)));
        return (long)(approximateMemorySize * ratio);
    }
    
    /**
     * Checks wether the entry has been modified
     */
    public boolean contains(FileSystemRecoveryEntry fEntry) {
        return this.containsKey(fEntry.getName());
    }
    
    public String getFileHash(FileSystemRecoveryEntry fEntry) {
        return getFileHash(fEntry.getName());        
    }
    
    public String getFileHash(String entryName) {
        return (String)this.files.get(entryName);        
    }
    
    public String getDirectoryHash(FileSystemRecoveryEntry fEntry) {
        return getDirectoryHash(fEntry.getName());        
    }
    
    public String getDirectoryHash(String entryName) {
        return (String)this.directories.get(entryName);        
    }
    
    public Object remove(FileSystemRecoveryEntry entry) {
        String key = entry.getName();
        String hash = (String)files.remove(key);
        this.approximateMemorySize -= measureString(key) + measureString(hash);
        
        return hash;
    }
    
    /**
     * Checks wether the entry has been modified
     */
    public boolean hasBeenModified(FileSystemRecoveryEntry fEntry) {
        return
        ! (
                ((String)files.get(fEntry.getName()) + INTERNAL_SEP)
                .startsWith(hash(fEntry) + INTERNAL_SEP)
        );
    }
    
    public ArchiveTrace cloneTrace() {
        ArchiveTrace ret = new ArchiveTrace();
        
        ret.files.putAll(this.files);
        ret.directories.putAll(this.directories);
        ret.approximateMemorySize = this.approximateMemorySize;
        
        return ret;
    }
}
