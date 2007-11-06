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

import com.myJava.file.attributes.Attributes;
import com.myJava.file.driver.DefaultFileSystemDriver;
import com.myJava.file.driver.DriverAlreadySetException;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.util.log.Logger;

/**
 * Gestionnaire de système de fichiers.
 * <BR>Ce gestionnaire référence des FileSystemDrivers pour différents points de montage (répertoires) et route les
 * demandes d'accès (réplication des méthodes de la classe File) au driver approprié.
 * <BR>
 * <BR>Par défaut, un DefaultFileSystemDriver est utilisé. 
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 6892146605129115786
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
public class FileSystemManager {

    protected static FileSystemManager instance = new FileSystemManager();
    
    /**
     * Drivers indexés par point de montage
     */
    protected Map drivers = new HashMap();
    
    /**
     * Drivers indexés par point de montage.
     * <BR>Contrairement à la map "drivers", cette map ne contient que les drivers
     * qui ont été explicitement enregistrés par la méthode "registerDriver".
     * <BR>Elle sert à réinitialiser la map "drivers" suite à l'appel à la méthode "unregisterDriver"
     */
    protected Map driversReference = new HashMap();
    
    /**
     * Driver par défaut.
     */
    protected FileSystemDriver defaultDriver = new DefaultFileSystemDriver();
    
    /**
     * Racines du FileSystem
     */
    protected Set roots = new HashSet();
    
    /**
     * Optimisation : ce flag est à "true" si aucun driver spécifique n'a été enregistré.
     * <BR>On évite ainsi des recherches dans la Map des drivers : le driver par défaut
     * est systématiquement retourné.
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
     * Enregistre un driver pour le point de montage spécifié. 
     */
    public synchronized void registerDriver(File mountPoint, FileSystemDriver driver) throws DriverAlreadySetException, IOException {
        FileSystemDriver existing = this.getDriverAtMountPoint(mountPoint);
        if (existing != null && ! existing.equals(driver)) {
            if (driver.isContentSensitive()) {
                File[] files = null;
                try {
                    files = FileSystemManager.listFiles(mountPoint);
                } catch (Exception e) {
                    Logger.defaultLogger().error("An error occured while trying to list existing file during the driver registration. The driver will still be registered but this can result in an unstable state.", e, "FilesystemManager.registerDriver");
                }
                
                if (files != null && files.length != 0) {
                    throw new DriverAlreadySetException("Driver already set for mount point : [" + mountPoint.getAbsolutePath() + "] ; existing driver = [" + existing.getClass().getName() + "]. This mountPoint contains " + files.length + " files or directories. It must be cleared before this operation.");
                } 
            }
            
            // Si un autre driver existait pour ce point de montage, on tente de le supprimer.
            unregisterDriver(mountPoint);
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
        
        // Suppression du driver de la map de référence
        this.driversReference.remove(mountPoint);
        
        // Réinitialisation de la map de drivers
        this.drivers.clear();
        this.drivers.putAll(this.driversReference);
    }

    /**
     * Retourne le driver enregistré pour le point de montage passé en argument.
     * <BR>Il n'y a pas de recherche récursive dans les répertoires parents; la méthode retourne null si aucun driver n'a été enregistré pour ce point de montage. 
     */
    public synchronized FileSystemDriver getDriverAtMountPoint(File mountPoint) {
        return (FileSystemDriver)this.drivers.get(mountPoint);
    }
    
    public synchronized void flush(File file) throws IOException {
        FileSystemDriver driver = getDriver(file);
        driver.flush();
    }
    
    /**
     * Retourne le driver approprié pour le fichier spécifié.
     * <BR>Si aucun driver n'est trouvé, le driver par défaut est retourné. 
     */
    public synchronized FileSystemDriver getDriver(File file) {
        // Si aucun driver n'a été enregistré, on retourne le driver par défaut
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
     * Spécifie le driver par défaut (celui qui est utilisé si aucun driver n'a été enregistré pour un chemin donné).
     */
    public synchronized void setDefaultDriver(FileSystemDriver defaultDriver) {
        this.defaultDriver = defaultDriver;
    }
    
    /**
     * Enregistre le driver sans vérifier qu'aucun driver n'a été préalablement spécifié (à utiliser avec précautions !) 
     */
    private Object registerDriverWithoutCheck(File mountPoint, FileSystemDriver driver) {
        this.hasOnlyDefaultDriver = false;
        return this.drivers.put(mountPoint, driver);
    }
    
    private FileSystemDriver lookupDriver(File file, boolean firstCall) {     
        // Sinon, on recherche le Driver
        Object driver = this.drivers.get(file);
        if (driver == null) {
            if (this.isRoot(file)) {
                return this.defaultDriver;
            } else {
                // Recherche du driver par le répertoire parent
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
    
    /**
     * Tells wether the file is a link or not
     */
    public static boolean isLink(File file) throws IOException {
        if (! file.exists()) {
            return false;
        } else {
            return ! getAbsolutePath(file).equals(getCanonicalPath(file));
        }
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
    
    public static boolean directFileAccessSupported(File file) {
        return getInstance().getDriver(file).directFileAccessSupported();
    }
    
    public static Attributes getAttributes(File file) throws IOException {
        return getInstance().getDriver(file).getAttributes(file);
    }
    
    public static void applyAttributes(Attributes p, File f) throws IOException {
        getInstance().getDriver(f).applyAttributes(p, f);
    }
    
    public static void deleteOnExit(File f) {
        getInstance().getDriver(f).deleteOnExit(f);        
    }
    
    public static boolean isReadable(File file) {
        if (file == null || isDirectory(file)) {
            return false;
        } else {
            String message = null;
            
            FileLock lock = null;
            RandomAccessFile raf = null;
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
                    if (lock != null) {
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
            
            if (lock == null) {
                Logger.defaultLogger().info("The following file is locked by the system : " + FileSystemManager.getAbsolutePath(file));
                if (message != null) {
                    Logger.defaultLogger().info("Cause : " + message);
                }
                return false;
            } else {
                return true;
            }

            /*
            boolean isLocked = false;
            InputStream str = null;
            try {
                str = getFileInputStream(file);
                str.read();
            } catch (Exception e) {
                isLocked = true;	    
                Logger.defaultLogger().info("The following file is locked by the system : " + FileSystemManager.getAbsolutePath(file));
            } finally {
                if (str != null) {
                    try {
                        str.close();
                    } catch (IOException ignored) {
                    }
                }
            }

            return ! isLocked;
            */
        }
    }
}
