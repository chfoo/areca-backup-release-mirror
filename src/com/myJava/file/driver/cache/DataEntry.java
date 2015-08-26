package com.myJava.file.driver.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.myJava.object.ToStringHelper;

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
public class DataEntry {
   
    protected static final int UNSET = -1;
    protected long length = UNSET;

    private long lastModified = UNSET;
    private boolean readable;
    private boolean writable;
    private boolean hidden;
    private boolean directory;
    private boolean exists;
    private short type;
    
    private boolean readableSet;
    private boolean writableSet;
    private boolean hiddenSet;
    private boolean directorySet;
    private boolean existsSet;
    private boolean typeSet;
    
    private DataEntry parent = null;
    
    private boolean populated = false;
    private Map contentMap;
    
    private Set populatedFilters = null;

    public DataEntry() {
    }

    public long getLength() {
        return length;
    }

    private void setParent(DataEntry parent) {
        this.parent = parent;
        
        this.parent.setDirectory(true);
        if (this.existsSet && this.exists) {
            this.parent.setExists(true);
        }
    }

    public int getDepth() {
        if (parent == null) {
            return 0;
        } else {
            return parent.getDepth() + 1;
        }
    }

    public void setLength(long length) {
        this.length = length;
    }
    
    private synchronized Map getOrCreateContent() {
        if (contentMap == null) {
            contentMap = new HashMap();
        }
        return contentMap;
    }
    
    public synchronized int computeChildren() {
        if (this.contentMap == null || this.contentMap.isEmpty()) {
            return 0;
        } else {
            Iterator iter = this.contentMap.values().iterator();
            int n=0;
            while (iter.hasNext()) {
                DataEntry entry = (DataEntry)iter.next();
                n+= entry.computeChildren() + 1;
            }
            return n;
        }
    }
    
    public synchronized void clearChildren() {
        this.contentMap = null;
        this.populated = false;
        this.populatedFilters = null;
    }
    
    public synchronized void putEntry(String name, DataEntry entry) {
        if (this.directorySet && (! this.directory)) {
            throw new IllegalArgumentException("This entry is a directory.");
        }
        if (getOrCreateContent().put(name, entry) != null) {
            throw new IllegalStateException("An entry already exists.");
        }
        entry.setParent(this);
    }
    
    /**
     * Returns :
     * <BR>-An entry if it is found
     * <BR>-Null if nothing was found in the cache we're not sure whether the file/directory exists
     * <BR>
     * <BR>Throw a NonExistingEntryException if the entry does not exist
     */
    public synchronized DataEntry getEntry(String name) throws NonExistingEntryException {
        DataEntry entry = null;
        if (this.contentMap != null) {
            entry = (DataEntry)this.contentMap.get(name);
        }
        if (entry == null && populated) {
            throw new NonExistingEntryException(name + " does not exist");
        }
        
        return entry;
    }
    
    public synchronized Set getNames() {
        if (this.directorySet && (! this.directory)) {
            return new HashSet();
        } else {
            return this.getOrCreateContent().keySet();
        }
    }

    public synchronized void setPopulated() {
        this.populated = true;
        this.populatedFilters = null;
    }
    
    public synchronized void setPopulated(Object filter) {
        if (filter == null) {
            this.setPopulated();
        } else if (! populated) {
            getOrCreatePopulatedFilters().add(filter);
        }
    }

    public boolean isPopulated() {
        return populated;
    }
    
