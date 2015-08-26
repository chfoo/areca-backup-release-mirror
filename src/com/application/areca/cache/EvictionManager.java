package com.application.areca.cache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.application.areca.LogHelper;
import com.application.areca.MemoryHelper;
import com.application.areca.impl.AbstractFileSystemMedium;
import com.myJava.system.OSTool;
import com.myJava.util.log.Logger;

/**
 * Eviction manager for ArchiveDataCache class
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
public class EvictionManager {
    private static final double TARGET_CACHE_MEMORY_USAGE_RATIO = 0.5;
    
    private static EvictionManager instance = new EvictionManager();
    
    private List caches = new ArrayList();
    
    public EvictionManager() {
    }
    
    public static EvictionManager getInstance() {
        return instance;
    }
    
    public synchronized void registerCache(AbstractArchiveDataCache cache) {
        this.caches.add(cache);
    }
    
    public synchronized boolean gcIfNeeded() {    	
        if (! canGrow(1 - MemoryHelper.getMemorySafetyMargin())) {
            this.gc();
            return false;
        } else {
            return true;
        }
    }
    
    private static final boolean TH_DUMP = false;
    
    /**
     * Clears data caches until the memory usage is below the target memory ratio.
     */
    private synchronized void gc() {
        Logger.defaultLogger().info("GC started : Memory usage = " + OSTool.getMemoryUsage(), this.getClass().getName());
        
        while (! canGrow(TARGET_CACHE_MEMORY_USAGE_RATIO)) {
            //Logger.defaultLogger().info("Performing a VM garbage collect.", this.getClass().getName());
            Runtime.getRuntime().gc(); // First try : standard VM gc ...
            
            if (! canGrow(TARGET_CACHE_MEMORY_USAGE_RATIO)) {
                Logger.defaultLogger().info("Performing a cache eviction.", this.getClass().getName());
                
                if (TH_DUMP) {
                	LogHelper.logThreadInformations();
                }
                
                if (! freeSomeMemory()) {
                    break;
                }
            }
        }
        
        Logger.defaultLogger().info("GC terminated : Memory usage = " + OSTool.getMemoryUsage(), this.getClass().getName());
    }
    
    /**
     * Frees memory by destroying some cached data
     * <BR>Returns :
     * <BR>- True if data has been successfully destroyed
     * <BR>- False if no destroyable data has been found
     */
    private boolean freeSomeMemory() {
        Iterator iter = this.caches.iterator();
        double candidateScore = Double.MAX_VALUE;
        AbstractArchiveDataCache candidateCache = null;
        AbstractFileSystemMedium candidateMedium = null;
        
        // Finds the {cache, medium} pair with the least score
        while (iter.hasNext() && candidateScore > 0) {
            AbstractArchiveDataCache cache = (AbstractArchiveDataCache)iter.next();
            AbstractFileSystemMedium medium = cache.getLeastAccessedNonEmptyMapMedium();
            
            if (medium != null) {
	            double score = cache.findArchiveDataMap(medium).computeScore();
	            //Logger.defaultLogger().info("      > candidate cache : " + cache.getClass().getName() + " - medium : " + medium.getDescription() + " - Score = " + score);
	            if (score < candidateScore) {
	                candidateScore = score;
	                candidateCache = cache;
	                candidateMedium = medium;
	            }
            } else {
                Logger.defaultLogger().info("      (No candidate data in cache : " + cache.getClass().getName() + ")");
            }
        }

        // Clear this {cache, medium} pair
        if (candidateMedium != null) {
            Logger.defaultLogger().info("Cleaning the cache : " + candidateCache.getClass().getName() + " - for medium : " + candidateMedium.getDescription() + " - Score = " + candidateScore);
            ArchiveDataMap mapToClear = candidateCache.findArchiveDataMap(candidateMedium);
            mapToClear.clear();
            return true;
        } else {
            Logger.defaultLogger().info("No more data to clean in caches.");
            return false; // No more maps to clear : exit gc
        }
    }
    
    /**
     * Tells wether there is enough physical memory to let the cache grow.
     * <BR>This decision is based on the cache's memoryRatio attribute.
     */
    private synchronized boolean canGrow(double memoryRatio) {
        return OSTool.getMemoryUsage() < memoryRatio * OSTool.getMaxMemory();
    }
}
