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

/**
 * Process de backup/recovery
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
public class RecoveryProcess 
implements TargetActions, Identifiable {
    
    private File source;
    private String comments;
    
    /**
     * Liste des cibles à traiter
     */
    private HashMap targets;
    
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
    
    public boolean isRunning() {
        Iterator iter = this.getTargetIterator();            
        while (iter.hasNext()) {
            AbstractRecoveryTarget target = (AbstractRecoveryTarget)iter.next();
            if (target.isRunning()) {
                return true;
            }
        }
        
        return false;
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
            
            if (id == this.targets.size()) {
                // Cas 1 : tous les ids sont utilisés (pas de trou)
                return id + 1;
            } else {
                // Cas 2 : il y a des trous -> on retourne le premier id libre
                for (int i = 1; i<id; i++) {
                    if (this.getTargetById(i) == null) {
                        return i;
                    }
                }
                
                // Cas 3 : ne doit jamais arriver
                return id+1;
            }
        }
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
        initProgress(context);
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
        checkTarget(target, ACTION_SIMULATE);
        this.initProgress(context);
        return target.processSimulate(context);
    }
    
    public IndicatorMap processIndicatorsOnTarget(AbstractRecoveryTarget target, ProcessContext context) throws ApplicationException {
        checkTarget(target, ACTION_INDICATORS); // Même contraintes que pour un backup
        this.initProgress(context);
        return target.computeIndicators();
    }
    
    public void processRecoverOnTarget(AbstractRecoveryTarget target, String[] filters, String path, GregorianCalendar date, boolean recoverDeletedEntries, ProcessContext context) throws ApplicationException {
        checkTarget(target, ACTION_RECOVER);
		this.initProgress(context);
        target.processRecover(path, filters, date, recoverDeletedEntries, context);
    }
    
    public void processRecoverOnTarget(AbstractRecoveryTarget target, String path, GregorianCalendar date, RecoveryEntry entry, ProcessContext context) throws ApplicationException {
        checkTarget(target, ACTION_RECOVER);
		this.initProgress(context);
        target.processRecover(path, date, entry, context);
    }
    
    public void processCompactOnTarget(AbstractRecoveryTarget target, GregorianCalendar fromDate, GregorianCalendar toDate, boolean keepDeletedEntries, Manifest manifest, ProcessContext context) throws ApplicationException {
        checkTarget(target, ACTION_COMPACT_OR_DELETE);     
		this.initProgress(context);
        target.processMerge(fromDate, toDate, keepDeletedEntries, manifest, context);
    }  
    
    public void processCompactOnTarget(AbstractRecoveryTarget target, int fromDelay, int toDelay, boolean keepDeletedEntries, ProcessContext context) throws ApplicationException {
 		this.initProgress(context);
 		processCompactOnTargetImpl(target, fromDelay, toDelay, keepDeletedEntries, context);
    }  
    
    public void processCompactOnTargetImpl(AbstractRecoveryTarget target, int fromDelay, int toDelay, boolean keepDeletedEntries, ProcessContext context) throws ApplicationException {
        checkTarget(target, ACTION_COMPACT_OR_DELETE);    
        target.processMerge(fromDelay, toDelay, keepDeletedEntries, context);
    }
    
    public void processDeleteOnTarget(AbstractRecoveryTarget target, int delay, ProcessContext context) throws ApplicationException {
		this.initProgress(context);
        processDeleteOnTargetImpl(target, delay, context);
    }  
    
    public void processDeleteOnTargetImpl(AbstractRecoveryTarget target, int delay, ProcessContext context) throws ApplicationException {
        checkTarget(target, ACTION_COMPACT_OR_DELETE);    
        target.processDeleteArchives(delay, context);
    }  
    
    public void processDeleteOnTarget(AbstractRecoveryTarget target, GregorianCalendar fromDate, ProcessContext context) throws ApplicationException {
        checkTarget(target, ACTION_COMPACT_OR_DELETE);    
		this.initProgress(context);
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
    
    private void initProgress(ProcessContext pc) {
    }
}

