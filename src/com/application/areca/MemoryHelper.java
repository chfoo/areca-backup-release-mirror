package com.application.areca;

import com.myJava.system.OSTool;

/**
 * Static class that provides helper methods for memory management.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 7453350623295719521
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
public class MemoryHelper {

    private static double MEMORY_BY_ENTRY_KB = ArecaTechnicalConfiguration.get().getMemoryByEntryKb();
    private static long MEMORY_BASE_KB = ArecaTechnicalConfiguration.get().getMemoryBaseKb();
    
    private static double MEMORY_SAFETY_MARGIN = ArecaTechnicalConfiguration.get().getMemorySafetyMargin();
    private static double MEMORY_USAGE_RATE = 1 - MEMORY_SAFETY_MARGIN;  
    
    /**
     * Checks that enough memory has been allocated to Areca 
     * to store the number of entries provided as argument. 
     */
    public static boolean isOverQuota(long entries) {
        return OSTool.getMaxMemoryKB() < getTheoreticalMemoryKB(entries);
    }
    
    public static long getTheoreticalMemoryKB(long entries) {
        double theoreticalMemoryUsage = MEMORY_BASE_KB + entries * MEMORY_BY_ENTRY_KB;
        return (long)(theoreticalMemoryUsage / MEMORY_USAGE_RATE);
    }
    
    public static long getTheoreticalMemoryMB(long entries) {
        return (long)(getTheoreticalMemoryKB(entries) / 1024);
    }
    
    public static long getMaxManageableEntries() {
        double nb = OSTool.getMaxMemoryKB() * MEMORY_USAGE_RATE - MEMORY_BASE_KB;
        return (long)(nb / MEMORY_BY_ENTRY_KB);
    }

    public static String getMemoryTitle(AbstractRecoveryTarget target, long entries) {
        return "Target " + target.getId() + " (" + target.getTargetName() + ") - Memory Warning !";
    }
    
    public static String getMemoryMessage(AbstractRecoveryTarget target, long entries) {
        return 
	        "Only " + OSTool.getMaxMemoryMB() + " MBytes have been allocated to Areca" + 
	        ", which may be insufficient to securely manage this target (" + target.getTargetName() + "),\nregarding its size (approximately " + entries + " files).\n\n" +
	        "You can either :\n" +
	        "- split this target into smaller ones (it is advised that your target's size does not overcome " + MemoryHelper.getMaxManageableEntries() + " files), or \n" +
	        "- increase the memory that is allocated to Areca (in your case, it is advised to allocate at least " + MemoryHelper.getTheoreticalMemoryMB(entries) + " MBytes).\n" + 
	        "You will find more informations on how to increase the memory dedicated to Areca on Areca's Website.";        
    }
    
    /**
     * Returns the ratio of memory which is kept free for Areca.
     * <BR>For instance a ratio of 0.3 means that Areca's caching strategy will always attempts to keep 30% of
     * the overall memory available, and will trigger a GC if the free memory falls bellow this limit.
     */
    public static double getMemorySafetyMargin() {
        return MEMORY_SAFETY_MARGIN;
    }
}
