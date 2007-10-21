package com.application.areca.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Set;

import com.application.areca.AbstractMedium;
import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ApplicationException;
import com.application.areca.ArecaTechnicalConfiguration;
import com.application.areca.EntryArchiveData;
import com.application.areca.Errors;
import com.application.areca.LogHelper;
import com.application.areca.RecoveryEntry;
import com.application.areca.RecoveryProcess;
import com.application.areca.TargetActions;
import com.application.areca.Utils;
import com.application.areca.adapters.ProcessXMLWriter;
import com.application.areca.cache.ArchiveManifestCache;
import com.application.areca.cache.ArchiveTraceCache;
import com.application.areca.context.ProcessContext;
import com.application.areca.impl.policy.EncryptionPolicy;
import com.application.areca.impl.policy.FileSystemPolicy;
import com.application.areca.indicator.Indicator;
import com.application.areca.indicator.IndicatorMap;
import com.application.areca.indicator.IndicatorTypes;
import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.driver.DriverAlreadySetException;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.file.driver.event.EventFileSystemDriver;
import com.myJava.file.driver.event.LoggerFileSystemDriverListener;
import com.myJava.object.ToStringHelper;
import com.myJava.util.errors.ActionError;
import com.myJava.util.errors.ActionReport;
import com.myJava.util.history.DefaultHistory;
import com.myJava.util.history.History;
import com.myJava.util.log.Logger;

