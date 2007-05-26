package com.application.areca.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.GregorianCalendar;

import com.application.areca.ApplicationException;
import com.application.areca.RecoveryEntry;
import com.application.areca.cache.ArchiveTraceCache;
import com.application.areca.context.ProcessContext;
import com.application.areca.metadata.trace.ArchiveTrace;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.archive.ArchiveAdapter;
import com.myJava.file.archive.ArchiveReader;
import com.myJava.file.archive.ArchiveWriter;

/**
 * Incremental storage support which uses an archive to store the data.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4945525256658487980
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
public abstract class AbstractIncrementalArchiveMedium extends AbstractIncrementalFileSystemMedium {

    public String getDescription() {
        String type = "incremental";
        if (overwrite) {
            type = "image"; 
        }
        return "Compressed " + type + " medium. (" + fileSystemPolicy.getBaseArchivePath() + ")";        
    }    
    
    /**
     * Construction de l'archive
     */
    protected void buildArchive(ProcessContext context) throws IOException {
    	super.buildArchive(context);
    	
        AbstractFileSystemMedium.tool.createDir(FileSystemManager.getParentFile(context.getCurrentArchiveFile()));
        File archive = context.getCurrentArchiveFile();
        context.setArchiveWriter(new ArchiveWriter(getArchiveAdapter(archive, true)));  
    }
    
    protected void storeFileInArchive(File file, String path, ProcessContext context) throws ApplicationException {
        try {
            context.getArchiveWriter().addFile(file, path);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    public void open(ProcessContext context) throws ApplicationException {
        if (overwrite) {
            // Delete all archives
            this.target.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.2);
            this.deleteArchives(null, context);
            this.target.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.8);
        }
        super.open(context);
    }
    
    protected void closeArchive(ProcessContext context) throws IOException {
        if (context.getArchiveWriter() != null) {
            context.getArchiveWriter().close();
            
            if (context.getArchiveWriter().isEmpty()) {
                AbstractFileSystemMedium.tool.delete(context.getCurrentArchiveFile(), true);
                FileSystemManager.createNewFile(context.getCurrentArchiveFile());
            }
        }
    }
    
    /**
     * Builds an ArchiveAdapter for the file passed as argument. 
     */
    protected ArchiveAdapter getArchiveAdapter(File f, boolean write) throws IOException {
        if (write) {
            return getArchiveAdapter(FileSystemManager.getFileOutputStream(f));
        } else {
            long length = 0;
            if (FileSystemManager.exists(f)) {
                length = FileSystemManager.length(f);
            }
            return getArchiveAdapter(FileSystemManager.getFileInputStream(f), length);            
        }
    }
    
    protected abstract ArchiveAdapter getArchiveAdapter(OutputStream out) throws IOException;
    protected abstract ArchiveAdapter getArchiveAdapter(InputStream in, long length) throws IOException;

    
    protected void archiveRawRecover(File[] elementaryArchives, String[] entriesToRecover, File targetFile) throws ApplicationException {
        try {
            this.target.getProcess().getInfoChannel().logInfo(null, "Data recovery ...");
            
            for (int i=0; i<elementaryArchives.length; i++) {
                this.target.getTaskMonitor().checkTaskCancellation();      
                this.target.getProcess().getInfoChannel().updateCurrentTask(i+1, elementaryArchives.length, "Processing " + FileSystemManager.getPath(elementaryArchives[i]) + " ...");
                
                ArchiveReader zrElement = new ArchiveReader(getArchiveAdapter(elementaryArchives[i], false));

                // Copie de l'élément en cours.
                zrElement.injectIntoDirectory(targetFile, entriesToRecover);
                zrElement.close();
                
                this.target.getTaskMonitor().getCurrentActiveSubTask().setCurrentCompletion(i+1, elementaryArchives.length);  
            }
        } catch (Throwable e) {
            throw new ApplicationException(e);
        }    
    }    
    
    protected void buildArchiveFromDirectory(File sourceDir, File destination, ProcessContext context) throws ApplicationException {
        try {
            // Zippage du répertoire
            AbstractFileSystemMedium.tool.createDir(FileSystemManager.getParentFile(destination));
            context.setArchiveWriter(new ArchiveWriter(getArchiveAdapter(destination, true)));
            context.getArchiveWriter().addFile(sourceDir, "");
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }    
	
    public void recoverEntry(
            GregorianCalendar date, 
            RecoveryEntry entryToRecover, 
            Object destination,
            ProcessContext context            
    ) throws ApplicationException {
        if (destination == null || entryToRecover == null) {
            return;
        }
        
        File archive = this.getLastArchive(date);
        FileTool tool = new FileTool();
        
        try {
            FileSystemRecoveryEntry entry = (FileSystemRecoveryEntry)entryToRecover;
            
            String fileName = entry.getName();
            File tmp = new File(fileName);
            
            File targetFile = new File((File)destination, FileSystemManager.getName(tmp));
            if (! FileSystemManager.exists(FileSystemManager.getParentFile(targetFile))) {
                tool.createDir(FileSystemManager.getParentFile(targetFile));
            }
            
            ArchiveReader zf = new ArchiveReader(getArchiveAdapter(archive, false));           
            InputStream zin = zf.getInputStream(entry.getName());
            if (zin != null) {
	            OutputStream fout = FileSystemManager.getFileOutputStream(targetFile);
	            tool.copy(zin, fout, true, true);
            }
            
            String hash = ArchiveTraceCache.getInstance().getTrace(this, archive).getFileHash(entry);
            FileSystemManager.setLastModified(targetFile, ArchiveTrace.extractFileModificationDateFromTrace(hash));
        } catch (Throwable e) {
            throw new ApplicationException(e);
        }
    }
    
    public boolean isCompressed() {
        return true;
    }
    
    /**
     * Registers a generic entry - wether it has been filtered or not.
     */
    protected void registerGenericEntry(FileSystemRecoveryEntry entry, ProcessContext context) throws IOException {
        context.getTraceAdapter().writeEntry(entry);
    }
    
    /**
     * Registers an entry after it has passed the filters. (hence a stored entry) 
     */
    protected void registerStoredEntry(FileSystemRecoveryEntry entry, ProcessContext context) throws IOException {
        context.getContentAdapter().writeEntry(entry);
    }
}
