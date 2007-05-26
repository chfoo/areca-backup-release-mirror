package com.application.areca;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;

import com.application.areca.context.ProcessContext;
import com.application.areca.indicator.IndicatorMap;
import com.application.areca.metadata.manifest.Manifest;
import com.myJava.file.FileSystemManager;
import com.myJava.util.errors.ActionReport;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
 * Process de backup/recovery
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
public class RecoveryProcess 
implements TargetActions, Identifiable {
    
    private File source;
    private String comments;

    /**
     * Object used to monitor the current process
     */
    protected TaskMonitor taskMonitor;    
    
    /**
     * Liste des cibles à traiter
     */
    private HashMap targets;
    
    /**
     * Logger spécifique utilisé pour les retours utilisateur (typiquement : affichage à l'écran)
     */
    private UserInformationChannel infoChannel;
    
    /**
     * Constructeur
     * @param id ID du process
     */
    public RecoveryProcess(File source) {
        this.targets = new HashMap();
        this.source = source;
    }
    
    /**
     * Appelée avant suppression
     */
    public void doBeforeDelete() {
    	Iterator iter = this.getTargetIterator();
    	while (iter.hasNext()) {
    		AbstractRecoveryTarget tg = (AbstractRecoveryTarget)iter.next();
    		tg.doBeforeDelete();
    	}
    }
    
    /**
     *Appelée après suppression
     */
    public void doAfterDelete() {
    	Iterator iter = this.getTargetIterator();
    	while (iter.hasNext()) {
    		AbstractRecoveryTarget tg = (AbstractRecoveryTarget)iter.next();
    		tg.doAfterDelete();
    	}
    }
    
    /**
     * Itérateur sur les cibles, triées par Nom 
     */
    public Iterator getSortedTargetIterator() {
        AbstractRecoveryTarget[] tgs = new AbstractRecoveryTarget[targets.size()];
        Iterator iter = getTargetIterator();
        int i = 0;
        while (iter.hasNext()) {
            tgs[i++] = (AbstractRecoveryTarget)iter.next();
        }
        Arrays.sort(tgs, new TargetComparator());
        
        ArrayList list = new ArrayList();
        for (i=0; i<tgs.length; i++) {
            list.add(tgs[i]);
        }
        return list.iterator();
    }
    
    private static class TargetComparator implements Comparator {
        
        public int compare(Object o1, Object o2) {
            AbstractRecoveryTarget tg1 = (AbstractRecoveryTarget)o1;
            AbstractRecoveryTarget tg2 = (AbstractRecoveryTarget)o2;
            return tg1.getTargetName().toLowerCase().compareTo(tg2.getTargetName().toLowerCase());
        }
}
    
    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
    
    public String getName() {
        String fileName = FileSystemManager.getName(this.source);
        int index = fileName.indexOf('.');
        fileName = fileName.substring(0, index);
        String firstLetter = (""+fileName.charAt(0)).toUpperCase();
        
        return (firstLetter + fileName.substring(1)).replace('_', ' ');
    }
    
    public void addTarget(AbstractRecoveryTarget target) {
        this.targets.put(new Integer(target.getId()), target);
    }
    
    public AbstractRecoveryTarget getTargetById(int id) {
        return (AbstractRecoveryTarget)targets.get(new Integer(id));
    }
    
    public void removeTarget(AbstractRecoveryTarget target) {
        this.removeTarget(target.getId());
    }
    
    public void removeTarget(int id) {
    	AbstractRecoveryTarget tg = this.getTargetById(id);
    	if (tg != null) {
    		tg.doBeforeDelete();
    	}
        this.targets.remove(new Integer(id));
    	if (tg != null) {
    		tg.doAfterDelete();
    	}
    }
    
    public int getNextFreeTargetId() {
        if (this.targets.size() == 0) {
            return 1;
        } else {
            Iterator iter = this.getTargetIterator();            
            
            AbstractRecoveryTarget target = (AbstractRecoveryTarget)iter.next();
            int id = target.getId();

            while (iter.hasNext()) {
                target = (AbstractRecoveryTarget)iter.next();
                id = (int)Math.max(id, target.getId());
            }
            return id + 1;
        }
    }

    public TaskMonitor getTaskMonitor() {
        return taskMonitor;
    }
    
    public void setTaskMonitor(TaskMonitor taskMonitor) {
        this.taskMonitor = taskMonitor;
    }
    
    public int getTargetCount() {
        return this.targets.size();
    }
    
    public Iterator getTargetIterator() {
        return this.targets.values().iterator();
    }
    
    public String getSource() {
        return FileSystemManager.getName(this.source);
    }
    
    public File getSourceFile() {
        return source;
    }

    public String getUid() {
        return FileSystemManager.getAbsolutePath(getSourceFile());
    }

    /**
     * Lance le process de backup et traite itérativement chacune des targets.
     */
    public void launchBackup() throws ApplicationException {
        this.initProgress();
        double nb = this.getTargetCount();
        double targetShare = (nb == 0? 0.99 : 0.99/nb);
        Iterator iter = this.getTargetIterator();
        StringBuffer errors = new StringBuffer();
        boolean hasErrors = false;
        while (iter.hasNext()) {
            this.taskMonitor.resetCancellationState();
            AbstractRecoveryTarget target = (AbstractRecoveryTarget)iter.next();
            this.taskMonitor.addNewSubTask(targetShare);
            TaskMonitor mon = this.taskMonitor.getCurrentActiveSubTask();
            try {
                processBackupOnTargetImpl(target, null, new ProcessContext(target));
            } catch (Throwable e) {
                if (! TaskCancelledException.isTaskCancellation(e)) {
                    Logger.defaultLogger().error("An error occured during the processing of target " + target.getTargetName() + ".", e);
                    errors.append("\n").append(e.getMessage());
                    hasErrors = true;
                } else {
                    Logger.defaultLogger().info("Task cancelled : " + target.getTargetName());
                }

                mon.enforceCompletion();
            }
        }
        
        if (hasErrors) {
            throw new ApplicationException("Errors : " + errors.toString());
        }
        
        this.getTaskMonitor().setCurrentCompletion(1.0);
    }

    /**
     * Traite le backup d'une cible donnée.
     * 
     * @param target
     * @throws ApplicationException
     */
    public void processBackupOnTarget(AbstractRecoveryTarget target, ProcessContext context) throws ApplicationException {
        processBackupOnTarget(target, null, context);
    }
    
    /**
     * Traite le backup d'une cible donnée.
     * 
     * @param target
     * @throws ApplicationException
     */
    public void processBackupOnTarget(AbstractRecoveryTarget target, Manifest manifest, ProcessContext context) throws ApplicationException {
        initProgress();
        processBackupOnTargetImpl(target, manifest, context);
    }
    
    private void processBackupOnTargetImpl(AbstractRecoveryTarget target, Manifest manifest, ProcessContext context) throws ApplicationException {
        checkTarget(target, ACTION_BACKUP);
        
        try {
            Logger.defaultLogger().info("Target processing : [Backup] - [" + target.getFullName() + "] - [" + target.getId() + "]");         
            target.processBackup(manifest, context);
        } finally {
            Logger.defaultLogger().info("End of target processing : [Backup] - [" + target.getFullName() + "] - [" + target.getId() + "]");
        }
    }
    
    /**
     * @param target
     * @throws ApplicationException
     */
    public RecoveryEntry[] processSimulateOnTarget(AbstractRecoveryTarget target, ProcessContext context) throws ApplicationException {
        checkTarget(target, ACTION_BACKUP);
        this.initProgress();
        return target.processSimulate(context);
    }
    
    public IndicatorMap processIndicatorsOnTarget(AbstractRecoveryTarget target) throws ApplicationException {
        checkTarget(target, ACTION_INDICATORS); // Même contraintes que pour un backup
        this.initProgress();
        return target.computeIndicators();
    }
    
    public void processRecoverOnTarget(AbstractRecoveryTarget target, String[] filters, String path, GregorianCalendar date, boolean recoverDeletedEntries, ProcessContext context) throws ApplicationException {
        checkTarget(target, ACTION_RECOVER);
		this.initProgress();
        target.processRecover(path, filters, date, recoverDeletedEntries, context);
    }
    
    public void processRecoverOnTarget(AbstractRecoveryTarget target, String path, GregorianCalendar date, RecoveryEntry entry, ProcessContext context) throws ApplicationException {
        checkTarget(target, ACTION_RECOVER);
		this.initProgress();
        target.processRecover(path, date, entry, context);
    }
    
    public void processCompactOnTarget(AbstractRecoveryTarget target, GregorianCalendar fromDate, GregorianCalendar toDate, Manifest manifest, ProcessContext context) throws ApplicationException {
        checkTarget(target, ACTION_COMPACT_OR_DELETE);     
		this.initProgress();
        target.processCompact(fromDate, toDate, manifest, context);
    }  
    
    public void processCompactOnTarget(AbstractRecoveryTarget target, int delay, ProcessContext context) throws ApplicationException {
 		this.initProgress();
 		processCompactOnTargetImpl(target, delay, context);
    }  
    
    public void processCompactOnTargetImpl(AbstractRecoveryTarget target, int delay, ProcessContext context) throws ApplicationException {
        checkTarget(target, ACTION_COMPACT_OR_DELETE);    
        target.processCompact(delay, context);
    }
    
    public void processDeleteOnTarget(AbstractRecoveryTarget target, int delay, ProcessContext context) throws ApplicationException {
        checkTarget(target, ACTION_COMPACT_OR_DELETE);    
		this.initProgress();
        target.processDeleteArchives(delay, context);
    }  
    
    public void processDeleteOnTarget(AbstractRecoveryTarget target, GregorianCalendar fromDate, ProcessContext context) throws ApplicationException {
        checkTarget(target, ACTION_COMPACT_OR_DELETE);    
		this.initProgress();
        target.processDeleteArchives(fromDate, context);
    }  
    
    protected void checkTarget(AbstractRecoveryTarget target, int action) throws ApplicationException {
        if (target == null) {
            throw new ApplicationException("Invalid target : null");
        }

        ActionReport report = target.checkTargetState(action);
        if (! report.isDataValid()) {
            throw new ApplicationException(report);
        }        
    }
    
    public String toString() {
        return "Group : " + this.source;
    }
    
    /**
     * Retourne la description du process
     */
    public String getDescription() {

        StringBuffer buf = new StringBuffer();
        buf.append("Group #").append(FileSystemManager.getAbsolutePath(this.source));
        buf.append("\nTargets :");
        
        Iterator iter = this.getTargetIterator();
        while (iter.hasNext()) {
            AbstractRecoveryTarget target = (AbstractRecoveryTarget)iter.next();
            buf.append("\n-------------------------");            
            buf.append("\n");
            buf.append(target.getDescription());
        }
        buf.append("\n-------------------------");          
        
        return new String(buf);
    }
    
    private void initProgress() {
        this.taskMonitor = new TaskMonitor();
        this.taskMonitor.addListener(this.getInfoChannel());
        this.taskMonitor.setCurrentCompletion(0.0);
    }
    
    public UserInformationChannel getInfoChannel() {
        return infoChannel;
    }
    
    public void setInfoChannel(UserInformationChannel infoChannel) {
        this.infoChannel = infoChannel;
    }
}

