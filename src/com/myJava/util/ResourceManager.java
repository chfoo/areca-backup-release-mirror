package com.myJava.util;

import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Utility class for localized applications
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

public class ResourceManager {

	/**
	 * Dictionnaries
	 */
    protected Hashtable resources;

    public ResourceManager() {
        this.resources = new Hashtable();
    }

    /**
     * Return or load the requested dictionnary
     */
    protected ResourceBundle getResource(String name, String language) throws IllegalArgumentException {
        if (language == null || language.length() != 2) {
            throw new IllegalArgumentException("ISO-639 language code mandatory.");
        }
        ResourceBundle res = (ResourceBundle)(resources.get(name + "." + language.toLowerCase()));

        if (res == null) {
            Locale loc = new Locale(language.toLowerCase(), "");
            res = ResourceBundle.getBundle(name, loc);
            this.store(res, name, language);
        }

        return res;
    }
    
    /**
     * Store the dictionnary passed as argument
     */
    protected void store(ResourceBundle resource, String name, String language) {
        this.resources.put(name + "." + language.toLowerCase(), resource);
    }

    /**
     * Return the requested translation
     */
    public String getString(String key, String resourceName, String language) throws IllegalArgumentException {
        if (key == null || key.trim().length() == 0) {
            return "";
        } else {
            return this.getResource(resourceName, language).getString(key);
        }
    }
}