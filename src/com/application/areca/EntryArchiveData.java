package com.application.areca;

import com.application.areca.metadata.manifest.Manifest;


/**
 * Data for a stored entry
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
public class EntryArchiveData {
	protected short status;    
    protected Manifest manifest;
    protected String hash;
    protected long metadataVersion;

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
		this.status = EntryStatus.STATUS_UNKNOWN;
	}
	
	public short getStatus() {
		return status;
	}
	
	public void setStatus(short status) {
		this.status = status;
	}

	public long getMetadataVersion() {
		return metadataVersion;
	}

	public void setMetadataVersion(long metadataVersion) {
		this.metadataVersion = metadataVersion;
	}
}
