package com.myJava.object;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 
 * 
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
    
    public static int hash(int result, int[] argument) {
        if (argument == null) {
            return result;
        } else {
            int ret = result;
            for (int i=0; i<argument.length; i++) {
                ret = hash(ret, argument[i]);
            }
            return ret;
        }   
    }
    
    public static int hash(int result, byte[] argument) {
        if (argument == null) {
            return result;
        } else {
            int ret = result;
            for (int i=0; i<argument.length; i++) {
                ret = hash(ret, argument[i]);
            }
            return ret;
        }   
    }
	
	public static int hash(int result, long argument) {
		return (int)(result + BASE * argument);
	}
	
	public static int hash(int result, double argument) {
		return (int)(result + BASE * 10000 * argument);   // Not very accurate .... to be changed
	}
	
	public static int hash(int result, List argument) {
	    return hash(result, argument == null ? null : argument.iterator());
	}
    
    // The hashCode must be order-independant in the case of a Set.
    public static int hash(int result, Set argument) {
        if (argument == null) {
            return result;
        } else {
            int[] hashCodes = new int[argument.size()];
            Iterator iter = argument.iterator();
            for (int i=0; iter.hasNext(); i++) {
                hashCodes[i] = iter.next().hashCode();
            }
            Arrays.sort(hashCodes);
            return hash(result, hashCodes);
        }
    }
    
    public static int hash(int result, Object[] argument) {
        if (argument == null) {
            return result;
        } else {
            int ret = result;
            for (int i=0; i<argument.length; i++) {
                ret = hash(ret, argument[i]);
            }
            return ret;
        }
    }
    
    public static int hash(int result, Object argument) {
        if (argument == null) {
            return result;
        } else if (argument instanceof Object[]) {
            return hash(result, (Object[])argument);
        } else if (argument instanceof int[]) {
            return hash(result, (int[])argument);    
        } else if (argument instanceof byte[]) {
            return hash(result, (byte[])argument);              
        } else if (argument instanceof Set) {
            return hash(result, (Set)argument);
        } else if (argument instanceof List) {
            return hash(result, (List)argument);            
        } else {
            return hash(result, argument.hashCode());
        }
    }
    
    private static int hash(int result, Iterator iter) {
        if (iter == null) {
            return result;
        } else {
            int h = result;
            while (iter.hasNext()) {
                h = hash(h, iter.next());
            }
            return h;
        }
    }
}
