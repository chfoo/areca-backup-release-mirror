package com.application.areca.context;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.application.areca.AbstractTarget;
import com.application.areca.UserInformationChannel;
import com.application.areca.metadata.content.ArchiveContentAdapter;
import com.application.areca.metadata.content.ContentFileIterator;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.trace.ArchiveTraceAdapter;
import com.application.areca.metadata.trace.TraceFileIterator;
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
public class ProcessContext {
    
    /**
     * Archive being written
     */
    protected File currentArchiveFile;
    
    /**
     * Trace writer
     */
    protected ArchiveTraceAdapter traceAdapter;
    
    /**
     * Archive content adapter (used for read/write operations)
     */
    protected ArchiveContentAdapter contentAdapter;
    protected ArchiveContentAdapter hashAdapter;
    protected ArchiveContentAdapter sequenceAdapter;

    /**
     * Written bytes
     */
    protected long inputBytes = 0;
    
    /**
     * Trace file - as of last backup
     */
    protected TraceFileIterator referenceTrace;
    
    /**
     * Tell whether the context have been initialized or not
     */
    protected boolean isInitialized;
    
    /**
     * Archive writer
     */
    private ArchiveWriter archiveWriter;   
    
    /**
     * Manifest
     */
    protected Manifest manifest;
    
    /**
     * Nr of sources to store
     */
    protected int rootCount;
    
    protected Properties overridenDynamicProperties = new Properties();
    
    /**
     * Report being built during the process
     */
    protected ProcessReport currentReport;
    
    protected FileSystemIterator fileSystemIterator;  
    
    /**
     * Iterator used by delta handlers to locate the HashSequence
     */
    protected ArrayList contentIterators = new ArrayList();
    
    protected String backupScheme;
    
    protected Map sequenceRoots = new HashMap();
    
    protected File recoveryDestination;
    
    protected ArrayList simulationResult;
    
    protected MeteredOutputStreamListener outputStreamListener = new MeteredOutputStreamListener();
    
    private UserInformationChannel infoChannel;
    
    /**
     * This object is used to keep the archive's content in case of uncompressed "image" targets.
     * <BR>See "prepareContext()" method.
     */
    private ContentFileIterator previousHashIterator;
    
    private List invalidRecoveredFiles = new ArrayList();
    private List uncheckedRecoveredFiles = new ArrayList();
    private List unrecoveredFiles = new ArrayList();
    private long nbChecked = 0;
    
    public void reset(boolean operationalOnly) {
        this.rootCount = 0;
        this.manifest = null;
        this.archiveWriter = null;
        this.hashAdapter = null;
        this.contentAdapter = null;
        this.sequenceAdapter = null;
        this.isInitialized = false;
        this.referenceTrace = null;
        this.traceAdapter = null;
        this.contentIterators.clear();
        this.sequenceRoots.clear();
        this.simulationResult = null;
        this.invalidRecoveredFiles.clear();
        this.uncheckedRecoveredFiles.clear();
        this.unrecoveredFiles.clear();
        this.previousHashIterator = null;
        this.nbChecked = 0;
        this.inputBytes = 0;
        this.outputStreamListener = new MeteredOutputStreamListener();
        if (! operationalOnly) {
            this.getReport().reset();
        }
    }
    
    public ProcessContext(AbstractTarget target, UserInformationChannel channel) {
        this(target, channel, null);
    }
    
    public void addChecked() {
    	this.nbChecked++;
    }

    public long getNbChecked() {
		return nbChecked;
	}

	public ProcessContext(AbstractTarget target, UserInformationChannel channel, TaskMonitor taskMonitor) {
        this.currentReport = new ProcessReport(target);
        this.infoChannel = channel;
        if (taskMonitor != null) {
            this.infoChannel.setTaskMonitor(taskMonitor);
        }
    }
    
    public ArchiveContentAdapter getContentAdapter() {
        return contentAdapter;
    }

    public File getRecoveryDestination() {
        return recoveryDestination;
    }

    public ArrayList getSimulationResult() {
		return simulationResult;
	}

	public void setSimulationResult(ArrayList simulationResult) {
		this.simulationResult = simulationResult;
	}

	public void setRecoveryDestination(File recoveryDestination) {
        this.recoveryDestination = recoveryDestination;
    }

	public MeteredOutputStreamListener getOutputStreamListener() {
		return this.outputStreamListener;
	}

    public Map getSequenceRoots() {
        return sequenceRoots;
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

	public Properties getOverridenDynamicProperties() {
        return overridenDynamicProperties;
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
        return currentReport;
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

    public int getRootCount() {
        return rootCount;
    }

    public void setRootCount(int rootCount) {
        this.rootCount = rootCount;
    }
}
