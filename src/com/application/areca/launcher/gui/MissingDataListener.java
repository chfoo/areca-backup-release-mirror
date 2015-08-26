package com.application.areca.launcher.gui;

import com.application.areca.AbstractTarget;
import com.application.areca.ConfigurationSource;
import com.application.areca.adapters.XMLTags;
import com.application.areca.impl.policy.EncryptionPolicy;
import com.application.areca.launcher.gui.common.SecuredRunner;
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
    
    public EncryptionPolicy missingEncryptionDataDetected(
    		AbstractTarget target, 
    		String algorithm, 
    		Boolean encryptNames, 
    		String nameWrappingMode,
    		ConfigurationSource source
    ) throws AdapterException {
		final MissingEncryptionDataWindow frm = new MissingEncryptionDataWindow(target, algorithm, encryptNames, source);
		
		SecuredRunner.execute(
				new Runnable() {
					public void run() {
						Application.getInstance().showDialog(frm);
					}
				});
		
		if (frm.isSaved()) {
			EncryptionPolicy policy = new EncryptionPolicy();
			policy.setEncrypted(true);
			policy.setEncryptionAlgorithm(frm.getAlgo());
			policy.setEncryptionKey(frm.getPassword());
			policy.setEncryptNames(frm.isEncryptFileNames().booleanValue());
			policy.setNameWrappingMode(nameWrappingMode);
			
			return policy;
		} else {
			return null;
		}
    }

	public Object missingFTPDataDetected(AbstractTarget target, ConfigurationSource source) throws AdapterException {
		MissingFTPDataWindow frm = new MissingFTPDataWindow(target, source);
		Application.getInstance().showDialog(frm);
		return new Object[] {frm.getPassword()};
	}
}
