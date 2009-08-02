package com.application.areca.launcher.gui.common;

import java.util.Locale;

import com.application.areca.TranslationData;
import com.application.areca.Utils;
import com.application.areca.context.ReportingConfiguration;
import com.myJava.configuration.FrameworkConfiguration;

/**
 * @author Stephane Brunel
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
public final class ArecaPreferences {
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
    private static final String DISPLAY_JAVA_VENDOR_MESSAGE = "display.java.vendor.message";
	private static final String CHECK_NEW_VERSIONS = "check.new.versions";
	private static final String GUI_LOG_LEVEL = "gui.log.level";
    
	public static final int UNDEFINED = -1;
	public static final int LAST_WORKSPACE_MODE = 0;
	public static final int DEFAULT_WORKSPACE_MODE = 1;
	
    private static final String STARTUP_MODE_LAST = "last";
    private static final String STARTUP_MODE_DEFAULT = "default";
	
	static {
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
    
    public static void setDisplayJavaVendorMessage(boolean b) {
        LocalPreferences.instance().set(DISPLAY_JAVA_VENDOR_MESSAGE, b);
        synchronizeClientConfigurations();
    }
	
	public static void setLastWorkspaceCopyLocation(String dir) {
	    LocalPreferences.instance().set(LAST_WORKSPACE_COPY_LOCATION, dir);
	    synchronizeClientConfigurations();
	}
	
    public static void setLogLevel(int level) {
        LocalPreferences.instance().set(GUI_LOG_LEVEL, level);
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
	
	public static boolean getLastWorkspaceCopyMask() {
	    return LocalPreferences.instance().getBoolean(LAST_WORKSPACE_COPY_MASK);
	}
	
	public static int getLogLevel() {
	    return LocalPreferences.instance().getInt(GUI_LOG_LEVEL, FrameworkConfiguration.getInstance().getLogLevel());
	}
    
    public static boolean isInformationSynthetic() {
        return LocalPreferences.instance().getBoolean(INFO_SYNTHETIC, false);
    }
    
    public static boolean isCheckNewVersions() {
        return LocalPreferences.instance().getBoolean(CHECK_NEW_VERSIONS, false);
    }
    
    public static boolean isDisplayJavaVendorMessage() {
        return LocalPreferences.instance().getBoolean(DISPLAY_JAVA_VENDOR_MESSAGE, true);
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
}
