package com.myJava.util;

import java.util.Iterator;
import java.util.List;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 3274863990151426915
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
public class DuplicateHelper {
    
    public static PublicClonable duplicate(PublicClonable o) {
        if (o == null) {
            return null;
        } else {
            return o.duplicate();            
        }
    }

    public static List duplicate(List l) {
        if (l == null) {
            return null;
        } else {
            try {
                List list = (List)l.getClass().newInstance();
                Iterator iter = l.iterator();
                while (iter.hasNext()) {
                    list.add(duplicate(iter.next()));
                }
                return list;
            } catch (InstantiationException e) {
                throw new IllegalArgumentException(e.getMessage());
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }

    public static Object duplicate(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof PublicClonable) {
            return ((PublicClonable)o).duplicate();
        } else if (o instanceof List) {
            return duplicate((List)o);
        } else {
            throw new IllegalArgumentException(o.toString() + " is not cloneable.");
        }
    }
}
