package com.application.areca.metadata.manifest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.application.areca.ApplicationException;
import com.application.areca.impl.AbstractFileSystemMedium;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.util.log.Logger;

/**
 * Reads Manifests from the disk.
 * <BR>
 * @author Olivier PETRUCCI
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
public class ManifestManager {
    private static FileTool tool = new FileTool();
    
    public static Manifest readManifestForArchive(AbstractFileSystemMedium medium, File archive) throws ApplicationException {
        try {
            if (! medium.checkArchiveCompatibility(archive)) {
                return null;
            }
            
            File dataDir = medium.getDataDirectory(archive);
            File manifestFile = new File(dataDir, medium.getManifestName());
            if (FileSystemManager.exists(manifestFile)) {
                InputStream is = FileSystemManager.getFileInputStream(manifestFile);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                tool.copy(is, baos, true, true);
                byte[] content = baos.toByteArray();
                
                return Manifest.decode(new String(content));
            } else {
                return null;
            }
        } catch (IOException e) {
            Logger.defaultLogger().error(e);
            throw new ApplicationException(e);
        }
    }
}
