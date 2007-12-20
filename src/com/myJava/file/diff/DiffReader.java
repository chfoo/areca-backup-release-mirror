package com.myJava.file.diff;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

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
public class DiffReader {
    private int blockSize;
    private ArrayList hash;
    private InputStream in;
    private DiffProcessor processor;


    public DiffReader(int blockSize, ArrayList hash, InputStream in, DiffProcessor processor) {
        this.hash = hash;
        this.in = in;
        this.processor = processor;
        this.blockSize = blockSize;
    }
    
    public void read() throws IOException {
        LinkedList currentBlock = new LinkedList(blockSize);
        
    }
}
