package com.application.areca;

import com.application.areca.metadata.manifest.Manifest;


/**
 * Donnees pour une version historisee d'une RecoveryEntry
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2736893395693886205
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
public class EntryArchiveData {
	// status par defaut
	public static final short STATUS_UNKNOWN = -1;
	
	// status historiques, affichables a l'utilisateur
	public static final short STATUS_MISSING = 0;
	public static final short STATUS_CREATED = 1;
	public static final short STATUS_FIRST_BACKUP = 2;
	public static final short STATUS_MODIFIED = 3;
	public static final short STATUS_DELETED = 4;

	// status binaires : existe / n'existe pas (archives non incrementales)
	public static final short STATUS_NOT_STORED = 6;
	public static final short STATUS_STORED = 7;

	protected short status;    
    protected Manifest manifest;
    protected String hash;

    public Manifest getManifest() {
        return manifest;
    }
    
    public void setManifest(Manifest manifest) {
        this.manifest = manifest;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String trace) {
        this.hash = trace;
    }
	
	public EntryArchiveData() {
		this.status = STATUS_UNKNOWN;
	}
	
	public short getStatus() {
		return status;
	}
	public void setStatus(short status) {
		this.status = status;
	}
}
