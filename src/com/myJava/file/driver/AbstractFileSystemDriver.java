package com.myJava.file.driver;

import java.io.File;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.FileNameUtil;
import com.myJava.file.InvalidPathException;
import com.myJava.system.OSTool;

/**
 * Abstract implementation for filesystem driver
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
public abstract class AbstractFileSystemDriver 
implements FileSystemDriver {
    private static long MAX_FILEPATH = FrameworkConfiguration.getInstance().getMaxFilePath();
    private static int FORCE_MAX_FILEPATH_CHECK = FrameworkConfiguration.getInstance().getForceMaxFilePathCheck();    
    public static boolean CHECK_PATH = shallCheckPath();

    private static boolean shallCheckPath() {
    	if (MAX_FILEPATH <= 0 || FORCE_MAX_FILEPATH_CHECK == 0) {
    		return false;
    	} else if (FORCE_MAX_FILEPATH_CHECK == 1) {
    		return true;
    	} else {
    		return OSTool.isSystemWindows() && ! (OSTool.isJavaVersionGreaterThanOrEquals(new int[] {1, 6}));
    	}
    }
    
    protected void checkFilePath(File f) throws InvalidPathException {
        if (CHECK_PATH) {
            String p = getAbsolutePath(f);
            if (p != null && p.length() > MAX_FILEPATH) {
                throw new InvalidPathException("File path (" + p + ") exceeds maximum length (" + MAX_FILEPATH + "). You should upgrade to Java 1.6 or higher.");
            }
        }
    }

    public static String normalizeIfNeeded(String path) {
        if (File.separatorChar == '\\') {
            return FileNameUtil.normalizePath(path);
        } else {
            return path;
        }
    }
}