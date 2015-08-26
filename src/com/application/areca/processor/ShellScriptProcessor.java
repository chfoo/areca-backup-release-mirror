package com.application.areca.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.application.areca.impl.tools.TagHelper;
import com.myJava.object.Duplicable;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.system.OSTool;
import com.myJava.util.log.Logger;

/**
 * Launches a shell script
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
public class ShellScriptProcessor extends AbstractProcessor {
    private String command;
    private String commandParameters;

    public ShellScriptProcessor() {
        super();
    }
    
    public String getCommand() {
        return command;
    }
    
    public void setCommand(String command) {
        this.command = command;
    }
    
    public boolean requireStatistics() {
		return false;
	}
    
	public String getKey() {
		return "Execute shell script";
	}

    public String getCommandParameters() {
        return commandParameters;
    }

    public void setCommandParameters(String parameters) {
        this.commandParameters = parameters;
    }

    private String[] getFullCommand(ProcessContext context) {
        List args = new ArrayList();
        
        if (commandParameters != null) {
            StringTokenizer stt = new StringTokenizer(commandParameters, ";");
            while (stt.hasMoreTokens()) {
                args.add(TagHelper.replaceParamValues(stt.nextToken(), context));
            }
        }

        String[] elements = new String[args.size() + (OSTool.isSystemWindows() ? 3 : 1)];
        
        int offset = 0;
        if (OSTool.isSystemWindows()) {
            elements[0] = "cmd";
            elements[1] = "/c";
            offset = 2;
        }
        
        elements[offset] = this.command;
        for (int i=0; i<args.size(); i++) {
            elements[i+1+offset] = (String)args.get(i);
        }
        
        return elements;
    }
    
    public void runImpl(ProcessContext context) throws ApplicationException {
        BufferedReader errorReader = null;
        Process process = null;
        try {
            String[] fullCommand = getFullCommand(context);
            process = Runtime.getRuntime().exec(fullCommand);
            int retValue = process.waitFor(); // Wait until the process finishes
            String err = "";
            if (retValue != 0) {
                errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                err = " : [" + errorReader.readLine() + "]";
            }
            Logger.defaultLogger().info("Shell command [" + paramsToString(fullCommand) + "] executed - exit value = " + retValue + err); 
       
            if (retValue != 0) {
            	throw new ApplicationException("Error running " + paramsToString(fullCommand) + " - exit value = " + retValue + err);
            }
        } catch (Throwable e) {
            if (e instanceof ApplicationException) {
                throw (ApplicationException)e;
            } else {
                String msg = "Error during shell commmand execution (" + command + ") : " + e.getMessage();
                throw new ApplicationException(msg, e);
            }
        } finally {
            try {
                // ERROR
                if (errorReader != null) {
                    errorReader.close();
                } else if (process != null) {
                    process.getErrorStream().close();
                }
                
                // OUT
                if (process != null) {
                    process.getInputStream().close();
                    process.getOutputStream().close();
                    
                    // Make sure that the process is destroyed ... closing the streams doesn't seem to be enough on some VM implementations (?!)
                    process.destroy();
                    process = null;
                }
            } catch (IOException e) {
                Logger.defaultLogger().error("Error closing stream.", e);
            }
        }
    }
    
    public boolean requiresFilteredEntriesListing() {
        return false;
    }
    
    public String getParametersSummary() {
        if (commandParameters != null && commandParameters.trim().length() != 0) {
            return this.command + " - {" + commandParameters + "}";   
        } else {
            return this.command;
        }
    }

    private String paramsToString(String[] params) {
        String s = "";
        for (int i=0; i<params.length; i++) {
            s += " \"" + params[i] + "\"";
        }
        return s;
    }
    
    public Duplicable duplicate() {
        ShellScriptProcessor pro = new ShellScriptProcessor();
        copyAttributes(pro);
        pro.command = this.command;
        return pro;
    }
    
    public void validate() throws ProcessorValidationException {
        if (command == null || command.trim().length() == 0) {
            throw new ProcessorValidationException("A shell command must be supplied.");
        }
        
        if (commandParameters != null) {
            if (commandParameters.indexOf('\"') != -1) {
                throw new ProcessorValidationException("Shell arguments can't contain quotes.");
            }
        }
    }
    
    
    public boolean equals(Object obj) {
        if (obj == null || (! (obj instanceof ShellScriptProcessor)) ) {
            return false;
        } else {
            ShellScriptProcessor other = (ShellScriptProcessor)obj;
            return 
            	super.equals(other)
            	&& EqualsHelper.equals(this.command, other.command)
                && EqualsHelper.equals(this.commandParameters, other.commandParameters)
            ;
        }
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, super.hashCode());
        h = HashHelper.hash(h, this.command);
        h = HashHelper.hash(h, this.commandParameters);
        return h;
    }
}
