package com.application.areca.plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import com.application.areca.ApplicationException;
import com.application.areca.ArecaConfiguration;
import com.application.areca.ArecaFileConstants;
import com.application.areca.Utils;
import com.myJava.file.FileSystemManager;
import com.myJava.util.log.Logger;

/**
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
public class PluginRegistry implements ArecaFileConstants {
    public static final String KEY_JAR_PATH = "plugin.jar.file";
    public static final String KEY_CLASS = "plugin.class";
    public static final String SEPARATOR = ";";  
    private static final String PLUGIN_DIRECTORY = ArecaConfiguration.get().getPluginsLocationOverride();

    private static PluginRegistry instance = new PluginRegistry();

    private Map plugins = new HashMap();

    public static PluginRegistry getInstance() {
        return instance;
    }

    private PluginRegistry() {
    	File pluginDirectory;
    	if (PLUGIN_DIRECTORY == null) {
    		pluginDirectory = new File(Utils.getApplicationRoot(), DEFAULT_PLUGIN_SUBDIRECTORY_NAME);
    	} else {
    		pluginDirectory = new File(PLUGIN_DIRECTORY);
    	}
    	load(pluginDirectory);

        // Add default plugins : HD, FTP
        register(new DefaultStoragePlugin());
        register(new FTPStoragePlugin());
        register(new SFTPStoragePlugin());
    }

    public void load(File pluginsDirectory) {
        Logger.defaultLogger().info("Looking for plugins in directory : " + FileSystemManager.getDisplayPath(pluginsDirectory));
        String[] pluginFileNames = FileSystemManager.list(pluginsDirectory);
        if (pluginFileNames != null) {
            for (int i=0; i<pluginFileNames.length; i++) {
                File pluginDirectory = new File(pluginsDirectory, pluginFileNames[i]);
                if (FileSystemManager.isDirectory(pluginDirectory)) {
                    Logger.defaultLogger().info("Attempting to load plugin directory : " + FileSystemManager.getDisplayPath(pluginDirectory));
                    File configFile = new File(pluginDirectory, FileSystemManager.getName(pluginDirectory) + ".properties");
                    Plugin plugin = null;
                    try {
                        plugin = instanciate(configFile);
                    } catch (ApplicationException e) {
                        Logger.defaultLogger().error("Error during plugin initialization.", e);
                    }
                    if (plugin != null) {
                        register(plugin);
                        Logger.defaultLogger().info("Plugin directory successfully loaded.");
                    } else {
                        Logger.defaultLogger().warn("Inconsistent plugin or not a plugin directory.");                
                    }
                } else {
                    Logger.defaultLogger().error("Invalid plugin data : " + FileSystemManager.getName(pluginDirectory) + ". Only directories named after the plugin they contain are expected in Areca's main plugins directory (" + pluginsDirectory + ")"); 
                    String name = FileSystemManager.getName(pluginDirectory).toLowerCase();
                    if (name.endsWith(".zip") || name.endsWith(".gz") || name.endsWith(".7z") || name.endsWith(".tar")) {
                        Logger.defaultLogger().error(name + " seems to be an archive. If you downloaded this file as a plugin for Areca, you should probably unzip it."); 
                    }
                }
            }
        }
    }

    public void register(Plugin plugin) {
        this.plugins.put(plugin.getId(), plugin);
        Logger.defaultLogger().info("Plugin successfully registered : " + plugin.toString());
    }

    private Plugin instanciate(File configFile) throws ApplicationException {
        try {
            if (FileSystemManager.exists(configFile)) {
                Logger.defaultLogger().info("Reading plugin configuration file : " + FileSystemManager.getDisplayPath(configFile));

                // Read plugin config file
                InputStream is = null;
                Properties props = new Properties();
                try {
                    is = FileSystemManager.getFileInputStream(configFile);
                    props.load(is);            
                } catch (IOException e) {
                    Logger.defaultLogger().error(e);
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            Logger.defaultLogger().error(e);
                        }
                    }
                }
                
                // Build class loader
                File rootDir = FileSystemManager.getParentFile(configFile);
                String classpath = props.getProperty(KEY_JAR_PATH);
                ArrayList jars = new ArrayList();
                StringTokenizer stt = new StringTokenizer(classpath, SEPARATOR);
                while (stt.hasMoreTokens()) {
                	String jar = stt.nextToken();
                	jars.add(jar);
                }
                // Also add the parent directory
                jars.add("/");
                
                URL[] urls = new URL[jars.size()];
                for (int i=0; i<urls.length; i++) {
                	String path = (String)jars.get(i);
                	
                    // Load Jar
                    File jarFile = new File(rootDir, path);
                    Logger.defaultLogger().info("Loading jar file : " + FileSystemManager.getDisplayPath(jarFile));
                    urls[i] = new URL("file:" + FileSystemManager.getAbsolutePath(jarFile));
                }
                ClassLoader cl = new URLClassLoader(urls);
                
                // Load main class
                String mainClass = props.getProperty(KEY_CLASS);
                Logger.defaultLogger().info("Instanciating class : " + mainClass);
                Class pluginClass = cl.loadClass(mainClass);
                Plugin plugin = (Plugin)pluginClass.newInstance();
                plugin.setClassLoader(cl);
                plugin.setPath(FileSystemManager.getAbsolutePath(rootDir));
                return plugin;
            } else {
                Logger.defaultLogger().info("Plugin configuration file not found.");
                return null;        
            }
        } catch (Throwable e) {
            Logger.defaultLogger().error(e);
            throw new ApplicationException(e);
        }
    }

    public Collection getAll(Class implementedInterface, boolean ordered) {
    	List ret = new ArrayList();
    	Iterator iter = this.plugins.values().iterator();
    	
    	while (iter.hasNext()) {
    		Object plugin = iter.next();
    		if (implementedInterface.isAssignableFrom(plugin.getClass())) {
    			ret.add(plugin);
    		}
    	}
    	
    	if (ordered) {
    		Object[] content = ret.toArray();
    		Arrays.sort(content);
    		ret = Arrays.asList(content);
    	}
        return ret;
    }

    public Plugin getById(String id) {
        return (Plugin)this.plugins.get(id);
    }
}
