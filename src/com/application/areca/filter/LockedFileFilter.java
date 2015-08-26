package com.application.areca.filter;

import java.io.File;
import java.io.IOException;

import com.myJava.file.FileSystemManager;
import com.myJava.file.ReadableCheckResult;
import com.myJava.file.metadata.FileMetaDataAccessor;
import com.myJava.object.Duplicable;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.util.log.Logger;

/**
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
public class LockedFileFilter extends AbstractArchiveFilter {
	private static final long serialVersionUID = 5217697405726544538L;

	public void acceptParameters(String parameters) {
    }
    
    public short acceptIteration(File entry, File data) {
        return WILL_MATCH_PERHAPS;
    }
    
    /**
     * Cette condition ne s'applique que sur les fichiers (pour des raisons d'optimisation).
     * Les repertoires retournent systematiquement "true"
     */
    public boolean acceptElement(File entry, File data) {
        if (entry == null) {
            return false;
        } else if (FileSystemManager.isDirectory(data)) {
            return true;
        } else {
        	if (! FileSystemManager.exists(data)) { // dangling links are accepted 
    		    Logger.defaultLogger().warn("The following file is a dangling link : " + FileSystemManager.getDisplayPath(entry));
        		return logicalNot;
        	} else {
        		short type;
				try {
					type = FileSystemManager.getType(data);
				} catch (IOException e) {
					Logger.defaultLogger().error("Error reading attributes for " + data.getAbsolutePath(), e);
					throw new IllegalArgumentException("Error reading attributes for " + data.getAbsolutePath(), e);
				}
        		if (type == FileMetaDataAccessor.TYPE_PIPE) {
        			return logicalNot;  
        		} else {
	        		ReadableCheckResult res = FileSystemManager.isReadable(data);
	
	        		if (res.isReadable()) {       		    
	        			return logicalNot;                
	        		} else {
	        		    Logger.defaultLogger().warn("The following file is locked by the system : " + FileSystemManager.getDisplayPath(data));
	        		    if (res.getCause() != null) {
	        		        Logger.defaultLogger().info("Cause : " + res.getCause());
	        		    }
	        			
	        			return ! logicalNot;
	        		}
        		}
        	}
        }
    }

    public Duplicable duplicate() {
        LockedFileFilter filter = new LockedFileFilter();
        filter.logicalNot = this.logicalNot;
        return filter;
    }    
    
    public String getStringParameters() {
        return  null;
    }
    
    public boolean requiresParameters() {
        return false;
    }
    
    public boolean equals(Object obj) {
        if (obj == null || (! (obj instanceof LockedFileFilter)) ) {
            return false;
        } else {
            LockedFileFilter other = (LockedFileFilter)obj;
            return 
            	EqualsHelper.equals(this.logicalNot, other.logicalNot)
           	;
        }
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, this.logicalNot);
        return h;
    }
}
