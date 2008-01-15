package com.application.areca.impl.policy;

import java.io.File;
import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

import com.application.areca.ApplicationException;
import com.application.areca.impl.EncryptionConfiguration;
import com.myJava.encryption.EncryptionUtil;
import com.myJava.file.driver.AbstractLinkableFileSystemDriver;
import com.myJava.file.driver.EncryptedFileSystemDriver;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.object.PublicClonable;
import com.myJava.object.ToStringHelper;
import com.myJava.util.Util;
import com.myJava.util.log.Logger;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 1926729655347670856
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
public class EncryptionPolicy implements PublicClonable {
   
    /**
     * Clef de cryptage si les fichiers sont cryptées
     */
    protected String encryptionKey = null;
    
    /**
     * Algorithm used if encryption is enabled
     */
    protected String encryptionAlgorithm = null;
    
    protected boolean isEncrypted = false;
    
    
    public FileSystemDriver initFileSystemDriver(File basePath, FileSystemDriver predecessor) throws ApplicationException {                 
        if (this.isEncrypted()) {         
            // Génération de la clef + paramètres
            EncryptionConfiguration params = EncryptionConfiguration.getParameters(this.getEncryptionAlgorithm());
            Key key = new SecretKeySpec(getNormalizedEncryptionKey(this.getEncryptionKey(), params), params.getAlgorithm());
            
            // Initialisation du driver
            AbstractLinkableFileSystemDriver driver = new EncryptedFileSystemDriver(
                    basePath,
                    params.getTransformation(),
                    params.getIV(), 
                    key
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
        } else if (
                params.getKeyConvention().equals(EncryptionConfiguration.KEYCONV_HASH)
                || params.getKeyConvention().equals(EncryptionConfiguration.KEYCONV_OLD)
        ) {
            return true;
        } else if (params.getKeyConvention().equals(EncryptionConfiguration.KEYCONV_RAW)) {
            try {
                byte[] b = Util.parseHexa(encryptionKey);
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
    private static byte[] getNormalizedEncryptionKey(String encryptionKey, EncryptionConfiguration params) {
        if (! validateEncryptionKey(encryptionKey, params)) {
            throw new IllegalArgumentException("Illegal key : [" + encryptionKey + "] for algorithm : [" + params.getFullName()  + "/" + params.getId() + "]");
        }

        if (params.getKeyConvention().equals(EncryptionConfiguration.KEYCONV_HASH)) {
            return EncryptionUtil.getNormalizedEncryptionKey(encryptionKey, params.getKeySize());
        } else if (params.getKeyConvention().equals(EncryptionConfiguration.KEYCONV_RAW)) {
            return Util.parseHexa(encryptionKey);
        } else {
            // Older key generation method .... NOT RECOMMENDED (platform dependant and weak)
	        String normalizedKey = encryptionKey;
	        while (normalizedKey.length() < params.getKeySize()) {
	            normalizedKey = normalizedKey + normalizedKey;
	        }
	        normalizedKey = normalizedKey.substring(0, params.getKeySize());
	        return normalizedKey.getBytes();
        }
    }
    
    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }
    
    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = (encryptionAlgorithm == null ? EncryptionConfiguration.DEFAULT_ALGORITHM : encryptionAlgorithm);
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
    
    public void setEncrypted(boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
    }
    
    public PublicClonable duplicate() {
        EncryptionPolicy other = new EncryptionPolicy();
        other.encryptionAlgorithm = encryptionAlgorithm;
        other.encryptionKey = encryptionKey;
        other.isEncrypted = isEncrypted;
        return other;
    }
    
    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("IsEncrypted", this.isEncrypted, sb);
        if (isEncrypted) {
            ToStringHelper.append("Algorithm", this.encryptionAlgorithm, sb);
            ToStringHelper.append("Key", this.encryptionKey, sb);
        }
        return ToStringHelper.close(sb);
    }
}
