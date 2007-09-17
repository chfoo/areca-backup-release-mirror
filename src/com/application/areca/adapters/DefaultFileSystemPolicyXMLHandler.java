package com.application.areca.adapters;

import org.w3c.dom.Node;

import com.application.areca.impl.policy.DefaultFileSystemPolicy;
import com.application.areca.impl.policy.FileSystemPolicy;
import com.application.areca.plugins.FileSystemPolicyXMLHandler;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 3732974506771028333
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
public class DefaultFileSystemPolicyXMLHandler
implements FileSystemPolicyXMLHandler, XMLTags {

    public FileSystemPolicy read(Node mediumNode) throws AdapterException {
        Node pathNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ARCHIVEPATH);
        DefaultFileSystemPolicy policy = new DefaultFileSystemPolicy();
        policy.setBaseArchivePath(pathNode.getNodeValue());
        policy.setId(POLICY_HD);
        return policy;
    }

    public void write(FileSystemPolicy policy, StringBuffer sb) {
        sb.append(" ");
        sb.append(XML_MEDIUM_ARCHIVEPATH);
        sb.append("=");
        sb.append(AbstractXMLWriter.encode(policy.getBaseArchivePath()));   
    }

}
