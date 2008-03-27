package com.application.areca.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ApplicationException;
import com.application.areca.cache.ArchiveManifestCache;
import com.application.areca.context.ProcessContext;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.manifest.ManifestKeys;
import com.application.areca.version.VersionInfos;
import com.myJava.file.CompressionArguments;
import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.archive.ArchiveAdapter;
import com.myJava.file.archive.ArchiveReader;
import com.myJava.file.archive.ArchiveWriter;
import com.myJava.file.archive.zip64.ZipArchiveAdapter;
import com.myJava.file.archive.zip64.ZipConstants;
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
 * <BR>Areca Build ID : 7289397627058093710
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
        return "Compressed " + type + " medium. (" + fileSystemPolicy.getArchivePath() + ")";        
    }    
    
    /**
     * Buid the archive
     */
    protected void buildArchive(ProcessContext context) throws IOException, ApplicationException {
    	super.buildArchive(context);
        File archive = context.getCurrentArchiveFile();
        context.setArchiveWriter(new ArchiveWriter(buildArchiveAdapter(archive, true, context)));
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
            
            OutputStream out = context.getArchiveWriter().getAdapter().getArchiveOutputStream();
            this.handler.store(entry, in, out, context);
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
    protected ArchiveAdapter buildArchiveAdapter(File f, boolean write, ProcessContext context) throws IOException, ApplicationException {      
        ArchiveAdapter adapter = null;
        if (write) {
            if (compressionArguments.isMultiVolumes()) {
                adapter = new ZipArchiveAdapter(buildVolumeStrategy(f, write, context), compressionArguments.getVolumeSize() * 1024 * 1024, compressionArguments.isUseZip64());   
            } else {
                AbstractFileSystemMedium.tool.createDir(FileSystemManager.getParentFile(f));
                adapter =  new ZipArchiveAdapter(FileSystemManager.getFileOutputStream(f, false, context.getOutputStreamListener()), compressionArguments.isUseZip64());   
            }
            if (compressionArguments.getComment()!= null) {
                adapter.setArchiveComment(compressionArguments.getComment());
            }
        } else {
            if (compressionArguments.isMultiVolumes()) {
                adapter = new ZipArchiveAdapter(buildVolumeStrategy(f, write, context), 1);   
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
        
        // BACKWARD-COMPATIBILITY : versions bellow 6.0 didn't handle charsets correctly -> set UTF-8 as default
        if (! write) {
        	Manifest mf = ArchiveManifestCache.getInstance().getManifest(this, f);
        	boolean isOld = false;
        	if (mf == null) {
        		Logger.defaultLogger().warn("No manifest found for " + FileSystemManager.getAbsolutePath(f) + " ... assuming that this archive was created by an older version than 6.0");
        		isOld = true;
        	} else {
        		String version = mf.getStringProperty(ManifestKeys.VERSION);
        		if (version == null) {
        			isOld = true;
        		} else {
        			isOld = VersionInfos.isBeforeOrEquals(version, "5.5.7");
        		}
        	}
        	
        	if (isOld) {
        		adapter.setCharset(Charset.forName(ZipConstants.DEFAULT_CHARSET));
        	}
        }
        // EOF BACKWARD-COMPATIBILITY
        
        return adapter;
    }
    
    private VolumeStrategy buildVolumeStrategy(File f, boolean write, ProcessContext context) throws IOException {       
        if (write) {
            AbstractFileSystemMedium.tool.createDir(f);
        }
        ZipVolumeStrategy strat = new ZipVolumeStrategy(new File(f, MV_ARCHIVE_NAME), compressionArguments.getNbDigits());
        strat.setListener(context.getOutputStreamListener());
        return strat;
    }
    
    public PublicClonable duplicate() {
        IncrementalZipMedium other = new IncrementalZipMedium();
        copyAttributes(other);
        return other;
    }

    protected String getArchiveExtension() {
        return 
        	compressionArguments.isMultiVolumes() || (! compressionArguments.isAddExtension()) ? 
        			"" : CompressionArguments.ZIP_SUFFIX;
    }

    public File[] ensureLocalCopy(
            File[] archivesToProcess, 
            boolean overrideRecoveredFiles, 
            File destination, 
            Map filtersByArchive, 
            ProcessContext context
    ) throws IOException, ApplicationException {
        try {
            context.getInfoChannel().print("Data recovery ...");   
            List ret = new ArrayList();
            if (overrideRecoveredFiles) {
            	ret.add(destination);
            }
            
            for (int i=0; i<archivesToProcess.length; i++) {
            	String[] filters = null;
            	if (filtersByArchive != null) {
                	List lstFilters = (List)filtersByArchive.get(archivesToProcess[i]);
                	if (lstFilters != null) {
                		filters = (String[])lstFilters.toArray(new String[lstFilters.size()]);
                	}
            	}

                context.getTaskMonitor().checkTaskCancellation();  
                String scope = filtersByArchive == null ? "All entries" : (filters == null ? "No entry" : filters.length + (filters.length <=1 ? " entry" : " entries"));
                context.getInfoChannel().updateCurrentTask(i+1, archivesToProcess.length, "Processing " + FileSystemManager.getPath(archivesToProcess[i]) + " (" + scope + ") ...");
                
            	if (filtersByArchive == null || (filters != null && filters.length != 0)) {
	                ArchiveReader zrElement = new ArchiveReader(buildArchiveAdapter(archivesToProcess[i], false, context));
	
	                File realDestination;
	                if (overrideRecoveredFiles) {
	                    realDestination = destination;
	                } else {
	                    realDestination = new File(destination, FileSystemManager.getName(archivesToProcess[i]));
	                    ret.add(realDestination);
	                }
	                zrElement.injectIntoDirectory(realDestination, filters, context.getTaskMonitor(), context.getOutputStreamListener());
	                zrElement.close();
            	}
                context.getTaskMonitor().getCurrentActiveSubTask().setCurrentCompletion(i+1, archivesToProcess.length);  
            }
            
            return (File[])ret.toArray(new File[ret.size()]);       
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
            FileTool.getInstance().delete(loc, true);
        }
    }

    protected void computeMergeDirectories(ProcessContext context) throws ApplicationException {
		File[] recoveredFiles = context.getReport().getRecoveryResult().getRecoveredArchivesAsArray();
    	GregorianCalendar lastArchiveDate = ArchiveManifestCache.getInstance().getManifest(this, recoveredFiles[recoveredFiles.length - 1]).getDate();
		context.setCurrentArchiveFile(new File(computeArchivePath(lastArchiveDate)));
	}

	protected void buildMergedArchiveFromDirectory(ProcessContext context) 
    throws ApplicationException {		
        try {
        	context.getOutputStreamListener().reset();
            AbstractFileSystemMedium.tool.createDir(FileSystemManager.getParentFile(context.getCurrentArchiveFile()));
            context.setArchiveWriter(new ArchiveWriter(buildArchiveAdapter(context.getCurrentArchiveFile(), true, context)));
            context.getArchiveWriter().addFile(context.getRecoveryDestination(), "", context.getTaskMonitor());
        } catch (IOException e) {
            throw new ApplicationException(e);
        } catch (TaskCancelledException e) {
            throw new ApplicationException(e);
        }
    }

    /**
     * Registers a generic entry - whether it has been filtered or not.
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
