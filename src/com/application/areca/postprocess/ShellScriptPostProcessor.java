package com.application.areca.postprocess;

import com.application.areca.ApplicationException;
import com.application.areca.context.ProcessContext;
import com.myJava.util.EqualsHelper;
import com.myJava.util.HashHelper;
import com.myJava.util.PublicClonable;
import com.myJava.util.log.Logger;

/**
 * Launches a shell script
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4945525256658487980
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

    public ShellScriptPostProcessor() {
        super();
    }
    
    public String getCommand() {
        return command;
    }
    
    public void setCommand(String command) {
        this.command = command;
    }
    
    public void run(ProcessContext context) throws ApplicationException {
        try {
            Process p = Runtime.getRuntime().exec(command);
            Logger.defaultLogger().info("Shell command [" + this.command + "] executed. Exit value = [" + p.waitFor() + "]"); // Wait until the process finishes
        } catch (Throwable e) {
            String msg = "Error during shell commmand execution (" + command + ") : " + e.getMessage();
            throw new ApplicationException(msg, e);
        }
    }
    
    public boolean requiresProcessReport() {
        return false;
    }
    
    public String getParametersSummary() {
        return this.command;
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
    }
    
    
    public boolean equals(Object obj) {
        if (obj == null || (! (obj instanceof ShellScriptPostProcessor)) ) {
            return false;
        } else {
            ShellScriptPostProcessor other = (ShellScriptPostProcessor)obj;
            return EqualsHelper.equals(this.command, other.command);
        }
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, this.command);
        return h;
    }
}
