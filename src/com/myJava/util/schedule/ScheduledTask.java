package com.myJava.util.schedule;

import java.util.GregorianCalendar;

import com.myJava.util.log.Logger;

/**
 * Tâche destinée à être gérée dans un scheduler
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4331497872542711431
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

public abstract class ScheduledTask implements Runnable {

    /**
     * Date de dernière exécution
     */
    protected GregorianCalendar tskLastExecutionDate;

    /**
     * Délai (secondes).
     * Il s'agit du délai de test de "evaluateCondition"
     */
    protected long tskDelay;

    /**
     * Le thread associé à la tâche
     */
    protected Thread tskThread;

    /**
     * Flag interne indiquant si la tâche doit s'arrêter.
     */
    protected boolean tskShallStop;

    /**
     * Constructeur.
     * Le délai est spécifié en secondes.
     */
    public ScheduledTask(long delay) {
        super();
        this.tskDelay = delay;
        this.tskThread = null;
        this.tskLastExecutionDate = null;
    }

    /**
     * Méthode exécutée lors de l'appel à la tâche.
     */
    public void execute() {
        Logger.defaultLogger().info("Scheduled task startup.", this.getClass().getName());
        this.tskLastExecutionDate = new GregorianCalendar();
    }

    /**
     * Méthode déterminant si la tâche doit être exécutée ou non
     */
    public boolean evaluateCondition() {
        return true;
    }

    /**
     * Démarre la tâche
     */
    public void startTask() {
        this.tskShallStop = false;
        Thread th = new Thread(this);
        th.setName("Scheduled task : " + this.getClass().getName());
        th.setDaemon(true);
        th.start();
        this.tskThread = th;
    }

    /**
     * Indique si la tâche fonctionne
     */
    public boolean isRunning() {
        return this.tskThread.isAlive();
    }

    /**
     * Arrête la tâche
     */
    public void stopTask() {
        this.tskShallStop = true;
    }

    /**
     * Méthode mettant le thread courant en pause
     */
    public void pauseThread() {
        try {
            Thread.sleep(this.tskDelay * 1000);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Méthode tournant en tâche de fond.
     */
    public void run() {
        while (! this.tskShallStop) {
            if (this.evaluateCondition()) {
                this.execute();
            }
            this.pauseThread();
        }
    }
}