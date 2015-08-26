package com.application.areca.plugins;


import com.application.areca.adapters.FTPFileSystemPolicyXMLHandler;
import com.application.areca.launcher.gui.FTPStorageSelectionHelper;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.application.areca.version.VersionInfos;
import com.myJava.util.version.VersionData;

/**
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
public class FTPStoragePlugin
extends AbstractStoragePlugin 
implements StoragePlugin {
    public static final String PLG_NAME = "FTP server";
    public static final String PLG_ID = "ftp";

    public FTPStoragePlugin() {
        super();
        this.setId(PLG_ID);
        this.description = "Enables access to FTP or FTPs servers.";
    }

    public String getFullName() {
        return PLG_NAME;
    }

    public String getToolTip() {
        return ResourceManager.instance().getLabel("targetedition.storage.ftp.tt");
    }

    public String getDisplayName() {
        return ResourceManager.instance().getLabel("targetedition.storage.ftp");
    }
    
    public boolean storageSelectionHelperProvided() {
        return true;
    }
    
    public VersionData getVersionData() {
        return VersionInfos.getLastVersion();
    }
    
    public FileSystemPolicyXMLHandler buildFileSystemPolicyXMLHandler() {
        return new FTPFileSystemPolicyXMLHandler();
    }
    
    public StorageSelectionHelper getStorageSelectionHelper() {
        return new FTPStorageSelectionHelper();
    }
}
