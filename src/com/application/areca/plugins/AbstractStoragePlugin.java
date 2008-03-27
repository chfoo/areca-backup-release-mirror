package com.application.areca.plugins;

import java.io.File;

import com.application.areca.ResourceManager;
import com.myJava.object.ToStringHelper;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 7289397627058093710
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
public abstract class AbstractStoragePlugin
implements StoragePlugin {
    protected static final ResourceManager RM = ResourceManager.instance();
    protected File sourceJar;
    protected String id;

    public File getSourceJar() {
        return sourceJar;
    }

    public boolean storageSelectionHelperProvided() {
        return false;
    }

    public void setSourceJar(File sourceJar) {
        this.sourceJar = sourceJar;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getToolTip() {
        return null;
    }
    
    public String getDisplayName() {
        return null;
    }
    
    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("Name", this.getFullName(), sb);
        ToStringHelper.append("Id", this.getId(), sb);
        ToStringHelper.append("Version", this.getVersionData(), sb);
        return ToStringHelper.close(sb);
    }
}
