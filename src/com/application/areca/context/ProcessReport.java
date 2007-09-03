package com.application.areca.context;

import java.io.File;
import java.util.Date;
import java.util.GregorianCalendar;

import com.application.areca.AbstractRecoveryTarget;

/**
 * Contains reporting data.
 * <BR>ProcessReports are created during backup/recovery/merge processes.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -2622785387388097396
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
public class ProcessReport {

    /**
     * Ignored files (because not modified)
     */
    protected int ignoredFiles = 0;
    
    /**
     * Number of really stored files
     */
    protected int savedFiles = 0;   
    
    /**
     * Number of files which have been filtered
     */
    protected int filteredEntries = 0;
    
    /**
     * Number of files that have not been found in the source directory
     * --> That means that they have been deleted from the disk
     */
    protected int deletedFiles = 0;
    
    /**
     * Number of processed directories
     */
    protected int unfilteredDirectories;

    /**
     * Number of processed files
     */
    protected int unfilteredFiles;    
    
    /**
     * Archives which have been restored during the recovery/merge process
     */
    protected File[] recoveredFiles;
    
    /**
     * Process start date (ms)
     */
    protected long startMillis = System.currentTimeMillis();
    
    /**
     * Map of filtered entries
     */
    protected FilteredEntries filteredEntriesData;

    protected AbstractRecoveryTarget target;
    
    /**
     * Start date used to compute the data flow
     */
    protected long dataFlowStart = 0;
    
    /**
     * Stop date used to compute the data flow
     */
    protected long dataFlowStop = 0;
    
    /**
     * Written bytes
     */
    protected long written = 0;

    /**
     * Tells wether the process is being validated
     */
    protected boolean commited = false;
    
    public ProcessReport(AbstractRecoveryTarget target) {
        this.target = target;
    } 
    
    public AbstractRecoveryTarget getTarget() {
        return target;
    }
    /**
     * Resets all counters ... except the duration counter
     */
    public void reset() {
        this.unfilteredDirectories = 0;
        this.unfilteredFiles = 0;
        this.filteredEntriesData = new FilteredEntries();
        this.filteredEntries = 0;
        this.ignoredFiles = 0;
        this.recoveredFiles = null;
        this.savedFiles = 0;
    }
    
    public File[] getRecoveredFiles() {
        return recoveredFiles;
    }
    
    public void setRecoveredFiles(File[] recoveredFiles) {
        this.recoveredFiles = recoveredFiles;
    }
    
    public GregorianCalendar getStartDate() {
        Date d = new Date(this.startMillis);
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(d);
        return c;
    }
    
    public int getIgnoredFiles() {
        return ignoredFiles;
    }
    
    public void startDataFlowTimer() {
        dataFlowStart = System.currentTimeMillis();
    }
    
    public void stopDataFlowTimer() {
        dataFlowStop = System.currentTimeMillis() + 1;
    }
    
    public void addWritten(long w) {
        written += w;
    }
    
    public long getDataFlowTimeInSecond() {
        return (long)((dataFlowStop - dataFlowStart)/1000.0);
    }
    
    public long getWrittenInKB() {
        return (long)(written / 1024.0);
    }
    
    public long getDataFlowInKBPerSecond() {
        return (long)(1000.0 / 1024.0 * written / (dataFlowStop - dataFlowStart));
    }
    
    public int getProcessedEntries() {
        return this.filteredEntries + this.unfilteredDirectories + this.unfilteredFiles;
    }
    
    public int getSavedFiles() {
        return savedFiles;
    }
    
    public void addSavedFile() {
        this.savedFiles++;
    }
    
    public void addIgnoredFile() {
        this.ignoredFiles++;
    }   
    
    public void addFilteredEntry() {
        this.filteredEntries++;
    }   

    public long getStartMillis() {
        return startMillis;
    }
    
    public int getFilteredEntries() {
        return filteredEntries;
    }

    public int getUnfilteredDirectories() {
        return unfilteredDirectories;
    }
    
    public int getUnfilteredFiles() {
        return unfilteredFiles;
    }
    
    public void addDirectoryCount() {
        this.unfilteredDirectories++;
    }
    
    public void addFileCount() {
        this.unfilteredFiles++;
    }

    public FilteredEntries getFilteredEntriesData() {
        if (filteredEntriesData == null) {
            filteredEntriesData = new FilteredEntries();
        }
        
        return filteredEntriesData;
    }
    

    public boolean isCommited() {
        return commited;
    }
    
    public void setCommited() {
        this.commited = true;
    }
    
    public int getDeletedFiles() {
        return deletedFiles;
    }

    public void setDeletedFiles(int deletedFiles) {
        this.deletedFiles = deletedFiles;
    }
}
