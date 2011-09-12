package com.application.areca;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.GregorianCalendar;

import com.myJava.file.FileSystemManager;
import com.myJava.system.OSTool;
import com.myJava.util.log.Logger;

/**
 * Utility methods
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2011, Olivier PETRUCCI.

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
public class Utils implements ArecaFileConstants {
    private static final ResourceManager RM = ResourceManager.instance();
    private static DateFormat DF;
    public static final String FILE_DATE_SEPARATOR = ".";
    private static final NumberFormat NF = new DecimalFormat();
    private static final String LN_DIRECTORY = ArecaConfiguration.get().getLanguageLocationOverride();
    private static final String EXEC_DIRECTORY = ArecaConfiguration.get().getBinLocationOverride();

    static {
        NF.setGroupingUsed(true);
        initDateFormat(null);
    }

    public static void initDateFormat(String format) {
        if (format != null && format.trim().length() != 0) {
            try {
                DF = new SimpleDateFormat(format);
            } catch (Throwable e) {
                Logger.defaultLogger().warn("The following error occurred during date format initialization : " + e.getMessage() + ". A default date format will be used.");
                DF = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
            }
        } else {
            DF = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        }
    }

    public static File getApplicationRoot() {
        String dir = System.getProperty("user.dir");
        if (dir != null && dir.trim().length() != 0) {
            return new File(dir);
        } else {
            Logger.defaultLogger().warn("Unable to load the 'user.dir' java property. Using classpath to detect application root.");
            URL url = ClassLoader.getSystemClassLoader().getResource(ResourceManager.RESOURCE_NAME + "_en.properties");
            File file = new File(URLDecoder.decode(url.getFile()));
            return FileSystemManager.getParentFile(FileSystemManager.getParentFile(file));
        }
    }

    public static TranslationData[] getTranslations() {
        final String prefix = ResourceManager.RESOURCE_NAME + "_";
        final String suffix = ".properties";

    	File translationsRoot;
    	if (LN_DIRECTORY == null) {
    		translationsRoot = new File(getApplicationRoot(), DEFAULT_TRANSLATION_SUBDIRECTORY_NAME);
    	} else {
    		translationsRoot = new File(LN_DIRECTORY);
    	}

        File[] files = FileSystemManager.listFiles(translationsRoot, new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(prefix) && name.endsWith(suffix);
            }
        });

        if (files == null) {
            Logger.defaultLogger().warn("Unable to locate the translation files in " + FileSystemManager.getAbsolutePath(translationsRoot));
            if (! FileSystemManager.exists(translationsRoot)) {
                Logger.defaultLogger().warn(FileSystemManager.getAbsolutePath(translationsRoot) + " does not exist.");
            }
            return new TranslationData[0];
        } else {
        	TranslationData[] languages = new TranslationData[files.length];
            for (int i=0; i<files.length; i++) {
            	String fileName = FileSystemManager.getName(files[i]);
            	String data = fileName.substring(ResourceManager.RESOURCE_NAME.length(), fileName.length() - suffix.length() + 1);
            	String lg ;
            	boolean deprecated;
            	if (data.startsWith(ResourceManager.RESOURCE_NAME_DEPRECATED_SUFFIX + "_")) {
            		deprecated = true;
            		lg = data.substring(ResourceManager.RESOURCE_NAME_DEPRECATED_SUFFIX.length() + 1, ResourceManager.RESOURCE_NAME_DEPRECATED_SUFFIX.length() + 3);
            	} else {
                	deprecated = false;
                	lg = data.substring(1, 3);
            	}
                languages[i] = new TranslationData(lg, deprecated);
            }

            Arrays.sort(languages);
            return languages;
        }
    }
    
    /**
     * Build the "areca_cl" file name according to the user's system and technical configuration
     */
    public static File buildExecutableFile() {
    	String executableName;
        if (OSTool.isSystemWindows()) {
        	executableName = "areca_cl.exe";
        } else {
        	executableName = "areca_cl.sh";
        }
    	
        File executableDirectory;
    	if (EXEC_DIRECTORY == null) {
            File applicationRoot = Utils.getApplicationRoot();
            if (OSTool.isSystemWindows()) {
            	executableDirectory = applicationRoot;
            } else {
            	executableDirectory = new File(applicationRoot, DEFAULT_BIN_SUBDIRECTORY_NAME);
            }
    	} else {
    		executableDirectory = new File(EXEC_DIRECTORY);
    	}

    	return new File(executableDirectory, executableName);
    }

    public static String getTranslationsAsString() {
        StringBuffer sb = new StringBuffer();
        TranslationData[] lges = Utils.getTranslations();
        for (int i=0; i<lges.length; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append(lges[i].getLanguage());
        }
        return sb.toString();
    }

    public static String extractShortFilePath(File fileDir, File baseDir) {
        return extractShortFilePath(fileDir, FileSystemManager.getAbsolutePath(baseDir));
    }

    public static String extractShortFilePath(File fileDir, String sBaseDir) {
        String sFileDir = FileSystemManager.getPath(fileDir);
        int index = sBaseDir.length();

        if (
        		OSTool.isSystemWindows()		
                && index == 0
                && sFileDir.length() > 2
                && sFileDir.charAt(0) != '/'
                && sFileDir.charAt(0) != '\\'
                && sFileDir.charAt(1) == ':'
                && (sFileDir.charAt(2) == '/' || sFileDir.charAt(2) == '\\')
        ) {
        	// Windows dedicated code : transform c:\toto into c/toto
            return sFileDir.charAt(0) + (sFileDir.length() > 3 ? sFileDir.substring(2) : "");
        } else if (index < sFileDir.length()) {
            while(sFileDir.charAt(index) == '/' || sFileDir.charAt(index) == '\\') {
                index++;
            }

            return sFileDir.substring(index);
        } else {
            return "";
        }
    }

    public static String formatDisplayDate(GregorianCalendar cal) {
        if (cal == null) {
            return RM.getLabel("common.undated.label");
        } else {
            return DF.format(cal.getTime());
        }
    }

    public static String formatFileSize(File f) {
        if (FileSystemManager.isDirectory(f)) {
            return RM.getLabel("common.unsized.label");
        } else {
            return formatFileSize(FileSystemManager.length(f));
        }
    }

    public static String formatFileSize(long argSize) {
        long size = argSize;

    	if (size >= 1024) {
    		size = (long)(argSize / 1024);
            return NF.format(size) + " " + RM.getLabel("common.kb.label");
    	} else {
            return NF.format(size) + " " + RM.getLabel("common.bytes.label");
    	}
    }

    public static String formatLong(long argLong) {
        NumberFormat nf = new DecimalFormat();
        nf.setGroupingUsed(true);

        return nf.format(argLong);
    }

    /**
     * Retourne le nombre de millisecondes sous forme d'une dur�e.
     */
    public static String formatDuration(long ms) {
        if (ms < 1000) {
            return "" + ms + " " + RM.getLabel("common.time.ms");
        }

        // On utilise les secondes ...
        long nbSecondes = ms/1000;

        // Formattage
        long nbHeures = (long)(nbSecondes/3600);
        nbSecondes = nbSecondes - nbHeures*3600;

        long nbMinutes = (long)(nbSecondes/60);
        nbSecondes = nbSecondes - nbMinutes*60;

        StringBuffer sb = new StringBuffer();
        if (nbHeures > 0) {
            sb.append(nbHeures).append(" ");
            sb.append(RM.getLabel("common.time.h")).append(" ");
        }

        if (nbMinutes > 0) {
            sb.append(nbMinutes).append(" ");
            sb.append(RM.getLabel("common.time.mn")).append(" ");
        }

        if (nbSecondes > 0) {
            sb.append(nbSecondes).append(" ");
            sb.append(RM.getLabel("common.time.s")).append(" ");
        }

        return sb.toString();
    }

    public static boolean isEmpty(String o) {
        return o == null || o.trim().equals("");
    }

    /**
     * Normalizes the fileName provided as argument.
     * <BR>All file separators are removed and "?" are removed (URL parameter separator)
     */
    public static String normalizeFileName(String origName) {
        int l = origName.length();
        StringBuffer sb = new StringBuffer();
        char c;
        char pc = 'A';
        for (int i=0; i<l; i++) {
            c = origName.charAt(i);

            if (c != '/' && c != '\\' && c != '?') {
                sb.append(c);
                pc = c;
            } else if (pc != ' ') {
                sb.append(' ');
                pc = ' ';
            }
        }

        return sb.toString().trim();
    }
    
	/**
	 * Windows file paths are not case sensitive, but Areca IS.
	 * <BR>In some cases, this can lead to errors -> we need to normalize file paths
	 */
	public static String normalizePath(String path) {
		if (path == null || path.length() == 0) {
			return path;
		} else if (OSTool.isSystemWindows()) {
			try {
				return FileSystemManager.getCanonicalPath(new File(path));
			} catch (IOException e) {
				throw new IllegalArgumentException("Error caught while normalizing path : " + path, e);
			}
		} else {
			return path;
		}
	}
}
