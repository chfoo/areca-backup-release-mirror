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
public class LogHelper {

    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yy-MM-dd HH:mm");
    public static String SEPARATOR = " - ";
    
    public static StringBuffer format(int level, String message, String source, boolean verbose) {   	
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
    
    private static StringBuffer formatVerbose(int level, String message, String source) {
    	StringBuffer sb = new StringBuffer();
        // Date de la log
        sb.append(DATE_FORMAT.format(new Date()));
        
        // Level de la log
        sb.append(SEPARATOR).append(resolveLevel(level));
        
        // Source de la log
        if (source != null && !source.equals("")) {
            sb.append(SEPARATOR).append(source);
        }
        
        // Message de la log
        if (message!= null && !message.equals("")) {
            sb.append(SEPARATOR).append(message);
        }
        
        // Log complete :
        return sb;
    }
    
    private static StringBuffer formatNonVerbose(int level, String message, String source) {
    	StringBuffer sb = new StringBuffer();
        // Level de la log
    	if (level <= 4) {
            sb.append(resolveLevel(level)).append(SEPARATOR);
    	}
        
        // Source de la log
        if (source != null && !source.equals("")) {
            sb.append(SEPARATOR).append(source);
        }
        
        // Message de la log
        if (message!= null && !message.equals("")) {
            sb.append(SEPARATOR).append(message);
        }
        
        // Log complete :
        return sb;
    }
    
    public static String resolveLevel(int l) {
    	switch (l) {
    		case 1:
    			return "ERROR";
    		case 2:
        		return "WARNING";
    		case 3:
        		return "INFO";
    		case 4:
        		return "DETAIL";
    		default:
        		return "FINEST";
    	}
    }
}
