package com.application.areca.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.application.areca.AbstractTarget;
import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.application.areca.impl.tools.RecoveryFilterMap;
import com.application.areca.metadata.manifest.Manifest;
import com.myJava.file.CompressionArguments;
import com.myJava.file.FileFilterList;
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
import com.myJava.object.Duplicable;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;

/**
 * Incremental storage support which uses an archive to store the data.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2010, Olivier PETRUCCI.

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

	protected String getSubDescription() {
		return "Compressed";
	}  

	/**
	 * Buid the archive
	 */
	protected void buildArchive(ProcessContext context) throws IOException, ApplicationException {
		super.buildArchive(context);
		File archive = context.getCurrentArchiveFile();
		context.setArchiveWriter(new ArchiveWriter(buildArchiveAdapter(archive, true, context)));
	}

	public int getMaxRetries() {
		return 0;
	}

	public boolean retrySupported() {
		return false;
	}

	protected void storeFileInArchive(FileSystemRecoveryEntry entry, InputStream in, ProcessContext context) 
	throws IOException, ApplicationException, TaskCancelledException {
		try {
			File file = entry.getFile();
			String path = entry.getKey();

			if (context.getTaskMonitor() != null) {
				context.getTaskMonitor().checkTaskState();
			}

			if (FileNameUtil.startsWithSeparator(path)) {
				path = path.substring(1);
			}

			long length = FileSystemManager.length(file);
			context.getArchiveWriter().getAdapter().addEntry(path, length);            

			OutputStream out = context.getArchiveWriter().getAdapter().getArchiveOutputStream();
			this.handler.store(entry, in, out, context);
			context.getArchiveWriter().getAdapter().closeEntry();
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw e;
		}
	}  

	public void open(Manifest manifest, ProcessContext context, String backupScheme) throws ApplicationException {
		if (imageBackups) {
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
		&& ! (backupScheme.equals(AbstractTarget.BACKUP_SCHEME_INCREMENTAL) && this.imageBackups);
	}

	protected void prepareContext(ProcessContext context) {
	}

	/**
	 * Builds an ArchiveAdapter for the file passed as argument. 
	 */
	protected ArchiveAdapter buildArchiveAdapter(File f, boolean write, ProcessContext context) throws IOException, ApplicationException {      
		ArchiveAdapter adapter = null;
		if (write) {
			if (compressionArguments.isMultiVolumes()) {
				adapter = new ZipArchiveAdapter(
						buildVolumeStrategy(f, write, context), 
						compressionArguments.getVolumeSize() * 1024 * 1024, 
						compressionArguments.isUseZip64(),
						compressionArguments.getLevel()
				);   

			} else {
				AbstractFileSystemMedium.tool.createDir(FileSystemManager.getParentFile(f));
				adapter =  new ZipArchiveAdapter(
						FileSystemManager.getFileOutputStream(f, false, context.getOutputStreamListener()), 
						compressionArguments.isUseZip64(),
						compressionArguments.getLevel()
				);   
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
		} else {
			adapter.setCharset(Charset.forName(ZipConstants.DEFAULT_CHARSET));
		}

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

	public Duplicable duplicate() {
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
			final File[] archivesToProcess, 
			final boolean overrideRecoveredFiles, 
			final File destination, 
			RecoveryFilterMap filtersByArchive, 
			final ProcessContext context
	) throws IOException, ApplicationException, TaskCancelledException {
		try {
			context.getInfoChannel().print("Data recovery ...");   
			final List ret = new ArrayList();
			if (overrideRecoveredFiles) {
				ret.add(destination);
			}

			for (int i=0; i<archivesToProcess.length; i++) {
				final FileFilterList filters;
				final int index = i;
				if (filtersByArchive != null) {
					filters = (FileFilterList)filtersByArchive.get(archivesToProcess[i]);
				} else {
					filters = null;
				}

				logRecoveryStep(filtersByArchive, filters, archivesToProcess[i], context);

				if (filtersByArchive == null || (filters != null && filters.size() != 0)) {
					ensureLocalCopy(archivesToProcess[index], overrideRecoveredFiles, destination, filters, ret, context);
				} else {
					ret.add(null);
				}
				context.getTaskMonitor().getCurrentActiveSubTask().setCurrentCompletion(i+1, archivesToProcess.length);  
			}

			return (File[])ret.toArray(new File[ret.size()]);       
		} catch (TaskCancelledException e) {
			throw e;
		} catch (IOException e) {
			throw e;        	
		} catch (Exception e) {
			throw new ApplicationException(e);
		}    
	}

	private void ensureLocalCopy(File archiveToProcess, boolean overrideRecoveredFiles, File destination, FileFilterList filters, List ret, ProcessContext context) 
	throws IOException, ApplicationException, TaskCancelledException {
		ArchiveReader zrElement = new ArchiveReader(buildArchiveAdapter(archiveToProcess, false, context));

		File realDestination;
		if (overrideRecoveredFiles) {
			realDestination = destination;
		} else {
			realDestination = new File(destination, FileSystemManager.getName(archiveToProcess));
			if (! ret.contains(realDestination)) {
				ret.add(realDestination);
			}
		}
		zrElement.injectIntoDirectory(realDestination, filters, context.getTaskMonitor(), context.getOutputStreamListener());
		zrElement.close();
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

	protected void registerUnstoredFile(FileSystemRecoveryEntry entry, ProcessContext context) {
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
}
