package com.application.areca.context;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import com.application.areca.AbstractTarget;
import com.application.areca.UserInformationChannel;
import com.application.areca.impl.tools.RecoveryFilterMap;
import com.application.areca.metadata.content.ArchiveContentAdapter;
import com.application.areca.metadata.content.ContentFileIterator;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.trace.ArchiveTraceAdapter;
import com.application.areca.metadata.trace.TraceFileIterator;
import com.application.areca.metadata.transaction.TransactionPoint;
import com.myJava.file.FileSystemManager;
import com.myJava.file.MeteredOutputStreamListener;
import com.myJava.file.archive.ArchiveWriter;
import com.myJava.file.iterator.FileSystemIterator;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2011, Olivier PETRUCCI.

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
public class ProcessContext implements Externalizable {
    
    /**
	 * The ProcessContext is serializable, and needs a serial version uid
	 */
	private static final long serialVersionUID = 102772337124247716L;

	////////////////
	// BACKUP
	////////////////
	
	/**
     * Archive being written
     */
    protected File currentArchiveFile;
    
    /**
     * Archive content adapter (used for read/write operations)
     */
    protected ArchiveContentAdapter contentAdapter;
    protected ArchiveContentAdapter hashAdapter;
    protected ArchiveContentAdapter sequenceAdapter;
	
    /**
     * Read bytes
     */
    protected long inputBytes = 0;
    
    /**
     * Trace iterator - as of last backup
     */
    protected TraceFileIterator referenceTrace;
    
    /**
     * Archive writer
     */
    private ArchiveWriter archiveWriter; 
    
    /**
     * Trace writer
     */
    protected ArchiveTraceAdapter traceAdapter;
    
    protected String backupScheme;
    
    /**
     * Current transaction point (the one being written)
     */
    private TransactionPoint currentTransactionPoint;

    /**
     * Index used during backup
     */
    private long entryIndex;
    
    private long transactionBound;
    
	////////////////
	// MERGE
	////////////////
	
	
	////////////////
	// RECOVERY
	////////////////
    protected File recoveryDestination;
    private List invalidRecoveredFiles = new ArrayList();
    private List uncheckedRecoveredFiles = new ArrayList();
    private List unrecoveredFiles = new ArrayList();
    private long nbChecked = 0;
	
    /**
     * Number of recovery errors that were explained in detail (used to limit the number of displayed messages)
     */
    private int detailedRecoveryErrors;
    
    /**
     * Temporary variable set during recovery - used for logging purpose
     */
    private RecoveryFilterMap filesByArchive;
    
    /**
     * Tracefile used for recovery : tells which files have to be recovered + contains some metadata
     * (last modification date)
     */
    private File traceFile;
    
	////////////////
	// ALL
	////////////////
    
    /**
     * Manifest
     */
    protected Manifest manifest;
    
    /**
     * Report being built during the process
     */
    protected ProcessReport report;
    
    private UserInformationChannel infoChannel;
    
	////////////////
	// ???
	////////////////
    
    /**
     * Tell whether the context have been initialized or not
     */
    protected boolean isInitialized;

    protected FileSystemIterator fileSystemIterator;  
    
    /**
     * Iterator used by delta handlers to locate the HashSequence
     */
    protected ArrayList contentIterators = new ArrayList();

    protected MeteredOutputStreamListener outputStreamListener = new MeteredOutputStreamListener();

    /**
     * This object is used to keep the archive's content in case of uncompressed "image" targets.
     * <BR>See "prepareContext()" method.
     */
    private ContentFileIterator previousHashIterator;
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	entryIndex = in.readLong();
		inputBytes = in.readLong();
		isInitialized = in.readBoolean();
		nbChecked = in.readLong();
		currentArchiveFile = (File)in.readObject();
		backupScheme = (String)in.readObject();
		outputStreamListener = (MeteredOutputStreamListener)in.readObject();
		manifest = (Manifest)in.readObject();
		report = (ProcessReport)in.readObject();
		fileSystemIterator = (FileSystemIterator)in.readObject();
		transactionBound = in.readLong();
		
		String hashPath = (String)in.readObject();
		if (hashPath != null) {
			File hashFile = new File(hashPath);
			if (FileSystemManager.exists(hashFile)) {
				previousHashIterator = ArchiveContentAdapter.buildIterator(hashFile);
			}
		}
		
