package com.application.areca.impl.tools;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.GregorianCalendar;

import com.application.areca.context.ProcessContext;
import com.myJava.file.FileSystemManager;
import com.myJava.system.OSTool;
import com.myJava.util.CalendarUtils;
import com.myJava.util.Util;
import com.myJava.util.log.Logger;

/**
 * Helper class for dynamic tags processing
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
public class TagHelper {
    public static final String PARAM_ARCHIVE_PATH = "%ARCHIVE%";
    public static final String PARAM_ARCHIVE_LOGICAL_PATH = "%ARCHIVE_LOGICAL_PATH%";
    public static final String PARAM_ARCHIVE_NAME = "%ARCHIVE_NAME%";
    public static final String PARAM_TARGET_UID = "%TARGET_UID%";
    public static final String PARAM_TARGET_NAME = "%TARGET_NAME%";
    public static final String PARAM_COMPUTER_NAME = "%COMPUTER_NAME%";
    public static final String PARAM_USER_NAME = "%USER_NAME%";
    public static final String PARAM_DATE = "%DATE%";
    public static final String PARAM_BACKUP_TYPE = "%BACKUP_TYPE%";
    public static final String PARAM_TIME = "%TIME%";
    public static final String PARAM_SUCCESS = "%SUCCESS%";
    public static final String PARAM_HAS_WARNINGS = "%HAS_WARNINGS%";
    
    public static String getTagListHelp() {
    	return 
    			PARAM_ARCHIVE_PATH + ", " +
    			PARAM_ARCHIVE_LOGICAL_PATH + ",\n" +
    			PARAM_ARCHIVE_NAME + ", " +
    			PARAM_TARGET_UID + ",\n" +
    			PARAM_TARGET_NAME + ", " +
    			PARAM_COMPUTER_NAME + ",\n" +
    			PARAM_USER_NAME + ", " +
    			PARAM_DATE + ", " +
    			PARAM_BACKUP_TYPE + ",\n" +
    			PARAM_TIME + ", " +
    			PARAM_SUCCESS + ", " +
    			PARAM_HAS_WARNINGS			
    	;
    }
    
    public static String replaceTag(String value, String oldTag, String newTag) {
        return Util.replace(value, oldTag, newTag);
    }
    
    public static String replaceParamValues(String param, ProcessContext context) {
        if (param == null) {
            return null;
        } else {
            String value = param;
            
            if (context.getCurrentArchiveFile() != null) {
            	String archivePath = FileSystemManager.getPhysicalPath(context.getCurrentArchiveFile());
            	String archiveLogicalPath = FileSystemManager.getAbsolutePath(context.getCurrentArchiveFile());     	
            	if (archivePath == null) {
            		archivePath = "";
            		archiveLogicalPath = "";
            	}
            	if (OSTool.isSystemWindows()) {
            		archivePath = Util.replace(archivePath, "/", "\\");
            		archiveLogicalPath = Util.replace(archiveLogicalPath, "/", "\\");
            	}
                value = Util.replace(value, PARAM_ARCHIVE_PATH, archivePath);
                value = Util.replace(value, PARAM_ARCHIVE_LOGICAL_PATH, archiveLogicalPath);
                value = Util.replace(value, PARAM_ARCHIVE_NAME, FileSystemManager.getName(context.getCurrentArchiveFile()));
            } else {
                value = Util.replace(value, PARAM_ARCHIVE_PATH, "");
                value = Util.replace(value, PARAM_ARCHIVE_NAME, "");
            }
            
            value = Util.replace(value, PARAM_TARGET_UID, context.getReport().getTarget().getUid());
            value = Util.replace(value, PARAM_TARGET_NAME, context.getReport().getTarget().getName());
            
            try {
                String localHost = InetAddress.getLocalHost().getHostName();
                value = Util.replace(value, PARAM_COMPUTER_NAME, localHost);
            } catch (UnknownHostException e) {
                Logger.defaultLogger().error("Error detecting local host name", e);
            }
            
            GregorianCalendar now = new GregorianCalendar();
            value = Util.replace(value, PARAM_DATE, CalendarUtils.getDateToString(now));
            value = Util.replace(value, PARAM_TIME, CalendarUtils.getTimeToString(now));
            value = Util.replace(value, PARAM_USER_NAME, OSTool.getUserName());
            
            value = Util.replace(value, PARAM_SUCCESS, (! context.getReport().hasError()) ? "1" : "0");
            value = Util.replace(value, PARAM_HAS_WARNINGS, context.getReport().getLogMessagesContainer().hasWarnings() ? "1" : "0");

            value = Util.replace(value, PARAM_BACKUP_TYPE, context.getBackupScheme());

            return value;
        }
    }
}
