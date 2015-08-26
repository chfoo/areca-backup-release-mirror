package com.myJava.file.delta.sequence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.myJava.file.delta.Constants;
import com.myJava.file.delta.tools.IOHelper;
import com.myJava.util.log.Logger;

/**
 * Format : 
 * - VERSION : short
 * - BLOCKSIZE : long
 * For each entry :
 * - Quick hash : int
 * - Index : long
 * - Size : int
 * - Full hash : byte[]
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
public class SequenceAdapter implements Constants {
    private static final short VERSION = 1;

    private static SequenceAdapter INSTANCE = new SequenceAdapter();
    
    public static SequenceAdapter getInstance() {
    	return INSTANCE;
    }
    
    public void serialize(OutputStream out, HashSequence sequence) throws IOException {
        IOHelper.writeShort(VERSION, out);
        IOHelper.writeLong(sequence.getBlockSize(), out);
        SimilarEntrySet[] sets = sequence.getInternalData();
        for (int i=0; i<sets.length; i++) {
        	if (sets[i] != null) {
	            Iterator iter = sets[i].iterator();
	            while(iter.hasNext()) {
	                HashSequenceEntry entry = (HashSequenceEntry)iter.next();
	                IOHelper.writeInt(entry.getQuickHash(), out);
	                IOHelper.writeLong(entry.getIndex(), out);
	                IOHelper.writeInt(entry.getSize(), out);
	                out.write(entry.getFullHash());
	            }
        	}
        }
    }
    
    public HashSequence deserialize(InputStream in) throws IOException {
        byte[] sig = new byte[2 + 8];
        byte[] entryData = new byte[4 + 8 + 4];
        int nb = IOHelper.readFully(in, sig);
        long nbBuckets = 0;
        if (nb == -1) {
            return null;
        } else {
            int blockSize = (int)IOHelper.get64(sig, 2);
            HashSequence seq = new HashSequence(blockSize);
            while (IOHelper.readFully(in, entryData) != -1) {
                byte[] fullHash = new byte[HASH_ALG_KLENGTH];
                IOHelper.readFully(in, fullHash);
                nbBuckets++;
                seq.add(
                        (int)IOHelper.get32(entryData, 0), // quick hash
                        fullHash, 
                        IOHelper.get64(entryData, 4), // index
                        (int)IOHelper.get32(entryData, 12) // size
                );
            }

            //Logger.defaultLogger().fine("Sequence adapter : " + nbBuckets + " buckets read.");
            return seq;
        }
    }
    
    public HashSequence deserialize(byte[] data) {
    	HashSequence ret = null;
    	try {
        	GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(data));
    		try {
				ret = deserialize(in);
			} finally {
				in.close();
			}
    	} catch (IOException e) {
    		Logger.defaultLogger().error(e);
    		throw new IllegalArgumentException(e);
    	}
    	return ret;
    }
    
    public byte[] serialize(HashSequence sequence) {
       	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	try {
        	GZIPOutputStream zout = new GZIPOutputStream(out);
	    	try {
	    		serialize(zout, sequence);
	    	} finally {
	    		zout.close();
	    	}
    	} catch (IOException e) {
    		Logger.defaultLogger().error(e);
    		throw new IllegalArgumentException(e);
    	}
    	return out.toByteArray();
    }
}
