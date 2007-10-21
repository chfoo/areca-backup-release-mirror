package com.myJava.file;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Stack;

import com.myJava.util.log.Logger;


/**
 * Itérateur permettant de lister le contenu d'un répertoire.
 * <BR>Tous les éléments du répertoire (fichiers et sous répertoires) sont retournés par l'itérateur.
 * <BR>Les sous-répertoires sont traités récursivement par l'itérateur.
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
public class FileSystemIterator implements Iterator {

    protected Stack fileSystemLevels;
    protected FileSystemLevel currentLevel;
    protected File baseDirectory;
    protected Object nextCachedObject;
    protected boolean followSymLinks;
    
    public FileSystemIterator(File baseDirectory, boolean followSymLinks) {
        this.baseDirectory = baseDirectory;
        this.fileSystemLevels = new Stack();
        this.currentLevel = new FileSystemLevel(baseDirectory);        
        this.followSymLinks = followSymLinks;
        fetchNext();        
    }

    /**
     * Retourne le prochain élément.
     * Attention : ca peut être un fichier ou un répertoire.
     * Cet élément est filtré par appel aux filtres.
     */
    private File nextFileOrDirectory() {
        if (currentLevel.hasMoreElements()) {
            File f = currentLevel.nextElement();

            try {
                if (FileSystemManager.isDirectory(f) && (followSymLinks || (! FileSystemManager.isLink(f)))) {
                    this.fileSystemLevels.push(this.currentLevel);
                    this.currentLevel = new FileSystemLevel(f);
                }
            } catch (IOException e) {
                Logger.defaultLogger().error("Unreadable file : " + FileSystemManager.getAbsolutePath(f), e);
                throw new IllegalArgumentException("Unreadable file : " + FileSystemManager.getAbsolutePath(f));
            }
            return f;  

        } else {
            if (this.fileSystemLevels.isEmpty()) {
                return null;
            } else {
                this.currentLevel = (FileSystemLevel)this.fileSystemLevels.pop();
                return this.nextFileOrDirectory();
            }
        }
    }
    
    /**
     * Retourne le prochain élément;
     */
    public Object next() {
        Object next = this.nextCachedObject;
        fetchNext();
        
        return next;
    }

    public boolean hasNext() {
        return (this.nextCachedObject != null);
    }
    
    public void remove() {
        throw new UnsupportedOperationException("Not supported by this implementation.");
    }
    
    private void fetchNext() {
        this.nextCachedObject = nextFileOrDirectory();
    }
    
    private class FileSystemLevel {
        private File[] levelFiles;
        private int index;
        
        public FileSystemLevel(File baseDirectory) {
            if (FileSystemManager.isDirectory(baseDirectory)) {
                this.levelFiles = FileSystemManager.listFiles(baseDirectory);
            } else {
                this.levelFiles = new File[] {baseDirectory};
            }
            this.index = 0;
        }
        
        public boolean hasMoreElements() {
            return (this.index <= this.levelFiles.length-1);
        }
        
        public File nextElement() {
            this.index++;
            return this.levelFiles[index-1];
        }
    }    
}
