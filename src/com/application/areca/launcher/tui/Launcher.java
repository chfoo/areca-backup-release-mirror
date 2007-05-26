package com.application.areca.launcher.tui;

import java.io.File;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ArecaTechnicalConfiguration;
import com.application.areca.RecoveryProcess;
import com.application.areca.UserInformationChannel;
import com.application.areca.adapters.ProcessXMLReader;
import com.application.areca.context.ProcessContext;
import com.application.areca.launcher.CommandConstants;
import com.application.areca.launcher.InvalidCommandException;
import com.application.areca.launcher.UserCommand;
import com.application.areca.version.VersionInfos;
import com.myJava.file.FileSystemManager;
import com.myJava.util.CalendarUtils;
import com.myJava.util.log.FileLogProcessor;
import com.myJava.util.log.Logger;
import com.myJava.util.os.OSTool;

/**
 * Launcher
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
public class Launcher implements CommandConstants {
    public static String SEPARATOR = "------------------------------------------------------------------";
    private static UserInformationChannel channel = new LoggerUserInformationChannel();
    
    /**
     * Méthode principale de lancement.
     * @param args
     */
    public static void main(String[] args) {
        checkJavaVersion();
        ArecaTechnicalConfiguration.initialize();
        UserCommand command = null;
        try {
            command = new UserCommand(args);
            command.parse();
            
            // On logge dans le répertoire du fichier de config
            if (command.hasOption(OPTION_CONFIG)) {
                File f = new File(command.getOption(OPTION_CONFIG));
                if (FileSystemManager.exists(f)) {
        	        Logger.defaultLogger().removeAllProcessors();
        	        FileLogProcessor proc = new FileLogProcessor(command.getOption(OPTION_CONFIG) + ".log");
        	        Logger.defaultLogger().addProcessor(proc);
                }
            }
            
            Logger.defaultLogger().info("Starting the process ... config = [" + command.getOption(OPTION_CONFIG) + "].");
            channel.logInfo("", "Starting the process ... config = [" + command.getOption(OPTION_CONFIG) + "].");
            
            ProcessXMLReader adapter = new ProcessXMLReader(command.getOption(OPTION_CONFIG));
            adapter.setMissingDataListener(new MissingDataListener());
            RecoveryProcess process = adapter.load();
            process.setInfoChannel(channel);
            
            if (command.getCommand().equalsIgnoreCase(COMMAND_COMPACT)) {
                processCompact(command, process);
            } else if (command.getCommand().equalsIgnoreCase(COMMAND_RECOVER)) {
                processRecover(command, process);
            } else if (command.getCommand().equalsIgnoreCase(COMMAND_BACKUP)) {
                processBackup(command, process);
            } else if (command.getCommand().equalsIgnoreCase(COMMAND_DESCRIBE)) {
                processDescribe(command, process);
            } else if (command.getCommand().equalsIgnoreCase(COMMAND_DELETE)) {
                processDelete(command, process);
            }
            
            Logger.defaultLogger().info("End of process.");
            channel.logInfo("", "End of process.");
            
        } catch (InvalidCommandException e) {
            channel.logInfo("", SEPARATOR);
            channel.logInfo("", "File Backup Software (Copyright 2005-2007, Olivier PETRUCCI)");
            channel.logInfo("", SEPARATOR);
            channel.logInfo("", "Syntax   : (command) (options)");
            channel.logInfo("", "Commands : describe / backup / merge / delete / recover");
            channel.logInfo("", "Options (describe): -config (your xml config file)");
            channel.logInfo("", "Options (backup)  : -config (your xml config file) [-target (specific target)]");
            channel.logInfo("", "Options (merge) : -config (your xml config file) -target (specific target) [-date (recovery date : YYYY-MM-DD) / -delay (nr of days)]");
            channel.logInfo("", "Options (delete) : -config (your xml config file) -target (specific target) [-date (recovery date : YYYY-MM-DD) / -delay (nr of days)]");
            channel.logInfo("", "Options (recover) : -config (your xml config file) -target (specific target) -destination (destination folder) [-date (recovery date : YYYY-MM-DD)]");
            channel.logInfo("", SEPARATOR);
            channel.logInfo("", "Invalid Command : " + command.toString());
            channel.logInfo("", e.getMessage());
            channel.logInfo("", SEPARATOR);
        } catch (Throwable e) {
            channel.logInfo("", SEPARATOR);
            channel.logWarning("", "An error occured during the process : " + e.getMessage());
            channel.logWarning("", "Please refer to the log file : " + ((FileLogProcessor)Logger.defaultLogger().find(FileLogProcessor.class)).getCurrentLogFile());
            channel.logInfo("", SEPARATOR);
            
            // On logge tout systématiquement.
            Logger.defaultLogger().error(e);
        }
    }
    
    /**
     * Processus de backup
     *
     * @param command
     * @param process
     */
    private static void processBackup(UserCommand command, RecoveryProcess process) throws Exception {
        if (command.hasOption(OPTION_TARGET)) {
            AbstractRecoveryTarget target = getTarget(process, command.getOption(OPTION_TARGET));
            ProcessContext context = new ProcessContext(target);
            process.processBackupOnTarget(
                    target,
                    context
            );
        } else {
            process.launchBackup();
        }
    }
    
    /**
     * Merges the archives.
     *
     * @param command
     * @param process
     */
    private static void processCompact(UserCommand command, RecoveryProcess process) throws Exception {
        String strDelay = command.getOption(OPTION_DELAY);
        AbstractRecoveryTarget target =getTarget(process, command.getOption(OPTION_TARGET));
        if (strDelay != null) {
            // A delay (in days) is provided
            process.processCompactOnTarget(
                    target,
                    Integer.parseInt(strDelay),
                    new ProcessContext(target)
            );
        } else {
            // A full date is provided
            process.processCompactOnTarget(
                    target,
                    null,
                    CalendarUtils.resolveDate(command.getOption(OPTION_DATE), null),
                    null,
                    new ProcessContext(target)
            );
        }
    }
    
    /**
     * Merges the archives.
     *
     * @param command
     * @param process
     */
    private static void processDelete(UserCommand command, RecoveryProcess process) throws Exception {
        String strDelay = command.getOption(OPTION_DELAY);
        AbstractRecoveryTarget target =getTarget(process, command.getOption(OPTION_TARGET));
        if (strDelay != null) {
            // A delay (in days) is provided
            process.processDeleteOnTarget(
                    target,
                    Integer.parseInt(strDelay),
                    new ProcessContext(target)
            );
        } else {
            // A full date is provided
            process.processDeleteOnTarget(
                    target,
                    CalendarUtils.resolveDate(command.getOption(OPTION_DATE), null),
                    new ProcessContext(target)
            );
        }
    }
    
    /**
     * Processus de chargement d'une archive
     *
     * @param command
     * @param process
     */
    private static void processRecover(UserCommand command, RecoveryProcess process) throws Exception {
        AbstractRecoveryTarget target =getTarget(process, command.getOption(OPTION_TARGET));
        process.processRecoverOnTarget(
                target,
                null,
                command.getOption(OPTION_DESTINATION),
                CalendarUtils.resolveDate(command.getOption(OPTION_DATE), null),
                false, 
                new ProcessContext(target)
        );
    }
    
    /**
     * Processus de description d'un fichier de config
     *
     * @param command
     * @param process
     */
    private static void processDescribe(UserCommand command, RecoveryProcess process) throws Exception {
        channel.logInfo("", process.getDescription());
    }
    
    /**
     * Retourne la target demandée.
     *
     * @param process
     * @param targetId
     * @return
     * @throws InvalidCommandException
     */
    private static AbstractRecoveryTarget getTarget(RecoveryProcess process, String targetId) throws InvalidCommandException {
        AbstractRecoveryTarget target = process.getTargetById(Integer.parseInt(targetId));
        if (target == null) {
            throw new InvalidCommandException("Invalid target ID : [" + targetId + "]");
        } else {
            return target;
        }
    }
    
    private static void checkJavaVersion() {
        if (!
                OSTool.isJavaVersionGreaterThanOrEquals(VersionInfos.REQUIRED_JAVA_VERSION)
        ) {
            System.out.println(SEPARATOR + "\n ");
            System.out.println(VersionInfos.VERSION_MSG);
            System.out.println(SEPARATOR);
            System.exit(-1);
        }
    }
}