		String tracePath = (String)in.readObject();
		if (tracePath != null) {
			File traceFile = new File(tracePath);
			if (FileSystemManager.exists(traceFile)) {
				referenceTrace = ArchiveTraceAdapter.buildIterator(traceFile);
			}
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		// Ignored
		//ArchiveWriter archiveWriter; -> Used by "zip" archives
		//List invalidRecoveredFiles = new ArrayList(); -> Not used during backup
		//List uncheckedRecoveredFiles = new ArrayList(); -> Not used during backup
		//List unrecoveredFiles = new ArrayList(); -> Not used during backup
		//out.writeObject(contentIterators);  -> Ignored because these iterators are already closed by the "rollback" method
		//out.writeObject(recoveryDestination); -> Not used during backup
		//filesByArchive
		//detailedRecoveryErrors
		//traceFile
		
		// Initialized afterwards
	    //ArchiveTraceAdapter traceAdapter;
		//ArchiveContentAdapter contentAdapter;
		//ArchiveContentAdapter hashAdapter;
		//ArchiveContentAdapter sequenceAdapter;
		//UserInformationChannel infoChannel;
		//out.writeObject(currentTransactionPoint);
		
		out.writeLong(entryIndex);
		out.writeLong(inputBytes);
		out.writeBoolean(isInitialized);
		out.writeLong(nbChecked);
		out.writeObject(currentArchiveFile);
		out.writeObject(backupScheme);
		out.writeObject(outputStreamListener);
		out.writeObject(manifest);
		out.writeObject(report);
		out.writeObject(fileSystemIterator);
		out.writeLong(transactionBound);
		
		String hashPath = previousHashIterator == null ? null : FileSystemManager.getAbsolutePath(previousHashIterator.getSource());
		out.writeObject(hashPath);
		
		String tracePath = referenceTrace == null ? null : FileSystemManager.getAbsolutePath(referenceTrace.getSource());
		out.writeObject(tracePath);
	}

	public void reset(boolean operationalOnly) {
		this.entryIndex = 0;
        this.manifest = null;
        this.archiveWriter = null;
        this.hashAdapter = null;
        this.contentAdapter = null;
        this.sequenceAdapter = null;
        this.isInitialized = false;
        this.referenceTrace = null;
        this.traceAdapter = null;
        this.contentIterators.clear();
        this.invalidRecoveredFiles.clear();
        this.uncheckedRecoveredFiles.clear();
        this.unrecoveredFiles.clear();
        this.previousHashIterator = null;
        this.detailedRecoveryErrors = 0;
        this.traceFile = null;
        this.nbChecked = 0;
        this.inputBytes = 0;
        this.filesByArchive = null;
        this.outputStreamListener = new MeteredOutputStreamListener();
        if (! operationalOnly) {
            this.getReport().reset();
        }
    }

	public long getTransactionBound() {
		return transactionBound;
	}
	
	public void addDetailedRecoveryError() {
		detailedRecoveryErrors++;
	}

	public int getDetailedRecoveryErrors() {
		return detailedRecoveryErrors;
	}

	public void setTransactionBound(long transactionBound) {
		this.transactionBound = transactionBound;
	}

	public ProcessContext() {
		this(null, null);
	}

    public ProcessContext(AbstractTarget target, UserInformationChannel channel) {
        this(target, channel, null);
    }

	public ProcessContext(AbstractTarget target, UserInformationChannel channel, TaskMonitor taskMonitor) {
        this.report = new ProcessReport(target);
        this.infoChannel = channel;
        if (taskMonitor != null) {
            this.infoChannel.setTaskMonitor(taskMonitor);
            this.infoChannel.setContext(this);
        }
    }

	public void setReport(ProcessReport currentReport) {
		this.report = currentReport;
	}
	
	public RecoveryFilterMap getFilesByArchive() {
		return filesByArchive;
	}

	public void setFilesByArchive(RecoveryFilterMap filesByArchive) {
		this.filesByArchive = filesByArchive;
	}

	public TransactionPoint getCurrentTransactionPoint() {
		return currentTransactionPoint;
	}

	public void setCurrentTransactionPoint(TransactionPoint currentTP) {
		this.currentTransactionPoint = currentTP;
	}

	public void addChecked() {
    	this.nbChecked++;
    }

    public long getNbChecked() {
		return nbChecked;
	}
    
    public ArchiveContentAdapter getContentAdapter() {
        return contentAdapter;
    }

    public File getRecoveryDestination() {
        return recoveryDestination;
    }

	public void setRecoveryDestination(File recoveryDestination) {
        this.recoveryDestination = recoveryDestination;
    }

	public MeteredOutputStreamListener getOutputStreamListener() {
		return this.outputStreamListener;
	}

    public ArrayList getContentIterators() {
		return contentIterators;
	}

	public String getBackupScheme() {
        return backupScheme;
    }

    public void setBackupScheme(String backupScheme) {
        this.backupScheme = backupScheme;
    }

