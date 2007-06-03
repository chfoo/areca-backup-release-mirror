package com.application.areca.impl.tools;

import java.io.File;
import java.io.FilenameFilter;
import java.util.GregorianCalendar;

import com.application.areca.ApplicationException;
import com.application.areca.cache.ArchiveManifestCache;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.metadata.manifest.Manifest;
import com.myJava.util.log.Logger;

/**
 * Archive file filter.
 * <BR>Checks that :
 * <BR>- The file is compatible with the medium
 * <BR>- A manifest exists for the given file
 * <BR>- The manifest's date matchs the param dates
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2162742295696737000
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
public class ArchiveNameFilter implements FilenameFilter {
    private GregorianCalendar fromDate;
    private GregorianCalendar toDate;
    private AbstractIncrementalFileSystemMedium medium;
    
    public ArchiveNameFilter(GregorianCalendar fromDate, GregorianCalendar toDate, AbstractIncrementalFileSystemMedium medium) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.medium = medium;
    }
    
    public boolean accept(File dir, String name) {
        File archive = new File(dir, name);
        
        if (! medium.checkArchiveCompatibility(archive)) {
            return false;
        }
        
        try {
            Manifest mf = ArchiveManifestCache.getInstance().getManifest(medium, archive);
            if (mf == null) {
                return false;
            } else if (this.fromDate == null && this.toDate == null) {
                return true;
            } else {
                GregorianCalendar fileDate = mf.getDate();
                if (fileDate == null) {
                    return false;
                } else {
                    return (
                        (fromDate == null || fileDate.after(fromDate))
                        && (toDate == null || ! fileDate.after(toDate))
                    );
                }
            }
        } catch (ApplicationException e) {
            Logger.defaultLogger().error(e);
            return false;
        }
    }
}