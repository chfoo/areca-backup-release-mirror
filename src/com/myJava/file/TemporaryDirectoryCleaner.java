package com.myJava.file;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.myJava.util.schedule.ScheduledTask;

/**
 * Tache de nettoyage de r�pertoire temporaire :
 * Supprime tous les fichiers plus vieux que N jours.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2010, Olivier PETRUCCI.

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
     *  D�lai de nettoyage (en jours)
     */
    protected int tpcCleanDelay;


    /**
     * Constructeur : v�rification toutes les 12 heures.
     * L'historique s'entend en jours.
     */
    public TemporaryDirectoryCleaner(String path, int cleanDelay) {
        super(12 * 3600);
        this.tpcPath = path;
        this.tpcCleanDelay = cleanDelay;
    }

    /**
     * Constructeur : v�rification toutes les 12 heures.
     * L'historique est de 3 jours.
     */
    public TemporaryDirectoryCleaner(String path) {
        this(path, 3);
    }

    /**
     * Ex�cution : Nettoyage du chemin.
     * liste tous les fichiers du chemin, et v�rifie la date
     * pour chacun d'eux.
     * Si cette date est ant�rieure � l'historique, alors le fichier
     * est supprim�.
     * S'appuie sur la date de derni�re modification pour les v�rifications.
     */
    public void execute() {
        super.execute();

        synchronized(this) {
            File tmpDir = new File(this.tpcPath);
            File[] files = FileSystemManager.listFiles(tmpDir);
            for (int i=0; i<files.length; i++) {
                if (this.checkFileToBeDeleted(files[i])) {
                    FileSystemManager.delete(files[i]);
                }
            }
        }
    }

    /**
     * V�rifie si le fichier doit �tre supprim� ou non
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