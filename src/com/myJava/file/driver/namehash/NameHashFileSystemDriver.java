package com.myJava.file.driver.namehash;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import com.myJava.file.FileSystemManager;
import com.myJava.file.OutputStreamListener;
import com.myJava.file.driver.FileCacheableInformations;
import com.myJava.file.driver.AbstractLinkableFileSystemDriver;
import com.myJava.file.metadata.FileMetaData;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.object.ToStringHelper;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
 * Paths length cannot exceed 256 characters under Windows. <BR>
 * This due to a limitation of Sun's VM implementation for Windows and a
 * limitation of the windows platform itself. <BR>
 * This Driver computes a hashCode for each component of the files paths, and
 * uses it as physical name. <BR>
 * The advantage of using these hashCodes is that they are smaller than the
 * original name. This original name is stored in a "companion" file and can
 * thus be retrieved by simply reading the content of this companion file. <BR>
 * <BR>
 * This class is particularly useful for EncryptedFileSystemDrivers which may
 * create encrypted paths of greater length than the original path. <BR>
 * 
 * @author Olivier PETRUCCI <BR>
 *         
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
public class NameHashFileSystemDriver extends AbstractLinkableFileSystemDriver {

	/**
	 * Suffix which is used for companion files
	 */
	private static final String DECODED_SUFF = "_.hash.decoded";

	protected HashCache cache = new HashCache();
	protected File directoryRoot;

	/**
	 * Constructor
	 */
	public NameHashFileSystemDriver(File directoryRoot) {
		this.directoryRoot = directoryRoot.getAbsoluteFile();
	}

	/**
	 * Return the root of the Driver
	 */
	public File getDirectoryRoot() {
		return directoryRoot;
	}

	public boolean canRead(File file) {
		return this.predecessor.canRead(this.encodeFileName(file));
	}

	public short getType(File file) throws IOException {
		return this.predecessor.getType(this.encodeFileName(file));
	}

	public boolean canWrite(File file) {
		return this.predecessor.canWrite(this.encodeFileName(file));
	}

	public String getPhysicalPath(File file) {
		return predecessor.getPhysicalPath(this.encodeFileName(file));
	}

	public boolean createNewFile(File file) throws IOException {
		File encoded = this.encodeFileName(file);
		boolean ok = this.predecessor.createNewFile(encoded);
		if (ok) {
			createDecodingFile(encoded, predecessor.getName(file));
			return true;
		} else {
			return false;
		}
	}

	public boolean createNamedPipe(File pipe) throws IOException {
		File encoded = this.encodeFileName(pipe);
		boolean ok = this.predecessor.createNamedPipe(encoded);
		if (ok) {
			createDecodingFile(encoded, predecessor.getName(pipe));
			return true;
		} else {
			return false;
		}
	}

	public boolean createSymbolicLink(File symlink, String realPath)
			throws IOException {
		File encoded = this.encodeFileName(symlink);
		boolean ok = this.predecessor.createSymbolicLink(encoded, realPath);
		if (ok) {
			createDecodingFile(encoded, predecessor.getName(symlink));
			return true;
		} else {
			return false;
		}
	}

	public boolean delete(File file) {
		if (file == null) {
			return true;
		}
		File encoded = this.encodeFileName(file);

		return this.predecessor.delete(encoded)
				&& this.predecessor.delete(this.getDecodingFile(encoded));
	}

	public void forceDelete(File file, TaskMonitor monitor) throws IOException,
			TaskCancelledException {
		if (file == null) {
			return;
		}
		File encoded = this.encodeFileName(file);

		this.predecessor.forceDelete(encoded, monitor);
		this.predecessor.forceDelete(this.getDecodingFile(encoded), monitor);
	}

	public boolean exists(File file) {
		return this.predecessor.exists(this.encodeFileName(file));
	}

	public boolean isDirectory(File file) {
		return this.predecessor.isDirectory(this.encodeFileName(file));
	}

