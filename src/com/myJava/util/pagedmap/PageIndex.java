package com.myJava.util.pagedmap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.myJava.file.FileSystemManager;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2156529904998511409
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
public class PageIndex {
    protected static final String SEPARATOR = "#-#";
    
    protected File indexFile;
    protected List items = null;
    protected PagedMap map;
    protected PageIndexItem favorite = null;

    public PageIndex(File indexFile, PagedMap map) throws IOException {
        this.indexFile = indexFile;
        this.map = map;
        this.load();
    }

    public PagedMap getMap() {
        return map;
    }

    /**
     * FORMAT : 
     * "from SEPARATOR to"
     */
    private void load() throws IOException {
        items = new ArrayList();

        if (FileSystemManager.exists(indexFile)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(FileSystemManager.getFileInputStream(indexFile)));
            try {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    PageIndexItem item = new PageIndexItem(this);
                    int i = line.indexOf(SEPARATOR);
    
                    String strFrom = line.substring(0, i);        
                    String strTo = line.substring(i + SEPARATOR.length());        
    
                    item.setFromKey(map.decode(strFrom));
                    item.setToKey(map.decode(strTo));
                }
            } finally {
                reader.close();
            }
        }
    }
    
    public void flush() throws IOException {
        BufferedWriter writer = new BufferedWriter(FileSystemManager.getWriter(indexFile));
        try {
            Iterator iter = this.getItems();
            while (iter.hasNext()) {
                PageIndexItem item = (PageIndexItem) iter.next();
                String line = map.encode(item.fromKey) + SEPARATOR + map.encode(item.toKey);
                writer.write(line + "\n");
            }
        } finally {
            writer.close();
        }
    }
    
    public PageIndexItem getItem(int index) {
        if (index > this.items.size() - 1) {
            return null;
        } else {
            return (PageIndexItem)this.items.get(index);
        }
    }
    
    public Iterator getItems() {
        return this.items.iterator();
    }
    
    public int getItemCount() {
        return this.items.size();
    }

    public PageIndexItem getItem(Object key) {
        if (
                favorite != null 
                && map.getKeyComparator().compare(favorite.toKey, key) >= 0 
                && map.getKeyComparator().compare(favorite.fromKey, key) <= 0) {
            return favorite;
        }
        
        Iterator iter = getItems();
        while (iter.hasNext()) {
            PageIndexItem item = (PageIndexItem)iter.next();
            if (map.getKeyComparator().compare(item.toKey, key) >= 0 && map.getKeyComparator().compare(item.fromKey, key) <= 0) {
                favorite = item;
                return item;
            }
        }
        return null;
    }
    
    public void resetCaches(PageIndexItem caller) {
        Iterator iter = this.getItems();
        while (iter.hasNext()) {
            PageIndexItem item = (PageIndexItem)iter.next();
            if (item.getVolume() != caller.getVolume()) {
                item.resetCache();
            }
        }
    }
    
    public PageIndexItem getLastItem() {
        if (items.size() == 0) {
            return null;
        } else {
            return (PageIndexItem)items.get(items.size() - 1);
        }
    }
}
