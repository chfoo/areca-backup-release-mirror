package com.myJava.file.delta;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.myJava.file.delta.bucket.Bucket;
import com.myJava.file.delta.bucket.NewBytesBucket;
import com.myJava.file.delta.bucket.ReadPreviousBucket;
import com.myJava.file.delta.tools.IOHelper;
import com.myJava.object.ToStringHelper;

/**
 * Implements a diff layer over the original file.
 * <BR>Diff layers only store differences between the original file and the current file.
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
public class DeltaLayer 
implements Constants {
    
    private byte[] tmpBytes = new byte[8];
    private NewBytesBucket tmpOverride = new NewBytesBucket();
    private ReadPreviousBucket tmpPrevious = new ReadPreviousBucket();
    private long tmpFrom = 0;
    
    private InputStream stream;
    private Bucket currentBucket;
    private String name;

    public DeltaLayer(InputStream stream, String name) {
        this.stream = stream;
        this.name = name;
        //this.stream.mark(1024*1024*1024); // Why this instruction ?? It causes OutOfMemory exceptions when recovering large files and doesn't seem to be of any use ...
    }

    public InputStream getStream() {
        return stream;
    }
    
    public void close() throws IOException {
        stream.close();
    }
    
    public Bucket getCurrentBucket() {
        return currentBucket;
    }
    
    /**
     * Read the next bucket from the stream.
     * <BR>This bucket can be accessed by the "getCurrentBucket()" method.
     */
    public void readNextBucket() throws IOException {
        int read = IOHelper.readFully(stream, tmpBytes);
        
        if (read == -1) {
            currentBucket = null;
        } else {
            long sig = IOHelper.get64(tmpBytes, 0);
            if (sig == SIG_NEW) {
                currentBucket = tmpOverride;
            } else if (sig == SIG_READ) {
                currentBucket = tmpPrevious;            
            } else {
            	String message = "Illegal signature : " + ToStringHelper.serialize(tmpBytes) + " (" + sig + "). ";
            	ByteArrayOutputStream baos = new ByteArrayOutputStream();
            	message += "Expected ";
            	IOHelper.writeLong(SIG_NEW, baos);
            	message += ToStringHelper.serialize(baos.toByteArray()) + " or ";
            	baos.reset();
            	IOHelper.writeLong(SIG_READ, baos);
            	message += ToStringHelper.serialize(baos.toByteArray()) + ".";
            	throw new IOException(message);
            }
            currentBucket.init(stream);
            currentBucket.setFrom(tmpFrom);
            tmpFrom += currentBucket.getLength();
        }
    }
    
    /**
     * Read the whole content of the underlying stream and generates a String.
     */
    public String traverse() throws IOException {
    	stream.reset();
    	
        StringBuffer sb = new StringBuffer();
        readNextBucket();
        while (currentBucket != null) {
            sb.append("\n").append(currentBucket.toString());
            if (currentBucket.getSignature() == SIG_NEW) {
            	IOHelper.skipFully(stream, currentBucket.getLength());
            }
            readNextBucket();
        }
        return sb.toString();
    }
    
    public String toString() {
    	StringBuffer sb = ToStringHelper.init(this);
    	ToStringHelper.append("Name", name, sb);
    	ToStringHelper.append("CurrentBucket", currentBucket, sb);    		
    	return ToStringHelper.close(sb);
    }
}
