
package com.application.areca.launcher.gui.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.Stack;

import com.application.areca.ArecaFileConstants;
import com.application.areca.LogHelper;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.util.log.Logger;

/**
 * This class manages user preferences and load/store them into the home directory of the current user
 * 
 * <BR>
 * @author Stephane BRUNEL
 * <BR>
 *
 */

 /*
 Copyright 2005-2010, Olivier PETRUCCI.

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

public class LocalPreferences implements ArecaFileConstants {

    private static final String HEADER = "Areca user preferences";
    private static LocalPreferences instance = new LocalPreferences();

    private final Properties preferences = new Properties();
    
    private LocalPreferences() {
        load();
    }
    
    public static LocalPreferences instance() {
        return instance;
    }

    public void set(String key, String value) {
        preferences.setProperty(key, value);
    }

    public void set(String key, int value) {
        set(key, String.valueOf(value));
    }
    
    public void set(String key, boolean value) {
        set(key, String.valueOf(value));
    }

    public String get(String key) {
        return get(key, null);
    }
    
    public Stack getStack(String key) {
    	Stack s = new Stack();
    	Iterator iter = preferences.keySet().iterator();
    	ArrayList list = new ArrayList();
    	while (iter.hasNext()) {
    		String k = (String)iter.next();
    		if (k.startsWith(key + ".")) {
    			Integer nb = Integer.decode(k.substring(key.length() + 1));
    			list.add(nb);
    		}
    	}
    	Collections.sort(list);
    	
    	Iterator iter2 = list.iterator();
    	while (iter2.hasNext()) {
    		String k = key + "." + iter2.next();
    		s.push(preferences.get(k));
    	}
    	
    	return s;
    }
    
    public void set(String key, Stack s) {
    	for (int i=0; i<s.size(); i++) {
    		preferences.setProperty(key + "." + i, s.get(i).toString());
    	}
    }

    public String get(String key, String defaultValue) {
        String value = preferences.getProperty(key);
        return value == null ? defaultValue : value;
    }

    public int getInt(String key) {
        try {
            return Integer.parseInt(get(key));
        } catch (Exception ex) {
            return 0;
        }
    }

    public int getInt(String key, int defaultValue) {
        try {
            String value = get(key);
            return value == null ? defaultValue : Integer.parseInt(get(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        try {
            String value = get(key);
            return value == null ? defaultValue : Boolean.valueOf(get(key)).booleanValue();
        } catch (Exception ex) {
            return defaultValue;
        }
    }
    
    public boolean getBoolean(String key) {
        return Boolean.valueOf(get(key)).booleanValue();
    }

    public boolean contains(String key) {
        return preferences.containsKey(key);
    }

    public void save() {
        try {
        	File tg = getFile(false);
        	FileTool.getInstance().createDir(FileSystemManager.getParentFile(tg));

            FileOutputStream fos = new FileOutputStream(tg);
            preferences.store(fos, HEADER);
            fos.close();
        } catch (IOException ioex) {
            Logger.defaultLogger().warn("Error while saving user preferences into file " + FileSystemManager.getAbsolutePath(getFile(false)), ioex, "");
        }
    }

    private File getFile(boolean deprecated) {
    	if (deprecated) {
    		return new File(System.getProperty("user.home"), USER_PREFERENCES_PATH_DEPRECATED);
    	} else {
    		return new File(System.getProperty("user.home"), USER_PREFERENCES_PATH);
    	}
    }

    private void load() {
        File prefFile = this.getFile(false);
        boolean deprecatedFile = false;
        if (! prefFile.exists()) {
        	prefFile = this.getFile(true);
        	deprecatedFile = true;
        }
        try {
            if (prefFile.exists() && prefFile.isFile()) {
	            FileInputStream fis = new FileInputStream(prefFile);
	            preferences.load(fis);
	            fis.close();
	            
	            if (deprecatedFile) {
	            	FileTool.getInstance().delete(prefFile, true);
	            	save();
	            }
            }
        } catch (IOException ioex) {
            Logger.defaultLogger().warn("Error while loading user preferences from file " + FileSystemManager.getAbsolutePath(prefFile), ioex, "");
        }
    }
    
    public void logProperties() {
        LogHelper.logProperties("User preferences :", this.preferences);
    }

    public Properties getPreferences() {
        return preferences;
    }
}
