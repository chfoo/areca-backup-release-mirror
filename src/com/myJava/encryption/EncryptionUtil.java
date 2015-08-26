package com.myJava.encryption;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
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
public class EncryptionUtil {
	private static final String RND_ALGORITHM = "SHA1PRNG";
	
	/**
	 * Nr of iterations used during encyption key generation phase.
	 */
	private static final int KEYGEN_ITERATION_COUNT = FrameworkConfiguration.getInstance().getEncryptionKGIters();
	
	/**
	 * Salt added during encryption key generation phase.
	 * <BR>This salt is kept constant because we want the key derivation process to be deterministic
	 * and ONLY based on the password entered by the user (necessary if the target's configuration is
	 * lost : we must be able to reconstruct the encryption key with no other data than the password.
	 * <BR>
	 * <BR>So this 'salt' is quite weak (constant) ... it's only here to force potential attackers to 
	 * build their own hash table.
	 * <BR>
	 * <BR>For a safer encryption key, just use Areca's "raw" key convention, which allow you to simply
	 * enter the encryption key (as an hexadecimal sequence)
	 */
	private static final String KEYGEN_SALT = FrameworkConfiguration.getInstance().getEncryptionKGSalt();
    private static final String KEY_ALG = FrameworkConfiguration.getInstance().getEncryptionKGAlg();
    private static final String REF_ENC = FrameworkConfiguration.getInstance().getEncryptionKGSaltEncoding();
	
    private static final int MAX_SEEDS = 20;
    private static int SEED_WATERMARK = 0;
    private static final long[] SEEDS = new long[MAX_SEEDS];
    private static int CURRENT_SEED_INDEX = -1;
    
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
    
    public static Key buildKeyFromRawInput(String rawKey, String alg) {
    	return new SecretKeySpec(Util.base16Decode(rawKey), alg);
    }
    
    public static Key buildKeyFromPassphrase(String encryptionSeed, int keySize, String alg) {
        try {
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALG);
			byte[] salt = KEYGEN_SALT.getBytes(REF_ENC);
			PBEKeySpec pbKeySpec = new PBEKeySpec(encryptionSeed.toCharArray(), salt, KEYGEN_ITERATION_COUNT, keySize*8);
			SecretKey pbKey = keyFactory.generateSecret(pbKeySpec);
			byte[] b = pbKey.getEncoded();
			
	    	return new SecretKeySpec(b, alg);
		} catch (NoSuchAlgorithmException e) {
			Logger.defaultLogger().error(e);
			throw new IllegalArgumentException("Unsupported key derivation algorithm : " + KEY_ALG);
		} catch (UnsupportedEncodingException e) {
			Logger.defaultLogger().error(e);
			throw new IllegalArgumentException("Unsupported encoding : " + REF_ENC);
		} catch (InvalidKeySpecException e) {
			Logger.defaultLogger().error(e);
			throw new IllegalArgumentException("Unsupported parameters", e);
		}
    }
    
    /**
     * Register a new seed with a probability of 5%
     */
    public static synchronized void registerRandomSeed() {
    	if (Math.random() < 0.05) {
        	CURRENT_SEED_INDEX++;
        	SEED_WATERMARK = Math.min(++SEED_WATERMARK, MAX_SEEDS);
        	SEEDS[CURRENT_SEED_INDEX % MAX_SEEDS] = System.currentTimeMillis();
    	}
    }

    public static synchronized byte[] generateRandomKey(int keySize) {
    	try {
    		Logger.defaultLogger().info("Generating new random key. Algorithm = " + RND_ALGORITHM + ". Seed count = " + (SEED_WATERMARK + 1) + " ...");
			SecureRandom sr = SecureRandom.getInstance(RND_ALGORITHM);
			sr.setSeed(System.currentTimeMillis());
			for (int i=0; i<SEED_WATERMARK; i++) {
				sr.setSeed(SEEDS[i]);
			}
			byte[] b = new byte[keySize];
			sr.nextBytes(b);
			return b;
		} catch (NoSuchAlgorithmException e) {
			Logger.defaultLogger().error("An error was detected while generating encryption key.", e);
			throw new IllegalArgumentException(e);
		}
    }
}