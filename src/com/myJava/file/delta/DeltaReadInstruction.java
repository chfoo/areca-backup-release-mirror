package com.myJava.file.delta;

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
public class DeltaReadInstruction {
    protected int writeOffset; // Temporary work data
    private long readFrom;
    private long readTo;

    public int getWriteOffset() {
        return writeOffset;
    }

    public void setWriteOffset(int writeOffset) {
        this.writeOffset = writeOffset;
    }
    
    public void setReadFrom(long readFrom) {
        this.readFrom = readFrom;
    }

    public long getReadTo() {
        return readTo;
    }
    
    public long getReadFrom() {
        return readFrom;
    }
    
    public void setReadTo(long readTo) {
        this.readTo = readTo;
    }

    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("ReadFrom", readFrom, sb);
        ToStringHelper.append("ReadTo", readTo, sb);
        ToStringHelper.append("Length", readTo - readFrom + 1, sb);
        ToStringHelper.append("WriteFrom", writeOffset, sb);
        ToStringHelper.append("WriteTo", writeOffset + readTo - readFrom, sb);
        return ToStringHelper.close(sb);
    }
}
