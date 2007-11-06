package com.myJava.file.archive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.util.Util;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
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
public class ArchiveReader {
    
    private ArchiveAdapter adapter;
    private FileTool tool;
    
    public ArchiveReader(ArchiveAdapter adapter) throws IOException, FileNotFoundException {      
        this.adapter = adapter;
        this.tool = FileTool.getInstance();
    }

    public InputStream getInputStream(String entry, TaskMonitor monitor) 
    throws IOException, TaskCancelledException {
      String entr;
        while((entr = adapter.getNextEntry()) != null) {
            if (monitor != null) {
                monitor.checkTaskCancellation();
            }
            
            if (entr.equals(entry)) {
                return adapter.getArchiveInputStream();
            }
        }

        return null;
    }
    
    public void injectIntoDirectory(File dir) throws IOException {
        try {
            this.injectIntoDirectory(dir, null, null);
        } catch (TaskCancelledException ignored) {
            // This exception is never thrown because no monitor is set. 
        }
    }
    
    public void injectIntoDirectory(File dir, String[] entriesToRecover, TaskMonitor monitor) 
    throws IOException, TaskCancelledException {
        if (entriesToRecover != null) {
            for (int i=0; i<entriesToRecover.length; i++) {
                entriesToRecover[i] = Util.trimSlashes(entriesToRecover[i]);
            }
        }
        
        if (dir == null || (FileSystemManager.exists(dir) && (! FileSystemManager.isDirectory(dir)))) {
            throw new IllegalArgumentException("Invalid directory");
        }
        
        try {
            String fileName;
            while((fileName = adapter.getNextEntry()) != null) {
                try {
                    if (monitor != null) {
                        monitor.checkTaskCancellation();
                    }
                    
                    if (entriesToRecover == null || Util.passFilter(Util.trimSlashes(fileName), entriesToRecover)) {
                        File target = new File(dir, fileName);

	                    if (FileSystemManager.exists(target)) {
	                        FileSystemManager.delete(target);
	                    }

	                    tool.createDir(FileSystemManager.getParentFile(target));
	                    tool.copy(adapter.getArchiveInputStream(), FileSystemManager.getFileOutputStream(target), false, true);    
                    }
                } catch (IOException e) {
                    Logger.defaultLogger().error(e);
                    throw e;
                } catch (RuntimeException e) {
                    Logger.defaultLogger().error(e);
                    throw e;
                } finally {
                    adapter.closeEntry();
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
