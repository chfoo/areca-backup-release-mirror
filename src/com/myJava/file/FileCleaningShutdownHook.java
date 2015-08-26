package com.myJava.file;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.myJava.util.log.Logger;

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
public class FileCleaningShutdownHook {
	private static FileCleaningShutdownHook INSTANCE = new FileCleaningShutdownHook();
	
	public static FileCleaningShutdownHook getInstance() {
		return INSTANCE;
	}
	
	private Set content = new HashSet();
	
	private FileCleaningShutdownHook() {
		Runnable rn = new Runnable() {
			public void run() {
				FileCleaningShutdownHook.this.clean();
			}
		};
		Thread th = new Thread(rn);
		th.setDaemon(false);
		th.setName("Remove temporary files");
		Runtime.getRuntime().addShutdownHook(th);
	}
	
	public synchronized void addFile(File file) {
		content.add(file);
	}
	
	public synchronized void removeFile(File file) {
		content.remove(file);
	}
	
	private void clean() {
		synchronized(this) {
			if (content.size() != 0) {
				Logger.defaultLogger().info("Deleting " + content.size() + " temporary files ...");
				Iterator iter = content.iterator();
				while (iter.hasNext()) {
					File target = (File)iter.next();
					try {
						FileTool.getInstance().delete(target);
					} catch (IOException e) {
						Logger.defaultLogger().error("Error deleting " + target, e);
					}
				}
				Logger.defaultLogger().info("Temporary files deleted.");
			}
		}
	}
}
