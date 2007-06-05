package com.application.areca.filter;

import com.application.areca.ArchiveFilter;
import com.application.areca.RecoveryEntry;
import com.myJava.util.EqualsHelper;
import com.myJava.util.HashHelper;


/**
 * Classe de base des filters.
 * Définit la notion de 'including'/'excluding'
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -6307890396762748969
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
public abstract class AbstractArchiveFilter implements ArchiveFilter {

    protected boolean exclude = false;
    
    public AbstractArchiveFilter() {
        this.exclude = false;
    }
    
    public AbstractArchiveFilter(boolean isExclude) {
        this.exclude = isExclude;
    }
    
    public boolean isExclude() {
        return exclude;
    }
    
    public void setExclude(boolean exclude) {
        this.exclude = exclude;
    }
    
    public abstract boolean accept(RecoveryEntry entry);
    
    public boolean requiresParameters() {
        return true;
    }

    public boolean equals(Object obj) {
        if (! EqualsHelper.checkClasses(this, obj)) {
            return false;
        } else {
            AbstractArchiveFilter other = (AbstractArchiveFilter)obj;
            return (
                    EqualsHelper.equals(other.getStringParameters(), this.getStringParameters())
                    && EqualsHelper.equals(other.isExclude(), this.isExclude())
            );
        }
    }
    
    public int hashCode() {
        int hash = HashHelper.initHash(this);
        hash = HashHelper.hash(hash, this.exclude);
        hash = HashHelper.hash(hash, this.getStringParameters());
        return hash;
    }
    
    public boolean traceFilteredFiles() {
        return true;
    }
    
    public int compareTo(Object o) {
        if (o == null) {
            return 1;
        } else {
            ArchiveFilter other = (ArchiveFilter)o;
            return (this.getClass().getName() + this.getStringParameters()).compareTo(other.getClass().getName() + other.getStringParameters());
        }
    }
}
