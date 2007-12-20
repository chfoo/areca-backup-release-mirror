package com.myJava.file.diff;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4331497872542711431
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
public class LinkedList {
    private LinkedListElement root;
    private LinkedListElement current;
    private int size = 0;
    private int maxSize;

    public LinkedList(int maxSize) {
        this.maxSize = maxSize;
    }

    public void add(byte data) {
        if (size == maxSize) {
            this.root = this.root.getNext();
            size--;
        }
        
        if (root == null) {
            root = new LinkedListElement();
            root.setData(data);
        } else {
            current.setNext(new LinkedListElement());
            current.getNext().setData(data);
            this.current = this.current.getNext();
        }
    }
}
