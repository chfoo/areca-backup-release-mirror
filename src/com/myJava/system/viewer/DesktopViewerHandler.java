package com.myJava.system.viewer;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.myJava.system.NoBrowserFoundException;
import com.myJava.util.log.Logger;

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
public class DesktopViewerHandler 
implements ViewerHandler {

    public boolean test() {
    	return Desktop.isDesktopSupported() && isBrowseSupported();
    }
	
	public void browse(URL url) throws IOException, NoBrowserFoundException {
		try {
			URI uri = new URI(url.toExternalForm());
			Desktop.getDesktop().browse(uri);
		} catch (URISyntaxException e) {
			Logger.defaultLogger().error(e);
			throw new IOException(e);
		}
	}

	public boolean isBrowseSupported() {
		return Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
	}

	public boolean isOpenSupported() {
		return Desktop.getDesktop().isSupported(Desktop.Action.OPEN);
	}

	public void open(File file) throws IOException {
		Desktop.getDesktop().open(file);
	}
}
