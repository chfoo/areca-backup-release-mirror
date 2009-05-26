package com.myJava.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.driver.DefaultFileSystemDriver;
import com.myJava.file.driver.DriverAlreadySetException;
import com.myJava.file.driver.FileCacheableInformations;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.file.metadata.FileMetaData;
import com.myJava.util.log.Logger;

/**
 * Gestionnaire de systeme de fichiers.
 * <BR>Ce gestionnaire reference des FileSystemDrivers pour differents points de montage (repertoires) et route les
 * demandes d'acces (replication des methodes de la classe File) au driver approprie.
 * <BR>
 * <BR>Par defaut, un DefaultFileSystemDriver est utilise. 
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

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
public class FileSystemManager {
    protected static FileSystemManager instance = new FileSystemManager();
    protected static int MAX_CACHED_MOUNTPOINTS = FrameworkConfiguration.getInstance().getMaxCachedMountPoints();
    
    /**
     * Drivers indexes par point de montage
     */
    protected Map drivers = new HashMap();
    
    /**
     * Drivers indexes par point de montage.
     * <BR>Contrairement a la map "drivers", cette map ne contient que les drivers
     * qui ont ete explicitement enregistres par la methode "registerDriver".
     * <BR>Elle sert a reinitialiser la map "drivers" suite a l'appel a la methode "unregisterDriver"
     */
    protected Map driversReference = new HashMap();
    
    /**
     * Driver par defaut.
     */
    protected FileSystemDriver defaultDriver = new DefaultFileSystemDriver();
    
    /**
     * Racines du FileSystem
     */
    protected Set roots = new HashSet();
    
    /**
     * Optimisation : ce flag est a "true" si aucun driver specifique n'a ete enregistre.
     * <BR>On evite ainsi des recherches dans la Map des drivers : le driver par defaut
     * est systematiquement retourne.
     */
    protected boolean hasOnlyDefaultDriver = true;
    
    public static FileSystemManager getInstance() {
        return instance;
    }
    
    public FileSystemManager() {
        // Init the filesystem roots
        File[] rootArray = File.listRoots();
        for (int i=0; i<rootArray.length; i++) {
            this.roots.add(rootArray[i]);
        }
    }

    /**
     * Enregistre un driver pour le point de montage specifie. 
     */
    public synchronized void registerDriver(File mountPoint, FileSystemDriver driver) throws DriverAlreadySetException, IOException {
    	FileSystemDriver existing = this.getDriverAtMountPoint(mountPoint);
        if (existing != null && ! existing.equals(driver)) {    // The former instance check was replaced by a more standard call to the "equals" method.
        														// This solves the problems that occured when reopening a target that had already been opened.
            if (driver.isContentSensitive()) {
                File[] files = null;
                try {
                    files = FileSystemManager.listFiles(mountPoint);
                } catch (Exception e) {
                    Logger.defaultLogger().error("An error occurred while trying to list existing file during the driver registration. The driver will still be registered but this can result in an unstable state.", e, "FilesystemManager.registerDriver");
                }
                
                if (files != null && files.length != 0) {
                    throw new DriverAlreadySetException("Driver already set for mount point : [" + mountPoint.getAbsolutePath() + "] ; existing driver = [" + existing.getClass().getName() + "]. This mountPoint contains " + files.length + " files or directories. It must be cleared before this operation.");
                } 
            }
            
            // Si un autre driver existait pour ce point de montage, on tente de le supprimer.
            driver.mount();
            unregisterDriver(mountPoint);
        } else {
            driver.mount();
        }
       
        Logger.defaultLogger().info("Registring a new file system driver : Mount Point = " + mountPoint + ", Driver = " + driver);
        registerDriverWithoutCheck(mountPoint, driver);
        this.driversReference.put(mountPoint, driver);
    }
    
    /**
     * Deletes the driver currently registered at this mount point.
     */
    public synchronized void unregisterDriver(File mountPoint) throws IOException {
        FileSystemDriver existing = this.getDriverAtMountPoint(mountPoint);
        Logger.defaultLogger().info("Unregistring file system driver : Mount Point = " + mountPoint + ", Driver = " + existing);
        try {
            existing.unmount();
        } catch (Throwable e) {
            Logger.defaultLogger().error(e);
        }
        
        // Suppression du driver de la map de reference
        this.driversReference.remove(mountPoint);
        
        // Reinitialisation de la map de drivers
        this.initDriverCache();
    }
    
    private void initDriverCache() {
        this.drivers.clear();
        this.drivers.putAll(this.driversReference);
    }

    /**
     * Retourne le driver enregistre pour le point de montage passe en argument.
     * <BR>Il n'y a pas de recherche recursive dans les repertoires parents; la methode retourne null si aucun driver n'a ete enregistre pour ce point de montage. 
     */
    public synchronized FileSystemDriver getDriverAtMountPoint(File mountPoint) {
        return (FileSystemDriver)this.drivers.get(mountPoint);
    }
    
    public synchronized void flush(File file) throws IOException {
        FileSystemDriver driver = getDriver(file);
        driver.flush();
    }
    
    /**
     * Retourne le driver approprie pour le fichier specifie.
     * <BR>Si aucun driver n'est trouve, le driver par defaut est retourne. 
     */
    public synchronized FileSystemDriver getDriver(File file) {
        // Si aucun driver n'a ete enregistre, on retourne le driver par defaut
        if (this.hasOnlyDefaultDriver) {
            return this.defaultDriver;
        }
        
        // Sinon, on recherche le Driver
        return lookupDriver(file, true);
    }

    public synchronized FileSystemDriver getDefaultDriver() {
        return defaultDriver;
    }
    
    /**
     * Specifie le driver par defaut (celui qui est utilise si aucun driver n'a ete enregistre pour un chemin donne).
     */
    public synchronized void setDefaultDriver(FileSystemDriver defaultDriver) {
        this.defaultDriver = defaultDriver;
    }
    
    /**
     * Enregistre le driver sans verifier qu'aucun driver n'a ete prealablement specifie (a utiliser avec precautions !) 
     */
    private Object registerDriverWithoutCheck(File mountPoint, FileSystemDriver driver) {
        this.hasOnlyDefaultDriver = false;
        if (this.drivers.size() >= MAX_CACHED_MOUNTPOINTS) {
        	// Max size reached -> destroy all
            this.initDriverCache();
        }
        return this.drivers.put(mountPoint, driver);
    }
    
    private FileSystemDriver lookupDriver(File file, boolean firstCall) {     
        // Sinon, on recherche le Driver
        Object driver = this.drivers.get(file);
        if (driver == null) {
            if (this.isRoot(file)) {
                return this.defaultDriver;
            } else {
                // Recherche du driver par le repertoire parent
                File parent = file.getParentFile();
                FileSystemDriver returned = this.lookupDriver(parent, false);

                if (! firstCall) {
                    this.registerDriverWithoutCheck(file, returned);
                }
               
                // On retourne le driver
                return returned;
            }
        } else {
            return (FileSystemDriver)driver;
        }
    }
    
    public boolean isRoot(File file) {
        return this.roots.contains(file) || file.getParentFile() == null;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FILE class mimic
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public static boolean isFile(File file) {
        return getInstance().getDriver(file).isFile(file);
    }
    
    public static boolean isDirectory(File file) {
        return getInstance().getDriver(file).isDirectory(file);
    }
    
    public static long length(File file) {
        return getInstance().getDriver(file).length(file);
    }
    
    public static boolean delete(File file) {
        return getInstance().getDriver(file).delete(file);
    }
    
    public static boolean renameTo(File sourceFile, File destinationFile) {
        FileSystemDriver sourceDriver = getInstance().getDriver(sourceFile);
        FileSystemDriver destinationDriver = getInstance().getDriver(destinationFile);
        
        if (sourceDriver.equals(destinationDriver)) {
            return getInstance().getDriver(sourceFile).renameTo(sourceFile, destinationFile);            
        } else {
            return false;
        }
    }
    
    public static boolean exists(File file) {
        return getInstance().getDriver(file).exists(file);
    }
    
    public static boolean canRead(File file) {
        return getInstance().getDriver(file).canRead(file);
    }
    
    public static boolean canWrite(File file) {
        return getInstance().getDriver(file).canWrite(file);
    }
    
    public static boolean createNewFile(File file) throws IOException {
        return getInstance().getDriver(file).createNewFile(file);
    }
    
    public static boolean isAbsolute(File file) throws IOException {
        return getInstance().getDriver(file).isAbsolute(file);
    }
    
    public static boolean isHidden(File file) throws IOException {
        return getInstance().getDriver(file).isHidden(file);
    }    
    
    public static boolean mkdir(File file) throws IOException {
        return getInstance().getDriver(file).mkdir(file);
    }      

    public static boolean createSymbolicLink(File symlink, String realPath) throws IOException {
        return getInstance().getDriver(symlink).createSymbolicLink(symlink, realPath);
    }
    
    public static boolean createNamedPipe(File pipe) throws IOException {
        return getInstance().getDriver(pipe).createNamedPipe(pipe);
    }
    
    /**
     * Return the type of the file
     * <BR>See types listed in FileMetaDataAccessor 
     */
    public static short getType(File file) throws IOException {
        return getInstance().getDriver(file).getType(file);
    }
    
    public static boolean mkdirs(File file) throws IOException {
        return getInstance().getDriver(file).mkdirs(file);
    }      
    
    public static boolean setLastModified(File file, long time) {
        return getInstance().getDriver(file).setLastModified(file, time);
    }
    
    public static boolean setReadOnly(File file) {
        return getInstance().getDriver(file).setReadOnly(file);
    }
    
    public static File getAbsoluteFile(File file) {
        return getInstance().getDriver(file).getAbsoluteFile(file);
    }
    
    public static File getCanonicalFile(File file) throws IOException {
        return getInstance().getDriver(file).getCanonicalFile(file);
    }
    
    public static String getName(File file) {
        return getInstance().getDriver(file).getName(file);
    }
    
    public static File getParentFile(File file) {
        return getInstance().getDriver(file).getParentFile(file);
    }
    
    public static String getParent(File file) {
        return getInstance().getDriver(file).getParent(file);
    }
    
    public static String getPath(File file) {
        return getInstance().getDriver(file).getPath(file);
    }

    public static String[] list(File file) {
        return getInstance().getDriver(file).list(file);
    }
    
    public static short getAccessEfficiency(File file) {
        return getInstance().getDriver(file).getAccessEfficiency();
    }
    
    public static String[] list(File file, FilenameFilter filter) {
        return getInstance().getDriver(file).list(file, filter);
    }
    
    public static File[] listFiles(File file, FileFilter filter) {
        return getInstance().getDriver(file).listFiles(file, filter);
    }
    
    public static String getAbsolutePath(File file) {
        return getInstance().getDriver(file).getAbsolutePath(file);
    }
    
    public static String getCanonicalPath(File file) throws IOException {
        return getInstance().getDriver(file).getCanonicalPath(file);
    }
    
    public static long lastModified(File file) {
        return getInstance().getDriver(file).lastModified(file);
    }
    
    public static File[] listFiles(File file) {
        return getInstance().getDriver(file).listFiles(file);
    }
    
    public static File[] listFiles(File file, FilenameFilter filter) {
        return getInstance().getDriver(file).listFiles(file, filter);
    }
    
    public static InputStream getFileInputStream(File file) throws IOException {
        return getInstance().getDriver(file).getFileInputStream(file);
    }
    
    public static OutputStream getCachedFileOutputStream(File file) throws IOException {
        return getInstance().getDriver(file).getCachedFileOutputStream(file);
    }  
    
    public static OutputStream getFileOutputStream(File file) throws IOException {
        return getInstance().getDriver(file).getFileOutputStream(file);
    } 
    
    public static OutputStream getFileOutputStream(File file, boolean append, OutputStreamListener listener) throws IOException {
        return getInstance().getDriver(file).getFileOutputStream(file, append, listener);
    }  
    
    public static OutputStream getFileOutputStream(File file, boolean append) throws IOException {
        return getInstance().getDriver(file).getFileOutputStream(file, append);
    }    
    
    public static OutputStream getFileOutputStream(String file) throws IOException {
        return getFileOutputStream(new File(file));
    } 
    
    public static InputStreamReader getReader(File file) throws IOException {
        return new InputStreamReader(getFileInputStream(file));
    }
    
    public static OutputStreamWriter getWriter(File file) throws IOException {
        return new OutputStreamWriter(getFileOutputStream(file));
    }    
    
    public static OutputStreamWriter getWriter(File file, boolean append) throws IOException {
        return new OutputStreamWriter(getFileOutputStream(file, append));
    }   
    
    public static OutputStreamWriter getWriter(String file, boolean append) throws IOException {
        return new OutputStreamWriter(getFileOutputStream(new File(file), append));
    }   
    
    public static InputStream getCachedFileInputStream(File file) throws IOException {
        return getInstance().getDriver(file).getCachedFileInputStream(file);
    }
    
    public static FileCacheableInformations getInformations(File file) {
        return getInstance().getDriver(file).getInformations(file);
    }
    
    public static FileMetaData getMetaData(File file, boolean onlyBasicAttributes) throws IOException {
        return getInstance().getDriver(file).getMetaData(file, onlyBasicAttributes);
    }
    
    public static void applyMetaData(FileMetaData p, File f) throws IOException {
        getInstance().getDriver(f).applyMetaData(p, f);
    }
    
    public static void deleteOnExit(File f) {
        getInstance().getDriver(f).deleteOnExit(f);        
    }
    
    public static ReadableCheckResult isReadable(File file) {
    	ReadableCheckResult ret = new ReadableCheckResult();
        if (file == null || isDirectory(file)) {
            ret.setReadable(false);
        } else {
            String message = null;
            FileLock lock = null;
            RandomAccessFile raf = null;
            
            synchronized (FileSystemManager.class) {
	            try {
	                raf = new RandomAccessFile(file, "r");
	            } catch (Throwable e) {
	                message = e.getMessage();
	            }
	            
	            if (raf != null) {
	                FileChannel chn = raf.getChannel();
	                try {
	                    lock = chn.tryLock(0L, Long.MAX_VALUE, true);
	                } catch (Throwable e) {
	                    message = e.getMessage();             
	                } finally {
	                    if (lock != null && lock.isValid()) {
	                        try {
	                            lock.release();
	                        } catch (IOException ignored) {
	                        }
	                    }
	
	                    try {
	                        raf.close();
	                    } catch (IOException ignored) {
	                    }
	                }
	            }
            }
            
            if (lock == null) {
                ret.setReadable(false);
                ret.setCause(message);
            } else {
                ret.setReadable(true);
            }
        }
        
        return ret;
    }
}
