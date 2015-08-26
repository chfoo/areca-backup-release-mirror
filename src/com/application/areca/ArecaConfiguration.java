package com.application.areca;

import com.myJava.configuration.FrameworkConfiguration;

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
public class ArecaConfiguration 
extends FrameworkConfiguration {   
    public static String URI = "fwk.properties";

    /**
     * Activate SMTP accesses debug log
     */
    public static String KEY_SMTP_DEBUG = "smtp.debug";
    
    /**
     * Base memory needed by areca (regardless to the number of targets / entries)
     */
    public static String KEY_MEMORY_BASE_KB = "memory.base.kb";
    
    /**
     * KB / entry needed by Areca
     */
    public static String KEY_MEMORY_BY_ENTRY_KB = "memory.by.entry.kb";
    
    /**
     * Safety margin which is taken into account when Areca computes the required memory to handle a target
     */
    public static String KEY_MEMORY_SAFETY_MARGIN = "memory.safety.margin";
    
    /**
     * Tells whether a xml copy of the target's configuration must be created on the backup location
     */
    public static String KEY_XML_BACKUP = "xml.config.backup";
    
    /**
     * Enable backup debug log
     */
    public static String KEY_BACKUP_DEBUG_MODE = "backup.debug";
    
    /**
     * Enable thread monitor
     */
    public static String KEY_ENABLE_TH_MONITOR = "thread.monitor";
    
    /**
     * Enable check debug log
     */
    public static String KEY_CHECK_DEBUG_MODE = "check.debug";
    
    /**
     * Enable storage location accesses debug log
     */
    public static String KEY_REPOSITORYACCESS_DEBUG = "repository.access.debug";
    
    /**
     * Enable file cached in/output streams debug log
     */
    public static String KEY_FILESTREAMS_DEBUG = "file.streams.debug";
    
    /**
     * Tells whether the metadata of files stored on a HD must be cached or not
     */
    public static String KEY_REPOSITORYACCESS_HD_CACHE = "repository.access.hd.cache";
    
    /**
     * Tells whether the metadata of files stored on a FTP server must be cached or not
     */
    public static String KEY_REPOSITORYACCESS_FTP_CACHE = "repository.access.ftp.cache";
    
    /**
     * Cache depth (HD file metadata)
     */
    public static String KEY_REPOSITORYACCESS_HD_CACHE_DEPTH = "repository.access.hd.cache.depth";

    /**
     * Cache depth (FTP file metadata)
     */
    public static String KEY_REPOSITORYACCESS_FTP_CACHE_DEPTH = "repository.access.ftp.cache.depth";
    
    /**
     * Tells whether Areca will do some checks (for instance check that the storage location is not a subdirectory of the sources directories)
     */
    public static String KEY_CHECK_DIRECTORY_CONSISTENCY = "repository.check.consistency";
    
    /**
     * Activate debug mode for metadata handling
     */
    public static String KEY_MDT_DEBUG = "metadata.debug";
    
    /**
     * Minimum bucket size for delta storage
     */
    public static String KEY_DELTA_MIN_BUCKETSIZE = "delta.min.bucket.size";
    
    /**
     * Maximum bucket size for delta storage
     */
    public static String KEY_DELTA_MAX_BUCKETSIZE = "delta.max.bucket.size";
    
    /**
     * Target buckets number for delta storage
     */
    public static String KEY_DELTA_TARGET_BUCKER_NUMBER = "delta.target.bucket.number";

    /**
     * Log location (set this property if you want to force this location)
     */
    public static String KEY_FORCED_LOG_LOCATION = "log.location.override";
    
    /**
     * Launch scripts path (set this property if you want to force this location)
     */
    public static String KEY_FORCED_BIN_LOCATION = "bin.location.override";
    
    /**
     * Plugin path (set this property if you want to force this location)
     */
    public static String KEY_FORCED_PLUGIN_LOCATION = "plugins.location.override";
    
    /**
     * Module path (set this property if you want to force this location)
     */
    public static String KEY_FORCED_MODULE_LOCATION = "modules.location.override";
    
    /**
     * Language files path (set this property if you want to force this location)
     */
    public static String KEY_FORCED_LN_LOCATION = "languages.location.override";
    
    /**
     * Number of retries in case of FTP transfer error
     */
    public static String KEY_MAX_FTP_RETRIES = "ftp.max.retries";
    
    /**
     * Delay before the thread monitor raises an inactivity alert
     */
    public static String KEY_TH_MONITOR_DELAY = "thread.monitor.delay";
    
    /**
     * Name of the "commit" file
     */
    public static String KEY_COMMIT_FILE_NAME = "commit.file.name";
    
    /**
     * Nr of kBytes written before saving a transaction point
     */
    public static String KEY_TRANSACTION_SIZE_KB = "transaction.size";
    
    /**
     * Whether target properties are logged when an action is performed (merge, backup, ...)
     */
    public static String KEY_LOG_TARGET_PROPERTIES = "log.target.properties";
    
    /**
     * Prevent empty archives from being created
     */
    public static String KEY_PREVENT_EMPTY_ARCHIVES = "prevent.empty.archives";

    public static boolean DEF_SMTP_DEBUG = false;
    public static long DEF_MEMORY_BASE_KB = 4 * 1024;
    public static double DEF_MEMORY_BY_ENTRY_KB = 1.3;
    public static double DEF_MEMORY_SAFETY_MARGIN = 0.0;
    public static boolean DEF_XML_BACKUP = true;
    public static boolean DEF_BACKUP_DEBUG_MODE = false;
    public static boolean DEF_CHECK_DEBUG_MODE = false;
    public static boolean DEF_REPOSITORYACCESS_DEBUG = false;
    public static boolean DEF_FILESTREAMS_DEBUG = false;
    public static boolean DEF_REPOSITORYACCESS_HD_CACHE = false;
    public static boolean DEF_REPOSITORYACCESS_FTP_CACHE = true;
    public static int DEF_REPOSITORYACCESS_HD_CACHE_DEPTH = 2;
    public static int DEF_REPOSITORYACCESS_FTP_CACHE_DEPTH = 3;
    public static boolean DEF_CHECK_DIRECTORY_CONSISTENCY = true;
    public static int DEF_DELTA_MIN_BUCKETSIZE = 1 * 1024;
    public static int DEF_DELTA_MAX_BUCKETSIZE = 1 * 1024 * 1024;
    public static int DEF_DELTA_TARGET_BUCKER_NUMBER = 100;
    public static String DEF_FORCED_LOG_LOCATION = null;
    public static String DEF_FORCED_BIN_LOCATION = null;
    public static String DEF_FORCED_PLUGIN_LOCATION = null;
    public static String DEF_FORCED_MODULE_LOCATION = null;
    public static String DEF_FORCED_LN_LOCATION = null;
    public static int DEF_MAX_FTP_RETRIES = 5;
    public static boolean DEF_MDT_DEBUG = false;
    public static boolean DEF_ENABLE_TH_MONITOR = false;
    public static long DEF_TH_MONITOR_DELAY = 1000*60*3;
    public static String DEF_COMMIT_FILE_NAME = ".committed";
    public static long DEF_TRANSACTION_SIZE_KB = 50*1024;
    public static boolean DEF_LOG_TARGET_PROPERTIES = true;
    public static boolean DEF_PREVENT_EMPTY_ARCHIVES = false;
    
    public ArecaConfiguration() {
        super();
    }
    
    public static ArecaConfiguration get() {
        return (ArecaConfiguration)FrameworkConfiguration.getInstance();
    }

    public ArecaConfiguration(String url) {
        super(url);
    }
    
    public static synchronized void initialize() {
        ArecaConfiguration cfg = new ArecaConfiguration(URI);
        FrameworkConfiguration.setInstance(cfg);
    }

    public boolean isSMTPDebugMode() {
        return getProperty(KEY_SMTP_DEBUG, DEF_SMTP_DEBUG);
    }
    
    public boolean isLogTargetProperties() {
        return getProperty(KEY_LOG_TARGET_PROPERTIES, DEF_LOG_TARGET_PROPERTIES);
    }
    
    public boolean isPreventEmptyArchives() {
        return getProperty(KEY_PREVENT_EMPTY_ARCHIVES, DEF_PREVENT_EMPTY_ARCHIVES);
    }
    
    public String getLogLocationOverride() {
        return getProperty(KEY_FORCED_LOG_LOCATION, DEF_FORCED_LOG_LOCATION);
    }
    
    public String getCommitFileName() {
        return getProperty(KEY_COMMIT_FILE_NAME, DEF_COMMIT_FILE_NAME);
    }
    
    public String getBinLocationOverride() {
        return getProperty(KEY_FORCED_BIN_LOCATION, DEF_FORCED_BIN_LOCATION);
    }
    
    public String getPluginsLocationOverride() {
        return getProperty(KEY_FORCED_PLUGIN_LOCATION, DEF_FORCED_PLUGIN_LOCATION);
    }
    
    public String getModulesLocationOverride() {
        return getProperty(KEY_FORCED_MODULE_LOCATION, DEF_FORCED_MODULE_LOCATION);
    }
    
    public String getLanguageLocationOverride() {
        return getProperty(KEY_FORCED_LN_LOCATION, DEF_FORCED_LN_LOCATION);
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
    
    public boolean isMetaDataDebugMode() {
        return getProperty(KEY_MDT_DEBUG, DEF_MDT_DEBUG);
    }
    
    public boolean isRepositoryHDCache() {
        return getProperty(KEY_REPOSITORYACCESS_HD_CACHE, DEF_REPOSITORYACCESS_HD_CACHE);
    }
    
    public boolean isRepositoryFTPCache() {
        return getProperty(KEY_REPOSITORYACCESS_FTP_CACHE, DEF_REPOSITORYACCESS_FTP_CACHE);
    }
    
    public int getMaxFTPRetries() {
        return getProperty(KEY_MAX_FTP_RETRIES, DEF_MAX_FTP_RETRIES);
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
    
    public boolean isXMLBackup() {
        return getProperty(KEY_XML_BACKUP, DEF_XML_BACKUP);
    }
    
    public boolean isThreadMonitorEnabled() {
        return getProperty(KEY_ENABLE_TH_MONITOR, DEF_ENABLE_TH_MONITOR);
    }
    
    public long getThreadMonitorDelay() {
        return getProperty(KEY_TH_MONITOR_DELAY, DEF_TH_MONITOR_DELAY);
    }
    
    public boolean isBackupDebug() {
        return getProperty(KEY_BACKUP_DEBUG_MODE, DEF_BACKUP_DEBUG_MODE);
    }
    
    public boolean isCheckDebug() {
        return getProperty(KEY_CHECK_DEBUG_MODE, DEF_CHECK_DEBUG_MODE);
    }
    
    public long getTransactionSize() {
        return getProperty(KEY_TRANSACTION_SIZE_KB, DEF_TRANSACTION_SIZE_KB);
    }
}
