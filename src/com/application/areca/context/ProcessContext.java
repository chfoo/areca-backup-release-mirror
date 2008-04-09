package com.application.areca.context;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.UserInformationChannel;
import com.application.areca.impl.tools.FileSystemLevel;
import com.application.areca.metadata.content.ArchiveContentAdapter;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.trace.ArchiveTrace;
import com.application.areca.metadata.trace.ArchiveTraceAdapter;
import com.myJava.file.MeteredOutputStreamListener;
import com.myJava.file.archive.ArchiveWriter;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2380639557663016217
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
public class ProcessContext {
    
    /**
     * Fichier pointant sur l'archive zip en cours de construction (qui sera renommee a l'issue
     * du commit en archive definitive)
     */
    protected File currentArchiveFile;
    
    /**
     * Writer pour la trace
     */
    protected ArchiveTraceAdapter traceAdapter;
    
    /**
     * Archive content adapter (used for read/write operations)
     */
    protected ArchiveContentAdapter contentAdapter;

    /**
     * Written bytes
     */
    protected long inputBytes = 0;
    
    /**
     * trace contenant les fichiers listes lors de la derniere execution.
     */
    protected ArchiveTrace previousTrace;
    
    /**
     * Tells wether the context have been initialized or not
     */
    protected boolean isInitialized;
    
    /**
     * Archive dans laquelle sera faite le backup.
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
    
    protected Stack fileSystemLevels;
    protected FileSystemLevel currentLevel;    
    
    protected ArrayList previousContents = new ArrayList();
    protected ArrayList previousArchives = new ArrayList();
    
    protected String backupScheme;
    
    protected Map sequenceRoots = new HashMap();
    
    protected File recoveryDestination;
    
    protected MeteredOutputStreamListener outputStreamListener = new MeteredOutputStreamListener();
    
    /**
     * Logger sp�cifique utilis� pour les retours utilisateur (typiquement : affichage � l'�cran)
     */
    private UserInformationChannel infoChannel;

    public void reset(boolean operationalOnly) {
        this.rootCount = 0;
        this.manifest = null;
        this.archiveWriter = null;
        this.contentAdapter = null;
        this.isInitialized = false;
        this.previousTrace = null;
        this.traceAdapter = null;
        inputBytes = 0;
        this.outputStreamListener = new MeteredOutputStreamListener();
        if (! operationalOnly) {
            this.getReport().reset();
        }
    }
    
    public ProcessContext(AbstractRecoveryTarget target, UserInformationChannel channel) {
        this(target, channel, null);
    }
    
    public ProcessContext(AbstractRecoveryTarget target, UserInformationChannel channel, TaskMonitor taskMonitor) {
        this.currentReport = new ProcessReport(target);
        this.fileSystemLevels = new Stack();
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

    public void setRecoveryDestination(File recoveryDestination) {
        this.recoveryDestination = recoveryDestination;
    }
    
	public MeteredOutputStreamListener getOutputStreamListener() {
		return this.outputStreamListener;
	}

	public ArrayList getPreviousArchives() {
        return previousArchives;
    }

    public Map getSequenceRoots() {
        return sequenceRoots;
    }

    public ArrayList getPreviousContents() {
        return previousContents;
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
    
    public File getCurrentArchiveFile() {
        return currentArchiveFile;
    }

    public Properties getOverridenDynamicProperties() {
        return overridenDynamicProperties;
    }

    public void setCurrentArchiveFile(File currentArchiveFile) {
        this.currentArchiveFile = currentArchiveFile;
    }
    
    public ArchiveTrace getPreviousTrace() {
        return previousTrace;
    }
    
    public void setPreviousTrace(ArchiveTrace previousTrace) {
        this.previousTrace = previousTrace;
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

    public FileSystemLevel getCurrentLevel() {
        return currentLevel;
    }
    
    public void setCurrentLevel(FileSystemLevel currentLevel) {
        this.currentLevel = currentLevel;
    }
    
    public Stack getFileSystemLevels() {
        return fileSystemLevels;
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
