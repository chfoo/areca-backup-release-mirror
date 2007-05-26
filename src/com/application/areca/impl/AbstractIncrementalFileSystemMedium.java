package com.application.areca.impl;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipException;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ApplicationException;
import com.application.areca.ArecaTechnicalConfiguration;
import com.application.areca.EntryArchiveData;
import com.application.areca.Errors;
import com.application.areca.LogHelper;
import com.application.areca.MemoryHelper;
import com.application.areca.RecoveryEntry;
import com.application.areca.StoreException;
import com.application.areca.TargetActions;
import com.application.areca.Utils;
import com.application.areca.cache.ArchiveManifestCache;
import com.application.areca.cache.ArchiveTraceCache;
import com.application.areca.context.ProcessContext;
import com.application.areca.impl.tools.ArchiveComparator;
import com.application.areca.impl.tools.ArchiveNameFilter;
import com.application.areca.metadata.content.ArchiveContent;
import com.application.areca.metadata.content.ArchiveContentAdapter;
import com.application.areca.metadata.content.ArchiveContentManager;
import com.application.areca.metadata.data.MetaData;
import com.application.areca.metadata.data.MetaDataAdapter;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.manifest.ManifestManager;
import com.application.areca.metadata.trace.ArchiveTrace;
import com.application.areca.metadata.trace.ArchiveTraceAdapter;
import com.application.areca.search.DefaultSearchCriteria;
import com.application.areca.search.SearchCriteria;
import com.application.areca.search.SearchMatcher;
import com.application.areca.search.SearchResultItem;
import com.application.areca.search.TargetSearchResult;
import com.application.areca.version.VersionInfos;
import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemIterator;
import com.myJava.file.FileSystemManager;
import com.myJava.file.attributes.Attributes;
import com.myJava.util.errors.ActionError;
import com.myJava.util.errors.ActionReport;
import com.myJava.util.log.Logger;
import com.myJava.util.os.OSTool;
import com.myJava.util.taskmonitor.TaskCancelledException;

