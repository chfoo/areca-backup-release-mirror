package com.application.areca.processor;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ApplicationException;
import com.application.areca.cache.ArchiveManifestCache;
import com.application.areca.context.ProcessContext;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.metadata.data.MetaData;
import com.application.areca.metadata.data.MetaDataManager;
import com.application.areca.metadata.manifest.Manifest;
import com.myJava.file.FileSystemManager;
import com.myJava.object.DuplicateHelper;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.object.PublicClonable;
import com.myJava.util.log.Logger;

/**
 * NOT FINALIZED YET
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 6892146605129115786
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
public class StrategyProcessor extends AbstractProcessor {

    private static final String PROP_LAST_EXECUTIONS = "strategy.processor.last.executions";
    private static final String PROP_LAST_PARAMS = "strategy.processor.parameters";
    private static final String PROP_SEPARATOR = ";";
    private static final long ONE_DAY_MILLIS = 24 * 3600 * 1000;
    private int[] delays;

    /**
     * @param target
     */
    public StrategyProcessor() {
        super();
    }

    public int[] getDelays() {
        return delays;
    }

    public void setDelays(int[] delays) {
        this.delays = delays;
    }

    /*
     * For instance, "[1; 7; 4; 3] means {1;1;1;1;1;1;1;7;7;7;28;28;+infinity}
     * ie 7 archives of 1 day; 3 archives of 1 week (7 days); 2 archives of 1 month (4 weeks - 28 days) and 1 remaining archive.
     */
    public void runImpl(ProcessContext context) throws ApplicationException {
        if (delays == null || delays.length == 0) {
            Logger.defaultLogger().info("Invalid parameters - the strategy will not be applied.");
            return;
        } else {
            AbstractRecoveryTarget target = context.getReport().getTarget();
            AbstractIncrementalFileSystemMedium medium = (AbstractIncrementalFileSystemMedium)target.getMedium();
            
            // Last archive date
            File lastArchive = medium.getLastArchive(null);
            Manifest mf = ArchiveManifestCache.getInstance().getManifest(medium, lastArchive);
            MetaData metaData = null;
            try {
                metaData = MetaDataManager.getMetaDataForArchive(medium, lastArchive);
            } catch (IOException e) {
                Logger.defaultLogger().error("Error reading MetaData for archive : " + FileSystemManager.getAbsolutePath(lastArchive), e);
            }
            
            if (mf != null && metaData != null) {
                GregorianCalendar lastArchiveDate = mf.getDate();
                long lastArchiveTimeMillis = lastArchiveDate.getTimeInMillis();
                long now = System.currentTimeMillis();
                
                // Last archive delay (in days)
                int lastArchiveDelay = (int)((now - lastArchiveTimeMillis) / ONE_DAY_MILLIS);
                int[] lastExecutions = parseIntArray(metaData.getDynamicProperty(PROP_LAST_EXECUTIONS));
                int[] lastParams = parseIntArray(metaData.getDynamicProperty(PROP_LAST_PARAMS));
                if (! EqualsHelper.equals(lastParams, this.delays)) {
                    lastExecutions = new int[this.delays.length];
                    for (int i=0; i<lastExecutions.length; i++) {
                        lastExecutions[i] = Integer.MAX_VALUE - lastArchiveDelay;
                    }
                }
                
                // Process
                int unit = 1;
                for (int delayTypeIndex = 0; delayTypeIndex<delays.length - 1; delayTypeIndex++) {
                    context.getInfoChannel().getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.98 / delays.length, "strategy index : " + delayTypeIndex);
                    unit *= delays[delayTypeIndex];
                    int repetition = delays[delayTypeIndex + 1];
                    if (delayTypeIndex != 0) {
                        repetition--;
                    }

                    if (lastExecutions[delayTypeIndex] + lastArchiveDelay < unit) {
                        Logger.defaultLogger().fine("Last merge too recent - ignoring.");
                        lastExecutions[delayTypeIndex] += lastArchiveDelay;
                        break;
                    } else {
                        lastExecutions[delayTypeIndex] = 0;
                        Logger.defaultLogger().info("Merge strategy : " + repetition + " archives of " + unit + " days.");
                        for (int repIndex = 0; repIndex<repetition; repIndex++) {
                            context.getInfoChannel().getTaskMonitor().getCurrentActiveSubTask().addNewSubTask(0.98 / repetition, "strategy iteration : " + repIndex);
                            if (unit > 1) {
                                int fromDelay = (repIndex + 1 ) * unit;
                                int toDelay = fromDelay + unit;
                                
                                // Merge !
                                target.getProcess().processCompactOnTargetImpl(target, fromDelay, toDelay, false, new ProcessContext(target, context.getInfoChannel()));
                            }
                        }
                        context.getInfoChannel().getTaskMonitor().getCurrentActiveSubTask().enforceCompletion();
                    }
                }
                context.getInfoChannel().getTaskMonitor().getCurrentActiveSubTask().enforceCompletion();
                context.getOverridenDynamicProperties().setProperty(PROP_LAST_EXECUTIONS, encodeStringArray(lastExecutions));
            } else {
                Logger.defaultLogger().info("No archives found - the strategy will not be applied.");
            }
        }
    }
    
    private static String encodeStringArray(int[] array) {
        String ret = "";
        for (int i=0; i<array.length; i++) {
            if (i != 0) {
                ret += PROP_SEPARATOR;
            }
            ret += array[i];
        }
        
        return ret;
    }
    
    private static int[] parseIntArray(String str) {
        StringTokenizer stt = new StringTokenizer(str, PROP_SEPARATOR);
        int[] delays = new int[stt.countTokens()];
        for (int i=0; stt.hasMoreTokens(); i++) {
            delays[i] = Integer.parseInt(stt.nextToken());
        }
        return delays;
    }
    
    public boolean requiresFilteredEntriesListing() {
        return false;
    }
    
    public String getParametersSummary() {
        if (delays == null) {
            return "[]";
        } else {
            String ret = "[";
            for (int i=0; i<delays.length; i++) {
                if (i != 0) {
                    ret += "; ";
                }
                ret += delays[i];
            }
            return ret + "]";
        }
    }
    
    public PublicClonable duplicate() {
        StrategyProcessor pro = new StrategyProcessor();
        pro.delays = DuplicateHelper.duplicate(delays);
        return pro;
    }

    public void validate() throws ProcessorValidationException {
        if (delays == null) {
            throw new ProcessorValidationException("The management parameters must be set.");
        }
    }
    
    public boolean equals(Object obj) {
        if (obj == null || (! (obj instanceof StrategyProcessor)) ) {
            return false;
        } else {
            StrategyProcessor other = (StrategyProcessor)obj;
            return 
                EqualsHelper.equals(this.delays, other.delays);
        }
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, this.delays);
        return h;
    }
}
