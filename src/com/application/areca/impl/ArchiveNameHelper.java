package com.application.areca.impl;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.myJava.util.Util;

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
public class ArchiveNameHelper {
	public static final char SUFFIX_SEPARATOR = '_';
	
	public static final String B_YEAR_2 = "%YY%";
	public static final String B_YEAR_4 = "%YYYY%";
	public static final String B_MONTH = "%MM%";
	public static final String B_DAY = "%DD%";
	public static final String B_HOUR = "%hh%";
	public static final String B_MINUTE = "%mm%";
	
	public static final String[] TOKENS = new String[] {B_DAY, B_HOUR, B_MINUTE, B_MONTH, B_YEAR_2, B_YEAR_4};
	
	private static Map getTokenMap(String val) {
		HashMap map = new HashMap();
		for (int i=0; i<TOKENS.length; i++) {
			map.put(TOKENS[i], val);
		}
		return map;
	}
	
	private static String format(int n) {
		if (n < 10) {
			return "0" + String.valueOf(n);
		} else {
			return String.valueOf(n);
		}
	}
	
	private static Map getTokenMap(GregorianCalendar cal) {
		HashMap map = new HashMap();
		
		int year = cal.get(GregorianCalendar.YEAR);
		int month = cal.get(GregorianCalendar.MONTH) + 1;
		int day = cal.get(GregorianCalendar.DAY_OF_MONTH);
		int hour = cal.get(GregorianCalendar.HOUR_OF_DAY);
		int min = cal.get(GregorianCalendar.MINUTE);
		
		map.put(B_DAY, format(day));
		map.put(B_HOUR, format(hour));
		map.put(B_MINUTE, format(min));
		map.put(B_MONTH, format(month));
		map.put(B_YEAR_2, format(year - 2000));
		map.put(B_YEAR_4, format(year));
		
		return map;
	}
	
	private static String parseName(String pattern, Map map) {
		String name = pattern;
		Iterator iter = map.keySet().iterator();
		while (iter.hasNext()) {
			String token = (String)iter.next();
			name = Util.replace(name, token, (String)map.get(token));
		}
		
		return name;
	} 
	

	public static String parseName(String pattern, GregorianCalendar cal) {
		Map map = cal == null ? getTokenMap("") : getTokenMap(cal);
		return parseName(pattern, map);
	}
	
	public static boolean matchPattern(String name, String pattern, String extension) {	
		if (! name.endsWith(extension)) {
			return false;
		} else {
			name = name.substring(0, name.length() - extension.length());
		}
		
		HashMap map = new HashMap();
		for (int i=0; i<TOKENS.length; i++) {
			int l = TOKENS[i].length() - 2;
			String rep = "";
			for (int j=0; j<l; j++) {
				rep += '%';
			}
			map.put(TOKENS[i], rep);
		}
		
		String parsed = parseName(pattern, map);
		
		// Suffix-tolerant check
		if (parsed.length() > name.length()) {
			return false;
		} else if (parsed.length() < name.length()) {
			String suffix = name.substring(parsed.length());
			if (suffix.charAt(0) != SUFFIX_SEPARATOR) {
				return false;
			} else {
				for (int i=1; i<suffix.length(); i++) {
					if (! isNumber(suffix.charAt(i))) {
						return false;
					}
				}
			}
		}
		
		for (int i=0; i<parsed.length(); i++) {
			char pc = parsed.charAt(i);
			char nc = name.charAt(i);
			
			if (pc != nc && (pc != '%' || ! isNumber(nc))) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean isNumber(char c) {
		return c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9';
	}
}
