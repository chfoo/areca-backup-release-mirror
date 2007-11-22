package com.application.areca.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.myJava.file.FileTool;
import com.myJava.file.attributes.Attributes;
import com.myJava.system.OSTool;
import com.myJava.util.Util;
import com.myJava.util.errors.ActionError;
import com.myJava.util.errors.ActionReport;
import com.myJava.util.log.Logger;
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
public abstract class AbstractIncrementalFileSystemMedium 
extends AbstractFileSystemMedium 
implements TargetActions {

    protected static final boolean DEBUG_MODE = ((ArecaTechnicalConfiguration)ArecaTechnicalConfiguration.getInstance()).isBackupDebug();
    
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
           
            context.setTraceAdapter(new ArchiveTraceAdapter(this, traceFile, this.trackDirectories, ((FileSystemRecoveryTarget)this.target).isTrackSymlinks()));
            context.getTraceAdapter().setTrackPermissions(this.trackPermissions);
            context.setContentAdapter(new ArchiveContentAdapter(contentFile));            
            
        } catch (Exception e) {
            throw new ApplicationException(e);
        }
    }
    
    protected void recoverSymLink(FileSystemRecoveryEntry entry, File archive, File destination) throws ApplicationException {
        try {
            String fileName = entry.getName();
            File tmp = new File(fileName);
            File targetFile = new File(destination, FileSystemManager.getName(tmp));
            if (! FileSystemManager.exists(FileSystemManager.getParentFile(targetFile))) {
                tool.createDir(FileSystemManager.getParentFile(targetFile));
            }
            
            String hash = ArchiveTraceCache.getInstance().getTrace(this, archive).getSymLinkHash(entry);
            String path = ArchiveTrace.extractSymLinkPathFromTrace(hash);
            
            FileSystemManager.createSymbolicLink(targetFile, path);
        } catch (IOException e) {
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
                
                if (
                        FileSystemManager.isFile(fEntry.getFile()) && (
                                (! FileSystemManager.isLink(fEntry.getFile()))
                                || (! ((FileSystemRecoveryTarget)this.target).isTrackSymlinks())
                        )
                ) {
                    // The entry is stored if it has been modified
                    if (this.checkFileModified(fEntry, context)) {
                        if (DEBUG_MODE) {
                            Logger.defaultLogger().fine("[" + FileSystemManager.getAbsolutePath(fEntry.getFile()) + "] : Backup in progress ...");
                        }
                        this.storeFileInArchive(fEntry.getFile(), fEntry.getName(), context);
                        context.getReport().addWritten(FileSystemManager.length(fEntry.getFile()));
                        this.registerStoredEntry(fEntry, context);

                        context.getReport().addSavedFile();
                    } else {
                        if (DEBUG_MODE) {
                            Logger.defaultLogger().fine("[" + FileSystemManager.getAbsolutePath(fEntry.getFile()) + "] : Unchanged.");
                        }
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
    
    public void recoverEntry(
            GregorianCalendar date, 
            RecoveryEntry entryToRecover, 
            Object destination,
            ProcessContext context            
    ) throws ApplicationException {
        if (destination == null || entryToRecover == null) {
            return;
        }

        File archive = this.getLastArchive(date);
        try {
            FileSystemRecoveryEntry entry = (FileSystemRecoveryEntry)entryToRecover;
            if (! FileSystemManager.exists((File)destination)) {
                tool.createDir((File)destination);
            }
            
            if (entry.isLink()) {
                recoverSymLink(entry, archive, (File)destination);
            } else {            
                File tmp = new File(entry.getName());
                File targetFile = new File((File)destination, FileSystemManager.getName(tmp));
                
                recoverEntryImpl(archive, entry, destination, context);

                String hash = ArchiveTraceCache.getInstance().getTrace(this, archive).getFileHash(entry);
                if (hash != null) {
                    FileSystemManager.setLastModified(targetFile, ArchiveTrace.extractFileModificationDateFromTrace(hash));
                }
            }
        } catch (ApplicationException e) {
            throw e;            
        } catch (Throwable e) {
            throw new ApplicationException(e);
        }
    }
    
    public abstract void recoverEntryImpl(
            File archive,
            FileSystemRecoveryEntry entry, 
            Object destination,
            ProcessContext context            
    ) throws IOException, TaskCancelledException, ApplicationException;
    
    /**
     * Fermeture de l'archive
     */
    public void commitBackup(ProcessContext context) throws ApplicationException {
        this.target.secureUpdateCurrentTask("Commiting backup ...", context);
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
	            this.target.secureUpdateCurrentTask("Creating a copy of the target's XML configuration ...", context);
	            this.storeTargetConfigBackup(context);
            }
            
            this.target.secureUpdateCurrentTask("Commit completed.", context);
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
    		    Logger.defaultLogger().displayApplicationMessage(
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
        if (context.getFinalArchiveFile() != null) {
            AbstractFileSystemMedium.tool.delete(context.getFinalArchiveFile(), true);
            AbstractFileSystemMedium.tool.delete(getDataDirectory(context.getFinalArchiveFile()), true);
        }

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
        this.target.secureUpdateCurrentTask("Rollbacking backup ...", context);
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
                    } catch (Throwable e) {
                        Logger.defaultLogger().error("Error closing archive", e);
                        if (e instanceof ApplicationException) {
                            throw (ApplicationException)e;
                        } else {
                            throw new ApplicationException("Error closing archive", e);
                        }
                    } finally {    
                        try {
                            // Flush des données
                            FileSystemManager.getInstance().flush(context.getCurrentArchiveFile());
                        } finally {
	                        // Suppression de l'archive de travail
	                        AbstractFileSystemMedium.tool.delete(context.getCurrentArchiveFile(), true);
	                        AbstractFileSystemMedium.tool.delete(this.getDataDirectory(context.getCurrentArchiveFile()), true);
	                        
	                        this.target.secureUpdateCurrentTask("Rollback completed.", context);
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
    public void recover(
            Object destination, 
            String[] filter, 
            GregorianCalendar date, 
            boolean recoverDeletedEntries, 
            ProcessContext context) 
    throws ApplicationException {      
        ArchiveTrace trace = null;
        if (recoverDeletedEntries) {
            trace = this.buildAggregatedTrace(null, date);
        } else {
            trace = ArchiveTraceCache.getInstance().getTrace(this, getLastArchive(date));
        }
        
        recover(destination, filter, 1, null, date, true, trace, context);
        rebuidDirectories((File)destination, filter, trace);
        rebuildSymLinks((File)destination, filter, trace);
    }
    
    private void rebuildSymLinks(File destination, String[] filters, ArchiveTrace trace) throws ApplicationException {
        try {
            if (((FileSystemRecoveryTarget)this.target).isTrackSymlinks()){
                // Rebuild symbolic links
                Iterator iter = trace.symLinkEntrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry)iter.next();
                    File symLink = new File(destination, (String)entry.getKey());
                    
                    if (filters == null || Util.passFilter(FileSystemManager.getAbsolutePath(symLink), filters)) {
                        File parent = symLink.getParentFile();
                        if (! FileSystemManager.exists(parent)) {
                            tool.createDir(parent);
                        }
                        String hash = (String)entry.getValue();
                        FileSystemManager.createSymbolicLink(symLink, ArchiveTrace.extractSymLinkPathFromTrace(hash));
                    }
                }
            }
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }
    
    /**
     * Creates all missing directories from the directory list contained in the trace.
     * <BR>Allows to recover empty directories. 
     */
    private void rebuidDirectories(File destination, String[] filters, ArchiveTrace trace) throws ApplicationException {
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

    public void merge(
            GregorianCalendar fromDate, 
            GregorianCalendar toDate, 
            boolean keepDeletedFiles,
            Manifest mfToInsert,
            ProcessContext context        
    ) throws ApplicationException {
        
        try {
            this.checkRepository();
            
            if (toDate == null) {
                throw new ApplicationException("'To date' is mandatory");
            }
            
            if (! overwrite) { // No "compact" if "overwrite" = true
                Logger.defaultLogger().info(
                        "Starting merge from " + Utils.formatDisplayDate(fromDate) + " to " + Utils.formatDisplayDate(toDate)
                         + ". 'keep' option set to '" + keepDeletedFiles + "'."
                );
                
                // Init des archives
                context.setCurrentArchiveFile(new File(computeArchivePath(toDate) + TMP_ARCHIVE_SUFFIX));
                
                // Nettoyage, en prévention, avant la fusion
                this.cleanMerge(context);
                
                // Restauration dans ce répertoire
                File tmpDestination = new File(FileSystemManager.getAbsolutePath(context.getCurrentArchiveFile()) + TMP_COMPACT_LOCATION_SUFFIX);
                context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.8, "recover");

                ArchiveTrace trace;
                if (keepDeletedFiles) {
                    trace = this.buildAggregatedTrace(fromDate, toDate);
                } else {
                    trace = ArchiveTraceCache.getInstance().getTrace(this, getLastArchive(toDate));
                }

                context.getReport().setRecoveredFiles(recover(
                        tmpDestination, 
                        null, 
                        2,
                        fromDate, 
                        toDate, 
                        false, 
                        keepDeletedFiles ? null : trace, 
                        context
                ));

                context.getInfoChannel().print("Recovery completed - Merged archive creation ...");     
                context.getInfoChannel().updateCurrentTask(0, 0, FileSystemManager.getPath(context.getCurrentArchiveFile()));
                
                // Suppression du manifeste existant
                File mfFile = new File(tmpDestination, getManifestName());
                if (FileSystemManager.exists(mfFile)) {
                    AbstractFileSystemMedium.tool.delete(mfFile, true);
                }
                File oldMfFile = new File(tmpDestination, getOldManifestName()); // Rétro-compatibilité
                if (FileSystemManager.exists(oldMfFile)) {
                    AbstractFileSystemMedium.tool.delete(oldMfFile, true);
                }
                
                context.getTaskMonitor().checkTaskCancellation();
                
                if (context.getReport().getRecoveredFiles().length >= 2) {
                    GregorianCalendar lastArchiveDate = ArchiveManifestCache.getInstance().getManifest(this, context.getReport().getRecoveredFiles()[context.getReport().getRecoveredFiles().length - 1]).getDate();
                    context.setFinalArchiveFile(new File(computeArchivePath(lastArchiveDate)));
                    
	                // Construction de l'archive à un emplacement temporaire
	                buildArchiveFromDirectory(tmpDestination, context.getCurrentArchiveFile(), context);
	                
	                // Construction du manifeste, suite à la fusion
	                if (mfToInsert == null) {
	                    mfToInsert = this.buildDefaultMergeManifest(context.getReport().getRecoveredFiles(), fromDate, toDate);
	                }
	                context.setManifest(mfToInsert);
	                context.getManifest().setType(Manifest.TYPE_COMPACT);
	                context.getManifest().setDate(lastArchiveDate);
	                context.getManifest().addProperty("Source", "Archive merge");
	                context.getManifest().addProperty("Merge start date", Utils.formatDisplayDate(fromDate));
	                context.getManifest().addProperty("Merge end date", Utils.formatDisplayDate(toDate));
	                
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
        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {
            context.getTaskMonitor().getCurrentActiveSubTask().setCurrentCompletion(1);     
            try {
                FileSystemManager.getInstance().flush(context.getCurrentArchiveFile());
            } catch (IOException e) {
                throw new ApplicationException(e);
            }
        }
    } 
    
    protected boolean checkArchive(String basePath, String archivePath, File archive) {
        boolean ok = super.checkArchive(basePath, archivePath, archive);
        if (ok) {
            // Additional check : merge temporary directories
            if (            
                    archivePath.startsWith(basePath + Utils.FILE_DATE_SEPARATOR)
                    && archivePath.endsWith(TMP_COMPACT_LOCATION_SUFFIX)
                    && FileSystemManager.isDirectory(archive)
            ) {     
                destroyTemporaryFile(archive);
                return false;
            }
        }
        return true;
    }
    
    public void cleanMerge(ProcessContext context) throws IOException {
        File tmpDestination = new File(FileSystemManager.getAbsolutePath(context.getCurrentArchiveFile()) + TMP_COMPACT_LOCATION_SUFFIX);
        AbstractFileSystemMedium.tool.delete(tmpDestination, true);
    }
    
    public void commitMerge(ProcessContext context) throws ApplicationException {
        if (! this.overwrite) {
            this.target.secureUpdateCurrentTask("Commiting merge ...", context);
            super.commitMerge(context);
            
            try {
                // Nettoyage des données temporaires
                this.cleanMerge(context);
                
                // Fermeture de l'archive
                this.closeArchive(context); 
                
                if (context.getReport().getRecoveredFiles() != null && context.getReport().getRecoveredFiles().length >= 2) {
                    // Suppression des archives compactées
                    for (int i=0; i<context.getReport().getRecoveredFiles().length; i++) {
                        this.deleteArchive(context.getReport().getRecoveredFiles()[i]);                       
                    }

                    // conversion de l'archive
                    this.convertArchiveToFinal(context);

                    this.target.secureUpdateCurrentTask("Merge completed - " + context.getReport().getRecoveredFiles().length + " archives merged.", context);
                } else {
                    this.target.secureUpdateCurrentTask("Merge completed - No archive merged.", context);
                }

            } catch (IOException e) {		
                Logger.defaultLogger().error("Exception caught during merge commit.", e);
                this.rollbackMerge(context);
                throw new ApplicationException(e);
            }
        }
    }
    
    public void rollbackMerge(ProcessContext context) throws ApplicationException {
        if (! this.overwrite) {
            this.target.secureUpdateCurrentTask("Rollbacking merge ...", context);
            try {
                try {
                    // Nettoyage des données temporaires
                    this.cleanMerge(context);
                } finally {
                    try {
                        // Fermeture de l'archive
                        this.closeArchive(context); 
                    } finally {
                        // Suppression de l'archive
                        AbstractFileSystemMedium.tool.delete(context.getCurrentArchiveFile(), true);
                        AbstractFileSystemMedium.tool.delete(this.getDataDirectory(context.getCurrentArchiveFile()), true);
                        
                        this.target.secureUpdateCurrentTask("Rollback completed.", context);
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
     * <BR>The recovery is actually made if there are at least <code>minimumArchiveNumber</code> archives to recover
     */
    protected File[] recover(
            Object destination, 
            String[] filters,
            int minimumArchiveNumber,
            GregorianCalendar fromDate, 
            GregorianCalendar toDate, 
            boolean applyAttributes,
            ArchiveTrace trace,
            ProcessContext context            
    ) throws ApplicationException {
        if (toDate != null) {
            toDate = (GregorianCalendar)toDate.clone();
            toDate.add(GregorianCalendar.MILLISECOND, 1);
        }
        if (fromDate != null) {
            fromDate = (GregorianCalendar)fromDate.clone();
            fromDate.add(GregorianCalendar.MILLISECOND, -1);
        }
        File[] recoveredArchives = null;
        try {
            File targetFile = (File)destination;
            
            // Première étape : on recopie l'ensemble des archives
            recoveredArchives = this.listArchives(fromDate, toDate);
            
            if (recoveredArchives.length >= minimumArchiveNumber) {
                context.getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.9, "recover");

                // Process Recovery
                boolean useDummyMode = false;
                if (filters != null && filters.length != 0) {
                    for (int i=0; i<filters.length; i++) {
                        if (FileNameUtil.endsWithSeparator(filters[i])) {
                            useDummyMode = true;
                            break;
                        }
                    }
                } else {
                    useDummyMode = true;
                }
                
                if (useDummyMode) {
                    // Dummy mode : recover all archives
                    this.archiveRawRecover(recoveredArchives, filters, targetFile, context);
                } else {
                    // Smart mode : iterate on each entry and recover its latest version only
                    String root = ((FileSystemRecoveryTarget)this.getTarget()).getSourceDirectory();
                    Map entriesByArchive = new HashMap();
                    
                    Set entriesToDispatch = new HashSet();
                    for (int e=0; e<filters.length; e++) {
                        entriesToDispatch.add(new FileSystemRecoveryEntry(root, new File(root, filters[e])));
                    }
                    
                    // Build a list of entries to recover indexed by archive
                    for (int i=recoveredArchives.length - 1; i>=0 && entriesToDispatch.size() > 0; i--) {
                        ArchiveContent content = ArchiveContentManager.getContentForArchive(this, recoveredArchives[i]);
                        Iterator iter = entriesToDispatch.iterator();
                        Set toRemove = new HashSet();
                        while (iter.hasNext()) {
                            FileSystemRecoveryEntry entry = (FileSystemRecoveryEntry)iter.next();
                            if (content.contains(entry)) {
                                List entries = (List)entriesByArchive.get(recoveredArchives[i]);
                                if (entries == null) {
                                    entries = new ArrayList();
                                    entriesByArchive.put(recoveredArchives[i], entries);
                                }
                                entries.add(entry);
                                toRemove.add(entry);
                            }
                        }
                        entriesToDispatch.removeAll(toRemove);
                    }
                    
                    // Recovery
                    Iterator iter = entriesByArchive.keySet().iterator();
                    double share = 0.98/entriesByArchive.size();
                    while (iter.hasNext()) {
                        File archive = (File)iter.next();
                        List entries = (List)entriesByArchive.get(archive);
                        
                        String[] filtersForArchive = new String[entries.size()];
                        for (int i=0; i<filtersForArchive.length; i++) {
                            filtersForArchive[i] = ((RecoveryEntry)entries.get(i)).getName();
                        }
                        
                        context.getInfoChannel().getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(share, FileSystemManager.getAbsolutePath(archive));
                        this.archiveRawRecover(new File[] {archive}, filtersForArchive, targetFile, context);
                    }
                    context.getInfoChannel().getTaskMonitor().getCurrentActiveSubTask().enforceCompletion();
                }
                
                context.getTaskMonitor().checkTaskCancellation();
                
                // Deuxième étape : on nettoie le répertoire cible.
                if (trace != null) {
                    this.applyTrace(
                            targetFile, 
                            trace,
                            applyAttributes,
                            filters,
                            true,
                            context);
                }
            }
            
            // On retourne pour info la liste des fichiers restaurés
            return recoveredArchives;
            
        } catch (IOException e) {
            throw new ApplicationException(e);
        } catch (TaskCancelledException e) {
            throw new ApplicationException(e);
        } finally {
            context.getTaskMonitor().getCurrentActiveSubTask().setCurrentCompletion(1);            
        }
    } 
    
    /**
     * Applique le fichier de trace.
     */
    protected void applyTrace(
            File targetFile, 
            ArchiveTrace trace,
            boolean applyAttributes,
            String[] filters,
            boolean cancelSensitive,
            ProcessContext context
    ) throws IOException, TaskCancelledException {      
        // Nettoyage : on supprime 
        // - Tous les fichiers n'apparaissant pas dans la trace
        // - Tous les répertoires vides
        // - MAIS attention : on ne supprime pas la trace.
        Iterator iter = new FileSystemIterator(targetFile, false);
        while (iter.hasNext()) {
            if (cancelSensitive) {
                context.getTaskMonitor().checkTaskCancellation();  // Check for cancels only if we are cancel sensitive --> useful for "commit"
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
    protected abstract void archiveRawRecover(
            File[] elementaryArchives, 
            String[] filters, 
            File targetFile,
            ProcessContext context
    ) throws ApplicationException;
    
    /**
     * Indique si l'entrée a été modifiée depuis la dernière exécution 
     */
    protected boolean checkFileModified(FileSystemRecoveryEntry fEntry, ProcessContext context) throws IOException {
        return context.getPreviousTrace().hasFileBeenModified(fEntry);
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
        
            return getEntrySetFromTrace(trace, storedFiles);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }
    
    private ArchiveTrace buildAggregatedTrace(GregorianCalendar fromDate, GregorianCalendar toDate) throws ApplicationException {
        Logger.defaultLogger().info("Building aggregated archive trace ...");
        
        if (fromDate != null) {
            fromDate = (GregorianCalendar)fromDate.clone();
            fromDate.add(GregorianCalendar.MILLISECOND, -1);
        }
        
        ArchiveTrace content = new ArchiveTrace();
        File[] archives = this.listArchives(fromDate, toDate);
        for (int i=0; i<archives.length; i++) {
            File archive = archives[i];
            Logger.defaultLogger().info("Merging archive trace (" + FileSystemManager.getAbsolutePath(archive) + ") ...");
            ArchiveTrace trace = ArchiveTraceCache.getInstance().getTrace(this, archive);
            content.merge(trace);
        }
        
        Logger.defaultLogger().info("Aggregated archive trace built.");
        return content;
    }
    
    public Set getLogicalView() throws ApplicationException {
        ArchiveTrace mergedTrace = buildAggregatedTrace(null, null);
        ArchiveTrace latestTrace = ArchiveTraceCache.getInstance().getTrace(this, this.getLastArchive(null));
        
        Map latestContent = new HashMap();
        if (latestTrace != null) {
            latestContent.putAll(latestTrace.getFileMap());
            latestContent.putAll(latestTrace.getDirectoryMap());
            latestContent.putAll(latestTrace.getSymLinkMap());
        }
        
        return getEntrySetFromTrace(mergedTrace, latestContent);
    }
    
    private Set getEntrySetFromTrace(ArchiveTrace source, Map referenceMap) {
        Set elements = new HashSet();
        Iterator iter = source.fileEntrySet().iterator();
        String baseDirectory = this.fileSystemPolicy.getBaseArchivePath();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            
            String entryPath = (String)entry.getKey();
            String entryTrace = (String)entry.getValue();
            
            try {                
                elements.add(new FileSystemRecoveryEntry(
                        baseDirectory, 
                        new File(baseDirectory, entryPath),
                        referenceMap.containsKey(entryPath) ? RecoveryEntry.STATUS_STORED: RecoveryEntry.STATUS_NOT_STORED,
                        ArchiveTrace.extractFileSizeFromTrace(entryTrace)
                ));
            } catch (RuntimeException e) {
                Logger.defaultLogger().error("Error reading archive trace : for file [" + entryPath + "], trace = [" + entryTrace + "]", e);
                throw e;
            }
        }
        
        iter = source.directoryEntrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            String entryPath = (String)entry.getKey();
            
            elements.add(new FileSystemRecoveryEntry(
                    baseDirectory, 
                    new File(baseDirectory, entryPath),
                    referenceMap.containsKey(entryPath) ? RecoveryEntry.STATUS_STORED: RecoveryEntry.STATUS_NOT_STORED,
                    -1
            ));
        }
        
        iter = source.symLinkEntrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            String entryPath = (String)entry.getKey();
            String entryTrace = (String)entry.getValue();
            
            long size = -1;
            if (ArchiveTrace.extractSymLinkFileFromTrace(entryTrace)) {
                size = 0;
            }
            
            elements.add(new FileSystemRecoveryEntry(
                    baseDirectory, 
                    new File(baseDirectory, entryPath),
                    referenceMap.containsKey(entryPath) ? RecoveryEntry.STATUS_STORED: RecoveryEntry.STATUS_NOT_STORED,
                    size,
                    true
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
        
        ArchiveTrace trace = context.getPreviousTrace();
        
        try {
            if (((FileSystemRecoveryTarget)this.target).isTrackSymlinks() && FileSystemManager.isLink(fEntry.getFile())) {
                // Vérification que l'entrée sera stockée.
                if (! trace.containsSymLink(fEntry)) {
                    fEntry.setStatus(EntryArchiveData.STATUS_CREATED);
                } else if (trace.hasSymLinkBeenModified(fEntry)) {
                    fEntry.setStatus(EntryArchiveData.STATUS_MODIFIED);
                } else {
                    fEntry.setStatus(RecoveryEntry.STATUS_NOT_STORED);            
                }
                trace.removeFile(fEntry);
            } else if (FileSystemManager.isFile(fEntry.getFile())) {
                // Vérification que l'entrée sera stockée.
                if (! trace.containsFile(fEntry)) {
                    fEntry.setStatus(EntryArchiveData.STATUS_CREATED);
                } else if (trace.hasFileBeenModified(fEntry)) {
                    fEntry.setStatus(EntryArchiveData.STATUS_MODIFIED);
                } else {
                    fEntry.setStatus(RecoveryEntry.STATUS_NOT_STORED);            
                }
                trace.removeFile(fEntry);
            } else if (this.trackDirectories) {
                if (! trace.containsDirectory(fEntry)) {
                    fEntry.setStatus(EntryArchiveData.STATUS_CREATED);
                } else {
                    fEntry.setStatus(RecoveryEntry.STATUS_NOT_STORED);
                }
                trace.removeDirectory(fEntry);
            } else {
                fEntry.setStatus(RecoveryEntry.STATUS_NOT_STORED);
            }
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }
    
    public List closeSimulation(ProcessContext context) throws ApplicationException {
        ArchiveTrace trace = context.getPreviousTrace();
        if (trace == null) {
            return new ArrayList();
        } else {
            ArrayList ret = new ArrayList(trace.fileSize() + trace.directorySize());
            
            // Files
            Iterator iter = trace.fileEntrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry)iter.next();
                String path = (String)entry.getKey();
                String hash = (String)entry.getValue();
                
                long size = ArchiveTrace.extractFileSizeFromTrace(hash); 
                ret.add(new FileSystemRecoveryEntry(fileSystemPolicy.getBaseArchivePath(), new File(fileSystemPolicy.getBaseArchivePath(), path), EntryArchiveData.STATUS_DELETED, size));
            }
            
            // Directories
            iter = trace.directoryEntrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry)iter.next();
                String path = (String)entry.getKey();               
                ret.add(new FileSystemRecoveryEntry(fileSystemPolicy.getBaseArchivePath(), new File(fileSystemPolicy.getBaseArchivePath(), path), EntryArchiveData.STATUS_DELETED, 0));
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
            ManifestManager.writeManifest(this, context.getManifest(), context.getCurrentArchiveFile());            
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
			
            if (entry.isLink()) {
                // LINK
                if (trace.containsSymLink(entry)) {
                    ead.setStatus(EntryArchiveData.STATUS_UNCHANGED);
                } else {
                    ead.setStatus(EntryArchiveData.STATUS_NONEXISTANT);
                }
            } else {
                // STANDARD FILE / DIRECTORY
    			if (content.contains(entry)) {
    				ead.setStatus(EntryArchiveData.STATUS_CHANGED);
    			} else {
    				if (trace.containsFile(entry)) {
    					ead.setStatus(EntryArchiveData.STATUS_UNCHANGED);
    				} else {
    					ead.setStatus(EntryArchiveData.STATUS_NONEXISTANT);
    				}
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
            if (lastArchive != null) {
                this.searchWithinArchive(criteria, lastArchive, result);
            }
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
        String root = ((FileSystemRecoveryTarget)this.getTarget()).getSourceDirectory();
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