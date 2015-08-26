package com.application.areca.impl;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.application.areca.AbstractTarget;
import com.application.areca.ApplicationException;
import com.application.areca.ArecaRawFileList;
import com.application.areca.CheckParameters;
import com.application.areca.RecoveryEntry;
import com.application.areca.SimulationResult;
import com.application.areca.TargetActions;
import com.application.areca.context.ProcessContext;
import com.application.areca.context.StatusList;
import com.application.areca.impl.copypolicy.AbstractCopyPolicy;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.manifest.ManifestKeys;
import com.application.areca.metadata.transaction.TransactionPoint;
import com.application.areca.processor.Processor;
import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.iterator.FileSystemIterator;
import com.myJava.file.iterator.SourcesHelper;
import com.myJava.file.metadata.FileMetaDataAccessor;
import com.myJava.object.Duplicable;
import com.myJava.object.DuplicateHelper;
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
    	this.sourcesRoot = SourcesHelper.computeSourceRoot(this.sources);
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

    public String getSourcesRoot() {
        return this.sourcesRoot;
    }

    public String getSourceDirectory() {
    	return SourcesHelper.computeSourceDirectory(sourcesRoot);
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
    public boolean nextElement(ProcessContext context, RecoveryEntry cursor) throws ApplicationException {
		//Chronometer.instance().start("nextElement");
		
    	if (context.getFileSystemIterator() == null) {
    		//Chronometer.instance().stop("nextElement");
    		return false;
    	} else {
    		File f = context.getFileSystemIterator().nextFile();
    		if (f == null) {
    			context.getReport().setUnfilteredDirectories((int)context.getFileSystemIterator().getDirectories());
    			context.getReport().setUnfilteredFiles((int)context.getFileSystemIterator().getFiles());
    			context.getReport().setFilteredEntries((int)context.getFileSystemIterator().getFiltered());
        		//Chronometer.instance().stop("nextElement");
    			return false;
    		} else {
    			FileSystemRecoveryEntry entry = (FileSystemRecoveryEntry)cursor;
    			entry.init(context.getFileSystemIterator().getRoot() == null ? null : FileSystemManager.getAbsolutePath(context.getFileSystemIterator().getRoot()), f);

                if (entry.getKey().length() == 0) {
            		//Chronometer.instance().stop("nextElement");
            		return nextElement(context, cursor);
                } else {
                    entry.setSize(FileSystemManager.length(f));
                    try {
    					entry.setLink(this.isTrackSymlinks() && FileMetaDataAccessor.TYPE_LINK == FileSystemManager.getType(f));
    				} catch (IOException e) {
    					Logger.defaultLogger().error(e);
    					throw new ApplicationException(e);
    				}
                    
            		//Chronometer.instance().stop("nextElement");
                	return true;
                }
    		}
    	}
    }

	protected void open(TransactionPoint transactionPoint, ProcessContext context) throws ApplicationException {
        Logger.defaultLogger().info("Reading backup context ...");
		super.open(transactionPoint, context);
        Logger.defaultLogger().info("Backup context initialized.");
	}

	public void open(Manifest manifest, ProcessContext context, String backupScheme) throws ApplicationException {
        Logger.defaultLogger().info("Initializing backup context ...");
        Logger.defaultLogger().info("Global source root : " + this.sourcesRoot);

        super.open(manifest, context, backupScheme);
        initCurrentLevel(context);
        Logger.defaultLogger().info("Backup context initialized.");
    }

    private void initCurrentLevel(ProcessContext context) throws ApplicationException {
    	if (context.getFileSystemIteratorBuilder() == null) {
    		context.setFileSystemIteratorBuilder(new DefaultFileSystemIteratorBuilder());
    	}
    	
    	FileSystemIterator iterator = context.getFileSystemIteratorBuilder().buildFileSystemIterator(this);
    	if (iterator != null) {
    		iterator.setLogProgress(true);
    		iterator.setFilter(filterGroup);
    		iterator.setMonitor(context.getTaskMonitor().getCurrentActiveSubTask());
        	context.setFileSystemIterator(iterator);
    	}
    }

    public synchronized SimulationResult processSimulateImpl(ProcessContext context, boolean returnDetailedResults) throws ApplicationException {
        Logger.defaultLogger().info("Initializing simulation context ...");
        this.initCurrentLevel(context);
        Logger.defaultLogger().info("Simulation context initialized.");
        return super.processSimulateImpl(context, returnDetailedResults);
    }

    public static File buildRecoveryFile(String destination, boolean appendSuffix) {
    	if (appendSuffix) {
            File f = new File(normalizeDestination(destination), RECOVERY_LOCATION_SUFFIX);
            for (int i=0; FileSystemManager.exists(f); i++) {
                f = new File(normalizeDestination(destination), RECOVERY_LOCATION_SUFFIX + i);
            }
            return f;
    	} else {
    		return new File(destination);
    	}
    }

    public void processArchiveCheck(
    		CheckParameters checkParams,
    		GregorianCalendar date,
    		Set ignoreList,
    		boolean runProcessors,
    		ProcessContext context) throws ApplicationException {
    	try {
    		validateTargetState(ACTION_RECOVER, context);
    		
    		runPreProcessors(Processor.ACTION_CHECK, 0.1, runProcessors, context);
    		
			double remaining = 
					1.0 
					- (! runProcessors || this.postProcessors.isEmpty(Processor.ACTION_CHECK) ? 0 : 0.1) 
					- (! runProcessors || this.preProcessors.isEmpty(Processor.ACTION_CHECK) ? 0 : 0.1);
			
			context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(remaining, "check");
    		
    		try {
    			String destination = null;
    			if (checkParams.isUseSpecificLocation()) {
    				destination = checkParams.getSpecificLocation();
    			}
    			this.medium.checkArchives(destination, checkParams.isCheckLastArchiveOnly(),checkParams.isSimulateRecovery(), date, ignoreList, context);
    		} catch (TaskCancelledException e) {
    			throw new ApplicationException(e);
    		}

			// Set status
			if (context.getReport().hasRecoveryIssues()) {
				context.getReport().getStatus().addItem(StatusList.KEY_ARCHIVE_CHECK, "The archives were not successfully checked.");
			} else {
				context.getReport().getStatus().addItem(StatusList.KEY_ARCHIVE_CHECK);	
			}
		} catch (Exception e) {
			Logger.defaultLogger().error("An error has been caught : ", e);
			String msg = "The archives were not successfully checked. (" + e.getMessage() + ")";
			context.getReport().getStatus().addItem(StatusList.KEY_ARCHIVE_CHECK, msg);
			throw wrapException(e);
		} finally {
			context.getReport().setStopMillis();
			
    		runPostProcessors(Processor.ACTION_CHECK, 0.1, runProcessors, context);
		}
    }

    /**
     * Recover the data
     */
    public void processRecoverImpl(
    		String destination,
    		boolean appendSuffix,
    		ArecaRawFileList filters,
    		AbstractCopyPolicy policy,
    		GregorianCalendar date,
    		boolean keepDeletedEntries,
    		boolean checkRecoveredEntries,
    		ProcessContext context
    ) throws ApplicationException {
        try {
			this.medium.recover(
			        buildRecoveryFile(destination, appendSuffix),
			        filters,
					policy,
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
    		AbstractCopyPolicy policy,
    		boolean checkRecoveredEntries,
    		ProcessContext context
    ) throws ApplicationException {
        File dest = buildRecoveryFile(destination, true);
        try {
			this.medium.recover(
			        dest,
			        new ArecaRawFileList(entry),
					policy,
			        date,
			        false,
			        checkRecoveredEntries,
			        context);
		} catch (TaskCancelledException e1) {
			throw new ApplicationException(e1);
		}

        File recoveredFile = new File(dest, entry);
        File targetFile = new File(normalizeDestination(destination), FileSystemManager.getName(recoveredFile));

        Logger.defaultLogger().info("Moving " + FileSystemManager.getDisplayPath(recoveredFile) + " to " + FileSystemManager.getDisplayPath(targetFile));
        FileSystemManager.renameTo(recoveredFile, targetFile);
        try {
            FileTool.getInstance().delete(dest);
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

    public Manifest buildDefaultMergeManifest(GregorianCalendar fromDate, GregorianCalendar toDate) throws ApplicationException {
        if (! (this.medium instanceof AbstractIncrementalFileSystemMedium)) {
            throw new IllegalArgumentException("Only incremental mediums are supported by this method");
        } else {
            AbstractIncrementalFileSystemMedium mdm = (AbstractIncrementalFileSystemMedium)this.medium;

            GregorianCalendar mFromDate = (GregorianCalendar)fromDate.clone();
            mFromDate.add(Calendar.MILLISECOND, -1);
            GregorianCalendar mToDate = (GregorianCalendar)toDate.clone();
            mToDate.add(Calendar.MILLISECOND, 1);
            File[] recoveredFiles = mdm.listArchives(mFromDate, mToDate, true);
            return mdm.buildDefaultMergeManifest(recoveredFiles, fromDate, toDate);
        }
    }
}

