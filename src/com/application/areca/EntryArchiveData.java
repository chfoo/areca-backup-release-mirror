package com.application.areca;

import com.application.areca.metadata.manifest.Manifest;

/**
 * Données pour une version historisée d'une RecoveryEntry
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -3366468978279844961
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
	// status par défaut
	public static final short STATUS_UNKNOWN = -1;
	
	// status historiques, affichables à l'utilisateur
	public static final short STATUS_MISSING = 0;
	public static final short STATUS_CREATED = 1;
	public static final short STATUS_FIRST_BACKUP = 2;
	public static final short STATUS_MODIFIED = 3;
	public static final short STATUS_DELETED = 4;

	
	// status binaires : existe / n'existe pas (archives non incrémentales)
	public static final short STATUS_NONEXISTANT = 5;
	public static final short STATUS_EXISTANT = 6;

	// status binaires : modifié / non modifié (archives incrémentales, avec STATUS_NONEXISTANT)
	public static final short STATUS_UNCHANGED = 7;
	public static final short STATUS_CHANGED = 8;
	
	protected Manifest manifest;
	protected short status;
	
	public EntryArchiveData() {
		this.status = STATUS_UNKNOWN;
	}
	
	public Manifest getManifest() {
		return manifest;
	}
	public void setManifest(Manifest manifest) {
		this.manifest = manifest;
	}
	public short getStatus() {
		return status;
	}
	public void setStatus(short status) {
		this.status = status;
	}
}
