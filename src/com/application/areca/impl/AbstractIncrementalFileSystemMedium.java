package com.application.areca.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.application.areca.AbstractTarget;
import com.application.areca.ApplicationException;
import com.application.areca.ArchiveMedium;
import com.application.areca.ArecaConfiguration;
import com.application.areca.ArecaFileConstants;
import com.application.areca.ArecaRawFileList;
import com.application.areca.EntryArchiveData;
import com.application.areca.EntryStatus;
import com.application.areca.LogHelper;
import com.application.areca.MergeParameters;
import com.application.areca.RecoveryEntry;
import com.application.areca.StoreException;
import com.application.areca.TargetActions;
import com.application.areca.Utils;
import com.application.areca.adapters.write.TargetXMLWriter;
import com.application.areca.cache.ArchiveManifestCache;
import com.application.areca.context.ProcessContext;
import com.application.areca.context.RecoveryResult;
import com.application.areca.impl.copypolicy.AbstractCopyPolicy;
import com.application.areca.impl.handler.ArchiveHandler;
import com.application.areca.impl.handler.DeltaArchiveHandler;
import com.application.areca.impl.handler.EntriesDispatcher;
import com.application.areca.impl.policy.AccessInformations;
import com.application.areca.impl.tools.ArchiveComparator;
import com.application.areca.impl.tools.ArchiveNameFilter;
import com.application.areca.impl.tools.RecoveryFilterMap;
import com.application.areca.metadata.AbstractMetaDataEntry;
import com.application.areca.metadata.AbstractMetaDataFileIterator;
import com.application.areca.metadata.FileList;
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
import com.application.areca.metadata.transaction.TransactionPoint;
import com.application.areca.search.DefaultSearchCriteria;
import com.application.areca.search.SearchCriteria;
import com.application.areca.search.SearchMatcher;
import com.application.areca.search.SearchResultItem;
import com.application.areca.search.TargetSearchResult;
import com.application.areca.version.VersionInfos;
import com.myJava.file.EventInputStream;
import com.myJava.file.FileList.FileListIterator;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.HashInputStreamListener;
import com.myJava.file.archive.ArchiveWriter;
import com.myJava.file.archive.zip64.ZipArchiveAdapter;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.file.driver.contenthash.ContentHashFileSystemDriver;
import com.myJava.file.iterator.FilePathComparator;
import com.myJava.file.metadata.FileMetaDataAccessor;
import com.myJava.file.metadata.FileMetaDataSerializationException;
import com.myJava.system.OSTool;
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
public abstract class AbstractIncrementalFileSystemMedium 
extends AbstractFileSystemMedium 
implements TargetActions {

	private static class DirectoryData {
		public long size;
		public boolean exists;
		public TraceEntry entry;
	}

	protected static final boolean DEBUG_MODE = ArecaConfiguration.get().isBackupDebug();
	protected static final boolean CHECK_DEBUG_MODE = ArecaConfiguration.get().isCheckDebug();
	protected static final boolean TH_MON_ENABLED = ArecaConfiguration.get().isThreadMonitorEnabled();
	protected static final long TH_MON_DELAY = ArecaConfiguration.get().getThreadMonitorDelay();
	protected static final int MAX_DETAILED_ERRORS = 50;

	/**
	 * Filenames reserved by Areca
	 */
	protected static final String[] RESERVED_NAMES = new String[] {
		ArecaFileConstants.TMP_MERGE_LOCATION, 
		ArecaFileConstants.CHECK_DESTINATION, 
		ArecaFileConstants.HISTORY_NAME
	};

	/**
	 * Handler for archive processing
	 */
	protected ArchiveHandler handler;
	
	/**
	 * Tells whether the content of the file must be checked to decide whether it will be stored or not
	 */
	protected boolean inspectFileContent = false;

	public void setHandler(ArchiveHandler handler) {
		this.handler = handler;
		handler.setMedium(this);
	}

	public ArchiveHandler getHandler() {
		return handler;
	}

	public boolean isInspectFileContent() {
		return inspectFileContent;
	}

	public void setInspectFileContent(boolean inspectFileContent) {
		this.inspectFileContent = inspectFileContent;
	}

	public Manifest buildDefaultBackupManifest() throws ApplicationException {
		Manifest manifest = new Manifest(Manifest.TYPE_BACKUP);

		Manifest lastMf = ArchiveManifestCache.getInstance().getManifest(this, this.getLastArchive());
		if (lastMf != null) {
			manifest.setTitle(lastMf.getTitle());
		}

		return manifest;
	}

	/**
	 * Return a description for the medium
	 */
	public String getDescription() {
		String type;
		if (image) {
			type = "image"; 
		} else if (handler instanceof DeltaArchiveHandler) {
			type = "delta";
		} else {
			type = "standard";
		}
		return getSubDescription() + " " + type + " storage (" + fileSystemPolicy.getArchivePath() + ")";        
	}  

	protected abstract String getSubDescription();

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
	
	protected abstract void dbgBuildArchiveFileList(File archive, BufferedWriter writer) throws IOException, ApplicationException;
	
	public File createDebuggingData(File directory) throws ApplicationException, TaskCancelledException {
		ZipArchiveAdapter adapter = null;
		ArchiveWriter writer = null;
		try {
			File targetFile = new File(directory, "areca_debug.zip");
			FileTool.getInstance().delete(targetFile);
			adapter = new ZipArchiveAdapter(FileSystemManager.getFileOutputStream(targetFile), false, 9);
			writer = new ArchiveWriter(adapter);
			
			// Store configuration
			String configurationPrefix = ""+ getTarget().getUid()+".bcfg";
			StringBuffer sb = new StringBuffer();
			TargetXMLWriter tgWriter = new TargetXMLWriter(sb, false);
			tgWriter.setRemoveSensitiveData(true);
			tgWriter.serializeTarget((FileSystemTarget)this.getTarget());
			writer.addFile(configurationPrefix, tgWriter.getXML());
			
			// Store history
			String historyPrefix = "history";
			File historyFile = new File(fileSystemPolicy.getArchiveDirectory(), this.getHistoryName());
			writer.addFile(historyFile, historyPrefix, null, null);
			
			// Store readme
			String readmePrefix = "readme.txt";
			String readme = "This zip file contains debugging informations that can be used to diagnose issues encountered with Areca Backup.";
			readme += "\nIt has been generated on " + CalendarUtils.getFullDateToString(new GregorianCalendar()) + " with Areca-Backup v" + VersionInfos.getLastVersion().getVersionId() + " for target #" + getTarget().getUid() + " (" + getTarget().getName() + ").";
			readme += "\n\nIt contains : ";
			readme += "\n- 'properties.txt' : The properties of your Java environment and your user preferences";
			readme += "\n- 'history' : The history of operations performed on your archives (merges, backups, recoveries, ...)";
			readme += "\n- '" + getTarget().getUid()+".bcfg' : Your target configuration (without passwords)";
			readme += "\n\n ... and for each archive :";
			readme += "\n- 'manifest' : Various informations about your archive (description, filename encoding, number of stored files, ...)";
			readme += "\n- 'trace' : The list of all source files at backup time (name, size and last modification date, plus some attributes depending of your filesystem - user permissions for instance)";
			readme += "\n- 'content' : The list of all files that were actually stored in your archive (name, size)";
			readme += "\n\n'" + getTarget().getUid()+".bcfg' and 'properties.txt' can be edited directly with your favorite text editor.";
			readme += "\n\nTo check the content of 'history', 'manifest', 'trace' and 'content' files, add the '.gz' extension to their name, open them with an archive reader (Winzip or 7zip for instance) and edit the contained file with your text editor.";
			writer.addFile(readmePrefix, readme);
			
			// Store system properties
			String propertiesPrefix = "properties.txt";
			writer.addFile(propertiesPrefix, Utils.getPropertiesAndPreferences());
			
			// Handle archives
			File[] archives = this.listArchives(null, null, true);
			for (int i=0; i<archives.length; i++) {
				Logger.defaultLogger().info("Creating debug data for " + FileSystemManager.getAbsolutePath(archives[i]) + " ...");
				
	            File dataDir = AbstractFileSystemMedium.getDataDirectory(archives[i]);
	            String prefix = FileSystemManager.getName(dataDir) + "/";
	            
				// Handle trace
				File traceFile = ArchiveTraceManager.resolveTraceFileForArchive(this, archives[i]);
				String traceFullPath = prefix + FileSystemManager.getName(traceFile);
	            writer.addFile(traceFile, traceFullPath, null, null);
	            
	            // Handle manifest
	            File manifestFile = new File(dataDir, getManifestName());
				String manifestFullPath = prefix + FileSystemManager.getName(manifestFile);
	            writer.addFile(manifestFile, manifestFullPath, null, null);
	            
	            // Handle content
	            File contentFile = ArchiveContentManager.resolveContentFileForArchive(this, archives[i]);
				String contentFullPath = prefix + FileSystemManager.getName(contentFile);
	            writer.addFile(contentFile, contentFullPath, null, null);
	            
	            // Handle effective content
	    		File effContentFile = FileTool.getInstance().generateNewWorkingFile(null, null, "areca", true);
	    		BufferedWriter ctWriter = null;
	    		try {
					ctWriter = new BufferedWriter(FileSystemManager.getWriter(effContentFile));
					this.dbgBuildArchiveFileList(archives[i], ctWriter);
				} finally {
					if (ctWriter != null) {
						ctWriter.close();
					}
				}
				writer.addFile(effContentFile, FileSystemManager.getName(archives[i]) + "___.txt", null, null);
				FileTool.getInstance().delete(effContentFile);
			}
			return targetFile;
		} catch (IOException e) {
			throw new ApplicationException("Error caught while generating debugging data.", e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					throw new ApplicationException("Error caught while generating debugging data.", e);
				}
			}
		}
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
			boolean simulateRecovery,
			GregorianCalendar date, 
			Set ignoreList,
			ProcessContext context) 
	throws ApplicationException, TaskCancelledException {
		this.ensureInstalled();
		
		this.initializeThrottling(context.getTaskMonitor());
		
		try {
			// Compute recovery destination
			File destinationRoot = destination == null ? fileSystemPolicy.getArchiveDirectory() : new File((String)destination);
			File destinationFile = FileTool.getInstance().createNewWorkingDirectory(
					destinationRoot, 
					ArecaFileConstants.CHECK_DESTINATION, 
					true
			);

			context.getInfoChannel().print("Checking archive (working directory : " + FileSystemManager.getDisplayPath(destinationFile) + ") ...");

			// Get the trace file
			File lastArchive = getLastArchive(null, ignoreList, date);
			File traceFile = ArchiveTraceManager.resolveTraceFileForArchive(this, lastArchive);

			// Recover at a temporary location - activate the archive check option
			ArchiveScope perimeter;
			if (checkOnlyArchiveContent && handler.autonomousArchives()) {
				perimeter = new FileScopePerimeter(lastArchive);
			} else {
				perimeter = new DateArchiveScope(null, date);
			}
			perimeter.setIgnoredArchives(ignoreList);
			//HERE
			recover(
					destinationFile, 
					null,
					null, 
					null,
					1, 
					perimeter,
					traceFile, 
					ArchiveMedium.RECOVER_MODE_RECOVER,
					false,
					true, 
					simulateRecovery,
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
					context.getInfoChannel().print("Cleaning recovered files (" + FileSystemManager.getDisplayPath(context.getRecoveryDestination()) + ") ...");
					if (FileSystemManager.exists(context.getRecoveryDestination())) {
						FileTool.getInstance().delete(context.getRecoveryDestination());
					}
					context.getInfoChannel().print("Recovered files cleaned.");
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
			Logger.defaultLogger().info("Cleaning temporary data in " + FileSystemManager.getDisplayPath(context.getRecoveryDestination()) + " ...");
			AbstractFileSystemMedium.tool.delete(context.getRecoveryDestination());
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
		//Chronometer.instance().start("commit");
		
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
			context.getReport().setContentFile(context.getContentAdapter().getFile());
			context.setContentAdapter(null);

			// Close the hash file
			context.getHashAdapter().close();
			context.setHashAdapter(null);

			// Add properties to manifest
			context.getManifest().addProperty(ManifestKeys.UNMODIFIED_FILES, context.getReport().getIgnoredFiles());
			if (FileSystemManager.isFile(context.getCurrentArchiveFile()) || (! isImage())) {
				context.getManifest().addProperty(ManifestKeys.ARCHIVE_SIZE, context.getOutputStreamListener().getWritten()); 
			}
			context.getManifest().addProperty(ManifestKeys.STORED_FILES, context.getReport().getSavedFiles());
			context.getManifest().addProperty(ManifestKeys.ARCHIVE_NAME, FileSystemManager.getName(context.getCurrentArchiveFile()));

			// Check archive size
			FileSystemManager.getInstance().clearCachedData(context.getCurrentArchiveFile());
			if (FileSystemManager.isFile(context.getCurrentArchiveFile())) {
				long size = FileSystemManager.length(context.getCurrentArchiveFile());
				if (size != context.getOutputStreamListener().getWritten()) {
					Logger.defaultLogger().warn("Caution : Archive size (" + size + ") doesn't match the amount of written data (" + context.getOutputStreamListener().getWritten());
				} else {
					Logger.defaultLogger().fine("Archive size validated (" + size + ")");
				}
			}

			// Store the manifest
			this.storeManifest(context);  

			// Flush all local files
			FileSystemManager.getInstance().flush(context.getCurrentArchiveFile());

			// Convert the archive : commit
			this.convertArchiveToFinal(context);

			// Once the archive is committed, we can do all the cleaning.

			// Create a copy of the target's XML configuration
			if (ArecaConfiguration.get().isXMLBackup()) {
				this.target.secureUpdateCurrentTask("Creating a copy of the target's XML configuration ...", context);
				this.storeTargetConfigBackup(context);
			}

			// Close the trace iterator
			if (context.getReferenceTrace() != null) {
				context.getReferenceTrace().close();
			}

			// Once all is completed, we can close (and destroy) the current transaction point
			closeCurrentTransactionPoint(true, true, context);

			// Destroy intermediate transaction data
			File transactionDir = new File(getDataDirectory(context.getCurrentArchiveFile()), ArecaFileConstants.TRANSACTION_FILE);
			FileTool.getInstance().delete(transactionDir);

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
			//Chronometer.instance().stop("commit");
			//Chronometer.instance().stop("open_to_commit");
		}
	}

	protected void closeCurrentTransactionPoint(boolean commit, boolean destroy, ProcessContext context) throws ApplicationException {
		try {
			if (context.getCurrentTransactionPoint() != null) {

				// In some cases, this method can be invoked on an already committed transaction point
				// (when we are resuming a backup, which means that we're starting from an already committed transaction point)
				if (! context.getCurrentTransactionPoint().isCommitted()) {
					context.getCurrentTransactionPoint().writeClose(commit, context);
				}

				if (destroy) {
					context.getCurrentTransactionPoint().destroyTransactionFiles();
				}
			}
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		}
	}

	/**
	 * Save a temporary transaction point
	 */
	public void initTransactionPoint(ProcessContext context) throws ApplicationException {
		if (useTransactions) {
			closeCurrentTransactionPoint(true, false, context);

			context.setCurrentTransactionPoint(
					new TransactionPoint(getDataDirectory(context.getCurrentArchiveFile()), context.getCurrentTransactionPoint())
			);

			try {
				context.getCurrentTransactionPoint().writeInit(context);
			} catch (IOException e) {
				Logger.defaultLogger().error(e);
				throw new ApplicationException(e);
			}
		}
	}

	public void preCommitMerge(ProcessContext context) throws ApplicationException {
		if (! this.image) {
			this.target.secureUpdateCurrentTask("Committing merge ...", context);
			super.preCommitMerge(context);

			try {
				// Clean temporary data
				this.cleanMerge(context);

				// Close the archive
				this.closeArchive(context); 

				// Commit the archive
				if (context.getCurrentArchiveFile() != null) {
					this.convertArchiveToFinal(context);
				}
				
				this.target.secureUpdateCurrentTask("Merge completed", context);
			} catch (IOException e) {		
				Logger.defaultLogger().error("Exception caught during merge commit.", e);
				this.rollbackMerge(context);
				throw new ApplicationException(e);
			}
		}
	}
	
	public void finalizeMerge(ProcessContext context) throws ApplicationException {
		if (! this.image) {
			this.target.secureUpdateCurrentTask("Cleaning old archives ...", context);
			context.getInfoChannel().print("Cleaning old archives ...");

			try {
				// Delete recovered archives
				File[] recoveredFiles = context.getReport().getRecoveryResult().getRecoveredArchivesAsArray();
				File[] ignoredFiles = context.getReport().getRecoveryResult().getIgnoredArchivesAsArray();

				// Delete unprocessed archives
				Logger.defaultLogger().info("Deleting unnecessary archives : " + ignoredFiles.length + " archives.");
				for (int i=0; i<ignoredFiles.length; i++) {
					Logger.defaultLogger().info("Deleting " + FileSystemManager.getDisplayPath(ignoredFiles[i]) + " ...");
					this.deleteArchive(ignoredFiles[i]);                       
				}

				if (recoveredFiles.length >= 2) {
					// Delete recovered archives
					Logger.defaultLogger().info("Cleaning recovered archives : " + recoveredFiles.length + " archives.");
					for (int i=0; i<recoveredFiles.length; i++) {
						Logger.defaultLogger().info("Deleting " + FileSystemManager.getDisplayPath(recoveredFiles[i]) + " ...");
						this.deleteArchive(recoveredFiles[i]);                       
					}

					this.target.secureUpdateCurrentTask("" + context.getReport().getRecoveryResult().getProcessedArchives().size() + " archives merged.", context);
				} else {
					this.target.secureUpdateCurrentTask("No archive merged.", context);
				}

			} catch (IOException e) {		
				Logger.defaultLogger().error("Exception caught during merge finalization.", e);
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
			boolean mergeRecoveredFiles,
			File destination,
			RecoveryFilterMap filtersByArchive, 
			AbstractCopyPolicy policy,
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
			if (prp == -1 && (forceComputation || FileSystemManager.isFile(archive) || FileSystemManager.getAccessEfficiency(archive) > FileSystemDriver.ACCESS_EFFICIENCY_GOOD)) {
				try {
					Logger.defaultLogger().info("Computing size for " + FileSystemManager.getDisplayPath(archive));
					prp = FileTool.getInstance().getSize(archive);
				} catch (FileNotFoundException e) {
					Logger.defaultLogger().error("Error computing size for " + FileSystemManager.getDisplayPath(archive), e);
				}
				manifest.addProperty(ManifestKeys.ARCHIVE_SIZE, String.valueOf(prp));
			}
			return prp;
		} else {
			return 0;
		}
	}

	public List getEntries(AggregatedViewContext context, String root, GregorianCalendar date) throws ApplicationException {
		return getAggregatedView(context, root, date, false);
	}

	/**
	 * Return the final content file name
	 */
	public String getContentFileName() {
		return ArecaFileConstants.CONTENT_FILE;
	}

	/**
	 * Return the final hash file name
	 */
	public String getHashFileName() {
		return ArecaFileConstants.HASH_FILE;
	}

	/**
	 * Return the final trace file name
	 */
	public String getTraceFileName() {
		return ArecaFileConstants.TRACE_FILE;            
	}   

	/**
	 * Returns the last archive for a given date.
	 */
	public File getLastArchive(String backupScheme, GregorianCalendar date) throws ApplicationException {
		return getLastArchive(backupScheme, null, date);
	}
	
	/**
	 * Returns the last archive for a given date.
	 */
	public File getLastArchive(String backupScheme, Set exclusionList, GregorianCalendar date) throws ApplicationException {
		String defaultName = computeArchivePath(date);
		File defaultFile = new File(defaultName);

		if ((exclusionList == null || ! exclusionList.contains(defaultFile)) && FileSystemManager.exists(defaultFile) && hasBackupScheme(defaultFile, backupScheme) && isCommitted(defaultFile)) {
			return defaultFile;
		} else {
			File[] archives = listArchives(null, null, date, exclusionList, true);
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
				if (backupScheme != null && backupScheme.equals(AbstractTarget.BACKUP_SCHEME_FULL)) {
					return archives[0];
				}

				return null;
			}
		}
	}

	public List getLogicalView(AggregatedViewContext context, String root, boolean aggregated) throws ApplicationException {
		return getAggregatedView(context, root, null, aggregated);
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
				if (prp == null || prp.equals(AbstractTarget.BACKUP_SCHEME_INCREMENTAL)) {
					// do nothing
				} else if (prp.equals(AbstractTarget.BACKUP_SCHEME_DIFFERENTIAL)) {
					hasDifferentialBackup = true;
				} else if (prp.equals(AbstractTarget.BACKUP_SCHEME_FULL)) {
					hasFullBackup = true;
				}
			}
		}  
		if (hasFullBackup) {
			manifest.addProperty(ManifestKeys.OPTION_BACKUP_SCHEME, AbstractTarget.BACKUP_SCHEME_FULL);
		} else if (hasDifferentialBackup) {
			manifest.addProperty(ManifestKeys.OPTION_BACKUP_SCHEME, AbstractTarget.BACKUP_SCHEME_DIFFERENTIAL);            
		} else {
			manifest.addProperty(ManifestKeys.OPTION_BACKUP_SCHEME, AbstractTarget.BACKUP_SCHEME_INCREMENTAL);            
		}
	}

	public void install() throws ApplicationException {
		super.install();
		
		if (compressionArguments.isMultiVolumes() && ! compressionArguments.isAddExtension()) {
			throw new ApplicationException("The \".zip\" extension is mandatory if zip-splitting is enabled.");
		}

		if ((! handler.supportsImageBackup()) && image) {
			throw new ApplicationException("Incoherent configuration : image archives are not compatible with delta backup.");
		}
	}
	
	/**
	 * Lists the medium's archives
	 */
	public File[] listArchives(String root, GregorianCalendar fromDate, GregorianCalendar toDate, Set excludeList, boolean committedOnly) throws ApplicationException {
		this.ensureInstalled();
		
		if (root == null) {
			root = fileSystemPolicy.getArchivePath();
		}
		
		File[] ret = null;
		if (this.image) {
			File f = new File(root, computeArchiveName(fromDate));
			if (FileSystemManager.exists(f) && checkArchiveCompatibility(f, committedOnly) && (excludeList == null || ! excludeList.contains(f))) {
				ret = new File[] {f};                
			} else {
				ret = new File[] {};
			}
		} else {
			File rootArchiveDirectory = new File(root);
			File[] elementaryArchives = FileSystemManager.listFiles(rootArchiveDirectory, new ArchiveNameFilter(fromDate, toDate, this, committedOnly));

			if (elementaryArchives != null) {
				List filtered = new ArrayList();
				
				for (int i=0; i<elementaryArchives.length; i++) {
					if (excludeList == null || ! excludeList.contains(elementaryArchives[i])) {
						filtered.add(elementaryArchives[i]);
					}
				}
				
				elementaryArchives = (File[])filtered.toArray(new File[filtered.size()]);

				Arrays.sort(elementaryArchives, new ArchiveComparator(this));
			} else {
				elementaryArchives = new File[0];
			}

			ret = elementaryArchives;
		}

		return ret;
	}

	/**
	 * Lists the medium's archives
	 */
	public File[] listArchives(GregorianCalendar fromDate, GregorianCalendar toDate, boolean committedOnly) throws ApplicationException {
		AccessInformations accessInfos = getFileSystemPolicy().checkReachable();
		if (! accessInfos.isReachable()) {
			Logger.defaultLogger().error(accessInfos.getMessage(), accessInfos.getException());
			throw new ApplicationException(accessInfos.getMessage(), accessInfos.getException());
		}

		return listArchives(null, fromDate, toDate, null, committedOnly);
	}

	/**
	 * Log recovery informations
	 */
	public void logRecoveryStep(RecoveryFilterMap filesByArchive, com.application.areca.metadata.FileList files, File archive, ProcessContext context) 
	throws TaskCancelledException {
		context.getTaskMonitor().checkTaskState();  
		if (filesByArchive != null && files == null) {
			context.getInfoChannel().print("Skipping " + FileSystemManager.getPath(archive));
		} else {
			String scope;

			if (filesByArchive == null) {
				scope = "All entries";
			} else if (files == null) {
				scope = "No entry";
			} else {
				scope =  "" + files.size();
				if (files.size() <= 1) {
					if (! files.containsDirectories()) {
						scope += " file";
					} else {
						scope += " directory";						
					}
				} else {
					if (files.containsDirectories() && files.containsFiles()) {
						scope += " files or directories";
					} else if (files.containsDirectories()) {
						scope += " directories";
					} else {
						scope += " files";
					}
				}
			}
			context.getInfoChannel().print("Recovering " + FileSystemManager.getDisplayPath(archive) + " (" + scope + ") ...");
		}
	}

	public void merge(
			GregorianCalendar fromDate, 
			GregorianCalendar toDate, 
			Manifest mfToInsert,
			MergeParameters params,
			ProcessContext context        
	) throws ApplicationException {
		this.ensureInstalled();
		
		this.initializeThrottling(context.getTaskMonitor());
		
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

			if (! image) { // No merge if "overwrite" = true
				Logger.defaultLogger().info(
						"Starting merge from " + Utils.formatDisplayDate(fromDate) + " to " + Utils.formatDisplayDate(toDate)
						+ "."
				);

				// Recovery of the merged archives
				context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.8, "recover");

				// Resolve trace file
				File traceFile;
				if (params.isKeepDeletedEntries()) {
					File[] archives = this.listArchives(fromDate, toDate, true);
					traceFile = TraceMerger.buildAggregatedTraceFile(this, archives);
				} else {
					traceFile = ArchiveTraceManager.resolveTraceFileForArchive(this, getLastArchive(null, toDate));
				}

				// Recover
				recover(
						null, 
						params.isUseSpecificLocation() ? new File(params.getSpecificLocation()) : null,
								null, 
								null,
								2,
								new DateArchiveScope(fromDate, toDate),
								traceFile, 
								ArchiveMedium.RECOVER_MODE_MERGE,
								params.isKeepDeletedEntries(),
								false,
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
					context.getManifest().addProperty(ManifestKeys.CHECKED, context.isChecked());
					
					AbstractTarget.addBasicInformationsToManifest(context.getManifest());
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

	public void open(Manifest manifest, TransactionPoint transactionPoint, ProcessContext context) throws ApplicationException { 
		this.ensureInstalled();
		
		this.initializeThrottling(context.getTaskMonitor());
		//Chronometer.instance().start("open_to_commit");
		//Chronometer.instance().start("open");
		
		try {  
			if (transactionPoint == null) {

				// Check the repository for uncommitted archives, or initialize the context from the previous transaction point
				this.checkRepository();

				Logger.defaultLogger().info("Opening medium (Backup scheme = '" + context.getBackupScheme() + "') ..."); 

				// Compute the archive path
				context.setCurrentArchiveFile(new File(computeFinalArchivePath()));  

				// Read the previous trace
				if (context.getBackupScheme().equals(AbstractTarget.BACKUP_SCHEME_FULL)) {
					Logger.defaultLogger().info("Using an empty archive as reference.");
					context.setReferenceTrace(null);
				} else {
					File lastArchive;
					if (context.getBackupScheme().equals(AbstractTarget.BACKUP_SCHEME_DIFFERENTIAL)) {
						lastArchive = this.getLastArchive(AbstractTarget.BACKUP_SCHEME_FULL, null);
					} else {
						lastArchive = this.getLastArchive();
					}

					if (lastArchive != null && FileSystemManager.exists(lastArchive)) {
						// Check file encoding
						this.checkArchivesEncoding(new File[] {lastArchive});
						
						// Resolve trace file
						Logger.defaultLogger().info("Using the following archive as reference : " + FileSystemManager.getDisplayPath(lastArchive) + ".");
						File trcFile = ArchiveTraceManager.resolveTraceFileForArchive(this, lastArchive);
						File f;
						if (image) {
							// In case of image backups, we need to duplicate the trace file in order to read it during the backup process.
							f = duplicateMetadataFile(trcFile, context);
							removeMarkerFile(lastArchive);  
						} else {
							f = trcFile;					
						}

						context.setReferenceTrace(ArchiveTraceAdapter.buildIterator(f));
					} else {
						// Build an empty trace
						Logger.defaultLogger().info("Using an empty archive as reference.");
						context.setReferenceTrace(null);
					} 
				}

				// Set the manifest
				if (manifest != null) {
					context.setManifest(manifest);
					manifest.addProperty(
							ManifestKeys.OPTION_BACKUP_SCHEME, 
							context.getReferenceTrace() == null ? AbstractTarget.BACKUP_SCHEME_FULL : context.getBackupScheme()
					);
				}

				// Initiate the archive
				buildArchive(context);
			} else {
				// Resuming a previous backup -> read the context from the transaction point
				ProcessContext source = transactionPoint.deserializeProcessContext(context);
				context.setEntryIndex(source.getEntryIndex());
				context.setInputBytes(source.getInputBytes());
				context.setInitialized(source.isInitialized());
				context.setCurrentArchiveFile(source.getCurrentArchiveFile());
				context.setBackupScheme(source.getBackupScheme());
				context.setOutputStreamListener(source.getOutputStreamListener());
				context.setManifest(source.getManifest());
				context.setReport(source.getReport());
				context.getReport().setTarget(this.getTarget());
				context.setFileSystemIterator(source.getFileSystemIterator());
				context.setCurrentTransactionPoint(transactionPoint);
				context.setPreviousHashIterator(source.getPreviousHashIterator());
				context.setReferenceTrace(source.getReferenceTrace());

				context.getManifest().addProperty(ManifestKeys.IS_RESUMED, "true");
				Logger.defaultLogger().info("Resuming backup (Backup scheme = '" + context.getBackupScheme() + "') ..."); 
			}

			LogHelper.logFileInformations("Backup location : ", fileSystemPolicy.getArchiveDirectory()); 
			LogHelper.logFileInformations("Final archive : ", context.getCurrentArchiveFile()); 

			// Create trace, content, hash, ...
			File traceFile = new File(getDataDirectory(context.getCurrentArchiveFile()), getTraceFileName());
			context.setTraceAdapter(new ArchiveTraceAdapter(traceFile, target.getSourceDirectory(), target.isTrackSymlinks()));

			File contentFile = new File(getDataDirectory(context.getCurrentArchiveFile()), getContentFileName());
			context.setContentAdapter(new ArchiveContentAdapter(contentFile, target.getSourceDirectory()));      

			File hashFile = new File(getDataDirectory(context.getCurrentArchiveFile()), getHashFileName());
			context.setHashAdapter(new ArchiveContentAdapter(hashFile, target.getSourceDirectory()));  
			//CHelper.handle(context);

			// Read transaction point 
			if (transactionPoint != null) {
				context.getTraceAdapter().bulkInit(transactionPoint.getTraceFile());
				context.getHashAdapter().bulkInit(transactionPoint.getHashFile());
				context.getContentAdapter().bulkInit(transactionPoint.getContentFile());
			}

			// Call handler-specific initializations
			handler.init(context, transactionPoint);

			// Call medium-specific initializations
			this.prepareContext(context, transactionPoint);

			// Enable thread monitor if requested
			if (TH_MON_ENABLED) {
				ThreadMonitorItem item = new ThreadMonitorItem(this.getTarget().getUid(), TH_MON_DELAY);
				ThreadMonitor.getInstance().register(item);
			}
		} catch (Exception e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		}
		
		//Chronometer.instance().stop("open");
	}

	/**
	 * Recovers the stored data
	 */
	public void recover(
			Object destination, 
			ArecaRawFileList filter, 
			AbstractCopyPolicy policy,
			GregorianCalendar date, 
			boolean recoverDeletedEntries,
			boolean checkRecoveredFiles,
			ProcessContext context) 
	throws ApplicationException, TaskCancelledException {  
		this.ensureInstalled();
		
		try {
			File traceFile;
			if (recoverDeletedEntries) {
				File[] archives = this.listArchives(null, date, true);
				traceFile = TraceMerger.buildAggregatedTraceFile(this, archives);
			} else {
				traceFile = ArchiveTraceManager.resolveTraceFileForArchive(this, getLastArchive(null, date));
			}


			// Recover the data
			recover(
					(File)destination, 
					null,
					filter, 
					policy,
					1, 
					new DateArchiveScope(null, date),
					traceFile, 
					ArchiveMedium.RECOVER_MODE_RECOVER, 
					recoverDeletedEntries, 
					checkRecoveredFiles, 
					false,
					context);

			// Create missing directories and symbolic links
			this.target.secureUpdateCurrentTask("Creating missing directories and symbolic links ...", context);
			try {
				RebuildOtherFilesTraceHandler handler = new RebuildOtherFilesTraceHandler((File)destination, filter, policy);
				ArchiveTraceAdapter.traverseTraceFile(handler, traceFile, context);
			} catch (IOException e) {
				throw new ApplicationException(e);
			} catch (FileMetaDataSerializationException e) {
				throw new ApplicationException(e);
			}

			// Apply metadata
			applyMetaData((File)destination, traceFile, policy.listExcludedFiles(), context);
		} catch (IOException e) {
			throw new ApplicationException(e);
		} catch (FileMetaDataSerializationException e) {
			throw new ApplicationException(e);
		}
	}

	public void rollbackBackup(ProcessContext context) throws ApplicationException {
		if (TH_MON_ENABLED) {
			ThreadMonitor.getInstance().remove(this.getTarget().getUid());
		}

		if (this.getTarget().checkResumeSupported() == null) {
			this.target.secureUpdateCurrentTask("Aborting backup ...", context);
		} else {
			this.target.secureUpdateCurrentTask("Rollbacking backup ...", context);
		}

		try {
			// Close the current transaction point
			closeCurrentTransactionPoint(false, false, context);
		} finally {
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
								if (
										(target.checkResumeSupported() != null)
										&& (context.getCurrentArchiveFile() != null)
								) {
									AbstractFileSystemMedium.tool.delete(context.getCurrentArchiveFile());
									AbstractFileSystemMedium.tool.delete(getDataDirectory(context.getCurrentArchiveFile()));
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
										if (this.getTarget().checkResumeSupported() == null) {
											this.target.secureUpdateCurrentTask("Abort completed.", context);
										} else {
											this.target.secureUpdateCurrentTask("Rollback completed.", context);
										}
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
	}

	public void rollbackMerge(ProcessContext context) throws ApplicationException {
		if (! this.image) {
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
						if (context.getCurrentArchiveFile() != null && ! isCommitted(context.getCurrentArchiveFile())) {
							AbstractFileSystemMedium.tool.delete(context.getCurrentArchiveFile());
							AbstractFileSystemMedium.tool.delete(getDataDirectory(context.getCurrentArchiveFile()));
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

	public TargetSearchResult search(SearchCriteria criteria) throws ApplicationException, TaskCancelledException {
		this.ensureInstalled();
		
		TargetSearchResult result = new TargetSearchResult();
		DefaultSearchCriteria dCriteria = (DefaultSearchCriteria)criteria;

		if (dCriteria.isRestrictLatestArchive()) {
			File lastArchive = this.getLastArchive();
			if (lastArchive != null) {
				this.searchWithinArchive(criteria, lastArchive, result);
			}
		} else {
			File[] archives = this.listArchives(null, null, true);
			for (int i=0; i<archives.length; i++) {
				this.searchWithinArchive(criteria, archives[i], result);
			}
		}

		return result;
	}

	public void simulateEntryProcessing(RecoveryEntry entry, boolean haltOnFirstDifference, ProcessContext context)
	throws ApplicationException, TaskCancelledException {
		try {
			FileSystemRecoveryEntry fEntry = (FileSystemRecoveryEntry)entry;

			// Context initialization
			if (! context.isInitialized()) {
				File archive = this.getLastArchive();
				if (archive != null) {
					context.setReferenceTrace(ArchiveTraceAdapter.buildIterator(ArchiveTraceManager.resolveTraceFileForArchive(this, archive)));
				}
				context.setInitialized();
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
							if (((FileSystemTarget)this.target).isTrackSymlinks() && FileMetaDataAccessor.TYPE_LINK == type) {
								link = true;
								fEntry.setSize(0);
							}
							compareEntries(iter.current(), fEntry, link, context);
						}
					}

					// Fetch next entry
					iter.next();
					break;
				} else if (result < 0) {
					// File found in source files but not found in trace -> new File
					fEntry.setStatus(EntryStatus.STATUS_CREATED);

					if (((FileSystemTarget)this.target).isTrackSymlinks() && FileMetaDataAccessor.TYPE_LINK == FileSystemManager.getType(fEntry.getFile())) {
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
		} catch (FileMetaDataSerializationException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		} catch (NoSuchAlgorithmException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		}
	} 
	
	private boolean compareEntries(TraceEntry traceEntry, FileSystemRecoveryEntry entry, boolean asLink, ProcessContext context) 
	throws IOException, TaskCancelledException, NoSuchAlgorithmException, FileMetaDataSerializationException {
		String oldHash = traceEntry == null ? null : ArchiveTraceParser.extractHashFromTrace(traceEntry.getData());
		String newHash = ArchiveTraceParser.hash(entry, asLink);
		
		if (newHash.equals(oldHash)) {
			
			if (inspectFileContent && ! asLink) {
				String newSha = Util.base64Encode(FileTool.getInstance().hashFileContent(entry.getFile(), context.getTaskMonitor()));
				String oldSha = ArchiveTraceParser.extractShaFromTrace(traceEntry.getData());
				
				if (newSha.equals(oldSha)) {
					entry.setStatus(EntryStatus.STATUS_NOT_STORED);
					return false;
				} else {
					entry.setStatus(EntryStatus.STATUS_MODIFIED);
					return true;
				}
			} else {
				entry.setStatus(EntryStatus.STATUS_NOT_STORED);
				return false;
			}
		} else {
			entry.setStatus(EntryStatus.STATUS_MODIFIED);
			return true;
		}
	}

	/**
	 * Stores an entry
	 */
	public void store(RecoveryEntry entry, final ProcessContext context) 
	throws StoreException, ApplicationException, TaskCancelledException {
		//Chronometer.instance().start("store");
		
		if (TH_MON_ENABLED) {
			ThreadMonitor.getInstance().notify(this.getTarget().getUid());
		}

		if (entry != null) {
			final FileSystemRecoveryEntry fEntry = (FileSystemRecoveryEntry)entry;
			String shaBase64 = null;
			try {
				short type = FileSystemManager.getType(fEntry.getFile());
				if (
						FileSystemManager.isFile(fEntry.getFile()) && (
								(FileMetaDataAccessor.TYPE_LINK != type)
								|| (! ((FileSystemTarget)this.target).isTrackSymlinks())
						) && (FileMetaDataAccessor.TYPE_PIPE != type)
				) {
					// The entry is stored if it has been modified	
					if (this.checkModified(fEntry, context)) {
						if (DEBUG_MODE) {
							Logger.defaultLogger().fine("[" + FileSystemManager.getDisplayPath(fEntry.getFile()) + "] : Backup in progress ...");
						}

						// Add a listener to the inputStream
						final HashInputStreamListener listener = new HashInputStreamListener();

						this.doAndRetry(new IOTask() {
							public void run() throws IOException, TaskCancelledException, ApplicationException {
								InputStream in = FileSystemManager.getFileInputStream(fEntry.getFile());
								in = new EventInputStream(in, listener);
								listener.reset();

					    		//Chronometer.instance().start("storeImpl");
								storeFileInArchive(fEntry, in, context);
					    		//Chronometer.instance().stop("storeImpl");
							}
						}, "An error occurred while storing " + fEntry.getKey());

						context.addInputBytes(FileSystemManager.length(fEntry.getFile()));
						context.getContentAdapter().writeContentEntry(fEntry);
						
						shaBase64 = Util.base64Encode(listener.getHash());
						
						context.getHashAdapter().writeHashEntry(fEntry, shaBase64);
						context.getReport().addSavedFile();
					} else {
						if (DEBUG_MODE) {
							Logger.defaultLogger().fine("[" + FileSystemManager.getDisplayPath(fEntry.getFile()) + "] : Unchanged.");
						}
						this.registerUnstoredFile(fEntry, context);
						context.getReport().addIgnoredFile();
						
						if (inspectFileContent) {
							shaBase64 = ArchiveTraceParser.extractShaFromTrace(context.getReferenceTrace().current().getData());
						}
					}
				}

				// Register the entry
				context.getTraceAdapter().writeEntry(fEntry, inspectFileContent ? shaBase64 : null);
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
		//Chronometer.instance().stop("store");
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
		if (image && backupScheme.equals(AbstractTarget.BACKUP_SCHEME_DIFFERENTIAL)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Build the archive
	 */
	protected void buildArchive(ProcessContext context) throws IOException, ApplicationException {
	}    

	protected abstract void buildMergedArchiveFromDirectory(ProcessContext context) throws ApplicationException;

	/**
	 * Check that we do not use a reserved name as archive name
	 */
	protected void checkFileSystemPolicy() {
		String nameToCheck = this.fileSystemPolicy.getArchiveName();
		for (int i=0; i<RESERVED_NAMES.length; i++) {
			if (nameToCheck.startsWith(RESERVED_NAMES[i])) {
				throw new IllegalArgumentException("Invalid archive name (" + nameToCheck + "). This name is reserved by " + VersionInfos.APP_SHORT_NAME + ". Please choose a different one.");
			}
		}
	} 

	protected abstract void closeArchive(ProcessContext context) throws IOException, ApplicationException;

	/**
	 * Build an archive name
	 */
	protected String computeArchiveName(GregorianCalendar date) {
		GregorianCalendar cal;

		if (image) {
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
		if (! image) {
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

	protected void computeMergedArchiveFile(ProcessContext context) throws ApplicationException {
		File[] recoveredFiles = context.getReport().getRecoveryResult().getRecoveredArchivesAsArray();
		GregorianCalendar lastArchiveDate = ArchiveManifestCache.getInstance().getManifest(this, recoveredFiles[recoveredFiles.length - 1]).getDate();
		context.setCurrentArchiveFile(new File(computeArchivePath(lastArchiveDate)));
	}

	protected void convertArchiveToFinal(ProcessContext context) throws IOException, ApplicationException {
		markCommitted(context.getCurrentArchiveFile());
	}

	protected void copyAttributes(Object clone) {
		super.copyAttributes(clone);

		AbstractIncrementalFileSystemMedium other = (AbstractIncrementalFileSystemMedium)clone;
		other.handler = (ArchiveHandler)this.handler.duplicate();
		other.handler.setMedium(other);
		
		other.inspectFileContent = this.inspectFileContent;
	}

	/**
	 * Deletes the archive - WHETHER IT IS COMMITTED OR NOT
	 */
	public void deleteArchive(File archive) throws IOException {
		AbstractFileSystemMedium.tool.delete(archive);
		AbstractFileSystemMedium.tool.delete(getDataDirectory(archive));
		handler.archiveDeleted(archive);
		ArchiveManifestCache.getInstance().removeManifest(this, archive);
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
				boolean found = trcIter.fetch(entry);
				if (found) {
					ead.setHash(trcIter.current().getData());
					ead.setMetadataVersion(trcIter.getHeader().getVersion());

					// Build a content iterator
					ctnIter = ArchiveContentManager.buildIteratorForArchive(this, archive);
					found = ctnIter.fetch(entry);
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
		// We can simply assume that if the filename starts with "CHECK_DESTINATION",
		// it really is a temporary check destination.
		// This is ensured by the checkFileSystemPolicy() method
		return name.startsWith(ArecaFileConstants.CHECK_DESTINATION); 
	}

	protected boolean matchArchiveName(File f) {
		String name = FileSystemManager.getName(f);

		if (image) {
			String parsed = computeArchiveName(null);
			return parsed.equals(name);
		} else {
			return ArchiveNameHelper.matchPattern(name, fileSystemPolicy.getArchiveName(), getArchiveExtension());
		}
	}

	protected void prepareContext(ProcessContext context, TransactionPoint transactionPoint) throws IOException {
	}

	/**
	 * Recovers the files at the requested recovery location.
	 * <BR>'filters' may be null ...
	 * <BR>
	 * <BR>If it has enough memory, Areca optimizes the recovery process to only recover the needed files (instead of
	 * blindly recovering the whole archives)
	 */
	private void recoverImpl(
			File targetFile,                         	// Where to recover
			File workingDirectory,						// if no target file is set, working directory that will be used as temporary recovery location
			ArecaRawFileList argFilter,                 // Filters the recovered entries
			AbstractCopyPolicy policy,
			File[] optimizedArchives,
			File traceFile,                       	    // Optional trace to apply to the recovered data
			short mode,                                 // Recovery mode : see ArchiveMedium.RECOVER_MODE_MERGE / RECOVER_MODE_RECOVER
			boolean recoverDeletedEntries,				// Also recover deleted entries
			boolean checkRecoveredFiles,				// Whether Areca must check if the recovered files' hash is the same as the reference hash
			boolean simulateRecovery,					// If the "checkRecoveredFiles" flag has been enabled, this flag controls whether a full recovery will be performed or not
			ProcessContext context                		// Execution context
	) throws ApplicationException, TaskCancelledException {
		FileSystemDriver initialDriver = null;
		boolean resetDriver = false;

		// Set final filter
		ArecaRawFileList filters = (argFilter == null || argFilter.isEmpty()) ? new ArecaRawFileList("/") : argFilter; // OP : changed back to "/", due to recovery problems when using non optimized recovery mode (previous value : "")

		logRecoveryParameters(recoverDeletedEntries, filters, optimizedArchives);
		try {
			// First stage : recover data
			context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(checkRecoveredFiles ? 0.7 : 0.9, "recover");

			// If no destination was set, compute it from the last archive's date.
			if (targetFile == null) {
				if (workingDirectory == null) {
					GregorianCalendar lastArchiveDate = ArchiveManifestCache.getInstance().getManifest(this, optimizedArchives[optimizedArchives.length - 1]).getDate();
					targetFile = new File(computeArchivePath(lastArchiveDate));
				} else {
					targetFile = FileTool.getInstance().generateNewWorkingFile(
							workingDirectory, 
							null, 
							ArecaFileConstants.TMP_MERGE_LOCATION, 
							true
					);
				}
			}
			context.setRecoveryDestination(targetFile);
			context.setTraceFile(traceFile);
			FileTool.getInstance().createDir(targetFile);
			Logger.defaultLogger().info("Files will be recovered in " + FileSystemManager.getDisplayPath(targetFile));

			if (mode == ArchiveMedium.RECOVER_MODE_MERGE) {
				computeMergedArchiveFile(context);
			}

			// Build a map of entries to recover
			RecoveryFilterMap entriesByArchive = buildEntriesMap(optimizedArchives, filters, traceFile, context);
			context.setFilesByArchive(entriesByArchive);

			try {
				// Set specific driver if 'simulate recovery' has been requested
				// (which will compute files' content hashcode on-the-fly)
				if (checkRecoveredFiles && simulateRecovery) {
					resetDriver = true;
					initialDriver = FileSystemManager.getInstance().getDriverAtMountPoint(targetFile);
					if (initialDriver instanceof ContentHashFileSystemDriver) {
						Logger.defaultLogger().warn("Driver registered at " + targetFile + " is already an instance of 'ContentHashFileSystemDriver'.");
					} else {
						FileSystemDriver driver = FileSystemManager.getInstance().getDriver(targetFile);
						FileSystemManager.getInstance().registerDriver(targetFile, new ContentHashFileSystemDriver(driver));
						handler.initializeSimulationDriverData(driver, context);
					}
				}

				// Recover the data
				handler.recoverRawData(optimizedArchives, entriesByArchive, policy, traceFile, mode, context);
				context.getTaskMonitor().checkTaskState();

				// Third stage: check hash
				if (checkRecoveredFiles) {
					checkRecoveredFiles(targetFile, optimizedArchives, filters, policy == null ? null : policy.listExcludedFiles(), simulateRecovery, context);
				} 
			} finally {
				if (resetDriver) {
					if (initialDriver == null) {
						FileSystemManager.getInstance().unregisterDriver(targetFile);
					} else {
						FileSystemManager.getInstance().registerDriver(targetFile, initialDriver);
					}
				}
			}
		} catch (TaskCancelledException e) {
			throw e;
		} catch (Throwable e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		} finally {
			context.getTaskMonitor().getCurrentActiveSubTask().setCurrentCompletion(1);  
		}
	}
	
	public void checkArchivesEncoding(File[] archives) {
		try {
			String currentEncoding = OSTool.getIANAFileEncoding();
			for (int i=0; i<archives.length; i++) {
				Manifest mf = ArchiveManifestCache.getInstance().getManifest(this, archives[i]);
				if (mf != null) {
					String archiveEncoding = mf.getStringProperty(ManifestKeys.ENCODING);
					if (archiveEncoding != null && ! archiveEncoding.equalsIgnoreCase(currentEncoding)) {
						Logger.defaultLogger().warn("The filename encoding used for " + FileSystemManager.getAbsolutePath(archives[i]) + "(" + FileSystemManager.getDisplayPath(archives[i]) + ") is " + archiveEncoding + ", which is different from your current encoding (" + currentEncoding + ").\nThis can result in recovery or merge issues if some of your filenames contain non-ASCII characters.\nIf you are running Linux, this can be an issue related to the value of the LANG, LANGUAGE or LC_CTYPE variables in your environment\n(Important : these variables may have to be set explicitely for crontab)");
					}
				}
			}
		} catch (Exception e) {
			// shall throw no exception - informational only - we do not want this check to disturb the behaviour
			Logger.defaultLogger().error("Error caught while checking archive encoding.", e);
		}
	}

	/**
	 * Recovers the files at the requested recovery location, according to the recovery dates passed as argument.
	 * <BR>'filters' may be null ...
	 * <BR>The recovery is actually done if there are at least <code>minimumArchiveNumber</code> archives to recover
	 */
	protected void recover(
			File targetFile,                         	// Where to recover
			File workingDirectory,						// if no target file is set, working directory that will be used as temporary recovery location
			ArecaRawFileList argFilter,                         // Filters the recovered entries
			AbstractCopyPolicy policy,
			int minimumArchiveNumber,           		// The recovery is done only if there are at least this number of archives to recover
			ArchiveScope perimeter,
			File traceFile,                       	    // Optional trace to apply to the recovered data
			short mode,                                 // Recovery mode : see ArchiveMedium.RECOVER_MODE_MERGE / RECOVER_MODE_RECOVER
			boolean recoverDeletedEntries,				// Also recover deleted entries
			boolean checkRecoveredFiles,				// Whether areca must check if the recovered files' hash is the same as the reference hash
			boolean simulateRecovery,					// If the "checkRecoveredFiles" flag has been enabled, this flag controls whether a full recovery will be performed or not
			ProcessContext context                		// Execution context
	) throws ApplicationException, TaskCancelledException {
		this.ensureInstalled();
		
		RecoveryResult result = new RecoveryResult();
		context.getReport().setRecoveryResult(result);

		Logger.defaultLogger().info("Recovering " + perimeter.displayScope() + ".");

		// List archives to be recovered
		buildArchiveListToRecover(result, perimeter, recoverDeletedEntries);
		File[] optimizedArchives = result.getRecoveredArchivesAsArray();

		if (optimizedArchives.length >= minimumArchiveNumber) {
			
			// Check archives (encoding)
			checkArchivesEncoding(optimizedArchives);
			
			// Recover
			recoverImpl(
					targetFile, 
					workingDirectory, 
					argFilter, 
					policy,
					optimizedArchives, 
					traceFile, 
					mode, 
					recoverDeletedEntries, 
					checkRecoveredFiles, 
					simulateRecovery, 
					context
			);
		}
	}

	private void logRecoveryParameters(boolean recoverDeletedEntries, ArecaRawFileList filters, File[] optimizedArchives) {
		if (recoverDeletedEntries) {
			Logger.defaultLogger().info("Deleted entries will be recovered.");
		} else {
			Logger.defaultLogger().info("Deleted entries won't be recovered.");
		}
		String strflt = "Recovery filter : ";
		if (filters != null) {
			for (int i=0; i<filters.length(); i++) {
				if (i!= 0) {
					strflt += ", ";
				}
				strflt += filters.get(i);
			}
		} else {
			strflt += "<null>";
		}
		Logger.defaultLogger().info(strflt);
		Logger.defaultLogger().info("" + optimizedArchives.length + " archives will be processed.");
	}

	private RecoveryFilterMap buildEntriesMap(File[] optimizedArchives, ArecaRawFileList filters, File traceFile, ProcessContext context) 
	throws IOException, FileMetaDataSerializationException, TaskCancelledException, ApplicationException {
		Logger.defaultLogger().fine("Building entries map for " + optimizedArchives.length + " archives.");
		
		// Create a normalized copy of the file list
		filters.sort();
		filters.deduplicate();
		ArecaRawFileList normalized = (ArecaRawFileList)filters.duplicate();
		normalized.removeTrailingSlashes();

		// Dispatch entries
		EntriesDispatcher dispatcher = this.handler.buildEntriesDispatcher(optimizedArchives);

		try {
			if (! filters.hasDirs()) {
				// Only entries of "file" type -> explicitly dispatch entries
				for (int e=0; e<normalized.length(); e++) {
					dispatcher.dispatchEntry(normalized.get(e));
				}
			} else {
				// Both files and directories -> traverse trace file
				try {
					EntrySetTraceHandler handler = new EntrySetTraceHandler(normalized, dispatcher);
					ArchiveTraceAdapter.traverseTraceFile(handler, traceFile, context);
				} catch (IllegalStateException e) {
					Logger.defaultLogger().fine("Error reading " + traceFile);
					return null;
				}
			}
		} finally {
			dispatcher.close();
		}

		RecoveryFilterMap entriesByArchive = dispatcher.getResult();
		Logger.defaultLogger().info("" + dispatcher.getEntriesCount() + " files will be recovered.");
		return entriesByArchive;
	}

	private void checkRecoveredFiles(
			File targetFile, 
			File[] archives, 
			ArecaRawFileList filters, 
			FileList filteredFiles,
			boolean simulateRecovery, 
			ProcessContext context
	) throws IOException, ApplicationException, TaskCancelledException {
		Logger.defaultLogger().info("Checking recovered files ...");
		AbstractMetaDataFileIterator refIter;
		if (archives.length == 1) {
			// Build reference content iterator
			Logger.defaultLogger().info("Using content of archive " + archives[0].getAbsolutePath() + " as reference.");
			refIter = ArchiveContentManager.buildIteratorForArchive(this, archives[0]);
		} else {
			// Build reference trace iterator
			Logger.defaultLogger().info("Using trace of archive " + archives[archives.length - 1].getAbsolutePath() + " as reference.");
			refIter = ArchiveTraceManager.buildIteratorForArchive(this, archives[archives.length - 1]);
		}

		checkHash(targetFile, archives, filters, filteredFiles, refIter, simulateRecovery, context);
	}

	protected abstract void registerUnstoredFile(FileSystemRecoveryEntry entry, ProcessContext context) throws IOException;

	/**
	 * Applies the metadata to the recovered files / symlinks / directories :
	 * <BR>- last modification date
	 * <BR>- permissions
	 * <BR>- ACL ... (if supported by the local metadata accessor)
	 */
	private void applyMetaData(File destination, File traceFile, FileList excludedFiles, ProcessContext context) 
	throws ApplicationException, TaskCancelledException {
		this.target.secureUpdateCurrentTask("Applying metadata ...", context);

		try {
			UpdateMetaDataTraceHandler handler = new UpdateMetaDataTraceHandler();
			handler.setDestination(destination);
			handler.setExcludedFiles(excludedFiles);
			
			ArchiveTraceAdapter.traverseTraceFile(handler, traceFile, context);
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
			ArchiveScope perimeter,
			boolean recoverDeletedEntries
	) throws ApplicationException {
		File[] listedArchives = perimeter.buildArchiveList(this);
		result.addProcessedArchives(listedArchives);

		if (recoverDeletedEntries) {
			Logger.defaultLogger().info("Keeping deleted entries - no archive optimization required.");
			result.addRecoveredArchives(listedArchives);
		} else {
			boolean ignoreIncrementalAndDifferentialArchives = false;
			boolean ignoreAllArchives = false;

			for (int i=listedArchives.length - 1; i>=0; i--) {
				Manifest mf = ArchiveManifestCache.getInstance().getManifest(this, listedArchives[i]);
				String prp = mf.getStringProperty(ManifestKeys.OPTION_BACKUP_SCHEME, AbstractTarget.BACKUP_SCHEME_INCREMENTAL);
				if (prp.equals(AbstractTarget.BACKUP_SCHEME_FULL) && ! ignoreAllArchives) {
					Logger.defaultLogger().info("Adding " + FileSystemManager.getDisplayPath(listedArchives[i]) + " (" + prp + ") to recovery list.");
					result.getRecoveredArchives().add(0, listedArchives[i]);
					ignoreAllArchives = true;
					Logger.defaultLogger().info("Previous archives will be ignored.");
				} else if (prp.equals(AbstractTarget.BACKUP_SCHEME_DIFFERENTIAL)  && ! ignoreIncrementalAndDifferentialArchives && ! ignoreAllArchives) {
					Logger.defaultLogger().info("Adding " + FileSystemManager.getDisplayPath(listedArchives[i]) + " (" + prp + ") to recovery list.");
					result.getRecoveredArchives().add(0, listedArchives[i]);
					ignoreIncrementalAndDifferentialArchives = true;
					Logger.defaultLogger().info("Previous incremental and differential archives will be ignored.");                            
				} else if (prp.equals(AbstractTarget.BACKUP_SCHEME_INCREMENTAL) && ! ignoreIncrementalAndDifferentialArchives && ! ignoreAllArchives) {
					Logger.defaultLogger().info("Adding " + FileSystemManager.getDisplayPath(listedArchives[i]) + " (" + prp + ") to recovery list.");
					result.getRecoveredArchives().add(0, listedArchives[i]);
				} else {
					Logger.defaultLogger().info("Adding " + FileSystemManager.getDisplayPath(listedArchives[i]) + " (" + prp + ") to ignore list.");
					result.getIgnoredArchives().add(0, listedArchives[i]);
				}
			}
		}
	}

	/**
	 * Check that the file passed as argument matches the hashcode contained in the "entry" argument.
	 */
	private void checkHash(File file, ContentEntry entry, boolean simulatedRecovery, ProcessContext context) 
	throws IOException, TaskCancelledException, ApplicationException {
		// Check the file
		byte[] storedHash = ArchiveContentParser.interpretAsHash(entry.getKey(), entry.getData());
		if (storedHash == null) {
			context.getInfoChannel().warn(entry.getKey() + " : no reference hash could be found.");
			context.getReport().getUncheckedRecoveredFiles().add(entry.getKey());
		} else {
			this.doAndRetry(new CheckHash(context, storedHash, entry, file, simulatedRecovery), "Error while checking " + file.getAbsolutePath());
		}
	}

	public class CheckHash implements IOTask {
		private ProcessContext context;
		private byte[] storedHash;
		private ContentEntry entry;
		private boolean simulatedRecovery;
		private File file;

		public CheckHash(ProcessContext context, byte[] storedHash, ContentEntry entry, File file, boolean simulatedRecovery) {
			this.context = context;
			this.storedHash = storedHash;
			this.entry = entry;
			this.simulatedRecovery = simulatedRecovery;
			this.file = file;
		}

		public void run() throws IOException, TaskCancelledException, ApplicationException {
			byte[] computedHash;
			try {
				if (simulatedRecovery) {
					computedHash = FileTool.getInstance().getFileBytes(file);
				} else {
					computedHash = FileTool.getInstance().hashFileContent(file, context.getTaskMonitor());
				}
			} catch (NoSuchAlgorithmException e) {
				throw new ApplicationException(e);
			} catch (IOException e) {
				throw new ApplicationException(e);
			}
			String suffix = " Simulation flag set to " + (simulatedRecovery ? "ON" : "OFF") + " - file : " + file;
			
			if (computedHash.length != storedHash.length) {
				throw new IllegalStateException("Incoherent hash lengths (" + Util.base16Encode(computedHash) + " versus " + Util.base16Encode(storedHash) + ")." + suffix);
			} else {
				boolean ok = true;
				for (int i=0; i<computedHash.length; i++) {
					if (computedHash[i] != storedHash[i]) {
						context.getInfoChannel().warn(entry.getKey() + " was not properly recovered : its hash (" + Util.base16Encode(computedHash) + ") is different from the reference hash (" + Util.base16Encode(storedHash) + ")." + suffix);
						context.getReport().getInvalidRecoveredFiles().add(entry.getKey());
						String info = getRecoveryInformations(entry.getKey(), context);
						if (info != null) {
							Logger.defaultLogger().fine(info);
						}
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
			ArecaRawFileList filters,
			FileList filteredFiles,
			AbstractMetaDataFileIterator referenceIterator,
			boolean simulatedRecovery,
			ProcessContext context) 
	throws IOException, TaskCancelledException, ApplicationException {
		context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.2, "check");
		context.getInfoChannel().print("Recovery completed - Checking recovered files (this may take some time) ...");  

		if (CHECK_DEBUG_MODE) {
			if (filters == null) {
				Logger.defaultLogger().fine("No filter.");
			} else {
				for (int i=0; i<filters.length(); i++) {
					Logger.defaultLogger().fine("Filter[" + i + "] : " + filters.get(i));
				}
			}
		}

		ContentFileIterator[] iters = new ContentFileIterator[archives.length];
		FileListIterator filteredFilesIterator = filteredFiles == null ? null : filteredFiles.iterator();
		try {
			// Build hashIterators
			for (int i=0; i<archives.length; i++) {
				File hashFile = ArchiveContentManager.resolveHashFileForArchive(this, archives[i]);
				iters[i] = ArchiveContentAdapter.buildIterator(hashFile);
			}

			while (referenceIterator.hasNext()) {
				AbstractMetaDataEntry entry = referenceIterator.nextEntry();

				if (CHECK_DEBUG_MODE) {
					Logger.defaultLogger().fine("Entry : " + entry.getKey());
				}

				// The entry will only be checked if it passes the filters
				File target = new File(destination, entry.getKey());
				if (
						(filteredFilesIterator == null || (! filteredFilesIterator.fetch(FileSystemManager.getAbsolutePath(target)))) 
						&& (filters == null || Util.passFilter(entry.getKey(), filters.asArray()))
				) {
					if (entry.getType() == MetadataConstants.T_FILE) {				
						if (! FileSystemManager.exists(target)) {
							context.getInfoChannel().warn(entry.getKey() + " was not recovered ... it should have.");
							context.getReport().getUnrecoveredFiles().add(entry.getKey());
							String info = getRecoveryInformations(entry.getKey(), context);
							if (info != null) {
								Logger.defaultLogger().fine(info);
							}
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
								found = iters[i].fetch(entry.getKey());
								if (found) {
									if (CHECK_DEBUG_MODE) {
										Logger.defaultLogger().fine("Hash found ! Verifying ...");
									}
									// The entry has been found --> check the hash
									checkHash(target, iters[i].current(), simulatedRecovery, context);
									context.getReport().addChecked();
									break;
								}
							}
							if (! found) {
								context.getInfoChannel().warn("No reference hash could be found for " + entry.getKey());
								context.getReport().getUncheckedRecoveredFiles().add(entry.getKey());
								String info = getRecoveryInformations(entry.getKey(), context);
								if (info != null) {
									Logger.defaultLogger().fine(info);
								}
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
			context.getInfoChannel().print("Check completed - " + context.getReport().getNbChecked() + " files checked.");

			// Close iterators
			try {
				if (filteredFilesIterator != null) {
					filteredFilesIterator.close();
				}
			} finally {
				for (int i=0; i<archives.length; i++) {
					iters[i].close();
				}
			}
		}
	}

	private String getRecoveryInformations(String entry, ProcessContext context) {
		if (context.getFilesByArchive() != null && context.getDetailedRecoveryErrors() < MAX_DETAILED_ERRORS) {
			context.addDetailedRecoveryError();
			try {
				File[] archives = context.getFilesByArchive().lookupEntry(entry);
				String ret = entry + " was recovered from ";
				for (int i=0; i<archives.length; i++) {
					if (i != 0) {
						ret += ", ";
					}
					ret += archives[i] + " (cnt=";

					// Add content data
					ContentFileIterator ctnInter = ArchiveContentManager.buildIteratorForArchive(this, archives[i]);
					try {
						if (ctnInter.fetch(entry)) {
							ret += ctnInter.current().getData();
						} else {
							ret += "not found in content file";
						}
					} finally {
						ctnInter.close();
					}
					
					ret += " / trc=";
					
					// Add trace data
					File traceFile = ArchiveTraceManager.resolveTraceFileForArchive(this, archives[i]);
					TraceFileIterator trcIter = ArchiveTraceAdapter.buildIterator(traceFile);
					try {
						if (trcIter.fetch(entry)) {
							ret += trcIter.current().getData();
						} else {
							ret += "not found in trace file";
						}
					} finally {
						trcIter.close();
					}

					// Add hash data
					File hashFile = ArchiveContentManager.resolveHashFileForArchive(this, archives[i]);
					ContentFileIterator hashInter = ArchiveContentAdapter.buildIterator(hashFile);
					try {
						if (hashInter.fetch(entry)) {
							ret += " / hash64=";
							ret += hashInter.current().getData();
							ret += " / hash16=";
							try {
								ret += Util.base16Encode(Util.base64Decode(hashInter.current().getData()));
							} catch (Exception e) {
								Logger.defaultLogger().warn("Error decoding hashcode", e);
							}
						} else {
							ret += " / hash=";
							ret += "not found in hash file";
						}
					} finally {
						hashInter.close();
					}
					
					// Add manifest data
					Manifest manifest = ArchiveManifestCache.getInstance().getManifest(this, archives[i]);
					ret += 
						" / scheme=" + manifest.getStringProperty(ManifestKeys.OPTION_BACKUP_SCHEME, "unknown") + 
						" / version=" + manifest.getStringProperty(ManifestKeys.VERSION, "unknown") + 
						" / resumed=" + manifest.getStringProperty(ManifestKeys.IS_RESUMED, "false") + 
						" / date=" + Utils.formatDisplayDate(manifest.getDate()) + 
						" / checked=" + manifest.getStringProperty(ManifestKeys.CHECKED, "unknown") + 
						" / type=" + (manifest.getType() == Manifest.TYPE_BACKUP ? "Backup" : "Merge"); 
					
					ret += ")";
				}
				return ret;
			} catch (IOException e) {
				Logger.defaultLogger().error("Error while reading entry data.", e);
			} catch (ApplicationException e) {
				Logger.defaultLogger().error("Error while reading entry data.", e);
			}
		}
		return null;
	}

	/**
	 * Check whether the file has been modified since the previous backup or not
	 * @throws FileMetaDataSerializationException 
	 */
	private boolean checkModified(FileSystemRecoveryEntry entry, ProcessContext context) 
	throws IOException, TaskCancelledException, NoSuchAlgorithmException, FileMetaDataSerializationException {
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
				return compareEntries(iter.current(), entry, false, context);
			} else if (result < 0) {
				// File found in source files but not found in trace -> new File
				entry.setStatus(EntryStatus.STATUS_CREATED);
				return true;
			} else {
				// File found in trace but not among source files -> deleted file
				context.getReport().addDeletedFile();
				iter.next();
			}
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
					referenceIter = ArchiveTraceAdapter.buildIterator(trcFile);
				} else {
					// Case 2 : a date is provided : use archive content as of provided date (used by physical view)
					File ctnFile = ArchiveContentManager.resolveContentFileForArchive(this, referenceArchive);
					referenceIter = ArchiveContentAdapter.buildIterator(ctnFile);
				}

				// Merge the traces
				File[] archives;
				if (logicalView && aggregated) {
					archives = this.listArchives(null, null, true);
				} else {
					archives = new File[] {referenceArchive};
				}
				context.setData(TraceMerger.buildAggregatedTraceFile(this, archives, referenceIter));
			}

			File aggrFile = (File)context.getData();
			TraceFileIterator aggrIter = ArchiveTraceAdapter.buildIterator(aggrFile);
			try {
				// Populate the set
				boolean found = true;
				if (root.length() != 0) {
					found = aggrIter.fetch(root);
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
									addDirectoryData(entry, directories);
								}
							} else if (entry.getType() == MetadataConstants.T_FILE) {
								String subdirectory = entryKey.substring(0, idx);
								DirectoryData dt = (DirectoryData)directories.get(subdirectory);

								if (dt == null) {
									Logger.defaultLogger().error("No reference data found in archive trace for directory : " + subdirectory + " (entry key = [" + entryKey + "])");
									Logger.defaultLogger().fine("Directory reference data :\n" + directories.toString());
								} else {
									// Update directory data ('exist' flag and size)
									boolean exists = entry.getData().charAt(0) == '1';
									dt.exists = dt.exists || exists;
									if (exists || logicalView) {
										dt.size += Long.parseLong(entry.getData().substring(1));
									}
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
		} catch (FileMetaDataSerializationException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		}
	}

	private DirectoryData addDirectoryData(TraceEntry entry, Map directories) {
		DirectoryData dt = new DirectoryData();
		dt.entry = entry;
		dt.size = 0;
		dt.exists = false;
		directories.put(entry.getKey(), dt);
		return dt;
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

	private void searchWithinArchive(SearchCriteria criteria, File archive, TargetSearchResult result) throws ApplicationException, TaskCancelledException {
		Logger.defaultLogger().info("Searching in " + FileSystemManager.getDisplayPath(archive) + " ...");
		TraceFileIterator iter = null;
		criteria.getMonitor().checkTaskState();

		try {
			try {
				File traceFile = ArchiveTraceManager.resolveTraceFileForArchive(this, archive);
				iter = ArchiveTraceAdapter.buildIterator(traceFile);

				SearchMatcher matcher = new SearchMatcher((DefaultSearchCriteria)criteria);

				Manifest mf = ArchiveManifestCache.getInstance().getManifest(this, archive);
				while (iter.hasNext()) {
					criteria.getMonitor().checkTaskState();

					TraceEntry trcEntry = iter.next();
					if (trcEntry.getType() != MetadataConstants.T_DIR && matcher.matches(trcEntry.getKey())) {
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
	 * <BR>The copy is stored in the "transaction" subdirectory so it can be reused if resuming a pending backup.
	 */
	protected File duplicateMetadataFile(File source, ProcessContext context) {
		File target = null;
		if (FileSystemManager.exists(source)) {
			try {
				File transactionDirectory = new File(getDataDirectory(context.getCurrentArchiveFile()), ArecaFileConstants.TRANSACTION_FILE);

				// Copy file in a temporary place - no shutdown hook is registered -> the file will have to be deleted explicitly
				target = FileTool.getInstance().generateNewWorkingFile(transactionDirectory, ArecaFileConstants.TEMPORARY_DIR_NAME, FileSystemManager.getName(source), false);
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
			// Todo : clean this.
			// Merged content files should handle their "root" header properly instead of using the target's default root
			FileSystemTarget target = (FileSystemTarget)this.getTarget();

			// Build the writers
			contentWriter = new ArchiveContentAdapter(contentTarget, target.getSourceDirectory());
			hashWriter = new ArchiveContentAdapter(hashTarget, target.getSourceDirectory());
			if (handlerTarget != null) {
				handlerWriter = new ArchiveContentAdapter(handlerTarget, target.getSourceDirectory());
			}

			// Build trace Iterator
			traceIter = ArchiveTraceAdapter.buildIterator(traceFile);

			// Build content/hash/handler files iterator
			contentIters = new ContentFileIterator[recoveredFiles.length];
			hashIters = new ContentFileIterator[recoveredFiles.length];
			if (handlerTarget != null) {
				handlerIters = new ContentFileIterator[recoveredFiles.length];
			}
			for (int i=0; i<recoveredFiles.length; i++) {
				File contentFile = ArchiveContentManager.resolveContentFileForArchive(this, recoveredFiles[i]);
				contentIters[i] = ArchiveContentAdapter.buildIterator(contentFile);

				File hashFile = ArchiveContentManager.resolveHashFileForArchive(this, recoveredFiles[i]);
				hashIters[i] = ArchiveContentAdapter.buildIterator(hashFile);

				if (handlerTarget != null) {
					File handlerFile = ArchiveContentManager.resolveSequenceFileForArchive(this, recoveredFiles[i]);
					handlerIters[i] = ArchiveContentAdapter.buildIterator(handlerFile);
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
				throw new ApplicationException("Error writing merged content on " + FileSystemManager.getDisplayPath(hashTarget), e);
			}
		}
	}
}