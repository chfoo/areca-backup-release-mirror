package com.myJava.util.collections;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Composite iterator built upon a map containing <code>Set<code>s indexed by keys.
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
public class CompositeSetIterator implements Iterator {

    private Map content = null;
    private Iterator keyIterator = null;
    private Iterator currentIterator = null;
    
    public CompositeSetIterator(Map content) {
        this.content = content;
        this.keyIterator = content.keySet().iterator();
    }

    public void remove() {
        throw new UnsupportedOperationException("This iterator is read-only");
    }

    public boolean hasNext() {
        if (currentIterator != null && currentIterator.hasNext()) {
            return true;
        } else {
            if (keyIterator.hasNext()) {
                Object nextKey = keyIterator.next();
                this.currentIterator = ((Set)content.get(nextKey)).iterator();
                return this.hasNext();
            } else {
                this.currentIterator = null;
                return false;
            }
        }
    }

    public Object next() {
        return currentIterator.next();
    }
}
