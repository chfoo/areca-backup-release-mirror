package com.application.areca.context;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import com.application.areca.ArchiveFilter;
import com.application.areca.RecoveryEntry;
import com.application.areca.Utils;
import com.myJava.util.Utilitaire;
import com.myJava.util.log.FileLogProcessor;
import com.myJava.util.log.Logger;

/**
 * Report Writer : generates a text from the report.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -6307890396762748969
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
    
    public ProcessReportWriter(Writer writer) {
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
        
        if (report.getFilteredEntries() != 0) {
	        writeSpace();
	        write("Filtered entries (grouped by filter) :");
	        Iterator iter = report.getFilteredEntriesData().getFilterIterator();
	        while (iter.hasNext()) {
	            ArchiveFilter filter = (ArchiveFilter)iter.next();
	            writeSeparator();
	            String str ="Filter : " + Utilitaire.getClassName(filter.getClass().getName());
	            if (filter.requiresParameters()) {
	                str += " [" + filter.getStringParameters() + "]";
	            }
	            write(str);
	            writeSpace();
	            
	            Iterator entries = report.getFilteredEntriesData().getFilteredEntries(filter).iterator();
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
    
    private void writeSpace() throws IOException {
        write(" ");
    }
    
    public void close() throws IOException {
        this.writer.close();
    }
}
