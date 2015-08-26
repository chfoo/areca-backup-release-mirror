package com.myJava.system.viewer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.system.NoBrowserFoundException;
import com.myJava.system.OSTool;
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
public class DefaultViewerHandler 
implements ViewerHandler {
    private static final String[] BROWSERS = FrameworkConfiguration.getInstance().getOSBrowsers();
    private static String APPLE_FILE_MGR = "com.apple.eio.FileManager";
    
    public boolean test() {
    	return true;
    }
    
	public void browse(URL urlObj) throws IOException, NoBrowserFoundException {
    	String url = urlObj.toExternalForm();
        if (OSTool.isSystemMACOS()) {
            try {
				Class fileMgr = Class.forName(APPLE_FILE_MGR);
				Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] {String.class});
				openURL.invoke(null, new Object[] {urlObj});
			} catch (Exception e) {
				Logger.defaultLogger().error(e);
				throw new NoBrowserFoundException(e);
			}
        } else if (OSTool.isSystemWindows()) {
            // Workaround : there is a bug in Win2K and certain WinXP releases which prevents the help url to be loaded properly.
            if (url.startsWith("file:/") && url.charAt(6) != '/') {
                url = "file:///" + url.substring(6);
                url = URLDecoder.decode(url);
            }
            OSTool.execute(new String[] {"rundll32", "url.dll,FileProtocolHandler", url}, true);
        } else {
            String browser = null;
            for (int count = 0; count < BROWSERS.length && browser == null; count++) {
                if (OSTool.execute(new String[] {"which", BROWSERS[count]}) == 0) {
                    browser = BROWSERS[count];
                }
            }
            
            if (browser != null) {
                // Browser found --> Go !
            	OSTool.execute(new String[] {browser, url}, true);
            } else {
                throw new NoBrowserFoundException("No browser cound be found.");
            }
        }
	}

	public boolean isBrowseSupported() {
		return true;
	}

	public boolean isOpenSupported() {
		return false;
	}

	public void open(File file) throws IOException {
		throw new UnsupportedOperationException("The 'open' method is not supported by this implementation.");
	}
}
