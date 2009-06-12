package com.application.areca.launcher.tui;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.application.areca.AbstractArecaLauncher;
import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ArecaFileConstants;
import com.application.areca.ArecaTechnicalConfiguration;
import com.application.areca.TargetGroup;
import com.application.areca.UserInformationChannel;
import com.application.areca.adapters.ProcessXMLReader;
import com.application.areca.context.ProcessContext;
import com.application.areca.metadata.manifest.Manifest;
import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemManager;
import com.myJava.util.CalendarUtils;
import com.myJava.util.log.ConsoleLogProcessor;
import com.myJava.util.log.FileLogProcessor;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
 * Launcher
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

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
    private UserInformationChannel channel;
    
	static {
		AbstractArecaLauncher.setInstance(new Launcher());
	}
    
    public static void main(String[] args) {
        Launcher launcher = new Launcher();
        launcher.launch(args);
        launcher.exit();
    }
    
	protected boolean returnErrorCode() {
		return true;
	}
    
    protected void launchImpl(String[] args) {
		channel = new LoggerUserInformationChannel(false);
        UserCommandLine command = null;
        try {
            command = new UserCommandLine(args);
            command.parse();
            
            // On logge dans le repertoire du fichier de config
            if (command.hasOption(OPTION_CONFIG)) {
                File f = new File(command.getOption(OPTION_CONFIG));
                if (FileSystemManager.exists(f)) {
                    Logger.defaultLogger().remove(FileLogProcessor.class);
                    Logger.defaultLogger().remove(ConsoleLogProcessor.class);
                    File configFile = new File(command.getOption(OPTION_CONFIG));
                    
                    String configName = FileSystemManager.getName(configFile);
                    if (configName.endsWith(".xml")) {
                    	configName = configName.substring(0, configName.length() - 4);
                    }
                    
                    FileLogProcessor proc;
                    if (ArecaTechnicalConfiguration.get().getLogLocationOverride() == null) {
                        File logDir = new File(FileSystemManager.getParentFile(configFile), ArecaFileConstants.LOG_SUBDIRECTORY_NAME);
                    	proc = new FileLogProcessor(new File(logDir, configName));
                    } else {
                    	proc = new FileLogProcessor(new File(ArecaTechnicalConfiguration.get().getLogLocationOverride(), configName));
                    }
        	        Logger.defaultLogger().addProcessor(proc);
                }
            }
            
            ProcessXMLReader adapter = new ProcessXMLReader(new File(command.getOption(OPTION_CONFIG)));
            adapter.setMissingDataListener(new MissingDataListener());
            TargetGroup process = adapter.load();
            AbstractRecoveryTarget target = null;
            String suffix = ".";
            if (command.hasOption(OPTION_TARGET)) {
                target = getTarget(process, command.getOption(OPTION_TARGET));
                suffix = " - target = [" + target.getTargetName() + "].";
            }
            ProcessContext context = new ProcessContext(target, channel, new TaskMonitor("tui-main"));
            
            context.getReport().setLogMessagesContainer(Logger.defaultLogger().getTlLogProcessor().activateMessageTracking());
            
            Logger.defaultLogger().info("Starting the process ... config = [" + command.getOption(OPTION_CONFIG) + "]" + suffix);
            channel.print("Starting the process ... config = [" + command.getOption(OPTION_CONFIG) + "]" + suffix);
            
            if (command.getCommand().equalsIgnoreCase(COMMAND_MERGE.getName())) {
                processMerge(command, process, context);
            } else if (command.getCommand().equalsIgnoreCase(COMMAND_RECOVER.getName())) {
                processRecover(command, process, context);
            } else if (command.getCommand().equalsIgnoreCase(COMMAND_BACKUP.getName())) {
                processBackup(command, process, context);
            } else if (command.getCommand().equalsIgnoreCase(COMMAND_DESCRIBE.getName())) {
                processDescribe(command, process, context);
            } else if (command.getCommand().equalsIgnoreCase(COMMAND_DELETE.getName())) {
                processDelete(command, process, context);
            } else if (command.getCommand().equalsIgnoreCase(COMMAND_CHECK.getName())) {
                processCheck(command, process, context);
            }
            
            Logger.defaultLogger().info("End of process.");
            channel.print("End of process.");
            
        } catch (InvalidCommandException e) {
        	setErrorCode(ERR_SYNTAX); // Syntax error
        	printHelp();
            channel.print(command.toString());
            channel.print("Error : invalid arguments (" + e.getMessage() + ")");
        } catch (Throwable e) {
            handleError(e);
        }
    }
    
    private void printHelp() {
        channel.print(SEPARATOR);
        channel.print("Areca Backup");
        channel.print("Copyright 2005-2009, Olivier PETRUCCI");
        channel.print("List of valid arguments :");
        channel.print("");
        channel.print("Describe the targets :");
        channel.print("      describe -config (your xml config file)");
        
        channel.print("");
        channel.print("Launch a backup :");
        channel.print("      backup -config (your xml config file) [-f] [-d] [-c] [-s] [-target (specific target)] [-title (archive title)]");
        channel.print("         -f to force full backup (instead of incremental backup)");
        channel.print("         -d to force differential backup (instead of incremental backup)");
        channel.print("         -c to check the archive consistency after backup");
        channel.print("         -s to disable asynchronous processing when handling a target group");        
        channel.print("         -title to set a title to the archive");         

        channel.print("");
        channel.print("Merge archives :");
        channel.print("      merge -config (your xml config file) -target (specific target) [-title (archive title)] [-k] -date (merged date : YYYY-MM-DD) / -from (nr of days - 0='-infinity') -to (nr of days - 0='today')");
        channel.print("         -k to keep deleted files in the merged archive");    
        channel.print("         -title to set a title to the archive");   
        channel.print("         -date to specify the reference date used for merging");
        channel.print("         OR -from/-to (nr of days) to specify the archive range used for merging");
        
        channel.print("");
        channel.print("Delete archives :");
        channel.print("      delete -config (your xml config file) -target (specific target) [-date (deletion date : YYYY-MM-DD) / -delay (nr of days)]");
        
        channel.print("");
        channel.print("Recover archives :");
        channel.print("      recover -config (your xml config file) -target (specific target) -destination (destination folder) [-date (recovered date : YYYY-MM-DD)] [-c]");
        channel.print("         -c to check consistency of recovered files");     
        channel.print("         -date to specify the recovery date");
        
        channel.print("");
        channel.print("Check archives :");
        channel.print("      check -config (your xml config file) -target (specific target) [-destination (destination folder)] [-date (checked date : YYYY-MM-DD)] [-a]");
        channel.print("         -a to check all files (not only those contained in the archive denoted by the date argument)");     
        channel.print("         -date to specify the archive which will be checked");
     
        channel.print("");
        channel.print(SEPARATOR);
    }
    
    /**
     * Error handling.
     * <BT>Set the error code.
     */
    private void handleError(Throwable e) {
    	setErrorCode(ERR_UNEXPECTED);
        channel.print(SEPARATOR);
        channel.print("An error occurred during the process : " + e.getMessage());
        if (((FileLogProcessor)Logger.defaultLogger().find(FileLogProcessor.class)) != null) {
            channel.print("Please refer to the log file : " + ((FileLogProcessor)Logger.defaultLogger().find(FileLogProcessor.class)).getCurrentLogFile());
        }
        channel.print(SEPARATOR);
        
        // Log all !
        Logger.defaultLogger().error(e);
    }
    
    /**
     * Backup
     */
    private void processBackup(UserCommandLine command, final TargetGroup process, final ProcessContext context) 
    throws Exception {
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
        
        final boolean disableCheck = ! (
                command.getOption(OPTION_CHECK_FILES) != null
                && command.getOption(OPTION_CHECK_FILES).trim().length() != 0
        );
        
        final boolean forceSync = (
                command.getOption(OPTION_SYNC) != null
                && command.getOption(OPTION_SYNC).trim().length() != 0
        );
        
        final Manifest manifest;
        if (command.hasOption(OPTION_TITLE)) {
        	manifest = new Manifest(Manifest.TYPE_BACKUP);
        	manifest.setTitle(command.getOption(OPTION_TITLE));
        } else {
        	manifest = null;
        }
        
        if (command.hasOption(OPTION_TARGET)) {
            AbstractRecoveryTarget target = getTarget(process, command.getOption(OPTION_TARGET));            
            process.processBackupOnTarget(
                    target,
                    manifest,
                    backupScheme,
                    false,
                    disableCheck,
                    context
            );
        } else {
            List thList = new ArrayList();
            
            Iterator iter = process.getTargetIterator();
            while (iter.hasNext()) {
                final AbstractRecoveryTarget tg = (AbstractRecoveryTarget)iter.next();
                final ProcessContext cloneCtx = new ProcessContext(tg, new LoggerUserInformationChannel(!forceSync), new TaskMonitor("tui-clone"));
                Runnable rn = new Runnable() {
                    public void run() {
                        try {
                            
                        	cloneCtx.getReport().setLogMessagesContainer(Logger.defaultLogger().getTlLogProcessor().activateMessageTracking());
                            process.processBackupOnTarget(
                                    tg,
                                    manifest,
                                    backupScheme,
                                    false,
                                    disableCheck,
                                    cloneCtx
                            );
                        } catch (Exception e) {
                            handleError(e);
                        }
                    }
                };
                
                if (forceSync) {
                	// Sync mode
                	rn.run();
                	
                	// Clear log for the next target.
                    Logger.defaultLogger().getTlLogProcessor().clearLog();
                } else {
                	// Async mode
	                Thread th = new Thread(rn);
	                th.setName("Backup on " + tg.getTargetName());
	                th.setDaemon(false);
	                thList.add(th);
	                th.start();
                }
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
     */
    private void processMerge(UserCommandLine command, TargetGroup process, ProcessContext context) 
    throws Exception {
        String strDelay = command.getOption(OPTION_DELAY);
        String strFrom = command.getOption(OPTION_FROM);
        String strTo = command.getOption(OPTION_TO);
        AbstractRecoveryTarget target =getTarget(process, command.getOption(OPTION_TARGET));
        
        boolean keepDeletedEntries = (
                command.getOption(OPTION_KEEP_DELETED_ENTRIES) != null
                && command.getOption(OPTION_KEEP_DELETED_ENTRIES).trim().length() != 0
        );
        
        final Manifest manifest;
        if (command.hasOption(OPTION_TITLE)) {
        	manifest = new Manifest(Manifest.TYPE_MERGE);
        	manifest.setTitle(command.getOption(OPTION_TITLE));
        } else {
        	manifest = null;
        }
        
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
            
            process.processMergeOnTarget(
                    target,
                    from, 
                    to,
                    manifest,
                    keepDeletedEntries, 
                    context
            );
        } else {
            // A full date is provided
            process.processMergeOnTarget(
                    target,
                    null,
                    CalendarUtils.resolveDate(command.getOption(OPTION_DATE), null),
                    manifest,
                    keepDeletedEntries, 
                    context
            );
        }
    }
    
    /**
     * Delete the archives.
     */
    private void processDelete(UserCommandLine command, TargetGroup process, ProcessContext context) 
    throws Exception {
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
     * Recovery
     */
    private void processRecover(UserCommandLine command, TargetGroup process, ProcessContext context) 
    throws Exception {
        AbstractRecoveryTarget target =getTarget(process, command.getOption(OPTION_TARGET));
        
        String destination = command.getOption(OPTION_DESTINATION);
        if (FileNameUtil.endsWithSeparator(destination)) {
            destination = destination.substring(0, destination.length() - 1);
        }
        
        boolean checkRecoveredFiles = (
                command.getOption(OPTION_CHECK_FILES) != null
                && command.getOption(OPTION_CHECK_FILES).trim().length() != 0
        );
        
        process.processRecoverOnTarget(
                target,
                null,
                destination,
                CalendarUtils.resolveDate(command.getOption(OPTION_DATE), null),
                false, 
                checkRecoveredFiles,
                context
        );
    }

    private void processCheck(UserCommandLine command, TargetGroup process, ProcessContext context) 
    throws Exception {
        AbstractRecoveryTarget target =getTarget(process, command.getOption(OPTION_TARGET));
        
        String destination = command.getOption(OPTION_DESTINATION);
        if (destination == "") {
        	destination = null;
        }
        if (destination != null && FileNameUtil.endsWithSeparator(destination)) {
            destination = destination.substring(0, destination.length() - 1);
        }
        
        boolean checkAll = (
                command.getOption(OPTION_CHECK_ALL) != null
                && command.getOption(OPTION_CHECK_ALL).trim().length() != 0
        );
        
        process.processCheckOnTarget(
                target,
                destination,
                ! checkAll,
                CalendarUtils.resolveDate(command.getOption(OPTION_DATE), null),
                context
        );
        
        if (context.getInvalidRecoveredFiles().size() != 0) {
        	context.getInfoChannel().warn("Some errors were found (see above).");
        	setErrorCode(ERR_INVALID_ARCHIVE); // Syntax error
        } else if (context.getRecoveryDestination() != null) {
        	String suffix = "";
        	if (checkAll) {
        		suffix = "s";
        	}
        	context.getInfoChannel().print("Archive" + suffix + " successfully checked.");
        }
    }
    
    /**
     * Description
     */
    private void processDescribe(UserCommandLine command, TargetGroup process, ProcessContext context) 
    throws Exception {
        channel.print("\n" + process.getDescription());
    }
    
    private AbstractRecoveryTarget getTarget(TargetGroup process, String targetId) 
    throws InvalidCommandException {
        AbstractRecoveryTarget target = process.getTargetById(Integer.parseInt(targetId));
        if (target == null) {
            throw new InvalidCommandException("invalid target ID : " + targetId);
        } else {
            return target;
        }
    }
}

