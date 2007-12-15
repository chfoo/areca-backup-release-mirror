package com.myJava.encryption;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;

import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 3675112183502703626
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
public class EncryptionUtil {
    
    public static boolean equals(Cipher c1, Cipher c2) {
        if (c1 == null && c2 == null) {
            return true;
        } else if (c1 == null || c2 == null) {
            return false;
        } else {
            return ( 
                    EqualsHelper.equals(c1.getAlgorithm(), c2.getAlgorithm())
                    && EqualsHelper.equals(c1.getBlockSize(), c2.getBlockSize())
            );
        }
    }
    
    public static int hash(Cipher c) {
        int h = HashHelper.initHash(c);
        
        if (c != null) {
            h = HashHelper.hash(h, c.getAlgorithm());
            h = HashHelper.hash(h, c.getIV());
            h = HashHelper.hash(h, c.getBlockSize());
        }
        
        return h;
    }
    
    /**
     * Normalizes the key so it can by used by the encryption algorithm.
     * <BR>- It is platform independant (while the former key creation algorithm depends on the platform's encoding)
     * <BR>- It generates stronger keys (the password can have any size : it is hashed using MD5 and (if needed) SHA to produce a byte array)
     * <BR>The key size is expressed in bytes
     */
    public static byte[] getNormalizedEncryptionKey(String encryptionSeed, int keySize) {
        String referenceEncoding = "UTF-8"; // The bytes are extracted using UTF-8 character encoding.
        try {               
            // Extract the bytes from the encryption seed
            byte[] originalBytes = encryptionSeed.getBytes(referenceEncoding);
            byte[] result = new byte[keySize];
            
            // Create a 128 bit key
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(originalBytes);
            byte[] md5Result = md.digest();
            
            for (int i=0; i<result.length && i<md5Result.length; i++) {
                result[i] = md5Result[i];
            }
            
            // if the requested key size is greater than 128 bits, create another hash using SHA -> 160 bits
            if (md5Result.length < keySize) {
                MessageDigest mdSha = MessageDigest.getInstance("SHA");
                mdSha.update(originalBytes);
                byte[] shaResult = mdSha.digest();
                
                // If still not enough, return an error
                if (shaResult.length + md5Result.length < keySize) {
                    throw new IllegalArgumentException("Invalid key size : expected " + keySize + " and got " + (shaResult.length + md5Result.length));
                }
                
                // Append the result.
                for (int i=0; (i + md5Result.length)<result.length && i<shaResult.length; i++) {
                    result[i + md5Result.length] = shaResult[i];
                }
            }
            
            return result;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("No such hash function : MD5");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Unsupported encoding : " + referenceEncoding);                
        }
    }
}