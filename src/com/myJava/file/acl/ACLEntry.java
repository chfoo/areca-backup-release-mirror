package com.myJava.file.acl;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 8785459451506899793
 */
 
 /*
 Copyright 2005-2007, Olivier PETRUCCI.
 
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
public class ACLEntry {
	public static final String SCOPE_USER = "user";
	public static final String SCOPE_GROUP = "group";
	public static final String SCOPE_OTHER = "others";
	public static final String SCOPE_MASK = "mask";
	
	/**
	 * ACLEntry.SCOPE_USER, ACLEntry.SCOPE_GROUP or ACLEntry.SCOPE_OTHER
	 */
	private String scope;
	
	/**
	 * Name of the user/group; optional
	 */
	private String name;
	
	/**
	 * read
	 */
	private boolean read;
	
	/**
	 * write
	 */
	private boolean write;
	
	/**
	 * execute
	 */
	private boolean execute;
	
	/**
	 * Default attribute - directories only
	 */
	private boolean def;

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean isWrite() {
		return write;
	}

	public void setWrite(boolean write) {
		this.write = write;
	}

	public boolean isExecute() {
		return execute;
	}

	public void setExecute(boolean execute) {
		this.execute = execute;
	}

	public boolean isDef() {
		return def;
	}

	public void setDef(boolean def) {
		this.def = def;
	}
}
