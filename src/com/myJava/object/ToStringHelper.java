package com.myJava.object;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper for "toString" methods
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2011, Olivier PETRUCCI.

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
public class ToStringHelper {

    public static StringBuffer init(Object o) {
        StringBuffer sb = new StringBuffer();
        if (o == null) {
            sb.append("<null>");
        } else {
            sb.append("[").append(o.getClass().getName());
        }
        return sb;
    }
    
    public static void append(String name, Object o, StringBuffer sb) {
        preAppend(name, sb);
        normalize(o, sb);
        postAppend(sb);
    }
    
    public static void append(String name, String s, StringBuffer sb) {
        preAppend(name, sb);
        normalize(s, sb);
        postAppend(sb);
    }
    
    public static void append(String name, Set s, StringBuffer sb) {
        if (s == null) {
            appendNull(name, sb);
        } else {
            append(name, s.iterator(), sb);
        }
    }
    
    public static void append(String name, List l, StringBuffer sb) {
        if (l == null) {
            appendNull(name, sb);
        } else {
            append(name, l.iterator(), sb);
        }
    }
    
    public static void append(String name, Map m, StringBuffer sb) {
        if (m == null) {
            appendNull(name, sb);
        } else {
            preAppend(name, sb);
            serialize(m, sb);
            postAppend(sb);
        }
    }
    
    public static void serialize(Map m, StringBuffer sb) {
        if (m != null) {
            sb.append("{");
            Iterator iter = m.entrySet().iterator();
            boolean first = true;
            while (iter.hasNext()) {
                if (! first) {
                    sb.append(", ");
                }
                first = false;
                
                Map.Entry entry = (Map.Entry)iter.next();
                sb.append("[");
                normalize(entry.getKey(), sb);
                sb.append("] = [");
                normalize(entry.getValue(), sb);
                sb.append("]");
            }
            sb.append("}");
        }
    }
    
    public static String serialize(Object[] o) {
        if (o == null) {
            return "<null>";
        } else {
            StringBuffer b = new StringBuffer();
            b.append('{');
            for (int i=0; i<o.length; i++) {
                if (i != 0) {
                    b.append(", ");
                }
                if (o[i] != null) {
                    b.append(String.valueOf(o[i]));
                } else {
                    b.append("<null>");
                }
            }
            b.append('}');
            return b.toString();
        }
    }
    
    private static void appendNull(String name, StringBuffer sb) {
        preAppend(name, sb);
        normalize(null, sb);
        postAppend(sb);
    }
    
    private static void append(String name, Iterator iter, StringBuffer sb) {
        preAppend(name, sb);
        if (iter == null) {
            normalize(iter, sb);
        } else {
            sb.append("{");
            boolean first = true;
            while (iter.hasNext()) {
                if (! first) {
                    sb.append(", ");
                }
                first = false;
                normalize(iter.next(), sb);   
            }
            sb.append("}");
        }
        postAppend(sb);
    }
    
    public static void append(String name, boolean b, StringBuffer sb) {
        preAppend(name, sb);
        sb.append(b);
        postAppend(sb);
    }
    
    public static void append(String name, int i, StringBuffer sb) {
        preAppend(name, sb);
        sb.append(i);
        postAppend(sb);
    }
    
    public static void append(String name, long l, StringBuffer sb) {
        preAppend(name, sb);
        sb.append(l);
        postAppend(sb);
    }
    
    public static void append(String name, double d, StringBuffer sb) {
        preAppend(name, sb);
        sb.append(d);
        postAppend(sb);
    }
    
    public static String close(StringBuffer sb) {
        return sb.append("]").toString();
    }
    
    private static void preAppend(String name, StringBuffer sb) {
        sb.append(" - ");
        sb.append(name).append(" = ");
    }
    
    private static void postAppend(StringBuffer sb) {
        sb.append(" ");
    }
    
    private static void normalize(Object o, StringBuffer sb) {
        if (o == null) {
            sb.append("<null>");
        } else {
            sb.append(o.toString());
        }
    }
    
    private static void normalize(String s, StringBuffer sb) {
        if (s == null) {
            sb.append("<null>");
        } else {
            sb.append("\"").append(s).append("\"");
        }
    }
    
    public static String serialize(byte[] dt) {
        String ret = "";
        for (int i=0; i<dt.length; i++) {
            ret += " " + dt[i];
        }
        return ret;
    }
}
