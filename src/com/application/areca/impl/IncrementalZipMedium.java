package com.application.areca.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.application.areca.metadata.manifest.Manifest;
import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.archive.ArchiveAdapter;
import com.myJava.file.archive.ArchiveReader;
import com.myJava.file.archive.ArchiveWriter;
import com.myJava.file.archive.zip64.ZipArchiveAdapter;
import com.myJava.file.archive.zip64.ZipVolumeStrategy;
import com.myJava.file.multivolumes.VolumeStrategy;
import com.myJava.object.PublicClonable;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;

/**
 * Incremental storage support which uses an archive to store the data.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 8290826359148479344
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

    protected void storeFileInArchive(FileSystemRecoveryEntry entry, ProcessContext context) 
    throws ApplicationException {
        try {
            File file = entry.getFile();
            String path = entry.getName();
            
            if (context.getTaskMonitor() != null) {
                context.getTaskMonitor().checkTaskCancellation();
            }

            if (FileNameUtil.startsWithSeparator(path)) {
                path = path.substring(1);
            }
            
            long length = FileSystemManager.length(file);
            context.getArchiveWriter().getAdapter().addEntry(path, length);            
            InputStream in = FileSystemManager.getFileInputStream(entry.getFile());
            this.handler.store(entry, in, context.getArchiveWriter().getAdapter().getArchiveOutputStream(), context);
            context.getArchiveWriter().getAdapter().closeEntry();
        } catch (IOException e) {
            Logger.defaultLogger().error(e);
            throw new ApplicationException(e);
        } catch (TaskCancelledException e) {
            throw new ApplicationException(e);
        }
    }  

    public void open(Manifest manifest, ProcessContext context, String backupScheme) throws ApplicationException {
        if (overwrite) {
            // Delete all archives
            context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.2, "backup-delete");
            this.deleteArchives(null, context);
            context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.8, "backup-main");
        }
        super.open(manifest, context, backupScheme);
    }
    
    protected void closeArchive(ProcessContext context) throws IOException {
        if (context.getArchiveWriter() != null) {
            context.getArchiveWriter().close();
        }
    }

    public boolean supportsBackupScheme(String backupScheme) {
        return 
            super.supportsBackupScheme(backupScheme)
            && ! (backupScheme.equals(AbstractRecoveryTarget.BACKUP_SCHEME_INCREMENTAL) && this.overwrite);
    }

    /**
     * Builds an ArchiveAdapter for the file passed as argument. 
     */
    protected ArchiveAdapter buildArchiveAdapter(File f, boolean write) throws IOException {      
        ArchiveAdapter adapter = null;
        if (write) {
            if (compressionArguments.isMultiVolumes()) {
                adapter = new ZipArchiveAdapter(buildVolumeStrategy(f, write), compressionArguments.getVolumeSize() * 1024 * 1024, compressionArguments.isUseZip64());   
            } else {
                AbstractFileSystemMedium.tool.createDir(FileSystemManager.getParentFile(f));
                adapter =  new ZipArchiveAdapter(FileSystemManager.getFileOutputStream(f), compressionArguments.isUseZip64());   
            }
            if (compressionArguments.getComment()!= null) {
                adapter.setArchiveComment(compressionArguments.getComment());
            }
            
            return adapter;
        } else {
            if (compressionArguments.isMultiVolumes()) {
                adapter = new ZipArchiveAdapter(buildVolumeStrategy(f, write), 1);   
            } else {
                long length = 0;
                if (FileSystemManager.exists(f)) {
                    length = FileSystemManager.length(f);
                }
                adapter = new ZipArchiveAdapter(FileSystemManager.getFileInputStream(f), length);    
            }        
        }
        
        if (compressionArguments.getCharset() != null) {
            adapter.setCharset(compressionArguments.getCharset());
        }
        return adapter;
    }
    
    private VolumeStrategy buildVolumeStrategy(File f, boolean write) throws IOException {       
        if (write) {
            AbstractFileSystemMedium.tool.createDir(f);
        }
        return new ZipVolumeStrategy(new File(f, MV_ARCHIVE_NAME), compressionArguments.getExtension());
    }
    
    public PublicClonable duplicate() {
        IncrementalZipMedium other = new IncrementalZipMedium();
        copyAttributes(other);
        return other;
    }

    protected String getArchiveExtension() {
        return compressionArguments.isMultiVolumes() ? "" : compressionArguments.getExtension();
    }

    public File[] ensureLocalCopy(
            File[] archivesToProcess, 
            boolean overrideRecoveredFiles, 
            File destination, 
            String[] filters, 
            ProcessContext context
    ) throws IOException, ApplicationException {
        try {
            context.getInfoChannel().print("Data recovery ...");   
            File[] ret = new File[overrideRecoveredFiles ? 1 : archivesToProcess.length];
            
            for (int i=0; i<archivesToProcess.length; i++) {
                context.getTaskMonitor().checkTaskCancellation();      
                context.getInfoChannel().updateCurrentTask(i+1, archivesToProcess.length, "Recovering " + FileSystemManager.getPath(archivesToProcess[i]) + " ...");
                
                ArchiveReader zrElement = new ArchiveReader(buildArchiveAdapter(archivesToProcess[i], false));

                // Copie de l'élément en cours.
                File realDestination;
                if (overrideRecoveredFiles) {
                    realDestination = destination;
                    ret[0] = destination;
                } else {
                    realDestination = new File(destination, FileSystemManager.getName(archivesToProcess[i]));
                    ret[i] = realDestination;
                }
                zrElement.injectIntoDirectory(realDestination, filters, context.getTaskMonitor());
                zrElement.close();
                
                context.getTaskMonitor().getCurrentActiveSubTask().setCurrentCompletion(i+1, archivesToProcess.length);  
            }
            
            return ret;
        } catch (Throwable e) {
            throw new ApplicationException(e);
        }    
    }

    public void completeLocalCopyCleaning(File copy, ProcessContext context) throws IOException, ApplicationException {
        FileTool.getInstance().delete(copy, true);
    }

    public void cleanLocalCopies(List copies, ProcessContext context) throws IOException, ApplicationException {
        for (int i=0; i<copies.size(); i++) {
            File loc = (File)copies.get(i);
            FileSystemManager.delete(loc);
        }
    }

    protected void buildArchiveFromDirectory(File sourceDir, File destination, ProcessContext context) 
    throws ApplicationException {
        try {
            // Zippage du répertoire
            AbstractFileSystemMedium.tool.createDir(FileSystemManager.getParentFile(destination));
            context.setArchiveWriter(new ArchiveWriter(buildArchiveAdapter(destination, true)));
            context.getArchiveWriter().addFile(sourceDir, "", context.getTaskMonitor());
        } catch (IOException e) {
            throw new ApplicationException(e);
        } catch (TaskCancelledException e) {
            throw new ApplicationException(e);
        }
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
