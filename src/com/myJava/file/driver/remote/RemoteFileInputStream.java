package com.myJava.file.driver.remote;

import java.io.IOException;
import java.io.InputStream;

import com.myJava.file.driver.remote.ftp.FTPConnectionException;
import com.myJava.util.log.Logger;

/**
 * InputStream implementation that wraps a source InputStream and redefines the "close" operation.
 * <BR>
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
public class RemoteFileInputStream extends InputStream {

    private AbstractProxy proxy;
    private InputStream in;
    private String ownerId;
    private boolean closed;

    /**
     * @param proxy
     * @param in
     */
    public RemoteFileInputStream(AbstractProxy proxy, InputStream in, String ownerId) {
        super();
        this.proxy = proxy;
        this.in = in;
        this.ownerId = ownerId;
        this.closed = false;
    }

    public int available() throws IOException {
        if (in != null) {
            return in.available();
        } else {
            return 0;
        }
    }

    public void close() throws IOException {
        try {
            if (closed) {
                throw new IOException("Stream already closed.");
            }
            proxy.debug("InputStream : close()");
            if (in != null) {
                in.close();
            }
            this.closed = true;
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
        if (in != null) {
            in.mark(readlimit);   
        }
    }
    
    public boolean markSupported() {
        if (in != null) {
            return in.markSupported();
        } else {
            return false;
        }
    }
    
    public int read() throws IOException {
        if (in != null) {
            return in.read();
        } else {
            return -1;
        }
    }
    
    public int read(byte[] b, int off, int len) throws IOException {
/*
    	double prob = 0.03;
    	double rnd = 0.5*(Util.getRnd() + 1);
    	if (rnd < prob) {
    		String msg = "RANDOM ERROR (" + rnd + ")";
    		throw new IOException(msg);
    	}
*/
    	
        if (in != null) {
            return in.read(b, off, len);
        } else {
            return -1;
        }
    }
    
    public int read(byte[] b) throws IOException {
        if (in != null) {
            return in.read(b);
        } else {
            return -1;
        }
    }
    
    public synchronized void reset() throws IOException {
        if (in != null) {
            in.reset();
        }
    }
    
    public long skip(long n) throws IOException {
        if (in != null) {
            return in.skip(n);
        } else {
            return 0;
        }
    }
}
