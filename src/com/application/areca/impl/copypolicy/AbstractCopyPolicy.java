package com.application.areca.impl.copypolicy;

import java.io.File;
import java.io.IOException;

import com.application.areca.context.ProcessContext;
import com.application.areca.metadata.FileList;
import com.myJava.file.FileSystemManager;
import com.myJava.file.copypolicy.CopyPolicy;
import com.myJava.file.copypolicy.CopyPolicyException;
import com.myJava.util.log.Logger;

/**
 * 
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
public abstract class AbstractCopyPolicy implements CopyPolicy {
	private FileList excludedFiles = new FileList();
	protected ProcessContext context;

	public boolean accept(File file) throws CopyPolicyException {
		boolean accepted = acceptImpl(file);
		if (! accepted) {
			registerFilteredFile(file);
		}
		return accepted;
	}
	
	protected abstract boolean acceptImpl(File file) throws CopyPolicyException;
	
	protected void registerFilteredFile(File f) throws CopyPolicyException {
		Logger.defaultLogger().fine(FileSystemManager.getDisplayPath(f) + " has been excluded from recovery.");
		try {
			excludedFiles.add(FileSystemManager.getAbsolutePath(f));
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw new CopyPolicyException("Error caught while adding excluded file.", e);
		}
	}
	
	/**
	 * Lock the list of excluded files and return it to the caller
	 * @return
	 * @throws IOException
	 */
	public FileList listExcludedFiles() throws IOException {
		excludedFiles.lock();
		return excludedFiles;
	}

	public void setContext(ProcessContext context) {
		this.context = context;
	}
}
