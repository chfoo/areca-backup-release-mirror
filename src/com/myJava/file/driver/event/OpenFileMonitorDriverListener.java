package com.myJava.file.driver.event;

import java.util.HashSet;

import com.myJava.util.log.Logger;

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
public class OpenFileMonitorDriverListener implements FileSystemDriverListener {
    private HashSet openFiles = new HashSet();
    private static HashSet monitored = new HashSet();
    
    static {
        monitored.add("renameTo");
        monitored.add("delete");
    }
    
    public void methodEnded(FileSystemDriverEvent event) {
        if (event.getMethod() != null && event.getFile() != null) {
            String path = event.getFile().getAbsolutePath();
            
            if (event.getMethod().equals("getFileOutputStream") || event.getMethod().equals("getFileInputStream")) {
                openFiles.add(path);
            } else if (event.getMethod().equals("o.close") || event.getMethod().equals("i.close")) {           	
                openFiles.remove(path);
            } else if (monitored.contains(event.getMethod()) && openFiles.contains(path)) {
                log("Caution : Trying to work on " + path + " while it is still open !", "FilesystemManager." + event.getMethod() + "()");
            }
        }
    }

    private static void log(String str, String source) {
        Logger.defaultLogger().error(str, source);
        System.out.println(str + " - " + source);
    }
    
    public void methodStarted(FileSystemDriverEvent event) {
    }
}
