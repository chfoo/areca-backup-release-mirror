package com.application.areca;

import java.io.File;
import java.util.Enumeration;
import java.util.Properties;

import com.application.areca.version.VersionInfos;
import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.FileSystemManager;
import com.myJava.system.OSTool;
import com.myJava.util.log.Logger;

/**
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
public class LogHelper {
    private static String SEPARATOR = "----------------------------------------";
    
    public static void logStartupInformations() {

        Logger.defaultLogger().info(SEPARATOR);
        log("System informations :");
        log("Version : " + VersionInfos.getLastVersion().getVersionId());
        log("Build ID : " + VersionInfos.getBuildId());
        log("Available Memory : " + OSTool.getMaxMemoryMB() + " MB");
        log("OS Name : " + OSTool.getOSDescription());
        log("Java Virtual Machine : " + OSTool.getVMDescription());
        log("File encoding : " + OSTool.getIANAFileEncoding());
        log("Language : " + OSTool.getUserLanguage());
        log("Framework overriden properties : " + FrameworkConfiguration.getInstance().toString());
        log("Available translations : " + Utils.getTranslationsAsString());
        Logger.defaultLogger().info(SEPARATOR);
    }
    
    public static void logFileInformations(String description, File f) {
        StringBuffer sb = new StringBuffer(description).append(' ');
        try {
            if (f == null) {
                sb.append("<null>");
            } else {
                String ap = FileSystemManager.getAbsolutePath(f);
                String cp = FileSystemManager.getCanonicalPath(f);
                sb.append("AbsolutePath = ").append(ap);
                if (! ap.equals(cp)) {
                    sb.append(", CanonicalPath = ").append(cp);
                }
            }
        } catch (Throwable e) {
            Logger.defaultLogger().error(e);
        } finally {
            log(sb.toString());
        }
    }
    
    public static void logProperties(String description, Properties p) {
        try {
            Logger.defaultLogger().info(SEPARATOR);
            log(description);
            
            if (p == null) {
                log("<null>");
            } else {
                Enumeration en = p.keys();
                while (en.hasMoreElements()) {
                    String key = (String)en.nextElement();
                    log("[" + key + "] = [" + p.getProperty(key) + "]");
                }
            }
            Logger.defaultLogger().info(SEPARATOR);
        } catch (Throwable e) {
            Logger.defaultLogger().error(e);
        }        
    }
    
    private static void log(String str) {
        Logger.defaultLogger().info(str);
    }
}
