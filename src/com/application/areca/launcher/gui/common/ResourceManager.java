package com.application.areca.launcher.gui.common;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.myJava.util.log.Logger;

/**
 * This resource manager allows accessing to GUI labels and error messages
 * 
 * <BR>
 * @author Stephane BRUNEL
 * <BR>
 * <BR>Areca Build ID : 4945525256658487980
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
public class ResourceManager {
    private ResourceBundle properties = null;
    
    private static ResourceManager instance = null;
    
    private static HashMap instances = new HashMap();

    public static synchronized ResourceManager instance() {
        return instance("resources");
    }
    
    public static synchronized ResourceManager instance(String domain) {
        ResourceManager rm = (ResourceManager)instances.get(domain);
        if (instance == null) {
            instance = new ResourceManager(domain);
        }
        return instance;
    }

    protected ResourceManager(String domain) {
        try {
            properties = ResourceBundle.getBundle(domain, Locale.getDefault());
        } catch (MissingResourceException ex) {
            properties = ResourceBundle.getBundle(domain, Locale.ENGLISH);
        } catch (Exception ex) {
            Logger.defaultLogger().error(ex);
        }
    }

    private String getString(String key, boolean useDefault, String def) {
        try {
	        String str = properties.getString(key);
	        return str;
        } catch (MissingResourceException mrex) {
            if (useDefault) {
                return def;
            }
            return new StringBuffer().append('[').append(key).append(']').toString();
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
