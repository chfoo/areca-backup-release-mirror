package com.myJava.file.delta.sequence;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.delta.Constants;
import com.myJava.file.delta.tools.HashTool;
import com.myJava.file.delta.tools.LinkedList;
import com.myJava.util.log.Logger;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 11620171963739279
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
public class FileSequencerByteProcessor
implements ByteProcessor, Constants {

    private String hashAlgorithm = HASH_ALG;
    private int blockSize;
    private File fileToWrite; 

    private long totalRead = 0;
    private long position = 0;
    private int currentQuickHash = 0;
    private HashSequence ret;
    private LinkedList block;

    public FileSequencerByteProcessor(int blockSize) {
        this.blockSize = blockSize;
    }

    public void activateSerialization(File target) {
        fileToWrite = target;
    }

    public void close() throws ByteProcessorException {
        if (totalRead % blockSize != 0) {
            int d;
            for (d = 0; d<blockSize; d++) {
                totalRead++;
                currentQuickHash = HashTool.hash(currentQuickHash, HashSequenceEntry.DEFAULT_BYTE);
                block.add(HashSequenceEntry.DEFAULT_BYTE);

                if (totalRead % blockSize == 0) {
                    ret.add(currentQuickHash, block.computeHash(hashAlgorithm), position++, blockSize - d - 1);
                    break;
                }
            }
        }

        if (fileToWrite != null) {
            OutputStream bos = null;
            try {
            	File parent = FileSystemManager.getParentFile(fileToWrite);
            	FileTool.getInstance().createDir(parent);
            	
                bos = new GZIPOutputStream(FileSystemManager.getFileOutputStream(fileToWrite));
                SequenceAdapter adapter = new SequenceAdapter();
                adapter.serialize(bos, ret);
            } catch (IOException e) {
                Logger.defaultLogger().error(e);
                throw new ByteProcessorException(e.getMessage());
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        Logger.defaultLogger().error(e);
                        throw new ByteProcessorException(e.getMessage());
                    }
                }
            }
        }
    }

    public void open() {
        ret = new HashSequence(blockSize);
        block = new LinkedList(blockSize, 1024);
    }

    public void processByte(int read) {
        byte bRead = (byte)read;
        totalRead++;
        currentQuickHash = HashTool.hash(currentQuickHash, bRead);
        block.add(bRead);
        if (totalRead % blockSize == 0) {
            ret.add(currentQuickHash, block.computeHash(hashAlgorithm), position++, blockSize);
            currentQuickHash = 0;
        }
    }

    public HashSequence getSequence() {
        return ret;
    }
}
