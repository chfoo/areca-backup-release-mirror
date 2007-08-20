package com.myJava.file.archive.zip64;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.myJava.file.FileSystemDriver;
import com.myJava.file.FileSystemManager;
import com.myJava.file.multivolumes.VolumeStrategy;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4438212685798161280
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
public class ZipVolumeStrategy implements VolumeStrategy {

    private static String VOLUME_SUFFIX = ".z";
    private static String LAST_VOLUME_SUFFIX = ".zip";
    
    protected File file;
    protected int currentVolume = -1;
    protected boolean endReached = false;

    public ZipVolumeStrategy(File file) {
        this.file = file;
    }

    public int getCurrentVolumeNumber() {
        return currentVolume;
    }

    public OutputStream getNextOutputStream() throws IOException {
        return FileSystemManager.getFileOutputStream(getNextFile());
    }

    public InputStream getNextInputStream() throws IOException {
        if (endReached) {
            return null;
        } else {
            File f =getNextFile();
            if (FileSystemManager.exists(f)) {
                return FileSystemManager.getFileInputStream(f);
            } else {
                f = getFinalArchive();
                endReached = true;
                if (FileSystemManager.exists(f)) {
                    return FileSystemManager.getFileInputStream(f);
                } else {
                    return null;
                }
            }
        }
    }
    
    private File getNextFile() {
        currentVolume++;
        return getVolume(currentVolume);
    }
    
    private File getVolume(int vol) {
        String suffix = VOLUME_SUFFIX;
        if (vol+1 < 10) {
            suffix += "0";
        } else if (vol + 1 > 100) {
            throw new IllegalStateException("Unable to handle more than 100 zip volumes.");
        }
        
        File target = new File(FileSystemManager.getParentFile(file), FileSystemManager.getName(file) + suffix + (vol + 1));
        return target;
    }

    public int getVolumesCount() {
        return currentVolume+1;
    }
    
    private File getFinalArchive() {
        return new File(FileSystemManager.getParentFile(file), FileSystemManager.getName(file) + LAST_VOLUME_SUFFIX);
    }

    public void close() throws IOException {
        File lastArchive = getVolume(this.currentVolume);
        File finalArchive = getFinalArchive();
        if (! FileSystemManager.renameTo(lastArchive, finalArchive)) {
            throw new IOException("Unable to finalize zip archive : " + FileSystemManager.getAbsolutePath(lastArchive));
        }
    }
}
