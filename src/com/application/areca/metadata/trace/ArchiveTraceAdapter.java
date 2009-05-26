package com.application.areca.metadata.trace;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.application.areca.context.ProcessContext;
import com.application.areca.impl.FileSystemRecoveryEntry;
import com.application.areca.metadata.AbstractMetaDataEntry;
import com.application.areca.metadata.AbstractMetadataAdapter;
import com.application.areca.metadata.MetadataConstants;
import com.application.areca.metadata.MetadataEncoder;
import com.myJava.file.iterator.FilePathComparator;
import com.myJava.file.metadata.FileMetaDataSerializationException;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;

/**
 * <BR>File adapter for ArchiveTrace read/write operations.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

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
public class ArchiveTraceAdapter extends AbstractMetadataAdapter {
    /**
     * Boolean that sets whether directories must be read or not
     */
    protected boolean trackSymlinks;
    protected boolean trackMetaData;
    
    protected String previousKey = null;
    
    public ArchiveTraceAdapter(File traceFile) throws IOException {
        this(traceFile, false);
    }
    
    public ArchiveTraceAdapter(File traceFile, boolean trackSymlinks) {
        this.trackSymlinks = trackSymlinks;
        this.file = traceFile;
    }
   
    public void setTrackPermissions(boolean trackMetaData) {
        this.trackMetaData = trackMetaData;
    }
    
    public void writeEntry(FileSystemRecoveryEntry entry) 
    throws IOException, FileMetaDataSerializationException {
    	if (previousKey != null) {
	    	if (FilePathComparator.instance().compare(entry.getKey(), previousKey) <= 0) {
	    		throw new IllegalStateException(entry.getKey() + " <= " + previousKey);
	    	}
    	}
    	writeEntry(ArchiveTraceParser.serialize(entry, trackMetaData, trackSymlinks));
    }
    
    public void writeEntry(char type, String key, String data) throws IOException {
        writeEntry(type + MetadataEncoder.encode(key) + MetadataConstants.SEPARATOR + data);
    }
    
    public void writeEntry(String serializedEntry) throws IOException {
        initWriter();
        this.writer.write("\r\n" + serializedEntry);
        this.written++;
    }
    
    /**
     * Read the archive trace file line by line and call the TraceHandler provided as argument
     * for each line.
     */
    public void traverseTraceFile(TraceHandler handler, ProcessContext context) 
    throws IOException, FileMetaDataSerializationException, TaskCancelledException {
        long version = getVersion();
        String encoding = resolveEncoding(version);
        handler.setVersion(version);
        InputStream in = this.getInputStream();
        
        try {
            BufferedReader reader = new BufferedReader(encoding == null ? new InputStreamReader(in) : new InputStreamReader(in, encoding));            
            String line = null;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() != 0 && !header) {
                	TraceEntry entry = (TraceEntry)decodeEntry(line);
                	
            		// It is VERY important that the key / hash are compliant with the current serialization
            		// format. Each line can indeed be written as part of a new trace (case of archive merge)
            		// and will be assumed to match the current serialization format.
                	handler.newRow(entry.getType(), entry.getKey(), entry.getData(), context);
                }
            	header = false;
            }
        	handler.close();
        } finally {
            try {
            	in.close();
            } catch (Exception ignored) {
            }
        }
    }
    
    public AbstractMetaDataEntry decodeEntry(String serialized) {
    	if (DEBUG) {
    		Logger.defaultLogger().fine("Parsing trace : [" + serialized + "]");
    	}
    	TraceEntry entry = new TraceEntry();
    	
		int index = serialized.indexOf(MetadataConstants.SEPARATOR);
		String key;
		String hash;
		if (index == -1) {
			key = MetadataEncoder.decode(serialized);
			hash = null;
		} else {
			key = MetadataEncoder.decode(serialized.substring(0, index));
			hash = serialized.substring(index + MetadataConstants.SEPARATOR.length());
		}
		//handle current directory
		entry.setType(key.charAt(0));  	// register the type marker
		entry.setKey( key.substring(1)); 		// remove the type marker
    	entry.setData(hash);
		
		return entry;
    }
    
    /**
     * Build a TraceFileIterator
     */
    public TraceFileIterator buildIterator() throws IOException {
		 long version = getVersion();
	     String encoding = resolveEncoding(version);
	     InputStream in = this.getInputStream();
         BufferedReader reader = new BufferedReader(encoding == null ? new InputStreamReader(in) : new InputStreamReader(in, encoding)); 

         TraceFileIterator iter = new TraceFileIterator(reader, this);
         return iter;
    }
}
