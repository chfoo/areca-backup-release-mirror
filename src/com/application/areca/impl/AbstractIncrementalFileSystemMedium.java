package com.application.areca.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import com.application.areca.LogHelper;
import com.application.areca.MemoryHelper;
import com.application.areca.RecoveryEntry;
import com.application.areca.StoreException;
import com.application.areca.TargetActions;
import com.application.areca.Utils;
import com.application.areca.cache.ArchiveManifestCache;
import com.application.areca.cache.ArchiveTraceCache;
import com.application.areca.context.ProcessContext;
import com.application.areca.context.RecoveryResult;
import com.application.areca.impl.handler.ArchiveHandler;
import com.application.areca.impl.tools.ArchiveComparator;
import com.application.areca.impl.tools.ArchiveNameFilter;
import com.application.areca.metadata.content.ArchiveContent;
import com.application.areca.metadata.content.ArchiveContentAdapter;
import com.application.areca.metadata.content.ArchiveContentManager;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.manifest.ManifestKeys;
import com.application.areca.metadata.manifest.ManifestManager;
import com.application.areca.metadata.trace.ArchiveTrace;
import com.application.areca.metadata.trace.ArchiveTraceAdapter;
import com.application.areca.search.DefaultSearchCriteria;
import com.application.areca.search.SearchCriteria;
import com.application.areca.search.SearchMatcher;
import com.application.areca.search.SearchResultItem;
import com.application.areca.search.TargetSearchResult;
import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemIterator;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.attributes.Attributes;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.util.CalendarUtils;
import com.myJava.util.Util;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;

