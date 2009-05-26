package com.myJava.util.history;

import java.util.GregorianCalendar;
import java.util.HashMap;


/**
 * Interface that defines an history on events
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

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
public interface History {

	/**
	 * Tells whether the history is empty or not
	 */
    public boolean isEmpty();
    
    /**
     * Add an entry
     */
    public void addEntry(HistoryEntry entry);
    
    /**
     * Return the internal content of the history as a Map
     * <BR>Key = GregorianCalendar
     * <BR>Value = HistoryEntry
     */
    public HashMap getContent();
    
    /**
     * Return the keys (dates) as an ordered array
     */
    public GregorianCalendar[] getOrderedKeys();
    
    /**
     * Write data on disk
     */
    public void flush();
    
    /**
     * Removes all entries from the history
     */
    public void clear();
    
    /**
     * Does not clear the internal data; destroys only the data stored on disk.
     * <BR>The internal data can be written on disk using the "flush" method.
     */
    public void clearData();
    
    /**
     * Imports the content of the source history
     */
    public void importHistory(History source);
}