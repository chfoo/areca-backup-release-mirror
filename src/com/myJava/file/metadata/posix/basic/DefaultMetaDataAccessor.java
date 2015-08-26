package com.myJava.file.metadata.posix.basic;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.FileSystemManager;
import com.myJava.file.metadata.FileMetaData;
import com.myJava.file.metadata.FileMetaDataAccessor;
import com.myJava.file.metadata.FileMetaDataSerializer;
import com.myJava.file.metadata.posix.PosixMetaDataImpl;
import com.myJava.file.metadata.posix.PosixMetaDataSerializer;
import com.myJava.system.OSTool;
import com.myJava.util.log.Logger;

/**
 * Default metadata accessor.
 * <BR>It uses standard posix commands to read / write file metadata (permissions, owner and group only) so it is more robust
 * (but also less performant) than com.myJava.file.metadata.posix.jni.JNIMetaDataAccessor
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
public class DefaultMetaDataAccessor implements FileMetaDataAccessor {

	private static final String DESCRIPTION = "Default meta data accessor for Posix systems. It uses the \"ls\", \"chmod\" and \"chown\" system commands to handle file attributes (owner, group and permissions).\nExtended attributes, ACL and special bits are not handled by this accessor.";
	private static final FileMetaDataSerializer SERIALIZER = new PosixMetaDataSerializer();
	private static final String LS_ARGS = FrameworkConfiguration.getInstance().getPosixMetadataAccessorArgs();
	private static final String LS_CMD = FrameworkConfiguration.getInstance().getPosixMetadataAccessorCommand();
	
	public FileMetaData getMetaData(File f, boolean onlyBasicAttributes) throws IOException {
        PosixMetaDataImpl p = new PosixMetaDataImpl();
        BufferedReader reader = null;
        BufferedReader errorReader = null;
        Process process = null;
        String str = null;
        
        try {
            process = Runtime.getRuntime().exec(new String[] {LS_CMD, LS_ARGS, FileSystemManager.getAbsolutePath(f)});
            process.waitFor();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            str = reader.readLine();

            if (str == null) {
                errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String err = errorReader.readLine();
                throw new IOException("Error during file permission reading for file [" + FileSystemManager.getDisplayPath(f) + "] : " + err); 
            }
            
            // Permissions
            int perm = 
                readrwx(str.charAt(1), str.charAt(2), str.charAt(3), 64)	// user
                + readrwx(str.charAt(4), str.charAt(5), str.charAt(6), 8)	// group
                + readrwx(str.charAt(7), str.charAt(8), str.charAt(9), 1);  // other            
            p.setMode(perm);
            
            // Size
            int index = 9;
            while (str.charAt(index) != ' ') {
                index++;
            }
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
            p.setGroup(str.substring(index2 + 1, index3).trim());
            
            // Date
            p.setLastmodified(f.lastModified());
        } catch (Exception e) {
            Logger.defaultLogger().error(e);
            throw new IOException("Unable to parse attributes for file : " + FileSystemManager.getDisplayPath(f) + " (" + str + ")");
        } finally {
            str = null;
            
            // Explicitly close all streams
            if (reader != null) {
                reader.close();
                reader = null;
            } 
            if (errorReader != null) {
                errorReader.close();
                errorReader = null;
            }
            
            if (process != null) {
                process.getInputStream().close();
                process.getErrorStream().close();
                process.getOutputStream().close();
                
                // Make sure that the process is destroyed ... closing the streams doesn't seem to be enough on some VM implementations (?!)
                process.destroy();
                process = null;
            }
        }

        return p;
	}
	
	public FileMetaData buildEmptyMetaData() {
		return new PosixMetaDataImpl();
	}

	public FileMetaDataSerializer getMetaDataSerializer() {
		return SERIALIZER;
	}

	public void setMetaData(File f, FileMetaData attr) throws IOException {
        PosixMetaDataImpl target = (PosixMetaDataImpl)attr;
        
        if (target.getOwner() != null || target.getGroup() != null || target.getMode() != PosixMetaDataImpl.UNDEF_MODE) {
	        PosixMetaDataImpl current = (PosixMetaDataImpl)getMetaData(f, true);
	
	        // Owner / Group
	        if (
	        		target.getOwner() != null
	        		&& target.getGroup() != null
	                && ((! current.getOwner().equals(target.getOwner())) || (! current.getGroup().equals(target.getGroup())))
	        ) {
	            OSTool.execute(new String[] {"chown", target.getOwner() + ":" + target.getGroup(), FileSystemManager.getAbsolutePath(f)});
	        }
	        
	        // Mode
	        if (
	        		target.getMode() != PosixMetaDataImpl.UNDEF_MODE
	        		&& current.getMode() != target.getMode()
	        ) {
	        	OSTool.execute(new String[] {"chmod", "" + target.getModeBase10(), FileSystemManager.getAbsolutePath(f)});
	        }
        }
        
        // Date
        if (target.getLastmodified() != FileMetaData.UNDEF_DATE) {
        	f.setLastModified(target.getLastmodified());
        }
	}
	
	public boolean test() {
		return (! OSTool.isSystemWindows());
	}
	
	public boolean ACLSupported() {
		return false;
	}

	public boolean extendedAttributesSupported() {
		return false;
	}

	public short getType(File f) throws IOException {
		if (isSymLink(f)) {
			return TYPE_LINK;
		} else if (f.isDirectory()) {
			return TYPE_DIRECTORY;
		} else {
			return TYPE_FILE;
		}
	}

	public boolean typeSupported(short type) {
		return (type == TYPE_DIRECTORY || type == TYPE_FILE || type == TYPE_LINK);
	}

	private boolean isSymLink(File f) throws IOException {
        if (! f.exists()) {
            return true;  // Specific case of dangling symbolic links
        } else {
        	return ! f.getAbsolutePath().equals(f.getCanonicalPath());
        }
	}
	
    private static int readrwx(char r, char w, char x, int order) {
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
        return ret * order;
    }

	public String getDescription() {
		return DESCRIPTION;
	}
}
