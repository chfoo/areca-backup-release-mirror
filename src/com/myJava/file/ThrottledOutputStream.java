package com.myJava.file;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2011, Olivier PETRUCCI.

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

	/**
	 * Expected throughput - in bytes per second
	 */
	private long throughput;
	private OutputStream out;
	private long timer = 0;

	public ThrottledOutputStream(OutputStream out, long throughput) {
		this.throughput = throughput;
		this.out = out;
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
		startTimer();
		out.write(b);
		stopTimer(1);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		startTimer();
		out.write(b, off, len);
		stopTimer(len);
	}
	
	private void startTimer() {
		timer = System.currentTimeMillis();
	}
	
	private void stopTimer(int nrOfBytes) {
		long millis = timer + ((1000 * nrOfBytes)/throughput) - System.currentTimeMillis();
        if (millis > 0) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ignored) {
            }
        }
	}
	
	public static void main(String[] args) {
		String content = "";
		for (int i=0; i<10*1024; i++) {
			content += "0123456789";
		}
		byte[] data = content.getBytes();
		System.out.println("Writing " + data.length + " bytes.");
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		long start = System.currentTimeMillis();
		ThrottledOutputStream out = new ThrottledOutputStream(baos, 50*1024);
		try {
			out.write(data);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long stop = System.currentTimeMillis();
		
		System.out.println("Elapsed : " + (stop-start) + " ms");
		byte[] read = baos.toByteArray();
		
		boolean ok = true;
		for (int i=0; i<read.length; i++) {
			if (read[i] != data[i]) {
				ok = false;
				break;
			}
		}
		if (ok) {
			System.out.println("Content successfully checked.");
		} else {
			System.out.println("Content not consistent.");
		}
	}
}