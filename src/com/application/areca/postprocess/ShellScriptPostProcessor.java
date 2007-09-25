package com.application.areca.postprocess;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.application.areca.impl.TagHelper;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.object.PublicClonable;
import com.myJava.util.log.Logger;

/**
 * Launches a shell script
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 7453350623295719521
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
public class ShellScriptPostProcessor extends AbstractPostProcessor {
    private String command;
    private String commandParameters;

    public ShellScriptPostProcessor() {
        super();
    }
    
    public String getCommand() {
        return command;
    }
    
    public void setCommand(String command) {
        this.command = command;
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
        
        String[] elements = new String[args.size() + 1];
        elements[0] = this.command;
        for (int i=0; i<args.size(); i++) {
            elements[i+1] = (String)args.get(i);
        }
        
        return elements;
    }
    
    public void run(ProcessContext context) throws ApplicationException {
        try {
            String[] fullCommand = getFullCommand(context);
            Process p = Runtime.getRuntime().exec(fullCommand);
            Logger.defaultLogger().info("Shell command [" + this.command + "] executed with the following parameters :" + paramsToString(fullCommand) + ". Exit value = [" + p.waitFor() + "]"); // Wait until the process finishes
        } catch (Throwable e) {
            String msg = "Error during shell commmand execution (" + command + ") : " + e.getMessage();
            throw new ApplicationException(msg, e);
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
        for (int i=1; i<params.length; i++) {
            s += " \"" + params[i] + "\"";
        }
        return s;
    }
    
    public PublicClonable duplicate() {
        ShellScriptPostProcessor pro = new ShellScriptPostProcessor();
        pro.command = this.command;
        return pro;
    }
    
    public void validate() throws PostProcessorValidationException {
        if (command == null || command.trim().length() == 0) {
            throw new PostProcessorValidationException("A shell command must be supplied.");
        }
        
        if (commandParameters != null) {
            if (commandParameters.indexOf('\"') != -1) {
                throw new PostProcessorValidationException("Shell arguments can't contain quotes.");
            }
        }
    }
    
    
    public boolean equals(Object obj) {
        if (obj == null || (! (obj instanceof ShellScriptPostProcessor)) ) {
            return false;
        } else {
            ShellScriptPostProcessor other = (ShellScriptPostProcessor)obj;
            return 
                EqualsHelper.equals(this.command, other.command)
                && EqualsHelper.equals(this.commandParameters, other.commandParameters)
            ;
        }
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, this.command);
        h = HashHelper.hash(h, this.commandParameters);
        return h;
    }
}
