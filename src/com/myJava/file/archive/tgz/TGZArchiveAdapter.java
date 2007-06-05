package com.myJava.file.archive.tgz;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.myJava.file.archive.AbstractArchiveAdapter;
import com.myJava.file.archive.ArchiveAdapter;
import com.myJava.file.archive.MeteredOutputStream;
import com.myJava.file.archive.tar.TarEntry;
import com.myJava.file.archive.tar.TarInputStream;
import com.myJava.file.archive.tar.TarOutputStream;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -6307890396762748969
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
public class TGZArchiveAdapter 
extends AbstractArchiveAdapter
implements ArchiveAdapter {
    
    private static long ZIP32_OVERALL_SIZE_LIMIT = 4294967295L;
    
    public TGZArchiveAdapter(InputStream in, long streamSize) throws IOException {
        super(ACCESS_READ, streamSize);

        if (streamSize != 0) {
            this.zin = new TarInputStream(new GZIPInputStream(in));
        }
    }
    
    public TGZArchiveAdapter(OutputStream out) throws IOException {
        super(ACCESS_WRITE, 0);
        
        MeteredOutputStream os = new MeteredOutputStream(new BufferedOutputStream(out));
        os.setMaxSize(ZIP32_OVERALL_SIZE_LIMIT, "Archive too voluminous : Zip32 archives can't grow over " + (long)(ZIP32_OVERALL_SIZE_LIMIT/1024) + " kbytes.");
        os.setMaxSize(-1, null); //tmp
        
        GZIPOutputStream gzip = new GZIPOutputStream(os);
        this.zout = new TarOutputStream(gzip);
    }

    public void addEntry(String entryName, long size) throws IOException {     
        super.addEntry(entryName, size);    
        TarEntry entry = new TarEntry(entryName);
        entry.setSize(size);
        ((TarOutputStream)zout).putNextEntry(entry);
    }
    
    public void closeEntry() throws IOException {
        if (zout != null) {
            ((TarOutputStream)zout).closeEntry();
        }
    }
    
    public String getNextEntry() throws IOException {
        if (zin == null) {
            return null;
        }
        
        TarEntry entry = ((TarInputStream)zin).getNextEntry();
        if (entry == null) {
            return null;
        } else {
            return entry.getName();
        }
    }
}
