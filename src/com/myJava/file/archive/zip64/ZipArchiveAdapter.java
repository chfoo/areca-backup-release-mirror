package com.myJava.file.archive.zip64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.archive.AbstractArchiveAdapter;
import com.myJava.file.archive.ArchiveAdapter;
import com.myJava.file.archive.ArchiveReader;
import com.myJava.file.archive.ArchiveWriter;
import com.myJava.file.multivolumes.VolumeInputStream;
import com.myJava.file.multivolumes.VolumeStrategy;

/**
 * <BR>
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
public class ZipArchiveAdapter 
extends AbstractArchiveAdapter
implements ArchiveAdapter {

    public ZipArchiveAdapter(InputStream in, long streamSize) {
        super(ACCESS_READ, streamSize);
        if (streamSize != 0) {
            this.zin = new ZipInputStream(in);
        }
    }

    public ZipArchiveAdapter(VolumeStrategy strategy, long streamSize) {
        super(ACCESS_READ, 0);
        if (streamSize != 0) {
            this.zin = new ZipInputStream(new VolumeInputStream(strategy));
        }
    }

    public ZipArchiveAdapter(OutputStream out, boolean useZip64) {
        super(ACCESS_WRITE, 0);
        this.zout = new ZipOutputStream(out, useZip64);
        ((ZipOutputStream)zout).setLevel(9);
    }

    public ZipArchiveAdapter(VolumeStrategy strategy, long volumeSize, boolean useZip64) {
        super(ACCESS_WRITE, 0);
        this.zout = new ZipOutputStream(strategy, volumeSize, useZip64);
        ((ZipOutputStream)zout).setLevel(9);
    }

    public void addEntry(String entryName, long size) throws IOException {       
        super.addEntry(entryName, size);
        ZipEntry entry = new ZipEntry(entryName);
        ((ZipOutputStream)zout).putNextEntry(entry);
    }

    public void close() throws IOException {
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

    public static void main(String[] args) {
        for (int i=295; i<300; i++) {
            check(true, true, i);
        }
        
        for (int i=300; i<305; i++) {
            check(false, true, i);
        }
        
        for (int i=501; i<503; i++) {
            check(false, true, i);
        }
        
        for (int i=622; i<626; i++) {
            check(true, true, i);
        }
    }

    private static void check(boolean z64, boolean mv, int size) {
        try {
            String source = "/home/olivier/Desktop/test";
            String dest = "/home/olivier/Incoming/zip" + (z64 ? 1 : 0) + "_" + (mv ? 1 : 0) + "_" + size + "/";
            String clone = "/home/olivier/Incoming/test_clone" + (z64 ? 1 : 0) + "_" + (mv ? 1 : 0) + "_" + size;
            System.out.println(dest);
            String destinationMV = dest + "archive_mv";
            String destinationFull = dest + "archive_full.zip";    

            ////////////// WRITE /////////////

            FileTool tool = new FileTool();
            tool.delete(new File(clone), true);
            tool.delete(new File(dest), true);
            tool.createDir(new File(dest));

            ArchiveWriter writer;
            if (mv) {
                ZipVolumeStrategy strategy = new ZipVolumeStrategy(new File(destinationMV));
                ZipArchiveAdapter adapter = new ZipArchiveAdapter(strategy, size, z64);
                writer = new ArchiveWriter(adapter);
            } else {
                ZipArchiveAdapter adapter = new ZipArchiveAdapter(new FileOutputStream(destinationFull), z64);
                // ZipArchiveAdapter adapter = new ZipArchiveAdapter(
                //        new VolumeOutputStream(
                //                new ZipVolumeStrategy(new File(destinationFull), new DefaultFileSystemDriver())
                //                , size)
                //        , z64
                //);
                writer = new ArchiveWriter(adapter);
            }

            File[] files = FileSystemManager.listFiles(new File(source));
            for (int i=0; i<files.length; i++) {
                writer.addFile(files[i]);
            }

            writer.close();

            ////////////// READ /////////////
            ArchiveReader reader;
            if (mv) {
                ZipVolumeStrategy strategy = new ZipVolumeStrategy(new File(destinationMV));
                ZipArchiveAdapter adapter = new ZipArchiveAdapter(strategy, 1);
                reader = new ArchiveReader(adapter);
            } else {
                ZipArchiveAdapter adapter = new ZipArchiveAdapter(new FileInputStream(destinationFull), 1);
                //ZipArchiveAdapter adapter = new ZipArchiveAdapter(
                //         new VolumeInputStream(new ZipVolumeStrategy(new File(destinationFull), new DefaultFileSystemDriver()))
                //        , 1
                //);
                reader = new ArchiveReader(adapter);
            }

            reader.injectIntoDirectory(new File(clone));
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
