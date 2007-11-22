package com.myJava.util.pagedmap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

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
public class PageIndexItem {
    protected Object fromKey;
    protected Object toKey;
    protected File file;
    protected int volume;
    protected PageIndex index;
    protected PageCache cache;
    protected int written = 0;
    protected BufferedWriter writer;
    
    public PageIndexItem(PageIndex index) {
        this.index = index;
        this.volume = index.getItemCount();
        this.file = new File(FileSystemManager.getAbsolutePath(index.getMap().getPath()) + ".pm" + volume);
        index.items.add(this);
    }

    public File getFile() {
        return file;
    }
    
    public void resetCache() {
        this.cache = null;
    }
    
    public Object getFromKey() {
        return fromKey;
    }

    public PageIndex getIndex() {
        return index;
    }

    public void setFromKey(Object fromKey) {
        this.fromKey = fromKey;
    }
    
    public Object getToKey() {
        return toKey;
    }
    
    public void setToKey(Object toKey) {
        this.toKey = toKey;
    }
    
    public int getVolume() {
        return volume;
    }

    public PageCache getOrLoadCache() throws IOException {
        if (cache == null) {
            this.load();
        }
        return cache;
    }

    public int getWritten() {
        return written;
    }
    
    private void open() throws IOException {
        writer = new BufferedWriter(FileSystemManager.getWriter(file));
    }
    
    public void close() throws IOException {
        writer.close();
    }

    public void put(Object key, Object value) throws IOException {
        if (toKey != null && this.getIndex().getMap().getKeyComparator().compare(key, toKey) <= 0) {
            throw new IllegalArgumentException("Illegal key : " + key.toString() + " - It is <= " + toKey.toString());
        } else {
            toKey = key;
            
            if (this.cache != null) {
                this.cache.put(key, value);
            }
            
            String line = getIndex().getMap().encode(key) + PagedMap.SEPARATOR + getIndex().getMap().encode(value);
            
            if (writer == null) {
                open();
            }
            
            writer.write(line + "\n");
            written++;
        }
    }
    
    private void load() throws IOException {
        this.index.resetCaches(this);
        System.out.println("loading " + this.file + " ...");
        
        this.cache = new PageCache();
        BufferedReader reader = new BufferedReader(new InputStreamReader(FileSystemManager.getFileInputStream(file)));
        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                int i = line.indexOf(PagedMap.SEPARATOR);
                Object key = getIndex().getMap().decode(line.substring(0, i));
                Object value = getIndex().getMap().decode(line.substring(i + PagedMap.SEPARATOR.length()));
                
                cache.put(key, value);
            }
        } finally {
            reader.close();
        }
    }
}