	public boolean isFile(File file) {
		return this.predecessor.isFile(this.encodeFileName(file));
	}

	public boolean isHidden(File file) {
		return this.predecessor.isHidden(this.encodeFileName(file));
	}

	public long lastModified(File file) {
		return this.predecessor.lastModified(this.encodeFileName(file));
	}

	public FileCacheableInformations getInformations(File file) {
		return this.predecessor.getInformations(this.encodeFileName(file));
	}

	public FileMetaData getMetaData(File f, boolean onlyBasicAttributes)
			throws IOException {
		return this.predecessor.getMetaData(this.encodeFileName(f),
				onlyBasicAttributes);
	}

	public long length(File file) {
		return this.predecessor.length(this.encodeFileName(file));
	}

	public void deleteOnExit(File f) {
		predecessor.deleteOnExit(this.encodeFileName(f));
		predecessor.deleteOnExit(this.getDecodingFile(f));
	}

	public String[] list(File file, FilenameFilter filter) {
		String[] files = this.predecessor.list(this.encodeFileName(file),
				new FilenameFilterAdapter(filter, this));
		return parseFiles(file, files);
	}

	public String[] list(File file) {
		String[] files = this.predecessor.list(this.encodeFileName(file),
				new FilenameFilterAdapter(this));
		return parseFiles(file, files);
	}

	private File[] parseFiles(File[] files) {
		ArrayList ret = new ArrayList();

		if (files == null) {
			return null;
		} else {
			for (int i = 0; i < files.length; i++) {
				try {
					ret.add(this.decodeFileName(files[i]));
				} catch (Throwable e) {
					Logger.defaultLogger().error(
							"Error reading file name "
									+ predecessor.getAbsolutePath(files[i])
									+ ". This file will be ignored. ("
									+ e.getMessage() + ")");
				}
			}

			return (File[]) ret.toArray(new File[ret.size()]);
		}
	}

	private String[] parseFiles(File parent, String[] files) {
		ArrayList ret = new ArrayList();

		if (files == null) {
			return null;
		} else {
			for (int i = 0; i < files.length; i++) {
				try {
					ret.add(this.decodeFileName(this.encodeFileName(parent), files[i]));
				} catch (Throwable e) {
					Logger.defaultLogger().error(
							"Error reading file name " + files[i]
									+ ". This file will be ignored. ("
									+ e.getMessage() + ")");
				}
			}

			return (String[]) ret.toArray(new String[ret.size()]);
		}
	}

	public File[] listFiles(File file, FileFilter filter) {
		File[] files = this.predecessor.listFiles(this.encodeFileName(file),
				new FileFilterAdapter(filter, this));
		return parseFiles(files);
	}

	public File[] listFiles(File file, FilenameFilter filter) {
		File[] files = this.predecessor.listFiles(this.encodeFileName(file),
				new FilenameFilterAdapter(filter, this));
		return parseFiles(files);
	}

	public File[] listFiles(File file) {
		File[] files = this.predecessor.listFiles(this.encodeFileName(file),
				new FileFilterAdapter(this));
		return parseFiles(files);
	}

