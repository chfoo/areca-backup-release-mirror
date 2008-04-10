package com.application.areca.metadata.content;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import com.application.areca.impl.FileSystemRecoveryEntry;
import com.application.areca.metadata.AbstractMetadataAdapter;
import com.myJava.file.FileTool;

/**
 * 
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4765044255727194190
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
public class ArchiveContentAdapter extends AbstractMetadataAdapter {

    public ArchiveContentAdapter(File contentFile) {
        this.file = contentFile;
    }
    
    public void writeEntry(FileSystemRecoveryEntry entry) throws IOException {
        initWriter();
        this.writer.write("\r\n" + ArchiveContent.serialize(entry));
    }
    
    public void writeContent(ArchiveContent contentToWrite) throws IOException {
        Iterator iter = contentToWrite.getContent();
        while (iter.hasNext()) {
            writeEntry((FileSystemRecoveryEntry)iter.next());
        }
    }
    
    /**
     * Parses each line initializes the trace content
     */
    public ArchiveContent readContent() throws IOException {
        FileTool tool = FileTool.getInstance();
        long version = getVersion();
        String encoding = resolveEncoding(version);
        
        String[] str = tool.getInputStreamRows(this.getInputStream(), encoding, true);
        if (str.length >= 1) {
            str[0] = null; // The first line holds version data
        }
        ArchiveContent content = new ArchiveContent();

        content.parse(str, version);
        return content;
    }
}
