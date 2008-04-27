package com.application.areca.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.application.areca.ApplicationException;
import com.application.areca.cache.ArchiveTraceCache;
import com.application.areca.context.ProcessContext;
import com.application.areca.metadata.content.ArchiveContent;
import com.application.areca.metadata.content.ArchiveContentManager;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.InvalidPathException;
import com.myJava.file.driver.CompressedFileSystemDriver;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.object.PublicClonable;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;

/**
 * Support incremental non compresse (repertoire)
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 5323430991191230653
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
        return "Uncompressed " + type + " medium. (" + fileSystemPolicy.getArchivePath() + ")";        
    }    
    
    protected void storeFileInArchive(FileSystemRecoveryEntry entry, ProcessContext context) throws ApplicationException, TaskCancelledException {
        File targetFile = new File(context.getCurrentArchiveFile(), entry.getName());
        File targetDirectory = FileSystemManager.getParentFile(targetFile);
        OutputStream out = null;
        try {
            if (! FileSystemManager.exists(targetDirectory)) {
                FileTool.getInstance().createDir(targetDirectory);
            }
            
            out = FileSystemManager.getFileOutputStream(targetFile, false, context.getOutputStreamListener());
            InputStream in = FileSystemManager.getFileInputStream(entry.getFile());
            
            this.handler.store(entry, in, out, context);
        } catch (InvalidPathException e) {
            throw new ApplicationException("Error storing file " + FileSystemManager.getAbsolutePath(entry.getFile()) + " : " + e.getMessage(), e);
        } catch (Throwable e) {
        	if (e instanceof TaskCancelledException) {
        		throw (TaskCancelledException)e;
        	} else {
        		throw new ApplicationException("Error storing file " + FileSystemManager.getAbsolutePath(entry.getFile()) + " - target=" + FileSystemManager.getAbsolutePath(targetFile), e);
        	}
        } finally {
        	if (out != null) {
        		try {
					out.close();
				} catch (IOException e) {
					Logger.defaultLogger().error(e);
				}
        	}
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
            Map filtersByArchive, 
            ProcessContext context
    ) throws IOException, ApplicationException {
        if (overrideRecoveredFiles) {
            try {
            	ArchiveContent[] contents = new ArchiveContent[archivesToProcess.length];
                for (int i=0; i<archivesToProcess.length; i++) {
                	contents[i] = ArchiveContentManager.getContentForArchive(this, archivesToProcess[i]);
                }
            	
                context.getInfoChannel().print("Data recovery ...");
                for (int i=0; i<archivesToProcess.length; i++) {
                	List filters = null;
                	if (filtersByArchive != null) {
                    	filters = (List)filtersByArchive.get(archivesToProcess[i]);
                	}
                	
                    context.getTaskMonitor().checkTaskCancellation();
                    context.getInfoChannel().updateCurrentTask(i+1, archivesToProcess.length, FileSystemManager.getPath(archivesToProcess[i]));
                    Logger.defaultLogger().info("Recovering " + FileSystemManager.getPath(archivesToProcess[i]) + " ...");                
                    
                    // Copie de l'element en cours.
                    if (filtersByArchive == null) {
                    	copyFile(archivesToProcess[i], destination, FileSystemManager.getAbsolutePath(archivesToProcess[i]), i, contents, context);
                    } else if (filters != null) {
                        for (int j=0; j<filters.size(); j++) {
                            File sourceFileOrDirectory = new File(archivesToProcess[i], (String)filters.get(j));
                            if (FileSystemManager.exists(sourceFileOrDirectory)) {
                                File targetDirectory = FileSystemManager.getParentFile(new File(destination, (String)filters.get(j)));
                                tool.copy(sourceFileOrDirectory, targetDirectory, context.getTaskMonitor(), context.getOutputStreamListener());
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
    
    private void copyFile(File source, File destination, String root, int index, ArchiveContent[] contents, ProcessContext context) 
    throws IOException, TaskCancelledException {
		String localPath = FileSystemManager.getAbsolutePath(source).substring(root.length());
    	if (FileSystemManager.isFile(source)) {
    		boolean ok = true;
    		for (int i=index+1; i<contents.length; i++) {
    			if (contents[i].contains(new FileSystemRecoveryEntry(root, new File(root, localPath)))) {
    				ok = false;
    				System.out.println("ignoring " + source.getAbsolutePath());
    				break;
    			}
    		}
    		if (ok) {
    			File tg = new File(destination, localPath);
    			tool.copyFile(source, FileSystemManager.getFileOutputStream(tg, false, context.getOutputStreamListener()), true, context.getTaskMonitor());
    		}
    	} else {
			tool.createDir(new File(destination, localPath));
    		File[] files = FileSystemManager.listFiles(source);
    		for (int i=0; i<files.length; i++) {
    			copyFile(files[i], destination, root, index, contents, context);
    		}
    	}
    }
    
    protected void closeArchive(ProcessContext context) throws IOException {
        // Does nothing (directory)
    }

    protected void computeMergeDirectories(ProcessContext context) {
    	context.setCurrentArchiveFile(context.getRecoveryDestination());
	}

	protected void buildMergedArchiveFromDirectory(ProcessContext context) throws ApplicationException {
        // does nothing
    	/*
    	try {
            tool.moveDirectoryContent(sourceDir, destination, true, context.getTaskMonitor());
        } catch (IOException e) {
            throw new ApplicationException(e);
        } catch (TaskCancelledException e) {
            throw new ApplicationException(e);            
        }
        */
    } 

	public void commitBackup(ProcessContext context) throws ApplicationException {
        super.commitBackup(context);
        
        if (overwrite) {
            try {
                this.applyTrace(
                        new File(computeFinalArchivePath()),
                        ArchiveTraceCache.getInstance().getTrace(this, context.getCurrentArchiveFile()),
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
    	super.convertArchiveToFinal(context);
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