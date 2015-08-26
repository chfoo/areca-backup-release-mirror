package com.application.areca;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import com.application.areca.adapters.ConfigurationHandler;
import com.application.areca.launcher.LocalPreferences;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.MissingDataListener;
import com.application.areca.version.VersionInfos;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.util.log.ConsoleLogProcessor;
import com.myJava.util.log.FileLogProcessor;
import com.myJava.util.log.Logger;
import com.myJava.util.xml.AdapterException;

/**
 * <BR>This class implements a workspace.
 * <BR>A workspace is a collection of WorkspaceItems
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
public class Workspace {
	protected File root;
	protected TargetGroup content;
	protected Application application;

	public static Workspace open(String path, Application application, boolean installMedium) throws AdapterException {
		Workspace w = new Workspace(path, application);
		w.loadDirectory(w.root, installMedium);
		return w;
	}
	
	private Workspace(String path, Application application) {
		this.application = application; 
		this.root = new File(path);
	}

	public String toString() {
		return "Workspace : " + FileSystemManager.getDisplayPath(root);
	}

	public void remove(String id) {
		content.remove(id);
	}

	public void remove(TargetGroup group) {
		this.remove(group.getUid());
	}
	
	public void remove(AbstractTarget target) {
		this.remove(target.getUid());
	}

	public Iterator getIterator() {
		return this.content.getIterator();
	}    

	public String getPath() {
		return FileSystemManager.getAbsolutePath(root);
	}
	
	public File getPathFile() {
		return new File(getPath());
	}
	
	public TargetGroup getContent() {
		return content;
	}
	
    public boolean isBackupWorkspace() {
    	return 
    	getContent() != null
    	&& getContent().getLoadedFrom() != null
    	&& getContent().getLoadedFrom().isBackupCopy();
    }
    
    private void setupLogProcessorsForWorkspace(File workspaceLocation) {
    	// Handle log configuration
		Logger.defaultLogger().remove(FileLogProcessor.class);
		Logger.defaultLogger().remove(ConsoleLogProcessor.class); // we don't want the default console processor that is set in the Logger class.
		FileLogProcessor proc;
		if (ArecaConfiguration.get().getLogLocationOverride() == null) {
			File directoryLog = new File(FileSystemManager.getAbsolutePath(workspaceLocation) + "/" + ArecaFileConstants.LOG_SUBDIRECTORY_NAME + "/");
			
			// Backward compatibility
			File deprecatedDirectoryLog = new File(FileSystemManager.getAbsolutePath(workspaceLocation) + "/" + ArecaFileConstants.DEPRECATED_LOG_SUBDIRECTORY_NAME + "/");					
			if (FileSystemManager.exists(deprecatedDirectoryLog)) {
				if (FileSystemManager.exists(directoryLog)) {
					try {
						FileTool.getInstance().delete(deprecatedDirectoryLog);
					} catch (IOException e) {
						Logger.defaultLogger().warn("Error while trying to move " + FileSystemManager.getDisplayPath(deprecatedDirectoryLog) + " to " + FileSystemManager.getDisplayPath(directoryLog) + " : " + e.getMessage());
					}
				} else {
					FileSystemManager.renameTo(deprecatedDirectoryLog, directoryLog);
				}
				Logger.defaultLogger().info("Backward compatibility : Log directory : " + FileSystemManager.getDisplayPath(deprecatedDirectoryLog) + " moved to " + FileSystemManager.getDisplayPath(directoryLog));
			}
			// EOF Backward compatibility					

			proc = new FileLogProcessor(new File(directoryLog, VersionInfos.APP_SHORT_NAME.toLowerCase()));
		} else {
			proc = new FileLogProcessor(new File(ArecaConfiguration.get().getLogLocationOverride(), VersionInfos.APP_SHORT_NAME.toLowerCase()));
		}
		Logger.defaultLogger().addProcessor(proc);

		LogHelper.logStartupInformations();
		LocalPreferences.instance().logProperties();
    }

	/**
	 * Load the workspace denoted by the path passed as argument.
	 */
	private void loadDirectory(File f, boolean installMedium) throws AdapterException {
		try {
			if (installMedium) {
				setupLogProcessorsForWorkspace(f);
			}

			if (FileSystemManager.exists(f)) {
				// Load content
				content = ConfigurationHandler.getInstance().readTargetGroup(f, new MissingDataListener(), installMedium);
			}
			
			if (content == null) {
				content = new TargetGroup("<root>");
				content.setLoadedFrom(new ConfigurationSource(false, f));
			}
			Logger.defaultLogger().info("Path : [" + f + "] - " + (this.content == null ? 0 : this.content.size()) + " items loaded.");
		} catch (RuntimeException e) {
			Logger.defaultLogger().error(e);
			throw e;
		}
	} 
}
