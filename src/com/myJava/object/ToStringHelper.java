package com.myJava.object;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.NumberFormat;
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
public class ToStringHelper {
	private static NumberFormat nf = NumberFormat.getNumberInstance();
	
	static {
		nf.setMinimumFractionDigits(3);
		nf.setMaximumFractionDigits(3);
	}
	
    public static StringBuffer init(Object o) {
        StringBuffer sb = new StringBuffer();
        if (o == null) {
            sb.append("<null>");
        } else {
            sb.append("[").append(o.getClass().getSimpleName());
        }
        return sb;
    }
    
    /**
     * Low performance tostring default implementation
     * @param o
     * @return
     */
    public static String defaultToString(Object o, Class refClass) {
    	if (o == null) {
    		return "<null>";
    	} else {
    		Method[] methods = refClass.getMethods();
    		StringBuffer sb = init(o);
    		for (int i=0; i<methods.length; i++) {
    			Method m = methods[i];
    			String prop = getPropertyName(m);
    			if (prop != null) {
    				try {
    					Object value = m.invoke(o, new Object[0]);
    					append(prop, value, sb);
    				} catch (Exception ignored) {
    				}
    			}
    		}
    		return close(sb);
    	}
    }
    
    /**
     * returns null if not a getter, property name otherwise
     * @param m
     * @return
     */
    private static String getPropertyName(Method m) {
    	if (Modifier.isStatic(m.getModifiers())) {
    		return null;
    	}
    	
    	String name = m.getName();
    	int idx = -1;
    	if (name.startsWith("get")) {
    		idx = 3;
    	} else if (name.startsWith("is")) {
    		idx = 2;
    	} else {
    		return null;
    	}
    	
    	char c = name.charAt(idx);
    	if (c < 65 || c > 90) {
    		return null;
    	}
    	
    	if (m.getParameterTypes().length != 0) {
    		return null;
    	}

    	if (m.getReturnType().equals(Void.TYPE)) {
    		return null;
    	}
    	
    	if (name.equals("getClass")) {
    		return null;
    	}
    	
    	return name.substring(idx);
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
                sb.append("]=[");
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
        sb.append(formatDouble(d));
        postAppend(sb);
    }
    
    public static String formatDouble(double d) {
    	return nf.format(d);
    }
    
    public static String close(StringBuffer sb) {
    	String ret = sb.toString().trim();
    	return ret + "]";
    }
    
    private static void preAppend(String name, StringBuffer sb) {
        sb.append(" - ");
        sb.append(name).append("=");
    }
    
    private static void postAppend(StringBuffer sb) {
        //sb.append(" ");
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
