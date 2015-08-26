package com.myJava.file.driver.remote.sftp;

import java.io.File;
import java.io.IOException;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.FileNameUtil;
import com.myJava.file.driver.remote.AbstractRemoteFileSystemDriver;
import com.myJava.file.driver.remote.RemoteFileInfoCache;
import com.myJava.file.driver.remote.sftp.jsch.SFTPProxy;
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
public class SFTPFileSystemDriver extends AbstractRemoteFileSystemDriver {
    public SFTPFileSystemDriver(SFTPProxy sftpProxy, File localRoot, String remoteRoot) {
    	super(localRoot);
    	this.maxProxies = FrameworkConfiguration.getInstance().getMaxFTPProxies();
        this.proxy = sftpProxy;
        this.remoteRootDirectory = FileNameUtil.normalizeSlashes(FileNameUtil.normalizePath(remoteRoot), true);
        this.proxy.setFileInfoCache(new RemoteFileInfoCache());
    } 

    public short getType(File file) throws IOException {
    	if (isFile(file)) {
    		return FileMetaDataAccessor.TYPE_FILE;
    	} else {
    		return FileMetaDataAccessor.TYPE_DIRECTORY;
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
            if (o instanceof SFTPFileSystemDriver) {
                SFTPFileSystemDriver other = (SFTPFileSystemDriver)o;

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
        ToStringHelper.append("HOST", ((SFTPProxy)this.proxy).getRemoteServer(), sb);
        ToStringHelper.append("PORT", ((SFTPProxy)this.proxy).getRemotePort(), sb);
        ToStringHelper.append("LOGIN", ((SFTPProxy)this.proxy).getLogin(), sb);
        return ToStringHelper.close(sb);
    }
    
    public String getPhysicalPath(File file) {
    	SFTPProxy pxy = (SFTPProxy)proxy;
        StringBuffer sb = new StringBuffer();
        sb.append("sftp://");
        sb.append(pxy.getLogin()).append("@").append(pxy.getRemoteServer()).append(":").append(pxy.getRemotePort());
        if (! FileNameUtil.startsWithSeparator(remoteRootDirectory)) {
            sb.append("/");
        }

        sb.append(remoteRootDirectory);
        String relativePath = getRemoteRelativePath(file);
        if (! relativePath.startsWith("/") && ! remoteRootDirectory.endsWith("/")) {
            sb.append("/");
        }
        sb.append(relativePath);
        return sb.toString();
	}
    
    /*
    public static void main(String[] args) {
    	try {
			String host = "192.168.2.102";
			String login = "sshboy";
			String password = "izn0g00d!";
			int port = 39;
			
			SFTPProxy px = new SFTPProxy(new ConsoleHostKeyVerification(), "/home/olivier");
			px.setRemoteServer(host);
			px.setRemotePort(port);
			px.setLogin(login);
			px.setPassword(password);
			
			File root = new File("/sftp"); 
			
			SFTPFileSystemDriver drv = new SFTPFileSystemDriver(px, root, "/test");
			FileSystemManager.getInstance().registerDriver(root, drv);
			
			
			File in = new File("/home/olivier/Bureau/changes_areca");
			File out = new File(root, "copy1");
			
			FileTool.getInstance().copyFile(in, out.getParentFile(), out.getName(), null, null);
		} catch (Throwable e) {
			e.printStackTrace();
		}
    }
    */
}