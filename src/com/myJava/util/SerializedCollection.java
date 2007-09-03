package com.myJava.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.myJava.file.FileSystemManager;
import com.myJava.util.log.Logger;
import com.myJava.util.os.OSTool;

/**
 * Partial <code>Collection</code> implementation which allows to store large collections.
 * <BR>The object contained in this collection are serialized and written into a buffer file (which is automatically destroyed
 * when the VM stops).
 * <BR>All add elements must implement the <code>Serialized</code> interface.
 * <BR>Note that the <code>add</code> and <code>addAll</code> methods always return true and don't check wether the
 * added element is already contained in the collection.
 * <BR>
 * <BR>This class relies on ObjectOut/Inputstreams, but only uses primitive types serializations features to avoid using instance pooling
 * implemented by these classes when "read/writeObject" is called (which results in a growing HashTable)
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -2622785387388097396
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
public abstract class SerializedCollection implements Collection {
    
    protected File bufferFile;
    private ObjectOutputStream out;
    private long count = 0;
    private boolean initialized = false;
    private boolean locked = false;
    
    public SerializedCollection() {
        long rnd = Utilitaire.getRndLong();
        this.bufferFile = new File(OSTool.getTempDirectory(), "java_serialized_collection_" + rnd + ".tmp");
    }
    
    public SerializedCollection(File bufferFile) {
        this.bufferFile = bufferFile;
    }
    
    public int size() {
        return (int)this.count;
    }
    
    public void lock() throws IOException {
        checkInitialized();
        locked = true;
        try {
            this.out.flush();
        } finally {
            this.out.close();   
            this.out = null;
        }
    }
    
    protected abstract void writeObject(ObjectOutputStream out, Object o) throws IOException;
    protected abstract Object readObject(ObjectInputStream in) throws IOException, ClassNotFoundException;
    
    private void checkInitialized() throws IOException {
        if (! initialized) {
            out = new ObjectOutputStream(new BufferedOutputStream(new GZIPOutputStream(FileSystemManager.getFileOutputStream(this.bufferFile))));
            count = 0;
            initialized = true;
        }
    }
    
    public void clear() {
        if (out != null) {
            try {
                out.close();
                out = null;
            } catch (IOException e) {
                Logger.defaultLogger().error(e);
            }
        }
        
        if (FileSystemManager.exists(bufferFile)) {
            FileSystemManager.delete(bufferFile);
        }
        count = 0;
        
        initialized = false;
    }
    
    public boolean isEmpty() {
        return (count == 0);
    }
    
    public boolean add(Object o) {
        if (locked) {
            throw new IllegalStateException("The SerializedCollection has been locked. No more data can be added");
        }
        try {
            checkInitialized();
            this.writeObject(out, o);
            
            count++;
            return true;
        } catch (IOException e) {
            Logger.defaultLogger().error(e);
            throw new IllegalArgumentException("Unable to add the following element : " + (o == null ? "null":o.toString()));
        }
    }
    
    public boolean addAll(Collection c) {
        Iterator iter = c.iterator();
        boolean changed = false;
        while (iter.hasNext()) {
            changed = this.add(iter.next()) || changed;
        }
        return changed;
    }
    
    /* (non-Javadoc)
     * @see java.util.Collection#iterator()
     */
    public Iterator iterator() {
        if (! locked) {
            throw new IllegalStateException("The SerializedCollection must be closed first.");
        }
        return new SerializedIterator(this.bufferFile, this.count);
    }
    
    /* (non-Javadoc)
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection c) {
        throw new UnsupportedOperationException("This method is not supported by this implementation");
    }
    
    /* (non-Javadoc)
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
        throw new UnsupportedOperationException("This method is not supported by this implementation");
    }
    
    /* (non-Javadoc)
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("This method is not supported by this implementation");
    }
    
    /* (non-Javadoc)
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException("This method is not supported by this implementation");
    }
    
    /* (non-Javadoc)
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException("This method is not supported by this implementation");
    }
    
    /* (non-Javadoc)
     * @see java.util.Collection#toArray(java.lang.Object[])
     */
    public Object[] toArray(Object[] a) {
        throw new UnsupportedOperationException("This method is not supported by this implementation");
    }
    
    public Object[] toArray() {
        return toArray(new Object[0]);
    }
    
    private class SerializedIterator implements Iterator {
        private Object nextObject = null;
        private ObjectInputStream in;
        private long remainingItems = -1;
        
        public SerializedIterator(File bufferFile, long size) {
            try {
                in = new ObjectInputStream(new BufferedInputStream(new GZIPInputStream(FileSystemManager.getFileInputStream(bufferFile))));
                
                this.remainingItems = size;
                readNextObject();
            } catch (IOException e) {
                Logger.defaultLogger().error(e);
                throw new IllegalStateException(e.getMessage());
            }
        }
        
        private void readNextObject() {
            try {
                if (remainingItems > 0) {
                    nextObject = readObject(in);
                    remainingItems--;
                } else {
                    nextObject = null;
                }
            } catch (IOException e) {
                Logger.defaultLogger().error(e);
                throw new IllegalStateException(e.getMessage());
            } catch (ClassNotFoundException e) {
                Logger.defaultLogger().error(e);
                throw new IllegalStateException(e.getMessage());
            }
        }
        
        public boolean hasNext() {
            return (nextObject != null);
        }
        
        public Object next() {
            Object ret = this.nextObject;
            readNextObject();
            return ret;
        }
        
        public void remove() {
            throw new UnsupportedOperationException("This method is not supported by this implementation");
        }
    }
    
    public static void main(String[] args) {
        try {
            long nb = 1000;
            SerializedCollection col = new LongSerializedCollection(new File("/home/olivier/Desktop/toto.tmp"));
            for (long i=0; i<nb; i++) {
                col.add(new Long(i));
            }
            col.lock();
            
            Iterator iter = col.iterator();
            while (iter.hasNext()) {
                System.out.println(iter.next().toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static class LongSerializedCollection extends SerializedCollection {
        
        /**
         * @param bufferFile
         */
        public LongSerializedCollection(File bufferFile) {
            super(bufferFile);
        }
        
        protected Object readObject(ObjectInputStream in) throws IOException,
        ClassNotFoundException {
            
            return new Long(in.readLong());
        }
        protected void writeObject(ObjectOutputStream out, Object o)
        throws IOException {
            out.writeLong(((Long)o).longValue());
        }
    }
}
