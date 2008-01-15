package com.myJava.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.util.Util;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;
import com.myJava.util.taskmonitor.TaskMonitor;


/**
 * Outil dédié à la manipulation de fichiers
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 1926729655347670856
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

public class FileTool {
    
    private static final long DEFAULT_DELETION_DELAY = FrameworkConfiguration.getInstance().getFileToolDelay();
    private static final int BUFFER_SIZE = FrameworkConfiguration.getInstance().getFileToolBufferSize();
    private static final int DELETION_GC_FREQUENCY = (int)(2000 / DEFAULT_DELETION_DELAY);
    private static final int DELETION_MAX_ATTEMPTS = 1000;
    
    private static FileTool instance = new FileTool();
    
    public static FileTool getInstance() {
        return instance;
    }
    
    private FileTool() {
    }
    
    public void copy(File sourceFileOrDirectory, File targetParentDirectory) throws IOException {
        try {
            copy(sourceFileOrDirectory, targetParentDirectory, null);
        } catch (TaskCancelledException ignored) {
            // Never happens since no monitor is set.
        }
    }
    
    /**
     * Copie le fichier ou répertoire source dans le répertoire parent destination.
     */
    public void copy(File sourceFileOrDirectory, File targetParentDirectory, TaskMonitor monitor) 
    throws IOException, TaskCancelledException {
        if (sourceFileOrDirectory == null || targetParentDirectory == null) {
            throw new IllegalArgumentException("Source : " + sourceFileOrDirectory + ", Destination : " + targetParentDirectory);
        }
        
        if (monitor != null) {
            monitor.checkTaskCancellation();
        }
        
        if (FileSystemManager.isFile(sourceFileOrDirectory)) {
        	this.copyFile(sourceFileOrDirectory, targetParentDirectory, monitor);
        } else {
            // Création du répertoire
            File td = new File(targetParentDirectory, FileSystemManager.getName(sourceFileOrDirectory));
            this.createDir(td);
            
            // Copie du contenu de la source dans le répertoire nouvellement créé.
            this.copyDirectoryContent(sourceFileOrDirectory, td, monitor);
        }
    }
    
    /**
     * Copie le fichier sourceFile vers le répertoire targetDirectory.
     * <BR>Le fichier est détruit s'il existe déjà.
     * <BR>
     * @param sourceFile Pointeur sur le fichier à copier
     * @param targetDirectory Répertoire cible. Si ce répertoire n'existe pas, il est créé (récursivement).
     */
    private void copyFile(File sourceFile, File targetDirectory, TaskMonitor monitor) 
    throws IOException, TaskCancelledException {
        copyFile(sourceFile, targetDirectory, FileSystemManager.getName(sourceFile), monitor);
    }
    
    /**
     * Copie le fichier sourceFile vers le répertoire targetDirectory, sous le nom targetShortFileName.
     * <BR>Le fichier est détruit s'il existe déjà.
     * <BR>
     * @param sourceFile Pointeur sur le fichier à copier
     * @param targetDirectory Répertoire cible. Si ce répertoire n'existe pas, il est créé (récursivement).
     */
    public void copyFile(File sourceFile, File targetDirectory, String targetShortFileName, TaskMonitor monitor) 
    throws IOException, TaskCancelledException {
        
        // Vérifications préalables
        if (! FileSystemManager.exists(targetDirectory)) {
            this.createDir(targetDirectory);
        }
        
        // Construction du fileOutputStream
        File tf = new File(targetDirectory, targetShortFileName);
        OutputStream outStream = FileSystemManager.getFileOutputStream(tf);
        
        // Copie
        this.copyFile(sourceFile, outStream, true, monitor);
    }
    
    /**
     * Copie le fichier sourceFile vers le flux outStream.
     * <BR>closeStream détermine si le flux de sortie sera fermé après la copie ou non
     */
    public void copyFile(File sourceFile, OutputStream outStream, boolean closeStream, TaskMonitor monitor) 
    throws IOException, TaskCancelledException  {      
        this.copy(FileSystemManager.getFileInputStream(sourceFile), outStream, true, closeStream, monitor);
    }

    public void copy(InputStream inStream, OutputStream outStream, boolean closeInputStream, boolean closeOutputStream) 
    throws IOException {
        try {
            copy(inStream, outStream, closeInputStream, closeOutputStream, null);
        } catch (TaskCancelledException e) {
            // Ignored
        }
    }
    
    /**
     * Copie le flux inStream vers le flux outStream.
     * <BR>inStream est fermé après copie.
     * <BR>closeStream détermine si le flux de sortie sera fermé après la copie ou non
     */
    public void copy(InputStream inStream, OutputStream outStream, boolean closeInputStream, boolean closeOutputStream, TaskMonitor monitor) 
    throws IOException, TaskCancelledException {

        try {
            byte[] in = new byte[BUFFER_SIZE];
            int nbRead;
            while (true) {
                if (monitor != null) {
                    monitor.checkTaskCancellation();
                }
                nbRead = inStream.read(in);
                if (nbRead == -1) {
                    break;
                }
                outStream.write(in, 0, nbRead);
            }
        } finally {
            try {
                if (closeInputStream) {
                    inStream.close();
                }
            } catch (Exception ignored) {
            } finally {
                try {
                    if (closeOutputStream) {
                        outStream.close();
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }
    
    /**
     * Copie le contenu du répertoire sourceDirectory dans le répertoire targetDirectory.
     * <BR>Exemple :
     * <BR>Si :
     * <BR>- sourceDirectory = c:\toto\sourceDir
     * <BR>- targetDirectory = d:\myDir
     * <BR>
     * <BR>Alors après la copie, le contenu de c:\toto\sourceDir sera copié dans d:\myDir
     * <BR>targetDirectory est créé (récursivement) s'il n'existe pas.
     */
    public void copyDirectoryContent(File sourceDirectory, File targetDirectory, TaskMonitor monitor) throws IOException, TaskCancelledException {
        if (! FileSystemManager.exists(targetDirectory)) {
            this.createDir(targetDirectory);
        }
        
        // Copie du contenu
        File[] files = FileSystemManager.listFiles(sourceDirectory);
        for (int i=0; i<files.length; i++) {
        	this.copy(files[i], targetDirectory, monitor);
        }
    }
    
    /**
     * DEPLACE le contenu du répertoire source vers le répertoire destination
     * (tous les fichiers et sous répertoires de la source sont déplacés
     * dans le répertoire destination).
     * <BR>Les fichiers existants sont écrasés.
     * <BR>Si le booléen "waitForAvailability" est activé, le processus attendra, pour chaque fichier ou répertoire
     * que celui ci soit disponible pour le déplacer (mise en attente du thread).
     * 
     * @param sourceDirectory
     * @param destinationDirectory
     * @throws IOException
     */
    public void moveDirectoryContent(File sourceDirectory, File destinationDirectory, boolean waitForAvailability, TaskMonitor monitor) 
    throws IOException, TaskCancelledException {
        if (! FileSystemManager.exists(destinationDirectory)) {
            this.createDir(destinationDirectory);
        }
        
        // Déplacement du contenu
        File[] files = FileSystemManager.listFiles(sourceDirectory);
        for (int i=0; i<files.length; i++) {
            this.move(files[i], destinationDirectory, waitForAvailability, monitor);
        }
    }
    
    public void move(File sourceFileOrDirectory, File targetParentDirectory, boolean waitForAvailability, TaskMonitor monitor) 
    throws IOException, TaskCancelledException {
        // Création du répertoire d'accueil si nécessaire
        if (! FileSystemManager.exists(targetParentDirectory)) {
            this.createDir(targetParentDirectory);
        }
        
        File destFile = new File(targetParentDirectory, FileSystemManager.getName(sourceFileOrDirectory));
        
        // Déplacement
        if (! FileSystemManager.renameTo(sourceFileOrDirectory, destFile)) {

        	// Si la tentative standard échoue (méthode "renameTo"), on tente une copie, puis suppression
        	this.copy(sourceFileOrDirectory, targetParentDirectory, monitor);
        	this.delete(sourceFileOrDirectory, waitForAvailability, monitor);
        }
    }
    
    /**
     * Supprime le répertoire et tout son contenu, récursivement, ou le fichier s'il s'agit d'un fichier.
     * <BR>Si le booléen "waitForAvailability" est activé, le processus attendra, pour chaque fichier ou répertoire
     * que celui ci soit disponible pour le supprimer (mise en attente du thread).
     * <BR>Une tentative de suppression sera faite toutes les "deletionDelay" millisecondes.
     */
    public void delete(File fileOrDirectory, boolean waitForAvailability, long deletionDelay, TaskMonitor monitor) 
    throws IOException, TaskCancelledException {
        if (monitor != null) {
            monitor.checkTaskCancellation();
        }
        
        if (FileSystemManager.isDirectory(fileOrDirectory)) {
            // Suppression du contenu
            File[] files = FileSystemManager.listFiles(fileOrDirectory);
            for (int i=0; i<files.length; i++) {
                this.delete(files[i], waitForAvailability, deletionDelay, monitor);
            }
        }

        if (waitForAvailability) {
            long retry = 0;
            try {
                while (! FileSystemManager.delete(fileOrDirectory)) {
                    retry++;
                    if (retry == 10 || retry == 100 || retry == 1000) {
                        Logger.defaultLogger().warn("Attempted to delete file (" + FileSystemManager.getAbsolutePath(fileOrDirectory) + ") during " + (retry * deletionDelay) + " ms but it seems to be locked !");
                    }
                    if (retry >= DELETION_MAX_ATTEMPTS) {
                        String[] files = FileSystemManager.list(fileOrDirectory);
                        throw new IOException(
                                "Unable to delete file : " 
                                + FileSystemManager.getAbsolutePath(fileOrDirectory) 
                                + " - isFile=" + FileSystemManager.isFile(fileOrDirectory) 
                                + " - Exists="  + FileSystemManager.exists(fileOrDirectory)
                                + " - Children="  + (files == null ? 0 : files.length)
                                + (files == null || files.length > 0 ? "(" + files[0] + " ...)" : "")
                        );
                    }
                    if (retry%DELETION_GC_FREQUENCY == 0) {
                        //Logger.defaultLogger().warn("File deletion (" + FileSystemManager.getAbsolutePath(fileOrDirectory) + ") : Performing a GC.");
                        System.gc(); // I know it's not very beautiful ... but it seems to be a bug with old file references (even if all streams are closed)
                    }
                    Thread.sleep(deletionDelay);
                }
            } catch (InterruptedException ignored) {
            }
        } else {
            FileSystemManager.delete(fileOrDirectory);
        }
    }
    
    public void delete(File fileOrDirectory, boolean waitForAvailability) 
    throws IOException {
        try {
            delete(fileOrDirectory, waitForAvailability, null);
        } catch (TaskCancelledException ignored) {
            // Never happens since no monitor is set
        }
    }
    
    public void delete(File fileOrDirectory, boolean waitForAvailability, TaskMonitor monitor) 
    throws IOException, TaskCancelledException {
        delete(fileOrDirectory, waitForAvailability, DEFAULT_DELETION_DELAY, monitor);
    }
    
    public void createFile(File destinationFile, String content) throws IOException {
        OutputStream fos = FileSystemManager.getFileOutputStream(destinationFile);
        OutputStreamWriter fw = new OutputStreamWriter(fos);
        fw.write(content);
        fw.flush();
        fw.close();
    }
    
    /**
     * Retourne le contenu intégral du fichier passé en argument sous forme de chaîne de
     * caractères.
     */
    public String getFileContent(File sourceFile) throws IOException {
        InputStream inStream = FileSystemManager.getFileInputStream(sourceFile);
        return getInputStreamContent(inStream, true);
    }
    

    public String getInputStreamContent(InputStream inStream, boolean closeStreamOnExit) throws IOException {
        return getInputStreamContent(inStream, null, closeStreamOnExit);
    }
    
    /**
     * Retourne le contenu intégral du stream passé en argument sous forme de chaîne de
     * caractères.
     */
    public String getInputStreamContent(InputStream inStream, String encoding, boolean closeStreamOnExit) throws IOException {
    	if (inStream == null) {
    		return null;
    	}
        
        int bSize = 65536;
        char[] b = new char[bSize];
    	
        StringBuffer content = new StringBuffer();
        try {
            InputStreamReader reader = encoding == null ? new InputStreamReader(inStream) : new InputStreamReader(inStream, encoding);            
            int read = 0;
            while ((read = reader.read(b)) != -1) {
                content.append(b, 0, read);
            }
        } finally {
            try {
                if (closeStreamOnExit) {
                    inStream.close();
                }
            } catch (Exception ignored) {
            }
        }
        return new String(content);
    }
    
    /**
     * Retourne le contenu du fichier sous forme de tableau de String.
     * <BR>Un String par ligne.
     * <BR>Les espaces superflus sont supprimés et les lignes vides sont ignorées.
     */
    public String[] getFileRows(File sourceFile) throws IOException {
        return getInputStreamRows(FileSystemManager.getFileInputStream(sourceFile), null, true);
    }
    
    /**
     * Retourne le contenu du flux sous forme de tableau de String.
     * <BR>Un String par ligne.
     * <BR>Les espaces superflus sont supprimés et les lignes vides sont ignorées.
     */
    public String[] getInputStreamRows(InputStream inStream, String encoding, boolean closeStreamOnExit) throws IOException {
        if (inStream == null) {
            return null;
        }

        ArrayList v = new ArrayList();
        try {
            BufferedReader reader = new BufferedReader(encoding == null ? new InputStreamReader(inStream) : new InputStreamReader(inStream, encoding));            
            String line = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() != 0) {
                    v.add(line);
                }
            }
        } finally {
            try {
                if (closeStreamOnExit) {
                    inStream.close();
                }
            } catch (Exception ignored) {
            }
        }
        return (String[])v.toArray(new String[v.size()]);
    }
    
    public String getFirstRow(InputStream stream, String encoding) throws IOException {
        BufferedReader reader = null;
        String line;
        try {
            reader = new BufferedReader(new InputStreamReader(stream, encoding));
            line = reader.readLine();
        } finally {
            if (reader != null) {
                reader.close();
            } else if (stream != null) {
                stream.close();
            }
        }
        return line;
    }
    
    /**
     * Remplace toutes les occurences de "searchString" par "newString" dans le fichier spécifié
     */
    public void replaceInFile(File baseFile, String searchString, String newString) throws IOException {
        String content = this.getFileContent(baseFile);
        content = Util.replace(content, searchString, newString);
        OutputStreamWriter fw = null;
        try {
            OutputStream fos = FileSystemManager.getFileOutputStream(FileSystemManager.getAbsolutePath(baseFile));
            fw = new OutputStreamWriter(fos);
            fw.write(content);
            fw.flush();
        } catch (IOException e) {
            throw e;
        } finally {
            fw.close();
        }
    }
    
    /**
     * Retourne "true" si le fichier contient "searchString" ... retourne false sinon.
     */
    public boolean checkContains(File baseFile, String searchString) throws IOException {
        String content = this.getFileContent(baseFile);
        return (content.indexOf(searchString) != -1);
    }
    
    /**
     * Returns true if "parent" contains or equals to "child"
     * @param parent
     * @param child
     * @return
     */
    public boolean isParentOf(File parent, File child) {
        if (child == null || parent == null) {
            return false;
        } else if (FileSystemManager.getAbsoluteFile(parent).equals(FileSystemManager.getAbsoluteFile(child))){
            return true;
        } else {
            return this.isParentOf(parent, FileSystemManager.getParentFile(child)); 
        }
    }
    
    /**
     * Returns the file's or directory's total length.
     */
    public long getSize(File fileOrDirectory) throws FileNotFoundException {
        if (FileSystemManager.isFile(fileOrDirectory)) {
            return FileSystemManager.length(fileOrDirectory);
        } else {
            File[] content = FileSystemManager.listFiles(fileOrDirectory);
            long l = 0;
            
            for (int i=0; i<content.length; i++) {
                l += getSize(content[i]);
            }
            
            return l;
        }
    }
    
    /**
     * Création récursive d'un répertoire
     * 
     * @param directory
     * @throws IOException
     */
    public void createDir(File directory) throws IOException {
        if (directory == null || FileSystemManager.exists(directory)) {
            return;
        } else {
            createDir(FileSystemManager.getParentFile(directory));
            FileSystemManager.mkdir(directory);
        }
    }
}