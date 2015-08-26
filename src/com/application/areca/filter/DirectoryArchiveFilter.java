package com.application.areca.filter;

import java.io.File;

import com.application.areca.Utils;
import com.myJava.file.FileSystemManager;
import com.myJava.object.Duplicable;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.util.log.Logger;

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
public class DirectoryArchiveFilter extends AbstractArchiveFilter {
	private static final long serialVersionUID = -5000073891121676700L;
	private File directory;

	public void acceptParameters(String parameters) {
        if (Utils.isEmpty(parameters)) {
            throw new IllegalArgumentException("Invalid directory : " + parameters);
        }
        this.directory = new File(parameters);
        
        if (! FileSystemManager.exists(directory)) {
            Logger.defaultLogger().warn("The filtered directory does not exist. (" + FileSystemManager.getDisplayPath(directory) + ")");
        }
    }

    public boolean checkParameters() {
    	return FileSystemManager.exists(directory);
	}

	public short acceptIteration(File entry, File data) {
        if (entry == null) {
            return WILL_MATCH_FALSE; 
        } else if (contains(directory, entry)) {
            return ! logicalNot ? WILL_MATCH_TRUE : WILL_MATCH_FALSE;
        } else if (contains(entry, directory)) {
            return WILL_MATCH_PERHAPS;
        } else {
            return logicalNot ? WILL_MATCH_TRUE : WILL_MATCH_FALSE;
        }
    }
    
    /**
     */
    public boolean acceptElement(File entry, File data) {  
        if (entry == null) {
            return false;
        } else if (FileSystemManager.isFile(entry)) {
            return contains(directory, entry) ? ! logicalNot : logicalNot;
        } else {
            if (contains(directory, entry)) {
                return ! logicalNot;
            } else if (contains(entry, directory)) {
                return true; // Always accept parent directories (exclusion or not)
            } else {
                return logicalNot;
            }
        }
    }
    
    public Duplicable duplicate() {
        DirectoryArchiveFilter filter = new DirectoryArchiveFilter();
        filter.logicalNot = this.logicalNot;
        filter.directory = this.directory;
        return filter;
    }    

    public String getStringParameters() {
        return FileSystemManager.getAbsolutePath(this.directory);
    }
    
    private boolean contains(File rootDirectory, File checked) {
        if (checked == null || rootDirectory == null) {
            return false;
        } else {   
            String strChecked = FileSystemManager.getAbsolutePath(checked);
            String strRoot = FileSystemManager.getAbsolutePath(rootDirectory);
            return
                strChecked.equals(strRoot)
                || strChecked.startsWith(strRoot + "/")
                || strChecked.startsWith(strRoot + "\\");                
        }
    }
    
    public boolean equals(Object obj) {
        if (obj == null || (! (obj instanceof DirectoryArchiveFilter)) ) {
            return false;
        } else {
            DirectoryArchiveFilter other = (DirectoryArchiveFilter)obj;
            return 
            	EqualsHelper.equals(this.directory, other.directory)
            	&& EqualsHelper.equals(this.logicalNot, other.logicalNot)
           	;
        }
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, FileSystemManager.getAbsolutePath(this.directory));
        h = HashHelper.hash(h, this.logicalNot);
        return h;
    }
}
