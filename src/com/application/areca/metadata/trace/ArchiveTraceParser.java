package com.application.areca.metadata.trace;

import java.io.File;
import java.io.IOException;

import com.application.areca.impl.FileSystemRecoveryEntry;
import com.application.areca.metadata.MetadataConstants;
import com.application.areca.metadata.MetadataEncoder;
import com.myJava.file.FileSystemManager;
import com.myJava.file.metadata.FileMetaData;
import com.myJava.file.metadata.FileMetaDataAccessor;
import com.myJava.file.metadata.FileMetaDataAccessorHelper;
import com.myJava.file.metadata.FileMetaDataSerializationException;
import com.myJava.file.metadata.FileMetaDataSerializer;
import com.myJava.util.log.Logger;

/**
 * FORMAT :
 * <BR>File : 		f[NAME];[SIZE];[DATE];[HASH];[PERMS] or f[NAME];[SIZE];[DATE];;[PERMS]	-> Hash = "[SIZE];[DATE]" or "[HASH]" if present
 * <BR>Directory : 	d[NAME];[DATE];[PERMS]													-> Hash = ""
 * <BR>SymLink : 	s[NAME];[d/f][PATH];[DATE];[PERMS]										-> Hash = "[d/f][PATH]"
 * <BR>Pipe : 		p[NAME];[DATE];[PERMS]													-> Hash = ""
 * <BR>'@' are reencoded as '@@'
 * <BR>';' are reencoded as '@P'
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
public class ArchiveTraceParser {

	/**
	 * Parses the entry's trace and extract its size.
	 */
	public static long extractFileSizeFromTrace(String trace) {
		try {
			int idx = trace.indexOf(MetadataConstants.SEPARATOR);
			return Long.parseLong(trace.substring(0, idx));
		} catch (RuntimeException e) {
			processException(trace, e);
			throw e;
		}
	}

	private static void processException(String trace, Exception e) {
		Logger.defaultLogger().error("Error processing trace : [" + trace + "]", e);
	}
	
	// size;date[;hash;attributes]
	public static String extractHashFromTrace(String trace) {
		int idx1 = trace.indexOf(MetadataConstants.SEPARATOR);					// size
		int idx2 = trace.indexOf(MetadataConstants.SEPARATOR, idx1 + 1);		// date

		if (idx2 < 0) {
			return trace;
		} else {
			return trace.substring(0, idx2);
		}
	}
	
	/**
	 * Return the SHA Hash code that is contained in the trace as a base64-encoded string
	 * <BR>The method assumes that the SHA hash is actually present
	 * @param trace
	 * @return
	 */
	// size;date[;hash;attributes]
	public static String extractShaFromTrace(String trace) throws FileMetaDataSerializationException {
		int idx1 = trace.indexOf(MetadataConstants.SEPARATOR);					// size
		int idx2 = trace.indexOf(MetadataConstants.SEPARATOR, idx1 + 1);		// date

		if (idx2 < 0) {
			return null;
		} else {
			int idx3 = trace.indexOf(MetadataConstants.SEPARATOR, idx2 + 1);		// attributes
			
			if (idx3 == -1) {
				throw new FileMetaDataSerializationException("Invalid trace string : [" + trace + "]; failed attempting to find hash data.");
			}
			return trace.substring(idx2 + 1, idx3);
		}
	}
	
	public static FileMetaData extractAttributesFromEntry(String key, char type, String hash, long version) throws FileMetaDataSerializationException {
		FileMetaData atts;
		if (type == MetadataConstants.T_DIR) {
			// Directory
			atts = ArchiveTraceParser.extractDirectoryAttributesFromTrace(hash, version);
		} else if (type == MetadataConstants.T_FILE) {
			// File
			atts = ArchiveTraceParser.extractFileAttributesFromTrace(hash, version);
		} else if (type == MetadataConstants.T_SYMLINK) {
			// Symlink
			atts = ArchiveTraceParser.extractSymLinkAttributesFromTrace(hash, version);
		} else if (type == MetadataConstants.T_PIPE) {
			// Pipe
			atts = ArchiveTraceParser.extractPipeAttributesFromTrace(hash, version);
		} else {
			throw new FileMetaDataSerializationException("Unsupported type for " + key + " : " + type + " / " + hash);
		}
		
		return atts;
	}

	// size;date[;hash;attributes] (version >=7) or size;date[;attributes] (version <7)
	public static FileMetaData extractFileAttributesFromTrace(String trace, long version) throws FileMetaDataSerializationException {
		try {
			FileMetaData data = null;
			int idx1 = trace.indexOf(MetadataConstants.SEPARATOR);				// size
			int idx2 = trace.indexOf(MetadataConstants.SEPARATOR, idx1 + 1);	// date

			if (idx2 < 0) {
				data = FileMetaDataAccessorHelper.getFileSystemAccessor().buildEmptyMetaData();
				data.setLastmodified(Long.parseLong(trace.substring(idx1 + 1)));
			} else {
				String strPerms;
				
				if (version < 7) {
					strPerms = trace.substring(idx2 + 1);
				} else {
					int idx3 = trace.indexOf(MetadataConstants.SEPARATOR, idx2 + 1);	// hash
					strPerms = trace.substring(idx3 + 1);
				}
				data = FileMetaDataAccessorHelper.getFileSystemAccessor().getMetaDataSerializer().deserialize(strPerms, version);
				if (data != null) {
					data.setLastmodified(Long.parseLong(trace.substring(idx1 + 1, idx2)));
				}
			}
			
			if (data != null && data.getLastmodified() < 0) {
				Logger.defaultLogger().warn("CAUTION : Negative modification date (" + data.getLastmodified() + "). It will be set to 0.");
				data.setLastmodified(0);
			}
			
			return data;
		} catch (RuntimeException e) {
			processException(trace, e);
			throw e;
		}
	}
	
	// date[;attributes]
	public static FileMetaData extractDirectoryAttributesFromTrace(String trace, long version) throws FileMetaDataSerializationException {
		try {
			FileMetaData data = null;
			int idx1 = trace.indexOf(MetadataConstants.SEPARATOR);			// date
			
			if (idx1 < 0) {
				data = FileMetaDataAccessorHelper.getFileSystemAccessor().buildEmptyMetaData();
				data.setLastmodified(Long.parseLong(trace));
			} else {
				data = FileMetaDataAccessorHelper.getFileSystemAccessor().getMetaDataSerializer().deserialize(trace.substring(idx1 + 1), version);            
				if (data != null) {
					data.setLastmodified(Long.parseLong(trace.substring(0, idx1)));
				}
			}
			
			if (data != null && data.getLastmodified() < 0) {
				Logger.defaultLogger().warn("CAUTION : Negative modification date (" + data.getLastmodified() + "). It will be set to 0.");
				data.setLastmodified(0);
			}
			
			return data;
		} catch (RuntimeException e) {
			processException(trace, e);
			throw e;
		}
	}
	
	// <type>path[;date;attributes]
	public static FileMetaData extractSymLinkAttributesFromTrace(String trace, long version) throws FileMetaDataSerializationException {
		try {
			FileMetaData data = null;
			int idx1 = trace.indexOf(MetadataConstants.SEPARATOR);			// path
			
			if (idx1 < 0) {
				// No meta data at all -> backward compatibility with versions previous to 6.1
				data = FileMetaDataAccessorHelper.getFileSystemAccessor().buildEmptyMetaData();
			} else {
				// Meta data found
				int idx2 = trace.indexOf(MetadataConstants.SEPARATOR, idx1 + 1);		// date
				
				if (idx2 < 0) {
					data = FileMetaDataAccessorHelper.getFileSystemAccessor().buildEmptyMetaData();
					data.setLastmodified(Long.parseLong(trace.substring(idx1 + 1)));
				} else {
					data = FileMetaDataAccessorHelper.getFileSystemAccessor().getMetaDataSerializer().deserialize(trace.substring(idx2 + 1), version);            
					if (data != null) {
						data.setLastmodified(Long.parseLong(trace.substring(idx1 + 1, idx2)));
					}
				}

				if (data != null && data.getLastmodified() < 0) {
					Logger.defaultLogger().warn("CAUTION : Negative modification date (" + data.getLastmodified() + "). It will be set to 0.");
					data.setLastmodified(0);
				}
			}

			return data;
		} catch (RuntimeException e) {
			processException(trace, e);
			throw e;
		}
	}

	// <type>path[;date;attributes]
	public static String extractSymLinkPathFromTrace(String trace) {
		try {
			int idx = trace.indexOf(MetadataConstants.SEPARATOR);
			String p;
			if (idx == -1) {
				p = trace.substring(1);
			} else {
				p = trace.substring(1, idx);
			}
			return MetadataEncoder.getInstance().decode(p);
		} catch (RuntimeException e) {
			processException(trace, e);
			throw e;
		}
	}

	// <type>path[;date;attributes]
	public static boolean extractSymLinkFileFromTrace(String trace) {
		try {
			return trace.charAt(0) == MetadataConstants.T_FILE;
		} catch (RuntimeException e) {
			processException(trace, e);
			throw e;
		}
	}
	
	// <type>path[;date;attributes]
	public static FileMetaData extractPipeAttributesFromTrace(String trace, long version) throws FileMetaDataSerializationException {
		return extractDirectoryAttributesFromTrace(trace, version);
	}

	/**
	 * Builds the key + hash
	 */
	protected static String serialize(FileSystemRecoveryEntry entry, boolean trackSymlinks, String shaBase64) throws IOException, FileMetaDataSerializationException {
		if (entry == null) {
			return null;
		}
		
		// Serialize
		StringBuffer sb = new StringBuffer();
		short type = FileSystemManager.getType(entry.getFile());
		if (trackSymlinks && FileMetaDataAccessor.TYPE_LINK == type) {      
			sb
			.append(MetadataConstants.T_SYMLINK)                
			.append(MetadataEncoder.getInstance().encode(entry.getKey()))
			.append(MetadataConstants.SEPARATOR)
			.append(hash(entry, true))
			.append(MetadataConstants.SEPARATOR)
			.append(FileSystemManager.lastModified(entry.getFile()));
		} else if (trackSymlinks && FileMetaDataAccessor.TYPE_PIPE == type) {      
			sb
			.append(MetadataConstants.T_PIPE)                
			.append(MetadataEncoder.getInstance().encode(entry.getKey()))
			.append(MetadataConstants.SEPARATOR)
			.append(FileSystemManager.lastModified(entry.getFile())); 
		} else if (FileSystemManager.isFile(entry.getFile())) {
			sb
			.append(MetadataConstants.T_FILE)
			.append(MetadataEncoder.getInstance().encode(entry.getKey()))
			.append(MetadataConstants.SEPARATOR)
			.append(hash(entry, false)) 
			.append(MetadataConstants.SEPARATOR);
			
			if (shaBase64 != null) {
				sb.append(shaBase64);
			}
		} else {
			sb
			.append(MetadataConstants.T_DIR)
			.append(MetadataEncoder.getInstance().encode(entry.getKey()))
			.append(MetadataConstants.SEPARATOR)
			.append(FileSystemManager.lastModified(entry.getFile())); 
		}
		
		// Get metadata
		FileMetaDataSerializer serializer = FileMetaDataAccessorHelper.getFileSystemAccessor().getMetaDataSerializer();
		File target = trackSymlinks ? entry.getFile() : FileSystemManager.getCanonicalFile(entry.getFile());
		sb.append(MetadataConstants.SEPARATOR);
		serializer.serialize(FileSystemManager.getMetaData(target, false), sb);

		// Return the result
		return sb.toString();
	}  

	/**
	 * Builds the hash key
	 */
	public static String hash(
			FileSystemRecoveryEntry fEntry, 
			boolean asLink
	) throws IOException {
		
		if (fEntry == null) {
			return null;
		} else if (asLink) {
			char prefix;
			if (FileSystemManager.isDirectory(fEntry.getFile())) {
				prefix = MetadataConstants.T_DIR;
			} else {
				prefix = MetadataConstants.T_FILE;
			}
			return prefix + MetadataEncoder.getInstance().encode(FileSystemManager.getCanonicalPath(fEntry.getFile()));
		} else if (FileSystemManager.isFile(fEntry.getFile())) {
			return new StringBuffer()
			.append(fEntry.getSize())
			.append(MetadataConstants.SEPARATOR)
			.append(FileSystemManager.lastModified(fEntry.getFile()))
			.toString();
		} else {
			throw new IllegalArgumentException("Only files are accepted. " + fEntry.getKey() + " is not a file.");
		}
	}  

	/**
	 * Checks whether the entry has been modified
	 */
	public static boolean hasBeenModified(String newHash, String oldHash) {
		return ! newHash.equals(oldHash);
	}
}
