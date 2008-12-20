package com.myJava.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import com.myJava.util.log.Logger;

/**
 * Framework configuration keys and default values.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 8785459451506899793
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
public class FrameworkConfiguration {
    private static FrameworkConfiguration instance = new FrameworkConfiguration(); 
    
    public static String KEY_ENCRYPTION_KG_ITER = "encryption.keygen.iterations";
    public static String KEY_ENCRYPTION_KG_SALT = "encryption.keygen.salt";
    public static String KEY_ENCRYPTION_KG_SALT_ENC = "encryption.keygen.salt.encoding";
    public static String KEY_ENCRYPTION_KG_ALG = "encryption.keygen.algorithm";
    public static String KEY_DELTA_DEBUG = "delta.debug";
    public static String KEY_FTP_MAX_PROXIES = "ftp.max.proxies";
    public static String KEY_FTP_DEBUG = "ftp.debug";
    public static String KEY_FT_DELAY = "filetool.delay";
    public static String KEY_FT_BUFFER_SIZE = "filetool.buffer.size";
    public static String KEY_FTP_NOOP_DELAY = "ftp.noop.delay";    
    public static String KEY_FTP_CACHE_SIZE = "ftp.cache.size";    
    public static String KEY_FTP_USE_CACHE = "ftp.use.cache";
    public static String KEY_HASH_CACHE_SIZE = "hash.cache.size";    
    public static String KEY_HASH_USE_CACHE = "hash.use.cache";
    public static String KEY_OS_BROWSERS = "os.browsers";  
    public static String KEY_SSE_PROTOCOLS = "sse.protocols";  
    public static String KEY_ZIP_BUFFER = "zip.buffer.size"; 
    public static String KEY_ZIP_MV_DIGITS = "zip.mv.digits"; 
    public static String KEY_LOG_LEVEL = "log.level";
    public static String KEY_FS_USE_BUFFER = "fs.use.buffer";   
    public static String KEY_FS_BUFFER_SIZE = "fs.buffer.size";   
    public static String KEY_FS_CACHE_DEBUG = "fs.cache.debug";    
    public static String KEY_LAUNCHER_IH = "launcher.initialheap";  
    public static String KEY_LAUNCHER_MH = "launcher.maxheap";   
    public static String KEY_LAUNCHER_WAITFOR = "launcher.waitfor"; 
    public static String KEY_ZIP_ENTRY_CHECK_ENABLE = "zip.crc.enable";   
    public static String KEY_MAX_FILEPATH_LENGTH = "fs.max.filepath";   
    public static String KEY_FORCE_FILEPATH_LENGTH_CHECK = "fs.max.filepath.check.force";   
    public static String KEY_DEFAULT_LOG_HISTORY = "log.default.history";
    public static String KEY_WRITABLE_DIRECTORIES = "fs.writable.directories";  
    public static String KEY_DELTA_LINKEDLIST_BUFFER_SIZE = "delta.linkedlist.buffer.size"; 
    public static String KEY_DELTA_HASHMAP_SIZE = "delta.hashmap.size"; 
    public static String KEY_DELTA_QUICKHASH_MULTIPLIER = "delta.quickhash.multiplier"; 
    public static String KEY_DELTA_QUICKHASH_MODULUS = "delta.quickhash.modulus"; 
    public static String KEY_FILESYSTEM_ACCESSOR = "filesystem.accessor.impl"; 
    
    public static int DEF_ENCRYPTION_KG_ITER = 96731;
    public static String DEF_ENCRYPTION_KG_SALT = "ù%${{²]}}[|`è€$£^¤*!§:/..;;,,_?\"\\°à@@%µ";
    public static String DEF_ENCRYPTION_KG_SALT_ENC = "UTF-8";
    public static String DEF_ENCRYPTION_KG_ALG = "PBKDF2WithHmacSHA1";
    public static boolean DEF_DELTA_DEBUG = false;
    public static int DEF_FTP_MAX_PROXIES = 3;
    public static long DEF_FTP_NOOP_DELAY = 30000;    
    public static boolean DEF_FTP_DEBUG = false;
    public static int DEF_FT_DELAY = 100;
    public static int DEF_FT_BUFFER_SIZE = 100000;
    public static int DEF_FTP_CACHE_SIZE = 200;    
    public static boolean DEF_FTP_USE_CACHE = true;    
    public static int DEF_HASH_CACHE_SIZE = 500;    
    public static boolean DEF_HASH_USE_CACHE = true;    
    public static String[] DEF_OS_BROWSERS = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
    public static String[] DEF_SSE_PROTOCOLS = {"TLS", "SSL"};  
    public static int DEF_ZIP_BUFFER = 2048;
    public static int DEF_ZIP_MV_DIGITS = 2; 
    public static int DEF_LOG_LEVEL = 8;
    public static boolean DEF_FS_USE_BUFFER = true;   
    public static int DEF_FS_BUFFER_SIZE = 100000;   
    public static boolean DEF_FS_CACHE_DEBUG = false; 
    public static int DEF_LAUNCHER_IH = -1;  
    public static int DEF_LAUNCHER_MH = -1;   
    public static boolean DEF_LAUNCHER_WAITFOR = true;  
    public static boolean DEF_ZIP_ENTRY_CHECK_ENABLE = true;  
    public static long DEF_MAX_FILEPATH_LENGTH = 256;   
    public static int DEF_FORCE_FILEPATH_LENGTH_CHECK = -1;   // -1 = UNSET, 0 = FORCE DISABLE, 1 = FORCE ENABLE
    public static int DEF_DEFAULT_LOG_HISTORY = 10;  
    public static String[] DEF_WRITABLE_DIRECTORIES = new String[] {};
    public static int DEF_DELTA_LINKEDLIST_BUFFER_SIZE = 200 * 1024; 
    public static int DEF_DELTA_HASHMAP_SIZE = 10007;
    public static int DEF_DELTA_QUICKHASH_MULTIPLIER = 691 * 13 * 11; 
    public static int DEF_DELTA_QUICKHASH_MODULUS = 4013423 * 17; 
    public static String DEF_FILESYSTEM_ACCESSOR = null; 

    private static String VM_PROPS_PREFIX = "launcher.d.";
    
    private String strUrl = null;
    private Properties props = new Properties();

    public static synchronized FrameworkConfiguration getInstance() {
        return instance;
    }
    
    public static synchronized void setInstance(FrameworkConfiguration i) {
        instance = i;
    }
    
    public FrameworkConfiguration() {
        init();
    }
    
    public FrameworkConfiguration(String url) {
        this.strUrl = url;
        init();
    }
    
    public Properties getProperties() {
        return props;
    }
    
    protected void init() {
        if (this.strUrl != null) {
            InputStream in = null;
            try {
                URL url = ClassLoader.getSystemClassLoader().getResource(strUrl);
                if (url != null) {
	                in = url.openStream();
	                if (in != null) {
	                    props.load(in);
	                }
                }
            } catch (IOException e) {
                Logger.defaultLogger().error("Error during framework properties loading", e);
            }  finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }
    
    public int getEncryptionKGIters() {
    	return getProperty(KEY_ENCRYPTION_KG_ITER, DEF_ENCRYPTION_KG_ITER);
    }
    
    public String getFileSystemAccessorImpl() {
    	return getProperty(KEY_FILESYSTEM_ACCESSOR, DEF_FILESYSTEM_ACCESSOR);
    }
    
    public String getEncryptionKGAlg() {
    	return getProperty(KEY_ENCRYPTION_KG_ALG, DEF_ENCRYPTION_KG_ALG);
    }
    
    public String getEncryptionKGSalt() {
    	return getProperty(KEY_ENCRYPTION_KG_SALT, DEF_ENCRYPTION_KG_SALT);
    }
    
    public String getEncryptionKGSaltEncoding() {
    	return getProperty(KEY_ENCRYPTION_KG_SALT_ENC, DEF_ENCRYPTION_KG_SALT_ENC);
    }
    
    public Map getJavaProperties() {
        return getPropertiesMap(VM_PROPS_PREFIX);
    }
    
    private Map getPropertiesMap(String prefix) {
        Enumeration enu = this.props.keys();
        HashMap map = new HashMap();
        while (enu.hasMoreElements()) {
            String key = (String)enu.nextElement();
            if (key.startsWith(prefix)) {
                map.put(key.substring(prefix.length()), props.get(key));
            }
        }
        return map;
    }
    
    public boolean isDeltaDebugMode() {
        return getProperty(KEY_DELTA_DEBUG, DEF_DELTA_DEBUG);
    }

    public int getFileToolDelay() {
        return getProperty(KEY_FT_DELAY, DEF_FT_DELAY);
    }
    
    public int getDeltaQuickHashModulus() {
        return getProperty(KEY_DELTA_QUICKHASH_MODULUS, DEF_DELTA_QUICKHASH_MODULUS);
    }
    
    public int getDeltaQuickHashMultiplier() {
        return getProperty(KEY_DELTA_QUICKHASH_MULTIPLIER, DEF_DELTA_QUICKHASH_MULTIPLIER);
    }
    
    public int getDeltaHashMapSize() {
        return getProperty(KEY_DELTA_HASHMAP_SIZE, DEF_DELTA_HASHMAP_SIZE);
    }
    
    public boolean isFTPDebugMode() {
        return getProperty(KEY_FTP_DEBUG, DEF_FTP_DEBUG);
    }
    
    public boolean isFTPCacheMode() {
        return getProperty(KEY_FTP_USE_CACHE, DEF_FTP_USE_CACHE);
    }
    
    public boolean isFSCacheDebug() {
        return getProperty(KEY_FS_CACHE_DEBUG, DEF_FS_CACHE_DEBUG);
    }
    
    public boolean isLauncherWaitFor() {
        return getProperty(KEY_LAUNCHER_WAITFOR, DEF_LAUNCHER_WAITFOR);
    }
    
    public long getMaxFilePath() {
        return getProperty(KEY_MAX_FILEPATH_LENGTH, DEF_MAX_FILEPATH_LENGTH);
    }
    
    public int getForceMaxFilePathCheck() {
        return getProperty(KEY_FORCE_FILEPATH_LENGTH_CHECK, DEF_FORCE_FILEPATH_LENGTH_CHECK);
    }
    
    public int getLauncherInitialHeap() {
        return getProperty(KEY_LAUNCHER_IH, DEF_LAUNCHER_IH);
    }
    
    public int getLauncherMaxHeap() {
        return getProperty(KEY_LAUNCHER_MH, DEF_LAUNCHER_MH);
    }
    
    public int getDefaultLogHistory() {
        return getProperty(KEY_DEFAULT_LOG_HISTORY, DEF_DEFAULT_LOG_HISTORY);
    }
    
    public boolean isHashCacheMode() {
        return getProperty(KEY_HASH_USE_CACHE, DEF_HASH_USE_CACHE);
    }
    
    public boolean isZipEntryCheckEnabled() {
        return getProperty(KEY_ZIP_ENTRY_CHECK_ENABLE, DEF_ZIP_ENTRY_CHECK_ENABLE);
    }
    
    public int getZipMvDigits() {
        return getProperty(KEY_ZIP_MV_DIGITS, DEF_ZIP_MV_DIGITS);
    }
    
    public int getHashCacheSize() {
        return getProperty(KEY_HASH_CACHE_SIZE, DEF_HASH_CACHE_SIZE);
    }
    
    public long getFTPNoopDelay() {
        return getProperty(KEY_FTP_NOOP_DELAY, DEF_FTP_NOOP_DELAY);
    }
    
    public int getFTPCacheSize() {
        return getProperty(KEY_FTP_CACHE_SIZE, DEF_FTP_CACHE_SIZE);
    }
    
    public int getLogLevel() {
        return getProperty(KEY_LOG_LEVEL, DEF_LOG_LEVEL);
    }
    
    public int getMaxFTPProxies() {
        return getProperty(KEY_FTP_MAX_PROXIES, DEF_FTP_MAX_PROXIES);
    }
    
    public int getFileToolBufferSize() {
        return getProperty(KEY_FT_BUFFER_SIZE, DEF_FT_BUFFER_SIZE);
    }
    
    public String[] getWritableDirectories() {
        return getProperty(KEY_WRITABLE_DIRECTORIES, DEF_WRITABLE_DIRECTORIES);
    }
    
    public String[] getOSBrowsers() {
        return getProperty(KEY_OS_BROWSERS, DEF_OS_BROWSERS);
    }
    
    public int getZipBufferSize() {
        return getProperty(KEY_ZIP_BUFFER, DEF_ZIP_BUFFER);
    }
    
    public boolean useFileSystemBuffer() {
        return getProperty(KEY_FS_USE_BUFFER, DEF_FS_USE_BUFFER);
    }
    
    public int getFileSystemBufferSize() {
        return getProperty(KEY_FS_BUFFER_SIZE, DEF_FS_BUFFER_SIZE);
    }
    
    public String[] getSSEProtocols() {
        return getProperty(KEY_SSE_PROTOCOLS, DEF_SSE_PROTOCOLS);
    }
    
    protected String getProperty(String key, String defaultValue) {
        String p = props.getProperty(key);
        if (p == null) {
            return defaultValue;
        } else {
            return p;
        }
    }
    
    public int getDeltaLinkedListBufferSize() {
        return getProperty(KEY_DELTA_LINKEDLIST_BUFFER_SIZE, DEF_DELTA_LINKEDLIST_BUFFER_SIZE);
    }
    
    protected boolean getProperty(String key, boolean defaultValue) {
        String p = props.getProperty(key);
        if (p == null) {
            return defaultValue;
        } else {
            return p.equalsIgnoreCase("true");
        }
    }
    
    protected int getProperty(String key, int defaultValue) {
        String p = props.getProperty(key);
        if (p == null) {
            return defaultValue;
        } else {
            return Integer.parseInt(p);
        }
    }
    
    protected long getProperty(String key, long defaultValue) {
        String p = props.getProperty(key);
        if (p == null) {
            return defaultValue;
        } else {
            return Long.parseLong(p);
        }
    }
    
    protected double getProperty(String key, double defaultValue) {
        String p = props.getProperty(key);
        if (p == null) {
            return defaultValue;
        } else {
            return Double.parseDouble(p);
        }
    }
    
    protected String[] getProperty(String key, String[] defaultValue) {
        String p = props.getProperty(key);
        if (p == null) {
            return defaultValue;
        } else {
            List data = new ArrayList();
            
            StringTokenizer stt = new StringTokenizer(p, ",");
            while (stt.hasMoreTokens()) {
                String t = stt.nextToken().trim();
                if (t.length() != 0) {
                    data.add(t);
                }
            }
            
            return (String[])data.toArray(new String[0]);
        }
    }
    
    public String toString() {
        return this.props.toString();
    }
}
