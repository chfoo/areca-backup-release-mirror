package com.application.areca.plugins;

import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.transaction.TransactionPoint;

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
public interface ConfigurationAddon {
	public String getId();
	
	public void open(TransactionPoint transactionPoint, ProcessContext context) throws ApplicationException;
	public void open(Manifest manifest, ProcessContext context, String backupScheme) throws ApplicationException;
	
	public void commitBackup(ProcessContext context) throws ApplicationException;
	public void rollbackBackup(ProcessContext context, String message) throws ApplicationException;
}
