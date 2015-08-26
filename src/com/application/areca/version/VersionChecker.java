package com.application.areca.version;

import java.net.MalformedURLException;
import java.net.URL;

import com.application.areca.ArecaURLs;
import com.myJava.util.log.Logger;
import com.myJava.util.version.VersionData;
import com.myJava.util.version.VersionDataAdapterException;
import com.myJava.util.version.VersionDataHandler;
import com.myJava.util.version.XMLVersionDataAdapter;

/**
 * Utility class which checks if new versions of Areca are available.
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
public class VersionChecker {
    private static VersionChecker instance = new VersionChecker();
    
    private String chekUrl;
    
    /**
     * Returns the single instance 
     */
    public static VersionChecker getInstance() {
        return instance;
    }
    
    /**
     * Creates a VersionValidator on the default URL
     * <BR>- Add a random argument to bypass proxies
     * <BR>- Add the current version ... this allows to adapt the response to Areca's version (warning messages if bugs have been discovered, ...)
     */
    private VersionChecker() {
    	this.chekUrl = ArecaURLs.VERSION_URL + "?currentVersion=" + VersionInfos.getLastVersion().getVersionId() + "&randomArg=";
    }  

    public String getCheckHost() {
        URL url = null;
        try {
        	url = new URL(chekUrl);
        } catch (MalformedURLException ignored) {
            ignored.printStackTrace();  // The default validation URL has been checked -->OK
        }
        return url.getHost();
	}

	/**
     * Checks wether a new version is available.
     * <BR>Returns null if no new version is available.
     * <BR>Returns the new version otherwise. 
     */
    public VersionData checkForNewVersion() throws VersionDataAdapterException {
        VersionDataHandler handler = new VersionDataHandler();
        handler.setAdapter(new XMLVersionDataAdapter());
        
        URL url = null;
        try {
        	url = new URL(chekUrl + Math.random());
        } catch (MalformedURLException ignored) {
            ignored.printStackTrace();  // The default validation URL has been checked -->OK
        }

        handler.setCheckUrl(url);

        Logger.defaultLogger().info("Opening url : " + url.toExternalForm());
        VersionData newVersion = handler.readVersionData();
        if (newVersion == null) {
            Logger.defaultLogger().error("Error : No version information found.");
            throw new VersionDataAdapterException("Unable to retrieve version informations from url : " + url.toExternalForm());
        } else {
            Logger.defaultLogger().info("Version information retrieved : " + newVersion.getVersionId());
        }
        return newVersion;
    }
}
