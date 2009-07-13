package com.application.areca.adapters;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import com.application.areca.ApplicationException;
import com.application.areca.TargetGroup;
import com.application.areca.impl.FileSystemRecoveryTarget;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;

/**
 * Process serializer
 * 
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
public class ProcessXMLWriter extends AbstractXMLWriter { 
	//public static final int CURRENT_VERSION = 2;
	//public static final int CURRENT_VERSION = 3; // introduced in v6.1
	//public static final int CURRENT_VERSION = 4; // introduced in v7.1 : Special files filters replace symbolic links filters 
	public static final int CURRENT_VERSION = 5; // introduced in v7.1.3 : follow_subdirectories replaced by follow_subdirs, and fix of serialization bug
	
    private TargetXMLWriter targetWriter;
    
    public ProcessXMLWriter() {
        this(false);
    }
    
    public ProcessXMLWriter(boolean removeEncryptionData) {
        super(new StringBuffer());
        targetWriter = new TargetXMLWriter(this.sb);
        targetWriter.setRemoveSensitiveData(removeEncryptionData);
    }
    
    public void serializeProcess(TargetGroup process) throws ApplicationException {
        serializeProcess(process, process.getSourceFile());
    }
    
    public void serializeProcess(TargetGroup process, File targetFile) throws ApplicationException {
        try {
            FileTool tool = FileTool.getInstance();
            
            if (FileSystemManager.exists(targetFile)) {
                if (! FileSystemManager.delete(targetFile)) {
                    throw new ApplicationException("The destination [" + FileSystemManager.getAbsolutePath(targetFile) + "] is a directory or can't be deleted.");
                }
            }
            
            // Create parent dir if it does not exist
            if (! FileSystemManager.exists(FileSystemManager.getParentFile(targetFile))) {
                tool.createDir(FileSystemManager.getParentFile(targetFile));
            }
            
            writeHeader();
            sb.append("\n<");
            sb.append(XML_PROCESS);
            sb.append(" ");
            sb.append(XML_PROCESS_DESCRIPTION);
            sb.append("=");
            sb.append(encode(process.getComments()));      
            sb.append(" ");
            sb.append(XML_VERSION);
            sb.append("=");
            sb.append(encode(CURRENT_VERSION));   
            sb.append(">");
            
            // Targets
            Iterator iter = process.getTargetIterator();
            while(iter.hasNext()) {
                FileSystemRecoveryTarget tg = (FileSystemRecoveryTarget)iter.next();
                this.targetWriter.serializeTarget(tg);
            }
            
            sb.append("\n</");
            sb.append(XML_PROCESS);
            sb.append(">");        
            
            
            OutputStream fos = FileSystemManager.getFileOutputStream(targetFile);
            OutputStreamWriter fw = new OutputStreamWriter(fos, getEncoding());
            fw.write(this.sb.toString());
            fw.close();
        } catch (UnsupportedEncodingException e) {
            throw new ApplicationException(e);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }
}
