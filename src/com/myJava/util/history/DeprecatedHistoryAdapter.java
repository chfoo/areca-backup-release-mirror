package com.myJava.util.history;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.GregorianCalendar;
import java.util.zip.GZIPInputStream;

import com.myJava.file.FileSystemManager;
import com.myJava.util.CalendarUtils;
import com.myJava.util.Util;
import com.myJava.util.log.Logger;
import com.myJava.util.xml.AdapterException;

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
public class DeprecatedHistoryAdapter implements HistoryReader {
    public static final String HDR = "[HISTORY_HDR]";
    
	public History read(File file) throws AdapterException {
		History history = new History();
        if (file != null && FileSystemManager.exists(file)) {
            BufferedReader reader = null;
            InputStream in = null;
            try {
                in = FileSystemManager.getFileInputStream(file);
                reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(in)));
                String strDate;
                String strContent;
                int type;
                String header = readNextSignificantLine(reader);
                int version;
                
                if (header != null) {
	                // Backward compatibility
	                if (header.indexOf(HDR) != -1) {
	                    version = 2;
	                    strDate = readNextSignificantLine(reader);
	                } else {
	                    version = 1;
	                    strDate = header;
	                }
	                
	                while (true) {
	                    strContent = readNextSignificantLine(reader);
	
	                    if (strDate != null && strContent != null) {
	                        if (version == 2) {
	                            type = Integer.parseInt(readNextSignificantLine(reader));
	                        } else {
	                            type = HistoryEntry.TYPE_UNKNOWN;
	                        }
	                        
	                        HistoryEntry entry = new HistoryEntry();
	                        entry.setDate(CalendarUtils.resolveDate(strDate, new GregorianCalendar()));
	                        entry.setDescription(decodeString(strContent));
	                        entry.setType(type);
	                        history.addEntry(entry);
	                    } else {
	                        break;
	                    }
	                    
	                    strDate = readNextSignificantLine(reader);
	                }
                }
            } catch (IOException e) {
            	Logger.defaultLogger().error(e);
            	throw new AdapterException(e);
        	} finally {
        		try {
	                if (reader != null) {
	                    reader.close();
	                } else if (in != null) {
	                    in.close();
	                }
                } catch (IOException e) {
                	throw new AdapterException(e);
                }
            }
        }
        
        return history;
	}
	
    private String readNextSignificantLine(BufferedReader reader) throws IOException {
        String str = reader.readLine();
        if (str == null || str.length() != 0) {
            return str;
        } else {
            return readNextSignificantLine(reader);
        }
    }
    
    private String decodeString(String source) {
        return Util.replace(Util.replace(source, "<<N>>", "\n"), "<<R>>", "\r");
    } 
}
