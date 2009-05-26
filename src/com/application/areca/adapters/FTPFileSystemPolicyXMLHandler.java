package com.application.areca.adapters;

import org.w3c.dom.Node;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.impl.policy.FTPFileSystemPolicy;
import com.application.areca.impl.policy.FileSystemPolicy;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

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
public class FTPFileSystemPolicyXMLHandler
extends AbstractFileSystemPolicyXMLHandler {
	
    public FileSystemPolicy read(
    		Node mediumNode, 
    		AbstractRecoveryTarget target,
    		TargetXMLReader reader
    ) throws AdapterException {
    	
        Node serverNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_HOST);
        Node portNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_PORT);
        Node passivNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_PASSIV);
        Node protocolNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_PROTOCOL);
        Node protectionNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_PROTECTION);
        Node implicitNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_IMPLICIT);
        Node loginNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_LOGIN);
        Node passwordNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_PASSWORD);
        Node dirNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_REMOTEDIR);
        Node nameNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ARCHIVENAME);
        
        // No storage policy found
        if (serverNode == null && portNode == null && passivNode == null && loginNode == null && passwordNode == null && dirNode == null) {
            throw new AdapterException("Medium storage policy not found : your medium must have either a '" + XML_MEDIUM_PATH + "' attribute or FTP attributes (" + XML_MEDIUM_FTP_HOST + ", " + XML_MEDIUM_FTP_LOGIN + ", " + XML_MEDIUM_FTP_PASSWORD + " ...)");            
        }
        
        // FTP policy initialization
        if (serverNode == null) {
            throw new AdapterException("FTP host not found : your medium must have a '" + XML_MEDIUM_FTP_HOST + "' attribute.");
        } 
        if (portNode == null) {
            throw new AdapterException("FTP remote port not found : your medium must have a '" + XML_MEDIUM_FTP_PORT + "' attribute.");
        } 
        if (loginNode == null) {
            throw new AdapterException("FTP login not found : your medium must have a '" + XML_MEDIUM_FTP_LOGIN + "' attribute.");
        } 
        if (dirNode == null) {
            throw new AdapterException("FTP remote directory not found : your medium must have a '" + XML_MEDIUM_FTP_REMOTEDIR + "' attribute.");
        } 

        FTPFileSystemPolicy policy = new FTPFileSystemPolicy();
        policy.setRemoteServer(serverNode.getNodeValue());
        policy.setRemotePort(Integer.parseInt(portNode.getNodeValue()));
        policy.setPassivMode(passivNode != null && passivNode.getNodeValue().equalsIgnoreCase("true"));
        
        if (protocolNode != null) {
            policy.setProtocol(protocolNode.getNodeValue());
            policy.setImplicit(implicitNode != null && implicitNode.getNodeValue().equalsIgnoreCase("true"));    
            
            if (protectionNode != null) {
                policy.setProtection(protectionNode.getNodeValue());
            } else {
                policy.setProtection("P");
            }
        }

        policy.setLogin(loginNode.getNodeValue());
        if (passwordNode != null) {
        	// Standard case
        	policy.setPassword(passwordNode.getNodeValue());
        } else {
        	// FTP Password missing
        	if (reader.getMissingDataListener() != null) {
        		Object[] ftpData = (Object[])reader.getMissingDataListener().missingFTPDataDetected(target);
                if (ftpData != null) {
                	policy.setPassword((String)ftpData[0]);
                }
            }
            
            if (policy.getPassword() == null || policy.getPassword().trim().length() == 0) { // Second check .... after missingDataListener invocation.
                throw new AdapterException("No FTP password found : your medium must have a '" + XML_MEDIUM_FTP_PASSWORD + "' attribute.");
            }
        }
        policy.setRemoteDirectory(dirNode.getNodeValue());
        policy.setId(POLICY_FTP);
        
        if (version == 1) {
        	policy.setArchiveName(buildDeprecatedArchiveName("bck", TargetXMLReader.isOverwrite(mediumNode)));
        } else {
        	policy.setArchiveName(nameNode.getNodeValue());
        }
        
        return policy;
    }

    public void write(
    		FileSystemPolicy source, 
    		TargetXMLWriter writer, 
    		boolean removeSensitiveData, 
    		StringBuffer sb) {
    	
        FTPFileSystemPolicy policy = (FTPFileSystemPolicy)source;
        
        sb.append(" ");
        sb.append(XML_MEDIUM_ARCHIVENAME);
        sb.append("=");
        sb.append(AbstractXMLWriter.encode(policy.getArchiveName()));
        
        sb.append(" ");
        sb.append(XML_MEDIUM_FTP_HOST);
        sb.append("=");
        sb.append(AbstractXMLWriter.encode(policy.getRemoteServer()));
        
        sb.append(" ");
        sb.append(XML_MEDIUM_FTP_PORT);
        sb.append("=");
        sb.append(AbstractXMLWriter.encode("" + policy.getRemotePort()));
        
        sb.append(" ");
        sb.append(XML_MEDIUM_FTP_PASSIV);
        sb.append("=");
        sb.append(AbstractXMLWriter.encode("" + policy.isPassivMode()));
        
        if (policy.getProtocol() != null)  {
            sb.append(" ");
            sb.append(XML_MEDIUM_FTP_PROTOCOL);
            sb.append("=");
            sb.append(AbstractXMLWriter.encode("" + policy.getProtocol()));
            
            sb.append(" ");
            sb.append(XML_MEDIUM_FTP_IMPLICIT);
            sb.append("=");
            sb.append(AbstractXMLWriter.encode("" + policy.isImplicit()));
            
            sb.append(" ");
            sb.append(XML_MEDIUM_FTP_PROTECTION);
            sb.append("=");
            sb.append(AbstractXMLWriter.encode("" + policy.getProtection()));
        }
        
        sb.append(" ");
        sb.append(XML_MEDIUM_FTP_LOGIN);
        sb.append("=");
        sb.append(AbstractXMLWriter.encode(policy.getLogin()));
        
        if (! removeSensitiveData) {
	        sb.append(" ");
	        sb.append(XML_MEDIUM_FTP_PASSWORD);
	        sb.append("=");
	        sb.append(AbstractXMLWriter.encode(policy.getPassword()));
        }
        
        sb.append(" ");
        sb.append(XML_MEDIUM_FTP_REMOTEDIR);
        sb.append("=");
        sb.append(AbstractXMLWriter.encode(policy.getRemoteDirectory()));
    }
}
