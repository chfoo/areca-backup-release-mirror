package com.application.areca.launcher.tui;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import com.application.areca.AbstractTarget;
import com.application.areca.ActionProxy;
import com.application.areca.ArecaConfiguration;
import com.application.areca.ArecaFileConstants;
import com.application.areca.CheckParameters;
import com.application.areca.MergeParameters;
import com.application.areca.TargetGroup;
import com.application.areca.UserInformationChannel;
import com.application.areca.WorkspaceItem;
import com.application.areca.adapters.ConfigurationHandler;
import com.application.areca.context.ProcessContext;
import com.application.areca.impl.FileSystemTarget;
import com.application.areca.impl.copypolicy.AbstractCopyPolicy;
import com.application.areca.impl.copypolicy.AlwaysOverwriteCopyPolicy;
import com.application.areca.impl.copypolicy.NeverOverwriteCopyPolicy;
import com.application.areca.launcher.AbstractArecaLauncher;
import com.application.areca.launcher.ArecaUserPreferences;
import com.application.areca.launcher.LocalPreferences;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.transaction.ConditionalTransactionHandler;
import com.application.areca.metadata.transaction.NoTransactionHandler;
import com.application.areca.metadata.transaction.TransactionHandler;
import com.application.areca.metadata.transaction.YesTransactionHandler;
import com.application.areca.version.VersionInfos;
import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.util.CalendarUtils;
import com.myJava.util.log.ConsoleLogProcessor;
import com.myJava.util.log.FileLogProcessor;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskMonitor;
import com.myJava.util.xml.AdapterException;

