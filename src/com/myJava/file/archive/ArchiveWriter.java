package com.myJava.file.archive;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Comparator;

import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.util.Util;
import com.myJava.util.taskmonitor.TaskCancelledException;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
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
public class ArchiveWriter {

	private ArchiveAdapter adapter;
	private FileTool tool;

	public ArchiveWriter(ArchiveAdapter adapter) throws IOException {
		this.tool = FileTool.getInstance();  
		this.adapter = adapter;
	}

	public void addFile(String fullName, String content) 
			throws IOException {
		try {
			if (FileNameUtil.startsWithSeparator(fullName)) {
				fullName = fullName.substring(1);
			}

			this.adapter.addEntry(fullName, 0);    
			OutputStreamWriter fw = new OutputStreamWriter(this.adapter.getArchiveOutputStream());
			fw.write(content);
			fw.flush();
			this.adapter.closeEntry();
		} catch (IOException e) {
			throw new IOException("Error storing " + fullName, e);
		}
	}

	public void addFile(File file, String fullName, Comparator comparator, TaskMonitor monitor) 
			throws IOException, TaskCancelledException {
		try {
			if (! FileSystemManager.exists(file)) {
				return;
			}

			if (monitor != null) {
				monitor.checkTaskState();
			}

			if (FileSystemManager.isFile(file)) {
				if (FileNameUtil.startsWithSeparator(fullName)) {
					fullName = fullName.substring(1);
				}

				long length = FileSystemManager.length(file);

				this.adapter.addEntry(fullName, length);            
				this.tool.copyFile(file, this.adapter.getArchiveOutputStream(), false, monitor);
				this.adapter.closeEntry();
			} else {
				String[] children = FileSystemManager.list(file);
				if (comparator != null) {
					Arrays.sort(children, comparator);
				}
				for (int i=0; i<children.length; i++) {
					File child = new File(file, children[i]);
					this.addFile(
							child, 
							Util.replace(FileSystemManager.getCanonicalPath(child), FileSystemManager.getCanonicalPath(file), fullName), 
							comparator,
							monitor
							);
				}
			}
		} catch (IOException e) {
			throw new IOException("Error storing " + file + " under key : " + fullName, e);
		}
	}

	public ArchiveAdapter getAdapter() {
		return adapter;
	}

	public void close() throws IOException {
		this.adapter.close();
	}
}