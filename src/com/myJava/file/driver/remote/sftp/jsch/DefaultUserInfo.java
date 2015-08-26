package com.myJava.file.driver.remote.sftp.jsch;


import com.jcraft.jsch.UserInfo;
import com.myJava.util.log.Logger;

/**
 * 
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
public class DefaultUserInfo implements UserInfo {
	private String password;
	private String certPassphrase;
	private String certPath;

	public DefaultUserInfo(String password, String certPassphrase, String certPath) {
		this.password = password;
		this.certPassphrase = certPassphrase;
		this.certPath = certPath;
	}

	public String getPassphrase() {
		if (certPassphrase == null || certPassphrase.trim().length() == 0) {
			Logger.defaultLogger().warn("No passphrase set for SSH private key file.");
		}
		return certPassphrase;
	}

	public String getPassword() {
		return password;
	}

	public boolean promptPassword(String message) {
		return true;
	}

	public boolean promptPassphrase(String message) {
		Logger.defaultLogger().info("Using passphrase for private key file : " + certPath);
		return true;
	}

	public boolean promptYesNo(String message) {
		throw new UnsupportedOperationException(message);
	}

	public void showMessage(String message) {
		Logger.defaultLogger().info(message);
	}
}
