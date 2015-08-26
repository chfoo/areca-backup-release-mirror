package com.myJava.util.threadmonitor;

/**
 * 
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
public class ThreadMonitorItem {
	private String key;
	private Thread thread;
	private long delay;
	private long lastNotification;

	public ThreadMonitorItem(String key, long delay) {
		this.key = key;
		this.delay = delay;
		this.thread = Thread.currentThread();
		this.lastNotification = System.currentTimeMillis();
	}

	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public Thread getThread() {
		return thread;
	}
	
	public void setThread(Thread thread) {
		this.thread = thread;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public long getLastNotification() {
		return lastNotification;
	}

	public void setLastNotification(long lastNotification) {
		this.lastNotification = lastNotification;
	}
	
	public boolean check(long time) {
		long delta = time - lastNotification;
		return delta <= delay;
	}
}
