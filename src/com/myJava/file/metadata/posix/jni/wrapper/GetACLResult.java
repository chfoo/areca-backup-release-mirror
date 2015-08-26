package com.myJava.file.metadata.posix.jni.wrapper;

import com.myJava.file.metadata.posix.ACL;
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
public class GetACLResult extends AbstractMethodResult {
	private ACL content;
	
	public GetACLResult() {
		content = new ACL();
	}

	public ACL getContent() {
		return content;
	}

	public String getErrorMessage() {
		if (this.transcodedErrorNumber == FileAccessWrapper.ERR_UNSUPPORTED) {
			return "ACL not supported";
		} else {
			return super.getErrorMessage();
		}
	}

	public void addEntry(int tag, int identifier, boolean r, boolean w, boolean x) {
		content.addEntry(tag, identifier, r, w, x);
	}
	
	public String toString() {
		StringBuffer sb = ToStringHelper.init(this);
		ToStringHelper.append("content", content, sb);
		ToStringHelper.append("returnCode", returnCode, sb);
		ToStringHelper.append("errorNumber", errorNumber, sb);
		ToStringHelper.append("transcodedErrorNumber", transcodedErrorNumber, sb);
		return ToStringHelper.close(sb);
	}
}
