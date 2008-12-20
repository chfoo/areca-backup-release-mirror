package com.application.areca.cache;

import java.io.File;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ApplicationException;
import com.application.areca.TargetGroup;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.launcher.gui.Workspace;
import com.myJava.util.log.Logger;

/**
 * Classe charg�e d'initialiser les caches en parcourant les archives :
 * <BR>- ArchiveTraceCache
 * <BR>- ArchiveManifestCache
 * <BR>- ZipReaderCache
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 8785459451506899793
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
public class CacheInitializer {
    
    /**
     * M�thode asynchrone de remplissage de cache pour un workspace donn�.
     */
    public static void populateCache(Workspace workspace) {
    	Logger.defaultLogger().info("Trace cache creation. (Workspace : " + workspace.getPath() + ")");
    	int currentPriority = Thread.currentThread().getPriority();
    	int cachePriority = (int)(1 * currentPriority);
    	
        Thread thr = new Thread(new CachePopulator(workspace));
        thr.setDaemon(true);
        thr.setPriority(cachePriority);
        thr.setName("Archive Trace Cache Populator");
        thr.start();
    }
    
    /**
     * Runner charg� du remplissage du cache
     */
    private static class CachePopulator implements Runnable {
        private Workspace workspace;
        
        // D�lai (en ms) avant que le remplissage du cache ne soit d�marr�. 
        private static final long POPULATION_START_DELAY = 3000;
        
        // D�lai entre le remplissage de deux targets
        private static final long POPULATION_TARGET_DELAY = 1000;        
        
        public CachePopulator(Workspace workspace) {
            this.workspace = workspace;
        }
        
        public void run() {
            try {
                Thread.sleep(POPULATION_START_DELAY);
            } catch (InterruptedException ignored) {
            }
            
            Iterator iter = this.workspace.getProcessIterator();
            try {
	            while(iter.hasNext()) {
	                if (! this.populateCache((TargetGroup)iter.next())) {
	                    Logger.defaultLogger().info("Memory threshold reached - Cache population stopped");
	                    break;
	                }
	            }
            } catch (ConcurrentModificationException e) {
                Logger.defaultLogger().warn("The process list has been modified. The cache population has thus been stopped; it will be restarted now.");
                run();
            }
        }
        
        /**
         * M�thode de remplissage de cache pour un process donn�.
         */
        private boolean populateCache(TargetGroup process) {
            Iterator iter = process.getTargetIterator();
            try {
                while(iter.hasNext()) {
                    AbstractRecoveryTarget target = (AbstractRecoveryTarget)iter.next();
                    if (! this.populateCache((AbstractIncrementalFileSystemMedium)target.getMedium())) {
                        return false;
                    }
                }
            } catch (ConcurrentModificationException e) {
                Logger.defaultLogger().warn("The process has been modified. The cache population has thus been stopped; it will be restarted now.");
                populateCache(process);
            }
            
            return true;
        }
        
        /**
         * Fills the cache associated to the Medium passed as argument.
         * <BR>Returns true if the caches still can grow (according to the GC policy)
         * <BR>Returns false otherwise.
         */    
        private boolean populateCache(AbstractIncrementalFileSystemMedium medium) {
            String task = "Trace cache creation. (Medium : " + medium.getTarget().getUid() + " - " + medium.getClass().getName() + ")";
            long time = System.currentTimeMillis();
            Logger.defaultLogger().info(task + " started.");
            
            File[] keys = medium.listArchives(null, null);
            
            for (int i=0; i<keys.length; i++) {
                try {                   
                    // Remplissage du ArchiveTraceCache et du ArchiveManifestCache.
                    if (! ArchiveTraceCache.getInstance().init(medium, keys[i])) {
                        Logger.defaultLogger().info(task + " stopped after " + (System.currentTimeMillis() - time) + " ms - not enough memory.");
                        return false;
                    }

					if (! ArchiveManifestCache.getInstance().init(medium, keys[i])) {
                        Logger.defaultLogger().info(task + " stopped after " + (System.currentTimeMillis() - time) + " ms - not enough memory.");
					    return false;
					}
				} catch (ApplicationException e) {
					Logger.defaultLogger().error("Error during cache creation.", e, "");
				}
            }
            
            Logger.defaultLogger().info(task + " completed in " + (System.currentTimeMillis() - time) + " ms.");
            
            try {
                Thread.sleep(POPULATION_TARGET_DELAY);
            } catch (InterruptedException ignored) {
            }
            
            return true;
        }
    }
}
