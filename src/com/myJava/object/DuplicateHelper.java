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
public class DuplicateHelper {
    
    public static Duplicable duplicate(Duplicable o) {
        if (o == null) {
            return null;
        } else {
            return o.duplicate();            
        }
    }

    public static List duplicate(List l) {
        return duplicate (l, true);
    }
    
    public static List duplicate(List l, boolean deepCloning) {
        if (l == null) {
            return null;
        } else {
            try {
                List list = (List)l.getClass().newInstance();
                Iterator iter = l.iterator();
                while (iter.hasNext()) {
                    if (deepCloning) {
                        list.add(duplicate(iter.next()));
                    } else {
                        list.add(iter.next());                        
                    }
                }
                return list;
            } catch (InstantiationException e) {
                throw new IllegalArgumentException(e.getMessage());
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }
    
    public static Set duplicate(Set l) {
        return duplicate (l, true);
    }
    
    public static Set duplicate(Set l, boolean deepCloning) {
        if (l == null) {
            return null;
        } else {
            try {
                Set list = (Set)l.getClass().newInstance();
                Iterator iter = l.iterator();
                while (iter.hasNext()) {
                    if (deepCloning) {
                        list.add(duplicate(iter.next()));
                    } else {
                        list.add(iter.next());                        
                    }
                }
                return list;
            } catch (InstantiationException e) {
                throw new IllegalArgumentException(e.getMessage());
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }
    
    public static int[] duplicate(int[] array) {
        if (array == null) {
            return null;
        } else {
            int[] clone = new int[array.length];
            for (int i=0; i<array.length; i++) {
                clone[i] = array[i];
            }
            return clone;
        }
    }
    
    public static boolean[] duplicate(boolean[] array) {
        if (array == null) {
            return null;
        } else {
        	boolean[] clone = new boolean[array.length];
            for (int i=0; i<array.length; i++) {
                clone[i] = array[i];
            }
            return clone;
        }
    }

    public static Object duplicate(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof Duplicable) {
            return ((Duplicable)o).duplicate();
        } else if (o instanceof List) {
            return duplicate((List)o);
        } else if (o instanceof Set) {
            return duplicate((Set)o);       
        } else if (o instanceof int[]) {
            return duplicate((int[])o);      
        } else if (o instanceof boolean[]) {
            return duplicate((boolean[])o);     
        } else {
            throw new IllegalArgumentException(o.toString() + " is not cloneable.");
        }
    }
}
