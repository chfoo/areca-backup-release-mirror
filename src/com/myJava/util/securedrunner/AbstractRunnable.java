package com.myJava.util.securedrunner;

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
public abstract class AbstractRunnable {
	private short status = Runner.STATUS_NOT_RUN;
	private Object result = null;
	private Exception exception = null;

	private synchronized void setRunning() {
		this.status = Runner.STATUS_RUNNING;
	}
	
	protected synchronized void finish(Object result) {
		this.result = result;
		this.status = Runner.STATUS_COMPLETED;
	}
	
	public synchronized short getStatus() {
		return status;
	}
	
	public Object getResult() {
		return result;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public void run() {
		setRunning();
		runImpl();
	}
	
	/**
	 * This method MUST call the "finish()" method once completed
	 * <BR>It must also catch exceptions and register them in the exception field for later check
	 */
	public abstract void runImpl();
}
