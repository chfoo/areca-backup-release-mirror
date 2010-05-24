package com.myJava.file.archive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.myJava.file.FileFilterList;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.OutputStreamListener;
import com.myJava.util.Util;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
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
public class ArchiveReader {
    
    private ArchiveAdapter adapter;
    private FileTool tool;
    
    public ArchiveReader(ArchiveAdapter adapter) throws IOException, FileNotFoundException {      
        this.adapter = adapter;
        this.tool = FileTool.getInstance();
    }
    
    public void injectIntoDirectory(File dir) throws IOException {
        try {
            this.injectIntoDirectory(dir, null, null, null);
        } catch (TaskCancelledException ignored) {
            // This exception is never thrown because no monitor is set. 
        }
    }
    
    public void injectIntoDirectory(
    		File dir, 
    		FileFilterList entriesToRecover, 
    		TaskMonitor monitor, 
    		OutputStreamListener listener) 
    throws IOException, TaskCancelledException {
        
        String[] normalizedEntries = null;
        boolean hasDirectories = false; /// Count the remaining entries only if there were only files to recover
        if (entriesToRecover != null) {
        	hasDirectories = entriesToRecover.containsDirectories();
            normalizedEntries = new String[entriesToRecover.size()];
            for (int i=0; i<entriesToRecover.size(); i++) {
                normalizedEntries[i] = Util.trimSlashes(entriesToRecover.get(i));
            }
        }
        
        if (dir == null || (FileSystemManager.exists(dir) && (! FileSystemManager.isDirectory(dir)))) {
            throw new IllegalArgumentException("Invalid directory");
        }
        
        try {
            String fileName;
            long remaining = (hasDirectories || entriesToRecover == null) ? -1 : entriesToRecover.size();
            while((fileName = adapter.getNextEntry()) != null) {
                try {
                    if (remaining == 0) {
                        break;
                    }
                    
                    if (monitor != null) {
                        monitor.checkTaskState();
                    }
                    
                    if (normalizedEntries == null || Util.passFilter(Util.trimSlashes(fileName), normalizedEntries)) {
                        remaining--;
                        
                        File target = new File(dir, fileName);

	                    if (FileSystemManager.exists(target)) {
	                        FileSystemManager.delete(target);
	                    }

	                    tool.createDir(FileSystemManager.getParentFile(target));
	                    tool.copy(adapter.getArchiveInputStream(), FileSystemManager.getFileOutputStream(target, false, listener), false, true, monitor);    
                    }
                    
                    // Close entry only if all was successful
                    adapter.closeEntry();
                } catch (IOException e) {
                    Logger.defaultLogger().error(e);
                    throw e;
                } catch (RuntimeException e) {
                    Logger.defaultLogger().error(e);
                    throw e;
                }
            }        
        } finally {
            adapter.close();
        }
    } 
    
    public void close() throws IOException {
        adapter.close();
    }
}
