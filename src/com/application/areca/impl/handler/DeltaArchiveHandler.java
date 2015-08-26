package com.application.areca.impl.handler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import com.application.areca.AbstractTarget;
import com.application.areca.ApplicationException;
import com.application.areca.ArchiveMedium;
import com.application.areca.ArecaConfiguration;
import com.application.areca.ArecaFileConstants;
import com.application.areca.cache.ArchiveManifestCache;
import com.application.areca.context.ProcessContext;
import com.application.areca.impl.FileSystemRecoveryEntry;
import com.application.areca.impl.FileSystemTarget;
import com.application.areca.impl.IOTask;
import com.application.areca.impl.copypolicy.AbstractCopyPolicy;
import com.application.areca.impl.tools.RecoveryFilterMap;
import com.application.areca.metadata.content.ArchiveContentAdapter;
import com.application.areca.metadata.content.ArchiveContentManager;
import com.application.areca.metadata.content.ContentEntry;
import com.application.areca.metadata.content.ContentFileIterator;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.manifest.ManifestKeys;
import com.application.areca.metadata.transaction.TransactionPoint;
import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.FileList;
import com.myJava.file.FileList.FileListIterator;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.delta.DeltaInputStream;
import com.myJava.file.delta.DeltaMerger;
import com.myJava.file.delta.DeltaProcessor;
import com.myJava.file.delta.DeltaProcessorException;
import com.myJava.file.delta.DeltaReader;
import com.myJava.file.delta.LayerHandler;
import com.myJava.file.delta.LayerWriterDeltaProcessor;
import com.myJava.file.delta.sequence.ByteProcessorException;
import com.myJava.file.delta.sequence.FileSequencerByteProcessor;
import com.myJava.file.delta.sequence.HashSequence;
import com.myJava.file.delta.sequence.SequenceAdapter;
import com.myJava.file.driver.DriverAlreadySetException;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.object.Duplicable;
import com.myJava.system.OSTool;
import com.myJava.util.Util;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;

