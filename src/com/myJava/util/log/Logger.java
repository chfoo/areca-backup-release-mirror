package com.myJava.util.log;

import java.util.ArrayList;
import java.util.Iterator;

import com.myJava.configuration.FrameworkConfiguration;


/**
 * Classe permettant de g�rer la log applicative
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 5323430991191230653
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

	private boolean isModifying = false;
	private boolean isReading = false;

	/**
	 * Niveau de log
	 */
	private int logLevel;

	private static Logger defaultLogger = new Logger();

	private ArrayList processors = new ArrayList();

	public synchronized void setModifying(boolean isModifying) {
		if (isModifying && this.isReading) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		this.isModifying = isModifying;
		this.notifyAll();
	}

	public synchronized void setReading(boolean isReading) {
		if (isReading && this.isModifying) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		this.isReading = isReading;
		this.notifyAll();
	}

	/**
	 * Constructeur.
	 */
	public Logger() {
		this.setLogLevel(FrameworkConfiguration.getInstance().getLogLevel());
		this.addProcessor(new ConsoleLogProcessor());
	}

	/**
	 * Indique le logger par d�faut de la classe
	 */
	public static Logger defaultLogger() {
		return defaultLogger;
	}

	/**
	 * Retourne le niveau de log
	 */
	public int getLogLevel() {
		return this.logLevel;
	}

	/**
	 * Indique le niveau de log.
	 * <BR>Tout appel de priorit� inf�rieure � la priorit� sp�cifi�e ne sera pas
	 * logg�
	 */
	public void setLogLevel(int l) {
		logLevel = l;
	}

	public void addProcessor(LogProcessor proc) {
		setModifying(true);
		try {
			this.processors.add(proc);
		} finally {
			setModifying(false);
		}
	}

	/**
	 * <BR>Retourne true en cas de succ�s, false en cas d'�chec.
	 */
	public boolean clearLog() {
		boolean b = true;
		setReading(true);
		try {
			Iterator iter = this.processors.iterator();
			while(iter.hasNext()) {
				LogProcessor proc = (LogProcessor)iter.next();
				b &= proc.clearLog();
			}
		} finally {
			setReading(false);    		
		}
		return b;
	}

	public void displayApplicationMessage(String messageKey, String title, String message) {
		setReading(true);
		try {
			Iterator iter = this.processors.iterator();
			while(iter.hasNext()) {
				LogProcessor proc = (LogProcessor)iter.next();
				proc.displayApplicationMessage(messageKey, title, message);
			}
		} finally {
			setReading(false);
		}
	}

	public LogProcessor find(Class c) {
		setReading(true);
		try {
			Iterator iter = this.processors.iterator();
			while (iter.hasNext()) {
				Object o = iter.next();
				if (c.isAssignableFrom(o.getClass())) {
					setReading(false);
					return (LogProcessor)o;
				}
			}
		} finally {
			setReading(false);
		}
		return null;
	}

	public void remove(Class c) {
		setModifying(true);
		try {
			Iterator iter = this.processors.iterator();
			while (iter.hasNext()) {
				LogProcessor o = (LogProcessor)iter.next();
				if (c.isAssignableFrom(o.getClass())) {
					o.unmount();
					iter.remove();
				}
			}
		} finally {
			setModifying(false);
		}
	}

	public void clearLog(Class c) {
		setReading(true);
		try {
			Iterator iter = this.processors.iterator();
			while (iter.hasNext()) {
				Object o = iter.next();
				if (c.isAssignableFrom(o.getClass())) {
					LogProcessor proc = (LogProcessor)o;
					proc.clearLog();
				}
			}
		} finally {
			setReading(false);
		}
	}

	/**
	 * Ecrit la log en v�rifiant le niveau pr�cis�.
	 */
	private void log(int level, String message, Throwable e, String source) {
		if (level <= logLevel) {
			setReading(true);
			try {
				Iterator iter = this.processors.iterator();
				while(iter.hasNext()) {
					LogProcessor proc = (LogProcessor)iter.next();
					try {
						proc.log(level, message, e, source);
					} catch (RuntimeException e1) {
						e1.printStackTrace();
					}
				}
			} finally {
				setReading(false);
			}
		}
	}

	/**
	 * Ecrit une log g�n�rique
	 */
	private void log(int level, String message, String source) {
		log(level, message, null, source);
	}

	/**
	 * Ecrit une erreur critique (niveau de log 1)
	 */
	public void error(String message, Throwable e, String source) {
		log(1, message, e, source);
	}

	/**
	 * Ecrit une erreur critique (niveau de log 1)
	 */
	public void error(String message, Throwable e) {
		error(message, e, "");
	}

	/**
	 * Ecrit une erreur critique (niveau de log 1)
	 */
	public void error(Throwable e) {
		error("", e, "");
	}    

	/**
	 * Ecrit une erreur critique (niveau de log 1)
	 */
	public void error(String message, String source) {
		log(1, message, source);
	}

	public void error(String message) {
		log(1, message, "");
	}

	/**
	 * Ecrit un warning (niveau de log 3)
	 */
	public void warn(String message, String source) {
		log(3, message, source);
	}

	/**
	 * Ecrit un warning (niveau de log 3)
	 */
	public void warn(String message) {
		log(3, message, "");
	}    

	/**
	 * Ecrit un warning (niveau de log 3)
	 */
	public void warn(String message, Throwable e, String source) {
		log(3, message, e, source);
	}

	/**
	 * Ecrit une information (niveau de log 6)
	 */
	public void info(String message, String source) {
		log(6, message, source);
	}

	/**
	 * Ecrit une info (niveau de log 6)
	 */
	public void info(String message, Throwable e, String source) {
		log(6, message, e, source);
	}

	/**
	 * Ecrit une info (niveau de log 6) 
	 */
	public void info(String message) {
		log(6, message, "");
	}

	public void fine(String message) {
		log(8, message, "");
	}
}