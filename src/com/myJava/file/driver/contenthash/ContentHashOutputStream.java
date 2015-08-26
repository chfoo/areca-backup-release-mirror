package com.myJava.file.driver.contenthash;

import java.io.IOException;
import java.io.OutputStream;
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
public class ContentHashOutputStream 
extends OutputStream {
	private static final String HASH_ALGORITHM = FrameworkConfiguration.getInstance().getFileHashAlgorithm();

    private OutputStream out;
    private MessageDigest dg;
    private boolean closed = false;

    public ContentHashOutputStream(OutputStream out) throws NoSuchAlgorithmException {
        super();
        this.out = out;
        this.dg = MessageDigest.getInstance(HASH_ALGORITHM);
    }

    public void close() throws IOException {
    	if (closed) {
    		return;
    	}
    	closed = true;
    	
        out.write(dg.digest());
    	this.flush();
        out.close();
    }

    public void flush() throws IOException {
        out.flush();
    }

    public void write(byte[] b, int off, int len) throws IOException {
    	update(b, off, len);
    }

    public void write(byte[] b) throws IOException {
    	update(b, 0, b.length);
    }

    public void write(int b) throws IOException {
    	update(new byte[] {(byte)b}, 0, 1);
    }
    
    private void update(byte[] buff, int off, int len) {
		dg.update(buff, off, len);
    }
}
