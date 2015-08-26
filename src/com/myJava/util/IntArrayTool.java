package com.myJava.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Int array utility class
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

public abstract class IntArrayTool {

    public static int sum(int[] values) {
        int total = 0;
        for (int i=0; i<values.length; i++) {
            total += values[i];
        }
        return total;
    }
    
    public static int max(int[] values) {
    	if (values == null || values.length == 0) {
    		return 0;
    	}
        int max = values[0];
        for (int i=1; i<values.length; i++) {
        	if (max < values[i]){
        		max = values[i];
        	}
        }
        return max;
    }
    
    public static int min(int[] values) {
    	if (values == null || values.length == 0) {
    		return 0;
    	}
        int min = values[0];
        for (int i=1; i<values.length; i++) {
        	if (min > values[i]){
        		min = values[i];
        	}
        }
        return min;
    }

    /**
     * Return the int[] content as a list
     */
    public static String getContentList(int[] values, String separator) {
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<values.length; i++) {
            sb.append(separator);
            sb.append(values[i]);
        }

        return sb.toString().substring(separator.length());
    }

    /**
     * Return the int[] content as a list
     */
    public static String getContentList(int[] values) {
        return IntArrayTool.getContentList(values, ",");
    }

    public static boolean contains(int[] values, int value) {
        for (int i=0; i<values.length; i++) {
            if (values[i] == value) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * List contains byte[]
     */
    public static List sort(List data, int nbDigits) {
    	int maxVal = 256;
    	List[] grouped = new List[maxVal];
    	
    	for (int d=nbDigits-1; d>=0; d--) {
        	for (int g=0; g<maxVal; g++) {
        		grouped[g] = new ArrayList();
        	}
        	
        	// Pass 1 : group by digit value
    		Iterator iter = data.iterator();
    		int digitValue;
    		int index = 0;
    		while (iter.hasNext()) {
    			byte[] number = (byte[])iter.next();
    			digitValue = number[d] + 128;
    			grouped[digitValue].add(new Integer(index));
    			index++;
    		}
    		
    		// Pass 2 : append data
    		List sorted = new ArrayList();
    		for (digitValue = 0; digitValue<maxVal; digitValue++) {
    			List toAdd = grouped[digitValue];
    			Iterator iter2 = toAdd.iterator();
    			while (iter2.hasNext()) {
    				int idx = ((Integer)iter2.next()).intValue();
    				sorted.add(data.get(idx));
    			}
    		}
    		
    		data = sorted;
    	}
    	
    	return data;
    }
    
    public static void main(String[] args) {
    	int maxNb = 1000000;
    	int wordLength = 20;
    	List data = new ArrayList();
    	
    	for (int i=0; i<maxNb; i++) {
    		byte[] d = new byte[wordLength];
    		for (int j=0; j<wordLength; j++) {
    			d[j] = (byte)(Util.getRnd() * 127);
    			//System.out.print(" " + d[j]);
    		}
    		data.add(d);
			//System.out.print("\n");
    	}
    	
    	System.out.println("");
    	long s = System.currentTimeMillis();
    	List ret = sort(data, wordLength);
    	System.out.println(System.currentTimeMillis() - s);
    	
    	for (int i=0; i<maxNb; i++) {
    		byte[] d = (byte[])ret.get(i);
    		for (int j=0; j<wordLength; j++) {
    			//System.out.print(" " + d[j]);
    		}
			//System.out.print("\n");
    	}
    	
    	long[] aa = new long[maxNb];
    	for (int i=0; i<maxNb; i++) {
    		aa[i] = Util.getRndLong();
    	}
    	
    	s = System.currentTimeMillis();
    	Arrays.sort(aa);
    	System.out.println(System.currentTimeMillis() - s);
    }
}