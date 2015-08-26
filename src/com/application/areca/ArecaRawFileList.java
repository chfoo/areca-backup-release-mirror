package com.application.areca;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemManager;
import com.myJava.file.iterator.FilePathComparator;
import com.myJava.object.Duplicable;
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
public class ArecaRawFileList implements Duplicable {
	private String[] content;

	public ArecaRawFileList(String[] content) {
		this.content = content;
	}

	public ArecaRawFileList(String content) {
		this(new String[] {content});
	}
	
	public String[] asArray() {
		return content;
	}

	public boolean isEmpty() {
		return length() == 0;
	}
	
	public boolean hasDirs() {
		for (int e=0; e<content.length; e++) {
			if (FileNameUtil.endsWithSeparator(content[e])) {
				return true;
			}
		}
		return false;
	}

	public void normalize(File directory) {
		for (int i=0; i<content.length; i++) {
			String normalized = FileSystemManager.getAbsolutePath(new File(directory, content[i]));
			if (FileNameUtil.endsWithSeparator(normalized)) {
				normalized = normalized.substring(0, normalized.length() - 1);
			}
			content[i] = normalized;
		}
	}

	public Duplicable duplicate() {
		ArecaRawFileList clone = new ArecaRawFileList(new String[content.length]);
		for (int i=0; i<content.length; i++) {
			clone.content[i] = content[i];
		}
		return clone;
	}

	public int length() {
		return content == null ? 0 : content.length;
	}

	public String get(int i) {
		return content[i];
	}

	public void sort() {
		Arrays.sort(content, FilePathComparator.instance());
	}
	
	public void removeTrailingSlashes() {
		for (int i=0; i<content.length; i++) {
			if (FileNameUtil.startsWithSeparator(content[i])) {
				content[i] = content[i].substring(1);
			}
 		}
	}

	private static boolean isContained(String parent, String child) {
		String normChild = child.endsWith("/") ? child.substring(0, child.length()-1) : child;
		String normParent = parent.endsWith("/") ? parent.substring(0, parent.length()-1) : parent;

		return parent.length() == 0 || normChild.equals(normParent) || normChild.startsWith(normParent + "/");
	}
	
	public String toString() {
		StringBuffer sb = ToStringHelper.init(this);
		for (int i=0; i<content.length; i++) {
			ToStringHelper.append(""+i, content[i], sb);
		}
		return ToStringHelper.close(sb);
	}

	/**
	 * Remove file entries that are already contained in directory entries
	 */
	public void deduplicate() {
		List deduplicated = new ArrayList();
		for (int child=0; child<content.length; child++) {
			boolean add = true;
			Iterator iter = deduplicated.iterator();
			while (iter.hasNext()) {
				String existing = (String)iter.next();
				if (isContained(existing, content[child])) {
					add = false;
					break;
				}
			}
			if (add) {
				deduplicated.add(content[child]);
			}
		}

		content = (String[])deduplicated.toArray(new String[deduplicated.size()]);
	}
}
