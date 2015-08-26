package com.myJava.file.archive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

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
public interface ArchiveAdapter {

    public static short ACCESS_UNDEF = -1;
    public static short ACCESS_WRITE = 0;
    public static short ACCESS_READ = 1;
    
    /**
     * Closes the archive
     */
    public void close() throws IOException;
    
    /**
     * Adds a new Entry
     */
    public void addEntry(String entryName, long size) throws IOException;
    
    /**
     * Returns the outputStream that can be used to write data 
     */
    public OutputStream getArchiveOutputStream();
    
    /**
     * Returns the inputStream that can be used to read data 
     */
    public InputStream getArchiveInputStream();
    
    /**
     * Iterates on the InputStream and returns the next entry.
     */
    public String getNextEntry() throws IOException;
    
    /**
     * Closes the current entry 
     */
    public void closeEntry() throws IOException;
    
    /**
     *Sets the archive comment
     */
    public void setArchiveComment(String comment);
    
    /**
     * Sets the charset used to encode filenames
     */
    public void setCharset(Charset charset);
}
