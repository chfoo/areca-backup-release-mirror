package com.myJava.file.driver.remote.ftp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

import org.apache.commons.net.DefaultSocketFactory;
import org.apache.commons.net.SocketClient;
import org.apache.commons.net.SocketFactory;
import org.apache.commons.net.ftp.FTPConnectionClosedException;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.ssl.NoCheckX509TrustManager;
import com.myJava.util.log.Logger;

/**
 * SocketFactory implementation that creates Secured Sockets suitable for the FTPSClient class.
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
public class SecuredSocketFactory implements SocketFactory {

    private static TrustManager[] NO_CHECK_TM = new TrustManager [] {new NoCheckX509TrustManager()};
    public static String[] PROTECTIONS = new String[] {"P", "C"};
    private static String KEY_ALGORITHM = "SunX509";
    private static String KEY_TYPE = "JKS";
    
    private DefaultSocketFactory unsecuredSocketFactory = new DefaultSocketFactory();
    private SSLContext sslContext = null;
    private String protocol = null;
    private String protection;
    private FTPSClient client;
    private boolean implicit = false;
    
    public SecuredSocketFactory(
            String protocol, 
            String protection,
            boolean checkServerCertificate,
            boolean implicit,
            InputStream certificateInputStream,
            String certificatePassword,
            FTPSClient client
    ) {
        Logger.defaultLogger().info("Initializing secured socket factory ...");
        acceptProtocol(protocol);
        this.protocol = protocol;
        this.protection = protection;
        
        if (protection == null || (! protection.equals("C") && ! protection.equals("P"))) {
            throw new IllegalArgumentException("Illegal protection method : [" + protection + "]. Only \"C\" and \"P\" are accepted.");
        }
        
        this.implicit = implicit;
        this.client = client;
        
        TrustManager tm[] = null;
        KeyManager km[] = null;
        
        // Init the keyStore if needed
        if (certificateInputStream != null) {
            try {
                Logger.defaultLogger().info("Loading certificate ...");
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KEY_ALGORITHM);
                KeyStore ks = KeyStore.getInstance(KEY_TYPE);
                char[] pwdChars = (certificatePassword == null ? null : certificatePassword.toCharArray());
                ks.load(certificateInputStream, pwdChars);
                kmf.init(ks, pwdChars);
                km = kmf.getKeyManagers();
            } catch (Exception e) {
                Logger.defaultLogger().error(e);
            }
        }
        
        // Init the trustmanager if needed
        if (! checkServerCertificate) {
            Logger.defaultLogger().info("Disabling server identification ...");
            tm = NO_CHECK_TM;
        }
        
        try {
            sslContext = SSLContext.getInstance(protocol);
            sslContext.init(km, tm, null);
        } catch (NoSuchAlgorithmException e) {
            Logger.defaultLogger().error(e);
        } catch (KeyManagementException e) {
            Logger.defaultLogger().error(e);
        }
    }
    
    private void acceptProtocol(String protocol) {
        String[] protocols = FrameworkConfiguration.getInstance().getSSEProtocols();
        for (int i=0; i<protocols.length; i++) {
            if (protocols[i].trim().equalsIgnoreCase(protocol.trim())) {
                Logger.defaultLogger().info(protocol + " protocol validated.");
                return;
            }
        }
        throw new IllegalArgumentException("Protocol not supported : " + protocol);
    }

	public ServerSocket createServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException {
        SSLServerSocket serverSocket = (SSLServerSocket)sslContext.getServerSocketFactory().createServerSocket(port, backlog, bindAddr);
        initServerSocket(serverSocket);
        return serverSocket;
    }
    
    public ServerSocket createServerSocket(int port, int backlog) throws IOException {
        SSLServerSocket serverSocket = (SSLServerSocket)sslContext.getServerSocketFactory().createServerSocket(port, backlog);
        initServerSocket(serverSocket);
        return serverSocket;
    }
    
    public ServerSocket createServerSocket(int port) throws IOException {
        SSLServerSocket serverSocket = (SSLServerSocket)sslContext.getServerSocketFactory().createServerSocket(port);
        initServerSocket(serverSocket);
        return serverSocket;
    }
    
    public Socket createSocket(InetAddress address, int port, InetAddress localAddr, int localPort) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    public Socket createSocket(InetAddress address, int port) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    public Socket createSocket(String host, int port, InetAddress localAddr, int localPort) throws UnknownHostException, IOException {
        throw new UnsupportedOperationException();
    }
    
    public Socket createSocket(String host, int port) throws UnknownHostException, IOException {
        if (implicit) {
            return createImplicitSocket(host, port);
        } else {
            return createExplicitSocket(host, port);
        }
    }
    
    public Socket createImplicitSocket(String host, int port) throws UnknownHostException, IOException {
        if (client.hasBeenNegociated()) {
            return (SSLSocket)sslContext.getSocketFactory().createSocket(host, port);
        } else {
            Logger.defaultLogger().info("Opening an implicit SSL connection on remote host");
            SSLSocket socket = (SSLSocket)sslContext.getSocketFactory().createSocket(host, port);
	        init(socket);
	
	        readReply(socket);
	        this.sendCommand("PBSZ 0", socket, true);
	        this.sendCommand("PROT " + protection, socket, false);
	        
	        return socket;
        }
    }
    
    public Socket createExplicitSocket(String host, int port) throws UnknownHostException, IOException {
        if (client.hasBeenNegociated()) {
            return (SSLSocket)sslContext.getSocketFactory().createSocket(host, port);
        } else {
            Logger.defaultLogger().info("Opening an explicit SSL connection on remote host");
	        Socket unsecured = unsecuredSocketFactory.createSocket(host, port);
	        readReply(unsecured);
	        this.sendCommand("AUTH " + protocol, unsecured, true);

	        SSLSocket socket = (SSLSocket)sslContext.getSocketFactory().createSocket(unsecured, host, port, true);
	        init(socket);

            this.sendCommand("PBSZ 0", socket, true);
            this.sendCommand("PROT " + protection, socket, false);

            return socket;
        }
    }
    
    private void init(SSLSocket socket) throws IOException{
        socket.setEnableSessionCreation(true);
        socket.setUseClientMode(true);
        socket.startHandshake();
        client.setNegociated();
    }

    private void initServerSocket(SSLServerSocket socket) throws IOException {
        socket.setUseClientMode(true);
    }

    private void sendCommand(String command, Socket socket, boolean readReply) throws IOException {
        Logger.defaultLogger().info("Sending FTP command : " + command);
        
        // Send Command
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), client.getControlEncoding()));
	    out.write(command + SocketClient.NETASCII_EOL);
        out.flush();

        if (readReply) {
            readReply(socket);            
        }
    }
    
    private void readReply(Socket socket) throws IOException {
        // Read response
        BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream(), client.getControlEncoding()));
        String line =  in.readLine();
        StringBuffer sb = new StringBuffer(line);
        int length = line.length();
        if (length > 3 && line.charAt(3) == '-') {
            do {
                line = in.readLine();
                sb.append(" / ").append(line);

                if (line == null) {
                    throw new FTPConnectionClosedException("Connection closed without indication.");
                }
            } while (! (
                    line.length() >= 4 
                    && line.charAt(3) != '-' 
                    && Character.isDigit(line.charAt(0))
            ));
        }
        Logger.defaultLogger().info("Received FTP server response : " + sb.toString());
    }
}
