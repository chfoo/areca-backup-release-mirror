package com.application.areca;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import com.application.areca.context.ProcessContext;
import com.application.areca.context.ReportingConfiguration;
import com.application.areca.filter.ArchiveFilter;
import com.application.areca.filter.FilterGroup;
import com.application.areca.indicator.IndicatorMap;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.manifest.ManifestKeys;
import com.application.areca.processor.ProcessorList;
import com.application.areca.search.SearchCriteria;
import com.application.areca.search.TargetSearchResult;
import com.application.areca.version.VersionInfos;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.object.PublicClonable;
import com.myJava.system.OSTool;
import com.myJava.util.CalendarUtils;
import com.myJava.util.errors.ActionReport;
import com.myJava.util.history.History;
import com.myJava.util.history.HistoryEntry;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
 * Abstract base implementation for recovery targets.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4331497872542711431
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
public abstract class AbstractRecoveryTarget 
implements HistoryEntryTypes, PublicClonable, Identifiable {
    
    private static final String K_FILTERED = "filtered entries";
    
    public static final String BACKUP_SCHEME_FULL = "Full backup";
    public static final String BACKUP_SCHEME_INCREMENTAL = "Incremental backup";
    public static final String BACKUP_SCHEME_DIFFERENTIAL = "Differential backup";
    
    protected ArchiveMedium medium;
    protected FilterGroup filterGroup = new FilterGroup();
    protected int id; // Numeric unique id of the target within its process
    protected String uid; // Unique identifier
    protected String targetName; // Name of the target
    protected RecoveryProcess process;
    protected String comments;
    protected ProcessorList postProcessors = new ProcessorList();
    protected ProcessorList preProcessors = new ProcessorList();
    protected boolean running;
     
    public void setProcess(RecoveryProcess process) {
        this.process = process;
    }
    
    protected void copyAttributes(Object clone) {
        AbstractRecoveryTarget other = (AbstractRecoveryTarget)clone;
        other.process = process;
        other.id = process.getNextFreeTargetId();
        other.uid = generateNewUID();
        other.targetName = "Copy of " + targetName;
        other.comments = comments;
        other.filterGroup = (FilterGroup)this.filterGroup.duplicate();
        other.postProcessors = (ProcessorList)postProcessors.duplicate();
        other.preProcessors = (ProcessorList)preProcessors.duplicate();
        other.setMedium((ArchiveMedium)medium.duplicate(), true);
    }
    
    public boolean supportsBackupScheme(String backupScheme) {
        return this.medium.supportsBackupScheme(backupScheme);
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
    
    public String getUid() {
        if (this.uid == null) {
            Logger.defaultLogger().info("No UID was specified for target '" + targetName + "'. Creating a random ID");
            this.uid = generateNewUID();
        }      
        
        return uid;
    }

    public String getTargetName() {
        return targetName;
    }
    
    public void setTargetName(String taskName) {
        this.targetName = taskName;
    }
    
    public String getDescription() {
        StringBuffer buf = new StringBuffer("Target #");
        buf.append(this.id);
        buf.append("\n");
        buf.append(this.getSpecificTargetDescription());
        buf.append("\nMedium : ");
        buf.append(medium.getDescription());
        return new String(buf);
    }
    
    protected abstract String getSpecificTargetDescription();
    
    public RecoveryProcess getProcess() {
        return this.process;
    }
    
    /**
     * Vérifie l'état du système avant toute action (archivage, backup, fusion) 
     */
    public ActionReport checkTargetState(int action) {
        return this.medium.checkMediumState(action);
    }

    public String getComments() {
        return comments;
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
    
    public TargetSearchResult search(SearchCriteria criteria) throws ApplicationException {
        return this.medium.search(criteria);
    }
    
    /**
     * @param medium The medium to set.
     */
    public void setMedium(ArchiveMedium medium, boolean revalidateMedium) {
        this.medium = medium;
        this.medium.setTarget(this, revalidateMedium);
    }
    
    public void addFilter(ArchiveFilter filter) {
        this.filterGroup.addFilter(filter);
    }
    
    public Iterator getFilterIterator() {
        return this.filterGroup.getFilterIterator();
    } 
    
    public History getHistory() {
        return this.medium.getHistory();
    }
    
    public void clearHistory() throws ApplicationException {
        try {
            History hist = this.medium.getHistory();
            if (hist != null) {
                hist.clear();
                hist.flush();
            }
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }
    
    /**
     * Ouvre la target - prend le lock sur la target.
     */
    protected void open(Manifest manifest, ProcessContext context, String backupScheme) throws ApplicationException {   
        medium.open(manifest, context, backupScheme);
    }   
    
    /**
     * Lance le backup sur cette target.
     */
    public synchronized void processBackup(Manifest manifest, ProcessContext context, String backupScheme) throws ApplicationException {
        boolean backupRequired = true;
        
        // Si requis, on pré-vérifie qu'au moins un fichier a été modifié avant de déclencher le backup.
        // 2 conditions :
        // - Le support requiert une pré-vérification
        // - Le manifeste est null (ie l'utilisateur n'en a pas fourni un explicitement) - Si un manifeste est renseigné, on fait tjs le backup.
        if (this.medium.isPreBackupCheckUseful() && manifest == null && backupScheme.equals(BACKUP_SCHEME_INCREMENTAL)) {
            context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.3, "pre-check");
            context.getInfoChannel().print("Pre-check in progress ...");
            this.processSimulateImpl(context, false);
            context.getInfoChannel().print("Pre-check completed.");
            backupRequired = (context.getReport().getSavedFiles() > 0 || context.getReport().getDeletedFiles() > 0);
            context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.7, "backup");
            context.reset(false);
        }
        
        if (backupRequired) {
            if (! this.preProcessors.isEmpty()) {
                context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.1, "pre-processors");
                TaskMonitor preProcessMon = context.getTaskMonitor().getCurrentActiveSubTask();
                try {     
                    this.preProcessors.run(context);
                } finally {
                    preProcessMon.enforceCompletion();
                }
            }
            
            if ((! this.postProcessors.isEmpty()) && (! this.preProcessors.isEmpty())) {
                context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.8, "backup-main");
            } else if ((! this.postProcessors.isEmpty()) || (! this.preProcessors.isEmpty())) {
                context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.9, "backup-main");
            }
            
            if (manifest == null) {
                manifest = new Manifest(Manifest.TYPE_BACKUP);
            }

            try {
                // Lance le backup ...
                context.reset(false);
                context.getInfoChannel().print("Backup in progress ...");
                context.getTaskMonitor().checkTaskCancellation();
                context.getReport().startDataFlowTimer();
                this.open(manifest, context, backupScheme);

                try {
                    HistoryEntry entry = new HistoryEntry(HISTO_BACKUP, "Backup.");
                    History h = this.getHistory();
                    if (h != null) {
                        h.addEntry(entry);
                    }
                } catch (IOException e1) {
                    throw new ApplicationException(e1);
                }

                RecoveryEntry entry = this.nextElement(context);
                long index = 0;
                while (entry != null) {
                    context.getInfoChannel().getTaskMonitor().checkTaskCancellation();
                    if (this.filterEntryBeforeStore(entry)) {
                        try {
                            index++;
                            this.medium.store(entry, context);
                            context.getInfoChannel().updateCurrentTask(index, 0, entry.toString());
                        } catch (StoreException e) {
                            throw new ApplicationException(e);
                        }
                    }
                    entry = this.nextElement(context); 
                }
                this.commitBackup(context);
                context.getReport().stopDataFlowTimer();
                Logger.defaultLogger().info(Utils.formatLong(context.getReport().getWrittenInKB()) + " kb stored in " + Utils.formatLong(context.getReport().getDataFlowTimeInSecond()) + " seconds.");                
                Logger.defaultLogger().info("Average data output : " + Utils.formatLong(context.getReport().getDataFlowInKBPerSecond()) + " kb/second.");
            } catch (Exception e) {
                Logger.defaultLogger().error(e);
                this.rollbackBackup(context);
                if (e instanceof ApplicationException) {
                    throw (ApplicationException)e;
                } else {
                    throw new ApplicationException(e);
                }
            } finally {
                if (! this.postProcessors.isEmpty()) {
                    context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.1, "post-processors");
	                TaskMonitor postProcessMon = context.getTaskMonitor().getCurrentActiveSubTask();
	                try {     
	                    this.postProcessors.run(context);
	                } finally {
	                    postProcessMon.enforceCompletion();
	                }
                }
                context.getInfoChannel().print("Backup completed."); 
            }
        } else {
            // Aucun backup nécessaire : on termine directement la tâche.
            context.getTaskMonitor().getCurrentActiveSubTask().setCurrentCompletion(1.0);
            context.getInfoChannel().print("No backup required - Operation completed.");     
        }
    }
    
    /**
     * Lance le backup sur cette target.
     */
    public synchronized RecoveryEntry[] processSimulate(ProcessContext context) throws ApplicationException {
        try {
            context.getInfoChannel().print("Simulation in progress ...");
            
            return this.processSimulateImpl(context, true);
        } finally {
            context.getInfoChannel().print("Simulation completed.");            
        }
    }    
    
    /**
     * Launches a simulation process.
     */
    public synchronized RecoveryEntry[] processSimulateImpl(ProcessContext context, boolean returnDetailedResult) throws ApplicationException {
        try {  
            
            TaskMonitor simulationGlobalMonitor = context.getInfoChannel().getTaskMonitor().getCurrentActiveSubTask();
            
            List entries = new ArrayList();
            RecoveryEntry entry = this.nextElement(context);
            long index = 0;
            while (entry != null) {
                context.getTaskMonitor().checkTaskCancellation();
                if (this.filterEntryBeforeStore(entry)) {
                    index++;
                    this.medium.simulateEntryProcessing(entry, context);
                    context.getInfoChannel().updateCurrentTask(index, 0, entry.toString());
                    if (entry.getStatus() != RecoveryEntry.STATUS_NOT_STORED) {
                        context.getReport().addSavedFile();
                        if (returnDetailedResult) {
                            entries.add(entry);
                        } else {
                            // Once we get a stored entry in "not detailed" mode, stop this method --> We know that it will be necessary to make a backup
                           simulationGlobalMonitor.enforceCompletion();
                           return null;
                        }
                    } else {
                        context.getReport().addIgnoredFile();
                    }
                }
                entry = this.nextElement(context); 
            }
            context.getTaskMonitor().checkTaskCancellation();

            // Add all deleted files
            List deletedEntries = medium.closeSimulation(context); 
            entries.addAll(deletedEntries);
            context.getReport().setDeletedFiles(deletedEntries.size());
            
            return (RecoveryEntry[])entries.toArray(new RecoveryEntry[0]);
        } catch (Exception e) {
        	if (e instanceof ApplicationException) {
        		throw (ApplicationException)e;
        	} else {
        		throw new ApplicationException(e);
        	}
        }
    }    
    
    public static void addBasicInformationsToManifest(Manifest mf) {
        mf.addProperty(ManifestKeys.VERSION, VersionInfos.getLastVersion().getVersionId());
        mf.addProperty(ManifestKeys.VERSION_DATE, VersionInfos.formatVersionDate(VersionInfos.getLastVersion().getVersionDate()));        
        mf.addProperty(ManifestKeys.BUILD_ID, VersionInfos.getBuildId());
        mf.addProperty(ManifestKeys.ENCODING, OSTool.getIANAFileEncoding());        
        mf.addProperty(ManifestKeys.OS_NAME, OSTool.getOSDescription());
    }
    
    /**
     * Validation du backup
     * <BR>Relache également le lock sur la target, afin que d'autres opérations puissent être effectuées.
     */
    protected void commitBackup(ProcessContext context) throws ApplicationException {
    	try {
            context.getTaskMonitor().checkTaskCancellation();
            context.getTaskMonitor().setCancellable(false);
        	context.getManifest().addProperty(ManifestKeys.FILTERED_ENTRIES, context.getReport().getFilteredEntries());
        	context.getManifest().addProperty(ManifestKeys.BACKUP_DURATION, Utils.formatDuration(System.currentTimeMillis() - context.getReport().getStartMillis()));     
        	context.getManifest().addProperty(ManifestKeys.TARGET_ID, this.getUid());
        	addBasicInformationsToManifest(context.getManifest());
        	
    		medium.commitBackup(context);
            context.getReport().setCommited();
    	} catch (Exception e) {
    	    Logger.defaultLogger().error("Exception caught during backup commit.", e);
    		this.rollbackBackup(context);
    		
    		if (e instanceof ApplicationException) {
    			throw (ApplicationException)e;
    		} else {
    			throw new ApplicationException(e);
    		}
    	}
    }
    
    /**
     * Annulation du backup
     * <BR>Relache également le lock sur la target, afin que d'autres opérations puissent être effectuées.
     */
    protected void rollbackBackup(ProcessContext context) throws ApplicationException {
		try {
            context.getTaskMonitor().setCancellable(false);
            HistoryEntry entry = new HistoryEntry(HISTO_BACKUP_CANCEL, "Backup cancellation.");
            History h = this.getHistory();
            if (h != null) {
                h.addEntry(entry);
            }
		} catch (IOException e1) {
			throw new ApplicationException(e1);
		} finally {
	        medium.rollbackBackup(context);
		}
    }
    
    public void processMerge(int fromDelay, int toDelay, boolean keepDeletedEntries, ProcessContext context) throws ApplicationException {
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
        processMerge(fromDate, toDate, keepDeletedEntries, null, context);
    }
    
    /**
     * Lance la fusion sur la target
     */
    public void processMerge(String date, boolean keepDeletedEntries, ProcessContext context) throws ApplicationException {
        processMerge(null, CalendarUtils.resolveDate(date, null), keepDeletedEntries, null, context);
    }
    
    /**
     * Lance la fusion sur la target
     */
    public void processMerge(
            GregorianCalendar fromDate, 
            GregorianCalendar toDate, 
            boolean keepDeletedEntries,
            Manifest manifest,
            ProcessContext context
    ) throws ApplicationException {
        try {
            context.getInfoChannel().print("Merge in progress ...");
            
    		try {
    		    
                HistoryEntry entry = new HistoryEntry(HISTO_MERGE, "Merge from " + Utils.formatDisplayDate(fromDate) + " to " + Utils.formatDisplayDate(toDate) + ".");
                History h = this.getHistory();
                if (h != null) {
                    h.addEntry(entry);
                }
    		} catch (IOException e) {
    			throw new ApplicationException(e);
    		}        
    		this.medium.merge(fromDate, toDate, keepDeletedEntries, manifest, context);
    		this.commitMerge(context);
    	} catch (Exception e) {
    	    Logger.defaultLogger().error(e);
    		this.rollbackMerge(context);
    		if (e instanceof ApplicationException) {
    			throw (ApplicationException)e;
    		} else {
    			throw new ApplicationException(e);
    		}
    	} finally {
            context.getInfoChannel().print("Merge completed.");
        }
    }  
    
    /**
     * Lance la suppression sur la target
     */
    public void processDeleteArchives(
            GregorianCalendar fromDate,
            ProcessContext context
    ) throws ApplicationException {
    	try {
            context.getTaskMonitor().setCancellable(false);
            context.getInfoChannel().print("Deletion in progress ...");
    		
    		try {
                History h = this.getHistory();
                if (h != null) {
                    HistoryEntry entry = new HistoryEntry(HISTO_DELETE, "Archive deletion from " + Utils.formatDisplayDate(fromDate) + ".");
                    h.addEntry(entry);
                }
    		} catch (IOException e) {
    			throw new ApplicationException(e);
    		}        
    		this.medium.deleteArchives(fromDate, context);
    	} catch (Exception e) {
    		if (e instanceof ApplicationException) {
    			throw (ApplicationException)e;
    		} else {
    			throw new ApplicationException(e);
    		}
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
    
    /**
     * Validation du merge
     */
    protected void commitMerge(ProcessContext context) throws ApplicationException {
        try {
            context.getTaskMonitor().checkTaskCancellation();
            context.getTaskMonitor().setCancellable(false);
            this.medium.commitMerge(context);
            context.getReport().setCommited();
            context.getTaskMonitor().resetCancellationState();
        } catch (TaskCancelledException e) {
            throw new ApplicationException(e);
        }
    }
    
    /**
     * Annulation du merge
     */
    protected void rollbackMerge(ProcessContext context) throws ApplicationException {
		try {
            context.getInfoChannel().getTaskMonitor().setCancellable(false);
            
            HistoryEntry entry = new HistoryEntry(HISTO_MERGE_CANCEL, "Merge cancellation.");
            History h = this.getHistory();
            if (h != null) {
                h.addEntry(entry);
            }
		} catch (IOException e1) {
			throw new ApplicationException(e1);
		} 
		
    	this.medium.rollbackMerge(context);
        context.getTaskMonitor().resetCancellationState();
    }
    
    /**
     * Rétablit l'archive
     */
    public void processRecover(String destination, String[] filters, GregorianCalendar date, boolean recoverDeletedEntries, ProcessContext context) throws ApplicationException {
        TaskMonitor globalMonitor = context.getTaskMonitor().getCurrentActiveSubTask();
        try {
            String strDate = date == null ? "" : " as of " + CalendarUtils.getDateToString(date);
            context.getInfoChannel().print("Recovery" + strDate + " in progress ...");
            StringBuffer sb = new StringBuffer("Recovery destination = " + destination);
            if (filters != null && filters.length != 0) {
                sb.append(", Items = {");
                for (int i=0; i<filters.length; i++) {
                    if (i != 0) {
                        sb.append(", ");
                    }
                    sb.append(filters[i]);
                }
                sb.append("}");
            }
            Logger.defaultLogger().info(sb.toString());
            
            if (date == null) {
                date = new GregorianCalendar();
            }
            try {
                HistoryEntry entry = new HistoryEntry(HISTO_RECOVER, "Recovery : " + Utils.formatDisplayDate(date) + ".");
                History h = this.getHistory();
                if (h != null) {
                    h.addEntry(entry);
                }
            } catch (IOException e1) {
                throw new ApplicationException(e1);
            }   
            this.processRecoverImpl(destination, filters, date, recoverDeletedEntries, context);
        } finally {
            context.getInfoChannel().print("Recovery completed.");
            globalMonitor.enforceCompletion();
        }
    }
    
    /**
     * Rétablit une version d'une élément d'archive
     */
    public void processRecover(String destination, GregorianCalendar date, RecoveryEntry entry, ProcessContext context) throws ApplicationException {
        TaskMonitor globalMonitor = context.getTaskMonitor().getCurrentActiveSubTask();
        try {
            String strDate = date == null ? "" : " as of " + CalendarUtils.getDateToString(date);
            context.getInfoChannel().print("Recovery of " + entry.getName() + strDate + " in progress ...");
            Logger.defaultLogger().info("Recovery destination = " + destination);
            this.processRecoverImpl(destination, date, entry, context);
        } finally {
            context.getInfoChannel().print("Recovery of " + entry.getName() + " completed.");
            globalMonitor.enforceCompletion();
        }
    }
    
    /**
     * Rétablit l'archive
     */
    protected abstract void processRecoverImpl(String destination, String[] filters, GregorianCalendar date, boolean recoverDeletedEntries, ProcessContext context) throws ApplicationException;    
    
    /**
     * Rétablit la version de l'archive
     */
    protected abstract void processRecoverImpl(String destination, GregorianCalendar date, RecoveryEntry entry, ProcessContext context) throws ApplicationException;    
    
    /**
     * Méthode à surcharger ... appelée avant suppression de la target
     */
    public void doBeforeDelete() {
    	if (this.getMedium() != null) {
    		this.getMedium().doBeforeDelete();
    	}
    }
    
    /**
     * Méthode à surcharger ... appelée après suppression de la target
     */
    public void doAfterDelete() {
    	if (this.getMedium() != null) {
    		this.getMedium().doAfterDelete();
    	}
    }
    
    /**
     * Exécute l'ensemble des filtres sur la target
     * @param entry
     * @return
     */
    protected boolean acceptEntry(RecoveryEntry entry, boolean storage, ProcessContext context) {
        boolean accept = storage ? filterGroup.acceptStorage(entry) : filterGroup.acceptIteration(entry);
        
        if (! accept) {
            context.getReport().addFilteredEntry();
            if (requiresFilteredEntriesListing()) {
                context.getReport().getFilteredEntriesData().addFilteredEntry(entry, K_FILTERED);
            }
            return false;
        } else {
            return true;
        }
    }
    
    protected boolean requiresFilteredEntriesListing() {
        return 
        	ReportingConfiguration.getInstance().isReportingEnabled()
        	|| this.postProcessors.requiresFilteredEntriesListing()
        ;
    }
    
    /**
     * Retourne la prochaine entry à stocker.
     * Attention : c'est cette méthode qui a la charge d'appeler la méthode
     * de filtre "acceptEntry".
     *  
     * @return La prochaine Entry, filtrée.
     */
    public abstract RecoveryEntry nextElement(ProcessContext context) throws ApplicationException;
    
    /**
     * Construit un manifeste par défaut pour le merge d'archives (utile pour le préremplissage du manifeste) 
     */
    public abstract Manifest buildDefaultMergeManifest(GregorianCalendar fromDate, GregorianCalendar toDate) throws ApplicationException;
    
    /**
     * A surcharger éventuellement. 
     * @param entry
     * @return
     */
    protected boolean filterEntryBeforeStore(RecoveryEntry entry) {
        return true;
    }
    
    public String toString() {
        if (this.targetName == null) {
            return "Elément " + this.id;
        } else {
            return this.targetName;
        }
    }
    
	public boolean equals(Object arg0) {
		if (arg0 == null) {
			return false;
		} else if (! (arg0 instanceof AbstractRecoveryTarget)) {
			return false;
		} else {
			AbstractRecoveryTarget other = (AbstractRecoveryTarget)arg0;
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
     * Génération d'un identifiant de target.
     * Utile pour la log. 
     */
    public abstract String getFullName();
    
    /**
     * Computes indicators on the stored data. 
     */
    public IndicatorMap computeIndicators() throws ApplicationException {
        return this.medium.computeIndicators();
    }
    
    /**
     * Build a new UID.
     */
    private static String generateNewUID() {
        try {
            SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
            return "" + Math.abs(prng.nextInt());
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
}

