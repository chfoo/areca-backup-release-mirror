package com.application.areca.context;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import com.application.areca.Utils;
import com.application.areca.indicator.Indicator;
import com.application.areca.indicator.IndicatorMap;
import com.application.areca.metadata.content.ArchiveContentAdapter;
import com.application.areca.metadata.content.ContentEntry;
import com.application.areca.metadata.content.ContentFileIterator;
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
public class ProcessReportWriter {
	public static final long MAX_LISTED_FILES = 1000L;
	
	private Writer writer;
	private boolean appendStatistics;
	private boolean appendStoredFiles;
	private long maxFileNumber = -1;

	public ProcessReportWriter(Writer writer, boolean appendStatistics, boolean appendStoredFiles, long maxFileNumber) {
		this.writer = writer;
		this.appendStatistics = appendStatistics;
		this.maxFileNumber = maxFileNumber;
		this.appendStoredFiles = appendStoredFiles;
	}

	private void writeStatusItem(StatusItem itm) throws IOException {
		String hdr = "     " + itm.getKey() + " : ";
		if (itm.isHasErrors()) {
			write(hdr + "Failure");
		} else {
			write(hdr + "Success");
		}
	}

	public void writeReport(ProcessReport report) throws IOException {
		write("" + report.getTarget().getName() + " (" + report.getTarget().getUid() + ") on " + Utils.formatDisplayDate(report.getStartDate()));

		writeSeparator();
		write("Overall Status : " + (report.hasError() ? "Failure":"Success"));
		if (report.getStatus().size() > 0) {
			write("Detailed Status :");
			boolean detailedErrors = false;
			Iterator sttIter = report.getStatus().iterator();
			while (sttIter.hasNext()) {
				StatusItem itm = (StatusItem)sttIter.next();
				writeStatusItem(itm);
				detailedErrors |= itm.isHasErrors();
			}

			if ((! detailedErrors) && report.hasError()) {
				StatusItem itm = new StatusItem();
				itm.setHasErrors(true);
				itm.setKey(StatusList.KEY_PREPARE);
				writeStatusItem(itm);
			}
		}

		writeSeparator();
		long dur = report.getStopMillis() - report.getStartMillis();
		write("Duration (without post-processing) : " + Utils.formatDuration(dur));

		if (
				(report.getStatus().hasItem(StatusList.KEY_BACKUP) || report.getStatus().hasItem(StatusList.KEY_SIMULATE) ) 
				&& (! report.getStatus().hasError(StatusList.KEY_BACKUP))
				&& (! report.getStatus().hasError(StatusList.KEY_SIMULATE))
				) {
			write("Written kbytes : " + report.getWrittenKBytes());
			writeSeparator();
			write("Processed directories and files : " + report.getProcessedEntries());
			write("Filtered directories and files : " + report.getFilteredEntries());
			write("Unfiltered directories : " + report.getUnfilteredDirectories());
			write("Unfiltered files : " + report.getUnfilteredFiles());
			write("Ignored files (not modified) : " + report.getIgnoredFiles());
			write("Saved files : " + report.getSavedFiles());

			if (this.appendStatistics) {
				writeSeparator();
				write("Archive statistics :");

				IndicatorMap indicators = report.getIndicators();
				if (indicators == null) {
					write("<no statistics available>");
				} else {
					Integer[] ids = indicators.getSortedIndicatorKeys();
					for (int i=0; i<ids.length; i++) {
						Integer id = (Integer)ids[i];
						Indicator indicator = indicators.getIndicator(id);
						write(indicator.getName() + " = " + indicator.getStringValue());
					}
				}
			}

			if (this.appendStoredFiles && report.getContentFile() != null) {
				writeSeparator();
				write("Stored files :");
				write("[Beginning]");
				ContentFileIterator iter = ArchiveContentAdapter.buildIterator(report.getContentFile());
				try {
					long count = 0;
					while (iter.hasNext()) {
						ContentEntry entry = iter.next();
						if (++count == maxFileNumber+1) {
							break;
						}
						write(entry.getKey());
					}
					write("[End]");
					if (count != 0 && count == maxFileNumber+1) {
						write("... (File list truncated to " + maxFileNumber + " files)");
					}
				} finally {
					iter.close();
				}
			}
		}

		LogMessagesContainer ctn = report.getLogMessagesContainer();
		if (! ctn.isEmpty()) {
			writeSeparator();
			write("Errors and Warnings :");
			Iterator iter = ctn.iterator();
			while (iter.hasNext()) {
				LogMessage message = (LogMessage)iter.next();
				write(LogHelper.format(message.getLevel(), message.getMessage(), message.getSource(), true).toString());
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
		FileLogProcessor fileProc = (FileLogProcessor)Logger.defaultLogger().find(FileLogProcessor.class);
		if (fileProc != null) {
			write("See log file for more details : " + fileProc.getCurrentLogFile());
		}
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
