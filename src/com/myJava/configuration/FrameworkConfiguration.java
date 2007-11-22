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
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2156529904998511409
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
    
    private static String KEY_FTP_MAX_PROXIES = "ftp.max.proxies";
    private static String KEY_FTP_DEBUG = "ftp.debug";
    private static String KEY_FT_DELAY = "filetool.delay";
    private static String KEY_FT_BUFFER_SIZE = "filetool.buffer.size";
    private static String KEY_FTP_NOOP_DELAY = "ftp.noop.delay";    
    private static String KEY_FTP_CACHE_SIZE = "ftp.cache.size";    
    private static String KEY_FTP_USE_CACHE = "ftp.use.cache";
    private static String KEY_HASH_CACHE_SIZE = "hash.cache.size";    
    private static String KEY_HASH_USE_CACHE = "hash.use.cache";
    private static String KEY_OS_BROWSERS = "os.browsers";  
    private static String KEY_SSE_PROTOCOLS = "sse.protocols";  
    private static String KEY_ZIP_BUFFER = "zip.buffer.size"; 
    private static String KEY_ZIP_MV_DIGITS = "zip.mv.digits"; 
    private static String KEY_LOG_LEVEL = "log.level";
    private static String KEY_FS_USE_BUFFER = "fs.use.buffer";   
    private static String KEY_FS_BUFFER_SIZE = "fs.buffer.size";   
    private static String KEY_FS_CACHE_DEBUG = "fs.cache.debug";    
    private static String KEY_LAUNCHER_IH = "launcher.initialheap";  
    private static String KEY_LAUNCHER_MH = "launcher.maxheap";   
    private static String KEY_LAUNCHER_WAITFOR = "launcher.waitfor";   
    
    private static int DEF_FTP_MAX_PROXIES = 3;
    private static long DEF_FTP_NOOP_DELAY = 30000;    
    private static boolean DEF_FTP_DEBUG = false;
    private static int DEF_FT_DELAY = 100;
    private static int DEF_FT_BUFFER_SIZE = 65536;
    private static int DEF_FTP_CACHE_SIZE = 100;    
    private static boolean DEF_FTP_USE_CACHE = true;    
    private static int DEF_HASH_CACHE_SIZE = 200;    
    private static boolean DEF_HASH_USE_CACHE = true;    
    private static String[] DEF_OS_BROWSERS = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
    private static String[] DEF_SSE_PROTOCOLS = {"TLS", "SSL"};  
    private static int DEF_ZIP_BUFFER = 2048;
    private static int DEF_ZIP_MV_DIGITS = 2; 
    private static int DEF_LOG_LEVEL = 8;
    private static boolean DEF_FS_USE_BUFFER = true;   
    private static int DEF_FS_BUFFER_SIZE = 65536;   
    private static boolean DEF_FS_CACHE_DEBUG = false; 
    private static int DEF_LAUNCHER_IH = -1;  
    private static int DEF_LAUNCHER_MH = -1;   
    private static boolean DEF_LAUNCHER_WAITFOR = true;  

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
    
    public Map getJavaProperties() {
        return getPropertiesMap(VM_PROPS_PREFIX);
    }
    
    private Map getPropertiesMap(String prefix) {
        Enumeration enum = this.props.keys();
        HashMap map = new HashMap();
        while (enum.hasMoreElements()) {
            String key = (String)enum.nextElement();
            if (key.startsWith(prefix)) {
                map.put(key.substring(prefix.length()), props.get(key));
            }
        }
        return map;
    }

    public int getFileToolDelay() {
        return getProperty(KEY_FT_DELAY, DEF_FT_DELAY);
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
    
    public int getLauncherInitialHeap() {
        return getProperty(KEY_LAUNCHER_IH, DEF_LAUNCHER_IH);
    }
    
    public int getLauncherMaxHeap() {
        return getProperty(KEY_LAUNCHER_MH, DEF_LAUNCHER_MH);
    }
    
    public boolean isHashCacheMode() {
        return getProperty(KEY_HASH_USE_CACHE, DEF_HASH_USE_CACHE);
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
