package com.myJava.file.metadata;

import java.io.File;
import java.io.IOException;

/**
 * Interface which is able to set and read part of / all the filesystem's metadata.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 7019623011660215288
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

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
public interface FileMetaDataAccessor {
	
	/**
	 * Return a description of the accessor
	 */
	public String getDescription();
	
	/**
	 * Return a FileMetaDataSerializer which is compatible with the metadata produced 
	 * by this accessor.
	 */
	public FileMetaDataSerializer getMetaDataSerializer();
	
	/**
	 * Return the meta data for the requested file
	 */
	public FileMetaData getMetaData(File file, boolean onlyBasicData) throws IOException;
	
	/**
	 * Return an empty instance of meta data
	 */
	public FileMetaData buildEmptyMetaData();
	
	/**
	 * Set the file's meta data
	 */
	public void setMetaData(File file, FileMetaData mdt) throws IOException;
	
	/**
	 * Test whether the accessor is compatible with the current system or not.
	 * <BR>This method may be useful if native code is used.
	 */
	public boolean test();
	
	/**
	 * Is the accessor able to handle ACL ?
	 */
	public boolean ACLSupported();
	
	/**
	 * Is the accessor able to handle extended attributes ?
	 */
	public boolean extendedAttributesSupported();
	
	/**
	 * Is the accessor able to distinguish between standard files / directories and symbolic links ? 
	 */
	public boolean symLinksSupported();
	
	/**
	 * Is the file a symbolic link ?
	 */
	public boolean isSymLink(File file) throws IOException;
	
	/**
	 * Is the accessor able to distinguish between standard files / directories and non standard
	 * files (pipes, socket, block special files, ...) ? 
	 */
	public boolean nonStandardFilesSupported();
	
	/**
	 * Is the file a non standard file (pipes, socket, block special files, ...) ?
	 */
	public boolean isNonStandardFile(File file) throws IOException;
}
