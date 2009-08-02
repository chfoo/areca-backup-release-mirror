package com.application.areca.launcher.gui;

import com.application.areca.AbstractTarget;
import com.application.areca.adapters.AdapterException;
import com.application.areca.adapters.XMLTags;

/**
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
public class MissingDataListener
implements com.application.areca.adapters.MissingDataListener, XMLTags {
    
    public Object missingEncryptionDataDetected(AbstractTarget target) throws AdapterException {
        return Application.getInstance().retrieveMissingEncryptionData(target);
    }

	public Object missingFTPDataDetected(AbstractTarget target) throws AdapterException {
        return Application.getInstance().retrieveMissingFTPData(target);
	}
}
