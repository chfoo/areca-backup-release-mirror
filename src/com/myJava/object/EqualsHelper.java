package com.myJava.object;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
public class EqualsHelper {

    public static boolean equals(Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return true;
		} else if (o1 == null || o2 == null) {
			return false;
		} else {
		    return o1.equals(o2);
		}
    }
	
	public static boolean equals(byte o1, byte o2) {
	    return o1 == o2;
	}
	
	public static boolean equals(char o1, char o2) {
	    return o1 == o2;
	}
	
	public static boolean equals(boolean o1, boolean o2) {
	    return o1 == o2;
	}
	
	public static boolean equals(int o1, int o2) {
	    return o1 == o2;
	}
	
	public static boolean equals(double o1, double o2) {
	    return o1 == o2;
	}
	
	public static boolean equals(long o1, long o2) {
	    return o1 == o2;
	}
	
	public static boolean equals(float o1, float o2) {
	    return o1 == o2;
	}
	
	public static boolean equals(List o1, List o2) {
	    if (! checkClasses(o1, o2)) {
	        return false;
	    } else if (o1.size() != o2.size()) {
	        return false;
	    } else {
	        Iterator iter1 = o1.iterator();
	        Iterator iter2 = o2.iterator();
	        
	        while (iter1.hasNext()) {
	            if (! equals(iter1.next(), iter2.next())) {
	                return false;
	            }
	        }
	        return true;
	    }
	}
	
    public static boolean equals(byte[] o1, byte[] o2) {
        if (o1.length != o2.length) {
            return false;
        } else {
            for (int i=0; i<o1.length; i++) {
                if (o1[i] != o2[i]) {
                    return false;
                }
            }
            return true;
        }
    }
    
    public static boolean equals(int[] o1, int[] o2) {
        if (o1.length != o2.length) {
            return false;
        } else {
            for (int i=0; i<o1.length; i++) {
                if (o1[i] != o2[i]) {
                    return false;
                }
            }
            return true;
        }
    }
    
    // Equals must be order-independant in case of a Set.
    public static boolean equals(Set o1, Set o2) {
        if (! checkClasses(o1, o2)) {
            return false;
        } else if (o1.size() != o2.size()) {
            return false;
        } else {
            Iterator iter1 = o1.iterator();
            while (iter1.hasNext()) {
                if (! o2.contains(iter1.next())) {
                    return false;
                }
            }
            return true;
        }
    }
	
	public static boolean checkNulls(Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return true;
		} else if (o1 == null || o2 == null) {
			return false;
		} else {
		    return true;
		}
	}
	
	/**
	 * Checks that these object either are both null or are instance of EXACTLY the same class
	 */
	public static boolean checkClasses(Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return true;
		} else if (o1 == null || o2 == null) {
			return false;
		} else {
		    return o1.getClass().equals(o2.getClass());
		}
	}
}
