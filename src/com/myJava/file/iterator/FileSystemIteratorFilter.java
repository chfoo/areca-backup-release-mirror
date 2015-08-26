package com.myJava.file.iterator;

import java.io.File;

/**
 * 
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
public interface FileSystemIteratorFilter {
	public static short WILL_MATCH_TRUE = 2;
	public static short WILL_MATCH_PERHAPS = 1;
	public static short WILL_MATCH_FALSE = 0;
	
	/**
	 * Tell whether the iterator will iterate into the directoryPath provided as argument 
	 * <BR>The directoryData argument is a non-nullable argument that allows the caller to specify that the filter will have to 
	 * get the data related to the file (internal file data, but also attributes such as ACL or modification date) from this
	 * file and not from the original file.
	 */
	public short acceptIteration(File directoryPath, File directoryData);

	/**
	 * Tell whether the iterator will return the elementPath to the caller
	 * <BR>The elementData argument is a non-nullable argument that allows the caller to specify that the filter will have to 
	 * get the data related to the file (internal file data, but also attributes such as ACL or modification date) from this
	 * file and not from the original file.
	 */
	public boolean acceptElement(File elementPath, File elementData);
}
