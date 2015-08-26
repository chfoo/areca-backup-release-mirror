package com.application.areca.impl.policy;

import java.io.File;

import com.application.areca.ApplicationException;
import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemManager;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.file.driver.cache.CachedFileSystemDriver;
import com.myJava.file.driver.remote.AbstractProxy;
import com.myJava.file.driver.remote.sftp.SFTPFileSystemDriver;
import com.myJava.file.driver.remote.sftp.jsch.SFTPProxy;
import com.myJava.object.Duplicable;
import com.myJava.object.ToStringHelper;

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
public class SFTPFileSystemPolicy 
extends AbstractRemoteFileSystemPolicy
implements FileSystemPolicy {      
    private String remoteServer;
    private int remotePort;
    private String login;
    private String password;
    private String hostKey;
    private boolean checkHostKey;
	private boolean useCertificateAuth;
	private String certificateFileName;
	private boolean encryptedCert;
    
    public String getDisplayableParameters(boolean fullPath) {
        StringBuffer sb = new StringBuffer();
        sb.append("sftp://");
        sb.append(login).append("@").append(remoteServer).append(":").append(remotePort);
        if (! FileNameUtil.startsWithSeparator(remoteDirectory)) {
            sb.append("/");
        }
        sb.append(remoteDirectory);
        
        if (fullPath) {
        	sb.append("/").append(STORAGE_DIRECTORY_PREFIX).append(getUid());
        }
        
        return sb.toString();
    }
    
    protected AbstractProxy buildProxy() {
		SFTPProxy proxy = new SFTPProxy(this.remoteDirectory);
        proxy.setLogin(login);
        proxy.setPassword(password);
        proxy.setRemotePort(remotePort);
        proxy.setRemoteServer(remoteServer);
        proxy.setHostKey(hostKey);
        proxy.setCheckHostKey(checkHostKey);
        proxy.setUseCertificateAuth(useCertificateAuth);
        proxy.setCertificateFileName(certificateFileName);
        if (encryptedCert) {
            proxy.setCertPassPhrase(password);	
        }
        
        return proxy;
    }

    public FileSystemDriver initFileSystemDriver() throws ApplicationException {
        FileSystemDriver base = new SFTPFileSystemDriver((SFTPProxy)buildProxy(), getLocalDirectory(), getRemoteDirectory());
        if (CACHE) {
            File storageDir = getArchiveDirectory();
            return new CachedFileSystemDriver(base, FileSystemManager.getParentFile(storageDir), CACHE_DEPTH);
        } else {
            return base;
        }
    }


    public boolean isEncryptedCert() {
		return encryptedCert;
	}

	public void setEncryptedCert(boolean encryptedCert) {
		this.encryptedCert = encryptedCert;
	}

	public void copyAttributes(SFTPFileSystemPolicy policy) {
    	super.copyAttributes(policy);
        policy.setRemoteServer(this.remoteServer);
        policy.setRemotePort(this.remotePort);
        policy.setLogin(this.login);
        policy.setHostKey(this.hostKey);
        policy.setPassword(this.password);
        policy.setCheckHostKey(checkHostKey);
        policy.setUseCertificateAuth(this.useCertificateAuth);
        policy.setCertificateFileName(this.certificateFileName);
        policy.setEncryptedCert(encryptedCert);
    }
    
    public Duplicable duplicate() {
        SFTPFileSystemPolicy policy = new SFTPFileSystemPolicy();
        copyAttributes(policy);
        return policy;
    }
    
	public AccessInformations checkReachable() {
		AccessInformations ret = new AccessInformations();
		return ret;
	}
	
	public boolean isUseCertificateAuth() {
		return useCertificateAuth;
	}

	public void setUseCertificateAuth(boolean useCertificateAuth) {
		this.useCertificateAuth = useCertificateAuth;
	}

	public String getCertificateFileName() {
		return certificateFileName;
	}

	public void setCertificateFileName(String certificateFileName) {
		this.certificateFileName = certificateFileName;
	}

    public boolean isCheckHostKey() {
		return checkHostKey;
	}

	public void setCheckHostKey(boolean checkHostKey) {
		this.checkHostKey = checkHostKey;
	}

	public String getLogin() {
        return login;
    }
    
    public void setLogin(String login) {
        this.login = login;
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
    
    public String getRemoteDirectory() {
        return remoteDirectory;
    }
    
    public void setRemoteDirectory(String remoteDirectory) {
        this.remoteDirectory = remoteDirectory;
    }
    
    public String getUid() {
        return medium.getTarget().getUid();
    }

    public String getHostKey() {
		return hostKey;
	}

	public void setHostKey(String hostKey) {
		this.hostKey = hostKey;
	}

	public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("Server", this.remoteServer, sb);
        ToStringHelper.append("Port", this.remotePort, sb);
        ToStringHelper.append("Login", this.login, sb);
        ToStringHelper.append("Directory", this.remoteDirectory, sb);
        ToStringHelper.append("Name", this.archiveName, sb);
        ToStringHelper.append("HostKey", this.hostKey, sb); 
        ToStringHelper.append("CheckHostKey", this.checkHostKey, sb);     
        ToStringHelper.append("UseCertificateAuth", this.useCertificateAuth, sb);  
        ToStringHelper.append("CertificateFileName", this.certificateFileName, sb);  
        ToStringHelper.append("EncryptedCert", this.encryptedCert, sb); 
        return ToStringHelper.close(sb);
    }
}
