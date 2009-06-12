package com.application.areca.adapters;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.application.areca.TargetGroup;
import com.myJava.file.FileSystemManager;

/**
 * Adapter for process serialization / deserialization
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
public class ProcessXMLReader implements XMLTags {
    protected Document xmlConfig;
    protected File configurationFile;
    protected MissingDataListener missingDataListener = null;
	protected boolean readIDInfosOnly = false;
    
    public ProcessXMLReader(File configurationFile) throws AdapterException {
        try {
            this.configurationFile = configurationFile;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            xmlConfig = builder.parse(configurationFile);
        } catch (Exception e) {
            AdapterException ex = new AdapterException(e);
            setSource(ex);
            throw ex;
        }
    }

    public void setMissingDataListener(MissingDataListener missingDataListener) {
        this.missingDataListener = missingDataListener;
    }
    
	public void setReadIDInfosOnly(boolean readIDInfosOnly) {
		this.readIDInfosOnly = readIDInfosOnly;
	}
    
    public TargetGroup load() throws AdapterException {
        try {
            Element root = this.xmlConfig.getDocumentElement();
            
            if (! root.getNodeName().equalsIgnoreCase(XML_PROCESS)) {
                throw new AdapterException("Group not found : your configuration file must have a group root : '" + XML_PROCESS + "'.");
            }
            
            TargetGroup process = new TargetGroup(this.configurationFile);        
            
            Node commentsNode = root.getAttributes().getNamedItem(XML_PROCESS_DESCRIPTION);
            if (commentsNode != null) {
                process.setComments(commentsNode.getNodeValue());
            }     
            
            Node versionNode = root.getAttributes().getNamedItem(XML_VERSION);
            int version = 1;
            if (versionNode != null) {
                version = Integer.parseInt(versionNode.getNodeValue());
            }  
            if (version > ProcessXMLWriter.CURRENT_VERSION) {
            	throw new AdapterException("Invalid XML version : This version of Areca can't handle XML versions above " + ProcessXMLWriter.CURRENT_VERSION + ". You are trying to read a version " + version);
            }
            
            NodeList targets = root.getElementsByTagName(XML_TARGET);
            for (int i=0; i<targets.getLength(); i++) {
                TargetXMLReader targetAdapter = new TargetXMLReader(targets.item(i), process, version);
                targetAdapter.setReadIDInfosOnly(this.readIDInfosOnly);
                targetAdapter.setMissingDataListener(missingDataListener);
                process.addTarget(targetAdapter.readTarget());
            }
            
            return process;
        } catch (AdapterException e) {
            setSource(e);
            throw e;            
        } catch (Exception e) {
            // On convertit toutes les exceptions en AdapterException
            AdapterException ex = new AdapterException(e);
            setSource(ex);
            throw ex;
        }
    }
    
    private void setSource(AdapterException e) {
        e.setSource(FileSystemManager.getAbsolutePath(configurationFile));
    }
}