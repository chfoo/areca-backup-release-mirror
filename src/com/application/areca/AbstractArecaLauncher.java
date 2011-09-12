package com.application.areca;

import java.util.Iterator;
import java.util.Map;

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
 Copyright 2005-2011, Olivier PETRUCCI.

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
    	
        Map javaargs = FrameworkConfiguration.getInstance().getJavaProperties();
        if (javaargs != null) {
            Iterator iter = javaargs.keySet().iterator();
            while (iter.hasNext()) {
                String key = (String)iter.next();
                String value = (String)javaargs.get(key);
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
}
