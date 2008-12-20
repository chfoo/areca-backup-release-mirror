package com.myJava.file.metadata.posix.linux.jni;

/**
 * This class wraps native POSIX methods for file permissions management.
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
public class FileAccessWrapper {
	/**
	 * Returns the result of the "fstat" C method.
	 * @param path
	 * @return
	 */
	public static native ExtendedFileData getData(String path, boolean followSymLinks);
	
	/**
	 * Sets the file's owner/group.
	 * <BR>Returns something !=0 if an error occurred.
	 */
	public static native int setFileOwner(String fileName, int owner, int group, boolean followSymLinks);
	
	/**
	 * Sets the file's permissions.
	 * <BR>Returns something !=0 if an error occurred.
	 */
	public static native int setFileMode(String fileName, int bitField, boolean followSymLinks);
	
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
	
	public static native AttributeNameList getAttributeNames(String file, boolean followSymLinks); 
	
	static {
		// remove the "lib" and the ".so" from the file name to get the library's name. 
		System.loadLibrary("arecafs");
	}
	
	/**
	 * Parses the file's "mode" field and returns its permissions.
	 * <BR>Returns something !=0 if an error occurred.
	 * <BR>0  OTHER - X
	 * <BR>1  OTHER - W
	 * <BR>2  OTHER - R
	 * <BR>3  GROUP - X
	 * <BR>4  GROUP - W
	 * <BR>5  GROUP - R
	 * <BR>6  USER  - X
	 * <BR>7  USER  - W
	 * <BR>8  USER  - R
	 * <BR>9  STICKY Byte
	 * <BR>10 GID Byte
	 * <BR>11 UID Byte
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
	public static boolean[] parseModeField(int bf) {
		boolean[] out = new boolean[16];

		for (int i=0; i<16; i++) {
			int b = (bf & (int)Math.pow(2, i)) / ((int)Math.pow(2, i));
			out[i] = (b == 1);
		}

		return out;
	}
	
	private static void displayModeField(int bf) {	
		String o = "";
		for (int i=0; i<16; i++) {
			int b = (bf & (int)Math.pow(2, i)) / ((int)Math.pow(2, i));
			o += " " + b;
		}

		System.out.println(o);
	}
	
	/**
	 * Parse the file's mode and tells whether the file is a socket or not
	 */
	public static boolean isSocket(int bf) {
		boolean[] b = parseModeField(bf);
		return (!b[12] & !b[13] & b[14] & b[15]);
	}
	
	/**
	 * Parse the file's mode and tells whether the file is a character special file or not
	 */
	public static boolean isCharacterSpecialFile(int bf) {
		boolean[] b = parseModeField(bf);
		return (!b[12] & b[13] & !b[14] & !b[15]);
	}
	
	/**
	 * Parse the file's mode and tells whether the file is a block special file or not
	 */
	public static boolean isBlockSpecialFile(int bf) {
		boolean[] b = parseModeField(bf);
		return (!b[12] & b[13] & b[14] & !b[15]);
	}
	
	/**
	 * Parse the file's mode and tells whether the file is a symbolic link or not
	 */
	public static boolean isSymbolicLink(int bf) {
		boolean[] b = parseModeField(bf);
		return (!b[12] & b[13] & !b[14] & b[15]);
	}
	
	/**
	 * Parse the file's mode and tells whether the file is a standard file or not
	 */
	public static boolean isFile(int bf) {
		boolean[] b = parseModeField(bf);
		return (!b[12] & !b[13] & !b[14] & b[15]);
	}
	
	/**
	 * Parse the file's mode and tells whether the file is a directory or not
	 */
	public static boolean isDirectory(int bf) {
		boolean[] b = parseModeField(bf);
		return (!b[12] & !b[13] & b[14] & !b[15]);
	}
	
	/**
	 * Parse the file's mode and tells whether the file is a pipe or not
	 */
	public static boolean isPipe(int bf) {
		boolean[] b = parseModeField(bf);
		return (b[12] & !b[13] & !b[14] & !b[15]);
	}

	public static void main(String[] args) {
		
		parseModeField(0001000);
		
		String file = "/media/clef_usb_op/test/toto.txt";
		//String file = "/home/olivier/Bureau";

		/*
		System.out.println(getGroupName(0));
		

		System.out.println(getGroupId("fuse"));
		System.out.println(getUserName(0));
		System.out.println(getUserId("fuse"));
		 */
		

		ExtendedFileData data = FileAccessWrapper.getData(file, false);
		System.out.println("File data for " + file + " : ");
		System.out.println(data);
		int md = (int)data.st_mode;
		displayModeField(md);
		
		System.out.println("IS LINK : " + isSymbolicLink(md));
		System.out.println("IS DIR : " + isDirectory(md));
		System.out.println("IS FILE : " + isFile(md));
		System.out.println("IS PIPE : " + isPipe(md));
		System.out.println("IS CHARACTER SF : " + isCharacterSpecialFile(md));
		System.out.println("IS BLOCK SF : " + isBlockSpecialFile(md));
		System.out.println("IS SOCKET : " + isSocket(md));
		
		AttributeNameList l = getAttributeNames(file, false);
		//System.out.println(l.length);
		System.out.println(l.data.length);
		
		int nb = l.length;

		for (int i=0; i<nb; i++) {
			System.out.println("----------");
			System.out.println("i="+i);
			System.out.println("c[i]="+(int)l.data[i] + " / " + (char)l.data[i]);
		}

		//boolean[] dt = parseModeField((int)data.st_mode);

		
		/*
		setFileMode(file, 33204, true);
		
		int ret = FileAccessWrapper.setFileOwner(file, 500, 423, true);
		System.out.println("Modifying owner : " + ret);
		
		data = FileAccessWrapper.getData(file, false);
		System.out.println("File data for " + file + " : ");
		System.out.println(data);
		*/
	}
}
