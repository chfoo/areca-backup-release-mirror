package com.myJava.file.driver.remote;

import java.io.File;

import com.myJava.object.ToStringHelper;
/**
 * This class extends the default "File" structure.
 * <BR>It represents a fictive local or a remote file.
 * <BR>In both cases, it does not exist on the local file system.
 * <BR>It does not redefine fileSystem actions such as "delete", "mkdir", ... (these actions are the resposibility of the FileSystemDriver)
 * but keep technical informations in cache such as file size, isDirectory, ... (to avoid making multiple remote accesses for the same file)
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
public class FictiveFile extends File {
	/**
	 * Length on the remote server
	 */
	private long remoteLength;
	
	/**
	 * Is file or directory on the remote server
	 */
	private boolean remoteDirectory;
	
	/**
	 * Exists on the remote server
	 */
	private boolean remoteExists;
	
	private long remoteTs;
	
	/**
	 * Technical attribute : has been initialized
	 */
	private boolean isInitialized;
	
	/**
	 * Technical attribute : original path, without modification.
	 * This local path is exposed as the File class path attribute.
	 */
	private String localPath;
	
	/**
	 * Technical attribute : original path, without modification 
	 */
	private String remotePath;	
	
	/**
	 * Technical attribute : Driver used for remote accesses 
	 */
	private AbstractRemoteFileSystemDriver driver;

	
	public FictiveFile(
	        String localPath, 
	        String remotePath, 
	        AbstractRemoteFileSystemDriver driver) {
	    
		super(localPath);
		
		this.isInitialized = false;
		this.driver = driver;
		this.localPath = localPath;
		this.remotePath = remotePath;
	}
	
    public FictiveFile(
            String localPath, 
            String remotePath,
            long remoteLength, 
            boolean remoteDirectory,
            boolean remoteExists,
            long remoteTs) {
        
        this(localPath, remotePath, null);
        
        this.remoteLength = remoteLength;
        this.remoteDirectory = remoteDirectory;
        this.remoteExists = remoteExists;
        this.remoteTs = remoteTs;

        this.isInitialized = true;
    }
    public String getLocalPath() {
        return localPath;
    }
    
    public String getRemotePath() {
        return remotePath;
    }
    
	public long length() {
		this.checkInitialized();
		return this.remoteLength;
	}
	
	public boolean isDirectory() {
		this.checkInitialized();		
		return remoteDirectory;
	}

	public boolean isFile() {
		return ! isDirectory();
	}
	
	private void checkInitialized() {
		if (! isInitialized) {
			this.driver.initFictiveLocalFile(this);
		}
	}
	
    public boolean exists() {
		this.checkInitialized();		
		return remoteExists;
    }    
    
    public long lastModified() {
		this.checkInitialized();		
		return remoteTs;
    }
    
	public void init(long remoteLength, boolean remoteDirectory, boolean remoteExists, long remoteTs) {
		this.isInitialized = true;		
		this.remoteLength = remoteLength;
		this.remoteDirectory = remoteDirectory;
		this.remoteExists = remoteExists;
		this.remoteTs = remoteTs;
	}
	
	public String toString() {
	    StringBuffer sb = ToStringHelper.init(this);
	    ToStringHelper.append("LOCAL PATH", localPath, sb);
	    ToStringHelper.append("REMOTE PATH", remotePath, sb);
	    ToStringHelper.append("LENGTH", remoteLength, sb);
	    ToStringHelper.append("TS", remoteTs, sb);
	    ToStringHelper.append("IS_DIRECTORY", remoteDirectory, sb);
	    ToStringHelper.append("EXISTS", remoteExists, sb); 
	    return ToStringHelper.close(sb);
	}
}