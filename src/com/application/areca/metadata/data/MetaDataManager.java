package com.application.areca.metadata.data;

import java.io.File;
import java.io.IOException;

import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.myJava.file.FileSystemManager;

/**
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 3274863990151426915
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
public class MetaDataManager {

    public static MetaData getMetaDataForArchive(AbstractIncrementalFileSystemMedium medium, File archive) throws IOException {
        File metadataDir = medium.getDataDirectory(archive);
        File metadataFile = new File(metadataDir, medium.getMetaDataFileName());
        
        if (FileSystemManager.exists(metadataFile)) {
            MetaDataAdapter dataAdapter = new MetaDataAdapter(metadataFile);
            return dataAdapter.readMetaData();
        } else {
            return new MetaData();
        }
    }
}