	public boolean mkdir(File file) {
		try {
			if (file == null) {
				return false;
			}
			File encoded = this.encodeFileName(file);
			boolean ok = this.predecessor.mkdir(encoded);
			if (ok) {
				if (!predecessor.getAbsoluteFile(file).equals(
						predecessor.getAbsoluteFile(this.directoryRoot))) {
					createDecodingFile(encoded, predecessor.getName(file));
				}

				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			Logger.defaultLogger().error(
					"Error raised while creating directory : "
							+ FileSystemManager.getDisplayPath(file), e);
			throw new IllegalArgumentException("IllegalArgumentException : "
					+ e.getMessage());
		}
	}

	public boolean renameTo(File source, File dest) {
		File encodedSource = this.encodeFileName(source);
		File encodedDest = this.encodeFileName(dest);
		File decodingSource = this.getDecodingFile(encodedSource);

		boolean ok = this.predecessor.delete(decodingSource);
		if (ok) {
			if (this.predecessor.renameTo(encodedSource, encodedDest)) {
				try {
					this.createDecodingFile(encodedDest,
							predecessor.getName(dest));
				} catch (IOException e) {
					throw new IllegalArgumentException(
							"Critical Error (IOException) !! Unable to create decoding file !"
									+ e.getMessage());
				}
				return true;
			} else {
				try {
					this.createDecodingFile(encodedSource,
							predecessor.getName(source));
				} catch (IOException e) {
					throw new IllegalArgumentException(
							"Critical Error (IOException) !! Unable to create decoding file !"
									+ e.getMessage());
				}
				return false;
			}
		} else {
			return false;
		}
	}

	public boolean setLastModified(File file, long time) {
		return this.predecessor
				.setLastModified(this.encodeFileName(file), time);
	}

	public void applyMetaData(FileMetaData p, File f) throws IOException {
		this.predecessor.applyMetaData(p, this.encodeFileName(f));
	}

	public boolean setReadOnly(File file) {
		return this.predecessor.setReadOnly(this.encodeFileName(file));
	}

	public InputStream getCachedFileInputStream(File file) throws IOException {
		File target = this.encodeFileName(file);
		return predecessor.getCachedFileInputStream(target);
	}

	public InputStream getFileInputStream(File file) throws IOException {
		File target = this.encodeFileName(file);
		return predecessor.getFileInputStream(target);
	}

	public OutputStream getCachedFileOutputStream(File file) throws IOException {
		File target = this.encodeFileName(file);
		this.createDecodingFile(target, predecessor.getName(file)); // Really
																	// create
																	// the
																	// decoding
																	// file to
																	// ensure
																	// that hash
																	// collisions
																	// will be
																	// detected
		return predecessor.getCachedFileOutputStream(target);
	}

	public OutputStream getFileOutputStream(File file) throws IOException {
		File target = this.encodeFileName(file);
		this.createDecodingFile(target, predecessor.getName(file));
		return predecessor.getFileOutputStream(target);
	}

	public OutputStream getFileOutputStream(File file, boolean append)
			throws IOException {
		return getFileOutputStream(file, append, null);
	}

	public OutputStream getFileOutputStream(File file, boolean append,
			OutputStreamListener listener) throws IOException {
		File target = this.encodeFileName(file);
		this.createDecodingFile(target, predecessor.getName(file));
		return predecessor.getFileOutputStream(target, append, listener);
	}

	/**
	 * No direct file access is supported !
	 */
	public boolean directFileAccessSupported() {
		return false;
	}

	/**
	 * Hash the fileName
	 */
	protected File encodeFileName(File file) {
		try {
			File orig = file.getAbsoluteFile();
			if (orig.equals(this.directoryRoot)) {
				return orig;
			} else {
				File encodedParent = this.encodeFileName(orig.getParentFile());
				return new File(encodedParent, this.encodeFileName(
						encodedParent, orig.getName()));
			}
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	/**
	 * Hash the fileName : - 2 first chars of the original name - length
	 * (hexadecimal) of the original name - Java hashCode (see String class) of
	 * the original name
	 */
	protected String encodeFileName(File encodedParent, String shortName)
			throws HashCollisionException, IOException {
		if (shortName == null) {
			return null;
		}

		if (shortName.length() == 0) {
			return "";
		}

		int hash = shortName.hashCode();
		StringBuffer sb = new StringBuffer();
		sb.append(shortName.charAt(0));
		if (shortName.length() > 1) {
			sb.append(shortName.charAt(1));
		}
		sb.append(Integer.toHexString(shortName.length()));
		sb.append(Integer.toHexString(hash));

		// Validate the encoded name against the potentially existing files with
		// same hashCode
		return validateEncodedName(encodedParent, shortName, sb.toString());
	}

	private String validateEncodedName(File encodedParent, String decodedName,
			String encodedName) throws HashCollisionException, IOException {
		// Check wether the hash has been used
		File decoding = new File(encodedParent, encodedName + DECODED_SUFF);

		String fullName = this.cache.getFullName(predecessor
				.getAbsolutePath(decoding));
		if (fullName == null) {
			if (predecessor.exists(decoding)) {
				BufferedReader reader = null;
				InputStream in = null;
				try {
					in = predecessor.getFileInputStream(decoding);
					reader = new BufferedReader(new InputStreamReader(in));
					fullName = reader.readLine();
					this.cache.registerFullName(
							predecessor.getAbsolutePath(decoding), fullName);
				} finally {
					if (reader != null) {
						reader.close();
					} else if (in != null) {
						in.close();
					}
				}
			}
		}

		if (fullName != null && !(fullName.equals(decodedName))) {
			throw new HashCollisionException("HashCollision for file : ["
					+ decodedName + "] : encodedName = [" + encodedName
					+ "] was already used in parent directory : ["
					+ encodedParent.getAbsolutePath() + "] for file ["
					+ fullName + "]");
		}

		return encodedName; // The encoded name has not already been used for a
							// different file --> OK
	}

	/**
	 * Reads the companion file to decode the hashed name
	 */
	protected File decodeFileName(File file) throws IOException {
		File orig = file.getAbsoluteFile();
		if (orig.equals(this.directoryRoot)) {
			return orig;
		} else {
			return new File(this.decodeFileName(orig.getParentFile()),
					this.decodeFileName(orig.getParentFile(), orig.getName()));
		}
	}

	/**
	 * Reads the companion file to decode the hashed name
	 */
	protected String decodeFileName(File parent, String shortName)
			throws IOException {
		File decoding = new File(parent, shortName + DECODED_SUFF);

		String fullName = this.cache.getFullName(predecessor
				.getAbsolutePath(decoding));
		if (fullName == null) {
			BufferedReader reader = null;
			InputStream in = null;
			try {
				in = predecessor.getFileInputStream(decoding);
				reader = new BufferedReader(new InputStreamReader(in));
				fullName = reader.readLine();
				this.cache.registerFullName(
						predecessor.getAbsolutePath(decoding), fullName);
				return fullName;
			} finally {
				if (reader != null) {
					reader.close();
				} else if (in != null) {
					in.close();
				}
			}
		}

		return fullName;
	}

	/**
	 * Computes the companion file's name.
	 */
	protected File getDecodingFile(File file) {
		if (file == null) {
			return null;
		}
		File parent = predecessor.getParentFile(file);

		return new File(parent, predecessor.getName(file) + DECODED_SUFF);
	}

	/**
	 * Creates the companion file, which stores the real name of the
	 * file/directory.
	 */
	protected void createDecodingFile(File encoded, String decodedName)
			throws IOException {
		OutputStreamWriter writer = null;
		try {
			File decoding = this.getDecodingFile(encoded);
			writer = new OutputStreamWriter(
					predecessor.getFileOutputStream(decoding));
			writer.write(decodedName);
			this.cache.registerFullName(
					FileSystemManager.getAbsolutePath(decoding), decodedName);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	/**
	 * Checks wether the file is a "companion" file (which must be ignored by
	 * file listing methods)
	 */
	protected boolean isDecodingFile(File f) {
		return isDecodingFile(predecessor.getName(f));
	}

	/**
	 * Checks wether the file is a "companion" file (which must be ignored by
	 * file listing methods)
	 */
	protected boolean isDecodingFile(String f) {
		return f.endsWith(DECODED_SUFF);
	}

	public int hashCode() {
		int h = HashHelper.initHash(this);
		h = HashHelper.hash(h, this.directoryRoot);
		h = HashHelper.hash(h, this.predecessor);

		return h;
	}

	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else if (o instanceof NameHashFileSystemDriver) {
			NameHashFileSystemDriver other = (NameHashFileSystemDriver) o;

			return (EqualsHelper
					.equals(other.directoryRoot, this.directoryRoot) && EqualsHelper
					.equals(other.predecessor, this.predecessor));
		} else {
			return false;
		}
	}

	protected static class FilenameFilterAdapter implements FilenameFilter {
		protected FilenameFilter filter;
		protected NameHashFileSystemDriver driver;

		public FilenameFilterAdapter(NameHashFileSystemDriver driver) {
			this(null, driver);
		}

		public FilenameFilterAdapter(FilenameFilter wrappedFilter,
				NameHashFileSystemDriver driver) {

			this.filter = wrappedFilter;
			this.driver = driver;
		}

		public boolean accept(File dir, String name) {
			try {
				if (driver.isDecodingFile(name)) {
					return false;
				} else {
					if (filter != null) {
						File targetDirectory = driver.decodeFileName(dir);
						String targetName = driver.decodeFileName(dir, name);

						return filter.accept(targetDirectory, targetName);
					} else {
						return true;
					}
				}
			} catch (Throwable e) {
				Logger.defaultLogger().error(
						"Error reading file name "
								+ driver.predecessor.getAbsolutePath(dir) + "/"
								+ name + ". This file will be ignored. ("
								+ e.getMessage() + ")");
				return false;
			}
		}

		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			} else if (!(obj instanceof FilenameFilterAdapter)) {
				return false;
			} else {
				FilenameFilterAdapter other = (FilenameFilterAdapter) obj;
				return EqualsHelper.equals(this.filter, other.filter)
						&& EqualsHelper.equals(this.driver, other.driver);
			}
		}

		public int hashCode() {
			int h = HashHelper.initHash(this);
			h = HashHelper.hash(h, filter);
			h = HashHelper.hash(h, driver);
			return h;
		}

		public String toString() {
			StringBuffer sb = ToStringHelper.init(this);
			ToStringHelper.append("Filter", this.filter, sb);
			ToStringHelper.append("Driver", this.driver, sb);
			return ToStringHelper.close(sb);
		}
	}

	protected static class FileFilterAdapter implements FileFilter {
		protected FileFilter filter;
		protected NameHashFileSystemDriver driver;

		public FileFilterAdapter(FileFilter wrappedFilter,
				NameHashFileSystemDriver driver) {

			this.filter = wrappedFilter;
			this.driver = driver;
		}

		public FileFilterAdapter(NameHashFileSystemDriver driver) {

			this.filter = null;
			this.driver = driver;
		}

		public boolean accept(File file) {
			try {
				if (driver.isDecodingFile(file)) {
					return false;
				} else {
					if (filter != null) {
						File target = driver.decodeFileName(file);
						return filter.accept(target);
					} else {
						return true;
					}
				}
			} catch (Throwable e) {
				Logger.defaultLogger().error(
						"Error reading file name "
								+ driver.predecessor.getAbsolutePath(file)
								+ ". This file will be ignored. ("
								+ e.getMessage() + ")");
				return false;
			}
		}

		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			} else if (!(obj instanceof FileFilterAdapter)) {
				return false;
			} else {
				FileFilterAdapter other = (FileFilterAdapter) obj;
				return EqualsHelper.equals(this.filter, other.filter)
						&& EqualsHelper.equals(this.driver, other.driver);
			}
		}

		public int hashCode() {
			int h = HashHelper.initHash(this);
			h = HashHelper.hash(h, filter);
			h = HashHelper.hash(h, driver);
			return h;
		}

		public String toString() {
			StringBuffer sb = ToStringHelper.init(this);
			ToStringHelper.append("Filter", this.filter, sb);
			ToStringHelper.append("Driver", this.driver, sb);
			return ToStringHelper.close(sb);
		}
	}

	public String toString() {
		StringBuffer sb = ToStringHelper.init(this);
		ToStringHelper.append("ROOT", this.directoryRoot, sb);
		ToStringHelper.append("PREDECESSOR", this.predecessor, sb);
		return ToStringHelper.close(sb);
	}
}