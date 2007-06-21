/*
 * @(#)ZipOutputStream.java	1.27 03/02/07
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.myJava.file.archive.zip64;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipException;

import com.myJava.util.SerializedCollection;
import com.myJava.util.log.Logger;

/**
 * This class implements an output stream filter for writing files in the
 * ZIP file format. Includes support for both compressed and uncompressed
 * entries. 
 * @author	David Connelly
 *
 *
 * <BR>This class was derived from the original java.util.zip.ZipOutputStream.
 * <BR>The following modifications were made :
 * <BR>- No more control over duplicates entries
 * <BR>- Zip64 Specific
 * <BR>- Uses SerializedCollections to ensure memory capacity
 * <BR>- Package and name change
 * @author Olivier Petrucci 
 * <BR>
 * <BR>CAUTION :
 * <BR>This file has been integrated into Areca.
 * <BR>It is has also possibly been adapted to meet Areca's needs. If such modifications has been made, they are described above.
 * <BR>Thanks to the authors for their work.
 * <BR>Areca Build ID : 3274863990151426915
 */
public class Zip64OutputStream extends DeflaterOutputStream implements ZipConstants {
    private ZipEntry entry;
    private SerializedCollection entries;
    private CRC32 crc = new CRC32();
    private long written;
    private long locoff = 0;
    private String comment;
    private int method = DEFLATED;
    private boolean finished;
    
    private boolean closed = false;
    
