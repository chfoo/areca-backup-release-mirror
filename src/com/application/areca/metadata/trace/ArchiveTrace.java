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
 * Trace format : 
 * File :       <SIZE>#-#<LASTMODIF>#-#<ATTRIBUTES>(optional)
 * Dir :                    !D!<LASTMODIF>#-#<ATTRIBUTES>(optional)
 * Link :                  !S!<PATH> 
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -2622785387388097396
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
    
    /**
     * Symlink marker
     */
    private static final String SYMLINK_MARKER = "!S!";
    
    private static final char SL_DIR_PREFIX = 'd';
    private static final char SL_FILE_PREFIX = 'f';
    
    private Map files;
    private Map directories;
    private Map symLinks;
    
    public ArchiveTrace() {
        files = new HashMap();
        directories = new HashMap();
        symLinks = new HashMap();
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
    
    public static String extractSymLinkPathFromTrace(String trace) {
        return trace.substring(1);
    }
    
    public static boolean extractSymLinkFileFromTrace(String trace) {
        return trace.charAt(0) == SL_FILE_PREFIX;
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
    
    public Set symLinkEntrySet() {
        return this.symLinks.entrySet();
    }
    
    public Set fileKeySet() {
        return this.files.keySet();
    }
    
    public int fileSize() {
        return files.size();
    }
    
    public int directorySize() {
        return directories.size();
    }
    
    public int symLinkSize() {
        return symLinks.size();
    }
    
    public Set getDirectoryList() {
        return directories.keySet();
    }
    
    public Map getFileMap() {
        return this.files;
    }
    
    public Map getDirectoryMap() {
        return this.directories;
    }
    
    public Map getSymLinkMap() {
        return this.symLinks;
    }
    
    public void merge(ArchiveTrace other) {
        this.files.putAll(other.files);
        this.directories.putAll(other.directories);
        this.symLinks.putAll(other.symLinks);
    }
    
    /**
     * Builds the key + hash
     */
    protected static String serialize(FileSystemRecoveryEntry fEntry, boolean trackPermissions, boolean trackSymlinks) {
        try {
            StringBuffer sb = new StringBuffer();
            if (fEntry == null) {
                return null;
            } else if (trackSymlinks && FileSystemManager.isLink(fEntry.getFile())) {      
                sb
                .append(SYMLINK_MARKER)                
                .append(fEntry.getName())
                .append(TRACE_SEP)
                .append(hash(fEntry, true));  
            } else if (FileSystemManager.isFile(fEntry.getFile())) {
                sb
                .append(fEntry.getName())
                .append(TRACE_SEP)
                .append(hash(fEntry, false));
                
                if (trackPermissions) {
                    sb.append(INTERNAL_SEP)
                    .append(AttributesHelper.serialize(FileSystemManager.getAttributes(fEntry.getFile())));
                }
            } else {
                sb
                .append(DIRECTORY_MARKER)
                .append(fEntry.getName())
                .append(TRACE_SEP)
                .append(FileSystemManager.lastModified(fEntry.getFile()));
                
                if (trackPermissions) {
                    sb.append(INTERNAL_SEP)
                    .append(AttributesHelper.serialize(FileSystemManager.getAttributes(fEntry.getFile())));
                }
            }
            
            return sb.toString();
        } catch (IOException e) {
            Logger.defaultLogger().error(e);
            throw new IllegalArgumentException(fEntry.getName());
        }
    }  
    
    /**
     * Builds the hash key
     */
    protected static String hash(FileSystemRecoveryEntry fEntry, boolean asLink) throws IOException {
        if (fEntry == null) {
            return null;
        } else if (asLink) {
            char prefix;
            if (FileSystemManager.isFile(fEntry.getFile())) {
                prefix = SL_FILE_PREFIX;
            } else {
                prefix = SL_DIR_PREFIX;
            }
            return prefix + FileSystemManager.getCanonicalPath(fEntry.getFile());
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
        } else if (str.startsWith(ArchiveTrace.SYMLINK_MARKER)) {
            // CASE 2 : SYMLINK
            registerSymLink(
                    key.substring(ArchiveTrace.SYMLINK_MARKER.length()),
                    hash,
                    pool);
        } else {
            // CASE 3 : FILE
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
    
    private void registerSymLink(String key, String hash, ObjectPool pool) {
        if (pool != null) {
            key = (String)pool.intern(key);
            hash = (String)pool.intern(hash);
        }
        this.symLinks.put(key, hash);
    }
    
    private long measureString(String s) {
        return s == null ? 0 : s.length();
    }
    
    /**
     * Retrieves the trace file content to be serialized
     */
    protected String buildFileTraceFileString(String entryKey) {
        if (entryKey == null) {
            return null;
        } else {
            return entryKey + TRACE_SEP + (String)this.files.get(entryKey);
        }
    }  
    
    /**
     * Retrieves the trace file content to be serialized
     */
    protected String buildDirectoryTraceFileString(String entryKey) {
        if (entryKey == null) {
            return null;
        } else {
            return DIRECTORY_MARKER + entryKey + TRACE_SEP + (String)this.directories.get(entryKey);
        }
    }  
    
    /**
     * Retrieves the trace file content to be serialized
     */
    protected String buildSymLinkTraceFileString(String entryKey) {
        if (entryKey == null) {
            return null;
        } else {
            return SYMLINK_MARKER + entryKey + TRACE_SEP + (String)this.symLinks.get(entryKey);
        }
    }  
    
    public boolean containsFileKey(String key) {
        return this.files.containsKey(key);
    }
    
    public boolean containsDirectoryKey(String key) {
        return this.directories.containsKey(key);
    }
    
    public boolean containsSymLinkKey(String key) {
        return this.symLinks.containsKey(key);
    }
    
    public long getApproximateMemorySize() {
        double ratio = ((double) (this.files.size() + this.directories.size()) / ((double)Math.max(this.files.size(), 1)));
        return (long)(approximateMemorySize * ratio);
    }

    public boolean containsFile(FileSystemRecoveryEntry fEntry) {
        return this.containsFileKey(fEntry.getName());
    }
    
    public boolean containsDirectory(FileSystemRecoveryEntry fEntry) {
        return this.containsDirectoryKey(fEntry.getName());
    }
    
    public boolean containsSymLink(FileSystemRecoveryEntry fEntry) {
        return this.containsSymLinkKey(fEntry.getName());
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
    
    public String getSymLinkHash(FileSystemRecoveryEntry fEntry) {
        return getSymLinkHash(fEntry.getName());        
    }
    
    public String getSymLinkHash(String entryName) {
        return (String)this.symLinks.get(entryName);        
    }
    
    public Object removeFile(FileSystemRecoveryEntry entry) {
        String key = entry.getName();
        String hash = (String)files.remove(key);
        this.approximateMemorySize -= measureString(key) + measureString(hash);
        
        return hash;
    }
    
    public Object removeDirectory(FileSystemRecoveryEntry entry) {
        String key = entry.getName();
        return (String)directories.remove(key);
    }
    
    public Object removeSymLink(FileSystemRecoveryEntry entry) {
        String key = entry.getName();
        return (String)symLinks.remove(key);
    }
    
    /**
     * Checks wether the entry has been modified
     */
    public boolean hasFileBeenModified(FileSystemRecoveryEntry fEntry) throws IOException {
        return
        ! (
                ((String)files.get(fEntry.getName()) + INTERNAL_SEP)
                .startsWith(hash(fEntry, false) + INTERNAL_SEP)
        );
    }
    
    public boolean hasSymLinkBeenModified(FileSystemRecoveryEntry fEntry) throws IOException {
        return
        ! (
                ((String)symLinks.get(fEntry.getName()) + INTERNAL_SEP)
                .startsWith(hash(fEntry, true) + INTERNAL_SEP)
        );
    }
    
    public ArchiveTrace cloneTrace() {
        ArchiveTrace ret = new ArchiveTrace();
        
        ret.files.putAll(this.files);
        ret.directories.putAll(this.directories);
        ret.symLinks.putAll(this.symLinks);
        ret.approximateMemorySize = this.approximateMemorySize;
        
        return ret;
    }
}
