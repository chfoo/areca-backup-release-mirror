package com.application.areca.metadata.content;

import com.application.areca.impl.FileSystemRecoveryEntry;
import com.application.areca.metadata.MetadataConstants;
import com.application.areca.metadata.MetadataEncoder;
import com.myJava.file.delta.sequence.HashSequence;
import com.myJava.file.delta.sequence.SequenceAdapter;
import com.myJava.util.Util;
import com.myJava.util.log.Logger;

/**
 * Class defining the physical content of an archive.
 * <BR>It is implemented as a set of RecoveryEntries.
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
public class ArchiveContentParser implements MetadataConstants {
    
    protected static String serialize(FileSystemRecoveryEntry entry) {
    	return MetadataEncoder.getInstance().encode(entry.getKey()) + SEPARATOR + entry.getSize();
    }
    
    protected static String serialize(FileSystemRecoveryEntry entry, String shaBase64) {
    	return MetadataEncoder.getInstance().encode(entry.getKey()) + SEPARATOR + shaBase64;
    }
    
    protected static String serialize(FileSystemRecoveryEntry entry, HashSequence sequence) {
    	byte[] serialized = SequenceAdapter.getInstance().serialize(sequence);
    	return MetadataEncoder.getInstance().encode(entry.getKey()) + SEPARATOR + Util.base64Encode(serialized);
    }
    
	public static long interpretAsLength(String name, String data) {
    	try {
			data = data.trim();
			if (data == null || data.length() == 0) {
				Logger.defaultLogger().warn("Error reading data for file : " + name + " (" + data + ").");
				return 0;
			} else {
				return Integer.parseInt(data);
			}
		} catch (RuntimeException e) {
			Logger.defaultLogger().error("Error reading data for file : " + name + " (" + data + ").");
			throw e;
		}
    }
    
    public static byte[] interpretAsHash(String name, String data) {
    	if (data == null) {
			Logger.defaultLogger().warn("Error reading data for file : " + name + " (" + data + ").");
    		return null;
    	}
		data = data.trim();
    	if (data.length() == 0) {
			Logger.defaultLogger().warn("Error reading data for file : " + name + " (" + data + ").");
			return null;
		}
    	try {
			return Util.base64Decode(data);
		} catch (RuntimeException e) {
			Logger.defaultLogger().error("Error reading hash from data for file : " + name + " (" + data + ").");
			throw e;
		}
    }
    
    public static HashSequence interpretAsSequence(String name, String data) {
    	try {
			data = data.trim();
			if (data == null || data.length() == 0) {
				Logger.defaultLogger().warn("Error reading sequence for file : " + name + " (" + data + ").");
				return null;
			} else {
				byte[] serialized = Util.base64Decode(data);
				return SequenceAdapter.getInstance().deserialize(serialized);
			}
		} catch (RuntimeException e) {
			Logger.defaultLogger().error("Error reading sequence from data for file : " + name + " (" + data + ").");
			throw e;
		}
    }
}
