package com.application.areca.adapters;

import org.w3c.dom.Node;

import com.application.areca.AbstractTarget;
import com.application.areca.adapters.read.TargetXMLReader;
import com.application.areca.impl.policy.FileSystemPolicy;
import com.application.areca.impl.policy.SFTPFileSystemPolicy;
import com.myJava.util.xml.AdapterException;
import com.myJava.util.xml.XMLTool;

/**
 * Serializer/Deserializer for SFTPFileSystemPolicy instances.
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
public class SFTPFileSystemPolicyXMLHandler
extends AbstractFileSystemPolicyXMLHandler {
	
    public static final String POLICY_SFTP  = "sftp";
    public static final String XML_MEDIUM_SFTP_LOGIN = "sftp_login";
    public static final String XML_MEDIUM_SFTP_PASSWORD = "sftp_password";
    public static final String XML_MEDIUM_SFTP_HOST = "sftp_host";
    public static final String XML_MEDIUM_SFTP_PORT = "sftp_port";
    public static final String XML_MEDIUM_SFTP_REMOTEDIR= "sftp_remotedir";
    public static final String XML_MEDIUM_SFTP_HOSTKEY= "sftp_hostkey";
    public static final String XML_MEDIUM_SFTP_CHECK_HOSTKEY= "sftp_check_hostkey";
    public static final String XML_MEDIUM_SFTP_USE_CERT= "sftp_use_certificate";
    public static final String XML_MEDIUM_SFTP_CERT= "sftp_certificate_path";
    public static final String XML_MEDIUM_SFTP_ENC_CERT= "sftp_encrypted_cert";
	
    public FileSystemPolicy read(
    		Node mediumNode, 
    		AbstractTarget target,
    		TargetXMLReader reader
    ) throws AdapterException {
    	
        Node serverNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_SFTP_HOST);
        Node portNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_SFTP_PORT);
        Node loginNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_SFTP_LOGIN);
        Node dirNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_SFTP_REMOTEDIR);
        Node nameNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ARCHIVENAME);
        Node hkNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_SFTP_HOSTKEY);
        Node checkhkNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_SFTP_CHECK_HOSTKEY);
        Node encryptedCertNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_SFTP_ENC_CERT);
        Node useCertNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_SFTP_USE_CERT);
        Node certNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_SFTP_CERT);
        
        // No storage policy found
        if (serverNode == null && portNode == null && loginNode == null && dirNode == null) {
            throw new AdapterException("Medium storage policy not found : your medium must have either a '" + XML_MEDIUM_PATH + "' attribute or SFTP attributes (" + XML_MEDIUM_FTP_HOST + ", " + XML_MEDIUM_FTP_LOGIN + ", " + XML_MEDIUM_FTP_PASSWORD + " ...)");            
        }
        
        // SFTP policy initialization
        if (serverNode == null) {
            throw new AdapterException("SFTP host not found : your medium must have a '" + XML_MEDIUM_SFTP_HOST + "' attribute.");
        } 
        if (portNode == null) {
            throw new AdapterException("SFTP remote port not found : your medium must have a '" + XML_MEDIUM_SFTP_PORT + "' attribute.");
        } 
        
        boolean encryptedCert = encryptedCertNode != null && encryptedCertNode.getNodeValue().equalsIgnoreCase("true");
        
        boolean checkHostKey = checkhkNode != null && checkhkNode.getNodeValue().equalsIgnoreCase("true");
        if (checkHostKey && hkNode == null) {
            throw new AdapterException("SFTP hostkey not found : your medium must have a '" + XML_MEDIUM_SFTP_HOSTKEY + "' attribute.");
        } 
        if (loginNode == null) {
            throw new AdapterException("SFTP login not found : your medium must have a '" + XML_MEDIUM_SFTP_LOGIN + "' attribute.");
        } 
        if (dirNode == null) {
            throw new AdapterException("SFTP remote directory not found : your medium must have a '" + XML_MEDIUM_SFTP_REMOTEDIR + "' attribute.");
        } 
        
        boolean useCert = useCertNode != null && useCertNode.getNodeValue().equalsIgnoreCase("true");
        if (useCert && certNode == null) {
            throw new AdapterException("SFTP certificate path not found : your medium must have a '" + XML_MEDIUM_SFTP_CERT + "' attribute.");
        } 
        
        SFTPFileSystemPolicy policy = new SFTPFileSystemPolicy();
        policy.setUseCertificateAuth(useCert);
        policy.setCertificateFileName(certNode == null ? null : certNode.getNodeValue());
        policy.setRemoteServer(serverNode.getNodeValue());
        policy.setRemotePort(Integer.parseInt(portNode.getNodeValue()));
        policy.setLogin(loginNode.getNodeValue());
        policy.setCheckHostKey(checkHostKey);
        policy.setEncryptedCert(encryptedCert);
        if (hkNode != null) {
        	policy.setHostKey(hkNode.getNodeValue());
        }
        
        String password = XMLTool.extractPassword(XML_MEDIUM_SFTP_PASSWORD, mediumNode);
        
        if (password != null) {
        	// Standard case
        	policy.setPassword(password);
        } else if ((! useCert) || encryptedCert ) {
        	// SFTP Password missing
        	if (reader.getMissingDataListener() != null) {
        		Object[] sftpData = (Object[])reader.getMissingDataListener().missingFTPDataDetected(target, reader.getSource());
                if (sftpData != null) {
                	policy.setPassword((String)sftpData[0]);
                }
            }
            
            if (policy.getPassword() == null || policy.getPassword().trim().length() == 0) { // Second check .... after missingDataListener invocation.
                throw new AdapterException("No SFTP password found : your medium must have a '" + XML_MEDIUM_SFTP_PASSWORD + "' attribute.");
            }
        }
        policy.setRemoteDirectory(dirNode.getNodeValue());
        policy.setId(POLICY_SFTP);
        policy.setArchiveName(nameNode.getNodeValue());

        return policy;
    }

    public void write(
    		FileSystemPolicy source, 
    		boolean removeSensitiveData, 
    		StringBuffer sb) {
    	
        SFTPFileSystemPolicy policy = (SFTPFileSystemPolicy)source;
        
        sb.append(XMLTool.encodeProperty(XML_MEDIUM_ARCHIVENAME, policy.getArchiveName()));
        sb.append(XMLTool.encodeProperty(XML_MEDIUM_SFTP_HOST, policy.getRemoteServer()));
        sb.append(XMLTool.encodeProperty(XML_MEDIUM_SFTP_PORT, policy.getRemotePort()));
        sb.append(XMLTool.encodeProperty(XML_MEDIUM_SFTP_HOSTKEY, policy.getHostKey()));
        sb.append(XMLTool.encodeProperty(XML_MEDIUM_SFTP_CHECK_HOSTKEY, policy.isCheckHostKey()));
        sb.append(XMLTool.encodeProperty(XML_MEDIUM_SFTP_LOGIN, policy.getLogin()));
        sb.append(XMLTool.encodeProperty(XML_MEDIUM_SFTP_USE_CERT, policy.isUseCertificateAuth()));
        sb.append(XMLTool.encodeProperty(XML_MEDIUM_SFTP_CERT, policy.getCertificateFileName()));
        sb.append(XMLTool.encodeProperty(XML_MEDIUM_SFTP_ENC_CERT, policy.isEncryptedCert()));
        
        if (! removeSensitiveData) {
            sb.append(XMLTool.encodePassword(XML_MEDIUM_SFTP_PASSWORD, policy.getPassword()));
        }
        
        sb.append(XMLTool.encodeProperty(XML_MEDIUM_SFTP_REMOTEDIR, policy.getRemoteDirectory()));
    }
}
