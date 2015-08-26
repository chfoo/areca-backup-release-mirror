package com.myJava.file.driver.remote;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.util.Util;
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
public class RemoteDebug {

    private static boolean debugMode = FrameworkConfiguration.getInstance().isFTPDebugMode();
    
    private static String previousOwner = null;
    private static long rowNum = 0;
    
    public synchronized void debug(long proxy, String owner, String message, Object arg) {
        if (isDebugMode()) {
            rowNum++;
            
            if (previousOwner == null || ! previousOwner.equals(owner)) {
                previousOwner = owner;
                System.out.println(" ");
            }
            
            if (arg == null) {
                display(normalize(rowNum + " :      " + proxy + " | " + owner + " > " + message));                
            } else {
                display(normalize(rowNum + " :      " + proxy + " | " + owner + " > " + message + " [" + arg.toString() + "]"));
            }
        }
    }

    private static String normalize(String s) {
        return Util.replace(Util.replace(s, "\n", " "), "\r", " ");
    }
    
    private static boolean isDebugMode() {
        return debugMode;
    }
    
    private static void display(String s) {
        Logger.defaultLogger().fine("      " + s);
    }
}
