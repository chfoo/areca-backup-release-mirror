package com.application.areca.launcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.application.areca.launcher.tui.Launcher;


/**
 * Classe implémentant une commande utilisateur 
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4438212685798161280
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
public class UserCommand implements CommandConstants {
    
    private static Map ACCEPTED_OPTIONS;
    private static List ACCEPTED_COMMANDS;
    
    static {
        ACCEPTED_OPTIONS = new HashMap();
        
        ArrayList optsBackup = new ArrayList();
        optsBackup.add(Launcher.OPTION_CONFIG);
        optsBackup.add(Launcher.OPTION_TARGET);
        ACCEPTED_OPTIONS.put(Launcher.COMMAND_BACKUP, optsBackup);
        
        ArrayList optsCompact = new ArrayList();
        optsCompact.add(Launcher.OPTION_CONFIG);
        optsCompact.add(Launcher.OPTION_TARGET);
        optsCompact.add(Launcher.OPTION_DATE);
        optsCompact.add(Launcher.OPTION_DELAY);
        ACCEPTED_OPTIONS.put(Launcher.COMMAND_COMPACT, optsCompact);
        
        ArrayList optsRecover = new ArrayList();
        optsRecover.add(Launcher.OPTION_CONFIG);
        optsRecover.add(Launcher.OPTION_TARGET);
        optsRecover.add(Launcher.OPTION_DATE);
        optsRecover.add(Launcher.OPTION_DESTINATION);
        ACCEPTED_OPTIONS.put(Launcher.COMMAND_RECOVER, optsRecover);
        
        ArrayList optsDescribe = new ArrayList();
        optsDescribe.add(Launcher.OPTION_CONFIG);
        ACCEPTED_OPTIONS.put(Launcher.COMMAND_DESCRIBE, optsDescribe);
        
        ArrayList optsDelete = new ArrayList();
        optsDelete.add(Launcher.OPTION_CONFIG);
        optsDelete.add(Launcher.OPTION_TARGET);
        optsDelete.add(Launcher.OPTION_DATE);
        optsDelete.add(Launcher.OPTION_DELAY);
        ACCEPTED_OPTIONS.put(Launcher.COMMAND_DELETE, optsDelete);
        
        
        
        ACCEPTED_COMMANDS = new ArrayList();
        ACCEPTED_COMMANDS.add(Launcher.COMMAND_BACKUP);
        ACCEPTED_COMMANDS.add(Launcher.COMMAND_COMPACT);
        ACCEPTED_COMMANDS.add(Launcher.COMMAND_RECOVER);
        ACCEPTED_COMMANDS.add(Launcher.COMMAND_DESCRIBE);
        ACCEPTED_COMMANDS.add(Launcher.COMMAND_DELETE);
    }
    
    private String[] args;
    private String command;
    private HashMap options;
    
    /**
     * Format :
     * [Command] [Param0] [Value0] ... [ParamN] [ValueN]
     */
    public UserCommand(String[] args) {
        this.args = rebuildArguments(args);
    }
    
    public void parse() throws InvalidCommandException {
        
        if (args.length%2 == 0) {
            throw new InvalidCommandException("Invalid argument number : "
                    + args.length);
        }
        
        options = new HashMap();
        command = args[0];
        if (! validateCommand(command)) {
            throw new InvalidCommandException("Invalid command : [" +
                    command + "]");
        }
        
        String option, value;
        for (int i=1; i<args.length; i+=2) {
            option = args[i];
            value = args[i+1];
            
            if (option != null && option.length() != 0) {
                if (! validateOption(command, option)) {
                    throw new InvalidCommandException("Invalid option : [" + option + "]");
                }
                
                options.put(option, value);
            }
        }
        
        this.validateStructure();
    }
    
    /**
     * Checks that the option is available for the command provided as
     argument.
     */
    protected static boolean validateOption(String command, String option) {
        List lst = (List)ACCEPTED_OPTIONS.get(command);
        return lst.contains(option);
    }
    
    /**
     * Checks that the command is valid.
     */
    protected static boolean validateCommand(String command) {
        return ACCEPTED_COMMANDS.contains(command);
    }
    
    /**
     * Validates the mandatory options.
     */
    protected void validateStructure() throws InvalidCommandException {
        if (!options.containsKey(Launcher.OPTION_CONFIG)) {
            throw new InvalidCommandException("The " + Launcher.OPTION_CONFIG + " option is mandatory.");
        } else {
            if (
                    (
                            
                            this.command.equalsIgnoreCase(Launcher.COMMAND_RECOVER)
                            ||
                            this.command.equalsIgnoreCase(Launcher.COMMAND_COMPACT)
                            ||
                            this.command.equalsIgnoreCase(Launcher.COMMAND_DELETE)
                    )
                    && (! this.options.containsKey(Launcher.OPTION_TARGET))
            ) {
                throw new InvalidCommandException("The " +
                        Launcher.OPTION_TARGET + " option is mandatory.");
            } else if (
                    this.command.equalsIgnoreCase(Launcher.COMMAND_RECOVER)
                    && (!
                            this.options.containsKey(Launcher.OPTION_DESTINATION))
            ) {
                throw new InvalidCommandException("The " +
                        Launcher.OPTION_DESTINATION + " option is mandatory.");
            } else if (
                    this.command.equalsIgnoreCase(Launcher.COMMAND_DELETE)
                    && (! this.options.containsKey(Launcher.OPTION_DELAY))
                    && (! this.options.containsKey(Launcher.OPTION_DATE))
            ) {
                throw new InvalidCommandException("The " +
                        Launcher.OPTION_DELAY + " or " + Launcher.OPTION_DATE + " option is mandatory.");
            }
        }
    }
    
    public String getCommand() {
        return this.command;
    }
    
    public String getOption(String option) {
        return (String)(this.options.get(option));
    }
    
    public boolean hasOption(String option) {
        return this.options.containsKey(option);
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<args.length; i++) {
            if (args[i].trim().length() != 0) {
                sb.append(" [").append(args[i]).append("] ");
            }
        }
        return sb.toString().trim();
    }
    
    private static String[] rebuildArguments(String[] args) {
        List ret = new ArrayList();
        boolean isStarted = false;
        String currentPart = "";
        for (int i=0; i<args.length; i++) {
            String data = args[i].trim();
            if (data.length() != 0) {
                if (isFirstPart(data)) {
                    if (isLastPart(data)) {
                        add(ret, data.substring(1, data.length() - 1));
                    } else {
                        isStarted = true;
                        currentPart = data.substring(1);
                    }
                } else if (isLastPart(data)) {
                    isStarted = false;
                    currentPart += " ";
                    currentPart += data.substring(0, data.length() - 1);
                    add(ret, currentPart);
                    currentPart = "";
                } else if (isStarted) {
                    currentPart += " ";
                    currentPart += data;
                } else {
                    add(ret, data);
                }
            }
        }
        
        return (String[])ret.toArray(new String[0]);
    }
    
    
    private static void add(List l, String v) {
        if (v.trim().length() != 0) {
            l.add(v);
        }
    }
    
    private static boolean isFirstPart(String s) {
        return 
        	s.charAt(0) == '\"' 
        	|| s.charAt(0) == '['
        ;
    }
    
    private static boolean isLastPart(String s) {
        return s.endsWith("\"") || s.endsWith("]");
    }
    
    public static void main(String[] args) {
        show(rebuildArguments(args));
    }
    
    private static void show(String[] args) {
        System.out.println("----------------------------------");
        for (int i=0; i<args.length; i++) {
            System.out.println("<" + args[i] + ">");
        }
    }
}