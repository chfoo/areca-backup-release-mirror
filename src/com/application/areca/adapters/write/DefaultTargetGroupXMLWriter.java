package com.application.areca.adapters.write;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import com.application.areca.ApplicationException;
import com.application.areca.TargetGroup;
import com.application.areca.adapters.ConfigurationHandler;
import com.application.areca.impl.FileSystemTarget;
import com.myJava.file.FileTool;
import com.myJava.util.log.Logger;

/**
 * Default Target group serializer - Calls target serializers
 * 
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
public class DefaultTargetGroupXMLWriter
implements XMLVersions { 
	private boolean removeEncryptionData;
	private boolean isBackupCopy;

	public DefaultTargetGroupXMLWriter(boolean removeEncryptionData, boolean isBackupCopy) {
		this.removeEncryptionData = removeEncryptionData;
		this.isBackupCopy = isBackupCopy;
	}

	public boolean serialize(TargetGroup group, File file) 
	throws ApplicationException {
		try {			
			FileTool.getInstance().createDir(file);

			// Serialize targets
			boolean result = true;
			Iterator iter = group.getIterator();
			while(iter.hasNext()) {
				Object o = iter.next();
				if (o instanceof FileSystemTarget) {
					FileSystemTarget target = (FileSystemTarget)o;
					result &= ConfigurationHandler.getInstance().serialize(target, file, removeEncryptionData, isBackupCopy);
				} else {
					TargetGroup subGroup = (TargetGroup)o;
					result &= ConfigurationHandler.getInstance().serialize(subGroup, new File(file, subGroup.getName()), removeEncryptionData, isBackupCopy);
				}
			}
			return result;
		} catch (UnsupportedEncodingException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		}
	}
}
