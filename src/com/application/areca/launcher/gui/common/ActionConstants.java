package com.application.areca.launcher.gui.common;

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
public interface ActionConstants {
    // Menu commands
	public static final String CMD_OPEN = "open";
	public static final String CMD_PREFERENCES = "preferences";
	public static final String CMD_BACKUP_WORKSPACE = "backup workspace";    
    public static final String CMD_IMPORT_CONF = "import configuration";    
    public static final String CMD_EXIT = "exit";
    public static final String CMD_SIMULATE = "simulate";    
    public static final String CMD_BACKUP = "backup";
    public static final String CMD_BACKUP_ALL = "backup all";    
    public static final String CMD_RECOVER = "recover";
    public static final String CMD_RECOVER_ENTRY_HISTO = "recover entry";
    public static final String CMD_VIEW_FILE_AS_TEXT_HISTO = "view file as text (histo)";
    public static final String CMD_VIEW_FILE_HISTO = "view file (histo)";
    public static final String CMD_VIEW_FILE_AS_TEXT = "view file as text";
    public static final String CMD_VIEW_FILE = "view file";
    public static final String CMD_COPY_FILENAMES = "copy filenames";
    public static final String CMD_RECOVER_WITH_FILTER = "recover with filter";
    public static final String CMD_RECOVER_WITH_FILTER_LATEST = "recover latest version with filter";
    public static final String CMD_MERGE = "merge";
    public static final String CMD_CHECK_ARCHIVES = "check archives";
    public static final String CMD_DELETE_ARCHIVES = "delete archives";
    public static final String CMD_ABOUT = "about";
    public static final String CMD_SUPPORT = "support";
    public static final String CMD_PLUGINS = "plugins";
    public static final String CMD_CHECK_VERSION = "check version";
    public static final String CMD_HELP = "help";    
    public static final String CMD_TUTORIAL = "tutorial";  
    public static final String CMD_HISTORY = "history";
    public static final String CMD_VIEW_MANIFEST = "view manifest"; 
    public static final String CMD_VIEW_CONTENT = "view content";
    public static final String CMD_INDICATORS = "compute indicators";
    public static final String CMD_CANCEL_CURRENT_PROCESS = "cancel current process";
    public static final String CMD_SEARCH = "search";
    public static final String CMD_BUILD_BATCH = "build batch";
    public static final String CMD_BUILD_STRATEGY = "build strategy";

    public static final String CMD_NEW_TARGET = "new target";
    public static final String CMD_EDIT_TARGET = "edit target";    
    public static final String CMD_DEL_TARGET = "del target";
    public static final String CMD_DUPLICATE_TARGET = "duplicate target";
    public static final String CMD_EDIT_XML = "edit xml";
    
    public static final String CMD_NEW_GROUP = "new group";
    public static final String CMD_DEL_GROUP = "del group";    
    
    public static final String CMD_SEARCH_PHYSICAL = "show physical";    
    public static final String CMD_SEARCH_LOGICAL = "show logical";    
    
    // Generic actions
    public static final String ACTION_SAVE = "save";
    public static final String ACTION_TEST = "test";
    public static final String ACTION_SEARCH = "search";
    public static final String ACTION_CANCEL = "cancel";
    public static final String ACTION_CLOSE = "close";    
    public static final String ACTION_CLEAR_HISTORY = "clear history";
    public static final String ACTION_CLEAR_LOG = "clear log";
    
    public static final String CANCEL_ACTION_KEY = "CANCEL_ACTION_KEY";
}
