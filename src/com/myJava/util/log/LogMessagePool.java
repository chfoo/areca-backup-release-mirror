package com.myJava.util.log;

/**
 * Pool of log messages.
 * <BR>This class is NOT thread safe : we assume that it is handled by the caller
 * <BR>(mainly because locks are already handled in the producer/consumer scheme used by the logger class)
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
public class LogMessagePool {

	private static final double reallocRatio = 2;
	private LogMessage[] free;
	private int nextFreeIndex;
	
	protected LogMessagePool(int initialCapacity) {
		this.free = new LogMessage[initialCapacity];
		for (int i=0; i<initialCapacity; i++) {
			this.free[i] = buildNewInstance();
		}
		
		nextFreeIndex = this.free.length - 1;
	}
	
	private void reallocate() {
		int newSize = (int)(reallocRatio * this.free.length);
		LogMessage[] newFree = new LogMessage[newSize];
		
		for (int i=0; i<=nextFreeIndex; i++) {
			newFree[i] = free[i];
		}
		
		int nbNew = newFree.length - free.length;
		
		for (int i=0; i<nbNew; i++) {
			newFree[nextFreeIndex + 1 + i] = buildNewInstance();
		}
		
		this.free = newFree;
		this.nextFreeIndex += nbNew;
	}
	
	protected LogMessage buildNewInstance() {
		return new LogMessage();
	}
	
	public LogMessage get() {
		if (nextFreeIndex == -1) {
			reallocate();
		}
		
		LogMessage ret = (LogMessage)free[nextFreeIndex];
		free[nextFreeIndex] = null;
		nextFreeIndex--;
		return ret;
	}
	
	public void release(LogMessage message) {
		if (message != null) {
			nextFreeIndex++;
			this.free[nextFreeIndex] = message;
		}
	}
}
