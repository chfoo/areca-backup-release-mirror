package com.application.areca.metadata.transaction;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
public class TransactionHeaderAdapter {
	public static final int CURRENT_VERSION = 1;

	private static final String XML_VERSION = "version";
	private static final String XML_TH = "transaction_point";
	private static final String XML_DATE = "date";
	private static final String XML_ARECA_VERSION = "areca_version";
	private static final String XML_SOURCES_ROOT = "sources_root";
	private static final String XML_BACKUP_SCHEME = "backup_scheme";
	
	public static final String ENCODING = "UTF-8";

	public TransactionPointHeader read(File file) throws AdapterException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document xml = builder.parse(FileSystemManager.getFileInputStream(file));
			return readTransactionPointHeader(xml);
		} catch (Exception e) {
			AdapterException ex = new AdapterException(e);
			ex.setSource(FileSystemManager.getAbsolutePath(file));
			throw ex;
		}
	}

	private TransactionPointHeader readTransactionPointHeader(Document xml) throws AdapterException {
		Element root = xml.getDocumentElement();

		if (! root.getNodeName().equalsIgnoreCase(XML_TH)) {
			throw new AdapterException("Illegal XML content : missing '" + XML_TH + "' tag.");
		} 

		Node versionNode = root.getAttributes().getNamedItem(XML_VERSION);
		int version = 1;
		if (versionNode != null) {
			version = Integer.parseInt(versionNode.getNodeValue());
		}  
		if (version > CURRENT_VERSION) {
			throw new AdapterException("Invalid transaction header XML version : This version of " + VersionInfos.APP_SHORT_NAME + " can't handle XML versions above " + CURRENT_VERSION + ". You are trying to read a version " + version);
		}

		TransactionPointHeader header = new TransactionPointHeader();

		GregorianCalendar cal = CalendarUtils.resolveDate(XMLTool.readNonNullableNode(root, XML_DATE), null);
		header.setDate(cal);
		header.setArecaVersion(XMLTool.readNonNullableNode(root, XML_ARECA_VERSION));
		header.setSourcesRoot(XMLTool.readNonNullableNode(root, XML_SOURCES_ROOT));
		header.setBackupScheme(XMLTool.readNonNullableNode(root, XML_BACKUP_SCHEME));
		
		return header;
	}

	public void write(TransactionPointHeader header, File file) throws IOException {
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(FileSystemManager.getFileOutputStream(file), ENCODING);
			writer.write(XMLTool.getHeader(ENCODING));

			writer.write("\n<");
			writer.write(XML_TH);
			writer.write(XMLTool.encodeProperty(XML_VERSION, CURRENT_VERSION));
			writer.write(XMLTool.encodeProperty(XML_DATE, CalendarUtils.getFullDateToString(header.getDate())));
			writer.write(XMLTool.encodeProperty(XML_ARECA_VERSION, header.getArecaVersion()));
			writer.write(XMLTool.encodeProperty(XML_SOURCES_ROOT, header.getSourcesRoot()));
			writer.write(XMLTool.encodeProperty(XML_BACKUP_SCHEME, header.getBackupScheme()));
			writer.write("/>");
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
}
