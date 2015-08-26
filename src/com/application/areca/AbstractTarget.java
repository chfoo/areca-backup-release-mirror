package com.application.areca;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.application.areca.context.ProcessContext;
import com.application.areca.context.StatusList;
import com.application.areca.filter.ArchiveFilter;
import com.application.areca.filter.FilterGroup;
import com.application.areca.impl.AbstractFileSystemMedium;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.impl.FileSystemRecoveryEntry;
import com.application.areca.impl.copypolicy.AbstractCopyPolicy;
import com.application.areca.indicator.IndicatorMap;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.manifest.ManifestKeys;
import com.application.areca.metadata.manifest.ManifestManager;
import com.application.areca.metadata.transaction.TransactionPoint;
import com.application.areca.plugins.ConfigurationAddon;
import com.application.areca.processor.Processor;
import com.application.areca.processor.ProcessorList;
import com.application.areca.search.SearchCriteria;
import com.application.areca.search.TargetSearchResult;
import com.application.areca.version.VersionInfos;
import com.myJava.file.FileSystemManager;
import com.myJava.object.Duplicable;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.system.OSTool;
import com.myJava.util.CalendarUtils;
import com.myJava.util.errors.ActionReport;
import com.myJava.util.history.HistoryEntry;
import com.myJava.util.history.HistoryHandler;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
 * Abstract base implementation for recovery targets.
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
public abstract class AbstractTarget 
extends AbstractWorkspaceItem
implements HistoryEntryTypes, Duplicable, TargetActions {
	public static final String BACKUP_SCHEME_FULL = "Full backup";
	public static final String BACKUP_SCHEME_INCREMENTAL = "Incremental backup";
	public static final String BACKUP_SCHEME_DIFFERENTIAL = "Differential backup";
	public static final String CONFIG_FILE_EXT_DEPRECATED= ".xml";
	public static final String CONFIG_FILE_EXT = ".bcfg";

	protected ArchiveMedium medium;
	protected FilterGroup filterGroup = new FilterGroup();
	protected int id = -1; // DEPRECATED - Numeric unique id of the target within its process
	protected String uid; // Unique identifier
	protected String targetName; // Name of the target
	protected String comments;
	protected ProcessorList postProcessors = new ProcessorList();
	protected ProcessorList preProcessors = new ProcessorList();
	protected boolean running;
	protected boolean createSecurityCopyOnBackup = true;
	protected Map configurationAddons = new HashMap();

	protected void copyAttributes(Object clone) {
		AbstractTarget other = (AbstractTarget)clone;
		other.parent = parent;
		other.uid = generateNewUID();
		other.targetName = "Copy of " + targetName;
		other.comments = comments;
		other.filterGroup = (FilterGroup)this.filterGroup.duplicate();
		other.postProcessors = (ProcessorList)postProcessors.duplicate();
		other.preProcessors = (ProcessorList)preProcessors.duplicate();
		other.setMedium((ArchiveMedium)medium.duplicate(), true);
		other.createSecurityCopyOnBackup = this.createSecurityCopyOnBackup;
	}

	public boolean hasDeepTargets() {
		return true;
	}

	public SupportedBackupTypes getSupportedBackupSchemes() {
		SupportedBackupTypes ret = new SupportedBackupTypes();

		if (medium.supportsBackupScheme(AbstractTarget.BACKUP_SCHEME_INCREMENTAL)) {
			ret.setSupported(AbstractTarget.BACKUP_SCHEME_INCREMENTAL);
		}
		if (medium.supportsBackupScheme(AbstractTarget.BACKUP_SCHEME_DIFFERENTIAL)) {
			ret.setSupported(AbstractTarget.BACKUP_SCHEME_DIFFERENTIAL);
		}
		if (medium.supportsBackupScheme(AbstractTarget.BACKUP_SCHEME_FULL)) {
			ret.setSupported(AbstractTarget.BACKUP_SCHEME_FULL);
		}

		return ret;
	}

	public ProcessorList getPostProcessors() {
		return postProcessors;
	}

	public ProcessorList getPreProcessors() {
		return preProcessors;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isCreateSecurityCopyOnBackup() {
		return createSecurityCopyOnBackup;
	}
	
	public Iterator getAddons() {
		return configurationAddons.values().iterator();
	}

	public ConfigurationAddon getAddonById(String id) {
		return (ConfigurationAddon)configurationAddons.get(id);
	}
	
	public void registerAddon(ConfigurationAddon addon) {
		this.configurationAddons.put(addon.getId(), addon);
	}
	
	public void setCreateSecurityCopyOnBackup(boolean createSecurityCopyOnBackup) {
		this.createSecurityCopyOnBackup = createSecurityCopyOnBackup;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public FilterGroup getFilterGroup() {
		return filterGroup;
	}

	public void setFilterGroup(FilterGroup filterGroup) {
		this.filterGroup = filterGroup;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public void destroyRepository() throws ApplicationException {
		this.medium.destroyRepository();
	}

	public String getUid() {
		if (this.uid == null) {
			Logger.defaultLogger().info("No UID was specified for target '" + targetName + "'. Creating a random ID");
			this.uid = generateNewUID();
		}      

		return uid;
	}

	public File computeConfigurationFile(File root) {
		return computeConfigurationFile(root, true);
	}

	public File computeConfigurationFile(File root, boolean appendAncestors) {
		File directory = appendAncestors ? parent.computeConfigurationFile(root) : root;
		return new File(directory, computeConfigurationFileName());
	}

	public String computeConfigurationFileName() {
		return this.getUid() + CONFIG_FILE_EXT;
	}

	public String getName() {
		return targetName;
	}

	public void setTargetName(String taskName) {
		this.targetName = taskName;
	}

	public String getDescription() {
		String ancestors = parent == null ? "" : this.parent.getFullPath();
		StringBuffer buf = new StringBuffer();
		buf.append(ancestors);
		if (ancestors.length() != 0) {
			buf.append("/");
		}
		buf.append(this.uid).append(" :");
		if (id > 0) {
			buf.append("\n\tLocal id : ");
			buf.append(id);
		}
		buf.append("\n\tName : ");
		buf.append(getName());
		buf.append("\n\t");
		buf.append(this.getSpecificTargetDescription());
		buf.append("\n\tStorage : ");
		buf.append(medium.getDescription());
		return new String(buf);
	}

	protected abstract String getSpecificTargetDescription();

	/**
	 * Check the system state before critical operations (merges, deletions, ...)
	 */
	public ActionReport checkTargetState(int action) {
		return this.medium.checkMediumState(action);
	}

	public void validateTargetState(int action, ProcessContext context) throws ApplicationException {
		ActionReport report = checkTargetState(action);
		if (! report.isDataValid()) {
			ApplicationException ex = new ApplicationException(report);
			Logger.defaultLogger().error("Incoherent state detected for target " + getName() + " : " + ex.getMessage());
			throw ex;
		}  
	}

	public String getComments() {
		return comments;
	}

	public File createDebuggingData(File directory) throws ApplicationException, TaskCancelledException {
		return medium.createDebuggingData(directory);
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	/**
	 * @return Returns the medium.
	 */
	public ArchiveMedium getMedium() {
		return medium;
	}

	public TargetSearchResult search(SearchCriteria criteria) throws ApplicationException, TaskCancelledException {
		return this.medium.search(criteria);
	}

	/**
	 * @param medium The medium to set.
	 */
	public void setMedium(ArchiveMedium medium, boolean revalidateMedium) {
		this.medium = medium;
		this.medium.setTarget(this, revalidateMedium);
		this.medium.checkStupidConfigurations();
	}

	public void addFilter(ArchiveFilter filter) {
		this.filterGroup.addFilter(filter);
	}

	public Iterator getFilterIterator() {
		return this.filterGroup.getFilterIterator();
	} 

	/**
	 * Open and lock the target
	 */
	protected void open(TransactionPoint transactionPoint, ProcessContext context) throws ApplicationException {
		if (checkResumeSupported() != null) {
			throw new ApplicationException("Transaction points are not supported by this target configuration.");
		} else {
			Iterator iter = this.getAddons();
			while (iter.hasNext()) {
				ConfigurationAddon addon = (ConfigurationAddon)iter.next();
				addon.open(transactionPoint, context);
			}
			
			medium.open(null, transactionPoint, context);
		}
	}  

	/**
	 * Open and lock the target
	 */
	protected void open(Manifest manifest, ProcessContext context, String backupScheme) throws ApplicationException {
		context.setBackupScheme(backupScheme);
		
		Iterator iter = this.getAddons();
		while (iter.hasNext()) {
			ConfigurationAddon addon = (ConfigurationAddon)iter.next();
			addon.open(manifest, context, backupScheme);
		}
		
		medium.open(manifest, null, context);
		String resumeSupportedResponse = this.checkResumeSupported();

		if (resumeSupportedResponse == null) {
			// Transaction points supported
			medium.initTransactionPoint(context);
		} else {
			Logger.defaultLogger().info("Intermediate transaction points not compatible with current target configuration (" + resumeSupportedResponse + "). No intermediate transaction point will be created during the backup.");
		}
	}  

	public String checkResumeSupported() {
		return medium.checkResumeSupported();
	}

	public synchronized void processBackup(
			Manifest manifest, 
			String backupScheme,
			CheckParameters checkParams,
			TransactionPoint transactionPoint,
			ProcessContext context
			) throws ApplicationException {
		try {
			this.validateTargetState(ACTION_BACKUP, context);

			runPreProcessors(Processor.ACTION_BACKUP, 0.1, transactionPoint == null, context);

			try {
				context.getInfoChannel().print("Backup in progress ...");
				context.getTaskMonitor().checkTaskState();

				// Start the backup
				context.reset(false);

				double remaining = 
						1.0 
						- (this.postProcessors.isEmpty(Processor.ACTION_BACKUP) ? 0 : 0.1) 
						- (this.preProcessors.isEmpty(Processor.ACTION_BACKUP) ? 0 : 0.1)
						- (checkParams.isCheck() ? 0.3 : 0);

				// Open the storage medium
				if (transactionPoint == null) {
					// Create main task monitor
					context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(remaining, "backup-main");

					context.getReport().startDataFlowTimer();
					if (manifest == null) {
						manifest = new Manifest(Manifest.TYPE_BACKUP);
					}

					this.open(manifest, context, backupScheme);

					this.medium.getHistoryHandler().addEntryAndFlush(new HistoryEntry(HISTO_BACKUP, "Backup."));
				} else {
					this.open(transactionPoint, context);

					// Following line removed on 2012-11-10 : not needed because the filter is serialized with the ProcessContext; 
					// Further modifications performed on target filter won't be taken into account.
					// This is necessary for compatibility with potential modifications applied to the filter by the ConfigurationAddons.
					//context.getFileSystemIterator().setFilter(this.filterGroup);
					context.getTaskMonitor().setCurrentSubTask(context.getFileSystemIterator().getMonitor(), remaining);

					this.medium.getHistoryHandler().addEntryAndFlush(new HistoryEntry(HISTO_RESUME, "Resume backup."));
				}
				context.setChecked(checkParams.isCheck());

				RecoveryEntry entry = new FileSystemRecoveryEntry();
				while (this.nextElement(context, entry)) {
					context.getInfoChannel().getTaskMonitor().checkTaskState();
					if (this.filterEntryBeforeStore(entry)) {
						try {
							medium.handleTransactionPoint(context);

							context.incrementEntryIndex();
							context.getInfoChannel().updateCurrentTask(context.getEntryIndex(), 0, entry.toString());
							this.medium.store(entry, context);
						} catch (StoreException e) {
							throw new ApplicationException(e);
						}
					} 
				}
				if (
						(context.getReport().getDeletedFiles() == 0 || ArecaConfiguration.get().isPreventEmptyArchives())
						&& context.getReport().getSavedFiles() == 0 
						&& ! ((AbstractIncrementalFileSystemMedium)medium).isImage()
				) {
					cancelBackup(context);
				} else {
					this.commitBackup(context);
					context.getReport().setWrittenKBytes(context.getOutputBytesInKB());
					context.getReport().stopDataFlowTimer();
					Logger.defaultLogger().info(Utils.formatLong(context.getInputBytesInKB()) + " kb read in " + Utils.formatLong(context.getReport().getDataFlowTimeInSecond()) + " seconds.");                
					Logger.defaultLogger().info("Average data input : " + Utils.formatLong(context.getInputBytesInKBPerSecond()) + " kb/second.");
					Logger.defaultLogger().info(Utils.formatLong(context.getReport().getWrittenKBytes()) + " kb written in " + Utils.formatLong(context.getReport().getDataFlowTimeInSecond()) + " seconds.");                
					Logger.defaultLogger().info("Average data output : " + Utils.formatLong(context.getOutputBytesInKBPerSecond()) + " kb/second.");
				}
			} catch (Throwable e) {
				if (! TaskCancelledException.isTaskCancellation(e)) {
					Logger.defaultLogger().error(e);
				}
				this.rollbackBackup(context, e.getMessage());
				throw wrapException(e);
			}
			//}
		} finally {
			Exception checkException = null;
			if ((! context.getReport().hasError()) && checkParams.isCheck() && context.getCurrentArchiveFile() != null && context.getReport().getSavedFiles() != 0) {
				context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.3, "archive check");
				TaskMonitor checkMon = context.getTaskMonitor().getCurrentActiveSubTask();

				try {
					// Get the date)
					Manifest mf = ManifestManager.readManifestForArchive((AbstractFileSystemMedium)this.medium, context.getCurrentArchiveFile());
					GregorianCalendar cal = mf.getDate();

					// Check the archive
					context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.6, "effective check");
					try {
						this.processArchiveCheck(checkParams, cal, null, false, context);
					} catch (Exception e) {
						Logger.defaultLogger().error("An error has been caught : ", e);
						checkException = e;
					}
					if (checkException != null || context.getReport().hasRecoveryIssues()) {
						String excMsg = "";
						if (checkException != null) {
							excMsg = " (got the following error : " + checkException.getMessage() + ")";
						}
						context.getInfoChannel().error("The created archive - " + FileSystemManager.getDisplayPath(context.getCurrentArchiveFile()) + " - was not successfully checked" + excMsg + ". It will be deleted.");
						context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.4, "deletion");  
						this.processDeleteArchives(cal, context);
					}
				} finally {
					checkMon.enforceCompletion();
				}
			}

			if (context.getReport() != null) {
				context.getReport().setStopMillis();
			}

			runPostProcessors(Processor.ACTION_BACKUP, 0.1, true, context);
			context.getInfoChannel().print("Backup completed."); 

			if (checkException != null) {
				throw wrapException(checkException);
			}
		}

		//Logger.defaultLogger().fine(Chronometer.instance().toString());
	}

	/**
	 * Launch a simulation process.
	 */
	public synchronized SimulationResult processSimulate(ProcessContext context) throws ApplicationException {
		try {
			validateTargetState(ACTION_SIMULATE, context);
			context.getInfoChannel().print("Simulation in progress ...");
			return this.processSimulateImpl(context, true);
		} finally {
			context.getInfoChannel().print("Simulation completed.");            
		}
	}    

	/**
	 * Launch a simulation process.
	 */
	public synchronized SimulationResult processSimulateImpl(ProcessContext context, boolean returnDetailedResult) throws ApplicationException {
		try {  
			TaskMonitor simulationGlobalMonitor = context.getInfoChannel().getTaskMonitor().getCurrentActiveSubTask();

			SimulationResult entries = new SimulationResult();
			FileSystemRecoveryEntry entry = new FileSystemRecoveryEntry();
			
			long index = 0;
			while (this.nextElement(context, entry)) {
				context.getTaskMonitor().checkTaskState();
				if (this.filterEntryBeforeStore(entry)) {
					index++;
					context.getInfoChannel().updateCurrentTask(index, 0, entry.toString());
					this.medium.simulateEntryProcessing(entry, !returnDetailedResult, context);
					if (entry.getStatus() != EntryStatus.STATUS_NOT_STORED) {
						if (entry.getStatus() == EntryStatus.STATUS_DELETED) {
							context.getReport().addDeletedFile();
						} else {
							context.getReport().addSavedFile();
						}

						if (returnDetailedResult) {
							entries.addEntry((FileSystemRecoveryEntry)entry.duplicate());
						} else {
							// Once we get a stored entry in "not detailed" mode, stop this method --> We know that it will be necessary to make a backup
							simulationGlobalMonitor.enforceCompletion();
							break;
						}
					} else {
						context.getReport().addIgnoredFile();
					}
				}
			}
			context.getTaskMonitor().checkTaskState();
			medium.closeSimulation(context); 

			context.getReport().getStatus().addItem(StatusList.KEY_SIMULATE);

			return entries;
		} catch (Throwable e) {
			context.getReport().getStatus().addItem(StatusList.KEY_SIMULATE, e.getMessage());
			throw wrapException(e);
		} finally {
			if (context != null) {
				context.getReport().setStopMillis();
			}
		}
	}    

	public static void addBasicInformationsToManifest(Manifest mf) {
		mf.addProperty(ManifestKeys.VERSION, VersionInfos.getLastVersion().getVersionId());
		mf.addProperty(ManifestKeys.VERSION_DATE, VersionInfos.formatVersionDate(VersionInfos.getLastVersion().getVersionDate()));        
		mf.addProperty(ManifestKeys.BUILD_ID, VersionInfos.getBuildId());
		mf.addProperty(ManifestKeys.ENCODING, OSTool.getIANAFileEncoding());        
		mf.addProperty(ManifestKeys.OS_NAME, OSTool.getOSDescription());
		mf.addProperty(ManifestKeys.JRE, System.getProperty("java.home"));
		mf.addProperty(ManifestKeys.ARECA_HOME, FileSystemManager.getParent(Utils.buildExecutableFile()));	
	}

	/**
	 * Commit the backup and release the lock on the target
	 */
	protected void commitBackup(ProcessContext context) throws ApplicationException {
		try {
			context.getTaskMonitor().checkTaskState();
			context.getTaskMonitor().setCancellable(false);
			context.getManifest().addProperty(ManifestKeys.FILTERED_ENTRIES, context.getReport().getFilteredEntries());
			context.getManifest().addProperty(ManifestKeys.BACKUP_DURATION, Utils.formatDuration(System.currentTimeMillis() - context.getReport().getStartMillis()));     
			context.getManifest().addProperty(ManifestKeys.TARGET_ID, this.getUid());
			context.getManifest().addProperty(ManifestKeys.CHECKED, context.isChecked());
			addBasicInformationsToManifest(context.getManifest());

			Iterator iter = this.getAddons();
			while (iter.hasNext()) {
				ConfigurationAddon addon = (ConfigurationAddon)iter.next();
				addon.commitBackup(context);
			}
			
			medium.commitBackup(context);
			context.getReport().getStatus().addItem(StatusList.KEY_BACKUP);
		} catch (Throwable e) {
			Logger.defaultLogger().error("Exception caught during backup commit.", e);
			this.rollbackBackup(context, e.getMessage());
			throw wrapException(e);
		}
	}

	protected ApplicationException wrapException(Throwable e) {
		if (e instanceof ApplicationException) {
			return (ApplicationException)e;
		} else {
			Logger.defaultLogger().error(e);
			return new ApplicationException(e);
		}
	}

	/**
	 * Rollback the backup and release the lock on the target
	 */
	protected void cancelBackup(ProcessContext context) throws ApplicationException {
		try {
			context.getTaskMonitor().setCancellable(false);
			medium.getHistoryHandler().addEntryAndFlush(new HistoryEntry(HISTO_BACKUP_ROLLBACK, "Backup cancellation (nothing to store)"));
		} finally {
			try {
				medium.rollbackBackup(context);
			} finally {
				context.getReport().getStatus().addItem(StatusList.KEY_BACKUP);
			}
		}
	}

	/**
	 * Rollback the backup and release the lock on the target
	 */
	protected void rollbackBackup(ProcessContext context, String message) throws ApplicationException {
		try {
			context.getTaskMonitor().setCancellable(false);
			medium.getHistoryHandler().addEntryAndFlush(new HistoryEntry(HISTO_BACKUP_ROLLBACK, "Backup rollback."));
		} finally {
			try {
				Iterator iter = this.getAddons();
				while (iter.hasNext()) {
					ConfigurationAddon addon = (ConfigurationAddon)iter.next();
					addon.rollbackBackup(context, message);
				}
			} finally {
				try {
					medium.rollbackBackup(context);
				} finally {
					context.getReport().getStatus().addItem(StatusList.KEY_BACKUP, message);
				}
			}
		}
	}

	public void processMerge(
			int fromDelay, 
			int toDelay, 
			Manifest manifest, 
			MergeParameters params,
			CheckParameters checkParams,
			boolean runProcessors,
			ProcessContext context
			) throws ApplicationException {
		if (fromDelay != 0 && toDelay != 0 && fromDelay < toDelay) {
			// switch from/to
			int tmp = toDelay;
			toDelay = fromDelay;
			fromDelay = tmp;
		}

		// From
		GregorianCalendar fromDate = null;
		if (fromDelay != 0) {
			fromDate = new GregorianCalendar();
			fromDate.add(Calendar.DATE, -1 * fromDelay);
		}

		// To
		GregorianCalendar toDate = new GregorianCalendar();
		toDate.add(Calendar.DATE, -1 * toDelay);

		// Go !
		processMerge(fromDate, toDate, manifest, params, checkParams, runProcessors, context);
	}

	protected void runPreProcessors(int action, double taskRatio, boolean runProcessors, ProcessContext context) throws ApplicationException {
		if (runProcessors && ! this.preProcessors.isEmpty(action)) {
			context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(taskRatio, "pre-processors");
			TaskMonitor preProcessMon = context.getTaskMonitor().getCurrentActiveSubTask();
			try {     
				this.preProcessors.run(action, context);
			} finally {
				preProcessMon.enforceCompletion();
			}
		}
	}

	protected void runPostProcessors(int action, double taskRatio, boolean runProcessors, ProcessContext context) throws ApplicationException {
		if (runProcessors && ! this.postProcessors.isEmpty(action)) {
			context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(taskRatio, "post-processors");
			TaskMonitor postProcessMon = context.getTaskMonitor().getCurrentActiveSubTask();
			try {  
				if (this.postProcessors.requireStatistics(action)) {
					try {
						context.getReport().setIndicators(this.computeIndicators(context));
						context.getReport().getStatus().addItem("Compute Statistics");
					} catch (Throwable e) {
						Logger.defaultLogger().error("Error while computing statistics.", e);
						context.getReport().getStatus().addItem("Compute Statistics", e.getMessage());
					}
				}
				this.postProcessors.run(action, context);
			} finally {
				postProcessMon.enforceCompletion();
			}
		}
	}

	/**
	 * Merge archives
	 */
	public void processMerge(
			GregorianCalendar fromDate, 
			GregorianCalendar toDate, 
			Manifest manifest,
			MergeParameters params,
			CheckParameters checkParams,
			boolean runProcessors,
			ProcessContext context
			) throws ApplicationException {
		try {
			validateTargetState(ACTION_MERGE_OR_DELETE, context);  

			runPreProcessors(Processor.ACTION_MERGE, 0.1, runProcessors, context);

			double remaining = 
					1.0 
					- (! runProcessors || this.postProcessors.isEmpty(Processor.ACTION_MERGE) ? 0 : 0.1) 
					- (! runProcessors || this.preProcessors.isEmpty(Processor.ACTION_MERGE) ? 0 : 0.1)
					- (checkParams.isCheck() ? 0.3 : 0);


			context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(remaining, "merge");

			context.getInfoChannel().print("Merge in progress ...");
			if (checkParams.isCheck()) {
				Logger.defaultLogger().info("Post-merge archive verification is enabled.");
			} else {
				Logger.defaultLogger().warn("Post-merge archive verification is disabled - this is *not* safe !");
			}
			context.setChecked(checkParams.isCheck());
			HistoryHandler handler = medium.getHistoryHandler();
			handler.addEntryAndFlush(new HistoryEntry(HISTO_MERGE, "Merge from " + Utils.formatDisplayDate(fromDate) + " to " + Utils.formatDisplayDate(toDate) + "."));

			try {
				this.medium.merge(fromDate, toDate, manifest, params, context);
				this.preCommitMerge(context);
				context.getReport().getStatus().addItem(StatusList.KEY_INITIATE_MERGE);
			} catch (Exception e) {
				context.getReport().getStatus().addItem(StatusList.KEY_INITIATE_MERGE, e.getMessage());
				throw e;
			}
			Exception checkException = null;
			ProcessContext checkContext = null;

			// Check merged archive
			if ((! context.getReport().hasError()) && checkParams.isCheck() && context.getCurrentArchiveFile() != null) {
				context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.3, "archive check");
				TaskMonitor checkMon = context.getTaskMonitor().getCurrentActiveSubTask();
				try {
					// Get the date)
					Manifest mf = ManifestManager.readManifestForArchive((AbstractFileSystemMedium)this.medium, context.getCurrentArchiveFile());
					GregorianCalendar cal = mf.getDate();

					// Check the archive
					context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.6, "effective check");
					try {
						checkContext = context.createSubContext();
						Iterator iter = context.getReport().getRecoveryResult().getProcessedArchives().iterator();
						Set ignoredArchives = new HashSet();
						while (iter.hasNext()) {
							ignoredArchives.add(iter.next());
						}
						this.processArchiveCheck(checkParams, cal, ignoredArchives, false, checkContext);
					} catch (Exception e) {
						Logger.defaultLogger().error("An error has been caught : ", e);
						checkException = e;
					}
				} finally {
					checkMon.enforceCompletion();
				}
			}

			// Raise error in case of check issue
			if (checkException != null || (checkContext != null && checkContext.getReport().hasRecoveryIssues())) {
				String excMsg = "";
				if (checkException != null) {
					excMsg = " (got the following error : " + checkException.getMessage() + ")";
				}
				context.getInfoChannel().error("The created archive - " + FileSystemManager.getDisplayPath(context.getCurrentArchiveFile()) + " - was not successfully checked" + excMsg + ". It will be deleted.");
				context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.4, "deletion");  
				try {
					this.medium.deleteArchive(context.getCurrentArchiveFile());
					context.getReport().getStatus().addItem(StatusList.KEY_MERGED_DELETE);
				} catch (Exception e) {
					context.getReport().getStatus().addItem(StatusList.KEY_MERGED_DELETE, e.getMessage());
					throw e;
				}
				context.getInfoChannel().print(FileSystemManager.getDisplayPath(context.getCurrentArchiveFile()) + " deleted.");

				if (checkException != null) {
					throw checkException;
				} else {
					throw new ApplicationException("Inconsistency detected in merged archive (" + FileSystemManager.getDisplayPath(context.getCurrentArchiveFile()) + "). It has been deleted.");
				}
			} else {
				this.finalizeMerge(context);
			}
		} catch (Throwable e) {
			Logger.defaultLogger().error(e);
			this.rollbackMerge(context, e.getMessage());
			throw wrapException(e);
		} finally {
			context.getReport().setStopMillis();

			runPostProcessors(Processor.ACTION_MERGE, 0.1, runProcessors, context);
			context.getInfoChannel().print("Merge completed.");
		}
	}  

	/**
	 * Delete archives
	 */
	public void processDeleteArchives(
			GregorianCalendar fromDate,
			ProcessContext context
			) throws ApplicationException {
		validateTargetState(ACTION_MERGE_OR_DELETE, context);  
		try {
			context.getTaskMonitor().setCancellable(false);
			context.getInfoChannel().print("Deletion in progress ...");

			HistoryHandler handler = medium.getHistoryHandler();
			handler.addEntryAndFlush(new HistoryEntry(HISTO_DELETE, "Archive deletion from " + Utils.formatDisplayDate(fromDate) + "."));

			this.medium.deleteArchives(fromDate, context);
			context.getReport().getStatus().addItem(StatusList.KEY_DELETE);
		} catch (Throwable e) {
			context.getReport().getStatus().addItem(StatusList.KEY_DELETE, e.getMessage());
			throw wrapException(e);
		} finally {
			context.getTaskMonitor().resetCancellationState();
			context.getInfoChannel().print("Deletion completed.");
		}   
	}  

	/**
	 * Deletes the archive which are newer than "delay" days.
	 */
	public void processDeleteArchives(int delay, ProcessContext context) throws ApplicationException {
		GregorianCalendar mergeDate = new GregorianCalendar();
		mergeDate.add(Calendar.DATE, -1 * delay);

		processDeleteArchives(mergeDate, context);
	}

	protected void preCommitMerge(ProcessContext context) throws ApplicationException {
		try {
			context.getTaskMonitor().checkTaskState();
			context.getTaskMonitor().setCancellable(false);
			this.medium.preCommitMerge(context);
		} catch (TaskCancelledException e) {
			throw new ApplicationException(e);
		}
	}

	protected void finalizeMerge(ProcessContext context) throws ApplicationException {
		this.medium.finalizeMerge(context);
		context.getReport().getStatus().addItem(StatusList.KEY_FINALIZE_MERGE);
		context.getTaskMonitor().resetCancellationState();
	}

	protected void rollbackMerge(ProcessContext context, String message) throws ApplicationException {
		context.getInfoChannel().getTaskMonitor().setCancellable(false);
		try {
			HistoryHandler handler = medium.getHistoryHandler();
			handler.addEntryAndFlush(new HistoryEntry(HISTO_MERGE_ROLLBACK, "Merge rollback."));
		} catch (Throwable e) {
			// Make sure no error is raised
			Logger.defaultLogger().error(e);
		} 

		try {
			this.medium.rollbackMerge(context);
		} finally {
			context.getReport().getStatus().addItem(StatusList.KEY_FINALIZE_MERGE, message);
		}
		context.getTaskMonitor().resetCancellationState();
	}

	/**
	 * Recover stored data
	 */
	public void processRecover(
			String destination, 
			boolean appendSuffix,
			ArecaRawFileList filters, 
			AbstractCopyPolicy policy,
			GregorianCalendar date, 
			boolean keepDeletedEntries,
			boolean checkRecoveredFiles, 
			ProcessContext context
			) throws ApplicationException {
		validateTargetState(ACTION_RECOVER, context);
		TaskMonitor globalMonitor = context.getTaskMonitor().getCurrentActiveSubTask();
		try {
			String strDate = date == null ? "" : " as of " + CalendarUtils.getDateToString(date);
			context.getInfoChannel().print("Recovery" + strDate + " in progress ...");
			StringBuffer sb = new StringBuffer("Recovery destination = " + destination);
			if (filters != null && ! filters.isEmpty()) {
				sb.append(", Items = {");
				for (int i=0; i<filters.length(); i++) {
					if (i != 0) {
						sb.append(", ");
					}
					sb.append(filters.get(i));
				}
				sb.append("}");
			}
			Logger.defaultLogger().info(sb.toString());
			Logger.defaultLogger().info("Override policy = " + policy.getClass().getSimpleName() + "," + (appendSuffix ? "" : " do not") + " append a subdirectory.");

			if (date == null) {
				date = new GregorianCalendar();
			}

			HistoryHandler handler = medium.getHistoryHandler();
			handler.addEntryAndFlush(new HistoryEntry(HISTO_RECOVER, "Recovery : " + Utils.formatDisplayDate(date) + "."));

			this.processRecoverImpl(destination, appendSuffix, filters, policy, date, keepDeletedEntries, checkRecoveredFiles, context);
		} finally {
			context.getInfoChannel().print("Recovery completed.");
			globalMonitor.enforceCompletion();
		}
	}

	/**
	 * Recovers a specific version of a given file
	 */
	public void processRecover(
			String destination, 
			GregorianCalendar date, 
			String entry, 
			AbstractCopyPolicy policy,
			boolean checkRecoveredFiles, 
			ProcessContext context
			) throws ApplicationException {
		validateTargetState(ACTION_RECOVER, context);
		TaskMonitor globalMonitor = context.getTaskMonitor().getCurrentActiveSubTask();
		try {
			String strDate = date == null ? "" : " as of " + CalendarUtils.getDateToString(date);
			context.getInfoChannel().print("Recovery of " + entry + strDate + " in progress ...");
			Logger.defaultLogger().info("Recovery destination = " + destination);
			this.processRecoverImpl(destination, date, entry, policy, checkRecoveredFiles,context);
		} finally {
			context.getInfoChannel().print("Recovery of " + entry + " completed.");
			globalMonitor.enforceCompletion();
		}
	}

	/**
	 * Recover stored data
	 */
	protected abstract void processRecoverImpl(
			String destination, 
			boolean appendSuffix,
			ArecaRawFileList filters, 
			AbstractCopyPolicy policy,
			GregorianCalendar date, 
			boolean keepDeletedEntries,
			boolean checkRecoveredFiles, 
			ProcessContext context
			) throws ApplicationException;    

	/**
	 * Check the archive's content
	 * <BR>IgnoreList contains a list of archives which will be ignored during recovery
	 */
	public abstract void processArchiveCheck(
			CheckParameters checkParams, 
			GregorianCalendar date, 
			Set ignoreList,
			boolean runProcessors,
			ProcessContext context
			) throws ApplicationException;

	/**
	 * Recover stored data
	 */
	protected abstract void processRecoverImpl(
			String destination, 
			GregorianCalendar date, 
			String name, 
			AbstractCopyPolicy policy,
			boolean checkRecoveredFiles,
			ProcessContext context
			) throws ApplicationException;    

	public void doBeforeDelete() {
		if (this.getMedium() != null) {
			this.getMedium().doBeforeDelete();
		}
	}

	public void doAfterDelete() {
		if (this.getMedium() != null) {
			this.getMedium().doAfterDelete();
		}
	}

	public abstract boolean nextElement(ProcessContext context, RecoveryEntry cursor) throws ApplicationException;

	public abstract Manifest buildDefaultMergeManifest(GregorianCalendar fromDate, GregorianCalendar toDate) throws ApplicationException;

	protected boolean filterEntryBeforeStore(RecoveryEntry entry) {
		return true;
	}

	public String toString() {
		if (this.targetName == null) {
			return "Element " + this.uid;
		} else {
			return this.targetName;
		}
	}

	public boolean equals(Object arg0) {
		if (arg0 == null) {
			return false;
		} else if (! (arg0 instanceof AbstractTarget)) {
			return false;
		} else {
			AbstractTarget other = (AbstractTarget)arg0;
			return (
					EqualsHelper.equals(other.getUid(), this.getUid())
					);
		}
	}

	public int hashCode() {
		int result = HashHelper.initHash(this);
		result = HashHelper.hash(result, this.getUid());
		return result;
	}

	/**
	 * Compute indicators on the stored data. 
	 */
	public IndicatorMap computeIndicators(ProcessContext context) throws ApplicationException {
		try {
			validateTargetState(ACTION_INDICATORS, context);
			return this.medium.computeIndicators();
		} catch (TaskCancelledException e) {
			throw new ApplicationException(e);
		}
	}

	/**
	 * Build a new UID.
	 */
	private static String generateNewUID() {
		try {
			SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
			return String.valueOf(Math.abs((long)prng.nextInt())); // Math.abs(Integer.MIN_VALUE) == Integer.MIN_VALUE ... hence the cast as "long"
		} catch (NoSuchAlgorithmException e) {
			Logger.defaultLogger().error("Error generating a random integer. Using Math.random instead.", e);
			return "" + Math.abs((int)(Math.random() * 10000000 + System.currentTimeMillis()));
		}
	}

	public void secureUpdateCurrentTask(long taskIndex, long taskCount, String task, ProcessContext context) {
		try {
			context.getInfoChannel().updateCurrentTask(taskIndex, taskCount, task);
		} catch (Throwable e) {
			Logger.defaultLogger().error(e);
		}
	}

	public void secureUpdateCurrentTask(String task, ProcessContext context) {
		secureUpdateCurrentTask(0, 1, task, context);
	}

	public TransactionPoint getLastTransactionPoint(String backupScheme) throws ApplicationException {
		return ((AbstractFileSystemMedium)medium).getLastTransactionPoint(backupScheme);
	}
}

