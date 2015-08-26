package com.application.areca.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.application.areca.impl.copypolicy.AbstractCopyPolicy;
import com.application.areca.impl.tools.RecoveryFilterMap;
import com.application.areca.metadata.content.ArchiveContentAdapter;
import com.application.areca.metadata.trace.ArchiveTraceManager;
import com.application.areca.metadata.transaction.TransactionPoint;
import com.myJava.file.FileList.FileListIterator;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.InvalidPathException;
import com.myJava.file.driver.CompressedFileSystemDriver;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.file.iterator.FileNameComparator;
import com.myJava.file.iterator.FileSystemIterator;
import com.myJava.object.Duplicable;
import com.myJava.util.Chronometer;
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
 Copyright 2005-2015, Olivier PETRUCCI.

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

	protected void prepareContext(ProcessContext context, TransactionPoint transactionPoint) throws IOException {
		super.prepareContext(context, transactionPoint);

		// If a transaction point is used, the previousHashIterator has been set during the context's deserialization.
		// -> No need to instantiate a new one.
		if (image && context.getReferenceTrace() != null && transactionPoint == null) {
			// see "registerUnstoredFile" method
			context.setPreviousHashIterator(
					ArchiveContentAdapter.buildIterator(duplicateMetadataFile(context.getHashAdapter().getFile(), context))
			);
		}
	}

	protected void registerUnstoredFile(FileSystemRecoveryEntry entry, ProcessContext context) throws IOException {
		if (image) {
			context.getContentAdapter().writeContentEntry(entry);

			boolean found = context.getPreviousHashIterator().fetch(entry.getKey());
			if (found) {
				context.getHashAdapter().writeGenericEntry(entry.getKey(), context.getPreviousHashIterator().current().getData());
			} else {
				// Shall not happen
				throw new IllegalArgumentException(entry.getKey() + " not found in hash file. Current entry = " + context.getPreviousHashIterator().current().getKey());
			}
		}
	}

	protected void dbgBuildArchiveFileList(File archive, BufferedWriter writer) throws IOException, ApplicationException {
		FileSystemIterator iter = new FileSystemIterator(archive, false, true, true, true);
		String prefix = FileSystemManager.getAbsolutePath(archive);
		while (iter.hasNext()) {
			File f = iter.nextFile();
			Logger.defaultLogger().fine("File : " + f);
			writer.write("\n"+FileSystemManager.getAbsolutePath(f).substring(prefix.length()));
		}
	}

	public int getMaxRetries() {
		return this.fileSystemPolicy.getMaxRetries();
	}

	public boolean retrySupported() {
		return this.fileSystemPolicy.retrySupported();
	}
	
	/*
	private static void cloneDirs(File target, File source) throws IOException {
		if (target == null || FileSystemManager.exists(target)) {
			return;
		} else {
			File parentTarget = FileSystemManager.getParentFile(target);
			File parentSource = FileSystemManager.getParentFile(source);
			cloneDirs(parentTarget, parentSource);

			FileSystemManager.mkdir(target);
			long lm = source.lastModified();
			target.setLastModified(lm);
			long lm2 = target.lastModified();
			
			if (lm != lm2) {
				System.out.println(target);
			} else {
				System.out.println("ok " + target);
			}
		}
	}
	*/

	protected void storeFileInArchive(FileSystemRecoveryEntry entry, InputStream in, ProcessContext context) 
	throws IOException, ApplicationException, TaskCancelledException {
		//Chronometer.instance().start("storeImpl");
		//Chronometer.instance().start("preStore");
		// Store the file
		File targetFile = new File(context.getCurrentArchiveFile(), entry.getKey());
		File targetDirectory = FileSystemManager.getParentFile(targetFile);
		OutputStream out = null;
		try {
			FileTool.getInstance().createDir(targetDirectory);

			out = FileSystemManager.getFileOutputStream(targetFile, false, context.getOutputStreamListener());
			
			//Chronometer.instance().stop("preStore");
//			Chronometer.instance().start("handlerStore");
			this.handler.store(entry, in, out, context);
			//Chronometer.instance().stop("handlerStore");
			//Chronometer.instance().start("postStore");
		} catch (InvalidPathException e) {
			throw new ApplicationException("Error storing file " + FileSystemManager.getDisplayPath(entry.getFile()) + " : " + e.getMessage(), e);
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw e;
		} catch (Throwable e) {
			if (e instanceof TaskCancelledException) {
				throw (TaskCancelledException)e;
			} else {
				throw new ApplicationException("Error storing file " + FileSystemManager.getDisplayPath(entry.getFile()) + " - target=" + FileSystemManager.getDisplayPath(targetFile), e);
			}
		} finally {
			if (out != null) {
				out.close();
			}
		}
		
		//Chronometer.instance().stop("postStore");
		//Chronometer.instance().stop("storeImpl");
	}

	public void completeLocalCopyCleaning(File copy, ProcessContext context) throws IOException, ApplicationException {
	}

	public void cleanLocalCopies(List copies, ProcessContext context) throws IOException, ApplicationException {
	}

	public File[] ensureLocalCopy(
			File[] archivesToProcess, 
			boolean mergeRecoveredFiles, 
			File destination, 
			RecoveryFilterMap filesByArchive, 
			AbstractCopyPolicy policy,
			ProcessContext context
	) throws IOException, ApplicationException, TaskCancelledException {
		if (mergeRecoveredFiles) {
			try {
				context.getInfoChannel().print("Data recovery ...");

				for (int i=0; i<archivesToProcess.length; i++) {
					if (filesByArchive == null) {
						throw new ApplicationException("The file map passed as argument shall not be null.");
					}

					com.application.areca.metadata.FileList files = (com.application.areca.metadata.FileList)filesByArchive.get(archivesToProcess[i]);
					logRecoveryStep(filesByArchive, files, archivesToProcess[i], context);

					// Copy current element
					if (files != null) {
						FileListIterator entries = files.iterator();
						try {
							while (entries.hasNext()) {
								String entry = (String)entries.next();
								File sourceFile = new File(archivesToProcess[i], entry);
								if (FileSystemManager.exists(sourceFile)) {
									File targetDirectory = FileSystemManager.getParentFile(new File(destination, entry));
									doAndRetry(new EnsureLocalCopyTask(sourceFile, targetDirectory, policy, context), "An error was detected during recovery of " + archivesToProcess[i].getAbsolutePath());
								}
							}
						} finally {
							entries.close();
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

	private class EnsureLocalCopyTask implements IOTask {
		private File destination;
		private File sourceFileOrDirectory;
		private ProcessContext context;
		private AbstractCopyPolicy policy;

		public EnsureLocalCopyTask(File sourceFileOrDirectory, File destination, AbstractCopyPolicy policy, ProcessContext context) {
			this.destination = destination;
			this.sourceFileOrDirectory = sourceFileOrDirectory;
			this.policy = policy;
			this.context = context;
		}

		public void run() throws IOException, TaskCancelledException, ApplicationException {
			tool.copy(sourceFileOrDirectory, destination, policy, new FileNameComparator(), context.getTaskMonitor(), context.getOutputStreamListener());
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
				FileTool.getInstance().copyDirectoryContent(context.getRecoveryDestination(), context.getCurrentArchiveFile(), null, null, context.getTaskMonitor(), null);
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

		if (image) {
			try {
				this.target.secureUpdateCurrentTask("Cleaning repository ...", context);
				MediumUtils.cleanObsoleteFiles(
						new File(computeFinalArchivePath()),
						ArchiveTraceManager.resolveTraceFileForArchive(this, context.getCurrentArchiveFile()),
						true);
				this.target.secureUpdateCurrentTask("Repository cleaned.", context);
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