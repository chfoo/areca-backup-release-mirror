package com.application.areca.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.application.areca.AbstractTarget;
import com.application.areca.ApplicationException;
import com.application.areca.Errors;
import com.application.areca.RecoveryEntry;
import com.application.areca.SimulationResult;
import com.application.areca.TargetActions;
import com.application.areca.context.ProcessContext;
import com.application.areca.context.StatusList;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.manifest.ManifestKeys;
import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.iterator.FileSystemIterator;
import com.myJava.file.metadata.FileMetaDataAccessor;
import com.myJava.object.Duplicable;
import com.myJava.object.DuplicateHelper;
import com.myJava.util.errors.ActionError;
import com.myJava.util.errors.ActionReport;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;

/**
 * Target implementation that handles files.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

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
public class FileSystemTarget
extends AbstractTarget
implements TargetActions {

    public static final String RECOVERY_LOCATION_SUFFIX = "rcv";

    protected String sourcesRoot = "";
    protected Set sources;
    protected boolean followSubdirectories = true;
    protected boolean trackEmptyDirectories = false;

    /**
     * Tells whether symbolic are considered as normal files or as symbolic links
     */
    protected boolean trackSymlinks = false;

    public Duplicable duplicate() {
        FileSystemTarget other = new FileSystemTarget();
        copyAttributes(other);
        return other;
    }

    protected void copyAttributes(Object clone) {
        FileSystemTarget other = (FileSystemTarget)clone;
        super.copyAttributes(other);
        other.sourcesRoot = sourcesRoot;
        other.trackSymlinks = trackSymlinks;
        other.followSubdirectories = followSubdirectories;
        other.trackEmptyDirectories = trackEmptyDirectories;
        other.sources = DuplicateHelper.duplicate(sources, false);
    }

    public void setSources(Set sources) {
        this.sources = sources;
        deduplicate(this.sources);
        this.computeSourceRoot();
    }

    public boolean isFollowSubdirectories() {
        return followSubdirectories;
    }

    public void setFollowSubdirectories(boolean followSubdirectories) {
        this.followSubdirectories = followSubdirectories;
    }

    public boolean isTrackEmptyDirectories() {
		return trackEmptyDirectories;
	}

	public void setTrackEmptyDirectories(boolean trackEmptyDirectories) {
		this.trackEmptyDirectories = trackEmptyDirectories;
	}

	/**
     * Removes duplicate sources
     */
    private static void deduplicate(Set sources) {
        HashSet toRemove = new HashSet();
        FileTool tool = FileTool.getInstance();

        Iterator iter1 = sources.iterator();
        while (iter1.hasNext()) {
            File tested = (File)iter1.next();

            Iterator iter2 = sources.iterator();
            while (iter2.hasNext()) {
                File potentialParent = (File)iter2.next();

                if (potentialParent != tested) {  // Yes, it's an instance check
                    if (tool.isParentOf(potentialParent, tested)) {
                        toRemove.add(tested);
                    }
                }
            }
        }

        sources.removeAll(toRemove);
    }

    public Set getSources() {
        return sources;
    }

    private void computeSourceRoot() {
        List paths = new ArrayList();
        int min = Integer.MAX_VALUE;
        if (sources.size() > 0) {
            Iterator iter = this.sources.iterator();
            while (iter.hasNext()) {
                File source = (File)iter.next();
                ArrayList path = new ArrayList();
                computeParents(source, path);
                paths.add(path);
                if (path.size() < min) {
                    min = path.size();
                }
            }

            int divergenceIndex = -1;
            for (int token=0; token<min && divergenceIndex == -1; token++) {
                File current = (File)((List)paths.get(0)).get(token);
                String currentStr = FileSystemManager.getAbsolutePath(current);
                for (int s=1; s<this.sources.size() && divergenceIndex == -1; s++) {
                    File other = (File)((List)paths.get(s)).get(token);
                    if (! currentStr.equals(FileSystemManager.getAbsolutePath(other))) {
                        divergenceIndex = token;
                    }
                }
            }

            if (divergenceIndex == 0) {
                sourcesRoot = "";
            } else if (divergenceIndex == -1) {
                sourcesRoot = FileSystemManager.getAbsolutePath((File)this.sources.iterator().next());
            } else {
                sourcesRoot = FileSystemManager.getAbsolutePath((File)((List)paths.get(0)).get(divergenceIndex - 1));
            }
        } else {
            sourcesRoot = "";
        }
    }

    private static void computeParents(File f, List l) {
        l.add(0, f);
        File parent = FileSystemManager.getParentFile(f);
        if (parent != null) {
            computeParents(parent, l);
        }
    }

    public String getSourcesRoot() {
        return this.sourcesRoot;
    }

    public String getSourceDirectory() {
        if (sourcesRoot.length() == 0) {
            return sourcesRoot;
        } else {
            File f = new File(sourcesRoot);

            if (FileSystemManager.isFile(f)) {
                return FileSystemManager.getAbsolutePath(FileSystemManager.getParentFile(f));
            } else {
                return this.sourcesRoot;
            }
        }
    }

    public boolean isTrackSymlinks() {
        return trackSymlinks;
    }

    public void setTrackSymlinks(boolean trackSymlinks) {
        this.trackSymlinks = trackSymlinks;
    }

    public void commitBackup(ProcessContext context) throws ApplicationException {
        context.getManifest().addProperty(ManifestKeys.UNFILTERED_DIRECTORIES, context.getReport().getUnfilteredDirectories());
        context.getManifest().addProperty(ManifestKeys.UNFILTERED_FILES, context.getReport().getUnfilteredFiles());
        context.getManifest().addProperty(ManifestKeys.SCANNED_ENTRIES, context.getReport().getUnfilteredFiles() + context.getReport().getUnfilteredDirectories() + context.getReport().getFilteredEntries());
        context.getManifest().addProperty(ManifestKeys.SOURCE_PATH, this.sourcesRoot);

        super.commitBackup(context);
    }

    /**
     * Returns the next element. It may be a file or a directory.
     * <BR>Filters are applied.
     */
    public RecoveryEntry nextElement(ProcessContext context) throws ApplicationException {
    	if (context.getFileSystemIterator() == null) {
    		return null;
    	} else {
    		File f = context.getFileSystemIterator().nextFile();
    		if (f == null) {
    			context.getReport().setUnfilteredDirectories((int)context.getFileSystemIterator().getDirectories());
    			context.getReport().setUnfilteredFiles((int)context.getFileSystemIterator().getFiles());
    			context.getReport().setFilteredEntries((int)context.getFileSystemIterator().getFiltered());
    			return null;
    		} else {
    			FileSystemRecoveryEntry entry = new FileSystemRecoveryEntry(this.getSourceDirectory(), f);
                entry.setSize(FileSystemManager.length(f));
                try {
					entry.setLink(this.isTrackSymlinks() && FileMetaDataAccessor.TYPE_LINK == FileSystemManager.getType(f));
				} catch (IOException e) {
					Logger.defaultLogger().error(e);
					throw new ApplicationException(e);
				}

                if (entry.getKey().length() == 0) {
                	return nextElement(context);
                } else {
                	return entry;
                }
    		}
    	}
    }

	public void open(Manifest manifest, ProcessContext context, String backupScheme) throws ApplicationException {
        Logger.defaultLogger().info("Initializing backup context ...");
        Logger.defaultLogger().info("Global source root : " + this.sourcesRoot);

        super.open(manifest, context, backupScheme);
        initCurrentLevel(context);
        Logger.defaultLogger().info("Backup context initialized.");
    }

    private void initCurrentLevel(ProcessContext context) throws ApplicationException {
        Iterator iter = this.sources.iterator();
        String[] sourceArray = new String[this.sources.size()];
        String root = this.getSourceDirectory();
        for (int i=0; i<sources.size(); i++) {
            File source = (File)iter.next();
            Logger.defaultLogger().info("Registering source directory : " + FileSystemManager.getAbsolutePath(source));
            sourceArray[i] = FileSystemManager.getAbsolutePath(source).substring(root.length());
        }
        context.setRootCount(this.sources.size());
        File fRoot = null;
        if (root != null && root.length() != 0) {
        	fRoot = new File(root);
        }
        if (this.sources.size() != 0) {
        	FileSystemIterator fsIter = new FileSystemIterator(fRoot, sourceArray, ! this.trackSymlinks, this.followSubdirectories, trackEmptyDirectories, true);
        	fsIter.setLogProgress(true);
        	fsIter.setFilter(this.filterGroup);
        	fsIter.setMonitor(context.getTaskMonitor().getCurrentActiveSubTask());
        	context.setFileSystemIterator(fsIter);
        }
    }

    public synchronized SimulationResult processSimulateImpl(ProcessContext context, boolean returnDetailedResults) throws ApplicationException {
        Logger.defaultLogger().info("Initializing simulation context ...");
        this.initCurrentLevel(context);
        Logger.defaultLogger().info("Simulation context initialized.");
        return super.processSimulateImpl(context, returnDetailedResults);
    }

    private File buildRecoveryFile(String destination) {
        File f = new File(normalizeDestination(destination), RECOVERY_LOCATION_SUFFIX);
        for (int i=0; FileSystemManager.exists(f); i++) {
            f = new File(normalizeDestination(destination), RECOVERY_LOCATION_SUFFIX + i);
        }
        return f;
    }

    public void processArchiveCheck(
    		String destination,
    		boolean checkOnlyArchiveContent,
    		GregorianCalendar date,
    		ProcessContext context) throws ApplicationException {
    	try {
    		validateTargetState(ACTION_RECOVER, context);
    		try {
    			this.medium.checkArchives(destination, checkOnlyArchiveContent, date, context);
    		} catch (TaskCancelledException e) {
    			throw new ApplicationException(e);
    		}
    		
			// Set status
			if (context.hasRecoveryProblem()) {
				context.getReport().getStatus().addItem(StatusList.KEY_ARCHIVE_CHECK, "The archives were not successfully checked.");
			} else {
				context.getReport().getStatus().addItem(StatusList.KEY_ARCHIVE_CHECK);	
			}
		} catch (Exception e) {
			Logger.defaultLogger().error("An error has been caught : ", e);
			String msg = "The archives were not successfully checked. (" + e.getMessage() + ")";
			context.getReport().getStatus().addItem(StatusList.KEY_ARCHIVE_CHECK, msg);
			throw wrapException(e);
		}
    }

    /**
     * Recover the data
     */
    public void processRecoverImpl(
    		String destination,
    		String[] filters,
    		GregorianCalendar date,
    		boolean keepDeletedEntries,
    		boolean checkRecoveredEntries,
    		ProcessContext context
    ) throws ApplicationException {
        try {
			this.medium.recover(
			        buildRecoveryFile(destination),
			        filters,
			        date,
			        keepDeletedEntries,
			        checkRecoveredEntries,
			        context
			 );
		} catch (TaskCancelledException e) {
			throw new ApplicationException(e);
		}
    }

    /**
     * Recover a specific entry
     */
    public void processRecoverImpl(
    		String destination,
    		GregorianCalendar date,
    		String entry,
    		boolean checkRecoveredEntries,
    		ProcessContext context
    ) throws ApplicationException {
        File dest = buildRecoveryFile(destination);
        try {
			this.medium.recover(
			        dest,
			        new String[] {entry},
			        date,
			        false,
			        checkRecoveredEntries,
			        context);
		} catch (TaskCancelledException e1) {
			throw new ApplicationException(e1);
		}

        File recoveredFile = new File(dest, entry);
        File targetFile = new File(normalizeDestination(destination), FileSystemManager.getName(recoveredFile));

        Logger.defaultLogger().info("Moving " + FileSystemManager.getAbsolutePath(recoveredFile) + " to " + FileSystemManager.getAbsolutePath(targetFile));
        FileSystemManager.renameTo(recoveredFile, targetFile);
        try {
            FileTool.getInstance().delete(dest, true);
        } catch (IOException e) {
            Logger.defaultLogger().error(e);
            throw new ApplicationException(e);
        }
    }

    private static String normalizeDestination(String destination) {
        if (destination == null) {
            return null;
        } else if (FileNameUtil.endsWithSeparator(destination)) {
            return destination.substring(0, destination.length() - 1);
        } else {
            return destination;
        }
    }

    protected String getSpecificTargetDescription() {
        return "Root : " + (this.sourcesRoot.length() != 0 ? this.sourcesRoot: "");
    }

    /**
     * Check the target's state
     */
    public ActionReport checkTargetState(int action) {

        // Validation
        ActionReport result = super.checkTargetState(action);

        if (action != ACTION_RECOVER && action != ACTION_MERGE_OR_DELETE) {
            Iterator iter = this.sources.iterator();
            while (iter.hasNext()) {
                File src = (File)iter.next();
                if (! FileSystemManager.exists(src)) {
                    result.addError(new ActionError(Errors.ERR_C_BASETARGETPATH, Errors.ERR_M_BASETARGETPATH + " (" + FileSystemManager.getAbsolutePath(src) + ")"));
                }
            }
        }

        return result;
    }

    public Manifest buildDefaultMergeManifest(GregorianCalendar fromDate, GregorianCalendar toDate) throws ApplicationException {
        if (! (this.medium instanceof AbstractIncrementalFileSystemMedium)) {
            throw new IllegalArgumentException("Only incremental mediums are supported by this method");
        } else {
            AbstractIncrementalFileSystemMedium mdm = (AbstractIncrementalFileSystemMedium)this.medium;

            GregorianCalendar mFromDate = (GregorianCalendar)fromDate.clone();
            mFromDate.add(Calendar.MILLISECOND, -1);
            GregorianCalendar mToDate = (GregorianCalendar)toDate.clone();
            mToDate.add(Calendar.MILLISECOND, 1);
            File[] recoveredFiles = mdm.listArchives(mFromDate, mToDate);
            return mdm.buildDefaultMergeManifest(recoveredFiles, fromDate, toDate);
        }
    }
}

