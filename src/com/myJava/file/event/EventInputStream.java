package com.myJava.file.event;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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
public class EventInputStream 
extends InputStream {
    
    private InputStream in;
    private File file;
    private EventFileSystemDriver driver;

    public EventInputStream(InputStream in, File file, EventFileSystemDriver driver) {
        super();
        this.in = in;
        this.file = file;
        this.driver = driver;
    }

    public int available() throws IOException {
        FileSystemDriverEvent event = new FileSystemDriverEvent("available", file, driver.getPredecessor());
        driver.throwStartEvent(event);
        int ret = in.available();
        driver.throwStopEvent(event);
        return ret;
    }
    
    public void close() throws IOException {
        FileSystemDriverEvent event = new FileSystemDriverEvent("close", file, driver.getPredecessor());
        driver.throwStartEvent(event);
        in.close();
        driver.throwStopEvent(event);
    }
    
    public void mark(int readlimit) {
        FileSystemDriverEvent event = new FileSystemDriverEvent("mark", file, driver.getPredecessor());
        driver.throwStartEvent(event);
        in.mark(readlimit);
        driver.throwStopEvent(event);
    }
    
    public boolean markSupported() {
        FileSystemDriverEvent event = new FileSystemDriverEvent("markSupported", file, driver.getPredecessor());
        driver.throwStartEvent(event);
        boolean ret = in.markSupported();
        driver.throwStopEvent(event);
        return ret;
    }
    
    public int read() throws IOException {
        FileSystemDriverEvent event = new FileSystemDriverEvent("read", file, driver.getPredecessor());
        driver.throwStartEvent(event);
        int ret = in.read();
        driver.throwStopEvent(event);
        return ret;
    }
    
    public int read(byte[] b, int off, int len) throws IOException {
        FileSystemDriverEvent event = new FileSystemDriverEvent("read", file, driver.getPredecessor());
        event.setArgument(new Integer(len));
        driver.throwStartEvent(event);
        int ret =  in.read(b, off, len);
        driver.throwStopEvent(event);
        return ret;
    }
    
    public int read(byte[] b) throws IOException {
        FileSystemDriverEvent event = new FileSystemDriverEvent("read", file, driver.getPredecessor());
        event.setArgument(new Integer(b.length));
        driver.throwStartEvent(event);
        int ret = in.read(b);
        driver.throwStopEvent(event);
        return ret;
    }
    
    public void reset() throws IOException {
        FileSystemDriverEvent event = new FileSystemDriverEvent("reset", file, driver.getPredecessor());
        driver.throwStartEvent(event);
        in.reset();
        driver.throwStopEvent(event);
    }
    
    public long skip(long n) throws IOException {
        FileSystemDriverEvent event = new FileSystemDriverEvent("skip", file, driver.getPredecessor());
        event.setArgument(new Long(n));
        driver.throwStartEvent(event);
        long ret = in.skip(n);
        driver.throwStopEvent(event);
        return ret;
    }
}