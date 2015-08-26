package com.myJava.file.driver.remote;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.util.log.Logger;

/**
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
public class LocalOutputStream extends OutputStream {
    private OutputStream out;
    private File file;
    private File localFile;
    private AbstractRemoteFileSystemDriver driver;
    private boolean closed = false;

	public LocalOutputStream(File file, AbstractRemoteFileSystemDriver driver) throws IOException {
		this.file = file;
		this.driver = driver;
        this.localFile = FileTool.getInstance().generateNewWorkingFile(null, "java", "ftpout", false);
        this.out = FileSystemManager.getFileOutputStream(localFile);
	}
	
	public void close() throws IOException {
    	if (closed) {
    		return;
    	}
    	closed = true;
    	
		try {
			try {
				flush();
			} finally {
				out.close();
			}
		} finally {            
        	driver.removeLocalInputFile(file);
            String remoteFile = driver.translateToRemote(file);
        	
            Logger.defaultLogger().info("Flushing " + FileSystemManager.getDisplayPath(localFile) + " to " + FileSystemManager.getDisplayPath(new File(remoteFile)));

            String owner = driver.buildNewOwnerId("flush");
            AbstractProxy proxy = driver.getAvailableProxy(owner);
            try {
                FileTool.getInstance().copy(
                        FileSystemManager.getFileInputStream(localFile),
                        proxy.getFileOutputStream(remoteFile),
                        true,
                        true
                );
            } finally {
            	try {
            		FileTool.getInstance().delete(localFile);
            	} finally {
                    driver.releaseProxy(proxy, owner);	
            	}
            }
		}
	}
	
	public void flush() throws IOException {
		out.flush();
	}
	
	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
	}
	
	public void write(byte[] b) throws IOException {
		out.write(b);
	}
	
	public void write(int b) throws IOException {
		out.write(b);
	}
}
