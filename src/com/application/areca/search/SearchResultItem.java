package com.application.areca.search;

import java.util.GregorianCalendar;

import com.application.areca.AbstractTarget;
import com.application.areca.metadata.trace.TraceEntry;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;

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
public class SearchResultItem {

    private AbstractTarget target;
    private TraceEntry entry;
    private GregorianCalendar calendar;
    
    public SearchResultItem() {
    }

    public GregorianCalendar getCalendar() {
        return calendar;
    }
    
    public void setCalendar(GregorianCalendar calendar) {
        this.calendar = calendar;
    }
    
    public TraceEntry getEntry() {
        return entry;
    }
    
    public void setEntry(TraceEntry entry) {
        this.entry = entry;
    }
    
    public AbstractTarget getTarget() {
        return target;
    }
    
    public void setTarget(AbstractTarget target) {
        this.target = target;
    }

    public boolean equals(Object obj) {
        if (obj == null || (! (obj instanceof SearchResultItem))) {
            return false;
        } else {
            SearchResultItem other = (SearchResultItem)obj;
            return 
            	EqualsHelper.equals(other.entry, this.entry)
            	&& EqualsHelper.equals(other.target, this.target);
        }
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, this.target);
        h = HashHelper.hash(h, this.entry);
        return h;
    }
    
    public String toString() {
        if (entry == null) {
            return "null";
        } else {
            return this.entry.toString();
        }
    }
}
