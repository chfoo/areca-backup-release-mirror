package com.application.areca.launcher.gui.confimport;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import com.application.areca.ApplicationException;
import com.application.areca.TargetGroup;
import com.application.areca.WorkspaceItem;
import com.application.areca.adapters.ConfigurationHandler;
import com.application.areca.adapters.XMLTags;
import com.application.areca.impl.AbstractFileSystemMedium;
import com.application.areca.impl.FileSystemTarget;
import com.application.areca.impl.policy.AccessInformations;
import com.application.areca.impl.policy.DefaultFileSystemPolicy;
import com.application.areca.impl.policy.FileSystemPolicy;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.myJava.file.FileSystemDriversRestorePoint;
import com.myJava.file.FileSystemManager;
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
public class WorkspaceItemImportHelper {
	private static final ResourceManager RM = ResourceManager.instance();
	
	public static void importWorkspaceItems(WorkspaceItem[] items) {
		try {
			// deduplicate the list
			ArrayList list = new ArrayList();
			for (int i = 0; i < items.length; i++) {
				boolean alreadyIncluded = false;
				for (int j = 0; j < items.length; j++) {
					if (i != j && items[i].isChildOf(items[j])) {
						alreadyIncluded = true;
						break;
					}
				}

				if (!alreadyIncluded) {
					list.add(items[i]);
				}
			}

			Iterator iter;
			
			// adjust target configuration to match archive location (if modified)
			iter = list.iterator();
			tryLocateArchives(iter);

			// import items
			iter = list.iterator();
			while (iter.hasNext()) {
				WorkspaceItem item = (WorkspaceItem) iter.next();

				if (item instanceof TargetGroup) {
					TargetGroup group = (TargetGroup) item;
					File file = new File(Application.getInstance().getWorkspace().getPath(), group.getName());
					ConfigurationHandler.getInstance().serialize(group, file, false, false);
				} else {
					FileSystemTarget target = (FileSystemTarget) item;
					ConfigurationHandler.getInstance().serialize(target, new File(Application.getInstance().getWorkspace().getPath()), false, false);
				}
			}

			Application.getInstance().refreshWorkspace();
		} catch (Throwable e) {
			Application.getInstance().handleException(RM.getLabel("error.importgrp.message"), e);
		}
	}
	
	private static void tryLocateArchives(Iterator iter) throws ApplicationException {
		while (iter.hasNext()) {
			WorkspaceItem item = (WorkspaceItem) iter.next();
			
			if (item instanceof TargetGroup) {
				tryLocateArchives(((TargetGroup)item).getIterator());
			} else {
				tryLocateArchives((FileSystemTarget)item);
			}
		}
	}
	
	private static void tryLocateArchives(FileSystemTarget target) throws ApplicationException {
		// Build restore point so we can recover a coherent state of the drivers 
		// registry after having tried multiple hypothesis regarding archives location
		FileSystemDriversRestorePoint rp = FileSystemManager.getInstance().buildRestorePoint();
		
		try {
			AbstractFileSystemMedium medium = (AbstractFileSystemMedium)target.getMedium();
			medium.install();
			
			Logger.defaultLogger().info("Checking archives accessibility for imported target: " + target.getName() + " (" + target.getUid() + ") ...");
			AccessInformations infos = medium.getFileSystemPolicy().checkReachable();
			boolean accessible = infos.isReachable();
			String message = infos.getMessage();
			
			if (accessible) {
				try {
					File[] archives = medium.listArchives(null, null, true);
					if (archives == null || archives.length == 0) {
						accessible = false;
						message = "No archive found";
					}
				} catch (Exception ignored) {
					accessible = false;
					message = ignored.getMessage();
				}
			}
			
			if (accessible) {
				Logger.defaultLogger().info("Archives are accessible.");
			} else {
				Logger.defaultLogger().info("Archives are not accessible (Message: \"" + message + "\") - trying to locate them ...");
				File candidate = locatePotentialStorageDirectory(target);
				if (candidate != null) {
					Logger.defaultLogger().info("Archive storage location found (" + candidate + ")");
					String oldLocation = medium.getFileSystemPolicy().getDisplayableParameters(true);
					String newLocation = FileSystemManager.getAbsolutePath(candidate);
					
					ImportAdjustmentWindow frm = new ImportAdjustmentWindow(target, oldLocation, newLocation);
					Application.getInstance().showDialog(frm);
					
					if (frm.isSaved() && frm.isSavedModifyLocation()) {
						Logger.defaultLogger().info("Adjusting archive location ...");
						String path = frm.getSavedProposedLocation();
						adjustFileSystemPolicy(target, new File(path));
					} else {
						Logger.defaultLogger().info("Target will not be adjusted.");
					}
				} else {
					Logger.defaultLogger().info("Archive storage location was not found.");
				}
			}
		} catch (Throwable e) {
			Logger.defaultLogger().warn("Error while trying to locate archives for " + target.getName() + " (" + target.getUid() + ").");
		} finally {
			// Cleanup
			FileSystemManager.getInstance().applyRestorePoint(rp);
		}
	}
	
