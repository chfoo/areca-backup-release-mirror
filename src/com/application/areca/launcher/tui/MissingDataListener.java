package com.application.areca.launcher.tui;

import com.application.areca.AbstractTarget;
import com.application.areca.ConfigurationSource;
import com.application.areca.adapters.XMLTags;
import com.application.areca.impl.policy.EncryptionPolicy;
import com.myJava.util.xml.AdapterException;

/**
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
public class MissingDataListener
implements com.application.areca.adapters.MissingDataListener, XMLTags {

    public EncryptionPolicy missingEncryptionDataDetected(AbstractTarget target, String algorithm, Boolean encryptNames, String nameWrappingMode, ConfigurationSource source) throws AdapterException {
        throw new AdapterException("No encryption key found in " + source.getSource() + " : your medium must have a '" + XML_MEDIUM_ENCRYPTIONKEY + "' attribute because it is encrypted (" + XML_MEDIUM_ENCRYPTED + " = true).");
    }

	public Object missingFTPDataDetected(AbstractTarget target, ConfigurationSource source) throws AdapterException {
        throw new AdapterException("No FTP password found in " + source.getSource() + " : your medium must have a '" + XML_MEDIUM_FTP_PASSWORD + "' attribute.");
	}
}
