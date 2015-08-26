package com.myJava.file.archive.zip64;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.myJava.system.OSTool;
import com.myJava.util.Util;
import com.myJava.util.collections.SerializedCollection;

/**
 * SerializedCollection implementation dedicated to ZipEntries
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
public class ZipEntrySerializedCollection extends SerializedCollection {

    /**
     * @param bufferFile
     */
    public ZipEntrySerializedCollection() throws IOException {
        super();
        
        // Override the buffer file
        long rnd = Util.getRndLong();
        this.bufferFile = new File(OSTool.getTempDirectory(), "java_zip64_buffer_" + rnd + ".tmp");
    }

    protected void writeObject(ObjectOutputStream out, Object o) throws IOException {
        ZipEntry e = (ZipEntry)o;
        out.writeUTF(e.name == null ? "" : e.name);
        out.writeBoolean(e.comment == null);
        if (e.comment != null) {
            out.writeUTF(e.comment);
        }
        out.writeInt(e.flag);
        out.writeInt(e.method);
        out.writeInt(e.version);
        out.writeLong(e.crc);
        out.writeLong(e.getSize());
        out.writeLong(e.csize);
        out.writeLong(e.time);
        out.writeInt(e.volumeNumber);
        out.writeLong(e.offset);
        out.writeBoolean(e.extra == null);
        if (e.extra != null) {
            out.writeInt(e.extra.length);
            out.write(e.extra);
        }
    }

    protected Object readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ZipEntry e = new ZipEntry(in.readUTF());
        boolean nullComment = in.readBoolean();
        if (! nullComment) {
            e.comment = in.readUTF();            
        }
        e.flag = in.readInt();
        e.method = in.readInt();
        e.version = in.readInt();
        e.crc = in.readLong();
        e.setSize(in.readLong());
        e.csize = in.readLong();
        e.time = in.readLong();
        e.volumeNumber = in.readInt();
        e.offset = in.readLong();
        boolean extraNull = in.readBoolean();
        if (! extraNull) {
            e.extra = new byte[in.readInt()];
            in.readFully(e.extra);
        }
        return e;
    }
}
