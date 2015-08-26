package com.myJava.file.driver.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.myJava.util.Util;

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
public abstract class AbstractProxy {

    // ID
    protected final long uid = Util.getRndLong();
    protected RemoteDebug debug = new RemoteDebug();
    // STATISTICS
    protected double nbGetRemoteFileInfos = 0;
    protected double nbCacheRetrieval = 0;
    protected long connectionId = -1;
    protected String ownerId = null;
    protected long lastOpTime = -1; // Last Op time (ms) ... see System.currentTimeMillis();
    protected RemoteFileInfoCache fileInfoCache;
    
    public abstract FictiveFile[] listFiles(String parentFile) throws RemoteConnectionException;
    public abstract InputStream getFileInputStream(String file) throws RemoteConnectionException;
    public abstract boolean renameTo(String source, String destination) throws RemoteConnectionException;
    public abstract boolean mkdir(String remoteFile) throws RemoteConnectionException;
    public abstract void completePendingCommand(boolean blocking) throws IOException, RemoteConnectionException;
    protected abstract void resetContextData();
    protected abstract void resetClient(Throwable e);
    public abstract void connect() throws RemoteConnectionException;
    public abstract void disconnect();
    public abstract boolean deleteFile(String remoteFile) throws RemoteConnectionException;
    public abstract boolean deleteDir(String remoteDir) throws RemoteConnectionException;
    public abstract AbstractProxy cloneProxy();
    public abstract FictiveFile getRemoteFileInfos(String remoteFile) throws RemoteConnectionException;
    public abstract OutputStream getFileOutputStream(String file, boolean append) throws RemoteConnectionException;

    public synchronized void flush() {
        clearCache();
        this.resetContextData();
    }
    
    public void setFileInfoCache(RemoteFileInfoCache fileInfoCache) {
        this.fileInfoCache = fileInfoCache;
    }
    
    public void debug(String message, Object arg) {
        debug.debug(uid, ownerId, message, arg);
    }
	
    public void debug(String message) {
        debug(message, null);
    }

    public long getConnectionId() {
        return connectionId;
    }

    public String getOwnerId() {
        return ownerId;
    }
    
    public synchronized boolean acquireLock(String owner) {
        if (ownerId != null && (! ownerId.equals(owner))) {
            debug("The owner : [" + owner + "] cannot lock the proxy because it is already locked by : [" + ownerId + "]");
            return false;
        } else {
            debug("Acquiring lock");
            this.ownerId = owner;
            return true;
        }
    }
    
    public synchronized void releaseLock(String owner) {
        if (ownerId != null && (! ownerId.equals(owner))) {
            throw new IllegalStateException("The owner : [" + owner + "] cannot unlock the proxy because it is locked by : [" + ownerId + "]");
        } else {
            debug("Releasing lock");
            this.ownerId = null;
        }
    }
    
    protected synchronized void checkLocked() {
        if (this.ownerId == null) {
            throw new IllegalStateException("Attempted to use the FTPProxy without having acquired a lock on it");
        }
    }
    
    public boolean hasReconnectSince(long lastConnectionId) {
        return lastConnectionId != this.connectionId;
    }
    
    protected void updateOpTime() {
        this.lastOpTime = System.currentTimeMillis();
    }


    public void removeCachedFileInfos(String fileName) {
        debug("Deleting cache entry - cache size = " + this.fileInfoCache.size(), fileName);
        this.fileInfoCache.removeCachedFileInfos(fileName);
    }
    
    protected void clearCache() {
    	if (fileInfoCache != null) {
	        debug("Clearing the cache - cache size = " + this.fileInfoCache.size());
	        this.fileInfoCache.clearCache();
    	}
    }
    
    protected void registerFileInfo(String remoteFileName, FictiveFile info) {
        debug("Adding cached data if possible - cache size = " + this.fileInfoCache.size(), info);
        this.fileInfoCache.registerFileInfo(remoteFileName, info);
    }
    
    public synchronized boolean createNewFile(String remoteFile) throws RemoteConnectionException {
        checkLocked();
        try {
            OutputStream out = this.getFileOutputStream(remoteFile);
            out.flush();
            out.close();
            return true;
        } catch (IOException e) {
            resetClient(e);
            return false;
        } finally {
            removeCachedFileInfos(remoteFile);
            resetContextData();
        }
    }
    
    public void initFictiveLocalFile(FictiveFile file) {
        try {
            // Retrieve remote informations.
            FictiveFile remoteFileInfos = this.getRemoteFileInfos(file.getRemotePath());
            
            // Init the fictive local file.
            if (remoteFileInfos != null) {
                file.init(remoteFileInfos.length(), remoteFileInfos.isDirectory(), remoteFileInfos.exists(), remoteFileInfos.lastModified());
            } else {
                file.init(0, false, false, 0);
            }
        } catch (RemoteConnectionException e) {
            resetClient(e);
            throw new IllegalStateException(e.getMessage());
        }
    }  
    
    public synchronized OutputStream getFileOutputStream(String file) throws RemoteConnectionException {
        return this.getFileOutputStream(file, false);
    }
}
