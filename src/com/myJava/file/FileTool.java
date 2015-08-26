package com.myJava.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.copypolicy.CopyPolicy;
import com.myJava.file.driver.DefaultFileSystemDriver;
import com.myJava.system.OSTool;
import com.myJava.util.Util;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
 * File handling utility <BR>
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

public class FileTool {
	private static final int BUFFER_SIZE = FrameworkConfiguration.getInstance()
			.getFileToolBufferSize();
	public static final String HASH_ALGORITHM = FrameworkConfiguration
			.getInstance().getFileHashAlgorithm();

	private static FileTool instance = new FileTool();

	public static FileTool getInstance() {
		return instance;
	}

	private FileTool() {
	}

	public void copy(File sourceFileOrDirectory, File targetParentDirectory)
			throws IOException {
		try {
			copy(sourceFileOrDirectory, targetParentDirectory, null, null,
					null, null);
		} catch (TaskCancelledException ignored) {
			// Never happens since no monitor is set.
		}
	}

	/**
	 * Copy the source file or directory in the parent destination.
	 */
	public void copy(File sourceFileOrDirectory, File targetParentDirectory,
			CopyPolicy policy, Comparator comparator, TaskMonitor monitor,
			OutputStreamListener listener) throws IOException,
			TaskCancelledException {
		if (sourceFileOrDirectory == null || targetParentDirectory == null) {
			throw new IllegalArgumentException("Source : "
					+ sourceFileOrDirectory + ", Destination : "
					+ targetParentDirectory);
		}

		if (monitor != null) {
			monitor.checkTaskState();
		}

		if (FileSystemManager.isFile(sourceFileOrDirectory)) {
			if (policy == null
					|| policy.accept(new File(targetParentDirectory,
							FileSystemManager.getName(sourceFileOrDirectory)))) {
				copyFile(sourceFileOrDirectory, targetParentDirectory,
						FileSystemManager.getName(sourceFileOrDirectory),
						monitor, listener);
			}
		} else {
			File td = new File(targetParentDirectory,
					FileSystemManager.getName(sourceFileOrDirectory));
			this.createDir(td);
			this.copyDirectoryContent(sourceFileOrDirectory, td, policy,
					comparator, monitor, listener);
		}
	}

	/**
	 * Copy the content of sourceDirectory into targetDirectory <BR>
	 * Example : <BR>
	 * - sourceDirectory = c:\toto\sourceDir <BR>
	 * - targetDirectory = d:\myDir <BR>
	 * <BR>
	 * The content of c:\toto\sourceDir will be copied into d:\myDir
	 */
	public void copyDirectoryContent(File sourceDirectory,
			File targetDirectory, CopyPolicy policy, Comparator comparator,
			TaskMonitor monitor, OutputStreamListener listener)
			throws IOException, TaskCancelledException {
		this.createDir(targetDirectory);

		String[] files = FileSystemManager.list(sourceDirectory);
		if (files == null) {
			if (FileSystemManager.exists(sourceDirectory)) {
				Logger.defaultLogger().warn(
						FileSystemManager.getDisplayPath(sourceDirectory)
								+ " : Directory exists but no children.");
			} else {
				Logger.defaultLogger().warn(
						FileSystemManager.getDisplayPath(sourceDirectory)
								+ " : Directory doesn't exist.");
			}
		} else {
			if (comparator != null) {
				Arrays.sort(files, comparator);
			}
			for (int i = 0; i < files.length; i++) {
				this.copy(new File(sourceDirectory, files[i]), targetDirectory, policy, comparator, monitor, listener);
			}
		}
	}

