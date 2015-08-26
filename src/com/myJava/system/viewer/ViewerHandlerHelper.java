package com.myJava.system.viewer;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.util.log.Logger;

/**
 * 
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
public class ViewerHandlerHelper {
	private static ViewerHandler VIEWER_HANDLER;

	static {
		// Viewer handler
		String configuredViewerHandler = FrameworkConfiguration.getInstance().getViewerHandlerImpl();
		try {
			ViewerHandler handler = (ViewerHandler)Class.forName(configuredViewerHandler).newInstance();
			if (! handler.test()) {
				throw new UnsupportedOperationException("Unsupported implementation");
			}
			VIEWER_HANDLER = handler;
		} catch (Throwable e) {
			Logger.defaultLogger().warn(configuredViewerHandler + " is not supported by your system (" + e.getClass().getName() + " : " + e.getMessage() + "). Switching to default implementation.");
			VIEWER_HANDLER = new DefaultViewerHandler();
		}
		Logger.defaultLogger().info("Using " + VIEWER_HANDLER.getClass().getName() + " as viewer handler.");
	}
	
    public static ViewerHandler getViewerHandler() {
    	return VIEWER_HANDLER;
    }
}
