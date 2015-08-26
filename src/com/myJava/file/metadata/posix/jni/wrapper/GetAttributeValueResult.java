package com.myJava.file.metadata.posix.jni.wrapper;

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
public class GetAttributeValueResult extends AbstractMethodResult {
	private byte[] data;

	public byte[] getData() {
		return data;
	}

	public void setValue(byte[] data) {
		this.data = data;
	}
	
	public String getErrorMessage() {
		if (this.transcodedErrorNumber == FileAccessWrapper.ERR_UNSUPPORTED) {
			return "Extended attributes not supported";
		} else {
			return super.getErrorMessage();
		}
	}
	
	public String toString() {
		StringBuffer sb = ToStringHelper.init(this);
		ToStringHelper.append("data", getAsString(), sb);
		ToStringHelper.append("returnCode", returnCode, sb);
		ToStringHelper.append("errorNumber", errorNumber, sb);
		ToStringHelper.append("transcodedErrorNumber", transcodedErrorNumber, sb);
		return ToStringHelper.close(sb);
	}
	
	public String getAsString() {
		if (data == null) {
			return "";
		} else {
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<data.length; i++) {
				sb.append((char)data[i]);
			}
			return sb.toString();
		}
	}
}
