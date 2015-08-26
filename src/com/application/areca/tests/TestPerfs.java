package com.application.areca.tests;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.application.areca.AbstractTarget;
import com.application.areca.ActionProxy;
import com.application.areca.ArecaConfiguration;
import com.application.areca.CheckParameters;
import com.application.areca.MergeParameters;
import com.application.areca.TargetGroup;
import com.application.areca.WorkspaceItem;
import com.application.areca.adapters.ConfigurationHandler;
import com.application.areca.cache.ArchiveManifestCache;
import com.application.areca.context.ProcessContext;
import com.application.areca.filter.DirectoryArchiveFilter;
import com.application.areca.filter.FileExtensionArchiveFilter;
import com.application.areca.filter.FileSizeArchiveFilter;
import com.application.areca.filter.FilterGroup;
import com.application.areca.filter.RegexArchiveFilter;
import com.application.areca.impl.AbstractFileSystemMedium;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.impl.FileSystemTarget;
import com.application.areca.impl.copypolicy.AlwaysOverwriteCopyPolicy;
import com.application.areca.launcher.tui.LoggerUserInformationChannel;
import com.application.areca.launcher.tui.MissingDataListener;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.metadata.transaction.YesTransactionHandler;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.driver.DefaultFileSystemDriver;
import com.myJava.util.CalendarUtils;
import com.myJava.util.log.LogMessagesContainer;
import com.myJava.util.log.Logger;
import com.myJava.util.log.ThreadLocalLogProcessor;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
 * 
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
public class TestPerfs {
	public static LoggerUserInformationChannel CHANNEL = new LoggerUserInformationChannel(false);
	private static String CURRENT_TEST = "";
	public static String CURRENT_TARGET = "";
	public static boolean SUCCESS = false;
	private static long START = System.currentTimeMillis();
	
	public static String RES = "target;uid;sync;async";

	public static final String SOURCES_S = ".source_files";
	public static final String RECOVERY_DIR_S = ".recovery_dir";
	
	public static TargetGroup load(String path, String source) throws Exception {
		log("Loading targets ...");
		WorkspaceItem item = ConfigurationHandler.getInstance().readObject(new File(path), new MissingDataListener(), null, true, true);
		overrideSource(item, source);
		return (TargetGroup)item;
	}
	
	private static void overrideSource(WorkspaceItem item, final String src) throws Exception {
		WorkspaceProcessor.process(item, new TargetHandler() {
			public void handle(FileSystemTarget target) throws Exception {
				Set sources = new HashSet();
				sources.add(new File(src));
				target.setSources(sources);
			}
		});
	}
	
	private static void cleanArchives(WorkspaceItem item) throws Exception {
		log("Destroying storage directories ...");
		WorkspaceProcessor.process(item, new TargetHandler() {
			public void handle(FileSystemTarget target) throws Exception {
				target.destroyRepository();
			}
		});
	}
	
	private static void showData(WorkspaceItem item) throws Exception {
		WorkspaceProcessor.process(item, new TargetHandler() {
			public void handle(FileSystemTarget target) throws Exception {
				System.out.println(target.getDescription());
			}
		});
	}
	
	private static ProcessContext buildContext(WorkspaceItem item) {
		LogMessagesContainer container = Logger.defaultLogger().getTlLogProcessor().activateMessageTracking();
		ProcessContext context = new ProcessContext((item instanceof AbstractTarget) ? (AbstractTarget)item : null, CHANNEL, new TaskMonitor("tui-main"));
		context.getReport().setLogMessagesContainer(container);
		return context;
	}
	
	private static void doBackup(final WorkspaceItem item, final String backupScheme) throws Exception {
		WorkspaceProcessor.process(item, new TargetHandler() {
			public void handle(FileSystemTarget target) throws Exception {
				CheckParameters checkParams = new CheckParameters(true, true, true, false, null);
				RES+="\n"+target.getName()+";"+target.getUid()+";";

				// Etape 1 : inits divers
				ActionProxy.processBackupOnTarget(
						target,
						null,
						backupScheme,
						checkParams,
						new YesTransactionHandler(),
						buildContext(target)
				);
				target.destroyRepository();
				
				// Etape 2 : sync
				//DefaultFileSystemDriver.ASYNC_OUTPUT = true;
				
				long start = System.currentTimeMillis();
				ActionProxy.processBackupOnTarget(
						target,
						null,
						backupScheme,
						checkParams,
						new YesTransactionHandler(),
						buildContext(target)
				);
				RES+=(System.currentTimeMillis()-start)+";";
				target.destroyRepository();
				
				// Etape 3 : async
				//DefaultFileSystemDriver.ASYNC_OUTPUT = false;
				
				start = System.currentTimeMillis();
				ActionProxy.processBackupOnTarget(
						target,
						null,
						backupScheme,
						checkParams,
						new YesTransactionHandler(),
						buildContext(target)
				);
				RES+=(System.currentTimeMillis()-start)+";";
				target.destroyRepository();
			}
		});
	}
	