/**
 * Medium that implements incremental storage
 * 
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 7289397627058093710
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
public abstract class AbstractIncrementalFileSystemMedium 
extends AbstractFileSystemMedium 
implements TargetActions {
	
	protected static final boolean DEBUG_MODE = ((ArecaTechnicalConfiguration)ArecaTechnicalConfiguration.getInstance()).isBackupDebug();

	/**
	 * Nom du fichier de trace.
	 */
	protected static final String TRACE_FILE_OLD = "recover.trc.";
	protected static final String TRACE_FILE = "trace";

	/**
	 * Content filename
	 */
	protected static final String CONTENT_FILE_OLD = "recover.ctn.";
	protected static final String CONTENT_FILE = "content";

	/**
	 * Metadata filename
	 */
	protected static final String METADATA_FILE = "metadata";

	/**
	 * Suffixe ajoute pour construire l'emplacement temporaire de restauration
	 * utilise pour la fusion des archives.
	 */
	protected static final String TMP_MERGE_LOCATION = "merge";

	/**
	 * Tells wether directories shall be tracked or not
	 * <BR>(Allows to recover empty directories)
	 */
	protected boolean trackDirectories = false;

	/**
	 * Tells wether file permissions shall be tracked or not
	 */
	protected boolean trackPermissions = false;

	/**
	 * Tells wether many archives shall be created on just one single archive
	 */
	protected boolean overwrite = false;

	/**
	 * Handler for archive processing
	 */
	protected ArchiveHandler handler;

	public void install() throws ApplicationException {
		super.install();		
		if (compressionArguments.isMultiVolumes() && ! compressionArguments.isAddExtension()) {
			throw new ApplicationException("The \".zip\" extension is mandatory if zip-splitting is enabled.");
		}
	}

	protected void copyAttributes(Object clone) {
		super.copyAttributes(clone);

		AbstractIncrementalFileSystemMedium other = (AbstractIncrementalFileSystemMedium)clone;
		other.overwrite = this.overwrite;
		other.trackDirectories = this.trackDirectories;
		other.trackPermissions = this.trackPermissions;
		other.handler = (ArchiveHandler)this.handler.duplicate();
		other.handler.setMedium(other);
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	public boolean isOverwrite() {
		return this.overwrite;
	}

	public ArchiveHandler getHandler() {
		return handler;
	}

	public void setHandler(ArchiveHandler handler) {
		this.handler = handler;
		handler.setMedium(this);
	}

	/**
	 * Retourne le nom du fichier de trace (sans repertoire) final (apres commit)
	 */
	public String getTraceFileName(boolean oldFormat) {
		if (oldFormat) {
			return TRACE_FILE_OLD + this.target.getUid();
		} else {
			return TRACE_FILE;            
		}
	}

	/**
	 * Retourne le nom du fichier de contenu (sans repertoire) final (apres commit)
	 */
	public String getContentFileName(boolean oldFormat) {
		if (oldFormat) {
			return CONTENT_FILE_OLD + this.target.getUid();
		} else{
			return CONTENT_FILE;
		}
	}

	/**
	 * Retourne le nom du fichier de metadata (sans repertoire) final (apres commit)
	 */
	public String getMetaDataFileName() {
		return METADATA_FILE;
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
				context.setPreviousTrace(new ArchiveTrace());
			} else {
				File lastArchive;
				if (backupScheme.equals(AbstractRecoveryTarget.BACKUP_SCHEME_DIFFERENTIAL)) {
					lastArchive = this.getLastArchive(AbstractRecoveryTarget.BACKUP_SCHEME_FULL, null);
				} else {
					lastArchive = this.getLastArchive();
				}

				if (lastArchive != null && FileSystemManager.exists(lastArchive)) {
					Logger.defaultLogger().info("Using the following archive as reference : " + FileSystemManager.getAbsolutePath(lastArchive) + ".");
					context.setPreviousTrace(ArchiveTraceCache.getInstance().getTrace(this, lastArchive));
				} else {
					// Build an empty trace
					Logger.defaultLogger().info("Using an empty archive as reference.");
					context.setPreviousTrace(new ArchiveTrace());
				} 
			}

			context.setManifest(manifest);
			manifest.addProperty(
					ManifestKeys.OPTION_BACKUP_SCHEME, 
					context.getPreviousTrace().isEmpty() ? AbstractRecoveryTarget.BACKUP_SCHEME_FULL : backupScheme
			);

			// Archive creation
			this.buildArchive(context);      

			// TraceWriter creation
			File traceFile = new File(getDataDirectory(context.getCurrentArchiveFile()), getTraceFileName(false));
			File contentFile = new File(getDataDirectory(context.getCurrentArchiveFile()), getContentFileName(false));

			context.setTraceAdapter(new ArchiveTraceAdapter(this, traceFile, this.trackDirectories, ((FileSystemRecoveryTarget)this.target).isTrackSymlinks()));
			context.getTraceAdapter().setTrackPermissions(this.trackPermissions);
			context.setContentAdapter(new ArchiveContentAdapter(contentFile));            

		} catch (Exception e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		}
	}

	protected void recoverSymLink(FileSystemRecoveryEntry entry, File archive, File destination) throws ApplicationException {
		try {
			String fileName = entry.getName();
			File tmp = new File(fileName);
			File targetFile = new File(destination, FileSystemManager.getName(tmp));
			if (! FileSystemManager.exists(FileSystemManager.getParentFile(targetFile))) {
				tool.createDir(FileSystemManager.getParentFile(targetFile));
			}

			String hash = ArchiveTraceCache.getInstance().getTrace(this, archive).getSymLinkHash(entry);
			String path = ArchiveTrace.extractSymLinkPathFromTrace(hash);

			FileSystemManager.createSymbolicLink(targetFile, path);
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		}
	}

	/**
	 * Build the archive
	 */
	protected void buildArchive(ProcessContext context) throws IOException, ApplicationException { 
		context.setCurrentArchiveFile(new File(computeFinalArchivePath()));  
		LogHelper.logFileInformations("Final archive : ", context.getCurrentArchiveFile());              
	}
	
	protected boolean matchArchiveName(File f) {
		String name = FileSystemManager.getName(f);
		
		if (overwrite) {
			String parsed = computeArchiveName(null);
			return parsed.equals(name);
		} else {
			return ArchiveNameHelper.matchPattern(name, fileSystemPolicy.getArchiveName(), getArchiveExtension());
		}
	}

	/**
	 * Build an archive name
	 */
	protected String computeArchiveName(GregorianCalendar date) {
		GregorianCalendar cal;
		
		if (overwrite) {
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
		if (! overwrite) {
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

	/**
	 * Retourne le chemin de l'archive
	 * @return
	 */
	protected String computeFinalArchivePath() {
		return computeArchivePath(new GregorianCalendar());
	}    

	public boolean isTrackDirectories() {
		return trackDirectories;
	}

	public void setTrackDirectories(boolean trackDirectories) {
		this.trackDirectories = trackDirectories;
	}

	public boolean isTrackPermissions() {
		return trackPermissions;
	}

	public void setTrackPermissions(boolean trackPermissions) {
		this.trackPermissions = trackPermissions;
	}

	public boolean supportsBackupScheme(String backupScheme) {
		if (overwrite && backupScheme.equals(AbstractRecoveryTarget.BACKUP_SCHEME_DIFFERENTIAL)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Stores an entry
	 */
	public void store(RecoveryEntry entry, ProcessContext context) throws StoreException, ApplicationException {
		if (entry == null) {
			return;
		} else {
			FileSystemRecoveryEntry fEntry = (FileSystemRecoveryEntry)entry;
			try {
				this.registerGenericEntry(fEntry, context);

				if (
						FileSystemManager.isFile(fEntry.getFile()) && (
								(! FileSystemManager.isLink(fEntry.getFile()))
								|| (! ((FileSystemRecoveryTarget)this.target).isTrackSymlinks())
						)
				) {
					// The entry is stored if it has been modified
					if (this.checkFileModified(fEntry, context)) {
						if (DEBUG_MODE) {
							Logger.defaultLogger().fine("[" + FileSystemManager.getAbsolutePath(fEntry.getFile()) + "] : Backup in progress ...");
						}
						this.storeFileInArchive(fEntry, context);
						context.addInputBytes(FileSystemManager.length(fEntry.getFile()));
						this.registerStoredEntry(fEntry, context);

						context.getReport().addSavedFile();
					} else {
						if (DEBUG_MODE) {
							Logger.defaultLogger().fine("[" + FileSystemManager.getAbsolutePath(fEntry.getFile()) + "] : Unchanged.");
						}
						context.getReport().addIgnoredFile();
					}
				}
			} catch (IOException e) {
				Logger.defaultLogger().error(e);
				throw new StoreException("Error during storage.", e);
			}
		}
	}

	/**
	 * Returns the length of the archive passed as argument if it can be computed.
	 * <BR>Returns -1 otherwise (for instance if the file system driver is too slow to compute the size recursively)
	 */
	public long getArchiveSize(File archive, boolean forceComputation) throws ApplicationException {
        Manifest manifest = ArchiveManifestCache.getInstance().getManifest(
                this, 
                archive
        );
		
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
	}

	protected abstract void closeArchive(ProcessContext context) throws IOException; 

	/**
	 * Registers a generic entry - wether it has been filtered or not.
	 */
	protected abstract void registerGenericEntry(FileSystemRecoveryEntry entry, ProcessContext context) throws IOException;

	/**
	 * Registers an entry after it has passed the filters. (hence a stored entry) 
	 */
	protected abstract void registerStoredEntry(FileSystemRecoveryEntry entry, ProcessContext context) throws IOException;

	/**
	 * Close the archive
	 */
	public void commitBackup(ProcessContext context) throws ApplicationException {
		this.target.secureUpdateCurrentTask("Commiting backup ...", context);
		long entries = context.getTraceAdapter().getWritten();
		try {             
			// Close the trace file
			if (context.getTraceAdapter() != null) {
				context.getTraceAdapter().close();
				context.setTraceAdapter(null);
			}  

			// Close the content file
			if (context.getContentAdapter() != null) {
				context.getContentAdapter().close();
				context.setContentAdapter(null);
			}
			
			// Close the archive
			this.closeArchive(context); 
 
			// Add properties to manifest
			context.getManifest().addProperty(ManifestKeys.UNMODIFIED_FILES, context.getReport().getIgnoredFiles());
			context.getManifest().addProperty(ManifestKeys.ARCHIVE_SIZE, context.getOutputStreamListener().getWritten()); 
			context.getManifest().addProperty(ManifestKeys.STORED_FILES, context.getReport().getSavedFiles());
			context.getManifest().addProperty(ManifestKeys.ARCHIVE_NAME, FileSystemManager.getName(context.getCurrentArchiveFile()));

			// Store the manifest
			this.storeManifest(context);     

			// Flush all local files
			FileSystemManager.getInstance().flush(context.getCurrentArchiveFile());

			// Convert the archive : commit
			this.convertArchiveToFinal(context);

			// Create a copy of the target's XML configuration
			if (ArecaTechnicalConfiguration.get().isXMLBackup()) {
				this.target.secureUpdateCurrentTask("Creating a copy of the target's XML configuration ...", context);
				this.storeTargetConfigBackup(context);
			}

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
			ArchiveTraceCache.getInstance().remove(this, archive);    

			// Check memory usage
			if (MemoryHelper.isOverQuota(entries)) {
				Logger.defaultLogger().displayApplicationMessage(
						"" + this.getTarget().getUid(),
						MemoryHelper.getMemoryTitle(this.getTarget(), entries),
						MemoryHelper.getMemoryMessage(this.getTarget(), entries)
				);
			}
		}
	} 

	protected void convertArchiveToFinal(ProcessContext context) throws IOException, ApplicationException {
		markCommitted(context.getCurrentArchiveFile());
	}

	public void rollbackBackup(ProcessContext context) throws ApplicationException {
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
					// Close the content adapter
					if (context.getContentAdapter() != null) {
						try {
							context.getContentAdapter().close();
						} finally {
							context.setContentAdapter(null);
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
							// Flush all remaining data
							FileSystemManager.getInstance().flush(context.getCurrentArchiveFile());
						} finally {
							// Destroy directories
							AbstractFileSystemMedium.tool.delete(context.getCurrentArchiveFile(), true);
							AbstractFileSystemMedium.tool.delete(this.getDataDirectory(context.getCurrentArchiveFile()), true);

							this.target.secureUpdateCurrentTask("Rollback completed.", context);
						}
					}
				}
			}
		} catch (IOException e) {
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
			ProcessContext context) 
	throws ApplicationException {      
		ArchiveTrace trace = null;
		if (recoverDeletedEntries) {
			trace = this.buildAggregatedTrace(null, date);
		} else {
			trace = ArchiveTraceCache.getInstance().getTrace(this, getLastArchive(null, date));
		}

		recover(destination, filter, 1, null, date, true, trace, recoverDeletedEntries, ArchiveHandler.MODE_RECOVER, context);
		rebuidDirectories((File)destination, filter, trace);
		rebuildSymLinks((File)destination, filter, trace);
	}

	private void rebuildSymLinks(File destination, String[] filters, ArchiveTrace trace) throws ApplicationException {
		try {
			if (((FileSystemRecoveryTarget)this.target).isTrackSymlinks()){
				// Rebuild symbolic links
				Iterator iter = trace.symLinkEntrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry)iter.next();
					File symLink = new File(destination, (String)entry.getKey());

					if (filters == null || Util.passFilter(FileSystemManager.getAbsolutePath(symLink), filters)) {
						File parent = symLink.getParentFile();
						if (! FileSystemManager.exists(parent)) {
							tool.createDir(parent);
						}
						String hash = (String)entry.getValue();
						FileSystemManager.createSymbolicLink(symLink, ArchiveTrace.extractSymLinkPathFromTrace(hash));
					}
				}
			}
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		}
	}

	/**
	 * Creates all missing directories from the directory list contained in the trace.
	 * <BR>Allows to recover empty directories. 
	 */
	private void rebuidDirectories(File destination, String[] filters, ArchiveTrace trace) throws ApplicationException {
		try {
			String[] normalizedFilters = null;
			if (filters != null) {
				normalizedFilters = new String[filters.length];
				for (int i=0; i<filters.length; i++) {
					normalizedFilters[i] = FileSystemManager.getAbsolutePath(new File(destination, filters[i]));
					if (FileNameUtil.endsWithSeparator(normalizedFilters[i])) {
						normalizedFilters[i] = normalizedFilters[i].substring(0, normalizedFilters[i].length() - 1);
					}
				}
			}

			Iterator iter = trace.getDirectoryList().iterator();
			while (iter.hasNext()) {
				String key = (String)iter.next();
				File dir = new File(destination, key);
				if (matchFilters(dir, normalizedFilters)) {
					if (! FileSystemManager.exists(dir)) {
						AbstractFileSystemMedium.tool.createDir(dir);
					}
					applyDirectoryAttributes(dir, key, trace);
				}
			}
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		}
	}

	private void applyDirectoryAttributes(File dir, String key, ArchiveTrace trace) throws IOException {
		String hash = trace.getDirectoryHash(key);

		if (hash != null) {
			Attributes atts = ArchiveTrace.extractDirectoryAttributesFromTrace(hash);
			if (atts != null) {
				FileSystemManager.applyAttributes(atts, dir);
			}

			long lastModificationDate = ArchiveTrace.extractDirectoryModificationDateFromTrace(hash);
			if (lastModificationDate > 0) {
				FileSystemManager.setLastModified(dir, lastModificationDate);
			}
		}
	}

	private boolean matchFilters(File dir, String[] filters) {
		if (filters == null) {
			return true;
		} else {
			String tested = FileSystemManager.getAbsolutePath(dir);
			for (int i=0; i<filters.length; i++) {
				if (tested.equals(filters[i]) || tested.startsWith(filters[i] + "/")) {
					return true;
				}
			}

			return false;
		}
	}

	/**
	 * Lists the medium's archives
	 */
	public File[] listArchives(GregorianCalendar fromDate, GregorianCalendar toDate) {
		if (this.overwrite) {
			File f = new File(fileSystemPolicy.getArchivePath(), computeArchiveName(fromDate));
			if (FileSystemManager.exists(f)) {
				return new File[] {f};                
			} else {
				return new File[] {};
			}
		} else {
			File rootArchiveDirectory = fileSystemPolicy.getArchiveDirectory();
			File[] elementaryArchives = FileSystemManager.listFiles(rootArchiveDirectory, new ArchiveNameFilter(fromDate, toDate, this));

			if (elementaryArchives != null) {
				Arrays.sort(elementaryArchives, new ArchiveComparator(this));
			} else {
				elementaryArchives = new File[0];
			}

			return elementaryArchives;
		}
	} 
	
    protected abstract void computeMergeDirectories(ProcessContext context) throws ApplicationException ;

	public void merge(
			GregorianCalendar fromDate, 
			GregorianCalendar toDate, 
			boolean keepDeletedFiles,
			Manifest mfToInsert,
			ProcessContext context        
	) throws ApplicationException {

		try {
			this.checkRepository();

			if (toDate == null) {
				throw new ApplicationException("'To date' is mandatory");
			}

			if (! overwrite) { // No merge if "overwrite" = true
				Logger.defaultLogger().info(
						"Starting merge from " + Utils.formatDisplayDate(fromDate) + " to " + Utils.formatDisplayDate(toDate)
						+ ". 'Preserve Deleted Entries' option set to '" + keepDeletedFiles + "'."
				);

				// Recovery of the merged archives
				context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.8, "recover");

				ArchiveTrace trace;
				if (keepDeletedFiles) {
					trace = this.buildAggregatedTrace(fromDate, toDate);
				} else {
					trace = ArchiveTraceCache.getInstance().getTrace(this, getLastArchive(null, toDate));
				}
				
				// Recover
				recover(
					null, 
					null, 
					2,
					fromDate, 
					toDate, 
					false, 
					keepDeletedFiles ? null : trace, 
					keepDeletedFiles,    
					ArchiveHandler.MODE_MERGE,
					context
				);

				context.getInfoChannel().print("Recovery completed - Merged archive creation ...");     
				context.getInfoChannel().updateCurrentTask(0, 0, "merge");

				// Delete the current manifest --> Backward compatibility - to remove
				File mfFile = new File(context.getRecoveryDestination(), getManifestName());
				if (FileSystemManager.exists(mfFile)) {
					AbstractFileSystemMedium.tool.delete(mfFile, true);
				}
				File oldMfFile = new File(context.getRecoveryDestination(), getOldManifestName()); // Backward compatibility
				if (FileSystemManager.exists(oldMfFile)) {
					AbstractFileSystemMedium.tool.delete(oldMfFile, true);
				}

				context.getTaskMonitor().checkTaskCancellation();

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
					context.getManifest().addProperty(ManifestKeys.OPTION_KEEP_DELETED_ENTRIES, keepDeletedFiles);
					context.getManifest().addProperty(ManifestKeys.MERGE_START, Utils.formatDisplayDate(fromDate));
					context.getManifest().addProperty(ManifestKeys.MERGE_END, Utils.formatDisplayDate(toDate));
					context.getManifest().addProperty(ManifestKeys.MERGED_ARCHIVES, processedFiles.length);
					context.getManifest().addProperty(ManifestKeys.ARCHIVE_SIZE, context.getOutputStreamListener().getWritten());					
					context.getManifest().addProperty(ManifestKeys.ARCHIVE_NAME, FileSystemManager.getName(context.getCurrentArchiveFile()));

					AbstractRecoveryTarget.addBasicInformationsToManifest(context.getManifest());
					this.storeManifest(context);

					// Build and store the trace file
					File target = new File(getDataDirectory(context.getCurrentArchiveFile()), getTraceFileName(false));
					ArchiveTraceAdapter traceAdapter = new ArchiveTraceAdapter(this, target);
					traceAdapter.writeTrace(trace);
					traceAdapter.close();

					// Build and store the merged content file
					ArchiveContent merged = new ArchiveContent();
					for (int i=0; i<recoveredFiles.length; i++) {
						merged.override(ArchiveContentManager.getContentForArchive(this, recoveredFiles[i]));
					}
					merged.clean(trace);

					target = new File(getDataDirectory(context.getCurrentArchiveFile()), getContentFileName(false));
					ArchiveContentAdapter contentAdapter = new ArchiveContentAdapter(target);
					contentAdapter.writeContent(merged);
					contentAdapter.close();
				}
			}
		} catch (ApplicationException e) {
			Logger.defaultLogger().error(e);
			throw e;
		} catch (Exception e) {
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

	public void cleanMerge(ProcessContext context) throws IOException {
		if (context.getCurrentArchiveFile() != null && context.getRecoveryDestination() != null && ! context.getCurrentArchiveFile().equals(context.getRecoveryDestination())) {
			AbstractFileSystemMedium.tool.delete(context.getRecoveryDestination(), true);
		}
	}

	public void commitMerge(ProcessContext context) throws ApplicationException {
		if (! this.overwrite) {
			this.target.secureUpdateCurrentTask("Commiting merge ...", context);
			super.commitMerge(context);

			try {
				// Nettoyage des donnees temporaires
				this.cleanMerge(context);

				// Fermeture de l'archive
				this.closeArchive(context); 

				File[] recoveredFiles = context.getReport().getRecoveryResult().getRecoveredArchivesAsArray();
				File[] ignoredFiles = context.getReport().getRecoveryResult().getIgnoredArchivesAsArray();

				// Suppression des archives ignorees
				Logger.defaultLogger().info("Deleting unnecessary archives : " + ignoredFiles.length + " archives.");
				for (int i=0; i<ignoredFiles.length; i++) {
					Logger.defaultLogger().info("Deleting " + FileSystemManager.getAbsolutePath(ignoredFiles[i]) + " ...");
					this.deleteArchive(ignoredFiles[i]);                       
				}

				if (recoveredFiles.length >= 2) {
					// Suppression des archives restaurees
					Logger.defaultLogger().info("Deleting recovered archives : " + recoveredFiles.length + " archives.");
					for (int i=0; i<recoveredFiles.length; i++) {
						Logger.defaultLogger().info("Deleting " + FileSystemManager.getAbsolutePath(recoveredFiles[i]) + " ...");
						this.deleteArchive(recoveredFiles[i]);                       
					}

					// conversion de l'archive
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

	public void rollbackMerge(ProcessContext context) throws ApplicationException {
		if (! this.overwrite) {
			this.target.secureUpdateCurrentTask("Rollbacking merge ...", context);
			try {
				try {
					// Nettoyage des donnees temporaires
					this.cleanMerge(context);
				} finally {
					try {
						// Fermeture de l'archive
						this.closeArchive(context); 
					} finally {
						// Suppression de l'archive
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

	protected abstract void buildMergedArchiveFromDirectory(ProcessContext context) throws ApplicationException;    

	/**
	 * Build an optimized list of archives to recover.
	 * <BR>The result is stored in the RecoveryResult.
	 */
	private void buildArchiveListToRecover(
			RecoveryResult result, 
			GregorianCalendar fromDate,
			GregorianCalendar toDate,
			boolean forceAllEntriesRecovery
	) throws ApplicationException {
		File[] listedArchives = this.listArchives(fromDate, toDate);

		result.addProcessedArchives(listedArchives);
		if (! forceAllEntriesRecovery) {
			boolean ignoreIncrementalAndDifferentialArchives = false;
			boolean ignoreAllArchives = false;

			// BACKWARD COMPATIBILITY
			// Necessary because previous versions of Areca (before 5.3.3) didn't store the backup scheme in the manifest
			// So we have to assume that the first archive is a full backup
			boolean fullArchiveScope = (fromDate == null);
			if (! fullArchiveScope) {
				File[] previousArchives = listArchives(null, fromDate);
				fullArchiveScope = (previousArchives == null || previousArchives.length == 0);                    
			}
			// EOF BACKWARD COMPATIBILITY

			for (int i=listedArchives.length - 1; i>=0; i--) {
				Manifest mf = ArchiveManifestCache.getInstance().getManifest(this, listedArchives[i]);
				String prp = mf.getStringProperty(ManifestKeys.OPTION_BACKUP_SCHEME, AbstractRecoveryTarget.BACKUP_SCHEME_INCREMENTAL);
				if (i == 0 && fullArchiveScope) {
					// Force "full backup" scheme for the first archive
					prp = AbstractRecoveryTarget.BACKUP_SCHEME_FULL;
				}

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
		} else {
			result.addRecoveredArchives(listedArchives);
		}
	}
	
	protected Set buildAtomicEntrySet(String[] entries, ArchiveTrace trace) {
		String root = ((FileSystemRecoveryTarget)this.getTarget()).getSourceDirectory();
		Set entriesToDispatch = new HashSet();
		List directoriesToDispatch = new ArrayList();
		for (int e=0; e<entries.length; e++) {
			if (FileNameUtil.endsWithSeparator(entries[e])) {
				if (FileNameUtil.startsWithSeparator(entries[e])) {
					directoriesToDispatch.add(entries[e].substring(1));
				} else {
					directoriesToDispatch.add(entries[e]);
				}
			} else {
				entriesToDispatch.add(new FileSystemRecoveryEntry(root, new File(root, entries[e])));	                        	
			}
		}
		
		String[] dirs = (String[])directoriesToDispatch.toArray(new String[directoriesToDispatch.size()]);
		Iterator iter = trace.fileKeySet().iterator();
		while (iter.hasNext()) {
			String f = (String)iter.next();
			for (int i=0; i<dirs.length; i++) {
				if (f.startsWith(dirs[i])) {
					entriesToDispatch.add(new FileSystemRecoveryEntry(root, new File(root, f)));
					break;
				}
			}
		}
		
		return entriesToDispatch;
	}

	/**
	 * Recovers the files at the requested recovery location, according to the recovery dates passed as argument.
	 * <BR>'filters' may be null ...
	 * <BR>The recovery is actually done if there are at least <code>minimumArchiveNumber</code> archives to recover
	 */
	protected void recover(
			Object destination,                         // Where to recover
			String[] filters,                           // Filters the recovered entries
			int minimumArchiveNumber,           		// The recovery is done only if there are at least this number of archives to recover
			GregorianCalendar fromDate,          		// Recovery from date
			GregorianCalendar toDate,             		// Recovery to date
			boolean applyAttributes,              		// Tells whether the attributes must be applied or not
			ArchiveTrace trace,                       	// Optional trace to apply to the recovered data
			boolean forceAllEntriesRecovery,  			// Tells whether all entries will be recovered or not
			short mode,                                 // Recovery mode : see ArchiveHandler.MODE_MERGE / MODE_RECOVER
			ProcessContext context                		// Execution context
	) throws ApplicationException {
		File targetFile = (File)destination;
		
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
			buildArchiveListToRecover(result, fromDate, toDate, forceAllEntriesRecovery);

			// Second stage : recover data
			if (result.getRecoveredArchives().size() >= minimumArchiveNumber) {
				context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.9, "recover");
				File[] optimizedArchives = result.getRecoveredArchivesAsArray();
				
				// If no destination was set, compute it from the last archive's date.
				if (targetFile == null) {
					GregorianCalendar lastArchiveDate = ArchiveManifestCache.getInstance().getManifest(this, optimizedArchives[optimizedArchives.length - 1]).getDate();
					targetFile = new File(computeArchivePath(lastArchiveDate));
				}
				FileTool.getInstance().delete(targetFile, true);
				context.setRecoveryDestination(targetFile);
				FileTool.getInstance().createDir(targetFile);
				
				if (mode == ArchiveHandler.MODE_MERGE) {
					computeMergeDirectories(context);
				}
				
				// Process Recovery
				if (filters == null || filters.length == 0) {
					// Dummy mode : recover all entries / archives
					Logger.defaultLogger().info("Recovering in standard mode.");
					handler.recoverRawData(optimizedArchives, null, mode, context);
				} else {
					// Smart mode : iterate on each entry and recover its latest version only
					Logger.defaultLogger().info("Recovering in optimized mode.");
					Set entriesToDispatch = buildAtomicEntrySet(filters, trace);
					Logger.defaultLogger().info("" + entriesToDispatch.size() + " entries will be recovered.");

					// Recovery
					Map entriesByArchive = handler.dispatchEntries(optimizedArchives, entriesToDispatch);
					handler.recoverRawData(optimizedArchives, entriesByArchive, mode, context);
				}

				context.getTaskMonitor().checkTaskCancellation();

				// Third stage: clean recovery directory
				if (trace != null) {
					this.applyTrace(
							targetFile, 
							trace,
							applyAttributes,
							true,
							context);
				}
			}
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		} catch (TaskCancelledException e) {
			throw new ApplicationException(e);
		} finally {
			context.getTaskMonitor().getCurrentActiveSubTask().setCurrentCompletion(1);            
		}
	} 

	/**
	 * Ensures that the stored files are present on the local disk. Useful for some archive handlers that need
	 * to ensure this before processing the archives.
	 * <BR>Returns a set of directories where local copies can be found.
	 */
	public abstract File[] ensureLocalCopy(
			File[] archivesToProcess, 
			boolean overrideRecoveredFiles,
			File destination,
            Map filtersByArchive, 
			ProcessContext context
	) throws IOException, ApplicationException;

	public abstract void cleanLocalCopies(
			List copies, 
			ProcessContext context
	) throws IOException, ApplicationException;

	public abstract void completeLocalCopyCleaning(
			File copy, 
			ProcessContext context
	) throws IOException, ApplicationException;

	/**
	 * Applique le fichier de trace.
	 */
	protected void applyTrace(
			File targetFile, 
			ArchiveTrace trace,
			boolean applyAttributes,
			boolean cancelSensitive,
			ProcessContext context
	) throws IOException, TaskCancelledException {      
		// Nettoyage : on supprime 
		// - Tous les fichiers n'apparaissant pas dans la trace
		// - Tous les repertoires vides
		// - MAIS attention : on ne supprime pas la trace.
		Iterator iter = new FileSystemIterator(targetFile, false);
		while (iter.hasNext()) {
			if (cancelSensitive) {
				context.getTaskMonitor().checkTaskCancellation();  // Check for cancels only if we are cancel sensitive --> useful for "commit"
			}

			File f = (File)iter.next();
			if (FileSystemManager.isFile(f)) {
				String shortPath = Utils.extractShortFilePath(f, targetFile);
				String hash = trace.getFileHash(shortPath);

				if (hash == null) {
					// No trace found for the current file --> destroy it
					deleteRecur(targetFile, f);
				} else if (applyAttributes) {
					// File found --> set additional attributes
					FileSystemManager.setLastModified(f, ArchiveTrace.extractFileModificationDateFromTrace(hash));
					Attributes atts = ArchiveTrace.extractFileAttributesFromTrace(hash);
					if (atts != null) {
						FileSystemManager.applyAttributes(atts, f);
					}
				}
			} else {
				// On tente betement de supprimer le repertoire
				// Il ne le sera que s'il ne contient aucun fichier
				deleteRecur(targetFile, f); 
			}
		}
	}

	private void deleteRecur(File root, File current) {
		if (FileSystemManager.delete(current)) {
			File parent = FileSystemManager.getParentFile(current);
			if (FileNameUtil.normalizePath(FileSystemManager.getAbsolutePath(parent))
					.startsWith(FileNameUtil.normalizePath(FileSystemManager.getAbsolutePath(root)))) {
				deleteRecur(root, parent); // Le repertoire parent ne sera supprime que s'il est vide
			}
		}
	}

	/**
	 * Checks whether the entry has been modified since the last backup
	 */
	protected boolean checkFileModified(FileSystemRecoveryEntry fEntry, ProcessContext context) throws IOException {
		return context.getPreviousTrace().hasFileBeenModified(fEntry);
	}

	// Retourne une map de taille index�e par path
	protected Map formatContent(File file) throws IOException {
		Map map = new HashMap();
		ArchiveContent content = ArchiveContentManager.getContentForArchive(this, file);
		Iterator iter = content.getContent();
		while (iter.hasNext()) {
			FileSystemRecoveryEntry entry = (FileSystemRecoveryEntry)iter.next();
			map.put(
					entry.getName(),
					new Long(entry.getSize())
			);
		}
		return map;
	}

	public Set getEntries(File archive) throws ApplicationException {
		try {
			Map storedFiles = this.formatContent(archive);
			ArchiveTrace trace = ArchiveTraceCache.getInstance().getTrace(this, archive);

			return getEntrySetFromTrace(trace, storedFiles);
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		}
	}

	private ArchiveTrace buildAggregatedTrace(GregorianCalendar fromDate, GregorianCalendar toDate) throws ApplicationException {
		Logger.defaultLogger().info("Building aggregated archive trace ...");

		if (fromDate != null) {
			fromDate = (GregorianCalendar)fromDate.clone();
			fromDate.add(GregorianCalendar.MILLISECOND, -1);
		}

		ArchiveTrace content = new ArchiveTrace();
		File[] archives = this.listArchives(fromDate, toDate);
		for (int i=0; i<archives.length; i++) {
			File archive = archives[i];
			Logger.defaultLogger().info("Merging archive trace (" + FileSystemManager.getAbsolutePath(archive) + ") ...");
			ArchiveTrace trace = ArchiveTraceCache.getInstance().getTrace(this, archive);
			content.merge(trace);
		}

		Logger.defaultLogger().info("Aggregated archive trace built.");
		return content;
	}

	public Set getLogicalView() throws ApplicationException {
		ArchiveTrace mergedTrace = buildAggregatedTrace(null, null);
		ArchiveTrace latestTrace = ArchiveTraceCache.getInstance().getTrace(this, this.getLastArchive());

		Map latestContent = new HashMap();
		if (latestTrace != null) {
			latestContent.putAll(latestTrace.getFileMap());
			latestContent.putAll(latestTrace.getDirectoryMap());
			latestContent.putAll(latestTrace.getSymLinkMap());
		}

		return getEntrySetFromTrace(mergedTrace, latestContent);
	}

	private Set getEntrySetFromTrace(ArchiveTrace source, Map referenceMap) {
		Set elements = new HashSet();
		Iterator iter = source.fileEntrySet().iterator();
		String baseDirectory = fileSystemPolicy.getArchivePath();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry)iter.next();

			String entryPath = (String)entry.getKey();
			String entryTrace = (String)entry.getValue();

			try {                
				elements.add(new FileSystemRecoveryEntry(
						baseDirectory, 
						new File(baseDirectory, entryPath),
						referenceMap.containsKey(entryPath) ? RecoveryEntry.STATUS_STORED: RecoveryEntry.STATUS_NOT_STORED,
						ArchiveTrace.extractFileSizeFromTrace(entryTrace)
				));
			} catch (RuntimeException e) {
				Logger.defaultLogger().error("Error reading archive trace : for file [" + entryPath + "], trace = [" + entryTrace + "]", e);
				throw e;
			}
		}

		iter = source.directoryEntrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry)iter.next();
			String entryPath = (String)entry.getKey();

			elements.add(new FileSystemRecoveryEntry(
					baseDirectory, 
					new File(baseDirectory, entryPath),
					referenceMap.containsKey(entryPath) ? RecoveryEntry.STATUS_STORED: RecoveryEntry.STATUS_NOT_STORED,
					-1
			));
		}

		iter = source.symLinkEntrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry)iter.next();
			String entryPath = (String)entry.getKey();
			String entryTrace = (String)entry.getValue();

			long size = -1;
			if (ArchiveTrace.extractSymLinkFileFromTrace(entryTrace)) {
				size = 0;
			}

			elements.add(new FileSystemRecoveryEntry(
					baseDirectory, 
					new File(baseDirectory, entryPath),
					referenceMap.containsKey(entryPath) ? RecoveryEntry.STATUS_STORED: RecoveryEntry.STATUS_NOT_STORED,
					size,
					true
			));
		}

		return elements;
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

	public void simulateEntryProcessing(RecoveryEntry entry, ProcessContext context) throws ApplicationException {
		FileSystemRecoveryEntry fEntry = (FileSystemRecoveryEntry)entry;

		// Init du contexte
		if (! context.isInitialized()) {
			File archive = this.getLastArchive();
			ArchiveTrace trace = ArchiveTraceCache.getInstance().getTrace(this, archive);
			ArchiveTrace cloned = null;
			if (trace != null) {
				cloned = trace.cloneTrace();
			} else {
				cloned = new ArchiveTrace();
			}
			context.setPreviousTrace(cloned);
			context.setInitialized();
		}

		ArchiveTrace trace = context.getPreviousTrace();

		try {
			if (((FileSystemRecoveryTarget)this.target).isTrackSymlinks() && FileSystemManager.isLink(fEntry.getFile())) {
				// Verification que l'entree sera stockee.
				if (! trace.containsSymLink(fEntry)) {
					fEntry.setStatus(EntryArchiveData.STATUS_CREATED);
				} else if (trace.hasSymLinkBeenModified(fEntry)) {
					fEntry.setStatus(EntryArchiveData.STATUS_MODIFIED);
				} else {
					fEntry.setStatus(RecoveryEntry.STATUS_NOT_STORED);            
				}
				trace.removeFile(fEntry);
			} else if (FileSystemManager.isFile(fEntry.getFile())) {
				// Verification que l'entree sera stockee.
				if (! trace.containsFile(fEntry)) {
					fEntry.setStatus(EntryArchiveData.STATUS_CREATED);
				} else if (trace.hasFileBeenModified(fEntry)) {
					fEntry.setStatus(EntryArchiveData.STATUS_MODIFIED);
				} else {
					fEntry.setStatus(RecoveryEntry.STATUS_NOT_STORED);            
				}
				trace.removeFile(fEntry);
			} else if (this.trackDirectories) {
				if (! trace.containsDirectory(fEntry)) {
					fEntry.setStatus(EntryArchiveData.STATUS_CREATED);
				} else {
					fEntry.setStatus(RecoveryEntry.STATUS_NOT_STORED);
				}
				trace.removeDirectory(fEntry);
			} else {
				fEntry.setStatus(RecoveryEntry.STATUS_NOT_STORED);
			}
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		}
	}

	public List closeSimulation(ProcessContext context) throws ApplicationException {
		ArchiveTrace trace = context.getPreviousTrace();
		if (trace == null) {
			return new ArrayList();
		} else {
			ArrayList ret = new ArrayList(trace.fileSize() + trace.directorySize());

			// Files
			Iterator iter = trace.fileEntrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry)iter.next();
				String path = (String)entry.getKey();
				String hash = (String)entry.getValue();

				long size = ArchiveTrace.extractFileSizeFromTrace(hash); 
				ret.add(new FileSystemRecoveryEntry(fileSystemPolicy.getArchivePath(), new File(fileSystemPolicy.getArchivePath(), path), EntryArchiveData.STATUS_DELETED, size));
			}

			// Directories
			iter = trace.directoryEntrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry)iter.next();
				String path = (String)entry.getKey();               
				ret.add(new FileSystemRecoveryEntry(fileSystemPolicy.getArchivePath(), new File(fileSystemPolicy.getArchivePath(), path), EntryArchiveData.STATUS_DELETED, 0));
			}            

			return ret;
		}
	}

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

		// BACKWARD COMPATIBILITY
		// Necessary because previous versions of Areca (before 5.3.3) didn't store the backup scheme in the manifest
		// So we have to assume that the first archive is a full backup
		boolean fullArchiveScope = (fromDate == null);
		if (! fullArchiveScope) {
			File[] previousArchives = listArchives(null, fromDate);
			fullArchiveScope = (previousArchives == null || previousArchives.length == 0);                    
		}
		// EOF BACKWARD COMPATIBILITY

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
		if (hasFullBackup || fullArchiveScope) {
			manifest.addProperty(ManifestKeys.OPTION_BACKUP_SCHEME, AbstractRecoveryTarget.BACKUP_SCHEME_FULL);
		} else if (hasDifferentialBackup) {
			manifest.addProperty(ManifestKeys.OPTION_BACKUP_SCHEME, AbstractRecoveryTarget.BACKUP_SCHEME_DIFFERENTIAL);            
		} else {
			manifest.addProperty(ManifestKeys.OPTION_BACKUP_SCHEME, AbstractRecoveryTarget.BACKUP_SCHEME_INCREMENTAL);            
		}
	}

	public void storeManifest(ProcessContext context) throws ApplicationException {
		if (context.getManifest() != null) {
			ManifestManager.writeManifest(this, context.getManifest(), context.getCurrentArchiveFile());            
		}
	}  

	/**
	 * Deletes the archive - WETHER IT IS COMMITED OR NOT
	 */
	protected void deleteArchive(File archive) throws IOException {
		AbstractFileSystemMedium.tool.delete(archive, true);
		AbstractFileSystemMedium.tool.delete(this.getDataDirectory(archive), true);
	}

	/**
	 * Retourne le status de l'entree, dans l'archive specifiee.
	 * 
	 * @param entry
	 * @param archive
	 * @return
	 * @throws ApplicationException
	 */
	protected EntryArchiveData getArchiveData(FileSystemRecoveryEntry entry, File archive) throws ApplicationException {
		try {
			ArchiveTrace trace = ArchiveTraceCache.getInstance().getTrace(this, archive);
			ArchiveContent content = ArchiveContentManager.getContentForArchive(this, archive);

			EntryArchiveData ead = new EntryArchiveData();
			Manifest mf = ArchiveManifestCache.getInstance().getManifest(this, archive);
			ead.setManifest(mf);
			ead.setHash(trace.getFileHash(entry));

			if (entry.isLink()) {
				// LINK
				if (trace.containsSymLink(entry)) {
					ead.setStatus(EntryArchiveData.STATUS_STORED);
				} else {
					ead.setStatus(EntryArchiveData.STATUS_NOT_STORED);
				}
			} else {
				// STANDARD FILE
				if (content.contains(entry)) {
					ead.setStatus(EntryArchiveData.STATUS_STORED);
				} else {
					ead.setStatus(EntryArchiveData.STATUS_NOT_STORED);
				}
			}

			return ead;
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
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

	private void searchWithinArchive(SearchCriteria criteria, File archive, TargetSearchResult result) throws ApplicationException {
		SearchMatcher matcher = new SearchMatcher((DefaultSearchCriteria)criteria);

		ArchiveTrace trace = ArchiveTraceCache.getInstance().getTrace(this, archive);
		Manifest mf = ArchiveManifestCache.getInstance().getManifest(this, archive);
		String root = ((FileSystemRecoveryTarget)this.getTarget()).getSourceDirectory();
		Iterator iter = trace.fileKeySet().iterator();
		while (iter.hasNext()) {
			String name = (String)iter.next();
			if (matcher.matches(name)) {
				File path = new File(root, name);
				FileSystemRecoveryEntry entry = new FileSystemRecoveryEntry(root, path, RecoveryEntry.STATUS_STORED);
				SearchResultItem item = new SearchResultItem();
				item.setCalendar(mf.getDate());
				item.setEntry(entry);
				item.setTarget(this.getTarget());

				result.addSearchresultItem(item);
			}
		}
	}
}