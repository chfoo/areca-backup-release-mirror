package com.application.areca.launcher;

import java.util.Locale;
import java.util.Stack;

import com.application.areca.TranslationData;
import com.application.areca.Utils;
import com.application.areca.context.ReportingConfiguration;
import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.FileSystemManager;
import com.myJava.system.OSTool;

/**
 * @author Stephane Brunel
 * @author Olivier Petrucci
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
public final class ArecaUserPreferences {
    private static final String STARTUP_DISPLAY_LOGICAL_VIEW = "startup.display.logical.view";
    private static final String STARTUP_MODE = "startup.mode";
	private static final String STARTUP_WORKSPACE = "startup.workspace";
	private static final String LAST_WORKSPACE = "lastworkspace";
	private static final String LANG = "lang";
	private static final String LNF = "lnf";
	private static final String ARCHIVE_STORAGE = "archive.storage";
	private static final String DISPLAY_REPORT = "archive.displayreport";
	private static final String LAST_WORKSPACE_COPY_LOCATION = "workspace.last.copy.location";
	private static final String LAST_WORKSPACE_COPY_MASK = "workspace.last.copy.mask";
    private static final String TEXT_EDITOR = "editor.text";
    private static final String INFO_SYNTHETIC = "info.channel.synthetic";
    private static final String DATE_FORMAT = "date.format";
    public static final String DISPLAY_JAVA_VENDOR_MESSAGE = "display.java.vendor.message";
    public static final String DISPLAY_DEPRECATED_IMAGE_MESSAGE = "display.deprecated.image.message";
	private static final String CHECK_NEW_VERSIONS = "check.new.versions";
	private static final String GUI_LOG_LEVEL_DEPRECATED = "gui.log.level";
	private static final String GUI_LOG_LEVEL = "log.level";
	private static final String CHECK_FORCE_DEFAULT_LOCATION = "check.force.default.location";
	private static final String CHECK_USE_SPECIFIC_LOCATION = "check.use.specific.location";
	private static final String CHECK_SPECIFIC_LOCATION = "check.specific.location";
	private static final String MERGE_USE_SPECIFIC_LOCATION = "merge.use.specific.location";
	private static final String MERGE_SPECIFIC_LOCATION = "merge.specific.location";
	private static final String DISPLAY_WS_ADDRESS = "display.ws.address";
	private static final String DISPLAY_TOOLBAR = "display.toolbar";
	private static final String WS_HISTORY = "ws.history";
	private static final String LAUNCH_COUNT = "launch.count";
	private static final String DONATION_MSG_DAY = "dnt.msg.day";
	private static final String DONATION_THRES = "dnt.msg.threshold";
    
	public static final int UNDEFINED = -1;
	public static final int LAST_WORKSPACE_MODE = 0;
	public static final int DEFAULT_WORKSPACE_MODE = 1;
	
    private static final String STARTUP_MODE_LAST = "last";
    private static final String STARTUP_MODE_DEFAULT = "default";
    
    public static final int MAX_HISTORY_SIZE = 10;

    public static void initialize(String configurationDirectory) {
    	LocalPreferences.initialize(configurationDirectory);
	    synchronizeClientConfigurations();
    }
    
    public static String getPath() {
    	return FileSystemManager.getAbsolutePath(LocalPreferences.instance().getFile(false));
    }
	
    public static boolean isDisplayWSAddress() {
        return LocalPreferences.instance().getBoolean(DISPLAY_WS_ADDRESS, true);
    }
    
    public static void setDisplayWSAddress(boolean flag) {
        LocalPreferences.instance().set(DISPLAY_WS_ADDRESS, flag);
        synchronizeClientConfigurations();
    }
    
    public static boolean isDisplayToolBar() {
        return LocalPreferences.instance().getBoolean(DISPLAY_TOOLBAR, true);
    }
    
    public static void setDisplayToolBar(boolean flag) {
        LocalPreferences.instance().set(DISPLAY_TOOLBAR, flag);
        synchronizeClientConfigurations();
    }
    
    public static Stack getWorkspaceHistory() {
        return LocalPreferences.instance().getStack(WS_HISTORY);
    }
    
    public static void setWorkspaceHistory(Stack h) {
        LocalPreferences.instance().set(WS_HISTORY, h);
        synchronizeClientConfigurations();
    }
    
    public static String getDateFormat() {
        return LocalPreferences.instance().get(DATE_FORMAT, null);
    }
    
    public static void setDateFormat(String df) {
        LocalPreferences.instance().set(DATE_FORMAT, df);
        synchronizeClientConfigurations();
    }
	
	public static String getDefaultArchiveStorage() {
	    return LocalPreferences.instance().get(ARCHIVE_STORAGE, "");
	}
	
	public static void setDefaultArchiveStorage(String dir) {
	    LocalPreferences.instance().set(ARCHIVE_STORAGE, dir);
	    synchronizeClientConfigurations();
	}
    
    public static void setCheckNewVersion(boolean b) {
        LocalPreferences.instance().set(CHECK_NEW_VERSIONS, b);
        synchronizeClientConfigurations();
    }
    
    public static void setDisplayMessage(String key, boolean b) {
        LocalPreferences.instance().set(key, b);
        synchronizeClientConfigurations();
    }
    
    public static boolean isDisplayMessage(String key) {
        return LocalPreferences.instance().getBoolean(key, true);
    }
    
    public static boolean isDisplayLogicalViewOnStartup() {
        return LocalPreferences.instance().getBoolean(STARTUP_DISPLAY_LOGICAL_VIEW, false);
    }
    
    public static void setDisplayLogicalViewOnStartup(boolean show) {
	    LocalPreferences.instance().set(STARTUP_DISPLAY_LOGICAL_VIEW, show);
	    synchronizeClientConfigurations();
    }
	
	public static void setLastWorkspaceCopyLocation(String dir) {
	    LocalPreferences.instance().set(LAST_WORKSPACE_COPY_LOCATION, dir);
	    synchronizeClientConfigurations();
	}
	
    public static void setLogLevel(int level) {
        LocalPreferences.instance().remove(GUI_LOG_LEVEL_DEPRECATED);
        LocalPreferences.instance().set(GUI_LOG_LEVEL, level);
        synchronizeClientConfigurations();
    }
    
    public static void setCheckForceDefaultLocation(boolean mask, String uid) {
        LocalPreferences.instance().remove(CHECK_USE_SPECIFIC_LOCATION + "." + normalize(uid));
        LocalPreferences.instance().set(CHECK_FORCE_DEFAULT_LOCATION + "." + normalize(uid), mask);
        synchronizeClientConfigurations();
    }
	
	public static void setCheckSpecificLocation(String path, String uid) {
	    LocalPreferences.instance().set(CHECK_SPECIFIC_LOCATION + "." + normalize(uid), path);
	    synchronizeClientConfigurations();
	}
	
    public static void setMergeUseSpecificLocation(boolean mask, String uid) {
        LocalPreferences.instance().set(MERGE_USE_SPECIFIC_LOCATION + "." + normalize(uid), mask);
        synchronizeClientConfigurations();
    }
	
	public static void setMergeSpecificLocation(String path, String uid) {
	    LocalPreferences.instance().set(MERGE_SPECIFIC_LOCATION + "." + normalize(uid), path);
	    synchronizeClientConfigurations();
	}
    
    public static void setEditionCommand(String command) {
        LocalPreferences.instance().set(TEXT_EDITOR, command);
        synchronizeClientConfigurations();
    }
	
	public static void setLastWorkspaceCopyMask(boolean mask) {
	    LocalPreferences.instance().set(LAST_WORKSPACE_COPY_MASK, mask);
	    synchronizeClientConfigurations();
	}
    
    public static void setInformationSynthetic(boolean synthetic) {
        LocalPreferences.instance().set(INFO_SYNTHETIC, synthetic);
        synchronizeClientConfigurations();
    }
	
	public static String getLastWorkspace() {
	    return LocalPreferences.instance().get(LAST_WORKSPACE, "");
	}
	
	public static String getLastWorkspaceCopyLocation() {
	    return LocalPreferences.instance().get(LAST_WORKSPACE_COPY_LOCATION, "");
	}
    
    public static String getEditionCommand() {
        return LocalPreferences.instance().get(TEXT_EDITOR, "");
    }
    
    public static boolean hasEditionCommand() {
        return LocalPreferences.instance().get(TEXT_EDITOR, "").length() != 0;
    }
    
    public static boolean isCheckForceDefaultLocation(String uid) {
    	return LocalPreferences.instance().getBoolean(CHECK_FORCE_DEFAULT_LOCATION + "." + normalize(uid), false);
    }
    
    public static boolean getMergeUseSpecificLocation(String uid) {
    	return LocalPreferences.instance().getBoolean(MERGE_USE_SPECIFIC_LOCATION + "." + normalize(uid), false);
    }
	
	public static String getCheckSpecificLocation(String uid) {
		return LocalPreferences.instance().get(CHECK_SPECIFIC_LOCATION + "." + normalize(uid), OSTool.getTempDirectory());
	}
	
	public static String getMergeSpecificLocation(String uid) {
		return LocalPreferences.instance().get(MERGE_SPECIFIC_LOCATION + "." + normalize(uid), OSTool.getTempDirectory());
	}
	
	public static boolean getLastWorkspaceCopyMask() {
	    return LocalPreferences.instance().getBoolean(LAST_WORKSPACE_COPY_MASK);
	}
	
	public static int getLaunchCount() {
	    return LocalPreferences.instance().getInt(LAUNCH_COUNT, 0);
	}
	
	public static int getDonationThreshold() {
	    return LocalPreferences.instance().getInt(DONATION_THRES, 120);
	}
	
	public static int getDonationMsgDay() {
	    return LocalPreferences.instance().getInt(DONATION_MSG_DAY, 0);
	}
	
	public static int getLogLevel() {
	    return LocalPreferences.instance().getInt(GUI_LOG_LEVEL, 4);
	}
    
    public static boolean isInformationSynthetic() {
        return LocalPreferences.instance().getBoolean(INFO_SYNTHETIC, true);
    }
    
    public static boolean isCheckNewVersions() {
        return LocalPreferences.instance().getBoolean(CHECK_NEW_VERSIONS, false);
    }
	
	public static void setLastWorkspace(String lw) {
	    LocalPreferences.instance().set(LAST_WORKSPACE, lw);
	    synchronizeClientConfigurations();
	}
	
	public static int getStartupMode() {
	    String mode = LocalPreferences.instance().get(STARTUP_MODE);
	    if (STARTUP_MODE_LAST.equals(mode)) {
	        return LAST_WORKSPACE_MODE;
	    } else if ("default".equals(mode)) {
	        return DEFAULT_WORKSPACE_MODE;
	    }
	    return UNDEFINED;
	}
	
	public static void setStartupMode(int mode) {
	    LocalPreferences.instance().set(STARTUP_MODE, mode == LAST_WORKSPACE_MODE ? STARTUP_MODE_LAST : STARTUP_MODE_DEFAULT);
	    synchronizeClientConfigurations();
	}
	
	public static void setLaunchCount(int cnt) {
	    LocalPreferences.instance().set(LAUNCH_COUNT, cnt);
	    synchronizeClientConfigurations();
	}
	
	public static void setDonationMsgDay(int cnt) {
	    LocalPreferences.instance().set(DONATION_MSG_DAY, cnt);
	    synchronizeClientConfigurations();
	}
	
	public static String getDefaultWorkspace() {
	    return LocalPreferences.instance().get(STARTUP_WORKSPACE, "");
	}
	
	public static void setDefaultWorkspace(String dw) {
	    LocalPreferences.instance().set(STARTUP_WORKSPACE, dw);
	    synchronizeClientConfigurations();
	}
	
	public static String getLnF() {
	    return LocalPreferences.instance().get(LNF);
	}

	public static void setLnF(String lnf) {
	    LocalPreferences.instance().set(LNF, lnf);
	    synchronizeClientConfigurations();
	}
	
	public static String getLang() {
	    return LocalPreferences.instance().get(LANG, System.getProperty("user.language"));
	}
	
	public static String resolveLanguage() {
    	String currentLg = getLang();
        TranslationData[] lges = Utils.getTranslations();
        
        // Check that the language exists in the language list
        boolean found = false;
        for (int i=0; i<lges.length; i++) {
            if (lges[i].getLanguage().equals(currentLg)) {
            	found = true;
            	break;
            }
        }
        if (! found) {
        	currentLg = "en";
        }

        return currentLg;
	}
	
	public static void setLang(String lang) {
	    LocalPreferences.instance().set(LANG, lang);
	    synchronizeClientConfigurations();
	}

	public static boolean getDisplayReport() {
	    return LocalPreferences.instance().getBoolean(DISPLAY_REPORT);
	}
	
	public static void setDisplayReport(boolean display) {
	    LocalPreferences.instance().set(DISPLAY_REPORT, display);
	    synchronizeClientConfigurations();
	}
	
	private static void synchronizeClientConfigurations() {
	    ReportingConfiguration.getInstance().setReportingEnabled(getDisplayReport());
        if (getLang() != null) {
            Locale.setDefault(new Locale(getLang()));
        }
        Utils.initDateFormat(getDateFormat());
	}
	
	private static String normalize(String str) {
		return str
		.replace('\\', '_')
		.replace('/', '_')
		.replace('=', '_')
		.replace(' ', '_')
		.replace('#', '_')
		.replace('\r', '_')
		.replace('\n', '_')
		.replace('\t', '_')
		;
	}
}
