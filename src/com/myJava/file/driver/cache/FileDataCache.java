package com.myJava.file.driver.cache;

import java.util.StringTokenizer;

import com.myJava.util.Util;


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
public class FileDataCache {
    private String root;
    private DataEntry rootEntry;
    private int maxDepth;

    public FileDataCache(String root, int maxDepth) {
        this.root = root;
        this.maxDepth = maxDepth;
    }
    
    private void setRootEntry(DataEntry rt) {
        if (rt == null) {
            throw new IllegalArgumentException("Invalid data.");
        }
        this.rootEntry = rt;
    }
    
    public int computeSize() {
        return rootEntry == null ? 0 : (1 + rootEntry.computeChildren());
    }
    
    public void clear() {
        this.rootEntry.clearChildren();
    }

    public DataEntry lookup(String fullPath) throws NonExistingEntryException, MaxDepthReachedException {
        String path = normalizePath(fullPath);
        checkDepthLocal(path);
        return lookupByLocalPath(path);
    }

    public DataEntry lookupParent(String fullPath) throws NonExistingEntryException {
        String path = getParentPathNormalized(normalizePath(fullPath));
        return lookupByLocalPath(path);
    }
    
    public DataEntry put(String fullPath) throws MaxDepthReachedException {
        return putByLocalPath(normalizePath(fullPath), null);
    }
    
    private DataEntry putByLocalPath(String localPath, DataEntry entry) throws MaxDepthReachedException {
        checkDepthLocal(localPath);
        
        if (entry == null) {
            entry = new DataEntry();
        }
        
        if (localPath == null) {
            this.setRootEntry(entry);
            return entry;
        } else {
            String parentPath = getParentPathNormalized(localPath);
            DataEntry dir = null;

            try {
                dir = lookupByLocalPath(parentPath);
            } catch (NonExistingEntryException e) {
            }
            
            if (dir == null) {
                dir = new DataEntry();
                dir = putByLocalPath(parentPath, dir);
            } else if (dir.isExistsSet() && (! dir.isExists())) {
                throw new IllegalArgumentException("The parent does not exist : " + localPath);
            }
            
            String name = getNameNormalized(localPath);
            DataEntry existing = null;
            try {
                existing = dir.getEntry(name);
            } catch (NonExistingEntryException e) {
            }
            if (existing != null) {
                throw new IllegalArgumentException("There is already an entry at " + localPath + " : " + existing.toString());
            } else {
                dir.putEntry(name, entry);   
                return entry;
            }
        }
    }
    
    private String getParentPathNormalized(String localPath) {
        int index = localPath.lastIndexOf('/');
        
        if (index == -1) {
            return null;
        } else {
            String ret = localPath.substring(0, index);
            if (ret.length() == 0) {
                ret = null;
            }
            return ret;
        }
    }
    
    private String getNameNormalized(String localPath) {
        int index = localPath.lastIndexOf('/');
        String ret = localPath.substring(index + 1);  
        return ret;
    }
    
    private DataEntry lookupByLocalPath(String path) throws NonExistingEntryException {
        if (path == null) {
            return rootEntry;
        }
        
        StringTokenizer stt = new StringTokenizer(path, "/\\");
        DataEntry entry = rootEntry;
        while (stt.hasMoreTokens()) {
            if (entry == null) {
                return null;
            } else {
                entry = entry.getEntry(stt.nextToken());
            }
        }
        return entry;
    }

    private String normalizePath(String fullPath) {
        if (! fullPath.startsWith(root)) {
            throw new IllegalArgumentException("Invalid path : [" + fullPath + "]. It should begin with [" + root + "].");
        }
        String ret = fullPath.substring(root.length());
        if (ret.length() == 0) {
            ret = null;
        }

        return ret;
    }
    
    public void checkDepth(String fullPath) throws MaxDepthReachedException {
        checkDepthLocal(normalizePath(fullPath));
    }
    
    private void checkDepthLocal(String path) throws MaxDepthReachedException {
        int depth = Util.count(path, 0, '/');
        if (depth > maxDepth) {
            throw new MaxDepthReachedException();
        }
    }
}
