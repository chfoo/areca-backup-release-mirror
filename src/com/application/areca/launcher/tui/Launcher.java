package com.application.areca.launcher.tui;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.application.areca.AbstractArecaLauncher;
import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.RecoveryProcess;
import com.application.areca.UserInformationChannel;
import com.application.areca.adapters.ProcessXMLReader;
import com.application.areca.context.ProcessContext;
import com.application.areca.launcher.CommandConstants;
import com.application.areca.launcher.InvalidCommandException;
import com.application.areca.launcher.UserCommandLine;
import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemManager;
import com.myJava.util.CalendarUtils;
import com.myJava.util.Util;
import com.myJava.util.log.ConsoleLogProcessor;
import com.myJava.util.log.FileLogProcessor;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
 * Launcher
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 8290826359148479344
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
public class Launcher
extends AbstractArecaLauncher
implements CommandConstants {
    
    private static UserInformationChannel channel = new LoggerUserInformationChannel(false);
    
    public static void main(String[] args) {
        Launcher launcher = new Launcher();
        launcher.launch(args);
    }
    
    /**
     * Méthode principale de lancement.
     * @param args
     */
    protected void launchImpl(String[] args) {
        UserCommandLine command = null;
        try {
            command = new UserCommandLine(args);
            command.parse();
            
            // On logge dans le répertoire du fichier de config
            if (command.hasOption(OPTION_CONFIG)) {
                File f = new File(command.getOption(OPTION_CONFIG));
                if (FileSystemManager.exists(f)) {
                    Logger.defaultLogger().remove(FileLogProcessor.class);
                    Logger.defaultLogger().remove(ConsoleLogProcessor.class);
                    File configFile = new File(Util.replace(command.getOption(OPTION_CONFIG), ".xml", ""));
                    File logFile = new File(
                            FileSystemManager.getParentFile(configFile) + "/log/",
                            FileSystemManager.getName(configFile)
                    );
        	        FileLogProcessor proc = new FileLogProcessor(logFile);
        	        Logger.defaultLogger().addProcessor(proc);
                }
            }
            
            Logger.defaultLogger().info("Starting the process ... config = [" + command.getOption(OPTION_CONFIG) + "].");
            channel.print("Starting the process ... config = [" + command.getOption(OPTION_CONFIG) + "].");
            
            ProcessXMLReader adapter = new ProcessXMLReader(command.getOption(OPTION_CONFIG));
            adapter.setMissingDataListener(new MissingDataListener());
            RecoveryProcess process = adapter.load();
            AbstractRecoveryTarget target = null;
            if (command.hasOption(OPTION_TARGET)) {
                target = getTarget(process, command.getOption(OPTION_TARGET));
            }
            ProcessContext context = new ProcessContext(target, channel, new TaskMonitor("tui-main"));
            
            if (command.getCommand().equalsIgnoreCase(COMMAND_COMPACT.getName())) {
                processCompact(command, process, context);
            } else if (command.getCommand().equalsIgnoreCase(COMMAND_RECOVER.getName())) {
                processRecover(command, process, context);
            } else if (command.getCommand().equalsIgnoreCase(COMMAND_BACKUP.getName())) {
                processBackup(command, process, context);
            } else if (command.getCommand().equalsIgnoreCase(COMMAND_DESCRIBE.getName())) {
                processDescribe(command, process, context);
            } else if (command.getCommand().equalsIgnoreCase(COMMAND_DELETE.getName())) {
                processDelete(command, process, context);
            }
            
            Logger.defaultLogger().info("End of process.");
            channel.print("End of process.");
            
        } catch (InvalidCommandException e) {
            channel.print(SEPARATOR);
            channel.print("File Backup Software (Copyright 2005-2007, Olivier PETRUCCI)");
            channel.print(SEPARATOR);
            channel.print("Syntax   : (command) (options)");
            channel.print("Commands : describe / backup / merge / delete / recover");
            channel.print("Options (describe): -config (your xml config file)");
            channel.print("Options (backup)  : -config (your xml config file) [-f] [-d] [-target (specific target)]");
            channel.print("Options (merge) : -config (your xml config file) -target (specific target)  [-k] -date (recovery date : YYYY-MM-DD) / -from (nr of days - 0='-infinity') -to (nr of days - 0='today')");
            channel.print("Options (delete) : -config (your xml config file) -target (specific target) [-date (recovery date : YYYY-MM-DD) / -delay (nr of days)]");
            channel.print("Options (recover) : -config (your xml config file) -target (specific target) -destination (destination folder) [-date (recovery date : YYYY-MM-DD)]");
            channel.print(SEPARATOR);
            channel.print("Invalid Command : " + command.toString());
            channel.print(e.getMessage());
            channel.print(SEPARATOR);
        } catch (Throwable e) {
            handleError(e);
        }
    }
    
    private static void handleError(Throwable e) {
        channel.print(SEPARATOR);
        channel.print("An error occured during the process : " + e.getMessage());
        if (((FileLogProcessor)Logger.defaultLogger().find(FileLogProcessor.class)) != null) {
            channel.print("Please refer to the log file : " + ((FileLogProcessor)Logger.defaultLogger().find(FileLogProcessor.class)).getCurrentLogFile());
        }
        channel.print(SEPARATOR);
        
        // On logge tout systématiquement.
        Logger.defaultLogger().error(e);
    }
    
    /**
     * Processus de backup
     *
     * @param command
     * @param process
     */
    private static void processBackup(UserCommandLine command, final RecoveryProcess process, final ProcessContext context) throws Exception {
        final String backupScheme;
        String fOption = command.getOption(OPTION_FULL_BACKUP);
        String dOption = command.getOption(OPTION_DIFFERENTIAL_BACKUP);        
        if (fOption != null && fOption.trim().length() != 0) {
            backupScheme = AbstractRecoveryTarget.BACKUP_SCHEME_FULL;
        } else if (dOption != null && dOption.trim().length() != 0) {
            backupScheme = AbstractRecoveryTarget.BACKUP_SCHEME_DIFFERENTIAL;
        } else {
            backupScheme = AbstractRecoveryTarget.BACKUP_SCHEME_INCREMENTAL;
        }
        
        if (command.hasOption(OPTION_TARGET)) {
            AbstractRecoveryTarget target = getTarget(process, command.getOption(OPTION_TARGET));
            process.processBackupOnTarget(
                    target,
                    context,
                    backupScheme
            );
        } else {
            List thList = new ArrayList();
            
            Iterator iter = process.getTargetIterator();
            while (iter.hasNext()) {
                final AbstractRecoveryTarget tg = (AbstractRecoveryTarget)iter.next();
                final ProcessContext cloneCtx = new ProcessContext(tg, new LoggerUserInformationChannel(true), new TaskMonitor("tui-clone"));
                Thread th = new Thread(new Runnable() {
                    public void run() {
                        try {
                            process.processBackupOnTarget(
                                    tg,
                                    cloneCtx,
                                    backupScheme
                            );
                        } catch (Exception e) {
                            handleError(e);
                        }
                    }
                });
                th.setName("Backup on " + tg.getTargetName());
                th.setDaemon(false);
                thList.add(th);
                th.start();
            }
            
            // Wait for all threads to die
            Iterator thIter = thList.iterator();
            while (thIter.hasNext()) {
                Thread th = (Thread) thIter.next();
                th.join();
            }
        }
    }
    
    /**
     * Merges the archives.
     *
     * @param command
     * @param process
     */
    private static void processCompact(UserCommandLine command, RecoveryProcess process, ProcessContext context) throws Exception {
        String strDelay = command.getOption(OPTION_DELAY);
        String strFrom = command.getOption(OPTION_FROM);
        String strTo = command.getOption(OPTION_TO);
        AbstractRecoveryTarget target =getTarget(process, command.getOption(OPTION_TARGET));
        
        boolean keepDeletedEntries = (
                command.getOption(OPTION_KEEP_DELETED_ENTRIES) != null
                && command.getOption(OPTION_KEEP_DELETED_ENTRIES).trim().length() != 0
        );
        
        if (strDelay != null || strFrom != null || strTo != null) {
            // A delay (in days) is provided
            int from = 0;
            if (strFrom != null) {
                from = Integer.parseInt(strFrom);
            }
            
            int to = 0;
            if (strTo != null) {
                to = Integer.parseInt(strTo);
            } else if (strDelay != null) {
                to = Integer.parseInt(strDelay);
            }
            
            process.processCompactOnTarget(
                    target,
                    from, 
                    to,
                    keepDeletedEntries,
                    context
            );
        } else {
            // A full date is provided
            process.processCompactOnTarget(
                    target,
                    null,
                    CalendarUtils.resolveDate(command.getOption(OPTION_DATE), null),
                    keepDeletedEntries,
                    null,
                    context
            );
        }
    }
    
    /**
     * Merges the archives.
     *
     * @param command
     * @param process
     */
    private static void processDelete(UserCommandLine command, RecoveryProcess process, ProcessContext context) throws Exception {
        String strDelay = command.getOption(OPTION_DELAY);
        AbstractRecoveryTarget target =getTarget(process, command.getOption(OPTION_TARGET));
        if (strDelay != null) {
            // A delay (in days) is provided
            process.processDeleteOnTarget(
                    target,
                    Integer.parseInt(strDelay),
                    context
            );
        } else {
            // A full date is provided
            process.processDeleteOnTarget(
                    target,
                    CalendarUtils.resolveDate(command.getOption(OPTION_DATE), null),
                    context
            );
        }
    }
    
    /**
     * Processus de chargement d'une archive
     *
     * @param command
     * @param process
     */
    private static void processRecover(UserCommandLine command, RecoveryProcess process, ProcessContext context) throws Exception {
        AbstractRecoveryTarget target =getTarget(process, command.getOption(OPTION_TARGET));
        
        String destination = command.getOption(OPTION_DESTINATION);
        if (FileNameUtil.endsWithSeparator(destination)) {
            destination = destination.substring(0, destination.length() - 1);
        }
        process.processRecoverOnTarget(
                target,
                null,
                destination,
                CalendarUtils.resolveDate(command.getOption(OPTION_DATE), null),
                false, 
                context
        );
    }
    
    /**
     * Processus de description d'un fichier de config
     *
     * @param command
     * @param process
     */
    private static void processDescribe(UserCommandLine command, RecoveryProcess process, ProcessContext context) throws Exception {
        channel.print(process.getDescription());
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
}

