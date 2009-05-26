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
import com.myJava.util.log.Logger;

/**
 * Target group
 * <BR>
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
public class TargetGroup 
implements Identifiable {
    
    private File source;
    private String comments;
    
    /**
     * Group's content
     */
    private HashMap targets;
    
    public TargetGroup(File source) {
        this.targets = new HashMap();
        this.source = source;
    }

	public void doBeforeDelete() {
    	Iterator iter = this.getTargetIterator();
    	while (iter.hasNext()) {
    		AbstractRecoveryTarget tg = (AbstractRecoveryTarget)iter.next();
    		tg.doBeforeDelete();
    	}
    }

    public void doAfterDelete() {
    	Iterator iter = this.getTargetIterator();
    	while (iter.hasNext()) {
    		AbstractRecoveryTarget tg = (AbstractRecoveryTarget)iter.next();
    		tg.doAfterDelete();
    	}
    }
    
    /**
     * Returns an iterator on targets, sorted by name
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
                // Cas 1 : all ids are used (no hole)
                return id + 1;
            } else {
                // Cas 2 : there are holes -> return the first free id
                for (int i = 1; i<id; i++) {
                    if (this.getTargetById(i) == null) {
                        return i;
                    }
                }
                
                // Cas 3 : shall never happen
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
     * Launch a backup on a target
     */
    public void processBackupOnTarget(
    		AbstractRecoveryTarget target, 
    		Manifest manifest, 
    		String backupScheme,
    		boolean disablePreCheck,
    		boolean disableArchiveCheck,
    		ProcessContext context
    ) throws ApplicationException {
        initProgress(context);
        try {
            Logger.defaultLogger().info("Starting backup on " + target.getTargetName() + " (" + target.getUid() + "). Backup scheme = " + backupScheme);         
            target.processBackup(manifest, backupScheme, disablePreCheck, disableArchiveCheck, context);
        } finally {
            Logger.defaultLogger().info("Backup completed on " + target.getTargetName() + " (" + target.getUid() + ")");
        }
    }
    
    /**
     * Launch a simulation on a target
     */
    public SimulationResult processSimulateOnTarget(AbstractRecoveryTarget target, ProcessContext context) throws ApplicationException {
        this.initProgress(context);
        return target.processSimulate(context);
    }
    
    /**
     * Compute indicators for a target
     */
    public IndicatorMap processIndicatorsOnTarget(AbstractRecoveryTarget target, ProcessContext context) throws ApplicationException {
        this.initProgress(context);
        return target.computeIndicators();
    }
    
    /**
     * Launch a check on a target
     */
    public void processCheckOnTarget(
    		AbstractRecoveryTarget target, 
    		String destination,
    		boolean checkOnlyArchiveContent, 
    		GregorianCalendar date, 
    		ProcessContext context
    ) throws ApplicationException {
		this.initProgress(context);
        target.processArchiveCheck(destination, checkOnlyArchiveContent, date, context);
    }
    
    /**
     * Launch a recovery on a target
     */
    public void processRecoverOnTarget(
    		AbstractRecoveryTarget target, 
    		String[] filters, 
    		String path, 
    		GregorianCalendar date, 
    		boolean keepDeletedEntries,
    		boolean checkRecoveredEntries, 
    		ProcessContext context
    ) throws ApplicationException {
		this.initProgress(context);
        target.processRecover(path, filters, date, keepDeletedEntries, checkRecoveredEntries, context);
    }
    
    public void processRecoverOnTarget(
    		AbstractRecoveryTarget target, 
    		String path, 
    		GregorianCalendar date, 
    		String entry, 
    		boolean checkRecoveredEntries, 
    		ProcessContext context
    ) throws ApplicationException {
		this.initProgress(context);
        target.processRecover(path, date, entry, checkRecoveredEntries, context);
    }
    
    /**
     * Launch a merge on a target
     */
    public void processMergeOnTarget(AbstractRecoveryTarget target, GregorianCalendar fromDate, GregorianCalendar toDate, Manifest manifest, boolean keepDeletedEntries, ProcessContext context) throws ApplicationException {  
		this.initProgress(context);
        target.processMerge(fromDate, toDate, manifest, keepDeletedEntries, context);
    }  
    
    public void processMergeOnTarget(AbstractRecoveryTarget target, int fromDelay, int toDelay, Manifest manifest, boolean keepDeletedEntries, ProcessContext context) throws ApplicationException {
 		this.initProgress(context);
 		processMergeOnTargetImpl(target, fromDelay, toDelay, manifest, keepDeletedEntries, context);
    }  
    
    public void processMergeOnTargetImpl(AbstractRecoveryTarget target, int fromDelay, int toDelay, Manifest manifest, boolean keepDeletedEntries, ProcessContext context) throws ApplicationException { 
        target.processMerge(fromDelay, toDelay, manifest, keepDeletedEntries, context);
    }
    
    /**
     * Deletes archives for a target
     */
    public void processDeleteOnTarget(AbstractRecoveryTarget target, int delay, ProcessContext context) throws ApplicationException {
		this.initProgress(context);
        processDeleteOnTargetImpl(target, delay, context);
    }  
    
    public void processDeleteOnTargetImpl(AbstractRecoveryTarget target, int delay, ProcessContext context) throws ApplicationException {  
        target.processDeleteArchives(delay, context);
    }  
    
    public void processDeleteOnTarget(AbstractRecoveryTarget target, GregorianCalendar fromDate, ProcessContext context) throws ApplicationException {   
		this.initProgress(context);
        target.processDeleteArchives(fromDate, context);
    }  
    
    public String toString() {
        return "Group : " + this.source;
    }
    
    /**
     * Retourne la description du process
     */
    public String getDescription() {

        StringBuffer buf = new StringBuffer();
        buf.append("Description file : ").append(FileSystemManager.getAbsolutePath(this.source));
        buf.append("\nContent :");
        
        Iterator iter = this.getTargetIterator();
        while (iter.hasNext()) {
            AbstractRecoveryTarget target = (AbstractRecoveryTarget)iter.next();         
            buf.append("\n");
            buf.append(target.getDescription());
        }        
        
        return new String(buf);
    }
    
    private void initProgress(ProcessContext pc) {
    }
}

