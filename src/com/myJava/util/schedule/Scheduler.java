package com.myJava.util.schedule;

import java.util.Vector;

/**
 * Classe permettant de g�rer des t�ches planifi�es.
 * <BR>
 * @author Olivier PETRUCCI
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

public class Scheduler {

    /**
     * Scheduler par d�faut
     */
    protected static Scheduler defaultScheduler = new Scheduler();

    /**
     * Liste des t�ches
     */
    protected Vector scdTasks;

    /**
     * Constructeur
     */
    public Scheduler() {
        this.scdTasks = new Vector();
    }

    /**
     * Ajout d'une t�che
     */
    public void addTask(ScheduledTask task) {
        this.scdTasks.add(task);
        task.startTask();
    }

    /**
     * Retourne le nombre de t�ches
     */
    public int getTaskCount() {
        return this.scdTasks.size();
    }

    /**
     * Retourne la t�che demand�e
     */
    public ScheduledTask getTask(int i) {
        return (ScheduledTask)this.scdTasks.elementAt(i);
    }

    /**
     * Arr�te les t�ches
     */
    public void stopAllTasks() {
        for (int i=0; i<this.getTaskCount(); i++) {
            this.getTask(i).stopTask();
        }
    }

    /**
     * Retourne le scheduler par d�faut.
     */
    public static Scheduler getDefaultScheduler() {
        return Scheduler.defaultScheduler;
    }
}