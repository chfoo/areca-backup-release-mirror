package com.myJava.file.driver;

import java.io.File;
import java.io.IOException;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
 * Utility class for FileSystemDriver implementations
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
public class FileSystemDriverUtils {
	private static final long DEFAULT_DELETION_DELAY = FrameworkConfiguration.getInstance().getFileToolDelay();
	private static final int DELETION_GC_FREQUENCY = (int) (2000 / DEFAULT_DELETION_DELAY);
	private static final int DELETION_MAX_ATTEMPTS = 1000;
	
	/**
	 * Delete the directory / file and all its content. <BR>
	 * If "waitForAvailability" is true, the process will wait - for each file
	 * or directory - until it is available. (the thread will be paused) and
	 * will make an attempt every "deletionDelay" milliseconds.
	 */
	public static void forceDelete(File fileOrDirectory, FileSystemDriver driver, TaskMonitor monitor)
	throws IOException,	TaskCancelledException {
/*
		if (
				(! forceDeleteImpl(fileOrDirectory, driver, monitor)) 
				&& (driver.exists(fileOrDirectory))
		) {			
			Logger.defaultLogger().warn("Attempted to delete "+ driver.getAbsolutePath(fileOrDirectory)+ " but it seems to be locked; retrying. ("+Thread.currentThread().getId()+"-"+driver.getClass().getName()+")");
						
			System.gc(); // I know it's not very beautiful ...
			// but it seems to be a bug with old
			// file references (even if all streams
			// are closed)
			
			try {
				Thread.sleep(10*DEFAULT_DELETION_DELAY);
			} catch (InterruptedException ignored) {
			}
			
			if (
					(! forceDeleteImpl(fileOrDirectory, driver, monitor)) 
					&& (driver.exists(fileOrDirectory))
			) {
				String[] files = driver.list(fileOrDirectory);
				throw new IOException("Unable to delete "+ driver.getAbsolutePath(fileOrDirectory) + " - isFile=" + driver.isFile(fileOrDirectory) + " - Exists=" + driver.exists(fileOrDirectory) + " - Children=" + (files == null ? 0 : files.length) + (files != null && files.length > 0 ? "(" + files[0] + " ...)" : ""));
			}
		}
		*/

		int retry = 0;
		try {
			while (
					(! forceDeleteImpl(fileOrDirectory, driver, monitor)) 
					&& (driver.exists(fileOrDirectory))
			) {
				retry++;
				if (retry == 100 || retry == DELETION_MAX_ATTEMPTS) {
					Logger
					.defaultLogger()
					.warn(
							"Attempted to delete file ("
							+ driver.getAbsolutePath(fileOrDirectory)
							+ ") during "
							+ (retry * DEFAULT_DELETION_DELAY)
							+ " ms but it seems to be locked !");
				} else if (retry > DELETION_MAX_ATTEMPTS) {
					String[] files = driver.list(fileOrDirectory);

					throw new IOException("Unable to delete file : "
							+ driver.getAbsolutePath(fileOrDirectory)
							+ " - isFile="
							+ driver.isFile(fileOrDirectory)
							+ " - Exists="
							+ driver.exists(fileOrDirectory)
							+ " - Children="
							+ (files == null ? 0 : files.length)
							+ (files == null || files.length > 0 ? "("
									+ files[0] + " ...)" : "")
					);
				}
				if (retry % DELETION_GC_FREQUENCY == 0) {
					System.gc(); // I know it's not very beautiful ...
					// but it seems to be a bug with old
					// file references (even if all streams
					// are closed)
				}
				Thread.sleep(DEFAULT_DELETION_DELAY);
			}
		} catch (InterruptedException ignored) {
		}
	}
	
	/**
	 * Delete the directory / file and all its content.
	 */
	public static boolean forceDeleteImpl(File fileOrDirectory, FileSystemDriver driver, TaskMonitor monitor)
	throws IOException,	TaskCancelledException {
		if (monitor != null) {
			monitor.checkTaskState();
		}

		if (driver.isDirectory(fileOrDirectory)) {
			String[] itemNames = driver.list(fileOrDirectory);
			if (itemNames != null) {
				for (int i = 0; i < itemNames.length; i++) {
					File f = new File(fileOrDirectory, itemNames[i]);
					forceDelete(f, driver, monitor);
				}
			}
		}

		return driver.delete(fileOrDirectory);
	}
}
