package com.application.areca.metadata.trace;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.application.areca.ArecaConfiguration;
import com.application.areca.context.ProcessContext;
import com.application.areca.impl.FileSystemRecoveryEntry;
import com.application.areca.metadata.AbstractMetaDataEntry;
import com.application.areca.metadata.AbstractMetadataAdapter;
import com.application.areca.metadata.MetadataConstants;
import com.application.areca.metadata.MetadataEncoder;
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
public class ArchiveTraceAdapter extends AbstractMetadataAdapter {
	protected boolean trackSymlinks;

	private ArchiveTraceAdapter(File traceFile) throws IOException {
		this(traceFile, null, false);
	}

	public ArchiveTraceAdapter(File traceFile, String prefix, boolean trackSymlinks) {
		super(traceFile, prefix, ArecaConfiguration.get().useGzip());
		this.trackSymlinks = trackSymlinks;
	}

	public void writeEntry(FileSystemRecoveryEntry entry, String shaBase64) 
	throws IOException, FileMetaDataSerializationException {
		write(ArchiveTraceParser.serialize(entry, trackSymlinks, shaBase64));
	}

	public void writeEntry(char type, String key, String data) throws IOException {
		write(type + MetadataEncoder.getInstance().encode(key) + MetadataConstants.SEPARATOR + data);
	}

	/**
	 * Read the archive trace file line by line and call the TraceHandler provided as argument
	 * for each line.
	 */
	private void traverse(TraceHandler handler, ProcessContext context) 
	throws IOException, FileMetaDataSerializationException, TaskCancelledException {
		MetadataHeader hdr = getMetaData();
		String encoding = hdr.getEncoding();
		handler.setVersion(hdr.getVersion());

		InputStream in = this.buildInputStream();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(encoding == null ? new InputStreamReader(in) : new InputStreamReader(in, encoding));            
			String line = null;

			// Skip the header
			skipHeader(reader);
			
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() != 0) {
					TraceEntry entry = (TraceEntry)decodeEntry(line);

					// It is VERY important that the key / hash are compliant with the current serialization
					// format. Each line can indeed be written as part of a new trace (case of archive merge)
					// and will be assumed to match the current serialization format.
					handler.newRow(entry.getType(), entry.getKey(), entry.getData(), context);
				}
			}
			handler.close();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				in.close();
			} catch (Exception ignored) {
			}
		}
	}

	public boolean isTrackSymlinks() {
		return trackSymlinks;
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
			key = MetadataEncoder.getInstance().decode(serialized);
			hash = null;
		} else {
			key = MetadataEncoder.getInstance().decode(serialized.substring(0, index));
			hash = serialized.substring(index + MetadataConstants.SEPARATOR.length());
		}
		//handle current directory
		entry.setType(key.charAt(0));  			// register the type marker
		entry.setKey(key.substring(1)); 		// remove the type marker
		entry.setData(hash);

		return entry;
	}

	/**
	 * Build a TraceFileIterator
	 */
	private TraceFileIterator buildIterator() throws IOException {
		String encoding = getMetaData().getEncoding();
		
		InputStream in = this.buildInputStream();
		BufferedReader reader = new BufferedReader(encoding == null ? new InputStreamReader(in) : new InputStreamReader(in, encoding)); 

		// Skip the header
		skipHeader(reader);
		
		// Build the iterator
		TraceFileIterator iter = new TraceFileIterator(reader, this);
		return iter;
	}

	public static TraceFileIterator buildIterator(File traceFile) throws IOException {
		if (traceFile == null) {
			throw new NullPointerException("Trace file name shall not be null");
		}
		ArchiveTraceAdapter adapter = new ArchiveTraceAdapter(traceFile);
		return adapter.buildIterator();
	}
	
	public static void traverseTraceFile(TraceHandler handler, File traceFile, ProcessContext context) 
	throws IOException, FileMetaDataSerializationException, TaskCancelledException {
		ArchiveTraceAdapter adapter = new ArchiveTraceAdapter(traceFile);
		adapter.traverse(handler, context);
	}
	
	protected AbstractMetadataAdapter buildReader(File sourceFile) throws IOException {
		return new ArchiveTraceAdapter(sourceFile);
	}
}
