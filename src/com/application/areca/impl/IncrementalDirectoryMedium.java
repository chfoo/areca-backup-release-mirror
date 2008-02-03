package com.application.areca.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.application.areca.ApplicationException;
import com.application.areca.cache.ArchiveTraceCache;
import com.application.areca.context.ProcessContext;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.driver.CompressedFileSystemDriver;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.object.PublicClonable;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;

/**
 * Support incrémental non compressé (répertoire)
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
public class IncrementalDirectoryMedium extends AbstractIncrementalFileSystemMedium {

    public PublicClonable duplicate() {
        IncrementalDirectoryMedium other = new IncrementalDirectoryMedium();
        copyAttributes(other);
        return other;
    }

    protected FileSystemDriver buildStorageDriver(File storageDir) throws ApplicationException {
        FileSystemDriver driver = super.buildStorageDriver(storageDir);

        if (this.compressionArguments.isCompressed()) {
            driver = new CompressedFileSystemDriver(storageDir, driver, compressionArguments);
        }

        return driver;
    }

    /**
     * Retourne la description du support
     */
    public String getDescription() {
        String type = "incremental";
        if (overwrite) {
            type = "image"; 
        }
        return "Uncompressed " + type + " medium. (" + fileSystemPolicy.getBaseArchivePath() + ")";        
    }    

    protected void storeFileInArchive(FileSystemRecoveryEntry entry, ProcessContext context) throws ApplicationException {
        File targetFile = new File(context.getCurrentArchiveFile(), entry.getName());
        File targetDirectory = FileSystemManager.getParentFile(targetFile);
        OutputStream out = null;
        InputStream in = null;
        try {
            try {
                if (! FileSystemManager.exists(targetDirectory)) {
                    FileTool.getInstance().createDir(targetDirectory);
                }

                out = FileSystemManager.getFileOutputStream(targetFile);
                in = FileSystemManager.getFileInputStream(entry.getFile());

                this.handler.store(entry, in, out, context);
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        } catch (Throwable e) {
            throw new ApplicationException("Error storing file " + FileSystemManager.getAbsolutePath(entry.getFile()) + " - target=" + FileSystemManager.getAbsolutePath(targetFile), e);
        }
    }

    public void completeLocalCopyCleaning(File copy, ProcessContext context) throws IOException, ApplicationException {
    }

    public void cleanLocalCopies(List copies, ProcessContext context) throws IOException, ApplicationException {
    }

    public File[] ensureLocalCopy(
            File[] archivesToProcess, 
            boolean overrideRecoveredFiles, 
            File destination, 
            String[] filters, 
            ProcessContext context
    ) throws IOException, ApplicationException {
        if (overrideRecoveredFiles) {
            try {
                context.getInfoChannel().print("Data recovery ...");
                for (int i=0; i<archivesToProcess.length; i++) {
                    context.getTaskMonitor().checkTaskCancellation();
                    context.getInfoChannel().updateCurrentTask(i+1, archivesToProcess.length, FileSystemManager.getPath(archivesToProcess[i]));
                    Logger.defaultLogger().info("Recovering " + FileSystemManager.getPath(archivesToProcess[i]) + " ...");                

                    // Copie de l'élément en cours.
                    if (filters == null) {
                        tool.copyDirectoryContent(archivesToProcess[i], destination, context.getTaskMonitor());
                    } else {
                        for (int j=0; j<filters.length; j++) {
                            File sourceFileOrDirectory = new File(archivesToProcess[i], filters[j]);
                            if (FileSystemManager.exists(sourceFileOrDirectory)) {
                                File targetDirectory = FileSystemManager.getParentFile(new File(destination, filters[j]));
                                tool.copy(sourceFileOrDirectory, targetDirectory, context.getTaskMonitor());
                            }
                        }
                    }

                    context.getTaskMonitor().getCurrentActiveSubTask().setCurrentCompletion(i+1, archivesToProcess.length);
                }

                return new File[] {destination};
            } catch (IOException e) {
                throw new ApplicationException(e);
            } catch (TaskCancelledException e) {
                throw new ApplicationException(e);
            }  
        } else {
            return archivesToProcess;
        }
    }

    protected void closeArchive(ProcessContext context) throws IOException {
        // Ne fait rien (répertoire)
    }

    protected void buildArchiveFromDirectory(File sourceDir, File destination, ProcessContext context) throws ApplicationException {
        try {
            tool.moveDirectoryContent(sourceDir, destination, true, context.getTaskMonitor());
        } catch (IOException e) {
            throw new ApplicationException(e);
        } catch (TaskCancelledException e) {
            throw new ApplicationException(e);            
        }
    } 

    public void commitBackup(ProcessContext context) throws ApplicationException {
        super.commitBackup(context);

        if (overwrite) {
            try {
                this.applyTrace(
                        new File(computeFinalArchivePath()),
                        ArchiveTraceCache.getInstance().getTrace(this, context.getFinalArchiveFile()),
                        false,
                        false,
                        context); // --> Call to "clean" in "cancel unsensitive" mode
            } catch (IOException e) {
                throw new ApplicationException(e);
            } catch (TaskCancelledException e) {
                throw new ApplicationException(e);
            }
        }
    }

    protected void convertArchiveToFinal(ProcessContext context) throws IOException, ApplicationException {
        // Case of empty archive (nothing to store)
        if (! FileSystemManager.exists(context.getCurrentArchiveFile())) {
            AbstractFileSystemMedium.tool.createDir(context.getCurrentArchiveFile());
        }

        try {
            if (overwrite) {
                AbstractFileSystemMedium.tool.moveDirectoryContent(context.getCurrentArchiveFile(), context.getFinalArchiveFile(), true, context.getTaskMonitor());
                AbstractFileSystemMedium.tool.delete(context.getCurrentArchiveFile(), true);

                AbstractFileSystemMedium.tool.moveDirectoryContent(getDataDirectory(context.getCurrentArchiveFile()), getDataDirectory(context.getFinalArchiveFile()), true, context.getTaskMonitor());
                AbstractFileSystemMedium.tool.delete(getDataDirectory(context.getCurrentArchiveFile()), true);
            } else {
                super.convertArchiveToFinal(context);
            }
        } catch (TaskCancelledException e) {
            throw new ApplicationException(e);
        }
    }

    protected void registerGenericEntry(FileSystemRecoveryEntry entry, ProcessContext context) throws IOException {
        context.getTraceAdapter().writeEntry(entry);
        if (this.overwrite) {
            context.getContentAdapter().writeEntry(entry); // All entries are stored
        }
    }

    protected void registerStoredEntry(FileSystemRecoveryEntry entry, ProcessContext context) throws IOException {
        if (! this.overwrite) {
            context.getContentAdapter().writeEntry(entry);
        }
    }

    protected String getArchiveExtension() {
        return "";
    }
}