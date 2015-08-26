package com.myJava.file.iterator;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.myJava.file.FileSystemManager;

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
public class SourcesHelper {
    public static String computeSourceRoot(Set sourceDirectories) {
        List paths = new ArrayList();
        int min = Integer.MAX_VALUE;
        if (sourceDirectories.size() > 0) {
            Iterator iter = sourceDirectories.iterator();
            while (iter.hasNext()) {
                File source = (File)iter.next();
                ArrayList path = new ArrayList();
                computeParents(source, path);
                paths.add(path);
                if (path.size() < min) {
                    min = path.size();
                }
            }

            int divergenceIndex = -1;
            for (int token=0; token<min && divergenceIndex == -1; token++) {
                File current = (File)((List)paths.get(0)).get(token);
                String currentStr = FileSystemManager.getAbsolutePath(current);
                for (int s=1; s<sourceDirectories.size() && divergenceIndex == -1; s++) {
                    File other = (File)((List)paths.get(s)).get(token);
                    if (! currentStr.equals(FileSystemManager.getAbsolutePath(other))) {
                        divergenceIndex = token;
                    }
                }
            }

            if (divergenceIndex == 0) {
            	return "";
            } else if (divergenceIndex == -1) {
            	return FileSystemManager.getAbsolutePath((File)sourceDirectories.iterator().next());
            } else {
            	return FileSystemManager.getAbsolutePath((File)((List)paths.get(0)).get(divergenceIndex - 1));
            }
        } else {
        	return "";
        }
    }
    
    public static String computeSourceDirectory(String sourcesRoot) {
        if (sourcesRoot.length() == 0) {
            return sourcesRoot;
        } else {
            File f = new File(sourcesRoot);

            if (FileSystemManager.isFile(f)) {
                return FileSystemManager.getAbsolutePath(FileSystemManager.getParentFile(f));
            } else {
                return sourcesRoot;
            }
        }
    }

    private static void computeParents(File f, List l) {
        l.add(0, f);
        File parent = FileSystemManager.getParentFile(f);
        if (parent != null) {
            computeParents(parent, l);
        }
    }
}
