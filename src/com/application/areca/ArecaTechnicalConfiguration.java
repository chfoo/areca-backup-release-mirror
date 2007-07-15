package com.application.areca;

import com.myJava.configuration.FrameworkConfiguration;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -1628055869823963574
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
public class ArecaTechnicalConfiguration 
extends FrameworkConfiguration {
    private static String URI = "fwk.properties";
    
    private static String KEY_SMTP_DEBUG = "smtp.debug";
    private static String KEY_MEMORY_BASE_KB = "memory.base.kb";
    private static String KEY_MEMORY_BY_ENTRY_KB = "memory.by.entry.kb";
    private static String KEY_MEMORY_SAFETY_MARGIN = "memory.safety.margin";
    private static String KEY_CACHE_PRELOAD = "cache.preload";
    private static String KEY_XML_BACKUP = "xml.config.backup";
    
    private static boolean DEF_SMTP_DEBUG = false;
    private static long DEF_MEMORY_BASE_KB = 20 * 1024;
    private static double DEF_MEMORY_BY_ENTRY_KB = 2;
    private static double DEF_MEMORY_SAFETY_MARGIN = 0.3;
    private static boolean DEF_CACHE_PRELOAD = false;
    private static boolean DEF_XML_BACKUP = true;
    
    public ArecaTechnicalConfiguration() {
        super();
    }
    
    public static ArecaTechnicalConfiguration get() {
        return (ArecaTechnicalConfiguration)FrameworkConfiguration.getInstance();
    }

    public ArecaTechnicalConfiguration(String url) {
        super(url);
    }
    
    public static synchronized void initialize() {
        ArecaTechnicalConfiguration cfg = new ArecaTechnicalConfiguration(URI);
        FrameworkConfiguration.setInstance(cfg);
    }
    
    public boolean isSMTPDebugMode() {
        return getProperty(KEY_SMTP_DEBUG, DEF_SMTP_DEBUG);
    }
    
    public long getMemoryBaseKb() {
        return getProperty(KEY_MEMORY_BASE_KB, DEF_MEMORY_BASE_KB);
    }
    
    public double getMemoryByEntryKb() {
        return getProperty(KEY_MEMORY_BY_ENTRY_KB, DEF_MEMORY_BY_ENTRY_KB);
    }
    
    public double getMemorySafetyMargin() {
        return getProperty(KEY_MEMORY_SAFETY_MARGIN, DEF_MEMORY_SAFETY_MARGIN);
    }
    
    public boolean isCachePreload() {
        return getProperty(KEY_CACHE_PRELOAD, DEF_CACHE_PRELOAD);
    }
    
    public boolean isXMLBackup() {
        return getProperty(KEY_XML_BACKUP, DEF_XML_BACKUP);
    }
}
