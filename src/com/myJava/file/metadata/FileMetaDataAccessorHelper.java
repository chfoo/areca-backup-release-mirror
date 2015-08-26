package com.myJava.file.metadata;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.metadata.posix.basic.DefaultMetaDataAccessor;
import com.myJava.file.metadata.windows.WindowsMetaDataAccessor;
import com.myJava.system.OSTool;
import com.myJava.util.log.Logger;

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
public class FileMetaDataAccessorHelper {

	private static FileMetaDataAccessor INSTANCE;
	
	static {
		synchronized (FileMetaDataAccessorHelper.class) {
			String configuredAccessor = FrameworkConfiguration.getInstance().getFileSystemAccessorImpl();
			
			if (configuredAccessor != null) {
				try {
					// Load the configured accessor
					Logger.defaultLogger().info("Loading configured file metadata accessor : [" + configuredAccessor + "] ...");
					INSTANCE = (FileMetaDataAccessor)Class.forName(configuredAccessor).newInstance();
					Logger.defaultLogger().fine("Configured metadata accessor description : ");
					Logger.defaultLogger().fine(INSTANCE.getDescription());
					
					// Test the configured accessor
					Logger.defaultLogger().info("Testing configured file metadata accessor ...");
					if (INSTANCE.test()) {
						Logger.defaultLogger().info("[" + configuredAccessor + "] validated.");
					} else {
						Logger.defaultLogger().warn("[" + configuredAccessor + "] not validated. The default metadata accessor will be used instead. See FAQ for more informations about file metadata management. (ACL & extended attributes)");
						INSTANCE = null;
					}
				} catch (Exception e) {
					Logger.defaultLogger().error("Error while loading configured file metadata accessor : [" + configuredAccessor + "]. Check your configuration.", e);
				}
			}
			
			if (INSTANCE == null) {
				// No configured accessor or accessor unsuccessfully loaded
				if (OSTool.isSystemWindows()) {
					INSTANCE = new WindowsMetaDataAccessor();
				} else {
					INSTANCE = new DefaultMetaDataAccessor();			
				}
				
				// Test the default accessor
				Logger.defaultLogger().info("Default file metadata accessor loaded : [" + INSTANCE.getClass().getName() + "].");
				Logger.defaultLogger().fine("Default metadata accessor description : ");
				Logger.defaultLogger().fine(INSTANCE.getDescription());
				if (INSTANCE.test()) {
					Logger.defaultLogger().info("[" + INSTANCE.getClass().getName() + "] validated.");
				} else {
					Logger.defaultLogger().error("Illegal default file metadata accessor : [" + INSTANCE.getClass().getName() + "]. Exiting !");
					throw new IllegalStateException("Illegal default file metadata accessor : [" + INSTANCE.getClass().getName() + "]");
				}
			}
			
			Logger.defaultLogger().info("ACL support : " + (INSTANCE.ACLSupported() ? "yes" : "no"));
			Logger.defaultLogger().info("Extended attributes support : " + (INSTANCE.extendedAttributesSupported() ? "yes" : "no"));
		}
	}
	
	public static FileMetaDataAccessor getFileSystemAccessor() {
		return INSTANCE;
	}
}
