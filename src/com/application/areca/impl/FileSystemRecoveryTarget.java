package com.application.areca.impl;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ApplicationException;
import com.application.areca.Errors;
import com.application.areca.LogHelper;
import com.application.areca.RecoveryEntry;
import com.application.areca.TargetActions;
import com.application.areca.context.ProcessContext;
import com.application.areca.metadata.manifest.Manifest;
import com.myJava.file.FileSystemManager;
import com.myJava.util.PublicClonable;
import com.myJava.util.errors.ActionError;
import com.myJava.util.errors.ActionReport;
import com.myJava.util.log.Logger;

/**
 * Type de cible dédiée au backup de fichiers.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2162742295696737000
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
public class FileSystemRecoveryTarget 
extends AbstractRecoveryTarget 
implements TargetActions {

    public static final String RECOVERY_LOCATION_SUFFIX = "recovered_data";
    
    protected File sourcePath;
   
    public PublicClonable duplicate() {
        FileSystemRecoveryTarget other = new FileSystemRecoveryTarget();
        copyAttributes(other);
        return other;
    }
    
    protected void copyAttributes(Object clone) {
        FileSystemRecoveryTarget other = (FileSystemRecoveryTarget)clone;
        super.copyAttributes(other);
        other.sourcePath = sourcePath;
    }

    public void setSourcePath(File sourcePath) {
        this.sourcePath = sourcePath;
    }
    
    public File getSourcePath() {
        return this.sourcePath;
    }
    
    public File getSourceDirectory() {
        if (FileSystemManager.isFile(this.sourcePath)) {
            return FileSystemManager.getParentFile(this.sourcePath);
        } else {
            return this.sourcePath;
        }
    }
    
    public void commitBackup(ProcessContext context) throws ApplicationException {
        context.getManifest().addProperty("Unfiltered directories", "" + context.getReport().getUnfilteredDirectories());
        context.getManifest().addProperty("Unfiltered files", "" + context.getReport().getUnfilteredFiles());        
        context.getManifest().addProperty("Scanned entries (files or directories)", "" + (context.getReport().getUnfilteredFiles() + context.getReport().getUnfilteredDirectories() + context.getReport().getFilteredEntries()));        
        context.getManifest().addProperty("Source path", FileSystemManager.getAbsolutePath(this.sourcePath));
        
        super.commitBackup(context);
    }
    
    /**
     * Retourne le prochain élément.
     * Attention : ca peut être un fichier ou un répertoire.
     * Cet élément est filtré par appel aux filtres.
     */
    public RecoveryEntry nextElement(ProcessContext context) throws ApplicationException {
        double completionStep = 0.98 / (context.getCurrentLevel().getSize() == 0 ? 1 : context.getCurrentLevel().getSize());
        if (context.getCurrentLevel().hasMoreElements()) {
            File f = context.getCurrentLevel().nextElement();
            FileSystemRecoveryEntry entry = new FileSystemRecoveryEntry(this.getSourceDirectory(), f);
            if (this.acceptEntry(entry, context)) {
	            if (FileSystemManager.isDirectory(f)) {
	                context.getFileSystemLevels().push(context.getCurrentLevel());
	                context.setCurrentLevel(new FileSystemLevel(f));
	                
	                // Progress information
	                this.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(completionStep);
	                
	                // MAJ stats
	                context.getReport().addDirectoryCount();
	            } else {
	                entry.setSize(FileSystemManager.length(f));
	                this.getTaskMonitor().getCurrentActiveSubTask().addCompletion(completionStep);
	                context.getReport().addFileCount();
	            }
	            return entry;  
            } else {
                this.getTaskMonitor().getCurrentActiveSubTask().addCompletion(completionStep);                
                return this.nextElement(context);
            }
        } else {
            this.getTaskMonitor().getCurrentActiveSubTask().setCurrentCompletion(1.0);
            
            if (context.getFileSystemLevels().isEmpty()) {
                return null;
            } else {
                context.setCurrentLevel((FileSystemLevel)context.getFileSystemLevels().pop());
                return this.nextElement(context);
            }
        }
    }
    
    public String getFullName() {
        String tName = this.targetName;
        if (tName == null) {
            tName = "Target " + this.getId();
        }
        return tName + " - FileSystem (" + FileSystemManager.getAbsolutePath(this.sourcePath) + ")";
    }
    
    public void open(Manifest manifest, ProcessContext context) throws ApplicationException {
        Logger.defaultLogger().info("Opening Target ...");
        LogHelper.logFileInformations("Source path : ", this.sourcePath);
        
        super.open(manifest, context);
        initCurrentLevel(context);
    }   
    
    private void initCurrentLevel(ProcessContext context) throws ApplicationException {
        context.getFileSystemLevels().clear();
        context.setCurrentLevel(new FileSystemLevel(sourcePath)); 
    }
    

    public synchronized RecoveryEntry[] processSimulateImpl(ProcessContext context, boolean returnDetailedResults) throws ApplicationException {
        this.initCurrentLevel(context);
        return super.processSimulateImpl(context, returnDetailedResults);
    }
    
    /**
     * Lance le recover sur la target
     */    
    public void processRecoverImpl(String destination, String[] filters, GregorianCalendar date, boolean recoverDeletedEntries, ProcessContext context) throws ApplicationException {
        this.medium.recover(new File(destination, RECOVERY_LOCATION_SUFFIX), filters, date, recoverDeletedEntries, context);
    }  
    
    /**
     * Lance le recover sur la target
     */    
    public void processRecoverImpl(String destination, GregorianCalendar date, RecoveryEntry entry, ProcessContext context) throws ApplicationException {
        this.medium.recoverEntry(date, entry, new File(destination), context);
    }  
    
    /**
     * @see AbstractRecoveryTarget#getSpecificTargetDescription()
     */
    protected String getSpecificTargetDescription() {
        return "Target - FileSystem (" + FileSystemManager.getAbsolutePath(this.sourcePath) + ")";
    }

    /**
     * Valide l'état de la target
     */
    public ActionReport checkTargetState(int action) {
        
        // Validation
        ActionReport result = super.checkTargetState(action);
        
        File f = sourcePath;
        if (action != ACTION_RECOVER && action != ACTION_COMPACT_OR_DELETE) {
	        if (! FileSystemManager.exists(f)) {
	            result.addError(new ActionError(Errors.ERR_C_BASETARGETPATH, Errors.ERR_M_BASETARGETPATH));
	        }
        }
        
        return result;
    }
    
    public Manifest buildDefaultMergeManifest(GregorianCalendar fromDate, GregorianCalendar toDate) throws ApplicationException {
        if (! (this.medium instanceof AbstractIncrementalFileSystemMedium)) {
            throw new IllegalArgumentException("Only incremental mediums are supported by this method");
        } else {
            AbstractIncrementalFileSystemMedium mdm = (AbstractIncrementalFileSystemMedium)this.medium;
            
            GregorianCalendar mFromDate = (GregorianCalendar)fromDate.clone();
            mFromDate.add(Calendar.MILLISECOND, -1);
            GregorianCalendar mToDate = (GregorianCalendar)toDate.clone();
            mToDate.add(Calendar.MILLISECOND, 1);
            File[] recoveredFiles = mdm.listArchives(mFromDate, mToDate);
            return mdm.buildDefaultMergeManifest(recoveredFiles, fromDate, toDate);
        }
    }
}
