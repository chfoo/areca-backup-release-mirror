package com.application.areca.impl.tools;

import java.io.File;

import com.application.areca.ApplicationException;
import com.myJava.file.FileSystemManager;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 8290826359148479344
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
    private File baseDirectory;
    private boolean haveFilesBeenStored = false;
    private boolean hasBeenSent = false;
    private FileSystemLevel parent;
    
    public FileSystemLevel(File baseDirectory, FileSystemLevel parent) throws ApplicationException {
        if (! FileSystemManager.exists(baseDirectory)) {
            // On laisse volontairement se déclencher les NullPointerExceptions
            throw new ApplicationException("The requested directory (" + FileSystemManager.getAbsolutePath(baseDirectory) + ") doesn't exist.");
        }
        
        this.parent = parent;
        this.baseDirectory = baseDirectory;
        
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

    public FileSystemLevel getParent() {
        return parent;
    }

    public boolean hasMoreElements() {
        return (this.index <= this.levelFiles.length-1);
    }

    public File getBaseDirectory() {
        return baseDirectory;
    }

    public File nextElement() {
        this.index++;
        return this.levelFiles[index-1];
    }
    
    public double getProgress() {
        return (double)index / (double)(this.levelFiles.length == 0 ? 1 : this.levelFiles.length);
    }

    public boolean isHasBeenSent() {
        return hasBeenSent;
    }

    public void setHasBeenSent(boolean hasBeenSent) {
        this.hasBeenSent = hasBeenSent;
    }

    public boolean isHaveFilesBeenStored() {
        return haveFilesBeenStored;
    }

    public void setHaveFilesBeenStored(boolean haveFilesBeenStored) {
        this.haveFilesBeenStored = haveFilesBeenStored;
    }

    public int getSize() {
        if (this.levelFiles == null) {
            return 0;
        } else {
            return this.levelFiles.length;
        }
    }
}
