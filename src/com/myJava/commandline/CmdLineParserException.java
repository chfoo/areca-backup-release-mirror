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
public class CmdLineParserException extends Exception {

	final public static String UNKNOWN_OPTION ="Unknown option";
	final public static String TYPE_MISMATCH = "Type mismatch";
	final public static String MISSING_MANDATORY_OPTION = "Missing mandatory argument";
	
	final private static long serialVersionUID = 1L;
	
	private String optionName_=null; 
	private String type_=null;
	
	public String getOptionName() {
		return optionName_;
	}
	
	public String getType() {
		return type_;
	}
	
	public CmdLineParserException() {
		super();
	}

	public CmdLineParserException(String type) {
		super(type);
		type_ = type;
	}

	public CmdLineParserException(String type,String optionName){
		super(type + " : -" + optionName);
		optionName_ = optionName;
		type_ = type;
	}
	
	public CmdLineParserException(Throwable arg0) {
		super(arg0);
	}

	public CmdLineParserException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
	
	public String toString() {
		StringBuffer buf=new StringBuffer();
		buf.append("CmdLineParserException : ");
		buf.append(type_);
		buf.append(" for parameter : ");
		buf.append(optionName_);
		return buf.toString();
	}
}
