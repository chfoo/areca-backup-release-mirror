package com.myJava.file.driver;

import java.io.File;
import java.io.IOException;

import com.myJava.object.ToStringHelper;


/**
 * Implements a decorator pattern
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

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
public abstract class AbstractLinkableFileSystemDriver
extends AbstractFileSystemDriver 
implements LinkableFileSystemDriver {

    protected FileSystemDriver predecessor;
    
    public FileSystemDriver getPredecessor() {
        return predecessor;
    }
    
    public void setPredecessor(FileSystemDriver predecessor) {
        this.predecessor = predecessor;
    }

    public boolean supportsLongFileNames() {
        return predecessor.supportsLongFileNames();
    }

    public void flush() throws IOException {
        predecessor.flush();
    }
    
    public void mount() throws IOException {
        predecessor.mount();
    }

    public void unmount() throws IOException {
        predecessor.unmount();
    }
    
    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("Predecessor", this.predecessor, sb);
        return ToStringHelper.close(sb);
    }
    
    public short getAccessEfficiency() {
        return predecessor.getAccessEfficiency();
    }
    
    public File getAbsoluteFile(File file) {
        return this.predecessor.getAbsoluteFile(file);
    }

	public String getAbsolutePath(File file) {
        return this.predecessor.getAbsolutePath(file);
    }
    
    public File getCanonicalFile(File file) throws IOException {
        return this.predecessor.getCanonicalFile(file);
    }
    
    public String getCanonicalPath(File file) throws IOException {
        return this.predecessor.getCanonicalPath(file);
    }
    
    public String getName(File file) {
        return this.predecessor.getName(file);
    }
    
    public String getParent(File file) {
        return this.predecessor.getParent(file);
    }
    
    public File getParentFile(File file) {
        return this.predecessor.getParentFile(file);
    }
    
    public String getPath(File file) {
        return this.predecessor.getPath(file);
    }
}

