package com.application.areca.metadata;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.application.areca.ArecaConfiguration;
import com.application.areca.version.VersionInfos;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.util.log.Logger;

/**
 * Abstract implementation for metada adapters
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
public abstract class AbstractMetadataAdapter {
	protected boolean DEBUG = ArecaConfiguration.get().isMetaDataDebugMode();

	protected static final String DATA_CHARSET = "UTF-8";
	protected static final String VERSION_HEADER = "#### MDT_FORMAT_VERSION=";
	protected static final String GLOBAL_PREFIX_HEADER = "#### PREFIX=";
	//protected static final String VERSION = VERSION_HEADER + "1"; // Initial metadata version : uses the default character encoding
	//protected static final String VERSION = VERSION_HEADER + "2"; // uses UTF-8 encoding
	//protected static final String VERSION = VERSION_HEADER + "3"; // new separators
	//protected static final String VERSION = VERSION_HEADER + "4"; // new posix attributes format
	//protected static final String VERSION = VERSION_HEADER + "5"; // traces and contents are now ordered
	//protected static final String VERSION = VERSION_HEADER + "6"; // The global file prefix is written in the file's header
	protected static final String VERSION = VERSION_HEADER + "7"; // Trace format modification : files traces now include sha hash

	private static FileTool TOOL = FileTool.getInstance();

	/**
	 * Writer
	 */
	private Writer writer;

	/**
	 * OS
	 */
	private OutputStream outputStream;

	/**
	 * Tells wether the content is compressed or not
	 */
	private boolean isCompressed = true;

	/**
	 * Global prefix of the metadata file. All names are considered relative to this prefix
	 */
	private String globalPrefix;

	/**
	 * File header in "read" mode
	 */
	private MetadataHeader header = null;

	/**
	 * File
	 */
	protected File file;
	
	protected boolean closed = false;

	/**
	 * Secondary adapter - used for transaction point serialization
	 */
	protected AbstractMetadataAdapter secondaryAdapter;

	public AbstractMetadataAdapter(File file, String globalPrefix, boolean compressed) {
		this.file = file;
		this.globalPrefix = globalPrefix;
		this.isCompressed = compressed;
	}

	private void initOutputStream() throws IOException {
		if (outputStream == null) {
			File parent = FileSystemManager.getParentFile(file);
			if (! FileSystemManager.exists(parent)) {
				TOOL.createDir(parent);
			}

			if (isCompressed) {
				// Metadata are compressed
				this.outputStream = new GZIPOutputStream(
						FileSystemManager.getCachedFileOutputStream(file) // METADATA are written in "cached" mode
				);
			} else {
				this.outputStream = FileSystemManager.getCachedFileOutputStream(file); // METADATA are written in "cached" mode
			}
		}
	} 

	public File getFile() {
		return file;
	}
	
	private void initWriter() throws IOException {
		if (writer == null) {
			initOutputStream();
			this.writer = new OutputStreamWriter(this.outputStream, DATA_CHARSET);
			this.writer.write(VERSION + "\n" + GLOBAL_PREFIX_HEADER + MetadataEncoder.getInstance().encode(this.globalPrefix) + "\n");
		}
	}

	protected void write(String content) throws IOException {
		this.initWriter();
		this.writer.write("\r\n" + content);

		// Write in secondary Adapter
		if (this.secondaryAdapter != null) {
			this.secondaryAdapter.write(content);
		}
	}  

	public String getGlobalPrefix() {
		return globalPrefix;
	}

	public void close() throws IOException {
    	if (closed) {
    		return;
    	}
    	closed = true;
    	
		// Make sure that the file is properly created (with its header)
		initWriter();
    	
		try {
			if (writer != null) {
				try {
					this.writer.flush();
				} finally {
					this.writer.close();
				}
			} else if (outputStream != null) {
				try {
					this.outputStream.flush();
				} finally {
					this.outputStream.close();
				}
			}

			/*
			if (! FileSystemManager.exists(file)) {
				TOOL.createDir(FileSystemManager.getParentFile(file));
				FileSystemManager.createNewFile(file);
			}
			*/
		} finally {
			// Close secondary Adapter
			if (this.secondaryAdapter != null) {
				this.secondaryAdapter.close();
			}
		}
	}

	protected InputStream buildInputStream() throws IOException {
		if (isCompressed && FileSystemManager.length(file) != 0) {
			return new GZIPInputStream(FileSystemManager.getCachedFileInputStream(file));
		} else {
			return FileSystemManager.getCachedFileInputStream(file);
		}
	}

	protected MetadataHeader getMetaData() throws IOException {
		if (this.header == null) {
			long version = 0L;
			String prefix = null;
			FileTool tool = FileTool.getInstance();
			String[] lines = tool.getInputStreamRows(buildInputStream(), DATA_CHARSET, 2, true);
			if (lines != null) {
				if (lines.length > 0) {
					if (lines[0].startsWith(VERSION_HEADER)) {
						String str = lines[0].substring(VERSION_HEADER.length()).trim();
						version = Long.parseLong(str);
					}
				}

				if (version >= 6 && lines.length > 1) {
					// Version 6 or newer : global prefix is written in file's header
					prefix = MetadataEncoder.getInstance().decode(lines[1].substring(GLOBAL_PREFIX_HEADER.length()).trim());
				}
			}

			if (version != 0 && version < 5) {
				Logger.defaultLogger().warn("Incompatible metadata format : version=" + version);
				throw new IllegalArgumentException("The archive your are trying to read was created with an old version of " + VersionInfos.APP_SHORT_NAME + ". Your current version of " + VersionInfos.APP_SHORT_NAME + " (" + VersionInfos.getLastVersion().getVersionId() + ") is not compatible with archives created with older versions than 7.0.");
			}

			this.header = new MetadataHeader(version, prefix);
		}
		return this.header;
	}

	public abstract AbstractMetaDataEntry decodeEntry(String line);

	public AbstractMetadataAdapter getSecondaryAdapter() {
		return secondaryAdapter;
	}

	public void setSecondaryAdapter(AbstractMetadataAdapter secondaryAdapter) {
		this.secondaryAdapter = secondaryAdapter;
	}

	/**
	 * Skip the file's header
	 */
	public void skipHeader(BufferedReader reader) throws IOException {
		for (int i=0; i<getMetaData().getHeaderSize(); i++) {
			reader.readLine();
		}
	}
	
	protected abstract AbstractMetadataAdapter buildReader(File sourceFile) throws IOException;
	
	/**
	 * Copy the content to the destination adapter.
	 * <BR>The destination adapter is not closed - further data can be happened
	 */
	public void bulkInit(File sourceFile) throws IOException {
		AbstractMetadataAdapter source = buildReader(sourceFile);
		
		MetadataHeader hdr = source.getMetaData();
		String encoding = hdr.getEncoding();
		InputStream in = source.buildInputStream();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(encoding == null ? new InputStreamReader(in) : new InputStreamReader(in, encoding));            
			String line = null;
			
			// Skip the header
			source.skipHeader(reader);
			
			// Copy data
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() != 0) {
					write(line);
				}
			}
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

	public static class MetadataHeader {
		private long version = -1;
		private String globalPrefix = null;

		public MetadataHeader(long version, String globalPrefix) {
			this.version = version;
			this.globalPrefix = globalPrefix;
		}

		public long getVersion() {
			return version;
		}

		public String getGlobalPrefix() {
			return globalPrefix;
		}

		public String getEncoding() {
			if (version >= 2) {
				return DATA_CHARSET; // Version >= 2 <=> UTF-8
			} else {
				return null; // Version == 1 <=> Default charset
			}
		}

		public int getHeaderSize() {
			if (getVersion() < 6) {
				return 1;
			} else {
				return 2;
			}
		}
	}
}
