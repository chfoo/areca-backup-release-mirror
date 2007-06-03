package com.application.areca.launcher.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ArchiveFilter;
import com.application.areca.ArecaTechnicalConfiguration;
import com.application.areca.LogHelper;
import com.application.areca.RecoveryProcess;
import com.application.areca.adapters.AdapterException;
import com.application.areca.adapters.ProcessXMLReader;
import com.application.areca.cache.CacheInitializer;
import com.application.areca.launcher.gui.common.LocalPreferences;
import com.application.areca.postprocess.PostProcessor;
import com.application.areca.version.VersionInfos;
import com.myJava.file.FileSystemManager;
import com.myJava.util.log.FileLogProcessor;
import com.myJava.util.log.Logger;

/**
 * <BR>Classe implémentant un workspace
 * <BR>Un workspace référence un ensemble de processus (un par fichier de configuration)
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2162742295696737000
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
public class Workspace {

    protected String path;
    protected HashMap processes;
    protected HashMap xmlFiles;
    protected Application application;
    
    public Workspace(String path, Application application) throws AdapterException {
        this.application = application;
        this.path = FileSystemManager.getAbsolutePath(new File(path));
        this.processes = new HashMap(10);
        this.xmlFiles = new HashMap(10);        
        this.loadDirectory(this.path);
    }
    
    public int getProcessCount() {
        return this.processes.size();
    }
    
    public RecoveryProcess getProcessById(String source) {
        return (RecoveryProcess)processes.get(source);
    }
    
    public File getConfigFile(RecoveryProcess process) {
        return (File)this.xmlFiles.get(process);
    }
    
    public String toString() {
        return "Workspace : " + path;
    }
    
    public void removeProcess(String source) {
    	RecoveryProcess process = this.getProcessById(source);
    	if (process != null) {
    		process.doBeforeDelete();
    	}
        this.processes.remove(source);
    	if (process != null) {
    		process.doAfterDelete();
    	}
    }
    
    public void removeProcess(RecoveryProcess process) {
        this.removeProcess(process.getSource());
    }
    
    public Iterator getProcessIterator() {
        return this.processes.values().iterator();
    }    
    
    /**
     * Itérateur sur les process, triés par ID 
     */
    public Iterator getSortedProcessIterator() {

        String[] ids = new String[processes.size()];
        Iterator iter = getProcessIterator();
        int i = 0;
        while (iter.hasNext()) {
            ids[i++] = ((RecoveryProcess)iter.next()).getSource();
        }
        Arrays.sort(ids);
        
        ArrayList list = new ArrayList();
        for (i=0; i<ids.length; i++) {
            list.add(this.getProcessById(ids[i]));
        }
        return list.iterator();
    }    
    
    public String getPath() {
        return this.path;
    }
    
    public void addProcess(RecoveryProcess process) {
        this.processes.put(process.getSource(), process);
    }
    
    private void loadDirectory(String path) throws AdapterException {
	    File f = new File(path);
	    if (FileSystemManager.exists(f)) {
	        Logger.defaultLogger().removeAllProcessors();
	        FileLogProcessor proc = new FileLogProcessor(new File(FileSystemManager.getAbsolutePath(f) + "/log/", VersionInfos.APP_NAME.toLowerCase()));
	        Logger.defaultLogger().addProcessor(proc);

            LogHelper.logStartupInformations();
            LocalPreferences.instance().logProperties();
            
		    File[] children = FileSystemManager.listFiles(f);
		    
		    for (int i=0; i<children.length; i++) {
		        if (FileSystemManager.isFile(children[i]) && FileSystemManager.getName(children[i]).toLowerCase().endsWith(".xml")) {
	                ProcessXMLReader adapter = new ProcessXMLReader(children[i]);
	                adapter.setMissingDataListener(new MissingDataListener());
	                RecoveryProcess process = adapter.load();
	                this.addProcess(process);
	                this.xmlFiles.put(process, children[i]);
		        }
		    } 
	    }
	    
	    Logger.defaultLogger().info("Path : [" + path + "] - " + this.processes.size() + " groups loaded.");
	    
	    // Une fois que le workspace est chargé, on lance la tâche de remplissage de cache
	    if (ArecaTechnicalConfiguration.get().isCachePreload()) {
	        CacheInitializer.populateCache(this);
	    }
    }  
    
    public ArchiveFilter[] buildFilterArray() {
        Iterator iter = this.getProcessIterator();
        HashSet set = new HashSet();
        while (iter.hasNext()) {
            RecoveryProcess process = (RecoveryProcess)iter.next();
            Iterator tgIter = process.getTargetIterator();
            while (tgIter.hasNext()) {
                AbstractRecoveryTarget tg = (AbstractRecoveryTarget)tgIter.next();
                Iterator fIter = tg.getFilterIterator();
                while (fIter.hasNext()) {
                    set.add(fIter.next());
                }
            }
        }
        ArchiveFilter[] ret = (ArchiveFilter[])set.toArray(new ArchiveFilter[0]);
        Arrays.sort(ret);
        return ret;
    }
    
    public PostProcessor[] buildPostProcessorArray() {
        Iterator iter = this.getProcessIterator();
        HashSet set = new HashSet();
        while (iter.hasNext()) {
            RecoveryProcess process = (RecoveryProcess)iter.next();
            Iterator tgIter = process.getTargetIterator();
            while (tgIter.hasNext()) {
                AbstractRecoveryTarget tg = (AbstractRecoveryTarget)tgIter.next();
                Iterator pIter = tg.getPostProcessors().iterator();
                while (pIter.hasNext()) {
                    set.add(pIter.next());
                }
            }
        }
        PostProcessor[] ret = (PostProcessor[])set.toArray(new PostProcessor[0]);
        Arrays.sort(ret);
        return ret;
    }
}
