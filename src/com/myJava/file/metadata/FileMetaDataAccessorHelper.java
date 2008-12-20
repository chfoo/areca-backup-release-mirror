package com.myJava.file.metadata;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.metadata.posix.linux.LinuxMetaDataAccessor;
import com.myJava.file.metadata.windows.WindowsMetaDataAccessor;
import com.myJava.system.OSTool;
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
public class FileMetaDataAccessorHelper {

	private static FileMetaDataAccessor INSTANCE;
	
	static {
		synchronized (FileMetaDataAccessorHelper.class) {
			String configuredAccessor = FrameworkConfiguration.getInstance().getFileSystemAccessorImpl();
			if (configuredAccessor != null) {
				try {
					INSTANCE = (FileMetaDataAccessor)Class.forName(configuredAccessor).newInstance();
				} catch (Exception e) {
					Logger.defaultLogger().error("Error while loading file system accessor : [" + configuredAccessor + "]. Check your configuration.", e);
				}
			}
			
			if (INSTANCE == null) {
				// No configured accessor or accessor unsuccessfully loaded
				if (OSTool.isSystemWindows()) {
					INSTANCE = new WindowsMetaDataAccessor();
				} else {
					INSTANCE = new LinuxMetaDataAccessor();			
				}
			}
		}
	}
	
	public static FileMetaDataAccessor getFileSystemAccessor() {
		return INSTANCE;
	}
}
