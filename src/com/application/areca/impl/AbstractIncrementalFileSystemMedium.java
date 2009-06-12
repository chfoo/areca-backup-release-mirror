package com.application.areca.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ApplicationException;
import com.application.areca.ArecaTechnicalConfiguration;
import com.application.areca.EntryArchiveData;
import com.application.areca.EntryStatus;
import com.application.areca.LogHelper;
import com.application.areca.MemoryHelper;
import com.application.areca.RecoveryEntry;
import com.application.areca.StoreException;
import com.application.areca.TargetActions;
import com.application.areca.Utils;
import com.application.areca.cache.ArchiveManifestCache;
import com.application.areca.context.ProcessContext;
import com.application.areca.context.RecoveryResult;
import com.application.areca.impl.handler.ArchiveHandler;
import com.application.areca.impl.tools.ArchiveComparator;
import com.application.areca.impl.tools.ArchiveNameFilter;
import com.application.areca.impl.tools.RecoveryFilterMap;
import com.application.areca.metadata.AbstractMetaDataEntry;
import com.application.areca.metadata.AbstractMetaDataFileIterator;
import com.application.areca.metadata.MetadataConstants;
import com.application.areca.metadata.content.ArchiveContentAdapter;
import com.application.areca.metadata.content.ArchiveContentManager;
import com.application.areca.metadata.content.ArchiveContentParser;
import com.application.areca.metadata.content.ContentEntry;
import com.application.areca.metadata.content.ContentFileIterator;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.manifest.ManifestKeys;
import com.application.areca.metadata.manifest.ManifestManager;
import com.application.areca.metadata.trace.ArchiveTraceAdapter;
import com.application.areca.metadata.trace.ArchiveTraceManager;
import com.application.areca.metadata.trace.ArchiveTraceParser;
import com.application.areca.metadata.trace.EntrySetTraceHandler;
import com.application.areca.metadata.trace.RebuildOtherFilesTraceHandler;
import com.application.areca.metadata.trace.TraceEntry;
import com.application.areca.metadata.trace.TraceFileIterator;
import com.application.areca.metadata.trace.TraceMerger;
import com.application.areca.metadata.trace.UpdateMetaDataTraceHandler;
import com.application.areca.search.DefaultSearchCriteria;
import com.application.areca.search.SearchCriteria;
import com.application.areca.search.SearchMatcher;
import com.application.areca.search.SearchResultItem;
import com.application.areca.search.TargetSearchResult;
import com.myJava.file.EventInputStream;
import com.myJava.file.FileFilterList;
import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.HashInputStreamListener;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.file.iterator.FilePathComparator;
import com.myJava.file.iterator.FileSystemIterator;
import com.myJava.file.metadata.FileMetaDataAccessor;
import com.myJava.file.metadata.FileMetaDataSerializationException;
import com.myJava.util.CalendarUtils;
import com.myJava.util.Util;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;
import com.myJava.util.threadmonitor.ThreadMonitor;
import com.myJava.util.threadmonitor.ThreadMonitorItem;

