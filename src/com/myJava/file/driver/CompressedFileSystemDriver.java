package com.myJava.file.driver;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.myJava.file.CompressionArguments;
import com.myJava.file.OutputStreamListener;
import com.myJava.file.archive.zip64.ZipEntry;
import com.myJava.file.archive.zip64.ZipInputStream;
import com.myJava.file.archive.zip64.ZipOutputStream;
import com.myJava.file.archive.zip64.ZipVolumeStrategy;
import com.myJava.file.metadata.FileMetaData;
import com.myJava.file.multivolumes.VolumeInputStream;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.object.ToStringHelper;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
 * "Linkable" driver with compression capabilities
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
public class CompressedFileSystemDriver 
extends AbstractLinkableFileSystemDriver {

	private CompressionArguments compression = new CompressionArguments();
	private File root;

	/**
	 * @param directoryRoot
	 * @param key
	 */
	public CompressedFileSystemDriver(
			File root,
			FileSystemDriver predecessor, 
			CompressionArguments compression) {
		super();
		this.root = root;
		this.compression = compression;
		this.setPredecessor(predecessor);
	}

	public boolean canRead(File file) {
		return this.predecessor.canRead(encode(file));
	}

	public String getPhysicalPath(File file) {
		return predecessor.getPhysicalPath(encode(file));
	}

	public short getType(File file) throws IOException {
		return this.predecessor.getType(encode(file));
	}

	public boolean canWrite(File file) {
		return this.predecessor.canWrite(encode(file));
	}

	public void forceDelete(File file, TaskMonitor monitor)
			throws IOException, TaskCancelledException {
		File[] f = resolveFiles(file);
		for (int i=0; i<f.length; i++) {
			predecessor.forceDelete(f[i], monitor);
		}
	}

	public boolean delete(File file) {
		File[] f = resolveFiles(file);
		boolean bool = true;
		for (int i=0; i<f.length; i++) {
			if (! predecessor.delete(f[i])) {
				bool = false;
				break;
			}
		}
		return bool;
	}

	public boolean createNewFile(File file) throws IOException {
		return this.predecessor.createNewFile(encode(file));
	}

	public boolean exists(File file) {
		return this.predecessor.exists(encode(file));
	}

	public FileCacheableInformations getInformations(File file) {
		File[] f = resolveFiles(file);
		boolean bool = true;
		FileCacheableInformations fi = null;
		long length = -1;
		for (int i=0; i<f.length; i++) {
			fi = predecessor.getInformations(f[i]);
			if (fi.isLengthSet()) {
				length += fi.getLength();
			}
		}
		if (fi != null && length != -1) {
			fi.enforceLength(length);
		}
		return fi;
	}

	public boolean isDirectory(File file) {
		return this.predecessor.isDirectory(encode(file));
	}

	public boolean isFile(File file) {
		return this.predecessor.isFile(encode(file));
	}

	public boolean isHidden(File file) {
		return this.predecessor.isHidden(encode(file));
	}

	public long lastModified(File file) {
		File[] f = resolveFiles(file);
		long time = 0;
		for (int i=0; i<f.length; i++) {
			time = Math.max(time, predecessor.lastModified(f[i]));
		}
		return time;
	}

	public long length(File file) {
		File[] f = resolveFiles(file);
		long length = 0;
		for (int i=0; i<f.length; i++) {
			length += predecessor.length(f[i]);
		}
		return length;
	}

	public String[] list(File file, FilenameFilter filter) {
		String[] files = this.predecessor.list(this.encode(file), new FilenameFilterAdapter(filter, this));
		return processFiles(files);
	}

	public String[] list(File file) {
		String[] files = this.predecessor.list(this.encode(file));        
		return processFiles(files);
	}

	private static void logDecodingIssue(String f) {
		Logger.defaultLogger().warn("Areca was not able to handle the following file : " + f + ". This may happen for files created by an external program. The file will be ignored by Areca.");
	}

	private File[] processFiles(File[] files) {
		if (files != null) {
			ArrayList list = new ArrayList();
			for (int i=0; i<files.length; i++) {
				if ((! compression.isAddExtension()) || files[i].getName().endsWith(CompressionArguments.ZIP_SUFFIX)) {
					try {
						list.add(this.decode(files[i]));
					} catch (FileNameException e) {
						logDecodingIssue(""+files[i]);
					}
				}
			}
			return (File[])list.toArray(new File[list.size()]);
		} else {
			return null;
		}
	}
	
	private String[] processFiles(String[] files) {
		if (files != null) {
			ArrayList list = new ArrayList();
			for (int i=0; i<files.length; i++) {
				if ((! compression.isAddExtension()) || files[i].endsWith(CompressionArguments.ZIP_SUFFIX)) {
					try {
						list.add(this.decode(files[i]));
					} catch (FileNameException e) {
						logDecodingIssue(""+files[i]);
					}
				}
			}
			return (String[])list.toArray(new String[list.size()]);
		} else {
			return null;
		}
	}

	public File[] listFiles(File file, FileFilter filter) {
		File[] files = this.predecessor.listFiles(this.encode(file), new FileFilterAdapter(filter, this));        
		return processFiles(files);
	}

	public File[] listFiles(File file, FilenameFilter filter) {
		File[] files = this.predecessor.listFiles(this.encode(file), new FilenameFilterAdapter(filter, this));
		return processFiles(files);
	}

	public File[] listFiles(File file) {
		File[] files = this.predecessor.listFiles(this.encode(file));
		return processFiles(files);
	}

	public boolean mkdir(File file) {
		return this.predecessor.mkdir(encode(file));
	}

	public boolean mkdirs(File file) {
		return this.predecessor.mkdirs(encode(file));
	}

	public boolean renameTo(File source, File dest) {
		File[] f = resolveFiles(source);
		boolean bool = true;

		File target = new File(encode(predecessor.getParentFile(dest)), predecessor.getName(dest));
		ZipVolumeStrategy vol = new ZipVolumeStrategy(target, compression.getNbDigits());
		for (int i=0; i<f.length; i++) {
			File encodedDest;
			if (i == f.length - 1) {
				encodedDest = vol.getFinalArchive();
			} else {
				encodedDest = vol.getNextFile();
			}

			if (! predecessor.renameTo(f[i], encodedDest)) {
				bool = false;
				break;
			}
		}
		return bool;
	}

	public void mount() throws IOException {
		if (compression.isMultiVolumes() && ! compression.isAddExtension()) {
			throw new IllegalStateException("The \".zip\" extension is mandatory if zip-splitting is enabled.");
		}
		super.mount();
	}

	public boolean setLastModified(File file, long time) {
		File[] f = resolveFiles(file);
		boolean bool = true;
		for (int i=0; i<f.length; i++) {
			if (! predecessor.setLastModified(f[i], time)) {
				bool = false;
				break;
			}
		}
		return bool;
	}

	public boolean setReadOnly(File file) {
		File[] f = resolveFiles(file);
		boolean bool = true;
		for (int i=0; i<f.length; i++) {
			if (! predecessor.setReadOnly(f[i])) {
				bool = false;
				break;
			}
		}
		return bool;
	}

	public InputStream getFileInputStream(File file) throws IOException {
		return getFileInputStream(file, false);
	}

	public InputStream getCachedFileInputStream(File file) throws IOException {
		return getFileInputStream(file, true);
	}

	public InputStream getFileInputStream(File file, boolean cached) throws IOException {
		ZipInputStream zin;
		if (compression.isMultiVolumes()) {
			File target = new File(encode(predecessor.getParentFile(file)), predecessor.getName(file));
			ZipVolumeStrategy strategy = new ZipVolumeStrategy(target, predecessor, cached, compression.getNbDigits());
			zin = new ZipInputStream(new VolumeInputStream(strategy));
		} else if (cached){
			zin = new ZipInputStream(predecessor.getCachedFileInputStream(encode(file)));
		} else {
			zin = new ZipInputStream(predecessor.getFileInputStream(encode(file)));
		}
		if (compression.getCharset() != null) {
			zin.setCharset(compression.getCharset());
		}
		try {
			zin.getNextEntry();
		} catch (IOException e) {
			try {
				zin.close();
			} catch (IOException ignored) {
			}
			throw e;
		}
		return zin;
	}

	public OutputStream getCachedFileOutputStream(File file) throws IOException {
		return getOutputStream(file, true, null);
	}    

	public OutputStream getFileOutputStream(File file) throws IOException {
		return getOutputStream(file, false, null);
	}    

	private OutputStream getOutputStream(File file, boolean cached, OutputStreamListener listener) throws IOException {
		ZipOutputStream zout;
		if (compression.isMultiVolumes()) {
			File target = new File(encode(predecessor.getParentFile(file)), predecessor.getName(file));
			ZipVolumeStrategy strategy = new ZipVolumeStrategy(target, predecessor, cached, compression.getNbDigits());
			strategy.setListener(listener);
			zout = new ZipOutputStream(
					strategy, 
					compression.getVolumeSize() * 1024 * 1024, 
					compression.isUseZip64()
					);
		} else {
			OutputStream base;
			if (cached) {
				base = predecessor.getCachedFileOutputStream(encode(file)); // ! to fix : listener is ignored
			} else {
				base = predecessor.getFileOutputStream(encode(file), false, listener);        		
			}
			zout = new ZipOutputStream(base, compression.isUseZip64());
		}
		if (compression.getLevel() >= 0) {
			zout.setLevel(compression.getLevel());          	
		} else {
			zout.setLevel(9);
		}
		if (compression.getCharset() != null) {
			zout.setCharset(compression.getCharset());
		}
		if (compression.getComment() != null) {
			zout.setComment(compression.getComment());
		}
		try {
			zout.putNextEntry(new ZipEntry(file.getName()));
		} catch (IOException e) {
			try {
				zout.close();
			} catch (IOException ignored) {
			}
			throw e;
		}
		return zout;
	}    

	public OutputStream getFileOutputStream(File file, boolean append, OutputStreamListener listener) throws IOException {
		if (append) {
			throw new IllegalArgumentException("Cannot open an OutputStream in 'append' mode on a compressed FileSystem");
		}
		return getOutputStream(file, false, listener);
	}

	public OutputStream getFileOutputStream(File file, boolean append) throws IOException {
		return getFileOutputStream(file, append, null);
	}   

	public void deleteOnExit(File file) {
		File[] f = resolveFiles(file);
		for (int i=0; i<f.length; i++) {
			predecessor.deleteOnExit(f[i]);
		}
	}

	public FileMetaData getMetaData(File file, boolean onlyBasicAttributes) throws IOException {
		return this.predecessor.getMetaData(encode(file), onlyBasicAttributes);
	}

	public void applyMetaData(FileMetaData p, File file) throws IOException {
		File[] f = resolveFiles(file);
		for (int i=0; i<f.length; i++) {
			predecessor.applyMetaData(p, f[i]);
		}
	}

	public int hashCode() {
		int h = HashHelper.initHash(this);
		h = HashHelper.hash(h, this.predecessor);
		h = HashHelper.hash(h, this.compression);
		h = HashHelper.hash(h, this.root);

		return h;
	}

	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else if (o instanceof CompressedFileSystemDriver) {
			CompressedFileSystemDriver other = (CompressedFileSystemDriver)o;

			return (
					EqualsHelper.equals(other.predecessor, this.predecessor) 
					&& EqualsHelper.equals(other.compression, this.compression) 
					&& EqualsHelper.equals(other.root, this.root) 
					);
		} else {
			return false;
		}
	}

	public String toString() {
		StringBuffer sb = ToStringHelper.init(this);
		ToStringHelper.append("Predecessor", this.predecessor, sb);
		return ToStringHelper.close(sb);
	}

	public boolean directFileAccessSupported() {
		return false;
	}

	protected File encode(File file) {
		File orig = file.getAbsoluteFile();
		if (orig.equals(this.root)) {
			return orig;
		} else {
			return new File(this.encode(orig.getParentFile()), this.encode(orig.getName()));
		}
	}

	protected File[] resolveFiles(File file) {
		File orig = file.getAbsoluteFile();
		if (orig.equals(this.root)) {
			return new File[] {orig};
		} else {
			if (compression.isMultiVolumes()) {
				File target = new File(encode(predecessor.getParentFile(orig)), predecessor.getName(orig));
				ZipVolumeStrategy vol = new ZipVolumeStrategy(target, compression.getNbDigits());
				ArrayList list = new ArrayList(1);
				while (true) {
					File f = vol.getNextFile();
					if (predecessor.exists(f)) {
						list.add(f);
					} else {
						break;
					}
				}
				list.add(vol.getFinalArchive());
				return (File[])list.toArray(new File[list.size()]);
			} else {
				return new File[] {new File(this.encode(orig.getParentFile()), this.encode(orig.getName()))};
			}
		}
	}

	protected File decode(File file) throws FileNameException {
		File orig = file.getAbsoluteFile();
		if (orig.equals(this.root)) {
			return orig;
		} else {
			return new File(this.decode(orig.getParentFile()), this.decode(orig.getName()));
		}
	}

	private String encode(String name) {
		if (name.length() == 0) {
			return "";
		} else {
			return compression.isAddExtension() ? name + CompressionArguments.ZIP_SUFFIX : name;
		}
	}

	private String decode(String name) throws FileNameException {
		if (compression.isAddExtension() && (! name.endsWith(CompressionArguments.ZIP_SUFFIX))) {
			throw new FileNameException("Illegal file name : " + name + ". It is expected to end with '" + CompressionArguments.ZIP_SUFFIX + "'");
		}
		return compression.isAddExtension() ? name.substring(0, name.length() - CompressionArguments.ZIP_SUFFIX.length()) : name;
	}

	protected static class FileNameException extends Exception {
		public FileNameException(String arg0) {
			super(arg0);
		}
	}

	protected static class FilenameFilterAdapter implements FilenameFilter {
		protected FilenameFilter filter;
		protected CompressedFileSystemDriver driver;

		public FilenameFilterAdapter(
				FilenameFilter wrappedFilter,
				CompressedFileSystemDriver driver) {
			this.filter = wrappedFilter;
			this.driver = driver;
		}

		public boolean accept(File dir, String name) {
			try {
				File targetDirectory = driver.decode(dir);
				String targetName = driver.decode(name);
				return filter.accept(targetDirectory, targetName);
			} catch (FileNameException e) {
				logDecodingIssue(""+dir + " / " + name);
				return false;
			}
		}

		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			} else if (! (obj instanceof FilenameFilterAdapter)) {
				return false;
			} else {
				FilenameFilterAdapter other = (FilenameFilterAdapter)obj;
				return 
						EqualsHelper.equals(this.filter, other.filter)
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
		protected CompressedFileSystemDriver driver;

		public FileFilterAdapter(
				FileFilter wrappedFilter,
				CompressedFileSystemDriver driver) {
			this.filter = wrappedFilter;
			this.driver = driver;
		}

		public boolean accept(File filename) {
			File target;
			try {
				target = driver.decode(filename);
				return filter.accept(target);
			} catch (FileNameException e) {
				logDecodingIssue(""+filename);
				return false;
			}

		}

		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			} else if (! (obj instanceof FileFilterAdapter)) {
				return false;
			} else {
				FileFilterAdapter other = (FileFilterAdapter)obj;
				return 
						EqualsHelper.equals(this.filter, other.filter)
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
		
		public static void main(String[] args) {
			File dir = new File("c:\\users\\olivier");
			String[] data = dir.list();
			for (int i=0; i<data.length; i++) {
				System.out.println(data[i]);
			}
		}
	}
}