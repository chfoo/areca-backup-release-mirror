package com.myJava.file;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.myJava.file.attributes.Attributes;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.object.ToStringHelper;

/**
 * Driver "chainable" apportant des fonctionnalités de compression.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 3732974506771028333
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
public class CompressedFileSystemDriver 
extends AbstractLinkableFileSystemDriver {

    /**
     * @param directoryRoot
     * @param key
     */
    public CompressedFileSystemDriver(FileSystemDriver predecessor) {
        super();
        this.setPredecessor(predecessor);
    }
    
    public boolean canRead(File file) {
        return this.predecessor.canRead(file);
    }
    
    public boolean canWrite(File file) {
        return this.predecessor.canWrite(file);
    }
    
    public boolean createNewFile(File file) throws IOException {
        return this.predecessor.createNewFile(file);
    }
    
    public boolean delete(File file) {
        return this.predecessor.delete(file);
    }
    
    public boolean exists(File file) {
        return this.predecessor.exists(file);
    }
    
    public File getAbsoluteFile(File file) {
        return this.predecessor.getAbsoluteFile(file);
    }

    public FileInformations getInformations(File file) {
        return this.predecessor.getInformations(file);
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
    
    public boolean isAbsolute(File file) {
        return this.predecessor.isAbsolute(file);
    }
    
    public boolean isDirectory(File file) {
        return this.predecessor.isDirectory(file);
    }
    
    public boolean isFile(File file) {
        return this.predecessor.isFile(file);
    }
    
    public boolean createSymbolicLink(File symlink, String realPath) throws IOException {
        return this.predecessor.createSymbolicLink(symlink, realPath);
    }

    public boolean isHidden(File file) {
        return this.predecessor.isHidden(file);
    }
    
    public long lastModified(File file) {
        return this.predecessor.lastModified(file);
    }
    
    public long length(File file) {
        return this.predecessor.length(file);
    }
    
    public String[] list(File file, FilenameFilter filter) {
        return predecessor.list(file, filter);
    }
    
    public String[] list(File file) {
        return predecessor.list(file);
    }
    
    public File[] listFiles(File file, FileFilter filter) {
        return predecessor.listFiles(file, filter);
    }
    
    public File[] listFiles(File file, FilenameFilter filter) {
        return predecessor.listFiles(file, filter);
    }
    
    public File[] listFiles(File file) {
        return predecessor.listFiles(file);
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
    
    public boolean setReadOnly(File file) {
        return this.predecessor.setReadOnly(file);
    }
    
    public InputStream getFileInputStream(File file) throws IOException {
        return new GZIPInputStream(predecessor.getFileInputStream(file));
    }
    
    public OutputStream getCachedFileOutputStream(File file) throws IOException {
        return new GZIPOutputStream(predecessor.getCachedFileOutputStream(file));
    }    
    
    public OutputStream getFileOutputStream(File file) throws IOException {
        return new GZIPOutputStream(predecessor.getFileOutputStream(file));
    }    
    
    public OutputStream getFileOutputStream(File file, boolean append) throws IOException {
        if (append) {
            throw new IllegalArgumentException("Cannot open an OutputStream in 'append' mode on a compressed FileSystem");
        }
        return getFileOutputStream(file);
    }   

    public void deleteOnExit(File f) {
        predecessor.deleteOnExit(f);
    }
    
    public Attributes getAttributes(File f) throws IOException {
        return this.predecessor.getAttributes(f);
    }

    public void applyAttributes(Attributes p, File f) throws IOException {
        this.predecessor.applyAttributes(p, f);
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, this.predecessor);
        
        return h;
    }
    
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o instanceof CompressedFileSystemDriver) {
            CompressedFileSystemDriver other = (CompressedFileSystemDriver)o;
            
            return (
                    EqualsHelper.equals(other.predecessor, this.predecessor) 
            );
        } else {
            return false;
        }
    }
    
    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("Predecessor", this.predecessor, sb);
        return ToStringHelper.close(sb);
    }

    public boolean directFileAccessSupported() {
        return false;
    }
    
    public boolean isContentSensitive() {
        return true;
    }
}