package com.application.areca.postprocess;

import java.io.File;
import java.io.IOException;

import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.application.areca.context.ProcessReport;
import com.application.areca.context.ProcessReportWriter;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.util.EqualsHelper;
import com.myJava.util.HashHelper;
import com.myJava.util.PublicClonable;
import com.myJava.util.log.Logger;

/**
 * Dumps the report into a file
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4945525256658487980
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
public class FileDumpPostProcessor extends AbstractPostProcessor {

    private File destinationFolder;

    /**
     * @param target
     */
    public FileDumpPostProcessor() {
        super();
    }

    public File getDestinationFolder() {
        return destinationFolder;
    }
    
    public void setDestinationFolder(File destinationFolder) {
        this.destinationFolder = destinationFolder;
    }
    
    public void run(ProcessContext context) throws ApplicationException {
        ProcessReportWriter writer = null;
        try {
            ProcessReport report = context.getReport();
            if (! FileSystemManager.exists(destinationFolder)) {
                FileTool tool = new FileTool();
                tool.createDir(destinationFolder);
            }
            File destination = new File(destinationFolder, report.getTarget().getUid() + "_" + FileSystemManager.getName(context.getFinalArchiveFile()) + ".report");
            
            writer = new ProcessReportWriter(FileSystemManager.getWriter(destination));
            writer.writeReport(report);
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
    
    public boolean requiresProcessReport() {
        return true;
    }
    
    public String getParametersSummary() {
        return FileSystemManager.getAbsolutePath(this.destinationFolder);
    }
    
    public PublicClonable duplicate() {
        FileDumpPostProcessor pro = new FileDumpPostProcessor();
        pro.destinationFolder = this.destinationFolder;
        return pro;
    }

    public void validate() throws PostProcessorValidationException {
        if (FileSystemManager.isFile(this.destinationFolder)) {
            throw new PostProcessorValidationException("Invalid argument : '" + FileSystemManager.getAbsolutePath(this.destinationFolder) + "' is a file - Please select a directory.");
        }
    }

    public boolean equals(Object obj) {
        if (obj == null || (! (obj instanceof FileDumpPostProcessor)) ) {
            return false;
        } else {
            FileDumpPostProcessor other = (FileDumpPostProcessor)obj;
            return EqualsHelper.equals(this.destinationFolder, other.destinationFolder);
        }
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, this.destinationFolder.getAbsolutePath());
        return h;
    }
}
