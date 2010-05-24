package com.myJava.file.iterator;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import com.myJava.file.FileSystemManager;
import com.myJava.object.ToStringHelper;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2010, Olivier PETRUCCI.

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
public class FileSystemLevel {
	private File[] levelFiles;
	private int index;
    private boolean hasBeenReturned = false;
    private FileSystemLevel parent;
    private File root;
    private double completionIncrement = 0;
    private boolean directoryRoot;						// The root can be a file or a directory
    
	public FileSystemLevel(File root, FileSystemLevel parent, boolean sorted) {
		this.directoryRoot = FileSystemManager.isDirectory(root);
		if (directoryRoot) {
			File[] files = FileSystemManager.listFiles(root);

			if (files == null) {
				this.levelFiles = new File[0];
			} else {
				if (sorted) {
					Arrays.sort(files, new FileNameComparator());
				}
				this.levelFiles = files;
			}
		} else {
			this.levelFiles = new File[] {root};
		}
		this.index = 0;
        this.parent = parent;
        this.root = root;
        this.completionIncrement = 0.99 / (getSize() == 0 ? 1 : getSize());
	}

	public double getCompletionIncrement() {
		return completionIncrement;
	}

	public boolean hasMoreElements() {
		return (this.index <= this.levelFiles.length-1);
	}

	public File nextElement() {
		this.index++;
		return this.levelFiles[index-1];
	}

    public FileSystemLevel getParent() {
        return parent;
    }

    public double getProgress() {
        return (double)index / (double)(this.levelFiles.length == 0 ? 1 : this.levelFiles.length);
    }

    public File getRoot() {
		return root;
	}
    
	public boolean isDirectoryRoot() {
		return directoryRoot;
	}

	public boolean isHasBeenReturned() {
        return hasBeenReturned;
    }

    public void setHasBeenReturned(boolean hasBeenReturned) {
        this.hasBeenReturned = hasBeenReturned;
    }

    public int getSize() {
        if (this.levelFiles == null) {
            return 0;
        } else {
            return this.levelFiles.length;
        }
    }
    
	private static class FileNameComparator implements Comparator {
		public int compare(Object arg0, Object arg1) {
			File f0 = (File)arg0;
			File f1 = (File)arg1;
			return f0.getName().compareTo(f1.getName());
		}
	}
	
    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("Root", this.root, sb);
        ToStringHelper.append("Increment", this.completionIncrement, sb);
        return ToStringHelper.close(sb);
    }
}
