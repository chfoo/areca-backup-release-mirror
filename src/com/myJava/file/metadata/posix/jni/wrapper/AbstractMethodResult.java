package com.myJava.file.metadata.posix.jni.wrapper;

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
public abstract class AbstractMethodResult {
	
	protected int returnCode = 0;
	protected int errorNumber = 0;
	protected int transcodedErrorNumber = 0;

	public boolean isError() {
		return returnCode != 0;
	}
	
	public String getErrorToString() {
		return getErrorMessage() + " (Error " + errorNumber + ")";
	}
	
	public String getErrorMessage() {
		if (! isError()) {
			return "ok";
		} else if (transcodedErrorNumber == FileAccessWrapper.ERR_NOT_ENOUGH_MEMORY) {
			return "Not enough memory";
		} else if (transcodedErrorNumber == FileAccessWrapper.ERR_UNSUPPORTED) {
			return "Unsupported operation";
		} else if (transcodedErrorNumber == FileAccessWrapper.ERR_NAME_TOOLONG) {
			return "Name too long";
		} else if (transcodedErrorNumber == FileAccessWrapper.ERR_NOT_A_DIRECTORY) {
			return "Not a directory";
		} else if (transcodedErrorNumber == FileAccessWrapper.ERR_FILE_NOT_FOUND) {
			return "File not found";
		} else if (transcodedErrorNumber == FileAccessWrapper.ERR_BUFFER_TOO_SMALL) {
			return "Buffer too small";
		} else if (transcodedErrorNumber == FileAccessWrapper.ERR_ATTRIBUTE_NOT_FOUND) {
			return "Attribute not found";
		} else if (transcodedErrorNumber == FileAccessWrapper.ERR_INVALID_DATA) {
			return "Invalid data";
		} else if (transcodedErrorNumber == FileAccessWrapper.ERR_NOT_ENOUGH_DISK_SPACE) {
			return "Not enough disk space";
		} else if (transcodedErrorNumber == FileAccessWrapper.ERR_PERMISSION_DENIED) {
			return "Permission denied";
		} else if (transcodedErrorNumber == FileAccessWrapper.ERR_RO_FILESYSTEM) {
			return "Read-only filesystem";
		} else if (transcodedErrorNumber == FileAccessWrapper.ERR_DISK_QUOTA) {
			return "Disk quota error";
		} else if (transcodedErrorNumber == FileAccessWrapper.ERR_INTERNAL) {
			return "Internal error";
		} else if (transcodedErrorNumber == FileAccessWrapper.ERR_IO) {
			return "I/O error";
		} else if (transcodedErrorNumber == FileAccessWrapper.ERR_NOT_IMPLEMENTED) {
			return "Not implemented";
		} else if (transcodedErrorNumber == FileAccessWrapper.ERR_LOOP) {
			return "Recursive symbolic link";
		} else if (transcodedErrorNumber == FileAccessWrapper.ERR_ACCESS_DENIED) {
			return "Access denied";
		} else {
			return "Unexpected error";
		}
	}

	public int getReturnCode() {
		return returnCode;
	}

	public int getErrorNumber() {
		return errorNumber;
	}

	public int getTranscodedErrorNumber() {
		return transcodedErrorNumber;
	}

	public void setError(int returnCode, int errorNumber, int transcodedErrorNumber) {
		this.returnCode = returnCode;
		this.errorNumber = errorNumber;
		this.transcodedErrorNumber = transcodedErrorNumber;
	}
}
