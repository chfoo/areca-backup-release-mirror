package com.application.areca.context;

import java.io.IOException;
import java.io.Writer;

import com.application.areca.Utils;
import com.myJava.util.log.FileLogProcessor;
import com.myJava.util.log.Logger;

/**
 * Report Writer : generates a text from the report.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 231019873304483154
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
public class ProcessReportWriter {

    private Writer writer;
    
    public ProcessReportWriter(Writer writer) {
        this.writer = writer;
    }

    public void writeReport(ProcessReport report) throws IOException {
        writeSeparator();
        write("" + report.getTarget().getTargetName() + " (" + report.getTarget().getUid() + ") on " + Utils.formatDisplayDate(report.getStartDate()));
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
        write(" ");
    }

    public void close() throws IOException {
        this.writer.close();
    }
}
