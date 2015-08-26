package com.myJava.file;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.myJava.util.schedule.ScheduledTask;

/**
 * Tache de nettoyage de répertoire temporaire :
 * Supprime tous les fichiers plus vieux que N jours.
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

public class TemporaryDirectoryCleaner extends ScheduledTask {

    /**
     *  Chemin contenant les uploads
     */
    protected String tpcPath;

    /**
     *  Délai de nettoyage (en jours)
     */
    protected int tpcCleanDelay;


    /**
     * Constructeur : vérification toutes les 12 heures.
     * L'historique s'entend en jours.
     */
    public TemporaryDirectoryCleaner(String path, int cleanDelay) {
        super(12 * 3600);
        this.tpcPath = path;
        this.tpcCleanDelay = cleanDelay;
    }

    /**
     * Constructeur : vérification toutes les 12 heures.
     * L'historique est de 3 jours.
     */
    public TemporaryDirectoryCleaner(String path) {
        this(path, 3);
    }

    /**
     * Exécution : Nettoyage du chemin.
     * liste tous les fichiers du chemin, et vérifie la date
     * pour chacun d'eux.
     * Si cette date est antérieure à l'historique, alors le fichier
     * est supprimé.
     * S'appuie sur la date de dernière modification pour les vérifications.
     */
    public void execute() {
        super.execute();

        synchronized(this) {
            File tmpDir = new File(this.tpcPath);
            String[] fileNames = FileSystemManager.list(tmpDir);
            for (int i=0; i<fileNames.length; i++) {
            	File file = new File(tmpDir, fileNames[i]);
            	
                if (this.checkFileToBeDeleted(file)) {
                    FileSystemManager.delete(file);
                }
            }
        }
    }

    /**
     * Vérifie si le fichier doit être supprimé ou non
     */
    protected boolean checkFileToBeDeleted(File f) {
        GregorianCalendar fileDate = this.getFileDate(f);
        GregorianCalendar border = new GregorianCalendar();
        border.add(Calendar.DATE, -1 * this.tpcCleanDelay);

        return fileDate.before(border);
    }

    /**
     * Retourne la date du fichier
     */
    protected GregorianCalendar getFileDate(File f) {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date(FileSystemManager.lastModified(f)));
        return c;
    }
}