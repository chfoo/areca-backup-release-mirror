package com.application.areca.metadata.manifest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.application.areca.version.VersionInfos;
import com.myJava.file.FileSystemManager;
import com.myJava.util.CalendarUtils;
import com.myJava.util.xml.AdapterException;
import com.myJava.util.xml.XMLTool;

/**
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
public class XMLManifestAdapter implements ManifestReader {
	public static final int CURRENT_VERSION = 1;

	private static final String XML_VERSION = "version";
	private static final String XML_MANIFEST = "manifest";
	private static final String XML_TITLE = "title";
	private static final String XML_DESCRIPTION = "description";
	private static final String XML_DATE = "date";
	private static final String XML_TYPE = "type";
	private static final String XML_PROPERTIES = "properties";
	private static final String XML_PROPERTY = "property";
	private static final String XML_KEY = "key";
	private static final String XML_VALUE = "value";

	public static final String ENCODING = "UTF-8";
	
	private boolean compress = true;
	
	public XMLManifestAdapter(boolean compress) {
		this.compress = compress;
	}

	public Manifest read(File file) throws AdapterException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputStream stream;
			if (compress) {
				stream = new GZIPInputStream(FileSystemManager.getFileInputStream(file));
			} else {
				stream = FileSystemManager.getFileInputStream(file);
			}
			Document xml = builder.parse(stream);
			return readManifest(xml);
		} catch (ZipException e) {
			AdapterException ex = new AdapterException(e);
			ex.setSource(FileSystemManager.getAbsolutePath(file));
			ex.setPotentialEncryptionIssue(true);
			throw ex;
		} catch (Exception e) {
			AdapterException ex = new AdapterException(e);
			ex.setSource(FileSystemManager.getAbsolutePath(file));
			throw ex;
		}
	}

	public Manifest readManifest(Document xml) throws AdapterException {
		Element root = xml.getDocumentElement();

		if (! root.getNodeName().equalsIgnoreCase(XML_MANIFEST)) {
			throw new AdapterException("Illegal XML content : missing '" + XML_MANIFEST + "' tag.");
		} 

		Node versionNode = root.getAttributes().getNamedItem(XML_VERSION);
		int version = 1;
		if (versionNode != null) {
			version = Integer.parseInt(versionNode.getNodeValue());
		}  
		if (version > CURRENT_VERSION) {
			throw new AdapterException("Invalid manifest XML version : This version of " + VersionInfos.APP_SHORT_NAME + " can't handle XML versions above " + CURRENT_VERSION + ". You are trying to read a version " + version);
		}

		int type = Integer.parseInt(XMLTool.readNonNullableNode(root, XML_TYPE));
		Manifest manifest = new Manifest(type);

		GregorianCalendar cal = CalendarUtils.resolveDate(XMLTool.readNonNullableNode(root, XML_DATE), null);
		manifest.setDate(cal);

		manifest.setDescription(XMLTool.readNullableNode(root, XML_DESCRIPTION));
		manifest.setTitle(XMLTool.readNullableNode(root, XML_TITLE));

		NodeList children = root.getChildNodes();
		for (int n=0; n<children.getLength(); n++) {
			if (children.item(n).getNodeName().equals(XML_PROPERTIES)) {
				NodeList propertyNodes = children.item(n).getChildNodes();

				for (int i=0; i<propertyNodes.getLength(); i++) {
					Node propertyNode = propertyNodes.item(i);
					if (propertyNode.getNodeName().equals(XML_PROPERTY)) {
						String key = XMLTool.readNonNullableNode(propertyNode, XML_KEY);
						String value = XMLTool.readNullableNode(propertyNode, XML_VALUE);
						manifest.addProperty(key, value);
					}
				}
			}
		}

		return manifest;
	}

	public void write(Manifest manifest, File file) throws AdapterException {
		OutputStreamWriter writer = null;
		try {
			if (compress) {
				writer = new OutputStreamWriter(new GZIPOutputStream(FileSystemManager.getFileOutputStream(file)), ENCODING);
			} else {
				writer = new OutputStreamWriter(FileSystemManager.getFileOutputStream(file), ENCODING);
			}
			writer.write(XMLTool.getHeader(ENCODING));

			writer.write("\n<");
			writer.write(XML_MANIFEST);

			writer.write(XMLTool.encodeProperty(XML_VERSION, CURRENT_VERSION));
			writer.write(XMLTool.encodeProperty(XML_TYPE, manifest.getType()));
			writer.write(XMLTool.encodeProperty(XML_TITLE, manifest.getTitle()));
			writer.write(XMLTool.encodeProperty(XML_DESCRIPTION, manifest.getDescription()));
			writer.write(XMLTool.encodeProperty(XML_DATE, CalendarUtils.getFullDateToString(manifest.getDate())));

			writer.write(">");

			writer.write("\n<");
			writer.write(XML_PROPERTIES);
			writer.write(">");
			Iterator iter = manifest.propertyIterator();
			while (iter.hasNext()) {
				String prop = (String)iter.next();
				String val = manifest.getStringProperty(prop);

				writer.write("\n<");
				writer.write(XML_PROPERTY);
				writer.write(XMLTool.encodeProperty(XML_KEY, prop));
				writer.write(XMLTool.encodeProperty(XML_VALUE, val));
				writer.write(" />");
			}
			writer.write("\n</");
			writer.write(XML_PROPERTIES);
			writer.write(">");

			writer.write("\n</");
			writer.write(XML_MANIFEST);
			writer.write(">");
		} catch (Exception e) {
			throw new AdapterException(e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					throw new AdapterException(e);
				}
			}
		}
	}
}
