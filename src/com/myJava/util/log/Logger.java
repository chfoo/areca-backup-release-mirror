package com.myJava.util.log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.myJava.configuration.FrameworkConfiguration;


/**
 * Classe permettant de gérer la log applicative
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 3732974506771028333
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

    /**
     * Niveau de log
     */
    private int logLevel;

    /**
     *  Logger par défaut.
     */
    private static Logger defaultLogger = new Logger();
    
    private List processors = new ArrayList();

    /**
     * Constructeur.
     */
    public Logger() {
        this.setLogLevel(FrameworkConfiguration.getInstance().getLogLevel());
        this.addProcessor(new ConsoleLogProcessor());
    }
    
    public void removeAllProcessors() {
        this.processors.clear();
    }

    /**
     * Indique le logger par défaut de la classe
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
     * <BR>Tout appel de priorité inférieure à la priorité spécifiée ne sera pas
     * loggé
     */
    public void setLogLevel(int l) {
        logLevel = l;
    }
    
    public void addProcessor(LogProcessor proc) {
        this.processors.add(proc);
    }

    /**
     * <BR>Retourne true en cas de succès, false en cas d'échec.
     */
    public boolean clearLog() {
        Iterator iter = this.processors.iterator();
        boolean b = true;
        while(iter.hasNext()) {
            LogProcessor proc = (LogProcessor)iter.next();
            b &= proc.clearLog();
        }
        return b;
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
        Iterator iter = this.processors.iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (c.isAssignableFrom(o.getClass())) {
                iter.remove();
            }
        }
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
    
    /**
     * Ecrit la log en vérifiant le niveau précisé.
     */
    private void log(int level, String message, Throwable e, String source) {
        if (level <= logLevel) {
            Iterator iter = this.processors.iterator();
            while(iter.hasNext()) {
                LogProcessor proc = (LogProcessor)iter.next();
                proc.log(level, message, e, source);
            }
        }
    }

    /**
     * Ecrit une log générique
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