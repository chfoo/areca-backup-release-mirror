package com.application.areca.filter;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.myJava.file.iterator.FileSystemIteratorFilter;
import com.myJava.object.Duplicable;
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
public class FilterGroup 
implements ArchiveFilter, FileSystemIteratorFilter, Serializable {
	private static final long serialVersionUID = 7093482082393943951L;
	
	private boolean isAnd = true;
    private List filters = new ArrayList();
    private boolean logicalNot = false;
    
    /**
     * Adds a filter and recomputes the cached values for
     * the "doesExclusionApplyToChildrenImpl" method.      
     */         
    public void addFilter(ArchiveFilter f) {
        this.filters.add(f);
    }
    
    public Iterator getFilterIterator() {
        return this.filters.iterator();
    } 
    
    public boolean isAnd() {
        return isAnd;
    }

    public void setAnd(boolean isAnd) {
        this.isAnd = isAnd;
    }

    public boolean checkParameters() {
		return true;
	}

	/**
     * Accepts (or refuses) an entry
     */         
    public short acceptIteration(File entry, File data) {
        short matchFilter = WILL_MATCH_TRUE;
        
        Iterator iter = this.getFilterIterator();
        if (this.isAnd()) {
            // AND
            matchFilter = WILL_MATCH_TRUE;
            while (iter.hasNext()) {
                ArchiveFilter filter = (ArchiveFilter)iter.next();
                short answer = filter.acceptIteration(entry, data);
                if (answer == WILL_MATCH_FALSE) {
                    matchFilter = WILL_MATCH_FALSE;
                    break;
                } else if (answer == WILL_MATCH_PERHAPS) {
                    matchFilter = WILL_MATCH_PERHAPS;
                }
            }
        } else {
            // OR
            matchFilter = WILL_MATCH_FALSE;            
            while (iter.hasNext()) {
                ArchiveFilter filter = (ArchiveFilter)iter.next();
                short answer = filter.acceptIteration(entry, data);
                if (answer == WILL_MATCH_TRUE) {
                    matchFilter = WILL_MATCH_TRUE;
                    break;
                } else if (answer == WILL_MATCH_PERHAPS) {
                    matchFilter = WILL_MATCH_PERHAPS;
                }
            }
        }
        
        if (logicalNot) {
        	if (matchFilter == WILL_MATCH_TRUE) {
        		matchFilter = WILL_MATCH_FALSE;
        	} else if (matchFilter == WILL_MATCH_FALSE) {
        		matchFilter = WILL_MATCH_TRUE;
        	}
        }
        return matchFilter;
    }
    
    /**
     * Accepts (or refuses) an entry
     */         
    public boolean acceptElement(File entry, File data) {
        boolean matchFilter;
        
        Iterator iter = this.getFilterIterator();
        if (this.isAnd()) {
            // AND
            matchFilter = true;
            while (iter.hasNext()) {
                ArchiveFilter filter = (ArchiveFilter)iter.next();
                if (! filter.acceptElement(entry, data)) {
                    matchFilter = false;
                    break;
                }
            }
        } else {
            // OR
            matchFilter = false;            
            while (iter.hasNext()) {
                ArchiveFilter filter = (ArchiveFilter)iter.next();
                if (filter.acceptElement(entry, data)) {
                    matchFilter = true;
                    break;
                }
            }
        }
        return logicalNot ? ! matchFilter : matchFilter;
    }
    
    public void remove(ArchiveFilter filter) {
        this.filters.remove(filter);
    }

    public boolean equals(Object obj) {
        if (! EqualsHelper.checkClasses(this, obj)) {
            return false;
        } else {
            FilterGroup other = (FilterGroup)obj;
            return (
                    EqualsHelper.equals(other.isAnd(), this.isAnd())
                    && EqualsHelper.equals(other.isLogicalNot(), this.isLogicalNot())
                    && EqualsHelper.equals(other.filters, this.filters)                    
            );
        }
    }
    
    public int hashCode() {
        int hash = HashHelper.initHash(this);
        hash = HashHelper.hash(hash, this.logicalNot);
        hash = HashHelper.hash(hash, this.isAnd());
        Iterator iter = this.getFilterIterator();
        while (iter.hasNext()) {
            hash = HashHelper.hash(hash, iter.next());
        }
        return hash;
    }

    public Duplicable duplicate() {
        FilterGroup other = new FilterGroup();
        other.setAnd(this.isAnd);
        other.setLogicalNot(this.logicalNot);
        Iterator iter = this.getFilterIterator();
        while (iter.hasNext()) {
            ArchiveFilter filter = (ArchiveFilter)iter.next();
            other.addFilter((ArchiveFilter)filter.duplicate());
        }
        return other;
    }

    public boolean isLogicalNot() {
        return logicalNot;
    }

    public boolean requiresParameters() {
        return false;
    }
    
    public String getStringParameters() {
        return "";
    }
    
    public void acceptParameters(String parameters) {
        throw new UnsupportedOperationException("Parameters are not supported by this implementation.");
    }

    public void setLogicalNot(boolean exclude) {
        logicalNot = exclude;
    }
}
