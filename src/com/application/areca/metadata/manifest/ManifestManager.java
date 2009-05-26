package com.application.areca.metadata.manifest;

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
 *
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

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
    private static FileTool tool = FileTool.getInstance();
    public static String ENCODING = "UTF-8";
    
    public static Manifest readManifestForArchive(AbstractFileSystemMedium medium, File archive) throws ApplicationException {
        try {
            File dataDir = medium.getDataDirectory(archive);
            File manifestFile = new File(dataDir, medium.getManifestName());
            if (FileSystemManager.exists(manifestFile)) {
                InputStream is = new GZIPInputStream(FileSystemManager.getFileInputStream(manifestFile));
                String content = tool.getInputStreamContent(is, ENCODING, true);
                return Manifest.decode(content);
            } else {
                return null;
            }
        } catch (IOException e) {
            Logger.defaultLogger().error(e);
            throw new ApplicationException(e);
        }
    }
    
    public static void writeManifest(AbstractFileSystemMedium medium, Manifest mf, File archive) throws ApplicationException {
        // Creation du manifeste
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
