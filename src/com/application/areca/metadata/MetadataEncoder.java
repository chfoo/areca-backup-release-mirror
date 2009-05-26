package com.application.areca.metadata;

import com.myJava.util.Util;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

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
public class MetadataEncoder implements MetadataConstants {
    
    public static String encode(String in) {
    	String out = in;
    	out = Util.replace(out, SPEC_CHAR, SC_AROBASE);
    	out = Util.replace(out, SEPARATOR, SC_SEMICOLON);
    	out = Util.replace(out, "\r", SC_R);
    	out = Util.replace(out, "\n", SC_N);
    	return out;
    }
    
    public static String decode(String in) {
    	String out = in;
    	out = Util.replace(out, SC_AROBASE, SC_RESERVED);
    	out = Util.replace(out, SC_SEMICOLON, SEPARATOR);
    	out = Util.replace(out, SC_R, "\r");
    	out = Util.replace(out, SC_N, "\n");
    	out = Util.replace(out, SC_RESERVED, SPEC_CHAR);
    	return out;
    }
}
