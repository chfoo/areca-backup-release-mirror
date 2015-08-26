package com.application.areca.filter;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import com.application.areca.Utils;
import com.myJava.file.FileSystemManager;
import com.myJava.object.Duplicable;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;

/**
 * Check the file's extension
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
public class FileExtensionArchiveFilter extends AbstractArchiveFilter {
	private static final long serialVersionUID = -4526135026464366338L;
	
	private static final String SEPARATOR = ", ";
    protected ArrayList extensions = new ArrayList();

	public void addExtension(String ext) {
        this.extensions.add(ext);
    }
    
    public String getStringParameters() {
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<extensions.size(); i++) {
            if (i != 0) {
                sb.append(SEPARATOR);
            }
            sb.append((String)extensions.get(i));
        }
        
        return sb.toString();
    }
    
    private boolean checkFormat(String parameters) {
        StringTokenizer stt = new StringTokenizer(parameters, ",");
        while (stt.hasMoreTokens()) {
            String ext = stt.nextToken().trim();
            if (ext.startsWith("*")) {
            	ext = ext.substring(1);
            }
            
            if (ext.length() == 0) {
                return false;
            }                                               
        }   
        return true;
    }
    
    public void acceptParameters(String parameters) {
        if (Utils.isEmpty(parameters) || (! this.checkFormat(parameters))) {
            throw new IllegalArgumentException("Invalid parameters : " + parameters);
        }
        
        StringTokenizer stt = new StringTokenizer(parameters, SEPARATOR);
        this.extensions.clear();
        while (stt.hasMoreTokens()) {
        	String ext = stt.nextToken().trim();
            if (ext.startsWith("*")) {
            	ext = ext.substring(1);
            }
            this.addExtension(ext);
        }
    }

    public Duplicable duplicate() {
        FileExtensionArchiveFilter clone = new FileExtensionArchiveFilter();
        clone.logicalNot = this.logicalNot;
        clone.extensions = (ArrayList)this.extensions.clone();
        return clone;
    }
    
    public Iterator getExtensionIterator() {
        return this.extensions.iterator();
    }
    
    public short acceptIteration(File entry, File data) {
        return WILL_MATCH_PERHAPS;
    }
    
    public boolean acceptElement(File entry, File data) {
        if (entry == null) {
            return false;
        } else {
            if (FileSystemManager.isDirectory(entry)) {
                return true;
            } else {
		        Iterator iter = this.extensions.iterator();
		        while (iter.hasNext()) {
		            if (checkExtension(FileSystemManager.getName(entry), (String)iter.next())) {
		                return ! logicalNot;
		            }
		        }
		        return logicalNot;
            }
        }
    }
    
    private static boolean checkExtension(String name, String extension) {
        return (name != null && extension != null && name.toLowerCase().endsWith(extension.toLowerCase()));
    }
    
    public boolean equals(Object obj) {
        if (obj == null || (! (obj instanceof FileExtensionArchiveFilter)) ) {
            return false;
        } else {
            FileExtensionArchiveFilter other = (FileExtensionArchiveFilter)obj;
            return 
            	EqualsHelper.equals(this.logicalNot, other.logicalNot)
            	&& EqualsHelper.equals(this.extensions, other.extensions)
           	;
        }
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, this.extensions);
        h = HashHelper.hash(h, this.logicalNot);
        return h;
    }
}
