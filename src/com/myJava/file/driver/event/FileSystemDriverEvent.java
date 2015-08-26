package com.myJava.file.driver.event;

import java.io.File;

import com.myJava.object.ToStringHelper;

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
public class FileSystemDriverEvent {

    private String method;
    private File file;
    private Object[] arguments;
    private long start = System.currentTimeMillis();
    private long end = 0;
    private EventFileSystemDriver driver;

    public FileSystemDriverEvent(String method, File file, EventFileSystemDriver driver) {
        super();
        this.method = method;
        this.file = file;
        this.driver = driver;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }
    
    public void setArgument(Object argument) {
        this.arguments = new Object[] {argument};
    }

    public String getMethod() {
        return method;
    }

    public void registerStop() {
        this.end = System.currentTimeMillis();
    }

    public File getFile() {
        return file;
    }

    public boolean hasEnded() {
        return this.end != 0;
    }
    
    public long getDurationInMs() {
        if (end == 0) {
            registerStop();
        }
        return end - start;
    }
    
    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("Driver ID", this.driver.getIdentifier(), sb);
        ToStringHelper.append("Method", method, sb);
        ToStringHelper.append("File", driver.getPredecessor().getAbsolutePath(file), sb);
        if (this.arguments != null) {
            for (int i=0; i<arguments.length; i++) {
                ToStringHelper.append("Argument" + i, arguments[i], sb);
            }
        }
        if (hasEnded()) {
            ToStringHelper.append("Duration", "" + getDurationInMs(), sb);
        }
        return ToStringHelper.close(sb);
    }
}
