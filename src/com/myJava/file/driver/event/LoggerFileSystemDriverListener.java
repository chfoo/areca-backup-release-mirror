package com.myJava.file.driver.event;

import java.util.HashSet;
import java.util.Set;

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
public class LoggerFileSystemDriverListener implements FileSystemDriverListener {

    private Set filteredMethods = new HashSet();
    
    public LoggerFileSystemDriverListener() {
        filteredMethods.add("getAbsolutePath");
        filteredMethods.add("getAbsoluteFile");
        filteredMethods.add("getPath");
        filteredMethods.add("getParent");
        filteredMethods.add("getParentFile");
        filteredMethods.add("getName");
    }

    public void methodStarted(FileSystemDriverEvent event) {
    }

    public Set getFilteredMethods() {
        return filteredMethods;
    }

    public void setFilteredMethods(Set filteredMethods) {
        this.filteredMethods = filteredMethods;
    }

    public void methodEnded(FileSystemDriverEvent event) {
        if (! filteredMethods.contains(event.getMethod())) {
            Logger.defaultLogger().info("Method event received : " + event.toString());
        }
    }
}
