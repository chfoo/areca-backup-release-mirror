package com.myJava.file.delta.tools;

import java.security.MessageDigest;


/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2736893395693886205
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
public class LinkedListElement {
    private LinkedListElement next;
    private byte[] data;
    private int addIndex = -1;
    private int remIndex = -1;
    
    public LinkedListElement(int bufferSize) {
        this.data = new byte[bufferSize];
    }
    
    public LinkedListElement getNext() {
        return next;
    }
    
    public void setNext(LinkedListElement next) {
        this.next = next;
    }
    
    public boolean canAdd() {
        return addIndex < data.length - 1;
    }
    
    public void add(byte b) {
        data[++addIndex] = b;
    }
    
    public boolean canRemove() {
        return remIndex < data.length - 1 && remIndex < addIndex;
    }
    
    public byte remove() {
        return data[++remIndex];
    }
    
    public int availableData() {
        return addIndex - remIndex;
    }
    
    public void digest(MessageDigest digest) {
        digest.update(getBytes());
    }
    
    public int getFirst() {
        if (addIndex - remIndex != 0) {
            return data[remIndex + 1];
        } else {
            if (next == null) {
                return -1;
            } else {
                return next.getFirst();
            }
        }
    }
    
    public byte[] getBytes() {
        int nb = addIndex - remIndex;
        byte[] ret = new byte[nb];
        for (int i=0; i<nb; i++) {
            ret[i] = data[i + remIndex + 1];
        }
        return ret;
    }
}
