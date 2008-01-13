package com.myJava.file.driver;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.myJava.file.attributes.Attributes;

/**
 * Interface définissant un driver pour système de fichier.
 * <BR>Ce driver redéfinit les opérations de base de la classe File (ainsi que les
 * opérations permettant d'obtenir des flux E/S, etc.), et prend en charge certaines
 * opérations bas niveau (cryptage, compression, par exemple).
 * <BR>
 * <BR>Il n'apporte aucun autre service que ces opérations bas niveau (opérations avancées sur les fichiers,
 * par exemple). Ces opérations restent du ressort des classes utilisatrices.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2367131098465853703
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
public interface FileSystemDriver {
    
    public static short ACCESS_EFFICIENCY_GOOD = 30;
    public static short ACCESS_EFFICIENCY_AVERAGE = 20;
    public static short ACCESS_EFFICIENCY_POOR = 10;
    
    /*
     * Unsupported <code>File</code> methods :
     * - public URI toURI()
     * - public URL toURL() 
     * - public int compareTo(File pathname)
     * - public int compareTo(Object o)
     * - public static File createTempFile(String prefix, String suffix)
     * - public static File createTempFile(String prefix, String suffix, File directory)
     * - public static File[] listRoots();
     */   
    
    /*
     * <code>File</code> class mimic.
     */
    public boolean canRead(File file);
    public boolean canWrite(File file);
    public boolean createNewFile(File file) throws IOException;
    public boolean delete(File file);
    public boolean exists(File file);
    public File getAbsoluteFile(File file);
    public String getAbsolutePath(File file);
    public File getCanonicalFile(File file) throws IOException;
    public String getCanonicalPath(File file) throws IOException;
    public String getName(File file);
    public String getParent(File file);
    public File getParentFile(File file);
    public String getPath(File file);
    public boolean isAbsolute(File file);
    public boolean isDirectory(File file);
    public boolean isFile(File file);
    public boolean isHidden(File file);
    public long lastModified(File file);
    public long length(File file);
    public String[] list(File file);
    public String[] list(File file, FilenameFilter filter);
    public File[] listFiles(File file);
    public File[] listFiles(File file, FileFilter filter);
    public File[] listFiles(File file, FilenameFilter filter);
    public boolean mkdir(File file);
    public boolean mkdirs(File file);
    public boolean renameTo(File source, File dest);
    public boolean setLastModified(File file, long time);
    public boolean setReadOnly(File file);
    public void deleteOnExit(File f);
    
    /**
     * Ouvre un flux d'entrée sur le fichier
     */
    public InputStream getFileInputStream(File file) throws IOException;
    
    /**
     * Ouvre un flux de sortie sur le fichier 
     */
    public OutputStream getFileOutputStream(File file) throws IOException;
    
    /**
     * Returns an OutputStream for the given File
     * <BR>This outputStream will be cached, and written only during call to "flush" 
     */
    public OutputStream getCachedFileOutputStream(File file) throws IOException;
    
    /**
     * Ouvre un flux de sortie sur le fichier 
     */
    public OutputStream getFileOutputStream(File file, boolean append) throws IOException;
    
    /**
     * Indique si les accès directs au fileSystem sont acceptés.
     * <BR>Utile pour savoir si les classes utilisant directement des objets "File"
     * peuvent être utilisées. (Comme par exemple la classe ZipFile)
     */
    public boolean directFileAccessSupported();
    
    /**
     * Returns the file's attributes
     */
    public Attributes getAttributes(File f) throws IOException;
    
    /**
     * Create a Symbolic link
     */
    public boolean createSymbolicLink(File symlink, String realPath) throws IOException;
    
    /**
     * Applies the attributes provided as argument 
     */
    public void applyAttributes(Attributes p, File f) throws IOException;
    
    public boolean supportsLongFileNames();
    
    public void flush() throws IOException;
    
    public void unmount() throws IOException;
    
    /**
     * Tells wether accesses are fast or not
     */
    public short getAccessEfficiency();
    
    /**
     * Returns true if the driver is sensitive to the content.
     * <BR>In other words, if the driver has a chance to fail during initialization if files pre-exist on its storage location
     */
    public boolean isContentSensitive();
    
    /**
     * Returns a global set of file informations (length, lastmodified, ...)
     */
    public FileInformations getInformations(File file);
}