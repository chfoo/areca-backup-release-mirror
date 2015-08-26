package com.myJava.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.driver.DefaultFileSystemDriver;
import com.myJava.file.driver.DriverAlreadySetException;
import com.myJava.file.driver.FileCacheableInformations;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.file.metadata.FileMetaData;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
 * File system manager.
 * <BR>This class maps various drivers to their mount point and uses them to handle file operations
 * <BR>By default, a DefaultFileSystemDriver is used, which routes method calls to the File class.
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
public class FileSystemManager {
	protected static FileSystemManager instance = new FileSystemManager();
	protected static int MAX_CACHED_MOUNTPOINTS = FrameworkConfiguration.getInstance().getMaxCachedMountPoints();

	/**
	 * Drivers indexed by mount point
	 */
	protected Map driverCache = new HashMap();

	/**
	 * Drivers indexed by mount point. This map is used as reference to reset the driver cache when needed.
	 */
	protected Map driversReference = new HashMap();

	/**
	 * Default driver
	 */
	protected FileSystemDriver defaultDriver = new DefaultFileSystemDriver();

	/**
	 * Roots of the filesystem
	 */
	protected Set roots = new HashSet();

	/**
	 * Optimization : this flag is set to "true" as soon as a first driver is explicitely registered
	 */
	protected boolean hasOnlyDefaultDriver = true;

	public static FileSystemManager getInstance() {
		return instance;
	}

	public FileSystemManager() {
		// Init the filesystem roots
		File[] rootArray = File.listRoots();
		for (int i = 0; i < rootArray.length; i++) {
			this.roots.add(rootArray[i]);
		}
	}
	
	public FileSystemDriversRestorePoint buildRestorePoint() {
		return new FileSystemDriversRestorePoint(this.driversReference);
	}
	
	public void applyRestorePoint(FileSystemDriversRestorePoint restorePoint) {
		restorePoint.apply(driversReference);
		this.resetDriverCache();
	}

	/**
	 * Register a driver for a specific mount point
	 */
	public synchronized void registerDriver(File mountPoint, FileSystemDriver driver) throws DriverAlreadySetException, IOException {
		this.hasOnlyDefaultDriver = false;
		
		FileSystemDriver existing = this.getDriverAtMountPoint(mountPoint);
		driver.mount();
		if (existing != null && ! existing.equals(driver)) {
			unregisterDriver(mountPoint);
		}

		Logger.defaultLogger().info("Registring a new file system driver : Mount Point = " + mountPoint + ", Driver = " + driver);
		
		// Add the driver to the reference map
		this.driversReference.put(mountPoint, driver);
		this.resetDriverCache();
	}

	/**
	 * Deletes the driver currently registered at this mount point.
	 */
	public synchronized void unregisterDriver(File mountPoint) throws IOException {
		FileSystemDriver existing = this.getDriverAtMountPoint(mountPoint);
		Logger.defaultLogger().info("Unregistring file system driver : Mount Point = " + mountPoint + ", Driver = " + existing);
		try {
			existing.unmount();
		} catch (Throwable e) {
			Logger.defaultLogger().error(e);
		}

		// Remove the driver from the reference map
		this.driversReference.remove(mountPoint);
		this.resetDriverCache();
	}

	/**
	 * Clear the driver cache and put all references contained in the "driverReference" map
	 */
	private void resetDriverCache() {
		this.driverCache.clear();
		this.driverCache.putAll(this.driversReference);
	}

	/**
	 * Return the driver that has been explicitely registered for the mount point passed as argument
	 * <BR>No recursive lookup is done
	 */
	public synchronized FileSystemDriver getDriverAtMountPoint(File mountPoint) {
		return (FileSystemDriver) this.driversReference.get(mountPoint);
	}

	/**
	 * Retourne le driver approprie pour le fichier specifie. <BR>
	 * Si aucun driver n'est trouve, le driver par defaut est retourne.
	 */
	public synchronized FileSystemDriver getDriver(File file) {
		// Si aucun driver n'a ete enregistre, on retourne le driver par defaut
		if (this.hasOnlyDefaultDriver) {
			return this.defaultDriver;
		}

		// Sinon, on recherche le Driver
		return resolveDriver(file, true);
	}

	public synchronized FileSystemDriver getDefaultDriver() {
		return defaultDriver;
	}

	/**
	 * Specifie le driver par defaut (celui qui est utilise si aucun driver n'a
	 * ete enregistre pour un chemin donne).
	 */
	public synchronized void setDefaultDriver(FileSystemDriver defaultDriver) {
		this.defaultDriver = defaultDriver;
	}

