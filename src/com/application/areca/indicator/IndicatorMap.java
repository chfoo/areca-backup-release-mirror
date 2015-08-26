package com.application.areca.indicator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Map of TargetIndicators 
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
public class IndicatorMap {

    private Map internalMap = new HashMap(10);
    
    public IndicatorMap() {
        super();
    }

    public Indicator getIndicator(Integer id) {
        return (Indicator)internalMap.get(id);
    }
    
    public void addIndicator(Indicator indicator) {
        this.internalMap.put(indicator.getId(), indicator);
    }
    
    public void clear() {
        this.internalMap.clear();
    }
    
    public Iterator keyIterator() {
        return this.internalMap.keySet().iterator();
    }
    
    public Integer[] getSortedIndicatorKeys() {
        Integer[] keys = (Integer[])this.internalMap.keySet().toArray(new Integer[0]);
        Arrays.sort(keys);
        
        return keys;
    }
    
    public boolean isEmpty() {
        return this.internalMap.isEmpty();
    }
}
