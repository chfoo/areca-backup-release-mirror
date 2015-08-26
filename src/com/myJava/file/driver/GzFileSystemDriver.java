package com.myJava.file.driver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.myJava.file.OutputStreamListener;

/**
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
public class GzFileSystemDriver extends AbstractLinkableFileSystemDriver {

	public GzFileSystemDriver(FileSystemDriver predecessor) {
		super();
		setPredecessor(predecessor);
	}

	public InputStream getFileInputStream(File file) throws IOException {
		return new GZIPInputStream(super.getFileInputStream(file));
	}

	public OutputStream getFileOutputStream(File file) throws IOException {
		return new GZIPOutputStream(super.getFileOutputStream(file), true);
	}

	public OutputStream getFileOutputStream(File file, boolean append, OutputStreamListener listener) throws IOException {
		return new GZIPOutputStream(super.getFileOutputStream(file, append, listener), true);
	}

	public OutputStream getFileOutputStream(File file, boolean append) throws IOException {
		return new GZIPOutputStream(super.getFileOutputStream(file, append), true);
	}

	public OutputStream getCachedFileOutputStream(File file) throws IOException {
		return new GZIPOutputStream(super.getCachedFileOutputStream(file), true);
	}

	public InputStream getCachedFileInputStream(File file) throws IOException {
		return new GZIPInputStream(super.getCachedFileInputStream(file));
	}
}
