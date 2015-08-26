package com.application.areca.impl.handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.application.areca.impl.FileSystemRecoveryEntry;
import com.application.areca.impl.copypolicy.AbstractCopyPolicy;
import com.application.areca.impl.tools.RecoveryFilterMap;
import com.application.areca.metadata.transaction.TransactionPoint;
import com.myJava.file.FileTool;
import com.myJava.file.driver.DriverAlreadySetException;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.object.Duplicable;
import com.myJava.util.taskmonitor.TaskCancelledException;

/**
 * Default handler : handles standard files.
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
public class DefaultArchiveHandler
extends AbstractArchiveHandler {

	public void store(FileSystemRecoveryEntry entry, InputStream in, OutputStream out, ProcessContext context)
	throws ApplicationException, IOException, TaskCancelledException {    
		FileTool.getInstance().copy(in, out, true, false, context.getTaskMonitor());
	}

	public void recoverRawData(
			File[] archivesToRecover, 
			RecoveryFilterMap filtersByArchive,
			AbstractCopyPolicy policy,
			File referenceTrace,
			short mode,
			ProcessContext context
	) throws IOException, ApplicationException, TaskCancelledException {
		// Simply recover the files
		medium.ensureLocalCopy(
				archivesToRecover, 
				true, 
				context.getRecoveryDestination(), 
				filtersByArchive, 
				policy,
				context
		);
	}

	public void archiveDeleted(File archive) 
	throws IOException {
	}

	public void close(ProcessContext context) 
	throws IOException {
	}

	public void init(ProcessContext context, TransactionPoint transactionPoint) 
	throws IOException {
	}
	
	public void initializeSimulationDriverData(FileSystemDriver initialDriver, ProcessContext context) throws IOException, DriverAlreadySetException {
	}

	public Duplicable duplicate() {
		return new DefaultArchiveHandler();
	}
	
	public EntriesDispatcher buildEntriesDispatcher(File[] archives) {
		return new DefaultEntriesDispatcher(archives, medium);
	}

	public boolean supportsImageBackup() {
		return true;
	}

	public File getContentFile(File archive) {
		return null;
	}

	public boolean autonomousArchives() {
		return true;
	}
}