/**
 * Medium that implements incremental storage
 * 
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
public abstract class AbstractIncrementalFileSystemMedium 
extends AbstractFileSystemMedium 
implements TargetActions {

	private static class DirectoryData {
		public long size;
		public boolean exists;
		public TraceEntry entry;
	}

	protected static final boolean DEBUG_MODE = ArecaTechnicalConfiguration.get().isBackupDebug();
	protected static final boolean CHECK_DEBUG_MODE = ArecaTechnicalConfiguration.get().isCheckDebug();
	protected static final boolean TH_MON_ENABLED = ArecaTechnicalConfiguration.get().isThreadMonitorEnabled();
	protected static final long TH_MON_DELAY = ArecaTechnicalConfiguration.get().getThreadMonitorDelay();
	
	/**
	 * Trace filename
	 */
	protected static final String TRACE_FILE = "trace";

	/**
	 * Content filename
	 */
	protected static final String CONTENT_FILE = "content";

	/**
	 * hash filename
	 */
	protected static final String HASH_FILE = "hash";

	/**
	 * Metadata filename
	 */
	protected static final String METADATA_FILE = "metadata";

	/**
	 * Temporary merge location
	 */
	protected static final String TMP_MERGE_LOCATION = "merge";

	/**
	 * Temporary directory used during archive check
	 */
	protected static final String CHECK_DESTINATION = "chk";

	/**
	 * Filenames reserved by Areca
	 */
	protected static final String[] RESERVED_NAMES = 
		new String[] {METADATA_FILE, TMP_MERGE_LOCATION, CHECK_DESTINATION, HISTORY_NAME};

	/**
	 * Tells whether file permissions shall be tracked or not
	 */
	protected boolean trackPermissions = false;

	/**
	 * Tells whether many archives shall be created on just one single archive
	 */
	protected boolean imageBackups = false;

	/**
	 * Handler for archive processing
	 */
	protected ArchiveHandler handler;

	public Manifest buildDefaultBackupManifest() throws ApplicationException {
		Manifest manifest = new Manifest(Manifest.TYPE_BACKUP);

		Manifest lastMf = ArchiveManifestCache.getInstance().getManifest(this, this.getLastArchive());
		if (lastMf != null) {
			manifest.setTitle(lastMf.getTitle());
		}

		return manifest;
	}

	public Manifest buildDefaultMergeManifest(File[] recoveredArchives, GregorianCalendar fromDate, GregorianCalendar toDate) throws ApplicationException {
		if (toDate != null) {
			toDate = (GregorianCalendar)toDate.clone();
			toDate.add(GregorianCalendar.MILLISECOND, 1);
		}
		if (fromDate != null) {
			fromDate = (GregorianCalendar)fromDate.clone();
			fromDate.add(GregorianCalendar.MILLISECOND, -1);
		}

		Manifest manifest = new Manifest(Manifest.TYPE_MERGE);
		manifest.setDate(toDate);
		if (fromDate == null) {
			manifest.setTitle("Merge as of " + CalendarUtils.getDateToString(toDate));            
		} else {
			manifest.setTitle("Merge from " + CalendarUtils.getDateToString(fromDate) + " to " + CalendarUtils.getDateToString(toDate));
		}
		StringBuffer sb = new StringBuffer();

		for (int i = recoveredArchives.length - 1; i>=0; i--) {
			Manifest mf = ArchiveManifestCache.getInstance().getManifest(this, recoveredArchives[i]);
			if (mf != null) {
				if (i != recoveredArchives.length - 1) {
					sb.append("\n\n");
				}
				sb.append(Utils.formatDisplayDate(mf.getDate()));
				if ((mf.getTitle() != null && mf.getTitle().length() != 0) || (mf.getDescription() != null && mf.getDescription().length() != 0)) {
					sb.append(" :");
				}
				if (mf.getTitle() != null && mf.getTitle().length() != 0) {
					sb.append(" ");
					sb.append(mf.getTitle());
				}
				if (mf.getDescription() != null && mf.getDescription().length() != 0) {
					sb.append("\n");
					sb.append(mf.getDescription());
				}
			}
		}
		manifest.setDescription(sb.toString());    
		return manifest;
	}

	/**
	 * Check the archive denoted by the date passed as argument.
	 * <BR>Files are recovered at the location passed as argument and verified against their hash code.
	 * <BR>The result is stored in the context (see {@link ProcessContext#getUncheckedRecoveredFiles()} and
	 * {@link ProcessContext#getInvalidRecoveredFiles()}).
	 */
	public void checkArchives(
			Object destination, 
			boolean checkOnlyArchiveContent, 
			GregorianCalendar date, 
			ProcessContext context) 
	throws ApplicationException, TaskCancelledException {
		try {
			// Compute recovery destination
			File destinationRoot = destination == null ? fileSystemPolicy.getArchiveDirectory() : new File((String)destination);
			String destinationName = CHECK_DESTINATION;
			int suffix = 0;
			while (FileSystemManager.exists(new File(destinationRoot, destinationName))) {
				destinationName = CHECK_DESTINATION + suffix++;
			}
			File destinationFile = new File(destinationRoot, destinationName);
			context.getInfoChannel().print("Checking archive (working directory : " + FileSystemManager.getAbsolutePath(destinationFile) + ") ...");

			// Get the trace file
			File lastArchive = getLastArchive(null, date);
			File traceFile = ArchiveTraceManager.resolveTraceFileForArchive(this, lastArchive);

			// Recover at a temporary location - activate the archive check option
			GregorianCalendar fromDate = null;
			if (checkOnlyArchiveContent && handler.autonomousArchives()) {
				if (date == null) {
					// No date passed as argument -> use last archive
					Manifest mf = ArchiveManifestCache.getInstance().getManifest(this, lastArchive);
					if (mf != null) {
						date = mf.getDate();
					}
				}

				if (date != null) {
					fromDate = (GregorianCalendar)date.clone();
					fromDate.add(GregorianCalendar.MILLISECOND, -1);
				}
			}
			recover(
					destinationFile, 
					null, 
					1, 
					fromDate, 
					date, 
					traceFile, 
					ArchiveHandler.MODE_RECOVER,
					false,
					true, 
					context
			);
		} catch (IOException e) {
			Logger.defaultLogger().error("Error during archive verification.", e);
			throw new ApplicationException(e);
		} catch (RuntimeException e) {
			Logger.defaultLogger().error("Error during archive verification.", e);
			throw new ApplicationException(e);
		} finally {
			try {
				// Destroy the recovered data
				if (context.getRecoveryDestination() == null) {
					context.getInfoChannel().print("No archive to check.");
				} else {
					context.getInfoChannel().print("Deleting recovered files (" + FileSystemManager.getAbsolutePath(context.getRecoveryDestination()) + ") ...");
					if (
							FileSystemManager.exists(context.getRecoveryDestination())) {
						FileTool.getInstance().delete(context.getRecoveryDestination(), true);
					}
					context.getInfoChannel().print("Recovered files deleted.");
				}
			} catch (IOException e) {
				Logger.defaultLogger().error("Error during archive verification.", e);
				throw new ApplicationException(e);
			}
		}
	}

	public abstract void cleanLocalCopies(
			List copies, 
			ProcessContext context
	) throws IOException, ApplicationException;

	public void cleanMerge(ProcessContext context) throws IOException {
		if (context.getCurrentArchiveFile() != null && context.getRecoveryDestination() != null && ! context.getCurrentArchiveFile().equals(context.getRecoveryDestination())) {
			AbstractFileSystemMedium.tool.delete(context.getRecoveryDestination(), true);
		}
	}

	public void closeSimulation(ProcessContext context) throws ApplicationException {
		try {
			TraceFileIterator iter = context.getReferenceTrace();
			if (iter != null) {
				iter.close();
			}
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		}
	}

	/**
	 * Close the archive
	 */
	public void commitBackup(ProcessContext context) throws ApplicationException {
		if (TH_MON_ENABLED) {
			ThreadMonitor.getInstance().remove(this.getTarget().getUid());
		}
		
		this.target.secureUpdateCurrentTask("Committing backup ...", context);
		try {  
			// Close the trace file
			context.getTraceAdapter().close();
			context.setTraceAdapter(null);

			// Close the archive
			this.closeArchive(context); 

			// Close the handler
			this.handler.close(context);

			// Close the content file
			context.getContentAdapter().close();
			context.setContentAdapter(null);

			// Close the hash file
			context.getHashAdapter().close();
			context.setHashAdapter(null);

			// Add properties to manifest
			context.getManifest().addProperty(ManifestKeys.UNMODIFIED_FILES, context.getReport().getIgnoredFiles());
			context.getManifest().addProperty(ManifestKeys.ARCHIVE_SIZE, context.getOutputStreamListener().getWritten()); 
			context.getManifest().addProperty(ManifestKeys.STORED_FILES, context.getReport().getSavedFiles());
			context.getManifest().addProperty(ManifestKeys.ARCHIVE_NAME, FileSystemManager.getName(context.getCurrentArchiveFile()));

			// Store the manifest
			this.storeManifest(context);     

			// Convert the archive : commit
			this.convertArchiveToFinal(context);

			// Create a copy of the target's XML configuration
			if (ArecaTechnicalConfiguration.get().isXMLBackup()) {
				this.target.secureUpdateCurrentTask("Creating a copy of the target's XML configuration ...", context);
				this.storeTargetConfigBackup(context);
			}

			// Close the trace iterator
			if (context.getReferenceTrace() != null) {
				context.getReferenceTrace().close();
			}

			// Flush all local files
			FileSystemManager.getInstance().flush(context.getCurrentArchiveFile());

			this.target.secureUpdateCurrentTask("Commit completed.", context);
		} catch (Exception e) {
			Logger.defaultLogger().error(e);
			if (e instanceof ApplicationException) {
				throw (ApplicationException)e;
			} else {
				throw new ApplicationException(e);
			}
		} finally {
			File archive = this.getLastArchive();

			// Just for security reasons : in some cases (Directory non incremental mediums in particular)
			// the data caches won't detect that the archive content has changed and won't refresh their data
			// By doing so, we enforce the cache refresh.
			ArchiveManifestCache.getInstance().remove(this, archive); 
		}
	}

	public void commitMerge(ProcessContext context) throws ApplicationException {
		if (! this.imageBackups) {
			this.target.secureUpdateCurrentTask("Committing merge ...", context);
			super.commitMerge(context);

			try {
				// Clean temporary data
				this.cleanMerge(context);

				// Close the archive
				this.closeArchive(context); 

				File[] recoveredFiles = context.getReport().getRecoveryResult().getRecoveredArchivesAsArray();
				File[] ignoredFiles = context.getReport().getRecoveryResult().getIgnoredArchivesAsArray();

				// Delete unprocessed archives
				Logger.defaultLogger().info("Deleting unnecessary archives : " + ignoredFiles.length + " archives.");
				for (int i=0; i<ignoredFiles.length; i++) {
					Logger.defaultLogger().info("Deleting " + FileSystemManager.getAbsolutePath(ignoredFiles[i]) + " ...");
					this.deleteArchive(ignoredFiles[i]);                       
				}

				if (recoveredFiles.length >= 2) {
					// Delete recovered archives
					Logger.defaultLogger().info("Deleting recovered archives : " + recoveredFiles.length + " archives.");
					for (int i=0; i<recoveredFiles.length; i++) {
						Logger.defaultLogger().info("Deleting " + FileSystemManager.getAbsolutePath(recoveredFiles[i]) + " ...");
						this.deleteArchive(recoveredFiles[i]);                       
					}

					// Commit the archive
					this.convertArchiveToFinal(context);
					this.target.secureUpdateCurrentTask("Merge completed - " + context.getReport().getRecoveryResult().getProcessedArchives().size() + " archives merged.", context);
				} else {
					this.target.secureUpdateCurrentTask("Merge completed - No archive merged.", context);
				}

			} catch (IOException e) {		
				Logger.defaultLogger().error("Exception caught during merge commit.", e);
				this.rollbackMerge(context);
				throw new ApplicationException(e);
			}
		}
	}

	public abstract void completeLocalCopyCleaning(
			File copy, 
			ProcessContext context
	) throws IOException, ApplicationException;

	/**
	 * Ensure that the stored files are present on the local disk. Useful for some archive handlers that need
	 * to ensure this before processing the archives.
	 * <BR>Return a set of directories where local copies can be found.
	 * <BR>This set is :
	 * <BR>- either of length 1 (if overrideRecoveredFiles = true)
	 * <BR>- either of exact same length as archivesToProcess
	 */
	public abstract File[] ensureLocalCopy(
			File[] archivesToProcess, 
			boolean overrideRecoveredFiles,
			File destination,
			RecoveryFilterMap filtersByArchive, 
			ProcessContext context
	) throws IOException, ApplicationException, TaskCancelledException;

	/**
	 * Returns the length of the archive passed as argument if it can be computed.
	 * <BR>Returns -1 otherwise (for instance if the file system driver is too slow to compute the size recursively)
	 */
	public long getArchiveSize(File archive, boolean forceComputation) throws ApplicationException {
		Manifest manifest = ArchiveManifestCache.getInstance().getManifest(
				this, 
				archive
		);

		if (manifest != null) {
			long prp = manifest.getLongProperty(ManifestKeys.ARCHIVE_SIZE, -1);
			if (prp == -1 && (forceComputation || FileSystemManager.isFile(archive) || FileSystemManager.getAccessEfficiency(archive) > FileSystemDriver.ACCESS_EFFICIENCY_POOR)) {
				try {
					Logger.defaultLogger().info("Computing size for " + FileSystemManager.getAbsolutePath(archive));
					prp = FileTool.getInstance().getSize(archive);
				} catch (FileNotFoundException e) {
					Logger.defaultLogger().error("Error computing size for " + FileSystemManager.getAbsolutePath(archive), e);
				}
				manifest.addProperty(ManifestKeys.ARCHIVE_SIZE, String.valueOf(prp));
			}
			return prp;
		} else {
			return 0;
		}
	}

	/**
	 * Return the final content file name
	 */
	public String getContentFileName() {
		return CONTENT_FILE;
	}

	public List getEntries(AggregatedViewContext context, String root, GregorianCalendar date) throws ApplicationException {
		return getAggregatedView(context, root, date, false);
	}

	public ArchiveHandler getHandler() {
		return handler;
	}

	/**
	 * Return the final hash file name
	 */
	public String getHashFileName() {
		return HASH_FILE;
	}

	/**
	 * Returns the last archive for a given date.
	 */
	public File getLastArchive(String backupScheme, GregorianCalendar date) throws ApplicationException {
		String defaultName = computeArchivePath(date);
		File defaultFile = new File(defaultName);

		if (FileSystemManager.exists(defaultFile) && hasBackupScheme(defaultFile, backupScheme) && isCommitted(defaultFile)) {
			return defaultFile;
		} else {
			File[] archives = listArchives(null, date);
			if (archives == null || archives.length == 0) {
				return null;
			} else {
				// First attempt : rely on the archive's "backup scheme" flag
				for (int i=archives.length - 1; i>=0; i--) {
					if (hasBackupScheme(archives[i], backupScheme)) {
						return archives[i];    
					}
				}

				// Special case : if we are looking for a full backup then 
				//                       the first archive must be a full backup
				if (backupScheme != null && backupScheme.equals(AbstractRecoveryTarget.BACKUP_SCHEME_FULL)) {
					return archives[0];
				}

				return null;
			}
		}
	}

	public List getLogicalView(AggregatedViewContext context, String root, boolean aggregated) throws ApplicationException {
		return getAggregatedView(context, root, null, aggregated);
	}

	/**
	 * Return the final metadata file name
	 */
	public String getMetaDataFileName() {
		return METADATA_FILE;
	} 

	/**
	 * Return the final trace file name
	 */
	public String getTraceFileName() {
		return TRACE_FILE;            
	}    

	public void initMergeManifest(File[] recoveredArchives, GregorianCalendar fromDate, GregorianCalendar toDate, Manifest manifest) throws ApplicationException {
		if (toDate != null) {
			toDate = (GregorianCalendar)toDate.clone();
			toDate.add(GregorianCalendar.MILLISECOND, 1);
		}
		if (fromDate != null) {
			fromDate = (GregorianCalendar)fromDate.clone();
			fromDate.add(GregorianCalendar.MILLISECOND, -1);
		}

		manifest.setDate(toDate);

		boolean hasFullBackup = false;
		boolean hasDifferentialBackup = false;

		for (int i = recoveredArchives.length - 1; i>=0; i--) {
			Manifest mf = ArchiveManifestCache.getInstance().getManifest(this, recoveredArchives[i]);
			if (mf != null) {
				String prp = mf.getStringProperty(ManifestKeys.OPTION_BACKUP_SCHEME);
				if (prp == null || prp.equals(AbstractRecoveryTarget.BACKUP_SCHEME_INCREMENTAL)) {
					// do nothing
				} else if (prp.equals(AbstractRecoveryTarget.BACKUP_SCHEME_DIFFERENTIAL)) {
					hasDifferentialBackup = true;
				} else if (prp.equals(AbstractRecoveryTarget.BACKUP_SCHEME_FULL)) {
					hasFullBackup = true;
				}
			}
		}  
		if (hasFullBackup) {
			manifest.addProperty(ManifestKeys.OPTION_BACKUP_SCHEME, AbstractRecoveryTarget.BACKUP_SCHEME_FULL);
		} else if (hasDifferentialBackup) {
			manifest.addProperty(ManifestKeys.OPTION_BACKUP_SCHEME, AbstractRecoveryTarget.BACKUP_SCHEME_DIFFERENTIAL);            
		} else {
			manifest.addProperty(ManifestKeys.OPTION_BACKUP_SCHEME, AbstractRecoveryTarget.BACKUP_SCHEME_INCREMENTAL);            
		}
	}

	public void install() throws ApplicationException {
		super.install();		
		if (compressionArguments.isMultiVolumes() && ! compressionArguments.isAddExtension()) {
			throw new ApplicationException("The \".zip\" extension is mandatory if zip-splitting is enabled.");
		}

		if ((! handler.supportsImageBackup()) && imageBackups) {
			throw new ApplicationException("Incoherent configuration : image archives are not compatible with delta backup.");
		}
	}

	public boolean isOverwrite() {
		return this.imageBackups;
	}

	public boolean isTrackPermissions() {
		return trackPermissions;
	}

	/**
	 * Lists the medium's archives
	 */
	public File[] listArchives(GregorianCalendar fromDate, GregorianCalendar toDate) {
		File[] ret = null;
		if (this.imageBackups) {
			File f = new File(fileSystemPolicy.getArchivePath(), computeArchiveName(fromDate));
			if (FileSystemManager.exists(f) && checkArchiveCompatibility(f)) {
				ret = new File[] {f};                
			} else {
				ret = new File[] {};
			}
		} else {
			File rootArchiveDirectory = fileSystemPolicy.getArchiveDirectory();
			File[] elementaryArchives = FileSystemManager.listFiles(rootArchiveDirectory, new ArchiveNameFilter(fromDate, toDate, this));

			if (elementaryArchives != null) {
				Arrays.sort(elementaryArchives, new ArchiveComparator(this));
			} else {
				elementaryArchives = new File[0];
			}

			ret = elementaryArchives;
		}

		return ret;
	}

	/**
	 * Log recovery informations
	 */
	public void logRecoveryStep(RecoveryFilterMap filtersByArchive, FileFilterList filters, File archive, ProcessContext context) 
	throws TaskCancelledException {
		context.getTaskMonitor().checkTaskState();  
		if (filtersByArchive != null && filters == null) {
			context.getInfoChannel().print("Skipping " + FileSystemManager.getPath(archive));
		} else {
			String scope;

			if (filtersByArchive == null) {
				scope = "All entries";
			} else if (filters == null) {
				scope = "No entry";
			} else {
				scope =  "" + filters.size();
				if (filters.size() <= 1) {
					if (filters.containsFiles()) {
						scope += " file";
					} else {
						scope += " directory";						
					}
				} else {
					if (filters.containsDirectories() && filters.containsFiles()) {
						scope += " files or directories";
					} else if (filters.containsDirectories()) {
						scope += " directories";
					} else {
						scope += " files";
					}
				}
			}
			context.getInfoChannel().print("Recovering " + FileSystemManager.getPath(archive) + " (" + scope + ") ...");
		}
	}

	public void merge(
			GregorianCalendar fromDate, 
			GregorianCalendar toDate, 
			Manifest mfToInsert,
			boolean keepDeletedEntries,
			ProcessContext context        
	) throws ApplicationException {

		try {
			this.checkRepository();

			if (toDate == null) {
				throw new ApplicationException("'To date' is mandatory");
			} else {
				toDate = (GregorianCalendar)toDate.clone();
				toDate.add(GregorianCalendar.MILLISECOND, 1);
			}

			if (fromDate != null) {
				fromDate = (GregorianCalendar)fromDate.clone();
				fromDate.add(GregorianCalendar.MILLISECOND, -1);
			}

			if (! imageBackups) { // No merge if "overwrite" = true
				Logger.defaultLogger().info(
						"Starting merge from " + Utils.formatDisplayDate(fromDate) + " to " + Utils.formatDisplayDate(toDate)
						+ "."
				);

				// Recovery of the merged archives
				context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.8, "recover");

				// Resolve trace file
				File traceFile;
				if (keepDeletedEntries) {
					File[] archives = this.listArchives(fromDate, toDate);
					traceFile = TraceMerger.buildAggregatedTraceFile(this, archives);
				} else {
					traceFile = ArchiveTraceManager.resolveTraceFileForArchive(this, getLastArchive(null, toDate));
				}

				// Recover
				recover(
						null, 
						null, 
						2,
						fromDate, 
						toDate, 
						traceFile, 
						ArchiveHandler.MODE_MERGE,
						keepDeletedEntries,
						false,
						context
				);

				context.getInfoChannel().print("Recovery completed - Merged archive creation ...");     
				context.getInfoChannel().updateCurrentTask(0, 0, "merge");

				context.getTaskMonitor().checkTaskState();

				File[] recoveredFiles = context.getReport().getRecoveryResult().getRecoveredArchivesAsArray();
				File[] processedFiles = context.getReport().getRecoveryResult().getProcessedArchivesAsArray();
				if (recoveredFiles.length >= 2) {
					GregorianCalendar lastArchiveDate = ArchiveManifestCache.getInstance().getManifest(this, recoveredFiles[recoveredFiles.length - 1]).getDate();

					// Rebuild the merged archive - set the context.currentArchiveFile field.
					buildMergedArchiveFromDirectory(context);

					// Build the new manifest
					if (mfToInsert == null) {
						mfToInsert = this.buildDefaultMergeManifest(processedFiles, fromDate, toDate);
					}
					initMergeManifest(processedFiles, fromDate, toDate, mfToInsert);
					context.setManifest(mfToInsert);
					context.getManifest().setDate(lastArchiveDate);
					context.getManifest().addProperty(ManifestKeys.MERGE_START, Utils.formatDisplayDate(fromDate));
					context.getManifest().addProperty(ManifestKeys.MERGE_END, Utils.formatDisplayDate(toDate));
					context.getManifest().addProperty(ManifestKeys.MERGED_ARCHIVES, processedFiles.length);
					context.getManifest().addProperty(ManifestKeys.ARCHIVE_SIZE, context.getOutputStreamListener().getWritten());					
					context.getManifest().addProperty(ManifestKeys.ARCHIVE_NAME, FileSystemManager.getName(context.getCurrentArchiveFile()));

					AbstractRecoveryTarget.addBasicInformationsToManifest(context.getManifest());
					this.storeManifest(context);

					// Store the trace file
					File target = new File(getDataDirectory(context.getCurrentArchiveFile()), getTraceFileName());
					FileTool.getInstance().copyFile(traceFile, FileSystemManager.getParentFile(target), FileSystemManager.getName(target), context.getTaskMonitor(), null);

					// Build and store the merged content file
					storeAggregatedContent(recoveredFiles, traceFile, context);
				}
			}
		} catch (ApplicationException e) {
			Logger.defaultLogger().error(e);
			throw e;
		} catch (Throwable e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		} finally {
			context.getTaskMonitor().getCurrentActiveSubTask().setCurrentCompletion(1);     
			try {
				if (context.getCurrentArchiveFile() != null) {
					FileSystemManager.getInstance().flush(context.getCurrentArchiveFile());
				}
			} catch (IOException e) {
				Logger.defaultLogger().error(e);
				throw new ApplicationException(e);
			}
		}
	}   

	/**
	 * Prepares the backup
	 */
	public void open(Manifest manifest, ProcessContext context, String backupScheme) throws ApplicationException {      
		try {  
			this.checkRepository();

			Logger.defaultLogger().info("Opening medium (Backup scheme = '" + backupScheme + "') ...");
			LogHelper.logFileInformations("Backup location :", fileSystemPolicy.getArchiveDirectory());  

			// Read the previous trace
			if (backupScheme.equals(AbstractRecoveryTarget.BACKUP_SCHEME_FULL)) {
				Logger.defaultLogger().info("Using an empty archive as reference.");
				context.setReferenceTrace(null);
			} else {
				File lastArchive;
				if (backupScheme.equals(AbstractRecoveryTarget.BACKUP_SCHEME_DIFFERENTIAL)) {
					lastArchive = this.getLastArchive(AbstractRecoveryTarget.BACKUP_SCHEME_FULL, null);
				} else {
					lastArchive = this.getLastArchive();
				}

				if (lastArchive != null && FileSystemManager.exists(lastArchive)) {
					Logger.defaultLogger().info("Using the following archive as reference : " + FileSystemManager.getAbsolutePath(lastArchive) + ".");
					File trcFile = ArchiveTraceManager.resolveTraceFileForArchive(this, lastArchive);
					ArchiveTraceAdapter adapter;
					if (imageBackups) {
						// In case of image backups, we need to duplicate the trace file in order to read it during the backup process.
						adapter = new ArchiveTraceAdapter(duplicateMetadataFile(trcFile, context));
					} else {
						adapter = new ArchiveTraceAdapter(trcFile);						
					}

					context.setReferenceTrace(adapter.buildIterator());
				} else {
					// Build an empty trace
					Logger.defaultLogger().info("Using an empty archive as reference.");
					context.setReferenceTrace(null);
				} 
			}

			context.setManifest(manifest);
			manifest.addProperty(
					ManifestKeys.OPTION_BACKUP_SCHEME, 
					context.getReferenceTrace() == null ? AbstractRecoveryTarget.BACKUP_SCHEME_FULL : backupScheme
			);

			// Archive creation
			buildArchive(context);

			// TraceWriter creation
			File traceFile = new File(getDataDirectory(context.getCurrentArchiveFile()), getTraceFileName());
			context.setTraceAdapter(new ArchiveTraceAdapter(traceFile, ((FileSystemRecoveryTarget)this.target).isTrackSymlinks()));
			context.getTraceAdapter().setTrackPermissions(this.trackPermissions);

			File contentFile = new File(getDataDirectory(context.getCurrentArchiveFile()), getContentFileName());
			context.setContentAdapter(new ArchiveContentAdapter(contentFile));      

			File hashFile = new File(getDataDirectory(context.getCurrentArchiveFile()), getHashFileName());
			context.setHashAdapter(new ArchiveContentAdapter(hashFile));      

			// handler-specific initializations
			handler.init(context);

			// medium-specific initializations
			prepareContext(context);
			
			// Enable thread monitor if requested
			if (TH_MON_ENABLED) {
				ThreadMonitorItem item = new ThreadMonitorItem(this.getTarget().getUid(), TH_MON_DELAY);
				ThreadMonitor.getInstance().register(item);
			}
		} catch (Exception e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		}
	} 

	/**
	 * Recovers the stored data
	 */
	public void recover(
			Object destination, 
			String[] filter, 
			GregorianCalendar date, 
			boolean recoverDeletedEntries,
			boolean checkRecoveredFiles,
			ProcessContext context) 
	throws ApplicationException, TaskCancelledException {  
		try {
			File traceFile;
			if (recoverDeletedEntries) {
				File[] archives = this.listArchives(null, date);
				traceFile = TraceMerger.buildAggregatedTraceFile(this, archives);
			} else {
				traceFile = ArchiveTraceManager.resolveTraceFileForArchive(this, getLastArchive(null, date));
			}

			// Recover the data
			recover(destination, filter, 1, null, date, traceFile, ArchiveHandler.MODE_RECOVER, recoverDeletedEntries, checkRecoveredFiles, context);

			// Create missing directories and symbolic links
			this.target.secureUpdateCurrentTask("Creating missing directories and symbolic links ...", context);
			try {
				ArchiveTraceAdapter adapter = new ArchiveTraceAdapter(traceFile);
				RebuildOtherFilesTraceHandler handler = new RebuildOtherFilesTraceHandler((File)destination, filter);
				adapter.traverseTraceFile(handler, context);
			} catch (IOException e) {
				throw new ApplicationException(e);
			} catch (FileMetaDataSerializationException e) {
				throw new ApplicationException(e);
			}

			// Apply metadata
			applyMetaData((File)destination, traceFile, context);
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}

	public void rollbackBackup(ProcessContext context) throws ApplicationException {
		if (TH_MON_ENABLED) {
			ThreadMonitor.getInstance().remove(this.getTarget().getUid());
		}
		
		this.target.secureUpdateCurrentTask("Rollbacking backup ...", context);
		try {
			try {
				// Close the trace adapter
				if (context.getTraceAdapter() != null) {
					try {
						context.getTraceAdapter().close();
					} finally {
						context.setTraceAdapter(null);
					}
				}
			} finally {
				try {
					// Close the adapters
					try {
						ArchiveContentAdapter adapter = context.getContentAdapter();
						context.setContentAdapter(null);
						if (adapter != null) {
							adapter.close();
						}
					} finally {
						try {
							ArchiveContentAdapter adapter = context.getHashAdapter();
							context.setHashAdapter(null);
							if (adapter != null) {
								adapter.close();
							}
						} finally {
							handler.close(context);
						}
					}
				} finally {
					try {
						this.closeArchive(context);
					} catch (Throwable e) {
						Logger.defaultLogger().error("Error closing archive", e);
						if (e instanceof ApplicationException) {
							throw (ApplicationException)e;
						} else {
							throw new ApplicationException("Error closing archive", e);
						}
					} finally {    
						try {
							// Destroy directories
							if (context.getCurrentArchiveFile() != null) {
								AbstractFileSystemMedium.tool.delete(context.getCurrentArchiveFile(), true);
								AbstractFileSystemMedium.tool.delete(this.getDataDirectory(context.getCurrentArchiveFile()), true);
							}
						} finally {
							try {
								// Close the trace iterator
								if (context.getReferenceTrace() != null) {
									context.getReferenceTrace().close();
								}
							} finally {
								try {
									// Flush all remaining data
									if (context.getCurrentArchiveFile() != null) {
										FileSystemManager.getInstance().flush(context.getCurrentArchiveFile());
									}
								} finally {
									this.target.secureUpdateCurrentTask("Rollback completed.", context);
								}
							}
						}
					}
				}
			}
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		}
	}

	public void rollbackMerge(ProcessContext context) throws ApplicationException {
		if (! this.imageBackups) {
			this.target.secureUpdateCurrentTask("Rollbacking merge ...", context);
			try {
				try {
					// Clean temporary data
					this.cleanMerge(context);
				} finally {
					try {
						// Close the archive
						this.closeArchive(context); 
					} finally {
						// Delete the archive
						if (! isCommitted(context.getCurrentArchiveFile())) {
							AbstractFileSystemMedium.tool.delete(context.getCurrentArchiveFile(), true);
							AbstractFileSystemMedium.tool.delete(this.getDataDirectory(context.getCurrentArchiveFile()), true);
						}

						this.target.secureUpdateCurrentTask("Rollback completed.", context);
					}
				}
			} catch (IOException e) {
				Logger.defaultLogger().error(e);
				throw new ApplicationException(e);
			}
		}
	}

	public TargetSearchResult search(SearchCriteria criteria) throws ApplicationException {
		TargetSearchResult result = new TargetSearchResult();
		DefaultSearchCriteria dCriteria = (DefaultSearchCriteria)criteria;

		if (dCriteria.isRestrictLatestArchive()) {
			File lastArchive = this.getLastArchive();
			if (lastArchive != null) {
				this.searchWithinArchive(criteria, lastArchive, result);
			}
		} else {
			File[] archives = this.listArchives(null, null);
			for (int i=0; i<archives.length; i++) {
				this.searchWithinArchive(criteria, archives[i], result);
			}
		}

		return result;
	}

	public void setHandler(ArchiveHandler handler) {
		this.handler = handler;
		handler.setMedium(this);
	}

	public void setOverwrite(boolean overwrite) {
		this.imageBackups = overwrite;
	} 

	public void setTrackPermissions(boolean trackPermissions) {
		this.trackPermissions = trackPermissions;
	}

	public void simulateEntryProcessing(RecoveryEntry entry, boolean haltOnFirstDifference, ProcessContext context) throws ApplicationException {
		try {
			FileSystemRecoveryEntry fEntry = (FileSystemRecoveryEntry)entry;

			// Context initialization
			if (! context.isInitialized()) {
				File archive = this.getLastArchive();
				if (archive != null) {
					ArchiveTraceAdapter adapter = new ArchiveTraceAdapter(ArchiveTraceManager.resolveTraceFileForArchive(this, archive));
					context.setReferenceTrace(adapter.buildIterator());
				}
				context.setInitialized();
				context.setSimulationResult(new ArrayList());
			}

			TraceFileIterator iter = context.getReferenceTrace();
			if (iter == null) {
				fEntry.setStatus(EntryStatus.STATUS_CREATED);
				return;
			}
			String shortPath = entry.getKey();

			while (true) {
				String key = iter.current() == null ? null : iter.current().getKey();

				// Compare the file paths
				int result = key == null ? -1 : FilePathComparator.instance().compare(shortPath, key);

				if (result == 0) {
					// Found among source files and in trace -> ok : check hash codes
					if (FileSystemManager.isDirectory(fEntry.getFile())) {
						// Directory
						fEntry.setStatus(EntryStatus.STATUS_NOT_STORED);   
					} else {
						short type = FileSystemManager.getType(fEntry.getFile());
						if (FileMetaDataAccessor.TYPE_PIPE == type) {
							fEntry.setStatus(EntryStatus.STATUS_NOT_STORED);  
						} else {
							boolean link = false;
							if (((FileSystemRecoveryTarget)this.target).isTrackSymlinks() && FileMetaDataAccessor.TYPE_LINK == type) {
								link = true;
								fEntry.setSize(0);
							}
							String newHash = ArchiveTraceParser.hash(fEntry, link);
							String oldHash = iter.current() == null ? null : ArchiveTraceParser.extractHashFromTrace(iter.current().getData());

							// return result
							if (newHash.equals(oldHash)) {
								fEntry.setStatus(EntryStatus.STATUS_NOT_STORED);
							} else {
								fEntry.setStatus(EntryStatus.STATUS_MODIFIED);
							}
						}
					}

					// Fetch next entry
					iter.next();
					break;
				} else if (result < 0) {
					// File found in source files but not found in trace -> new File
					fEntry.setStatus(EntryStatus.STATUS_CREATED);

					if (((FileSystemRecoveryTarget)this.target).isTrackSymlinks() && FileMetaDataAccessor.TYPE_LINK == FileSystemManager.getType(fEntry.getFile())) {
						fEntry.setSize(0);
					}

					break;
				} else {
					// File found in trace but not among source files -> deleted file
					iter.next();

					if (haltOnFirstDifference) {
						fEntry.setStatus(EntryStatus.STATUS_DELETED);
						break;
					}
				}
			}
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		}
	} 

	/**
	 * Stores an entry
	 */
	public void store(RecoveryEntry entry, final ProcessContext context) 
	throws StoreException, ApplicationException, TaskCancelledException {	
		if (TH_MON_ENABLED) {
			ThreadMonitor.getInstance().notify(this.getTarget().getUid());
		}
		
		if (entry == null) {
			return;
		} else {
			final FileSystemRecoveryEntry fEntry = (FileSystemRecoveryEntry)entry;
			try {
				short type = FileSystemManager.getType(fEntry.getFile());
				if (
						FileSystemManager.isFile(fEntry.getFile()) && (
								(FileMetaDataAccessor.TYPE_LINK != type)
								|| (! ((FileSystemRecoveryTarget)this.target).isTrackSymlinks())
						) && (FileMetaDataAccessor.TYPE_PIPE != type)
				) {
					// The entry is stored if it has been modified
					if (this.checkModified(fEntry, context)) {
						if (DEBUG_MODE) {
							Logger.defaultLogger().fine("[" + FileSystemManager.getAbsolutePath(fEntry.getFile()) + "] : Backup in progress ...");
						}

						// Add a listener to the inputStream
						final HashInputStreamListener listener = new HashInputStreamListener();

						this.doAndRetry(new IOTask() {
							public void run() throws IOException, TaskCancelledException, ApplicationException {
								InputStream in = FileSystemManager.getFileInputStream(fEntry.getFile());
								in = new EventInputStream(in, listener);
								try {
									listener.reset();
								} catch (NoSuchAlgorithmException e) {
									throw new ApplicationException(e);
								}
								storeFileInArchive(fEntry, in, context);
							}
						}, "An error occured while storing " + fEntry.getKey());

						context.addInputBytes(FileSystemManager.length(fEntry.getFile()));
						context.getContentAdapter().writeContentEntry(fEntry);
						context.getHashAdapter().writeHashEntry(fEntry, listener.getHash());

						context.getReport().addSavedFile();
					} else {
						if (DEBUG_MODE) {
							Logger.defaultLogger().fine("[" + FileSystemManager.getAbsolutePath(fEntry.getFile()) + "] : Unchanged.");
						}
						this.registerUnstoredFile(fEntry, context);
						context.getReport().addIgnoredFile();
					}
				}

				// Register the entry
				context.getTraceAdapter().writeEntry(fEntry);

			} catch (IOException e) {
				Logger.defaultLogger().error(e);
				throw new StoreException("Error during storage of " + entry.getKey() + " : " + e.getMessage(), e);
			} catch (NoSuchAlgorithmException e) {
				Logger.defaultLogger().error(e);
				throw new StoreException("Error during storage of " + entry.getKey() + " : " + e.getMessage(), e);
			} catch (FileMetaDataSerializationException e) {
				Logger.defaultLogger().error(e);
				throw new StoreException("Error during storage of " + entry.getKey() + " : " + e.getMessage(), e);
			}
		}
	}

	public void doAndRetry(IOTask rn, String message) 
	throws IOException, TaskCancelledException, ApplicationException {
		try {
			rn.run();
		} catch (IOException e) {		
			// New attempts if retry is supported
			if (this.retrySupported()) {
				IOException exception = e;
				for (int retry = 1; retry<=this.getMaxRetries(); retry++) {
					Logger.defaultLogger().warn(message + " (" + exception.getMessage() + "). Retrying (attempt " + retry + " of " + this.getMaxRetries() + ") ...");
					try {
						rn.run();
						exception = null;
						break;
					} catch (IOException ex) {
						exception = ex;
					}
				}

				if (exception != null) {
					throw exception;
				}
			} else {
				throw e;
			}
		} catch (TaskCancelledException e) {
			throw e;
		} catch (ApplicationException e) {
			Logger.defaultLogger().warn(message, e);
			throw e;
		} catch (RuntimeException e) {
			Logger.defaultLogger().warn(message, e);
			throw e;
		}
	}

	public abstract boolean retrySupported();
	public abstract int getMaxRetries();

	public void storeManifest(ProcessContext context) throws ApplicationException {
		if (context.getManifest() != null) {
			ManifestManager.writeManifest(this, context.getManifest(), context.getCurrentArchiveFile());            
		}
	}

	public boolean supportsBackupScheme(String backupScheme) {
		if (imageBackups && backupScheme.equals(AbstractRecoveryTarget.BACKUP_SCHEME_DIFFERENTIAL)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Build the archive
	 */
	protected void buildArchive(ProcessContext context) throws IOException, ApplicationException {
		context.setCurrentArchiveFile(new File(computeFinalArchivePath()));  
		LogHelper.logFileInformations("Final archive : ", context.getCurrentArchiveFile()); 
	}    

	/**
	 * Build an explicit set of entries (files) to recover.
	 * <BR>If the set's size reaches maxSetSize, then the process is stopped and
	 * null is returned.
	 * <BR>Otherwise, the set is returned.
	 */
	protected String[] buildAtomicEntrySet(String[] entries, File traceFile, int maxSetSize, ProcessContext context)
	throws IOException, FileMetaDataSerializationException, TaskCancelledException {
		Set entriesToDispatch = new HashSet();
		List directoriesToDispatch = new ArrayList();
		int size = 0;

		// separate files and directories amongst entries to recover
		for (int e=0; e<entries.length; e++) {
			if (entries[e].length() == 0 || FileNameUtil.endsWithSeparator(entries[e])) {
				if (FileNameUtil.startsWithSeparator(entries[e])) {
					directoriesToDispatch.add(entries[e].substring(1));
				} else {
					directoriesToDispatch.add(entries[e]);
				}
			} else {
				entriesToDispatch.add(entries[e]);	
				size++;
				if (maxSetSize != -1 && size >= maxSetSize) {
					return null;
				}
			}
		}

		// process directories
		String[] dirs = (String[])directoriesToDispatch.toArray(new String[directoriesToDispatch.size()]);
		ArchiveTraceAdapter adapter = new ArchiveTraceAdapter(traceFile);
		EntrySetTraceHandler handler = new EntrySetTraceHandler(dirs, maxSetSize, entriesToDispatch, size);
		try {
			adapter.traverseTraceFile(handler, context);
		} catch (IllegalStateException e) {
			return null;
		}

		// Once the set is build (and unicity ensured), build an ordered list of entries to recover
		String[] ret = new String[entriesToDispatch.size()];
		Iterator iter = entriesToDispatch.iterator();
		int i=0;
		while (iter.hasNext()) {
			ret[i++] = (String)iter.next();
		}
		Arrays.sort(ret, FilePathComparator.instance());

		return ret;
	}

	protected abstract void buildMergedArchiveFromDirectory(ProcessContext context) throws ApplicationException;

	/**
	 * Check that we do not use a reserved name as archive name
	 */
	protected void checkFileSystemPolicy() {
		String nameToCheck = this.fileSystemPolicy.getArchiveName();
		for (int i=0; i<RESERVED_NAMES.length; i++) {
			if (nameToCheck.startsWith(RESERVED_NAMES[i])) {
				throw new IllegalArgumentException("Invalid archive name (" + nameToCheck + "). This name is reserved by Areca. Please choose a different one.");
			}
		}
	} 

	/**
	 * Delete unwanted files (ie files that have been recovered but that do not appear in the trace file)
	 */
	protected void cleanUnwantedFiles(
			File targetFile, 
			File traceFile,
			boolean cancelSensitive,
			ProcessContext context
	) throws IOException, TaskCancelledException, FileMetaDataSerializationException {   
		FileSystemIterator targetIterator = new FileSystemIterator(targetFile, false, true, true, true);
		ArchiveTraceAdapter adapter = new ArchiveTraceAdapter(traceFile);
		TraceFileIterator traceIterator = null;

		try {
			traceIterator = adapter.buildIterator();
			FilePathComparator comparator = new FilePathComparator();

			File toCheck = fetchNextFile(targetIterator);				// Ignore the recovery root
			toCheck = fetchNextFile(targetIterator);
			TraceEntry entry = fetchNextTraceEntry(traceIterator);
			while (true) {	
				if (toCheck == null) {
					break;
				}
				String shortPath = Utils.extractShortFilePath(toCheck, targetFile);

				// Compare the file paths
				int result = entry == null ? -1 : comparator.compare(shortPath, entry.getKey());

				if (result == 0) {
					// Found among recovered files and in trace -> ok
					toCheck = fetchNextFile(targetIterator);
					entry = fetchNextTraceEntry(traceIterator);
				} else if (result < 0) {
					// File found in recovered files but not found in trace -> destroy it
					deleteRecur(targetFile, toCheck);
					toCheck = fetchNextFile(targetIterator);
				} else {
					// File found in trace but not among recovered files -> ignore it
					entry = fetchNextTraceEntry(traceIterator);
				}
			}
		} finally {
			if (traceIterator != null) {
				traceIterator.close();
			}
		}
	}

	protected abstract void closeArchive(ProcessContext context) throws IOException, ApplicationException;

	/**
	 * Build an archive name
	 */
	protected String computeArchiveName(GregorianCalendar date) {
		GregorianCalendar cal;

		if (imageBackups) {
			cal = null;
		} else {
			cal = (date != null ? date : new GregorianCalendar());		
		}

		String name = ArchiveNameHelper.parseName(this.fileSystemPolicy.getArchiveName(), cal);

		// We must avoid empty names
		if (name.trim().length() == 0) {
			name = "b";
		}
		String target = name + getArchiveExtension();
		if (! imageBackups) {
			int c = 1;
			while (FileSystemManager.exists(new File(fileSystemPolicy.getArchiveDirectory(), target))) {
				target = name + ArchiveNameHelper.SUFFIX_SEPARATOR + c + getArchiveExtension();
				c++;
			}
		}

		return target;
	}

	/**
	 * Build a full archive path
	 */
	protected String computeArchivePath(GregorianCalendar date) {
		return FileSystemManager.getAbsolutePath(new File(fileSystemPolicy.getArchiveDirectory(), computeArchiveName(date)));
	}

	protected String computeFinalArchivePath() {
		return computeArchivePath(new GregorianCalendar());
	}

	protected abstract void computeMergeDirectories(ProcessContext context) throws ApplicationException ;

	protected void convertArchiveToFinal(ProcessContext context) throws IOException, ApplicationException {
		markCommitted(context.getCurrentArchiveFile());
	}

	protected void copyAttributes(Object clone) {
		super.copyAttributes(clone);

		AbstractIncrementalFileSystemMedium other = (AbstractIncrementalFileSystemMedium)clone;
		other.imageBackups = this.imageBackups;
		other.trackPermissions = this.trackPermissions;
		other.handler = (ArchiveHandler)this.handler.duplicate();
		other.handler.setMedium(other);
	}

	/**
	 * Deletes the archive - WHETHER IT IS COMMITTED OR NOT
	 */
	protected void deleteArchive(File archive) throws IOException {
		AbstractFileSystemMedium.tool.delete(archive, true);
		AbstractFileSystemMedium.tool.delete(this.getDataDirectory(archive), true);
		handler.archiveDeleted(archive);
	}

	protected EntryArchiveData getArchiveData(String entry, File archive) throws ApplicationException {
		try {
			EntryArchiveData ead = new EntryArchiveData();
			TraceFileIterator trcIter = null;
			ContentFileIterator ctnIter = null;

			try {
				Manifest mf = ArchiveManifestCache.getInstance().getManifest(this, archive);
				ead.setManifest(mf);

				// Build a trace iterator
				trcIter = ArchiveTraceManager.buildIteratorForArchive(this, archive);
				boolean found = trcIter.fetchUntil(entry);
				if (found) {
					ead.setHash(trcIter.current().getData());

					// Build a content iterator
					ctnIter = ArchiveContentManager.buildIteratorForArchive(this, archive);
					found = ctnIter.fetchUntil(entry);
					if (found) {
						ead.setStatus(EntryStatus.STATUS_STORED);
					} else {
						ead.setStatus(EntryStatus.STATUS_NOT_STORED);
					}
				}
			} finally {
				if (trcIter != null) {
					trcIter.close();
				}
				if (ctnIter != null) {
					ctnIter.close();
				}
			}

			return ead;
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		}
	}

	protected boolean isWorkingDirectory(File f) {
		String name = FileSystemManager.getName(f);
		// We can simply assume that if the file starts with "CHECK_DESTINATION",
		// it really is a temporary check destination.
		// This is ensured by the checkFileSystemPolicy() method
		return name.startsWith(CHECK_DESTINATION); 
	}

	protected boolean matchArchiveName(File f) {
		String name = FileSystemManager.getName(f);

		if (imageBackups) {
			String parsed = computeArchiveName(null);
			return parsed.equals(name);
		} else {
			return ArchiveNameHelper.matchPattern(name, fileSystemPolicy.getArchiveName(), getArchiveExtension());
		}
	}

	protected abstract void prepareContext(ProcessContext context) throws IOException;

	/**
	 * Recovers the files at the requested recovery location, according to the recovery dates passed as argument.
	 * <BR>'filters' may be null ...
	 * <BR>The recovery is actually done if there are at least <code>minimumArchiveNumber</code> archives to recover
	 * <BR>
	 * <BR>If it has enough memory, Areca optimizes the recovery process to only recover the needed files (instead of
	 * blindly recovering the whole archives)
	 */
	protected void recover(
			Object destination,                         // Where to recover
			String[] argFilter,                         // Filters the recovered entries
			int minimumArchiveNumber,           		// The recovery is done only if there are at least this number of archives to recover
			GregorianCalendar fromDate,          		// Recovery from date
			GregorianCalendar toDate,             		// Recovery to date
			File traceFile,                       	    // Optional trace to apply to the recovered data
			short mode,                                 // Recovery mode : see ArchiveHandler.MODE_MERGE / MODE_RECOVER
			boolean recoverDeletedEntries,				// Also recover deleted entries
			boolean checkRecoveredFiles,				// Whether areca must check if the recovered files' hash is the same as the reference hash
			ProcessContext context                		// Execution context
	) throws ApplicationException, TaskCancelledException {
		File targetFile = (File)destination;

		// Compute maximum number of entries that allow optimized recovery
		int maxEntries = (int)MemoryHelper.getMaxManageableEntries();

		// Set final filter
		String[] filters = (argFilter == null || argFilter.length == 0) ? new String[] {""} : argFilter;

		if (toDate != null) {
			toDate = (GregorianCalendar)toDate.clone();
			toDate.add(GregorianCalendar.MILLISECOND, 1);
		}
		if (fromDate != null) {
			fromDate = (GregorianCalendar)fromDate.clone();
			fromDate.add(GregorianCalendar.MILLISECOND, -1);
		}
		RecoveryResult result = new RecoveryResult();
		context.getReport().setRecoveryResult(result);
		try {
			// First stage : list archives to recover
			buildArchiveListToRecover(result, fromDate, toDate, recoverDeletedEntries);

			// Second stage : recover data
			if (result.getRecoveredArchives().size() >= minimumArchiveNumber) {
				context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(checkRecoveredFiles ? 0.7 : 0.9, "recover");
				File[] optimizedArchives = result.getRecoveredArchivesAsArray();

				// If no destination was set, compute it from the last archive's date.
				if (targetFile == null) {
					GregorianCalendar lastArchiveDate = ArchiveManifestCache.getInstance().getManifest(this, optimizedArchives[optimizedArchives.length - 1]).getDate();
					targetFile = new File(computeArchivePath(lastArchiveDate));
				}
				FileTool.getInstance().delete(targetFile, true);
				context.setRecoveryDestination(targetFile);
				FileTool.getInstance().createDir(targetFile);
				Logger.defaultLogger().info("Files will be recovered in " + targetFile.getAbsolutePath());

				if (mode == ArchiveHandler.MODE_MERGE) {
					computeMergeDirectories(context);
				}

				// Process Recovery
				RecoveryFilterMap entriesByArchive = null;
				String[] entriesToDispatch = buildAtomicEntrySet(filters, traceFile, maxEntries, context);
				if (entriesToDispatch == null) {
					// Dummy mode : recover all entries / archives
					Logger.defaultLogger().info("Too many entries (over " + maxEntries + " entries) to use optimized mode ... recovering in standard mode.");

					// Build a default filter map (which will be applied to all archives, regardless to their real content)
					if (filters != null && filters.length != 0) {
						entriesByArchive = new RecoveryFilterMap(false);
						FileFilterList defaultFilter = new FileFilterList();
						for (int i=0; i<filters.length; i++) {
							defaultFilter.add(filters[i]);
						}
						for (int i=0; i<optimizedArchives.length; i++) {
							entriesByArchive.put(optimizedArchives[i], defaultFilter);
						}
					}
				} else {
					// Smart mode : iterate on each entry and recover its latest version only
					Logger.defaultLogger().info("Recovering in optimized mode.");
					Logger.defaultLogger().info("" + entriesToDispatch.length + " files will be recovered.");

					// Build an optimized filter map
					entriesByArchive = handler.dispatchEntries(optimizedArchives, entriesToDispatch);
				}

				// Recover the data
				handler.recoverRawData(optimizedArchives, entriesByArchive, mode, context);

				context.getTaskMonitor().checkTaskState();

				// Third stage: clean recovery directory ... necessary if the "dummy" mode has been used
				boolean dummyMode = (entriesToDispatch == null);
				if (traceFile != null && dummyMode) {
					Logger.defaultLogger().info("Cleaning recovery directory ...");
					this.cleanUnwantedFiles(
							targetFile, 
							traceFile,
							true,
							context);
					Logger.defaultLogger().info("Recovery directory cleaned.");
				}

				// Fourth stage: check hash
				if (checkRecoveredFiles) {

					AbstractMetaDataFileIterator refIter;
					if (optimizedArchives.length == 1) {
						// Build reference content iterator
						Logger.defaultLogger().info("Using content of archive " + optimizedArchives[0].getAbsolutePath() + " as reference.");
						refIter = ArchiveContentManager.buildIteratorForArchive(this, optimizedArchives[0]);
					} else {
						// Build reference trace iterator
						Logger.defaultLogger().info("Using trace of archive " + optimizedArchives[optimizedArchives.length - 1].getAbsolutePath() + " as reference.");
						refIter = ArchiveTraceManager.buildIteratorForArchive(this, optimizedArchives[optimizedArchives.length - 1]);
					}

					checkHash(targetFile, optimizedArchives, filters, refIter, context);
				} 
			}
		} catch (TaskCancelledException e) {
			throw e;
		} catch (Exception e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		} finally {
			context.getTaskMonitor().getCurrentActiveSubTask().setCurrentCompletion(1);            
		}
	}

	protected abstract void registerUnstoredFile(FileSystemRecoveryEntry entry, ProcessContext context) throws IOException;

	/**
	 * Applies the metadata to the recovered files / symlinks / directories :
	 * <BR>- last modification date
	 * <BR>- permissions
	 * <BR>- ACL ... (if supported by the local metadata accessor)
	 */
	private void applyMetaData(File destination, File traceFile, ProcessContext context) 
	throws ApplicationException, TaskCancelledException {
		this.target.secureUpdateCurrentTask("Applying metadata ...", context);
		UpdateMetaDataTraceHandler handler = new UpdateMetaDataTraceHandler();
		handler.setDestination(destination);
		ArchiveTraceAdapter adapter = new ArchiveTraceAdapter(traceFile, ((FileSystemRecoveryTarget)this.target).isTrackSymlinks());

		try {
			adapter.traverseTraceFile(handler, context);
			this.target.secureUpdateCurrentTask("Metadata applied.", context);
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		} catch (FileMetaDataSerializationException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		}
	}

	/**
	 * Build an optimized list of archives to recover.
	 * <BR>The result is stored in the RecoveryResult.
	 */
	private void buildArchiveListToRecover(
			RecoveryResult result, 
			GregorianCalendar fromDate,
			GregorianCalendar toDate,
			boolean recoverDeletedEntries
	) throws ApplicationException {
		File[] listedArchives = this.listArchives(fromDate, toDate);
		result.addProcessedArchives(listedArchives);

		if (recoverDeletedEntries) {
			result.addRecoveredArchives(listedArchives);
		} else {
			boolean ignoreIncrementalAndDifferentialArchives = false;
			boolean ignoreAllArchives = false;

			for (int i=listedArchives.length - 1; i>=0; i--) {
				Manifest mf = ArchiveManifestCache.getInstance().getManifest(this, listedArchives[i]);
				String prp = mf.getStringProperty(ManifestKeys.OPTION_BACKUP_SCHEME, AbstractRecoveryTarget.BACKUP_SCHEME_INCREMENTAL);
				if (prp.equals(AbstractRecoveryTarget.BACKUP_SCHEME_FULL) && ! ignoreAllArchives) {
					Logger.defaultLogger().info("Adding " + FileSystemManager.getAbsolutePath(listedArchives[i]) + " (" + prp + ") to recovery list.");
					result.getRecoveredArchives().add(0, listedArchives[i]);
					ignoreAllArchives = true;
					Logger.defaultLogger().info("Previous archives will be ignored.");
				} else if (prp.equals(AbstractRecoveryTarget.BACKUP_SCHEME_DIFFERENTIAL)  && ! ignoreIncrementalAndDifferentialArchives && ! ignoreAllArchives) {
					Logger.defaultLogger().info("Adding " + FileSystemManager.getAbsolutePath(listedArchives[i]) + " (" + prp + ") to recovery list.");
					result.getRecoveredArchives().add(0, listedArchives[i]);
					ignoreIncrementalAndDifferentialArchives = true;
					Logger.defaultLogger().info("Previous incremental and differential archives will be ignored.");                            
				} else if (prp.equals(AbstractRecoveryTarget.BACKUP_SCHEME_INCREMENTAL) && ! ignoreIncrementalAndDifferentialArchives && ! ignoreAllArchives) {
					Logger.defaultLogger().info("Adding " + FileSystemManager.getAbsolutePath(listedArchives[i]) + " (" + prp + ") to recovery list.");
					result.getRecoveredArchives().add(0, listedArchives[i]);
				} else {
					Logger.defaultLogger().info("Adding " + FileSystemManager.getAbsolutePath(listedArchives[i]) + " (" + prp + ") to ignore list.");
					result.getIgnoredArchives().add(0, listedArchives[i]);
				}
			}
		}
	}

	/**
	 * Check that the file passed as argument matches the hashcode contained in the "entry" argument.
	 */
	private void checkHash(File file, ContentEntry entry, ProcessContext context) 
	throws IOException, TaskCancelledException, ApplicationException {
		// Check the file
		byte[] storedHash = ArchiveContentParser.interpretAsHash(entry.getKey(), entry.getData());
		if (storedHash == null) {
			Logger.defaultLogger().warn(entry.getKey() + " : no reference hash could be found.");
			context.getUncheckedRecoveredFiles().add(entry.getKey());
		} else {
			this.doAndRetry(new CheckHash(context, storedHash, entry, file), "Error while checking " + file.getAbsolutePath());
		}
	}

	public class CheckHash implements IOTask {
		private ProcessContext context;
		private byte[] storedHash;
		private ContentEntry entry;
		private File file;

		public CheckHash(ProcessContext context, byte[] storedHash, ContentEntry entry, File file) {
			this.context = context;
			this.storedHash = storedHash;
			this.entry = entry;
			this.file = file;
		}

		public void run() throws IOException, TaskCancelledException, ApplicationException {
			byte[] computedHash;
			try {
				computedHash = FileTool.getInstance().hashFileContent(file, context.getTaskMonitor());
			} catch (NoSuchAlgorithmException e) {
				throw new ApplicationException(e);
			}

			if (computedHash.length != storedHash.length) {
				throw new IllegalStateException("Incoherent hash lengths (" + computedHash.length + " versus " + storedHash.length + ").");
			} else {
				boolean ok = true;
				for (int i=0; i<computedHash.length; i++) {
					if (computedHash[i] != storedHash[i]) {
						context.getInfoChannel().warn(entry.getKey() + " was not properly recovered : its hash (" + Util.base16Encode(computedHash) + ") is different from the reference hash (" + Util.base16Encode(storedHash) + ").");
						context.getInvalidRecoveredFiles().add(entry.getKey());
						ok = false;
						break;
					}
				}
				if (ok && CHECK_DEBUG_MODE) {
					Logger.defaultLogger().fine("Check ok.");
				}
			}
		}
	}

	private void checkHash(
			File destination, 
			File[] archives, 
			String[] filters,
			AbstractMetaDataFileIterator referenceIterator,
			ProcessContext context) 
	throws IOException, TaskCancelledException, ApplicationException {
		context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.2, "check");
		context.getInfoChannel().print("Recovery completed - Checking recovered files (this may take some time) ...");  

		if (CHECK_DEBUG_MODE) {
			if (filters == null) {
				Logger.defaultLogger().fine("No filter.");
			} else {
				for (int i=0; i<filters.length; i++) {
					Logger.defaultLogger().fine("Filter[" + i + "] : " + filters[i]);
				}
			}
		}
		
		ContentFileIterator[] iters = new ContentFileIterator[archives.length];
		try {
			// Build hashIterators
			for (int i=0; i<archives.length; i++) {
				File hashFile = ArchiveContentManager.resolveHashFileForArchive(this, archives[i]);
				ArchiveContentAdapter adapter = new ArchiveContentAdapter(hashFile);
				iters[i] = adapter.buildIterator();
			}

			while (referenceIterator.hasNext()) {
				AbstractMetaDataEntry entry = referenceIterator.nextEntry();
				
				if (CHECK_DEBUG_MODE) {
					Logger.defaultLogger().fine("Entry : " + entry.getKey());
				}

				// The entry will only be checked if it passes the filters
				if (filters == null || Util.passFilter(entry.getKey(), filters)) {
					File target = new File(destination, entry.getKey());
					if (entry.getType() == MetadataConstants.T_FILE) {
						if (! FileSystemManager.exists(target)) {
							Logger.defaultLogger().warn(entry.getKey() + " has not been recovered ... it should have !");
							context.getUnrecoveredFiles().add(entry.getKey());
						} else {
							if (CHECK_DEBUG_MODE) {
								Logger.defaultLogger().fine(entry.getKey() + " is a file ... checking its hash ...");
							}
							
							// Iterate among the hash files
							boolean found = false;
							for (int i=archives.length-1; i>=0; i--) {
								if (CHECK_DEBUG_MODE) {
									Logger.defaultLogger().fine("Looking for hash in " + archives[i].getAbsolutePath());
								}
								found = iters[i].fetchUntil(entry.getKey());
								if (found) {
									if (CHECK_DEBUG_MODE) {
										Logger.defaultLogger().fine("Hash found ! Verifying ...");
									}
									// The entry has been found --> check the hash
									checkHash(target, iters[i].current(), context);
									context.addChecked();
									break;
								}
							}
							if (! found) {
								Logger.defaultLogger().warn("No reference hash could be found for " + entry.getKey());
								context.getUncheckedRecoveredFiles().add(entry.getKey());
							}
						}
					} else {
						if (CHECK_DEBUG_MODE) {
							Logger.defaultLogger().fine(entry.getKey() + " is not a file.");
						}
					}
				} else {
					if (CHECK_DEBUG_MODE) {
						Logger.defaultLogger().fine(entry.getKey() + " does not pass the filter.");
					}
				}
			}
		} finally {
			context.getInfoChannel().print("Check completed - " + context.getNbChecked() + " files checked.");

			// Close iterators
			for (int i=0; i<archives.length; i++) {
				iters[i].close();
			}
		}
	}

	/**
	 * Check whether the file has been modified since the previous backup or not
	 */
	private boolean checkModified(FileSystemRecoveryEntry entry, ProcessContext context) throws IOException {
		TraceFileIterator iter = context.getReferenceTrace();
		if (iter == null) {
			return true;		// No iterator -> Full backup
		}
		String shortPath = entry.getKey();

		while (true) {
			String key = iter.current() == null ? null : iter.current().getKey();

			// Compare the file paths
			int result = key == null ? -1 : FilePathComparator.instance().compare(shortPath, key);

			if (result == 0) {
				// Found among source files and in trace -> ok : check hash codes
				String newHash = ArchiveTraceParser.hash(entry, false);
				String oldHash = iter.current() == null ? null : ArchiveTraceParser.extractHashFromTrace(iter.current().getData());

				// Fetch next entry
				iter.next();

				// return result
				if (newHash.equals(oldHash)) {
					entry.setStatus(EntryStatus.STATUS_NOT_STORED);
					return false;
				} else {
					entry.setStatus(EntryStatus.STATUS_MODIFIED);
					return true;
				}
			} else if (result < 0) {
				// File found in source files but not found in trace -> new File
				entry.setStatus(EntryStatus.STATUS_CREATED);
				return true;
			} else {
				// File found in trace but not among source files -> deleted file
				iter.next();
			}
		}
	}

	/**
	 * Try to delete the file and its parent(s)
	 */
	private void deleteRecur(File root, File current) {
		if (FileSystemManager.delete(current)) {
			File parent = FileSystemManager.getParentFile(current);
			if (FileNameUtil.normalizePath(FileSystemManager.getAbsolutePath(parent))
					.startsWith(FileNameUtil.normalizePath(FileSystemManager.getAbsolutePath(root)))) {
				deleteRecur(root, parent); // The parent will be deleted only if it is empty
			}
		}
	}

	private File fetchNextFile(FileSystemIterator iter) {
		if (iter.hasNext()) {
			return iter.nextFile();
		} else {
			return null;
		}
	}

	private TraceEntry fetchNextTraceEntry(TraceFileIterator iter) throws IOException {
		if (iter.hasNext()) {
			return iter.next();
		} else {
			return null;
		}
	}  

	private List getAggregatedView(AggregatedViewContext context, String root, GregorianCalendar date, boolean aggregated) throws ApplicationException {
		try {	
			Map directories = new HashMap();
			File referenceArchive = this.getLastArchive(null, date);
			if (referenceArchive == null) {
				return new ArrayList();
			}
			boolean logicalView = date == null;

			// Build aggregated iterator
			if (context.getData() == null) {
				// Build the reference iterator
				AbstractMetaDataFileIterator referenceIter;
				if (logicalView) {
					// Case 1 : no date has been provided : use latest trace as reference (used by logical view)
					File trcFile = ArchiveTraceManager.resolveTraceFileForArchive(this, referenceArchive);
					ArchiveTraceAdapter trcReader = new ArchiveTraceAdapter(trcFile);
					referenceIter = trcReader.buildIterator();
				} else {
					// Case 2 : a date is provided : use archive content as of provided date (used by physical view)
					File ctnFile = ArchiveContentManager.resolveContentFileForArchive(this, referenceArchive);
					ArchiveContentAdapter ctnReader = new ArchiveContentAdapter(ctnFile);
					referenceIter = ctnReader.buildIterator();
				}

				// Merge the traces
				File[] archives;
				if (logicalView && aggregated) {
					archives = this.listArchives(null, null);
				} else {
					archives = new File[] {referenceArchive};
				}
				context.setData(TraceMerger.buildAggregatedTraceFile(this, archives, referenceIter));
			}

			File aggrFile = (File)context.getData();
			ArchiveTraceAdapter aggrReader = new ArchiveTraceAdapter(aggrFile);
			TraceFileIterator aggrIter = aggrReader.buildIterator();
			try {
				// Populate the set
				boolean found = true;
				if (root.length() != 0) {
					found = aggrIter.fetchUntil(root);
					// skip the directory itself
					aggrIter.next(); 
				}

				if (! found) {
					throw new IllegalStateException(root + " not found in aggregated trace.");
				} else {
					ArrayList ret = new ArrayList();

					while (aggrIter.hasNext()) {
						TraceEntry entry = aggrIter.next();
						String entryKey = entry.getKey();

						// Check that the entry is a (direct or indirect) child of the root entry
						if (entryKey.startsWith(root) && (root.length() == 0 || entryKey.charAt(root.length()) == '/')) {
							int idx = entryKey.indexOf('/', root.length()+1);

							// If the entry is a direct child of the root entry, add it to the returned list
							if (idx == -1) {
								ret.add(entry);

								// If the entry is a directory, register it
								if (entry.getType() == MetadataConstants.T_DIR) {
									DirectoryData dt = new DirectoryData();
									dt.entry = entry;
									dt.size = 0;
									dt.exists = false;
									directories.put(entryKey, dt);
								}
							} else if (entry.getType() == MetadataConstants.T_FILE) {
								String subdirectory = entryKey.substring(0, idx);
								DirectoryData dt = (DirectoryData)directories.get(subdirectory);

								// Update directory data ('exist' flag and size)
								boolean exists = entry.getData().charAt(0) == '1';
								dt.exists = dt.exists || exists;
								if (exists || logicalView) {
									dt.size += Long.parseLong(entry.getData().substring(1));
								}
							}
						} else {
							break;
						}
					}

					// Process directories
					Iterator iter = directories.entrySet().iterator();
					while (iter.hasNext()) {
						Map.Entry entry = (Map.Entry)iter.next();
						DirectoryData data = (DirectoryData)entry.getValue();
						data.entry.setData((data.exists ? "1":"0") + data.size);
					}

					// Return the set
					return ret;
				}
			} finally {
				if (aggrIter != null) {
					aggrIter.close();
				}
			}
		} catch (TaskCancelledException e) {
			Logger.defaultLogger().error(e); // SHALL NEVER HAPPEN
			return null;
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		}
	}

	private boolean hasBackupScheme(File archive, String backupScheme) throws ApplicationException {
		if (backupScheme == null) {
			return true;
		} else  {
			Manifest mf = ArchiveManifestCache.getInstance().getManifest(this, archive);
			if (mf == null) {
				return false;
			} else {
				String tested = mf.getStringProperty(ManifestKeys.OPTION_BACKUP_SCHEME);
				return tested != null && tested.equals(backupScheme);
			}
		}
	}

	private void searchWithinArchive(SearchCriteria criteria, File archive, TargetSearchResult result) throws ApplicationException {
		TraceFileIterator iter = null;

		try {
			try {
				File traceFile = ArchiveTraceManager.resolveTraceFileForArchive(this, archive);
				ArchiveTraceAdapter adapter = new ArchiveTraceAdapter(traceFile);
				iter = adapter.buildIterator();

				SearchMatcher matcher = new SearchMatcher((DefaultSearchCriteria)criteria);

				Manifest mf = ArchiveManifestCache.getInstance().getManifest(this, archive);
				String root = ((FileSystemRecoveryTarget)this.getTarget()).getSourceDirectory();

				while (iter.hasNext()) {
					TraceEntry trcEntry = iter.next();
					if (trcEntry.getType() != MetadataConstants.T_DIR && matcher.matches(trcEntry.getKey())) {
						File path = new File(root, trcEntry.getKey());
						SearchResultItem item = new SearchResultItem();
						item.setCalendar(mf.getDate());
						item.setEntry(trcEntry);
						item.setTarget(this.getTarget());

						result.addSearchresultItem(item);
					}
				}
			} finally {
				if (iter != null) {
					iter.close();
				}
			}
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		}
	}

	/**
	 * It is sometime necessary to work on a copy of metadata files.
	 * <BR>This method creates a temporary copy of a source file.
	 */
	protected File duplicateMetadataFile(File source, ProcessContext context) {
		File target = null;
		if (FileSystemManager.exists(source)) {
			try {
				// Copy file in a temporary place
				target = FileTool.getInstance().generateNewWorkingFile("areca", "mdt", true);
				FileTool.getInstance().copyFile(source, FileSystemManager.getParentFile(target), FileSystemManager.getName(target), null, null);
			} catch (IOException e) {
				Logger.defaultLogger().error(e);
				throw new IllegalStateException(e);
			} catch (TaskCancelledException e) {
				// ignored : never happens
				Logger.defaultLogger().error(e);
			}
		}
		return target;
	}

	/**
	 * Called by the "merge" method : merges the metadata files : content and hash files.
	 */
	private void storeAggregatedContent(
			File[] recoveredFiles, 
			File traceFile, 
			ProcessContext context
	) throws ApplicationException, TaskCancelledException {
		File contentTarget = new File(getDataDirectory(context.getCurrentArchiveFile()), getContentFileName());
		File hashTarget = new File(getDataDirectory(context.getCurrentArchiveFile()), getHashFileName());
		//////////////// ugly, but no time to do better /////////////////////////////
		File handlerTarget = handler.getContentFile(context.getCurrentArchiveFile());
		/////////////////////////////////////////////////////////////////////////////

		ArchiveContentAdapter contentWriter = null;
		ArchiveContentAdapter hashWriter = null;
		ArchiveContentAdapter handlerWriter = null;
		ContentFileIterator[] contentIters = null;
		ContentFileIterator[] hashIters = null;
		ContentFileIterator[] handlerIters = null;
		TraceFileIterator traceIter = null;

		try {
			// Build the writers
			contentWriter = new ArchiveContentAdapter(contentTarget);
			hashWriter = new ArchiveContentAdapter(hashTarget);
			if (handlerTarget != null) {
				handlerWriter = new ArchiveContentAdapter(handlerTarget);
			}

			// Build trace Iterator
			ArchiveTraceAdapter adp = new ArchiveTraceAdapter(traceFile);
			traceIter = adp.buildIterator();

			// Build content/hash/handler files iterator
			contentIters = new ContentFileIterator[recoveredFiles.length];
			hashIters = new ContentFileIterator[recoveredFiles.length];
			if (handlerTarget != null) {
				handlerIters = new ContentFileIterator[recoveredFiles.length];
			}
			for (int i=0; i<recoveredFiles.length; i++) {
				File contentFile = ArchiveContentManager.resolveContentFileForArchive(this, recoveredFiles[i]);
				ArchiveContentAdapter contentReader = new ArchiveContentAdapter(contentFile);
				contentIters[i] = contentReader.buildIterator();

				File hashFile = ArchiveContentManager.resolveHashFileForArchive(this, recoveredFiles[i]);
				ArchiveContentAdapter hashReader = new ArchiveContentAdapter(hashFile);
				hashIters[i] = hashReader.buildIterator();

				if (handlerTarget != null) {
					File handlerFile = ArchiveContentManager.resolveSequenceFileForArchive(this, recoveredFiles[i]);
					ArchiveContentAdapter handlerReader = new ArchiveContentAdapter(handlerFile);
					handlerIters[i] = handlerReader.buildIterator();
				}
			}

			String previousKey = null;
			boolean shallStop = false;
			while (! shallStop) {

				// Look for minimum key
				int minIndex = -1;
				for (int i=recoveredFiles.length-1; i>=0; i--) {
					if (contentIters[i].current() != null) {
						int result = minIndex == -1 ? -1 : FilePathComparator.instance().compare(contentIters[i].current().getKey(), contentIters[minIndex].current().getKey());
						if (result < 0) {
							minIndex = i;
						}
					}
				}

				// Nothing more to read
				if (minIndex == -1) {
					break;
				}

				// Once the key has been found, compare it to the previous entry
				int result = previousKey == null ? -1 : FilePathComparator.instance().compare(previousKey, contentIters[minIndex].current().getKey());
				if (result == 0) {
					// already written -> do nothing
				} else if (result < 0) {
					// minKey > previousEntry -> new entry -> write it
					while (true) {
						TraceEntry trcEntry = traceIter.current();
						if (trcEntry == null) {
							// Nothing more in trace -> do not write entry and stop iterating
							shallStop = true;
							break;
						} else {						
							// Compare the content entry and the trace entry
							int trcResult = FilePathComparator.instance().compare(contentIters[minIndex].current().getKey(), trcEntry.getKey());
							if (trcResult == 0) {
								// Found in trace -> write entry and advance trace
								contentWriter.writeGenericEntry(contentIters[minIndex].current());
								hashWriter.writeGenericEntry(hashIters[minIndex].current());
								if (handlerTarget != null) {
									handlerWriter.writeGenericEntry(handlerIters[minIndex].current());
								}
								traceIter.next();
								break;
							} else if (trcResult < 0) {
								// traceKey > contentKey -> not found in trace -> do not write entry
								break;
							} else {
								// traceKey < contentKey -> advance trace and do nothing
								traceIter.next();
							}
						}
					}
				} else {
					// minKey < previousEntry -> error !
					throw new IllegalStateException("" + contentIters[minIndex].current() + "<" + previousKey);
				}

				// Fetch next entry
				contentIters[minIndex].next();
				hashIters[minIndex].next();
				if (handlerTarget != null) {
					handlerIters[minIndex].next();					
				}
			}
		} catch (IOException e) {
			Logger.defaultLogger().error("Error merging content", e);
			throw new ApplicationException("Error merging content", e);
		} finally {
			try {
				try {
					if (contentWriter != null) {
						contentWriter.close();
					}
					if (hashWriter != null) {
						hashWriter.close();
					}
					if (handlerWriter != null) {
						handlerWriter.close();
					}
				} finally {
					try {
						if (contentIters != null) {
							for (int i=0; i<recoveredFiles.length; i++) {
								if (contentIters != null) {
									contentIters[i].close();
								}
								if (hashIters != null) {
									hashIters[i].close();
								}
								if (handlerIters != null) {
									handlerIters[i].close();
								}
							}
						}
					} finally {
						if (traceIter != null) {
							traceIter.close();
						}
					}
				}
			} catch (IOException e) {
				Logger.defaultLogger().error(e);
				throw new ApplicationException("Error writing merged content on " + FileSystemManager.getAbsolutePath(hashTarget), e);
			}
		}
	}
}