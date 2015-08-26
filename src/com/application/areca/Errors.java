package com.application.areca;


/**
 * List errors thrown during the pre-check process.
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
public interface Errors {
    public static final int ERR_C_BASEARCHIVEPATH = 1;
    public static final int ERR_C_BASETARGETPATH = 2;
    public static final int ERR_C_EXIST = 3;    
    public static final int ERR_C_MEDIUM_ENCRYPTION_NOT_INITIALIZED = 4;
    public static final int ERR_C_INCLUSION= 5;
    
    public static final String ERR_M_BASEARCHIVEPATH = "The storage directory does not exist.";
    public static final String ERR_M_BASETARGETPATH = "The source directory does not exist.";
    public static final String ERR_M_EXIST = "A backup has already been made at this date.";
    public static final String ERR_M_MEDIUM_ENCRYPTION_NOT_INITIALIZED = "FileSystem driver not initialized.";
    public static final String ERR_M_INCLUSION = "The backup directory is a sub directory of the source directory.\nThis must be corrected : please move the backup directory to another location.\n(It can not be a subdirectory of the source directory)";
    
    public static final String ERR_ALL_OK = "Process successfully completed.";
}
