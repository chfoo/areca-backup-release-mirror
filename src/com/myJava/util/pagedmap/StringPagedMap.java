package com.myJava.util.pagedmap;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2156529904998511409
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
public class StringPagedMap extends PagedMap {

    public StringPagedMap(File path, int pageSize) throws IOException {
        super(path, new StringComparator(), pageSize);
    }

    protected Object decode(String o) {
        return o;
    }

    protected String encode(Object o) {
        return (String)o;
    }

    protected static class StringComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            String s1 = (String)o1;
            String s2 = (String)o2;
            return s1.compareTo(s2);
        }
    }
    
    public static void main(String[] args) {
        try {
            StringPagedMap map = new StringPagedMap(new File("/home/olivier/Desktop/map"), 3);
            map.put("k10", "o10");
            map.put("k11", "o11");
            map.put("k12", "o12");
            map.put("k13", "o13");
            map.put("k14", "o14");
            map.put("k15", "o15");
            map.put("k16", "o16");
            map.put("k17", "o17");
            map.put("k18", "o18");
            map.put("k19", "o19");
            map.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        /*
        try {
            StringPagedMap map = new StringPagedMap(new File("/home/olivier/Desktop/map"), 3);
            System.out.println(map.get("k17"));
            System.out.println(map.get("k16"));
            System.out.println(map.get("k15"));
            System.out.println(map.get("k19"));
            System.out.println(map.get("k10"));
            System.out.println(map.get("k1011"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
        
        try {
            StringPagedMap map = new StringPagedMap(new File("/home/olivier/Desktop/map"), 3);
            Iterator iter = map.keyIterator();
            while (iter.hasNext()) {
                String key = (String)iter.next();
                String value = (String)map.get(key);
                System.out.println("" + key + " = " + value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
