package com.application.areca.filter;

import java.io.File;
import java.util.regex.Pattern;

import com.application.areca.Utils;
import com.myJava.object.Duplicable;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;

/**
 * Checks that the entry matches the regex passed as argument
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 231019873304483154
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
public class RegexArchiveFilter extends AbstractArchiveFilter {
    
	/**
	 * Uncompiled pattern
	 */
    private String regex;
    
    /**
     * Compiled pattern
     */
    private Pattern pattern;

	public void acceptParameters(String parameters) {
        if (Utils.isEmpty(parameters)) {
            throw new IllegalArgumentException("Invalid parameters : " + parameters);
        }
        setRegex(parameters);
    }
    
    public boolean acceptIteration(File entry) {
        return true;
    }
    
    public boolean acceptStorage(File entry) {
        if (entry == null) {
            return false;
        } else {
            if (pattern.matcher(entry.getName()).find()) {
            	return !exclude;
            } else {
            	return exclude;
            }
        }
    }
    
    public Duplicable duplicate() {
        RegexArchiveFilter filter = new RegexArchiveFilter();
        filter.exclude = this.exclude;
        filter.setRegex(this.regex);
        return filter;
    }

	public String getStringParameters() {
		return regex;
	}

	private void setRegex(String regex) {
		this.regex = regex;
        this.pattern = Pattern.compile(regex);
	}
	
    public boolean equals(Object obj) {
        if (obj == null || (! (obj instanceof RegexArchiveFilter)) ) {
            return false;
        } else {
            RegexArchiveFilter other = (RegexArchiveFilter)obj;
            return 
            	EqualsHelper.equals(this.exclude, other.exclude)
            	&& EqualsHelper.equals(this.regex, other.regex)
           	;
        }
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, this.regex);
        h = HashHelper.hash(h, this.exclude);
        return h;
    }
}
