package com.myJava.file.delta.tools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.myJava.util.log.Logger;



/**
 * Implements a buffered FIFO list of bytes of fixed length
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2011, Olivier PETRUCCI.

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
public class LinkedList {

    private boolean eof = false;
    private int maxSize;
    private byte[] buffer;
    private int currentIndex = 0;
    private int firstIndex = 0;

    public LinkedList(int maxSize) {
        this.maxSize = maxSize;
        this.buffer = new byte[maxSize];
    }

    public void add(byte data) {       
    	buffer[currentIndex] = data;

    	currentIndex++;
    	if (currentIndex == maxSize) {
    		currentIndex = 0;
    		firstIndex = 0;
    		eof = true;
    	} else if (eof) {
    		firstIndex = currentIndex;
    	}
    }
    
    public int getFirst() {
    	return buffer[firstIndex];
    }
    
    public int computeQuickHash() {
        if (eof) {
            int hash = HashTool.hash(0, buffer, firstIndex, maxSize - firstIndex);
            return HashTool.hash(hash, buffer, 0, firstIndex);
        } else {
        	return HashTool.hash(0, buffer, 0, currentIndex);
        }
    }

    public byte[] computeHash(String algorithm) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(algorithm);
            if (eof) {
                digest.update(buffer, firstIndex, maxSize - firstIndex);
                digest.update(buffer, 0, firstIndex);
            } else {
                digest.update(buffer, 0, currentIndex);
            }

            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            Logger.defaultLogger().error(e);
            return null;
        }
    }

    public String toString() {
    	int s;
    	
    	if (eof) {
    		s = maxSize;
    	} else {
    		s = currentIndex;
    	}
    	
        byte[] dt = new byte[s];
        for (int i=0; i<s; i++) {
        	dt[i] = buffer[(firstIndex + i)%maxSize];
        }
        
        String ret = "";
        for (int i=0; i<dt.length; i++) {
            ret += " " + (int)dt[i];
        }
        return ret;
    }
    
    public static void main(String[] args) {
        int maxSize = 23;
        int maxValue = 200;
        
        LinkedList lst = new LinkedList(maxSize);
        for (int i=1; i<maxValue; i++) {
            lst.add((byte)i);
            System.out.println(lst.toString());
            System.out.println(lst.getFirst());
        }
    }
}
