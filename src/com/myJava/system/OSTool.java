package com.myJava.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.FileSystemManager;
import com.myJava.object.ToStringHelper;
import com.myJava.util.log.Logger;

/**
 * Utility class for all system calls
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
public class OSTool {
    private static final long MAX_MEMORY = Runtime.getRuntime().maxMemory();
    private static final long MAX_MEMORY_KB = MAX_MEMORY / 1024;
    private static final long MAX_MEMORY_MB = MAX_MEMORY / 1048576;
    
    private static boolean IS_SYSTEM_WINDOWS;
    private static boolean IS_SYSTEM_MAC;
    private static int[] JAVA_VERSION;
    private static String FORMATTED_JAVA_VERSION;
    
    private static String JAVA_FILE_ENCODING;
    private static int CODEPAGE = -1;
    private static String IANA_FILE_ENCODING;
    private static String USER_HOME;
    private static String USER_NAME;
    private static String TMP_DIR;
    private static String LINE_SEPARATOR = System.getProperty("line.separator");

    private static Charset[] CHARSETS;
    
    static {
        JAVA_FILE_ENCODING = System.getProperty("file.encoding");
        
        if (JAVA_FILE_ENCODING.toLowerCase().startsWith("cp")) {
        	try {
				CODEPAGE = Integer.parseInt(JAVA_FILE_ENCODING.substring(2));
			} catch (NumberFormatException e) {
				Logger.defaultLogger().fine("Non parsable charset : " + JAVA_FILE_ENCODING);
			}
        }
        
        Charset cs = Charset.forName(JAVA_FILE_ENCODING);
        IANA_FILE_ENCODING = cs.name();
        
        String os = System.getProperty("os.name");

        IS_SYSTEM_WINDOWS = (os.toLowerCase().indexOf("windows") != -1);
        IS_SYSTEM_MAC = (os.toLowerCase().indexOf("mac os") != -1);
        
        FORMATTED_JAVA_VERSION = System.getProperty("java.version");
        if (FORMATTED_JAVA_VERSION == null) {
            JAVA_VERSION = new int[0];
        } else {
            StringTokenizer stt = new StringTokenizer(FORMATTED_JAVA_VERSION, "._-,;/\\ ");
            List lst = new ArrayList();
            while (stt.hasMoreTokens()) {
                lst.add(stt.nextToken());
            }
            
            JAVA_VERSION = new int[lst.size()];
            for (int i=0; i<lst.size(); i++) {
                try {
                    JAVA_VERSION[i] = Integer.parseInt((String)lst.get(i));
                } catch (NumberFormatException e) {
                    JAVA_VERSION[i] = 0;
                }
            }
        }
        
        USER_HOME = System.getProperty("user.home");
        USER_NAME = System.getProperty("user.name");
        
        String configuredTmpDir = FrameworkConfiguration.getInstance().getTemporaryDirectory();
        TMP_DIR = configuredTmpDir == null ? System.getProperty("java.io.tmpdir") : configuredTmpDir;
        File f = new File(TMP_DIR, "tmp-" + USER_NAME);
        TMP_DIR = f.getAbsolutePath();
        
        Map map = Charset.availableCharsets();
        Iterator iter = map.values().iterator();
        CHARSETS = new Charset[map.size()];
        for (int i=0; iter.hasNext(); i++) {
            Charset charset = (Charset)iter.next();
            CHARSETS[i] = charset;
        }
        Arrays.sort(CHARSETS);
    }
    
    public static Charset[] getCharsets() {
        return CHARSETS;
    }
    
    public static String getOSDescription() {
        return System.getProperty("os.name") + " - " + System.getProperty("os.version");
    }
    
    public static String getJavaFileEncoding() {
        return JAVA_FILE_ENCODING;
    }
    
    public static int getCodePage() {
    	return CODEPAGE;
    }
    
    public static String getIANAFileEncoding() {
        return IANA_FILE_ENCODING;
    }
    
    public static String getUserLanguage() {
        return System.getProperty("user.language");
    }
    
    public static String getLineSeparator() {
        return LINE_SEPARATOR;
    }
    
    public static String getUserHome() {
        return USER_HOME;
    }
    
    public static String getUserDir() {
        return System.getProperty("user.dir");
    }
    
    public static String getUserName() {
        return USER_NAME;
    }
    
    public static String getTempDirectory() {
        return TMP_DIR;
    }
    
	public static int execute(String[] cmd) throws IOException {
		return execute(cmd, false);
	}
	
	/**
	 * Works only on Windows
	 * @return
	 */
	public static boolean isAdmin() {
		if (! isSystemWindows()) {
			return false;
		}
		
		try {
	        String command = "reg query \"HKU\\S-1-5-19\"";
	        Process p = Runtime.getRuntime().exec(command);
	        p.waitFor();
	        int exitValue = p.exitValue(); 
	
	        if (0 == exitValue) {
	            return true;
	        } else {
	            return false;
	        }
	    } catch (Exception e) {
	    	Logger.defaultLogger().error(e);
	        return false;
	    }
	}
	
	public static boolean is64BitsJVM() {
		String osArch = System.getProperty("os.arch");
		String sunArchDataModel = System.getProperty("sun.arch.data.model");
		if (osArch != null && osArch.trim().length() != 0) {
			return osArch.indexOf("64") != -1;
		} else if (sunArchDataModel != null && sunArchDataModel.trim().length() != 0) {
			return sunArchDataModel.indexOf("64") != -1;
		} else {
			throw new UnsupportedOperationException("No property could be used to determine JVM architecture.");
		}
	}
	
	public static boolean is64BitsOS() {
	    return (System.getenv("ProgramW6432") != null);
	}

	public static int execute(String[] cmd, boolean async) throws IOException {
        Process p = null;
        int ret = 0;
        try {
            p = Runtime.getRuntime().exec(cmd);
            
            if (! async) {
	            ret = p.waitFor();
	            
	            if (ret != 0) {
	            	BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	                String err = errorReader.readLine();
	            	Logger.defaultLogger().warn("Error executing " + ToStringHelper.serialize(cmd) + " : " + err);
	            }
            }
        } catch (InterruptedException e) {
            Logger.defaultLogger().error("Error executing " + ToStringHelper.serialize(cmd), e);
            throw new IOException(e.getMessage());     
        } catch (RuntimeException e) {
            Logger.defaultLogger().error("Error executing " + ToStringHelper.serialize(cmd), e);
            throw e;
        } catch (IOException ioe) {
            Logger.defaultLogger().error("Error executing " + ToStringHelper.serialize(cmd), ioe);
            throw ioe;
        } finally {
            try {
                if (p != null) {
                    try {
                        if (p.getErrorStream() != null) {
                            p.getErrorStream().close();
                        }
                    } finally {
                        try {
                            if (p.getInputStream() != null) {
                                p.getInputStream().close();
                            }
                        } finally {
                            if (p.getOutputStream() != null) {
                                p.getOutputStream().close();
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                Logger.defaultLogger().error("Error closing streams", e);
            } finally {
            	if (p != null && (!async)) {
            		p.destroy();
            	}
            }
        }
        
        return ret;
    }
    
    public static boolean isSystemWindows() {
        return IS_SYSTEM_WINDOWS;
    }
    
    public static boolean isSystemMACOS() {
        return IS_SYSTEM_MAC;
    }
    
    public static long getMaxMemory() {
        return MAX_MEMORY;
    }
    
    public static long getMaxMemoryKB() {
        return MAX_MEMORY_KB;
    }
    
    public static long getMaxMemoryMB() {
        return MAX_MEMORY_MB;
    }
    
    public static long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }
    
    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }
    
    public static long getMemoryUsage() {
        return getTotalMemory() - getFreeMemory();
    }
    
    public static int[] getJavaVersion() {
        return JAVA_VERSION;
    }
    
    public static String getFormattedJavaVersion() {
        return FORMATTED_JAVA_VERSION;
    }
    
    public static String getVMDescription() {
        return System.getProperty("java.runtime.name") + " - " + System.getProperty("java.runtime.version") + " - " + getJavaVendor();
    }
    
    public static boolean isJavaVersionGreaterThanOrEquals(int[] referenceVersion) {
        int maxIndex = Math.min(referenceVersion.length, JAVA_VERSION.length);
        for (int i=0; i<maxIndex; i++) {
            if (referenceVersion[i] < JAVA_VERSION[i]) {
                return true;
            } else if (referenceVersion[i] > JAVA_VERSION[i]) {
                return false;
            }
        }
        return (referenceVersion.length <= JAVA_VERSION.length);
    }
    
    public static String getJavaVendor() {
        String vd = System.getProperty("java.vm.vendor"); 
        if (vd == null || vd.trim().length() == 0) {
            vd = System.getProperty("java.vendor"); 
        }
        
        return vd;
    }
    
    public static String formatJavaVersion(int[] version) {
        String s = "";
        for (int i=0; i<version.length; i++) {
            if (i != 0) {
                s += ".";
            }
            s += version[i];
        }
        return s;
    }
}
