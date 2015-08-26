package com.myJava.file.driver.remote.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import javax.net.ssl.SSLSocket;


/**
 * FTPClient subclass that manages secured sockets
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
public class FTPSClient extends FTPClient {
    
    private boolean negociated = false;
    
    public FTPSClient(
            String protocol, 
            String protection, 
            boolean implicit,
            InputStream certificateInputStream,
            String certificatePassword,
            boolean ignorePsvErrors
    ) {
        super(ignorePsvErrors);
        this.setSocketFactory(
                new SecuredSocketFactory(
                        protocol, 
                        protection, 
                        false, 
                        implicit,
                        certificateInputStream,
                        certificatePassword,
                        this)
        );
    }
    
    public boolean hasBeenNegociated() {
        return negociated;
    }
    
    public void setNegociated() {
        this.negociated = true;
    }
    
    protected Socket _openDataConnection_(int command, String arg) throws IOException {
        SSLSocket socket = (SSLSocket)super._openDataConnection_(command, arg);
        if (socket != null) {
            socket.setEnableSessionCreation(true);
            socket.setUseClientMode(true);
            socket.startHandshake();
        }
        return socket;
    }   
    
    public void disconnect() throws IOException {
        this._socket_.close();
    }
}