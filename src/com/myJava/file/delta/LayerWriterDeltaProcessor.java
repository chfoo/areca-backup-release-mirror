package com.myJava.file.delta;

import java.io.IOException;
import java.io.OutputStream;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.delta.sequence.HashSequenceEntry;
import com.myJava.file.delta.tools.IOHelper;
import com.myJava.file.delta.tools.CircularList;
import com.myJava.util.log.Logger;

/**
 * Diff file format : Set of items, which are either :
 *              [NEW_BYTES_SIGNATURE : 8 bytes][SIZE : 4 bytes][DATA : *SIZE* bytes]
 *              or [FOUND_BLOCK_SIGNATURE : 8 bytes][FROM_POSITION : 8 bytes][TO_POSITION : 8 bytes]
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
public class LayerWriterDeltaProcessor
implements DeltaProcessor, Constants {
	private static final boolean DEBUG = FrameworkConfiguration.getInstance().isDeltaDebugMode();
    private static final int BUFFER_SIZE = 1024 * 1024;

    private OutputStream out;
    private long currentPosition = 0; // Position in the original file
    
    private byte[] buffer;
    private int bufferIndex = 0;
    
    private long from = -1;
    
    public LayerWriterDeltaProcessor(OutputStream out) {
        this.out = out;
        this.buffer = new byte[BUFFER_SIZE];
    }

    private void flushNewBytes() throws DeltaProcessorException {
        if (bufferIndex != 0) {
    		if (DEBUG) {
    			Logger.defaultLogger().fine("Flushing " + bufferIndex + " new bytes.");
    		}
            try {
                writeLong(SIG_NEW);
                writeInt(bufferIndex);
                writeBuffer();
                bufferIndex = 0;
            } catch (IOException e) {
                Logger.defaultLogger().error(e);
                throw new DeltaProcessorException(e);
            }
        }
    }
    
    private void flushReadBlocks() throws DeltaProcessorException {
        if (from != -1) {
            try {
        		if (DEBUG) {
        			Logger.defaultLogger().fine("Flushing read buckets : from " + from + " to " + (currentPosition-1));
        		}
                writeLong(SIG_READ);
                writeLong(from);
                writeLong(currentPosition - 1);
                from = -1;
            } catch (IOException e) {
                Logger.defaultLogger().error(e);
                throw new DeltaProcessorException(e);
            }
        }
    }
    
    public void blockFound(HashSequenceEntry entry, CircularList block) throws DeltaProcessorException {
		if (DEBUG) {
			Logger.defaultLogger().fine("Bucket found : index=" + entry.getIndex() + ", size=" + entry.getSize() + ", quickHash=" + entry.getQuickHash());
		}
        flushNewBytes();
        if (from == -1) {
            from = currentPosition;
        }
        currentPosition += entry.getSize();
    }
    
    public void blockFound(long readFrom, long readTo) throws DeltaProcessorException {
        flushNewBytes();
        if (from == -1) {
            from = readFrom;
        }
        currentPosition = readTo + 1;
    }

    public void newBytes(byte[] data, int offset, int len) throws DeltaProcessorException {
    	if (len == 0) {
            flushReadBlocks();
    	}
    	
        for (int i=offset; i<offset+len; i++) {
            newByte(data[i]);
        }
    }
    
    public void newByte(byte data) throws DeltaProcessorException {
        flushReadBlocks();

        if (bufferIndex == buffer.length) {
            flushNewBytes();
        }
        
        buffer[bufferIndex] = data;
        bufferIndex++;
    }
    
    public void bytesLost(long from, long to) throws DeltaProcessorException {
		if (DEBUG) {
			Logger.defaultLogger().fine("Deleted data : " + (to-from+1) + " bytes.");
		}
        flushNewBytes();
        flushReadBlocks();
        
        currentPosition+=to-from+1;
    }

    public void begin() throws DeltaProcessorException {
    }

    public void end() throws DeltaProcessorException {
        flushNewBytes();
        flushReadBlocks();
    }
    
    private void writeInt(long v) throws IOException {
        IOHelper.writeInt(v, out);
    }
    
    private void writeLong(long v) throws IOException {
        IOHelper.writeLong(v, out);       
    }
    
    private void writeBuffer() throws IOException {
        out.write(buffer, 0, bufferIndex);
    }
}
