package com.application.areca.metadata.manifest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
 * <BR>Areca Build ID : -1628055869823963574
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
    public static String ENCODING = "UTF-8";
    
    public static Manifest readManifestForArchive(AbstractFileSystemMedium medium, File archive) throws ApplicationException {
        try {
            if (! medium.checkArchiveCompatibility(archive)) {
                return null;
            }
            
            File dataDir = medium.getDataDirectory(archive);
            File manifestFile = new File(dataDir, medium.getManifestName());
            File oldManifestFile = new File(dataDir, medium.getOldManifestName());
            if (FileSystemManager.exists(manifestFile)) {
                // Newest version
                InputStream is = new GZIPInputStream(FileSystemManager.getFileInputStream(manifestFile));
                String content = tool.getInputStreamContent(is, ENCODING, true);
                return Manifest.decode(content);
            } else if (FileSystemManager.exists(oldManifestFile)) {
                // Older versions
                InputStream is = FileSystemManager.getFileInputStream(oldManifestFile);
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
    
    public static void writeManifest(AbstractFileSystemMedium medium, Manifest mf, File archive) throws ApplicationException {
        // Création du manifeste
        Writer w = null;
        try {
            File metadataDir = medium.getDataDirectory(archive);
            if (! FileSystemManager.exists(metadataDir)) {
                tool.createDir(metadataDir);
            }
            File manifestFile = new File(metadataDir, medium.getManifestName());
            OutputStream os = new GZIPOutputStream(FileSystemManager.getFileOutputStream(manifestFile));
            w = new OutputStreamWriter(os, ENCODING);
            w.write(mf.encode());
        } catch (IOException e) {
            throw new ApplicationException(e);            
        } finally {
            try {
                w.flush();
                w.close();
            } catch (IOException e) {
                Logger.defaultLogger().error(e);
            }
        }
    }
}
