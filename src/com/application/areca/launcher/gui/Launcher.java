package com.application.areca.launcher.gui;

import java.util.Locale;

import javax.swing.JOptionPane;

import com.application.areca.ArecaTechnicalConfiguration;
import com.application.areca.version.VersionInfos;
import com.myJava.util.log.Logger;
import com.myJava.util.os.OSTool;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -6307890396762748969
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
public class Launcher {
    
    public static void main(String[] args) {
        boolean killOnError = true;
        try {
            ArecaTechnicalConfiguration.initialize();
            checkJavaVersion();
            
            String workspace = null;
            switch (ArecaPreferences.getStartupMode()) {
            case ArecaPreferences.LAST_WORKSPACE_MODE:
                workspace = ArecaPreferences.getLastWorkspace();
            break;
            case ArecaPreferences.DEFAULT_WORKSPACE_MODE:
                workspace = ArecaPreferences.getDefaultWorkspace();
            break;
            }
            if (workspace == null) {
                if (args.length != 0) {
                    workspace = args[0];
                } else {
                    workspace = System.getProperty("user.dir");
                }
            }

            String lang = ArecaPreferences.getLang();
            if (lang != null) {
                Locale.setDefault(new Locale(lang));
            } else {
                ArecaPreferences.setLang(Locale.getDefault().getCountry().toLowerCase());
            }
            
            Application gui = Application.getInstance();
            gui.loadWorkspace(workspace);
            killOnError = false;  // Now that the gui was initialized, don't kill on error 
            gui.show();
        } catch (Throwable e) {
            e.printStackTrace();
            Logger.defaultLogger().error("Unexpected error", e);
            
            if (killOnError) {
                Logger.defaultLogger().warn("Critical error during initialization ... exiting.");
                System.exit(-1);
            }
        }
    }
    
    private static void checkJavaVersion() {
        if (! OSTool.isJavaVersionGreaterThanOrEquals(VersionInfos.REQUIRED_JAVA_VERSION)) {
            System.out.println("----------------------------------------------------------------------------------\n ");
            System.out.println(VersionInfos.VERSION_MSG);
            System.out.println("----------------------------------------------------------------------------------");
            
            JOptionPane.showMessageDialog(null,
                    VersionInfos.VERSION_MSG, VersionInfos.APP_NAME + " - Invalid Java Version", JOptionPane.ERROR_MESSAGE);

            System.exit(-1);
        }
    }
}