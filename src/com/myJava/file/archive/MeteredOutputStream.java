package com.myJava.file.archive;

import java.io.IOException;
import java.io.OutputStream;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -1628055869823963574
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
public class MeteredOutputStream extends OutputStream {

    private OutputStream out;
    private long size = 0;
    private long maxSize = -1;
    private String errorMessage;
    
    /**
     * @param out
     */
    public MeteredOutputStream(OutputStream out) {
        this.out = out;
    }
    
    public void setMaxSize(long maxSize, String errorMessage) {
        this.maxSize = maxSize;
        
        if (errorMessage != null) {
            this.errorMessage = errorMessage;
        } else {
            this.errorMessage = "Error : Can't write more data than " + (long)(maxSize/1024) + " kbytes.";
        }
    }
    
    public void close() throws IOException {
        out.close();
    }
    
    public void flush() throws IOException {
        out.flush();
    }
    
    public void write(byte[] b, int off, int len) throws IOException {
        if (maxSize > 0 && (len+size) > maxSize) {
            throw new IllegalArgumentException(errorMessage);
        }
        out.write(b, off, len);
        size += len;
    }
    
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }
    
    public void write(int b) throws IOException {
        out.write(new byte[] {(byte)b});
    }
}
