package com.application.areca.impl.copypolicy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.application.areca.metadata.content.ArchiveContentAdapter;
import com.myJava.file.copypolicy.CopyPolicy;
import com.myJava.file.copypolicy.CopyPolicyException;


/**
 * 
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2011, Olivier PETRUCCI.

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
public class ArecaCompositeCopyPolicy implements CopyPolicy {
	private CopyPolicy[] policies;
	private ArchiveContentAdapter currentWriter;
	private List files = new ArrayList();

	public ArecaCompositeCopyPolicy(CopyPolicy[] policies) {
		this.policies = policies;
	}
	
	public ArecaCompositeCopyPolicy(CopyPolicy p1, CopyPolicy p2) {
		if (p1 != null && p2 != null) {
			this.policies = new CopyPolicy[] {p1, p2};
		} else if (p1 != null) {
			this.policies = new CopyPolicy[] {p1};
		} else if (p2 != null) {
			this.policies = new CopyPolicy[] {p2};
		} else {
			this.policies = new CopyPolicy[0];
		}
	}

	public boolean accept(File file) throws CopyPolicyException {
		for (int i=0; i<policies.length; i++) {
			if (! policies[i].accept(file)) {
				registerExcludedFile(file);
				return false;
			}
		}
		return true;
	}
	
	private void registerExcludedFile(File file) {
		
	}
	
	public void switchToNewArchive() {
		//this.currentWriter = new ArchiveContentAdapter(contentFile, prefix)
	}
	
	public CopyPolicy buildPolicyFromExcludedFiles() {
		return null;
	}
}
