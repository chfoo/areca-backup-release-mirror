package com.myJava.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.myJava.object.ToStringHelper;

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
public class Chronometer {
	private static ThreadLocal instance = new ThreadLocal() {
		protected Object initialValue() {
			return new Chronometer();
		}
	};
	
	private static short STARTED = 0;
	private static short STOPPED = 1;
	
	private Map items = new HashMap();
	
	public void start(String name) {
		ChronometerItem itm = (ChronometerItem)items.get(name);
		if (itm == null) {
			itm = new ChronometerItem();
			items.put(name, itm);
		}
		if (itm.state != STOPPED) {
			throw new IllegalStateException("Chronometer " + name + " already started");
		}
		itm.state = STARTED;
		itm.from = System.currentTimeMillis(); 
	}
	
	public void stop(String name) {
		ChronometerItem itm = (ChronometerItem)items.get(name);

		if (itm.state != STARTED) {
			throw new IllegalStateException("Chronometer " + name + " already stopped");
		}
		itm.state = STOPPED;
		itm.nb++;
		long d = (System.currentTimeMillis() - itm.from);
		
		itm.duration += d;
		
		itm.maxDuration = Math.max(itm.maxDuration, d);
		itm.minDuration = Math.min(itm.minDuration, d);
	}
	
	public void reset() {
		items.clear();
	}
	
	public String toString() {
		StringBuffer sb = ToStringHelper.init(this);
		Iterator iter = items.keySet().iterator();
		while (iter.hasNext()) {
			String name = (String)iter.next();
			ChronometerItem itm = (ChronometerItem)items.get(name);
			ToStringHelper.append("\n" + name, itm, sb);
		}
		return ToStringHelper.close(sb);
	}
	
	public static Chronometer instance() {
		return (Chronometer)instance.get();
	}
	
	private static class ChronometerItem {
		public short state = STOPPED;
		public long from = -1;
		public long nb = 0;
		public long maxDuration = 0;
		public long minDuration = Long.MAX_VALUE;
		public long duration = 0;
		
		public String toString() {
			StringBuffer sb = ToStringHelper.init(this);
			ToStringHelper.append("Measures", nb, sb);
			ToStringHelper.append("Total duration", duration, sb);
			ToStringHelper.append("Average duration", duration / Math.max(nb, 1), sb);
			ToStringHelper.append("Min duration", minDuration, sb);
			ToStringHelper.append("Max duration", maxDuration, sb);
			return ToStringHelper.close(sb);
		}
	}
}
