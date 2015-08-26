package com.myJava.file.driver;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import com.myJava.file.OutputStreamListener;
import com.myJava.file.ThrottledOutputStream;
import com.myJava.object.ToStringHelper;

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
public class ThrottledFileSystemDriver extends AbstractLinkableFileSystemDriver {

	private ThrottleHandler tHandler;
	private double maxThroughput = -1;
	
	public ThrottledFileSystemDriver(FileSystemDriver predecessor, double maxThroughputKbPerSecond) {
		super();
		this.maxThroughput = maxThroughputKbPerSecond;
		this.tHandler = new ThrottleHandler(maxThroughputKbPerSecond);
		setPredecessor(predecessor);
	}

	public OutputStream getFileOutputStream(File file) throws IOException {
		return new ThrottledOutputStream(super.getFileOutputStream(file), tHandler);
	}

	public OutputStream getFileOutputStream(File file, boolean append, OutputStreamListener listener) throws IOException {
		return new ThrottledOutputStream(super.getFileOutputStream(file, append, listener), tHandler);
	}

	public OutputStream getFileOutputStream(File file, boolean append) throws IOException {
		return new ThrottledOutputStream(super.getFileOutputStream(file, append), tHandler);
	}

	public OutputStream getCachedFileOutputStream(File file) throws IOException {
		return new ThrottledOutputStream(super.getCachedFileOutputStream(file), tHandler);
	}
	
	public ThrottleHandler getThrottleHandler() {
		return tHandler;
	}

	public void settHandler(ThrottleHandler tHandler) {
		this.tHandler = tHandler;
	}

	public String toString() {
		StringBuffer sb = ToStringHelper.init(this);
		ToStringHelper.append("Predecessor", this.predecessor, sb);
		ToStringHelper.append("MaxThroughput", this.maxThroughput, sb);
		return ToStringHelper.close(sb);
	}
}
