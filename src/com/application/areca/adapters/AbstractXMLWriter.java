package com.application.areca.adapters;

import com.myJava.util.Utilitaire;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -4899974077672581254
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
public abstract class AbstractXMLWriter implements XMLTags {
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"" + getEncoding() + "\"?>";
    protected StringBuffer sb;
    
    public AbstractXMLWriter(StringBuffer sb) {
        this.sb = sb;
    }

    protected static String encode(String orig) {
        String ret = orig;

        ret = Utilitaire.replace(ret, "&", "&amp;");
        ret = Utilitaire.replace(ret, "\n", "&#xA;");
        ret = Utilitaire.replace(ret, "<", "&lt;");
        ret = Utilitaire.replace(ret, ">", "&gt;");  
        ret = Utilitaire.replace(ret, "\"", "&quot;");
        ret = Utilitaire.replace(ret, "'", "&apos;");            
        
        return "\"" + ret + "\"";
    }
    
    public void writeHeader() {
        sb.append(XML_HEADER);
    }
    
    public String getXML() {
        return sb.toString();
    }
    
    protected static String getEncoding() {
        //return OSTool.getIANAFileEncoding();
        return "UTF-8";
    }
}
