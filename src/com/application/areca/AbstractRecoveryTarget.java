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
import com.application.areca.indicator.IndicatorMap;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.postprocess.PostProcessorList;
import com.application.areca.search.SearchCriteria;
import com.application.areca.search.TargetSearchResult;
import com.application.areca.version.VersionInfos;
import com.myJava.util.CalendarUtils;
import com.myJava.util.DuplicateHelper;
import com.myJava.util.EqualsHelper;
import com.myJava.util.HashHelper;
import com.myJava.util.PublicClonable;
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
 * <BR>Areca Build ID : -6307890396762748969
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
    
    protected ArchiveMedium medium;
    protected List filters = new ArrayList();
    protected int id; // Numeric unique id of the target within its process
    protected String uid; // Unique identifier
    protected String targetName; // Name of the target
    protected RecoveryProcess process;
    protected String comments;
    protected PostProcessorList postProcessors = new PostProcessorList();
     
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
        other.filters = DuplicateHelper.duplicate(filters);
        other.postProcessors = (PostProcessorList)postProcessors.duplicate();
        other.setMedium((ArchiveMedium)medium.duplicate(), true);
    }
    
    public PostProcessorList getPostProcessors() {
        return postProcessors;
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
    
    public ArchiveFilter getFilterAt(int i) {
        return (ArchiveFilter)this.filters.get(i);
    }
    
    public void addFilter(ArchiveFilter filter) {
        this.filters.add(filter);
    }
    
    public int getFilterCount() {
        return this.filters.size();
    }
    
    public Iterator getFilterIterator() {
        return this.filters.iterator();
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
    protected void open(Manifest manifest, ProcessContext context) throws ApplicationException {   
        medium.open(manifest, context);
    }   
    
    /**
     * Lance le backup sur cette target.
     */
    public synchronized void processBackup(Manifest manifest, ProcessContext context) throws ApplicationException {
        boolean backupRequired = true;
        
        // Si requis, on pré-vérifie qu'au moins un fichier a été modifié avant de déclencher le backup.
        // 2 conditions :
        // - Le support requiert une pré-vérification
        // - Le manifeste est null (ie l'utilisateur n'en a pas fourni un explicitement) - Si un manifeste est renseigné, on fait tjs le backup.
        if (this.medium.isPreBackupCheckUseful() && manifest == null) {
            this.process.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.3);
            this.getProcess().getInfoChannel().logInfo(null, "Pre-check in progress ...");
            this.processSimulateImpl(context, false);
            this.getProcess().getInfoChannel().logInfo(null, "Pre-check completed.");
            backupRequired = (context.getReport().getSavedFiles() > 0 || context.getReport().getDeletedFiles() > 0);
            this.process.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.7);
            
            context.getReport().reset();
        }
        
        if (backupRequired) {
            if (! this.postProcessors.isEmpty()) {
                this.process.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.9);
            }
            
            if (manifest == null) {
                manifest = new Manifest();
                manifest.setType(Manifest.TYPE_BACKUP);
            }

            try {
                // Lance le backup ...
                this.getProcess().getInfoChannel().logInfo(null, "Backup in progress ...");
                this.getTaskMonitor().checkTaskCancellation();
                this.open(manifest, context);

                try {
                    HistoryEntry entry = new HistoryEntry(HISTO_BACKUP, "Backup.");
                    this.getHistory().addEntry(entry);
                } catch (IOException e1) {
                    throw new ApplicationException(e1);
                }

                RecoveryEntry entry = this.nextElement(context);
                long index = 0;
                while (entry != null) {
                    this.getTaskMonitor().checkTaskCancellation();
                    if (this.filterEntryBeforeStore(entry)) {
                        try {
                            index++;
                            this.medium.store(entry, context);
                            this.process.getInfoChannel().updateCurrentTask(index, 0, entry.toString());
                        } catch (StoreException e) {
                            throw new ApplicationException(e);
                        }
                    }
                    entry = this.nextElement(context); 
                }
                this.commitBackup(context);
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
	                this.process.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.1);
	                TaskMonitor postProcessMon = this.process.getTaskMonitor().getCurrentActiveSubTask();
	                try {     
	                    this.postProcessors.postProcess(context);
	                } finally {
	                    postProcessMon.enforceCompletion();
	                }
                }
                this.getProcess().getInfoChannel().logInfo(null, "Backup completed."); 
            }
        } else {
            // Aucun backup nécessaire : on termine directement la tâche.
            this.process.getTaskMonitor().getCurrentActiveSubTask().setCurrentCompletion(1.0);
            this.getProcess().getInfoChannel().logInfo(null, "No backup required - Operation completed.");     
        }
    }
    
    /**
     * Lance le backup sur cette target.
     */
    public synchronized RecoveryEntry[] processSimulate(ProcessContext context) throws ApplicationException {
        try {
            this.getProcess().getInfoChannel().logInfo(null, "Simulation in progress ...");
            
            return this.processSimulateImpl(context, true);
        } finally {
            this.getProcess().getInfoChannel().logInfo(null, "Simulation completed.");            
        }
    }    
    
    /**
     * Launches a simulation process.
     */
    public synchronized RecoveryEntry[] processSimulateImpl(ProcessContext context, boolean returnDetailedResult) throws ApplicationException {
        try {  
            
            TaskMonitor simulationGlobalMonitor = this.getTaskMonitor().getCurrentActiveSubTask();
            
            List entries = new ArrayList();
            RecoveryEntry entry = this.nextElement(context);
            long index = 0;
            while (entry != null) {
                this.getTaskMonitor().checkTaskCancellation();
                if (this.filterEntryBeforeStore(entry)) {
                    index++;
                    this.medium.simulateEntryProcessing(entry, context);
                    this.process.getInfoChannel().updateCurrentTask(index, 0, entry.toString());
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
            this.getTaskMonitor().checkTaskCancellation();

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
    
    public TaskMonitor getTaskMonitor() {
        return this.process.taskMonitor;
    }
    
    public static void addBasicInformationsToManifest(Manifest mf) {
        mf.addProperty("Version", VersionInfos.getLastVersion().getVersionId() + " (" + VersionInfos.formatVersionDate(VersionInfos.getLastVersion().getVersionDate()) + ")");
    }
    
    /**
     * Validation du backup
     * <BR>Relache également le lock sur la target, afin que d'autres opérations puissent être effectuées.
     */
    protected void commitBackup(ProcessContext context) throws ApplicationException {
    	try {
            this.getTaskMonitor().checkTaskCancellation();
            this.getTaskMonitor().setCancellable(false);
        	context.getManifest().addProperty("Filtered entries", "" + context.getReport().getFilteredEntries());
        	context.getManifest().addProperty("Backup duration", Utils.formatDuration(System.currentTimeMillis() - context.getReport().getStartMillis()));     
        	context.getManifest().addProperty("Target ID", this.getUid());
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
            this.getTaskMonitor().setCancellable(false);
            HistoryEntry entry = new HistoryEntry(HISTO_BACKUP_CANCEL, "Backup cancellation.");
			this.getHistory().addEntry(entry);
		} catch (IOException e1) {
			throw new ApplicationException(e1);
		} finally {
	        medium.rollbackBackup(context);
		}
    }
    
    /**
     * Merges the archive which are older than "delay" days.
     */
    public void processCompact(int delay, ProcessContext context) throws ApplicationException {
        GregorianCalendar mergeDate = new GregorianCalendar();
        mergeDate.add(Calendar.DATE, -1 * delay);
        
        processCompact(null, mergeDate, null, context);
    }
    
    /**
     * Lance la fusion sur la target
     */
    public void processCompact(String date, ProcessContext context) throws ApplicationException {
        processCompact(null, CalendarUtils.resolveDate(date, null), null, context);
    }
    
    /**
     * Lance la fusion sur la target
     */
    public void processCompact(
            GregorianCalendar fromDate, 
            GregorianCalendar toDate, 
            Manifest manifest,
            ProcessContext context
    ) throws ApplicationException {
        try {
            this.getProcess().getInfoChannel().logInfo(null, "Merge in progress ...");
            
    		try {
    		    
                HistoryEntry entry = new HistoryEntry(HISTO_MERGE, "Merge from " + Utils.formatDisplayDate(fromDate) + " to " + Utils.formatDisplayDate(toDate) + ".");
    			this.getHistory().addEntry(entry);
    		} catch (IOException e) {
    			throw new ApplicationException(e);
    		}        
    		this.medium.compact(fromDate, toDate, manifest, context);
    		this.commitCompact(context);
    	} catch (Exception e) {
    	    Logger.defaultLogger().error(e);
    		this.rollbackCompact(context);
    		if (e instanceof ApplicationException) {
    			throw (ApplicationException)e;
    		} else {
    			throw new ApplicationException(e);
    		}
    	} finally {
            this.getProcess().getInfoChannel().logInfo(null, "Merge completed.");
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
            this.getTaskMonitor().setCancellable(false);
            this.getProcess().getInfoChannel().logInfo(null, "Deletion in progress ...");
    		
    		try {
    		    if (this.getHistory() != null) {
    		        HistoryEntry entry = new HistoryEntry(HISTO_DELETE, "Archive deletion from " + Utils.formatDisplayDate(fromDate) + ".");
                	this.getHistory().addEntry(entry);
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
            this.getProcess().getInfoChannel().logInfo(null, "Deletion completed.");
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
     * Validation du compact
     * <BR>Relache également le lock sur la target, afin que d'autres opérations puissent être effectuées.
     */
    protected void commitCompact(ProcessContext context) throws ApplicationException {
        try {
            this.getTaskMonitor().checkTaskCancellation();
            this.getTaskMonitor().setCancellable(false);
            this.medium.commitCompact(context);
            context.getReport().setCommited();
        } catch (TaskCancelledException e) {
            throw new ApplicationException(e);
        }
    }
    
    /**
     * Annulation du compact
     * <BR>Relache également le lock sur la target, afin que d'autres opérations puissent être effectuées.
     */
    protected void rollbackCompact(ProcessContext context) throws ApplicationException {
		try {
            this.getTaskMonitor().setCancellable(false);
            
            HistoryEntry entry = new HistoryEntry(HISTO_MERGE_CANCEL, "Merge cancellation.");
			this.getHistory().addEntry(entry);
		} catch (IOException e1) {
			throw new ApplicationException(e1);
		} 
		
    	this.medium.rollbackCompact(context);
    }
    
    /**
     * Rétablit l'archive
     */
    public void processRecover(String destination, String[] filters, GregorianCalendar date, boolean recoverDeletedEntries, ProcessContext context) throws ApplicationException {
        TaskMonitor globalMonitor = this.getTaskMonitor().getCurrentActiveSubTask();
        try {
            String strDate = date == null ? "" : " as of " + CalendarUtils.getDateToString(date);
            this.getProcess().getInfoChannel().logInfo(null, "Recovery" + strDate + " in progress ...");
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
    			this.getHistory().addEntry(entry);
            } catch (IOException e1) {
                throw new ApplicationException(e1);
            }   
            this.processRecoverImpl(destination, filters, date, recoverDeletedEntries, context);
        } finally {
            this.getProcess().getInfoChannel().logInfo(null, "Recovery completed.");
            globalMonitor.enforceCompletion();
        }
    }
    
    /**
     * Rétablit une version d'une élément d'archive
     */
    public void processRecover(String destination, GregorianCalendar date, RecoveryEntry entry, ProcessContext context) throws ApplicationException {
        TaskMonitor globalMonitor = this.getTaskMonitor().getCurrentActiveSubTask();
        try {
            String strDate = date == null ? "" : " as of " + CalendarUtils.getDateToString(date);
            this.getProcess().getInfoChannel().logInfo(null, "Recovery of " + entry.getName() + strDate + " in progress ...");
            this.processRecoverImpl(destination, date, entry, context);
        } finally {
            this.getProcess().getInfoChannel().logInfo(null, "Recovery of " + entry.getName() + " completed.");
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
    protected boolean acceptEntry(RecoveryEntry entry, ProcessContext context) {
        Iterator iter = this.getFilterIterator();
        while (iter.hasNext()) {
            ArchiveFilter filter = (ArchiveFilter)iter.next();
            if (! filter.accept(entry)) {
                context.getReport().addFilteredEntry();
                if (requiresProcessReport() && filter.traceFilteredFiles()) {
                    context.getReport().getFilteredEntriesData().addFilteredEntry(entry, filter);
                }
                return false;
            }
        }
        return true;
    }
    
    protected boolean requiresProcessReport() {
        return 
        	ReportingConfiguration.getInstance().isReportingEnabled()
        	|| this.postProcessors.requiresProcessReport()
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
    
    public void secureUpdateCurrentTask(long taskIndex, long taskCount, String task) {
        try {
            this.process.getInfoChannel().updateCurrentTask(taskIndex, taskCount, task);
        } catch (Throwable e) {
            Logger.defaultLogger().error(e);
        }
    }
    
    public void secureUpdateCurrentTask(String task) {
        secureUpdateCurrentTask(0, 1, task);
    }
}

