package com.application.areca.impl.policy;

import java.io.File;

import com.application.areca.ApplicationException;
import com.application.areca.ArchiveMedium;
import com.application.areca.ArecaTechnicalConfiguration;
import com.myJava.file.FileSystemManager;
import com.myJava.file.driver.DefaultFileSystemDriver;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.file.driver.cache.CachedFileSystemDriver;
import com.myJava.object.Duplicable;
import com.myJava.object.ToStringHelper;
import com.myJava.util.log.Logger;

/**
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
public class DefaultFileSystemPolicy 
extends AbstractFileSystemPolicy
implements FileSystemPolicy {    
    private static final boolean CACHE = ArecaTechnicalConfiguration.get().isRepositoryHDCache();
    private static final int CACHE_DEPTH = ArecaTechnicalConfiguration.get().getRepositoryHDCacheDepth();
    
    /**
     * Storage path
     */
    protected String archivePath;
    protected ArchiveMedium medium;

    public void validate(boolean extendedTests) throws ApplicationException {
    }

    public FileSystemDriver initFileSystemDriver() throws ApplicationException {
        FileSystemDriver base = new DefaultFileSystemDriver();
        if (CACHE) {
            File storageDir = getArchiveDirectory();
            return new CachedFileSystemDriver(base, FileSystemManager.getParentFile(storageDir), CACHE_DEPTH);
        } else {
            return base;
        }
    }
    
    public int getMaxRetries() {
    	return 0;
	}

	public boolean retrySupported() {
		return false;
	}

    public String getArchivePath() {
		return this.archivePath;
	}

	public void setArchivePath(String archivePath) {
		this.archivePath = archivePath;
	}
	
    public void copyAttributes(DefaultFileSystemPolicy policy) {
    	super.copyAttributes(policy);
        policy.setArchivePath(archivePath);
    }

	public Duplicable duplicate() {
        DefaultFileSystemPolicy policy = new DefaultFileSystemPolicy();
        copyAttributes(policy);
        return policy;
    }

    public String getDisplayableParameters() {
        File tmpF = getArchiveDirectory();
        File mainStorageDirectory = FileSystemManager.getParentFile(tmpF);
        if (mainStorageDirectory == null) {
            return FileSystemManager.getAbsolutePath(tmpF);
        } else {
            return FileSystemManager.getAbsolutePath(mainStorageDirectory);                    
        }
    }
    
    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("Path", this.archivePath, sb);
        ToStringHelper.append("Name", this.archiveName, sb);
        return ToStringHelper.close(sb);
    }

    public ArchiveMedium getMedium() {
        return medium;
    }
    
    public void setMedium(ArchiveMedium medium) {
        this.medium = medium;
    }
    
    public void synchronizeConfiguration() {
        File archiveStorageDirectory = getArchiveDirectory();
        File rootDirectory = null;
        
        if (! FileSystemManager.getInstance().isRoot(archiveStorageDirectory)) {
            rootDirectory = FileSystemManager.getParentFile(archiveStorageDirectory);
        } else {
            // BAD !
            Logger.defaultLogger().warn("Inconsistent storage directory : " + archivePath, "DefaultFileSystemPolicy.synchronizeConfiguration()");
            rootDirectory = archiveStorageDirectory;
        }
        
        File newStorageDirectory = new File(rootDirectory, getMedium().getTarget().getUid());
        this.archivePath = FileSystemManager.getAbsolutePath(newStorageDirectory);
    }
}
