package com.application.areca.adapters;

import org.w3c.dom.Node;

import com.application.areca.AbstractTarget;
import com.application.areca.adapters.read.TargetXMLReader;
import com.application.areca.impl.policy.FTPFileSystemPolicy;
import com.application.areca.impl.policy.FileSystemPolicy;
import com.myJava.util.xml.AdapterException;
import com.myJava.util.xml.XMLTool;

/**
 * Serializer/Deserializer for FTPFileSystemPolicy instances.
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
public class FTPFileSystemPolicyXMLHandler
extends AbstractFileSystemPolicyXMLHandler {
	
    public FileSystemPolicy read(
    		Node mediumNode, 
    		AbstractTarget target,
    		TargetXMLReader reader
    ) throws AdapterException {
    	
        Node serverNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_HOST);
        Node portNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_PORT);
        Node passivNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_PASSIV);
        Node protocolNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_PROTOCOL);
        Node protectionNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_PROTECTION);
        Node implicitNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_IMPLICIT);
        Node loginNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_LOGIN);
        Node dirNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_REMOTEDIR);
        Node nameNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ARCHIVENAME);
        Node ctrlEncodingNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_CTRL_ENCODING);
        Node ignorePsvErrorsNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_IGNORE_PSV_ERR);

        // No storage policy found
        if (serverNode == null && portNode == null && passivNode == null && loginNode == null && dirNode == null) {
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
        policy.setPassiveMode(passivNode != null && passivNode.getNodeValue().equalsIgnoreCase("true"));
        policy.setIgnorePsvErrors(ignorePsvErrorsNode != null && ignorePsvErrorsNode.getNodeValue().equalsIgnoreCase("true"));
        
        if (protocolNode != null) {
            policy.setProtocol(protocolNode.getNodeValue());
            policy.setImplicit(implicitNode != null && implicitNode.getNodeValue().equalsIgnoreCase("true"));    
            
            if (protectionNode != null) {
                policy.setProtection(protectionNode.getNodeValue());
            } else {
                policy.setProtection("P");
            }
        }

        String password = XMLTool.extractPassword(XML_MEDIUM_FTP_PASSWORD, mediumNode);
        
        policy.setLogin(loginNode.getNodeValue());
        if (password != null) {
        	// Standard case
        	policy.setPassword(password);
        } else {
        	// FTP Password missing
        	if (reader.getMissingDataListener() != null) {
        		Object[] ftpData = (Object[])reader.getMissingDataListener().missingFTPDataDetected(target, reader.getSource());
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
        
        if (ctrlEncodingNode != null && ! "null".equals(ctrlEncodingNode.getNodeValue())) {
        	policy.setControlEncoding(ctrlEncodingNode.getNodeValue());
        }
        
        return policy;
    }

    public void write(
    		FileSystemPolicy source, 
    		boolean removeSensitiveData, 
    		StringBuffer sb) {
    	
        FTPFileSystemPolicy policy = (FTPFileSystemPolicy)source;

        sb.append(XMLTool.encodeProperty(XML_MEDIUM_ARCHIVENAME, policy.getArchiveName()));
        sb.append(XMLTool.encodeProperty(XML_MEDIUM_FTP_HOST, policy.getRemoteServer()));
        sb.append(XMLTool.encodeProperty(XML_MEDIUM_FTP_PORT, policy.getRemotePort()));
        sb.append(XMLTool.encodeProperty(XML_MEDIUM_FTP_PASSIV, policy.isPassivMode()));
        sb.append(XMLTool.encodeProperty(XML_MEDIUM_FTP_IGNORE_PSV_ERR, policy.isIgnorePsvErrors()));

        if (policy.getProtocol() != null)  {
            sb.append(XMLTool.encodeProperty(XML_MEDIUM_FTP_PROTOCOL, policy.getProtocol()));
            sb.append(XMLTool.encodeProperty(XML_MEDIUM_FTP_IMPLICIT, policy.isImplicit()));
            sb.append(XMLTool.encodeProperty(XML_MEDIUM_FTP_PROTECTION, policy.getProtection()));
        }
        
        sb.append(XMLTool.encodeProperty(XML_MEDIUM_FTP_LOGIN, policy.getLogin()));
        
        if (! removeSensitiveData) {
            sb.append(XMLTool.encodePassword(XML_MEDIUM_FTP_PASSWORD, policy.getPassword()));
        }
        
        if (policy.getControlEncoding() != null) {
            sb.append(XMLTool.encodeProperty(XML_MEDIUM_FTP_CTRL_ENCODING, policy.getControlEncoding()));
        }
        sb.append(XMLTool.encodeProperty(XML_MEDIUM_FTP_REMOTEDIR, policy.getRemoteDirectory()));
    }
}
