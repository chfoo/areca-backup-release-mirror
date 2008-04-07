package com.application.areca;

import com.myJava.configuration.FrameworkConfiguration;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 8363716858549252512
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
    private static String KEY_BACKUP_DEBUG_MODE = "backup.debug";
    private static String KEY_REPOSITORYACCESS_DEBUG = "repository.access.debug";
    private static String KEY_FILESTREAMS_DEBUG = "file.streams.debug";
    private static String KEY_REPOSITORYACCESS_HD_CACHE = "repository.access.hd.cache";
    private static String KEY_REPOSITORYACCESS_FTP_CACHE = "repository.access.ftp.cache";
    private static String KEY_REPOSITORYACCESS_HD_CACHE_DEPTH = "repository.access.hd.cache.depth";
    private static String KEY_REPOSITORYACCESS_FTP_CACHE_DEPTH = "repository.access.ftp.cache.depth";
    private static String KEY_CHECK_DIRECTORY_CONSISTENCY = "repository.check.consistency";
    private static String KEY_DELTA_MIN_BUCKETSIZE = "delta.min.bucket.size";
    private static String KEY_DELTA_MAX_BUCKETSIZE = "delta.max.bucket.size";
    private static String KEY_DELTA_TARGET_BUCKER_NUMBER = "delta.target.bucket.number";
    
    private static boolean DEF_SMTP_DEBUG = false;
    private static long DEF_MEMORY_BASE_KB = 5 * 1024;
    private static double DEF_MEMORY_BY_ENTRY_KB = 1.7;
    private static double DEF_MEMORY_SAFETY_MARGIN = 0.0;
    private static boolean DEF_CACHE_PRELOAD = false;
    private static boolean DEF_XML_BACKUP = true;
    private static boolean DEF_BACKUP_DEBUG_MODE = false;
    private static boolean DEF_REPOSITORYACCESS_DEBUG = false;
    private static boolean DEF_FILESTREAMS_DEBUG = false;
    private static boolean DEF_REPOSITORYACCESS_HD_CACHE = false;
    private static boolean DEF_REPOSITORYACCESS_FTP_CACHE = true;
    private static int DEF_REPOSITORYACCESS_HD_CACHE_DEPTH = 2;
    private static int DEF_REPOSITORYACCESS_FTP_CACHE_DEPTH = 2;
    private static boolean DEF_CHECK_DIRECTORY_CONSISTENCY = true;
    private static int DEF_DELTA_MIN_BUCKETSIZE = 1 * 1024;
    private static int DEF_DELTA_MAX_BUCKETSIZE = 1 * 1024 * 1024;
    private static int DEF_DELTA_TARGET_BUCKER_NUMBER = 100;
    
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
    
    public boolean isCheckRepositoryConsistency() {
        return getProperty(KEY_CHECK_DIRECTORY_CONSISTENCY, DEF_CHECK_DIRECTORY_CONSISTENCY);
    }
    
    public boolean isRepositoryAccessDebugMode() {
        return getProperty(KEY_REPOSITORYACCESS_DEBUG, DEF_REPOSITORYACCESS_DEBUG);
    }
    
    public boolean isFileStreamsDebugMode() {
        return getProperty(KEY_FILESTREAMS_DEBUG, DEF_FILESTREAMS_DEBUG);
    }
    
    
    public boolean isRepositoryHDCache() {
        return getProperty(KEY_REPOSITORYACCESS_HD_CACHE, DEF_REPOSITORYACCESS_HD_CACHE);
    }
    
    public boolean isRepositoryFTPCache() {
        return getProperty(KEY_REPOSITORYACCESS_FTP_CACHE, DEF_REPOSITORYACCESS_FTP_CACHE);
    }
    
    public int getDeltaMinBucketSize() {
        return getProperty(KEY_DELTA_MIN_BUCKETSIZE, DEF_DELTA_MIN_BUCKETSIZE);
    }
    
    public int getDeltaMaxBucketSize() {
        return getProperty(KEY_DELTA_MAX_BUCKETSIZE, DEF_DELTA_MAX_BUCKETSIZE);
    }
    
    public int getDeltaTargetBucketNumber() {
        return getProperty(KEY_DELTA_TARGET_BUCKER_NUMBER, DEF_DELTA_TARGET_BUCKER_NUMBER);
    }
    
    public int getRepositoryHDCacheDepth() {
        return getProperty(KEY_REPOSITORYACCESS_HD_CACHE_DEPTH, DEF_REPOSITORYACCESS_HD_CACHE_DEPTH);
    }
    
    public int getRepositoryFTPCacheDepth() {
        return getProperty(KEY_REPOSITORYACCESS_FTP_CACHE_DEPTH, DEF_REPOSITORYACCESS_FTP_CACHE_DEPTH);
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
    
    public boolean isBackupDebug() {
        return getProperty(KEY_BACKUP_DEBUG_MODE, DEF_BACKUP_DEBUG_MODE);
    }
}
