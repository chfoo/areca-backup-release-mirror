package com.application.areca.context;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import com.application.areca.Utils;
import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.util.log.FileLogProcessor;
import com.myJava.util.log.LogHelper;
import com.myJava.util.log.LogMessage;
import com.myJava.util.log.LogMessagesContainer;
import com.myJava.util.log.Logger;

/**
 * Report Writer : generates a text from the report.
 * <BR>
 * @author Olivier PETRUCCI
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
public class ProcessReportWriter {
    private Writer writer;
    
    public ProcessReportWriter(Writer writer) {
        this.writer = writer;
    }

    public void writeReport(ProcessReport report) throws IOException {
        write("" + report.getTarget().getTargetName() + " (" + report.getTarget().getUid() + ") on " + Utils.formatDisplayDate(report.getStartDate()));
        
        writeSeparator();
        write("Overall Status : " + (report.getStatus().hasError() ? "Failure":"Success"));
        if (report.getStatus().size() > 1) {
	        write("Detailed Status :");
	        Iterator sttIter = report.getStatus().iterator();
	        while (sttIter.hasNext()) {
	        	StatusItem itm = (StatusItem)sttIter.next();
	        	String hdr = "     " + itm.getKey() + " : ";
	        	if (itm.isHasErrors()) {
	        		write(hdr + "Failure");
	        	} else {
	        		write(hdr + "Success");
	        	}
	        }
        }
        
        writeSeparator();
        long dur = System.currentTimeMillis() - report.getStartMillis();
        write("Duration : " + Utils.formatDuration(dur));
        
        if (! report.getStatus().hasError(StatusList.KEY_BACKUP)) {
            write("Written kbytes : " + report.getWrittenKBytes());
            writeSeparator();
            write("Processed directories and files : " + report.getProcessedEntries());
            write("Filtered directories and files : " + report.getFilteredEntries());
            write("Unfiltered directories : " + report.getUnfilteredDirectories());
            write("Unfiltered files : " + report.getUnfilteredFiles());
            write("Ignored files (not modified) : " + report.getIgnoredFiles());
            write("Saved files : " + report.getSavedFiles());
        }
        
        LogMessagesContainer ctn = report.getLogMessagesContainer();
        if (! ctn.isEmpty()) {
            writeSeparator();
        	write("Errors and Warnings :");
        	Iterator iter = ctn.iterator();
        	while (iter.hasNext()) {
        		LogMessage message = (LogMessage)iter.next();
        		write(com.myJava.util.log.LogHelper.format(message.getLevel(), message.getMessage(), message.getSource(), true));
        		if (message.getException() != null) {
        			write(LogHelper.formatException(message.getException()));
        		}
        	}
        	
        	if (ctn.isMaxSizeReached()) {
                writeSeparator();
        		write("Maximum number of messages reached (" + FrameworkConfiguration.getInstance().getMaxInlineLogMessages() + "). You can increase it by modifying the '" + FrameworkConfiguration.KEY_MAX_INLINE_LOG_MESSAGES + "' property in your technical configuration.");
        	}
        }
    	
        writeSeparator();
        write("See log file for more details : " + ((FileLogProcessor)Logger.defaultLogger().find(FileLogProcessor.class)).getCurrentLogFile());
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
