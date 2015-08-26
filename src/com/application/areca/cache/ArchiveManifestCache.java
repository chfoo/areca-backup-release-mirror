package com.application.areca.cache;

import java.io.File;

import com.application.areca.ApplicationException;
import com.application.areca.impl.AbstractFileSystemMedium;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.manifest.ManifestManager;

/**
 * Cache of Manifests
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
public class ArchiveManifestCache extends AbstractArchiveDataCache {
    
    private static ArchiveManifestCache instance = new ArchiveManifestCache();
    
    public static ArchiveManifestCache getInstance() {
        return instance;
    }
    
    private ArchiveManifestCache() {
        super(EvictionManager.getInstance());
    }
    
    /**
     * Returns the requested manifest
     */    
    public synchronized Manifest getManifest(AbstractFileSystemMedium medium, File key) throws ApplicationException {
        if (medium == null || key == null) {
            return null;
        }
        
        Manifest mf = (Manifest)this.get(medium, key);
        if (mf == null) {
            mf = ManifestManager.readManifestForArchive(medium, key);
            if (mf != null) {
                this.put(medium, key, mf, computeApproximateManifestSize(mf));
            }
        }
        
        return mf;
    }
    
    private long computeApproximateManifestSize(Manifest mf) {
        return 300 + 2*(mf.getDescription() == null ? 0 : mf.getDescription().length()) + 2*(mf.getTitle() == null ? 0 : mf.getTitle().length());
    }
    
    /**
     * Init the data in cache.
     * <BR>Returns true if the cache still can grow (ie if no gc has been required after having added the data)
     */
    public synchronized boolean init(AbstractFileSystemMedium medium, File key) throws ApplicationException {
        if (medium == null || key == null) {
            return true;
        }
        
        Manifest mf = (Manifest)this.get(medium, key);
        if (mf == null) {
            mf = ManifestManager.readManifestForArchive((AbstractIncrementalFileSystemMedium)medium, key);
            return this.put(medium, key, mf, computeApproximateManifestSize(mf));
        } else {
            return true;
        }
    }
    
    public synchronized Manifest removeManifest(AbstractFileSystemMedium medium, File key) {
        return (Manifest)this.remove(medium, key);
    } 
}