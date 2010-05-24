package com.myJava.file.delta;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import com.myJava.file.delta.sequence.ByteProcessor;
import com.myJava.file.delta.sequence.ByteProcessorException;
import com.myJava.file.delta.sequence.HashSequence;
import com.myJava.file.delta.sequence.HashSequenceEntry;
import com.myJava.file.delta.tools.HashTool;
import com.myJava.file.delta.tools.LinkedList;
import com.myJava.util.taskmonitor.TaskCancelledException;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
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
public class DeltaReader implements Constants {   
    private int blockSize;
    private HashSequence seq;
    private InputStream in;
    private String hashAlgorithm = HASH_ALG;
    private DeltaProcessor[] processors;
    private ByteProcessor bproc;

    public DeltaReader(int blockSize, InputStream in, DeltaProcessor[] processors, ByteProcessor bproc) {
        this.blockSize = blockSize;
        this.in = in;
        this.processors = processors;
        this.bproc = bproc;
    }
    
    public DeltaReader(HashSequence seq, InputStream in, DeltaProcessor[] processors, ByteProcessor bproc) {
        if (seq == null) {
            throw new IllegalArgumentException("The hash sequence can't be null.");
        }
        this.seq = seq;
        this.blockSize = seq.getBlockSize();
        this.in = in;
        this.processors = processors;
        this.bproc = bproc;
    }
    
    private long computeSig(long totalRead) {
    	return totalRead%blockSize;
    }
    
    public void read(TaskMonitor monitor) throws IOException, DeltaException, DeltaProcessorException, ByteProcessorException, TaskCancelledException {
        LinkedList currentBlock = new LinkedList(blockSize);
        long totalRead = 0;
        long position = 0;
        int currentQuickHash = 0;
        long breakSize = -1;
        long lastBlockIndex = -1;
        long significant = blockSize;
        
        for (int x=0; x<processors.length; x++) {
            processors[x].begin();
        }
        
        if (bproc != null) {
            bproc.open();
        }
        
        while (true) {
        	monitor.checkTaskState();
        	
            int read = in.read();
            byte bRead;
            if (read == -1) {
                if (totalRead == 0 || totalRead == breakSize) {
                	bRead = (byte)read;
                    break;
                } else {
                    if (breakSize == -1) {
                        breakSize = totalRead + blockSize -1 ;
                        significant = computeSig(totalRead);
                    }
                    bRead = HashSequenceEntry.DEFAULT_BYTE;
                }
            } else {
            	bRead = (byte)read;
            	if (bproc != null) {
            		bproc.processByte(bRead);
            	}
            }
            
            // Read data
            totalRead++;
            if (totalRead <= blockSize) {
                currentQuickHash = HashTool.hash(currentQuickHash, bRead);
            } else {
                currentQuickHash = HashTool.update(currentQuickHash, bRead, (byte)currentBlock.getFirst());
            }
            currentBlock.add(bRead);

            // Look for
            boolean found = false;
            if (totalRead >= blockSize) {
                if (seq != null && seq.contains(currentQuickHash)) {
                    byte[] fh = currentBlock.computeHash(hashAlgorithm);
                    List entries = seq.get(currentQuickHash, fh);
                    if (entries != null) {
                        Iterator iter = entries.iterator();
                        HashSequenceEntry candidate = null;
                        while (iter.hasNext()) {
                            HashSequenceEntry entry = (HashSequenceEntry)iter.next();
                            if (entry.getIndex() > lastBlockIndex && (candidate == null || entry.getIndex() < candidate.getIndex())) {
                                candidate = entry;
                            }
                        }
                        if (candidate != null && candidate.getSize() == significant) {
                            // Found !
                            if (candidate.getIndex() <= lastBlockIndex) {
                                throw new DeltaException("Incompatible indexes : current = " + candidate.getIndex() + ", last = " + lastBlockIndex);
                            } else if (candidate.getIndex() > lastBlockIndex + 1) {
                                // Block lost !
                                for (int x=0; x<processors.length; x++) {
                                    processors[x].bytesLost((lastBlockIndex + 1) * blockSize, candidate.getIndex() * blockSize - 1);
                                }
                            }
                            lastBlockIndex = candidate.getIndex();
                            for (int x=0; x<processors.length; x++) {
                                processors[x].blockFound(candidate, currentBlock);
                            }
                            
                            // go ahead (and reset all)
                            currentQuickHash = 0;
                            totalRead = 0;
                            found = true;
                        }
                    } else {
                    	//Logger.defaultLogger().info("No entries found.");
                    }
                }

                if (! found) {
                    for (int x=0; x<processors.length; x++) {
                        processors[x].newByte((byte)currentBlock.getFirst());   
                    }
                }
            }

            position++;
        }
        
        if (seq != null && lastBlockIndex < seq.getSize() - 1) {
            // Block lost !
            for (int x=0; x<processors.length; x++) {
                processors[x].bytesLost((lastBlockIndex + 1) * blockSize, seq.getSize() * blockSize - 1);
            }
        }
        
        for (int x=0; x<processors.length; x++) {
            processors[x].end();
        }
        
        if (bproc != null) {
            bproc.close();
        }
    }
}
