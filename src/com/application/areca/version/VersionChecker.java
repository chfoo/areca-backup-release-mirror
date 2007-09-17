package com.application.areca.version;

import java.net.MalformedURLException;
import java.net.URL;

import com.myJava.util.version.OnlineVersionDataAdapter;
import com.myJava.util.version.VersionData;
import com.myJava.util.version.VersionDataAdapterException;

/**
 * Utility class which checks if new versions of Areca are available.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 3732974506771028333
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
public class VersionChecker {
    
    private static final String DEFAULT_CHECK_URL = "http://areca.sourceforge.net/version.php";
    private static VersionChecker instance = new VersionChecker();
    
    private URL chekUrl;
    
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
        try {
            chekUrl = new URL(DEFAULT_CHECK_URL + "?randomArg=" + Math.random() + "&currentVersion=" + VersionInfos.getLastVersion().getVersionId());
        } catch (MalformedURLException ignored) {
            ignored.printStackTrace();  // The default validation URL has been checked -->OK
        }
    }    
    
    public URL getChekUrl() {
        return chekUrl;
    }
    
    public void setChekUrl(URL chekUrl) {
        this.chekUrl = chekUrl;
    }
    
    /**
     * Checks wether a new version is available.
     * <BR>Returns null if no new version is available.
     * <BR>Returns the new version otherwise. 
     */
    public VersionData checkForNewVersion() throws VersionDataAdapterException {
        OnlineVersionDataAdapter adapter = new OnlineVersionDataAdapter();
        adapter.setCheckUrl(this.chekUrl);
        
        VersionData newVersion = adapter.readVersionData();
        if (newVersion == null) {
            throw new VersionDataAdapterException("Unable to retrieve version informations from url : " + chekUrl.toExternalForm());
        }
        return newVersion;
    }
}
