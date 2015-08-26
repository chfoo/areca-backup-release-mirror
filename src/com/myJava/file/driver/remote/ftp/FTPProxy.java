package com.myJava.file.driver.remote.ftp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.FileNameUtil;
import com.myJava.file.driver.remote.AbstractProxy;
import com.myJava.file.driver.remote.AbstractRemoteFileSystemDriver;
import com.myJava.file.driver.remote.FictiveFile;
import com.myJava.file.driver.remote.RemoteConnectionException;
import com.myJava.file.driver.remote.RemoteFileInputStream;
import com.myJava.file.driver.remote.RemoteFileOutputStream;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.object.ToStringHelper;
import com.myJava.util.Util;
import com.myJava.util.log.Logger;

/**
 * Proxy that abstracts the ftp access layer.
 * <BR>It wraps the ftp framework.
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
public class FTPProxy extends AbstractProxy {
	private static final long TIME_BETWEEN_OPS = FrameworkConfiguration.getInstance().getFTPNoopDelay(); // Time between noops (milliseconds)
	private static final boolean CACHE_ENABLED = FrameworkConfiguration.getInstance().isRemoteCacheMode();

	// PARAMETERS
	private String remoteServer;
	private int remotePort;
	private String login;
	private String password;
	private boolean passivMode;
	private String protocol = null;
	private String protection = null;
	private boolean impliciteSec = false;
	private String controlEncoding = null;
	private boolean ignorePsvErrors = false;

	// CLIENT
	private FTPClient client;
	private String workingDirectory = null;

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public boolean isPassivMode() {
		return passivMode;
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

	public String getControlEncoding() {
		return controlEncoding;
	}

	public boolean isIgnorePsvErrors() {
		return ignorePsvErrors;
	}

	public void setIgnorePsvErrors(boolean ignorePsvErrors) {
		this.ignorePsvErrors = ignorePsvErrors;
	}

	public void setControlEncoding(String controlEncoding) {
		if (controlEncoding != null && controlEncoding.trim().length() == 0) {
			this.controlEncoding = null;
		} else {
			this.controlEncoding = controlEncoding;
		}
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
			&& EqualsHelper.equals(this.ignorePsvErrors, o.ignorePsvErrors)
			&& EqualsHelper.equals(this.controlEncoding, o.controlEncoding)
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
		h = HashHelper.hash(h, this.ignorePsvErrors);
		h = HashHelper.hash(h, this.controlEncoding);
		h = HashHelper.hash(h, this.remoteServer);
		return h;
	}    

	public String toString() {
		StringBuffer sb = ToStringHelper.init(this);
		ToStringHelper.append("Host", remoteServer, sb);
		ToStringHelper.append("Port", remotePort, sb);
		return ToStringHelper.close(sb);
	}

	public boolean isSecured() {
		return this.protocol != null;
	}

	private boolean changeWorkingDirectory(String inst, String dir) throws IOException {
		if ((!CACHE_ENABLED) || dir == null || (! (dir.equals(workingDirectory)))) {           
			boolean res = client.changeWorkingDirectory(encodeRemoteFile(dir));
			if (res) {
				this.workingDirectory = dir;
				debug(inst + " : changeWorkingDirectory - OK", dir);
			} else {
				debug(inst + " : changeWorkingDirectory - NOK", dir);
			}
			return res;
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
						null,  // TODO
						ignorePsvErrors
				);
			} else {
				this.client = new FTPClient(ignorePsvErrors);
			}
			if (this.controlEncoding != null) {
				this.client.setControlEncoding(this.controlEncoding); 
				debug("control encoding : ", controlEncoding);
			}
			
			//InetAddress adr = InetAddress.getByName(this.remoteServer);
			Logger.defaultLogger().info("Trying to connect to server : " + this.remoteServer + " ...");

			debug("connect : connect", remoteServer);
			client.connect(remoteServer, this.remotePort);
			//RemoteConnectionMonitor.getInstance().registerNewConnection(client, this);

			Logger.defaultLogger().info("Received FTP server response : " + formatFTPReplyString(client.getReplyString()));
			this.connectionId = Util.getRndLong();

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

						// PASSIVE MODE REQUESTED
						debug("connect : pasv");
						client.enterLocalPassiveMode();

						reply = client.getReplyCode();
						if( ! FTPReply.isPositiveCompletion(reply)) {
							String msg = formatFTPReplyString(client.getReplyString());
							this.disconnect();
							throw new FTPConnectionException("Unable to switch to passive mode. Received response : " + msg);
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
			throw new FTPConnectionException("Unknown server : " + this.remoteServer);
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

	protected void resetClient(Throwable e) {
		debug("Destroying client because of exception.", e);
		Logger.defaultLogger().error("Client reset because of the following error.", e, "AbstractProxy.resetClient()");
		try {
			this.disconnect();
		} catch (Throwable ex) {
			Logger.defaultLogger().warn("Error caucht while trying to disconnect from remote server.", ex, "AbstractProxy.resetClient()");
		}
		this.client = null;
		clearCache();
	}

	/**
	 * Disconnects from the server.
	 */
	public synchronized void disconnect() {
		try {
			this.resetContextData();

			if (this.client != null && this.client.isConnected()) {
				Logger.defaultLogger().info("Disconnecting from server : " + this.remoteServer + " ...");
				debug("disconnect : disconnect");
				this.client.disconnect();
				//RemoteConnectionMonitor.getInstance().registerCloseEvent(client, this);
				Logger.defaultLogger().info("OK : disconnected from server : " + this.remoteServer + ".");
			}
		} catch (IOException e) {
			Logger.defaultLogger().error("An error occurred while trying to disconnect from the following FTP server : " + this.remoteServer, e);
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
					shallReconnect = false;
					debug("checkConnection : connection successfully checked");
				} else {
					shallReconnect = false;
					debug("checkConnection : no need to check connection");
				}
			} catch (FTPConnectionClosedException e) {
				// Do nothing, since this response is expected.
			} catch (Throwable e) {
				debug("checkConnection", e);
				Logger.defaultLogger().error("Got an error during connection check", e);
			}
		}

		if (shallReconnect) {
			if (client != null) {
				Logger.defaultLogger().info("Disconnected from server : " + this.remoteServer + " ... tyring to reconnect.");
				debug("checkConnection : disconnected ... trying to reconnect", client.getReplyString());                
			} else {
				debug("checkConnection : not connected ... trying to connect"); 	
			}
			this.connect();
		}
	}

	public synchronized boolean deleteFile(String remoteFile) throws FTPConnectionException {
		checkLocked();
		this.checkConnection();
		try {
			this.updateOpTime();
			debug("deleteFile : ", remoteFile);
			return client.deleteFile(encodeRemoteFile(remoteFile));
		} catch (IOException e) {
			resetClient(e);
			resetContextData();
			throw new FTPConnectionException(e.getMessage());
		} catch (RuntimeException e) {
			resetContextData();
			throw e;
		} finally {
			removeCachedFileInfos(remoteFile);
		}
	}

	public synchronized boolean deleteDir(String remoteDir) throws FTPConnectionException {
		checkLocked();
		this.checkConnection();
		try {
			this.updateOpTime();
			debug("deleteDir : ", remoteDir);
			resetContextData();
			return FTPReply.isPositiveCompletion(client.rmd(encodeRemoteFile(remoteDir)));
		} catch (IOException e) {
			resetClient(e);
			resetContextData();
			throw new FTPConnectionException(e.getMessage());
		} catch (RuntimeException e) {
			resetContextData();
			throw e;
		} finally {
			removeCachedFileInfos(remoteDir);
		}
	}

	public synchronized boolean mkdir(String remoteFile) throws FTPConnectionException {
		checkLocked();
		this.checkConnection();
		try {
			File f = new File(remoteFile);

			this.changeWorkingDirectory("mkdir", AbstractRemoteFileSystemDriver.normalizeIfNeeded(f.getParent()));
			this.updateOpTime();
			debug("mkdir : mkdir", remoteFile);
			boolean result = client.makeDirectory(encodeRemoteFile(f.getName()));
			FictiveFile existing = this.fileInfoCache.getCachedFileInfos(remoteFile);
			if (result) {
				if (existing != null) {
					existing.init(0, true, true, 0);
				} else {
					this.fileInfoCache.registerFileInfo(remoteFile, new FictiveFile(remoteFile, remoteFile, 0, true, true, 0));
				}
			}
			return result;
		} catch (IOException e) {
			removeCachedFileInfos(remoteFile);
			resetClient(e);
			throw new FTPConnectionException(e.getMessage());
		} finally {
			resetContextData();
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
			boolean result = client.rename(encodeRemoteFile(source), encodeRemoteFile(destination));
			this.updateOpTime();
			return result;
		} catch (IOException e) {
			resetClient(e);
			throw new FTPConnectionException(e.getMessage());
		} finally {
			resetContextData();

			FictiveFile file = fileInfoCache.getCachedFileInfos(destination);
			if (file != null && file.exists() && file.isFile()) {
				// Case 1 : "source" is a file -> selectively remove the entries from the cache
				removeCachedFileInfos(source);
				removeCachedFileInfos(destination);
			} else {
				// Case 2 : We don't know whether "source" is a file or a directory -> destroy all the cache
				clearCache();
			}
		}
	}

	public synchronized InputStream getFileInputStream(String file) throws FTPConnectionException {
		checkLocked();
		this.checkConnection();
		try {
			debug("getFileInputStream : retrieveFileStream", file);
			InputStream result = client.retrieveFileStream(encodeRemoteFile(file));

			if (result == null) {
				Logger.defaultLogger().error("Error trying to get an inputstream on " + file + " : got FTP return message : " + client.getReplyString(), "FTPProxy.getFileInputStream()");
				throw new FTPConnectionException("Unable to read file : No response from FTP server.");
			}

			this.updateOpTime();
			return new RemoteFileInputStream(this, result, ownerId);
		} catch (FTPConnectionException e) {
			resetClient(e);
			throw e;
		} catch (IOException e) {
			resetClient(e);
			throw new FTPConnectionException(e.getMessage());
		}
	}

	public synchronized OutputStream getFileOutputStream(String file, boolean append) throws RemoteConnectionException {
		checkLocked();
		this.checkConnection();
		OutputStream result = null;
		try {
			if (append) {
				debug("getFileOutputStream : appendFileStream", file);
				result = client.appendFileStream(encodeRemoteFile(file));
			} else {
				debug("getFileOutputStream : storeFileStream", file);
				result = client.storeFileStream(encodeRemoteFile(file));                
			}

			if (result == null) {
				String rep = client.getReplyString();
				Logger.defaultLogger().error("Error trying to get an outputstream on " + file + " : got FTP return message : " + rep, "FTPProxy.getFileOutputStream()");
				throw new FTPConnectionException("Unable to write file : Response received from FTP server was : [" + rep + "]");
			}

			this.updateOpTime();
			return new RemoteFileOutputStream(this, result, ownerId, file);
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
						String remotePath = FileNameUtil.normalizeSlashes(parentFile + "/" + files[i].getName(), false);
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
		if (file == null || file.getName() == null) {
			return false;
		}
		
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
			String rmtfile = encodeRemoteFile(remoteFile);
			FTPFile[] files = client.listFiles(rmtfile);

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

	public synchronized void completePendingCommand(boolean blocking) throws IOException, RemoteConnectionException {
		checkLocked();
		debug( "completePendingCommand : completePendingCommand");
		if (! this.client.completePendingCommand()) {
			if (blocking) {
				throw new FTPConnectionException("Error trying to complete pending FTP instructions - got the following response from server : " + formatFTPReplyString(this.client.getReplyString()));
			} else {
				Logger.defaultLogger().warn("Closing inputstream : " + formatFTPReplyString(this.client.getReplyString()));                
			}
		}

		resetContextData();
	}

	protected void resetContextData() {
		this.workingDirectory = null;
	} 

	public AbstractProxy cloneProxy() {
		FTPProxy proxy = new FTPProxy();
		proxy.setLogin(login);
		proxy.setPassivMode(passivMode);
		proxy.setImpliciteSec(impliciteSec);
		proxy.setProtocol(protocol);
		proxy.setProtection(protection);
		proxy.setPassword(password);
		proxy.setRemotePort(remotePort);
		proxy.setRemoteServer(remoteServer);
		proxy.setIgnorePsvErrors(ignorePsvErrors);
		proxy.setControlEncoding(controlEncoding);
		proxy.setFileInfoCache(fileInfoCache);

		return proxy;
	}

	private static String formatFTPReplyString(String source) {
		return source.replace('\n', ' ').replace('\r', ' ');
	}
	
	private String encodeRemoteFile(String path) {
		return path;
		//return "\"" + path + "\"";
	}
}