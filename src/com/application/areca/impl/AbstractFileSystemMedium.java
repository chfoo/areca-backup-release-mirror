package com.application.areca.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import com.application.areca.AbstractMedium;
import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ApplicationException;
import com.application.areca.ArecaTechnicalConfiguration;
import com.application.areca.EntryArchiveData;
import com.application.areca.Errors;
import com.application.areca.TargetActions;
import com.application.areca.TargetGroup;
import com.application.areca.Utils;
import com.application.areca.adapters.ProcessXMLWriter;
import com.application.areca.cache.ArchiveManifestCache;
import com.application.areca.context.ProcessContext;
import com.application.areca.impl.policy.EncryptionPolicy;
import com.application.areca.impl.policy.FileSystemPolicy;
import com.application.areca.indicator.Indicator;
import com.application.areca.indicator.IndicatorMap;
import com.application.areca.indicator.IndicatorTypes;
import com.application.areca.metadata.MetadataConstants;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.manifest.ManifestKeys;
import com.application.areca.metadata.trace.ArchiveTraceAdapter;
import com.application.areca.metadata.trace.ArchiveTraceManager;
import com.application.areca.metadata.trace.ArchiveTraceParser;
import com.application.areca.metadata.trace.TraceEntry;
import com.application.areca.metadata.trace.TraceFileIterator;
import com.myJava.file.CompressionArguments;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.driver.DriverAlreadySetException;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.file.driver.event.EventFileSystemDriver;
import com.myJava.file.driver.event.LoggerFileSystemDriverListener;
import com.myJava.file.driver.event.OpenFileMonitorDriverListener;
import com.myJava.object.ToStringHelper;
import com.myJava.util.errors.ActionError;
import com.myJava.util.errors.ActionReport;
import com.myJava.util.history.DefaultHistory;
import com.myJava.util.history.History;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;