/**
 * <BR>Support permettant un stockage incrémental des FileSystemEntries.
 * <BR>Une entrée n'est stockée que si elle a été créée ou modifiée depuis le dernier backup.
 * <BR>La vérification s'appuie sur :
 * <BR>- La taille du fichier
 * <BR>- Sa date de dernière modification
 * 
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
public abstract class AbstractIncrementalFileSystemMedium 
extends AbstractFileSystemMedium 
implements TargetActions {

    /**
     * Nom du fichier de trace.
     */
    protected static final String TRACE_FILE_OLD = "recover.trc.";
    protected static final String TRACE_FILE = "trace";

    /**
     * Content filename
     */
    protected static final String CONTENT_FILE_OLD = "recover.ctn.";
    protected static final String CONTENT_FILE = "content";
    
    /**
     * Metadata filename
     */
    protected static final String METADATA_FILE = "metadata";
    
    /**
     * Suffixe ajouté pour construire l'emplacement temporaire de restauration
     * utilisé pour la fusion des archives.
     */
    protected static final String TMP_COMPACT_LOCATION_SUFFIX = ".compact.tmp";
    
    /**
     * Préfixe du fichier d'archive (nom court du fichier, sans répertoire)
     */
    protected String archiveFilePrefix;
    
    /**
     * Tells wether directories shall be tracked or not
     * <BR>(Allows to recover empty directories)
     */
    protected boolean trackDirectories = false;
    
    /**
     * Tells wether file permissions shall be tracked or not
     */
    protected boolean trackPermissions = false;
    
    /**
     * Tells wether many archives shall be created on just one single archive
     */
    protected boolean overwrite = false;
    
    public void install() throws ApplicationException {
        super.install();
        this.archiveFilePrefix = FileSystemManager.getName(new File(fileSystemPolicy.getBaseArchivePath()));
    }
    
    protected void copyAttributes(Object clone) {
        super.copyAttributes(clone);
        AbstractIncrementalFileSystemMedium other = (AbstractIncrementalFileSystemMedium)clone;
        other.overwrite = this.overwrite;
        other.trackDirectories = this.trackDirectories;
        other.trackPermissions = this.trackPermissions;
    }
    
    public String getArchiveFilePrefix() {
        return archiveFilePrefix;
    }
    
    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public Manifest getManifest(GregorianCalendar date) throws ApplicationException {
        return ManifestManager.readManifestForArchive(this, getLastArchive(date));
    }
    
    public boolean isOverwrite() {
        return this.overwrite;
    }
    
    public ActionReport checkMediumState(int action) {
        ActionReport report = super.checkMediumState(action);
        
        if ((action == ACTION_BACKUP || action == ACTION_ALL ) && ! checkDate()) {
            report.addError(new ActionError(Errors.ERR_C_EXIST, Errors.ERR_M_EXIST));
        }  
        
        return report;
    }
    
    /**
     * Retourne le nom du fichier de trace (sans répertoire) final (après commit)
     */
    public String getTraceFileName(boolean oldFormat) {
        if (oldFormat) {
            return TRACE_FILE_OLD + this.target.getUid();
        } else {
            return TRACE_FILE;            
        }
    }
    
    /**
     * Retourne le nom du fichier de contenu (sans répertoire) final (après commit)
     */
    public String getContentFileName(boolean oldFormat) {
        if (oldFormat) {
            return CONTENT_FILE_OLD + this.target.getUid();
        } else{
            return CONTENT_FILE;
        }
    }
    
    /**
     * Retourne le nom du fichier de metadata (sans répertoire) final (après commit)
     */
    public String getMetaDataFileName() {
        return METADATA_FILE;
    }
    
    /**
     * Ouvre le fichier de trace permettant de déterminer quels fichiers ont été modifiés
     * depuis la dernière exécution.
     */
    public void open(ProcessContext context) throws ApplicationException {      
        try {  
            this.checkRepository();
            
            // Lecture trace précédente
            File lastArchive = this.getLastArchive(null);
            if (lastArchive != null && FileSystemManager.exists(lastArchive)) {
                // Lecture du fichier
                context.setPreviousTrace(ArchiveTraceCache.getInstance().getTrace(this, lastArchive));
            } else {
                // Construction trace vide
                context.setPreviousTrace(new ArchiveTrace());
            } 
            
            // Empty archive creation
            Logger.defaultLogger().info("Opening medium ...");
            LogHelper.logFileInformations("Backup location :", new File(this.getArchivePath()));  
            this.buildArchive(context);      
            
            // TraceWriter creation
            File traceFile = new File(getDataDirectory(context.getCurrentArchiveFile()), getTraceFileName(false));
            File contentFile = new File(getDataDirectory(context.getCurrentArchiveFile()), getContentFileName(false));
           
            context.setTraceAdapter(new ArchiveTraceAdapter(this, traceFile, this.trackDirectories));
            context.getTraceAdapter().setTrackPermissions(this.trackPermissions);
            context.setContentAdapter(new ArchiveContentAdapter(contentFile));            
            
        } catch (Exception e) {
            throw new ApplicationException(e);
        }
    }
    
    /**
     * Construction de l'archive
     */
    protected void buildArchive(ProcessContext context) throws IOException {
        context.setCurrentArchiveFile(new File(computeFinalArchivePath() + TMP_ARCHIVE_SUFFIX));  
        context.setFinalArchiveFile(new File(computeFinalArchivePath()));  
        
        LogHelper.logFileInformations("Working archive : ", context.getCurrentArchiveFile());
        LogHelper.logFileInformations("Final archive : ", context.getFinalArchiveFile());              
    }
    
    /**
     * Retourne le chemin de l'archive
     * @return
     */
    protected String computeArchivePath(GregorianCalendar date) {
        if (this.overwrite) {
            return fileSystemPolicy.getBaseArchivePath() + getArchiveExtension();            
        } else {
            return Utils.dateTimeFileName(fileSystemPolicy.getBaseArchivePath(), date, getArchiveExtension());
        }
    } 
    
    protected abstract String getArchiveExtension();
    
    /**
     * Retourne le chemin de l'archive
     * @return
     */
    protected String computeFinalArchivePath() {
        return computeArchivePath(new GregorianCalendar());
    }    
    
    public boolean isTrackDirectories() {
        return trackDirectories;
    }
    
    public void setTrackDirectories(boolean trackDirectories) {
        this.trackDirectories = trackDirectories;
    }
    
    public boolean isTrackPermissions() {
        return trackPermissions;
    }
    
    public void setTrackPermissions(boolean trackPermissions) {
        this.trackPermissions = trackPermissions;
    }
    
    /**
     * Stores an entry
     */
    public void store(RecoveryEntry entry, ProcessContext context) throws StoreException, ApplicationException {
        if (entry == null) {
            return;
        } else {
            FileSystemRecoveryEntry fEntry = (FileSystemRecoveryEntry)entry;
            try {
                this.registerGenericEntry(fEntry, context);
                
                if (FileSystemManager.isFile(fEntry.getFile())) {
                    
                    // The entry is stored if it has been modified
                    if (this.checkModified(fEntry, context)) {
                        this.storeFileInArchive(fEntry.getFile(), fEntry.getName(), context);
                        this.registerStoredEntry(fEntry, context);

                        context.getReport().addSavedFile();
                    } else {
                        context.getReport().addIgnoredFile();
                    }
                }
            } catch (IOException e) {
                throw new StoreException("Error during storage.", e);
            }
        }
    }
    
    protected abstract void closeArchive(ProcessContext context) throws IOException; 
    
    /**
     * Registers a generic entry - wether it has been filtered or not.
     */
    protected abstract void registerGenericEntry(FileSystemRecoveryEntry entry, ProcessContext context) throws IOException;
    
    /**
     * Registers an entry after it has passed the filters. (hence a stored entry) 
     */
    protected abstract void registerStoredEntry(FileSystemRecoveryEntry entry, ProcessContext context) throws IOException;
    
    /**
     * Fermeture de l'archive
     */
    public void commitBackup(ProcessContext context) throws ApplicationException {
        this.target.secureUpdateCurrentTask("Commiting backup ...");
        long entries = context.getTraceAdapter().getWritten();
        try {  
            writeMetaData(context);
            
            // Fermeture de la trace
            if (context.getTraceAdapter() != null) {
                context.getTraceAdapter().close();
                context.setTraceAdapter(null);
            }
            
            // Fermeture du fichier de contenu
            if (context.getContentAdapter() != null) {
                context.getContentAdapter().close();
                context.setContentAdapter(null);
            }
            
            // Ajout propriétés au manifeste
            context.getManifest().addProperty("Unmodified files (not stored)", "" + context.getReport().getIgnoredFiles());            
            context.getManifest().addProperty("Stored files", "" + context.getReport().getSavedFiles());
            
            // Ajout éventuel du manifeste
            this.storeManifest(context);     
            
            // Fermeture de l'archive
            this.closeArchive(context); 
            
            // Flush des données
            FileSystemManager.getInstance().flush(context.getCurrentArchiveFile());
            
            // conversion de l'archive
            this.convertArchiveToFinal(context);
            
            // Create a copy of the target's XML configuration
            if (ArecaTechnicalConfiguration.get().isXMLBackup()) {
	            this.target.secureUpdateCurrentTask("Creating a copy of the target's XML configuration ...");
	            this.storeTargetConfigBackup(context);
            }
            
            this.target.secureUpdateCurrentTask("Commit completed.");
        } catch (Exception e) {
            if (e instanceof ApplicationException) {
                throw (ApplicationException)e;
            } else {
                throw new ApplicationException(e);
            }
        } finally {
    	    File archive = this.getLastArchive(null);

    	    // Just for security reasons : in some cases (Directory non incremental mediums in particular)
    	    // the data caches won't detect that the archive content has changed and won't refrech their data
    	    // By doing so, we enforce the cache refresh.
    	    ArchiveManifestCache.getInstance().remove(this, archive);
    		ArchiveTraceCache.getInstance().remove(this, archive);    
    	
    		// Check memory usage
    		if (MemoryHelper.isOverQuota(entries)) {
    		    this.getTarget().getProcess().getInfoChannel().displayApplicationMessage(
    		            "" + this.getTarget().getUid(),
    		           MemoryHelper.getMemoryTitle(this.getTarget(), entries),
    		           MemoryHelper.getMemoryMessage(this.getTarget(), entries)
    		    );
    		}
        }
    } 
    
    protected MetaData buildMetaData() {
        MetaData data = new MetaData();
        data.setEngineVersionId(VersionInfos.getLastVersion().getVersionId());
        data.setFileEncoding(OSTool.getIANAFileEncoding());
        data.setEngineBuildId("" + VersionInfos.getBuildId());
        data.setOSName(OSTool.getOSDescription());
        return data;
    }
    
    private void writeMetaData(ProcessContext context) throws IOException {
        // write MetaData
        File metaDataFile = new File(getDataDirectory(context.getCurrentArchiveFile()), getMetaDataFileName());
        MetaDataAdapter adapter = new MetaDataAdapter(metaDataFile);
        adapter.writeMetaData(buildMetaData());
        adapter.close();
    }
    
    protected void convertArchiveToFinal(ProcessContext context) throws IOException, ApplicationException {
        AbstractFileSystemMedium.tool.delete(context.getFinalArchiveFile(), true);
        AbstractFileSystemMedium.tool.delete(getDataDirectory(context.getFinalArchiveFile()), true);
        
        // The temporary archive is renamed into the final archive.
        // 5 attempts ...
        if (FileSystemManager.exists(context.getCurrentArchiveFile())) {
	        int attempts = 3;
	        while (
	                (! FileSystemManager.renameTo(context.getCurrentArchiveFile(), context.getFinalArchiveFile()))
	                && attempts >= 0
	        ) {
	            attempts--;
	            try {
	                Thread.sleep(1000);
	            } catch (InterruptedException ignored) {}
	        }
	        
	        if (attempts < 0) {
	            throw new ApplicationException("Unable to finalize archive.");
	        }
        }
        
        // Do the same with the data directory
        if (FileSystemManager.exists(getDataDirectory(context.getCurrentArchiveFile()))) {
	        int attempts = 3;
	        while (
	                (! FileSystemManager.renameTo(getDataDirectory(context.getCurrentArchiveFile()), getDataDirectory(context.getFinalArchiveFile())))
	                && attempts >= 0
	        ) {
	            attempts--;
	            try {
	                Thread.sleep(1000);
	            } catch (InterruptedException ignored) {}
	        }
	        
	        if (attempts < 0) {
	            throw new ApplicationException("Unable to finalize archive.");
	        }
        }
    }
    
    public void rollbackBackup(ProcessContext context) throws ApplicationException {
        this.target.secureUpdateCurrentTask("Rollbacking backup ...");
        try {
            try {
                // Fermeture de la trace
                if (context.getTraceAdapter() != null) {
                    try {
                        context.getTraceAdapter().close();
                    } finally {
                        context.setTraceAdapter(null);
                    }
                }
            } finally {
                try {
                    // Fermeture de la trace
                    if (context.getContentAdapter() != null) {
                        try {
                            context.getContentAdapter().close();
                        } finally {
                            context.setContentAdapter(null);
                        }
                    }
                } finally {
                    try {
                        this.closeArchive(context);
                    } finally {    
                        try {
                            // Flush des données
                            FileSystemManager.getInstance().flush(context.getCurrentArchiveFile());
                        } finally {
	                        // Suppression de l'archive de travail
	                        AbstractFileSystemMedium.tool.delete(context.getCurrentArchiveFile(), true);
	                        AbstractFileSystemMedium.tool.delete(this.getDataDirectory(context.getCurrentArchiveFile()), true);
	                        
	                        this.target.secureUpdateCurrentTask("Rollback completed.");
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }
    
    /**
     * Rétablit le contenu des archives dans le répertoire demandé, jusqu'à la date précisée.
     */
    public void recover(Object destination, String[] filter, GregorianCalendar date, boolean recoverDeletedEntries, ProcessContext context) throws ApplicationException {      
        ArchiveTrace trace = null;
        if (recoverDeletedEntries) {
            trace = this.buildAggregatedTrace(date, true);
        } else {
            trace = ArchiveTraceCache.getInstance().getTrace(this, getLastArchive(date));
        }
        
        recover(destination, filter, null, date, true, trace, context);
        rebuidDirectoryStructure((File)destination, filter, trace);
    }
    
    /**
     * Creates all missing directories from the directory list contained in the trace.
     * <BR>Allows to recover empty directories. 
     */
    private void rebuidDirectoryStructure(File destination, String[] filters, ArchiveTrace trace) throws ApplicationException {
        try {
            if (filters != null) {
                for (int i=0; i<filters.length; i++) {
                    filters[i] = FileSystemManager.getAbsolutePath(new File(destination, filters[i]));
                    if (FileNameUtil.endsWithSeparator(filters[i])) {
                        filters[i] = filters[i].substring(0, filters[i].length() - 1);
                    }
                }
            }
            
            Iterator iter = trace.getDirectoryList().iterator();
            while (iter.hasNext()) {
                String key = (String)iter.next();
                File dir = new File(destination, key);
                if (matchFilters(dir, filters)) {
	                if (! FileSystemManager.exists(dir)) {
	                    AbstractFileSystemMedium.tool.createDir(dir);
	                }
	                applyDirectoryAttributes(dir, key, trace);
                }
            }
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }
    
    private void applyDirectoryAttributes(File dir, String key, ArchiveTrace trace) throws IOException {
        String hash = trace.getDirectoryHash(key);
        
        if (hash != null) {
            Attributes atts = ArchiveTrace.extractDirectoryAttributesFromTrace(hash);
            if (atts != null) {
                FileSystemManager.applyAttributes(atts, dir);
            }
            
            long lastModificationDate = ArchiveTrace.extractDirectoryModificationDateFromTrace(hash);
            if (lastModificationDate > 0) {
                FileSystemManager.setLastModified(dir, lastModificationDate);
            }
        }
    }
    
    private boolean matchFilters(File dir, String[] filters) {
        if (filters == null) {
            return true;
        } else {
            String tested = FileSystemManager.getAbsolutePath(dir);
            for (int i=0; i<filters.length; i++) {
                if (tested.equals(filters[i]) || tested.startsWith(filters[i] + "/")) {
                    return true;
                }
            }
            
            return false;
        }
    }
    
    /**
     * Liste les archives du medium
     * 
     * @param date
     * @return
     */
    public File[] listArchives(GregorianCalendar fromDate, GregorianCalendar toDate) {
        if (this.overwrite) {
            File f = new File(fileSystemPolicy.getBaseArchivePath() + getArchiveExtension());
            if (FileSystemManager.exists(f)) {
                return new File[] {f};                
            } else {
                return new File[] {};
            }
        } else {
            File rootArchiveDirectory = FileSystemManager.getParentFile(new File(fileSystemPolicy.getBaseArchivePath()));
            File[] elementaryArchives = FileSystemManager.listFiles(rootArchiveDirectory, new ArchiveNameFilter(fromDate, toDate, this));
            
            if (elementaryArchives != null) {
                Arrays.sort(elementaryArchives, new ArchiveComparator(this));
            } else {
                elementaryArchives = new File[0];
            }
            
            return elementaryArchives;
        }
    }  
    
    /**
     * Rétablit le contenu des archives à la date précisée.
     */
    public void compact(
            GregorianCalendar fromDate, 
            GregorianCalendar toDate, 
            Manifest mfToInsert,
            ProcessContext context        
    ) throws ApplicationException {
        
        try {
            this.checkRepository();
            
            if (! overwrite) { // No "compact" if "overwrite" = true
                Logger.defaultLogger().info("Starting merge from " + Utils.formatDisplayDate(fromDate) + " to " + Utils.formatDisplayDate(toDate));
                
                // Init des archives
                context.setCurrentArchiveFile(new File(computeArchivePath(toDate) + TMP_ARCHIVE_SUFFIX));
                context.setFinalArchiveFile(new File(computeArchivePath(toDate)));
                
                // Nettoyage, en prévention, avant la fusion
                this.cleanCompact(context);
                
                // Restauration dans ce répertoire
                File tmpDestination = new File(FileSystemManager.getAbsolutePath(context.getFinalArchiveFile()) + TMP_COMPACT_LOCATION_SUFFIX);
                this.target.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.8);
                ArchiveTrace trace = ArchiveTraceCache.getInstance().getTrace(this, getLastArchive(toDate));
                context.getReport().setRecoveredFiles(recover(tmpDestination, null, fromDate, toDate, false, trace, context));
                this.target.getProcess().getInfoChannel().logInfo(null, "Recovery completed - Merged archive creation ...");     
                this.target.getProcess().getInfoChannel().updateCurrentTask(0, 0, FileSystemManager.getPath(context.getCurrentArchiveFile()));
                
                // Suppression du manifeste existant
                File manifestFile = new File(tmpDestination, MANIFEST_FILE);
                AbstractFileSystemMedium.tool.delete(manifestFile, true);
                
                this.target.getTaskMonitor().checkTaskCancellation();
                
                if (context.getReport().getRecoveredFiles().length > 0) {
	                // Construction de l'archive à un emplacement temporaire
	                buildArchiveFromDirectory(tmpDestination, context.getCurrentArchiveFile(), context);
	                
	                // Construction du manifeste, suite à la fusion
	                if (mfToInsert == null) {
	                    mfToInsert = this.buildDefaultMergeManifest(context.getReport().getRecoveredFiles(), fromDate, toDate);
	                }
	                context.setManifest(mfToInsert);
	                context.getManifest().setType(Manifest.TYPE_COMPACT);
	                context.getManifest().setDate(toDate);
	                context.getManifest().addProperty("Source", "Archive merge");
	                context.getManifest().addProperty("Archive's start date", Utils.formatDisplayDate(fromDate));
	                context.getManifest().addProperty("Archive's end date", Utils.formatDisplayDate(toDate));
	                
	                AbstractRecoveryTarget.addBasicInformationsToManifest(context.getManifest());
	                this.storeManifest(context);
	                
	                // Stockage de la trace
	                File target = new File(getDataDirectory(context.getCurrentArchiveFile()), getTraceFileName(false));
	                context.setTraceAdapter(new ArchiveTraceAdapter(this, target));
	                context.getTraceAdapter().writeTrace(trace);
	                context.getTraceAdapter().close();
	                
	                // Stockage du contenu fusionné
	                ArchiveContent merged = new ArchiveContent();
	                for (int i=0; i<context.getReport().getRecoveredFiles().length; i++) {
	                    merged.override(ArchiveContentManager.getContentForArchive(this, context.getReport().getRecoveredFiles()[i]));
	                }
	                merged.clean(trace);
	                
	                target = new File(getDataDirectory(context.getCurrentArchiveFile()), getContentFileName(false));
	                ArchiveContentAdapter adapter = new ArchiveContentAdapter(target);
	                adapter.writeContent(merged);
	                adapter.close();
	                
	                // Write MetaData
	                writeMetaData(context);
                }
            }
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(e);
        } catch (IOException e) {
            throw new ApplicationException(e);
        } catch (TaskCancelledException e) {
            throw new ApplicationException(e);            
        } finally {
            this.target.getTaskMonitor().getCurrentActiveSubTask().setCurrentCompletion(1);     
            try {
                FileSystemManager.getInstance().flush(context.getCurrentArchiveFile());
            } catch (IOException e) {
                throw new ApplicationException(e);
            }
        }
    } 
    
    public void cleanCompact(ProcessContext context) throws IOException {
        File tmpDestination = new File(FileSystemManager.getAbsolutePath(context.getFinalArchiveFile()) + TMP_COMPACT_LOCATION_SUFFIX);
        AbstractFileSystemMedium.tool.delete(tmpDestination, true);
    }
    
    public void commitCompact(ProcessContext context) throws ApplicationException {
        if (! this.overwrite) {
            this.target.secureUpdateCurrentTask("Commiting merge ...");
            super.commitCompact(context);
            
            try {
                // Nettoyage des données temporaires
                this.cleanCompact(context);
                
                // Fermeture de l'archive
                this.closeArchive(context); 
                
                // Suppression des archives compactées
                if (context.getReport().getRecoveredFiles() != null) {
                    for (int i=0; i<context.getReport().getRecoveredFiles().length; i++) {
                        this.deleteArchive(context.getReport().getRecoveredFiles()[i]);                       
                    }
                }
                
                // conversion de l'archive
                this.convertArchiveToFinal(context);
                
                this.target.secureUpdateCurrentTask("Merge completed.");
            } catch (IOException e) {		
                Logger.defaultLogger().error("Exception caught during merge commit.", e);
                this.rollbackCompact(context);
                throw new ApplicationException(e);
            }
        }
    }
    
    public void rollbackCompact(ProcessContext context) throws ApplicationException {
        if (! this.overwrite) {
            this.target.secureUpdateCurrentTask("Rollbacking merge ...");
            try {
                try {
                    // Nettoyage des données temporaires
                    this.cleanCompact(context);
                } finally {
                    try {
                        // Fermeture de l'archive
                        this.closeArchive(context); 
                    } finally {
                        // Suppression de l'archive
                        AbstractFileSystemMedium.tool.delete(context.getCurrentArchiveFile(), true);
                        AbstractFileSystemMedium.tool.delete(this.getDataDirectory(context.getCurrentArchiveFile()), true);
                        
                        this.target.secureUpdateCurrentTask("Rollback completed.");
                    }
                }
            } catch (IOException e) {
                throw new ApplicationException(e);
            }
        }
    }
    
    protected abstract void buildArchiveFromDirectory(File sourceDir, File destination, ProcessContext context) throws ApplicationException;    
    
    /**
     * Rétablit le contenu des archives dans le répertoire demandé, jusqu'à la date précisée.
     * <BR>Selon la valeur de "deleteArchives", supprime ou non les archives décompressées.
     * <BR>'filters' may be null ...
     */
    protected File[] recover(
            Object destination, 
            String[] filters,
            GregorianCalendar fromDate, 
            GregorianCalendar toDate, 
            boolean applyAttributes,
            ArchiveTrace trace,
            ProcessContext context            
    ) throws ApplicationException {
        toDate = (GregorianCalendar)toDate.clone();
        toDate.add(GregorianCalendar.MILLISECOND, 1);   
        if (fromDate != null) {
            fromDate = (GregorianCalendar)fromDate.clone();
            fromDate.add(GregorianCalendar.MILLISECOND, -1);
        }
        File[] recoveredArchives = null;
        try {
            File targetFile = (File)destination;
            
            // Première étape : on recopie l'ensemble des archives
            recoveredArchives = this.listArchives(fromDate, toDate);
            
            if (recoveredArchives.length != 0) {
                this.target.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.9);
                this.archiveRawRecover(recoveredArchives, filters, targetFile);
                
                this.target.getTaskMonitor().checkTaskCancellation();
                
                // Deuxième étape : on nettoie le répertoire cible.
                this.applyTrace(
                        targetFile, 
                        trace,
                        applyAttributes,
                        true);
            }
            
            // On retourne pour info la liste des fichiers restaurés
            return recoveredArchives;
            
        } catch (IOException e) {
            throw new ApplicationException(e);
        } catch (TaskCancelledException e) {
            throw new ApplicationException(e);
        } finally {
            this.target.getTaskMonitor().getCurrentActiveSubTask().setCurrentCompletion(1);            
        }
    } 
    
    /**
     * Applique le fichier de trace.
     */
    protected void applyTrace(
            File targetFile, 
            ArchiveTrace trace,
            boolean applyAttributes,
            boolean cancelSensitive
    ) throws IOException, TaskCancelledException {      
        // Nettoyage : on supprime 
        // - Tous les fichiers n'apparaissant pas dans la trace
        // - Tous les répertoires vides
        // - MAIS attention : on ne supprime pas la trace.
        Iterator iter = new FileSystemIterator(targetFile);
        while (iter.hasNext()) {
            if (cancelSensitive) {
                this.target.getTaskMonitor().checkTaskCancellation();  // Check for cancels only if we are cancel sensitive --> useful for "commit"
            }
            
            File f = (File)iter.next();
            if (FileSystemManager.isFile(f)) {
                String shortPath = Utils.extractShortFilePath(f, targetFile);
                String hash = trace.getFileHash(shortPath);
                
                if (hash == null) {
                    // No trace found for the current file --> destroy it
                    deleteRecur(targetFile, f);
                } else if (applyAttributes) {
                    // File found --> set additional attributes
                    FileSystemManager.setLastModified(f, ArchiveTrace.extractFileModificationDateFromTrace(hash));
                    Attributes atts = ArchiveTrace.extractFileAttributesFromTrace(hash);
                    if (atts != null) {
                        FileSystemManager.applyAttributes(atts, f);
                    }
                }
            } else {
                // On tente bêtement de supprimer le répertoire
                // Il ne le sera que s'il ne contient aucun fichier
                deleteRecur(targetFile, f); 
            }
        }
    }
    
    private void deleteRecur(File root, File current) {
        if (FileSystemManager.delete(current)) {
            File parent = FileSystemManager.getParentFile(current);
            if (FileNameUtil.normalizePath(FileSystemManager.getAbsolutePath(parent))
                    .startsWith(FileNameUtil.normalizePath(FileSystemManager.getAbsolutePath(root)))) {
                deleteRecur(root, parent); // Le répertoire parent ne sera supprimé que s'il est vide
            }
        }
    }
    
    /**
     * Récupération brute du contenu de l'archive sans trace, ni suppression des fichiers obsolètes. 
     * <BR>'filters' can be null ...
     */
    protected abstract void archiveRawRecover(File[] elementaryArchives, String[] filters, File targetFile) throws ApplicationException;
    
    /**
     * Indique si l'entrée a été modifiée depuis la dernière exécution 
     */
    protected boolean checkModified(FileSystemRecoveryEntry fEntry, ProcessContext context) {
        return context.getPreviousTrace().hasBeenModified(fEntry);
    }
    
    /**
     * Vérifie si l'archive existe ou non
     * (éviter les écrasements)
     */
    protected boolean checkDate() {
        File f = new File(Utils.dateTimeFileName(fileSystemPolicy.getBaseArchivePath(), getArchiveExtension()));
        return ! FileSystemManager.exists(f);
    }
    
    // Retourne une map de taille indexée par path
    protected Map formatContent(File file) throws IOException {
        Map map = new HashMap();
        ArchiveContent content = ArchiveContentManager.getContentForArchive(this, file);
        Iterator iter = content.getContent();
        while (iter.hasNext()) {
            FileSystemRecoveryEntry entry = (FileSystemRecoveryEntry)iter.next();
            map.put(
                    entry.getName(),
                    new Long(entry.getSize())
            );
        }
        return map;
    }
    
    public Set getEntries(File archive) throws ApplicationException {
        try {
            Map storedFiles = this.formatContent(archive);
            
            ArchiveTrace trace = ArchiveTraceCache.getInstance().getTrace(this, archive);
            HashSet elements = new HashSet();
            
            Iterator iter = trace.fileEntrySet().iterator();
            File baseDirectory = ((FileSystemRecoveryTarget)this.getTarget()).getSourceDirectory();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry)iter.next();
                
                String entryPath = (String)entry.getKey();
                String entryTrace = (String)entry.getValue();
                
                elements.add(new FileSystemRecoveryEntry(
                        baseDirectory, 
                        new File(baseDirectory, entryPath),
                        storedFiles.keySet().contains(entryPath) ? RecoveryEntry.STATUS_STORED : RecoveryEntry.STATUS_NOT_STORED,
                                ArchiveTrace.extractFileSizeFromTrace(entryTrace)
                ));
            }
            
            return elements;
        } catch (ZipException e) {
            throw new ApplicationException(e);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }
    
    private ArchiveTrace buildAggregatedTrace(GregorianCalendar dateLimit, boolean includeDirectories) throws ApplicationException {
        Logger.defaultLogger().info("Building aggregated archive trace ...");
        
        ArchiveTrace content = new ArchiveTrace();
        File[] archives = this.listArchives(null, dateLimit);
        for (int i=0; i<archives.length; i++) {
            File archive = archives[i];
            Logger.defaultLogger().info("Merging archive trace (" + FileSystemManager.getAbsolutePath(archive) + ") ...");
            ArchiveTrace trace = ArchiveTraceCache.getInstance().getTrace(this, archive);
            content.merge(trace, includeDirectories);
        }
        
        Logger.defaultLogger().info("Aggregated archive trace built.");
        return content;
    }
    
    public Set getLogicalView() throws ApplicationException {
        Set elements = new HashSet();
        
        Map merged = buildAggregatedTrace(null, false).getFileMap();
        ArchiveTrace latestTrace = ArchiveTraceCache.getInstance().getTrace(this, this.getLastArchive(null));
        Map latestContent = latestTrace == null ? new HashMap() : latestTrace.getFileMap();
        
        Iterator iter = merged.entrySet().iterator();
        File baseDirectory = ((FileSystemRecoveryTarget)this.getTarget()).getSourceDirectory();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            
            String entryPath = (String)entry.getKey();
            String entryTrace = (String)entry.getValue();
            
            elements.add(new FileSystemRecoveryEntry(
                    baseDirectory, 
                    new File(baseDirectory, entryPath),
                    latestContent.containsKey(entryPath) ? RecoveryEntry.STATUS_STORED: RecoveryEntry.STATUS_NOT_STORED,
                    ArchiveTrace.extractFileSizeFromTrace(entryTrace)
            ));
        }
   
        return elements;
    }
    
    /**
     * Retourne la dernière archive précédant une date donnée
     */
    public File getLastArchive(GregorianCalendar date) {
        String defaultName = computeArchivePath(date);
        File defaultFile = new File(defaultName);
        
        if (FileSystemManager.exists(defaultFile)) {
            return defaultFile;
        } else {
            File[] archives = listArchives(null, date);
            if (archives.length == 0) {
                return null;
            } else {
                return archives[archives.length - 1];    
            }
        }
    }
    
    public void simulateEntryProcessing(RecoveryEntry entry, ProcessContext context) throws ApplicationException {
        FileSystemRecoveryEntry fEntry = (FileSystemRecoveryEntry)entry;
        if (FileSystemManager.isFile(fEntry.getFile())) {
            // Init du contexte
            if (! context.isInitialized()) {
                File archive = this.getLastArchive(null);
                ArchiveTrace trace = ArchiveTraceCache.getInstance().getTrace(this, archive);
                
                ArchiveTrace cloned = null;
                if (trace != null) {
                    cloned = trace.cloneTrace();
                } else {
                    cloned = new ArchiveTrace();
                }
                
                context.setPreviousTrace(cloned);
                context.setInitialized();
            }
            
            // Vérification que l'entrée sera stockée.
            ArchiveTrace trace = context.getPreviousTrace();
            
            if (! trace.contains(fEntry)) {
                fEntry.setStatus(EntryArchiveData.STATUS_CREATED);
            } else if (trace.hasBeenModified(fEntry)) {
                fEntry.setStatus(EntryArchiveData.STATUS_MODIFIED);
            } else {
                fEntry.setStatus(RecoveryEntry.STATUS_NOT_STORED);            
            }
            
            trace.remove(fEntry);
        } else {
            fEntry.setStatus(RecoveryEntry.STATUS_NOT_STORED);    
        }
    }
    
    public List closeSimulation(ProcessContext context) throws ApplicationException {
        ArchiveTrace trace = context.getPreviousTrace();
        if (trace == null) {
            return new ArrayList();
        } else {
            ArrayList ret = new ArrayList(trace.fileSize());
            
            Iterator iter = trace.fileEntrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry)iter.next();
                String path = (String)entry.getKey();
                String hash = (String)entry.getValue();
                
                long size = ArchiveTrace.extractFileSizeFromTrace(hash); 
                ret.add(new FileSystemRecoveryEntry(new File(fileSystemPolicy.getBaseArchivePath()), new File(fileSystemPolicy.getBaseArchivePath(), path), EntryArchiveData.STATUS_DELETED, size));
            }
            
            return ret;
        }
    }
    
    public Manifest buildDefaultBackupManifest() throws ApplicationException {
        Manifest manifest = new Manifest();
        
        Manifest lastMf = ArchiveManifestCache.getInstance().getManifest(this, this.getLastArchive(null));
        if (lastMf != null) {
            manifest.setAuthor(lastMf.getAuthor());
        	manifest.setTitle(lastMf.getTitle());
        }
        manifest.setType(Manifest.TYPE_BACKUP);
        
        return manifest;
    }
    
    public Manifest buildDefaultMergeManifest(File[] recoveredArchives, GregorianCalendar fromDate, GregorianCalendar toDate) throws ApplicationException {
        Manifest manifest = new Manifest();
        manifest.setType(Manifest.TYPE_COMPACT);
        manifest.setDate(toDate);
        if (fromDate == null) {
            manifest.setTitle("Merge as of " + Utils.formatDisplayDate(toDate));            
        } else {
            manifest.setTitle("Merge from " + Utils.formatDisplayDate(fromDate) + " to " + Utils.formatDisplayDate(toDate));
        }
        StringBuffer sb = new StringBuffer();
        for (int i = recoveredArchives.length - 1; i>=0; i--) {
            Manifest mf = ArchiveManifestCache.getInstance().getManifest(this, recoveredArchives[i]);
            if (mf != null) {
                if (i != recoveredArchives.length - 1) {
                    sb.append("\n\n");
                }
                sb.append(Utils.formatDisplayDate(mf.getDate()));
                if (mf.getAuthor() != null && mf.getAuthor().length() != 0) {
                    sb.append(" (");
                    sb.append(mf.getAuthor());
                    sb.append(")");
                }
                if ((mf.getTitle() != null && mf.getTitle().length() != 0) || (mf.getDescription() != null && mf.getDescription().length() != 0)) {
                    sb.append(" :");
                }
                if (mf.getTitle() != null && mf.getTitle().length() != 0) {
                    sb.append(" ");
                    sb.append(mf.getTitle());
                }
                if (mf.getDescription() != null && mf.getDescription().length() != 0) {
                    sb.append("\n");
                    sb.append(mf.getDescription());
                }
            }
        }
        manifest.setDescription(sb.toString());    
        return manifest;
    }
    
    /**
     * Ajout du manifeste au medium.
     * Réinitialise également le manifeste à <null>.
     * Après l'appel à cette méthode, le manifeste n'est donc plus accessible.
     */
    public void storeManifest(ProcessContext context) throws ApplicationException {
        if (context.getManifest() != null) {
            // Création du manifeste
            File manifestFile;
            try {
                File metadataDir = getDataDirectory(context.getCurrentArchiveFile());
                
                if (! FileSystemManager.exists(metadataDir)) {
                    AbstractFileSystemMedium.tool.createDir(metadataDir);
                }
                manifestFile = new File(metadataDir, MANIFEST_FILE);
                Writer fw = FileSystemManager.getWriter(manifestFile);
                fw.write(context.getManifest().encode());
                fw.flush();
                fw.close();
            } catch (IOException e) {
                throw new ApplicationException(e);
            }                
        }
    }  
    
    /**
     * Deletes the archive - WETHER IT IS COMMITED OR NOT
     */
    protected void deleteArchive(File archive) throws IOException {
        AbstractFileSystemMedium.tool.delete(archive, true);
        AbstractFileSystemMedium.tool.delete(this.getDataDirectory(archive), true);
    }

    /**
     * Retourne le status de l'entrée, dans l'archive spécifiée.
     * 
     * @param entry
     * @param archive
     * @return
     * @throws ApplicationException
     */
	protected EntryArchiveData getArchiveData(FileSystemRecoveryEntry entry, File archive) throws ApplicationException {
		try {
			EntryArchiveData ead = new EntryArchiveData();
			Manifest mf = ArchiveManifestCache.getInstance().getManifest(this, archive);
			ead.setManifest(mf);
			
			ArchiveContent content = ArchiveContentManager.getContentForArchive(this, archive);
			ArchiveTrace trace = ArchiveTraceCache.getInstance().getTrace(this, archive);
			
			if (content.contains(entry)) {
				ead.setStatus(EntryArchiveData.STATUS_CHANGED);
			} else {
				if (trace.contains(entry)) {
					ead.setStatus(EntryArchiveData.STATUS_UNCHANGED);
				} else {
					ead.setStatus(EntryArchiveData.STATUS_NONEXISTANT);
				}
			}
			
			return ead;
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}

    public TargetSearchResult search(SearchCriteria criteria) throws ApplicationException {
        TargetSearchResult result = new TargetSearchResult();
        DefaultSearchCriteria dCriteria = (DefaultSearchCriteria)criteria;
        
        if (dCriteria.isRestrictLatestArchive()) {
            File lastArchive = this.getLastArchive(null);
            this.searchWithinArchive(criteria, lastArchive, result);
        } else {
            File[] archives = this.listArchives(null, null);
            for (int i=0; i<archives.length; i++) {
                this.searchWithinArchive(criteria, archives[i], result);
            }
        }
        
        return result;
    }
    
    private void searchWithinArchive(SearchCriteria criteria, File archive, TargetSearchResult result) throws ApplicationException {
        SearchMatcher matcher = new SearchMatcher((DefaultSearchCriteria)criteria);
        
        ArchiveTrace trace = ArchiveTraceCache.getInstance().getTrace(this, archive);
        Manifest mf = ArchiveManifestCache.getInstance().getManifest(this, archive);
        File root = ((FileSystemRecoveryTarget)this.getTarget()).getSourceDirectory();
        Iterator iter = trace.fileKeySet().iterator();
        while (iter.hasNext()) {
            String name = (String)iter.next();
            if (matcher.matches(name)) {
                File path = new File(root, name);
                FileSystemRecoveryEntry entry = new FileSystemRecoveryEntry(root, path, RecoveryEntry.STATUS_STORED);
                SearchResultItem item = new SearchResultItem();
                item.setCalendar(mf.getDate());
                item.setEntry(entry);
                item.setTarget(this.getTarget());
                
                result.addSearchresultItem(item);
            }
        }
    }
}