	private static File locatePotentialStorageDirectory(FileSystemTarget target) {
		File configPath = target.getLoadedFrom().getSource();
		File candidate;
		
		// Case1 : conf file is contained in target storage directory
		candidate = configPath.getParentFile();
		if (validateCandidate(target, candidate)) {
			return candidate;
		}
		
		AbstractFileSystemMedium medium = (AbstractFileSystemMedium)target.getMedium();
		FileSystemPolicy policy = medium.getFileSystemPolicy();
		String storageSubdirectoryPath = policy.getArchivePath();
		File storageSubdirectory = new File(storageSubdirectoryPath);
		String storageSubdirectoryName = FileSystemManager.getName(storageSubdirectory);
		
		// Case2 : conf file is stored at the same level as storage directory
		File configDir = FileSystemManager.getParentFile(configPath);
		candidate = new File(configDir, storageSubdirectoryName);
		if (validateCandidate(target, candidate)) {
			return candidate;
		}
		
		File configDirParent = FileSystemManager.getParentFile(configDir);
		if (configDirParent != null) {
			
			// Case3 : conf file is stored in a directory which is at the same level as the storage directory
			candidate = new File(configDirParent, storageSubdirectoryName);
			if (validateCandidate(target, candidate)) {
				return candidate;
			}
		}
		
		File[] drives = File.listRoots();
		
		// Case 4 : directory is the same but drive letter has changed ... to be used on Windows only 
		// ... I guess Linux users do not need such assistance
		if (policy instanceof DefaultFileSystemPolicy && OSTool.isSystemWindows()) {
			String path = policy.getArchivePath().replace('\\', '/');
			int idx = path.indexOf('/');
			String suffix = path.substring(idx);
			
			if (drives != null) {
				for (int i=0; i<drives.length; i++) {
					File drive = drives[i];
					candidate = new File (drive, suffix);
					if (validateCandidate(target, candidate)) {
						return candidate;
					}
				}
			}
		}
		
		// Case 5 : try various hypothesis around [c:\], [d:\] etc.
		if (drives != null) {
			for (int i=0; i<drives.length; i++) {
				File drive = drives[i];
				
				if ((candidate = tryHypothesis(target, drive, storageSubdirectoryName)) != null) {
					return candidate;
				}
			}
		}
		
		// Case 6 : try various hypothesis around [userHome]\
		File userHome = new File(OSTool.getUserHome());
		if ((candidate = tryHypothesis(target, userHome, storageSubdirectoryName)) != null) {
			return candidate;
		}
		
		// Case 7 : try various hypothesis around [userDesktop]\
		File userDesktop = new File(userHome, "Desktop");
		if ((candidate = tryHypothesis(target, userDesktop, storageSubdirectoryName)) != null) {
			return candidate;
		}
		
		// Case 8 : try various hypothesis around [userDocuments]\
		File userDocuments = new File(userHome, "Documents");
		if ((candidate = tryHypothesis(target, userDocuments, storageSubdirectoryName)) != null) {
			return candidate;
		}
		
		return null;
	}
	
