package com.myJava.file;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.filechooser.FileFilter;

import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.object.ToStringHelper;

/**
 * FileFilter par defaut : filtre les fichiers par extension.
 * 
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
public class DefaultFileFilter extends FileFilter {
    
    private HashSet filters = new HashSet();
    private String description = "";
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void addAcceptedExtension(String extension) {
        filters.add(extension.toLowerCase());
    }
    
    public Set getAcceptedExtensions() {
        return this.filters;
    }
    
    public boolean accept(File f) {
        if (f == null) {
            return false;
        }
        
        if (FileSystemManager.isDirectory(f)) {
            return true;
        }
        
        String extension = getFileExtension(f);
        return (extension != null && filters.contains(extension));
    }
    
    public String getFileExtension(File f) {
        if (f != null) {
            String filename = FileSystemManager.getName(f);
            int index = filename.lastIndexOf('.');
            if (index > 0 && index < filename.length() - 1) {
                return filename.substring(index + 1).toLowerCase();
            };
        }
        return null;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (! (obj instanceof DefaultFileFilter)) {
            return false;
        } else {
            DefaultFileFilter other = (DefaultFileFilter)obj;
            return EqualsHelper.equals(this.filters, other.filters);
        }
    }

    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, filters);
        return h;
    }

    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("Description", this.description, sb);
        ToStringHelper.append("Filters", this.filters, sb);
        return ToStringHelper.close(sb);
    }
}