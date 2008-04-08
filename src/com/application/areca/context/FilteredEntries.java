package com.application.areca.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.application.areca.RecoveryEntry;

/**
 * List of filtered entries indexed by ArchiveFilter
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 6668125177615540854
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
public class FilteredEntries {
    
    private Map content = new HashMap();
    
    public FilteredEntries() {
    }

    public void addFilteredEntry(RecoveryEntry entry, Object key) {
        this.getFilteredEntries(key).add(entry);
    }
    
    public Iterator getKeyIterator() {
        return content.keySet().iterator();
    }
    
    public List getFilteredEntries(Object key) {
        List data = (List)this.content.get(key);
        if (data == null) {
            data = new ArrayList();
            content.put(key, data);
        }
        return data;
    }
    
    public boolean isEmpty() {
        return this.content.isEmpty();
    }
}
