package com.application.areca.metadata.content;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.application.areca.impl.FileSystemRecoveryEntry;
import com.application.areca.metadata.AbstractMetaDataEntry;
import com.application.areca.metadata.AbstractMetadataAdapter;
import com.application.areca.metadata.MetadataConstants;
import com.application.areca.metadata.MetadataEncoder;
import com.myJava.file.delta.sequence.HashSequence;
import com.myJava.file.iterator.FilePathComparator;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;

/**
 * 
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
public class ArchiveContentAdapter extends AbstractMetadataAdapter {
    protected String previousKey = null;
    
    public ArchiveContentAdapter(File contentFile) {
        this.file = contentFile;
    }
    
    private void writeSerializedEntry(String entry) throws IOException {
        initWriter();
        this.writer.write("\r\n" + entry);
    }
    
    public void writeGenericEntry(ContentEntry entry) throws IOException {
    	checkEntry(entry.getKey());
    	writeGenericEntry(entry.getKey(), entry.getData());
    }
    
    public void writeGenericEntry(String key, String data) throws IOException {
    	checkEntry(key);
    	writeSerializedEntry(key + MetadataConstants.SEPARATOR + data);
    }
    
    public void writeContentEntry(FileSystemRecoveryEntry entry) throws IOException {
    	checkEntry(entry.getKey());
    	writeSerializedEntry(ArchiveContentParser.serialize(entry));
    }
    
    public void writeHashEntry(FileSystemRecoveryEntry entry, byte[] hash) throws IOException {
    	checkEntry(entry.getKey());
    	writeSerializedEntry(ArchiveContentParser.serialize(entry, hash));
    }
    
    public void writeSequenceEntry(FileSystemRecoveryEntry entry, HashSequence sequence) throws IOException {
    	checkEntry(entry.getKey());
    	writeSerializedEntry(ArchiveContentParser.serialize(entry, sequence));
    }
    
    private void checkEntry(String key) {
    	if (previousKey != null) {
	    	if (FilePathComparator.instance().compare(key, previousKey) <= 0) {
	    		throw new IllegalStateException(key + " <= " + previousKey);
	    	}
    	}
    }
    
    /**
     * Same as traverseContentFile but ignored TaskCancelledExceptions
     * <BR>(useful when we know they will never be thrown)
     */
    public void traverseContentFileNoCancel(ContentHandler handler, ProcessContext context) throws IOException {
    	try {
			traverseContentFile(handler, context);
		} catch (TaskCancelledException ignored) {
		}
    }
    
    public AbstractMetaDataEntry decodeEntry(String serialized) {
    	if (DEBUG) {
    		Logger.defaultLogger().fine("Parsing content : [" + serialized + "]");
    	}
    	ContentEntry entry = new ContentEntry();
    	
		int index = serialized.indexOf(MetadataConstants.SEPARATOR);
		String key;
		String data;
		if (index == -1) {
			key = MetadataEncoder.decode(serialized);
			data = null;
		} else {
			key = MetadataEncoder.decode(serialized.substring(0, index));
			data = serialized.substring(index + MetadataConstants.SEPARATOR.length());
		} 
		
		entry.setKey(key);
		entry.setData(data);
		
		return entry;
    }
    
    /**
     * Read the archive content file line by line and call the ContentHandler provided as argument
     * for each line.
     */
    public void traverseContentFile(ContentHandler handler, ProcessContext context) throws IOException, TaskCancelledException {
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
                	ContentEntry entry = (ContentEntry)decodeEntry(line);
        			
        			// It is VERY important that the key / data are compliant with the current serialization
        			// format. Each line can indeed be written as part of a new content (case of archive merge)
        			// and will be assumed to match the current serialization format.
                	handler.newRow(entry.getKey(), entry.getData(), context);
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
    
    /**
     * Build a ContentFileIterator
     */
    public ContentFileIterator buildIterator(boolean deleteOnClose) throws IOException {
    	ContentFileIterator iter = buildIterator();
    	if (deleteOnClose) {
    		iter.setDeleteOnClose(this.file);
    	}
    	return iter;
    }
    
    /**
     * Build a ContentFileIterator
     */
    public ContentFileIterator buildIterator() throws IOException {
		 long version = getVersion();
	     String encoding = resolveEncoding(version);
	     InputStream in = this.getInputStream();
         BufferedReader reader = new BufferedReader(encoding == null ? new InputStreamReader(in) : new InputStreamReader(in, encoding)); 

         ContentFileIterator iter = new ContentFileIterator(reader, this);
         return iter;
    }
    
	
	public static void handleFile(File file, ContentHandler contentHandler, ProcessContext context) throws IOException, ApplicationException {
		if (file != null) {
			ArchiveContentAdapter reader = new ArchiveContentAdapter(file);
			reader.traverseContentFileNoCancel(contentHandler, context);
		}
	}
}
