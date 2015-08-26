package com.myJava.file.delta;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import com.myJava.file.delta.sequence.ByteProcessor;
import com.myJava.file.delta.sequence.ByteProcessorException;
import com.myJava.file.delta.sequence.FileSequencerByteProcessor;
import com.myJava.file.delta.sequence.HashSequence;
import com.myJava.file.delta.sequence.HashSequenceEntry;
import com.myJava.file.delta.tools.HashTool;
import com.myJava.file.delta.tools.CircularList;
import com.myJava.util.taskmonitor.TaskCancelledException;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
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
public class DeltaReader implements Constants {   
	public static long SUCCESS_COUNTER = 0;
	public static long FAILURE_COUNTER = 0;

	private int blockSize;
	private HashSequence seq;
	private InputStream in;
	private String hashAlgorithm = HASH_ALG;
	private DeltaProcessor[] processors;
	private FileSequencerByteProcessor bproc;

	public DeltaReader(int blockSize, InputStream in, DeltaProcessor[] processors, FileSequencerByteProcessor bproc) {
		this.blockSize = blockSize;
		this.in = new BufferedInputStream(in, 10000);
		this.processors = processors;
		this.bproc = bproc;
	}

	public DeltaReader(HashSequence seq, InputStream in, DeltaProcessor[] processors, FileSequencerByteProcessor bproc) {
		if (seq == null) {
			throw new IllegalArgumentException("The hash sequence can't be null.");
		}
		this.seq = seq;
		this.blockSize = seq.getBlockSize();
		this.in = new BufferedInputStream(in, 10000);
		this.processors = processors;
		this.bproc = bproc;
	}

	private long computeSig(long totalRead) {
		return totalRead%blockSize;
	}
	
	public void read(TaskMonitor monitor) throws IOException, DeltaException, DeltaProcessorException, ByteProcessorException, TaskCancelledException {
		if (seq == null) {
			readNoSeq(monitor);
		} else {
			readSeq(monitor);
		}
	}

	public void readNoSeq(TaskMonitor monitor) throws IOException, DeltaException, DeltaProcessorException, ByteProcessorException, TaskCancelledException {
		CircularList currentBlock = new CircularList(blockSize);
		long totalRead = 0;
		long breakSize = -1;
		boolean doRead = true;

		for (int x=0; x<processors.length; x++) {
			processors[x].begin();
		}

		bproc.open();

		while (true) {
			monitor.checkTaskState();

			int read = doRead ? in.read() : -1;
			byte bRead;
			if (read == -1) {
				if (totalRead == 0 || totalRead == breakSize) {
					break;
				} else {
					if (breakSize == -1) {
						breakSize = totalRead + blockSize -1 ;
					}
					bRead = HashSequenceEntry.DEFAULT_BYTE;
					doRead = false;
				}
			} else {
				bRead = (byte)read;
				bproc.processByte(bRead);
			}

			totalRead++;
			currentBlock.add(bRead);

			if (totalRead >= blockSize) {
				for (int x=0; x<processors.length; x++) {
					processors[x].newByte(currentBlock.getFirst());   
				}
			}
		}

		for (int x=0; x<processors.length; x++) {
			processors[x].end();
		}
		bproc.close();
	}

	public void readSeq(TaskMonitor monitor) throws IOException, DeltaException, DeltaProcessorException, ByteProcessorException, TaskCancelledException {
		CircularList currentBlock = new CircularList(blockSize);
		long totalRead = 0;
		int currentQuickHash = 0;
		long breakSize = -1;
		long lastBlockIndex = -1;
		long significant = blockSize;
		boolean doRead = true;

		for (int x=0; x<processors.length; x++) {
			processors[x].begin();
		}

		bproc.open();

		while (true) {
			monitor.checkTaskState();

			int read = doRead ? in.read() : -1;
			byte bRead;
			if (read == -1) {
				if (totalRead == 0 || totalRead == breakSize) {
					break;
				} else {
					if (breakSize == -1) {
						breakSize = totalRead + blockSize -1 ;
						significant = computeSig(totalRead);
					}
					bRead = HashSequenceEntry.DEFAULT_BYTE;
					doRead = false;
				}
			} else {
				bRead = (byte)read;
				bproc.processByte(bRead);
			}

			totalRead++;
			
			// Register value and compute hash
			currentQuickHash = currentBlock.addValueAndUpdateQuickHash(currentQuickHash, bRead);

			// Look for
			boolean found = false;
			if (totalRead >= blockSize) {
				int idx = seq.getIndexIfExist(currentQuickHash);
				if (idx != -1) {
					byte[] fh = currentBlock.computeHash(hashAlgorithm);
					List entries = seq.get(currentQuickHash, idx, fh);
					if (entries != null) {

						HashSequenceEntry candidate = null;
						for (int e=0; e<entries.size(); e++) {
							HashSequenceEntry entry = (HashSequenceEntry)entries.get(e);
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
							currentBlock.reset();
							found = true;
							SUCCESS_COUNTER++;
						} else {
							//Logger.defaultLogger().fine("Full hash computed but no entry found.");
							FAILURE_COUNTER++;
						}
					} else {
						//Logger.defaultLogger().info("No entries found.");
						FAILURE_COUNTER++;
					}
				}

				if (! found) {
					for (int x=0; x<processors.length; x++) {
						processors[x].newByte(currentBlock.getFirst());   
					}
				}
			}
		}

		if (lastBlockIndex < seq.getSize() - 1) {
			// Block lost !
			for (int x=0; x<processors.length; x++) {
				processors[x].bytesLost((lastBlockIndex + 1) * blockSize, seq.getSize() * blockSize - 1);
			}
		}

		for (int x=0; x<processors.length; x++) {
			processors[x].end();
		}
		bproc.close();
	}
}
