package com.myJava.util.threadmonitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.myJava.util.Util;
import com.myJava.util.log.Logger;

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
public class ThreadMonitor {

	private Map items = new HashMap();
	private long delay = 10000; // Check every 10 seconds
	
	private static final ThreadMonitor INSTANCE;
	
	static {
		INSTANCE = new ThreadMonitor();
		Runnable rn = new Runnable() {
			public void run() {
				while (true) {
					synchronized(INSTANCE) {
						INSTANCE.checkItems();
						try {
							INSTANCE.wait(INSTANCE.delay);
						} catch (InterruptedException e) {
							Logger.defaultLogger().error(e);
						}
					}
				}
				
			}
		};
		
		Logger.defaultLogger().info("Starting Thread Monitor.");
		Thread th = new Thread(rn);
		th.setDaemon(true);
		th.setName("ThreadMonitor's working thread");
		th.start();
	}
	
	public static ThreadMonitor getInstance() {
		return INSTANCE;
	}
	
	public synchronized void register(ThreadMonitorItem item) {
		items.put(item.getKey(), item);
	}
	
	public synchronized ThreadMonitorItem remove(String key) {
		return (ThreadMonitorItem)items.remove(key);
	}
	
	private ThreadMonitorItem get(Object key) {
		return (ThreadMonitorItem)items.get(key);
	}
	
	public synchronized void notify(String key) {
		ThreadMonitorItem item = this.get(key);
		if (item == null) {
			Logger.defaultLogger().warn("No monitor found for key [" + key + "]");
		} else {
			item.setLastNotification(System.currentTimeMillis());
		}
	}
	
	private void checkItems() {
		Iterator iter = items.keySet().iterator();
		long time = System.currentTimeMillis();
		while (iter.hasNext()) {
			ThreadMonitorItem item = this.get(iter.next());
			if (! item.check(time)) {
				Logger.defaultLogger().warn("Caution : The following monitor seems to be locked : " + item.getKey() + " (" + item.getThread().getName() + ")");
				Util.logThreadInformations(item.getKey(), item.getThread());
				Util.logAllThreadInformations();
			}
		}
	}
}
