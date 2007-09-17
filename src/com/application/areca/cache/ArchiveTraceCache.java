package com.application.areca.cache;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import com.application.areca.ApplicationException;
import com.application.areca.impl.AbstractFileSystemMedium;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.metadata.trace.ArchiveTrace;
import com.application.areca.metadata.trace.ArchiveTraceManager;

/**
 * Cache contenant les traces des AbstractIncrementalFileSystemArchiveMedium.
 * (stockées sous forme de maps).
 * <BR>A noter que toute tentative d'appel pour d'autres implémentations d'ArchiveMediums
 * déclenchera une IllegalArgumentException.
 * <BR><BR>Classe singleton.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 3732974506771028333
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
public class ArchiveTraceCache extends AbstractArchiveDataCache {
    
    private static ArchiveTraceCache instance = new ArchiveTraceCache();
    
    /**
     * Retourne l'instance de cache
     */
    public static ArchiveTraceCache getInstance() {
        return instance;
    }
    
    private ArchiveTraceCache() {
        super(EvictionManager.getInstance());
    }    
    
    /**
     * Retourne la trace demandée sous forme de Map.
     * <BR>La trace est lue du medium si elle n'est pas trouvée en cache.
     */
    public synchronized ArchiveTrace getTrace(AbstractFileSystemMedium medium, File key) throws ApplicationException {
        if (medium == null || key == null) {
            return null;
        }
        
        AbstractIncrementalFileSystemMedium incrMedium = checkMediumInstance(medium);
        
        ArchiveTrace mp = (ArchiveTrace)this.get(incrMedium, key);
        if (mp == null) {
            try {
				mp = ArchiveTraceManager.readTraceForArchive(incrMedium, key, this.findOrCreateArchiveDataMap(medium));
			} catch (IOException e) {
				throw new ApplicationException(e);
			}
            this.put(incrMedium, key, mp, mp.getApproximateMemorySize());
        }
        //debugContent(incrMedium);
        
        return mp;
    }
    
    /**
     * Compares the object pool size and the global archive trace size.
     * <BR>Used for debugging purpose ... highlights the instance pooling effect. 
     */
    /*
    private void debugContent(AbstractIncrementalFileSystemMedium medium) {
        ArchiveDataMap map = this.findArchiveDataMap(medium);
        if (map != null) {
	        int nbPooled = map.getPoolSize();
	        System.out.println(medium.getDescription());
	        System.out.println("POOL SIZE : " + nbPooled);
	        
	        Iterator iter = map.keyIterator();
	        int total = 0;
	        int nb = 0;
	        while(iter.hasNext()) {
	            nb++;
	            File k = (File)iter.next();
	            ArchiveTrace tr = (ArchiveTrace)map.get(k);
	            total += tr.fileKeySet().size();
	        }
	        System.out.println("NR OF TRACES : " + nb);
	        System.out.println("NR OF ROWS : " + total);
	        
	        if (nb != 0) {
	            System.out.println("INDICATOR : " + (2* total / nb));
	        }
        }
    }
    */
    
    /**
     * Inits the data in cache.
     * <BR>Returns true if the cache still can grow (ie if no gc has been required after having added the data)
     */
    public synchronized boolean init(AbstractFileSystemMedium medium, File key) throws ApplicationException {
        if (medium == null || key == null) {
            return true;
        }
        
        AbstractIncrementalFileSystemMedium incrMedium = checkMediumInstance(medium);
        
        ArchiveTrace mp = (ArchiveTrace)this.get(incrMedium, key);
        if (mp == null) {
            try {
				mp = ArchiveTraceManager.readTraceForArchive(incrMedium, key, this.findOrCreateArchiveDataMap(medium));
			} catch (IOException e) {
				throw new ApplicationException(e);
			}
            return this.put(incrMedium, key, mp, mp.getApproximateMemorySize());
        } else {
            return true;
        }
    }
    
    public synchronized ArchiveTrace removeTrace(AbstractFileSystemMedium medium, File key) {
        return (ArchiveTrace)this.remove(medium, key);
    }
    
    /** 
     * Valide qu'on est bien en présence d'un AbstractIncrementalFileSystemArchiveMedium
     */ 
    private AbstractIncrementalFileSystemMedium checkMediumInstance(AbstractFileSystemMedium medium) {
        if (! (medium instanceof AbstractIncrementalFileSystemMedium)) {
            throw new IllegalArgumentException("Only instances of AbstractIncrementalFileSystemArchiveMedium are accepted.");
        }
        
        return (AbstractIncrementalFileSystemMedium)medium;
    }
}