	private static void doRecover(final WorkspaceItem item, final String targetDir, final String[] additionalFiles, final boolean ignoreImageConfigs) throws Exception {
		WorkspaceProcessor.process(item, new TargetHandler() {
			public void handle(FileSystemTarget target) throws Exception {
				FileTool.getInstance().delete(new File(targetDir));
				new File(targetDir).mkdirs();
				
				GregorianCalendar date = new GregorianCalendar();
				
				ActionProxy.processRecoverOnTarget(
						target, 
						null, 
						new AlwaysOverwriteCopyPolicy(), 
						targetDir, 
						false, 
						date, 
						false, 
						true, 
						buildContext(target));
				
				try {
					AbstractIncrementalFileSystemMedium fMedium = (AbstractIncrementalFileSystemMedium)target.getMedium();

					String[] argAdditionalFiles;
					if (fMedium.isImage() && ignoreImageConfigs) {
						argAdditionalFiles = null;
					} else {
						argAdditionalFiles = additionalFiles;
					}
					
					CreateData.checkPresentFiles(targetDir, argAdditionalFiles);
				} catch (Exception e) {
					AbstractIncrementalFileSystemMedium medium = (AbstractIncrementalFileSystemMedium)target.getMedium();
					System.out.println(CalendarUtils.getFullDateToString(date));
					File[] archives = medium.listArchives(null, date, true);
					for (int i=0; i<archives.length; i++) {
						System.out.println("----------------------------------------------------------");
						System.out.println("- " + archives[i]);
						System.out.println("----------------------------------------------------------");
						Manifest mf = ArchiveManifestCache.getInstance().getManifest(medium, archives[i]);
						System.out.println(mf.toString());
					}

					throw e;
				}
				FileTool.getInstance().delete(new File(targetDir));
			}
		});
	}
	
	private static void doCheck(final WorkspaceItem item) throws Exception {
		WorkspaceProcessor.process(item, new TargetHandler() {
			public void handle(FileSystemTarget target) throws Exception {
				CheckParameters checkParams = new CheckParameters(true, false, true, false, null);
				ProcessContext ctx = buildContext(target);
				ActionProxy.processCheckOnTarget(target, checkParams, new GregorianCalendar(), ctx);
				if (ctx.getReport().getInvalidRecoveredFiles() != null && !ctx.getReport().getInvalidRecoveredFiles().isEmpty()) {
					Iterator iter = ctx.getReport().getInvalidRecoveredFiles().iterator();
					String msg = "Some recovered files are invalid : ";
					while (iter.hasNext()) {
						msg += iter.next() + " ";
					}
					throw new IllegalStateException(msg);
				}
				if (ctx.getReport().getUncheckedRecoveredFiles() != null && !ctx.getReport().getUncheckedRecoveredFiles().isEmpty()) {
					Iterator iter = ctx.getReport().getUncheckedRecoveredFiles().iterator();
					String msg = "Some recovered files were not checked : ";
					while (iter.hasNext()) {
						msg += iter.next() + " ";
					}
					throw new IllegalStateException(msg);
				}
				if (ctx.getReport().getUnrecoveredFiles() != null && !ctx.getReport().getUnrecoveredFiles().isEmpty()) {
					Iterator iter = ctx.getReport().getUnrecoveredFiles().iterator();
					String msg = "Some files were not recovered : ";
					while (iter.hasNext()) {
						msg += iter.next() + " ";
					}
					throw new IllegalStateException(msg);
				}
			}
		});
	}
	
