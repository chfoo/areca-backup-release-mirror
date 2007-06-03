package com.application.areca.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.crypto.spec.IvParameterSpec;

import com.myJava.util.EqualsHelper;
import com.myJava.util.HashHelper;

/**
 * Default parameters indexed by encryption algorithm
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2162742295696737000
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
public class EncryptionConfiguration {

    private static Map DEFAULT_PARAMETERS;
    public static final String DEFAULT_ALGORITHM = "DESede"; // Default algorithm ... used for backward compatibility
    public static final String RECOMMENDED_ALGORITHM = "AES_HASH"; // Recommended algorithm
    
    static {
        DEFAULT_PARAMETERS = new HashMap();
        
        // Triple DES
        EncryptionConfiguration tDesParam = new EncryptionConfiguration();
        tDesParam.setKeySize(24);
        tDesParam.setTransformation("DESede/CBC/PKCS5Padding");
        tDesParam.setAlgorithm("DESede");
        tDesParam.setId("DESede");
        tDesParam.setIV(new IvParameterSpec(new byte[] {0, 0, 0, 0, 0, 0, 0, 0}));
        tDesParam.setFullName("Triple DES (Without password hash)");
        tDesParam.setStrongEncryption(false);
        DEFAULT_PARAMETERS.put(tDesParam.getId(), tDesParam);
        
        // Triple DES with hash
        EncryptionConfiguration tDesHashParam = new EncryptionConfiguration();
        tDesHashParam.setKeySize(24);
        tDesHashParam.setTransformation("DESede/CBC/PKCS5Padding");
        tDesHashParam.setAlgorithm("DESede");
        tDesHashParam.setId("DESede_HASH");
        tDesHashParam.setIV(new IvParameterSpec(new byte[] {0, 0, 0, 0, 0, 0, 0, 0}));
        tDesHashParam.setFullName("Triple DES");
        tDesHashParam.setStrongEncryption(true);
        DEFAULT_PARAMETERS.put(tDesHashParam.getId(), tDesHashParam);
        
        // AES
        EncryptionConfiguration AESParam = new EncryptionConfiguration();
        AESParam.setKeySize(16);
        AESParam.setTransformation("AES");
        AESParam.setAlgorithm("AES");
        AESParam.setId("AES");
        AESParam.setIV(null);
        AESParam.setFullName("AES (Without password hash)");
        AESParam.setStrongEncryption(false);
        DEFAULT_PARAMETERS.put(AESParam.getId(), AESParam);
        
        // AES WITH HASH
        EncryptionConfiguration AESHashParam = new EncryptionConfiguration();
        AESHashParam.setKeySize(16);
        AESHashParam.setTransformation("AES");
        AESHashParam.setAlgorithm("AES");
        AESHashParam.setId("AES_HASH");
        AESHashParam.setIV(null);
        AESHashParam.setFullName("AES (Advanced Encryption Standard)");
        AESHashParam.setStrongEncryption(true);
        DEFAULT_PARAMETERS.put(AESHashParam.getId(), AESHashParam);
    }
    
    public static Set getAvailableAlgorithms() {
        return DEFAULT_PARAMETERS.keySet();
    }
    
    // Return the set of algorithms which are tagged with the "strong encryption" marker.
    public static Set getAvailableNonDeprecatedAlgorithms() {
        Iterator algos = DEFAULT_PARAMETERS.keySet().iterator();
        Set ret = new HashSet();
        while (algos.hasNext()) {
            String k = (String)algos.next();
            EncryptionConfiguration alg = getParameters(k);
            if (alg.isStrongEncryption()) {
                ret.add(k);
            }
        }
        
        return ret;
    }
    
    public static EncryptionConfiguration getParameters(String id) {
        return (EncryptionConfiguration)DEFAULT_PARAMETERS.get(id);
    }
    
    private int keySize;
    private String id;
    private String transformation;
    private String algorithm;
    private IvParameterSpec IV;
    private String fullName;
    private boolean strongEncryption;
    
    private EncryptionConfiguration() {
        super();
    }

    public int getKeySize() {
        return keySize;
    }

    public String getId() {
        return id;
    }
    
    private void setId(String id) {
        this.id = id;
    }
    
    private void setKeySize(int keySize) {
        this.keySize = keySize;
    }
    
    public String getTransformation() {
        return transformation;
    }
    
    private void setTransformation(String transformation) {
        this.transformation = transformation;
    }
    
    public String getAlgorithm() {
        return algorithm;
    }
    
    private void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    
    public IvParameterSpec getIV() {
        return IV;
    }
    
    private void setIV(IvParameterSpec iv) {
        IV = iv;
    }
    
    public String getFullName() {
        return fullName;
    }

    public boolean isStrongEncryption() {
        return strongEncryption;
    }
    
    private void setStrongEncryption(boolean hashPassword) {
        this.strongEncryption = hashPassword;
    }
    
    private void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, this.getAlgorithm());
        h = HashHelper.hash(h, this.isStrongEncryption());
        return h;
    }
    
    public String toString() {
        return this.getFullName();
    }
    
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (! (obj instanceof EncryptionConfiguration)) {
            return false;
        } else {
            EncryptionConfiguration other = (EncryptionConfiguration)obj;
            return 
            	EqualsHelper.equals(this.getAlgorithm(), other.getAlgorithm()) &&
            	EqualsHelper.equals(this.isStrongEncryption(), other.isStrongEncryption())            	
            ;
        }
    }
}