package com.myJava.util.log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -4899974077672581254
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
public class LogHelper {

    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yy-MM-dd HH:mm");
    
    public static String format(int level, String message, String source) {
        // Date de la log
        String logDate = "" + DATE_FORMAT.format(new Date());
        
        // Level de la log
        String logLev = " - " + level;
        
        // Source de la log
        String logSource = "";
        if (source != null && !source.equals("")) {
            logSource = " - " + source;
        }
        
        // Message de la log
        String logMess = "";
        if (message!= null && !message.equals("")) {
            logMess = " - " + message;
        }
        
        // Log complète :
        return logDate + logLev + logSource + logMess;
    }
}
