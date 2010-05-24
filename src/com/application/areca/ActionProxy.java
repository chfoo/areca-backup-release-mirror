package com.application.areca;

import java.util.GregorianCalendar;

import com.application.areca.context.ProcessContext;
import com.application.areca.indicator.IndicatorMap;
import com.application.areca.metadata.manifest.Manifest;
import com.myJava.util.log.Logger;

/**
 * 
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2010, Olivier PETRUCCI.

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
public class ActionProxy {
	   
    /**
     * Launch a backup on a target
     */
    public static void processBackupOnTarget(
    		AbstractTarget target, 
    		Manifest manifest, 
    		String backupScheme,
    		boolean disablePreCheck,
    		CheckParameters checkParams,
    		ProcessContext context
    ) throws ApplicationException {
        try {
            Logger.defaultLogger().info("Starting backup on " + target.getName() + " (" + target.getUid() + "). Backup scheme = " + backupScheme);         
            target.processBackup(manifest, backupScheme, disablePreCheck, checkParams, context);
        } finally {
            Logger.defaultLogger().info("Backup completed on " + target.getName() + " (" + target.getUid() + ")");
        }
    }
    
    /**
     * Launch a simulation on a target
     */
    public static SimulationResult processSimulateOnTarget(AbstractTarget target, ProcessContext context) throws ApplicationException {
        return target.processSimulate(context);
    }
    
    /**
     * Compute indicators for a target
     */
    public static IndicatorMap processIndicatorsOnTarget(AbstractTarget target, ProcessContext context) throws ApplicationException {
        return target.computeIndicators(context);
    }
    
    /**
     * Launch a check on a target
     */
    public static void processCheckOnTarget(
    		AbstractTarget target, 
    		CheckParameters checkParams,
    		GregorianCalendar date, 
    		ProcessContext context
    ) throws ApplicationException {
        target.processArchiveCheck(checkParams, date, context);
    }
    
    /**
     * Launch a recovery on a target
     */
    public static void processRecoverOnTarget(
    		AbstractTarget target, 
    		String[] filters, 
    		String path, 
    		GregorianCalendar date, 
    		boolean keepDeletedEntries,
    		boolean checkRecoveredEntries, 
    		ProcessContext context
    ) throws ApplicationException {
        target.processRecover(path, filters, date, keepDeletedEntries, checkRecoveredEntries, context);
    }
    
    public static void processRecoverOnTarget(
    		AbstractTarget target, 
    		String path, 
    		GregorianCalendar date, 
    		String entry, 
    		boolean checkRecoveredEntries, 
    		ProcessContext context
    ) throws ApplicationException {
        target.processRecover(path, date, entry, checkRecoveredEntries, context);
    }
    
    /**
     * Launch a merge on a target
     */
    public static void processMergeOnTarget(
    		AbstractTarget target, 
    		GregorianCalendar fromDate, 
    		GregorianCalendar toDate, 
    		Manifest manifest, 
    		MergeParameters params, 
    		ProcessContext context
    ) throws ApplicationException {  
        target.processMerge(fromDate, toDate, manifest, params, context);
    }  
    
    public static void processMergeOnTarget(
    		AbstractTarget target, 
    		int fromDelay, 
    		int toDelay, 
    		Manifest manifest, 
    		MergeParameters params, 
    		ProcessContext context
    ) throws ApplicationException {
 		processMergeOnTargetImpl(target, fromDelay, toDelay, manifest, params, context);
    }  
    
    public static void processMergeOnTargetImpl(
    		AbstractTarget target, 
    		int fromDelay, 
    		int toDelay, 
    		Manifest manifest, 
    		MergeParameters params, 
    		ProcessContext context
    ) throws ApplicationException { 
        target.processMerge(fromDelay, toDelay, manifest, params, context);
    }
    
    /**
     * Deletes archives for a target
     */
    public static void processDeleteOnTarget(AbstractTarget target, int delay, ProcessContext context) throws ApplicationException {
        processDeleteOnTargetImpl(target, delay, context);
    }  
    
    public static void processDeleteOnTargetImpl(AbstractTarget target, int delay, ProcessContext context) throws ApplicationException {  
        target.processDeleteArchives(delay, context);
    }  
    
    public static void processDeleteOnTarget(AbstractTarget target, GregorianCalendar fromDate, ProcessContext context) throws ApplicationException {   
        target.processDeleteArchives(fromDate, context);
    }  
}
