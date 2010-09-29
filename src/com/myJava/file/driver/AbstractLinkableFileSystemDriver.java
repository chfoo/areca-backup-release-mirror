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
 Copyright 2005-2010, Olivier PETRUCCI.

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
    
    public boolean exists(File file) {
        return predecessor.exists(file);
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
    
    public boolean isDirectory(File file) {
    	return this.predecessor.isDirectory(file);
    }
    
    public boolean isFile(File file) {
    	return this.predecessor.isFile(file);
    }
    
    public boolean mkdir(File file) {
    	return this.predecessor.mkdir(file);
    }
    
    public boolean mkdirs(File file) {
    	return this.predecessor.mkdirs(file);
    }
    
    public boolean renameTo(File source, File dest) {
    	return this.predecessor.renameTo(source, dest);
    }
    
    public boolean setLastModified(File file, long time) {
    	return this.predecessor.setLastModified(file, time);
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
    

	public boolean canRead(File file) {
		return predecessor.canRead(file);
	}

	public boolean canWrite(File file) {
		return predecessor.canWrite(file);
	}
	

	public FileCacheableInformations getInformations(File file) {
		return predecessor.getInformations(file);
	}

	public short getType(File file) throws IOException {
		return predecessor.getType(file);
	}

	public void clearCachedData(File file) throws IOException {
		predecessor.clearCachedData(file);
	}
}

