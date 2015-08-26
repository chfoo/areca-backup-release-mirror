package com.application.areca.context;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.GregorianCalendar;

import com.application.areca.AbstractTarget;
import com.application.areca.indicator.IndicatorMap;
import com.myJava.util.log.LogMessagesContainer;
import com.myJava.util.log.Logger;

/**
 * Contains reporting data.
 * <BR>ProcessReports are created during backup/recovery/merge processes.
 * <BR>TODO : refactor : create specific implementations that match the processes needs in terms of fields / methods (and remove the "mergeperformed" ... fields)
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
public class ProcessReport implements Serializable {
	private static final long serialVersionUID = 6295165918784264911L;

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
     * Start date used to compute the data flow
     */
    protected long dataFlowStart = 0;
    
    /**
     * Stop date used to compute the data flow
     */
    protected long dataFlowStop = 0;
    
    /**
     * Number of processed files
     */
    protected int unfilteredFiles;    
    
    /**
     * Archives which have been restored during the recovery/merge process
     */
    protected transient RecoveryResult recoveryResult;
    
    /**
     * Process start date (ms)
     */
    protected long startMillis = System.currentTimeMillis();

    /**
     * Process stop date (ms)
     */
    protected long stopMillis = startMillis;
    
    protected transient AbstractTarget target;
    
    /**
     * nr of written kbytes
     */
    protected long writtenKBytes;
    
    /**
     * Check : number of checked files
     */
    protected long nbChecked = 0;
    
    /**
     * Check : List of recovered files that were detected as invalid
     */
    protected transient MaxCapacityList invalidRecoveredFiles = new MaxCapacityList();
    
    /**
     * Check : List of recovered files that could not be checked
     */
    protected transient MaxCapacityList uncheckedRecoveredFiles = new MaxCapacityList();
    
    /**
     * Check : List of files that were not recovered
     */
    protected transient MaxCapacityList unrecoveredFiles = new MaxCapacityList();

    /**
     * Tells whether the process has errors
     */
    protected StatusList status = new StatusList();
    
    protected LogMessagesContainer logMessagesContainer;
    
    private transient IndicatorMap indicators;
    
    /**
     * Content file of the created archive
     */
    private transient File contentFile;
    
    private ProcessReport() {
        this.logMessagesContainer = Logger.defaultLogger().getTlLogProcessor().getMessageContainer();
    }
    
    public ProcessReport(AbstractTarget target) {
    	this();
        this.target = target;
    } 

    public void setTarget(AbstractTarget target) {
		this.target = target;
	}

	public AbstractTarget getTarget() {
        return target;
    }
	
	public void overrideStatus(StatusList sl) {
		this.status = sl;
	}
    
    /**
     * Resets all counters ... except the duration counter
     */
    public void reset() {
    	//this.action = TargetActions.ACTION_UNKNOWN; > Never reset : the main action doesn't change
        this.unfilteredDirectories = 0;
        this.unfilteredFiles = 0;
        this.filteredEntries = 0;
        this.ignoredFiles = 0;
        this.recoveryResult = null;
        this.savedFiles = 0;
        this.invalidRecoveredFiles.clear();
        this.uncheckedRecoveredFiles.clear();
        this.unrecoveredFiles.clear();
        this.nbChecked = 0;
        if (this.logMessagesContainer != null) {
        	this.logMessagesContainer.clear();
        }
    }

    public RecoveryResult getRecoveryResult() {
        return recoveryResult;
    }

    public void setRecoveryResult(RecoveryResult recoveryResult) {
        this.recoveryResult = recoveryResult;
    }

    public GregorianCalendar getStartDate() {
        Date d = new Date(this.startMillis);
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(d);
        return c;
    }
    
	public void addChecked() {
    	this.nbChecked++;
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
    
    public long getDataFlowTimeInSecond() {
        return (long)((dataFlowStop - dataFlowStart)/1000.0);
    }
    
    public int getProcessedEntries() {
        return this.filteredEntries + this.unfilteredDirectories + this.unfilteredFiles;
    }
    
    public int getSavedFiles() {
        return savedFiles;
    }
    
    public long getNbChecked() {
		return nbChecked;
	}

	public MaxCapacityList getInvalidRecoveredFiles() {
		return invalidRecoveredFiles;
	}
	
	public void addInvalidRecoveredFile(String file) {
		invalidRecoveredFiles.add(file);
	}
	
	public MaxCapacityList getUncheckedRecoveredFiles() {
		return uncheckedRecoveredFiles;
	}
	
	public void addUncheckedRecoveredFile(String file) {
		uncheckedRecoveredFiles.add(file);
	}

	public MaxCapacityList getUnrecoveredFiles() {
		return unrecoveredFiles;
	}
	
	public void addUnrecoveredFile(String file) {
		unrecoveredFiles.add(file);
	}

	public boolean hasRecoveryIssues() {
		return 
			(invalidRecoveredFiles != null && invalidRecoveredFiles.size() > 0)
			|| (uncheckedRecoveredFiles != null && uncheckedRecoveredFiles.size() > 0)
			|| (unrecoveredFiles != null && unrecoveredFiles.size() > 0);
	}
    
    public void addSavedFile() {
        this.savedFiles++;
    }
    
    public void addIgnoredFile() {
        this.ignoredFiles++;
    }  
    
    public void addDeletedFile() {
        this.deletedFiles++;
    }   

    public void setFilteredEntries(int filteredEntries) {
		this.filteredEntries = filteredEntries;
	}

	public long getStartMillis() {
        return startMillis;
    }

    public long getStopMillis() {
		return stopMillis;
	}
    
    public void setStopMillis() {
    	this.stopMillis = System.currentTimeMillis();
    }

	public IndicatorMap getIndicators() {
		return indicators;
	}

	public void setIndicators(IndicatorMap indicators) {
		this.indicators = indicators;
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

    public void setUnfilteredDirectories(int unfilteredDirectories) {
		this.unfilteredDirectories = unfilteredDirectories;
	}

	public void setUnfilteredFiles(int unfilteredFiles) {
		this.unfilteredFiles = unfilteredFiles;
	}

    public long getDataFlowStart() {
		return this.dataFlowStart;
	}

	public long getDataFlowStop() {
		return this.dataFlowStop;
	}

	public StatusList getStatus() {
		return status;
	}

	public int getDeletedFiles() {
        return deletedFiles;
    }

    public void setDeletedFiles(int deletedFiles) {
        this.deletedFiles = deletedFiles;
    }

	public LogMessagesContainer getLogMessagesContainer() {
		return logMessagesContainer;
	}

	public void setLogMessagesContainer(LogMessagesContainer logMessagesContainer) {
		this.logMessagesContainer = logMessagesContainer;
	}

	public long getWrittenKBytes() {
		return writtenKBytes;
	}

	public void setWrittenKBytes(long writtenBytes) {
		this.writtenKBytes = writtenBytes;
	}
	
	public boolean hasError() {
		return this.status.hasError() || this.logMessagesContainer.hasErrors();
	}
	
	public boolean hasWarnings() {
		return this.logMessagesContainer.hasWarnings();
	}

	public File getContentFile() {
		return contentFile;
	}

	public void setContentFile(File contentFile) {
		this.contentFile = contentFile;
	}
}
