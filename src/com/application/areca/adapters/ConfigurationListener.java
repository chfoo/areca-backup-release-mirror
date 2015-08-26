package com.application.areca.adapters;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import com.application.areca.AbstractTarget;
import com.application.areca.ApplicationException;
import com.application.areca.ConfigurationSource;
import com.application.areca.TargetGroup;
import com.application.areca.WorkspaceItem;
import com.application.areca.impl.FileSystemTarget;
import com.application.areca.version.VersionInfos;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.util.log.Logger;

/**
 * Handles configuration modification events
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
public class ConfigurationListener {
	private static ConfigurationListener INSTANCE = new ConfigurationListener();

	public static ConfigurationListener getInstance() {
		return INSTANCE;
	}

	private ConfigurationListener() {
	}
	
	public void targetModified(AbstractTarget target, File configurationDirectory) throws IOException, ApplicationException {
		ensureConfigurationFileAvailability(target, configurationDirectory);
		ConfigurationHandler.getInstance().serialize((FileSystemTarget)target, target.getParent().computeConfigurationFile(configurationDirectory), false, false);
		synchronizeLoadedFrom(target, configurationDirectory);
	}
	
	public void targetCreated(AbstractTarget target, File configurationDirectory) throws IOException, ApplicationException {
		ensureConfigurationFileAvailability(target.getParent(), configurationDirectory);
		ConfigurationHandler.getInstance().serialize((FileSystemTarget)target, target.getParent().computeConfigurationFile(configurationDirectory), false, false);
		synchronizeLoadedFrom(target, configurationDirectory);
	}

	public void groupCreated(TargetGroup group, File configurationDirectory) throws IOException, ApplicationException {
		ensureConfigurationFileAvailability(group.getParent(), configurationDirectory);
		File f = group.computeConfigurationFile(configurationDirectory);
		FileTool.getInstance().createDir(f);
		synchronizeLoadedFrom(group, configurationDirectory);
	}

	public void itemDeleted(WorkspaceItem item, File configurationDirectory) throws IOException, ApplicationException {
		ensureConfigurationFileAvailability(item, configurationDirectory);
		
		File configFile = item.computeConfigurationFile(configurationDirectory);
		FileTool.getInstance().delete(configFile);
	}

	public void itemMoved(
			WorkspaceItem movedItem, 
			TargetGroup formerParent, 
			File configurationDirectory
	) throws IOException, ApplicationException {
		// Save new Parent
		TargetGroup newParent = movedItem.getParent();
		
		// Ensure availability of target directory
		ensureConfigurationFileAvailability(newParent, configurationDirectory);
		
		// Temporarily link the item to its former parent, to ensure config file availability
		formerParent.linkChild(movedItem);
		ensureConfigurationFileAvailability(formerParent, configurationDirectory);
		
		// Restore parent
		formerParent.remove(movedItem.getUid());
		newParent.linkChild(movedItem);
		
		// Move configuration file
		File formerParentFile = formerParent.computeConfigurationFile(configurationDirectory);
		File sourceFile = movedItem.computeConfigurationFile(formerParentFile, false);

		File targetFile = movedItem.computeConfigurationFile(configurationDirectory);
		File targetDir = FileSystemManager.getParentFile(targetFile);
		FileTool.getInstance().copy(sourceFile, targetDir);
		FileTool.getInstance().delete(sourceFile);
		
		synchronizeLoadedFrom(movedItem, configurationDirectory);
	}
	
	public File ensureConfigurationFileAvailability(WorkspaceItem item, File configurationDirectory) 
	throws IOException, ApplicationException {
		File configFile = item.computeConfigurationFile(configurationDirectory);

		if (! FileSystemManager.exists(configFile)) {
			// Migrate configuration data to the new format
			migrateConfiguration(item, configurationDirectory);
		}

		return configFile;
	}
	
	private void migrateConfiguration(WorkspaceItem item, File configurationDirectory) throws IOException, ApplicationException {
		// Locate the main group - which will be migrated
		TargetGroup group;
		if (item instanceof AbstractTarget) {
			group = item.getParent();
		} else {
			group = (TargetGroup)item;
		}
		
		// Check that the group format is deprecated
		if (group.getLoadedFrom().isDeprecated()) {
			// Destroy existing configuration file
			Logger.defaultLogger().warn("The configuration of \"" + group.getFullPath() + "\" will be migrated to the new format");
			FileTool.getInstance().delete(group.getLoadedFrom().getSource());	
			
			// Serialize group
			ConfigurationHandler.getInstance().serialize(
					group, 
					group.computeConfigurationFile(configurationDirectory), 
					false, 
					false);
			
			// Set proper source
			synchronizeLoadedFrom(group, configurationDirectory);
		}
	}
	
	private void synchronizeLoadedFrom(WorkspaceItem item, File configurationDirectory) throws IOException {
        ConfigurationSource source = new ConfigurationSource(false, item.computeConfigurationFile(configurationDirectory));
        
		if (! FileSystemManager.exists(source.getSource())) {
			throw new IOException("Configuration file not found (" + source.getSource() + "). Please restart " + VersionInfos.APP_SHORT_NAME + ".");
		}
        
		item.setLoadedFrom(source);
		
		if (item instanceof TargetGroup) {
			Iterator iter = ((TargetGroup)item).getIterator();
			while (iter.hasNext()) {
				WorkspaceItem child = (WorkspaceItem)iter.next();
				synchronizeLoadedFrom(child, configurationDirectory);
			}
		}
	}
}
