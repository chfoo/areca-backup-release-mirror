package com.myJava.file.archive;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.util.Utilitaire;

/**
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -4899974077672581254
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
public class ArchiveWriter {

    private ArchiveAdapter adapter;
    private FileTool tool;
    private long entries = 0;
    
    public ArchiveWriter(ArchiveAdapter adapter) throws IOException {
        this.tool = new FileTool();  
        this.adapter = adapter;
    }

    public void addFile(File file, String fullName) throws IOException {
        if (! FileSystemManager.exists(file)) {
            return;
        }
        
        if (FileSystemManager.isFile(file)) {
            if (FileNameUtil.startsWithSeparator(fullName)) {
                fullName = fullName.substring(1);
            }
            
            long length = FileSystemManager.length(file);
            
            this.adapter.addEntry(fullName, length);            
            this.tool.copyFile(file, this.adapter.getArchiveOutputStream(), false);
            this.adapter.closeEntry();
            this.entries++;
        } else {
            File[] children = FileSystemManager.listFiles(file);
            for (int i=0; i<children.length; i++) {
                this.addFile(children[i], Utilitaire.replace(FileSystemManager.getCanonicalPath(children[i]), FileSystemManager.getCanonicalPath(file), fullName));
            }
        }
    }

    public void addFile(File file) throws IOException {
        this.addFile(file, FileSystemManager.getCanonicalPath(file));
    }

    public void addFile(String file, String fullName) throws IOException {
        this.addFile(new File(file), fullName);
    }
    
    public void addFile(String file) throws IOException {
        this.addFile(new File(file));
    }
    
    public OutputStream getOutputStream() {
        return this.adapter.getArchiveOutputStream();
    }

    public void close() throws IOException {
        this.adapter.close();
    }
    
    public boolean isEmpty() {
        return (this.entries == 0);
    }
}