package com.application.areca;

import java.io.File;

import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemManager;
import com.myJava.object.Duplicable;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2011, Olivier PETRUCCI.

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
public class ArecaFileList implements Duplicable {
	private String[] content;
	
	public ArecaFileList(String[] content) {
		this.content = content;
	}
	
	public ArecaFileList(String content) {
		this(new String[] {content});
	}
	
	public boolean isEmpty() {
		return length() == 0;
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
		ArecaFileList clone = new ArecaFileList(new String[content.length]);
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

	public String[] asArray() {
		return content;
	}
}
