package com.myJava.file.delta.sequence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.object.ToStringHelper;


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
public class HashSequence {
    private static final int SIZE = FrameworkConfiguration.getInstance().getDeltaHashMapSize();
    
    private int blockSize; // For information purpose;
    private int size = 0;
    private List retList = new ArrayList();
    private SimilarEntrySet[] entries = new SimilarEntrySet[SIZE];

    public HashSequence(int blockSize) {
        this.blockSize = blockSize;
    }

    public void add(int quickHash, byte[] fullHash, long bucketPosition, int bucketSize) {
        int index = computeIndex(quickHash);
        if (entries[index] == null) {
            entries[index] = new SimilarEntrySet();
        }
        size++;
        entries[index].add(new HashSequenceEntry(quickHash, fullHash, bucketPosition, bucketSize));
    }
    
    public SimilarEntrySet[] getInternalData() {
        return entries;
    }
    
    public int getIndexIfExist(int quickHash) {
        int index = computeIndex(quickHash);
        if (entries[index] == null) {
            return -1;
        } else {
        	SimilarEntrySet set = entries[index];
        	for (int i=0; i<set.size(); i++) {
                HashSequenceEntry entry = (HashSequenceEntry)set.get(i);
                if (entry.getQuickHash() == quickHash) {
                    return index;
                }
            }
            return -1;
        }
    }

    public int getBlockSize() {
        return blockSize;
    }

    private int computeIndex(int quickHash) {
        return Math.abs(quickHash)%SIZE;
    }
    
    /**
     * CAUTION : The return list is recycled before each call to this method.
     */
    public List get(int quickHash, int index, byte[] fullHash) {
        retList.clear();
        if (entries[index] != null) {
        	SimilarEntrySet set = entries[index];
        	for (int e=0; e<set.size(); e++) {
        		
        		// Compare quick hash
                HashSequenceEntry entry = (HashSequenceEntry)set.get(e);
                if (entry.getQuickHash() == quickHash) {
                	
                	// Compare full hash
                    if (fullHash.length == entry.getFullHash().length) {
                        boolean eq = true;
                        for (int i=0; i<fullHash.length; i++) {
                            if (fullHash[i] != entry.getFullHash()[i]) {
                                eq = false;
                                break;
                            }
                        }
                        
                        if (eq) {
                            retList.add(entry);
                        }
                    }
                }
            }
        }
        return retList;
    }

    public int getSize() {
        return size;
    }

    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        for (int i=0; i<SIZE; i++) {
            if (this.entries[i] != null) {
                ToStringHelper.append("\nBucket" + i, entries[i], sb);
            }
        }
        return ToStringHelper.close(sb);
    }
}
