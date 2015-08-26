package com.myJava.file;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.myJava.configuration.FrameworkConfiguration;

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
public class HashOutputStreamListener implements OutputStreamListener {
	private static final String HASH_ALGORITHM = FrameworkConfiguration.getInstance().getFileHashAlgorithm();
	
	private boolean closed = false;
	private MessageDigest dg;
	
	// Reset has to be called
	public HashOutputStreamListener() {
	}
	
	public void bytesWritten(byte[] data, int offset, int length) {
		if (length > 0) {
			dg.update(data, offset, length);			
		}
	}

	public void byteWritten(int b) {
		if (b >= 0) {
			dg.update((byte)b);	
		}
	}
	
	public void reset() {
        try {
			dg = MessageDigest.getInstance(HASH_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

	public void closed() {
		closed = true;
	}

	public byte[] getHash() {
		if (! closed) {
			throw new IllegalStateException("The stream is not closed");
		}
		return dg.digest();
	}
}
