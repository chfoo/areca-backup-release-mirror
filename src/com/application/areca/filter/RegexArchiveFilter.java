package com.application.areca.filter;

import java.io.File;
import java.util.regex.Pattern;

import com.myJava.object.Duplicable;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.system.OSTool;

/**
 * Checks that the entry matches the regex passed as argument
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
public class RegexArchiveFilter extends AbstractArchiveFilter {
	private static final long serialVersionUID = 1090696259398514977L;
	
	public static String SCHEME_FULLPATH = "full_path";
    public static String SCHEME_NAME = "file_name";
    public static String SCHEME_PARENTDIR = "parent_directory";
	
	/**
	 * Uncompiled pattern
	 */
    private String regex;
    
    /**
     * Compiled pattern
     */
    private Pattern pattern;
    
    /**
     * Apply pattern to filename only
     */
    private String scheme = SCHEME_FULLPATH;
    
    /**
     * true : match the regex
     * <BR>false : find the pattern defined by the regex
     */
    private boolean match = false;

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getRegex() {
		return regex;
	}

	public void acceptParameters(String parameters) {
		setRegex(parameters);
    }
    
    public short acceptIteration(File entry, File data) {
    	if (scheme.equals(SCHEME_PARENTDIR)) {
    		// Check that the directory itself is accepted by the filter
			if (acceptElement(entry, data)) {
				return WILL_MATCH_TRUE;
			} else {
				return WILL_MATCH_FALSE;
			}
    	} else {
    		return WILL_MATCH_PERHAPS;
    	}
    }

    public boolean isMatch() {
		return match;
	}

	public void setMatch(boolean match) {
		this.match = match;
	}

	public boolean acceptElement(File entry, File data) {
        if (entry == null) {
            return false;
        } else {
        	String toMatch;
        	if (scheme.equals(SCHEME_FULLPATH)) {
        		toMatch = entry.getAbsolutePath();
        	} else if (scheme.equals(SCHEME_NAME)) {
        		toMatch = entry.getName();
        	} else {
        		toMatch = entry.getParent();
        	}
        	return acceptStorage(toMatch);
        }
    }
	
	private boolean acceptStorage(String toMatch) {
    	boolean found = match ? pattern.matcher(toMatch).matches() : pattern.matcher(toMatch).find();
    	
    	// Backward compatibility ... toMatch should be produced by Filesystemmanager.getAbsolutePath()
    	if (OSTool.isSystemWindows() && ! found) {
    		toMatch = toMatch.replace('\\', '/');
    		found = match ? pattern.matcher(toMatch).matches() : pattern.matcher(toMatch).find();
    	}

        if (found) {
        	return !logicalNot;
        } else {
        	return logicalNot;
        }
	}
    
    public Duplicable duplicate() {
        RegexArchiveFilter filter = new RegexArchiveFilter();
        filter.logicalNot = this.logicalNot;
        filter.setRegex(this.regex);
        filter.setScheme(this.scheme);
        filter.setMatch(this.match);
        return filter;
    }

	public String getStringParameters() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
        this.pattern = Pattern.compile(regex);
	}
	
    public boolean equals(Object obj) {
        if (obj == null || (! (obj instanceof RegexArchiveFilter)) ) {
            return false;
        } else {
            RegexArchiveFilter other = (RegexArchiveFilter)obj;
            return 
            	EqualsHelper.equals(this.logicalNot, other.logicalNot)
            	&& EqualsHelper.equals(this.regex, other.regex)
            	&& EqualsHelper.equals(this.match, other.match)     
            	&& EqualsHelper.equals(this.scheme, other.scheme)              	      	
           	;
        }
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, this.regex);
        h = HashHelper.hash(h, this.scheme);
        h = HashHelper.hash(h, this.match);
        h = HashHelper.hash(h, this.logicalNot);
        return h;
    }
}
