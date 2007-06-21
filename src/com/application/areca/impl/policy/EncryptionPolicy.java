package com.application.areca.impl.policy;

import java.io.File;
import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

import com.application.areca.ApplicationException;
import com.application.areca.impl.EncryptionConfiguration;
import com.myJava.encryption.EncryptionUtil;
import com.myJava.file.AbstractLinkableFileSystemDriver;
import com.myJava.file.EncryptedFileSystemDriver;
import com.myJava.file.FileSystemDriver;
import com.myJava.util.PublicClonable;
import com.myJava.util.ToStringHelper;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 3274863990151426915
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
    
    
    public FileSystemDriver initFileSystemDriver(String basePath, FileSystemDriver predecessor) throws ApplicationException {                 
        if (this.isEncrypted()) {
            File storageDir = new File(basePath).getParentFile();
            
            // Génération de la clef + paramètres
            EncryptionConfiguration params = EncryptionConfiguration.getParameters(this.getEncryptionAlgorithm());
            Key key = new SecretKeySpec(getNormalizedEncryptionKey(this.getEncryptionKey(), params), params.getAlgorithm());
            
            // Initialisation du driver
            AbstractLinkableFileSystemDriver driver = new EncryptedFileSystemDriver(
                    storageDir,
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
    
    /**
     * Normalizes the key so it can by used by the encryption algorithm.
     * <BR>Note that the "strong encryption" mode is highly recommended :
     * <BR>- It is platform independant (while the former key creation algorithm depends on the platform's encoding)
     * <BR>- It generates stronger keys (the password can have any size : it is hashed using MD5 and (if needed) SHA to produce a byte array)
     */
    private static byte[] getNormalizedEncryptionKey(String encryptionKey, EncryptionConfiguration params) {
        if (params.isStrongEncryption()) {
            return EncryptionUtil.getNormalizedEncryptionKey(encryptionKey, params.getKeySize());
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
        ToStringHelper.append("Algorithm", this.encryptionAlgorithm, sb);
        ToStringHelper.append("Key", this.encryptionKey, sb);
        return ToStringHelper.close(sb);
    }
}
