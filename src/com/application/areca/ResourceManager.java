package com.application.areca;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.myJava.util.log.Logger;

/**
 * This resource manager allows accessing to GUI labels and error messages
 * 
 * <BR>
 * @author Stephane BRUNEL
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

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
public class ResourceManager {
    public static final String RESOURCE_NAME = "resources";
    public static final String RESOURCE_NAME_DEPRECATED_SUFFIX = "_d";
    private static ResourceManager instance = new ResourceManager(RESOURCE_NAME, RESOURCE_NAME_DEPRECATED_SUFFIX);
    
    private ResourceBundle properties = null;
    private ResourceBundle defaultProperties = null;

    public static ResourceManager instance() {
        return instance;
    }

    protected ResourceManager(String domain, String deprecatedSuffix) {
        try {
            properties = ResourceBundle.getBundle(domain, Locale.getDefault());
        } catch (MissingResourceException ex) {
        	try {
                properties = ResourceBundle.getBundle(domain + deprecatedSuffix, Locale.getDefault());
            } catch (MissingResourceException ex2) {
            	properties = ResourceBundle.getBundle(domain, Locale.ENGLISH);
            }
        } catch (Exception ex) {
            Logger.defaultLogger().error(ex);
        } finally {
            try {
                defaultProperties = ResourceBundle.getBundle(domain, Locale.ENGLISH);
            } catch (Exception ex) {
                Logger.defaultLogger().error(ex);
            } 
        }
    }

    private String getString(String key, boolean useDefault, String def) {
        try {
            // Attempt to find the value
            return properties.getString(key);
        } catch (MissingResourceException mrex) {
            // Value not found
            if (useDefault) {
                // Use default value
                return def;
            } else {
                // Attempt to find it in the default resource file
                try {
                    return defaultProperties.getString(key);
                } catch (MissingResourceException defMrex) {
                    // Value not found in default property file -> return dummy value
                    return new StringBuffer().append('[').append(key).append(']').toString();
                }
            }
        }
    }
    
    public String removeDots(String input) {
    	String w = input.trim();
    	if (w.endsWith(":")) {
    		return w.substring(0, w.length() - 1).trim();
    	} else {
    		return input;
    	}
    }

    public String getLabel(String key) {
        return getString(key, false, null);
    }    

    public String getLabel(String key, String def) {
        return getString(key, true, def);
    }
    
    public char getChar(String key) {
        String str = getString(key, true, null);
        if (str != null && str.length() > 0) {
            return str.charAt(0);
        }
        return (char)0;
    }
    
    public String getLabel(String key, Object[] args) {
        String unformatted = getString(key, false, null).replaceAll("'", "''");
        
        MessageFormat fmt = new MessageFormat(unformatted);
        return fmt.format(args, new StringBuffer(), null).toString();
    }
}
