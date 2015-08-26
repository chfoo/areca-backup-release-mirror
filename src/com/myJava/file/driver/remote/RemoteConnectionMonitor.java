package com.myJava.file.driver.remote;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
public class RemoteConnectionMonitor {

	private static RemoteConnectionMonitor INSTANCE = new RemoteConnectionMonitor();
	private Map dataByConnection = new HashMap();
	
	public static RemoteConnectionMonitor getInstance() {
		return INSTANCE;
	}
	
	private static class RemoteConnectionData {
		public String proxyId;
		public String ownerWhenCreated;
		public boolean closed;
		
		public String toString() {
			return proxyId + " - " + (closed ? "closed":"open") + " - " + ownerWhenCreated;
		}
	}
	
	public synchronized void registerNewConnection(Object connection, AbstractProxy proxy) {
		RemoteConnectionData data = new RemoteConnectionData();
		data.closed = false;
		data.ownerWhenCreated = proxy.ownerId;
		data.proxyId = "" + proxy.uid;
		dataByConnection.put(connection, data);
	}
	
	public synchronized void registerCloseEvent(Object connection, AbstractProxy proxy) {
		RemoteConnectionData data = (RemoteConnectionData)dataByConnection.get(connection);
		if (data == null) {
			com.myJava.util.log.Logger.defaultLogger().warn("No data found for " + proxy.ownerId + " / " + proxy.uid);
			Logger.defaultLogger().info(toString());
		} else {
			data.closed = true;
		}
	}
	
	public synchronized String toString() {
		Iterator iter = dataByConnection.keySet().iterator();
		String ret = "";
		while (iter.hasNext()) {
			Object data = dataByConnection.get(iter.next());
			ret += "\n" + data.toString();
		}
		return ret;
	}
}