    public synchronized boolean isPopulated(Object filter) {
        if (populated || filter == null) {
            return populated;
        } else if (populatedFilters == null) {
            return false;
        } else {
            return populatedFilters.contains(filter);
        }
    }
    
    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        if (isSet(lastModified)) {
            ToStringHelper.append("lastModified", lastModified, sb);
        }
        if (isSet(length)) {
            ToStringHelper.append("length", length, sb);
        }
        if (typeSet) {
            ToStringHelper.append("type", type, sb);
        }
        if (readableSet) {
            ToStringHelper.append("readable", readable, sb);
        }
        if (writableSet) {
            ToStringHelper.append("writable", writable, sb);
        }
        if (hiddenSet) {
            ToStringHelper.append("hidden", hidden, sb);
        }
        if (directorySet) {
            ToStringHelper.append("directory", directory, sb);
        }
        if (existsSet) {
            ToStringHelper.append("exists", exists, sb);
        }
        ToStringHelper.append("populated", populated, sb);
        if (contentMap != null) {
            ToStringHelper.append("content size", contentMap.size(), sb);
        }
        return ToStringHelper.close(sb);
    }
    
    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
    
    public boolean isSet(long tested) {
        return tested != UNSET;
    }

    private synchronized Set getOrCreatePopulatedFilters() {
        if (populatedFilters == null) {
            this.populatedFilters = new HashSet();
        }
        
        return populatedFilters;
    }

    public boolean isExists() {
        if (! existsSet) {
            throw new IllegalStateException();
        }
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
        this.existsSet = true;
        
        if (exists) {
            if (this.parent != null) {
                this.parent.setExists(true);
                this.parent.setDirectory(true);
            }
        } else {
            this.clearChildren();
            this.setPopulated();
        }
    }
    
    public void reset() {
        this.length = UNSET;
        this.lastModified = UNSET;
        this.directorySet = false;
        this.existsSet = false;
        this.hiddenSet = false;
        this.readableSet = false;
        this.typeSet = false;
        this.writableSet = false;
        this.clearChildren();
    }

    public boolean isExistsSet() {
        return existsSet;
    }

    public boolean isHidden() {
        if (! hiddenSet) {
            throw new IllegalStateException();
        }
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
        this.hiddenSet = true;
    }
    
    public void setDirectory(boolean directory) {
        this.directory = directory;
        this.directorySet = true;
    }

    public boolean isReadable() {
        if (! readableSet) {
            throw new IllegalStateException();
        }
        return readable;
    }

    public void setReadable(boolean readable) {
        this.readable = readable;
        this.readableSet = true;
    }
    
    public short getType() {
        if (! typeSet) {
            throw new IllegalStateException();
        }
        return type;
    }
    
    public void setType(short type) {
        this.type = type;
        this.typeSet = true;
    }

    public boolean isWritable() {
        if (! writableSet) {
            throw new IllegalStateException();
        }
        return writable;
    }

    public boolean isDirectory() {
        if (! directorySet) {
            throw new IllegalStateException();
        }
        return directory;
    }

    public void setWritable(boolean writable) {
        this.writable = writable;
        this.writableSet = true;
    }

    public boolean isHiddenSet() {
        return hiddenSet;
    }

    public boolean isReadableSet() {
        return readableSet;
    }
    
    public boolean isTypeSet() {
        return typeSet;
    }

    public boolean isWritableSet() {
        return writableSet;
    }

    public boolean isDirectorySet() {
        return directorySet;
    }
    
    public void update(DataEntry o) {
        this.directory = o.directory;
        this.directorySet = o.directorySet;
        this.exists = o.exists;
        this.existsSet = o.existsSet;
        this.hidden = o.hidden;
        this.hiddenSet = o.hiddenSet;
        this.readable = o.readable;
        this.readableSet = o.readableSet;
        this.typeSet = o.typeSet;
        this.type = o.type;
        this.writable = o.writable;
        this.writableSet = o.writableSet;
        this.lastModified = o.lastModified;
        this.length = o.length;
        
        if (o.contentMap != null) {
            Iterator iter = o.contentMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry)iter.next();
                String name = (String)entry.getKey();                
                DataEntry data = (DataEntry)entry.getValue();
                this.putEntry(name, data);
            }
        }
        
        this.populated = o.populated;
        this.populatedFilters = o.populatedFilters;
    }
}
