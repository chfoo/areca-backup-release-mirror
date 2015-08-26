package com.myJava.util.history;

import java.util.Arrays;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;

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
public class History {
    protected HashMap content = new HashMap();
    
    public synchronized boolean isEmpty() {
        return content.isEmpty();
    }
    
    public synchronized void addEntry(HistoryEntry entry) {      
        GregorianCalendar date = entry.getDate();       
        content.put(date, entry); 
    }
    
    public synchronized HistoryEntry getEntry(GregorianCalendar key) {
    	return (HistoryEntry)content.get(key);
    }
    
    public synchronized GregorianCalendar[] getKeys(boolean ordered) {
        GregorianCalendar[] keys = (GregorianCalendar[])this.content.keySet().toArray(new GregorianCalendar[0]);
        
        if (ordered) {
        	Arrays.sort(keys, new GregorianCalendarComparator());
        }
        
        return keys;
    }
    
    public synchronized void clear() {
        this.content.clear();
    }
    
    protected static class GregorianCalendarComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            GregorianCalendar c1 = (GregorianCalendar)o1;
            GregorianCalendar c2 = (GregorianCalendar)o2;            
            
            if (o1 == null && o2 != null) {
                return -1;
            } else if (o1 == null) {
                return 0;
            } else if (o2 == null && o1 != null) {
                return 1;
            } else {
                if (c1.before(c2)) {
                    return -1;
                } else if (c2.before(c1)) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }
}
