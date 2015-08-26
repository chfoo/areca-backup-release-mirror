package com.myJava.file.metadata.posix.jni;

import java.util.HashMap;
import java.util.Map;

import com.myJava.file.metadata.posix.jni.wrapper.FileAccessWrapper;

/**
 * This class invokes C functions through JNI to handle group and user names/id transcoding.
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
public class UserGroupTranscoder {
	private static Map groupNames = new HashMap();
	private static Map userNames = new HashMap();
	private static Map groupIds = new HashMap();
	private static Map userIds = new HashMap();
	
	public static String getGroupName(int id) {
		Integer k = new Integer(id);
		String name = (String)groupNames.get(k);
		if (name == null) {
			name = FileAccessWrapper.getGroupName(id);

			groupNames.put(k, name);
			if (name != null) {
				groupIds.put(name, k);
			}
		}
		return name;
	}
	
	public static String getUserName(int id) {
		Integer k = new Integer(id);
		String name = (String)userNames.get(k);
		if (name == null) {
			name = FileAccessWrapper.getUserName(id);
			
			userNames.put(k, name);
			if (name != null) {
				userIds.put(name, k);
			}
		}
		return name;
	}
	
	public static int getGroupId(String name) {
		Integer id = (Integer)groupIds.get(name);
		if (id == null) {
			int iid = FileAccessWrapper.getGroupId(name);
			id = new Integer(iid);
			
			groupIds.put(name, id);
			if (iid != -1) {
				groupNames.put(id, name);
			}
		}
		return id.intValue();
	}
	
	public static int getUserId(String name) {
		Integer id = (Integer)userIds.get(name);
		if (id == null) {
			int iid = FileAccessWrapper.getUserId(name);
			id = new Integer(iid);
			
			userIds.put(name, id);
			if (iid != -1) {
				userNames.put(id, name);
			}
		}
		return id.intValue();
	}
}