/**
 * Handler that processes delta archives.
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
public class DeltaArchiveHandler
extends AbstractArchiveHandler {
	private static final boolean DEBUG = FrameworkConfiguration.getInstance().isDeltaDebugMode();
	private static final int MIN_BLOCK_SIZE_BYTE = ArecaConfiguration.get().getDeltaMinBucketSize();
	private static final int MAX_BLOCK_SIZE_BYTE = ArecaConfiguration.get().getDeltaMaxBucketSize();
	private static final int TARGET_BUCKET_NUMBER = ArecaConfiguration.get().getDeltaTargetBucketNumber();

	private static final String LOCAL_COPY_SUFFIX = "lcpy0";
	private static final String SEQUENCE_FOLDER = "seq";

	/**
	 * Temporary directory where all sequence files are stored
	 * <BR>This allows quicker sequence retrieval and limits bandwidth usage
	 */
	private static final File LOCAL_DATA_DIRECTORY = new File(OSTool.getTempDirectory(), "areca");

	/**
	 * Return the final sequence file name
	 */
	public String getSequenceFileName() {
		return ArecaFileConstants.SEQUENCE_FILE;
	}

	public void close(ProcessContext context) 
	throws IOException, ApplicationException {
		try {
			context.getSequenceAdapter().close();
			context.setSequenceAdapter(null);
		} finally {
			Iterator iter = context.getContentIterators().iterator();
			while (iter.hasNext()) {
				ContentFileIterator ctnIter = (ContentFileIterator)iter.next();
				ctnIter.close();
			}
			context.getContentIterators().clear();
		}
	}

	public void init(ProcessContext context, TransactionPoint transactionPoint) 
	throws IOException {
		FileSystemTarget target = (FileSystemTarget)medium.getTarget();
		File file = new File(medium.getDataDirectory(context.getCurrentArchiveFile()), getSequenceFileName());
		context.setSequenceAdapter(new ArchiveContentAdapter(file, target.getSourceDirectory()));  

		if (transactionPoint != null) {
			context.getSequenceAdapter().bulkInit(transactionPoint.getSequenceFile());
		}
	}

	/**
	 * Return the local folder where sequence data are temporarily stored.
	 */
	private File getLocalSequenceDirectory(File archive) {
		return new File(
				new File(
						new File(LOCAL_DATA_DIRECTORY, medium.getTarget().getUid()),
						FileSystemManager.getName(archive)
				), 
				SEQUENCE_FOLDER
		);
	}

	/**
	 * Locate the entry in the target's archives and return its HashSequence.
	 */
	private HashSequence lookupSequenceForEntry(FileSystemRecoveryEntry entry, ProcessContext context)
	throws ApplicationException, IOException, TaskCancelledException {
		// No sequence used in case of full backup
		if (context.getBackupScheme().equals(AbstractTarget.BACKUP_SCHEME_FULL)) {
			return null;
		}

		Iterator contents = context.getContentIterators().iterator();
		ContentFileIterator ctnIter = null;

		// Look among the already initialized iterators
		File lastArchive = null;
		while (contents.hasNext()) {
			ContentFileIterator currentCtnIter = (ContentFileIterator)contents.next();
			lastArchive = currentCtnIter.getReferenceArchive();
			if (DEBUG) {
				Logger.defaultLogger().fine("Entry " + entry.getKey() + " : checking sequence file contained in " + FileSystemManager.getDisplayPath(currentCtnIter.getReferenceArchive()));
			}
			boolean found = currentCtnIter.fetch(entry.getKey());
			if (found) {
				if (DEBUG) {
					Logger.defaultLogger().fine("Entry " + entry.getKey() + " : OK ");
				}
				ctnIter = currentCtnIter;
				break;
			} else {
				if (DEBUG) {
					Logger.defaultLogger().fine("Entry " + entry.getKey() + " : NOK ");
				}
			}
		}

		// Not found -> search among older archives
		boolean ignoreIncrementalAndDifferentialArchives = context.getBackupScheme().equals(AbstractTarget.BACKUP_SCHEME_DIFFERENTIAL);
		boolean lastPossibleCandidate = false;
		while (ctnIter == null) {
			GregorianCalendar toDate = null;
			if (lastArchive != null) {
				toDate = (GregorianCalendar)ArchiveManifestCache.getInstance().getManifest(medium, lastArchive).getDate().clone();
				toDate.add(GregorianCalendar.MILLISECOND, -1);
			}
			File[] previousArchives = listCandidatesForSequenceLookup(toDate, context.getBackupScheme());

			// Locate a new archive to load
			lastArchive = null;
			for (int i=previousArchives.length - 1; i>=0; i--) {

				// retrieve archive backup scheme
				String archiveBackupScheme = AbstractTarget.BACKUP_SCHEME_FULL;
				Manifest mf = ArchiveManifestCache.getInstance().getManifest(medium, previousArchives[i]);
				if (mf != null) {
					archiveBackupScheme = mf.getStringProperty(ManifestKeys.OPTION_BACKUP_SCHEME);
				}

				// Check archive scheme
				boolean validated = true;
				if (ignoreIncrementalAndDifferentialArchives) {
					validated = archiveBackupScheme.equals(AbstractTarget.BACKUP_SCHEME_FULL);
				}
				if (archiveBackupScheme.equals(AbstractTarget.BACKUP_SCHEME_DIFFERENTIAL)) {
					ignoreIncrementalAndDifferentialArchives = true;
				} else if (archiveBackupScheme.equals(AbstractTarget.BACKUP_SCHEME_FULL)) {
					lastPossibleCandidate = true;
				}

				if (validated) {
					lastArchive = previousArchives[i];
					break;
				}
			}

			if (lastArchive == null) {
				if (DEBUG) {
					Logger.defaultLogger().fine("Entry " + entry.getKey() + " : using <null> as sequence");
				}
				// No more archives to explore -> return null : the entry is a new one
				return null;
			}

			// Build an iterator
			if (DEBUG) {
				Logger.defaultLogger().fine("Opening hash file for : " + FileSystemManager.getDisplayPath(lastArchive));
			}
			File sequenceFile = ArchiveContentManager.resolveSequenceFileForArchive(medium, lastArchive);
			ctnIter = ArchiveContentAdapter.buildIterator(sequenceFile);
			ctnIter.setReferenceArchive(lastArchive);

			// Add the new iterator to the collection of already loaded iterators
			context.getContentIterators().add(ctnIter);

			// Check whether the entry can be found
			boolean found = ctnIter.fetch(entry.getKey());
			if (! found) {
				if (lastPossibleCandidate) {
					// The last possible candidate has been reached (it was a full backup) -> ignore previous archives and return null (new entry)
					return null;
				} else {
					ctnIter = null;
				}
			}
		}

		// Once a suitable iterator has bee found, extract the raw hash data
		if (DEBUG) {
			Logger.defaultLogger().fine("Entry " + entry.getKey() + " : using sequence file contained in " + FileSystemManager.getDisplayPath(ctnIter.getReferenceArchive()));
		}
		ContentEntry hashEntry = ctnIter.current();
		byte[] rawData = Util.base64Decode(hashEntry.getData());

		// Deserialize the hashSequence
		SequenceAdapter adapter = new SequenceAdapter();
		InputStream in = new GZIPInputStream(new ByteArrayInputStream(rawData));
		HashSequence seq = null;
		try {
			seq = adapter.deserialize(in);
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return seq;
	}

	private File[] listCandidatesForSequenceLookup(GregorianCalendar date, String backupScheme) throws ApplicationException {
		File[] files = medium.listArchives(null, date, true);
		return files;
	}

	private int computeBlockSize(File file) {
		long bs = FileSystemManager.length(file);
		return (int)Math.min(MAX_BLOCK_SIZE_BYTE, Math.max(MIN_BLOCK_SIZE_BYTE, bs / TARGET_BUCKET_NUMBER));
	}

	public void store(FileSystemRecoveryEntry entry, InputStream in, OutputStream out, ProcessContext context)
	throws ApplicationException, IOException, TaskCancelledException {
		HashSequence seq = lookupSequenceForEntry(entry, context);

		try {
			DeltaProcessor[] proc = new DeltaProcessor[] {new LayerWriterDeltaProcessor(out)};
			int blockSize = seq ==null ? computeBlockSize(entry.getFile()) : (int)seq.getBlockSize();
			if (DEBUG) {
				Logger.defaultLogger().fine("Entry : " + entry.getFile() + " - BlockSize = " + blockSize + (seq == null ? "C" : "R"));
			}
			FileSequencerByteProcessor sequencer = new FileSequencerByteProcessor(blockSize);
			DeltaReader reader;

			if (seq == null) {
				reader = new DeltaReader(
						computeBlockSize(entry.getFile()),
						in,
						proc,
						sequencer
				);
			} else {
				reader = new DeltaReader(
						seq,
						in,
						proc,
						sequencer
				);
			}

			reader.read(context.getTaskMonitor());

			// Add sequence data
			HashSequence returnedSequence = sequencer.getSequence();
			context.getSequenceAdapter().writeSequenceEntry(entry, returnedSequence);
		} catch (DeltaProcessorException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException("Error storing " + entry.getKey() + ".", e);
		} catch (ByteProcessorException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException("Error storing " + entry.getKey() + ".", e);            
		} finally {
			in.close();
		}
	}

	/**
	 * Delete the local sequence files associated to the deleted archive
	 */
	public void archiveDeleted(File archive) 
	throws IOException {
		File f = getLocalSequenceDirectory(archive);
		FileTool.getInstance().delete(f);
	}

	private File buildRecoveryFile(File destination) {
		File f = new File(destination, LOCAL_COPY_SUFFIX);
		for (int i=0; FileSystemManager.exists(f); i++) {
			f = new File(destination, LOCAL_COPY_SUFFIX + i);
		}
		return f;
	}

	/*
	private static void debug(File f) {
		try {
			String path = FileSystemManager.getAbsolutePath(f);
			DeltaLayer layer = new DeltaLayer(FileSystemManager.getFileInputStream(f), path);
			Logger.defaultLogger().info(path + " : " + layer.traverse());
			layer.close();
		} catch (Throwable e) {
			Logger.defaultLogger().error(e);
		}
	}
	 */

	public void initializeSimulationDriverData(FileSystemDriver initialDriver, ProcessContext context) throws IOException, DriverAlreadySetException {
		File workDirectory = buildRecoveryFile(context.getRecoveryDestination());
		FileSystemManager.getInstance().registerDriver(workDirectory, initialDriver);
	}

	public void recoverRawData(
			File[] archivesToRecover, 
			RecoveryFilterMap filtersByArchive,
			AbstractCopyPolicy policy,
			File referenceTrace,
			final short mode,
			final ProcessContext context
	) throws IOException, ApplicationException, TaskCancelledException {
		// 1 : Ensure that there is a local copy of the files to recover
		final File[] localFiles = medium.ensureLocalCopy(archivesToRecover, false, buildRecoveryFile(context.getRecoveryDestination()), filtersByArchive, policy, context);

		// 2 : Process the files to recover
		for (int i=0; i<localFiles.length; i++) {
			File localArchive = localFiles[i];

			String msg = "Processing archive " + i;
			if (localArchive != null) {
				msg += " (" + localArchive.getAbsolutePath() + ")";
			}
			FileList list = filtersByArchive.get(archivesToRecover[i]);
			if (list != null) {
				msg += " - " + list.size() + " entr" + (list.size() <= 1 ? "y" : "ies");
			} else {
				msg += " - No filter";
			}
			msg += " ...";
			Logger.defaultLogger().info(msg);
			if (localArchive != null && list != null) {
				FileListIterator iter = list.iterator();

				try {
					while (iter.hasNext()) {
						context.getTaskMonitor().checkTaskState();
						final String localPath = iter.next();
						final File f = new File(localArchive, localPath);
						if (DEBUG) {
							Logger.defaultLogger().fine("Processing entry : " + f.getAbsolutePath());
						}
						if (FileSystemManager.exists(f)) {
							if (FileSystemManager.isFile(f)) {
								final File target = new File(context.getRecoveryDestination(), localPath);

								if (policy == null || policy.accept(target)) {
									final int index = i;
									this.medium.doAndRetry(new IOTask() {
										public void run() throws IOException,TaskCancelledException,ApplicationException {
											recoverRawFile(f, target, localFiles, index, localPath, mode, context);
										}
									}, "Error while recovering " + target.getAbsolutePath());
								} else {
									if (DEBUG) {
										Logger.defaultLogger().fine(f.getAbsolutePath() + " : File refused by policy object.");
									}
								}
							} else {
								if (DEBUG) {
									Logger.defaultLogger().fine(f.getAbsolutePath() + " : Nothing to do (not a file)");
								}
							}
						} else {
							if (DEBUG) {
								Logger.defaultLogger().fine(f.getAbsolutePath() + " : Nothing to do (already processed)");
							}
						}
					}
				} finally {
					iter.close();
				}

				// Destroy the local archive
				medium.completeLocalCopyCleaning(localArchive, context);
			}
		}
	}
	
	

	protected void recoverRawFile(
			File f,
			File target,
			File[] localFiles,
			int i,
			String localPath,
			int mode,
			ProcessContext context
	) throws IOException, TaskCancelledException, ApplicationException {
		ArrayList localCopies = new ArrayList();
		localCopies.add(f);

		// Local input stream
		LayerHandler in = null;
		if (mode == ArchiveMedium.RECOVER_MODE_RECOVER) {
			in = new DeltaInputStream();
		} else {
			in = new DeltaMerger();
		}

		try {
			if (DEBUG) {
				Logger.defaultLogger().fine("   Looking for diff files for : " + f.getAbsolutePath());
			}
			in.addInputStream(FileSystemManager.getCachedFileInputStream(f), localPath);

			for (int j=i + 1; j<localFiles.length; j++) {
				File posteriorArchive = localFiles[j];
				if (posteriorArchive != null) {
					File f2 = new File(posteriorArchive, localPath);
					if (FileSystemManager.exists(f2)) {
						localCopies.add(f2);
						in.addInputStream(FileSystemManager.getCachedFileInputStream(f2), localPath);
						if (DEBUG) {
							Logger.defaultLogger().fine("   Adding diff file : " + f2.getAbsolutePath());
						}
					}
				}
			}

			// Target File
			FileTool.getInstance().createDir(FileSystemManager.getParentFile(target));
			OutputStream out = FileSystemManager.getFileOutputStream(target, false, context.getOutputStreamListener());

			if (mode == ArchiveMedium.RECOVER_MODE_RECOVER) {
				if (DEBUG) {
					Logger.defaultLogger().fine("Recovering ...");
				}
				// recover
				FileTool.getInstance().copy((DeltaInputStream)in, out, true, true, context.getTaskMonitor());
				if (DEBUG) {
					Logger.defaultLogger().fine("Recovery completed.");
				}
			} else {
				if (DEBUG) {
					Logger.defaultLogger().fine("Merging ...");
				}

				// merge
				try {
					((DeltaMerger)in).setProc(new LayerWriterDeltaProcessor(out));
					try {
						((DeltaMerger)in).merge(context.getTaskMonitor());
					} catch (DeltaProcessorException e) {
						Logger.defaultLogger().error(e);
						throw new ApplicationException("Error during merge.", e);
					}
				} finally {
					if (DEBUG) {
						Logger.defaultLogger().fine("Merge completed.");
					}
					if (out != null) {
						out.close();	
					}
				}
			}
		} finally {
			in.close();
		}

		// Delete local copies
		if (DEBUG) {
			Logger.defaultLogger().fine("   Cleaning local copies (" + localCopies.toString() + ") ...");
		}
		medium.cleanLocalCopies(localCopies, context);
		if (DEBUG) {
			Logger.defaultLogger().fine("   Local copies cleaned.");
		}
	}

	public EntriesDispatcher buildEntriesDispatcher(File[] archives) {
		return new DeltaEntriesDispatcher(archives, medium);
	}

	public Duplicable duplicate() {
		return new DeltaArchiveHandler();
	}

	public boolean supportsImageBackup() {
		return false;
	}

	public File getContentFile(File archive) {
		return new File(medium.getDataDirectory(archive), this.getSequenceFileName());
	}

	public boolean autonomousArchives() {
		return false;
	}
}
