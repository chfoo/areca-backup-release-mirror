package com.application.areca.metadata.manifest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.myJava.util.CalendarUtils;
import com.myJava.util.Util;
import com.myJava.util.collections.CollectionTools;

/**
 * Classe définissant un manifeste d'archive
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 3675112183502703626
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
public class Manifest {
    
    public static final long CURRENT_VERSION = 1;
    
    private static final String SEPARATOR = ";"; 
    private static final String HEADER = "|MFST_HDR|";
    
    public static final int TYPE_BACKUP = 0;
    public static final int TYPE_MERGE = 1;   
    
    private String author;
    private GregorianCalendar date;
    private String description;
    private String title;    
    private Map properties;
    private int type;
    
    public Manifest(int type) {
        this.date = new GregorianCalendar();
        this.properties = new HashMap();
        this.type = type;
    }
    
    private Manifest() {
        this(TYPE_BACKUP);
    }
    
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public GregorianCalendar getDate() {
        return date;
    }
    public void setDate(GregorianCalendar date) {
        this.date = date;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void addProperty(String name, String value) {
        this.properties.put(name, value);
    }
    
    public void addProperty(String name, boolean value) {
        addProperty(name, "" + value);
    }
    
    public void addProperty(String name, long value) {
        addProperty(name, "" + value);
    }

    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public String getStringProperty(String name) {
        return getStringProperty(name, null);
    }
    public String getStringProperty(String name, String defaultValue) {        
        String value = (String)this.properties.get(name);
        return value == null ? defaultValue : value;
    }
    
    public int getIntProperty(String name) {
        return Integer.parseInt((String)this.properties.get(name));
    }
    
    public boolean getBooleanProperty(String name) {
        return Boolean.valueOf((String)this.properties.get(name)).booleanValue();
    }
    
    public Iterator propertyIterator() {
        ArrayList list = new ArrayList(this.properties.size());
        list.addAll(properties.keySet());
        Object[] objs = list.toArray();
        Arrays.sort(objs);
        List ret = CollectionTools.toList(objs);
        return ret.iterator();
    }
    
    public static final long getCurrentVersion() {
        return CURRENT_VERSION;
    }
    
    public String encode() {
        StringBuffer sb = new StringBuffer();
        sb.append(getCurrentVersion());
        sb.append(HEADER);
        sb.append(encodeField(author));
        sb.append(SEPARATOR);
        sb.append(encodeField(CalendarUtils.getFullDateToString(date)));
        sb.append(SEPARATOR);
        sb.append(encodeField(description));
        sb.append(SEPARATOR);
        sb.append(encodeField(title));
        sb.append(SEPARATOR);
        sb.append(this.type);
        
        Iterator iter = this.properties.keySet().iterator();
        while(iter.hasNext()) {
            String key = (String)iter.next();
            
            sb.append(SEPARATOR);            
            sb.append(encodeField(key));
            sb.append(SEPARATOR);            
            sb.append(encodeField(getStringProperty(key)));            
        }

        return sb.toString();
    }
    
    public static Manifest decode(String source) {
        
        long version = 0;
        String content = source;
        
        int index = source.indexOf(HEADER);
        if (index != -1) {
            String versionStr = source.substring(0, index);
            version = Long.parseLong(versionStr);
            content = source.substring(index + HEADER.length());
        }
        
       if (version == 0) {
           return decodeVersion0(content);
       } else if (version == 1) {
           return decodeVersion1(content);
       } else {
           throw new IllegalArgumentException("Incompatible manifest version : " + version);
       }
    }
    
    private static Manifest decodeVersion1(String source) {
        source = Util.replace(source, SEPARATOR, " " + SEPARATOR + " ");
        String[] tokens = source.split(SEPARATOR);
        
        Manifest m = new Manifest();
        
        m.author = decodeField(tokens[0].trim());
        m.date = CalendarUtils.resolveDate(decodeField(tokens[1].trim()), null);
        m.description = decodeField(tokens[2].trim());
        m.title = decodeField(tokens[3].trim());
        m.type = Integer.parseInt(tokens[4].trim());
            
        for (int i=5; i<tokens.length; i+=2) {
            m.addProperty(decodeField(tokens[i].trim()), decodeField(tokens[i+1].trim()));
        }
        
        return m;
    }
    
    private static Manifest decodeVersion0(String source) {
        source = Util.replace(source, SEPARATOR, " " + SEPARATOR + " ");
        String[] tokens = source.split(SEPARATOR);
        
        Manifest m = new Manifest(TYPE_BACKUP);
        
        m.author = decodeField(tokens[0].trim());
        m.date = CalendarUtils.resolveDate(decodeField(tokens[1].trim()), null);
        m.description = decodeField(tokens[2].trim());
        
        if (tokens.length >= 4) {
            m.title = decodeField(tokens[3].trim());
            
            for (int i=4; i<tokens.length; i+=2) {
                m.addProperty(decodeField(tokens[i].trim()), decodeField(tokens[i+1].trim()));
            }
        }
        
        return m;
    }

    private static String encodeField(String source) {
        if (source == null) {
            return "";
        }
        
        String ret = Util.replace(source, "\\", "\\\\");
        ret = Util.replace(ret, SEPARATOR, "\\+");
        return ret;
    }
    
    private static String decodeField(String source) {
        if (source.equals("null")) {
            return null;
        }
        
        String ret = Util.replace(source, "\\\\", "\\");
        ret = Util.replace(ret, "\\+", SEPARATOR);
        return ret;        
    }
    
    public String toString() {
        if (this.title != null) {
            return this.title;
        } else {
            return this.author;
        }
    }
}