	private static void doCountArchives(final WorkspaceItem item, final int expected) throws Exception {
		WorkspaceProcessor.process(item, new TargetHandler() {
			public void handle(FileSystemTarget target) throws Exception {
				int toCheck = expected;
				if (((AbstractFileSystemMedium)target.getMedium()).isImage() && toCheck > 1) {
					toCheck = 1;
				}

				File[] files = ((AbstractIncrementalFileSystemMedium)target.getMedium()).listArchives(null, null, true);
				if (files == null) {
					if (toCheck != 0) {
						throw new IllegalStateException("Empty storage directory while expected " + toCheck + " archives.");
					}
				} else if (files.length != toCheck) {
					throw new IllegalStateException("Invalid archives number (" + files.length + ") ... expected " + toCheck + " archives.");
				}
				
				log("OK : " + files.length + " archives found.");
			}
		});
	}
	
	private static void adjustWorkspace(final WorkspaceItem item, String sources) throws Exception {
		File filtered = new File(sources, CreateData.FILTERED_DIR);
		final String path = FileSystemManager.getAbsolutePath(filtered);
		WorkspaceProcessor.process(item, new TargetHandler() {
			public void handle(FileSystemTarget target) throws Exception {
				FilterGroup exclusion = new FilterGroup();
				exclusion.setAnd(false);
				exclusion.setLogicalNot(true);
				
				DirectoryArchiveFilter filter = new DirectoryArchiveFilter();
				filter.setLogicalNot(false);
				filter.acceptParameters(path);
				
				RegexArchiveFilter filter2 = new RegexArchiveFilter();
				filter2.setLogicalNot(false);
				filter2.setScheme(RegexArchiveFilter.SCHEME_NAME);
				filter2.setMatch(false);
				filter2.setRegex("regexme");
				
				FileSizeArchiveFilter filter3 = new FileSizeArchiveFilter();
				filter3.setLogicalNot(false);
				filter3.acceptParameters(">999999999");
				
				FileExtensionArchiveFilter filter4 = new FileExtensionArchiveFilter();
				filter4.setLogicalNot(false);
				filter4.addExtension(".excludeme");
				
				exclusion.addFilter(filter);
				exclusion.addFilter(filter2);
				exclusion.addFilter(filter3);
				exclusion.addFilter(filter4);
				
				target.getFilterGroup().addFilter(exclusion);
			}
		});
	}
	
	public static void main(String[] args) {
		String ws = args[0].replace('\\', '/');
		String sources = ws + "/" + SOURCES_S;
		String recoveryDir = ws + "/" + RECOVERY_DIR_S;
		
		log("Using " + ws + " as workspace.");
		
		try {
			ArecaConfiguration.initialize();
	    	Logger.defaultLogger().setTlLogProcessor(new ThreadLocalLogProcessor());
	    	
	    	switchTo("Create Data");
	    	File f = new File(sources);
	    	FileTool.getInstance().delete(f);
	    	f = new File(recoveryDir);
	    	FileTool.getInstance().delete(f);
			CreateData.create(sources);
			
			switchTo("Load Target");
			TargetGroup workspace = load(ws, sources);
			adjustWorkspace(workspace, sources);
			showData(workspace);
			
			switchTo("Initial Clean");
			cleanArchives(workspace);
			doCountArchives(workspace, 0);
			
			switchTo("First backup");
			doBackup(workspace, AbstractTarget.BACKUP_SCHEME_INCREMENTAL);
			doCountArchives(workspace, 1);
			
			switchTo("Check first backup");
			doCheck(workspace);
			
			switchTo("Recover first backup");
			doRecover(workspace, recoveryDir, null, true);

			SUCCESS = true;
			log("-----------------------------");
			log("Tests performed successfully.");
		} catch (Exception e) {
			e.printStackTrace();
			SUCCESS = false;
			log("------------------------------");
			log("Errors while performing tests.");
			log("Failed at the following step :");
			log(CURRENT_TEST);
			log("... and for :");
			log(CURRENT_TARGET);
		} finally {
	    	try {
				File f = new File(sources);
				FileTool.getInstance().delete(f);
				f = new File(recoveryDir);
				FileTool.getInstance().delete(f);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				System.out.println(RES);
			}
		}
		long stop = System.currentTimeMillis();
		log("Elapsed : " + ((stop - START)/1000) + " sec.");
		log("------------------------------");
	}
	
	private static void switchTo(String task) {
		CURRENT_TEST = task;
		log("---------------------------------");
		log("Switching to \"" + task + "\" ...");
		log("---------------------------------");
	}
	
	private static void log(String msg) {
		System.out.println(msg);
	}
}
