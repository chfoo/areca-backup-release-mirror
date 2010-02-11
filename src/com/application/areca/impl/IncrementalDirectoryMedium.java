package com.application.areca.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.application.areca.impl.tools.RecoveryFilterMap;
import com.application.areca.metadata.content.ArchiveContentAdapter;
import com.application.areca.metadata.trace.ArchiveTraceManager;
import com.myJava.file.FileFilterList;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.InvalidPathException;
import com.myJava.file.driver.CompressedFileSystemDriver;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.object.Duplicable;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;

/**
 * Incremental medium that stores data in individual files (compressed or not)
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
public class IncrementalDirectoryMedium extends AbstractIncrementalFileSystemMedium {

	public Duplicable duplicate() {
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

	protected String getSubDescription() {
		return "Uncompressed";
	}

	protected void prepareContext(ProcessContext context) throws IOException {
		if (imageBackups && context.getReferenceTrace() != null) {
			// see "registerUnstoredFile" method
			ArchiveContentAdapter adapter = new ArchiveContentAdapter(duplicateMetadataFile(context.getHashAdapter().getFile(), context));
			context.setPreviousHashIterator(adapter.buildIterator(true));
		}
	}
	
    protected void registerUnstoredFile(FileSystemRecoveryEntry entry, ProcessContext context) throws IOException {
		if (imageBackups) {
			context.getContentAdapter().writeContentEntry(entry);
		
			boolean found = context.getPreviousHashIterator().fetchUntil(entry.getKey());
			if (found) {
				context.getHashAdapter().writeGenericEntry(entry.getKey(), context.getPreviousHashIterator().current().getData());
			} else {
				// Shall not happen
				throw new IllegalArgumentException(entry.getKey() + " not found in hash file. Current entry = " + context.getPreviousHashIterator().current().getKey());
			}
		}
	}

	public int getMaxRetries() {
		return this.fileSystemPolicy.getMaxRetries();
	}

	public boolean retrySupported() {
		return this.fileSystemPolicy.retrySupported();
	}

	protected void storeFileInArchive(FileSystemRecoveryEntry entry, InputStream in, ProcessContext context) 
	throws IOException, ApplicationException, TaskCancelledException {
		// Store the file
		File targetFile = new File(context.getCurrentArchiveFile(), entry.getKey());
		File targetDirectory = FileSystemManager.getParentFile(targetFile);
		OutputStream out = null;
		try {
			if (! FileSystemManager.exists(targetDirectory)) {
				FileTool.getInstance().createDir(targetDirectory);
			}

			out = FileSystemManager.getFileOutputStream(targetFile, false, context.getOutputStreamListener());
			this.handler.store(entry, in, out, context);
		} catch (InvalidPathException e) {
			throw new ApplicationException("Error storing file " + FileSystemManager.getAbsolutePath(entry.getFile()) + " : " + e.getMessage(), e);
		} catch (IOException e) {
            Logger.defaultLogger().error(e);
            throw e;
        } catch (Throwable e) {
			if (e instanceof TaskCancelledException) {
				throw (TaskCancelledException)e;
			} else {
				throw new ApplicationException("Error storing file " + FileSystemManager.getAbsolutePath(entry.getFile()) + " - target=" + FileSystemManager.getAbsolutePath(targetFile), e);
			}
		} finally {
			if (out != null) {
				out.close();
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
			RecoveryFilterMap filtersByArchive, 
			ProcessContext context
	) throws IOException, ApplicationException, TaskCancelledException {
		if (overrideRecoveredFiles) {
			try {
				context.getInfoChannel().print("Data recovery ...");
				for (int i=0; i<archivesToProcess.length; i++) {
					FileFilterList filters = null;
					if (filtersByArchive != null) {
						filters = (FileFilterList)filtersByArchive.get(archivesToProcess[i]);
					}
					logRecoveryStep(filtersByArchive, filters, archivesToProcess[i], context);

					// Copy current element
					if (filtersByArchive == null) {
						doAndRetry(new EnsureLocalCopy(archivesToProcess, i, destination, context), "An error was detected during recovery of " + archivesToProcess[i].getAbsolutePath());
						
					} else if (filters != null) {
						for (int j=0; j<filters.size(); j++) {
							File sourceFileOrDirectory = new File(archivesToProcess[i], filters.get(j));
							if (FileSystemManager.exists(sourceFileOrDirectory)) {
								File targetDirectory = FileSystemManager.getParentFile(new File(destination, filters.get(j)));
								doAndRetry(new EnsureLocalCopyByFilter(targetDirectory, sourceFileOrDirectory, context), "An error was detected during recovery of " + archivesToProcess[i].getAbsolutePath());
							}
						}
					}

					context.getTaskMonitor().getCurrentActiveSubTask().setCurrentCompletion(i+1, archivesToProcess.length);
				}

				return new File[] {destination};
			} catch (IOException e) {
				Logger.defaultLogger().error(e);
				throw e;
			} catch (TaskCancelledException e) {
				throw e;
			} catch (Throwable e) {
				Logger.defaultLogger().error(e);
				throw new ApplicationException(e);
			}
		} else {
			Logger.defaultLogger().info("No archive pre-processing needed.");
			return archivesToProcess;
		}
	}
	
	private class EnsureLocalCopyByFilter implements IOTask {
		private File targetDirectory;
		private File sourceFileOrDirectory;
		private ProcessContext context;

		public EnsureLocalCopyByFilter(File targetDirectory, File sourceFileOrDirectory, ProcessContext context) {
			this.targetDirectory = targetDirectory;
			this.sourceFileOrDirectory = sourceFileOrDirectory;
			this.context = context;
		}

		public void run() throws IOException, TaskCancelledException, ApplicationException {
			tool.copy(sourceFileOrDirectory, targetDirectory, context.getTaskMonitor(), context.getOutputStreamListener());
		}
	}
	
	private class EnsureLocalCopy implements IOTask {
		private File[] archivesToProcess;
		private int i;
		private File destination;
		private ProcessContext context;

		public EnsureLocalCopy(File[] archivesToProcess, int i, File destination, ProcessContext context) {
			this.archivesToProcess = archivesToProcess;
			this.i = i;
			this.destination = destination;
			this.context = context;
		}

		public void run() throws IOException, TaskCancelledException, ApplicationException {
			copyFile(archivesToProcess[i], destination, FileSystemManager.getAbsolutePath(archivesToProcess[i]), i, context);
		}
	}

	/**
	 * Copy a stored file to a local location denoted by the "destination" argument.
	 */
	private void copyFile(File source, File destination, String root, int index, ProcessContext context) 
	throws IOException, TaskCancelledException {
		String localPath = FileSystemManager.getAbsolutePath(source).substring(root.length());
		if (FileSystemManager.isFile(source)) {
			File tg = new File(destination, localPath);
			tool.copyFile(source, FileSystemManager.getFileOutputStream(tg, false, context.getOutputStreamListener()), true, context.getTaskMonitor());
		} else {
			tool.createDir(new File(destination, localPath));
			File[] files = FileSystemManager.listFiles(source);
			for (int i=0; i<files.length; i++) {
				copyFile(files[i], destination, root, index, context);
			}
		}
	}

	protected void closeArchive(ProcessContext context) throws IOException, ApplicationException {
		if (context.getPreviousHashIterator() != null) {
			context.getPreviousHashIterator().close();
		}
	}

	protected void buildMergedArchiveFromDirectory(ProcessContext context) throws ApplicationException {
		// Do nothing if all files were recovered into the final (merged) directory
		// Copy files into this directory otherwise
		if (context.getCurrentArchiveFile() != null && context.getRecoveryDestination() != null && ! context.getCurrentArchiveFile().equals(context.getRecoveryDestination())) {
			try {			
				AbstractFileSystemMedium.tool.createDir(context.getCurrentArchiveFile());
				FileTool.getInstance().copyDirectoryContent(context.getRecoveryDestination(), context.getCurrentArchiveFile(), context.getTaskMonitor(), null);
			} catch (IOException e) {
				throw new ApplicationException(e);
			} catch (TaskCancelledException e) {
				throw new ApplicationException(e);
			}
		}
	} 
	
	protected void computeMergedArchiveFile(ProcessContext context) throws ApplicationException {
		File recoveryDirectory = context.getRecoveryDestination();
		File backupMainDirectory = fileSystemPolicy.getArchiveDirectory();
		
		if (
				FileSystemManager.getParentFile(recoveryDirectory).equals(backupMainDirectory)
				&& matchArchiveName(recoveryDirectory)
		) {
			// First case : the temporary recovery directory looks like a standard archive directory
			// -> we will use it as final merged archive (which will prevent us from unnecessary file copy operations)
			context.setCurrentArchiveFile(context.getRecoveryDestination());
		} else {
			// Else : compute a new archive name
			super.computeMergedArchiveFile(context);
		}
	}

	public void commitBackup(ProcessContext context) throws ApplicationException {
		super.commitBackup(context);

		if (imageBackups) {
			try {
				this.cleanUnwantedFiles(
						new File(computeFinalArchivePath()),
						ArchiveTraceManager.resolveTraceFileForArchive(this, context.getCurrentArchiveFile()),
						false,
						context); // --> Call to "cleanUnwantedFiles" in "cancel unsensitive" mode
			} catch (TaskCancelledException e) {
				throw new ApplicationException(e);
			} catch (Exception e) {
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

	protected String getArchiveExtension() {
		return "";
	}
}