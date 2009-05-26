package com.application.areca.adapters;

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
public abstract class AbstractXMLWriter implements XMLTags {
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"" + getEncoding() + "\"?>";
    protected StringBuffer sb;
    
    public AbstractXMLWriter(StringBuffer sb) {
        this.sb = sb;
    }
    
    protected static String encode(long orig) {
    	return encode("" + orig);
    }
    
    protected static String encode(int orig) {
    	return encode("" + orig);
    }
    
    protected static String encode(boolean orig) {
    	return encode("" + orig);
    }

    protected static String encode(String orig) {
        String ret = orig;

        ret = Util.replace(ret, "&", "&amp;");
        ret = Util.replace(ret, "\n", "&#xA;");
        ret = Util.replace(ret, "<", "&lt;");
        ret = Util.replace(ret, ">", "&gt;");  
        ret = Util.replace(ret, "\"", "&quot;");
        ret = Util.replace(ret, "'", "&apos;");            
        
        return "\"" + ret + "\"";
    }
    
    public void writeHeader() {
        sb.append(XML_HEADER);
    }
    
    public String getXML() {
        return sb.toString();
    }
    
    protected static String getEncoding() {
        return "UTF-8";
    }
}