	/**
	 * Copy the file to the parent target directory, with the short name passed
	 * as argument.
	 */
	public void copyFile(File sourceFile, File targetDirectory,
			String targetShortFileName, TaskMonitor monitor,
			OutputStreamListener listener) throws IOException,
			TaskCancelledException {
		if (!FileSystemManager.exists(targetDirectory)) {
			this.createDir(targetDirectory);
		}

		File tf = new File(targetDirectory, targetShortFileName);
		OutputStream outStream = FileSystemManager.getFileOutputStream(tf,
				false, listener);

		this.copyFile(sourceFile, outStream, true, monitor);
	}

	/**
	 * Copy the source file to the target outputstream
	 */
	public void copyFile(File sourceFile, OutputStream outStream,
			boolean closeStream, TaskMonitor monitor) throws IOException,
			TaskCancelledException {
		InputStream in;
		try {
			in = FileSystemManager.getFileInputStream(sourceFile);
		} catch (IOException e) {
			if (closeStream) {
				try {
					outStream.close();
				} catch (IOException closeException) {
					Logger.defaultLogger().error(closeException);
				}
			}
			throw e;
		}
		this.copy(in, outStream, true, closeStream, monitor);
	}

	public void copy(InputStream inStream, OutputStream outStream,
			boolean closeInputStream, boolean closeOutputStream)
			throws IOException {
		try {
			copy(inStream, outStream, closeInputStream, closeOutputStream, null);
		} catch (TaskCancelledException ignored) {
		}
	}

	/**
	 * Copy inStream into outStream.
	 */
	public void copy(InputStream inStream, OutputStream outStream,
			boolean closeInputStream, boolean closeOutputStream,
			TaskMonitor monitor) throws IOException, TaskCancelledException {

		// Chronometer.instance().start("copy");
		try {
			byte[] in = new byte[BUFFER_SIZE];
			int nbRead;

			// Chronometer.instance().start("while");
			while (true) {
				if (monitor != null) {
					monitor.checkTaskState();
				}

				// Chronometer.instance().start("read");
				// int available = inStream.available();
				// if (available == 0) {
				// available = BUFFER_SIZE;
				// }
				// nbRead = inStream.read(in, 0, Math.min(available,
				// in.length));
				nbRead = inStream.read(in);
				// Chronometer.instance().stop("read");

				if (nbRead == -1) {
					break;
				}

				// Chronometer.instance().start("write");
				outStream.write(in, 0, nbRead);
				// Chronometer.instance().stop("write");
			}
			// Chronometer.instance().stop("while");
		} finally {
			// Chronometer.instance().start("close");
			try {
				if (closeInputStream && inStream != null) {
					inStream.close();
				}
			} finally {
				if (closeOutputStream && outStream != null) {
					outStream.close();
				}
			}
			// Chronometer.instance().stop("close");
		}
		// Chronometer.instance().stop("copy");
	}

	public void delete(File fileOrDirectory) throws IOException {
		try {
			DefaultFileSystemDriver drv = new DefaultFileSystemDriver();
			FileSystemManager.forceDelete(fileOrDirectory, null);
		} catch (TaskCancelledException ignored) {
			// Never happens since no monitor is set
		}
	}

	public void createFile(File destinationFile, String content)
			throws IOException {
		OutputStream fos = FileSystemManager
				.getFileOutputStream(destinationFile);
		OutputStreamWriter fw = new OutputStreamWriter(fos);
		fw.write(content);
		fw.flush();
		fw.close();
	}

	/**
	 * Return the content of the file as a String.
	 */
	public String getFileContent(File sourceFile) throws IOException {
		return getFileContent(sourceFile, null);
	}

