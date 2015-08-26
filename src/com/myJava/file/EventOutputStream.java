package com.myJava.file;

import java.io.IOException;
import java.io.OutputStream;

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
public class EventOutputStream extends OutputStream {
	private OutputStream out;
	private OutputStreamListener listener;
	
	public EventOutputStream(OutputStream out, OutputStreamListener listener) {
		this.out = out;
		this.listener = listener;
	}

	public void close() throws IOException {
		try {
			this.flush();
		} finally {
			this.out.close();
			if (listener != null) {
				listener.closed();	
			}
		}
	}

	public void flush() throws IOException {
		this.out.flush();
	}

	public void write(byte[] b, int offset, int length) throws IOException {
		this.out.write(b, offset, length);
		if (listener != null) {
			listener.bytesWritten(b, offset, length);
		}
	}

	public void write(byte[] b) throws IOException {
		this.out.write(b);
		if (listener != null) {
			listener.bytesWritten(b, 0, b.length);
		}
	}

	public void write(int b) throws IOException {
		this.out.write(b);
		if (listener != null) {
			listener.byteWritten(b);
		}
	}
}
