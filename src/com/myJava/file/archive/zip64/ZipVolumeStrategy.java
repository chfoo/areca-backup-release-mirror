package com.myJava.file.archive.zip64;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.FileSystemManager;
import com.myJava.file.multivolumes.VolumeStrategy;
import com.myJava.util.log.Logger;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 5653799526062900358
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

    private static final int ZIP_MV_DIGITS = FrameworkConfiguration.getInstance().getZipMvDigits();
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
        File f = getNextFile();
        Logger.defaultLogger().info("Opening next zip volume : " + FileSystemManager.getAbsolutePath(f));
        return FileSystemManager.getFileOutputStream(f);
    }

    public InputStream getNextInputStream() throws IOException {
        if (endReached) {
            return null;
        } else {
            File f =getNextFile();
            if (FileSystemManager.exists(f)) {
                Logger.defaultLogger().info("Opening next zip volume : " + FileSystemManager.getAbsolutePath(f));
                return FileSystemManager.getFileInputStream(f);
            } else {
                f = getFinalArchive();
                endReached = true;
                if (FileSystemManager.exists(f)) {
                    Logger.defaultLogger().info("Opening next zip volume : " + FileSystemManager.getAbsolutePath(f));
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
        String nb = "" + (vol+1);
        while (nb.length() < ZIP_MV_DIGITS) {
            nb = "0" + nb;
        }

        if (nb.length() > ZIP_MV_DIGITS) {
            throw new IllegalStateException("Unable to handle more than " + ((int)Math.pow(10, ZIP_MV_DIGITS)) + " zip volumes.");
        }
        
        return new File(FileSystemManager.getParentFile(file), FileSystemManager.getName(file) + suffix + nb);
    }

    public int getVolumesCount() {
        return currentVolume+1;
    }
    
    private File getFinalArchive() {
        return new File(FileSystemManager.getParentFile(file), FileSystemManager.getName(file) + LAST_VOLUME_SUFFIX);
    }
    
    public File getFirstVolume() {
        return getVolume(0);
    }

    public void close() throws IOException {
        File lastArchive = getVolume(this.currentVolume);
        File finalArchive = getFinalArchive();
        if (! FileSystemManager.renameTo(lastArchive, finalArchive)) {
            throw new IOException("Unable to finalize zip archive : " + FileSystemManager.getAbsolutePath(lastArchive));
        }
    }
}
