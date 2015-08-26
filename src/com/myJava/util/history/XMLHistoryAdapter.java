package com.myJava.util.history;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.GregorianCalendar;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
public class XMLHistoryAdapter implements HistoryReader {
	public static final int CURRENT_VERSION = 1;

	private static final String XML_VERSION = "version";
	private static final String XML_HISTORY = "history";
	private static final String XML_ENTRY = "entry";
	private static final String XML_DATE = "date";
	private static final String XML_DESCRIPTION = "description";
	private static final String XML_TYPE = "type";

	public static final String ENCODING = "UTF-8";

	public History read(File file) throws AdapterException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document xml = builder.parse(new GZIPInputStream(FileSystemManager.getFileInputStream(file)));
			return readHistory(xml);
		} catch (Exception e) {
			AdapterException ex = new AdapterException(e);
			ex.setSource(FileSystemManager.getAbsolutePath(file));
			throw ex;
		}
	}
	
	public History readHistory(Document xml) throws AdapterException {
		History hist = new History();
        Element root = xml.getDocumentElement();
        
        if (! root.getNodeName().equalsIgnoreCase(XML_HISTORY)) {
            throw new AdapterException("Illegal XML content : missing '" + XML_HISTORY + "' tag.");
        } 
        
        Node versionNode = root.getAttributes().getNamedItem(XML_VERSION);
        int version = 1;
        if (versionNode != null) {
            version = Integer.parseInt(versionNode.getNodeValue());
        }  
        if (version > CURRENT_VERSION) {
        	throw new AdapterException("Invalid history XML version : XML versions above " + CURRENT_VERSION + " are not supported. You are trying to read a version " + version);
        }
        
        NodeList entries = root.getElementsByTagName(XML_ENTRY);
        for (int i=0; i<entries.getLength(); i++) {
        	hist.addEntry(readEntry(entries.item(i)));
        }
        return hist;
	}
	
	public HistoryEntry readEntry(Node entryData) throws AdapterException {
		HistoryEntry entry = new HistoryEntry();
		entry.setType(Integer.parseInt(XMLTool.readNonNullableNode(entryData, XML_TYPE)));
		entry.setDescription(XMLTool.readNullableNode(entryData, XML_DESCRIPTION));
		
		GregorianCalendar cal = CalendarUtils.resolveDate(XMLTool.readNonNullableNode(entryData, XML_DATE), null);
		entry.setDate(cal);

		return entry;
	}

	public void write(History history, File file) throws AdapterException {
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(new GZIPOutputStream(FileSystemManager.getFileOutputStream(file)), ENCODING);
			writer.write(XMLTool.getHeader(ENCODING));

			writer.write("\n<");
			writer.write(XML_HISTORY);
			writer.write(XMLTool.encodeProperty(XML_VERSION, CURRENT_VERSION));
			writer.write(">");

			GregorianCalendar[] keys = history.getKeys(false);
			for (int i=0; i<keys.length; i++) {
				HistoryEntry entry = history.getEntry(keys[i]);
				serializeEntry(entry, writer);
			}

			writer.write("\n</");
			writer.write(XML_HISTORY);
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

	private void serializeEntry(HistoryEntry entry, Writer writer) throws IOException {
		writer.write("\n<");
		writer.write(XML_ENTRY);
		writer.write(XMLTool.encodeProperty(XML_DATE, CalendarUtils.getFullDateToString(entry.getDate())));
		writer.write(XMLTool.encodeProperty(XML_TYPE, entry.type));
		writer.write(XMLTool.encodeProperty(XML_DESCRIPTION, entry.description));
		writer.write("/>");
	}
}
