package com.application.areca.impl.handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.impl.FileSystemRecoveryEntry;
import com.myJava.object.PublicClonable;
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
public interface ArchiveHandler extends PublicClonable {

    public static final short MODE_MERGE = 1;
    public static final short MODE_RECOVER = 2;
    
    public AbstractIncrementalFileSystemMedium getMedium();
    public void setMedium(AbstractIncrementalFileSystemMedium medium);
    
    ////////////////////////////////////////////////////
    // Backup
    ////////////////////////////////////////////////////
    
    /**
     * The outputStream is provided by the Medium itself. The inputStream is provided by the target.
     * <BR>Reads the inputStream and writes the data in the outputStream, according to the handler's strategy.
     * (standard storage mode, delta storage, ...)
     */
    public void store(FileSystemRecoveryEntry entry, InputStream in, OutputStream out, ProcessContext context) 
    throws ApplicationException, IOException, TaskCancelledException;
    
    ////////////////////////////////////////////////////
    // Raw Recovery
    ////////////////////////////////////////////////////
    
    public void recoverRawData(
            File[] archivesToRecover, 
            Map filtersByArchive, 
            short mode,
            ProcessContext context
    ) throws IOException, ApplicationException;
    
    public Map dispatchEntries(File[] archives, Set entriesToRecover) throws ApplicationException, IOException;
}
