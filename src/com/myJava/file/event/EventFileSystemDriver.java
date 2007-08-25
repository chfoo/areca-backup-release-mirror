package com.myJava.file.event;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import com.myJava.file.AbstractLinkableFileSystemDriver;
import com.myJava.file.FileSystemDriver;
import com.myJava.file.LinkableFileSystemDriver;
import com.myJava.file.attributes.Attributes;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -3366468978279844961
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
public class EventFileSystemDriver 
extends AbstractLinkableFileSystemDriver
implements LinkableFileSystemDriver {

    private ArrayList listeners = new ArrayList();
    
    public EventFileSystemDriver(FileSystemDriver predecessor) {
        super();
        this.setPredecessor(predecessor);
    }
    
    public EventFileSystemDriver(FileSystemDriver predecessor, FileSystemDriverListener listener) {
        this(predecessor);
        addListener(listener);
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

    public void applyAttributes(Attributes p, File f) throws IOException {
        FileSystemDriverEvent event = new FileSystemDriverEvent("applyAttributes", f, predecessor);
        event.setArgument(p);
        throwStartEvent(event);
        predecessor.applyAttributes(p, f);
        throwStopEvent(event);
    }

    public boolean canRead(File file) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("canRead", file, predecessor);
        throwStartEvent(event);
        boolean res = predecessor.canRead(file);
        throwStopEvent(event);
        return res;
    }

    public boolean canWrite(File file) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("canWrite", file, predecessor);
        throwStartEvent(event);
        boolean res =  predecessor.canWrite(file);
        throwStopEvent(event);
        return res;
    }

    public boolean createNewFile(File file) throws IOException {
        FileSystemDriverEvent event = new FileSystemDriverEvent("createNewFile", file, predecessor);
        throwStartEvent(event);
        boolean res =  predecessor.createNewFile(file);
        throwStopEvent(event);
        return res;
    }

    public boolean createSymbolicLink(File symlink, String realPath) throws IOException {
        FileSystemDriverEvent event = new FileSystemDriverEvent("createSymbolicLink", symlink, predecessor);
        event.setArgument(realPath);
        throwStartEvent(event);
        boolean res =  predecessor.createSymbolicLink(symlink, realPath);
        throwStopEvent(event);
        return res;
    }

    public boolean delete(File file) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("delete", file, predecessor);
        throwStartEvent(event);
        boolean res =  predecessor.delete(file);
        throwStopEvent(event);
        return res;
    }

    public void deleteOnExit(File f) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("deleteOnExit", f, predecessor);
        throwStartEvent(event);
        predecessor.deleteOnExit(f);
        throwStopEvent(event);
    }

    public boolean directFileAccessSupported() {
        return predecessor.directFileAccessSupported();
    }

    public boolean exists(File file) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("exists", file, predecessor);
        throwStartEvent(event);
        boolean res =  predecessor.exists(file);
        throwStopEvent(event);
        return res;
    }

    public void flush() throws IOException {
        predecessor.flush();
    }

    public File getAbsoluteFile(File file) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("getAbsoluteFile", file, predecessor);
        throwStartEvent(event);
        File res = predecessor.getAbsoluteFile(file);
        throwStopEvent(event);
        return res;
    }

    public String getAbsolutePath(File file) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("getAbsolutePath", file, predecessor);
        throwStartEvent(event);
        String res = predecessor.getAbsolutePath(file);
        throwStopEvent(event);
        return res;
    }

    public short getAccessEfficiency() {
        return predecessor.getAccessEfficiency();
    }

    public Attributes getAttributes(File f) throws IOException {
        FileSystemDriverEvent event = new FileSystemDriverEvent("getAttributes", f, predecessor);
        throwStartEvent(event);
        Attributes res = predecessor.getAttributes(f);
        throwStopEvent(event);
        return res;
    }

    public OutputStream getCachedFileOutputStream(File file) throws IOException {
        FileSystemDriverEvent event = new FileSystemDriverEvent("getCachedFileOutputStream", file, predecessor);
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
        FileSystemDriverEvent event = new FileSystemDriverEvent("getCanonicalFile", file, predecessor);
        throwStartEvent(event);
        File res = predecessor.getCanonicalFile(file);
        throwStopEvent(event);
        return res;
    }

    public String getCanonicalPath(File file) throws IOException {
        FileSystemDriverEvent event = new FileSystemDriverEvent("getCanonicalPath", file, predecessor);
        throwStartEvent(event);
        String res = predecessor.getCanonicalPath(file);
        throwStopEvent(event);
        return res;
    }

    public InputStream getFileInputStream(File file) throws IOException {
        FileSystemDriverEvent event = new FileSystemDriverEvent("getFileInputStream", file, predecessor);
        throwStartEvent(event);
        InputStream res = new EventInputStream(
                predecessor.getFileInputStream(file),
                file,
                this
        );
        throwStopEvent(event);
        return res;
    }

    public OutputStream getFileOutputStream(File file, boolean append) throws IOException {
        FileSystemDriverEvent event = new FileSystemDriverEvent("getFileOutputStream", file, predecessor);
        event.setArgument(new Boolean(append));
        throwStartEvent(event);
        OutputStream res = new EventOutputStream(
                predecessor.getFileOutputStream(file, append),
                file,
                this
        );
        throwStopEvent(event);
        return res;
    }

    public OutputStream getFileOutputStream(File file) throws IOException {
        FileSystemDriverEvent event = new FileSystemDriverEvent("getFileOutputStream", file, predecessor);
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
        FileSystemDriverEvent event = new FileSystemDriverEvent("getName", file, predecessor);
        throwStartEvent(event);
        String res = predecessor.getName(file);
        throwStopEvent(event);
        return res;
    }

    public String getParent(File file) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("getParent", file, predecessor);
        throwStartEvent(event);
        String res = predecessor.getParent(file);
        throwStopEvent(event);
        return res;
    }

    public File getParentFile(File file) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("getParentFile", file, predecessor);
        throwStartEvent(event);
        File res = predecessor.getParentFile(file);
        throwStopEvent(event);
        return res;
    }

    public String getPath(File file) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("getPath", file, predecessor);
        throwStartEvent(event);
        String res = predecessor.getPath(file);
        throwStopEvent(event);
        return res;
    }

    public boolean isAbsolute(File file) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("isAbsolute", file, predecessor);
        throwStartEvent(event);
        boolean res = predecessor.isAbsolute(file);
        throwStopEvent(event);
        return res;
    }

    public boolean isContentSensitive() {
        return predecessor.isContentSensitive();
    }

    public boolean isDirectory(File file) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("isDirectory", file, predecessor);
        throwStartEvent(event);
        boolean res = predecessor.isDirectory(file);
        throwStopEvent(event);
        return res;
    }

    public boolean isFile(File file) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("isFile", file, predecessor);
        throwStartEvent(event);
        boolean res = predecessor.isFile(file);
        throwStopEvent(event);
        return res;
    }

    public boolean isHidden(File file) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("isHidden", file, predecessor);
        throwStartEvent(event);
        boolean res = predecessor.isHidden(file);
        throwStopEvent(event);
        return res;
    }

    public long lastModified(File file) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("lastModified", file, predecessor);
        throwStartEvent(event);
        long res = predecessor.lastModified(file);
        throwStopEvent(event);
        return res;
    }

    public long length(File file) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("length", file, predecessor);
        throwStartEvent(event);
        long res = predecessor.length(file);
        throwStopEvent(event);
        return res;
    }

    public String[] list(File file, FilenameFilter filter) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("list", file, predecessor);
        event.setArgument(filter);
        throwStartEvent(event);
        String[] res = predecessor.list(file, filter);
        throwStopEvent(event);
        return res;
    }

    public String[] list(File file) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("list", file, predecessor);
        throwStartEvent(event);
        String[] res = predecessor.list(file);
        throwStopEvent(event);
        return res;
    }

    public File[] listFiles(File file, FileFilter filter) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("listFiles", file, predecessor);
        event.setArgument(filter);
        throwStartEvent(event);
        File[] res = predecessor.listFiles(file, filter);
        throwStopEvent(event);
        return res;
    }

    public File[] listFiles(File file, FilenameFilter filter) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("listFiles", file, predecessor);
        event.setArgument(filter);
        throwStartEvent(event);
        File[] res = predecessor.listFiles(file, filter);
        throwStopEvent(event);
        return res;
    }

    public File[] listFiles(File file) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("listFiles", file, predecessor);
        throwStartEvent(event);
        File[] res = predecessor.listFiles(file);
        throwStopEvent(event);
        return res;
    }

    public boolean mkdir(File file) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("mkdir", file, predecessor);
        throwStartEvent(event);
        boolean res = predecessor.mkdir(file);
        throwStopEvent(event);
        return res;
    }

    public boolean mkdirs(File file) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("mkdirs", file, predecessor);
        throwStartEvent(event);
        boolean res = predecessor.mkdirs(file);
        throwStopEvent(event);
        return res;
    }

    public boolean renameTo(File source, File dest) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("renameTo", source, predecessor);
        event.setArgument(dest);
        throwStartEvent(event);
        boolean res = predecessor.renameTo(source, dest);
        throwStopEvent(event);
        return res;
    }

    public boolean setLastModified(File file, long time) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("setLastModified", file, predecessor);
        event.setArgument(new Long(time));
        throwStartEvent(event);
        boolean res = predecessor.setLastModified(file, time);
        throwStopEvent(event);
        return res;
    }

    public boolean setReadOnly(File file) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("setReadOnly", file, predecessor);
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
}
