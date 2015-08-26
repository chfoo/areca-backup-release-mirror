package com.application.areca.impl.tools;

import java.io.File;
import java.util.Comparator;

import com.application.areca.ApplicationException;
import com.application.areca.cache.ArchiveManifestCache;
import com.application.areca.impl.AbstractFileSystemMedium;
import com.application.areca.metadata.manifest.Manifest;
import com.myJava.util.log.Logger;


/**
 * Comparateur d'archives; classe par date croissante.
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
public class ArchiveComparator implements Comparator {
    
    private AbstractFileSystemMedium medium;
    
    public ArchiveComparator(AbstractFileSystemMedium medium) {
        this.medium = medium;
    }
    
    public int compare(Object o1, Object o2) {
        File f1 = (File)o1;
        File f2 = (File)o2;
        
        Manifest m1 = null;
        Manifest m2 = null;
        try {
            m1 = ArchiveManifestCache.getInstance().getManifest(medium, f1);
            m2 = ArchiveManifestCache.getInstance().getManifest(medium, f2);
        } catch (ApplicationException e) {
            Logger.defaultLogger().error(e);
        }
        
        if (m1 == null && m2 == null) {
            return 0;
        } else if (m1 == null && m2 != null) {
            return -1;
        } else if (m1 != null && m2 == null) {
            return 1;
        } else if (m1.getDate().equals(m2.getDate())) {
            return 0;
        } else if (m1.getDate().before(m2.getDate())) {
            return -1;
        } else {
            return 1;
        }
    }
}