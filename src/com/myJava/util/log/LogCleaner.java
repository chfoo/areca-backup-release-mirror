package com.myJava.util.log;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.myJava.file.FileSystemManager;
import com.myJava.util.schedule.ScheduledTask;

/**
 * Tache de nettoyage des logs.
 * Cette tâche est lancée automatiquement par le logger si
 * celui ci est marqué comme "historisable"
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2367131098465853703
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

public class LogCleaner extends ScheduledTask {
    
    /**
     *  Logger ayant lancé la tâche
     */
    protected FileLogProcessor lgcLogger;
    
    /**
     *  Historique de log conservé (en jours)
     */
    protected int lgcHistory;
    
    /**
     * Constructeur : vérification toutes les 2 heures.
     * L'historique s'entend en jours.
     */
    public LogCleaner(FileLogProcessor logger, int history) {
        super(2 * 3600);
        this.lgcLogger = logger;
        this.lgcHistory = history;
    }
    
    /**
     * Exécution : nettoyage du chemin de log.
     * liste tous les fichiers du chemin, et vérifie la date
     * pour chacun d'eux.
     * Si cette date est antérieure à l'historique, alors le fichier
     * est supprimé.
     * S'appuie sur la date de dernière modification pour les vérifications.
     */
    public void execute() {
        super.execute();

        File logDir = this.lgcLogger.getLogDirectory();
        if (logDir != null) {
            File[] files = FileSystemManager.listFiles(logDir);
            if (files != null) {
                for (int i=0; i<files.length; i++) {
                    if (this.checkFileToBeDeleted(FileSystemManager.getAbsolutePath(files[i]))) {
                        Logger.defaultLogger().info("Deleting old log file : " + files[i]);
                        synchronized(this.lgcLogger) {
                            FileSystemManager.delete(files[i]);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Vérifie si le fichier doit être supprimé ou non
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