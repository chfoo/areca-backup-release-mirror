package com.application.areca.filter;

import com.application.areca.RecoveryEntry;
import com.myJava.object.PublicClonable;

/**
 * Classe générique définissant un filtre d'entrée à backuper.
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
public interface ArchiveFilter 
extends PublicClonable {
	
    /**
     * Indique si l'entrée doit être acceptée ou refusée
     * @param entry
     * @return
     */
    public boolean acceptIteration(RecoveryEntry entry);
    
	/**
	 * Indique si l'entrée doit être acceptée ou refusée
	 * @param entry
	 * @return
	 */
    public boolean acceptStorage(RecoveryEntry entry);
    
    /**
     * Indique si le filtre fonctionne en inclusion ou exclusion
     * @return
     */
    public boolean isExclude();
    
    /**
     * Indique si le filtre fonctionne en inclusion ou exclusion
     * @param exclude
     */
    public void setExclude(boolean exclude);
    
    /**
     * Parses the string provided as argument and inits the filter
     */
    public void acceptParameters(String parameters);
    
    /**
     * Returns the filter's parameters as a String
     */
    public String getStringParameters();
    
    /**
     * Tells wether the filter needs to be parametrized or not 
     */
    public boolean requiresParameters();
}