    public void setContentAdapter(ArchiveContentAdapter contentAdapter) {
        this.contentAdapter = contentAdapter;
    }

    public ArchiveContentAdapter getSequenceAdapter() {
		return sequenceAdapter;
	}

	public void setSequenceAdapter(ArchiveContentAdapter sequenceAdapter) {
		this.sequenceAdapter = sequenceAdapter;
	}

	public File getCurrentArchiveFile() {
        return currentArchiveFile;
    }

	public List getInvalidRecoveredFiles() {
		return invalidRecoveredFiles;
	}
	
	public List getUncheckedRecoveredFiles() {
		return uncheckedRecoveredFiles;
	}

	public List getUnrecoveredFiles() {
		return unrecoveredFiles;
	}
	
	public boolean hasRecoveryIssues() {
		return 
			(invalidRecoveredFiles != null && invalidRecoveredFiles.size() > 0)
			|| (uncheckedRecoveredFiles != null && uncheckedRecoveredFiles.size() > 0)
			|| (unrecoveredFiles != null && unrecoveredFiles.size() > 0);
	}

    public void setCurrentArchiveFile(File currentArchiveFile) {
        this.currentArchiveFile = currentArchiveFile;
    }

    public TraceFileIterator getReferenceTrace() {
		return referenceTrace;
	}

	public void setReferenceTrace(TraceFileIterator referenceTrace) {
		this.referenceTrace = referenceTrace;
	}

	public ArchiveContentAdapter getHashAdapter() {
		return hashAdapter;
	}

	public void setHashAdapter(ArchiveContentAdapter hashAdapter) {
		this.hashAdapter = hashAdapter;
	}

	public ArchiveTraceAdapter getTraceAdapter() {
        return traceAdapter;
    }
    
    public void setTraceAdapter(ArchiveTraceAdapter traceAdapter) {
        this.traceAdapter = traceAdapter;
    } 

	public boolean isInitialized() {
        return isInitialized;
    }
    
    public void setInitialized() {
        this.isInitialized = true;
    }
    
    public ArchiveWriter getArchiveWriter() {
        return archiveWriter;
    }
    
    public void setArchiveWriter(ArchiveWriter archive) {
        this.archiveWriter = archive;
    }
    
    public Manifest getManifest() {
        return manifest;
    }
    
    public void setManifest(Manifest manifest) {
        this.manifest = manifest;
    }
    
    public ProcessReport getReport() {
        return report;
    }

    public ContentFileIterator getPreviousHashIterator() {
		return previousHashIterator;
	}

	public void setPreviousHashIterator(ContentFileIterator previousHashIterator) {
		this.previousHashIterator = previousHashIterator;
	}

	public void addInputBytes(long w) {
        inputBytes += w;
    }
    
    public long getInputBytesInKB() {
        return (long)(inputBytes / 1024.0);
    }
    
    public long getInputBytesInKBPerSecond() {
        return (long)(1000.0 / 1024.0 * inputBytes / (getReport().getDataFlowStop() - getReport().getDataFlowStart()));
    }
    
    public long getOutputBytesInKB() {
        return (long)(getOutputStreamListener().getWritten() / 1024.0);
    }
    
    public long getOutputBytesInKBPerSecond() {
        return (long)(1000.0 / 1024.0 * getOutputStreamListener().getWritten() / (getReport().getDataFlowStop() - getReport().getDataFlowStart()));
    }

    public FileSystemIterator getFileSystemIterator() {
		return fileSystemIterator;
	}

	public void setFileSystemIterator(FileSystemIterator fileSystemIterator) {
		this.fileSystemIterator = fileSystemIterator;
	}

	public UserInformationChannel getInfoChannel() {
        return infoChannel;
    }
    
    public TaskMonitor getTaskMonitor() {
        return infoChannel.getTaskMonitor();
    }

	public void setEntryIndex(long entryIndex) {
		this.entryIndex = entryIndex;
	}

	public long getEntryIndex() {
		return entryIndex;
	}
	
	public void incrementEntryIndex() {
		this.entryIndex++;
	}

	public long getInputBytes() {
		return inputBytes;
	}

	public void setInputBytes(long inputBytes) {
		this.inputBytes = inputBytes;
	}

	public void setInitialized(boolean isInitialized) {
		this.isInitialized = isInitialized;
	}

	public void setNbChecked(long nbChecked) {
		this.nbChecked = nbChecked;
	}

	public void setOutputStreamListener(MeteredOutputStreamListener outputStreamListener) {
		this.outputStreamListener = outputStreamListener;
	}

	public File getTraceFile() {
		return traceFile;
	}

	public void setTraceFile(File traceFile) {
		this.traceFile = traceFile;
	}
}
