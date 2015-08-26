package com.myJava.file.multivolumes;

import java.io.IOException;
import java.io.OutputStream;

import com.myJava.util.log.Logger;

/**
 * Outputstream implementation that slices the output into multiple volumes.
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
public class VolumeOutputStream extends OutputStream {

    protected VolumeStrategy strategy;
    protected OutputStream out;
    protected long currentVolumeSize = 0;
    protected long totalWritten = 0;
    
    /**
     * Volume Size (bytes)
     */
    protected long volumeSize = 0;
    
    public VolumeOutputStream(VolumeStrategy strategy, long volumeSize) {
        this.strategy = strategy;
        this.volumeSize = volumeSize;
        try {
            openNextVolume();
        } catch (IOException e) {
            Logger.defaultLogger().error(e);
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    
    public VolumeStrategy getStrategy() {
        return strategy;
    }

    public void close() throws IOException {
        out.close();
    }
    
    public void flush() throws IOException {
        out.flush();
    }
    
    public long getTotalWritten() {
        return this.totalWritten;
    }
    
    public long getWrittenInCurrentVolume() {
        return this.currentVolumeSize;
    }
    
    public void write(byte[] b, int off, int len) throws IOException {
        if (currentVolumeSize + len <= volumeSize) {
            writeData(b, off, len);
        } else {
            int firstPart = (int)(volumeSize - currentVolumeSize);
            writeData(b, off, firstPart);
            openNextVolume();
            this.write(b, firstPart + off, len - firstPart);
        }
    }
    
    private void openNextVolume() throws IOException {
        if (out != null) {
            out.close();
        }
        out = strategy.getNextOutputStream();
        currentVolumeSize = 0;
    }
    
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }
    
    public void write(int b) throws IOException {
        this.write(new byte[] {(byte)b});
    }

    private void writeData(byte[] b, int off, int len) throws IOException {
        totalWritten += len;
        currentVolumeSize += len;
        out.write(b, off, len);    
    }
    
    public void ensureCapacity(long length) throws IOException {
        if (this.currentVolumeSize + length > volumeSize) {
            openNextVolume();
        }
    }
}
