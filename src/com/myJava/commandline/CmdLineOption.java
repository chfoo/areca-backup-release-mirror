package com.myJava.commandline;

/**
 * <BR>
 * @author Ludovic QUESNELLE
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
public abstract class CmdLineOption {

	final public static String UNSET   = "unset";
	final public static String BOOLEAN = "boolean";
	final public static String STRING  = "string";
	final public static String ARRAY   = "array";
	
	private boolean isMandatory_ =false;
	private boolean hasBeenSet_  =false;
	private String  type_ = UNSET; 
	private String  name_ = null;
	private String  comment_ = null;
	
	public CmdLineOption(boolean mandatory,String name,String type,String comment){
		type_ = type;
		isMandatory_ = mandatory;
		name_ = name;
		comment_ = comment;
	}

	public boolean isMandatory() {
		return isMandatory_;
	}
		
	public abstract Object getValue();
	
	public String getComment() {
		return comment_;
	}
	
	public String getName() {
		return name_;
	}
	
	public boolean hasBeenSet(){
		return hasBeenSet_;
	}
	
	public void setHasBeenSet(){
		hasBeenSet_ = true;
	}
	
	public String getType() {
		return type_;
	}
}


