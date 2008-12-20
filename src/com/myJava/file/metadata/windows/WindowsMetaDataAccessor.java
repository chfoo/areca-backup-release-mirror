package com.myJava.file.metadata.windows;

import java.io.File;
import java.io.IOException;

import com.myJava.file.metadata.FileMetaDataAccessor;
import com.myJava.file.metadata.FileMetaData;
import com.myJava.file.metadata.FileMetaDataSerializer;

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
public class WindowsMetaDataAccessor implements FileMetaDataAccessor {

	private static final FileMetaDataSerializer SERIALIZER = new WindowsMetaDataSerializer();
	
	public FileMetaData getAttributes(File f) throws IOException {
        WindowsMetaData p = new WindowsMetaData();
        p.setCanRead(f.canRead());
        p.setCanWrite(f.canWrite());
        return p;
	}

	public FileMetaDataSerializer getAttributesSerializer() {
		return SERIALIZER;
	}

	public void setAttributes(File f, FileMetaData attr) throws IOException {
        WindowsMetaData perm = (WindowsMetaData)attr;
        if (! perm.isCanWrite()) {
            f.setReadOnly();
        }
	}
}
