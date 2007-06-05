package com.myJava.file.archive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;

/**
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -6307890396762748969
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
public class ArchiveReader {
    
    private ArchiveAdapter adapter;
    private FileTool tool;
    
    public ArchiveReader(ArchiveAdapter adapter) throws IOException, FileNotFoundException {      
        this.adapter = adapter;
        this.tool = new FileTool();
    }

    public InputStream getInputStream(String entry) throws IOException {
      String entr;
        while((entr = adapter.getNextEntry()) != null) {
            if (entr.equals(entry)) {
                return adapter.getArchiveInputStream();
            }
        }

        return null;
    }
    
    public void injectIntoDirectory(File dir) throws IOException {
        this.injectIntoDirectory(dir, null);
    }
    
    public void injectIntoDirectory(File dir, String[] entriesToRecover) throws IOException {
        if (entriesToRecover != null) {
            for (int i=0; i<entriesToRecover.length; i++) {
                entriesToRecover[i] = trimSlashes(entriesToRecover[i]);
            }
        }
        
        if (dir == null || (FileSystemManager.exists(dir) && (! FileSystemManager.isDirectory(dir)))) {
            throw new IllegalArgumentException("Invalid directory");
        }
        
        try {
            String fileName;
            while((fileName = adapter.getNextEntry()) != null) {
                try {
                    if (entriesToRecover == null || this.passFilter(fileName, entriesToRecover)) {
                        File target = new File(dir, fileName);

	                    if (FileSystemManager.exists(target)) {
	                        FileSystemManager.delete(target);
	                    }

	                    tool.createDir(FileSystemManager.getParentFile(target));
	                    tool.copy(adapter.getArchiveInputStream(), FileSystemManager.getFileOutputStream(target), false, true);    
                    }
                } finally {
                    adapter.closeEntry();
                }
            }        
        } finally {
            adapter.close();
        }
    }   
    
    private String trimSlashes(String orig) {
        if (orig == null || orig.length() == 0) {
            return orig;
        } else if (orig.length() == 1) {
            if (orig.charAt(0) == '/') {
                return "";
            } else {
                return orig;
            }
        } else {
	        boolean t0 = orig.charAt(0) == '/';
	        boolean tn = orig.charAt(orig.length() - 1) == '/';
	        if (t0 && tn) {
	            return orig.substring(1, orig.length() - 1);
	        } else if (t0) {
	            return orig.substring(1);
	        } else if (tn) {
	            return orig.substring(0, orig.length() - 1);
	        } else {
	            return orig;
	        }
        }
    }
    
    private boolean passFilter(String entry, String[] filter) {
        String test = trimSlashes(entry);
        for (int i=0; i<filter.length; i++) {
            
            if (filter[i].length() == 0 || test.equals(filter[i]) || test.startsWith(filter[i] + "/")) {
                return true;
            }
        }
        return false;
    }
    
    public void close() throws IOException {
        adapter.close();
    }
}
