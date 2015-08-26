package com.myJava.file.archive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


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
public abstract class AbstractArchiveAdapter implements ArchiveAdapter {
    protected short accessMode = ACCESS_UNDEF;
    protected long entryCount = 0;
    protected InputStream zin = null;
    protected OutputStream zout = null;
    
    /**
     * Constructor.
     * <BR>The case where "estimatedLength" == 0 must be managed carefully by subclasses.
     */
    public AbstractArchiveAdapter(short accessMode, long estimatedLength) {
        this.accessMode = accessMode;
    }
    
    protected boolean isWriter() {
        return this.accessMode == ACCESS_WRITE;
    }
    
    protected boolean isReader() {
        return this.accessMode == ACCESS_READ;
    }
    
    public void addEntry(String entryName, long size) throws IOException {       
        entryCount++;
    }
    
    public void close() throws IOException {
        if (zin != null) {
            zin.close();
        }
        if (zout != null) {          
            zout.close();
        }
    }
    
    public InputStream getArchiveInputStream() {
        return zin;
    }
    
    public OutputStream getArchiveOutputStream() {
        return zout;
    }
}
