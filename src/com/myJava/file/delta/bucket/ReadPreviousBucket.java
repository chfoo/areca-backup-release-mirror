package com.myJava.file.delta.bucket;

import java.io.IOException;
import java.io.InputStream;

import com.myJava.file.delta.Constants;
import com.myJava.file.delta.tools.IOHelper;
import com.myJava.object.ToStringHelper;

/**
 * [FOUND_BLOCK_SIGNATURE : 8 bytes][FROM_POSITION : 8 bytes][TO_POSITION : 8 bytes]
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
public class ReadPreviousBucket
extends AbstractBucket
implements Bucket, Constants {
    private byte[] tmp = new byte[16];
    private long readFrom;
    private long readTo;

    public long getLength() {
        return readTo - readFrom + 1;
    }

    public long getReadFrom() {
        return readFrom;
    }

    public void init(InputStream in) throws IOException {
        IOHelper.readFully(in, tmp);
        this.readFrom = IOHelper.get64(tmp, 0);
        this.readTo = IOHelper.get64(tmp, 8);
    }
    
    public long getSignature() {
        return SIG_READ;
    }
    
    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("From", from, sb);
        ToStringHelper.append("To", from + getLength() - 1, sb);
        ToStringHelper.append("Length", getLength(), sb);
        ToStringHelper.append("ReadFrom", readFrom, sb);
        ToStringHelper.append("ReadTo", readTo, sb);
        return ToStringHelper.close(sb);
    }
}
