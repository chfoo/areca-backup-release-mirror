package com.myJava.file.ftp;

import java.io.IOException;
import java.io.InputStream;

import com.myJava.util.log.Logger;

/**
 * InputStream implementation that wraps a source InputStream and redefines the "close" operation.
 * <BR>
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -1628055869823963574
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
public class FTPFileInputStream extends InputStream {

    private FTPProxy proxy;
    private InputStream in;
    private String ownerId;

    /**
     * @param proxy
     * @param in
     */
    public FTPFileInputStream(FTPProxy proxy, InputStream in, String ownerId) {
        super();
        this.proxy = proxy;
        this.in = in;
        this.ownerId = ownerId;
    }

    public int available() throws IOException {
        return in.available();
    }

    public void close() throws IOException {
        try {
            proxy.debug("InputStream : close()");
            in.close();
        } catch (Exception e) {
            Logger.defaultLogger().error(e);
            throw new IOException(e.getMessage());
        } finally {
            try {
                proxy.completePendingCommand(false);
            } catch (FTPConnectionException e) {
                Logger.defaultLogger().error(e);                
            } finally {
                this.proxy.releaseLock(ownerId);
            }
        }
    }
    
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
    }
    
    public boolean markSupported() {
        return in.markSupported();
    }
    
    public int read() throws IOException {
        return in.read();
    }
    
    public int read(byte[] b, int off, int len) throws IOException {
        return in.read(b, off, len);
    }
    
    public int read(byte[] b) throws IOException {
        return in.read(b);
    }
    
    public synchronized void reset() throws IOException {
        in.reset();
    }
    
    public long skip(long n) throws IOException {
        return in.skip(n);
    }
}
