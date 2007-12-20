package com.application.areca.adapters;

import org.w3c.dom.Node;

import com.application.areca.impl.policy.FTPFileSystemPolicy;
import com.application.areca.impl.policy.FileSystemPolicy;
import com.application.areca.plugins.FileSystemPolicyXMLHandler;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4331497872542711431
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
public class FTPFileSystemPolicyXMLHandler
implements FileSystemPolicyXMLHandler, XMLTags {

    public FileSystemPolicy read(Node mediumNode) throws AdapterException {
        Node serverNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_HOST);
        Node portNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_PORT);
        Node passivNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_PASSIV);
        Node protocolNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_PROTOCOL);
        Node protectionNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_PROTECTION);
        Node implicitNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_IMPLICIT);
        Node loginNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_LOGIN);
        Node passwordNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_PASSWORD);
        Node dirNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_REMOTEDIR);
        
        // No storage policy found
        if (serverNode == null && portNode == null && passivNode == null && loginNode == null && passwordNode == null && dirNode == null) {
            throw new AdapterException("Medium storage policy not found : your medium must have either a '" + XML_MEDIUM_ARCHIVEPATH + "' attribute or FTP attributes (" + XML_MEDIUM_FTP_HOST + ", " + XML_MEDIUM_FTP_LOGIN + ", " + XML_MEDIUM_FTP_PASSWORD + " ...)");            
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
        if (passwordNode == null) {
            throw new AdapterException("FTP password not found : your medium must have a '" + XML_MEDIUM_FTP_PASSWORD + "' attribute.");
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
        policy.setPassword(passwordNode.getNodeValue());
        policy.setRemoteDirectory(dirNode.getNodeValue());
        policy.setId(POLICY_FTP);
        return policy;
    }

    public void write(FileSystemPolicy source, StringBuffer sb) {
        FTPFileSystemPolicy policy = (FTPFileSystemPolicy)source;
        
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
        
        sb.append(" ");
        sb.append(XML_MEDIUM_FTP_PASSWORD);
        sb.append("=");
        sb.append(AbstractXMLWriter.encode(policy.getPassword()));
        
        sb.append(" ");
        sb.append(XML_MEDIUM_FTP_REMOTEDIR);
        sb.append("=");
        sb.append(AbstractXMLWriter.encode(policy.getRemoteDirectory()));
    }
}
