package com.myJava.file.driver.remote;

import java.io.IOException;
import java.io.OutputStream;

import com.myJava.file.driver.remote.ftp.FTPConnectionException;
import com.myJava.util.log.Logger;

/**
 * OutputStream implementation that wraps a source OutputStream and redefines the "close" operation.
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
public class RemoteFileOutputStream extends OutputStream {
    
    private AbstractProxy proxy;
    private OutputStream out;
    private String ownerId;
    private long lastConnectionId;
    private String fileName;
    private boolean closed = false;
    private Runnable onClose;
    
    /**
     * @param proxy
     * @param in
     */
    public RemoteFileOutputStream(AbstractProxy proxy, OutputStream out, String ownerId, String fileName) throws FTPConnectionException {
        super();
        this.proxy = proxy;
        this.out = out;
        this.ownerId = ownerId;
        lastConnectionId = proxy.getConnectionId();
        this.fileName = fileName;
    }
    
    public void close() throws IOException {
        proxy.debug("OutputStream : close()");
        
        if (closed) {
            proxy.debug("Outputstream already closed.", "RemoteFileOutputStream.close()");     
        } else {
            try {
                if (proxy.hasReconnectSince(this.lastConnectionId)) {
                    Logger.defaultLogger().error("Unable to properly close the OutputStream for " + fileName + " since the remote connection has been reinitialized since the stream's creation.", "RemoteFileOutputStream.close()");
                    proxy.debug("Unable to properly close the OutputStream for " + fileName + " since the remote connection has been reinitialized since the stream's creation.", "RemoteFileOutputStream.close()");                
                    throw new IOException("Unable to properly close the OutputStream for " + fileName + " since the remote connection has been reinitialized since the stream's creation.");
                }
                try {
                    flush();
                } finally {
                    out.close();
                    closed = true;
                }
            } catch (IOException e) {
                Logger.defaultLogger().error(e);
                throw e;
            } finally {
                try {
                    proxy.completePendingCommand(true);
                    if (onClose != null) {
                    	onClose.run();
                    }
                } catch (FTPConnectionException e) {
                    Logger.defaultLogger().error("Unable to complete the remote data transfert for " + fileName, e, "RemoteFileOutputStream.close()");
                    proxy.debug("Unable to complete the remote data transfert for " + fileName, "RemoteFileOutputStream.close()");
                    throw new IOException("Unable to complete the remote data transfert for " + fileName + " : " + e.getMessage());
                } finally {
                    this.proxy.releaseLock(ownerId);
                    this.proxy.removeCachedFileInfos(fileName);
                }
            }
        }
    }
    
    public void flush() throws IOException {
        out.flush();
    }
    
    public void write(byte[] b, int off, int len) throws IOException {
    	out.write(b, off, len);
    }
    
    public void write(byte[] b) throws IOException {
        out.write(b);
    }
    
    public void write(int b) throws IOException {
        out.write(b);
    }

	public Runnable getOnClose() {
		return onClose;
	}

	public void setOnClose(Runnable onClose) {
		this.onClose = onClose;
	}
}