	private static File tryHypothesis(FileSystemTarget target, File root, String storageSubdirectoryName) {
		File candidate;
		
		// Case a : try [root]\
		candidate = new File(root, storageSubdirectoryName);
		if (validateCandidate(target, candidate)) {
			return candidate;
		}
		
		// Case b : try [root]\[any-first-level-subdirectory]
		String[] childNames = FileSystemManager.list(root);
		if (childNames != null) {
			for (int i=0; i<childNames.length; i++) {
				File child = new File(root, childNames[i]);
				
				if (FileSystemManager.isDirectory(child)) {
					candidate = new File(child, storageSubdirectoryName);
					if (validateCandidate(target, candidate)) {
						return candidate;
					}
				}
			}
		}
		
		return null;
	}
	
	private static void installPolicy(AbstractFileSystemMedium medium, File location) throws ApplicationException {
		FileSystemPolicy currentPolicy = medium.getFileSystemPolicy();
		
		DefaultFileSystemPolicy newPolicy = new DefaultFileSystemPolicy();
		newPolicy.setId(XMLTags.POLICY_HD);
		
		String archivePath = FileSystemManager.getAbsolutePath(location);
		newPolicy.setArchivePath(archivePath);
		newPolicy.setArchiveName(currentPolicy.getArchiveName());
		
		medium.setFileSystemPolicy(newPolicy);
		medium.install();
	}
	
	private static boolean validateCandidate(FileSystemTarget target, File candidate) {
		Logger.defaultLogger().finest("Validating " + candidate + " ...");
		
		// Perform some basic consistency checks on candidate directory
		if (candidate == null) {
			return false;
		}
		
		if (! FileSystemManager.exists(candidate)) {
			return false;
		}
		
		String[] children = FileSystemManager.list(candidate);
		if (children == null || children.length == 0) {
			return false;
		}
		
		AbstractFileSystemMedium medium = (AbstractFileSystemMedium)target.getMedium();
		FileSystemPolicy currentPolicy = medium.getFileSystemPolicy();
		String storageSubdirectoryPath = currentPolicy.getArchivePath();
		File storageSubdirectory = new File(storageSubdirectoryPath);
		String storageSubdirectoryName = FileSystemManager.getName(storageSubdirectory);
		
		String candidateName = FileSystemManager.getName(candidate);
		if (! candidateName.equals(storageSubdirectoryName)) {
			return false;
		}
		
		// Duplicate the target, try to install the medium and see if archives are available
		boolean accessible = false;
		FileSystemTarget clone = (FileSystemTarget)target.duplicate();
		AbstractFileSystemMedium cloneMedium = (AbstractFileSystemMedium)clone.getMedium();
		try {
			installPolicy(cloneMedium, candidate);
			
			File[] archives = null;
			Exception exception = null;
			try {

				archives = cloneMedium.listArchives(null, null, true);
			} catch (Exception e) {
				exception = e;
			}
			
			if (archives == null || archives.length == 0) {
				String errorMessage = "No archives found in " + candidate;
				if (exception != null) {
					errorMessage += " (" + exception.getMessage() + ")";
				}
				Logger.defaultLogger().info(errorMessage);
			} else {
				accessible = true;
			}
		} catch (Throwable ignored) {
		}

		return accessible;
	}
	
	private static void adjustFileSystemPolicy(FileSystemTarget target, File storageDirectory) throws ApplicationException {
		AbstractFileSystemMedium medium = (AbstractFileSystemMedium)target.getMedium();
		FileSystemPolicy oldPolicy = medium.getFileSystemPolicy();
		
		try {
			installPolicy(medium, storageDirectory);
		} catch (Throwable e) {
			Logger.defaultLogger().info("Unable to locate archives for " +  target.getName() + ": " + e.getMessage());
			medium.setFileSystemPolicy(oldPolicy);
			medium.install();
		}
	}
}
