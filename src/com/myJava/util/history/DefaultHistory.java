package com.myJava.util.history;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.util.CalendarUtils;
import com.myJava.util.Util;
import com.myJava.util.log.Logger;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * TODO : refactor : implement XML serializers / deserializers
 * <BR>
 *
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

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
public class DefaultHistory implements History {
    
    private static final int VERSION = 2;
    private static final String HDR = "[HISTORY_HDR]";
    
    protected File file;
    protected HashMap content;
    protected FileTool tool = FileTool.getInstance();
    
    public DefaultHistory(File file) {
        this.file = file;
        try {
			this.load();
		} catch (Throwable e) {
			Logger.defaultLogger().error("Error during load of target history.", e);
		}
    }
    
    public boolean isEmpty() {
        return content.isEmpty();
    }

    public synchronized void importHistory(History h) {
        try {
			Map map = h.getContent();
			Iterator iter = map.entrySet().iterator();
			while (iter.hasNext()) {
			    Map.Entry entry = (Map.Entry)iter.next();
			    HistoryEntry value = (HistoryEntry)entry.getValue();
			    
			    addEntryWithoutFlush(value);
			}

			this.flush();
		} catch (Throwable e) {
			Logger.defaultLogger().error("Error during import of target history.", e);
		}
    }
    
    private void addEntryWithoutFlush(HistoryEntry entry) {        
        addEntryToContent(entry);
    }
    
    public synchronized void addEntry(HistoryEntry entry) {      
        addEntryWithoutFlush(entry);
        this.flush();
    }
    
    /**
     * Close the history and writes its content.
     * <BR>This mode may seem unefficient because it writes the whole history data instead of appending the new data.
     * <BR>The advantage of this approach is that it is compatible with FileSystemDrivers that do not support writing
     * "append" mode. 
     */
    public synchronized void flush() {       
        try {
			if (file != null) {
			    FileTool tool = FileTool.getInstance();
			    if (! FileSystemManager.exists(FileSystemManager.getParentFile(file))) {
			        tool.createDir(FileSystemManager.getParentFile(file));
			    }
			    
			    Writer fw = new OutputStreamWriter(new GZIPOutputStream(FileSystemManager.getFileOutputStream(file)));
			    fw.write(HDR + VERSION);
			    
			    Iterator iter = this.content.keySet().iterator();
			    while (iter.hasNext()) {
			        GregorianCalendar date = (GregorianCalendar)iter.next();
			        HistoryEntry entry = this.getEntry(date);
			        
			        fw.write("\n" + encode(entry));
			    }
			    fw.flush();
			    fw.close();
			}
		} catch (Throwable e) {
			Logger.defaultLogger().error("Error during flush of target history.", e);
		}
    }
    
    public void clearData() {
        if (file != null && FileSystemManager.exists(file)) {
            FileSystemManager.delete(file);
        }
    }
    
    /**
     * @see History#getContent()
     * TODO : not thread safe ... use synchronized maps
     */
    public synchronized HashMap getContent() {
        return content;
    }
    
    public synchronized GregorianCalendar[] getOrderedKeys() {
        GregorianCalendar[] keys = (GregorianCalendar[])this.content.keySet().toArray(new GregorianCalendar[0]);
        Arrays.sort(keys, new GregorianCalendarComparator());
        
        return keys;
    }
    
    private synchronized HistoryEntry getEntry(GregorianCalendar date) {
        return (HistoryEntry)(content.get(date));
    }
    
    public void clear() {
        this.content.clear();
    }
    
    private void load() throws IOException {
        this.content = new HashMap();
        if (this.file != null && FileSystemManager.exists(this.file)) {
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
	                        addEntryToContent(entry);
	                    } else {
	                        break;
	                    }
	                    
	                    strDate = readNextSignificantLine(reader);
	                }
                }
            } finally {
                if (reader != null) {
                    reader.close();
                } else if (in != null) {
                    in.close();
                }
            }
        }
    }
    
    private String readNextSignificantLine(BufferedReader reader) throws IOException {
        String str = reader.readLine();
        if (str == null || str.length() != 0) {
            return str;
        } else {
            return readNextSignificantLine(reader);
        }
    }
    
    private void addEntryToContent(HistoryEntry entry) {
        GregorianCalendar date = entry.getDate();       
        content.put(date, entry); 
    }
    
    private String encode(HistoryEntry source) {
        GregorianCalendar date = source.getDate();
        
        String strCal = CalendarUtils.getDateToString(date) + "-" + CalendarUtils.getFullTimeToString(date);
        return strCal + "\n" + Util.replace(Util.replace(source.getDescription(), "\n", "<<N>>"), "\r", "<<R>>") + "\n" + source.type;
    }
    
    private String decodeString(String source) {
        return Util.replace(Util.replace(source, "<<N>>", "\n"), "<<R>>", "\r");
    }  
    
    protected static class GregorianCalendarComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            GregorianCalendar c1 = (GregorianCalendar)o1;
            GregorianCalendar c2 = (GregorianCalendar)o2;            
            
            if (o1 == null && o2 != null) {
                return -1;
            } else if (o1 == null) {
                return 0;
            } else if (o2 == null && o1 != null) {
                return 1;
            } else {
                if (c1.before(c2)) {
                    return -1;
                } else if (c2.before(c1)) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }
}
