/*
 * @(#)ZipInputStream.java	1.33 03/02/07
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.myJava.file.archive.zip64;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;
import java.util.zip.CRC32;
import java.util.zip.Inflater;
import java.util.zip.ZipException;

import com.myJava.configuration.FrameworkConfiguration;

/**
 * This class implements an input stream filter for reading files in the
 * ZIP file format. Includes support for both compressed and uncompressed
 * entries.
 * 
 * @author	David Connelly
 * @version	1.33, 02/07/03
 * 
 * <BR>This class was derived from the original java.util.zip.ZipInputStream.
 * <BR>The following modifications were made :
 * <BR>- This new class reads zip32 and zip64 archives but IGNORES file size written in local entry headers. (these data are read from the EXT area)
 * <BR>- Package change
 * <BR>- Only DEFLATED files are handled
 * <BR>- Multivolumes support
 * <BR>- Buffer size parameterization
 * @author Olivier Petrucci 
 * <BR>
 * <BR>CAUTION :
 * <BR>This file has been integrated into Areca.
 * <BR>It is has also possibly been adapted to meet Areca's needs. If such modifications has been made, they are described above.
 * <BR>Thanks to the authors for their work.
 *
 */
public class ZipInputStream 
extends InflaterInputStream 
implements ZipConstants {

    private static final int ZIP_BUFFER_SIZE = FrameworkConfiguration.getInstance().getZipBufferSize();
    private static final boolean ZIP_ENABLE_ENTRY_CHECK = FrameworkConfiguration.getInstance().isZipEntryCheckEnabled();
    
    private ZipEntry entry;
    private CRC32 crc = new CRC32();
    private byte[] tmpbuf = new byte[ZIP_BUFFER_SIZE];
    private byte[] b = new byte[512];
    private Charset charset = Charset.forName(DEFAULT_CHARSET);
    
    private static final int DEFLATED = ZipEntry.DEFLATED;

    private boolean closed = false;
    // this flag is set to true after EOF has reached for
    // one entry
    private boolean entryEOF = false;

    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
    }

    public ZipInputStream(InputStream in) {
        super(new PushbackInputStream(in, ZIP_BUFFER_SIZE), new Inflater(true), ZIP_BUFFER_SIZE);
        usesDefaultInflater = true;
    }

    public ZipEntry getNextEntry() throws IOException {
        ensureOpen();
        if (entry != null) {
            closeEntry();
        }
        if (ZIP_ENABLE_ENTRY_CHECK) {
            crc.reset();
        }
        resetInflater();
        if ((entry = readLOC()) == null) {
            return null;
        }
        entryEOF = false;
        return entry;
    }

    public void closeEntry() throws IOException {
        ensureOpen();
        while (read(tmpbuf, 0, tmpbuf.length) != -1) ;    // -> To be enhanced : we should just skip the remaining bytes without decompressing them
        entryEOF = true;
    }

    public int available() throws IOException {
        ensureOpen();
        if (entryEOF) {
            return 0;
        } else {
            return 1;
        }
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        if (charset != null) {
            this.charset = charset;
        }
    }

    public int read(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        if (entry == null) {
            return -1;
        }
        switch (entry.method) {
        case DEFLATED:
            len = super.read(b, off, len);
            if (len == -1) {
                readEnd(entry);
                entryEOF = true;
                entry = null;
            } else if (ZIP_ENABLE_ENTRY_CHECK) {
                crc.update(b, off, len);
            }
            return len;
        default:
            throw new InternalError("invalid compression method");
        }
    }

    public long skip(long n) throws IOException {
        if (n < 0) {
            throw new IllegalArgumentException("negative skip length");
        }
        ensureOpen();
        int max = (int)Math.min(n, Integer.MAX_VALUE);
        int total = 0;
        while (total < max) {
            int len = max - total;
            if (len > tmpbuf.length) {
                len = tmpbuf.length;
            }
            len = read(tmpbuf, 0, len);
            if (len == -1) {
                entryEOF = true;
                break;
            }
            total += len;
        }
        return total;
    }

    public void close() throws IOException {
        if (!closed) {
            super.close();
            closed = true;
        }
    }

    private ZipEntry readLOC() throws IOException {
        try {
            readFully(tmpbuf, 0, LOCHDR);
        } catch (EOFException e) {
            return null;
        }
        long lc = get32(tmpbuf, 0); 
        if (lc != LOCSIG) {
            return null;
        }
        // get the entry name and create the ZipEntry first
        int len = get16(tmpbuf, LOCNAM);
        if (len == 0) {
            throw new ZipException("missing entry name");
        }
        int blen = b.length;
        if (len > blen) {
            do 
                blen = blen * 2;
            while (len > blen); 
            b = new byte[blen];
        } 
        readFully(b, 0, len);
        ZipEntry e = createZipEntry(ZipStringEncoder.decode(b, 0, len, this.charset));
        // now get the remaining fields for the entry
        e.version = get16(tmpbuf, LOCVER);
        e.flag = get16(tmpbuf, LOCFLG);
        if ((e.flag & 1) == 1) {
            throw new ZipException("encrypted ZIP entry not supported");
        }
        e.method = get16(tmpbuf, LOCHOW);
        e.time = get32(tmpbuf, LOCTIM);
        if ((e.flag & 8) == 8 && e.method != DEFLATED) {
            throw new ZipException("only DEFLATED entries can have EXT descriptor");
        }
        e.crc = get32(tmpbuf, LOCCRC);
        e.csize = get32(tmpbuf, LOCSIZ);
        e.setSize(get32(tmpbuf, LOCLEN));	 //xxxx
        e.isZip64 = requiresZip64(e);	
        len = get16(tmpbuf, LOCEXT);
        if (len > 0) {
            byte[] bb = new byte[len];
            readFully(bb, 0, len);
            e.extra = bb;
        }
        return e;
    }

    protected ZipEntry createZipEntry(String name) {
        return new ZipEntry(name);
    }

    private void readEnd(ZipEntry e) throws IOException {
        int n = inf.getRemaining();
        if (n > 0) {
            ((PushbackInputStream)in).unread(buf, len - n, n);
        }
        if ((e.flag & 8) == 8) {
            /* EXT descriptor present */
            if (e.isZip64) {
                readFully(tmpbuf, 0, EXTHDR64);
            } else {
                readFully(tmpbuf, 0, EXTHDR);
            }
            long sig = get32(tmpbuf, 0);
            if (sig != EXTSIG) { // no EXTSIG present
                throw new ZipException("Descriptor signature not found.");
            } else if (! e.isZip64) {
                e.crc = get32(tmpbuf, EXTCRC);
                e.csize = get32(tmpbuf, EXTSIZ);
                e.setSize(get32(tmpbuf, EXTLEN));
            } else {
                e.crc = get32(tmpbuf, EXTCRC);
                e.csize = get64(tmpbuf, EXTSIZ);
                e.setSize(get64(tmpbuf, EXTLEN64));
            }
        }
        if ((ZIP_ENABLE_ENTRY_CHECK) && (e.crc != crc.getValue())) {
            throw new ZipException(
                    "invalid entry CRC (expected 0x" + Long.toHexString(e.crc) +
                    " but got 0x" + Long.toHexString(crc.getValue()) + ")");
        }
        if (ZIP_ENABLE_ENTRY_CHECK && (e.getSize() != getTotalOut())) {
    		long mod = ((long)Integer.MAX_VALUE) - ((long)Integer.MIN_VALUE) + 1;
    		long mv = (getTotalOut() - e.getSize())%mod;
    		if (mv != 0) {  // Backward compatibility
                throw new ZipException(
                        "invalid entry size (expected " + e.getSize() + " but got " +
                        getTotalOut() + " bytes)");
    		}
        }
    }

    private void readFully(byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            int n = in.read(b, off, len);
            if (n == -1) {
                throw new EOFException();
            }
            off += n;
            len -= n;
        }
    }

    private static final int get16(byte b[], int off) {
        return (b[off] & 0xff) | ((b[off+1] & 0xff) << 8);
    }

    private static final long get32(byte b[], int off) {
        return get16(b, off) | ((long)get16(b, off+2) << 16);
    }

    private static final long get64(byte b[], int off) {
        return get32(b, off) | ((long)get32(b, off+4) << 32);
    }

    private static boolean requiresZip64(ZipEntry entry) {
        return (entry.getSize() == -1 || entry.csize == -1 || entry.version >= ZIP64VERSION); 
    }
    
    private static void checkSizes(long eSize, long totalOut) {

    }
}
