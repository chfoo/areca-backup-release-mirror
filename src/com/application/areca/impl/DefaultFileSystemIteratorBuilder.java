package com.application.areca.impl;

import java.io.File;
import java.util.Iterator;

import com.myJava.file.FileSystemManager;
import com.myJava.file.iterator.FileSystemIterator;
import com.myJava.util.log.Logger;

/**
 * 
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
public class DefaultFileSystemIteratorBuilder implements FileSystemIteratorBuilder {

	public FileSystemIterator buildFileSystemIterator(FileSystemTarget target) {
        if (target.getSources().size() != 0) {
            Iterator iter = target.getSources().iterator();
            String[] sourceArray = new String[target.getSources().size()];
            String root = target.getSourceDirectory();
            for (int i=0; i<target.getSources().size(); i++) {
                File source = (File)iter.next();
                Logger.defaultLogger().info("Registering source directory : " + FileSystemManager.getDisplayPath(source));
                sourceArray[i] = FileSystemManager.getAbsolutePath(source).substring(root.length());
            }
            File fRoot = null;
            if (root != null && root.length() != 0) {
            	fRoot = new File(root);
            }
            
        	return new FileSystemIterator(fRoot, sourceArray, ! target.isTrackSymlinks(), target.isFollowSubdirectories(), target.isTrackEmptyDirectories(), true);
        } else {
        	return null;
        }
	}
}
