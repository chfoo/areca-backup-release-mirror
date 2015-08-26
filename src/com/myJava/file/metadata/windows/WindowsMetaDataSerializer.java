package com.myJava.file.metadata.windows;

import com.myJava.file.metadata.FileMetaData;
import com.myJava.file.metadata.FileMetaDataSerializationException;
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
public class WindowsMetaDataSerializer implements FileMetaDataSerializer {

	public FileMetaData deserialize(String s, long version) throws FileMetaDataSerializationException {
        if (s.charAt(0) != 'w') {
        	return null;
        } else {
			int nb = Integer.parseInt("" + s.charAt(1));
	        WindowsMetaData p = new WindowsMetaData();
	        p.setCanRead(nb % 2 == 1);
	        p.setCanWrite(nb >= 2);
	        
	        return p;
        }
	}

	public void serialize(FileMetaData attr, StringBuffer sb) throws FileMetaDataSerializationException {
        WindowsMetaData perm = (WindowsMetaData)attr;
        int nb = 
            (perm.isCanRead() ? 1 : 0)
            + 2*(perm.isCanWrite() ? 1 : 0);
        
        sb.append("w").append(nb);
	}
}
