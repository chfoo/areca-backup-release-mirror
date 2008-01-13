package com.application.areca.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.application.areca.context.ProcessReport;
import com.application.areca.context.ProcessReportWriter;
import com.application.areca.impl.TagHelper;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.object.PublicClonable;
import com.myJava.util.log.Logger;

/**
 * Dumps the report into a file
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2367131098465853703
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
public class FileDumpProcessor extends AbstractProcessor {

    private File destinationFolder;
    private String reportName = "%TARGET_UID%_%ARCHIVE_NAME%.report";
    private boolean onlyIfError;
    private boolean listFiltered = true;

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

    public boolean isListFiltered() {
        return listFiltered;
    }

    public void setListFiltered(boolean listFiltered) {
        this.listFiltered = listFiltered;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String name) {
        this.reportName = name;
    }

    public boolean isOnlyIfError() {
        return onlyIfError;
    }

    public void setOnlyIfError(boolean onlyIfError) {
        this.onlyIfError = onlyIfError;
    }

    public void runImpl(ProcessContext context) throws ApplicationException {
        if ((! context.getReport().isCommited()) || (! this.onlyIfError)) {
            ProcessReportWriter writer = null;
            File destination = new File(
                    destinationFolder, 
                    TagHelper.replaceParamValues(
                            TagHelper.replaceTag(reportName, TagHelper.PARAM_ARCHIVE, TagHelper.PARAM_ARCHIVE_NAME), // The %ARCHIVE% tag cannot be used here !
                            context
                    )
            );
            Logger.defaultLogger().info("Writing backup report on : " + FileSystemManager.getAbsolutePath(destination));
            
            try {
                ProcessReport report = context.getReport();
                if (! FileSystemManager.exists(destinationFolder)) {
                    FileTool tool = FileTool.getInstance();
                    tool.createDir(destinationFolder);
                }
                writer = new ProcessReportWriter(FileSystemManager.getWriter(destination), listFiltered);
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
        } else {
            Logger.defaultLogger().info("No report file was written on disk because the backup was successfull");
        }
    }

    public boolean requiresFilteredEntriesListing() {
        return listFiltered;
    }

    public String getParametersSummary() {
        return FileSystemManager.getAbsolutePath(this.destinationFolder);
    }

    public PublicClonable duplicate() {
        FileDumpProcessor pro = new FileDumpProcessor();
        pro.destinationFolder = this.destinationFolder;
        pro.reportName = this.reportName;
        pro.listFiltered = this.listFiltered;
        pro.onlyIfError = this.onlyIfError;
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
                EqualsHelper.equals(this.destinationFolder, other.destinationFolder)
                && EqualsHelper.equals(this.reportName, other.reportName)
                && EqualsHelper.equals(this.listFiltered, other.listFiltered)
                && EqualsHelper.equals(this.onlyIfError, other.onlyIfError)
            ;
        }
    }

    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, FileSystemManager.getAbsolutePath(this.destinationFolder));
        h = HashHelper.hash(h, this.reportName);
        h = HashHelper.hash(h, this.listFiltered);
        h = HashHelper.hash(h, this.onlyIfError);
        return h;
    }
}
