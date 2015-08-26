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
import com.application.areca.AbstractTarget;
import com.application.areca.ApplicationException;
import com.application.areca.ArecaConfiguration;
import com.application.areca.ArecaFileConstants;
import com.application.areca.EntryArchiveData;
import com.application.areca.Errors;
import com.application.areca.TargetActions;
import com.application.areca.Utils;
import com.application.areca.adapters.ConfigurationHandler;
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
import com.application.areca.metadata.transaction.TransactionPoint;
import com.application.areca.metadata.transaction.TransactionPointHeader;
import com.application.areca.version.VersionInfos;
import com.myJava.file.CompressionArguments;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.driver.DriverAlreadySetException;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.file.driver.ThrottleHandler;
import com.myJava.file.driver.ThrottledFileSystemDriver;
import com.myJava.file.driver.event.EventFileSystemDriver;
import com.myJava.file.driver.event.LoggerFileSystemDriverListener;
import com.myJava.file.driver.event.OpenFileMonitorDriverListener;
import com.myJava.object.ToStringHelper;
import com.myJava.util.errors.ActionError;
import com.myJava.util.errors.ActionReport;
import com.myJava.util.history.HistoryHandler;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;
import com.myJava.util.taskmonitor.TaskMonitor;
import com.myJava.util.xml.AdapterException;

