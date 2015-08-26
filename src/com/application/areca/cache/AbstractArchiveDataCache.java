package com.application.areca.cache;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.application.areca.impl.AbstractFileSystemMedium;

/**
 * Abstract cache implementation.
 * <BR>Data are indexed by :
 * <BR>- FileSystemArchiveMedium
 * <BR>- File
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
public abstract class AbstractArchiveDataCache {
    private Map dataByArchiveMedium = new HashMap();
    
    private EvictionManager evictor;
    
    public AbstractArchiveDataCache(EvictionManager evictor) {
        evictor.registerCache(this);
        this.evictor = evictor;
    }
    
    public ArchiveDataMap findOrCreateArchiveDataMap(AbstractFileSystemMedium medium) {
        ArchiveDataMap dataMap = this.findArchiveDataMap(medium);
        if (dataMap == null) {
            dataMap = new ArchiveDataMap();
            dataByArchiveMedium.put(medium, dataMap);
            dataMap.registerUsage();
        }
        return dataMap;
    }
    
    protected ArchiveDataMap findArchiveDataMap(AbstractFileSystemMedium medium) {
        ArchiveDataMap dataMap = (ArchiveDataMap)this.dataByArchiveMedium.get(medium);
        if (dataMap != null) {
            dataMap.registerUsage();
        }
        return dataMap;
    }
    
    public synchronized Object get(AbstractFileSystemMedium medium, File key) {
        ArchiveDataMap map = this.findArchiveDataMap(medium);
        Object returned = null;
        if (map != null) {
            returned = map.get(key);
        }
        
        // Once the object has been retrieved in the cache, check that the memory is OK
        evictor.gcIfNeeded();
        
        return returned;
    }
    
    /**
     * This method registers the object passed as argument in the cache associated to
     * the medium.
     * <BR>Once the object has been referenced, it checks that the cache still can grow.
     * <BR>If it still can grow, the method simply returns true (which means : "GC has not been called")
     * <BR>If it cannot grow anymore, the method returns false (which means : "GC has been called")
     * <BR>
     * <BR>Note that, in both cases, the reference is added to the cache before GC is called.
     */
    public synchronized boolean put(AbstractFileSystemMedium medium, File key, Object data, long approximateDataSize) {
        this.findOrCreateArchiveDataMap(medium).put(key, data, approximateDataSize);
        
        // Once the object has been referenced in the cache, check that the memory is OK
        return evictor.gcIfNeeded();
    }
    
    /**
     * Remove an entry.
     * <BR>Return the removed entry
     */    
    public synchronized Object remove(AbstractFileSystemMedium medium, File key) {
        ArchiveDataMap map = this.findArchiveDataMap(medium);
        if (map == null) {
            return null;
        } else {
            return map.remove(key);
        }
    }    
    
    /**
     * Clear the cache
     */
    public synchronized void removeAllArchiveData() {
        this.dataByArchiveMedium.clear();
    }
    
    /**
     * Clear all data for the medium passed as argument
     */
    public synchronized void removeAllArchiveData(AbstractFileSystemMedium medium) {
        this.dataByArchiveMedium.remove(medium);
    }
    
    /**
     * Return the medium that has been the least accessed ...
     */
    protected synchronized AbstractFileSystemMedium getLeastAccessedNonEmptyMapMedium() {
        double minimum = Double.MAX_VALUE;
        AbstractFileSystemMedium returnMedium = null;
        
        Iterator iter = this.dataByArchiveMedium.entrySet().iterator();
        while (iter.hasNext() && minimum > 0) {
            Map.Entry entry = (Map.Entry)iter.next();
            ArchiveDataMap dataMap = (ArchiveDataMap)entry.getValue();
            AbstractFileSystemMedium medium = (AbstractFileSystemMedium)entry.getKey();
            double score = dataMap.computeScore();
            
            if (score < minimum && (! dataMap.isEmpty())) {
                minimum = score;
                returnMedium = medium;
            }
        }
        
        return returnMedium;
    }
}