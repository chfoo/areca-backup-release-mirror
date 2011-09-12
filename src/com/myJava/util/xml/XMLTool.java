package com.myJava.util.xml;

import org.w3c.dom.Node;

import com.myJava.util.Util;

/**
 * 
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2011, Olivier PETRUCCI.

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
public class XMLTool {
    public static String encode(long orig) {
    	return encode("" + orig);
    }
    
    public static String encode(int orig) {
    	return encode("" + orig);
    }
    
    public static String encode(boolean orig) {
    	return encode("" + orig);
    }

    public static String encode(String orig) {
        String ret = orig;

        ret = Util.replace(ret, "&", "&amp;");
        ret = Util.replace(ret, "\n", "&#xA;");
        ret = Util.replace(ret, "<", "&lt;");
        ret = Util.replace(ret, ">", "&gt;");  
        ret = Util.replace(ret, "\"", "&quot;");
        ret = Util.replace(ret, "'", "&apos;");            
        
        return "\"" + ret + "\"";
    }
    
    public static String getHeader(String encoding) {
        return "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>";
    }
    
    public static String encodeProperty(String property, int value) {
    	return encodeProperty(property, "" + value);
    }
    
    public static String encodeProperty(String property, boolean value) {
    	return encodeProperty(property, "" + value);
    }
    
    public static String encodeProperty(String property, long value) {
    	return encodeProperty(property, "" + value);
    }
    
    public static String encodeProperty(String property, String value) {
		StringBuffer sb = new StringBuffer();
		if (value != null) {
			sb.append(" ").append(property).append("=").append(encode(value));
		}
		return sb.toString();
    }
    
    public static String readNonNullableNode(Node data, String tag) throws AdapterException {
        Node node = data.getAttributes().getNamedItem(tag);
        if (node == null) {
        	throw new AdapterException("Invalid XML content : missing '" + tag + "' tag.");
        } else {
        	return node.getNodeValue();
        }
    }
    
    public static String readNullableNode(Node data, String tag) throws AdapterException {
        Node node = data.getAttributes().getNamedItem(tag);
        if (node == null) {
        	return null;
        } else {
        	return node.getNodeValue();
        }
    }
}
