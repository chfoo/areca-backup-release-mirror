package com.application.areca.launcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.application.areca.ArecaConfiguration;
import com.application.areca.version.VersionInfos;
import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.system.AbstractLauncher;
import com.myJava.system.OSTool;
import com.myJava.util.log.Logger;
import com.myJava.util.log.ThreadLocalLogProcessor;

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
public abstract class AbstractArecaLauncher 
extends AbstractLauncher {
	public static final int ERR_JAVA_VERSION = 3;
    public static String SEPARATOR = "------------------------------------------------------------------";
    
	private static AbstractArecaLauncher INSTANCE;
	
	public static AbstractArecaLauncher getInstance() {
		return INSTANCE;
	}
	
	// Not beautiful but safe enough because gui.Launcher
	// and tui.Launcher are mutually exclusive ... to be refactored when i'll have some time
	public static void setInstance(AbstractArecaLauncher instance) {
		INSTANCE = instance;
	}
    
    protected void initialize() {
        ArecaConfiguration.initialize();
    	Logger.defaultLogger().setTlLogProcessor(new ThreadLocalLogProcessor());
    	
        Map javaArgs = FrameworkConfiguration.getInstance().getJavaProperties();
        if (javaArgs != null) {
            Iterator iter = javaArgs.keySet().iterator();
            while (iter.hasNext()) {
                String key = (String)iter.next();
                String value = (String)javaArgs.get(key);
                Logger.defaultLogger().info("Overriding java property : [" + key + "] = [" + value + "]");
                System.setProperty(key, value);
            }
        }
    }

	protected void checkJavaVersion() {
        if (!
                OSTool.isJavaVersionGreaterThanOrEquals(VersionInfos.REQUIRED_JAVA_VERSION)
        ) {
            System.out.println(SEPARATOR + "\n ");
            System.out.println(VersionInfos.VERSION_MSG);
            showLine();
            setErrorCode(ERR_JAVA_VERSION);
            exit();
        }
        
        if (! VersionInfos.checkJavaVendor()) {
            showLine();
            System.out.println(VersionInfos.VENDOR_MSG);
            showLine();
        }
    }
    
    protected static void showLine() {
        System.out.println(SEPARATOR);
    }
    
    protected String[] preprocessArguments(String[] args) {
    	
    	// Backward compatibility : former versions of Areca used "JSmooth" as exe wrapper.
    	// Newer versions use Launch4J, which doesn't recognize JRE parameters such as JInitialheap or JMaxheap.
    	// --> These parameters have to be removed.
    	ArrayList ret = new ArrayList();
    	boolean memWarning = false;
    	for (int i=0; i<args.length; i++) {
    		String arg = args[i].trim().toLowerCase();
    		if (arg.startsWith("-jinitialheap=") || arg.startsWith("-jmaxheap=")) {
    			memWarning = true;
    		} else {
    			ret.add(args[i]);
    		}
    	}
    	
    	if (memWarning) {
    		System.out.println("parameters such as -jinitialheap or -jmaxheap should not be usefull anymore and will be ignored. If you wish to pass memory parameters to areca, please refer to online documentation on how to proceed.");
    	}
    	return (String[])ret.toArray(new String[ret.size()]);
    }
}
