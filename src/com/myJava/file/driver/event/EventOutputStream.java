package com.myJava.file.driver.event;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
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
public class EventOutputStream 
extends OutputStream {
    
    private OutputStream out;
    private File file;
    private EventFileSystemDriver driver;

    public EventOutputStream(OutputStream out, File file, EventFileSystemDriver driver) {
        super();
        this.out = out;
        this.file = file;
        this.driver = driver;
    }

    public void close() throws IOException {
        FileSystemDriverEvent event = driver.buildEvent("o.close", file);
        driver.throwStartEvent(event);
        out.close();
        driver.throwStopEvent(event);
    }

    public void flush() throws IOException {
        FileSystemDriverEvent event = driver.buildEvent("flush", file);
        driver.throwStartEvent(event);
        out.flush();
        driver.throwStopEvent(event);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        FileSystemDriverEvent event = driver.buildEvent("write", file);
        event.setArgument(new Integer(len));
        driver.throwStartEvent(event);
        out.write(b, off, len);
        driver.throwStopEvent(event);
    }

    public void write(byte[] b) throws IOException {
        FileSystemDriverEvent event = driver.buildEvent("write", file);
        event.setArgument(new Integer(b.length));
        driver.throwStartEvent(event);
        out.write(b);
        driver.throwStopEvent(event);
    }

    public void write(int b) throws IOException {
        FileSystemDriverEvent event = driver.buildEvent("write", file);
        driver.throwStartEvent(event);
        out.write(b);
        driver.throwStopEvent(event);
    }
}
