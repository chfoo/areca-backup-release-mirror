package com.application.areca.impl.policy;

import java.io.File;
import java.security.Key;

import com.application.areca.ApplicationException;
import com.application.areca.impl.EncryptionConfiguration;
import com.myJava.encryption.EncryptionUtil;
import com.myJava.file.driver.EncryptedFileSystemDriver;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.file.driver.AbstractLinkableFileSystemDriver;
import com.myJava.object.Duplicable;
import com.myJava.object.ToStringHelper;
import com.myJava.util.Util;
import com.myJava.util.log.Logger;

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
public class EncryptionPolicy implements Duplicable {

    protected String encryptionKey = null;
    protected String encryptionAlgorithm = null;
    protected boolean encryptNames = true; // Names are encrypted by default
    protected boolean isEncrypted = false;
    protected String nameWrappingMode = EncryptedFileSystemDriver.WRAP_DEFAULT;
    
    public FileSystemDriver initFileSystemDriver(File basePath, FileSystemDriver predecessor) throws ApplicationException {                 
        if (this.isEncrypted()) {         
            EncryptionConfiguration params = EncryptionConfiguration.getParameters(this.getEncryptionAlgorithm());
            Key key = getNormalizedEncryptionKey(this.getEncryptionKey(), params);
            
            // Driver initialization
            AbstractLinkableFileSystemDriver driver = new EncryptedFileSystemDriver(
                    basePath,
                    params.getTransformation(),
                    params.getIV(), 
                    key,
                    encryptNames,
                    nameWrappingMode
            );
        
            driver.setPredecessor(predecessor);
            return driver;
        } else {
            return predecessor;
        }
    }
    
    public static boolean validateEncryptionKey(String encryptionKey, EncryptionConfiguration params) {
        if (encryptionKey == null || encryptionKey.trim().length() == 0) {
            return false;
        } else if (params.getKeyConvention().equals(EncryptionConfiguration.KEYCONV_HASH)) {
            return true;
        } else if (params.getKeyConvention().equals(EncryptionConfiguration.KEYCONV_RAW)) {
            try {
                byte[] b = Util.base16Decode(encryptionKey);
                return (b.length == params.getKeySize());
            } catch (Throwable e) {
                return false;
            }
        } else {
            Logger.defaultLogger().warn("Unknown encryption configuration : " + params.getId());
            return false;
        }
    }
    
    /**
     * Normalizes the key so it can by used by the encryption algorithm.
     * <BR>Note that the "strong encryption" mode is highly recommended :
     * <BR>- It is platform independant (while the former key creation algorithm depends on the platform's encoding)
     * <BR>- It generates stronger keys (the password can have any size : it is hashed using MD5 and (if needed) SHA to produce a byte array)
     */
    private static Key getNormalizedEncryptionKey(String encryptionKey, EncryptionConfiguration params) {
        if (! validateEncryptionKey(encryptionKey, params)) {
            throw new IllegalArgumentException("Illegal key : [" + encryptionKey + "] for algorithm : [" + params.getFullName()  + "/" + params.getId() + "]");
        }

        if (params.getKeyConvention().equals(EncryptionConfiguration.KEYCONV_HASH)) {
            return EncryptionUtil.buildKeyFromPassphrase(encryptionKey, params.getKeySize(), params.getAlgorithm());
    	} else if (params.getKeyConvention().equals(EncryptionConfiguration.KEYCONV_RAW)) {
            return EncryptionUtil.buildKeyFromRawInput(encryptionKey, params.getAlgorithm());
        } else {
        	throw new IllegalArgumentException("Illegal encryption key convention : " + params.getKeyConvention());
        }
    }
    
    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }
    
    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }
    
    public String getEncryptionKey() {
        return encryptionKey;
    }
    
    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public boolean isEncryptNames() {
		return this.encryptNames;
	}

	public void setEncryptNames(boolean encryptNames) {
		this.encryptNames = encryptNames;
	}

	public void setEncrypted(boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
    }

	public String getNameWrappingMode() {
		return nameWrappingMode;
	}

	public void setNameWrappingMode(String nameWrappingMode) {
		this.nameWrappingMode = nameWrappingMode;
	}

	public Duplicable duplicate() {
        EncryptionPolicy other = new EncryptionPolicy();
        other.encryptionAlgorithm = encryptionAlgorithm;
        other.encryptionKey = encryptionKey;
        other.isEncrypted = isEncrypted;
        other.encryptNames = encryptNames;
        other.nameWrappingMode = nameWrappingMode;
        return other;
    }
    
    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("IsEncrypted", this.isEncrypted, sb);
        if (isEncrypted) {
            ToStringHelper.append("Algorithm", this.encryptionAlgorithm, sb);
            ToStringHelper.append("Encrypt names", this.encryptNames, sb);
            ToStringHelper.append("Wrap", this.nameWrappingMode, sb);
        }
        return ToStringHelper.close(sb);
    }
}
