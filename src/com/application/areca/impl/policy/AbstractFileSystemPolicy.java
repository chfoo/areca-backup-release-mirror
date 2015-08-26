package com.application.areca.impl.policy;

import java.io.File;

import com.application.areca.ArchiveMedium;

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
public abstract class AbstractFileSystemPolicy 
implements FileSystemPolicy {    
    protected String id;
    protected String archiveName;
    protected ArchiveMedium medium;

    public String getArchiveName() {
		return this.archiveName;
	}

	public void setArchiveName(String archiveName) {
		this.archiveName = archiveName;
	}

	public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public File getArchiveDirectory() {
    	return new File(getArchivePath());
    }
    
    public void copyAttributes(AbstractFileSystemPolicy policy) {
    	policy.archiveName = archiveName;
        policy.id = id;
    }
    
    public void setMedium(ArchiveMedium medium) {
        this.medium = medium;
    }
}
