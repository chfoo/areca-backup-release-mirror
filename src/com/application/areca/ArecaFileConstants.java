package com.application.areca;

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
public interface ArecaFileConstants {
	public static final String USER_ROOT_DIRECTORY = ".areca";
	public static final String USER_PREFERENCES_PATH = USER_ROOT_DIRECTORY + "/preferences.properties";
	public static final String USER_PREFERENCES_PATH_DEPRECATED = ".areca";
	public static final String USER_DEFAULT_WORKSPACE = USER_ROOT_DIRECTORY + "/workspace";
	public static final String DEPRECATED_LOG_SUBDIRECTORY_NAME = "log";
	public static final String LOG_SUBDIRECTORY_NAME = ".log";
	public static final String DEFAULT_PLUGIN_SUBDIRECTORY_NAME = "plugins";
	public static final String DEFAULT_MODULE_SUBDIRECTORY_NAME = "modules";
	public static final String DEFAULT_BIN_SUBDIRECTORY_NAME = "bin";
	public static final String DEFAULT_TRANSLATION_SUBDIRECTORY_NAME = "translations";
	
	/**
	 * Trace filename
	 */
	public static final String TRACE_FILE = "trace";

	/**
	 * Content filename
	 */
	public static final String CONTENT_FILE = "content";

	/**
	 * Hash filename
	 */
	public static final String HASH_FILE = "hash";

	/**
	 * Temporary merge location
	 */
	public static final String TMP_MERGE_LOCATION = "merge";

	/**
	 * Temporary directory used during archive check
	 */
	public static final String CHECK_DESTINATION = "chk";
	
	/**
	 * History file
	 */
	public static final String HISTORY_NAME = "history";
	
	/**
	 * Commit file
	 */
	public static final String COMMIT_MARKER_NAME = ArecaConfiguration.get().getCommitFileName();
	
	/**
	 * Sequence files (for delta storage)
	 */
	public static final String SEQUENCE_FILE = "sequence";
	
	/**
	 * Transaction points
	 */
	public static final String TRANSACTION_FILE = "transaction";
	
	/**
	 * Transaction header
	 */
	public static final String TRANSACTION_HEADER_FILE = "header";
	
	/**
	 * Name of subdirectory in transaction directory that is used to store temporary files
	 */
	public static final String TEMPORARY_DIR_NAME = "tmp";
}
