package com.myJava.util.pagedmap;

import java.io.IOException;
import java.util.Iterator;

import com.myJava.util.log.Logger;

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
public class PagedIterator implements Iterator {
    private PagedMap map;
    private int currentVolume = -1;
    private Iterator currentIterator;
    private PageIndexItem nextItem = null;
    private Object fetched;
    
    public PagedIterator(PagedMap map) {
        this.map = map;
        fetchNext();
    }

    public boolean hasNext() {
        return fetched != null || nextItem != null;
    }

    public Object next() {
        if (fetched == null && nextItem != null) {
            try {
                currentIterator = nextItem.getOrLoadCache().content.keySet().iterator();
                fetchNext();
            } catch (IOException e) {
                Logger.defaultLogger().error(e);
                throw new ArrayIndexOutOfBoundsException(e.getMessage());
            }
        }
        Object ret = fetched;
        fetchNext();
        return ret;
    }
    
    private void fetchNext() {
        if (currentIterator != null && currentIterator.hasNext()) {
            fetched = currentIterator.next();
        } else {
            currentVolume++;
            nextItem = this.map.index.getItem(currentVolume);
            fetched = null;
        }
    }

    public void remove() {
        throw new UnsupportedOperationException("Unsupported operation");
    }
}
