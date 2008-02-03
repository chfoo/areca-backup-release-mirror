package com.myJava.file.archive.zip64;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.CompressionArguments;
import com.myJava.file.FileSystemManager;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.file.multivolumes.VolumeStrategy;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 8290826359148479344
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
    private static final String DEFAULT_VOLUME_SUFFIX = CompressionArguments.ZIP_EXTENSION.substring(0, 2);
    
    protected int mvDigits = FrameworkConfiguration.getInstance().getZipMvDigits();
    protected File file;
    protected int currentVolume = -1;
    protected boolean endReached = false;
    protected FileSystemDriver driver;
    protected boolean cached = false;
    protected String lastExtension;
    protected String volumeSuffix;

    public ZipVolumeStrategy(File file, String lastExtension) {
        this(file, null, false, lastExtension);
    }
    
    /**
     * If no explicit driver is set, the strategy will use the filesystemmanager.
     */
    public ZipVolumeStrategy(File file, FileSystemDriver driver, boolean cached, String lastExtension) {
        this.file = file;
        this.driver = driver;
        this.cached = cached;
        this.lastExtension = lastExtension;
        
        if (lastExtension.length() >= 2) {
            this.volumeSuffix = lastExtension.substring(0, 2);
        } else {
            this.volumeSuffix = DEFAULT_VOLUME_SUFFIX;
        }
    }

    public int getCurrentVolumeNumber() {
        return currentVolume;
    }

    public FileSystemDriver getDriver() {
        return driver;
    }

    public int getMvDigits() {
        return mvDigits;
    }

    public void setMvDigits(int mv_digits) {
        this.mvDigits = mv_digits;
    }

    public OutputStream getNextOutputStream() throws IOException {
        File f = getNextFile();
        if (driver == null) {
            return cached ? FileSystemManager.getCachedFileOutputStream(f) : FileSystemManager.getFileOutputStream(f);
        } else {
            return cached ? driver.getCachedFileOutputStream(f) : driver.getFileOutputStream(f);        
        }
    }

    public InputStream getNextInputStream() throws IOException {
        if (endReached) {
            return null;
        } else {
            File f =getNextFile();
            if (exists(f)) {
                if (driver == null) {
                    return FileSystemManager.getFileInputStream(f);
                } else {
                    return driver.getFileInputStream(f);            
                }
            } else {
                f = getFinalArchive();
                endReached = true;
                if (exists(f)) {
                    if (driver == null) {
                        return FileSystemManager.getFileInputStream(f);
                    } else {
                        return driver.getFileInputStream(f);            
                    }
                } else {
                    return null;
                }
            }
        }
    }
    
    public File getNextFile() {
        currentVolume++;
        return getVolume(currentVolume);
    }
    
    private File getVolume(int vol) {
        String suffix = volumeSuffix;
        String nb = "" + (vol+1);
        while (nb.length() < mvDigits) {
            nb = "0" + nb;
        }

        if (nb.length() > mvDigits) {
            throw new IllegalStateException("Unable to handle more than " + ((int)Math.pow(10, mvDigits)) + " zip volumes.");
        }
        
        return new File(getParentFile(file), getName(file) + suffix + nb);
    }

    public int getVolumesCount() {
        return currentVolume+1;
    }
    
    public File getFinalArchive() {
        return new File(getParentFile(file), getName(file) + lastExtension);
    }
    
    public File getFirstVolume() {
        return getVolume(0);
    }

    public void close() throws IOException {
        File lastArchive = getVolume(this.currentVolume);
        File finalArchive = getFinalArchive();
        if (exists(lastArchive)) {
            if (exists(finalArchive)) {
                delete(finalArchive);
            }
            
            if (! renameTo(lastArchive, finalArchive)) {
                throw new IOException("Unable to finalize zip archive : " + getAbsolutePath(lastArchive));
            }
        }
    }
    
    private boolean exists(File f) {
        if (driver == null) {
            return FileSystemManager.exists(f);
        } else {
            return driver.exists(f);            
        }
    }
    
    private String getAbsolutePath(File f) {
        if (driver == null) {
            return FileSystemManager.getAbsolutePath(f);
        } else {
            return driver.getAbsolutePath(f);            
        }
    }
    
    private String getName(File f) {
        if (driver == null) {
            return FileSystemManager.getName(f);
        } else {
            return driver.getName(f);            
        }
    }
    
    private boolean renameTo(File f1, File f2) {
        if (driver == null) {
            return FileSystemManager.renameTo(f1, f2);
        } else {
            return driver.renameTo(f1, f2);            
        }
    }
    
    private boolean delete(File f) {
        if (driver == null) {
            return FileSystemManager.delete(f);
        } else {
            return driver.delete(f);            
        }
    }
    
    private File getParentFile(File f) {
        if (driver == null) {
            return FileSystemManager.getParentFile(f);
        } else {
            return driver.getParentFile(f);            
        }
    }
}
