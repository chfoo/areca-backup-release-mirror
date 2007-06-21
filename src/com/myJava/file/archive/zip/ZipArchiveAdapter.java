package com.myJava.file.archive.zip;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.myJava.file.archive.AbstractArchiveAdapter;
import com.myJava.file.archive.ArchiveAdapter;
import com.myJava.file.archive.MeteredOutputStream;

/**
 * <BR>
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
public class ZipArchiveAdapter 
extends AbstractArchiveAdapter
implements ArchiveAdapter {
    
    private static long ZIP32_ENTRY_SIZE_LIMIT = 4294967295L;
    private static long ZIP32_OVERALL_SIZE_LIMIT = 4294967295L;
    private static long ZIP32_MAX_ENTRIES = 65535L;
    
    public ZipArchiveAdapter(InputStream in, long streamSize) {
        super(ACCESS_READ, streamSize);

        if (streamSize != 0) {
            this.zin = new ZipInputStream(in);
        }
    }
    
    public ZipArchiveAdapter(OutputStream out) {
        super(ACCESS_WRITE, 0);
        
        MeteredOutputStream os = new MeteredOutputStream(new BufferedOutputStream(out));
        os.setMaxSize(ZIP32_OVERALL_SIZE_LIMIT, "Archive too voluminous : Zip32 archives can't grow over " + (long)(ZIP32_OVERALL_SIZE_LIMIT/1024) + " kbytes.");
        this.zout = new ZipOutputStream(os);
        ((ZipOutputStream)zout).setMethod(ZipOutputStream.DEFLATED);
        ((ZipOutputStream)zout).setLevel(9);
    }

    public void addEntry(String entryName, long size) throws IOException {
        if (size > ZIP32_ENTRY_SIZE_LIMIT) {
            throw new IllegalArgumentException(entryName + " is too voluminous (" + (long)(size / 1024) + " kbytes). Zip32 archives can't store files bigger than " + (long)(ZIP32_ENTRY_SIZE_LIMIT / 1024) + " kbytes.");
        }
        
        if (this.entryCount >= ZIP32_MAX_ENTRIES) {
            throw new IllegalArgumentException("Too many files in archive. Zip32 archive format does not allow to store more than " + ZIP32_MAX_ENTRIES + " files.");
        }
        
        super.addEntry(entryName, size);
        
        ZipEntry entry = new ZipEntry(entryName);
        ((ZipOutputStream)zout).putNextEntry(entry);
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
            ((ZipOutputStream)zout).closeEntry();
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
}
