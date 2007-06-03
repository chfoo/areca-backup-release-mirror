package com.application.areca.impl;

import java.io.File;

import com.application.areca.ApplicationException;
import com.myJava.file.FileSystemManager;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2162742295696737000
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
public class FileSystemLevel {
    private File[] levelFiles;
    private int index;
    
    public FileSystemLevel(File baseDirectory) throws ApplicationException {
        if (! FileSystemManager.exists(baseDirectory)) {
            // On laisse volontairement se déclencher les NullPointerExceptions
            throw new ApplicationException("The requested directory (" + FileSystemManager.getAbsolutePath(baseDirectory) + ") doesn't exist.");
        }
        
        if (FileSystemManager.isDirectory(baseDirectory)) {
            this.levelFiles = FileSystemManager.listFiles(baseDirectory);
            if (this.levelFiles == null) {
                this.levelFiles = new File[0];
            }
        } else {
            this.levelFiles = new File[] {baseDirectory};
        }
        this.index = 0;
    }
    
    public boolean hasMoreElements() {
        return (this.index <= this.levelFiles.length-1);
    }
    
    public File nextElement() {
        this.index++;
        return this.levelFiles[index-1];
    }
    
    public double getProgress() {
        return (double)index / (double)(this.levelFiles.length == 0 ? 1 : this.levelFiles.length);
    }
    
    public int getSize() {
        if (this.levelFiles == null) {
            return 0;
        } else {
            return this.levelFiles.length;
        }
    }
}
