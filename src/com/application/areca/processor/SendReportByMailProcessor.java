package com.application.areca.processor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;

import com.application.areca.ApplicationException;
import com.application.areca.ArecaConfiguration;
import com.application.areca.context.ProcessContext;
import com.application.areca.context.ProcessReport;
import com.application.areca.context.ProcessReportWriter;
import com.application.areca.impl.tools.TagHelper;
import com.application.areca.version.VersionInfos;
import com.myJava.object.Duplicable;
import com.myJava.object.HashHelper;
import com.myJava.util.log.Logger;

/**
 * Sends the report by email
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
public class SendReportByMailProcessor extends AbstractMailSendProcessor {

	private boolean appendStatistics;
	private boolean appendStoredFiles;
	private long maxStoredFiles = -1;

	public SendReportByMailProcessor() {
		super();

		setTitle(VersionInfos.APP_SHORT_NAME + " : backup report for target %TARGET_NAME%.");
		setMessage("Backup report :");
	}

	public boolean requireStatistics() {
		return appendStatistics;
	}

	public boolean isAppendStatistics() {
		return appendStatistics;
	}

	public void setAppendStatistics(boolean appendStatistics) {
		this.appendStatistics = appendStatistics;
	}

	public boolean isAppendStoredFiles() {
		return appendStoredFiles;
	}

	public void setAppendStoredFiles(boolean appendStoredFiles) {
		this.appendStoredFiles = appendStoredFiles;
	}

	public long getMaxStoredFiles() {
		return maxStoredFiles;
	}

	public void setMaxStoredFiles(long maxStoredFiles) {
		this.maxStoredFiles = maxStoredFiles;
	}

	public String getKey() {
		return "Send report by email";
	}

	public void runImpl(ProcessContext context) throws ApplicationException {
		PrintStream str = null;
		ByteArrayOutputStream baos = null;
		Logger.defaultLogger().info("Sending a mail report to : " + this.getRecipients() + " using SMTP server : " + this.getSmtpServer());
		try {
			String subject = TagHelper.replaceParamValues(this.getTitle(), context);
			String content = 
					TagHelper.replaceParamValues(this.getMessage(), context)
					+ "\n"
					+ getReportAsText(context.getReport());

			if (ArecaConfiguration.get().isSMTPDebugMode()) {
				baos = new ByteArrayOutputStream();
				str = new PrintStream(baos);
			}

			sendMail(
					subject,
					content,
					str,
					context
					);
		} catch (Exception e) {
			Logger.defaultLogger().error("Error during mail processing", e);
			throw new ApplicationException("Error during mail processing", e);
		} finally {
			if (baos != null) {
				Logger.defaultLogger().info(baos.toString(), "SendReportByMailProcessor.runImpl()");
			}
		}
	}

	/**
	 * Builds a string representation of the report using a report writer. 
	 */
	private String getReportAsText(ProcessReport report) throws IOException {
		ProcessReportWriter writer = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			writer = new ProcessReportWriter(new OutputStreamWriter(baos), appendStatistics, appendStoredFiles, maxStoredFiles);
			writer.writeReport(report);
		} finally {
			writer.close();            
		}

		return baos.toString();
	}

	public Duplicable duplicate() {
		SendReportByMailProcessor pro = new SendReportByMailProcessor();
		copyAttributes(pro);
		pro.appendStatistics = this.appendStatistics;
		pro.appendStoredFiles = this.appendStoredFiles;
		pro.maxStoredFiles = this.maxStoredFiles;
		return pro;
	}

	public boolean equals(Object obj) {
		if (obj == null || (! (obj instanceof SendReportByMailProcessor)) ) {
			return false;
		} else {
			SendReportByMailProcessor other = (SendReportByMailProcessor)obj;
			return super.equals(other)
					&& this.appendStatistics == other.appendStatistics
					&& this.appendStoredFiles == other.appendStoredFiles
					&& this.maxStoredFiles == other.maxStoredFiles
					;
		}
	}

	public int hashCode() {
		int h = HashHelper.initHash(this);
		h = HashHelper.hash(h, super.hashCode());            
		h = HashHelper.hash(h, this.appendStatistics); 
		h = HashHelper.hash(h, this.appendStoredFiles);  
		h = HashHelper.hash(h, this.maxStoredFiles);  
		return h;
	}
}
