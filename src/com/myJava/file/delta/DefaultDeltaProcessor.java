package com.myJava.file.delta;

import com.myJava.file.delta.sequence.HashSequenceEntry;
import com.myJava.file.delta.tools.CircularList;

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
public class DefaultDeltaProcessor implements DeltaProcessor {

    private String dt = "";
    private long from = -1;
    private long to = -1;
    private long totalNew = 0;
    
    private void flush() {
        if (from != -1) {
            System.out.println("New bytes : [" + dt + "] from " + from + " to " + to);
            from = -1;
            to = -1;
            dt = "";
        }
    }
    
    public void blockFound(HashSequenceEntry entry, CircularList block) {
        flush();
        System.out.println("Block found : " + entry.getIndex() + " - [" + block.toString().substring(0, entry.getSize()) + "]");
    }

    public void blockFound(long readFrom, long readTo) throws DeltaProcessorException {
    }

    public void newBytes(byte[] data, int offset, int len) throws DeltaProcessorException {
    }

    public void newByte(byte data) {
        if (from == -1) {
            from = 0;
        }
        to = 0;
        dt += (char)data;
        totalNew++;
    }
    
    public void bytesLost(long from, long to) {
        flush();
        System.out.println("Bytes lost : " + from + " to " + to);
    }

    public void begin() {
        System.out.println("Begin");
    }

    public void end() {
        flush();
        System.out.println("End");
        System.out.println("" + totalNew + " new bytes.");
    }
}
