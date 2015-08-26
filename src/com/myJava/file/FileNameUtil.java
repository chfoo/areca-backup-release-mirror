package com.myJava.file;

import com.myJava.util.Util;

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
public class FileNameUtil {

    /**
     * Replaces all separators by "/"
     * <BR>Adds a trailing file separator 
     */
    public static String normalizePath(String p) {
        return Util.replace(p, "\\", "/");
    }
    
    public static String normalizeSlashes(String p, boolean addTrailingSeparator) {
        p = Util.replace(p, "//", "/");
        p = Util.replace(p, "//", "/");

        if (addTrailingSeparator && (! p.endsWith("/"))) {
            p += "/";
        }
        
        return p;
    }
    
    public static boolean startsWithSeparator(String str) {
        return str != null && (str.startsWith("/") || str.startsWith("\\"));
    }
    
    public static boolean endsWithSeparator(String str) {
        return str != null && (str.endsWith("/") || str.endsWith("\\"));
    }
}
