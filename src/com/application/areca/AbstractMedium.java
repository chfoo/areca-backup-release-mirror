package com.application.areca;

import java.util.ArrayList;
import java.util.List;

import com.application.areca.impl.FileSystemTarget;
import com.application.areca.metadata.trace.ArchiveTraceParser;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.util.history.HistoryHandler;
import com.myJava.util.log.Logger;

/**
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
public abstract class AbstractMedium implements ArchiveMedium {

    protected FileSystemTarget target;
    protected boolean installed = false;
    protected HistoryHandler historyHandler;     
    
    /**
     * Nom du fichier d'historique
     */
    protected static final String HISTORY_NAME = "history";
    
    public void install() throws ApplicationException {
        Logger.defaultLogger().info("Installing medium : " + this.toString());
        this.installed = true;
    }
    
    protected void ensureInstalled() throws ApplicationException {
    	if (! installed) {
    		install();
    	}
    }
    
    protected void copyAttributes(Object clone) {
    }
    
    public AbstractTarget getTarget() {
        return this.target;
    }
    
    public String getHistoryName() {
        return HISTORY_NAME;
    } 
    
    public boolean isInstalled() {
    	return installed;
    }
    
    /**
     * Construit une liste de EAD ordonnee et dont les status sont a jour
     * a partir de la liste d'entree data.
     * 
     * @param data
     * @return
     */
    protected EntryArchiveData[] processEntryArchiveData(EntryArchiveData[] data) {
    	List list = new ArrayList();
    	
        String prevHash;
        String hash = null;
    	for (int i=0; i<data.length; i++) {
            EntryArchiveData e = data[i];
            prevHash = hash;
            hash = e.getHash();
    		
            if (hash == null) {
                if (prevHash != null) {
                    e.setStatus(EntryStatus.STATUS_DELETED);
                    list.add(e);
                }
            } else if (ArchiveTraceParser.hasBeenModified(hash, prevHash)) {
                //if (status != EntryArchiveData.STATUS_STORED) {
                //    e.setStatus(EntryArchiveData.STATUS_MISSING);
                //} else 
            	if (i == 0) {
                    e.setStatus(EntryStatus.STATUS_FIRST_BACKUP);
                } else if (prevHash == null) {
                    e.setStatus(EntryStatus.STATUS_CREATED);
                } else {
                    e.setStatus(EntryStatus.STATUS_MODIFIED);
                }
                list.add(e);
            }
    	}
    	
    	return (EntryArchiveData[])list.toArray(new EntryArchiveData[0]);
    }

	public boolean equals(Object arg0) {
		if (arg0 == null) {
			return false;
		} else if (! (arg0 instanceof AbstractMedium)) {
			return false;
		} else {
			AbstractMedium other = (AbstractMedium)arg0;
			return (
				EqualsHelper.equals(other.getTarget(), this.getTarget())
			);
		}
	}

	public int hashCode() {
		int result = HashHelper.initHash(this);
		result = HashHelper.hash(result, this.getTarget());
		return result;
	}
}
