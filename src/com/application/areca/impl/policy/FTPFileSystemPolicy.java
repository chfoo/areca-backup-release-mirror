package com.application.areca.impl.policy;

import java.io.File;

import com.application.areca.ApplicationException;
import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemManager;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.file.driver.cache.CachedFileSystemDriver;
import com.myJava.file.driver.remote.AbstractProxy;
import com.myJava.file.driver.remote.ftp.FTPFileSystemDriver;
import com.myJava.file.driver.remote.ftp.FTPProxy;
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
public class FTPFileSystemPolicy 
extends AbstractRemoteFileSystemPolicy
implements FileSystemPolicy {
    public static final int DEFAULT_PORT = 21;
    
    private String remoteServer;
    private int remotePort = DEFAULT_PORT;
    private String login;
    private String password;
    private boolean passivMode;
    private String protocol = null;
    private String protection = null;
    private boolean implicit = false;
	private String controlEncoding = null;
	private boolean ignorePsvErrors = false;
    
    public boolean isSecured() {
        return protocol != null && protocol.length() != 0;
    }

    public String getDisplayableParameters(boolean fullPath) {
        StringBuffer sb = new StringBuffer();
        if (isSecured()) {
            sb.append("ftps://");            
        } else {
            sb.append("ftp://");
        }
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
        FTPProxy proxy = new FTPProxy();
        proxy.setLogin(login);
        proxy.setIgnorePsvErrors(ignorePsvErrors);
        proxy.setPassivMode(passivMode);
        proxy.setImpliciteSec(implicit);
        proxy.setProtocol(protocol);
        proxy.setProtection(protection);
        proxy.setControlEncoding(controlEncoding);
        proxy.setPassword(password);
        proxy.setRemotePort(remotePort);
        proxy.setRemoteServer(remoteServer);
        
        return proxy;
    }

    public FileSystemDriver initFileSystemDriver() throws ApplicationException {
        FileSystemDriver base = new FTPFileSystemDriver((FTPProxy)buildProxy(), getLocalDirectory(), getRemoteDirectory());
        if (CACHE) {
            File storageDir = getArchiveDirectory();
            return new CachedFileSystemDriver(base, FileSystemManager.getParentFile(storageDir), CACHE_DEPTH);
        } else {
            return base;
        }
    }
    
    public void copyAttributes(FTPFileSystemPolicy policy) {
    	super.copyAttributes(policy);
        policy.setRemoteServer(this.remoteServer);
        policy.setRemotePort(this.remotePort);
        policy.setIgnorePsvErrors(this.ignorePsvErrors);
        policy.setLogin(this.login);
        policy.setPassword(this.password);
        policy.setPassiveMode(this.passivMode);
        policy.setImplicit(this.implicit);
        policy.setControlEncoding(this.controlEncoding);
        policy.setProtocol(this.protocol);
        policy.setProtection(this.protection);
    }
    
    public Duplicable duplicate() {
        FTPFileSystemPolicy policy = new FTPFileSystemPolicy();
        copyAttributes(policy);
        return policy;
    }
    
	public AccessInformations checkReachable() {
		AccessInformations ret = new AccessInformations();
		return ret;
	}
       
    public String getLogin() {
        return login;
    }
    
    public void setLogin(String login) {
        this.login = login;
    }

    public String getControlEncoding() {
		return controlEncoding;
	}

	public void setControlEncoding(String controlEncoding) {
		this.controlEncoding = controlEncoding;
	}

	public boolean isPassivMode() {
        return passivMode;
    }
    
    public void setPassiveMode(boolean passivMode) {
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

    public boolean isIgnorePsvErrors() {
		return ignorePsvErrors;
	}

	public void setIgnorePsvErrors(boolean ignorePsvErrors) {
		this.ignorePsvErrors = ignorePsvErrors;
	}

	public void setRemotePort(int remotePort) {
        if (remotePort == 0) {
            remotePort = DEFAULT_PORT;
        }
        this.remotePort = remotePort;
    }
    
    public String getRemoteServer() {
        return remoteServer;
    }
    
    public void setRemoteServer(String remoteServer) {
        this.remoteServer = remoteServer;
    }
    
    public String getUid() {
        return medium.getTarget().getUid();
    }

    public String getProtocol() {
        return protocol;
    }
    
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getProtection() {
        return protection;
    }

    public void setProtection(String protection) {
        this.protection = protection;
    }

    public boolean isImplicit() {
        return implicit;
    }
    
    public void setImplicit(boolean implicit) {
        this.implicit = implicit;
    }
    
    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("Server", this.remoteServer, sb);
        ToStringHelper.append("Port", this.remotePort, sb);
        ToStringHelper.append("Login", this.login, sb);
        ToStringHelper.append("Passiv", this.passivMode, sb);
        ToStringHelper.append("Protocol", this.protocol, sb);
        ToStringHelper.append("Ignore Pasv Errors", this.ignorePsvErrors, sb);
        ToStringHelper.append("Protection", this.protection, sb);  
        ToStringHelper.append("Control Encoding", this.controlEncoding, sb);
        ToStringHelper.append("Implicit", this.implicit, sb);
        ToStringHelper.append("Directory", this.remoteDirectory, sb);
        ToStringHelper.append("Name", this.archiveName, sb);
        return ToStringHelper.close(sb);
    }
}
