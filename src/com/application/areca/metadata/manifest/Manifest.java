package com.application.areca.metadata.manifest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.myJava.object.Duplicable;
import com.myJava.object.ToStringHelper;
import com.myJava.util.CalendarUtils;
import com.myJava.util.collections.CollectionTools;

/**
 * Archive's manifest implementation
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
public class Manifest
implements Duplicable, Serializable {
	private static final long serialVersionUID = -8883407978478338006L;
	
	public static final int TYPE_BACKUP = 0;
    public static final int TYPE_MERGE = 1;   
    
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
    
    public long getLongProperty(String name, long defaultValue) {
        String value = (String)this.properties.get(name);
        return value == null ? defaultValue : Long.parseLong(value);
    }
    
    public int getIntProperty(String name, int defaultValue) {
        String value = (String)this.properties.get(name);
        return value == null ? defaultValue : Integer.parseInt(value);
    }
    
    public int getIntProperty(String name) {
        return getIntProperty(name, 0);
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

	public Duplicable duplicate() {
		Manifest clone = new Manifest();
		clone.setDate((GregorianCalendar)date.clone());
		clone.setDescription(description);
		clone.setTitle(title);
		clone.setType(type);
		
		// Properties are not cloned
		return clone;
	}
	
	public String toString() {
		StringBuffer sb = ToStringHelper.init(this);
		ToStringHelper.append("Title", this.title, sb);
		ToStringHelper.append("Date", CalendarUtils.getFullDateToString(this.date), sb);
		ToStringHelper.append("Type", this.type, sb);
		Iterator iter = this.propertyIterator();
		while (iter.hasNext()) {
			String prop = (String)iter.next();
			String value = this.getStringProperty(prop);
			ToStringHelper.append(prop, value, sb);
		}
		
		return sb.toString();
	}
}
