package com.application.areca.metadata;

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
public interface MetadataConstants {
    public static final String DEPRECATED_SEP = "#-#";
    public static final String DEPRECATED_DIRECTORY_MARKER = "!D!";
    public static final String DEPRECATED_SYMLINK_MARKER = "!S!";
    public static final char DEPRECATED_INTERNAL_SEP = '-';
    
    public static final String SEPARATOR = ";";
    public static final String SC_SEMICOLON = "@P";
    public static final char T_DIR = 'd';
    public static final char T_FILE = 'f';
    public static final char T_SYMLINK = 's';
    public static final char T_PIPE = 'p';
}
