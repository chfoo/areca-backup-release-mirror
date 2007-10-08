package com.application.areca.filter;

import java.io.IOException;

import com.application.areca.RecoveryEntry;
import com.application.areca.Utils;
import com.application.areca.impl.FileSystemRecoveryEntry;
import com.myJava.file.FileSystemManager;
import com.myJava.file.attributes.Attributes;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.object.PublicClonable;
import com.myJava.util.log.Logger;

/**
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 6222835200985278549
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
public class FileOwnerArchiveFilter extends AbstractArchiveFilter {

    private static final char SEPARATOR = ':';
    
    private String owner;
    private String ownerGroup;
    
    public FileOwnerArchiveFilter() {
    }

    public void acceptParameters(String parameters) {
        if (Utils.isEmpty(parameters)) {
            throw new IllegalArgumentException("Invalid parameters : " + parameters);
        }
        
        owner = null;
        ownerGroup = null;
        
        String params = parameters.trim();
        int index = params.indexOf(SEPARATOR);
        if (index == -1) {
            this.owner = params;
        } else {
            if (index != 0) {
                this.owner = params.substring(0, index).trim();
            }
            this.ownerGroup = params.substring(index + 1).trim();            
        }
    }
    
    public boolean acceptIteration(RecoveryEntry entry) {
        return acceptStorage(entry);
    }
    
    public boolean acceptStorage(RecoveryEntry entry) {
        FileSystemRecoveryEntry fEntry = (FileSystemRecoveryEntry)entry;        
        if (fEntry == null) {
            return false;
        } else {
            try {
                Attributes atts = FileSystemManager.getAttributes(fEntry.getFile());
                boolean match = (owner == null || owner.equals(atts.getOwner()));
                match = match && (ownerGroup == null || ownerGroup.equals(atts.getOwnerGroup()));
                return match ? ! exclude : exclude;
            } catch (IOException e) {
                String msg = "Error reading file permissions for " + FileSystemManager.getAbsolutePath(fEntry.getFile());
                Logger.defaultLogger().info(msg);
                throw new IllegalArgumentException(msg);
            }
        }
    }
    
    public PublicClonable duplicate() {
        FileOwnerArchiveFilter filter = new FileOwnerArchiveFilter();
        filter.exclude = this.exclude;
        filter.owner = this.owner;
        filter.ownerGroup = this.ownerGroup;
        return filter;
    }

	public String getStringParameters() {
		return (owner == null ? "" : owner) + (ownerGroup == null ? "" : (SEPARATOR + ownerGroup));
	}
	
    public boolean equals(Object obj) {
        if (obj == null || (! (obj instanceof FileOwnerArchiveFilter)) ) {
            return false;
        } else {
            FileOwnerArchiveFilter other = (FileOwnerArchiveFilter)obj;
            return 
            	EqualsHelper.equals(this.exclude, other.exclude)
            	&& EqualsHelper.equals(this.owner, other.owner)
                && EqualsHelper.equals(this.ownerGroup, other.ownerGroup)
           	;
        }
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, this.owner);
        h = HashHelper.hash(h, this.exclude);
        h = HashHelper.hash(h, this.ownerGroup);
        return h;
    }
}
