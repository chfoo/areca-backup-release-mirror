package com.myJava.util.securedrunner;

import com.myJava.util.log.Logger;

/**
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
public class Runner {
	public static short STATUS_NOT_RUN = 0;
	public static short STATUS_RUNNING = 1;
	public static short STATUS_COMPLETED = 2;

	private static Runner INSTANCE = new Runner();

	private AbstractRunnable candidate = null;
	private Object result = null;
	private Integer lock = new Integer(1);

	public Runner() {
		Runnable rn = new Runnable() {
			public void run() {
				try {
					while(true) {
						synchronized(lock) {
							if (candidate != null) {
								if (candidate.getStatus() == STATUS_RUNNING) {
									candidate.setException(new IllegalStateException("Illegal runnable state : " + candidate.getStatus()));
								} else if (candidate.getStatus() == STATUS_NOT_RUN){
									candidate.run();
									result = candidate.getResult();
									candidate = null;
								}

								lock.notify();
							}

							if (candidate == null) {
								lock.wait();
							}
						}
					}
				} catch (InterruptedException e) {
					Logger.defaultLogger().error(e);
				}
			}
		};

		Thread executorThread = new Thread(rn, "Main runner thread");
		executorThread.setDaemon(true);
		executorThread.start();
	}

	public static Runner instance() {
		return INSTANCE;
	}

	public synchronized Object execute(AbstractRunnable rn) throws InterruptedException {
		synchronized(lock) {
			push(rn);
			while (candidate != null) { /// will be set to null once finished
				lock.wait();
			}
		}
		return result;
	}

	private void push(AbstractRunnable rn) {
		if (candidate != null) {
			// Shall not happen because the lock is released once the current candidate is completed and set to null
			throw new IllegalStateException("Already something being run !");
		}
		candidate = rn;
		result = null;
		lock.notify();
	}
}
