package com.application.areca.impl.handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ApplicationException;
import com.application.areca.cache.ArchiveManifestCache;
import com.application.areca.context.ProcessContext;
import com.application.areca.impl.FileSystemRecoveryEntry;
import com.application.areca.metadata.content.ArchiveContent;
import com.application.areca.metadata.content.ArchiveContentManager;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.manifest.ManifestKeys;
import com.myJava.file.FileSystemIterator;
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
import com.myJava.object.PublicClonable;
import com.myJava.util.log.Logger;

/**
 * Handler that processes delta archives.
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
public class DeltaArchiveHandler
extends AbstractArchiveHandler {

    private static final String LOCAL_COPY_SUFFIX = "lc";
    
    private static final String SEQUENCE_FOLDER = "seq";
    private static final int MIN_BLOCK_SIZE_BYTE = 2 * 1024; // 2 kbytes
    private static final int MAX_BLOCK_SIZE_BYTE = 10 * 1024*1024; // 10 Mbytes
    private static final int TARGET_BUCKET_NUMBER = 100;
    
    /**
     * Returns the first archives which contains the requested entry
     */
    private File lookupInArchive(FileSystemRecoveryEntry entry, ProcessContext context)
    throws ApplicationException, IOException {
        if (! context.getPreviousTrace().containsFile(entry)) {
            return null;
        } else {
            // Look within the already loaded archived
            Iterator archives = context.getPreviousArchives().iterator();
            Iterator contents = context.getPreviousContents().iterator();
            
            File archive = null;
            while (archives.hasNext()) {
                ArchiveContent ctn = (ArchiveContent)contents.next();
                archive = (File)archives.next();
                
                if (ctn.contains(entry)) {
                    return archive;
                }
            }
            
            // Load older archives
            File[] previousArchives = medium.listArchives(
                    null, 
                    archive == null ? null : ArchiveManifestCache.getInstance().getManifest(medium, archive).getDate()
            );
            for (int i=0; i<previousArchives.length; i++) {
                boolean validated = true;
                if (context.getBackupScheme().equals(AbstractRecoveryTarget.BACKUP_SCHEME_DIFFERENTIAL)) {
                    Manifest mf = ArchiveManifestCache.getInstance().getManifest(medium, archive);
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
            Logger.defaultLogger().warn("Warning : " + entry.getName() + " has not been found in archives - It SHOULD have been found !");
            return null;
        }
    }
    
    private File getSequenceFileForEntry(File archive, FileSystemRecoveryEntry entry, ProcessContext context) {
        File seqRoot = (File)context.getSequenceRoots().get(archive);
        if (seqRoot == null) {
            File metaDataRoot = medium.getDataDirectory(archive);
            seqRoot = new File(metaDataRoot, SEQUENCE_FOLDER);
            context.getSequenceRoots().put(archive, seqRoot);
        }

        return new File(seqRoot, entry.getName());
    }
    
    private HashSequence lookupSequenceForEntry(FileSystemRecoveryEntry entry, ProcessContext context)
    throws ApplicationException, IOException {
        File archive = lookupInArchive(entry, context);
        if (archive != null) {
            File sequenceFile = getSequenceFileForEntry(archive, entry, context);
            SequenceAdapter adapter = new SequenceAdapter();
            InputStream in = FileSystemManager.getFileInputStream(sequenceFile);
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
    throws ApplicationException, IOException {
        HashSequence seq = lookupSequenceForEntry(entry, context);
        
        try {
            DeltaProcessor[] proc = new DeltaProcessor[] {new LayerWriterDeltaProcessor(out, false)};
            int blockSize = seq ==null ? computeBlockSize(entry.getFile()) : (int)seq.getBlockSize();
            FileSequencerByteProcessor sequencer = new FileSequencerByteProcessor(blockSize);
            File seqFile = getSequenceFileForEntry(context.getCurrentArchiveFile(), entry, context);
            File seqDir = FileSystemManager.getParentFile(seqFile);
            FileTool.getInstance().createDir(seqDir);
            sequencer.activateSerialization(seqFile);
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
            
            reader.read();
        } catch (DeltaProcessorException e) {
            Logger.defaultLogger().error(e);
            throw new ApplicationException("Error storing " + entry.getName() + ".", e);
        } catch (ByteProcessorException e) {
            Logger.defaultLogger().error(e);
            throw new ApplicationException("Error storing " + entry.getName() + ".", e);            
        }
    }
    
    private File buildRecoveryFile(File destination) {
        File f = new File(destination, LOCAL_COPY_SUFFIX);
        for (int i=0; FileSystemManager.exists(f); i++) {
            f = new File(destination, LOCAL_COPY_SUFFIX + i);
        }
        return f;
    }
    
    public void recoverRawData(
            File[] archivesToRecover, 
            String[] filters,
            short mode,
            ProcessContext context
    ) throws IOException, ApplicationException {
        // 1 : Ensure that there is a local copy of the files to recover
        File[] localFiles = medium.ensureLocalCopy(archivesToRecover, false, buildRecoveryFile(context.getRecoveryDestination()), filters, context);
        
        // 2 : Process the files to recover
        for (int i=0; i<localFiles.length; i++) {
            File localArchive = localFiles[i];
            
            FileSystemIterator iter = new FileSystemIterator(localArchive, false);
            while (iter.hasNext()) {
                File f = (File)iter.next();
                
                if (FileSystemManager.isFile(f)) {
                    String localPath = FileSystemManager.getAbsolutePath(f).substring(FileSystemManager.getAbsolutePath(localArchive).length());
                    ArrayList localCopies = new ArrayList();
                    localCopies.add(f);
                    
                    // Local input stream
                    LayerHandler in = null;
                    if (mode == MODE_RECOVER) {
                        in = new DeltaInputStream();
                    } else {
                        in = new DeltaMerger();
                    }
                    in.addInputStream(FileSystemManager.getCachedFileInputStream(f));
                    
                    for (int j=i + 1; j<localFiles.length; j++) {
                        File posteriorArchive = localFiles[j];
                        File f2 = new File(posteriorArchive, localPath);
                        if (FileSystemManager.exists(f2)) {
                            localCopies.add(f2);
                            in.addInputStream(FileSystemManager.getCachedFileInputStream(f2));
                        }
                    }
                    
                    // Target File
                    File target = new File(context.getRecoveryDestination(), localPath);
                    File parent = FileSystemManager.getParentFile(target);
                    FileTool.getInstance().createDir(parent);
                    OutputStream out = FileSystemManager.getFileOutputStream(target);
                    
                    // Recover or merge
                    if (mode == MODE_RECOVER) {
                        FileTool.getInstance().copy((DeltaInputStream)in, out, true, true);
                    } else {
                        ((DeltaMerger)in).setProc(new LayerWriterDeltaProcessor(out, false));
                        try {
                            ((DeltaMerger)in).merge();
                        } catch (DeltaProcessorException e) {
                            Logger.defaultLogger().error(e);
                            throw new ApplicationException("Error during merge.", e);
                        }
                    }
                    
                    // Delete local copies
                    medium.cleanLocalCopies(localCopies, context);
                }
            }
            
            // Destroy the local archive
            medium.completeLocalCopyCleaning(localArchive, context);
        }
    }

    public PublicClonable duplicate() {
        return new DeltaArchiveHandler();
    }
    
    public boolean providesAutonomousArchives() {
        return false;
    }
}
