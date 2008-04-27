package com.application.areca.metadata;

import com.myJava.util.Util;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 5323430991191230653
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
public class MetadataEncoder implements MetadataConstants {
    
    public static String encode(String in) {
    	String out = in;
    	out = Util.replace(out, "@", SC_AROBASE);
    	out = Util.replace(out, ";", SC_SEMICOLON);
    	return out;
    }
    
    public static String decode(String in) {
    	String out = in;
    	out = Util.replace(out, SC_AROBASE, "@>");
    	out = Util.replace(out, SC_SEMICOLON, ";");
    	out = Util.replace(out, "@>", "@");
    	return out;
    }
    
    public static void main(String[] a) {
    	String[] in = new String[] {
    			"test",
    			"test@cjc",
    			"test;gh",
    			"test@@vcdsvc",
    			"ghj;;cdc",
    			"gcjhsd@;cdsc",
    			"gcjhsd@;cdsc@Pxsq",
    			"@cdsc;@@;cds;cds@d@s;@@sqs;;"
    	};
    	
    	for (int i=0; i<in.length; i++) {
    		String encoded = encode(in[i]);
    		String decoded = decode(encoded);
    		if (! decoded.equals(in[i])) {
    			System.out.println("ERROR !");
    		}
    		
    		System.out.println("Orig=" + in[i] + ", Encoded=" + encoded + ", Decoded=" + decoded);
    	}
    }
}
