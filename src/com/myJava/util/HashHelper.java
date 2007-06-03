package com.myJava.util;

import java.util.Iterator;
import java.util.List;

/**
 * 
 * 
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2162742295696737000
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
public class HashHelper {

	private static int BASE = 29;
	
	public static int initHash(Object argument) {
		if (argument == null) {
			return 0;
		} else {
			return hash(0, argument.getClass());
		}
	}
	
	public static int hash(int result, boolean argument) {
		return hash(result, argument ? 1 : 0);
	}
	
	public static int hash(int result, int argument) {
		return hash(result, (long)argument);
	}
	
	public static int hash(int result, long argument) {
		return (int)(result + BASE * argument);
	}
	
	public static int hash(int result, double argument) {
		return (int)(result + BASE * 10000 * argument);   // Not very accurate .... to be changed
	}
	
	public static int hash(int result, List argument) {
	    Iterator iter = argument.iterator();
	    int h = result;
	    while (iter.hasNext()) {
	        h = hash(h, iter.next());
	    }
	    return h;
	}
	
	public static int hash(int result, Object argument) {
		if (argument == null) {
			return result;
		} else if (argument instanceof Object[]) {
			return hash(result, (Object[])argument);
		} else {
			return hash(result, argument.hashCode());
		}
	}
	
	private static int hash(int result, Object[] argument) {
		int ret = result;
		
		for (int i=0; i<argument.length; i++) {
			ret = hash(ret, argument[i]);
		}
		
		return ret;
	}
}
