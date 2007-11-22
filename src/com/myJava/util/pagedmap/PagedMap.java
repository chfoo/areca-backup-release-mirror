package com.myJava.util.pagedmap;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

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
public abstract class PagedMap {
    /**
     * File trace separator
     */
    protected static final String SEPARATOR = "#-#";
    
    protected PageIndex index;
    protected File path;
    protected Comparator keyComparator;
    protected int pageSize;
    
    public PagedMap(File path, Comparator keyComparator, int pageSize) throws IOException {
        this.path = path;
        this.keyComparator = keyComparator;
        this.pageSize = pageSize;
        
        File indexFile = new File(FileSystemManager.getAbsolutePath(path) + ".index");
        this.index = new PageIndex(indexFile, this);
    }

    public void put(Object key, Object value) throws IOException {
        PageIndexItem lastItem = this.index.getLastItem();
        if (lastItem == null || lastItem.getWritten() == pageSize) {
            if (lastItem != null) {
                lastItem.close();
            }
            
            lastItem = new PageIndexItem(this.index);
            lastItem.setFromKey(key);
        }
        lastItem.put(key, value);
    }
    
    public Iterator keyIterator() {
        return new PagedIterator(this);
    }
    
    public void close() throws IOException {
        PageIndexItem lastItem = this.index.getLastItem();
        if (lastItem != null) {
            lastItem.close();
        }
        index.flush();
    }
    
    public File getPath() {
        return path;
    }

    public int getPageSize() {
        return pageSize;
    }

    public Object get(Object key) throws IOException {
        PageIndexItem item = this.index.getItem(key);
        if (item == null) {
            return null;
        } else {
            return item.getOrLoadCache().get(key);
        }
    }
    
    public Comparator getKeyComparator() {
        return keyComparator;
    }

    protected abstract String encode(Object o);
    protected abstract Object decode(String o);
}
