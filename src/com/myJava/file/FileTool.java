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

import com.myJava.configuration.FrameworkConfiguration;
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
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

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

	private static final long DEFAULT_DELETION_DELAY = FrameworkConfiguration.getInstance().getFileToolDelay();
	private static final int BUFFER_SIZE = FrameworkConfiguration.getInstance().getFileToolBufferSize();
	private static final int DELETION_GC_FREQUENCY = (int) (2000 / DEFAULT_DELETION_DELAY);
	private static final int DELETION_MAX_ATTEMPTS = 1000;
	private static final String HASH_ALGORITHM = FrameworkConfiguration.getInstance().getFileHashAlgorithm();

	private static FileTool instance = new FileTool();

	public static FileTool getInstance() {
		return instance;
	}

	private FileTool() {
	}

	public void copy(File sourceFileOrDirectory, File targetParentDirectory)
	throws IOException {
		try {
			copy(sourceFileOrDirectory, targetParentDirectory, null, null);
		} catch (TaskCancelledException ignored) {
			// Never happens since no monitor is set.
		}
	}

	/**
	 * Copy the source file or directory in the parent destination.
	 */
	public void copy(File sourceFileOrDirectory, File targetParentDirectory, TaskMonitor monitor, OutputStreamListener listener)
	throws IOException, TaskCancelledException {
		if (sourceFileOrDirectory == null || targetParentDirectory == null) {
			throw new IllegalArgumentException("Source : "
					+ sourceFileOrDirectory + ", Destination : "
					+ targetParentDirectory);
		}

		if (monitor != null) {
			monitor.checkTaskState();
		}

		if (FileSystemManager.isFile(sourceFileOrDirectory)) {
			copyFile(sourceFileOrDirectory, targetParentDirectory, FileSystemManager.getName(sourceFileOrDirectory), monitor, listener);
		} else {
			File td = new File(targetParentDirectory, FileSystemManager.getName(sourceFileOrDirectory));
			this.createDir(td);
			this.copyDirectoryContent(sourceFileOrDirectory, td, monitor,listener);
		}
	}

	/**
	 * Copy the file to the parent target directory, with the short name passed
	 * as argument.
	 */
	public void copyFile(File sourceFile, File targetDirectory, String targetShortFileName, TaskMonitor monitor, OutputStreamListener listener) 
	throws IOException,	TaskCancelledException {
		if (!FileSystemManager.exists(targetDirectory)) {
			this.createDir(targetDirectory);
		}

		File tf = new File(targetDirectory, targetShortFileName);
		OutputStream outStream = FileSystemManager.getFileOutputStream(tf, false, listener);

		this.copyFile(sourceFile, outStream, true, monitor);
	}

	/**
	 * Copy the source file to the target outputstream
	 */
	public void copyFile(File sourceFile, OutputStream outStream, boolean closeStream, TaskMonitor monitor) 
	throws IOException, TaskCancelledException {
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

	public void copy(InputStream inStream, OutputStream outStream, boolean closeInputStream, boolean closeOutputStream)
	throws IOException {
		try {
			copy(inStream, outStream, closeInputStream, closeOutputStream, null);
		} catch (TaskCancelledException ignored) {
		}
	}

	/**
	 * Copy inStream into outStream.
	 */
	public void copy(InputStream inStream, OutputStream outStream, boolean closeInputStream, boolean closeOutputStream, TaskMonitor monitor)
	throws IOException, TaskCancelledException {

		try {
			byte[] in = new byte[BUFFER_SIZE];
			int nbRead;

			while (true) {
				if (monitor != null) {
					monitor.checkTaskState();
				}
				nbRead = inStream.read(in);
				if (nbRead == -1) {
					break;
				}
				outStream.write(in, 0, nbRead);
			}
		} finally {
			try {
				if (closeInputStream && inStream != null) {
					inStream.close();
				}
			} finally {
				if (closeOutputStream && outStream != null) {
					outStream.close();
				}
			}
		}
	}

	/**
	 * Copy the content of sourceDirectory into targetDirectory <BR>
	 * Example : <BR>- sourceDirectory = c:\toto\sourceDir <BR>-
	 * targetDirectory = d:\myDir <BR>
	 * <BR>
	 * The content of c:\toto\sourceDir will be copied into d:\myDir
	 */
	public void copyDirectoryContent(File sourceDirectory, File targetDirectory, TaskMonitor monitor, OutputStreamListener listener)
	throws IOException,	TaskCancelledException {
		this.createDir(targetDirectory);

		File[] files = FileSystemManager.listFiles(sourceDirectory);
		for (int i = 0; i < files.length; i++) {
			this.copy(files[i], targetDirectory, monitor, listener);
		}
	}

	/**
	 * Delete the directory / file and all its content. <BR>
	 * If "waitForAvailability" is true, the process will wait - for each file
	 * or directory - until it is available. (the thread will be paused) and
	 * will make an attempt every "deletionDelay" milliseconds.
	 */
	public void delete(File fileOrDirectory, boolean waitForAvailability, long deletionDelay, TaskMonitor monitor)
	throws IOException,	TaskCancelledException {
		if (monitor != null) {
			monitor.checkTaskState();
		}

		if (FileSystemManager.isDirectory(fileOrDirectory)) {
			File[] files = FileSystemManager.listFiles(fileOrDirectory);
			for (int i = 0; i < files.length; i++) {
				this.delete(files[i], waitForAvailability, deletionDelay,
						monitor);
			}
		}

		if (waitForAvailability) {
			long retry = 0;
			try {
				while (!FileSystemManager.delete(fileOrDirectory)) {
					retry++;
					if (retry == 10 || retry == 100 || retry == 1000) {
						Logger
						.defaultLogger()
						.warn(
								"Attempted to delete file ("
								+ FileSystemManager
								.getAbsolutePath(fileOrDirectory)
								+ ") during "
								+ (retry * deletionDelay)
								+ " ms but it seems to be locked !");
					}
					if (retry >= DELETION_MAX_ATTEMPTS) {
						String[] files = FileSystemManager
						.list(fileOrDirectory);
						throw new IOException("Unable to delete file : "
								+ FileSystemManager
								.getAbsolutePath(fileOrDirectory)
								+ " - isFile="
								+ FileSystemManager.isFile(fileOrDirectory)
								+ " - Exists="
								+ FileSystemManager.exists(fileOrDirectory)
								+ " - Children="
								+ (files == null ? 0 : files.length)
								+ (files == null || files.length > 0 ? "("
										+ files[0] + " ...)" : ""));
					}
					if (retry % DELETION_GC_FREQUENCY == 0) {
						// Logger.defaultLogger().warn("File deletion (" +
						// FileSystemManager.getAbsolutePath(fileOrDirectory) +
						// ") : Performing a GC.");
						System.gc(); // I know it's not very beautiful ...
						// but it seems to be a bug with old
						// file references (even if all streams
						// are closed)
					}
					Thread.sleep(deletionDelay);
				}
			} catch (InterruptedException ignored) {
			}
		} else {
			FileSystemManager.delete(fileOrDirectory);
		}
	}

	public void delete(File fileOrDirectory, boolean waitForAvailability)
	throws IOException {
		try {
			delete(fileOrDirectory, waitForAvailability, null);
		} catch (TaskCancelledException ignored) {
			// Never happens since no monitor is set
		}
	}

	public void delete(File fileOrDirectory, boolean waitForAvailability, TaskMonitor monitor)
	throws IOException, TaskCancelledException {
		delete(fileOrDirectory, waitForAvailability, DEFAULT_DELETION_DELAY, monitor);
	}

	public void createFile(File destinationFile, String content)
	throws IOException {
		OutputStream fos = FileSystemManager.getFileOutputStream(destinationFile);
		OutputStreamWriter fw = new OutputStreamWriter(fos);
		fw.write(content);
		fw.flush();
		fw.close();
	}

	/**
	 * Return the content of the file as a String.
	 */
	public String getFileContent(File sourceFile)
	throws IOException {
		InputStream inStream = FileSystemManager.getFileInputStream(sourceFile);
		return getInputStreamContent(inStream, true);
	}

	public String getInputStreamContent(InputStream inStream, boolean closeStreamOnExit)
	throws IOException {
		return getInputStreamContent(inStream, null, closeStreamOnExit);
	}

	/**
	 * Return the content of the inputStream as a String.
	 */
	public String getInputStreamContent(InputStream inStream, String encoding, boolean closeStreamOnExit) 
	throws IOException {
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
	 * Return the content of the file as a String array (one string by line).
	 * <BR>
	 * The lines are trimmed and empty lines are ignored.
	 */
	public String[] getFileRows(File sourceFile) 
	throws IOException {
		return getInputStreamRows(FileSystemManager.getFileInputStream(sourceFile), null, true);
	}

	/**
	 * Return the content of the inputStream as a String array (one string by
	 * line). <BR>
	 * The lines are trimmed and empty lines are ignored.
	 */
	public String[] getInputStreamRows(InputStream inStream, String encoding, boolean closeStreamOnExit)
	throws IOException {
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
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.length() != 0) {
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
			reader = new BufferedReader(new InputStreamReader(stream, encoding), BUFFER_SIZE);
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
	public void replaceInFile(File baseFile, String searchString, String newString)
	throws IOException {

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
			return this.isParentOf(parent, FileSystemManager
					.getParentFile(child));
		}
	}

	/**
	 * Return the file's or directory's total length.
	 */
	public long getSize(File fileOrDirectory) 
	throws FileNotFoundException {
		if (FileSystemManager.isFile(fileOrDirectory)) {
			return FileSystemManager.length(fileOrDirectory);
		} else {
			File[] content = FileSystemManager.listFiles(fileOrDirectory);
			long l = 0;

			for (int i = 0; i < content.length; i++) {
				l += getSize(content[i]);
			}

			return l;
		}
	}

	/**
	 * Recursive creation of a directory
	 */
	public void createDir(File directory) 
	throws IOException {
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
	throws IOException, NoSuchAlgorithmException, TaskCancelledException {
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

	/**
	 * Return a new - non existing - temporary file or directory in the user's
	 * main temporary directory. <BR>
	 * <BR>
	 * This method also registers a security shutdown hook that destroys the
	 * temporary file. <BR>
	 * However, it is strongly advised to handle the created file's destruction
	 * explicitly as soon as it is not needed anymore and avoid using hooks
	 */
	public File generateNewWorkingFile(String subdir, String prefix, boolean registerDeleteHook)
	throws IOException {
		File tmp = null;
		int i = 0;
		while (tmp == null || FileSystemManager.exists(tmp)) {
			tmp = new File(OSTool.getTempDirectory() + "/" + subdir, prefix
					+ (i++));
		}

		File parent = FileSystemManager.getParentFile(tmp);
		if (!  FileSystemManager.exists(parent)) {
			FileSystemManager.mkdir(parent);
		}

		// register shutdown hook to destroy the created temporary file
		if (registerDeleteHook) {
			final File toRemove = tmp;
			Runnable rn = new Runnable() {
				public void run() {
					try {
						FileTool.getInstance().delete(toRemove, true);
					} catch (IOException e) {
						Logger.defaultLogger().error(e);
					}
				}
			};
			Thread th = new Thread(rn);
			th.setDaemon(false);
			th.setName("Remove temporary file or directory : "
					+ toRemove.getAbsolutePath());
			Runtime.getRuntime().addShutdownHook(th);
		}
		return tmp;
	}
}