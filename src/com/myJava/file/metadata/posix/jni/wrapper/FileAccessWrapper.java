package com.myJava.file.metadata.posix.jni.wrapper;

import com.myJava.file.metadata.posix.ACL;
import com.myJava.system.OSTool;

/**
 * This class wraps native POSIX methods for file permissions management.
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
public class FileAccessWrapper {
	// ERROR CODES
	public static final int ERR_UNEXPECTED = 1;					//
	public static final int ERR_NOT_ENOUGH_MEMORY = 100;		// ENOMEM
	public static final int ERR_UNSUPPORTED = 101;				// ENOTSUP
	public static final int ERR_NAME_TOOLONG = 102;				// ENAMETOOLONG
	public static final int ERR_NOT_A_DIRECTORY = 103;			// ENOTDIR
	public static final int ERR_FILE_NOT_FOUND = 104;			// ENOENT
	public static final int ERR_BUFFER_TOO_SMALL = 105;			// ERANGE
	public static final int ERR_ATTRIBUTE_NOT_FOUND = 106;		// ENOATTR
	public static final int ERR_INVALID_DATA = 107;				// EINVAL
	public static final int ERR_NOT_ENOUGH_DISK_SPACE = 108;	// ENOSPC
	public static final int ERR_PERMISSION_DENIED = 109;		// EPERM
	public static final int ERR_RO_FILESYSTEM = 110;			// EROFS
	public static final int ERR_DISK_QUOTA = 111;				// EDQUOT
	public static final int ERR_INTERNAL = 112;					// EFAULT
	public static final int ERR_IO = 113;						// EIO
	public static final int ERR_NOT_IMPLEMENTED = 114;			// ENOSYS
	public static final int ERR_LOOP = 115;						// ELOOP
	public static final int ERR_ACCESS_DENIED = 116;			// EACCES
	
	// MODE MASKS
	public static final int MASK_PERMISSION = 4095; 		// 2^00 + 2^01 + .... + 2^11
	public static final int MASK_TYPE = 61440; 				// 2^12 + 2^13 + 2^14 + 2^15
	public static final int TYPE_LINK = 40960;  			// 2^13 + 2^15
	public static final int TYPE_SOCKET = 49152;  			// 2^14 + 2^15
	public static final int TYPE_CHARSPECFILE = 8192;  		// 2^13
	public static final int TYPE_BLOCKSPECIALFILE = 24576;	// 2^13 + 2^14
	public static final int TYPE_FILE = 32768;  			// 2^15
	public static final int TYPE_DIRECTORY = 16384;  		// 2^14
	public static final int TYPE_PIPE = 4096;  				// 2^12
	
	/**
	 * Returns the result of the "fstat" C method.
	 * <BR>The "st_mode" field is set as follows :
	 * <BR>0  OTHER - X
	 * <BR>1  OTHER - W
	 * <BR>2  OTHER - R
	 * <BR>3  GROUP - X
	 * <BR>4  GROUP - W
	 * <BR>5  GROUP - R
	 * <BR>6  USER  - X
	 * <BR>7  USER  - W
	 * <BR>8  USER  - R
	 * <BR>9  STICKY Bit
	 * <BR>10 GID Bit
	 * <BR>11 UID Bit
	 * <BR>12 S_IFIFO
	 * <BR>13 S_IFCHR  /  S_IFLNK  /  S_IFBLK
	 * <BR>14 S_IFDIR  /              S_IFBLK  /  S_IFSOCK
	 * <BR>15 S_IFREG  /  S_IFLNK              /  S_IFSOCK
	 * <BR>
	 * <BR>Where :
	 * <BR>S_IFBLK  - Is this a block special file? (usually a block-based device of some sort)
	 * <BR>S_IFCHR  - Is this a character special file? (again, usually a character-based device of some sort)
	 * <BR>S_IFDIR  - Is this a directory?
	 * <BR>S_IFIFO  - Is this a pipe or FIFO special file?
	 * <BR>S_IFLNK  - Is this a symbolic link?
	 * <BR>S_IFREG  - Is this a regular file?
	 * <BR>S_IFSOCK - Is this a socket?
	 */
	public static native GetDataResult getData(String file, boolean followSymLinks);
	
	/**
	 * Sets the file's owner/group.
	 */
	public static native SetFileOwnerResult setFileOwner(String file, int owner, int group, boolean followSymLinks);
	
	/**
	 * Sets the file's mode.
	 */
	private static native SetFileModeResult setFileModeImpl(String file, int bitField);
	
	/**
	 * Sets the file's mode.
	 * <BR>The bit field is filtered by MASK_PERMISSION.
	 * <BR>
	 * <BR>LCHMOD is not implemented on Linux. We will use CHMOD instead and prevent calling this
	 * method if the file is a symlink. (permissions not supported on Linux)
	 */
	public static SetFileModeResult setFileMode(String file, int bitField) {
		if (! isA(bitField, TYPE_LINK)) {
			int filteredField = bitField & MASK_PERMISSION;
			return setFileModeImpl(file, filteredField);
		} else {
			return new SetFileModeResult();
		}
	}
	
	/**
	 * Returns the group id matching the name provided as argument.
	 * <BR>Returns -1 if no match was found
	 */
	public static native int getGroupId(String name);
	
	/**
	 * Returns the group name matching the id provided as argument.
	 * <BR>Returns NULL if no match was found
	 */
	public static native String getGroupName(int id);
	
	/**
	 * Returns the user id matching the name provided as argument.
	 * <BR>Returns -1 if no match was found
	 */
	public static native int getUserId(String name);
	
	/**
	 * Returns the user name matching the id provided as argument.
	 * <BR>Returns NULL if no match was found
	 */
	public static native String getUserName(int id);
	
	/**
	 * Return a list of extended attributes names
	 * <BR>bufferSize is the memory that will be allocated for the operation. If it is too small, the method will return an error.
	 */
	public static native GetAttributeNamesResult getAttributeNames(String file, int bufferSize, boolean followSymLinks); 

	/**
	 * Return the value of the attribute
	 */
	public static native GetAttributeValueResult getAttributeValue(String file, String attributeName, long size, boolean followSymLinks);

	/**
	 * Set the value of the attribute
	 */
	public static native SetAttributeValueResult setAttributeValue(String file, String attributeName, byte[] data, boolean followSymLinks);
	
	/**
	 * Return the ACL of the file passed as argument.
	 * <BR>If defaultACL == true, then only the default ACL is returned
	 * <BR>If defaultACL == false, then only the access ACL is returned
	 */
	public static native GetACLResult getACL(String file, boolean defaultACL);

	/**
	 * Set the acl for the file passed as argument
	 * <BR>If defaultACL == true, then only the default ACL is set
	 * <BR>If defaultACL == false, then only the access ACL is set
	 */
	public static native SetACLResult setACL(String file, ACL acl, int size, boolean defaultACL);
	
	static {
		// Load libraries
		System.loadLibrary("arecafs");
		System.loadLibrary("acl");
		
		try {
			// For some unknown reason, the first call to the "getMetaData" method seems to
			// fail (the "acl_get_file" C method returns the "file not found" error code). 
			// Hence this first call.
			getACL(OSTool.getUserDir(), false);
		} catch (Throwable ignored) {
			// Trash the error.
		}
	}
	
	/**
	 * Returns true if the mode has the "tested" type.
	 * <BR>"tested" must be chosen among TYPE_LINK, TYPE_SOCKET, TYPE_CHARSPECFILE, 
	 * TYPE_BLOCKSPECIALFILE, TYPE_FILE, TYPE_DIRECTORY, TYPE_PIPE  
	 */
	public static boolean isA(int mode, int tested) {
    	int type = (int)(mode & MASK_TYPE);
    	return (type == tested);
	}
	
	/**
	 * Display the "mode" field's binary form
	 */
	public static void displayModeField(int bf) {	
		String o = "";
		for (int i=0; i<16; i++) {
			int b = (bf & (int)Math.pow(2, i)) / ((int)Math.pow(2, i));
			o += " " + b;
		}

		System.out.println(o);
	}
}
