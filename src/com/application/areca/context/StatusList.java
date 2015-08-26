package com.application.areca.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * 
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
public class StatusList implements Serializable {
	private static final long serialVersionUID = -8243222132827394017L;
	
	public static String KEY_ARCHIVE_CHECK = "Check Archive";
	public static String KEY_BACKUP = "Backup";
	public static String KEY_INITIATE_MERGE = "Initiate Merge";
	public static String KEY_FINALIZE_MERGE = "Finalize Merge";
	public static String KEY_SIMULATE = "Simulate Backup";
	public static String KEY_DELETE = "Delete Archives";
	public static String KEY_MERGED_DELETE = "Delete Merged Archive";
	public static String KEY_PREPARE = "Pre/Post Backup Operations";
	
	private ArrayList content = new ArrayList();
	private boolean hasError = false;
	
	public Iterator iterator() {
		return content.iterator();
	}
	
	public void addItem(StatusItem item) {
		content.add(item);
		hasError = hasError || item.isHasErrors();
	}
	
	public void addItem(String key) {
		StatusItem itm = new StatusItem();
		itm.setKey(key);
		addItem(itm);
	}
	
	public void addItem(String key, String errorMessage) {
		StatusItem itm = new StatusItem();
		itm.setKey(key);
		itm.setErrorMessage(errorMessage);
		addItem(itm);
	}
	
	public boolean hasItem(String key) {
		Iterator iter = this.iterator();
		while (iter.hasNext()) {
			StatusItem itm = (StatusItem)iter.next();
			if (itm.getKey().equals(key)) {
				return true;
			}
		}
		return false;
	}
	
	public int size() {
		return content.size();
	}
	
	public boolean hasError() {
		return hasError;
	}
	
	public boolean hasError(String key) {
		Iterator iter = this.iterator();
		while (iter.hasNext()) {
			StatusItem itm = (StatusItem)iter.next();
			if (itm.getKey().equals(key) && itm.isHasErrors()) {
				return true;
			}
		}
		return false;
	}
}
