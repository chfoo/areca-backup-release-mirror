package com.myJava.file.driver;

import java.io.IOException;
import java.io.InputStream;

/**
 * utility class used to add specific bevahior to cipher streams (like removing exceptions on close)
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
public class CipherInputStreamWrapper extends InputStream {

	private InputStream cipherStream;
	private InputStream baseStream;
	
	public CipherInputStreamWrapper(InputStream cipherStream, InputStream baseStream) {
		this.cipherStream = cipherStream;
		this.baseStream = baseStream;
	}

	public void close() throws IOException {
		try {
			cipherStream.close();
		} catch(Exception e) {
			try {
				// if closing the cipher stream raised an exception, we attempt to close the base stream
				baseStream.close();
			} catch (Exception ignored) {
			}
		}
	}

	public int read() throws IOException {
		return cipherStream.read();
	}

	public int read(byte[] b, int off, int len) throws IOException {
		return cipherStream.read(b, off, len);
	}

	public int read(byte[] b) throws IOException {
		return cipherStream.read(b);
	}

	public int available() throws IOException {
		return cipherStream.available();
	}

	public synchronized void mark(int readlimit) {
		cipherStream.mark(readlimit);
	}

	public boolean markSupported() {
		return cipherStream.markSupported();
	}

	public synchronized void reset() throws IOException {
		cipherStream.reset();
	}

	public long skip(long n) throws IOException {
		return cipherStream.skip(n);
	}
}
