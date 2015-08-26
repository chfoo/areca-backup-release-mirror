package com.application.areca.context;

import java.util.ArrayList;
import java.util.Iterator;

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
public class MaxCapacityList {
	private static final int DEFAULT_MAX_CAPACITY = 200;
	
	private int currentSize = 0;
	private int maxSize = DEFAULT_MAX_CAPACITY;
	private boolean saturated = false;
	private ArrayList content = new ArrayList();
	
	public void add(String item) {
		if (currentSize < maxSize) {
			content.add(item);
			currentSize++;
		} else {
			saturated = true;
		}
	}
	
	public boolean isSaturated() {
		return saturated;
	}
	
	public Iterator iterator() {
		return content.iterator();
	}
	
	public boolean isEmpty() {
		return content.isEmpty();
	}
	
	public void clear() {
		content.clear();
		currentSize = 0;
		saturated = false;
	}
	
	public int size() {
		return currentSize;
	}
}
