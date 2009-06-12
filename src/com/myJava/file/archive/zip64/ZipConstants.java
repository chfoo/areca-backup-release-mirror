/*
 * @(#)ZipConstants.java	1.17 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.myJava.file.archive.zip64;

/*
 * This interface defines the constants that are used by the classes
 * which manipulate ZIP files.
 *
 * @version	1.17, 01/23/03
 * @author	David Connelly
 * 
 * <BR>This class was derived from the original java.util.zip.ZipConstants.
 * <BR>The following modifications were made :
 * <BR>- Zip64 constants were added
 * <BR>- Package change
 * <BR>
 * <BR>CAUTION :
 * <BR>This file has been integrated into Areca.
 * <BR>It is has also possibly been adapted to meet Areca's needs. If such modifications has been made, they are described above.
 * <BR>Thanks to the authors for their work.
 *
 */
public interface ZipConstants {
    /*
     * Header signatures
     */
    static long LOCSIG = 0x04034b50L;	// "PK\003\004"
    static long EXTSIG = 0x08074b50L;	// "PK\007\008"
    static long CENSIG = 0x02014b50L;	// "PK\001\002"
    static long ENDSIG = 0x06054b50L;	// "PK\005\006"
    static long ZIP64ENDSIG = 0x06064b50L;
    static long ZIP64ENDLOCSIG = 0x07064b50L;

    /*
     * Header sizes in bytes (including signatures)
     */
    static final int LOCHDR = 30;	// LOC header size
    static final int EXTHDR = 16;	// EXT header size
    static final int EXTHDR64 = 24;	// EXT header size
    static final int CENHDR = 46;	// CEN header size
    static final int ENDHDR = 22;	// END header size

    /*
     * Local file (LOC) header field offsets
     */
    static final int LOCVER = 4;	// version needed to extract
    static final int LOCFLG = 6;	// general purpose bit flag
    static final int LOCHOW = 8;	// compression method
    static final int LOCTIM = 10;	// modification time
    static final int LOCCRC = 14;	// uncompressed file crc-32 value
    static final int LOCSIZ = 18;	// compressed size
    static final int LOCLEN = 22;	// uncompressed size
    static final int LOCNAM = 26;	// filename length
    static final int LOCEXT = 28;	// extra field length

    /*
     * Extra local (EXT) header field offsets
     */
    static final int EXTCRC = 4;	// uncompressed file crc-32 value
    static final int EXTSIZ = 8;	// compressed size
    static final int EXTLEN = 12;	// uncompressed size
    static final int EXTLEN64 = 16;	// uncompressed size

    /*
     * Central directory (CEN) header field offsets
     */
    static final int CENVEM = 4;	// version made by
    static final int CENVER = 6;	// version needed to extract
    static final int CENFLG = 8;	// encrypt, decrypt flags
    static final int CENHOW = 10;	// compression method
    static final int CENTIM = 12;	// modification time
    static final int CENCRC = 16;	// uncompressed file crc-32 value
    static final int CENSIZ = 20;	// compressed size
    static final int CENLEN = 24;	// uncompressed size
    static final int CENNAM = 28;	// filename length
    static final int CENEXT = 30;	// extra field length
    static final int CENCOM = 32;	// comment length
    static final int CENDSK = 34;	// disk number start
    static final int CENATT = 36;	// internal file attributes
    static final int CENATX = 38;	// external file attributes
    static final int CENOFF = 42;	// LOC header offset

    /*
     * End of central directory (END) header field offsets
     */
    static final int ENDSUB = 8;	// number of entries on this disk
    static final int ENDTOT = 10;	// total number of entries
    static final int ENDSIZ = 12;	// central directory size in bytes
    static final int ENDOFF = 16;	// offset of first CEN header
    static final int ENDCOM = 20;	// zip file comment length
    
    /*
     * Zip 64 Extra Field
     */
    static final int ZIP64XTRAFIELD = 1;
    static final long ZIP64SIZEFLAG = -1;  // 0xFFFFFFFF
    static final int ZIP64XTRALENGTH = 2 + 2 + 8 + 8 + 8 + 4;
    static final int ZIP64ENDLENGTH = 4 + 8 + 2 + 2 + 4 + 4 + 8 + 8 + 8 + 8 + 0 - 12;    
    static final int ZIP64VERSION = 45;
    
    /*
     * Others
     */
    static long MVSIG = 0x08074b50L;   // Multi-volumes archive signature
    static final int ZIPVERSION = 20;
    static final short SIZE_SHORT = 2;
    static final short SIZE_INT = 4;
    static final short SIZE_LONG = 8;
    
    static final long MAX_INT = 0xFFFFFFFF;
    
    public static final String CHARSET_CP437 = "Cp437";
    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String DEFAULT_CHARSET = CHARSET_UTF8;
}