/**
 * <BR>
 * 
 * @author Olivier PETRUCCI <BR>
 *         
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
public abstract class AbstractFileSystemMedium 
extends AbstractMedium 
implements TargetActions, IndicatorTypes {

	private static final long DEFAULT_TRANSACTION_SIZE = ArecaConfiguration.get().getTransactionSize();
	public static final boolean CHECK_DIRECTORY_CONSISTENCY = ArecaConfiguration.get().isCheckRepositoryConsistency();
	private static final boolean REPOSITORY_ACCESS_DEBUG = ArecaConfiguration.get().isRepositoryAccessDebugMode();
	private static final boolean FILESTREAMS_DEBUG = ArecaConfiguration.get().isFileStreamsDebugMode();
	private static final String REPOSITORY_ACCESS_DEBUG_ID = VersionInfos.APP_SHORT_NAME + " repository access";

	/**
	 * Suffix added to the archive name to create the data directory (containing
	 * the manifest and trace)
	 */
	public static final String DATA_DIRECTORY_SUFFIX = "_data";

	/**
	 * Manifest name
	 */
	public static final String MANIFEST_FILE = "manifest";

	/**
	 * Name used for target configuration backup
	 */
	protected static final String TARGET_BACKUP_FILE_PREFIX = "/areca_config_backup";

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
	 * Compression arguments <BR>
	 * These arguments may be interpreted specifically depending on the medium
	 * type.
	 */
	protected CompressionArguments compressionArguments = new CompressionArguments();

	/**
	 * Tells whether many archives shall be created on just one single archive
	 */
	protected boolean image = false;
	
	/**
	 * If transactions are supported, size of the transactions, in kb
	 */
	protected long transactionSize = DEFAULT_TRANSACTION_SIZE;
	
	protected boolean useTransactions = true;
	
	protected ThrottleHandler throttleHandler;
	
	/**
	 * Maximum throughput, in kBytes per second
	 * <= 0 means "no limit"
	 */
	protected double maxThroughput = -1;

	public boolean isImage() {
		return this.image;
	}
	
	public void setImage(boolean imageBackups) {
		this.image = imageBackups;
	} 

	public boolean isUseTransactions() {
		return useTransactions;
	}

	public void setUseTransactions(boolean useTransactions) {
		this.useTransactions = useTransactions;
	}

	public String checkResumeSupported() {
		return null;
	}

	public long getTransactionSize() {
		return transactionSize;
	}

	public void setTransactionSize(long transactionSize) {
		this.transactionSize = transactionSize;
	}

	public double getMaxThroughput() {
		return maxThroughput;
	}

	public void setMaxThroughput(double maxThroughput) {
		this.maxThroughput = maxThroughput;
	}

	/**
	 * Checks that the archive provided as argument belongs to this medium
	 */
	public boolean checkArchiveCompatibility(File archive, boolean committedOnly) {
		if (archive == null) {
			return false;
		} else {
			String archivePath = FileSystemManager.getAbsolutePath(archive);

			return (!archivePath.endsWith(DATA_DIRECTORY_SUFFIX))
					&& ((! committedOnly) || isCommitted(archive));
		}
	}

	/**
	 * Save a temporary transaction point
	 */
	public void handleTransactionPoint(ProcessContext context) throws ApplicationException {
		if (
				this.checkResumeSupported() == null
				&& (context.getOutputBytesInKB() - context.getTransactionBound()) >= getTransactionSize()
		) {
			context.setTransactionBound(context.getOutputBytesInKB());
			initTransactionPoint(context);
		}
	}

	/**
	 * Checks "stupid" configurations .... typically, checks that the user
	 * didn't use the target's storage subdirectory as main storage directory.
	 */
	public boolean checkStupidConfigurations() {
		try {
			String path = this.fileSystemPolicy.getArchivePath();
			path = path.replace('\\', ' ').replace('/', ' ').trim();
			if (path.endsWith(target.getUid() + " " + target.getUid())) {
				Logger.defaultLogger().warn(
						"The main storage directory of '" + target.getName()
								+ "' seems suspect ("
								+ fileSystemPolicy.getArchivePath()
								+ "). Please check your target configuration.");
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			Logger.defaultLogger().error(e);
			return true;
		}
	}

	/**
	 * Check the medium's business rules before starting the action passed as
	 * argument
	 */
	public ActionReport checkMediumState(int action) {
		ActionReport result = new ActionReport();

		if (encryptionPolicy == null) {
			result.addError(new ActionError(
					Errors.ERR_C_MEDIUM_ENCRYPTION_NOT_INITIALIZED,
					Errors.ERR_M_MEDIUM_ENCRYPTION_NOT_INITIALIZED));
		}

		if (action != ACTION_DESCRIBE) {
			Iterator iter = ((FileSystemTarget) this.getTarget()).getSources().iterator();
			while (iter.hasNext()) {
				File src = (File) iter.next();
				if (CHECK_DIRECTORY_CONSISTENCY
						&& (!this.fileSystemPolicy.canHandle(src))) {
					result.addError(new ActionError(Errors.ERR_C_INCLUSION,
							Errors.ERR_M_INCLUSION));
				}
			}
		}

		return result;
	}

	public void preCommitMerge(ProcessContext context) throws ApplicationException {
		clearRelatedCaches();
	}

	public IndicatorMap computeIndicators() throws ApplicationException {
		ensureInstalled();
		
		Logger.defaultLogger().info("Computing statistics ...");
		try {
			IndicatorMap indicators = new IndicatorMap();
			File[] archives = this.listArchives(null, null, true);

			// Archives' physical size - APS
			long apsValue = 0;
			for (int i = 0; i < archives.length; i++) {
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
				File trcFile = ArchiveTraceManager.resolveTraceFileForArchive(
						(AbstractIncrementalFileSystemMedium) this, archive);
				TraceFileIterator iter = ArchiveTraceAdapter
						.buildIterator(trcFile);
				TraceEntry entry;
				long sfsValue = 0;
				long nofValue = 0;
				try {
					while (iter.hasNext()) {
						entry = iter.next();
						if (entry.getType() == MetadataConstants.T_FILE) {
							sfsValue += ArchiveTraceParser
									.extractFileSizeFromTrace(entry.getData());
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
				for (int i = archives.length - 1; i >= 0; i--) {
					Manifest mf = ArchiveManifestCache.getInstance().getManifest(this, archives[i]);
					if (
							this.isImage()
							|| (
								mf != null 
								&& AbstractTarget.BACKUP_SCHEME_FULL.equals(mf.getStringProperty(ManifestKeys.OPTION_BACKUP_SCHEME))
							)
					) {
						latestFullArchive = archives[i];
						break;
					}
				}

				if (latestFullArchive != null) {
					trcFile = ArchiveTraceManager.resolveTraceFileForArchive(
							(AbstractIncrementalFileSystemMedium) this,
							latestFullArchive);
					iter = ArchiveTraceAdapter.buildIterator(trcFile);
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
					psrValue = 100 * (double) psValue / (double) lsValue;
					psrStr = Utils.formatLong((long) psrValue) + " %";
				}

				Indicator psr = new Indicator();
				psr.setId(T_PSR);
				psr.setName(N_PSR);
				psr.setStringValue(psrStr);
				psr.setValue(psrValue);
				indicators.addIndicator(psr);

				if (! isImage()) {
					// Size without history - SWH
					long swhValue = Math.min((long) (psrValue * (double) sfsValue / 100.), apsValue);
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
					
					// Number of archives - NOA
					long noaValue = archives.length;
					Indicator noa = new Indicator();
					noa.setId(T_NOA);
					noa.setName(N_NOA);
					noa.setStringValue(Utils.formatLong(noaValue));
					noa.setValue(noaValue);
					indicators.addIndicator(noa);
				}
			}

			return indicators;
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException(e);
		}
	}

	public void deleteArchives(GregorianCalendar fromDate, ProcessContext context) throws ApplicationException {
		this.ensureInstalled();
		this.checkRepository();

		Logger.defaultLogger().info(
				"Starting deletion from " + Utils.formatDisplayDate(fromDate)
						+ ".");

		if (fromDate != null) {
			fromDate = (GregorianCalendar) fromDate.clone();
			fromDate.add(GregorianCalendar.MILLISECOND, -1);
		}

		File[] archives = this.listArchives(fromDate, null, true);

		for (int i = 0; i < archives.length; i++) {
			try {
				context.getInfoChannel().updateCurrentTask(
						i + 1,
						archives.length,
						"Deleting " + FileSystemManager.getName(archives[i])
								+ " ...");
				deleteArchive(archives[i]);
				context.getTaskMonitor().getCurrentActiveSubTask()
						.setCurrentCompletion(i + 1, archives.length + 1);
			} catch (Exception e) {
				throw new ApplicationException(e);
			}
		}

		try {
			FileSystemManager.getInstance().flush(
					fileSystemPolicy.getArchiveDirectory());
		} catch (IOException e) {
			throw new ApplicationException(e);
		}

		Logger.defaultLogger().info(
				"Deletion completed - " + archives.length + " archive"
						+ (archives.length > 1 ? "s" : "") + " deleted.");

		context.getTaskMonitor().getCurrentActiveSubTask().enforceCompletion();
	}

	public void destroyRepository() throws ApplicationException {
		this.ensureInstalled();
		File storage = fileSystemPolicy.getArchiveDirectory();
		try {
			// Delete repository
			Logger.defaultLogger().info("Deleting repository : " + FileSystemManager.getDisplayPath(storage) + " ...");
			FileTool.getInstance().delete(storage);
			Logger.defaultLogger().info(FileSystemManager.getDisplayPath(storage) + " deleted.");
			
			// Delete configuration backup
			File configBackup = computeConfigurationBackupFile();
			Logger.defaultLogger().info("Deleting configuration backup file : " + FileSystemManager.getDisplayPath(configBackup) + " ...");
			FileTool.getInstance().delete(configBackup);
		} catch (Exception e) {
			throw new ApplicationException("Error trying to delete directory : " + FileSystemManager.getDisplayPath(storage), e);
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
	 * Builds the data directory associated to the archive file provided as
	 * argument.
	 */
	public static File getDataDirectory(File archive) {
		return new File(FileSystemManager.getParentFile(archive), FileSystemManager.getName(archive) + DATA_DIRECTORY_SUFFIX);
	}

	public EncryptionPolicy getEncryptionPolicy() {
		return encryptionPolicy;
	}

	public FileSystemPolicy getFileSystemPolicy() {
		return fileSystemPolicy;
	}

	public synchronized HistoryHandler getHistoryHandler() {
		if (this.historyHandler == null) {
			File historyFile = new File(fileSystemPolicy.getArchiveDirectory(), this.getHistoryName());
			this.historyHandler = new HistoryHandler(historyFile);
		}

		return this.historyHandler;
	}

	public EntryArchiveData[] getHistory(String entry) throws ApplicationException {
		File[] archives = this.listArchives(null, null, true);
		ArrayList list = new ArrayList();

		for (int i = 0; i < archives.length; i++) {
			list.add(getArchiveData(entry, archives[i]));
		}

		return processEntryArchiveData((EntryArchiveData[]) list.toArray(new EntryArchiveData[0]));
	}

	public File getLastArchive() throws ApplicationException {
		return getLastArchive(null, null);
	}

	/**
	 * Return the last archive before the date passed as argument
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
				FileSystemManager.getInstance().registerDriver( storageDir.getParentFile(), baseDriver);
			} catch (Throwable e) {
				// Non-fatal error but DANGEROUS : It is highly advised to store
				// archives in subdirectories - not at the root
				Logger.defaultLogger()
						.warn("Error trying to register a driver at ["
								+ storageDir
								+ "]'s parent directory. It is probably because you tried to store archives at the root directory (/ or c:\\). It is HIGHLY advised to use subdirectories.",
								e, "Driver initialization");
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

	public abstract File[] listArchives(GregorianCalendar fromDate, GregorianCalendar toDate, boolean committedOnly) throws ApplicationException;

	public void setCompressionArguments(CompressionArguments compressionArguments) {
		this.compressionArguments = compressionArguments;
	}

	public void setEncryptionPolicy(EncryptionPolicy encryptionPolicy) {
		this.encryptionPolicy = encryptionPolicy;
	}

	/**
	 * Associates a fully initialized policy to the medium
	 * @param fileSystemPolicy
	 */
	public void setFileSystemPolicy(FileSystemPolicy fileSystemPolicy) {
		this.fileSystemPolicy = fileSystemPolicy;
		this.fileSystemPolicy.setMedium(this);
		checkFileSystemPolicy();
	}

	public void setTarget(AbstractTarget target, boolean revalidate) {
		this.target = (FileSystemTarget) target;
		if (revalidate) {
			fileSystemPolicy.synchronizeConfiguration();
		}
	}

	public String toString() {
		StringBuffer sb = ToStringHelper.init(this);
		ToStringHelper.append("FileSystemPolicy", this.fileSystemPolicy, sb);
		ToStringHelper.append("EncryptionPolicy", this.encryptionPolicy, sb);
		ToStringHelper.append("CompressionArguments", this.compressionArguments, sb);
		ToStringHelper.append("Image", this.image, sb);
		ToStringHelper.append("MaxThroughput", this.maxThroughput, sb);
		ToStringHelper.append("Transaction Size", this.transactionSize, sb);
		return ToStringHelper.close(sb);
	}

	protected FileSystemDriver buildBaseDriver(boolean main) throws ApplicationException {
		FileSystemDriver ret = this.fileSystemPolicy.initFileSystemDriver();
		if (this.maxThroughput > 0) {
			ThrottledFileSystemDriver th = new ThrottledFileSystemDriver(ret, maxThroughput);
			this.throttleHandler = th.getThrottleHandler();
			ret = th;
		}
		return ret;
	}

	protected FileSystemDriver buildStorageDriver(File storageDir) throws ApplicationException {
		return this.encryptionPolicy.initFileSystemDriver(storageDir, this.buildBaseDriver(false));
	}

	protected abstract void checkFileSystemPolicy();

	/**
	 * Look for uncommitted archives and destroy them <BR>
	 * Also destroy temporary recovery locations
	 */
	protected void checkRepository() {
		File storageDir = fileSystemPolicy.getArchiveDirectory();

		// List all potential archive files
		String[] archiveNames = FileSystemManager.list(storageDir);
		if (archiveNames != null) {
			for (int i = 0; i < archiveNames.length; i++) {
				// If it has not been committed - destroy it
				File archive = new File(storageDir, archiveNames[i]);
				if ((matchArchiveName(archive) && (!isCommitted(archive))) || (isWorkingDirectory(archive))) {
					destroyTemporaryFile(archive);
				}
			}
		}
	}

	/**
	 * Search for a valid transaction point
	 */
	public TransactionPoint getLastTransactionPoint(String backupScheme) throws ApplicationException {
		this.ensureInstalled();
		
		try {
			// Retrieve the last committed archive
			File lastArchive = this.getLastArchive();
			Manifest lastManifest = ArchiveManifestCache.getInstance()
					.getManifest(this, lastArchive);

			// List all non-committed archive files
			File storageDir = fileSystemPolicy.getArchiveDirectory();
			String[] archiveNames = FileSystemManager.list(storageDir);
			
			TransactionPoint candidate = null;

			if (archiveNames != null) {
				for (int i = 0; i < archiveNames.length; i++) {
					File archive = new File(storageDir, archiveNames[i]);
					
					if ((!isWorkingDirectory(archive))
							&& (matchArchiveName(archive))
							&& (!isCommitted(archive))) {
						TransactionPoint tp = TransactionPoint.findLastTransactionPoint(getDataDirectory(archive));

						if (tp != null) {
							TransactionPointHeader header = tp.readHeader();

							if (header != null) {
								GregorianCalendar tpDate = header.getDate();
								Logger.defaultLogger()
										.fine("Transaction data found : "
												+ tp.getPath()
												+ " - "
												+ Utils.formatDisplayDate(tpDate));

								// Check that the transaction point has been
								// created after the last committed archive
								if (lastManifest == null || tpDate.after(lastManifest.getDate())) {

									// Check that the global source root hasn't
									// changed since the transaction point
									if (header.getSourcesRoot().equalsIgnoreCase(target.getSourcesRoot())) {
										
										// Check backup scheme
										if (header.getBackupScheme().equals(backupScheme)) {
											
											// Check that the transaction point is
											// younger than the current candidate
											if (candidate == null || tpDate.after(candidate.readHeader().getDate())) {
												candidate = tp;
												Logger.defaultLogger().fine("Transaction data registered.");
											} else {
												Logger.defaultLogger().fine("Transaction data ignored.");
											}
										} else {
											Logger.defaultLogger().fine("Transaction data rejected : incompatible backup scheme ("
													+ header.getBackupScheme()
													+ ")");
										}

									} else {
										Logger.defaultLogger().fine("Transaction data rejected : incompatible source root ("
														+ header.getSourcesRoot()
														+ ")");
									}
								} else {
									Logger.defaultLogger().fine(
											"Transaction data rejected : older than last archive ("+ lastArchive + ")");
								}
							} else {
								Logger.defaultLogger().fine("Transaction data rejected : no header file.");
							}
						}
					}
				}
			}

			return candidate;
		} catch (AdapterException e) {
			Logger.defaultLogger().error("Error reading " + e.getSource());
			throw new ApplicationException(e);
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

	protected static File computeMarkerFile(File archive,
			boolean ensureParentDir) throws IOException {
		File dataDir = getDataDirectory(archive);
		if (ensureParentDir && !FileSystemManager.exists(dataDir)) {
			FileTool.getInstance().createDir(dataDir);
		}
		return new File(dataDir, ArecaFileConstants.COMMIT_MARKER_NAME);
	}

	protected void copyAttributes(Object clone) {
		super.copyAttributes(clone);
		AbstractFileSystemMedium other = (AbstractFileSystemMedium) clone;
		other.encryptionPolicy = (EncryptionPolicy) this.encryptionPolicy.duplicate();
		other.setFileSystemPolicy((FileSystemPolicy) this.fileSystemPolicy.duplicate());
		other.compressionArguments = (CompressionArguments) this.compressionArguments.duplicate();
		other.image = this.image;
		other.transactionSize = this.transactionSize;
		other.useTransactions = this.useTransactions;
		other.maxThroughput = this.maxThroughput;
	}

	protected void destroyTemporaryFile(File archive) {
		String name = FileSystemManager.isFile(archive) ? "file" : "directory";
		Logger.defaultLogger().warn("Uncommited " + name + " detected : " + FileSystemManager.getDisplayPath(archive));

		Logger.defaultLogger().displayApplicationMessage(
						null,
						"Temporary " + name + " detected.",
						""
								+ VersionInfos.APP_SHORT_NAME
								+ " has detected that the following "
								+ name
								+ " is a working "
								+ name
								+ " or a temporary archive which has not been commited :"
								+ "\n"
								+ FileSystemManager.getDisplayPath(archive)
								+ "\n\nThis " + name + " will be deleted.");

		try {
			Logger.defaultLogger().info("Deleting temporary " + name + " (" + FileSystemManager.getDisplayPath(archive) + ") ...");
			this.deleteArchive(archive);
			Logger.defaultLogger().info("Temporary " + name + " deleted.");
		} catch (Exception e) {
			Logger.defaultLogger().error(e);
		}
	}

	/**
	 * Return the status of the entry passed as argument in the archive
	 */
	protected abstract EntryArchiveData getArchiveData(String entry, File archive) throws ApplicationException;

	protected abstract String getArchiveExtension();

	protected boolean isCommitted(File archive) {
		File marker = computeMarkerFile(archive);
		return FileSystemManager.exists(marker);
	}

	/**
	 * Introduced in Areca v6.2 Tells whether the file is a working directory or
	 * not
	 */
	protected abstract boolean isWorkingDirectory(File f);

	protected static void markCommitted(File archive) throws IOException {
		File marker = computeMarkerFile(archive, true);
		OutputStream out = FileSystemManager.getFileOutputStream(marker);
		out.write("c".getBytes());
		out.close();
	}
	
	protected static void removeMarkerFile(File archive) throws IOException {
		File marker = computeMarkerFile(archive, false);
		FileTool.getInstance().delete(marker);
	}

	/**
	 * Introduced in Areca v6.0 <BR>
	 * Caution : will return 'false' for archives that have been built with
	 * previous versions of Areca
	 */
	protected abstract boolean matchArchiveName(File f);

	/**
	 * Store the file in the archive referenced by the context
	 */
	protected abstract void storeFileInArchive(FileSystemRecoveryEntry entry, InputStream in, ProcessContext context) throws IOException, ApplicationException, TaskCancelledException;

	private File computeConfigurationBackupDirectory() {
		File storageDir = this.fileSystemPolicy.getArchiveDirectory();
		File rootDir = FileSystemManager.getParentFile(storageDir);
		return new File(rootDir,TARGET_BACKUP_FILE_PREFIX);
	}
	
	private File computeConfigurationBackupFile() {
		return target.computeConfigurationFile(computeConfigurationBackupDirectory(), false);
	}

	/**
	 * Create a copy of the target's XML configuration and stores it in the main
	 * backup directory. <BR>
	 * This copy can be used later - in case of computer crash.
	 */
	protected void storeTargetConfigBackup(ProcessContext context) throws ApplicationException {
		this.ensureInstalled();
		
		if (this.target.isCreateSecurityCopyOnBackup()) {
			File dir = computeConfigurationBackupDirectory();
			Logger.defaultLogger().info("Creating a XML backup copy of target \""+ this.target.getName() + "\" on : " + FileSystemManager.getDisplayPath(dir));
					
			if (! ConfigurationHandler.getInstance().serialize(this.target, dir, true, true)) {
				Logger.defaultLogger().warn("Could not create XML configuration backup for " + FileSystemManager.getDisplayPath(context.getCurrentArchiveFile()) + ". It is HIGHLY advisable to create a backup copy of your configuration !");
			}
		} else {
			Logger.defaultLogger().warn("Configuration security copy has been disabled for this target. No XML configuration copy will be created. It is HIGHLY advisable to create a backup copy of your configuration !");
		}
	}
	
	protected void initializeThrottling(TaskMonitor monitor) {
		if (this.throttleHandler != null) {
			throttleHandler.initializeTimer(monitor);
		}
	}
}
