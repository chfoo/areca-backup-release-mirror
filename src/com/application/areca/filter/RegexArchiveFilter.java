package com.application.areca.filter;

import java.util.regex.Pattern;

import com.application.areca.RecoveryEntry;
import com.application.areca.Utils;
import com.application.areca.impl.FileSystemRecoveryEntry;
import com.myJava.util.EqualsHelper;
import com.myJava.util.HashHelper;
import com.myJava.util.PublicClonable;

/**
 * Vérifie si l'entrée est validée par l'expression régulière.
 * <BR>La condition s'applique sur le chemin complet du fichier à tester, hors la racine
 * du répertoire en cours de backup.
 * <BR>Par ailleurs, les répertoires ne sont pas filtrés.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4438212685798161280
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
public class RegexArchiveFilter extends AbstractArchiveFilter {
    
	/**
	 * Forme non compilée du pattern
	 */
    private String regex;
    
    /**
     * Forme compilée du pattern à matcher
     */
    private Pattern pattern;
    
    public RegexArchiveFilter() {
    }

    public void acceptParameters(String parameters) {
        if (Utils.isEmpty(parameters)) {
            throw new IllegalArgumentException("Invalid parameters : " + parameters);
        }
        setRegex(parameters);
    }
    
    public boolean acceptIteration(RecoveryEntry entry) {
        return true;
    }
    
    public boolean acceptStorage(RecoveryEntry entry) {
        FileSystemRecoveryEntry fEntry = (FileSystemRecoveryEntry)entry;        
        if (fEntry == null) {
            return false;
        } else {
            if (pattern.matcher(fEntry.getName()).find()) {
            	return !exclude;
            } else {
            	return exclude;
            }
        }
    }
    
    public PublicClonable duplicate() {
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
