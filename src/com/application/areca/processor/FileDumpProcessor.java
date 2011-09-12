package com.application.areca.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.application.areca.context.ProcessReport;
import com.application.areca.context.ProcessReportWriter;
import com.application.areca.impl.tools.TagHelper;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.object.Duplicable;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.util.log.Logger;

/**
 * Dump the report into a file
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2011, Olivier PETRUCCI.

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
public class FileDumpProcessor extends AbstractProcessor {

	private File destinationFolder;
	private String reportName = "%TARGET_UID%_%ARCHIVE_NAME%.report";
	private boolean appendStatistics;

	/**
	 * @param target
	 */
	public FileDumpProcessor() {
		super();
	}

	public File getDestinationFolder() {
		return destinationFolder;
	}

	public void setDestinationFolder(File destinationFolder) {
		this.destinationFolder = destinationFolder;
	}

	public boolean requireStatictics() {
		return appendStatistics;
	}

	public boolean isAppendStatistics() {
		return appendStatistics;
	}

	public void setAppendStatistics(boolean appendStatistics) {
		this.appendStatistics = appendStatistics;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String name) {
		this.reportName = name;
	}

	public void runImpl(ProcessContext context) throws ApplicationException {	
		ProcessReportWriter writer = null;
		File destination = new File(
				destinationFolder, 
				TagHelper.replaceParamValues(
						TagHelper.replaceTag(reportName, TagHelper.PARAM_ARCHIVE_PATH, TagHelper.PARAM_ARCHIVE_NAME), // The %ARCHIVE% tag cannot be used here !
						context
				)
		);
		Logger.defaultLogger().info("Writing backup report on : " + FileSystemManager.getAbsolutePath(destination));

		try {
			ProcessReport report = context.getReport();
			File parent = FileSystemManager.getParentFile(destination);
			if (! FileSystemManager.exists(parent)) {
				FileTool tool = FileTool.getInstance();
				tool.createDir(parent);
			}
			writer = new ProcessReportWriter(FileSystemManager.getWriter(destination), appendStatistics);
			writer.writeReport(report);
		} catch (FileNotFoundException e) {
			Logger.defaultLogger().error("The report filename is incorrect : " + FileSystemManager.getAbsolutePath(destination), e);            
			throw new IllegalArgumentException("The report filename is incorrect : " + FileSystemManager.getAbsolutePath(destination));
		} catch (IOException e) {
			Logger.defaultLogger().error("Exception caught during report generation", e);            
			throw new ApplicationException("Exception caught during report generation", e);
		} finally {
			try {
				writer.close();
			} catch (IOException e1) {
				Logger.defaultLogger().error("Exception caught during report generation", e1);
			}
		}
	}

	public String getParametersSummary() {
		return FileSystemManager.getAbsolutePath(this.destinationFolder);
	}

	public Duplicable duplicate() {
		FileDumpProcessor pro = new FileDumpProcessor();
		copyAttributes(pro);
		pro.destinationFolder = this.destinationFolder;
		pro.reportName = this.reportName;
		return pro;
	}

	public void validate() throws ProcessorValidationException {
		if (FileSystemManager.isFile(this.destinationFolder)) {
			throw new ProcessorValidationException("Invalid argument : '" + FileSystemManager.getAbsolutePath(this.destinationFolder) + "' is a file - Please select a directory.");
		}
	}

	public boolean equals(Object obj) {
		if (obj == null || (! (obj instanceof FileDumpProcessor)) ) {
			return false;
		} else {
			FileDumpProcessor other = (FileDumpProcessor)obj;
			return                 
			super.equals(other)
			&& EqualsHelper.equals(this.destinationFolder, other.destinationFolder)
			&& EqualsHelper.equals(this.reportName, other.reportName)
			;
		}
	}

	public int hashCode() {
		int h = HashHelper.initHash(this);
		h = HashHelper.hash(h, super.hashCode());
		h = HashHelper.hash(h, FileSystemManager.getAbsolutePath(this.destinationFolder));
		h = HashHelper.hash(h, this.reportName);
		return h;
	}
	
	public String getKey() {
		return "Write report";
	}
}
