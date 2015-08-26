package com.myJava.system;

import java.util.ArrayList;
import java.util.Iterator;

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
public abstract class AbstractLauncher {
	public static final int ERR_UNEXPECTED = 1;
	public static final int ERR_SYNTAX = 2;
	public static final int ERR_INVALID_ARCHIVE = 3;
	
    private int errorCode = 0;
    private ArrayList closeCallBacks = new ArrayList();
    
    public void launch(String[] args) {
        try {
            initialize();
            checkJavaVersion();
            launchImpl(preprocessArguments(args));   
        } catch (Throwable e) {
            e.printStackTrace();
            Logger.defaultLogger().error("Unexpected error", e);
            setErrorCode(ERR_UNEXPECTED);
        } finally {
        	exit();
        }
    }
    
    protected String[] preprocessArguments(String[] args) {
    	return args;
    }
    
    public void exit() {
    	exit(false);
    }
    
    public void exit(boolean force) {
    	Logger.defaultLogger().info("Closing Areca (force=" + force + ")");
    	Iterator iter = closeCallBacks.iterator();
    	while (iter.hasNext()) {
    		Runnable rn = (Runnable)iter.next();
    		rn.run();
    	}
    	
    	if (returnErrorCode() || force) {
    		System.exit(errorCode);
    	}
    }
    
    public void addCloseCallBack(Runnable rn) {
    	this.closeCallBacks.add(rn);
    }

    public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	protected abstract boolean returnErrorCode();
	protected abstract void initialize();
    protected abstract void launchImpl(String[] args);
    protected abstract void checkJavaVersion();
}