/**
 * Launcher
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
public class Launcher
extends AbstractArecaLauncher
implements CommandConstants {
	private UserInformationChannel channel;
	private static final int CURRENT_YEAR = VersionInfos.getLastVersion().getYear();

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
		Logger.defaultLogger().remove(ConsoleLogProcessor.class);
		UserCommandLine command = null;
		try {
			try {
		    	ArecaUserPreferences.initialize(System.getProperty("user.home"));
				ArecaUserPreferences.setLaunchCount(ArecaUserPreferences.getLaunchCount() + 1);
				LocalPreferences.instance().save();
			} catch (Exception ignored) {
			}
			command = new UserCommandLine(args);
			command.parse();

			// Log in the config file's parent directory
			if (command.hasOption(OPTION_CONFIG)) {
				File configFile = new File(command.getOption(OPTION_CONFIG));
				File parentFile = FileSystemManager.getParentFile(configFile);
				FileTool.getInstance().createDir(parentFile);
				Logger.defaultLogger().remove(FileLogProcessor.class);

				String configName = FileSystemManager.getName(configFile);
				if (configName.endsWith(FileSystemTarget.CONFIG_FILE_EXT_DEPRECATED)) {
					configName = configName.substring(0, configName.length() - FileSystemTarget.CONFIG_FILE_EXT_DEPRECATED.length());
				} else if (configName.endsWith(FileSystemTarget.CONFIG_FILE_EXT)) {
					configName = configName.substring(0, configName.length() - FileSystemTarget.CONFIG_FILE_EXT.length());
				}

				FileLogProcessor proc;
				if (ArecaConfiguration.get().getLogLocationOverride() == null) {
					File logDir = new File(parentFile, ArecaFileConstants.LOG_SUBDIRECTORY_NAME);
					proc = new FileLogProcessor(new File(logDir, configName));
				} else {
					proc = new FileLogProcessor(new File(ArecaConfiguration.get().getLogLocationOverride(), configName));
				}
				Logger.defaultLogger().addProcessor(proc);
			}
			
			WorkspaceItem item = null;
			
			if (! command.getCommand().equalsIgnoreCase(COMMAND_INFOS.getName())) {
				item = getItem(command);

				Logger.defaultLogger().info("Configuration path : " + command.getOption(OPTION_CONFIG));
				channel.print("Configuration path : " + command.getOption(OPTION_CONFIG) );

				if (item instanceof AbstractTarget) {
					Logger.defaultLogger().info("Target : " + item.getName());
					channel.print("Target : " + item.getName());
				}
			}

			if (command.getCommand().equalsIgnoreCase(COMMAND_MERGE.getName())) {
				processMerge(command, item);
			} else if (command.getCommand().equalsIgnoreCase(COMMAND_RECOVER.getName())) {
				processRecover(command, item);
			} else if (command.getCommand().equalsIgnoreCase(COMMAND_BACKUP.getName())) {
				processBackup(command, item);
			} else if (command.getCommand().equalsIgnoreCase(COMMAND_DESCRIBE.getName())) {
				processDescribe(command, item);
			} else if (command.getCommand().equalsIgnoreCase(COMMAND_INFOS.getName())) {
				processInfos(command);
			} else if (command.getCommand().equalsIgnoreCase(COMMAND_DELETE.getName())) {
				processDelete(command, item);
			} else if (command.getCommand().equalsIgnoreCase(COMMAND_CHECK.getName())) {
				processCheck(command, item);
			}

			Logger.defaultLogger().info("End of process.");
		} catch (InvalidCommandException e) {
			setErrorCode(ERR_SYNTAX); // Syntax error
			printHelp();
			channel.print(command.toString());
			channel.print("Error : invalid arguments (" + e.getMessage() + ")");
		} catch (Throwable e) {
			handleError(e);
		}
	}

	private ProcessContext buildContext(WorkspaceItem item) {
		ProcessContext context = new ProcessContext((item instanceof AbstractTarget) ? (AbstractTarget)item : null, channel, new TaskMonitor("tui-main"));
		context.getReport().setLogMessagesContainer(Logger.defaultLogger().getTlLogProcessor().activateMessageTracking());

		return context;
	}

	/**
	 * This method should be very simple, but ensuring backward compatibility makes it complicated :/
	 */
	private WorkspaceItem getItem(UserCommandLine command) throws InvalidCommandException, AdapterException {
		File config = new File(command.getOption(OPTION_CONFIG));
		String targetUID = null; // Backward compatibility

		if (! FileSystemManager.exists(config)) {
			// Configuration file not found. 2 cases :
			// - case 1 : New format configuration file and old format backup command
			// - case 2 : Old format configuration file and new format backup command

			String configPath = FileSystemManager.getAbsolutePath(config);
			if (configPath.endsWith(FileSystemTarget.CONFIG_FILE_EXT_DEPRECATED)) {

				// - case 1 : New format configuration file and old format backup command
				// => transcode the old configuration file name to the new format (configuration directory)
				File newFormatConfig = new File(configPath.substring(0, configPath.length() - FileSystemTarget.CONFIG_FILE_EXT_DEPRECATED.length()));
				channel.print(FileSystemManager.getDisplayPath(config) + " has been migrated. Switching to " + FileSystemManager.getDisplayPath(newFormatConfig) + ".");
				config = newFormatConfig;

			} else {

				// - case 2 : Old format configuration file and new format backup command
				// => transcode the new configuration file name to the old format (configuration xml file)
				File newFormatConfig;
				if (configPath.endsWith(FileSystemTarget.CONFIG_FILE_EXT)) {
					newFormatConfig = FileSystemManager.getParentFile(config);
					String name = FileSystemManager.getName(config);
					targetUID = name.substring(0, name.length() - FileSystemTarget.CONFIG_FILE_EXT.length());
				} else {
					newFormatConfig = config;
				}

				String newPath = FileSystemManager.getAbsolutePath(newFormatConfig);
				if (newPath.endsWith("/") || newPath.endsWith("\\")) {
					newPath = newPath.substring(0, newPath.length() - 1);
				}
				newPath += FileSystemTarget.CONFIG_FILE_EXT_DEPRECATED;

				channel.print(FileSystemManager.getAbsolutePath(config) + " does not exist (not migrated to new configuration format yet). Switching to " + newPath + " with target uid : " + targetUID + ".");
				config = new File(newPath);

			}
		}

		WorkspaceItem item = ConfigurationHandler.getInstance().readObject(config, new MissingDataListener(), null, true, true);
		if ((item instanceof TargetGroup) && command.hasOption(OPTION_TARGET)) {
			String id = command.getOption(OPTION_TARGET);
			AbstractTarget target = ((TargetGroup)item).getTarget(Integer.parseInt(id));
			if (target == null) {
				throw new InvalidCommandException("Invalid target ID : " + id);
			} else {
				return target;
			}
		} else if ((item instanceof TargetGroup) && targetUID != null) {
			AbstractTarget target = (AbstractTarget)((TargetGroup)item).getItem(targetUID);
			if (target == null) {
				throw new InvalidCommandException("Invalid target UID : " + targetUID);
			} else {
				return target;
			}
		} else if (item != null){
			return item;
		} else {
			throw new InvalidCommandException("Target or target group not found : " + FileSystemManager.getDisplayPath(config));
		}
	}

	private void printHelp() {
		channel.print(SEPARATOR);
		channel.print(VersionInfos.APP_NAME);
		channel.print("Copyright 2005-" + CURRENT_YEAR + ", Olivier PETRUCCI");
		channel.print("List of valid arguments :");
		
		channel.print("");
		channel.print("Show informations about Areca :");
		channel.print("      infos");
		
		channel.print("");
		channel.print("Describe targets :");
		channel.print("      describe -config (xml configuration file or directory)");

		channel.print("");
		channel.print("Launch a backup :");
		channel.print("      backup -config (xml configuration file or directory) [-f] [-d] [-c] [-wdir (working directory)] [-s] [-title (archive title)]");
		channel.print("         -f to force full backup (instead of incremental backup)");
		channel.print("         -d to force differential backup (instead of incremental backup)");
		channel.print("         -c to check the archive consistency after backup");
		channel.print("         -wdir to use a specific working directory during archive check");
		channel.print("         -resume to resume a pending backup if found");
		channel.print("         -cresume (nb days) to resume a pending backup if younger than 'nb days'");
		channel.print("         -s to disable asynchronous processing when handling a target group");        
		channel.print("         -title to set a title to the archive");         

		channel.print("");
		channel.print("Merge archives :");
		channel.print("      merge -config (xml configuration file) [-title (archive title)] [-k] -date (merged date : YYYY-MM-DD) / -from (nr of days - 0='-infinity') -to (nr of days - 0='today')");
		channel.print("         -k to keep deleted files in the merged archive");    
		channel.print("         -title to set a title to the archive");   
		channel.print("         -c to check the archive consistency after merge");
		channel.print("         -wdir to use a specific working directory");
		channel.print("         -date to specify the reference date used for merging");
		channel.print("         OR -from/-to (nr of days) to specify the archive range used for merging");

		channel.print("");
		channel.print("Delete archives :");
		channel.print("      delete -config (xml configuration file) [-date (deletion date : YYYY-MM-DD) / -delay (nr of days)]");

		channel.print("");
		channel.print("Recover archives :");
		channel.print("      recover -config (xml configuration file) -destination (destination folder) [-date (recovered date : YYYY-MM-DD)] [-c]");
		channel.print("         -c to check consistency of recovered files");   
		channel.print("         -o to overwrite existing files");  
		channel.print("         -nosubdir to prevent Areca to perform the recovery in a subdirectory"); 
		channel.print("         -date to specify the recovery date");

		channel.print("");
		channel.print("Check archives :");
		channel.print("      check -config (xml configuration file) [-wdir (working directory)] [-date (checked date : YYYY-MM-DD)] [-a]");
		channel.print("         -wdir to use a specific working directory");
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
	private void processBackup(UserCommandLine command, final WorkspaceItem item) 
	throws Exception {
		List threadContainer = new ArrayList();

		// Launch backup
		processBackup(command, item, threadContainer);

		// Wait for all threads to die
		Iterator thIter = threadContainer.iterator();
		while (thIter.hasNext()) {
			Thread th = (Thread) thIter.next();
			th.join();
		}
	}

	private void processBackup(UserCommandLine command, final WorkspaceItem item, List threadContainer) 
	throws Exception {
		final String backupScheme;
		String fOption = command.getOption(OPTION_FULL_BACKUP);
		String dOption = command.getOption(OPTION_DIFFERENTIAL_BACKUP);        
		if (fOption != null && fOption.trim().length() != 0) {
			backupScheme = AbstractTarget.BACKUP_SCHEME_FULL;
		} else if (dOption != null && dOption.trim().length() != 0) {
			backupScheme = AbstractTarget.BACKUP_SCHEME_DIFFERENTIAL;
		} else {
			backupScheme = AbstractTarget.BACKUP_SCHEME_INCREMENTAL;
		}

		final boolean forceSync = (
				command.getOption(OPTION_SYNC) != null
				&& command.getOption(OPTION_SYNC).trim().length() != 0
		);

		boolean resume = (
				command.getOption(OPTION_RESUME) != null
				&& command.getOption(OPTION_RESUME).trim().length() != 0
		);

		boolean conditionalResume = (
				command.getOption(OPTION_RESUME_CONDITIONAL) != null
				&& command.getOption(OPTION_RESUME_CONDITIONAL).trim().length() != 0
		);
		int resumeLimit = -1;
		if (conditionalResume) {
			resumeLimit = Integer.parseInt(command.getOption(OPTION_RESUME_CONDITIONAL));
		}

		final Manifest manifest;
		if (command.hasOption(OPTION_TITLE)) {
			manifest = new Manifest(Manifest.TYPE_BACKUP);
			manifest.setTitle(command.getOption(OPTION_TITLE));
		} else {
			manifest = null;
		}

		String destination = normalizePath(command.getOption(OPTION_SPEC_LOCATION));

		final CheckParameters checkParams = new CheckParameters(
				command.getOption(OPTION_CHECK_FILES) != null && command.getOption(OPTION_CHECK_FILES).trim().length() != 0,
				true,
				true,
				destination != null,
				destination
		);

		if (item instanceof AbstractTarget) {

			// Initialize transaction handler
			final TransactionHandler handler;
			if (resume) {
				handler = new YesTransactionHandler();
			} else if (conditionalResume) {
				handler = new ConditionalTransactionHandler(resumeLimit);
			} else {
				handler = new NoTransactionHandler();
			}

			// Create runnable
			Runnable rn = new Runnable() {
				public void run() {
					try {
						ActionProxy.processBackupOnTarget(
								(AbstractTarget)item,
								manifest,
								backupScheme,
								checkParams,
								handler,
								buildContext(item)
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
				th.setName("Backup on " + item.getName());
				th.setDaemon(false);
				threadContainer.add(th);
				th.start();
			}
		} else {
			TargetGroup group = (TargetGroup)item;

			Iterator iter = group.getIterator();
			while (iter.hasNext()) {
				processBackup(command, (WorkspaceItem)iter.next(), threadContainer);
			}
		}
	}

	/**
	 * Merges the archives.
	 */
	private void processMerge(UserCommandLine command, WorkspaceItem item) 
	throws Exception {
		ProcessContext context = buildContext(item);

		String strDelay = command.getOption(OPTION_DELAY);
		String strFrom = command.getOption(OPTION_FROM);
		String strTo = command.getOption(OPTION_TO);

		AbstractTarget target = null;
		if (item instanceof AbstractTarget) {
			target = (AbstractTarget)item;
		} else {
			throw new InvalidCommandException("Merge can only be performed on individual targets.");
		}

		final Manifest manifest;
		if (command.hasOption(OPTION_TITLE)) {
			manifest = new Manifest(Manifest.TYPE_MERGE);
			manifest.setTitle(command.getOption(OPTION_TITLE));
		} else {
			manifest = null;
		}

		String destination = normalizePath(command.getOption(OPTION_SPEC_LOCATION));

		final CheckParameters checkParams = new CheckParameters(
				command.getOption(OPTION_CHECK_FILES) != null && command.getOption(OPTION_CHECK_FILES).trim().length() != 0,
				true,
				true,
				destination != null,
				destination
		);
		
		boolean keepDeletedEntries = (
				command.getOption(OPTION_KEEP_DELETED_ENTRIES) != null
				&& command.getOption(OPTION_KEEP_DELETED_ENTRIES).trim().length() != 0
		);

		MergeParameters params = new MergeParameters(keepDeletedEntries, destination != null, destination);
		
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

			ActionProxy.processMergeOnTarget(
					target,
					from, 
					to,
					manifest,
					params, 
					checkParams,
					context
			);
		} else {
			// A full date is provided
			GregorianCalendar date = CalendarUtils.resolveDate(command.getOption(OPTION_DATE), null);
			adjustDate(date, true);
			
			ActionProxy.processMergeOnTarget(
					target,
					null,
					date,
					manifest,
					params, 
					checkParams,
					context
			);
		}
	}
	
	private static void adjustDate(Calendar date, boolean isToDate) {
		// No specific time provided : the whole day will be included
		if (
				isToDate 
				&& date != null
				&& date.get(Calendar.HOUR_OF_DAY) == 0 
				&& date.get(Calendar.MINUTE) == 0 
				&& date.get(Calendar.SECOND) == 0
		) {
			date.add(Calendar.DATE, 1);
			date.add(Calendar.MILLISECOND, -10);
		}
	}

	/**
	 * Delete the archives.
	 */
	private void processDelete(UserCommandLine command, WorkspaceItem item) 
	throws Exception {
		AbstractTarget target = null;
		if (item instanceof AbstractTarget) {
			target = (AbstractTarget)item;
		} else {
			throw new InvalidCommandException("Deletion can only be performed on individual targets.");
		}

		ProcessContext context = buildContext(item);

		String strDelay = command.getOption(OPTION_DELAY);
		if (strDelay != null) {
			// A delay (in days) is provided
			ActionProxy.processDeleteOnTarget(
					target,
					Integer.parseInt(strDelay),
					context
			);
		} else {
			GregorianCalendar date = CalendarUtils.resolveDate(command.getOption(OPTION_DATE), null);
			adjustDate(date, false);
			
			// A full date is provided
			ActionProxy.processDeleteOnTarget(
					target,
					date,
					context
			);
		}
	}

	/**
	 * Recovery
	 */
	private void processRecover(UserCommandLine command, WorkspaceItem item) 
	throws Exception {
		AbstractTarget target = null;
		if (item instanceof AbstractTarget) {
			target = (AbstractTarget)item;
		} else {
			throw new InvalidCommandException("Recovery can only be performed on individual targets.");
		}

		String destination = normalizePath(command.getOption(OPTION_DESTINATION));

		boolean checkRecoveredFiles = (
				command.getOption(OPTION_CHECK_FILES) != null
				&& command.getOption(OPTION_CHECK_FILES).trim().length() != 0
		);

		ProcessContext context = buildContext(item);

		boolean noSubDir = (
				command.getOption(OPTION_NO_SUBDIR) != null
				&& command.getOption(OPTION_NO_SUBDIR).trim().length() != 0
		);

		boolean overwrite = (
				command.getOption(OPTION_OVERWRITE) != null
				&& command.getOption(OPTION_OVERWRITE).trim().length() != 0
		);

		AbstractCopyPolicy policy;
		if (overwrite) {
			policy = new AlwaysOverwriteCopyPolicy();
		} else {
			policy = new NeverOverwriteCopyPolicy(); 	
		}
		policy.setContext(context);

		GregorianCalendar date = CalendarUtils.resolveDate(command.getOption(OPTION_DATE), null);
		adjustDate(date, true);

		ActionProxy.processRecoverOnTarget(
				target,
				null,
				policy,
				destination,
				! noSubDir,
				date,
				false, 
				checkRecoveredFiles,
				context
		);
	}

	private void processCheck(UserCommandLine command, WorkspaceItem item) 
	throws Exception {
		AbstractTarget target = null;
		if (item instanceof AbstractTarget) {
			target = (AbstractTarget)item;
		} else {
			throw new InvalidCommandException("Archive verification can only be performed on individual targets.");
		}

		String destination = normalizePath(command.getOption(OPTION_DESTINATION));
		if (destination == null) {
			destination = normalizePath(command.getOption(OPTION_SPEC_LOCATION));
		}

		boolean checkAll = (
				command.getOption(OPTION_CHECK_ALL) != null
				&& command.getOption(OPTION_CHECK_ALL).trim().length() != 0
		);

		ProcessContext context = buildContext(item);

		final CheckParameters checkParams = new CheckParameters(
				true,
				true,
				! checkAll,
				destination != null,
				destination
		);
		
		GregorianCalendar date = CalendarUtils.resolveDate(command.getOption(OPTION_DATE), null);
		adjustDate(date, true);
		
		ActionProxy.processCheckOnTarget(
				target,
				checkParams,
				date,
				context
		);

		if (context.getReport().hasRecoveryIssues()) {
			context.getInfoChannel().warn("Some errors were found (see above).");
			setErrorCode(ERR_INVALID_ARCHIVE); 
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
	private void processDescribe(UserCommandLine command, WorkspaceItem item) 
	throws Exception {
		channel.print("\n" + item.getDescription());
	}
	
	/**
	 * Infos
	 */
	private void processInfos(UserCommandLine command) 
	throws Exception {
		channel.print("Version : " + VersionInfos.getLastVersion().getVersionId());
		channel.print("Build Id : " + VersionInfos.getBuildId());
		channel.print("Maximum available memory (bytes) : " + Runtime.getRuntime().maxMemory());
		channel.print("JRE : " + System.getProperty("sun.boot.library.path"));
	}

	private String normalizePath(String path) {
		if (path == null) {
			return null;
		}
		if (path.length() == 0) {
			path = null;
		}
		if (path != null && FileNameUtil.endsWithSeparator(path)) {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}
}

