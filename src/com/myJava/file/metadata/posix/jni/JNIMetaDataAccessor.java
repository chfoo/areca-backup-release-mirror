package com.myJava.file.metadata.posix.jni;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import com.myJava.file.FileSystemManager;
import com.myJava.file.metadata.FileMetaData;
import com.myJava.file.metadata.FileMetaDataAccessor;
import com.myJava.file.metadata.FileMetaDataSerializer;
import com.myJava.file.metadata.posix.ExtendedAttribute;
import com.myJava.file.metadata.posix.ExtendedAttributeList;
import com.myJava.file.metadata.posix.PosixMetaDataImpl;
import com.myJava.file.metadata.posix.PosixMetaDataSerializer;
import com.myJava.file.metadata.posix.jni.wrapper.FileAccessWrapper;
import com.myJava.file.metadata.posix.jni.wrapper.GetACLResult;
import com.myJava.file.metadata.posix.jni.wrapper.GetAttributeNamesResult;
import com.myJava.file.metadata.posix.jni.wrapper.GetAttributeValueResult;
import com.myJava.file.metadata.posix.jni.wrapper.GetDataResult;
import com.myJava.file.metadata.posix.jni.wrapper.SetACLResult;
import com.myJava.file.metadata.posix.jni.wrapper.SetAttributeValueResult;
import com.myJava.file.metadata.posix.jni.wrapper.SetFileModeResult;
import com.myJava.file.metadata.posix.jni.wrapper.SetFileOwnerResult;
import com.myJava.system.OSTool;
import com.myJava.util.log.Logger;

