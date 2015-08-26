package com.myJava.file.iterator;

import java.util.Comparator;


/**
 * Compare two file paths (instanciated as Strings)
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
public class FilePathComparator implements Comparator {
	private static final FilePathComparator INSTANCE = new FilePathComparator();
	
	public static FilePathComparator instance() {
		return INSTANCE;
	}
	
	public int compare(Object arg0, Object arg1) {
		String s0 = (String)arg0;
		String s1 = (String)arg1;

		int i0 = s0.indexOf('/', 1);
		int i1 = s1.indexOf('/', 1);

		String ss0 = s0;
		if (i0 != -1) {
			ss0 = s0.substring(0, i0);
		}

		String ss1 = s1;
		if (i1 != -1) {
			ss1 = s1.substring(0, i1);
		}

		int c = ss0.compareTo(ss1);
		if (c == 0) {
			if (i0 == -1) {
				if (i1 == -1) {
					return 0;
				} else {
					return -1;
				}
			} else if (i1 == -1) {
				return 1;
			} else {
				String child0 = s0.substring(i0);
				String child1 = s1.substring(i1);
				return compare(child0, child1);
			}
		} else {
			return c;
		}
	}
}