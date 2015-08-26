package com.myJava.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

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
public class DateTimeHelper {
	public static int D_MONDAY = 0;
	public static int D_TUESDAY = 1;
	public static int D_WEDNESDAY = 2;
	public static int D_THURSDAY = 3;
	public static int D_FRIDAY = 4;
	public static int D_SATURDAY = 5;
	public static int D_SUNDAY = 6;

	private static int ZERO_YEAR = 2000;
	private static int ZERO_DOW = 5;
	private static GregorianCalendar ZERO =  new GregorianCalendar(ZERO_YEAR, 0, 1);
	
	private static int[] DAYS_PER_YEAR = {366,365,365,365,366,365,365,365,366,365,365,365,366,365,365,365,366,365,365,365,366,365,365,365,366,365,365,365};
	private static int[] CUMUL_DAYS_PER_YEAR = {0,366,731,1096,1461,1827,2192,2557,2922,3288,3653,4018,4383,4749,5114,5479,5844,6210,6575,6940,7305,7671,8036,8401,8766,9132,9497,9862,10227};
	

	public static Calendar translateToCalendar(int day) {
		if (day == 0) {
			return null;
		} else {
			Calendar ret = (Calendar)ZERO.clone();
			ret.add(ZERO.DAY_OF_MONTH, day);
			return ret;
		}
	}

	public static Calendar translateToCalendar(long millisec) {
		if (millisec == 0) {
			return null;
		} else {
			long nbDays = (long)(millisec / 86400000.0);
			
			Calendar ret = translateToCalendar((int)nbDays);
			
			long dayMillisec = millisec - nbDays * 86400000;
			
			ret.add(ZERO.MILLISECOND, (int)dayMillisec);
			return ret;
		}
	}
	
	/**
	 * Translate the calendar to a number of millisec since 01/01/2000
	 */
	public static long translateToMilliseconds(Calendar calendar)  {
		return 
				translateToDayNumber(calendar) * 86400000
				+ calendar.get(Calendar.HOUR_OF_DAY) * 3600000 
				+ calendar.get(Calendar.MINUTE) * 60000 
				+ calendar.get(Calendar.SECOND) * 1000 
				+ calendar.get(Calendar.MILLISECOND);
	}
	
	public static long getNowMilliseconds() {
		return translateToMilliseconds(new GregorianCalendar());
	}
	
	public static long AddDays(long initialDateMilliSeconds, int dayNumber) {
		return initialDateMilliSeconds + dayNumber * 24 * 3600 * 1000;
	}
	
	public static int getDayOfWeek(int day) {
		return (day+ZERO_DOW)%7;
	}
	
	/**
	 * 
	 * @param y
	 * @param m Month number, 1=Jan, 2=Feb, ..., 12=Dec
	 * @param d
	 * @return
	 */
	public static int translateToDayNumber(int y, int m, int d) {
		Calendar cal = new GregorianCalendar(y, m-1, d);
		return translateToDayNumber(cal);
	}
	
	/**
	 * Translate the calendar to a number of days since 01/01/2000
	 */
	public static int translateToDayNumber(Calendar cal) {
		if (cal == null) {
			return 0;
		} else {
			int y = cal.get(cal.YEAR) - ZERO_YEAR;
			
			int nbD = CUMUL_DAYS_PER_YEAR[y];
			nbD += cal.get(Calendar.DAY_OF_YEAR) - 1;
			
			return nbD;
		}
	}
	
	public static String formatDateTime(Calendar cal) {
		StringBuffer sb = new StringBuffer();
		sb
			.append(cal.get(cal.YEAR))
			.append("-")
			.append(1+cal.get(cal.MONTH))
			.append("-")			
			.append(cal.get(cal.DAY_OF_MONTH))
			.append(":")			
			.append(cal.get(cal.HOUR_OF_DAY))
			.append(":")			
			.append(cal.get(cal.MINUTE))
			.append(":")			
			.append(cal.get(cal.SECOND))
			.append(":")			
			.append(cal.get(cal.MILLISECOND))
			.append("|")			
			.append(translateToMilliseconds(cal))						
		;			
		return sb.toString();
	}
	
	public static String formatDate(Calendar cal) {
		StringBuffer sb = new StringBuffer();
		sb
			.append(cal.get(cal.YEAR))
			.append("-")
			.append(1+cal.get(cal.MONTH))
			.append("-")			
			.append(cal.get(cal.DAY_OF_MONTH))
			.append("|")			
			.append(translateToDayNumber(cal))						
		;			
		return sb.toString();
	}
	
	public static String formatDate(int date) {
		return formatDate(translateToCalendar(date));
	}
	
	public static String formatDateTime(long dateTime) {
		return formatDate(translateToCalendar(dateTime));
	}
}