/**
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
public abstract class AbstractFileSystemMedium 
extends AbstractMedium 
implements TargetActions, IndicatorTypes {

	public static final boolean CHECK_DIRECTORY_CONSISTENCY = ArecaTechnicalConfiguration.get().isCheckRepositoryConsistency();
	private static final boolean REPOSITORY_ACCESS_DEBUG = ArecaTechnicalConfiguration.get().isRepositoryAccessDebugMode();
	private static final boolean FILESTREAMS_DEBUG = ArecaTechnicalConfiguration.get().isFileStreamsDebugMode();
	private static final String REPOSITORY_ACCESS_DEBUG_ID = "Areca repository access";

	protected static final String COMMIT_MARKER_NAME = ".committed";

	/**
	 * Suffix added to the archive name to create the data directory (containing the manifest and trace)
	 */
	protected static final String DATA_DIRECTORY_SUFFIX = "_data";

	/**
	 * Manifest name
	 */
	protected static final String MANIFEST_FILE = "manifest";   

	/**
	 * Name used for target configuration backup
	 */
	protected static final String TARGET_BACKUP_FILE_PREFIX = "/areca_config_backup/target_backup_";
	protected static final String TARGET_BACKUP_FILE_SUFFIX = ".xml";

	/**
	 * File processing tool
	 */
	protected static final FileTool tool = FileTool.getInstance();

	/**
	 * Encryption arguments
	 */
	protected EncryptionPolicy encryptionPolicy = null;

	/**
	 * Base storage policy
	 */
	protected FileSystemPolicy fileSystemPolicy = null;

	/**
	 * Compression arguments
	 * <BR>These arguments may be interpreted specifically depending on the medium type.
	 */
	protected CompressionArguments compressionArguments = new CompressionArguments();

	/**
	 * Checks that the archive provided as argument belongs to this medium
	 */
	public boolean checkArchiveCompatibility(File archive) {
		if (archive == null) {
			return false;
		} else {
			String archivePath = FileSystemManager.getAbsolutePath(archive);

			return 
				(! archivePath.endsWith(DATA_DIRECTORY_SUFFIX))
				&& isCommitted(archive);
		}
	}   

	/**
	 * Valide diverses regles de gestion, notamment le fait que la gestion du cryptage est activee ou desactivee explicitement.
	 */
	public ActionReport checkMediumState(int action) {
		ActionReport result = new ActionReport();

		if (encryptionPolicy == null) {
			result.addError(new ActionError(Errors.ERR_C_MEDIUM_ENCRYPTION_NOT_INITIALIZED, Errors.ERR_M_MEDIUM_ENCRYPTION_NOT_INITIALIZED));
		}

		if (action != ACTION_DESCRIBE) {       
			// The backup directory mustn't be included in the base directory
			File backupDir = fileSystemPolicy.getArchiveDirectory();

			Iterator iter = ((FileSystemRecoveryTarget)this.getTarget()).sources.iterator();
			while (iter.hasNext()) {
				File src = (File)iter.next();
				if (CHECK_DIRECTORY_CONSISTENCY && AbstractFileSystemMedium.tool.isParentOf(src, backupDir)) {
					result.addError(new ActionError(Errors.ERR_C_INCLUSION, Errors.ERR_M_INCLUSION));               
				}
			}
		}

		return result;
	}

	/**
	 * On vide les caches lors de la fusion.
	 * <BR>A optimiser en tenant compte de la date.
	 */
	public void commitMerge(ProcessContext context) throws ApplicationException {
		clearRelatedCaches();
	}

	public IndicatorMap computeIndicators() throws ApplicationException {
		try {
			IndicatorMap indicators = new IndicatorMap();
			File[] archives = this.listArchives(null, null);

			// Number of archives - NOA
			long noaValue = archives.length;
			Indicator noa = new Indicator();
			noa.setId(T_NOA);
			noa.setName(N_NOA);
			noa.setStringValue(Utils.formatLong(noaValue));
			noa.setValue(noaValue);
			indicators.addIndicator(noa);

			// Archives' physical size - APS
			long apsValue = 0;
			for (int i=0; i<archives.length; i++) {
				apsValue += this.getArchiveSize(archives[i], true);
			}
			Indicator aps = new Indicator();
			aps.setId(T_APS);
			aps.setName(N_APS);
			aps.setStringValue(Utils.formatFileSize(apsValue));
			aps.setValue(apsValue);   
			indicators.addIndicator(aps);

			File archive = this.getLastArchive();
			if (archive != null) {
				// Source file size - SFS
				File trcFile = ArchiveTraceManager.resolveTraceFileForArchive((AbstractIncrementalFileSystemMedium)this, archive);
				ArchiveTraceAdapter adapter = new ArchiveTraceAdapter(trcFile);
				TraceFileIterator iter = adapter.buildIterator();
				TraceEntry entry;
				long sfsValue = 0;
				long nofValue = 0;
				try {
					while (iter.hasNext()) {
						entry = iter.next();
						if (entry.getType() == MetadataConstants.T_FILE) {
							sfsValue += ArchiveTraceParser.extractFileSizeFromTrace(entry.getData());
							nofValue++;
						}
					}
				} finally {
					iter.close();
				}

				Indicator sfs = new Indicator();
				sfs.setId(T_SFS);
				sfs.setName(N_SFS);
				sfs.setStringValue(Utils.formatFileSize(sfsValue));
				sfs.setValue(sfsValue);   
				indicators.addIndicator(sfs);

				// Number of files - NOF
				Indicator nof = new Indicator();
				nof.setId(T_NOF);
				nof.setName(N_NOF);
				nof.setStringValue(Utils.formatLong(nofValue));
				nof.setValue(nofValue);   
				indicators.addIndicator(nof);

				// Logical and physical size of the latest full backup
				long lsValue = 0;
				long psValue = 0;
				File latestFullArchive = null;
				for (int i=archives.length-1; i>=0; i--) {
					archive = archives[i];

					Manifest mf = ArchiveManifestCache.getInstance().getManifest(this, archive);
					if (mf != null && AbstractRecoveryTarget.BACKUP_SCHEME_FULL.equals(mf.getStringProperty(ManifestKeys.OPTION_BACKUP_SCHEME))) {
						latestFullArchive = archive;
						break;
					}
				}

				if (latestFullArchive != null) {                
					trcFile = ArchiveTraceManager.resolveTraceFileForArchive((AbstractIncrementalFileSystemMedium)this, latestFullArchive);
					adapter = new ArchiveTraceAdapter(trcFile);
					iter = adapter.buildIterator();
					try {
						while (iter.hasNext()) {
							entry = iter.next();
							if (entry.getType() == MetadataConstants.T_FILE) {
								lsValue += ArchiveTraceParser.extractFileSizeFromTrace(entry.getData());
							}
						}
					} finally {
						iter.close();
					}

					psValue = this.getArchiveSize(latestFullArchive, true);
				}

				// Physical size ratio - PSR            
				double psrValue;
				String psrStr;

				if (lsValue == 0) {
					psrValue = -1;
					psrStr = "N/A";
				} else {
					psrValue = 100 * (double)psValue / (double)lsValue;
					psrStr = Utils.formatLong((long)psrValue) + " %";
				}

				Indicator psr = new Indicator();
				psr.setId(T_PSR);
				psr.setName(N_PSR);
				psr.setStringValue(psrStr);
				psr.setValue(psrValue);   
				indicators.addIndicator(psr);

				// Size without history - SWH
				long swhValue = Math.min((long)(psrValue * (double)sfsValue / 100.), apsValue);
				Indicator swh = new Indicator();
				swh.setId(T_SWH);
				swh.setName(N_SWH);
				swh.setStringValue(Utils.formatFileSize(swhValue));
				swh.setValue(swhValue);   
				indicators.addIndicator(swh);

				// SOH
				long sohValue = apsValue - swhValue;
				Indicator soh = new Indicator();
				soh.setId(T_SOH);
				soh.setName(N_SOH);
				soh.setStringValue(Utils.formatFileSize(sohValue));
				soh.setValue(sohValue);   
				indicators.addIndicator(soh);
			}

			return indicators;
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		}
	}

	public void deleteArchives(GregorianCalendar fromDate, ProcessContext context) throws ApplicationException {
		this.checkRepository();

		Logger.defaultLogger().info(
				"Starting deletion from " + Utils.formatDisplayDate(fromDate) + "."
		);

		if (fromDate != null) {
			fromDate = (GregorianCalendar)fromDate.clone();
			fromDate.add(GregorianCalendar.MILLISECOND, -1);
		}

		File[] archives = this.listArchives(fromDate, null);

		for (int i=0; i<archives.length; i++) {
			try {
				context.getInfoChannel().updateCurrentTask(i+1, archives.length, FileSystemManager.getName(archives[i]));
				deleteArchive(archives[i]);
				context.getTaskMonitor().getCurrentActiveSubTask().setCurrentCompletion(i+1, archives.length + 1);
			} catch (Exception e) {
				throw new ApplicationException(e);
			}
		}

		try {
			FileSystemManager.getInstance().flush(fileSystemPolicy.getArchiveDirectory());
		} catch (IOException e) {
			throw new ApplicationException(e);
		}

		Logger.defaultLogger().info(
				"Deletion completed - " + archives.length + " archive" + (archives.length>1?"s":"") + " deleted."
		);

		context.getTaskMonitor().getCurrentActiveSubTask().enforceCompletion();          
	}

	public void destroyRepository() throws ApplicationException {
		File storage = fileSystemPolicy.getArchiveDirectory();
		Logger.defaultLogger().info("Deleting repository : " + FileSystemManager.getAbsolutePath(storage) + " ...");
		try {
			FileTool tool = FileTool.getInstance();
			tool.delete(storage, true);
			Logger.defaultLogger().info(FileSystemManager.getAbsolutePath(storage) + " deleted.");
		} catch (Exception e) {
			throw new ApplicationException("Error trying to delete directory : " + FileSystemManager.getAbsolutePath(storage), e);
		}
	}

	public void doAfterDelete() {
		clearRelatedCaches();
	}

	public void doBeforeDelete() {
	}

	public abstract long getArchiveSize(File archive, boolean forceComputation) throws ApplicationException;

	public CompressionArguments getCompressionArguments() {
		return compressionArguments;
	}

	/**
	 * Builds the data directory associated to the archive file provided as argument. 
	 */
	public File getDataDirectory(File archive) {
		return new File(
				FileSystemManager.getParentFile(archive),
				FileSystemManager.getName(archive) + DATA_DIRECTORY_SUFFIX
		);
	}

	public String getDisplayArchivePath() {
		return this.fileSystemPolicy.getDisplayableParameters();
	}

	public EncryptionPolicy getEncryptionPolicy() {
		return encryptionPolicy;
	}

	public FileSystemPolicy getFileSystemPolicy() {
		return fileSystemPolicy;
	}

	public synchronized History getHistory() {
		if (this.history == null) {
			Logger.defaultLogger().info("No history found ... initializing data ...");
			try {
				// historique
				this.history = new DefaultHistory(
						new File(
								fileSystemPolicy.getArchiveDirectory(), 
								this.getHistoryName()
						)
				);
				Logger.defaultLogger().info("History loaded.");
			} catch (Throwable e) {
				Logger.defaultLogger().error("Error during history loading", e);
				this.history = null;
			}
		}
		return this.history;
	}

	public EntryArchiveData[] getHistory(String entry) throws ApplicationException {
		File[] archives = this.listArchives(null, null);
		ArrayList list = new ArrayList();

		for (int i=0; i<archives.length; i++) {
			list.add(getArchiveData(entry, archives[i]));
		}

		return processEntryArchiveData((EntryArchiveData[])list.toArray(new EntryArchiveData[0]));
	}

	public File getLastArchive() throws ApplicationException {
		return getLastArchive(null, null);
	}

	/**
	 * Retourne la derniere archive precedant une date donnee
	 */
	public abstract File getLastArchive(String backupScheme, GregorianCalendar date) throws ApplicationException;

	public String getManifestName() {
		return MANIFEST_FILE;
	}

	public void install() throws ApplicationException {
		super.install();

		File storageDir = fileSystemPolicy.getArchiveDirectory();
		FileSystemDriver baseDriver = this.buildBaseDriver(true);
		FileSystemDriver storageDriver = this.buildStorageDriver(storageDir);

		List listeners = new ArrayList();
		if (REPOSITORY_ACCESS_DEBUG) {
			listeners.add(new LoggerFileSystemDriverListener());
		}
		if (FILESTREAMS_DEBUG) {
			listeners.add(new OpenFileMonitorDriverListener());
		}

		try {
			try {
				baseDriver = EventFileSystemDriver.wrapDriver(baseDriver, REPOSITORY_ACCESS_DEBUG_ID, listeners);
				FileSystemManager.getInstance().registerDriver(storageDir.getParentFile(), baseDriver);
			} catch (Throwable e) {
				// Non-fatal error but DANGEROUS : It is highly advised to store archives in subdirectories - not at the root
				Logger.defaultLogger().warn("Error trying to register a driver at [" + storageDir + "]'s parent directory. It is probably because you tried to store archives at the root directory (/ or c:\\). It is HIGHLY advised to use subdirectories.", e, "Driver initialization");
			}

			storageDriver = EventFileSystemDriver.wrapDriver(storageDriver, REPOSITORY_ACCESS_DEBUG_ID, listeners);
			FileSystemManager.getInstance().registerDriver(storageDir, storageDriver);
		} catch (DriverAlreadySetException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		}
	}

	public boolean isPreBackupCheckUseful() {
		return true;
	}

	public abstract File[] listArchives(GregorianCalendar fromDate, GregorianCalendar toDate);

	public void setCompressionArguments(CompressionArguments compressionArguments) {
		this.compressionArguments = compressionArguments;
	}

	public void setEncryptionPolicy(EncryptionPolicy encryptionPolicy) {
		this.encryptionPolicy = encryptionPolicy;
	}

	public void setFileSystemPolicy(FileSystemPolicy fileSystemPolicy) {
		this.fileSystemPolicy = fileSystemPolicy;
		this.fileSystemPolicy.setMedium(this);
		checkFileSystemPolicy();
	}

	public void setTarget(AbstractRecoveryTarget target, boolean revalidate) {
        this.target = (FileSystemRecoveryTarget)target;
		if (revalidate) {
			fileSystemPolicy.synchronizeConfiguration();
		}
	}

	public String toString() {
		StringBuffer sb = ToStringHelper.init(this);
		ToStringHelper.append("FileSystemPolicy", this.fileSystemPolicy, sb);
		ToStringHelper.append("EncryptionPolicy", this.encryptionPolicy, sb);
		ToStringHelper.append("CompressionArguments", this.compressionArguments, sb);
		return ToStringHelper.close(sb);
	}

	protected FileSystemDriver buildBaseDriver(boolean main) throws ApplicationException {
		return this.fileSystemPolicy.initFileSystemDriver();
	}

	protected FileSystemDriver buildStorageDriver(File storageDir) throws ApplicationException {
		return this.encryptionPolicy.initFileSystemDriver(storageDir, this.buildBaseDriver(false));
	}

	protected abstract void checkFileSystemPolicy();

	/**
	 * Look for uncommitted archives and destroy them
	 * <BR>Also destroy temporary recovery locations
	 */
	protected void checkRepository() {
		File storageDir = fileSystemPolicy.getArchiveDirectory();

		// List all potential archive files
		File[] archives = FileSystemManager.listFiles(storageDir);

		if (archives != null) {
			for (int i=0; i<archives.length; i++) {
				File archive = archives[i];

				// If it has not been committed - destroy it  
				if (
						(matchArchiveName(archive) && (! isCommitted(archive)))
						|| (isWorkingDirectory(archive))
				) {          
					destroyTemporaryFile(archive);
				}
			}
		}
	}

	protected void clearRelatedCaches() {
		ArchiveManifestCache.getInstance().removeAllArchiveData(this);
	}

	protected File computeMarkerFile(File archive) {
		try {
			return computeMarkerFile(archive, false);
		} catch (IOException e) {
			// Never thrown
			Logger.defaultLogger().error(e);
			return null;
		}
	}

	protected File computeMarkerFile(File archive, boolean ensureParentDir) throws IOException {
		File dataDir = getDataDirectory(archive);
		if (ensureParentDir && ! FileSystemManager.exists(dataDir)) {
			FileTool.getInstance().createDir(dataDir);
		}
		return new File(dataDir, COMMIT_MARKER_NAME);
	}

	protected void copyAttributes(Object clone) {
		super.copyAttributes(clone);
		AbstractFileSystemMedium other = (AbstractFileSystemMedium)clone;
		other.encryptionPolicy = (EncryptionPolicy)this.encryptionPolicy.duplicate();
		other.setFileSystemPolicy((FileSystemPolicy)this.fileSystemPolicy.duplicate());
		other.compressionArguments = (CompressionArguments)this.compressionArguments.duplicate();
	}

	protected abstract void deleteArchive(File archive) throws IOException;

	protected void destroyTemporaryFile(File archive) {
		String name = FileSystemManager.isFile(archive) ? "file" : "directory";
		Logger.defaultLogger().warn("Uncommited " + name + " detected : " + FileSystemManager.getAbsolutePath(archive));

		Logger.defaultLogger().displayApplicationMessage(
				null, 
				"Temporary " + name + " detected.", 
				"Areca has detected that the following " + name + " is a working " + name + " or a temporary archive which has not been commited :" 
				+ "\n" + FileSystemManager.getAbsolutePath(archive) 
				+ "\n\nThis " + name + " will be deleted."
		);

		try {
			this.deleteArchive(archive);
		} catch (Exception e) {
			Logger.defaultLogger().error(e);
		}
	}

	/**
	 * Retourne le status de l'entree, dans l'archive specifiee.
	 * 
	 * @param entry
	 * @param archive
	 * @return
	 * @throws ApplicationException
	 */
	protected abstract EntryArchiveData getArchiveData(String entry, File archive) throws ApplicationException;

	protected abstract String getArchiveExtension();

	protected boolean isCommitted(File archive) {
		File marker = computeMarkerFile(archive);
		return FileSystemManager.exists(marker);
	}

	/**
	 * Introduced in Areca v6.2
	 * Tells whether the file is a working directory or not
	 */
	protected abstract boolean isWorkingDirectory(File f);

	protected void markCommitted(File archive) throws IOException {
		File marker = computeMarkerFile(archive, true);
		OutputStream out = FileSystemManager.getFileOutputStream(marker);
		out.write("c".getBytes());
		out.close();
	}

	/**
	 * Introduced in Areca v6.0
	 * <BR>Caution : will return 'false' for archives that have been built with previous versions of Areca
	 */
	protected abstract boolean matchArchiveName(File f);

	/**
	 * Stocke le fichier passe en argument dans l'archive
	 * (independemment des filtres, ou politique de stockage; il s'agit la d'une
	 * methode purement utilitaire; en pratique : zip ou repertoire) 
	 */
	protected abstract void storeFileInArchive(FileSystemRecoveryEntry entry, InputStream in, ProcessContext context) throws IOException, ApplicationException, TaskCancelledException;

	/**
	 * Creates a copy of the target's XML configuration and stores it in the main backup directory.
	 * <BR>This copy can be used later - in case of computer crash. 
	 */
	protected void storeTargetConfigBackup(ProcessContext context) throws ApplicationException {
		if (this.target.isCreateSecurityCopyOnBackup()) {
			File storageDir = FileSystemManager.getParentFile(context.getCurrentArchiveFile());
			boolean ok = false;

			if (storageDir != null && FileSystemManager.exists(storageDir)) {
				File rootDir = FileSystemManager.getParentFile(storageDir);
				if (rootDir != null && FileSystemManager.exists(rootDir)) {
					File targetFile = new File(
							rootDir,
							TARGET_BACKUP_FILE_PREFIX + this.target.getUid() + TARGET_BACKUP_FILE_SUFFIX
					);

					Logger.defaultLogger().info("Creating a XML backup copy of target \"" + this.target.getTargetName() + "\" on : " + FileSystemManager.getAbsolutePath(targetFile));
					TargetGroup process = new TargetGroup(targetFile);
					process.addTarget(this.target);
					process.setComments("This group contains a backup copy of your target : \"" + this.target.getTargetName() + "\". It can be used in case of computer crash.\nDo not modify it as is will be automatically updated during backup processes.");

					ProcessXMLWriter writer = new ProcessXMLWriter(true);
					writer.serializeProcess(process);

					ok = true;
				}
			}

			if (!ok) {
				Logger.defaultLogger().warn("Improper backup location : " + FileSystemManager.getAbsolutePath(context.getCurrentArchiveFile()) + " - Could not create an XML configuration backup");
			}
		} else {
			Logger.defaultLogger().warn("Configuration security copy has been disabled for this target. No XML configuration copy will be created !");
		}
	}
}
