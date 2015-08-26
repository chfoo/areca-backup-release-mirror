package com.myJava.file.multivolumes;

import java.io.IOException;
import java.io.InputStream;

import com.myJava.util.log.Logger;

/**
 * Inputstream implementation that reads multiple volumes.
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
public class VolumeInputStream extends InputStream {

    protected VolumeStrategy strategy;
    protected InputStream in;
    protected byte[] singleByteBuffer = new byte[1];

    public VolumeInputStream(VolumeStrategy strategy) {
        this.strategy = strategy;
        try {
            in = strategy.getNextInputStream();
        } catch (IOException e) {
            Logger.defaultLogger().error(e);
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public void close() throws IOException {
        if (in != null) {
            in.close();
        }
    }

    public int read() throws IOException {
        int ret = read(singleByteBuffer);
        if (ret == -1) {
            return -1;
        } else {
            return singleByteBuffer[0]& 0xff;
        }
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int available() throws IOException {
        throw new UnsupportedOperationException();
    }

    public long skip(long n) throws IOException {
        throw new UnsupportedOperationException();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (in == null) {
            return -1;
        } else {
            int read = in.read(b, off, len);
            if (read < len && read != -1) {
                int off2 = off+read;
                int len2 = len - read;
                int read2 = read(b, off2, len2);

                return read + (read2 == -1 ? 0 : read2);
            } else if (read == -1) {
                in.close();
                in = strategy.getNextInputStream();

                if (in == null) {
                    return -1;
                } else {
                    return read(b, off, len);
                }
            } else {
                return read;
            }
        }
    }

    public synchronized void reset() throws IOException {
        throw new UnsupportedOperationException("Reset is not supported on this implementation");
    }

    public synchronized void mark(int readlimit) {
        throw new UnsupportedOperationException("Mark is not supported on this implementation");
    }

    public boolean markSupported() {
        return false;
    }
}
