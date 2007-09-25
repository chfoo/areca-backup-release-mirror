package com.application.areca.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
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
import com.myJava.file.archive.zip64.ZipArchiveAdapter;
import com.myJava.file.archive.zip64.ZipVolumeStrategy;
import com.myJava.file.multivolumes.VolumeStrategy;
import com.myJava.object.PublicClonable;

/**
 * Incremental storage support which uses an archive to store the data.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 7453350623295719521
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
public class IncrementalZipMedium extends AbstractIncrementalFileSystemMedium {

    private static String MV_ARCHIVE_NAME = "archive";
    private boolean multiVolumes = false;
    private String comment;
    private Charset charset;
    
    /**
     * Volume Size (MB)
     */
    private long volumeSize = -1;
    private boolean useZip64 = false;
    
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
        File archive = context.getCurrentArchiveFile();
        context.setArchiveWriter(new ArchiveWriter(buildArchiveAdapter(archive, true)));
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
            context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.2);
            this.deleteArchives(null, context);
            context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.8);
        }
        super.open(context);
    }
    
    protected void closeArchive(ProcessContext context) throws IOException {
        if (context.getArchiveWriter() != null) {
            context.getArchiveWriter().close();
            
            /*
            if (context.getArchiveWriter().isEmpty()) {
                AbstractFileSystemMedium.tool.delete(context.getCurrentArchiveFile(), true);
                FileSystemManager.createNewFile(context.getCurrentArchiveFile());
            }
            */
        }
    }

    public String getComment() {
        return comment;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public void setComment(String comment) {
        if (comment != null && comment.trim().length() == 0) {
            this.comment = null;
        } else {
            this.comment = comment;
        }
    }

    /**
     * Builds an ArchiveAdapter for the file passed as argument. 
     */
    protected ArchiveAdapter buildArchiveAdapter(File f, boolean write) throws IOException {      
        ArchiveAdapter adapter = null;
        if (write) {
            if (multiVolumes) {
                adapter = new ZipArchiveAdapter(buildVolumeStrategy(f, write), volumeSize * 1024 * 1024, useZip64);   
            } else {
                AbstractFileSystemMedium.tool.createDir(FileSystemManager.getParentFile(f));
                adapter =  new ZipArchiveAdapter(FileSystemManager.getFileOutputStream(f), useZip64);   
            }
            if (comment != null) {
                adapter.setArchiveComment(comment);
            }
            
            return adapter;
        } else {
            if (multiVolumes) {
                adapter = new ZipArchiveAdapter(buildVolumeStrategy(f, write), 1);   
            } else {
                long length = 0;
                if (FileSystemManager.exists(f)) {
                    length = FileSystemManager.length(f);
                }
                adapter = new ZipArchiveAdapter(FileSystemManager.getFileInputStream(f), length);    
            }        
        }
        
        if (charset != null) {
            adapter.setCharset(charset);
        }
        return adapter;
    }
    
    private VolumeStrategy buildVolumeStrategy(File f, boolean write) throws IOException {       
        if (write) {
            AbstractFileSystemMedium.tool.createDir(f);
        }
        return new ZipVolumeStrategy(new File(f, MV_ARCHIVE_NAME));
    }
    
    public PublicClonable duplicate() {
        IncrementalZipMedium other = new IncrementalZipMedium();
        copyAttributes(other);
        return other;
    }
    
    protected void copyAttributes(Object clone) {
        super.copyAttributes(clone);
        
        IncrementalZipMedium other = (IncrementalZipMedium)clone;
        other.multiVolumes = this.multiVolumes;
        other.volumeSize = this.volumeSize;
        other.useZip64 = this.useZip64;
    }

    protected String getArchiveExtension() {
        return multiVolumes ? "" : ".zip";
    }

    public boolean isMultiVolumes() {
        return multiVolumes;
    }

    public void setMultiVolumes(boolean multiVolumes) {
        this.multiVolumes = multiVolumes;
    }

    public long getVolumeSize() {
        return volumeSize;
    }

    public void setVolumeSize(long volumeSize) {
        this.volumeSize = volumeSize;
    }

    public boolean isUseZip64() {
        return useZip64;
    }

    public void setUseZip64(boolean useZip64) {
        this.useZip64 = useZip64;
    }

    protected void archiveRawRecover(
            File[] elementaryArchives, 
            String[] entriesToRecover, 
            File targetFile,
            ProcessContext context
    ) throws ApplicationException {
        try {
            context.getInfoChannel().print("Data recovery ...");
            
            for (int i=0; i<elementaryArchives.length; i++) {
                context.getTaskMonitor().checkTaskCancellation();      
                context.getInfoChannel().updateCurrentTask(i+1, elementaryArchives.length, "Processing " + FileSystemManager.getPath(elementaryArchives[i]) + " ...");
                
                ArchiveReader zrElement = new ArchiveReader(buildArchiveAdapter(elementaryArchives[i], false));

                // Copie de l'élément en cours.
                zrElement.injectIntoDirectory(targetFile, entriesToRecover);
                zrElement.close();
                
                context.getTaskMonitor().getCurrentActiveSubTask().setCurrentCompletion(i+1, elementaryArchives.length);  
            }
        } catch (Throwable e) {
            throw new ApplicationException(e);
        }    
    }    
    
    protected void buildArchiveFromDirectory(File sourceDir, File destination, ProcessContext context) throws ApplicationException {
        try {
            // Zippage du répertoire
            AbstractFileSystemMedium.tool.createDir(FileSystemManager.getParentFile(destination));
            context.setArchiveWriter(new ArchiveWriter(buildArchiveAdapter(destination, true)));
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
        FileTool tool = FileTool.getInstance();
        
        try {
            FileSystemRecoveryEntry entry = (FileSystemRecoveryEntry)entryToRecover;

            if (entry.isLink()) {
                recoverSymLink(entry, archive, (File)destination);
            } else {            
                String fileName = entry.getName();
                File tmp = new File(fileName);
                
                File targetFile = new File((File)destination, FileSystemManager.getName(tmp));
                if (! FileSystemManager.exists(FileSystemManager.getParentFile(targetFile))) {
                    tool.createDir(FileSystemManager.getParentFile(targetFile));
                }
                ArchiveReader zf = new ArchiveReader(buildArchiveAdapter(archive, false));           
                InputStream zin = zf.getInputStream(entry.getName());
                if (zin != null) {
    	            OutputStream fout = FileSystemManager.getFileOutputStream(targetFile);
    	            tool.copy(zin, fout, true, true);
                }
                
                String hash = ArchiveTraceCache.getInstance().getTrace(this, archive).getFileHash(entry);
                FileSystemManager.setLastModified(targetFile, ArchiveTrace.extractFileModificationDateFromTrace(hash));
            }
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
