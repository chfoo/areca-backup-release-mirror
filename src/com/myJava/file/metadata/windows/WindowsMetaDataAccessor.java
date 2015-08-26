package com.myJava.file.metadata.windows;

import java.io.File;
import java.io.IOException;

import com.myJava.file.metadata.FileMetaData;
import com.myJava.file.metadata.FileMetaDataAccessor;
import com.myJava.file.metadata.FileMetaDataSerializer;

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
public class WindowsMetaDataAccessor implements FileMetaDataAccessor {

	private static final String DESCRIPTION = "Default metadata accessor for Windows. It only handles the \"read-only\" file attribute.";
	private static final FileMetaDataSerializer SERIALIZER = new WindowsMetaDataSerializer();
	
	public FileMetaData getMetaData(File f, boolean onlyBasicAttributes) throws IOException {
        WindowsMetaData p = new WindowsMetaData();
        p.setCanRead(f.canRead());
        p.setCanWrite(f.canWrite());
        p.setLastmodified(f.lastModified());
        return p;
	}

	public FileMetaData buildEmptyMetaData() {
		return new WindowsMetaData();
	}

	public FileMetaDataSerializer getMetaDataSerializer() {
		return SERIALIZER;
	}

	public void setMetaData(File f, FileMetaData attr) throws IOException {
        WindowsMetaData wmdt = (WindowsMetaData)attr;
        if (wmdt.getLastmodified() != FileMetaData.UNDEF_DATE) {
        	f.setLastModified(wmdt.getLastmodified());
        }
        
        if (! wmdt.isCanWrite()) {
            f.setReadOnly();
        }
	}

	public boolean test() {
		// Can be run anywhere : it only uses standard java methods
		return true;
	}

	public boolean ACLSupported() {
		return false;
	}

	public boolean extendedAttributesSupported() {
		return false;
	}

	public short getType(File f) throws IOException {
		if (f.isDirectory()) {
			return TYPE_DIRECTORY;
		} else {
			return TYPE_FILE;
		}
	}

	public boolean typeSupported(short type) {
		return (type == TYPE_FILE || type == TYPE_DIRECTORY);
	}

	public String getDescription() {
		return DESCRIPTION;
	}
}
