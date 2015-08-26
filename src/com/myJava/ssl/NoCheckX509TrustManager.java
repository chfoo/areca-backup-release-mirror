package com.myJava.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.X509TrustManager;

import com.myJava.util.log.Logger;

/**
 * This trustManager is dedicated to server certificate verifications. 
 * It does not check that the certificate is present in the VM's keyStore.
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
public class NoCheckX509TrustManager implements X509TrustManager  {
    private Set alreadyLogged = new HashSet();
    
    public void checkClientTrusted(X509Certificate[] certs, String arg1) throws CertificateException {
        throw new CertificateException("NoCheckX509TrustManager can't be used for client certification.");
    }
    
    public void checkServerTrusted(X509Certificate[] certs, String alg) throws CertificateException {
        for (int i=0; i<certs.length; i++) {
            String name = certs[i].getIssuerDN().getName();
            if (! alreadyLogged.contains(name)) {
                Logger.defaultLogger().info("Server data (" + alg + ") : " + name, "NoCheckX509TrustManager");
                alreadyLogged.add(name);
            }
        }
    }
    
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}