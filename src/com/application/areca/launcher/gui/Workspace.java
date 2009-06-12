package com.application.areca.launcher.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import com.application.areca.ArecaFileConstants;
import com.application.areca.ArecaTechnicalConfiguration;
import com.application.areca.LogHelper;
import com.application.areca.ResourceManager;
import com.application.areca.TargetGroup;
import com.application.areca.adapters.AdapterException;
import com.application.areca.adapters.ProcessXMLReader;
import com.application.areca.launcher.gui.common.LocalPreferences;
import com.application.areca.version.VersionInfos;
import com.myJava.file.FileSystemManager;
import com.myJava.util.log.ConsoleLogProcessor;
import com.myJava.util.log.FileLogProcessor;
import com.myJava.util.log.Logger;

/**
 * <BR>This class implements a workspace.
 * <BR>A workspace is a collection of target groups, each group containing a collection of targets.
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
public class Workspace {

    protected String path;
    protected HashMap groups;
    protected HashMap xmlFiles;
    protected Application application;
    
    public Workspace(String path, Application application) throws AdapterException {
        this.application = application;
        this.path = FileSystemManager.getAbsolutePath(new File(path));
        this.groups = new HashMap(10);
        this.xmlFiles = new HashMap(10);        
        this.loadDirectory(this.path);
    }
    
    /**
     * Return the number of groups contained in the workspace
     */
    public int getGroupCount() {
        return this.groups.size();
    }
    
    public TargetGroup getGroupById(String source) {
        return (TargetGroup)groups.get(source);
    }
    
    public File getConfigFile(TargetGroup group) {
        return (File)this.xmlFiles.get(group);
    }
    
    public String toString() {
        return "Workspace : " + path;
    }
    
    public void removeGroup(String source) {
    	TargetGroup group = this.getGroupById(source);
    	if (group != null) {
    		group.doBeforeDelete();
    	}
        this.groups.remove(source);
    	if (group != null) {
    		group.doAfterDelete();
    	}
    }
    
    public void removeGroup(TargetGroup group) {
        this.removeGroup(group.getSource());
    }
    
    public Iterator getGroupIterator() {
        return this.groups.values().iterator();
    }    
    
    /**
     * Return an iterator. All target are sorted by ID
     */
    public Iterator getSortedGroupIterator() {
        String[] ids = new String[groups.size()];
        Iterator iter = getGroupIterator();
        int i = 0;
        while (iter.hasNext()) {
            ids[i++] = ((TargetGroup)iter.next()).getSource();
        }
        Arrays.sort(ids);
        
        ArrayList list = new ArrayList();
        for (i=0; i<ids.length; i++) {
            list.add(this.getGroupById(ids[i]));
        }
        return list.iterator();
    }    
    
    public String getPath() {
        return this.path;
    }
    
    public void addGroup(TargetGroup group) {
        this.groups.put(group.getSource(), group);
    }
    
    /**
     * Load the workspace denoted by the path passed as argument.
     * <BR>If xml files are found, they are parsed as a TargetGroup configuration
     */
    private void loadDirectory(String path) throws AdapterException {
	    try {
            File f = new File(path);
            if (FileSystemManager.exists(f)) {
                Logger.defaultLogger().remove(FileLogProcessor.class);
                Logger.defaultLogger().remove(ConsoleLogProcessor.class); // we don't want the default console processor that is set in the Logger class.
                FileLogProcessor proc;
                if (ArecaTechnicalConfiguration.get().getLogLocationOverride() == null) {
                	proc = new FileLogProcessor(new File(FileSystemManager.getAbsolutePath(f) + "/" + ArecaFileConstants.LOG_SUBDIRECTORY_NAME + "/", VersionInfos.APP_SHORT_NAME.toLowerCase()));
                } else {
                	proc = new FileLogProcessor(new File(ArecaTechnicalConfiguration.get().getLogLocationOverride(), VersionInfos.APP_SHORT_NAME.toLowerCase()));
                }
                Logger.defaultLogger().addProcessor(proc);

                LogHelper.logStartupInformations();
                LocalPreferences.instance().logProperties();
                
                File[] children = FileSystemManager.listFiles(f);
                
                for (int i=0; i<children.length; i++) {
                    if (FileSystemManager.isFile(children[i]) && FileSystemManager.getName(children[i]).toLowerCase().endsWith(".xml")) {
                        Logger.defaultLogger().info("Loading configuration file : " + FileSystemManager.getAbsolutePath(children[i]) + " ...");
                    	try {
	                        ProcessXMLReader adapter = new ProcessXMLReader(children[i]);
	                        adapter.setMissingDataListener(new MissingDataListener());
	                        TargetGroup group = adapter.load();
	                        this.addGroup(group);
	                        this.xmlFiles.put(group, children[i]);
                    	} catch (AdapterException e) {
                        	Logger.defaultLogger().error("Error detected in " + e.getSource());
                            Application.getInstance().handleException(
                                    ResourceManager.instance().getLabel("error.loadworkspace.message", new Object[] {e.getMessage(), e.getSource()}),
                                    e
                            );
                    	}
                    }
                } 
            }
            
            Logger.defaultLogger().info("Path : [" + path + "] - " + this.groups.size() + " groups loaded.");
        } catch (RuntimeException e) {
            Logger.defaultLogger().error(e);
            throw e;
        }
    }  
}
