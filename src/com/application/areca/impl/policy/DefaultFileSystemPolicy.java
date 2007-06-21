package com.application.areca.impl.policy;

import java.io.File;

import com.application.areca.ApplicationException;
import com.application.areca.ArchiveMedium;
import com.myJava.file.DefaultFileSystemDriver;
import com.myJava.file.FileSystemDriver;
import com.myJava.file.FileSystemManager;
import com.myJava.util.PublicClonable;
import com.myJava.util.ToStringHelper;
import com.myJava.util.log.Logger;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 3274863990151426915
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
public class DefaultFileSystemPolicy 
extends AbstractFileSystemPolicy
implements FileSystemPolicy {
    
    public static final String STORAGE_DIRECTORY_PREFIX = "storage_";
    public static final String DEFAULT_ARCHIVE_NAME = "bck";
    
    /**
     * Chemin ou seront stockées les archives (zip, sous répertoires, etc.)
     */
    protected String baseArchivePath;
    protected ArchiveMedium medium;

    public void validate(boolean extendedTests) throws ApplicationException {
    }

    public FileSystemDriver initFileSystemDriver() throws ApplicationException {
        return new DefaultFileSystemDriver();
    }
    
    public String getBaseArchivePath() {
        return baseArchivePath;
    }
    
    public void setBaseArchivePath(String baseArchivePath) {
        this.baseArchivePath = baseArchivePath;
    }

    public PublicClonable duplicate() {
        DefaultFileSystemPolicy other = new DefaultFileSystemPolicy();
        other.baseArchivePath = baseArchivePath;
        other.id = id;
        return other;
    }

    public String getDisplayableParameters() {
        File tmpF = FileSystemManager.getParentFile(new File(getBaseArchivePath()));
        File mainStorageDirectory = FileSystemManager.getParentFile(tmpF);
        if (mainStorageDirectory == null) {
            return FileSystemManager.getAbsolutePath(tmpF);
        } else {
            return FileSystemManager.getAbsolutePath(mainStorageDirectory);                    
        }
    }
    
    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("Archive", this.baseArchivePath, sb);
        return ToStringHelper.close(sb);
    }

    public ArchiveMedium getMedium() {
        return medium;
    }
    
    public void setMedium(ArchiveMedium medium) {
        this.medium = medium;
    }
    
    public void synchronizeConfiguration() {
        File archiveFile = new File(baseArchivePath);
        File archiveStorageDirectory = FileSystemManager.getParentFile(archiveFile); 
        File rootDirectory = null;
        
        if (! FileSystemManager.getInstance().isRoot(archiveStorageDirectory)) {
            rootDirectory = FileSystemManager.getParentFile(archiveStorageDirectory);
        } else {
            // BAD !
            Logger.defaultLogger().warn("Inconsistent storage directory : " + baseArchivePath, "DefaultFileSystemPolicy.synchronizeConfiguration()");
            rootDirectory = archiveStorageDirectory;
        }
        
        File newStorageDirectory = new File(rootDirectory, STORAGE_DIRECTORY_PREFIX + getMedium().getTarget().getUid());
        File newArchiveFile = new File(newStorageDirectory, FileSystemManager.getName(archiveFile));
        
        this.baseArchivePath = FileSystemManager.getAbsolutePath(newArchiveFile);
    }
}
