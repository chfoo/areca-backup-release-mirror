package com.application.areca.launcher.tui;


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
public interface CommandConstants {  
    public static UserCommand COMMAND_BACKUP = new UserCommand("backup");
    public static UserCommand COMMAND_RECOVER = new UserCommand("recover");
    public static UserCommand COMMAND_MERGE = new UserCommand("merge");
    public static UserCommand COMMAND_DESCRIBE = new UserCommand("describe");
    public static UserCommand COMMAND_INFOS = new UserCommand("infos");
    public static UserCommand COMMAND_DELETE = new UserCommand("delete");  
    public static UserCommand COMMAND_CHECK = new UserCommand("check");    
    
    public static UserOption OPTION_CONFIG = new UserOption("-config", 2);
    public static UserOption OPTION_DESTINATION = new UserOption("-destination", 2);
    public static UserOption OPTION_TARGET = new UserOption("-target", 2);
    public static UserOption OPTION_DELAY = new UserOption("-delay", 2);
    public static UserOption OPTION_FROM = new UserOption("-from", 2);
    public static UserOption OPTION_TO = new UserOption("-to", 2);
    public static UserOption OPTION_DATE = new UserOption("-date", 2);    
    public static UserOption OPTION_CHECK_FILES = new UserOption("-c", 1);  
    public static UserOption OPTION_OVERWRITE = new UserOption("-o", 1);  
    public static UserOption OPTION_NO_SUBDIR = new UserOption("-nosubdir", 1); 
    public static UserOption OPTION_SPEC_LOCATION = new UserOption("-wdir", 2);  
    public static UserOption OPTION_SYNC = new UserOption("-s", 1);  
    public static UserOption OPTION_RESUME = new UserOption("-resume", 1); 
    public static UserOption OPTION_RESUME_CONDITIONAL = new UserOption("-cresume", 2); 
    public static UserOption OPTION_CHECK_ALL = new UserOption("-a", 1); 
    public static UserOption OPTION_FULL_BACKUP = new UserOption("-f", 1);
    public static UserOption OPTION_KEEP_DELETED_ENTRIES = new UserOption("-k", 1);
    public static UserOption OPTION_DIFFERENTIAL_BACKUP = new UserOption("-d", 1);
    public static UserOption OPTION_TITLE = new UserOption("-title", 2);
}
