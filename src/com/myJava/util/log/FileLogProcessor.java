package com.myJava.util.log;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.system.OSTool;

/**
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
public class FileLogProcessor 
implements LogProcessor {
    
    private static SimpleDateFormat DF = new SimpleDateFormat("yy-MM-dd");
    private static int DEFAULT_LOG_HISTORY = FrameworkConfiguration.getInstance().getDefaultLogHistory();
    
    /**
     *  Booleen indiquant si on utilise un fichier unique ou si on utilise un fichier par jour
     */
    protected boolean uniqueFile;
    
    /**
     *  Chemin d'acces complet au fichier de log
     */
    private String fileName;
    
    /**
     * Process de nettoyage eventuel de la log.
     */
    protected LogCleaner cleaner;
    
    private FileLogProcessor() {
        this.enableLogHistory(DEFAULT_LOG_HISTORY);
    }
    
    public FileLogProcessor(String file) {
        this();
        this.fileName = file;
        
        File f = new File(fileName);
        File parent = FileSystemManager.getParentFile(f);
        if (! FileSystemManager.exists(parent)) {
            try {
            	FileTool.getInstance().createDir(parent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public FileLogProcessor(File file) {
        this(FileSystemManager.getAbsolutePath(file));
    }
    
    /**
     *  Declenche l'historisation de la log.
     *  <BR>L'historique s'entend en jours.
     */
    public void enableLogHistory(int history) {
        this.uniqueFile = false;
        if (this.cleaner != null) {
            this.cleaner.stopTask();
        }
        
        this.cleaner = new LogCleaner(this, history);
        this.cleaner.startTask();
    }
    
    /**
     * Retourne le fichier de log courante
     */
    public String getCurrentLogFile() {
        if (! this.uniqueFile) {
            return this.fileName + "." + DF.format(new Date()) + ".log";
        } else {
            return this.fileName + ".log";
        }
    }
    
    public void log(int level, String message, Throwable e, String source) {
        // Log complete :
        String logCt = LogHelper.format(level, message, source, true).toString();
        
    	if (level <= LogLevels.LOG_LEVEL_WARNING) {
            System.out.println(logCt);
            if (e != null) {
                e.printStackTrace();
            }
    	}
        
        // Ecriture de la log.
        try {
            String tgFile = getCurrentLogFile();
 
            Writer fw = FileSystemManager.getWriter(tgFile, true);
            fw.write(OSTool.getLineSeparator());
            fw.write(logCt);
            fw.flush();
            if (e != null) {
                fw.write(" - ");
                e.printStackTrace(new PrintWriter(fw, true));
            }
            fw.close();

        } catch (Exception exc) {
            System.out.println(" ");
            exc.printStackTrace();
        }
    }
    
    
    /**
     * Retourne le nom de base du fichier (sans la date, si on fonctionne en 
     * mode "historisation") 
     */
    public String getRootFileName() {
        return this.fileName;
    }
    
    /**
     * Retourne le repertoire de log
     */
    public File getLogDirectory() {
        if (this.fileName != null) {
            File f = new File(this.fileName);
            return FileSystemManager.getParentFile(f);
        } else {
            return null;
        }
    }
    
    /**
     * Efface le fichier de log.
     * <BR>Retourne true en cas de succes, false en cas d'echec.
     */
    public boolean clearLog() {
        if (fileName != null) {
            File f = new File(fileName);
            synchronized (this) {
                return (FileSystemManager.delete(f));	
			}
        } else {
            return false;
        }
    }

    public void displayApplicationMessage(String messageKey, String title, String message) {
        log(3, message, null, title);
    }

	public void unmount() {
		this.cleaner.stopTask();
	}
}
