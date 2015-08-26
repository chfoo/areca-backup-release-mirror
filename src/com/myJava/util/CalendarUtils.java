package com.myJava.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

/**
 * Calendar utility class
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
public class CalendarUtils {
    
    
    public static final String DATE_SEPARATOR = "_";
    public static final String TIME_SEPARATOR = "h";  
    public static final String FULL_TIME_SEPARATOR = "-";    
    
    /**
     * expected date format : YYYY MM DD [HH] [mm] [ss] [ms]
     * <BR>accepted separators : /-_ HhMm:.,; 
     */
    public static GregorianCalendar resolveDate(String date, GregorianCalendar defaultValue) {
        if (date == null) {
            return defaultValue;
        }
        
        try {
            StringTokenizer stt = new StringTokenizer(date, "/-_ HhMm:.,;");
            int year = Integer.parseInt(stt.nextToken());
            int month = Integer.parseInt(stt.nextToken()) - 1;
            int day = Integer.parseInt(stt.nextToken());
            
            int hour = 0;
            int minute = 0;
            int second = 0;
            int millisecond = 0;
            if (stt.hasMoreTokens()) {
                hour = Integer.parseInt(stt.nextToken());
            }
            
            if (stt.hasMoreTokens()) {
                minute = Integer.parseInt(stt.nextToken());
            }
            
            if (stt.hasMoreTokens()) {
                second = Integer.parseInt(stt.nextToken());
            }            

            if (stt.hasMoreTokens()) {
                millisecond = Integer.parseInt(stt.nextToken());
            }            
            
            GregorianCalendar cal = new GregorianCalendar();
            cal.set(year, month, day, hour, minute, second);
            cal.set(Calendar.MILLISECOND, millisecond);
            return cal;
            
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    
    /**
     * Return [HH]h[mm] 
     */
    public static String getTimeToString(Calendar argCal) {
    	Calendar cal = argCal;
        if (cal == null) {
            cal = new GregorianCalendar();
        }        
        
        String date = "";
        if (cal.get(Calendar.HOUR_OF_DAY) < 10) {
            date += "0";
        }        
        date += cal.get(Calendar.HOUR_OF_DAY) + TIME_SEPARATOR;

        if (cal.get(Calendar.MINUTE) < 10) {
            date += "0";
        }        
        date += cal.get(Calendar.MINUTE);
        
        return date;
    }
    
    /**
     * Return [HH]h[mm]-[ss]-[ms]
     */
    public static String getFullTimeToString(Calendar argCal) {
    	Calendar cal = argCal;
        if (cal == null) {
            cal = new GregorianCalendar();
        }        
        
        String date = getTimeToString(cal) + FULL_TIME_SEPARATOR;
        if (cal.get(Calendar.SECOND) < 10) {
            date += "0";
        }        
        date += cal.get(Calendar.SECOND) + FULL_TIME_SEPARATOR;

        if (cal.get(Calendar.MILLISECOND) < 10) {
            date += "0";
        }
        if (cal.get(Calendar.MILLISECOND) < 100) {
            date += "0";
        }                
        date += cal.get(Calendar.MILLISECOND);
        
        return date;
    }    
    
    /**
     * Return YYYYMMDD 
     */
    public static String getDateToString(Calendar argCal) {
    	Calendar cal = argCal;
        if (cal == null) {
            cal = new GregorianCalendar();
        }
        
        String date = "" + cal.get(Calendar.YEAR) + DATE_SEPARATOR;
        if (cal.get(Calendar.MONTH) + 1 < 10) {
            date += "0";
        }
        date += (cal.get(Calendar.MONTH) + 1) + DATE_SEPARATOR;
        if (cal.get(Calendar.DAY_OF_MONTH) < 10) {
            date += "0";
        }        
        date += cal.get(Calendar.DAY_OF_MONTH);
        
        return date;
    }
    
    /**
     * Return YYYYMMDD [HH]h[mm]-[ss]-[ms]
     */
    public static String getFullDateToString(Calendar cal) {
        return getDateToString(cal) + " " + getFullTimeToString(cal);
    }
    
    
    public static String getDateToString() {
        return getDateToString(null);
    }

    public static String getTimeToString() {
        return getTimeToString(null);
    } 
    
    public static Calendar removeTime(Calendar cal) {
    	return new GregorianCalendar(cal.get(cal.YEAR), cal.get(cal.MONTH), cal.get(cal.DAY_OF_MONTH));
    }
}
