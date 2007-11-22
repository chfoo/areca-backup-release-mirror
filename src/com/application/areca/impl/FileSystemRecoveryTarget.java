package com.application.areca.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ApplicationException;
import com.application.areca.Errors;
import com.application.areca.RecoveryEntry;
import com.application.areca.TargetActions;
import com.application.areca.context.ProcessContext;
import com.application.areca.metadata.manifest.Manifest;
import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.object.DuplicateHelper;
import com.myJava.object.PublicClonable;
import com.myJava.util.errors.ActionError;
import com.myJava.util.errors.ActionReport;
import com.myJava.util.log.Logger;

/**
 * Type de cible dédiée au backup de fichiers.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2156529904998511409
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
    
    protected String sourcesRoot = "";
    protected Set sources;
    
    /**
     * Tells wether symbolic are considered as normal files or as symbolic links
     */
    protected boolean trackSymlinks = false;
    
    public PublicClonable duplicate() {
        FileSystemRecoveryTarget other = new FileSystemRecoveryTarget();
        copyAttributes(other);
        return other;
    }
    
    protected void copyAttributes(Object clone) {
        FileSystemRecoveryTarget other = (FileSystemRecoveryTarget)clone;
        super.copyAttributes(other);
        other.sourcesRoot = sourcesRoot;
        other.trackSymlinks = trackSymlinks;
        other.sources = DuplicateHelper.duplicate(sources, false);
    }

    public void setSources(Set sources) {
        this.sources = sources;
        deduplicate(this.sources);
        this.computeSourceRoot();
    }
    
    /**
     * Removes duplicate sources
     */
    private static void deduplicate(Set sources) {
        HashSet toRemove = new HashSet();
        FileTool tool = FileTool.getInstance();
        
        Iterator iter1 = sources.iterator();
        while (iter1.hasNext()) {
            File tested = (File)iter1.next();
            
            Iterator iter2 = sources.iterator();
            while (iter2.hasNext()) {
                File potentialParent = (File)iter2.next();
                
                if (potentialParent != tested) {  // Yes, it's an instance check
                    if (tool.isParentOf(potentialParent, tested)) {
                        toRemove.add(tested);
                    }
                }
            }
        }
        
        sources.removeAll(toRemove);
    }

    public Set getSources() {
        return sources;
    }

    private void computeSourceRoot() {
        List paths = new ArrayList();
        int min = Integer.MAX_VALUE;
        if (sources.size() > 0) {
            Iterator iter = this.sources.iterator();
            while (iter.hasNext()) {
                File source = (File)iter.next();
                ArrayList path = new ArrayList();
                computeParents(source, path);
                paths.add(path);
                if (path.size() < min) {
                    min = path.size();
                }
            }
            
            int divergenceIndex = -1;
            for (int token=0; token<min && divergenceIndex == -1; token++) {
                File current = (File)((List)paths.get(0)).get(token);
                String currentStr = FileSystemManager.getAbsolutePath(current);
                for (int s=1; s<this.sources.size() && divergenceIndex == -1; s++) {
                    File other = (File)((List)paths.get(s)).get(token);
                    if (! currentStr.equals(FileSystemManager.getAbsolutePath(other))) {
                        divergenceIndex = token;
                    }
                }
            }
            
            if (divergenceIndex == 0) {
                sourcesRoot = "";
            } else if (divergenceIndex == -1) {
                sourcesRoot = FileSystemManager.getAbsolutePath((File)this.sources.iterator().next());
            } else {
                sourcesRoot = FileSystemManager.getAbsolutePath((File)((List)paths.get(0)).get(divergenceIndex - 1));
            }
        } else {
            sourcesRoot = "";
        }
    }
    
    private static void computeParents(File f, List l) {
        l.add(0, f);
        File parent = FileSystemManager.getParentFile(f);
        if (parent != null) {
            computeParents(parent, l);
        }
    }
    
    public String getSourcesRoot() {
        return this.sourcesRoot;
    }
    
    public String getSourceDirectory() {
        if (sourcesRoot.length() == 0) {
            return sourcesRoot;
        } else {
            File f = new File(sourcesRoot);
            
            if (FileSystemManager.isFile(f)) {
                return FileSystemManager.getAbsolutePath(FileSystemManager.getParentFile(f));
            } else {
                return this.sourcesRoot;
            }
        }
    }

    public boolean isTrackSymlinks() {
        return trackSymlinks;
    }

    public void setTrackSymlinks(boolean trackSymlinks) {
        this.trackSymlinks = trackSymlinks;
    }

    public void commitBackup(ProcessContext context) throws ApplicationException {
        context.getManifest().addProperty("Unfiltered directories", "" + context.getReport().getUnfilteredDirectories());
        context.getManifest().addProperty("Unfiltered files", "" + context.getReport().getUnfilteredFiles());        
        context.getManifest().addProperty("Scanned entries (files or directories)", "" + (context.getReport().getUnfilteredFiles() + context.getReport().getUnfilteredDirectories() + context.getReport().getFilteredEntries()));        
        context.getManifest().addProperty("Source path", this.sourcesRoot);
        
        super.commitBackup(context);
    }
    
    /**
     * Retourne le prochain élément.
     * Attention : ca peut être un fichier ou un répertoire.
     * Cet élément est filtré par appel aux filtres.
     */
    public RecoveryEntry nextElement(ProcessContext context) throws ApplicationException {

        while (true) {
            // Locate the first storable file
            while (context.getCurrentLevel().hasMoreElements()) {
                File f = context.getCurrentLevel().nextElement();
                FileSystemRecoveryEntry entry = new FileSystemRecoveryEntry(this.getSourceDirectory(), f);
    
                double completionStep = 0.98 / (context.getCurrentLevel().getSize() == 0 ? 1 : context.getCurrentLevel().getSize());
                if (context.getCurrentLevel().getParent() == null) {
                    completionStep /= Math.max(context.getRootCount(), 1);
                }
                
                try {
                    if (
                            FileSystemManager.isDirectory(f) && ((! trackSymlinks) || (! FileSystemManager.isLink(f)))
                    ) {
                        // check if we can iterate on this directory
                        if (this.acceptEntry(entry, false, context)) {
                            context.getFileSystemLevels().push(context.getCurrentLevel());
                            context.setCurrentLevel(new FileSystemLevel(f, context.getCurrentLevel()));
                            
                            // Progress information
                            context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(completionStep, FileSystemManager.getAbsolutePath(f));
                            Logger.defaultLogger().fine("Processing " + FileSystemManager.getAbsolutePath(f));
                            
                            // stats update
                            context.getReport().addDirectoryCount();
                        } else {
                            context.getTaskMonitor().getCurrentActiveSubTask().addCompletion(completionStep);
                        }
                    } else {
                        // Check if we can store the file
                        context.getTaskMonitor().getCurrentActiveSubTask().addCompletion(completionStep);
                        if (this.acceptEntry(entry, true, context)) {
                            entry.setSize(FileSystemManager.length(f));
                            context.getReport().addFileCount();
                            context.getCurrentLevel().setHaveFilesBeenStored(true);
                            return entry;
                        }
                    }
                } catch (IOException e) {
                    throw new ApplicationException(e);
                }
            }
            
            // No more elements - process the next file system level
            FileSystemRecoveryEntry entry = new FileSystemRecoveryEntry(
                    this.getSourceDirectory(), 
                    context.getCurrentLevel().getBaseDirectory()
            );
            
            // Check if we can store the directory
            if (
                    ( ! context.getCurrentLevel().isHasBeenSent())
                    && (! FileSystemManager.isFile(context.getCurrentLevel().getBaseDirectory()))
                    && entry.getName().length() != 0
                    && (
                            context.getCurrentLevel().isHaveFilesBeenStored()
                            || this.acceptEntry(entry, true, context)
                    )
            ) {
                context.getCurrentLevel().setHasBeenSent(true);
                if (context.getCurrentLevel().getParent() != null) {
                    context.getCurrentLevel().getParent().setHaveFilesBeenStored(true);
                }
    
                return entry;
            } else {               
                if (context.getFileSystemLevels().isEmpty()) {
                    context.getTaskMonitor().getCurrentActiveSubTask().setCurrentCompletion(1.0);
                    return null;
                } else {
                    if (context.getCurrentLevel().getParent() != null) {
                        context.getTaskMonitor().getCurrentActiveSubTask().setCurrentCompletion(1.0);
                    }
                    context.setCurrentLevel((FileSystemLevel)context.getFileSystemLevels().pop());
                }
            }
        }
    }
    
    public String getFullName() {
        String tName = this.targetName;
        if (tName == null) {
            tName = "Target " + this.getId();
        }
        return tName + (this.sourcesRoot.length() != 0 ? " - FileSystem (" + this.sourcesRoot + ")" : "");
    }
    
    public void open(Manifest manifest, ProcessContext context) throws ApplicationException {
        Logger.defaultLogger().info("Initializing backup context ...");
        Logger.defaultLogger().info("Global source root : " + this.sourcesRoot);
        
        super.open(manifest, context);
        initCurrentLevel(context);
        Logger.defaultLogger().info("Backup context initialized.");
    }   
    
    private void initCurrentLevel(ProcessContext context) throws ApplicationException {
        context.getFileSystemLevels().clear();
        Iterator iter = this.sources.iterator();
        while (iter.hasNext()) {
            File source = (File)iter.next();
            Logger.defaultLogger().info("Registering source directory : " + FileSystemManager.getAbsolutePath(source));
            context.getFileSystemLevels().push(new FileSystemLevel(source, null));
        }
        context.setRootCount(this.sources.size());
        context.setCurrentLevel((FileSystemLevel)context.getFileSystemLevels().pop()); 
    }
    

    public synchronized RecoveryEntry[] processSimulateImpl(ProcessContext context, boolean returnDetailedResults) throws ApplicationException {
        Logger.defaultLogger().info("Initializing simulation context ...");
        this.initCurrentLevel(context);
        Logger.defaultLogger().info("Simulation context initialized.");
        return super.processSimulateImpl(context, returnDetailedResults);
    }
    
    /**
     * Lance le recover sur la target
     */    
    public void processRecoverImpl(String destination, String[] filters, GregorianCalendar date, boolean recoverDeletedEntries, ProcessContext context) throws ApplicationException {
        this.medium.recover(
                new File(normalizeDestination(destination), RECOVERY_LOCATION_SUFFIX), 
                filters, 
                date, 
                recoverDeletedEntries, 
                context
         );
    }  
    
    /**
     * Lance le recover sur la target
     */    
    public void processRecoverImpl(String destination, GregorianCalendar date, RecoveryEntry entry, ProcessContext context) throws ApplicationException {
        this.medium.recoverEntry(date, entry, new File(normalizeDestination(destination)), context);
    } 
    
    private static String normalizeDestination(String destination) {
        if (destination == null) {
            return null;
        } else if (FileNameUtil.endsWithSeparator(destination)) {
            return destination.substring(0, destination.length() - 1);  
        } else {
            return destination;
        }
    }
    
    /**
     * @see AbstractRecoveryTarget#getSpecificTargetDescription()
     */
    protected String getSpecificTargetDescription() {
        return "Target" + (this.sourcesRoot.length() != 0 ? " - FileSystem (" + this.sourcesRoot + ")" : "");
    }

    /**
     * Valide l'état de la target
     */
    public ActionReport checkTargetState(int action) {
        
        // Validation
        ActionReport result = super.checkTargetState(action);
        
        if (action != ACTION_RECOVER && action != ACTION_COMPACT_OR_DELETE) {
            Iterator iter = this.sources.iterator();
            while (iter.hasNext()) {
                File src = (File)iter.next();
                if (! FileSystemManager.exists(src)) {
                    result.addError(new ActionError(Errors.ERR_C_BASETARGETPATH, Errors.ERR_M_BASETARGETPATH + " (" + FileSystemManager.getAbsolutePath(src) + ")"));
                }
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
