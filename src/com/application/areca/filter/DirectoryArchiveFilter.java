package com.application.areca.filter;

import java.io.File;

import com.application.areca.RecoveryEntry;
import com.application.areca.Utils;
import com.application.areca.impl.FileSystemRecoveryEntry;
import com.myJava.file.FileSystemManager;
import com.myJava.util.EqualsHelper;
import com.myJava.util.HashHelper;
import com.myJava.util.PublicClonable;

/**
 * Vérifie si l'entrée est bien contenue (directement ou indirectement) dans le répertoire
 * spécifié.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -2622785387388097396
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
public class DirectoryArchiveFilter extends AbstractArchiveFilter {
    private File directory;
    
    public DirectoryArchiveFilter() {
    }

    public void acceptParameters(String parameters) {
        if (Utils.isEmpty(parameters)) {
            throw new IllegalArgumentException("Invalid directory : " + parameters);
        }
        this.directory = new File(parameters);
    }
    
    public boolean acceptIteration(RecoveryEntry entry) {
        return acceptStorage(entry);
    }
    
    /**
     */
    public boolean acceptStorage(RecoveryEntry entry) {
        FileSystemRecoveryEntry fEntry = (FileSystemRecoveryEntry)entry;        
        if (fEntry == null) {
            return false;
        } else if (FileSystemManager.isFile(fEntry.getFile())) {
            return contains(directory, fEntry.getFile()) ? ! exclude : exclude;
        } else {
            if (contains(directory, fEntry.getFile())) {
                return ! exclude;
            } else if (contains(fEntry.getFile(), directory)) {
                return true; // Always accept parent directories (exclusion or not)
            } else {
                return exclude;
            }
        }
    }
    
    public PublicClonable duplicate() {
        DirectoryArchiveFilter filter = new DirectoryArchiveFilter();
        filter.exclude = this.exclude;
        filter.directory = this.directory;
        return filter;
    }    

    public String getStringParameters() {
        return  FileSystemManager.getAbsolutePath(this.directory);
    }
    
    private boolean contains(File rootDirectory, File checked) {
        if (checked == null || rootDirectory == null) {
            return false;
        } else {               
            if (rootDirectory.getAbsolutePath().equals(checked.getAbsolutePath())) {
                return true;
            } else {
                return contains(rootDirectory, FileSystemManager.getParentFile(checked));
            }
        }
    }
    
    public boolean equals(Object obj) {
        if (obj == null || (! (obj instanceof DirectoryArchiveFilter)) ) {
            return false;
        } else {
            DirectoryArchiveFilter other = (DirectoryArchiveFilter)obj;
            return 
            	EqualsHelper.equals(this.directory, other.directory)
            	&& EqualsHelper.equals(this.exclude, other.exclude)
           	;
        }
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, this.directory.getAbsolutePath());
        h = HashHelper.hash(h, this.exclude);
        return h;
    }
}
