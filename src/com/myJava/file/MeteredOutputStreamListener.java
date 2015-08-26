package com.myJava.file;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


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
public class MeteredOutputStreamListener implements OutputStreamListener, Externalizable {
	private long written;
	
	public void bytesWritten(byte[] data, int offset, int length) {
		written += length;
	}

	public void closed() {
	}

	public long getWritten() {
		return this.written;
	}

	public void byteWritten(int data) {
		written += 1;
	}

	public void reset() {
		written = 0;
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		written = in.readLong();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(written);
	}
}
