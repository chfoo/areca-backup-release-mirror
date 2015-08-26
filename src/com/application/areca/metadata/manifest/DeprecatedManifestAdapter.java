package com.application.areca.metadata.manifest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.util.CalendarUtils;
import com.myJava.util.Util;
import com.myJava.util.log.Logger;
import com.myJava.util.xml.AdapterException;

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
public class DeprecatedManifestAdapter implements ManifestReader {
    private static final String SEPARATOR = ";"; 
    private static final String HEADER = "|MFST_HDR|";
    private static String ENCODING = "UTF-8";
    
	public Manifest read(File file) throws AdapterException {
        try {
            if (FileSystemManager.exists(file)) {
                InputStream is = new GZIPInputStream(FileSystemManager.getFileInputStream(file));
                String content = FileTool.getInstance().getInputStreamContent(is, ENCODING, true);
                return decode(content);
            } else {
                return null;
            }
        } catch (IOException e) {
            Logger.defaultLogger().error(e);
            throw new AdapterException(e);
        }
	}

	private Manifest decode(String source) {
		long version = 0;
		String content = source;

		int index = source.indexOf(HEADER);
		if (index != -1) {
			String versionStr = source.substring(0, index);
			version = Long.parseLong(versionStr);
			content = source.substring(index + HEADER.length());
		}

		if (version == 0) {
			return decodeVersion0(content);
		} else if (version == 1) {
			return decodeVersion1(content);
		} else if (version == 2) {
			return decodeVersion2(content);           
		} else {
			throw new IllegalArgumentException("Incompatible manifest version : " + version);
		}
	}

	private static Manifest decodeVersion2(String source) {
		source = Util.replace(source, SEPARATOR, " " + SEPARATOR + " ");
		String[] tokens = source.split(SEPARATOR);

		Manifest m = new Manifest(Integer.parseInt(tokens[3].trim()));

		m.setDate(CalendarUtils.resolveDate(decodeField(tokens[0].trim()), null));
		m.setDescription(decodeField(tokens[1].trim()));
		m.setTitle(decodeField(tokens[2].trim()));

		for (int i=4; i<tokens.length; i+=2) {
			m.addProperty(decodeField(tokens[i].trim()), decodeField(tokens[i+1].trim()));
		}

		return m;
	}

	private static Manifest decodeVersion1(String source) {
		source = Util.replace(source, SEPARATOR, " " + SEPARATOR + " ");
		String[] tokens = source.split(SEPARATOR);

		Manifest m = new Manifest(Integer.parseInt(tokens[4].trim()));

		decodeField(tokens[0].trim()); // old field : author
		m.setDate(CalendarUtils.resolveDate(decodeField(tokens[1].trim()), null));
		m.setDescription(decodeField(tokens[2].trim()));
		m.setTitle(decodeField(tokens[3].trim()));

		for (int i=5; i<tokens.length; i+=2) {
			m.addProperty(decodeField(tokens[i].trim()), decodeField(tokens[i+1].trim()));
		}

		return m;
	}

	private static Manifest decodeVersion0(String source) {
		source = Util.replace(source, SEPARATOR, " " + SEPARATOR + " ");
		String[] tokens = source.split(SEPARATOR);

		Manifest m = new Manifest(Manifest.TYPE_BACKUP);

		decodeField(tokens[0].trim()); // old field : author
		m.setDate(CalendarUtils.resolveDate(decodeField(tokens[1].trim()), null));
		m.setDescription(decodeField(tokens[2].trim()));

		if (tokens.length >= 4) {
			m.setTitle(decodeField(tokens[3].trim()));

			for (int i=4; i<tokens.length; i+=2) {
				m.addProperty(decodeField(tokens[i].trim()), decodeField(tokens[i+1].trim()));
			}
		}

		return m;
	}

	private static String decodeField(String source) {
		if (source.equals("null")) {
			return null;
		}

		String ret = Util.replace(source, "\\\\", "\\");
		ret = Util.replace(ret, "\\+", SEPARATOR);
		return ret;        
	}
}
