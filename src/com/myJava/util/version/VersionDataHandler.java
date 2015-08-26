package com.myJava.util.version;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Reads a VersionData from an URL.
 * <BR>The content is expected to have the following format :
 * <BR>- VersionID (eg: 3.1.6)
 * <BR>- VersionDate : AAAA-M-D (eg: 2006-7-15)
 * <BR>- DownloadUrl (eg: http://download.myapp.net) 
 * <BR>- Description (A single line of text) 
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
public class VersionDataHandler {

	private URL checkUrl;
	private VersionDataAdapter adapter;

	public URL getCheckUrl() {
		return checkUrl;
	}
	public void setCheckUrl(URL checkUrl) {
		this.checkUrl = checkUrl;
	}
	public VersionDataAdapter getAdapter() {
		return adapter;
	}
	public void setAdapter(VersionDataAdapter adapter) {
		this.adapter = adapter;
	}
	
	public VersionData readVersionData() throws VersionDataAdapterException {
		InputStream in = null;

		try {
			in = checkUrl.openStream();
			return adapter.read(in);
		} catch (IOException e) {
			throw new VersionDataAdapterException(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ignored) {
					// Ignored
				}
			}
		}
	}

	public static void main(String[] args) {
		try {
			VersionDataHandler handler = new VersionDataHandler();
			handler.setCheckUrl(new URL("http://www.areca-backup.org/version_xml.php?currentVersion=7.1"));
			handler.setAdapter(new XMLVersionDataAdapter());

			System.out.println(handler.readVersionData());
			
			
			handler = new VersionDataHandler();
			handler.setCheckUrl(new URL("http://areca.sourceforge.net/version.php"));
			handler.setAdapter(new DeprecatedVersionDataAdapter());

			System.out.println(handler.readVersionData());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
