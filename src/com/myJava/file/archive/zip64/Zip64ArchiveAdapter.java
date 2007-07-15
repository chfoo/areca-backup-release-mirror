package com.myJava.file.archive.zip64;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.myJava.file.FileSystemManager;
import com.myJava.file.archive.AbstractArchiveAdapter;
import com.myJava.file.archive.ArchiveAdapter;
import com.myJava.file.archive.ArchiveReader;
import com.myJava.file.archive.ArchiveWriter;

/**
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
public class Zip64ArchiveAdapter 
extends AbstractArchiveAdapter
implements ArchiveAdapter {
    
    public Zip64ArchiveAdapter(InputStream in, long streamSize) {
        super(ACCESS_READ, streamSize);

        if (streamSize != 0) {
            this.zin = new ZipInputStream(in);
        }
    }
    
    public Zip64ArchiveAdapter(OutputStream out) {
        super(ACCESS_WRITE, 0);
        
        this.zout = new Zip64OutputStream(out);
        ((Zip64OutputStream)zout).setMethod(Zip64OutputStream.DEFLATED);
        ((Zip64OutputStream)zout).setLevel(9);
    }

    public void addEntry(String entryName, long size) throws IOException {       
        super.addEntry(entryName, size);
        ZipEntry entry = new ZipEntry(entryName);
        ((Zip64OutputStream)zout).putNextEntry(entry);
    }
    
    public void close() throws IOException {
        if (zout != null && this.entryCount == 0) {
            this.addMockEntry();
        }
        super.close();
    }
    
    public void closeEntry() throws IOException {
        if (zin != null) {
            ((ZipInputStream)zin).closeEntry();
        }
        if (zout != null) {
            ((Zip64OutputStream)zout).closeEntry();
        }
    }
    
    public String getNextEntry() throws IOException {
        if (zin == null) {
            return null;
        }
        
        ZipEntry entry = ((ZipInputStream)zin).getNextEntry();
        if (entry == null) {
            return null;
        } else {
            return entry.getName();
        }
    }
    
    public static void main(String[] args) {
        try {
            String source = "/home/olivier/Desktop/test";
            String destination = "/home/olivier/Desktop/test64.zip";
            
            FileSystemManager.delete(new File(destination));
            
            Zip64ArchiveAdapter adapter = new Zip64ArchiveAdapter(FileSystemManager.getFileOutputStream(new File(destination)));
            ArchiveWriter writer = new ArchiveWriter(adapter);
            
            File[] files = FileSystemManager.listFiles(new File(source));
            for (int i=0; i<files.length; i++) {
                writer.addFile(files[i]);
            }
            
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
