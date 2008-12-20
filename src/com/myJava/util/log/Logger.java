package com.myJava.util.log;

import java.util.ArrayList;
import java.util.Iterator;

import com.myJava.configuration.FrameworkConfiguration;


/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 8785459451506899793
 */
 
 /*
 Copyright 2005-2007, Olivier PETRUCCI.
 
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
public final class Logger {
	private Object lock = this;
	private ArrayList messages = new ArrayList();
	private int logLevel;
	private ArrayList processors = new ArrayList();

	private static Logger defaultLogger = new Logger();

	public Logger() {
		this.setLogLevel(FrameworkConfiguration.getInstance().getLogLevel());
		this.addProcessor(new ConsoleLogProcessor());

		Runnable consumer = new Runnable() {
			public void run() {
				try {
					while (true) {
						LogMessage msg = null;
						synchronized(lock) {
							if (! messages.isEmpty()) {
								msg = (LogMessage)messages.remove(0);
							}
						}

						if (msg != null) {
							Iterator iter = processors.iterator();
							while(iter.hasNext()) {
								LogProcessor proc = (LogProcessor)iter.next();
								try {
									proc.log(msg.level, msg.message, msg.e, msg.source);
								} catch (Throwable e) {
									e.printStackTrace();
								}
							}
						}

						synchronized(lock) {
							if (messages.isEmpty()) {
								try {
									lock.wait();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					}
				} catch (Throwable e) {
					// Unexpected exception during logging
					
					// Show a message in the console
					String msg = "An error occurred during logging. No more application messages will be displayed. It is advisable to restart Areca.";
					System.out.println(msg);
					e.printStackTrace();
					
					// Try to log the error
					Iterator iter = processors.iterator();
					while(iter.hasNext()) {
						LogProcessor proc = (LogProcessor)iter.next();
						try {
							proc.log(1, msg, e, "");
						} catch (RuntimeException e1) {
						}
					}
				}
			}
		};

		Thread th = new Thread(consumer);
		th.setDaemon(true);
		th.setName("Logger");
		th.start();
	}

	public static Logger defaultLogger() {
		return defaultLogger;
	}

	public void setLogLevel(int l) {
		logLevel = l;
	}

	public void addProcessor(LogProcessor proc) {
		ArrayList list = cloneProcessorList();
		list.add(proc);
		this.processors = list;
	}

	private ArrayList cloneProcessorList() {
		ArrayList clone = new ArrayList(this.processors.size());
		Iterator iter = processors.iterator();
		while (iter.hasNext()) {
			clone.add(iter.next());
		}
		return clone;
	}

	public void displayApplicationMessage(String messageKey, String title, String message) {
		Iterator iter = this.processors.iterator();
		while(iter.hasNext()) {
			LogProcessor proc = (LogProcessor)iter.next();
			proc.displayApplicationMessage(messageKey, title, message);
		}
	}

	public LogProcessor find(Class c) {
		Iterator iter = this.processors.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			if (c.isAssignableFrom(o.getClass())) {
				return (LogProcessor)o;
			}
		}
		return null;
	}

	public void remove(Class c) {
		ArrayList list = cloneProcessorList();

		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			LogProcessor o = (LogProcessor)iter.next();
			if (c.isAssignableFrom(o.getClass())) {
				o.unmount();
				iter.remove();
			}
		}

		this.processors = list;
	}

	public void clearLog(Class c) {
		Iterator iter = this.processors.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			if (c.isAssignableFrom(o.getClass())) {
				LogProcessor proc = (LogProcessor)o;
				proc.clearLog();
			}
		}
	}

	private void log(int level, String message, Throwable e, String source) {
		if (level <= logLevel) {
			synchronized (lock) {
				messages.add(new LogMessage(level, message, e, source));
				lock.notify();
			}
		}
	}

	private void log(int level, String message, String source) {
		log(level, message, null, source);
	}

	public void error(String message, Throwable e, String source) {
		log(1, message, e, source);
	}

	public void error(String message, Throwable e) {
		error(message, e, "");
	}

	public void error(Throwable e) {
		error("", e, "");
	}    

	public void error(String message, String source) {
		log(1, message, source);
	}

	public void error(String message) {
		log(1, message, "");
	}

	public void warn(String message, String source) {
		log(3, message, source);
	}

	public void warn(String message) {
		log(3, message, "");
	}    

	public void warn(String message, Throwable e, String source) {
		log(3, message, e, source);
	}

	public void info(String message, String source) {
		log(6, message, source);
	}

	public void info(String message) {
		log(6, message, "");
	}

	public void fine(String message) {
		log(8, message, "");
	}

	private static class LogMessage {
		private String message;
		private String source;
		private int level;
		private Throwable e;

		public LogMessage(int level, String message, Throwable e, String source) {
			this.message = message;
			this.source = source;
			this.level = level;
			this.e = e;
		}
	}
}