	/**
	 * Store a driver in the driver cache without any check (for instance that a previous driver exists)
	 */
	private Object cacheDriver(File mountPoint, FileSystemDriver driver) {
		if (this.driverCache.size() >= MAX_CACHED_MOUNTPOINTS) {
			// Max size reached -> destroy all
			this.resetDriverCache();
		}
		return this.driverCache.put(mountPoint, driver);
	}

	/**
	 * Resolve the driver for the file passed as argument
	 * @param file
	 * @param firstCall
	 * @return
	 */
	private FileSystemDriver resolveDriver(File file, boolean firstCall) {
		// lookup in driver map
		Object driver = this.driverCache.get(file);
		if (driver == null) {
			if (this.isRoot(file)) {
				return this.defaultDriver;
			} else {
				// search again - for parent directory
				File parent = file.getParentFile();
				FileSystemDriver returned = this.resolveDriver(parent, false);

				if (!firstCall) {
					// Register the driver in the driver cache
					this.cacheDriver(file, returned);
				}

				return returned;
			}
		} else {
			return (FileSystemDriver) driver;
		}
	}

	public boolean isRoot(File file) {
		return this.roots.contains(file) || file.getParentFile() == null;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// FILE class mimic
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////


	public synchronized void flush(File file) throws IOException {
		FileSystemDriver driver = getDriver(file);
		driver.flush();
	}
	
	public synchronized void clearCachedData(File file) throws IOException {
		FileSystemDriver driver = getDriver(file);
		driver.clearCachedData(file);
	}
	
	public static boolean isFile(File file) {
		return getInstance().getDriver(file).isFile(file);
	}

	public static boolean isDirectory(File file) {
		return getInstance().getDriver(file).isDirectory(file);
	}

	public static long length(File file) {
		return getInstance().getDriver(file).length(file);
	}

	public static boolean delete(File file) {
		return getInstance().getDriver(file).delete(file);
	}
	
    public static void forceDelete(File file, TaskMonitor monitor)
    throws IOException, TaskCancelledException {
		getInstance().getDriver(file).forceDelete(file, monitor);
	}

	public static boolean renameTo(File sourceFile, File destinationFile) {
		FileSystemDriver sourceDriver = getInstance().getDriver(sourceFile);
		FileSystemDriver destinationDriver = getInstance().getDriver(
				destinationFile);

		if (sourceDriver.equals(destinationDriver)) {
			return getInstance().getDriver(sourceFile).renameTo(sourceFile,
					destinationFile);
		} else {
			return false;
		}
	}

	public static boolean exists(File file) {
		return getInstance().getDriver(file).exists(file);
	}

	public static boolean canRead(File file) {
		return getInstance().getDriver(file).canRead(file);
	}

	public static boolean canWrite(File file) {
		return getInstance().getDriver(file).canWrite(file);
	}

	public static boolean createNewFile(File file) throws IOException {
		return getInstance().getDriver(file).createNewFile(file);
	}

	public static boolean isAbsolute(File file) throws IOException {
		return getInstance().getDriver(file).isAbsolute(file);
	}

	public static boolean isHidden(File file) throws IOException {
		return getInstance().getDriver(file).isHidden(file);
	}

	public static boolean mkdir(File file) throws IOException {
		return getInstance().getDriver(file).mkdir(file);
	}

	public static boolean createSymbolicLink(File symlink, String realPath)
			throws IOException {
		return getInstance().getDriver(symlink).createSymbolicLink(symlink,
				realPath);
	}

	public static boolean createNamedPipe(File pipe) throws IOException {
		return getInstance().getDriver(pipe).createNamedPipe(pipe);
	}

	/**
	 * Return the type of the file <BR>
	 * See types listed in FileMetaDataAccessor
	 */
	public static short getType(File file) throws IOException {
		return getInstance().getDriver(file).getType(file);
	}

	public static boolean mkdirs(File file) throws IOException {
		return getInstance().getDriver(file).mkdirs(file);
	}

	public static boolean setLastModified(File file, long time) {
		return getInstance().getDriver(file).setLastModified(file, time);
	}

	public static boolean setReadOnly(File file) {
		return getInstance().getDriver(file).setReadOnly(file);
	}

	public static File getAbsoluteFile(File file) {
		return getInstance().getDriver(file).getAbsoluteFile(file);
	}

	public static File getCanonicalFile(File file) throws IOException {
		return getInstance().getDriver(file).getCanonicalFile(file);
	}

	public static String getName(File file) {
		return getInstance().getDriver(file).getName(file);
	}

	public static File getParentFile(File file) {
		return getInstance().getDriver(file).getParentFile(file);
	}

	public static String getParent(File file) {
		return getInstance().getDriver(file).getParent(file);
	}

	public static String getPath(File file) {
		return getInstance().getDriver(file).getPath(file);
	}

	public static String[] list(File file) {
		return getInstance().getDriver(file).list(file);
	}

	public static short getAccessEfficiency(File file) {
		return getInstance().getDriver(file).getAccessEfficiency();
	}

	public static String[] list(File file, FilenameFilter filter) {
		return getInstance().getDriver(file).list(file, filter);
	}

	public static File[] listFiles(File file, FileFilter filter) {
		return getInstance().getDriver(file).listFiles(file, filter);
	}

	public static String getAbsolutePath(File file) {
		return getInstance().getDriver(file).getAbsolutePath(file);
	}
	
	public static String getPhysicalPath(File file) {
		return getInstance().getDriver(file).getPhysicalPath(file);
	}
	
	public static String getDisplayPath(File file) {
		String physical = getPhysicalPath(file);
		String absolute = getAbsolutePath(file);
		
		if (! physical.equalsIgnoreCase(absolute)) {
			return physical + " (" + absolute + ")";
		} else {
			return physical;
		}
	}

	public static String getCanonicalPath(File file) throws IOException {
		return getInstance().getDriver(file).getCanonicalPath(file);
	}

	public static long lastModified(File file) {
		return getInstance().getDriver(file).lastModified(file);
	}

	public static File[] listFiles(File file) {
		return getInstance().getDriver(file).listFiles(file);
	}

	public static File[] listFiles(File file, FilenameFilter filter) {
		return getInstance().getDriver(file).listFiles(file, filter);
	}

	public static InputStream getFileInputStream(File file) throws IOException {
		return getInstance().getDriver(file).getFileInputStream(file);
	}

	public static OutputStream getCachedFileOutputStream(File file)
			throws IOException {
		return getInstance().getDriver(file).getCachedFileOutputStream(file);
	}

	public static OutputStream getFileOutputStream(File file)
			throws IOException {
		return getInstance().getDriver(file).getFileOutputStream(file);
	}

	public static OutputStream getFileOutputStream(File file, boolean append,
			OutputStreamListener listener) throws IOException {
		return getInstance().getDriver(file).getFileOutputStream(file, append,
				listener);
	}

	public static OutputStream getFileOutputStream(File file, boolean append)
			throws IOException {
		return getInstance().getDriver(file).getFileOutputStream(file, append);
	}

	public static OutputStream getFileOutputStream(String file)
			throws IOException {
		return getFileOutputStream(new File(file));
	}

	public static InputStreamReader getReader(File file) throws IOException {
		return new InputStreamReader(getFileInputStream(file));
	}

	public static OutputStreamWriter getWriter(File file) throws IOException {
		return new OutputStreamWriter(getFileOutputStream(file));
	}

	public static OutputStreamWriter getWriter(File file, boolean append)
			throws IOException {
		return new OutputStreamWriter(getFileOutputStream(file, append));
	}

	public static OutputStreamWriter getWriter(String file, boolean append)
			throws IOException {
		return new OutputStreamWriter(getFileOutputStream(new File(file),
				append));
	}

	public static InputStream getCachedFileInputStream(File file)
			throws IOException {
		return getInstance().getDriver(file).getCachedFileInputStream(file);
	}

	public static FileCacheableInformations getInformations(File file) {
		return getInstance().getDriver(file).getInformations(file);
	}

	public static FileMetaData getMetaData(File file,
			boolean onlyBasicAttributes) throws IOException {
		return getInstance().getDriver(file).getMetaData(file,
				onlyBasicAttributes);
	}

	public static void applyMetaData(FileMetaData p, File f) throws IOException {
		getInstance().getDriver(f).applyMetaData(p, f);
	}

	public static void deleteOnExit(File f) {
		getInstance().getDriver(f).deleteOnExit(f);
	}

	public static ReadableCheckResult isReadable(File file) {
		ReadableCheckResult ret = new ReadableCheckResult();
		if (file == null || isDirectory(file)) {
			ret.setReadable(false);
		} else {
			String message = null;
			FileLock lock = null;
			RandomAccessFile raf = null;

			synchronized (FileSystemManager.class) {
				try {
					raf = new RandomAccessFile(file, "r");
				} catch (Throwable e) {
					message = e.getMessage();
				}

				if (raf != null) {
					FileChannel chn = raf.getChannel();
					try {
						lock = chn.tryLock(0L, Long.MAX_VALUE, true);
					} catch (Throwable e) {
						message = e.getMessage();
					} finally {
						if (lock != null && lock.isValid()) {
							try {
								lock.release();
							} catch (IOException ignored) {
							}
						}

						try {
							raf.close();
						} catch (IOException ignored) {
						}
					}
				}
			}

			if (lock == null) {
				ret.setReadable(false);
				ret.setCause(message);
			} else {
				ret.setReadable(true);
			}
		}

		return ret;
	}
}
