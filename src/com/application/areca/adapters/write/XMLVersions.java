package com.application.areca.adapters.write;

/**
 * XML version history
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
public interface XMLVersions {
	//public static final int CURRENT_VERSION = 2;
	//public static final int CURRENT_VERSION = 3; // introduced in v6.1
	//public static final int CURRENT_VERSION = 4; // introduced in v7.1 : Special files filters replace symbolic links filters 
	//public static final int CURRENT_VERSION = 5; // introduced in v7.1.3 : follow_subdirectories replaced by follow_subdirs, and fix of serialization bug
	//public static final int CURRENT_VERSION = 6; // introduced in v7.1.4 : filter parameterization change : "exclude" replaced by "logical_not" ... easier to understand
	public static final int CURRENT_VERSION = 7; // introduced in v7.1.5 : post processor parameterization (success / error / warning)
	
}
