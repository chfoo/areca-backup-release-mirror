package com.application.areca.metadata;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.application.areca.ArecaTechnicalConfiguration;
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
public abstract class AbstractMetadataAdapter {
	protected boolean DEBUG = ArecaTechnicalConfiguration.get().isMetaDataDebugMode();
	
    protected static final String DATA_CHARSET = "UTF-8";
    protected static final String VERSION_HEADER = "#### MDT_FORMAT_VERSION=";
    //protected static final String VERSION = VERSION_HEADER + "1"; // Initial metadata version : uses the default character encoding
    //protected static final String VERSION = VERSION_HEADER + "2"; // uses UTF-8 encoding
    //protected static final String VERSION = VERSION_HEADER + "3"; // new separators
    //protected static final String VERSION = VERSION_HEADER + "4"; // new posix attributes format
    protected static final String VERSION = VERSION_HEADER + "5"; // traces and contents are now ordered
    
    private FileTool TOOL = FileTool.getInstance();
    
    /**
     * Writer
     */
    protected Writer writer;
    
    /**
     * Counts the element that have been written
     */
    protected long written = 0;
    
    /**
     * OS
     */
    protected OutputStream outputStream;
    
    /**
     * Tells wether the content is compressed or not
     */
    protected boolean isCompressed = true;
    
    /**
     * File
     */
    protected File file;
    
    protected void initOutputStream() throws IOException {
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
            
            this.written = 0;
        }
    } 
 
    public File getFile() {
		return file;
	}

	protected void initWriter() throws IOException {
        if (writer == null) {
            initOutputStream();
            this.writer = new OutputStreamWriter(this.outputStream, DATA_CHARSET);
            this.writer.write(getVersionHeader() + "\n");
        }
    }    
    
    protected String getVersionHeader() {
        return VERSION;
    }
    
    public void close() throws IOException {
        if (writer != null) {
            this.writer.flush();
            this.writer.close();
        } else if (outputStream != null) {
            this.outputStream.flush();
            this.outputStream.close();
        }
        
        if (! FileSystemManager.exists(file)) {
            TOOL.createDir(FileSystemManager.getParentFile(file));
            FileSystemManager.createNewFile(file);
        }
    }
    
    protected InputStream getInputStream() throws IOException {
        if (isCompressed && FileSystemManager.length(file) != 0) {
	        return new GZIPInputStream(FileSystemManager.getCachedFileInputStream(file));
        } else {
	        return FileSystemManager.getCachedFileInputStream(file);
        }
    }
    
    protected long getVersion() throws IOException {
    	long version = 1L;
        FileTool tool = FileTool.getInstance();
        String firstLine = tool.getFirstRow(getInputStream(), DATA_CHARSET);
        if (firstLine != null) {
        	if (firstLine.startsWith(VERSION_HEADER)) {
		        String str = firstLine.substring(VERSION_HEADER.length()).trim();
		        version = Long.parseLong(str);
        	}
        	
	        if (version < 5) {
	        	Logger.defaultLogger().warn("Incompatible metadata format : version=" + version);
	        	throw new IllegalArgumentException("The archive your are trying to read was created with an old version of Areca. Your current version of Areca (" + VersionInfos.getLastVersion().getVersionId() + ") is not compatible with archives created with older versions than 7.0.");
	        }
        }
        
        return version;
    }
    
    public abstract AbstractMetaDataEntry decodeEntry(String line);
    
    protected String resolveEncoding(long version) throws IOException {
        if (version >= 2) {
            return DATA_CHARSET; // Version >= 2 <=> UTF-8
        } else {
            return null; // Version == 1 <=> Default charset
        }
    }
    
    public boolean isCompressed() {
        return isCompressed;
    }
    
    public void setCompressed(boolean isCompressed) {
        this.isCompressed = isCompressed;
    }

    public long getWritten() {
        return written;
    }
}
