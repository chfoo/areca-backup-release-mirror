package com.application.areca.adapters.read;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.application.areca.AbstractTarget;
import com.application.areca.ConfigurationSource;
import com.application.areca.TargetGroup;
import com.application.areca.adapters.MissingDataListener;
import com.application.areca.adapters.XMLTags;
import com.application.areca.adapters.write.XMLVersions;
import com.application.areca.version.VersionInfos;
import com.myJava.file.FileSystemManager;
import com.myJava.util.log.Logger;
import com.myJava.util.xml.AdapterException;

/**
 * Adapter for target group serialization / deserialization (old - deprecated - format)
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
public class DeprecatedTargetGroupXMLReader 
implements XMLTags, TargetGroupXMLReader {
	
    protected Document xmlConfig;
    protected File configurationFile;
    protected MissingDataListener missingDataListener = null;
	protected boolean readIDInfosOnly = false;
	protected boolean installMedium = true;
    
    public DeprecatedTargetGroupXMLReader(File configurationFile, boolean installMedium) throws AdapterException {
        try {
    		this.installMedium = installMedium;
            this.configurationFile = configurationFile;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            xmlConfig = builder.parse(configurationFile);
        } catch (Throwable e) {
        	Logger.defaultLogger().error("Invalid configuration file: " + configurationFile, e);
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
            
            if (! root.getNodeName().equalsIgnoreCase(XML_GROUP)) {
                throw new AdapterException("Group not found : your configuration file must have a group root : '" + XML_GROUP + "'.");
            }
            
            TargetGroup group = new TargetGroup(computeName(this.configurationFile));        

            Node versionNode = root.getAttributes().getNamedItem(XML_VERSION);
            int version = 1;
            if (versionNode != null) {
                version = Integer.parseInt(versionNode.getNodeValue());
            }  
            if (version > XMLVersions.CURRENT_VERSION) {
            	throw new AdapterException("Invalid XML version : This version of " + VersionInfos.APP_SHORT_NAME + " can't handle XML versions above " + XMLVersions.CURRENT_VERSION + ". You are trying to read a version " + version);
            }
            
            NodeList targets = root.getElementsByTagName(XML_TARGET);
            for (int i=0; i<targets.getLength(); i++) {
                TargetXMLReader targetAdapter = new TargetXMLReader(targets.item(i), installMedium);
                targetAdapter.setVersion(version);
                targetAdapter.setReadIDInfosOnly(this.readIDInfosOnly);
                targetAdapter.setMissingDataListener(missingDataListener);
                targetAdapter.setSource(new ConfigurationSource(true, configurationFile));
                
                AbstractTarget target = targetAdapter.readTarget();
                group.linkChild(target);
            }
            
            group.setLoadedFrom(new ConfigurationSource(true, configurationFile));
            return group;
        } catch (AdapterException e) {
            setSource(e);
            throw e;            
        } catch (Exception e) {
            AdapterException ex = new AdapterException(e);
            setSource(ex);
            throw ex;
        }
    }
    
    private void setSource(AdapterException e) {
        e.setSource(FileSystemManager.getAbsolutePath(configurationFile));
    }
    
	public boolean readable() throws AdapterException {
        try {
            Element root = xmlConfig.getDocumentElement();
            
            if (root.getNodeName().equalsIgnoreCase(XMLTags.XML_GROUP)) {
            	return true;
            } else {
            	return false;
            }
        } catch (Exception e) {
        	throw new AdapterException(e);
        }
	}
    
    private String computeName(File source) {
        String fileName = FileSystemManager.getName(source);
        int index = fileName.indexOf('.');
        if (index != -1) {
        	fileName = fileName.substring(0, index);
        }
        String firstLetter = (""+fileName.charAt(0)).toUpperCase();
        
        return (firstLetter + fileName.substring(1)).replace('_', ' ');
    }
}