package com.application.areca;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import com.myJava.file.FileSystemManager;
import com.myJava.util.CalendarUtils;

/**
 * Utilitaires
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -1700699344456460829
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
public class Utils {
    private static final ResourceManager RM = ResourceManager.instance();
    private static DateFormat DF;
    public static final String FILE_DATE_SEPARATOR = ".";
    private static final NumberFormat NF = new DecimalFormat();
    
    static {
        NF.setGroupingUsed(true);
        initDateFormat(null);
    }
    
    public static void initDateFormat(String format) {
        if (format != null && format.trim().length() != 0) {
            DF = new SimpleDateFormat(format);
        } else {
            DF = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        }
    }
    
    public static File getApplicationRoot() {
        URL url = ClassLoader.getSystemClassLoader().getResource(ResourceManager.RESOURCE_NAME + "_en.properties");
        File licenseFile = new File(URLDecoder.decode(url.getFile()));
        return FileSystemManager.getParentFile(FileSystemManager.getParentFile(licenseFile));
    }
    
    public static String[] getTranslations() {
        final int length = ResourceManager.RESOURCE_NAME.length() + 14;
        final String prefix = ResourceManager.RESOURCE_NAME + "_";
        final String suffix = ".properties";
        
        File translationsRoot = new File(getApplicationRoot(), "translations");
        File[] files = FileSystemManager.listFiles(translationsRoot, new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.length() == length && name.startsWith(prefix) && name.endsWith(suffix);
            }
        });
        
        String[] languages = new String[files.length];
        for (int i=0; i<files.length; i++) {
            languages[i] = FileSystemManager.getName(files[i]).substring(ResourceManager.RESOURCE_NAME.length() + 1, ResourceManager.RESOURCE_NAME.length() + 3);
        }
        
        return languages;
    }
    
    /**
     * Intègre la date courante au nom de fichier 
     */
    public static String dateTimeFileName(String originalFileName, String ext) {
        return dateTimeFileName(originalFileName, new GregorianCalendar(), ext);
    }      
    
    /**
     * Intègre la date/heure au nom de fichier 
     */
    public static String dateTimeFileName(String originalFileName, GregorianCalendar cal, String ext) {
        return originalFileName + FILE_DATE_SEPARATOR + CalendarUtils.getDateToString(cal) + FILE_DATE_SEPARATOR + CalendarUtils.getTimeToString(cal) + ext;
    }    
    
    /**
     * Retourne le chemin relatif de l'entry par rapport à sa racine. 
     */
    public static String extractShortFilePath(File fileDir, File baseDir) {
        String sBaseDir = FileSystemManager.getAbsolutePath(baseDir);
        String sFileDir = FileSystemManager.getAbsolutePath(fileDir);
        
        int index = sBaseDir.length();
        char chr = sFileDir.charAt(index);
	    if (chr == '/') { // Filter fileSystem separatorChar and zip entry separatorChar
            return sFileDir.substring(index + 1);            
        } else {
            return sFileDir.substring(index);
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
     * Retourne le nombre de millisecondes sous forme d'une durée. 
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
}
