
package com.application.areca.launcher.gui.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.application.areca.LogHelper;
import com.myJava.file.FileSystemManager;
import com.myJava.util.log.Logger;

/**
 * This class manages user preferences and load/store them into the home directory of the current user
 * 
 * <BR>
 * @author Stephane BRUNEL
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

public class LocalPreferences {

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
            FileOutputStream fos = new FileOutputStream(getFile());
            preferences.store(fos, HEADER);
            fos.close();
        } catch (IOException ioex) {
            Logger.defaultLogger().warn("Error while saving user preferences into file " + FileSystemManager.getAbsolutePath(getFile()), ioex, "");
        }
    }

    private File getFile() {
        String userDir = System.getProperty("user.home");
        return new File(userDir, ".areca");
    }

    private void load() {
        try {
            File prefFile = this.getFile();
            if (prefFile.exists()) {
	            FileInputStream fis = new FileInputStream(getFile());
	            preferences.load(fis);
	            fis.close();
                
                // Destroy older preferences
               preferences.remove("lnf");
               preferences.remove("mainframe.rightsplitpos");
               preferences.remove("log.display");

            }
        } catch (IOException ioex) {
            Logger.defaultLogger().warn("Error while loading user preferences from file " + FileSystemManager.getAbsolutePath(getFile()), ioex, "");
        }
    }
    
    public void logProperties() {
        LogHelper.logProperties("User preferences :", this.preferences);
    }

    public Properties getPreferences() {
        return preferences;
    }
}
