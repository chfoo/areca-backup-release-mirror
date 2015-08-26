package com.myJava.file.driver;

import java.io.File;


/**
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
public class FileCacheableInformations {
    private FileSystemDriver driver;
    private File file;
    
    private long length;
    private long lastModified;
    private boolean exists;
    private boolean readable;
    private boolean writable;
    private boolean directory;
    private boolean hidden;
    
    private boolean lengthSet;
    private boolean lastModifiedSet;
    private boolean existsSet;
    private boolean readableSet;
    private boolean writableSet;
    private boolean directorySet;
    private boolean hiddenSet;
    

    public FileCacheableInformations(
            long length, 
            long lastModified, 
            boolean exists, 
            boolean readable, 
            boolean writable, 
            boolean directory, 
            boolean hidden) {

        this.length = length;
        this.lastModified = lastModified;
        this.exists = exists;
        this.readable = readable;
        this.writable = writable;
        this.directory = directory;
        this.hidden = hidden;
        
        this.lengthSet = true;
        this.lastModifiedSet = true;
        this.existsSet = true;
        this.readableSet = true;
        this.writableSet = true;
        this.directorySet = true;
        this.hiddenSet = true;
    }
    
    public FileCacheableInformations(FileSystemDriver driver, File f) {
        this.driver = driver;
        this.file = f;
    }

    public boolean isDirectory() {
        if (! directorySet) {
            directorySet = true;
            this.directory = driver.isDirectory(file);
        }
        
        return directory;
    }

    public boolean isExists() {
        if (! existsSet) {
            existsSet = true;
            this.exists = driver.exists(file);
        }
        
        return exists;
    }

    public boolean isHidden() {
        if (! hiddenSet) {
            hiddenSet = true;
            this.hidden = driver.isHidden(file);
        }
        
        return hidden;
    }

    public long getLastModified() {
        if (! lastModifiedSet) {
            lastModifiedSet = true;
            this.lastModified = driver.lastModified(file);
        }
        
        return lastModified;
    }

    public long getLength() {
        if (! lengthSet) {
            lengthSet = true;
            this.length = driver.length(file);
        }
        
        return length;
    }

    public boolean isReadable() {
        if (! readableSet) {
            readableSet = true;
            this.readable = driver.canRead(file);
        }
        
        return readable;
    }

    public boolean isWritable() {
        if (! writableSet) {
            writableSet = true;
            this.writable = driver.canWrite(file);
        }
        
        return writable;
    }

    public boolean isDirectorySet() {
        return directorySet;
    }

    public boolean isExistsSet() {
        return existsSet;
    }

    public boolean isLastModifiedSet() {
        return lastModifiedSet;
    }

    public boolean isLengthSet() {
        return lengthSet;
    }

    public boolean isReadableSet() {
        return readableSet;
    }

    public boolean isWritableSet() {
        return writableSet;
    }
    
    public void enforceLength(long length) {
        lengthSet = true;
        this.length = length;
    }
}
