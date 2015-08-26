package com.myJava.file.delta;

import com.myJava.file.delta.sequence.HashSequenceEntry;
import com.myJava.file.delta.tools.CircularList;

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
public interface DeltaProcessor {
    public void begin() throws DeltaProcessorException;
    public void newBytes(byte[] data, int offset, int len) throws DeltaProcessorException;
    public void newByte(byte data) throws DeltaProcessorException;
    public void blockFound(HashSequenceEntry entry, CircularList block) throws DeltaProcessorException;
    public void blockFound(long readFrom, long readTo) throws DeltaProcessorException;
    public void bytesLost(long from, long to) throws DeltaProcessorException;
    public void end() throws DeltaProcessorException;
}