	public byte[] getFileBytes(File sourceFile) throws IOException {
		byte[] buffer = new byte[100000];
		InputStream in = null;
		try {
			in = FileSystemManager.getFileInputStream(sourceFile);
			int read = 0;
			byte[] ret = new byte[0];
			while ((read = in.read(buffer)) != -1) {
				byte[] newRet = new byte[ret.length + read];
				for (int i = 0; i < ret.length; i++) {
					newRet[i] = ret[i];
				}
				for (int i = 0; i < read; i++) {
					newRet[ret.length + i] = buffer[i];
				}
				ret = newRet;
			}
			return ret;
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	/**
	 * Return the content of the file as a String.
	 */
	public String getFileContent(File sourceFile, String encoding)
			throws IOException {
		InputStream inStream = FileSystemManager.getFileInputStream(sourceFile);
		return getInputStreamContent(inStream, encoding, true);
	}

	public String getInputStreamContent(InputStream inStream,
			boolean closeStreamOnExit) throws IOException {
		return getInputStreamContent(inStream, null, closeStreamOnExit);
	}

	/**
	 * Return the content of the inputStream as a String.
	 */
	public String getInputStreamContent(InputStream inStream, String encoding,
			boolean closeStreamOnExit) throws IOException {
		if (inStream == null) {
			return null;
		}

		char[] b = new char[BUFFER_SIZE];
		StringBuffer content = new StringBuffer();
		try {
			Reader reader = new BufferedReader(
					encoding == null ? new InputStreamReader(inStream)
							: new InputStreamReader(inStream, encoding),
					BUFFER_SIZE);
			int read = 0;
			while ((read = reader.read(b)) != -1) {
				content.append(b, 0, read);
			}
		} finally {
			if (closeStreamOnExit) {
				inStream.close();
			}
		}
		return new String(content);
	}

	/**
	 * Return the content of the file as a String array (one string by line). <BR>
	 * The lines are trimmed and empty lines are ignored.
	 */
	public String[] getFileRows(File sourceFile) throws IOException {
		return getInputStreamRows(
				FileSystemManager.getFileInputStream(sourceFile), null, true);
	}

	/**
	 * Return the content of the inputStream as a String array (one string by
	 * line). <BR>
	 * The lines are trimmed and empty lines are ignored.
	 */
	public String[] getInputStreamRows(InputStream inStream, String encoding,
			boolean closeStreamOnExit) throws IOException {
		return getInputStreamRows(inStream, encoding, -1, closeStreamOnExit);
	}

	/**
	 * Return the content of the inputStream as a String array (one string by
	 * line). <BR>
	 * The lines are trimmed and empty lines are ignored.
	 */
	public String[] getInputStreamRows(InputStream inStream, String encoding,
			long maxRows, boolean closeStreamOnExit) throws IOException {
		if (inStream == null) {
			return null;
		}

		ArrayList v = new ArrayList();
		try {
			BufferedReader reader = new BufferedReader(
					encoding == null ? new InputStreamReader(inStream)
							: new InputStreamReader(inStream, encoding),
					BUFFER_SIZE);
			String line = null;
			while ((line = reader.readLine()) != null && (maxRows != v.size())) {
				if (line.trim().length() != 0) {
					v.add(line);
				}
			}
		} finally {
			if (closeStreamOnExit) {
				inStream.close();
			}
		}
		return (String[]) v.toArray(new String[v.size()]);
	}

	public String getFirstRow(InputStream stream, String encoding)
			throws IOException {
		BufferedReader reader = null;
		String line;
		try {
			reader = new BufferedReader(
					new InputStreamReader(stream, encoding), 20000);
			line = reader.readLine();
		} finally {
			if (reader != null) {
				reader.close();
			} else if (stream != null) {
				stream.close();
			}
		}
		return line;
	}

	/**
	 * Replace all occurences or searchstring by newstring in basefile
	 */
	public void replaceInFile(File baseFile, String searchString,
			String newString) throws IOException {

		// Todo : TO BE OPTIMIZED ! > this version is awful !
		String content = this.getFileContent(baseFile);
		content = Util.replace(content, searchString, newString);
		OutputStreamWriter fw = null;
		try {
			OutputStream fos = FileSystemManager
					.getFileOutputStream(FileSystemManager
							.getAbsolutePath(baseFile));
			fw = new OutputStreamWriter(fos);
			fw.write(content);
			fw.flush();
		} catch (IOException e) {
			throw e;
		} finally {
			fw.close();
		}
	}

	/**
	 * Return true if the file contains the string passed as argument.
	 */
	public boolean checkContains(File baseFile, String searchString)
			throws IOException {
		String content = this.getFileContent(baseFile);
		return (content.indexOf(searchString) != -1);
	}

	/**
	 * Return true if "parent" contains or equals to "child"
	 */
	public boolean isParentOf(File parent, File child) {
		if (child == null || parent == null) {
			return false;
		} else if (FileSystemManager.getAbsoluteFile(parent).equals(
				FileSystemManager.getAbsoluteFile(child))) {
			return true;
		} else {
			return this.isParentOf(parent,
					FileSystemManager.getParentFile(child));
		}
	}

	/**
	 * Return the file's or directory's total length.
	 */
	public long getSize(File fileOrDirectory) throws FileNotFoundException {
		if (FileSystemManager.isFile(fileOrDirectory)) {
			return FileSystemManager.length(fileOrDirectory);
		} else {
			String[] childNames = FileSystemManager.list(fileOrDirectory);
			long l = 0;

			for (int i = 0; i < childNames.length; i++) {
				File f = new File(fileOrDirectory, childNames[i]);
				l += getSize(f);
			}

			return l;
		}
	}

	/**
	 * Recursive creation of a directory
	 */
	public void createDir(File directory) throws IOException {
		if (directory == null || FileSystemManager.exists(directory)) {
			return;
		} else {
			createDir(FileSystemManager.getParentFile(directory));
			FileSystemManager.mkdir(directory);
		}
	}

	/**
	 * Read the file's content and compute a hash code
	 */
	public byte[] hashFileContent(File target, TaskMonitor monitor)
			throws IOException, NoSuchAlgorithmException,
			TaskCancelledException {
		InputStream is = null;
		try {
			is = FileSystemManager.getFileInputStream(target);

			MessageDigest dg = MessageDigest.getInstance(HASH_ALGORITHM);
			byte[] buff = new byte[BUFFER_SIZE];
			int len;
			while ((len = is.read(buff)) != -1) {
				if (monitor != null) {
					monitor.checkTaskState();
				}
				dg.update(buff, 0, len);
			}
			return dg.digest();
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	public synchronized File createNewWorkingDirectory(File parent, String dirName, boolean registerDeleteHook) throws IOException {
		File target = computeNewWorkingFile(parent, null, dirName, registerDeleteHook);
		FileTool.getInstance().createDir(target);
		return target;
	}

	/**
	 * Return a new - non existing - temporary file or directory in the user's
	 * main temporary directory. <BR>
	 * <BR>
	 * This method also registers a security shutdown hook that destroys the
	 * temporary file. <BR>
	 * However, it is strongly advised to handle the created file's destruction
	 * explicitly as soon as it is not needed anymore and avoid using hooks
	 */
	public File generateNewWorkingFile(File rootFile, String subdir, String prefix, boolean registerDeleteHook) throws IOException {
		return computeNewWorkingFile(rootFile, subdir, prefix, registerDeleteHook);
	}

	private File computeNewWorkingFile(File rootFile, String subdir, String prefix, boolean registerDeleteHook) throws IOException {
		File tmp = null;
		int i = 0;
		File root = rootFile;
		if (root == null) {
			root = new File(OSTool.getTempDirectory());
			prefix += Util.getRndLong();
		}
		if (subdir != null) {
			root = new File(root, subdir);
		}
		while (tmp == null || FileSystemManager.exists(tmp)) {
			tmp = new File(root, prefix + (i++));
		}

		File parent = FileSystemManager.getParentFile(tmp);
		createDir(parent);

		// register shutdown hook to destroy the created temporary file
		if (registerDeleteHook) {
			FileCleaningShutdownHook.getInstance().addFile(tmp);
		}

		return tmp;
	}
}