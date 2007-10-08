package com.application.areca.impl.policy;

import com.application.areca.ApplicationException;
import com.application.areca.ArchiveMedium;
import com.myJava.file.FileSystemDriver;
import com.myJava.object.PublicClonable;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 6222835200985278549
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
public interface FileSystemPolicy extends PublicClonable {
    public String getId();
    public String getBaseArchivePath();
    public FileSystemDriver initFileSystemDriver() throws ApplicationException;
    public String getDisplayableParameters();
    public void validate(boolean extendedTests) throws ApplicationException;
    public void setMedium(ArchiveMedium medium);
    public void synchronizeConfiguration();
}
