package com.myJava.file.driver.event;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.myJava.file.OutputStreamListener;
import com.myJava.file.driver.FileCacheableInformations;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.file.driver.LinkableFileSystemDriver;
import com.myJava.file.driver.AbstractLinkableFileSystemDriver;
import com.myJava.file.metadata.FileMetaData;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.object.ToStringHelper;
import com.myJava.util.taskmonitor.TaskCancelledException;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
 * Event based file system driver
 * <BR>To be used with caution because of potential impact on performances (objects creations)
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
public class EventFileSystemDriver 
extends AbstractLinkableFileSystemDriver
implements LinkableFileSystemDriver {

    private List listeners = new ArrayList();
    private String identifier;
    
    public static FileSystemDriver wrapDriver(FileSystemDriver driver, String identifier, List listeners) {
        if (listeners == null || listeners.isEmpty()) {
            return driver;
        } else {
            EventFileSystemDriver ret = new EventFileSystemDriver(driver, identifier);
            ret.listeners = listeners;
            return ret;
        }
    }
    
    public EventFileSystemDriver(FileSystemDriver predecessor, String identifier) {
        super();
        this.identifier = identifier;
        this.setPredecessor(predecessor);
    }
    
    public EventFileSystemDriver(FileSystemDriver predecessor, String identifier, FileSystemDriverListener listener) {
        this(predecessor, identifier);
        addListener(listener);
    }

    public String getIdentifier() {
        return identifier;
    }

    public void addListener(FileSystemDriverListener listener) {
        this.listeners.add(listener);
    }
    
    protected void throwStartEvent(FileSystemDriverEvent event) {
        throwEvent(event, true);
    }
    
    protected void throwStopEvent(FileSystemDriverEvent event) {
        throwEvent(event, false);
    }
    
    private void throwEvent(FileSystemDriverEvent event, boolean start) {
        if (! start) {
            event.registerStop();
        }
        
        Iterator iter = this.listeners.iterator();
        while (iter.hasNext()) {
            FileSystemDriverListener listener = (FileSystemDriverListener)iter.next();
            if (start) {
                listener.methodStarted(event);
            } else {
                listener.methodEnded(event);
            }
        }
    }
    
    protected FileSystemDriverEvent buildEvent(String event, File f) {
        return new FileSystemDriverEvent(event, f, this);
    }

    public void applyMetaData(FileMetaData p, File f) throws IOException {
        FileSystemDriverEvent event = buildEvent("applyAttributes", f);
        event.setArgument(p);
        throwStartEvent(event);
        predecessor.applyMetaData(p, f);
        throwStopEvent(event);
    }

    public boolean canRead(File file) {
        FileSystemDriverEvent event = buildEvent("canRead", file);
        throwStartEvent(event);
        boolean res = predecessor.canRead(file);
        throwStopEvent(event);
        return res;
    }
    
    public short getType(File file) throws IOException {
        FileSystemDriverEvent event = buildEvent("getType", file);
        throwStartEvent(event);
        short res = predecessor.getType(file);
        throwStopEvent(event);
        return res;
    }

    public boolean canWrite(File file) {
        FileSystemDriverEvent event = buildEvent("canWrite", file);
        throwStartEvent(event);
        boolean res =  predecessor.canWrite(file);
        throwStopEvent(event);
        return res;
    }
    
    
    public String getPhysicalPath(File file) {
        FileSystemDriverEvent event = buildEvent("getPhysicalPath", file);
        throwStartEvent(event);
        String res =  predecessor.getPhysicalPath(file);
        throwStopEvent(event);
        return res;
	}

    public FileCacheableInformations getInformations(File file) {
        FileSystemDriverEvent event = buildEvent("getInformations", file);
        throwStartEvent(event);
        FileCacheableInformations res =  predecessor.getInformations(file);
        throwStopEvent(event);
        return res;
    }

    public boolean createNewFile(File file) throws IOException {
        FileSystemDriverEvent event = buildEvent("createNewFile", file);
        throwStartEvent(event);
        boolean res =  predecessor.createNewFile(file);
        throwStopEvent(event);
        return res;
    }

    public boolean createSymbolicLink(File symlink, String realPath) throws IOException {
        FileSystemDriverEvent event = buildEvent("createSymbolicLink", symlink);
        event.setArgument(realPath);
        throwStartEvent(event);
        boolean res =  predecessor.createSymbolicLink(symlink, realPath);
        throwStopEvent(event);
        return res;
    }
    
    public boolean createNamedPipe(File pipe) throws IOException {
        FileSystemDriverEvent event = buildEvent("createNamedPipe", pipe);
        throwStartEvent(event);
        boolean res =  predecessor.createNamedPipe(pipe);
        throwStopEvent(event);
        return res;
    }

    public void forceDelete(File file, TaskMonitor monitor) 
    throws IOException, TaskCancelledException {
        FileSystemDriverEvent event = buildEvent("forceDelete", file);
        throwStartEvent(event);
        predecessor.forceDelete(file, monitor);
        throwStopEvent(event);
	}

	public boolean delete(File file) {
        FileSystemDriverEvent event = buildEvent("delete", file);
        throwStartEvent(event);
        boolean res =  predecessor.delete(file);
        throwStopEvent(event);
        return res;
    }
    
    

    public void deleteOnExit(File f) {
        FileSystemDriverEvent event = buildEvent("deleteOnExit", f);
        throwStartEvent(event);
        predecessor.deleteOnExit(f);
        throwStopEvent(event);
    }

    public boolean exists(File file) {
        FileSystemDriverEvent event = buildEvent("exists", file);
        throwStartEvent(event);
        boolean res =  predecessor.exists(file);
        throwStopEvent(event);
        return res;
    }

    public void flush() throws IOException {
        predecessor.flush();
    }

    public File getAbsoluteFile(File file) {
        FileSystemDriverEvent event = buildEvent("getAbsoluteFile", file);
        throwStartEvent(event);
        File res = predecessor.getAbsoluteFile(file);
        throwStopEvent(event);
        return res;
    }

    public String getAbsolutePath(File file) {
        FileSystemDriverEvent event = buildEvent("getAbsolutePath", file);
        throwStartEvent(event);
        String res = predecessor.getAbsolutePath(file);
        throwStopEvent(event);
        return res;
    }

    public short getAccessEfficiency() {
        return predecessor.getAccessEfficiency();
    }

    public FileMetaData getMetaData(File f, boolean onlyBasicAttributes) throws IOException {
        FileSystemDriverEvent event = buildEvent("getAttributes", f);
        throwStartEvent(event);
        FileMetaData res = predecessor.getMetaData(f, onlyBasicAttributes);
        throwStopEvent(event);
        return res;
    }

    public OutputStream getCachedFileOutputStream(File file) throws IOException {
        FileSystemDriverEvent event = buildEvent("getCachedFileOutputStream", file);
        throwStartEvent(event);
        OutputStream res = new EventOutputStream(
                predecessor.getCachedFileOutputStream(file),
                file,
                this
        );
        throwStopEvent(event);
        return res;
    }

    public File getCanonicalFile(File file) throws IOException {
        FileSystemDriverEvent event = buildEvent("getCanonicalFile", file);
        throwStartEvent(event);
        File res = predecessor.getCanonicalFile(file);
        throwStopEvent(event);
        return res;
    }

    public String getCanonicalPath(File file) throws IOException {
        FileSystemDriverEvent event = buildEvent("getCanonicalPath", file);
        throwStartEvent(event);
        String res = predecessor.getCanonicalPath(file);
        throwStopEvent(event);
        return res;
    }
    
    public InputStream getCachedFileInputStream(File file) throws IOException {
        FileSystemDriverEvent event = buildEvent("getCachedFileInputStream", file);
        throwStartEvent(event);
        InputStream res = new EventInputStream(
                predecessor.getCachedFileInputStream(file),
                file,
                this
        );
        throwStopEvent(event);
        return res;
    }

    public InputStream getFileInputStream(File file) throws IOException {
        FileSystemDriverEvent event = buildEvent("getFileInputStream", file);
        throwStartEvent(event);
        InputStream res = new EventInputStream(
                predecessor.getFileInputStream(file),
                file,
                this
        );
        throwStopEvent(event);
        return res;
    }

    public OutputStream getFileOutputStream(File file, boolean append, OutputStreamListener listener) throws IOException {
        FileSystemDriverEvent event = buildEvent("getFileOutputStream", file);
        event.setArgument(new Boolean(append));
        throwStartEvent(event);
        OutputStream res = new EventOutputStream(
                predecessor.getFileOutputStream(file, append, listener),
                file,
                this
        );
        throwStopEvent(event);
        return res;
	}

	public OutputStream getFileOutputStream(File file, boolean append) throws IOException {
		return getFileOutputStream(file, append, null);
    }

    public OutputStream getFileOutputStream(File file) throws IOException {
        FileSystemDriverEvent event = buildEvent("getFileOutputStream", file);
        throwStartEvent(event);
        OutputStream res = new EventOutputStream(
                predecessor.getFileOutputStream(file),
                file,
                this
        );
        throwStopEvent(event);
        return res;
    }

    public String getName(File file) {
        FileSystemDriverEvent event = buildEvent("getName", file);
        throwStartEvent(event);
        String res = predecessor.getName(file);
        throwStopEvent(event);
        return res;
    }

    public String getParent(File file) {
        FileSystemDriverEvent event = buildEvent("getParent", file);
        throwStartEvent(event);
        String res = predecessor.getParent(file);
        throwStopEvent(event);
        return res;
    }

    public File getParentFile(File file) {
        FileSystemDriverEvent event = buildEvent("getParentFile", file);
        throwStartEvent(event);
        File res = predecessor.getParentFile(file);
        throwStopEvent(event);
        return res;
    }

    public String getPath(File file) {
        FileSystemDriverEvent event = buildEvent("getPath", file);
        throwStartEvent(event);
        String res = predecessor.getPath(file);
        throwStopEvent(event);
        return res;
    }

    public boolean isAbsolute(File file) {
        FileSystemDriverEvent event = buildEvent("isAbsolute", file);
        throwStartEvent(event);
        boolean res = predecessor.isAbsolute(file);
        throwStopEvent(event);
        return res;
    }

    public boolean isDirectory(File file) {
        FileSystemDriverEvent event = buildEvent("isDirectory", file);
        throwStartEvent(event);
        boolean res = predecessor.isDirectory(file);
        throwStopEvent(event);
        return res;
    }

    public boolean isFile(File file) {
        FileSystemDriverEvent event = buildEvent("isFile", file);
        throwStartEvent(event);
        boolean res = predecessor.isFile(file);
        throwStopEvent(event);
        return res;
    }

    public boolean isHidden(File file) {
        FileSystemDriverEvent event = buildEvent("isHidden", file);
        throwStartEvent(event);
        boolean res = predecessor.isHidden(file);
        throwStopEvent(event);
        return res;
    }

    public long lastModified(File file) {
        FileSystemDriverEvent event = buildEvent("lastModified", file);
        throwStartEvent(event);
        long res = predecessor.lastModified(file);
        throwStopEvent(event);
        return res;
    }

    public long length(File file) {
        FileSystemDriverEvent event = buildEvent("length", file);
        throwStartEvent(event);
        long res = predecessor.length(file);
        throwStopEvent(event);
        return res;
    }

    public String[] list(File file, FilenameFilter filter) {
        FileSystemDriverEvent event = buildEvent("list", file);
        event.setArgument(filter);
        throwStartEvent(event);
        String[] res = predecessor.list(file, filter);
        throwStopEvent(event);
        return res;
    }

    public String[] list(File file) {
        FileSystemDriverEvent event = buildEvent("list", file);
        throwStartEvent(event);
        String[] res = predecessor.list(file);
        throwStopEvent(event);
        return res;
    }

    public File[] listFiles(File file, FileFilter filter) {
        FileSystemDriverEvent event = buildEvent("listFiles", file);
        event.setArgument(filter);
        throwStartEvent(event);
        File[] res = predecessor.listFiles(file, filter);
        throwStopEvent(event);
        return res;
    }

    public File[] listFiles(File file, FilenameFilter filter) {
        FileSystemDriverEvent event = buildEvent("listFiles", file);
        event.setArgument(filter);
        throwStartEvent(event);
        File[] res = predecessor.listFiles(file, filter);
        throwStopEvent(event);
        return res;
    }

    public File[] listFiles(File file) {
        FileSystemDriverEvent event = buildEvent("listFiles", file);
        throwStartEvent(event);
        File[] res = predecessor.listFiles(file);
        throwStopEvent(event);
        return res;
    }

    public boolean mkdir(File file) {
        FileSystemDriverEvent event = buildEvent("mkdir", file);
        throwStartEvent(event);
        boolean res = predecessor.mkdir(file);
        throwStopEvent(event);
        return res;
    }

    public boolean mkdirs(File file) {
        FileSystemDriverEvent event = buildEvent("mkdirs", file);
        throwStartEvent(event);
        boolean res = predecessor.mkdirs(file);
        throwStopEvent(event);
        return res;
    }

    public boolean renameTo(File source, File dest) {
        FileSystemDriverEvent event = buildEvent("renameTo", source);
        event.setArgument(dest);
        throwStartEvent(event);
        boolean res = predecessor.renameTo(source, dest);
        throwStopEvent(event);
        return res;
    }

    public boolean setLastModified(File file, long time) {
        FileSystemDriverEvent event = buildEvent("setLastModified", file);
        event.setArgument(new Long(time));
        throwStartEvent(event);
        boolean res = predecessor.setLastModified(file, time);
        throwStopEvent(event);
        return res;
    }

    public boolean setReadOnly(File file) {
        FileSystemDriverEvent event = buildEvent("setReadOnly", file);
        throwStartEvent(event);
        boolean res = predecessor.setReadOnly(file);
        throwStopEvent(event);
        return res;
    }

    public boolean supportsLongFileNames() {
        return predecessor.supportsLongFileNames();
    }

    public void unmount() throws IOException {
        predecessor.unmount();
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, this.identifier);
        h = HashHelper.hash(h, this.predecessor);
        return h;
    }
    
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o instanceof EventFileSystemDriver) {
            EventFileSystemDriver other = (EventFileSystemDriver)o;
            
            return (
                    EqualsHelper.equals(other.identifier, this.identifier) 
                    && EqualsHelper.equals(other.predecessor, this.predecessor) 
            );
        } else {
            return false;
        }
    }
    
    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("Identifier", this.identifier, sb);
        ToStringHelper.append("Predecessor", this.predecessor, sb);
        return ToStringHelper.close(sb);
    }
}
