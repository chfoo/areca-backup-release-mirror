package com.application.areca.plugins;

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
public interface Plugin extends Comparable {
    public String getFullName();
    public String getToolTip();
    public String getDisplayName();
    public String getId();
    public void setId(String id);
    public VersionData getVersionData();
    public void setClassLoader(ClassLoader cl);
    public ClassLoader getClassLoader();
    public void setPath(String path);
    public String getPath();
    public String getDescription();
}
