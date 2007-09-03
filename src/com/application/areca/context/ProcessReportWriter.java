package com.application.areca.context;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import com.application.areca.RecoveryEntry;
import com.application.areca.Utils;
import com.myJava.util.log.FileLogProcessor;
import com.myJava.util.log.Logger;

/**
 * Report Writer : generates a text from the report.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -2622785387388097396
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
public class ProcessReportWriter {

    private Writer writer;
    private boolean writeFilteredEntries = true;
    
    public ProcessReportWriter(Writer writer, boolean writeFilteredEntries) {
        this.writeFilteredEntries = writeFilteredEntries;
        this.writer = writer;
    }
    
    

    public void writeReport(ProcessReport report) throws IOException {
        write("ARECA : BACKUP REPORT");
        writeSeparator();
        write("Target : " + report.getTarget().getTargetName() + " (#" + report.getTarget().getId() + ")");
        write("UID : " + report.getTarget().getUid());
        write("Start date : " + Utils.formatDisplayDate(report.getStartDate()));        
        long dur = System.currentTimeMillis() - report.getStartMillis();
        write("Duration : " + Utils.formatDuration(dur));
        
        if (report.isCommited()) {
            writeCommitedReport(report);
        } else {
            writeRollbackedReport(report);
        }
    }
    
    public void writeCommitedReport(ProcessReport report) throws IOException {
        write("Report status : OK");
        writeSeparator();
        write("Processed directories and files : " + report.getProcessedEntries());
        write("Filtered directories and files : " + report.getFilteredEntries());
        write("Unfiltered directories : " + report.getUnfilteredDirectories());
        write("Unfiltered files : " + report.getUnfilteredFiles());
        write("Ignored files (not modified) : " + report.getIgnoredFiles());
        write("Saved files : " + report.getSavedFiles());
        if (writeFilteredEntries && report.filteredEntriesData != null && (! report.filteredEntriesData.isEmpty())) {
            writeSeparator();
            write("Filtered entries :");
            Iterator iter = report.getFilteredEntriesData().getKeyIterator();
            while (iter.hasNext()) {
                Object key = iter.next();               
                Iterator entries = report.getFilteredEntriesData().getFilteredEntries(key).iterator();
                while (entries.hasNext()) {
                    RecoveryEntry entry = (RecoveryEntry)entries.next();
                    write(entry.getName());
                }
            }
        }
        writeSeparator();
    }
    
    public void writeRollbackedReport(ProcessReport report) throws IOException {
        write("Report status : FAILED !");
        write("See log file for more details : " + ((FileLogProcessor)Logger.defaultLogger().find(FileLogProcessor.class)).getCurrentLogFile());
        writeSeparator();
    }
    
    private void write(String line) throws IOException {
        this.writer.write(line);
        this.writer.write("\n");
    }
    
    private void writeSeparator() throws IOException {
        write("_________________________________________________");
        write(" ");
    }

    public void close() throws IOException {
        this.writer.close();
    }
}
