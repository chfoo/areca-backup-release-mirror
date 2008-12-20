package com.myJava.file.metadata.posix.linux;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.myJava.file.FileSystemManager;
import com.myJava.file.metadata.FileMetaData;
import com.myJava.file.metadata.FileMetaDataAccessor;
import com.myJava.file.metadata.FileMetaDataSerializer;
import com.myJava.file.metadata.posix.PosixMetaDataImpl;
import com.myJava.system.OSTool;
import com.myJava.util.log.Logger;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 8785459451506899793
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
public class LinuxMetaDataAccessor implements FileMetaDataAccessor {

	private static final FileMetaDataSerializer SERIALIZER = new LinuxMetaDataSerializer();
	
	public FileMetaData getAttributes(File f) throws IOException {
        PosixMetaDataImpl p = new PosixMetaDataImpl();
        BufferedReader reader = null;
        BufferedReader errorReader = null;
        Process process = null;
        
        try {
            process = Runtime.getRuntime().exec(new String[] {"ls", "-ald1", FileSystemManager.getAbsolutePath(f)});
            process.waitFor();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String str = reader.readLine();

            if (str == null) {
                errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String err = errorReader.readLine();
                throw new IOException("Error during file permission reading for file [" + FileSystemManager.getAbsolutePath(f) + "] : " + err); 
            }
            
            // Permissions
            int perm = 
                100 * readrwx(str.charAt(1), str.charAt(2), str.charAt(3))
                + 10 * readrwx(str.charAt(4), str.charAt(5), str.charAt(6))
                + readrwx(str.charAt(7), str.charAt(8), str.charAt(9));              
            p.setPermissions(perm);
            
            // Size
            int index = 10;
            while (str.charAt(index) == ' ') {
                index++;
            }
            while (str.charAt(index) != ' ') {
                index++;
            }
            while (str.charAt(index) == ' ') {
                index++;
            }
            
            // owner
            int index2 = str.indexOf(" ", index);
            p.setOwner(str.substring(index, index2));
            
            // owner group
            int index3 = str.indexOf(" ", index2 + 1);
            p.setOwnerGroup(str.substring(index2 + 1, index3).trim());
        } catch (InterruptedException e) {
            Logger.defaultLogger().error(e);
            throw new IOException("Unable to read attributes for file : " + FileSystemManager.getAbsolutePath(f));
        } finally {
            // Explicitly close all streams
            
            // IN
            if (reader != null) {
                reader.close();
            } else if (process != null) {
                process.getInputStream().close();
            }
            
            // ERROR
            if (errorReader != null) {
                errorReader.close();
            } else if (process != null) {
                process.getErrorStream().close();
            }
            
            // OUT
            if (process != null) {
                process.getOutputStream().close();
            }
        }

        return p;
	}

	public FileMetaDataSerializer getAttributesSerializer() {
		return SERIALIZER;
	}

	public void setAttributes(File f, FileMetaData attr) throws IOException {
        PosixMetaDataImpl current = (PosixMetaDataImpl)getAttributes(f);
        PosixMetaDataImpl target = (PosixMetaDataImpl)attr;
        
        // Owner / Group
        if (
                (! current.getOwner().equals(target.getOwner()))
                || (! current.getOwnerGroup().equals(target.getOwnerGroup()))
        ) {
            OSTool.execute(new String[] {"chown", target.getOwner() + ":" + target.getOwnerGroup(), FileSystemManager.getAbsolutePath(f)});
        }
        
        if (current.getPermissions() != target.getPermissions()) {
        	OSTool.execute(new String[] {"chmod", "" + target.getPermissions(), FileSystemManager.getAbsolutePath(f)});
        }
	}
	
    private static int readrwx(char r, char w, char x) {
        int ret = 0;
        if (r != '-') {
            ret += 4;
        }
        if (w != '-') {
            ret += 2;
        }
        if (x != '-') {
            ret += 1;
        }
        return ret;
    }
}
