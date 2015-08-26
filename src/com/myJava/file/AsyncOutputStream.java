package com.myJava.file;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import com.myJava.util.log.Logger;

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
public class AsyncOutputStream extends OutputStream {
	private static int WAIT = 5000;
	
	private OutputStream out;
	private ArrayList tasks = new ArrayList();
	private Object lock = this;
	private Exception error;
	private TaskConsumer consumer;
	private Thread consumerThread;

	public AsyncOutputStream(OutputStream out) {
		this.out = out;
		
		// Create and launch consumer thread
		consumer = new TaskConsumer(true);
		consumerThread = new Thread(consumer);
		consumerThread.setDaemon(false);
		consumerThread.setName("Write Task Consumer");
		consumerThread.start();
	}

	public void close() throws IOException {
		try {
			checkError();
			this.consumer.setInfiniteLoop(false);
			try {
				consumerThread.join();
			} catch (InterruptedException e) {
				throw new IOException(e);
			}
			this.flush();
		} finally {
			this.out.close();
		}
	}

	public void flush() throws IOException {
		checkError();
		this.out.flush();
	}

	public void write(byte[] b, int offset, int length) throws IOException {
		checkError();
		addTask(new WriteTask(cloneData(b, offset, length)));
	}

	public void write(byte[] b) throws IOException {
		checkError();
		write(b, 0, b.length);
	}

	public void write(int b) throws IOException {
		checkError();
		Logger.defaultLogger().warn("Caution : Accessing AsyncOutputStream byte by byte - not optimal.");
		addTask(new WriteTask(b));
	}
	
	private void addTask(WriteTask task) {
		synchronized (lock) {
			tasks.add(task);
			lock.notify();
		}
	}
	
	private byte[] cloneData(byte[] in, int offset, int length) {
		byte[] out = new byte[length];
		for (int i=offset; i<offset+length; i++) {
			out[i-offset] = in[i];
		}
		return out;
	}
	
	private static class WriteTask {
		private byte[] data;

		public WriteTask(byte[] data) {
			this.data = data;
		}
		
		public WriteTask(int data) {
			this.data = new byte[] {(byte)data};
		}

		public byte[] getData() {
			return data;
		}
	}
	
	private void checkError() throws IOException {
		synchronized(lock) {
			if (error != null) {
				error = null;
				throw new IOException(error);
			}
		}
	}
	
	private class TaskConsumer implements Runnable {	
		private boolean infiniteLoop = true;

		public TaskConsumer(boolean infiniteLoop) {
			this.infiniteLoop = infiniteLoop;
		}

		public void setInfiniteLoop(boolean infiniteLoop) {
			this.infiniteLoop = infiniteLoop;
		}

		public void run() {
			try {
				while (infiniteLoop || (! tasks.isEmpty())) {
					WriteTask task = null;
					synchronized(lock) {
						if (! tasks.isEmpty()) {
							task = (WriteTask)tasks.remove(0);
						}
					}

					if (task != null) {
						out.write(task.getData());
					}

					synchronized(lock) {
						if (infiniteLoop && tasks.isEmpty()) {
							try {
								lock.wait(WAIT);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}
			} catch (Exception e) {
				Logger.defaultLogger().error(e);
				error = e;
			}
		}
	}
}
