package com.myJava.file.driver.remote;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;

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
public class LocalInputStream extends InputStream {

    private InputStream in;
    private File file;
    private boolean closed = false;
    
    public LocalInputStream(File file) throws IOException {
        this.file = file;
        this.in = FileSystemManager.getFileInputStream(file);
    }

    public int available() throws IOException {
        return in.available();
    }

    public void close() throws IOException {
    	if (closed) {
    		return;
    	}
    	closed = true;
    	
        try {
            if (in != null) {
                in.close();
            }
        } finally {
            FileTool.getInstance().delete(file);
        }
    }

    public boolean equals(Object obj) {
        return in.equals(obj);
    }

    public int hashCode() {
        return in.hashCode();
    }

    public void mark(int readlimit) {
        in.mark(readlimit);
    }

    public boolean markSupported() {
        return in.markSupported();
    }

    public int read() throws IOException {
        return in.read();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    public int read(byte[] b) throws IOException {
        return in.read(b);
    }

    public void reset() throws IOException {
        in.reset();
    }

    public long skip(long n) throws IOException {
        return in.skip(n);
    }

    public String toString() {
        return in.toString();
    }
}
