package com.application.areca.filter;

import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import com.application.areca.RecoveryEntry;
import com.application.areca.Utils;
import com.application.areca.impl.FileSystemRecoveryEntry;
import com.myJava.file.FileSystemManager;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.object.PublicClonable;
import com.myJava.util.CalendarUtils;

/**
 * Checks that the file's date is posterior to the minDate
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4765044255727194190
 */
 
 /*
 Copyright 2005-2007, Olivier PETRUCCI.
 
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
public class FileDateArchiveFilter extends AbstractArchiveFilter {
    
    private long minDate;
    private boolean greaterThan;
    
    public FileDateArchiveFilter() {
    }

    public void acceptParameters(String parameters) {     
        if (
                Utils.isEmpty(parameters) 
                || (
                        (! parameters.trim().startsWith(">"))
                        && (! parameters.trim().startsWith("<")) 
                )
        ) {
            throw new IllegalArgumentException("Invalid parameters : " + parameters);
        }
        
        this.greaterThan = (parameters.indexOf('>') != -1);
        
        StringTokenizer stt = new StringTokenizer(parameters.trim().substring(1).trim(), "_-/.;, ");
        int year = Integer.parseInt(stt.nextToken());
        int month = Integer.parseInt(stt.nextToken()) - 1;
        int day = Integer.parseInt(stt.nextToken());
       
        if (day <= 0 || year < 0) {
            throw new IllegalArgumentException("Invalid parameters : " + parameters);
        }
        
        if (year < 70) {
            year += 2000;
        } else if (year < 100) {
            year += 1900;
        }
        
        GregorianCalendar c = new GregorianCalendar(year, month, day);
        this.minDate = c.getTimeInMillis();
    }
    
    public String getStringParameters() {
        GregorianCalendar c = new GregorianCalendar();
        c.setTimeInMillis(minDate);
        
        String prefix = this.greaterThan ? "> " : "< ";
        
        return prefix + CalendarUtils.getDateToString(c).replace('_', ' ');
    }
    
    public boolean acceptIteration(RecoveryEntry entry) {
        return true;
    }
    
    public boolean acceptStorage(RecoveryEntry entry) {
        FileSystemRecoveryEntry fEntry = (FileSystemRecoveryEntry)entry;        
        if (fEntry == null) {
            return false;
        } else {         
            boolean value;
            if (FileSystemManager.lastModified(fEntry.getFile()) > minDate) {
                value = greaterThan;
            } else {
                value = ! greaterThan;
            }
            
            if (exclude) {
                return ! value;
            } else {
                return value;
            }
        }
    }
    
    public PublicClonable duplicate() {
        FileDateArchiveFilter filter = new FileDateArchiveFilter();
        filter.exclude = this.exclude;
        filter.minDate = this.minDate;
        filter.greaterThan = this.greaterThan;
        return filter;
    }    

    public long getMinDate() {
        return minDate;
    }
    
    public boolean equals(Object obj) {
        if (obj == null || (! (obj instanceof FileDateArchiveFilter)) ) {
            return false;
        } else {
            FileDateArchiveFilter other = (FileDateArchiveFilter)obj;
            return 
            	EqualsHelper.equals(this.exclude, other.exclude)
            	&& EqualsHelper.equals(this.greaterThan, other.greaterThan)
            	&& EqualsHelper.equals(this.minDate, other.minDate)
           	;
        }
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, this.greaterThan);
        h = HashHelper.hash(h, this.exclude);
        h = HashHelper.hash(h, this.minDate);
        return h;
    }
}
