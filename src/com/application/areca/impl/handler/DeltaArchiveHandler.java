package com.application.areca.impl.handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ApplicationException;
import com.application.areca.ArecaTechnicalConfiguration;
import com.application.areca.cache.ArchiveManifestCache;
import com.application.areca.cache.ArchiveTraceCache;
import com.application.areca.context.ProcessContext;
import com.application.areca.impl.FileSystemRecoveryEntry;
import com.application.areca.metadata.content.ArchiveContent;
import com.application.areca.metadata.content.ArchiveContentManager;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.manifest.ManifestKeys;
import com.application.areca.metadata.trace.ArchiveTrace;
import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.FileSystemIterator;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.delta.DeltaInputStream;
import com.myJava.file.delta.DeltaLayer;
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
import com.myJava.object.PublicClonable;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;

/**
 * Handler that processes delta archives.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4765044255727194190
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
public class DeltaArchiveHandler
extends AbstractArchiveHandler {
	private static final boolean DEBUG = FrameworkConfiguration.getInstance().isDeltaDebugMode();
	private static final String LOCAL_COPY_SUFFIX = "lc";

	private static final String SEQUENCE_FOLDER = "seq";
	private static final int MIN_BLOCK_SIZE_BYTE = ArecaTechnicalConfiguration.get().getDeltaMinBucketSize();
	private static final int MAX_BLOCK_SIZE_BYTE = ArecaTechnicalConfiguration.get().getDeltaMaxBucketSize();
	private static final int TARGET_BUCKET_NUMBER = ArecaTechnicalConfiguration.get().getDeltaTargetBucketNumber();

	/**
	 * Returns the first archives which contains the requested entry
	 */
	private File lookupInArchive(FileSystemRecoveryEntry entry, ProcessContext context)
	throws ApplicationException, IOException {
		if (
				(! context.getPreviousTrace().containsFile(entry))
				|| context.getBackupScheme().equals(AbstractRecoveryTarget.BACKUP_SCHEME_FULL)
		) {
			return null;
		} else {
			// Look within the already loaded archived
			Iterator archives = context.getPreviousArchives().iterator();
			Iterator contents = context.getPreviousContents().iterator();

			File archive = null;
			while (archives.hasNext()) {
				ArchiveContent ctn = (ArchiveContent)contents.next();
				archive = (File)archives.next();

				if (DEBUG) {
					Logger.defaultLogger().fine("Looking in " + FileSystemManager.getAbsolutePath(archive));
				}
				if (ctn.contains(entry)) {
					return archive;
				}
			}

			// Load older archives
			GregorianCalendar toDate = archive == null ? null : ArchiveManifestCache.getInstance().getManifest(medium, archive).getDate();
			File[] previousArchives = medium.listArchives(null, toDate);
			for (int i=previousArchives.length - 1; i>=0; i--) {
				boolean validated = true;
				if (context.getBackupScheme().equals(AbstractRecoveryTarget.BACKUP_SCHEME_DIFFERENTIAL)) {
					Manifest mf = ArchiveManifestCache.getInstance().getManifest(medium, previousArchives[i]);
					validated = 
						mf != null 
						&& mf.getStringProperty(ManifestKeys.OPTION_BACKUP_SCHEME).equals(AbstractRecoveryTarget.BACKUP_SCHEME_FULL);
				}

				if (validated) {
					ArchiveContent ctn = ArchiveContentManager.getContentForArchive(medium, previousArchives[i]);
					context.getPreviousArchives().add(previousArchives[i]);
					context.getPreviousContents().add(ctn);

					if (ctn.contains(entry)) {
						return previousArchives[i];
					}
				}
			}

			// Not found -> SUSPECT
			Logger.defaultLogger().warn("Warning : " + entry.getName() + " has not been found in archives - It SHOULD have been found !!");
			return null;
		}
	}
	
	private File getSequenceFileForEntry(File archive, String entry, ProcessContext context) {
		File seqRoot = (File)context.getSequenceRoots().get(archive);
		if (seqRoot == null) {
			File metaDataRoot = medium.getDataDirectory(archive);
			seqRoot = new File(metaDataRoot, SEQUENCE_FOLDER);
			context.getSequenceRoots().put(archive, seqRoot);
		}

		return new File(seqRoot, entry);
	}

	private File getSequenceFileForEntry(File archive, FileSystemRecoveryEntry entry, ProcessContext context) {
		return getSequenceFileForEntry(archive, entry.getName(), context);
	}

	private HashSequence lookupSequenceForEntry(FileSystemRecoveryEntry entry, ProcessContext context)
	throws ApplicationException, IOException {
		File archive = lookupInArchive(entry, context);
		if (archive != null) {
			if (DEBUG) {
				Logger.defaultLogger().fine("Entry : " + entry.getFile() + " - Using sequence located in " + FileSystemManager.getAbsolutePath(archive) + ".");
			}
			File sequenceFile = getSequenceFileForEntry(archive, entry, context);
			SequenceAdapter adapter = new SequenceAdapter();
			InputStream in = new GZIPInputStream(FileSystemManager.getFileInputStream(sequenceFile));
			HashSequence seq = null;
			try {
				seq = adapter.deserialize(in);
			} finally {
				if (in != null) {
					in.close();
				}
			}
			return seq;
		} else {
			return null;
		}
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
			sequencer.activateSerialization(getSequenceFileForEntry(context.getCurrentArchiveFile(), entry, context));
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
		} catch (DeltaProcessorException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException("Error storing " + entry.getName() + ".", e);
		} catch (ByteProcessorException e) {
			Logger.defaultLogger().error(e);
			throw new ApplicationException("Error storing " + entry.getName() + ".", e);            
		} finally {
			in.close();
		}
	}

	private File buildRecoveryFile(File destination) {
		File f = new File(destination, LOCAL_COPY_SUFFIX);
		for (int i=0; FileSystemManager.exists(f); i++) {
			f = new File(destination, LOCAL_COPY_SUFFIX + i);
		}
		return f;
	}
	
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

	public void recoverRawData(
			File[] archivesToRecover, 
			Map filtersByArchive, 
			short mode,
			ProcessContext context
	) throws IOException, ApplicationException {
		// 1 : Ensure that there is a local copy of the files to recover
		File[] localFiles = medium.ensureLocalCopy(archivesToRecover, false, buildRecoveryFile(context.getRecoveryDestination()), filtersByArchive, context);

		// 2 : Process the files to recover
		for (int i=0; i<localFiles.length; i++) {
			File localArchive = localFiles[i];

        	String[] filters = null;
        	if (filtersByArchive != null) {
            	List lstFilters = (List)filtersByArchive.get(archivesToRecover[i]);
            	if (lstFilters != null) {
            		filters = (String[])lstFilters.toArray(new String[lstFilters.size()]);
            	} else {
            		filters = new String[0];
            	}
        	}
			
			FileSystemIterator iter = new FileSystemIterator(localArchive, filters, false);
			while (iter.hasNext()) {
				File f = (File)iter.next();

				if (FileSystemManager.isFile(f)) {
					String localPath = FileSystemManager.getAbsolutePath(f).substring(FileSystemManager.getAbsolutePath(localArchive).length());
					File target = new File(context.getRecoveryDestination(), localPath);

					if (! FileSystemManager.exists(target)) { // do not recover if the target file already exists (which means that it has already been recovered)
						ArrayList localCopies = new ArrayList();
						localCopies.add(f);

						// Local input stream
						LayerHandler in = null;
						if (mode == MODE_RECOVER) {
							in = new DeltaInputStream();
						} else {
							in = new DeltaMerger();
						}

						try {
							//debug(f);
							in.addInputStream(FileSystemManager.getCachedFileInputStream(f), localPath);
							File latestSequenceFile = null;
							
							if (mode == MODE_MERGE) {
								latestSequenceFile = getSequenceFileForEntry(archivesToRecover[i], localPath, context);
							}
							
							for (int j=i + 1; j<localFiles.length; j++) {
								File posteriorArchive = localFiles[j];
								File f2 = new File(posteriorArchive, localPath);
								if (FileSystemManager.exists(f2)) {
									localCopies.add(f2);
									//debug(f2);
									in.addInputStream(FileSystemManager.getCachedFileInputStream(f2), localPath);
									
									if (mode == MODE_MERGE) {
										latestSequenceFile = getSequenceFileForEntry(archivesToRecover[j], localPath, context);
									}
								}
							}

							// Target File
							FileTool.getInstance().createDir(FileSystemManager.getParentFile(target));
							OutputStream out = FileSystemManager.getFileOutputStream(target, false, context.getOutputStreamListener());

							if (mode == MODE_RECOVER) {
								// recover
								FileTool.getInstance().copy((DeltaInputStream)in, out, true, true);
							} else {
								// merge
								try {
									((DeltaMerger)in).setProc(new LayerWriterDeltaProcessor(out));
									try {
										((DeltaMerger)in).merge();
									} catch (DeltaProcessorException e) {
										Logger.defaultLogger().error(e);
										throw new ApplicationException("Error during merge.", e);
									}
								} finally {
									if (out != null) {
										out.close();	
									}
								}
								
								// Duplicate the sequence file
								InputStream seqIn = FileSystemManager.getFileInputStream(latestSequenceFile);
								File seqFile = getSequenceFileForEntry(context.getCurrentArchiveFile(), localPath, context);
								FileTool.getInstance().createDir(FileSystemManager.getParentFile(seqFile));
								OutputStream seqOut = FileSystemManager.getFileOutputStream(seqFile);
								try {
									FileTool.getInstance().copy(seqIn, seqOut, true, true);
								} finally {
									if (seqOut != null) {
										seqOut.close();	
									}
								}
							}
						} finally {
							in.close();
						}

						// Delete local copies
						medium.cleanLocalCopies(localCopies, context);
					}
				}
			}

			// Destroy the local archive
			medium.completeLocalCopyCleaning(localArchive, context);
		}
	}

	public Map dispatchEntries(File[] archives, Set entriesToRecover) throws ApplicationException, IOException {
		Map entriesByArchive = new HashMap();

		// Build a list of entries to recover indexed by archive
		Set detected = new HashSet();
		for (int i=archives.length - 1; i>=0 && entriesToRecover.size() > 0; i--) {
			Set toRemove = new HashSet();
			ArchiveTrace trace = ArchiveTraceCache.getInstance().getTrace(this.medium, archives[i]);
			Iterator detectedIter = detected.iterator();
			while (detectedIter.hasNext()) {
				FileSystemRecoveryEntry entry = (FileSystemRecoveryEntry)detectedIter.next();
				if (! trace.containsFile(entry)) {
					toRemove.add(entry);
				}
			}
			entriesToRecover.removeAll(toRemove);
			detected.removeAll(toRemove);

			ArchiveContent content = ArchiveContentManager.getContentForArchive(this.medium, archives[i]);
			Iterator iter = entriesToRecover.iterator();
			while (iter.hasNext()) {
				FileSystemRecoveryEntry entry = (FileSystemRecoveryEntry)iter.next();
				if (content.contains(entry)) {
					List entries = (List)entriesByArchive.get(archives[i]);
					if (entries == null) {
						entries = new ArrayList();
						entriesByArchive.put(archives[i], entries);
					}
					entries.add(entry.getName());
					detected.add(entry);
				}
			}
		}

		return entriesByArchive;
	}

	public PublicClonable duplicate() {
		return new DeltaArchiveHandler();
	}
}
