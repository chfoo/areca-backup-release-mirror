package com.myJava.file.ftp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.FileNameUtil;
import com.myJava.util.EqualsHelper;
import com.myJava.util.HashHelper;
import com.myJava.util.ToStringHelper;
import com.myJava.util.Utilitaire;
import com.myJava.util.log.Logger;

/**
 * Proxy that abstracts the ftp access layer.
 * <BR>It wraps the ftp framework.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4438212685798161280
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
public class FTPProxy {
    private static final long TIME_BETWEEN_OPS = FrameworkConfiguration.getInstance().getFTPNoopDelay(); // Time between noops (milliseconds)
    private static final boolean CACHE_ENABLED = FrameworkConfiguration.getInstance().isFTPCacheMode();
    
    // PARAMETERS
    private String remoteServer;
    private int remotePort;
    private String login;
    private String password;
    private boolean passivMode;
    private String protocol = null;
    private String protection = null;
    private boolean impliciteSec = false;
    
    // CLIENT
    private FTPClient client;
    private String workingDirectory = null;
    
    // ID
    protected final long uid = Utilitaire.getRndLong();
    protected long connectionId = -1;
    private String ownerId = null;

    // STATISTICS
    protected double nbGetRemoteFileInfos = 0;
    protected double nbCacheRetrieval = 0;
    private long lastOpTime = -1; // Last Op time (ms) ... see System.currentTimeMillis();
    private FTPDebug debug = new FTPDebug();
    
    private FTPFileInfoCache fileInfoCache;

    public FTPFileInfoCache getFileInfoCache() {
        return fileInfoCache;
    }
    
    public void setFileInfoCache(FTPFileInfoCache fileInfoCache) {
        this.fileInfoCache = fileInfoCache;
    }
    
    public String getLogin() {
        return login;
    }
    
    public void setLogin(String login) {
        this.login = login;
    }
    
    public boolean isPassivMode() {
        return passivMode;
    }

    public String getOwnerId() {
        return ownerId;
    }
    
    public void setPassivMode(boolean passivMode) {
        this.passivMode = passivMode;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public int getRemotePort() {
        return remotePort;
    }
    
    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }
    
    public String getRemoteServer() {
        return remoteServer;
    }
    
    public void setRemoteServer(String remoteServer) {
        this.remoteServer = remoteServer;
    }

    public String getProtocol() {
        return protocol;
    }

    public boolean isImpliciteSec() {
        return impliciteSec;
    }
    
    public void setImpliciteSec(boolean impliciteSec) {
        this.impliciteSec = impliciteSec;
    }
    
    public void setProtocol(String protocol) {
        if (protocol != null && protocol.trim().length() == 0) {
            this.protocol = null;
        } else {
            this.protocol = protocol;
        }
    }

    public String getProtection() {
        return protection;
    }

    public void setProtection(String protection) {
        if (protection != null && protection.trim().length() == 0) {
            this.protection = null;
        } else {
            this.protection = protection;
        }
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
    
    private void checkLocked() {
        if (this.ownerId == null) {
            throw new IllegalStateException("Attempted to use the FTPProxy without having acquired a lock on it");
        }
    }
    
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof FTPProxy) {
            FTPProxy o = (FTPProxy)obj;
            return
            	EqualsHelper.equals(this.passivMode, o.passivMode)
            	&& EqualsHelper.equals(this.protocol, o.protocol)
                && EqualsHelper.equals(this.protection, o.protection)
            	&& EqualsHelper.equals(this.impliciteSec, o.impliciteSec)      
            	&& EqualsHelper.equals(this.remotePort, o.remotePort)
            	&& EqualsHelper.equals(this.login, o.login)
            	&& EqualsHelper.equals(this.password, o.password)
            	&& EqualsHelper.equals(this.remoteServer, o.remoteServer);
        } else {
            return false;
        }
    }

    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, this.passivMode);
        h = HashHelper.hash(h, this.protocol);
        h = HashHelper.hash(h, this.protection);
        h = HashHelper.hash(h, this.impliciteSec);
        h = HashHelper.hash(h, this.remotePort);
        h = HashHelper.hash(h, this.login);
        h = HashHelper.hash(h, this.password);
        h = HashHelper.hash(h, this.remoteServer);
        return h;
    }    
    
    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("Host", remoteServer, sb);
        ToStringHelper.append("Port", remotePort, sb);
        return ToStringHelper.close(sb);
    }

    public long getConnectionId() {
        return connectionId;
    }
    
    public boolean isSecured() {
        return this.protocol != null;
    }
    
    private boolean changeWorkingDirectory(String inst, String dir) throws IOException {
        if (!CACHE_ENABLED || dir == null || ! (dir.equals(workingDirectory))) {
            this.workingDirectory = dir;
            
            debug(inst + " : changeWorkingDirectory", dir);
            return client.changeWorkingDirectory(dir);
        } else {
            debug(inst + " : working directory already set to ", dir);
            return true;
        }
    }

    /**
     * Enforce server reconnection (closes the current connection if it is still alive)
     */
    public synchronized void connect() throws FTPConnectionException {
        checkLocked();
        
        try {
            int reply;
            
            // Try to disconnect
            this.disconnect();
            
            // Open new connection
            if (isSecured()) {
                this.client = new FTPSClient(
                        protocol, 
                        protection, 
                        impliciteSec,
                        null, // TODO
                        null  // TODO
                );
            } else {
                this.client = new FTPClient();
            }
           
            //InetAddress adr = InetAddress.getByName(this.remoteServer);
            Logger.defaultLogger().info("Trying to connect to server : " + this.remoteServer + " ...");
            
            debug("connect : connect", remoteServer);
            client.connect(remoteServer, this.remotePort);
            
            Logger.defaultLogger().info("Received FTP server response : " + formatFTPReplyString(client.getReplyString()));
            this.connectionId = Utilitaire.getRndLong();

            reply = client.getReplyCode();
            
            if( ! FTPReply.isPositiveCompletion(reply)) {
                String msg = formatFTPReplyString(client.getReplyString());
                this.disconnect();
                throw new FTPConnectionException("Unable to communicate with remote FTP server. Got message : " + msg);
            } else {
                // CONNECTION OK
                Logger.defaultLogger().info("Trying to log in with user : " + this.login +  " ...");
                debug("connect : login", login + "/" + password);
                if ( ! client.login(this.login, this.password)) {
                    String msg = formatFTPReplyString(client.getReplyString());
                    this.disconnect();
                    throw new FTPConnectionException("Unable to login on FTP server (" + login + "/" + password + "). Received response : " + msg);
                } else {
                    Logger.defaultLogger().info("Logged in with user : " + this.login +  ". Received response : " + formatFTPReplyString(client.getReplyString()));
                    
                    // LOGIN OK
                    if (this.passivMode) {
                        Logger.defaultLogger().info("Switching to passive mode ...");
                        
                        // PASSIV MODE REQUESTED
                        debug("connect : pasv");
                        client.enterLocalPassiveMode();
                        
                        reply = client.getReplyCode();
                        if( ! FTPReply.isPositiveCompletion(reply)) {
                            String msg = formatFTPReplyString(client.getReplyString());
                            this.disconnect();
                            throw new FTPConnectionException("Unable to switch to passiv mode. Received response : " + msg);
                        } else {
                            this.updateOpTime();
                        }
                        // PASSIV MODE OK
                    } else {
                        this.updateOpTime();
                    }
                    
                    debug("connect : bin");
                    client.setFileType(FTP.BINARY_FILE_TYPE);
                    Logger.defaultLogger().info("Connected to server : " + this.remoteServer);
                }
            }
        } catch (UnknownHostException e) {
            resetClient(e);
            throw new FTPConnectionException("Unknown FTP server : " + this.remoteServer);
        } catch (SocketException e) {
            resetClient(e);
            throw new FTPConnectionException("Error during FTP connection : " + e.getMessage());
        } catch (IOException e) {
            resetClient(e);
            throw new FTPConnectionException("Error during FTP connection : " + e.getMessage());
        } finally {
            clearCache();
        }
    }
    
    public boolean hasReconnectSince(long lastConnectionId) {
        return lastConnectionId != this.connectionId;
    }
    
    /**
     * Disconnects from the server.
     */
    public synchronized void disconnect() {
        try {
            this.resetWorkingDirectory();
            
            if (this.client != null && this.client.isConnected()) {
                Logger.defaultLogger().info("Disconnecting from server : " + this.remoteServer + " ...");
                debug("disconnect : disconnect");
                this.client.disconnect();
                Logger.defaultLogger().info("OK : disconnected from server : " + this.remoteServer + ".");
            }
        } catch (IOException e) {
            Logger.defaultLogger().error("An error occured while trying to disconnect from the following FTP server : " + this.remoteServer, e);
        }
    }
    
    /**
     * Checks if the FTP connection is alive and reconnect to server if needed.
     */
    private synchronized void checkConnection() throws FTPConnectionException {
        boolean shallReconnect = true;
        
        if (client != null && client.isConnected()) {
            try {
                if ((System.currentTimeMillis() - lastOpTime) >= TIME_BETWEEN_OPS) {
                    shallReconnect = ! client.sendNoOp();
                    debug("checkConnection : noop", client.getReplyString());
                    this.updateOpTime();
                } else {
                    shallReconnect = false;
                }
            } catch (Throwable e) {
                debug("checkConnection", e);
                Logger.defaultLogger().error("Got an error during connection check", e);
            }
        }
        
        if (shallReconnect) {
            if (client != null) {
                Logger.defaultLogger().info("Disconnected from server : " + this.remoteServer + " ... tyring to reconnect.");
                debug("checkConnection : disconnected ... trying to reconnect", client.getReplyString());                
            }
            this.connect();
        }
    }
    
    private void updateOpTime() {
        this.lastOpTime = System.currentTimeMillis();
    }
    
    public synchronized boolean createNewFile(String remoteFile) throws FTPConnectionException {
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
            resetWorkingDirectory();
        }
    }
    
    public synchronized boolean delete(String remoteFileOrDir) throws FTPConnectionException {
        checkLocked();
        this.checkConnection();
        try {
            FictiveFile infos = this.getRemoteFileInfos(remoteFileOrDir);
            this.updateOpTime();
            
            if (infos.exists()) {
	            if (infos.isFile()) {
	                debug("delete : deleteFile", remoteFileOrDir);
	                return client.deleteFile(remoteFileOrDir);
	            } else {                
	                debug("delete : rmDir", remoteFileOrDir);
	                return FTPReply.isPositiveCompletion(client.rmd(remoteFileOrDir));
	            }
            } else {
                return true;
            }
        } catch (IOException e) {
            resetClient(e);
            throw new FTPConnectionException(e.getMessage());
        } finally {
            removeCachedFileInfos(remoteFileOrDir);
            resetWorkingDirectory();
        }
    }
    
    public synchronized boolean mkdir(String remoteFile) throws FTPConnectionException {
        checkLocked();
        this.checkConnection();
        try {
            File f = new File(remoteFile);
            
            this.changeWorkingDirectory("mkdir", FileNameUtil.heavyNormalizePath(f.getParent(), false));
            this.updateOpTime();
            debug("mkdir : mkdir", remoteFile);
            return client.makeDirectory(f.getName());
        } catch (IOException e) {
            resetClient(e);
            throw new FTPConnectionException(e.getMessage());
        } finally {
            removeCachedFileInfos(remoteFile);
            resetWorkingDirectory();
        }
    }
    
    public synchronized void noop() throws FTPConnectionException {
        checkLocked();
        this.checkConnection();
        try {
            debug("noop");
            client.sendNoOp();
            this.updateOpTime();
        } catch (IOException e) {
            resetClient(e);
            throw new FTPConnectionException(e.getMessage());
        }
    }
    
    public synchronized boolean renameTo(String source, String destination) throws FTPConnectionException {
        checkLocked();
        this.checkConnection();
        
        try {
            debug("renameTo : rename", source + "->" + destination);
            boolean result = client.rename(source, destination);
            this.updateOpTime();
            return result;
        } catch (IOException e) {
            resetClient(e);
            throw new FTPConnectionException(e.getMessage());
        } finally {
            resetWorkingDirectory();
            
            FictiveFile file = fileInfoCache.getCachedFileInfos(destination);
            if (file != null && file.exists() && file.isFile()) {
                // Case 1 : "source" is a file -> selectively remove the entries from the cache
                removeCachedFileInfos(source);
                removeCachedFileInfos(destination);
            } else {
                // Case 2 : We don't know wether "source" is a file or a directory -> destroy all the cache
                clearCache();
            }
        }
    }
    
    public synchronized void flush() {
        clearCache();
        this.resetWorkingDirectory();
    }
    
    public synchronized InputStream getFileInputStream(String file) throws FTPConnectionException {
        checkLocked();
        this.checkConnection();
        try {
            debug("getFileInputStream : retrieveFileStream", file);
            InputStream result = client.retrieveFileStream(file);
            
            if (result == null) {
                Logger.defaultLogger().error("Error trying to get an inputstream on " + file + " : got FTP return message : " + client.getReplyString(), "FTPProxy.getFileOutputStream()");
                throw new FTPConnectionException("Unable to read file : No response from FTP server.");
            }
            
            this.updateOpTime();
            return new FTPFileInputStream(this, result, ownerId);
        } catch (FTPConnectionException e) {
            resetClient(e);
            throw e;
        } catch (IOException e) {
            resetClient(e);
            throw new FTPConnectionException(e.getMessage());
        }
    }
    
    public synchronized OutputStream getFileOutputStream(String file) throws FTPConnectionException {
        return this.getFileOutputStream(file, false);
    }
    
    public synchronized OutputStream getFileOutputStream(String file, boolean append) throws FTPConnectionException {
        checkLocked();
        this.checkConnection();
        OutputStream result = null;
        try {
            if (append) {
                debug("getFileOutputStream : appendFileStream", file);
                result = client.appendFileStream(file);
            } else {
                debug("getFileOutputStream : storeFileStream", file);
                result = client.storeFileStream(file);                
            }
            
            if (result == null) {
                Logger.defaultLogger().error("Error trying to get an outputstream on " + file + " : got FTP return message : " + client.getReplyString(), "FTPProxy.getFileOutputStream()");
                throw new FTPConnectionException("Unable to write file : No response from FTP server.");
            }
            
            this.updateOpTime();
            return new FTPFileOutputStream(this, result, ownerId, file);
        } catch (FTPConnectionException e) {
            resetClient(e);
            throw e;
        } catch (IOException e) {
            resetClient(e);
            throw new FTPConnectionException(e.getMessage());
        }  finally {
            removeCachedFileInfos(file);
        }
    }	
    
    public synchronized FictiveFile[] listFiles(String parentFile) throws FTPConnectionException {
        checkLocked();
        this.checkConnection();
        try {
            
            // File lookup on server
            if (this.changeWorkingDirectory("listFiles", parentFile)) {
                debug("listFiles : listFiles - aL", parentFile);
	            FTPFile[] files = client.listFiles("-al");
	            this.updateOpTime();
	            ArrayList returned = new ArrayList();
	            for (int i=0; i<files.length; i++) {
	                if (acceptListedFile(files[i])) {
		                String remotePath = FileNameUtil.heavyNormalizePath(parentFile + "/" + files[i].getName(), false);
		                returned.add(new FictiveFile(remotePath, remotePath, files[i].getSize(), files[i].isDirectory(), true, files[i].getTimestamp().getTimeInMillis()));
	                }
	            }
	            
	            return (FictiveFile[])returned.toArray(new FictiveFile[0]);
            } else {
                return new FictiveFile[0];
            }
        } catch (IOException e) {
            resetClient(e);
            throw new FTPConnectionException(e.getMessage());
        }  
    }		
    
    /**
     * Filters the "." and ".." directories 
     */
    private boolean acceptListedFile(FTPFile file) {
        String name = file.getName().trim().toLowerCase();
        return (
            ! (
                name.endsWith("/..") 
                || name.endsWith("\\..")
                || name.endsWith("/.")
                || name.endsWith("\\.")
                || name.equals("..")
                || name.equals(".")
            )
        );
    }
    
    public synchronized FictiveFile getRemoteFileInfos(String remoteFile) throws FTPConnectionException {
        checkLocked();
        this.nbGetRemoteFileInfos++;        

        debug("getRemoteFileInfos : getCachedFileInfos", remoteFile);
        FictiveFile info = fileInfoCache.getCachedFileInfos(remoteFile);
        if (info != null) {
            debug("getRemoteFileInfos : Cached data were found", info);
            this.nbCacheRetrieval++;
            debug("Cache Efficiency", new Double(this.nbCacheRetrieval / this.nbGetRemoteFileInfos));
            return info;
        }
        
        this.checkConnection();
        try {          
            String shortName = new File(remoteFile).getName();
            this.changeWorkingDirectory("getRemoteFileInfos", "/");
            
            // File lookup on server
            debug("getRemoteFileInfos : listFiles", remoteFile);
            FTPFile[] files = client.listFiles(remoteFile);
            
            this.updateOpTime();
            if (files.length == 1 && getFileName(files[0].getName()).equals(shortName)) {
                // Existing file
                info = new FictiveFile(remoteFile, remoteFile, files[0].getSize(), files[0].isDirectory(), true, files[0].getTimestamp().getTimeInMillis()); // The local path is initialized to the remote path --> not relevant
            } else if (files.length != 0) {
                // Existing directory, containing files
                info = new FictiveFile(remoteFile, remoteFile, 0, true, true, 0); // The local path is initialized to the remote path --> not relevant                
            } else {
                if (this.changeWorkingDirectory("getRemoteFileInfos", remoteFile)) {
                    // Existing empty directory
                    info = new FictiveFile(remoteFile, remoteFile, 0, true, true, 0); // The local path is initialized to the remote path --> not relevant
                } else {
                    // Non existing file/directory
                    info = new FictiveFile(remoteFile, remoteFile, 0, false, false, 0); // Does not exist
                }
            }
            
            registerFileInfo(remoteFile, info);
            return info;
        } catch (IOException e) {
            resetClient(e);
            throw new FTPConnectionException(e.getMessage());
        } catch (Throwable e) {
            resetClient(e);
            return null;
        }
    }
    
    private static String getFileName(String name) {
        int i = name.lastIndexOf('/'); 
        if (i == -1) {
            return name;
        } else {
            return name.substring(i + 1);
        }
    }
    
    public synchronized void completePendingCommand(boolean blocking) throws IOException, FTPConnectionException {
        checkLocked();
        debug( "completePendingCommand : completePendingCommand");
        if (! this.client.completePendingCommand()) {
            if (blocking) {
                throw new FTPConnectionException("Error trying to complete pending FTP instructions - got the following response from server : " + formatFTPReplyString(this.client.getReplyString()));
            } else {
                Logger.defaultLogger().warn("Closing inputstream : got the following response from server : " + formatFTPReplyString(this.client.getReplyString()));                
            }
        }
        
        resetWorkingDirectory();
    }
    
    private void resetWorkingDirectory() {
        this.workingDirectory = null;
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
        } catch (FTPConnectionException e) {
            resetClient(e);
            throw new UnexpectedConnectionException(e);
        }
    }   
    
    private void resetClient(Throwable e) {
        debug("Destroying client because of exception.", e);
        Logger.defaultLogger().error("FTP client reset because of the following error.", e, "FTPProxy.resetClient()");
        try {
            this.disconnect();
        } catch (Throwable ex) {
            Logger.defaultLogger().warn("Error caucht while trying to disconnect from FTP server.", ex, "FTPProxy.resetClient()");
        }
        this.client = null;
        clearCache();
    }
    
    public FTPProxy cloneProxy() {
        FTPProxy proxy = new FTPProxy();
        proxy.setLogin(login);
        proxy.setPassivMode(passivMode);
        proxy.setImpliciteSec(impliciteSec);
        proxy.setProtocol(protocol);
        proxy.setProtection(protection);
        proxy.setPassword(password);
        proxy.setRemotePort(remotePort);
        proxy.setRemoteServer(remoteServer);
        proxy.setFileInfoCache(fileInfoCache);
        
        return proxy;
    }
    
    protected void debug(String message, Object arg) {
        debug.debug(uid, ownerId, message, arg);
    }
    
    protected void debug(String message) {
        debug(message, null);
    }
    
    protected void removeCachedFileInfos(String fileName) {
        debug("Deleting cache entry - cache size = " + this.fileInfoCache.size(), fileName);
        this.fileInfoCache.removeCachedFileInfos(fileName);
    }
    
    private void clearCache() {
        debug("Clearing the cache - cache size = " + this.fileInfoCache.size());
        this.fileInfoCache.clearCache();
    }
    
    private void registerFileInfo(String remoteFileName, FictiveFile info) {
        debug("Adding cached data if possible - cache size = " + this.fileInfoCache.size(), info);
        this.fileInfoCache.registerFileInfo(remoteFileName, info);
    }
    
    private static String formatFTPReplyString(String source) {
        return source.replace('\n', ' ').replace('\r', ' ');
    }
}