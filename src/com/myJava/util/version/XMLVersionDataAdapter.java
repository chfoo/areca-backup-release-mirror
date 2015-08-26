package com.myJava.util.version;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
public class XMLVersionDataAdapter implements VersionDataAdapter {
	private static final String XML_DATA = "versiondata";
	private static final String XML_ID = "id";
	private static final String XML_DESCRIPTION = "description";
	private static final String XML_DATE = "date";
	private static final String XML_URL = "url";
	private static final String XML_ADDITIONAL_NOTES = "additional notes";
	private static final String XML_IMPLEMENTATION_NOTES = "implementation notes";

	public static final String ENCODING = "UTF-8";

	public VersionData read(InputStream in) throws VersionDataAdapterException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document xml = builder.parse(in);
			return readData(xml);
		} catch (Exception e) {
			throw new VersionDataAdapterException(e);
		}
	}

	public VersionData readData(Document xml) throws MalformedURLException, AdapterException {
		Element root = xml.getDocumentElement();

		if (! root.getNodeName().equalsIgnoreCase(XML_DATA)) {
			throw new AdapterException("Illegal XML content : missing '" + XML_DATA + "' tag.");
		} 

		VersionData data = new VersionData();

		data.setVersionId(XMLTool.readNullableNode(root, XML_ID));
		data.setVersionDate(CalendarUtils.resolveDate(XMLTool.readNonNullableNode(root, XML_DATE), null));
		data.setDescription(XMLTool.readNullableNode(root, XML_DESCRIPTION));
		data.setDownloadUrl(new URL(XMLTool.readNonNullableNode(root, XML_URL)));

		data.setImplementationNodes(XMLTool.readNullableNode(root, XML_IMPLEMENTATION_NOTES));
		data.setAdditionalNotes(XMLTool.readNullableNode(root, XML_ADDITIONAL_NOTES));
		
		return data;
	}
}
