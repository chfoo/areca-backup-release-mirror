package com.myJava.file;

import java.io.IOException;
import java.io.OutputStream;

import com.myJava.file.driver.ThrottleHandler;

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
public class ThrottledOutputStream extends OutputStream {
	
	private OutputStream out;
	private ThrottleHandler tHandler;
	private int maxBatchSize;

	public ThrottledOutputStream(OutputStream out, ThrottleHandler tHandler) {
		this.out = out;
		this.tHandler = tHandler;
		this.maxBatchSize = tHandler.getMaxBatchSizeBytes();
	}

	public void close() throws IOException {
		out.close();
	}

	public void flush() throws IOException {
		out.flush();
	}
	
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	public void write(int b) throws IOException {
		out.write(b);
		tHandler.checkTimer(1);
	}
	
	public void write(byte[] b, int off, int len) throws IOException {
		if (len > maxBatchSize) {
			int nrFullBatches = (len / maxBatchSize);
			int remainingBytes = len - nrFullBatches * maxBatchSize;
			
			int currentOffset = off;
			for (int i=0; i<nrFullBatches; i++) {
				writeUnit(b, currentOffset, maxBatchSize);
				currentOffset += maxBatchSize;
			}
			
			if (remainingBytes > 0) {
				writeUnit(b, currentOffset, remainingBytes);
			}
		} else {
			writeUnit(b, off, len);
		}
	}

	private void writeUnit(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
		tHandler.checkTimer(len);
	}
}