/*
 * @(#)ZipOutputStream.java	1.27 03/02/07
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.myJava.file.archive.zip64;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipException;

import com.myJava.file.multivolumes.VolumeOutputStream;
import com.myJava.file.multivolumes.VolumeStrategy;
import com.myJava.util.collections.SerializedCollection;
import com.myJava.util.log.Logger;

/**
 * <BR>This class was derived from the original java.util.zip.ZipOutputStream.
 * <BR>The following modifications were made :
 * <BR>- No more control over duplicates entries
 * <BR>- Zip64 / Zip32 support
 * <BR>- Uses SerializedCollections to ensure memory capacity
 * <BR>- Package and name change
 * <BR>- Splitting management
 * <BR>- STORE method was removed
 * <BR>- Use EXT blocks to store data
 * @author Olivier Petrucci 
 * <BR>
 * <BR>CAUTION :
 * <BR>This file has been integrated into Areca.
 * <BR>It is has also possibly been adapted to meet Areca's needs. If such modifications has been made, they are described above.
 * <BR>Thanks to the authors for their work.
 *
 */
public class ZipOutputStream 
extends DeflaterOutputStream 
implements ZipConstants {

	private static int SIZE_LOC = SIZE_INT + 3*SIZE_SHORT + 4*SIZE_INT + 2*SIZE_SHORT;
	private static int SIZE_EXT_32 = 4*SIZE_INT;
	private static int SIZE_EXT_64 = 2*SIZE_INT + 2*SIZE_LONG;
	private static int SIZE_CEN = SIZE_INT + 4*SIZE_SHORT + 4*SIZE_INT+ 5*SIZE_SHORT + 2*SIZE_INT;
	private static int SIZE_Z64_END = SIZE_INT + SIZE_LONG + 2*SIZE_SHORT + 2*SIZE_INT + 4*SIZE_LONG + 2*SIZE_INT + SIZE_LONG + SIZE_INT;
	private static int SIZE_END = SIZE_INT + 4*SIZE_SHORT + 2*SIZE_INT + SIZE_SHORT;

	private static long ZIP32_ENTRY_SIZE_LIMIT = 4294967295L;
	private static long ZIP32_OVERALL_SIZE_LIMIT = 4294967295L;
	private static long ZIP32_MAX_ENTRIES = 65535L;

	private static String ZIP32_OVERALL_SIZE_MESSAGE = "Archive too big : Zip32 archives can't grow over " + (long)(ZIP32_OVERALL_SIZE_LIMIT/1024) + " kbytes. Use Zip64 instead.";

	private ZipEntry entry;
	private SerializedCollection entries;

	private CRC32 crc = new CRC32();
	private long totalWritten;
	private String comment;
	private VolumeStrategy volumeStrategy;
	private int CENStart = 0;
	private int Z64EODRStart = 0;
	private boolean useZip64 = false;
	private List entryCountByDiskNumber = new ArrayList();
	private boolean disableSizeCheck = false; // Useful for zip32 archives, in case of errors

	private boolean finished = false;
	private boolean opened = false;
	private boolean closed = false;

	private Charset charset = Charset.forName(DEFAULT_CHARSET);

	private void ensureOpen() throws IOException {
		if (closed) {
			throw new IOException("Stream closed");
		}

		if (! opened) {
			opened = true;
			if (this.volumeStrategy != null) {
				//writeInt(MVSIG);
			}
		}
	}

	public ZipOutputStream(OutputStream out, boolean useZip64) {
		super(out, new Deflater(Deflater.DEFAULT_COMPRESSION, true));
		usesDefaultDeflater = true;
		this.useZip64 = useZip64;
		try {
			this.entries = new ZipEntrySerializedCollection();
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw new IllegalStateException(e);
		}
	}

	public ZipOutputStream(VolumeStrategy strategy, long volumeSize, boolean useZip64) {
		this(new VolumeOutputStream(strategy, volumeSize), useZip64);
		this.volumeStrategy = strategy;
	}

	private void addEntryCount() {
		int volume = this.volumeStrategy == null ? 0 : this.volumeStrategy.getCurrentVolumeNumber();
		while (volume >= this.entryCountByDiskNumber.size()) {
			this.entryCountByDiskNumber.add(new Long(0));
		}

		Long currentCount = (Long)this.entryCountByDiskNumber.get(volume);
		this.entryCountByDiskNumber.set(volume, new Long(currentCount.longValue() + 1));
	}

	private long getEntryCount() {
		int volume = this.volumeStrategy == null ? 0 : this.volumeStrategy.getCurrentVolumeNumber();
		if (volume >= this.entryCountByDiskNumber.size()) {
			return 0;
		} else {
			Long currentCount = (Long)this.entryCountByDiskNumber.get(volume);
			return currentCount.longValue();
		}
	}

	public void setComment(String comment) {
		if (comment != null && comment.length() > 0xffff/3 
				&& ZipStringEncoder.encode(comment, charset).length > 0xffff) {
			throw new IllegalArgumentException("ZIP file comment too long.");
		}
		this.comment = comment;
	}

	public boolean isUseZip64() {
		return useZip64;
	}

	public void setUseZip64(boolean useZip64) {
		this.useZip64 = useZip64;
	}

	public void setLevel(int level) {
		def.setLevel(level);
	}

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(Charset charset) {
		if (charset != null) {
			this.charset = charset;
		}
	}

	public void putNextEntry(ZipEntry e) throws IOException {
		if ((!useZip64) && this.entries.size() >= ZIP32_MAX_ENTRIES) {
			throw new IOException("Too many files in archive. Zip32 archive format does not allow to store more than " + ZIP32_MAX_ENTRIES + " files.");
		}

		ensureOpen();
		if (entry != null) {
			closeEntry();	// close previous entry
		}
		if (e.time == -1) {
			e.setTime(System.currentTimeMillis());
		}
		if (e.getMethod() == -1) {
			e.setMethod(ZipEntry.DEFLATED);	// use default method
		}

		e.flag = 8; 		// bit 3 of general purpose bit flag
		if (charset.name().equals(CHARSET_UTF8)) {
			e.flag += 2048; // bit 11 of general purpose bit flag
		}

		if (useZip64) {
			e.version = ZIP64VERSION;
		} else {
			e.version = ZIPVERSION;
		}
		e.offset = volumeStrategy == null ? totalWritten : ((VolumeOutputStream)out).getWrittenInCurrentVolume();
		e.volumeNumber = volumeStrategy == null ? 0 : volumeStrategy.getCurrentVolumeNumber();
		writeLOC(e);
		entry = e;
	}

	public void closeEntry() throws IOException {
		ensureOpen();
		ZipEntry e = entry;
		if (e != null) {
			def.finish();
			while (!def.finished()) {
				deflate();
			}

			e.setSize(getTotalIn());
			e.csize = getTotalOut();
			e.crc = crc.getValue();

			if ((!useZip64) && e.getSize() > ZIP32_ENTRY_SIZE_LIMIT) {
				throw new IOException(e.name + " is too big (" + (long)(e.getSize() / 1024) + " kbytes). Zip32 archives can't store files bigger than " + (long)(ZIP32_ENTRY_SIZE_LIMIT / 1024) + " kbytes.");
			}

			writeEXT(e);

			resetDeflater();
			totalWritten += e.csize;
			crc.reset();
			entry = null;
			entries.add(e);
		}
	}

	public synchronized void write(byte[] b, int off, int len)
	throws IOException {
		if ((!useZip64) && (len+totalWritten) > ZIP32_OVERALL_SIZE_LIMIT) {
			this.disableSizeCheck = true;
			throw new IOException(ZIP32_OVERALL_SIZE_MESSAGE);
		}

		ensureOpen();
		if (off < 0 || len < 0 || off > b.length - len) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}

		if (entry == null) {
			throw new ZipException("no current ZIP entry");
		}
		super.write(b, off, len);
		crc.update(b, off, len);
	}

	public void finish() throws IOException {
		try {
			ensureOpen();
			if (finished) {
				return;
			}
			if (entry != null) {
				closeEntry();
			}

			entries.lock();

			// write central directory
			this.CENStart = volumeStrategy == null ? 0 : volumeStrategy.getCurrentVolumeNumber();
			long offRelativeToCurrentDisk = volumeStrategy == null ? totalWritten : ((VolumeOutputStream)out).getWrittenInCurrentVolume();
			long off = totalWritten;
			Iterator e = entries.iterator();
			while (e.hasNext()) {
				writeCEN((ZipEntry)e.next());
			}

			long cenSize = totalWritten - off;

			writeEND(offRelativeToCurrentDisk, cenSize);

			if ((!disableSizeCheck) && (!useZip64) && totalWritten > ZIP32_OVERALL_SIZE_LIMIT) {
				throw new IOException(ZIP32_OVERALL_SIZE_MESSAGE);
			}

			finished = true;
		} finally {
			try {
				// Clear the entry buffer
				this.entries.clear();
			} catch (Throwable e) {
				Logger.defaultLogger().error(e);
			}
		}
	}

	public void close() throws IOException {
		if (!closed) {
			try {
				super.close();
				closed = true;

				if (this.volumeStrategy != null) {
					this.volumeStrategy.close();
				}
			} catch (Throwable e) {
				// Force underlying stream closing.
				out.close();
				if (e instanceof IOException) {
					throw (IOException)e;
				} else if (e instanceof RuntimeException) {
					throw (RuntimeException)e;
				}
			}
		}
	}

	private long ensureCapacity(long size) throws IOException {
		if (this.volumeStrategy != null) {
			((VolumeOutputStream)this.out).ensureCapacity(size);
		}
		return this.totalWritten;
	}

	private void checkWritten(long mark, int size) throws IOException {
		if (mark + size != totalWritten) {
			throw new IOException("Inconsistent reserved space : reserved " + size + " - used " + (totalWritten-mark));
		}
	}

	private void writeLOC(ZipEntry e) throws IOException {
		byte[] nameBytes = ZipStringEncoder.encode(e.getName(), charset);
		int size = SIZE_LOC + nameBytes.length;
		long mark = ensureCapacity(size);

		writeInt(LOCSIG);	    // LOC header signature
		writeShort(e.version);      // version needed to extract
		writeShort(e.flag);         // general purpose bit flag
		writeShort(e.getMethod());       // compression method
		writeInt(e.time);           // last modification time

		// store size, uncompressed size, and crc-32 in data descriptor
		// immediately following compressed entry data
		writeInt(0);
		if (useZip64) {
			writeInt(ZIP64SIZEFLAG);
			writeInt(ZIP64SIZEFLAG);
		} else {
			writeInt(0);
			writeInt(0);
		}
		writeShort(nameBytes.length);
		writeShort(0);
		writeBytes(nameBytes, 0, nameBytes.length);

		checkWritten(mark, size);
	}

	private void writeEXT(ZipEntry e) throws IOException {
		int size = useZip64 ? SIZE_EXT_64 :SIZE_EXT_32 ;
		long mark = ensureCapacity(size);

		writeInt(EXTSIG);	    // EXT header signature
		writeInt(e.crc);	    	// crc-32
		if (useZip64) {
			writeLong(e.csize);	    // compressed size
			writeLong(e.getSize());	    // uncompressed size
		} else {
			writeInt(e.csize);      // compressed size
			writeInt(e.getSize());       // uncompressed size
		}

		checkWritten(mark, size);
	}

	private void writeCEN(ZipEntry e) throws IOException {
		byte[] nameBytes = ZipStringEncoder.encode(e.getName(), charset);
		byte[] commentBytes;
		if (e.getComment() != null) {
			commentBytes = ZipStringEncoder.encode(e.getComment(), charset);
		} else {
			commentBytes = null;
		}

		int size = SIZE_CEN
		+ (nameBytes == null ? 0 : nameBytes.length)
		+ (useZip64 ? 2*SIZE_SHORT + 3*SIZE_LONG + SIZE_INT : 0)
		+ (commentBytes == null ? 0 : commentBytes.length);
		long mark = ensureCapacity(size);

		writeInt(CENSIG);	    // CEN header signature
		writeShort(e.version);	    // version made by
		writeShort(e.version);	    // version needed to extract
		writeShort(e.flag);	    // general purpose bit flag
		writeShort(e.getMethod());	    // compression method
		writeInt(e.time);	    // last modification time
		writeInt(e.getCrc());	    // crc-32
		if (useZip64) {
			writeInt(ZIP64SIZEFLAG);	    // compressed size
			writeInt(ZIP64SIZEFLAG);	    // uncompressed size
		} else {
			writeInt(e.csize);      // compressed size
			writeInt(e.getSize());       // uncompressed size
		}

		writeShort(nameBytes.length);
		if (useZip64) {
			writeShort(ZIP64XTRALENGTH);
		} else {
			writeShort(0);
		}
		if (commentBytes != null) {
			writeShort(commentBytes.length);
		} else {
			writeShort(0);
		}
		if (useZip64) {
			writeShort(-1);         // starting disk number
		} else {
			writeShort(e.volumeNumber);         // starting disk number   
		}
		writeShort(0);		    // internal file attributes (unused)
		writeInt(0);		    // external file attributes (unused)
		if (useZip64) {
			writeInt(-1);	    // relative offset of local header
		} else {
			writeInt(e.offset);     // relative offset of local header
		}
		writeBytes(nameBytes, 0, nameBytes.length);
		if (useZip64) {
			writeZip64ExtraField(e);
		}
		if (commentBytes != null) {
			writeBytes(commentBytes, 0, commentBytes.length);
		}

		this.addEntryCount();
		checkWritten(mark, size);
	}

	private void writeZip64ExtraField(ZipEntry e) throws IOException {
		writeShort(ZIP64XTRAFIELD);
		writeShort(ZIP64XTRALENGTH - 4); // 8+8+8+4
		writeLong(e.getSize());
		writeLong(e.csize);
		writeLong(e.offset);
		writeInt(e.volumeNumber);
	}

	private void writeZip64END(long off, long len) throws IOException {       
		long cenOffset = volumeStrategy == null ? totalWritten : ((VolumeOutputStream)out).getWrittenInCurrentVolume();

		writeInt(ZIP64ENDSIG);	    		                   // zip64 end of central dir signature
		writeLong(ZIP64ENDLENGTH); 	                   // size of zip64 end of central directory record
		writeShort(getVersion()); 			                   // version made by
		writeShort(getVersion()); 			                   // version needed to extract 
		writeInt(volumeStrategy == null ? 0 : volumeStrategy.getCurrentVolumeNumber()); 	   // number of this disk
		writeInt(this.CENStart); 								   // number of the disk with the start of the central directory
		writeLong(this.getEntryCount()); 			                   // total number of entries in the central directory on this disk
		writeLong(entries.size()); 			                   // total number of entries in the central directory
		writeLong(len); 							                   // size of the central directory
		writeLong(off);  							                   // offset of start of central directory with respect to the starting disk number

		// LOCATOR
		writeInt(ZIP64ENDLOCSIG);	                                          // signature
		writeInt(this.Z64EODRStart);						                      // number of the disk with the start of the zip64 end of central directory
		writeLong(cenOffset);		                                                  // relative offset of the zip64 end of central directory record
		writeInt(volumeStrategy == null ? 1 : volumeStrategy.getVolumesCount());			      // total number of disks
	}

	private void writeEND(long off, long cenSize) throws IOException {
		byte[] commentBytes = null;
		if (comment != null) {      // zip file comment
			commentBytes = ZipStringEncoder.encode(comment, charset);
		}

		int size = SIZE_END + (commentBytes == null ? 0 : commentBytes.length) + (useZip64 ? SIZE_Z64_END : 0);
		long mark = ensureCapacity(size);

		if (useZip64) {
			this.Z64EODRStart = volumeStrategy == null ? 0 : volumeStrategy.getCurrentVolumeNumber();
			writeZip64END(off, cenSize);            
			cenSize += SIZE_Z64_END;
		}

		writeInt(ENDSIG);	    // END record signature
		writeShort(volumeStrategy == null ? 0 : this.volumeStrategy.getCurrentVolumeNumber());		    // number of this disk
		writeShort(this.CENStart);		    // central directory start disk
		if (useZip64) {
			writeShort(-1); // number of directory entries on disk
			writeShort(-1); // total number of directory entries
			writeInt(-1);		    // length of central directory
			writeInt(-1);		    // offset of central directory
		} else {
			writeShort((short)this.getEntryCount()); // number of directory entries on disk
			writeShort(entries.size()); // total number of directory entries
			writeInt(cenSize);          // length of central directory
			writeInt(off);          // offset of central directory
		}
		if (commentBytes != null) {	    // zip file comment
			writeShort(commentBytes.length);
			writeBytes(commentBytes, 0, commentBytes.length);
		} else {
			writeShort(0);
		}

		checkWritten(mark, size);
	}

	private int getVersion() throws ZipException {
		return 20;
	}

	private void writeShort(int v) throws IOException {
		OutputStream out = this.out;
		out.write((v >>> 0) & 0xff);
		out.write((v >>> 8) & 0xff);
		totalWritten += 2;
	}

	private void writeInt(long v) throws IOException {
		OutputStream out = this.out;
		out.write((int)((v >>>  0) & 0xff));
		out.write((int)((v >>>  8) & 0xff));
		out.write((int)((v >>> 16) & 0xff));
		out.write((int)((v >>> 24) & 0xff));
		totalWritten += 4;
	}

	private void writeLong(long v) throws IOException {
		OutputStream out = this.out;
		out.write((int)((v >>>  0) & 0xff));
		out.write((int)((v >>>  8) & 0xff));
		out.write((int)((v >>> 16) & 0xff));
		out.write((int)((v >>> 24) & 0xff));
		out.write((int)((v >>> 32) & 0xff));
		out.write((int)((v >>> 40) & 0xff));
		out.write((int)((v >>> 48) & 0xff));
		out.write((int)((v >>> 56) & 0xff));        
		totalWritten += 8;
	}

	private void writeBytes(byte[] b, int off, int len) throws IOException {
		super.out.write(b, off, len);
		totalWritten += len;
	}
}
