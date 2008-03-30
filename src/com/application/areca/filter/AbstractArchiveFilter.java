package com.application.areca.filter;

/**
 * Classe de base des filters.
 * D�finit la notion de 'including'/'excluding'
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2736893395693886205
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
    
    public boolean requiresParameters() {
        return true;
    }
}