/**
 * Support de stockage s'appuyant sur un système de fichiers.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 5653799526062900358
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
public abstract class AbstractFileSystemMedium 
extends AbstractMedium 
implements TargetActions, IndicatorTypes {

    private static final boolean REPOSITORY_ACCESS_DEBUG = ((ArecaTechnicalConfiguration)ArecaTechnicalConfiguration.getInstance()).isRepositoryAccessDebugMode();
    private static final String REPOSITORY_ACCESS_DEBUG_ID = "Areca repository access";
    
	protected static final String TMP_ARCHIVE_SUFFIX = ".not_commited";

    /**
     * Suffix added to the archive name to create the data directory (containing the manifest and trace)
     */
	protected static final String DATA_DIRECTORY_SUFFIX = "_data";
	
    /**
     * Nom du fichier de manifeste.
     */
    protected static final String MANIFEST_FILE = "manifest";   
    protected static final String MANIFEST_FILE_OLD = "manifest.txt";   
    
    /**
     * Name used for target configuration backup
     */
    protected static final String TARGET_BACKUP_FILE_PREFIX = "/areca_config_backup/target_backup_";
    protected static final String TARGET_BACKUP_FILE_SUFFIX = ".xml";
    
    /**
     * Filetool utilisé pour les manipulations sur fichiers
     */
    protected static final FileTool tool = FileTool.getInstance();

    /**
     * Encryption arguments
     */
    protected EncryptionPolicy encryptionPolicy = null;
    
    protected FileSystemPolicy fileSystemPolicy = null;
    
    public void install() throws ApplicationException {
        super.install();
        
        File storageDir = new File(fileSystemPolicy.getBaseArchivePath()).getParentFile();
        
        FileSystemDriver baseDriver = this.fileSystemPolicy.initFileSystemDriver();
        FileSystemDriver encryptedDriver = this.encryptionPolicy.initFileSystemDriver(storageDir, baseDriver);
        
        try {
            try {
                if (REPOSITORY_ACCESS_DEBUG) {
                    baseDriver = new EventFileSystemDriver(baseDriver, REPOSITORY_ACCESS_DEBUG_ID, new LoggerFileSystemDriverListener());
                }
                FileSystemManager.getInstance().registerDriver(storageDir.getParentFile(), baseDriver);
            } catch (Exception e) {
                // Non-fatal error but DANGEROUS : It is highly advised to store archives in subdirectories - not at the root
                Logger.defaultLogger().warn("Error trying to register a driver at [" + storageDir + "]'s parent directory. It is probably because you tried to store archives at the root directory (/ or c:\\). It is HIGHLY advised to use subdirectories.", e, "Driver initialization");
            }
            
            if (REPOSITORY_ACCESS_DEBUG) {
                encryptedDriver = new EventFileSystemDriver(encryptedDriver, REPOSITORY_ACCESS_DEBUG_ID, new LoggerFileSystemDriverListener());
            }
            FileSystemManager.getInstance().registerDriver(storageDir, encryptedDriver);
        } catch (DriverAlreadySetException e) {
            Logger.defaultLogger().error(e);
            throw new ApplicationException(e);
        } catch (IOException e) {
            Logger.defaultLogger().error(e);
            throw new ApplicationException(e);
        }
    }    

    public void setTarget(AbstractRecoveryTarget target, boolean revalidate) {
        super.setTarget(target, revalidate);
        if (revalidate) {
            fileSystemPolicy.synchronizeConfiguration();
        }
    }
    
    public void setEncryptionPolicy(EncryptionPolicy encryptionPolicy) {
        this.encryptionPolicy = encryptionPolicy;
    }
    
    public void setFileSystemPolicy(FileSystemPolicy fileSystemPolicy) {
        this.fileSystemPolicy = fileSystemPolicy;
        this.fileSystemPolicy.setMedium(this);
    }
    
    protected void copyAttributes(Object clone) {
        super.copyAttributes(clone);
        AbstractFileSystemMedium other = (AbstractFileSystemMedium)clone;
        other.encryptionPolicy = (EncryptionPolicy)this.encryptionPolicy.duplicate();
        other.setFileSystemPolicy((FileSystemPolicy)this.fileSystemPolicy.duplicate());
    }
    
    public String getManifestName() {
        return MANIFEST_FILE;
    }
    
    public String getOldManifestName() {
        return MANIFEST_FILE_OLD; // Maintained for backward-compatibility purpose
    }

    public String getBaseArchivePath() {
        return fileSystemPolicy.getBaseArchivePath();
    } 

    public void destroyRepository() throws ApplicationException {
        File storage = FileSystemManager.getParentFile(new File(this.getBaseArchivePath()));
        Logger.defaultLogger().info("Deleting repository : " + FileSystemManager.getAbsolutePath(storage) + " ...");
        try {
            FileTool tool = FileTool.getInstance();
            tool.delete(storage, true);
            Logger.defaultLogger().info(FileSystemManager.getAbsolutePath(storage) + " deleted.");
        } catch (Exception e) {
            throw new ApplicationException("Error trying to delete directory : " + FileSystemManager.getAbsolutePath(storage), e);
        }
    }

    /**
     * Checks that the archive provided as argument belongs to this medium
     */
    public boolean checkArchiveCompatibility(File archive) {
        String archivePath = FileSystemManager.getAbsolutePath(archive);
        String basePath = FileSystemManager.getAbsolutePath(new File(this.getBaseArchivePath()));
        
        return 
        	archivePath.equals(basePath)
        	|| (
        		archivePath.startsWith(basePath)
        		&& (! archivePath.endsWith(TMP_ARCHIVE_SUFFIX))
        		&& (! archivePath.endsWith(DATA_DIRECTORY_SUFFIX))        		
        	)
        ;
    }

    public synchronized History getHistory() {
        if (this.history == null) {
            Logger.defaultLogger().info("No history found ... initializing data ...");
	        try {
	            // historique
	            this.history = new DefaultHistory(
	                    new File(
	                            FileSystemManager.getParentFile(
	                                    new File(this.fileSystemPolicy.getBaseArchivePath())
	                            ), 
	                            this.getHistoryName()
	                    )
	            );
	            Logger.defaultLogger().info("History loaded.");
	        } catch (Throwable e) {
	            Logger.defaultLogger().error("Error during history loading", e);
	            this.history = null;
	        }
        }
        return this.history;
    }
    
    /**
     * Valide diverses règles de gestion, notamment le fait que la gestion du cryptage est activée ou désactivée explicitement.
     */
    public ActionReport checkMediumState(int action) {
        ActionReport result = new ActionReport();
        
        if (encryptionPolicy == null) {
            result.addError(new ActionError(Errors.ERR_C_MEDIUM_ENCRYPTION_NOT_INITIALIZED, Errors.ERR_M_MEDIUM_ENCRYPTION_NOT_INITIALIZED));
        }
        
        File basePath = new File(fileSystemPolicy.getBaseArchivePath());
        if (action != ACTION_DESCRIBE) {       
	        // The backup directory mustn't be included in the base directory
	        File backupDir = FileSystemManager.getParentFile(basePath);
            
            Iterator iter = ((FileSystemRecoveryTarget)this.getTarget()).sources.iterator();
            while (iter.hasNext()) {
                File src = (File)iter.next();
                if (AbstractFileSystemMedium.tool.isParentOf(src, backupDir)) {
                    result.addError(new ActionError(Errors.ERR_C_INCLUSION, Errors.ERR_M_INCLUSION));               
                }
            }
        }
        
        return result;
    }
    
    /**
     * Retourne le chemin des archives sous un format affichable à l'utilisateur 
     */
	public String getArchivePath() {
		return FileSystemManager.getAbsolutePath(FileSystemManager.getParentFile(new File(fileSystemPolicy.getBaseArchivePath())));
	}
	
	public String getDisplayArchivePath() {
		return this.fileSystemPolicy.getDisplayableParameters();
	}
	
	protected void checkRepository() {
	    File storageDir = FileSystemManager.getParentFile(new File(fileSystemPolicy.getBaseArchivePath()));
	    File[] archives = FileSystemManager.listFiles(storageDir);

	    if (archives != null) {
	        File base = new File(this.getBaseArchivePath());
	        String basePath = FileNameUtil.normalizePath(FileSystemManager.getAbsolutePath(base));

	        // List all potential archive files
	        for (int i=0; i<archives.length; i++) {
	            File archive = archives[i];
                String archivePath = FileNameUtil.normalizePath(FileSystemManager.getAbsolutePath(archive));
                
	            checkArchive(basePath, archivePath, archive);
	        }
	    }
	}
    
    protected boolean checkArchive(String basePath, String archivePath, File archive) {
        // Check wether the archive has been commited or not
        if (            
                archivePath.equals(basePath + TMP_ARCHIVE_SUFFIX)
                || (
                        archivePath.startsWith(basePath + Utils.FILE_DATE_SEPARATOR)
                        && archivePath.endsWith(TMP_ARCHIVE_SUFFIX)
                )
        ) {
            // If it has not been commited - destroy it            
            destroyTemporaryFile(archive);
            return false;
        } else {
            return true;
        }
    }
    
    protected void destroyTemporaryFile(File archive) {
        String name = FileSystemManager.isFile(archive) ? "file" : "directory";
        LogHelper.logFileInformations("CAUTION : Uncommited " + name + " detected : ", archive);

        try {
            this.deleteArchive(archive);
        } catch (Exception e) {
            Logger.defaultLogger().error(e);
        }

        Logger.defaultLogger().displayApplicationMessage(
                null, 
                "Temporary " + name + " detected.", 
                "Areca has detected that the following " + name + " is a temporary archive which has not been commited :" 
                + "\n" + FileSystemManager.getAbsolutePath(archive) 
                + "\n\nThis " + name + " has been deleted."
        );
    }
    
    /**
     * Stocke le fichier passé en argument dans l'archive
     * (indépendemment des filtres, ou politique de stockage; il s'agit là d'une
     * méthode purement utilitaire; en pratique : zip ou répertoire) 
     */
    protected abstract void storeFileInArchive(File file, String entryName, ProcessContext context) throws ApplicationException;
    
    public abstract File[] listArchives(GregorianCalendar fromDate, GregorianCalendar toDate);
    
    /**
     * Retourne la dernière archive précédant une date donnée
     */
    public abstract File getLastArchive(GregorianCalendar date) throws ApplicationException;
    
    
    /**
     * Builds the data directory associated to the archive file provided as argument. 
     */
    public File getDataDirectory(File archive) {
        return new File(
                FileSystemManager.getParentFile(archive),
                FileSystemManager.getName(archive) + DATA_DIRECTORY_SUFFIX
        );
    }
    
    /**
     * Retourne le status de l'entrée, dans l'archive spécifiée.
     * 
     * @param entry
     * @param archive
     * @return
     * @throws ApplicationException
     */
    protected abstract EntryArchiveData getArchiveData(FileSystemRecoveryEntry entry, File archive) throws ApplicationException;
    
	public EntryArchiveData[] getHistory(RecoveryEntry entry) throws ApplicationException {
		File[] archives = this.listArchives(null, null);
		FileSystemRecoveryEntry fEntry = (FileSystemRecoveryEntry)entry;
		ArrayList list = new ArrayList();
		
		for (int i=0; i<archives.length; i++) {
			list.add(getArchiveData(fEntry, archives[i]));
		}
		
		return processEntryArchiveData((EntryArchiveData[])list.toArray(new EntryArchiveData[0]));
	}
	
	/**
	 * On vide les caches lors de la fusion.
	 * <BR>A optimiser en tenant compte de la date.
	 */
	public void commitMerge(ProcessContext context) throws ApplicationException {
	    clearRelatedCaches();
	}

	public void doAfterDelete() {
	    clearRelatedCaches();
	}

	public void doBeforeDelete() {
	}
    
    public abstract boolean isCompressed();
    
    protected void clearRelatedCaches() {
		ArchiveManifestCache.getInstance().removeAllArchiveData(this);
		ArchiveTraceCache.getInstance().removeAllArchiveData(this); 
    }
    
    public FileSystemPolicy getFileSystemPolicy() {
        return fileSystemPolicy;
    }
    
    public void deleteArchives(GregorianCalendar fromDate, ProcessContext context) throws ApplicationException {
        this.checkRepository();
        
        Logger.defaultLogger().info(
                "Starting deletion from " + Utils.formatDisplayDate(fromDate) + "."
        );
        
        if (fromDate != null) {
            fromDate = (GregorianCalendar)fromDate.clone();
            fromDate.add(GregorianCalendar.MILLISECOND, -1);
        }
        
        File[] archives = this.listArchives(fromDate, null);
        
        for (int i=0; i<archives.length; i++) {
            try {
                context.getInfoChannel().updateCurrentTask(i+1, archives.length, FileSystemManager.getName(archives[i]));
                deleteArchive(archives[i]);
                context.getTaskMonitor().getCurrentActiveSubTask().setCurrentCompletion(i+1, archives.length + 1);
            } catch (Exception e) {
                throw new ApplicationException(e);
            }
        }
        
        try {
            FileSystemManager.getInstance().flush(FileSystemManager.getParentFile(new File(this.getBaseArchivePath())));
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
        
        Logger.defaultLogger().info(
                "Deletion completed - " + archives.length + " archive" + (archives.length>1?"s":"") + " deleted."
        );
        
        context.getTaskMonitor().getCurrentActiveSubTask().enforceCompletion();          
    }
    
    protected abstract void deleteArchive(File archive) throws IOException;
    
	public Set getEntries(GregorianCalendar date) throws ApplicationException {
	    return getEntries(getLastArchive(date));
	}
    
    /**
     * Retourne le contenu (sous forme de RecoveryEntries) de l'archive 
     */
	public abstract Set getEntries(File archive) throws ApplicationException;
	
    public boolean isPreBackupCheckUseful() {
        return true;
    }

    public EncryptionPolicy getEncryptionPolicy() {
        return encryptionPolicy;
    }
    
    /**
     * Creates a copy of the target's XML configuration and stores it in the main backup directory.
     * <BR>This copy can be used later - in case of computer crash. 
     */
    protected void storeTargetConfigBackup(ProcessContext context) throws ApplicationException {
        File storageDir = FileSystemManager.getParentFile(context.getFinalArchiveFile());
        boolean ok = false;
        
        if (storageDir != null && FileSystemManager.exists(storageDir)) {
            File rootDir = FileSystemManager.getParentFile(storageDir);
            if (rootDir != null && FileSystemManager.exists(rootDir)) {
                File targetFile = new File(
                        rootDir,
                        TARGET_BACKUP_FILE_PREFIX + this.target.getUid() + TARGET_BACKUP_FILE_SUFFIX
                );

                Logger.defaultLogger().info("Creating a XML backup copy of target \"" + this.target.getTargetName() + "\" on : " + FileSystemManager.getAbsolutePath(targetFile));
                RecoveryProcess process = new RecoveryProcess(targetFile);
                process.addTarget(this.target);
                process.setComments("This group contains a backup copy of your target : \"" + this.target.getTargetName() + "\". It can be used in case of computer crash.\nDo not modify it as is will be automatically updated during backup processes.");
                
                ProcessXMLWriter writer = new ProcessXMLWriter(true);
                writer.serializeProcess(process);
                
                ok = true;
            }
        }
        
        if (!ok) {
            Logger.defaultLogger().warn("Improper backup location : " + FileSystemManager.getAbsolutePath(context.getFinalArchiveFile()) + " - Could not create an XML configuration backup");
        }
    }
    
    public IndicatorMap computeIndicators() throws ApplicationException {
        IndicatorMap indicators = new IndicatorMap();
        File[] archives = this.listArchives(null, null);
        
        // NOA
        long noaValue = archives.length;
        Indicator noa = new Indicator();
        noa.setId(T_NOA);
        noa.setName(N_NOA);
        noa.setDescription(D_NOA);
        noa.setStringValue(Utils.formatLong(noaValue));
        noa.setValue(noaValue);
        indicators.addIndicator(noa);
        
        // APS
        long apsValue = 0;
        for (int i=0; i<archives.length; i++) {
            try {
                apsValue += AbstractFileSystemMedium.tool.getSize(archives[i]);
            } catch (FileNotFoundException shallNotHappen) {
                Logger.defaultLogger().error(shallNotHappen); // Ignored : shall not happen --> but logged anyway ...
            }
        }

        Indicator aps = new Indicator();
        aps.setId(T_APS);
        aps.setName(N_APS);
        aps.setDescription(D_APS);
        aps.setStringValue(Utils.formatFileSize(apsValue));
        aps.setValue(apsValue);   
        indicators.addIndicator(aps);
        
        // SFS
        File archive = this.getLastArchive(null);
        if (archive != null) {

            Set entries = this.getEntries(archive);
            Iterator iter = entries.iterator();
            RecoveryEntry entry;
            long sfsValue = 0;
            while (iter.hasNext()) {
                entry = (RecoveryEntry)iter.next();
                sfsValue += entry.getSize();
            }
            
            Indicator sfs = new Indicator();
            sfs.setId(T_SFS);
            sfs.setName(N_SFS);
            sfs.setDescription(D_SFS);
            sfs.setStringValue(Utils.formatFileSize(sfsValue));
            sfs.setValue(sfsValue);   
            indicators.addIndicator(sfs);
            
            // NOF
            long nofValue = entries.size();
            Indicator nof = new Indicator();
            nof.setId(T_NOF);
            nof.setName(N_NOF);
            nof.setDescription(D_NOF);
            nof.setStringValue(Utils.formatLong(nofValue));
            nof.setValue(nofValue);   
            indicators.addIndicator(nof);
            
            // ALS
            long alsValue = 0;
            for (int i=0; i< archives.length; i++) {
                archive = archives[i];
                
                entries = this.getEntries(archive);
                iter = entries.iterator();

                while (iter.hasNext()) {
                    entry = (RecoveryEntry)iter.next();
                    
                    if (entry.getStatus() == RecoveryEntry.STATUS_STORED) {
                        alsValue += entry.getSize();
                    }
                }
            }

            Indicator als = new Indicator();
            als.setId(T_ALS);
            als.setName(N_ALS);
            als.setDescription(D_ALS);
            als.setStringValue(Utils.formatFileSize(alsValue));
            als.setValue(alsValue);   
            indicators.addIndicator(als);
            
            // PSR            
            double psrValue;
            String psrStr;
            
            if (alsValue == 0) {
                psrValue = -1;
                psrStr = "N/A";
            } else {
                psrValue = 100 * (double)apsValue / (double)alsValue;
                psrStr = Utils.formatLong((long)psrValue) + " %";
            }
            
            Indicator psr = new Indicator();
            psr.setId(T_PSR);
            psr.setName(N_PSR);
            psr.setDescription(D_PSR);
            psr.setStringValue(psrStr);
            psr.setValue(psrValue);   
            indicators.addIndicator(psr);
            
            // SWH
            long swhValue = (long)(psrValue * (double)sfsValue / 100.);
            Indicator swh = new Indicator();
            swh.setId(T_SWH);
            swh.setName(N_SWH);
            swh.setDescription(D_SWH);
            swh.setStringValue(Utils.formatFileSize(swhValue));
            swh.setValue(swhValue);   
            indicators.addIndicator(swh);
            
            // SRR
            double srrValue;
            String srrStr;
            
            if (sfsValue == 0) {
                srrValue = -1;
                srrStr = "N/A";
            } else {
                srrValue = ((double)alsValue / (double)sfsValue - 1) * 100.;
                srrStr = Utils.formatLong((long)srrValue) + " %";
            }
            
            Indicator srr = new Indicator();
            srr.setId(T_SRR);
            srr.setName(N_SRR);
            srr.setDescription(D_SRR);
            srr.setStringValue(srrStr);
            srr.setValue(srrValue);   
            indicators.addIndicator(srr);
            
            // SOH
            long sohValue = apsValue - swhValue;
            Indicator soh = new Indicator();
            soh.setId(T_SOH);
            soh.setName(N_SOH);
            soh.setDescription(D_SOH);
            soh.setStringValue(Utils.formatFileSize(sohValue));
            soh.setValue(sohValue);   
            indicators.addIndicator(soh);
        }
        
        return indicators;
    }

    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("FileSystemPolicy", this.fileSystemPolicy, sb);
        ToStringHelper.append("EncryptionPolicy", this.encryptionPolicy, sb);
        return ToStringHelper.close(sb);
    }
}
