package com.application.areca;

import com.myJava.system.OSTool;



/**
 * Static class that provides helper methods for memory management.
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
public class MemoryHelper {

    private static double MEMORY_BY_ENTRY_KB = ArecaConfiguration.get().getMemoryByEntryKb();
    private static long MEMORY_BASE_KB = ArecaConfiguration.get().getMemoryBaseKb();
    
    private static double MEMORY_SAFETY_MARGIN = ArecaConfiguration.get().getMemorySafetyMargin();
    private static double MEMORY_USAGE_RATE = 1 - MEMORY_SAFETY_MARGIN;  
    
    public static long getMaxManageableEntries() {
        double nb = OSTool.getMaxMemoryKB() * MEMORY_USAGE_RATE - MEMORY_BASE_KB;
        return (long)(nb / MEMORY_BY_ENTRY_KB);
    }

    /**
     * Returns the ratio of memory which is kept free for Areca.
     * <BR>For instance a ratio of 0.3 means that Areca's caching strategy will always attempts to keep 30% of
     * the overall memory available, and will trigger a GC if the free memory falls below this limit.
     */
    public static double getMemorySafetyMargin() {
        return MEMORY_SAFETY_MARGIN;
    }
}
