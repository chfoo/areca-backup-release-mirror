package com.myJava.file.delta.sequence;

import com.myJava.file.delta.Constants;
import com.myJava.file.delta.tools.HashTool;
import com.myJava.file.delta.tools.CircularList;

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
public class FileSequencerByteProcessor
implements ByteProcessor, Constants {

    private String hashAlgorithm = HASH_ALG;
    private int blockSize;

    private long position = 0;
    private int currentQuickHash = 0;
    private HashSequence ret;
    private CircularList block;
    private boolean closed = false;
    
    private long totalReadMod = 0;

    public FileSequencerByteProcessor(int blockSize) {
        this.blockSize = blockSize;
    }

    public void close() throws ByteProcessorException {
    	if (closed) {
    		return;
    	}
    	closed = true;
    	
        if (totalReadMod != 0) {
            int d;
            for (d = 0; d<blockSize; d++) {
                currentQuickHash = HashTool.hash(currentQuickHash, HashSequenceEntry.DEFAULT_BYTE);
                block.add(HashSequenceEntry.DEFAULT_BYTE);

                totalReadMod++;
                if (totalReadMod == blockSize) {
                	totalReadMod = 0;
                    ret.add(currentQuickHash, block.computeHash(hashAlgorithm), position++, blockSize - d - 1);
                    break;
                }
            }
        }
    }

    public void open() {
        ret = new HashSequence(blockSize);
        block = new CircularList(blockSize);
    }

    public void processByte(byte read) {
        totalReadMod++;

        currentQuickHash = HashTool.hash(currentQuickHash, read);
        block.add(read);
        if (totalReadMod == blockSize) {
            ret.add(currentQuickHash, block.computeHash(hashAlgorithm), position++, blockSize);
            currentQuickHash = 0;
            totalReadMod = 0;
        }
    }

    public HashSequence getSequence() {
        return ret;
    }
}
