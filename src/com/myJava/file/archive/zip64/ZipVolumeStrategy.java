package com.myJava.file.archive.zip64;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.myJava.file.CompressionArguments;
import com.myJava.file.EventOutputStream;
import com.myJava.file.FileSystemManager;
import com.myJava.file.OutputStreamListener;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.file.multivolumes.VolumeStrategy;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2015, Olivier PETRUCCI.

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
    
    protected File file;
    protected int currentVolume = -1;
    protected int nbDigits;
    protected boolean endReached = false;
    protected FileSystemDriver driver;
    protected boolean cached = false;
    protected OutputStreamListener listener;
    protected boolean closed = false;

    public ZipVolumeStrategy(File file, int nbDigits) {
        this(file, null, false, nbDigits);
    }
    
    /**
     * If no explicit driver is set, the strategy will use the filesystemmanager.
     */
    public ZipVolumeStrategy(File file, FileSystemDriver driver, boolean cached, int nbDigits) {
        this.file = file;
        this.driver = driver;
        this.cached = cached;
        this.nbDigits = nbDigits;
    }

    public int getCurrentVolumeNumber() {
        return currentVolume;
    }

    public OutputStreamListener getListener() {
		return this.listener;
	}

	public void setListener(OutputStreamListener listener) {
		this.listener = listener;
	}

	public FileSystemDriver getDriver() {
        return driver;
    }

    public OutputStream getNextOutputStream() throws IOException {
        File f = getNextFile();
        OutputStream ret;
        if (driver == null) {
            ret = cached ? FileSystemManager.getCachedFileOutputStream(f) : FileSystemManager.getFileOutputStream(f);
        } else {
            ret = cached ? driver.getCachedFileOutputStream(f) : driver.getFileOutputStream(f);        
        }
        if (listener == null) {
        	return ret;
        } else {
        	return new EventOutputStream(ret, listener);
        }
    }

    public InputStream getNextInputStream() throws IOException {
        if (endReached) {
            return null;
        } else {
            File f = getNextFile();
            if (exists(f)) {
                //Logger.defaultLogger().info("Opening next zip volume : " + getAbsolutePath(f));
                if (driver == null) {
                    return FileSystemManager.getFileInputStream(f);
                } else {
                    return driver.getFileInputStream(f);            
                }
            } else {
                f = getFinalArchive();
                endReached = true;
                if (exists(f)) {
                    //Logger.defaultLogger().info("Opening next zip volume : " + getAbsolutePath(f));
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
        String suffix = VOLUME_SUFFIX;
        String nb = "" + (vol+1);
        while (nb.length() < nbDigits) {
            nb = "0" + nb;
        }

        if (nb.length() > nbDigits) {
            throw new IllegalStateException("Unable to handle more than " + ((int)Math.pow(10, nbDigits)) + " zip volumes.");
        }
        
        return new File(getParentFile(file), getName(file) + suffix + nb);
    }

    public int getVolumesCount() {
        return currentVolume+1;
    }
    
    public File getFinalArchive() {
        return new File(getParentFile(file), getName(file) + CompressionArguments.ZIP_SUFFIX);
    }
    
    public File getFirstVolume() {
        return getVolume(0);
    }

    public void close() throws IOException {
    	if (closed) {
    		return;
    	}
    	closed = true;
    	
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
