package com.myJava.file.metadata.posix.linux;

import com.myJava.file.metadata.FileMetaData;
import com.myJava.file.metadata.FileMetaDataSerializationException;
import com.myJava.file.metadata.FileMetaDataSerializer;
import com.myJava.file.metadata.posix.PosixMetaDataImpl;
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
public class LinuxMetaDataSerializer implements FileMetaDataSerializer {

	public FileMetaData deserialize(String s) throws FileMetaDataSerializationException {
        try {
			String perms = s.substring(1, 4);
			int iPerms = Integer.parseInt(perms);
			
			int index = s.indexOf(' ');
			String owner = s.substring(4, index);
			String group = s.substring(index + 1).trim();
			
			PosixMetaDataImpl p = new PosixMetaDataImpl();
			p.setOwner(owner);
			p.setOwnerGroup(group);
			p.setPermissions(iPerms);

			return p;
		} catch (RuntimeException e) {
			Logger.defaultLogger().error("Error caught during permission deserialization : " + s, e);
			throw e;
		}
	}

	public void serialize(FileMetaData attr, StringBuffer sb) throws FileMetaDataSerializationException {
        PosixMetaDataImpl perm = (PosixMetaDataImpl)attr;
        sb.append("u");
    	toString(perm.getPermissions(), sb);
        sb.append(perm.getOwner()).append(" ").append(perm.getOwnerGroup());	
	}
	
    private static void toString(int perm, StringBuffer sb) {
    	if (perm < 100) {
    		sb.append("0");
    	}
    	if (perm < 10) {
    		sb.append("0");
    	}
    	sb.append(perm);
    }
}
