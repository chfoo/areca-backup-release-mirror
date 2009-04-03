package com.myJava.file.delta.tools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.myJava.util.log.Logger;


/**
 * Implements a buffered FIFO list of bytes of fixed length
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 7299034069467778562
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

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
    private LinkedListElement root;
    private LinkedListElement current;
    private long size = 0;
    private long maxSize;
    private int bufferSize;

    public LinkedList(long maxSize, int bufferSize) {
        this.maxSize = maxSize;
        this.bufferSize = bufferSize;
    }

    public void add(byte data) {       
        if (root == null) {
            root = new LinkedListElement(bufferSize);
            root.add(data);
            current = root;
        } else {
            if (size == maxSize) {
                if ( ! root.canRemove()) {
                    root = root.getNext();
                }
                root.remove();
                size--;
            }
        	
            if (! current.canAdd()) {
                current.setNext(new LinkedListElement(bufferSize));
                current = current.getNext();
            }
            current.add(data);
        }
        
        size++;
    }
    
    public byte[] getBytes() {
        int currentIndex = 0;
        byte[] ret = new byte[(int)size];
        LinkedListElement elt = root;
        while (elt != null) {
            byte[] dt = elt.getBytes();
            for (int i=0; i<dt.length; i++) {
                ret[currentIndex++] = dt[i];
            }
            elt = elt.getNext();
        }
        return ret;
    }
    
    public int getFirst() {
        if (root == null) {
            return -1;
        } else {
            return root.getFirst();
        }
    }

    public byte[] computeHash(String algorithm) {
        LinkedListElement elt = root;
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(algorithm);
            while (elt != null) {
                elt.digest(digest);
                elt = elt.getNext();
            }
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            Logger.defaultLogger().error(e);
            return null;
        }
    }

    public String toString() {
        String ret = "";
        byte[] dt = getBytes();
        for (int i=0; i<dt.length; i++) {
            ret += (char)dt[i];
        }
        return ret;
    }
    
    public static void main(String[] args) {
        int maxSize = 23;
        int bufferSize = 50;
        int maxValue = 200;
        
        LinkedList lst = new LinkedList(maxSize, bufferSize);
        for (int i=0; i<maxValue; i++) {
            lst.add((byte)i);
            System.out.println(lst.toString());
            System.out.println(lst.computeHash("SHA").length);
        }
    }
}
