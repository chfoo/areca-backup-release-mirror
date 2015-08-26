package com.application.areca.launcher.gui.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
public class RefreshManager {
    protected Map monitors = new HashMap();
    
    public void registerRefreshable(Refreshable refreshable) {
        if (this.monitors.containsKey(refreshable.getRefreshableKey())) {
            throw new IllegalArgumentException("A monitor already exists for this Refreshable.");
        }
        
        this.monitors.put(refreshable.getRefreshableKey(), new RefreshMonitor(refreshable));
    }
    
    public void unregisterRefreshable(Refreshable refreshable) {      
        if (! this.monitors.containsKey(refreshable.getRefreshableKey())) {
            throw new IllegalArgumentException("No monitor found for this Refreshable.");
        }
        
        this.monitors.remove(refreshable.getRefreshableKey());
    }
    
    public void invalidate() {
        Iterator iter = this.monitors.values().iterator();
        while (iter.hasNext()) {
            RefreshMonitor monitor = (RefreshMonitor)iter.next();
            monitor.invalidate();
        }
    }
    
    public void getFocus(Refreshable refreshable) {
        RefreshMonitor monitor = (RefreshMonitor)this.monitors.get(refreshable.getRefreshableKey());
        monitor.getFocus();
    }
    
    public void lostFocus(Refreshable refreshable) {
        RefreshMonitor monitor = (RefreshMonitor)this.monitors.get(refreshable.getRefreshableKey());
        monitor.lostFocus();
    }
}
