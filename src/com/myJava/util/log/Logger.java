package com.myJava.util.log;

import java.util.ArrayList;
import java.util.Iterator;

import com.myJava.configuration.FrameworkConfiguration;

/**
 * <BR>
 * 
 * @author Olivier PETRUCCI <BR>
 *         
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
public class Logger implements LogLevels {
	public static final int[] LEVELS = new int[] { Logger.LOG_LEVEL_ERROR,
			Logger.LOG_LEVEL_WARNING, Logger.LOG_LEVEL_INFO,
			Logger.LOG_LEVEL_DETAIL, Logger.LOG_LEVEL_FINEST };

	private Object lock = this;
	private ArrayList messages = new ArrayList();
	private LogMessagePool pool = new LogMessagePool(200);
	private int logLevel;
	private ArrayList processors = new ArrayList();
	private static int WAIT = 5000;
	private ThreadLocalLogProcessor tlLogProcessor; // specific log processor -
													// depends on the current
													// thread ... to be
													// refactored

	private static Logger defaultLogger = new Logger();

	private class LogConsumer implements Runnable {
		private boolean infiniteLoop = true;
		private LogMessagePool pool;
		
		public LogConsumer(boolean infiniteLoop, LogMessagePool pool) {
			this.infiniteLoop = infiniteLoop;
			this.pool = pool;
		}

		public void run() {
			try {
				while (infiniteLoop || (!messages.isEmpty())) {
					LogMessage msg = null;
					synchronized (lock) {
						if (!messages.isEmpty()) {
							msg = (LogMessage)messages.remove(0);
						}
					}

					if (msg != null) {
						for (int i=0; i<processors.size(); i++) {
							LogProcessor proc = (LogProcessor)processors.get(i);
							try {
								proc.log(msg.getLevel(), msg.getMessage(), msg.getException(), msg.getSource());
							} catch (Throwable e) {
								e.printStackTrace();
							}
						}
					}

					synchronized (lock) {
						//this.pool.release(msg);
						
						if (infiniteLoop && messages.isEmpty()) {
							try {
								lock.wait(WAIT);
							} catch (InterruptedException ignored) {
							}
						}
					}
				}
			} catch (Throwable e) {
				// Unexpected exception during logging

				// Show a message in the console
				String msg = "An error occurred during logging. No more application messages will be displayed. It is advisable to restart the application.";
				System.out.println(msg);
				e.printStackTrace();

				// Try to log the error
				Iterator iter = processors.iterator();
				while (iter.hasNext()) {
					LogProcessor proc = (LogProcessor) iter.next();
					try {
						proc.log(1, msg, e, "");
					} catch (RuntimeException e1) {
					}
				}
			}
		}
	}

	public ThreadLocalLogProcessor getTlLogProcessor() {
		return tlLogProcessor;
	}

	public void setTlLogProcessor(ThreadLocalLogProcessor tlLogProcessor) {
		this.tlLogProcessor = tlLogProcessor;
	}

	public Logger() {
		this.setLogLevel(FrameworkConfiguration.getInstance().getLogLevel());
		this.addProcessor(new ConsoleLogProcessor(true));

		// Create and launch consumer thread
		LogConsumer consumer = new LogConsumer(true, this.pool);
		Thread consumerThread = new Thread(consumer);
		consumerThread.setDaemon(true);
		consumerThread.setName("Logger");
		consumerThread.start();

		// Create and register shutdown hook
		LogConsumer hook = new LogConsumer(false, this.pool);
		Thread shutdownThread = new Thread(hook);
		shutdownThread.setDaemon(false);
		shutdownThread.setName("Logger - Shutdown Thread");
		Runtime.getRuntime().addShutdownHook(shutdownThread);
	}

	public static Logger defaultLogger() {
		return defaultLogger;
	}

	public void setLogLevel(int l) {
		logLevel = l;
	}

	public int getLogLevel() {
		return logLevel;
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

	public void displayApplicationMessage(String messageKey, String title,
			String message) {
		Iterator iter = this.processors.iterator();
		while (iter.hasNext()) {
			LogProcessor proc = (LogProcessor) iter.next();
			proc.displayApplicationMessage(messageKey, title, message);
		}

		if (tlLogProcessor != null) {
			tlLogProcessor
					.displayApplicationMessage(messageKey, title, message);
		}
	}

	public static void overrideDefaultLogger(Logger newDefaultLogger) {
		defaultLogger = newDefaultLogger;
	}

	public LogProcessor find(Class c) {
		Iterator iter = this.processors.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			if (c.isAssignableFrom(o.getClass())) {
				return (LogProcessor) o;
			}
		}
		return null;
	}

	public void remove(Class c) {
		ArrayList list = cloneProcessorList();

		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			LogProcessor o = (LogProcessor) iter.next();
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
				LogProcessor proc = (LogProcessor) o;
				proc.clearLog();
			}
		}

		if (tlLogProcessor != null) {
			tlLogProcessor.clearLog();
		}
	}

	protected void log(int level, String message, Throwable e, String source) {
		if (level <= logLevel) {
			//LogMessage msg = pool.get();			// This row has to be added in the synchronized block because the messagePool is not thread safe (and we do not want to add another lock)
			LogMessage msg = new LogMessage();
			msg.init(level, message, source, e);
			
			synchronized (lock) {
				messages.add(msg);
				lock.notify();
			}
		}

		if (tlLogProcessor != null) {
			tlLogProcessor.log(level, message, e, source);
		}
	}

	private void log(int level, String message, String source) {
		log(level, message, null, source);
	}

	public void error(String message, Throwable e, String source) {
		log(LOG_LEVEL_ERROR, message, e, source);
	}

	public void error(String message, Throwable e) {
		error(message, e, "");
	}

	public void error(Throwable e) {
		error("", e, "");
	}

	public void error(String message, String source) {
		log(LOG_LEVEL_ERROR, message, source);
	}

	public void error(String message) {
		log(LOG_LEVEL_ERROR, message, "");
	}

	public void warn(String message, Throwable e) {
		warn(message, e, "");
	}

	public void warn(String message, String source) {
		log(LOG_LEVEL_WARNING, message, source);
	}

	public void warn(String message) {
		log(LOG_LEVEL_WARNING, message, "");
	}

	public void warn(String message, Throwable e, String source) {
		log(LOG_LEVEL_WARNING, message, e, source);
	}

	public void info(String message, String source) {
		log(LOG_LEVEL_INFO, message, source);
	}

	public void info(String message) {
		log(LOG_LEVEL_INFO, message, "");
	}

	public void fine(String message) {
		log(LOG_LEVEL_DETAIL, message, "");
	}

	public void finest(String message) {
		log(LOG_LEVEL_FINEST, message, "");
	}

	public void finest(Throwable e) {
		log(LOG_LEVEL_FINEST, "", e, "");
	}
}