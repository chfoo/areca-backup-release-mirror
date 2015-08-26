package com.application.areca.metadata.manifest;

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
public interface ManifestKeys {

    public static final String VERSION = "Version";
    public static final String VERSION_DATE = "Version date";
    public static final String BUILD_ID = "Build id";
    public static final String FILTERED_ENTRIES = "Filtered entries";
    public static final String BACKUP_DURATION = "Backup duration";
    public static final String TARGET_ID = "Target ID";
    public static final String CHECKED = "Checked";
    public static final String MERGE_START = "Merge start date";
    public static final String MERGE_END = "Merge end date";
    public static final String MERGED_ARCHIVES = "Merged archives";
    public static final String ARCHIVE_NAME = "Archive name";
    public static final String CURRENT_ARCHIVE_PATH = "Archive path";
    public static final String STORED_FILES = "Stored files";
    public static final String UNMODIFIED_FILES = "Unmodified files (not stored)";
    public static final String ARCHIVE_SIZE = "Archive size";
    public static final String UNFILTERED_DIRECTORIES = "Unfiltered directories";
    public static final String UNFILTERED_FILES = "Unfiltered files";
    public static final String SCANNED_ENTRIES = "Scanned entries (files or directories)";
    public static final String SOURCE_PATH = "Source path";
    public static final String ENCODING = "File encoding";
    public static final String OS_NAME = "Operating system";
    public static final String IS_RESUMED = "Resumed";
    public static final String JRE = "JRE";
    public static final String ARECA_HOME = "Areca Home";
    
    public static final String OPTION_PREFIX = "Option [";
    public static final String OPTION_SUFFIX = "]";
    public static final String OPTION_KEEP_DELETED_ENTRIES = OPTION_PREFIX + "preserve deleted entries" + OPTION_SUFFIX;
    public static final String OPTION_BACKUP_SCHEME = OPTION_PREFIX + "backup scheme" + OPTION_SUFFIX;
}