/**
 * Metadata accessor that uses JNI to invoke C functions.
 * <BR>It handles basic attributes (owner, group, permissions, sticky bit, set uid, set gid), extended attributers and ACLs.
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
public class JNIMetaDataAccessor implements FileMetaDataAccessor {
	private static final String DESCRIPTION = "Advanced meta data accessor for Linux systems. It uses lower levels system calls to handle file informations (owner, group, permissions, extended attributes, ACLs and special bits).";
	private static final int BUFFER_SIZE = 1024;
	private static final int BUFFER_MULTIPLIER = 10;
	private static final FileMetaDataSerializer SERIALIZER = new PosixMetaDataSerializer();

	public JNIMetaDataAccessor() {
	}

	public FileMetaData getMetaData(File f, boolean onlyBasicAttributes) throws IOException {
		PosixMetaDataImpl p = new PosixMetaDataImpl();
		String file = f.getAbsolutePath();

		if (! onlyBasicAttributes) {
			// ACL
			if (FileSystemManager.isDirectory(f)) {
				GetACLResult res = FileAccessWrapper.getACL(file, true); 
				if (res.getTranscodedErrorNumber() != FileAccessWrapper.ERR_UNSUPPORTED) {
					if (res.isError()) {
						throw new IOException("Error reading default ACL for " + file + " : " + res.getErrorToString());
					} else {
						if (! res.getContent().isEmpty()) {
							p.setDefaultAcl(res.getContent());	
						}
					}
				}
			}
			GetACLResult res = FileAccessWrapper.getACL(file, false);  
			if (res.getTranscodedErrorNumber() != FileAccessWrapper.ERR_UNSUPPORTED) {
				if (res.isError()) {
					Logger.defaultLogger().fine("ACL for " + file + " : " + res.toString());
					throw new IOException("Error reading access ACL for " + file + " : " + res.getErrorToString());
				} else {
					if (! res.getContent().isEmpty()) {
						p.setAccessAcl(res.getContent());
					}
				}
			}

			// Extended attributes
			GetAttributeNamesResult namesRes = null;
			int i = 1;
			while (namesRes == null || res.getTranscodedErrorNumber() == FileAccessWrapper.ERR_BUFFER_TOO_SMALL) {
				namesRes = FileAccessWrapper.getAttributeNames(file, (i*=BUFFER_MULTIPLIER)*BUFFER_SIZE, false);
			}
			if (res.getTranscodedErrorNumber() != FileAccessWrapper.ERR_UNSUPPORTED) {
				if (namesRes.isError()) {
					Logger.defaultLogger().fine("xattrs for " + file + " : " + namesRes.toString());
					throw new IOException("Error reading extended attribute names for " + file + " : " + namesRes.getErrorToString());
				} else {
					ExtendedAttributeList list = new ExtendedAttributeList();
					Iterator iter = namesRes.iterator();
					while (iter.hasNext()) {
						String name = (String)iter.next();
						GetAttributeValueResult valueRes = null;
						i=1;
						while (valueRes == null || res.getTranscodedErrorNumber() == FileAccessWrapper.ERR_BUFFER_TOO_SMALL) {
							valueRes = FileAccessWrapper.getAttributeValue(file, name, (i*=BUFFER_MULTIPLIER)*BUFFER_SIZE, false);
						}
						if (valueRes.isError()) {
							Logger.defaultLogger().fine("xattr for " + file + " / " + name + " : " + valueRes.toString());
							throw new IOException("Error reading extended (" + name + ") attribute value for " + file + " : " + valueRes.getErrorToString());
						} else {
							list.addAttribute(name, valueRes.getData());
						}
					}
					if (! list.isEmpty()) {
						p.setXattrList(list);
					}
				}

			}
		}

		// Base attributes
		GetDataResult dataRes = FileAccessWrapper.getData(file, false);

		if (dataRes.isError()) {
			throw new IOException("Error reading base attributes for " + file + " : " + dataRes.getErrorToString());
		} else {
			String group = UserGroupTranscoder.getGroupName((int)dataRes.st_gid);
			if (group == null) {
				Logger.defaultLogger().warn("No group found for gid " + dataRes.st_gid);
			}
			p.setGroup(group);
			
			String user = UserGroupTranscoder.getUserName((int)dataRes.st_uid); 
			if (user == null) {
				Logger.defaultLogger().warn("No user found for uid " + dataRes.st_uid);
			}
			p.setOwner(user);
			p.setMode((int)dataRes.st_mode);
			p.setLastmodified(dataRes.st_mtime * 1000);
		}

		return p;
	}

	public FileMetaDataSerializer getMetaDataSerializer() {
		return SERIALIZER;
	}

	public FileMetaData buildEmptyMetaData() {
		return new PosixMetaDataImpl();
	}

	public void setMetaData(File f, FileMetaData abstractAttr) throws IOException {
		PosixMetaDataImpl attrs = (PosixMetaDataImpl)abstractAttr;
		String file = f.getAbsolutePath();

		// Owner / Group
		if (attrs.getOwner() != null && attrs.getGroup() != null) {
			int owner = UserGroupTranscoder.getUserId(attrs.getOwner());
			if (owner == -1) {
				Logger.defaultLogger().warn("No user found for name " + attrs.getOwner());
			}
			
			int group = UserGroupTranscoder.getGroupId(attrs.getGroup());
			if (group == -1) {
				Logger.defaultLogger().warn("No group found for name " + attrs.getGroup());
			}
			
			if (owner == -1 || group == -1) {
				throw new IOException("Invalid owner or group : " + attrs.getOwner() + " (" + owner + ") / " + attrs.getGroup() + " (" + group + ")");
			} else {
				SetFileOwnerResult result = FileAccessWrapper.setFileOwner(file, owner, group, false);
				if (result.isError()) {
					Logger.defaultLogger().warn("Unable to set owner/group for " + file + " : " + result.getErrorToString());
				}
			}
		}

		// Last modification date and mode
		// They are not set on symlinks (not supported)
		if (! FileAccessWrapper.isA((int)attrs.getMode(), FileAccessWrapper.TYPE_LINK)) {
			if (attrs.getMode() != PosixMetaDataImpl.UNDEF_MODE) {
				SetFileModeResult result = FileAccessWrapper.setFileMode(file, attrs.getMode());
				if (result.isError()) {
					Logger.defaultLogger().warn("Unable to set mode for " + file + " : " + result.getErrorToString());
				}
			}

			if (attrs.getLastmodified() != PosixMetaDataImpl.UNDEF_DATE) {
				boolean result = f.setLastModified(attrs.getLastmodified());
				if (! result) {
					Logger.defaultLogger().warn("Unable to set last modification date for " + file);
				}
			}
		}

		// Extended attributes
		if(attrs.getXattrList() != null && ! attrs.getXattrList().isEmpty()) {
			Iterator xattrs = attrs.getXattrList().iterator();
			while (xattrs.hasNext()) {
				ExtendedAttribute xattr = (ExtendedAttribute)xattrs.next();
				SetAttributeValueResult result = FileAccessWrapper.setAttributeValue(file, xattr.getName(), xattr.getData(), false);
				if (result.isError()) {
					Logger.defaultLogger().warn("Unable to set extended attribute " + xattr.getName() + " / " + xattr.getAsString() + " for " + file + " : " + result.getErrorToString());
				}
			}
		}

		// Default ACL
		if (FileSystemManager.isDirectory(f) && attrs.getDefaultAcl() != null && ! attrs.getDefaultAcl().isEmpty()) {
			SetACLResult result = FileAccessWrapper.setACL(file, attrs.getDefaultAcl(), attrs.getDefaultAcl().size(), true);
			if (result.isError()) {
				Logger.defaultLogger().warn("Unable to set default ACL for " + file + " : " + result.getErrorToString());
			}
		}

		// Access ACL
		if (attrs.getAccessAcl() != null && ! attrs.getAccessAcl().isEmpty()) {
			SetACLResult result = FileAccessWrapper.setACL(file, attrs.getAccessAcl(), attrs.getAccessAcl().size(), false);
			if (result.isError()) {
				Logger.defaultLogger().warn("Unable to set access ACL for " + file + " : " + result.getErrorToString());
			}
		}
	}

	public short getType(File f) throws IOException {
		GetDataResult dataRes = FileAccessWrapper.getData(f.getAbsolutePath(), false);
		int mode = (int)dataRes.st_mode;
		
		if (FileAccessWrapper.isA(mode, FileAccessWrapper.TYPE_LINK)) {
			return TYPE_LINK;
		} else if (FileAccessWrapper.isA(mode, FileAccessWrapper.TYPE_BLOCKSPECIALFILE)) {
			return TYPE_BLOCK_SPEC_FILE;
		} else if (FileAccessWrapper.isA(mode, FileAccessWrapper.TYPE_CHARSPECFILE)) {
			return TYPE_CHAR_SPEC_FILE;
		} else if (FileAccessWrapper.isA(mode, FileAccessWrapper.TYPE_DIRECTORY)) {
			return TYPE_DIRECTORY;
		} else if (FileAccessWrapper.isA(mode, FileAccessWrapper.TYPE_FILE)) {
			return TYPE_FILE;
		} else if (FileAccessWrapper.isA(mode, FileAccessWrapper.TYPE_PIPE)) {
			return TYPE_PIPE;
		} else if (FileAccessWrapper.isA(mode, FileAccessWrapper.TYPE_SOCKET)) {
			return TYPE_SOCKET;
		} else {
			throw new IOException("Type not recognized for file " + f.getAbsolutePath() + " : " + dataRes.toString());
		}
	}

	public boolean typeSupported(short type) {
		return true;
	}

	public boolean ACLSupported() {
		return true;
	}

	public boolean extendedAttributesSupported() {
		return true;
	}

	public boolean test() {
		try {			
			getMetaData(new File(OSTool.getUserDir()), false);
			return true;
		} catch (Throwable e) {
			Logger.defaultLogger().warn(this.getClass().getName() + " cannot be used on this system. Got the following error : \"" + e.getClass().getName() + " : " + e.getMessage() + "\"");
			if (e.getMessage().toLowerCase().indexOf("no acl in java.library.path") != -1) {
				Logger.defaultLogger().warn("You should check that the 'acl' package is properly deployed on your computer.\nIn most cases, there should be a 'libacl.so' file or symbolic link somewhere on your filesystem.\nIf it is not the case, you should check for a 'libacl.<some version number>.so' file and create a symbolic link named 'libacl.so' on it.\nIf no such file can be found, just install the 'acl' package using your standard package manager.");
			}
			return false;
		}
	}

	public String getDescription() {
		return DESCRIPTION;
	}
}
