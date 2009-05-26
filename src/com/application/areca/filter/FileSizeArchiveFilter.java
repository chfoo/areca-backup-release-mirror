package com.application.areca.filter;

import java.io.File;

import com.application.areca.Utils;
import com.myJava.file.FileSystemManager;
import com.myJava.object.Duplicable;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;

/**
 * Checks that the file's size is below the specified max size (in bytes)
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
public class FileSizeArchiveFilter extends AbstractArchiveFilter {
    
    private long maxSize;
    private boolean greaterThan;

	public void acceptParameters(String parameters) {
        if (
                Utils.isEmpty(parameters) 
                || (
                        (! parameters.trim().startsWith(">"))
                        && (! parameters.trim().startsWith("<")) 
                )
        ) {
            throw new IllegalArgumentException("Invalid parameters : " + parameters);
        }
        
        this.greaterThan = (parameters.indexOf('>') != -1);       
        this.maxSize = Long.parseLong(parameters.trim().substring(1).trim());
    }
    
    public boolean acceptIteration(File entry) {
        return true;
    }
    
    /**
     * Directories always return "true"
     */
    public boolean acceptStorage(File entry) {   
        if (entry == null) {
            return false;
        } else if (FileSystemManager.isFile(entry)) {
            boolean value;
            if (FileSystemManager.length(entry) > maxSize) {
                value = greaterThan;
            } else {
                value = ! greaterThan;
            }
            
            if (exclude) {
                return ! value;
            } else {
                return value;
            }
        } else {
            return true;
        }
    }
    
    public Duplicable duplicate() {
        FileSizeArchiveFilter filter = new FileSizeArchiveFilter();
        filter.exclude = this.exclude;
        filter.maxSize = this.maxSize;
        filter.greaterThan = this.greaterThan;
        return filter;
    }    

    public String getStringParameters() {
        String prefix = this.greaterThan ? "> " : "< ";
        return prefix + maxSize;
    }
    
    public boolean equals(Object obj) {
        if (obj == null || (! (obj instanceof FileSizeArchiveFilter)) ) {
            return false;
        } else {
            FileSizeArchiveFilter other = (FileSizeArchiveFilter)obj;
            return 
            	EqualsHelper.equals(this.exclude, other.exclude)
            	&& EqualsHelper.equals(this.greaterThan, other.greaterThan)
            	&& EqualsHelper.equals(this.maxSize, other.maxSize)
           	;
        }
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, this.greaterThan);
        h = HashHelper.hash(h, this.exclude);
        h = HashHelper.hash(h, this.maxSize);
        return h;
    }
}
