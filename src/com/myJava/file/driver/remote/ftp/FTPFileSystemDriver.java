package com.myJava.file.driver.remote.ftp;

import java.io.File;
import java.io.IOException;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.FileNameUtil;
import com.myJava.file.driver.remote.AbstractRemoteFileSystemDriver;
import com.myJava.file.driver.remote.RemoteFileInfoCache;
import com.myJava.file.metadata.FileMetaDataAccessor;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.object.ToStringHelper;

/**
 * File System driver which is connected to a FTP Server.
 * <BR>
 * <BR>The Following methods are not supported :
 * <BR>- public boolean canRead(File file) 			--> Not supported
 * <BR>- public boolean canWrite(File file) 			--> Not supported
 * <BR>- public long lastModified(File file); 			--> Not supported
 * <BR>- public boolean setLastModified(File file, long time); 	--> Not supported
 * <BR>- public boolean setReadOnly(File file); 		--> Not supported        
 * <BR>- public boolean mkdirs(File file);			--> Not supported  
 * <BR>- public boolean isHidden(File file); 			--> Not supported
 * <BR>- public Permission getPermissions(File file); 			--> Not supported   
 * <BR>- public void applyPermissions(Permissions p, File f);	--> Not supported
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
public class FTPFileSystemDriver extends AbstractRemoteFileSystemDriver {
    protected String remoteRootDirectory;

    public FTPFileSystemDriver(FTPProxy ftpProxy, File localRoot, String remoteRoot) {
    	super(localRoot);
    	this.maxProxies = FrameworkConfiguration.getInstance().getMaxFTPProxies();
        this.proxy = ftpProxy;
        this.remoteRootDirectory = FileNameUtil.normalizeSlashes(FileNameUtil.normalizePath(remoteRoot), true);
        this.proxy.setFileInfoCache(new RemoteFileInfoCache());
    } 

    // Returns the normalized remote root
    private String getNormalizedRemoteRoot() {
        return remoteRootDirectory;
    }

    // Converts the local file name to a remote file name
    protected String translateToRemote(File localFile) {
        int l = this.getNormalizedLocalRoot().length();
        String path = normalizeIfNeeded(localFile.getAbsolutePath()); 

        if (l < path.length()) {
            return this.getNormalizedRemoteRoot() + path.substring(l);
        } else {
            return this.getNormalizedRemoteRoot();
        }
    }

    public short getType(File file) throws IOException {
    	if (isFile(file)) {
    		return FileMetaDataAccessor.TYPE_FILE;
    	} else {
    		return FileMetaDataAccessor.TYPE_DIRECTORY;
    	}
	}

	// Converts the remote file name to a local file name    
    protected String translateToLocal(String remoteFile) {
        int l = getNormalizedRemoteRoot().length();

        if (l < remoteFile.length()) {
            String suffix = remoteFile.substring(l);
            return this.getNormalizedLocalRoot() + suffix;
        } else {
            return this.getNormalizedLocalRoot();
        }
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, remoteRootDirectory);
        h = HashHelper.hash(h, localRootDirectory);
        h = HashHelper.hash(h, proxy);                
        return h;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else {
            if (o instanceof FTPFileSystemDriver) {
                FTPFileSystemDriver other = (FTPFileSystemDriver)o;

                return (
                        EqualsHelper.equals(this.remoteRootDirectory, other.remoteRootDirectory)
                        && EqualsHelper.equals(this.localRootDirectory, other.localRootDirectory)
                        && EqualsHelper.equals(this.proxy, other.proxy)			
                );
            } else {
                return false;
            }
        }
    }

    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("LOCAL DIR", this.localRootDirectory, sb);
        ToStringHelper.append("REMOTE DIR", this.remoteRootDirectory, sb);
        ToStringHelper.append("HOST", ((FTPProxy)this.proxy).getRemoteServer(), sb);
        ToStringHelper.append("PORT", ((FTPProxy)this.proxy).getRemotePort(), sb);
        ToStringHelper.append("PASSIV", ((FTPProxy)this.proxy).isPassivMode(), sb);
        ToStringHelper.append("LOGIN", ((FTPProxy)this.proxy).getLogin(), sb);
        return ToStringHelper.close(sb);
    }
}