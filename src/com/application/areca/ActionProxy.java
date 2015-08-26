package com.application.areca;

import java.util.GregorianCalendar;

import com.application.areca.context.ProcessContext;
import com.application.areca.impl.copypolicy.AbstractCopyPolicy;
import com.application.areca.indicator.IndicatorMap;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.transaction.TransactionHandler;
import com.application.areca.metadata.transaction.TransactionPoint;
import com.application.areca.version.VersionInfos;
import com.myJava.util.log.Logger;
import com.myJava.util.xml.AdapterException;

/**
 * 
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2015, Olivier PETRUCCI.

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
			CheckParameters checkParams,
			TransactionHandler transactionHandler,
			ProcessContext context
	) throws ApplicationException {
		try {
			LogHelper.logTarget(target);
			TransactionPoint transactionPoint = null;

			if (target.checkResumeSupported() == null && transactionHandler != null) {
				if (transactionHandler.shallSearchForPendingTransactions()) {
					// Look for pending transactions
					Logger.defaultLogger().info("Looking for pending transactions ...");
					try {
						transactionPoint = target.getLastTransactionPoint(backupScheme);
					} catch (ApplicationException ex) {
						Logger.defaultLogger().error("Error detected while trying to read transaction points.", ex);
					}
					
					// Check transaction point
					if (transactionPoint != null) {
						Logger.defaultLogger().info("Transaction point found : " + transactionPoint.displayedName());
						if (transactionHandler.shallHandleTransactionPoint(transactionPoint)) {
							String tpVersion;
							try {
								tpVersion = transactionPoint.readHeader().getArecaVersion();
								
								// Check whether Areca's version has changed since the transaction point
								if (VersionInfos.getLastVersion().getVersionId().equalsIgnoreCase(tpVersion)) {
									Logger.defaultLogger().info("Transaction point will be used.");
								} else {
									Logger.defaultLogger().warn("Transaction point will be used, but it seems that " + VersionInfos.APP_SHORT_NAME + " has been upgraded since this transaction point (transaction point version : " + tpVersion + "; current version : " + VersionInfos.getLastVersion().getVersionId() + "). If an error occur, please restart your backup from the beginning.");
								}
							} catch (AdapterException e) {
								Logger.defaultLogger().warn("Error reading transaction point attributes. It will be ignored.", e);
							}
						} else {
							transactionPoint = null;
							Logger.defaultLogger().info("Transaction point will be ignored.");
						}
					} else {
						Logger.defaultLogger().info("No transaction point found.");
					}
				}
			}

			Logger.defaultLogger().info("Starting backup on " + target.getName() + " (" + target.getUid() + "). Backup scheme = " + backupScheme);         
			target.processBackup(manifest, backupScheme, checkParams, transactionPoint, context);
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
		LogHelper.logTarget(target);
		target.processArchiveCheck(checkParams, date, null, true, context);
	}

	/**
	 * Launch a recovery on a target
	 */
	public static void processRecoverOnTarget(
			AbstractTarget target, 
			ArecaRawFileList filters, 
			AbstractCopyPolicy policy,
			String path, 
			boolean appendSuffix,
			GregorianCalendar date, 
			boolean keepDeletedEntries,
			boolean checkRecoveredEntries, 
			ProcessContext context
	) throws ApplicationException {
		LogHelper.logTarget(target);
		target.processRecover(path, appendSuffix, filters, policy, date, keepDeletedEntries, checkRecoveredEntries, context);
	}

	public static void processRecoverOnTarget(
			AbstractTarget target, 
			String path, 
			GregorianCalendar date, 
			String entry, 
			AbstractCopyPolicy policy,
			boolean checkRecoveredEntries, 
			ProcessContext context
	) throws ApplicationException {
		LogHelper.logTarget(target);
		target.processRecover(path, date, entry, policy, checkRecoveredEntries, context);
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
			CheckParameters checkParams,
			ProcessContext context
	) throws ApplicationException {  
		target.processMerge(fromDate, toDate, manifest, params, checkParams, true, context);
	}  

	public static void processMergeOnTarget(
			AbstractTarget target, 
			int fromDelay, 
			int toDelay, 
			Manifest manifest, 
			MergeParameters params, 
			CheckParameters checkParams,
			ProcessContext context
	) throws ApplicationException {
		LogHelper.logTarget(target);
		processMergeOnTargetImpl(target, fromDelay, toDelay, manifest, params, checkParams, true, context);
	}  

	public static void processMergeOnTargetImpl(
			AbstractTarget target, 
			int fromDelay, 
			int toDelay, 
			Manifest manifest, 
			MergeParameters params, 
			CheckParameters checkParams,
			boolean runProcessors,
			ProcessContext context
	) throws ApplicationException { 
		LogHelper.logTarget(target);
		target.processMerge(fromDelay, toDelay, manifest, params, checkParams, runProcessors, context);
	}

	/**
	 * Deletes archives for a target
	 */
	public static void processDeleteOnTarget(AbstractTarget target, int delay, ProcessContext context) throws ApplicationException {
		LogHelper.logTarget(target);
		target.processDeleteArchives(delay, context);
	}  

	public static void processDeleteOnTarget(AbstractTarget target, GregorianCalendar fromDate, ProcessContext context) throws ApplicationException {   
		LogHelper.logTarget(target);
		target.processDeleteArchives(fromDate, context);
	}  
}
