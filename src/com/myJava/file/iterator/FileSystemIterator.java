package com.myJava.file.iterator;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import com.myJava.file.FileSystemManager;
import com.myJava.file.metadata.FileMetaDataAccessor;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskMonitor;


/**
 * This iterator lists all elements of a source directory. They can be either files or subdirectories.
 * Subdirectories are processed recursively.
 * <BR>The iterator can iterate following the path's components lexicographic order if the "sorted" attribute is set to "true".
 * <BR>It also ensures that all parents of a file returned by the "next" method have been previously returned.
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
public class FileSystemIterator implements Iterator, Serializable {
	private static final long serialVersionUID = -1722528311566343624L;
	
	protected FileSystemIteratorFilter filter;
	protected File root;
	protected File baseDirectory;
	protected boolean followSymLinks;
	protected boolean followSubdirectories;
	protected boolean forceAllDirectories;
	protected boolean sorted;
	protected TaskMonitor monitor;
	protected boolean logProgress;
	protected boolean warnDanglingLinks = true;

	protected FileSystemLevel currentLevel;
	protected Stack fileSystemLevels;
	protected Stack priorFiles;
	protected File nextCachedFile;
	protected ArrayList sourceFiles;
	protected int sourceIndex;
	protected FileSystemIterator currentFileSystemSubIterator;
	protected long filtered;
	protected long files;
	protected long directories;
	protected long sourceCount;
	protected boolean initialized = false;
	protected Set pushedRoots;

	public FileSystemIterator(
			File baseDirectory,
			boolean followSymLinks,
			boolean followSubdirectories,
			boolean returnEmptyDirectories,
			boolean sorted
	) {
		this(baseDirectory, baseDirectory, followSymLinks, followSubdirectories, returnEmptyDirectories, sorted);
	}

	protected FileSystemIterator(
			File root,
			File baseDirectory,
			boolean followSymLinks,
			boolean followSubdirectories,
			boolean returnEmptyDirectories,
			boolean sorted
	) {
		init(root, 1, followSymLinks, followSubdirectories, returnEmptyDirectories, sorted);

		this.baseDirectory = baseDirectory;
		FileSystemLevel level = new FileSystemLevel(baseDirectory, null, sorted);
		setCurrentLevel(level);
	}

	public FileSystemIterator(
			File root,
			String[] sources,
			boolean followSymLinks,
			boolean followSubdirectories,
			boolean returnEmptyDirectories,
			boolean sorted
	) {
		init(root, sources.length, followSymLinks, followSubdirectories, returnEmptyDirectories, sorted);
		this.pushedRoots = new HashSet();

		// 1 : Sort (VERY important to ensure consistency : Areca relies on the assumption that files are sorted)
		if (this.sorted) {
			Arrays.sort(sources, new FilePathComparator());
		}

		// 2 : Set data
		sourceFiles = new ArrayList();
		for (int i=0; i<sources.length; i++) {
			File f = new File(root, sources[i]);
			sourceFiles.add(f);
		}
		this.sourceIndex = 0;
	}

	private void init(
			File root,
			int sourceCount,
			boolean followSymLinks,
			boolean followSubdirectories,
			boolean returnEmptyDirectories,
			boolean sorted
	) {
		this.root = root;
		this.sourceCount = sourceCount;
		this.fileSystemLevels = new Stack();
		this.priorFiles = new Stack();
		this.followSymLinks = followSymLinks;
		this.followSubdirectories = followSubdirectories;
		this.forceAllDirectories = returnEmptyDirectories;
		this.sorted = sorted;
	}

	public void setWarnDanglingLinks(boolean warnDanglingLinks) {
		this.warnDanglingLinks = warnDanglingLinks;
	}

	private void checkInitialized() {
		if (! initialized) {
			initialized = true;
			fetchNext();
		}
	}

	public void setMonitor(TaskMonitor monitor) {
		this.monitor = monitor;
	}

	public TaskMonitor getMonitor() {
		return monitor;
	}

	public void setFilter(FileSystemIteratorFilter filter) {
		this.filter = filter;
	}

	public boolean isLogProgress() {
		return logProgress;
	}

	public void setLogProgress(boolean logProgress) {
		this.logProgress = logProgress;
	}

	/**
	 * Return the total number of files that have been filtered
	 */
	public long getFiltered() {
		return filtered;
	}

	public long getFiles() {
		return files;
	}

	public long getDirectories() {
		return directories;
	}

	/**
	 * Same as nextFile()
	 */
	public Object next() {
		return nextFile();
	}

	/**
	 * Returns the next file
	 */
	public File nextFile() {
		checkInitialized();
		File next = this.nextCachedFile;
		fetchNext();
		return next;
	}

	public File getRoot() {
		return root;
	}

	public boolean hasNext() {
		checkInitialized();
		return (this.nextCachedFile != null);
	}

	public void remove() {
		throw new UnsupportedOperationException("Not supported by this implementation.");
	}

	private void setCurrentLevel(FileSystemLevel level) {
		this.currentLevel = level;
	}
	
	protected boolean acceptIteration(File directory) {
		return acceptIteration(directory, directory);
	}

	protected boolean acceptIteration(File directory, File dataDir) {
		short result = filter == null ? FileSystemIteratorFilter.WILL_MATCH_TRUE : filter.acceptIteration(directory, dataDir);
		if (result == FileSystemIteratorFilter.WILL_MATCH_FALSE) {
			this.filtered++;
			return false;
		} else {
			return true;
		}
	}

	protected boolean acceptElement(File element) {
		return acceptElement(element, element);
	}
	
	protected boolean acceptElement(File element, File dataFile) {
		boolean result = filter == null ? true : filter.acceptElement(element, dataFile);
		if (! result) {
			this.filtered++;
		}
		return result;
	}
	
	protected FileSystemIterator buildNewSubIterator(File nextSource) {
		return new FileSystemIterator(root, nextSource, followSymLinks, followSubdirectories, forceAllDirectories, sorted);
	}

	/**
	 * Returns the next element
	 * It can be either a file or a directory
	 */
	private File nextFileOrDirectory() {
		if (currentFileSystemSubIterator != null) {
			if (currentFileSystemSubIterator.hasNext()) {
				// Delegate to the current subIterator
				return (File)currentFileSystemSubIterator.next();
			} else {
				// Close the current subIterator
				this.directories += this.currentFileSystemSubIterator.directories;
				this.files += this.currentFileSystemSubIterator.files;
				this.filtered += this.currentFileSystemSubIterator.filtered;
				this.currentFileSystemSubIterator = null;
				return nextFileOrDirectory();
			}
		} else if (sourceFiles != null && sourceIndex < sourceFiles.size()) {
			// The current source file has been completed ... handle the next source
			File nextSource = (File)sourceFiles.get(sourceIndex++);
			if (monitor != null) {
				this.monitor.addNewSubTask(0.99/sourceCount, FileSystemManager.getDisplayPath(nextSource));
			}

			this.currentFileSystemSubIterator = buildNewSubIterator(nextSource);
			this.currentFileSystemSubIterator.setFilter(this.filter);
			this.currentFileSystemSubIterator.setWarnDanglingLinks(this.warnDanglingLinks);
			if (monitor != null) {
				this.currentFileSystemSubIterator.setMonitor(this.monitor.getCurrentActiveSubTask());
			}
			this.currentFileSystemSubIterator.setLogProgress(this.logProgress);
			this.currentFileSystemSubIterator.pushedRoots = pushedRoots;
			return nextFileOrDirectory();
		} else {
			while (true) {
				if (! this.priorFiles.isEmpty()) {
					// Return priority files or directories
					return (File)priorFiles.pop();
				} else if (currentLevel != null && currentLevel.hasMoreElements()) {
					// Get the next element
					File f = currentLevel.nextElement();

					// Check whether it is a symbolic link or not
					boolean isDirectory = FileSystemManager.isDirectory(f);
					boolean isFile = ! isDirectory;
					int isSymbolicLink = -1; // -1 = unset, 0 = false, 1 = true
					boolean registeredAsDirectory = false;

					// Register directory
					if (isDirectory && (followSymLinks || (isSymbolicLink = isSymbolicLink(f, isSymbolicLink)) == 0)) {
						// check if we can iterate on this directory
						if (followSubdirectories && this.acceptIteration(f)) {
							this.fileSystemLevels.push(this.currentLevel);
							// Progress information
							if (monitor != null) {
								monitor.getCurrentActiveSubTask().addNewSubTask(currentLevel.getCompletionIncrement(), FileSystemManager.getDisplayPath(f));
							}

							this.setCurrentLevel(new FileSystemLevel(f, this.currentLevel, sorted));
							if (logProgress) {
								Logger.defaultLogger().fine("Processing " + FileSystemManager.getDisplayPath(f));
							}
							registeredAsDirectory = true;
						}
					}

					// Progress information
					if (monitor != null && ! registeredAsDirectory) {
						monitor.getCurrentActiveSubTask().addCompletion(currentLevel.getCompletionIncrement());
					}

					// this check is needed because dangling symbolic links may return "false" here ...
					if (FileSystemManager.exists(f)) {
						// Check the file
						if (this.acceptElement(f)) {
							if (isFile) {
								this.files++;

								// Return the file (or push it into the priority stack, with its parents)
								pushFileAndParents(f);
							} else if ((! followSymLinks) && (isSymbolicLink = isSymbolicLink(f, isSymbolicLink)) == 1) {
								// Symbolic link to a directory ... if the "follow symlinks" option is disabled, this case must be handled as a standard file.
								pushFileAndParents(f);
							} else if (forceAllDirectories) {
								this.directories++;

								// Return the file (or push it into the priority stack, with its parents)
								pushFileAndParents(null);
							}
						}
					} else if (warnDanglingLinks && followSymLinks && (isSymbolicLink = isSymbolicLink(f, isSymbolicLink)) == 1) {
						String absPath = FileSystemManager.getAbsolutePath(f);
						String canPath = absPath;
						try {
							canPath = FileSystemManager.getCanonicalPath(f);
						} catch (IOException ignored) {
						}
						if (! canPath.equals(absPath)) {
							canPath += " (" + absPath + ")";
						}
						
						String message = "The following file was not found : " + canPath + ".";
						message += " If you are processing unix file systems, this warning may be caused by dangling symbolic links.";
						message += " This file will be excluded from the backup.";
						Logger.defaultLogger().warn(message);
					}
				} else {
					// Fetch the next fileSystemLevel
					if (monitor != null) {
						// Enforce completion
						monitor.getCurrentActiveSubTask().setCurrentCompletion(1.0);
					}
					if (this.fileSystemLevels.isEmpty()) {
						return null;
					} else {
						this.currentLevel = (FileSystemLevel)this.fileSystemLevels.pop();
					}
				}
			}
		}
	}

	private static int isSymbolicLink(File f, int currentValue) {
		if (currentValue != -1) {
			return currentValue;
		} else {
			try {
				return FileMetaDataAccessor.TYPE_LINK == FileSystemManager.getType(f) ? 1 : 0;
			} catch (IOException e) {
				Logger.defaultLogger().error("Unreadable file : " + FileSystemManager.getDisplayPath(f), e);
				throw new IllegalArgumentException("Unreadable file : " + FileSystemManager.getDisplayPath(f));
			}
		}
	}

	private void pushRoot() {
		File f = this.baseDirectory;
		while (f!= null && (! f.equals(root))) {
			f = FileSystemManager.getParentFile(f);
			if (f != null && ! (pushedRoots.contains(f))) {
				this.priorFiles.push(f);
				this.pushedRoots.add(f);
			}
		}
	}

	private void pushFileAndParents(File file) {
		// push the file
		if (file != null) {
			this.priorFiles.push(file);
		}

		// push parents if needed
		FileSystemLevel level = this.currentLevel;
		boolean push = level != null && this.currentLevel.isDirectoryRoot();
		while(
				(level != null)
				&& (! level.isHasBeenReturned())
		) {
			if (push) {
				this.priorFiles.push(level.getRoot());
				this.directories++;
			}
			level.setHasBeenReturned(true);
			level = level.getParent();
			push = true;
		}

		// Push root directory
		if (level == null) {
			pushRoot();
		}
	}

	private void fetchNext() {
		this.nextCachedFile = nextFileOrDirectory();
	}
}

