package com.myJava.file;

import java.io.IOException;
import java.io.InputStream;

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
public class EventInputStream extends InputStream {

	private InputStream in;
	private InputStreamListener listener;
	
	public EventInputStream(InputStream in, InputStreamListener listener) {
		this.in = in;
		this.listener = listener;
	}

	public void close() throws IOException {
		in.close();
		listener.close();
	}

	public int read() throws IOException {
		int read = in.read();
		listener.read(read);
		return read;
	}

	public int read(byte[] b, int off, int len) throws IOException {
		int read = in.read(b, off, len);
		listener.read(b, off, len, read);
		return read;
	}

	public int read(byte[] b) throws IOException {
		int read = in.read(b);
		listener.read(b, 0, b.length, read);
		return read;
	}

	public int available() throws IOException {
		int available = in.available();
		listener.available(available);
		return available;
	}
}
