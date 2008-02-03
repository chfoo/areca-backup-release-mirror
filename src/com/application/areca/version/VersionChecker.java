package com.application.areca.version;

import java.net.MalformedURLException;
import java.net.URL;

import com.myJava.util.Util;
import com.myJava.util.log.Logger;
import com.myJava.util.version.OnlineVersionDataAdapter;
import com.myJava.util.version.VersionData;
import com.myJava.util.version.VersionDataAdapterException;

/**
 * Utility class which checks if new versions of Areca are available.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 8290826359148479344
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
    
    private static final String DEFAULT_HOST = "http://areca.sourceforge.net";
    private static final String DEFAULT_PAGE = "/version.php";
    private static VersionChecker instance = new VersionChecker();
    
    private String baseHost = DEFAULT_HOST;
    private String page = DEFAULT_PAGE;
    
    /**
     * Returns the single instance 
     */
    public static VersionChecker getInstance() {
        return instance;
    }

    public String getBaseHost() {
        return baseHost;
    }

    public void setBaseHost(String baseHost) {
        this.baseHost = baseHost;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    /**
     * Checks wether a new version is available.
     * <BR>Returns null if no new version is available.
     * <BR>Returns the new version otherwise. 
     * <BR>
     * <BR>- Add a random argument to bypass proxies
     * <BR>- Add the current version ... this allows to adapt the response to Areca's version (warning messages if bugs have been discovered, ...)
     */
    public VersionData checkForNewVersion() throws VersionDataAdapterException {
        URL checkUrl = null;
        try {
            checkUrl = new URL(getBaseHost() + getPage() + "?randomArg=" + Util.getRndLong() + "&currentVersion=" + VersionInfos.getLastVersion().getVersionId());
        } catch (MalformedURLException ignored) {
            ignored.printStackTrace();  // The default validation URL has been checked -->OK
        }
        
        OnlineVersionDataAdapter adapter = new OnlineVersionDataAdapter();
        adapter.setCheckUrl(checkUrl);

        Logger.defaultLogger().info("Opening url : " + checkUrl.toExternalForm());
        VersionData newVersion = adapter.readVersionData();
        if (newVersion == null) {
            Logger.defaultLogger().error("Error : No version information found.");
            throw new VersionDataAdapterException("Unable to retrieve version informations from url : " + checkUrl.toExternalForm());
        } else {
            Logger.defaultLogger().info("Version information retrieved : " + newVersion.getVersionId());
        }
        return newVersion;
    }
}
