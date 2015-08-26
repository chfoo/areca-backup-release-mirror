package com.myJava.util.log;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.myJava.file.FileSystemManager;
import com.myJava.util.schedule.ScheduledTask;

/**
 * Tache de nettoyage des logs.
 * Cette t�che est lanc�e automatiquement par le logger si
 * celui ci est marqu� comme "historisable"
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

public class LogCleaner extends ScheduledTask {
    
    /**
     *  Logger ayant lanc� la t�che
     */
    protected FileLogProcessor lgcLogger;
    
    /**
     *  Historique de log conserv� (en jours)
     */
    protected int lgcHistory;
    
    /**
     * Constructeur : v�rification toutes les 2 heures.
     * L'historique s'entend en jours.
     */
    public LogCleaner(FileLogProcessor logger, int history) {
        super(2 * 3600);
        this.lgcLogger = logger;
        this.lgcHistory = history;
    }
    
    /**
     * Ex�cution : nettoyage du chemin de log.
     * liste tous les fichiers du chemin, et v�rifie la date
     * pour chacun d'eux.
     * Si cette date est ant�rieure � l'historique, alors le fichier
     * est supprim�.
     * S'appuie sur la date de derni�re modification pour les v�rifications.
     */
    public void execute() {
        super.execute();

        File logDir = this.lgcLogger.getLogDirectory();
        if (logDir != null) {
            String[] fileNames = FileSystemManager.list(logDir);
            if (fileNames != null) {
                for (int i=0; i<fileNames.length; i++) {
                	File f = new File(logDir, fileNames[i]);
                    if (this.checkFileToBeDeleted(FileSystemManager.getAbsolutePath(f))) {
                        synchronized(this.lgcLogger) {
                            FileSystemManager.delete(f);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * V�rifie si le fichier doit �tre supprim� ou non
     */
    protected boolean checkFileToBeDeleted(String fileName) {
        GregorianCalendar fileDate = null;
        File f = new File(this.lgcLogger.getRootFileName());
        if (fileName.toLowerCase().startsWith(FileSystemManager.getAbsolutePath(f).toLowerCase())) {
            fileDate = this.getFileDate(fileName);
            
            // Extraction date
            GregorianCalendar border = new GregorianCalendar();
            border.add(Calendar.DATE, -1 * this.lgcHistory);
            return fileDate.before(border);
            
        } else {
            return false;
        }
    }
    
    /**
     * Retourne la date du fichier
     */
    protected GregorianCalendar getFileDate(String fileName) {
        File f = new File(fileName);
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date(FileSystemManager.lastModified(f)));
        return c;
    }
}