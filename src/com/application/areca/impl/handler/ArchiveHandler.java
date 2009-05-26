package com.application.areca.impl.handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.impl.FileSystemRecoveryEntry;
import com.application.areca.impl.tools.RecoveryFilterMap;
import com.myJava.object.Duplicable;
import com.myJava.util.taskmonitor.TaskCancelledException;

/**
 * Base interface for archive handlers.
 * <BR>Handles :
 * <BR>- Merge actions
 * <BR>- Whole archive recovery actions
 * <BR>- Backup actions
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
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
public interface ArchiveHandler extends Duplicable {

    public static final short MODE_MERGE = 1;
    public static final short MODE_RECOVER = 2;
    
    /**
     * Medium to which the handler is associated
     */
    public AbstractIncrementalFileSystemMedium getMedium();
    
    /**
     * Medium to which the handler is associated
     */
    public void setMedium(AbstractIncrementalFileSystemMedium medium);
    
    /**
     * The outputStream is provided by the Medium itself. The inputStream is provided by the target.
     * <BR>Reads the inputStream and writes the data in the outputStream, according to the handler's strategy.
     * (standard storage mode, delta storage, ...)
     */
    public void store(FileSystemRecoveryEntry entry, InputStream in, OutputStream out, ProcessContext context) 
    throws ApplicationException, IOException, TaskCancelledException;
    
    /**
     * Restore the data
     */
    public void recoverRawData(
            File[] archivesToRecover, 
            RecoveryFilterMap filtersByArchive, 
            short mode,
            ProcessContext context
    ) throws IOException, ApplicationException, TaskCancelledException;
    
    /**
     * Return, for the array of entries passed as argument, the archives that will have to be recovered among the archive list passed as argument.
     * <BR>The returned map contains entries indexed by archive file.
     * <BR>entriesToRecover MUST be sorted
     */
    public RecoveryFilterMap dispatchEntries(File[] archives, String[] entriesToRecover) throws ApplicationException, IOException;
    
    /**
     * Init the handler
     */
    public void init(ProcessContext context) throws IOException;
    
    /**
     * Close the handler
     */
    public void close(ProcessContext context) throws IOException, ApplicationException;
	
	/**
	 * Callback after archive deletion
	 */
	public void archiveDeleted(File archive) throws IOException;
	
	/**
	 * Tells whether the handler supports image backups or not
	 * <BR>(some handlers are inherently incompatible with image backups)
	 */
	public boolean supportsImageBackup();
	
	/**
	 * Tells whether the handler builds autonomous archives
	 */
	public boolean autonomousArchives();
	
	/**
	 * Ugly but no time to do better
	 */
	public File getContentFile(File archive);
}