    /**
     * Check to make sure that this stream has not been closed
     */
    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
    }
    /**
     * Compression method for uncompressed (STORED) entries.
     */
    public static final int STORED = ZipEntry.STORED;
    
    /**
     * Compression method for compressed (DEFLATED) entries.
     */
    public static final int DEFLATED = ZipEntry.DEFLATED;
    
    /**
     * Creates a new ZIP output stream.
     * @param out the actual output stream
     */
    public Zip64OutputStream(OutputStream out) {
        super(out, new Deflater(Deflater.DEFAULT_COMPRESSION, true));
        usesDefaultDeflater = true;
        
        this.entries = new ZipEntrySerializedCollection();
    }
    
    /**
     * Sets the ZIP file comment.
     * @param comment the comment string
     * @exception IllegalArgumentException if the length of the specified
     *		  ZIP file comment is greater than 0xFFFF bytes
     */
    public void setComment(String comment) {
        if (comment != null && comment.length() > 0xffff/3 
                && getUTF8Length(comment) > 0xffff) {
            throw new IllegalArgumentException("ZIP file comment too long.");
        }
        this.comment = comment;
    }
    
    /**
     * Sets the default compression method for subsequent entries. This
     * default will be used whenever the compression method is not specified
     * for an individual ZIP file entry, and is initially set to DEFLATED.
     * @param method the default compression method
     * @exception IllegalArgumentException if the specified compression method
     *		  is invalid
     */
    public void setMethod(int method) {
        if (method != DEFLATED && method != STORED) {
            throw new IllegalArgumentException("invalid compression method");
        }
        this.method = method;
    }
    
    /**
     * Sets the compression level for subsequent entries which are DEFLATED.
     * The default setting is DEFAULT_COMPRESSION.
     * @param level the compression level (0-9)
     * @exception IllegalArgumentException if the compression level is invalid
     */
    public void setLevel(int level) {
        def.setLevel(level);
    }
    
    /**
     * Begins writing a new ZIP file entry and positions the stream to the
     * start of the entry data. Closes the current entry if still active.
     * The default compression method will be used if no compression method
     * was specified for the entry, and the current time will be used if
     * the entry has no set modification time.
     * @param e the ZIP entry to be written
     * @exception ZipException if a ZIP format error has occurred
     * @exception IOException if an I/O error has occurred
     */
    public void putNextEntry(ZipEntry e) throws IOException {
        ensureOpen();
        if (entry != null) {
            closeEntry();	// close previous entry
        }
        if (e.time == -1) {
            e.setTime(System.currentTimeMillis());
        }
        if (e.getMethod() == -1) {
            e.setMethod(method);	// use default method
        }
        switch (e.getMethod()) {
        case DEFLATED:
            if (e.size == -1 || e.csize == -1 || e.getCrc() == -1) {
                // store size, compressed size, and crc-32 in data descriptor
                // immediately following the compressed entry data
                e.flag = 8;
            } else if (e.size != -1 && e.csize != -1 && e.getCrc() != -1) {
                // store size, compressed size, and crc-32 in LOC header
                throw new IllegalStateException("EXT mode mandatory");
            } else {
                throw new ZipException(
                "DEFLATED entry missing size, compressed size, or crc-32");
            }
            e.version = ZIP64VERSION; // 20
            break;
        case STORED:
            // compressed size, uncompressed size, and crc-32 must all be
            // set for entries using STORED compression method
            if (e.size == -1) {
                e.size = e.csize;
            } else if (e.csize == -1) {
                e.csize = e.size;
            } else if (e.size != e.csize) {
                throw new ZipException(
                "STORED entry where compressed != uncompressed size");
            }
            if (e.size == -1 || e.getCrc() == -1) {
                throw new ZipException(
                "STORED entry missing size, compressed size, or crc-32");
            }
            e.version = ZIP64VERSION; // 10
            e.flag = 0;
            break;
        default:
            throw new ZipException("unsupported compression method");
        }
        e.offset = written;
        writeLOC(e);
        entries.add(e);
        entry = e;
    }
    
    /**
     * Closes the current ZIP entry and positions the stream for writing
     * the next entry.
     * @exception ZipException if a ZIP format error has occurred
     * @exception IOException if an I/O error has occurred
     */
    public void closeEntry() throws IOException {
        ensureOpen();
        ZipEntry e = entry;
        if (e != null) {
            switch (e.getMethod()) {
            case DEFLATED:
                def.finish();
                while (!def.finished()) {
                    deflate();
                }
                if ((e.flag & 8) == 0) {  // -> CASE 1 : Data in LOC header
                    throw new IllegalStateException("EXT mode mandatory");
                } else { // -> CASE 2 : data in data descriptor
                    e.size = def.getTotalIn();
                    e.csize = def.getTotalOut();
                    e.crc = crc.getValue();
                    writeEXT(e);
                }
                def.reset();
                written += e.csize;
                break;
            case STORED:
                // we already know that both e.size and e.csize are the same
                if (e.size != written - locoff) {
                    throw new ZipException(
                            "invalid entry size (expected " + e.size +
                            " but got " + (written - locoff) + " bytes)");
                }
                if (e.getCrc() != crc.getValue()) {
                    throw new ZipException(
                            "invalid entry crc-32 (expected 0x" +
                            Long.toHexString(e.getCrc()) + " but got 0x" +
                            Long.toHexString(crc.getValue()) + ")");
                }
                break;
            default:
                throw new InternalError("invalid compression method");
            }
            crc.reset();
            entry = null;
        }
    }
    
    /**
     * Writes an array of bytes to the current ZIP entry data. This method
     * will block until all the bytes are written.
     * @param b the data to be written
     * @param off the start offset in the data
     * @param len the number of bytes that are written
     * @exception ZipException if a ZIP file error has occurred
     * @exception IOException if an I/O error has occurred
     */
    public synchronized void write(byte[] b, int off, int len)
    throws IOException
    {
        ensureOpen();
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        
        if (entry == null) {
            throw new ZipException("no current ZIP entry");
        }
        switch (entry.getMethod()) {
        case DEFLATED:
            super.write(b, off, len);
            break;
        case STORED:
            written += len;
            if (written - locoff > entry.size) {
                throw new ZipException(
                "attempt to write past end of STORED entry");
            }
            out.write(b, off, len);
            break;
        default:
            throw new InternalError("invalid compression method");
        }
        crc.update(b, off, len);
    }
    
    /**
     * Finishes writing the contents of the ZIP output stream without closing
     * the underlying stream. Use this method when applying multiple filters
     * in succession to the same output stream.
     * @exception ZipException if a ZIP file error has occurred
     * @exception IOException if an I/O exception has occurred
     */
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
            long off = written;
            Iterator e = entries.iterator();
            while (e.hasNext()) {
                writeCEN((ZipEntry)e.next());
            }
            writeZip64END(off, written - off);
            writeEND(off, written-off);
            
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
    
    /**
     * Closes the ZIP output stream as well as the stream being filtered.
     * @exception ZipException if a ZIP file error has occurred
     * @exception IOException if an I/O error has occurred
     */
    public void close() throws IOException {
        if (!closed) {
            super.close();
            closed = true;
        }
    }
    
    /*
     * Writes local file (LOC) header for specified entry.
     */
    private void writeLOC(ZipEntry e) throws IOException {
        writeInt(LOCSIG);	    // LOC header signature
        writeShort(e.version);      // version needed to extract
        writeShort(e.flag);         // general purpose bit flag
        writeShort(e.getMethod());       // compression method
        writeInt(e.time);           // last modification time
        if ((e.flag & 8) == 8) {
            // store size, uncompressed size, and crc-32 in data descriptor
            // immediately following compressed entry data
            writeInt(0);
            writeInt(ZIP64SIZEFLAG);
            writeInt(ZIP64SIZEFLAG);
        } else {
            throw new IllegalStateException("EXT mode mandatory");
        }
        byte[] nameBytes = getUTF8Bytes(e.getName());
        writeShort(nameBytes.length);
        //writeShort(ZIP64XTRALENGTH);
       writeShort(0); //
        writeBytes(nameBytes, 0, nameBytes.length);
        //writeZip64ExtraField(ZIP64SIZEFLAG, ZIP64SIZEFLAG, 0);
        locoff = written;
    }
    
    /*
     * Writes extra data descriptor (EXT) for specified entry.
     */
    private void writeEXT(ZipEntry e) throws IOException {
        writeInt(EXTSIG);	    // EXT header signature
        writeInt(e.crc);	    	// crc-32
        writeLong(e.csize);	    // compressed size
        writeLong(e.size);	    // uncompressed size
    }
    
    /*
     * Write central directory (CEN) header for specified entry.
     * REMIND: add support for file attributes
     */
    private void writeCEN(ZipEntry e) throws IOException {
        writeInt(CENSIG);	    // CEN header signature
        writeShort(e.version);	    // version made by
        writeShort(e.version);	    // version needed to extract
        writeShort(e.flag);	    // general purpose bit flag
        writeShort(e.getMethod());	    // compression method
        writeInt(e.time);	    // last modification time
        writeInt(e.getCrc());	    // crc-32
        writeInt(ZIP64SIZEFLAG);	    // compressed size
        writeInt(ZIP64SIZEFLAG);	    // uncompressed size
        byte[] nameBytes = getUTF8Bytes(e.getName());
        writeShort(nameBytes.length);
        writeShort(ZIP64XTRALENGTH);
        byte[] commentBytes;
        if (e.getComment() != null) {
            commentBytes = getUTF8Bytes(e.getComment());
            writeShort(commentBytes.length);
        } else {
            commentBytes = null;
            writeShort(0);
        }
        writeShort(0);		    // starting disk number
        writeShort(0);		    // internal file attributes (unused)
        writeInt(0);		    // external file attributes (unused)
        writeInt(-1);	    // relative offset of local header
        writeBytes(nameBytes, 0, nameBytes.length);
        writeZip64ExtraField(e);
        if (commentBytes != null) {
            writeBytes(commentBytes, 0, commentBytes.length);
        }
    }
    
    private void writeZip64ExtraField(ZipEntry e) throws IOException {
        writeZip64ExtraField(e.size, e.csize, e.offset);
    }
    
    /**
     *    0x0001						2 bytes    Tag for this "extra" block type
     *    Size       						2 bytes    Size of this "extra" block
     *    Original Size       			8 bytes    Original uncompressed file size
     *    Compressed Size    	8 bytes    Size of compressed data
     *    Relative Header Offset  8 bytes    Offset of local header record
     *    Disk Start Number       4 bytes    Number of the disk on which this file starts 
     */
    private void writeZip64ExtraField(long size, long csize, long offset) throws IOException {
        writeShort(ZIP64XTRAFIELD);
        writeShort(ZIP64XTRALENGTH - 4); // 8+8+8+4
        writeLong(size);
        writeLong(csize);
        writeLong(offset);
        writeInt(0);
    }
    
    /**
     *  zip64 end of central dir signature                       											4 bytes  (0x06064b50)
     *  size of zip64 end of central directory record                									8 bytes
     *  version made by                 																		2 bytes
     *  version needed to extract       																		2 bytes
     *  number of this disk             																		4 bytes
     *  number of the disk with the start of the central directory  								4 bytes
     *  total number of entries in the central directory on this disk  							8 bytes
     *  total number of entries in the central directory               								8 bytes
     *  size of the central directory   																		8 bytes
     *  offset of start of central directory with respect to the starting disk number       8 bytes
     *  zip64 extensible data sector    																	(variable size)
     * 
     * 
     *  ---------> LOCATOR
     *   signature                       																		4 bytes  (0x07064b50)
     *   number of the disk with the start of the zip64 end of central directory    		4 bytes
     *   relative offset of the zip64 end of central directory record 							8 bytes 
     *   total number of disks           																	4 bytes
     */
    private void writeZip64END(long off, long len) throws IOException {
        long cenOffset = written;
        
        writeInt(ZIP64ENDSIG);	    		// zip64 end of central dir signature
        writeLong(ZIP64ENDLENGTH); 	// size of zip64 end of central directory record
        writeShort(getVersion()); 			// version made by
        writeShort(getVersion()); 			// version needed to extract 
        writeInt(0); 								// number of this disk
        writeInt(0); 								// number of the disk with the start of the central directory
        writeLong(entries.size()); 			// total number of entries in the central directory on this disk
        writeLong(entries.size()); 			// total number of entries in the central directory
        writeLong(len); 							// size of the central directory
        writeLong(off);  							// offset of start of central directory with respect to the starting disk number
        
        // LOCATOR
        writeInt(ZIP64ENDLOCSIG);	// signature
        writeInt(0);						// number of the disk with the start of the zip64 end of central directory
        writeLong(cenOffset);		// relative offset of the zip64 end of central directory record
        writeInt(1);						// total number of disks
    }
    
    private void writeEND(long off, long len) throws IOException {
        writeInt(ENDSIG);	    // END record signature
        writeShort(0);		    // number of this disk
        writeShort(0);		    // central directory start disk
        writeShort(-1); // number of directory entries on disk
        writeShort(-1); // total number of directory entries
        writeInt(-1);		    // length of central directory
        writeInt(-1);		    // offset of central directory
        if (comment != null) {	    // zip file comment
            byte[] b = getUTF8Bytes(comment);
            writeShort(b.length);
            writeBytes(b, 0, b.length);
        } else {
            writeShort(0);
        }
    }
    
    private int getVersion() throws ZipException {
        switch (method) {
        case DEFLATED:
            return 20;
        case STORED:
            return 10;
        default:
            throw new ZipException("unsupported compression method");
        }
    }
    
    /*
     * Writes a 16-bit short to the output stream in little-endian byte order.
     */
    private void writeShort(int v) throws IOException {
        OutputStream out = this.out;
        out.write((v >>> 0) & 0xff);
        out.write((v >>> 8) & 0xff);
        written += 2;
    }
    
    /*
     * Writes a 32-bit int to the output stream in little-endian byte order.
     */
    private void writeInt(long v) throws IOException {
        OutputStream out = this.out;
        out.write((int)((v >>>  0) & 0xff));
        out.write((int)((v >>>  8) & 0xff));
        out.write((int)((v >>> 16) & 0xff));
        out.write((int)((v >>> 24) & 0xff));
        written += 4;
    }
    
    /*
     * Writes a 64-bit int to the output stream in little-endian byte order.
     */
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
        written += 8;
    }
    
    /*
     * Writes an array of bytes to the output stream.
     */
    private void writeBytes(byte[] b, int off, int len) throws IOException {
        super.out.write(b, off, len);
        written += len;
    }
    
    /*
     * Returns the length of String's UTF8 encoding.
     */
    static int getUTF8Length(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i); 
            if (ch <= 0x7f) {
                count++;
            } else if (ch <= 0x7ff) {
                count += 2;
            } else {
                count += 3;
            }
        }
        return count;
    }
    
    /*
     * Returns an array of bytes representing the UTF8 encoding
     * of the specified String.
     */
    private static byte[] getUTF8Bytes(String s) {
        char[] c = s.toCharArray();
        int len = c.length;
        // Count the number of encoded bytes...
        int count = 0;
        for (int i = 0; i < len; i++) {
            int ch = c[i];
            if (ch <= 0x7f) {
                count++;
            } else if (ch <= 0x7ff) {
                count += 2;
            } else {
                count += 3;
            }
        }
        // Now return the encoded bytes...
        byte[] b = new byte[count];
        int off = 0;
        for (int i = 0; i < len; i++) {
            int ch = c[i];
            if (ch <= 0x7f) {
                b[off++] = (byte)ch;
            } else if (ch <= 0x7ff) {
                b[off++] = (byte)((ch >> 6) | 0xc0);
                b[off++] = (byte)((ch & 0x3f) | 0x80);
            } else {
                b[off++] = (byte)((ch >> 12) | 0xe0);
                b[off++] = (byte)(((ch >> 6) & 0x3f) | 0x80);
                b[off++] = (byte)((ch & 0x3f) | 0x80);
            }
        }
        return b;
    }
}
