package com.myJava.file.driver;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.myJava.file.OutputStreamListener;
import com.myJava.file.metadata.FileMetaData;
import com.myJava.util.taskmonitor.TaskCancelledException;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
 * This interface defines a file access layer.
 * <BR>It mimics the File class methods.
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
public interface FileSystemDriver {
    
    public static short ACCESS_EFFICIENCY_GOOD = 30;
    public static short ACCESS_EFFICIENCY_AVERAGE = 20;
    public static short ACCESS_EFFICIENCY_POOR = 10;
    
    /*
     * Unsupported <code>File</code> methods :
     * - public URI toURI()
     * - public URL toURL() 
     * - public int compareTo(File pathname)
     * - public int compareTo(Object o)
     * - public static File createTempFile(String prefix, String suffix)
     * - public static File createTempFile(String prefix, String suffix, File directory)
     * - public static File[] listRoots();
     */   
    
    /*
     * <code>File</code> class mimic.
     */
    public boolean canRead(File file);
    public boolean canWrite(File file);
    public boolean createNewFile(File file) throws IOException;
    public boolean delete(File file);
    public void forceDelete(File file, TaskMonitor monitor) throws IOException, TaskCancelledException;
    public boolean exists(File file);
    public File getAbsoluteFile(File file);
    public String getAbsolutePath(File file);
    public File getCanonicalFile(File file) throws IOException;
    public String getCanonicalPath(File file) throws IOException;
    public String getName(File file);
    public String getParent(File file);
    public File getParentFile(File file);
    public String getPath(File file);
    public boolean isAbsolute(File file);
    public boolean isDirectory(File file);
    public boolean isFile(File file);
    public boolean isHidden(File file);
    public long lastModified(File file);
    public long length(File file);
    public String[] list(File file);
    public String[] list(File file, FilenameFilter filter);
    public File[] listFiles(File file);
    public File[] listFiles(File file, FileFilter filter);
    public File[] listFiles(File file, FilenameFilter filter);
    public boolean mkdir(File file);
    public boolean mkdirs(File file);
    public boolean renameTo(File source, File dest);
    public boolean setLastModified(File file, long time);
    public boolean setReadOnly(File file);
    public void deleteOnExit(File f);
    

    public InputStream getFileInputStream(File file) throws IOException;
    public OutputStream getFileOutputStream(File file) throws IOException;
    public OutputStream getFileOutputStream(File file, boolean append, OutputStreamListener listener) throws IOException;
    public OutputStream getFileOutputStream(File file, boolean append) throws IOException;
    
    /**
     * Returns an OutputStream for the given File
     * <BR>This outputStream will be cached, and written only during call to "flush" 
     */
    public OutputStream getCachedFileOutputStream(File file) throws IOException;
    
    /**
     * Ensures that a local copy of the file exists and return an InputStream on this file
     */
    public InputStream getCachedFileInputStream(File file) throws IOException;
    
    /**
     * Returns the file's attributes
     */
    public FileMetaData getMetaData(File f, boolean onlyBasicAttributes) throws IOException;
    
    /**
     * Create a Symbolic link
     */
    public boolean createSymbolicLink(File symlink, String realPath) throws IOException;
    
    /**
     * Return the type of the file
     * <BR>See types listed in FileMetaDataAccessor 
     */
    public short getType(File file) throws IOException;
    
    /**
     * Create a named pipe
     */
    public boolean createNamedPipe(File pipe) throws IOException;
    
    /**
     * Applies the attributes provided as argument 
     */
    public void applyMetaData(FileMetaData p, File f) throws IOException;
    
    public boolean supportsLongFileNames();
    
    public void flush() throws IOException;
    
    public void mount() throws IOException;
    public void unmount() throws IOException;
    
    /**
     * Tells whether accesses are fast or not
     */
    public short getAccessEfficiency();
    
    /**
     * Returns a global set of file informations (length, lastmodified, ...)
     */
    public FileCacheableInformations getInformations(File file);
    
    /**
     * Clears cached file informations, if applicable.
     */
    public void clearCachedData(File file) throws IOException;
    
    /**
     * Returns the real path used to store the file (ie the path that can be used to access the file directly).
     * <BR>This method may return a null value if irrelevant.
     */
    public String getPhysicalPath(File file);
}