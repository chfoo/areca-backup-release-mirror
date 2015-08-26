package com.application.areca;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import com.application.areca.context.ProcessContext;
import com.application.areca.impl.AggregatedViewContext;
import com.application.areca.impl.copypolicy.AbstractCopyPolicy;
import com.application.areca.indicator.IndicatorMap;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.transaction.TransactionPoint;
import com.application.areca.search.SearchCriteria;
import com.application.areca.search.TargetSearchResult;
import com.myJava.object.Duplicable;
import com.myJava.util.errors.ActionReport;
import com.myJava.util.history.HistoryHandler;
import com.myJava.util.taskmonitor.TaskCancelledException;

/**
 * <BR>Interface that defines an abstract storage medium.
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
public interface ArchiveMedium extends Duplicable {

    public static final short RECOVER_MODE_MERGE = 1;
    public static final short RECOVER_MODE_RECOVER = 2;
    public static final short RECOVER_MODE_CHECK = 3;
    
	/**
	 * Check the medium's state
	 */
    public ActionReport checkMediumState(int action);
    
    /**
     * Stores the entry passed as argument
     */
    public void store(RecoveryEntry entry, ProcessContext context) 
    throws StoreException, ApplicationException, TaskCancelledException;
    
    /**
     * Merge the archives that have been created between fromDate and toDate.
     */
    public void merge(
            GregorianCalendar fromDate, 
            GregorianCalendar toDate, 
            Manifest manifest,
            MergeParameters params,
            ProcessContext context
    ) throws ApplicationException, TaskCancelledException;

    /**
     * Delete the archives that have been created between fromDate and toDate
     */
    public void deleteArchives(
            GregorianCalendar fromDate,
            ProcessContext context            
    ) throws ApplicationException, TaskCancelledException;
    
    /**
     * Recovers all entries matching the filter passed as argument, as of the date passed
     * as argument.
     */
    public void recover(
            Object destination, 
            ArecaRawFileList filter,
            AbstractCopyPolicy policy,
            GregorianCalendar date,
            boolean keepDeletedEntries,
            boolean checkRecoveredFiles,
            ProcessContext context            
    ) throws ApplicationException, TaskCancelledException;
    
	/**
	 * Check the archive denoted by the date passed as argument.
	 * <BR>Files are recovered at the location passed as argument and verified against their hash code.
	 * <BR>The result is stored in the context (see {@link ProcessContext#getUncheckedRecoveredFiles()} and
	 * {@link ProcessContext#getInvalidRecoveredFiles()}).
	 * <BR>
	 * <BR>The destination can be null. In this case, archives are temporarily recovered in a subdirectory of
	 * their storage location.
	 */
	public void checkArchives(
			Object destination, 
			boolean checkOnlyArchiveContent, 
			boolean simulateRecovery,
			GregorianCalendar date, 
			Set ignoreList,
			ProcessContext context) 
	throws ApplicationException, TaskCancelledException;
    
    /**
     * Destroy all archives.
     * <BR>Useful when the target is deleted.
     */
    public void destroyRepository() 
    throws ApplicationException;
    
    /**
     * Open the medium. This method is called before the backup is performed.
     */
    public void open(Manifest manifest, TransactionPoint transactionPoint,ProcessContext context) 
    throws ApplicationException;
    
    /**
     * Validate the backup
     */
    public void commitBackup(ProcessContext context) 
    throws ApplicationException;
    
    /**
     * Save a temporary transaction point
     */
    public void initTransactionPoint(ProcessContext context)
    throws ApplicationException;
    
    public void handleTransactionPoint(ProcessContext context)
    throws ApplicationException;
    
    /**
     * Cancel the backup
     */
    public void rollbackBackup(ProcessContext context) 
    throws ApplicationException;
    
    /**
     * Validate the "merge"
     */
    public void preCommitMerge(ProcessContext context) 
    throws ApplicationException;
    
	public void finalizeMerge(ProcessContext context)
	throws ApplicationException;
    
    /**
     * Cancel the "merge"
     */
    public void rollbackMerge(ProcessContext context) 
    throws ApplicationException;
    
    /**
     * Return the target to which the medium is bound
     */
    public AbstractTarget getTarget();
    
    /**
     * Return a description of the storage medium 
     */
    public String getDescription();
    
    /**
     * Return the history of all operations performed on the medium (merges, backups, ...)
     */
    public HistoryHandler getHistoryHandler();
    
    /**
     * Return the content of the archive matching the date passed as argument
     */
    public List getEntries(AggregatedViewContext context, String root, GregorianCalendar date) 
    throws ApplicationException;
    
    /**
     * Return a "logical view" of the target's content
     */
    public List getLogicalView(AggregatedViewContext context, String root, boolean aggregatedView) 
    throws ApplicationException;
    
    /**
     * Return the entry's history
     */
    public EntryArchiveData[] getHistory(String entry) 
    throws ApplicationException;
    
    /**
     * Callback which is invoked before deletion
     */
    public void doBeforeDelete();
    
    /**
     * Callback which is invoked after deletion
     */
    public void doAfterDelete();
    
    /**
     * Simulates the processing of the entry passed as argument.
     * <BR>Used during backup simulation.
     */
    public void simulateEntryProcessing(RecoveryEntry entry, boolean haltOnFirstDifference, ProcessContext context) 
    throws ApplicationException, TaskCancelledException;
    
    /**
     * Closes the simulation and returns all unprocessed entries (ie entries which have been deleted). 
     */
    public void closeSimulation(ProcessContext context) 
    throws ApplicationException;

    /**
     * Tells whether it is useful to perform a pre-check before backup.
     * <BR>It can be used, for instance, to check whether at least one file has been modified.
     */
    public boolean isPreBackupCheckUseful();
    
    /**
     * Computes indicators on the archives stored by the medium.
     */
    public IndicatorMap computeIndicators() 
    throws ApplicationException, TaskCancelledException;
    
    /**
     * Searches entries within the archives 
     */
    public TargetSearchResult search(SearchCriteria criteria) 
    throws ApplicationException, TaskCancelledException;
    
    /**
     * Set up all necessary objects for the medium (for instance file system drivers)
     */
    public void install() 
    throws ApplicationException;
    
	public void deleteArchive(File archive) throws IOException;
    
    /**
     * Set the medium's target
     */
    public void setTarget(AbstractTarget target, boolean revalidate);
    
    /**
     * Tells whether the backup scheme passed as argument is supported by the medium or not
     */
    public boolean supportsBackupScheme(String backupScheme);
    
    /**
     * Checks the target's configuration
     */
	public boolean checkStupidConfigurations();
	
	/**
	 * Tells whether the medium supports resuming backups
	 */
	public String checkResumeSupported();
	
	/**
	 * Create a zip archive that contains archive / system / target informations
	 */
	public File createDebuggingData(File directory) throws ApplicationException, TaskCancelledException;
}