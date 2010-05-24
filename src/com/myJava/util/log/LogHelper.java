package com.myJava.util.log;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2010, Olivier PETRUCCI.

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
public class LogHelper {

    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yy-MM-dd HH:mm");
    public static String SEPARATOR = " - ";
    
    public static String format(int level, String message, String source, boolean verbose) {   	
    	return verbose ?
    			formatVerbose(level, message, source)
    			: formatNonVerbose(level, message, source);
    }
    
    public static String formatException(Throwable e) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		e.printStackTrace(ps);
		ps.close();
		return new String(baos.toByteArray());
    }
    
    private static String formatVerbose(int level, String message, String source) {
        // Date de la log
        String logDate = "" + DATE_FORMAT.format(new Date());
        
        // Level de la log
        String logLev = SEPARATOR + resolveLevel(level);
        
        // Source de la log
        String logSource = "";
        if (source != null && !source.equals("")) {
            logSource = SEPARATOR + source;
        }
        
        // Message de la log
        String logMess = "";
        if (message!= null && !message.equals("")) {
            logMess = SEPARATOR + message;
        }
        
        // Log complete :
        return logDate + logLev + logSource + logMess;
    }
    
    private static String formatNonVerbose(int level, String message, String source) {
        // Level de la log
        String logLev = (level <= 4 ? resolveLevel(level) + SEPARATOR : "");
        
        // Source de la log
        String logSource = "";
        if (source != null && !source.equals("")) {
            logSource = source + SEPARATOR;
        }
        
        // Message de la log
        String logMess = "";
        if (message!= null && !message.equals("")) {
            logMess = message;
        }
        
        // Log complete :
        return logLev + logSource + logMess;
    }
    
    public static String resolveLevel(int l) {
    	if (l == 1) {
    		return "ERROR";
    	} else if (l <=4) {
    		return "WARNING";
    	} else if (l <=7) {
    		return "INFO";
    	} else {
    		return "DETAIL";
    	}
    }
}
