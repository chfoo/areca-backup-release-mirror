package com.application.areca.metadata;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.application.areca.cache.ObjectPool;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;

/**
 * Abstract implementation for metada adapters
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4331497872542711431
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
public class AbstractMetadataAdapter {

    protected static final String DATA_CHARSET = "UTF-8";
    protected static final String VERSION_HEADER = "#### MDT_FORMAT_VERSION=";
    protected static final String VERSION_1 = VERSION_HEADER + "1"; // Initial metadata version : uses the default character encoding
    protected static final String VERSION_2 = VERSION_HEADER + "2"; // Latest metadata version : uses UTF-8 encoding
    
    private FileTool TOOL = FileTool.getInstance();
    
    /**
     * Optional object pool.
     */
    protected ObjectPool objectPool = null;
    
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
    
    protected void initWriter() throws IOException {
        if (writer == null) {
            initOutputStream();
            this.writer = new OutputStreamWriter(this.outputStream, DATA_CHARSET);
            this.writer.write(getVersionHeader() + "\n");
        }
    }    
    
    protected String getVersionHeader() {
        return VERSION_2 ;
    }
    
    public void setObjectPool(ObjectPool objectPool) {
        this.objectPool = objectPool;
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
	        return new GZIPInputStream(FileSystemManager.getFileInputStream(file));
        } else {
	        return FileSystemManager.getFileInputStream(file);
        }
    }
    
    protected String resolveEncoding() throws IOException {
        FileTool tool = FileTool.getInstance();
        String firstLine = tool.getFirstRow(getInputStream(), DATA_CHARSET);
        if (firstLine != null && firstLine.startsWith(VERSION_2)) {
            return DATA_CHARSET; // Version2 <=> UTF-8
        } else {
            return null; // Version1 <=> Default charset
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
