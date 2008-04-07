package com.myJava.util.schedule;

import java.util.GregorianCalendar;

import com.myJava.util.log.Logger;

/**
 * T�che destin�e � �tre g�r�e dans un scheduler
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 8363716858549252512
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
     * Date de derni�re ex�cution
     */
    protected GregorianCalendar tskLastExecutionDate;

    /**
     * D�lai (secondes).
     * Il s'agit du d�lai de test de "evaluateCondition"
     */
    protected long tskDelay;

    /**
     * Le thread associ� � la t�che
     */
    protected Thread tskThread;

    /**
     * Flag interne indiquant si la t�che doit s'arr�ter.
     */
    protected boolean tskShallStop;

    /**
     * Constructeur.
     * Le d�lai est sp�cifi� en secondes.
     */
    public ScheduledTask(long delay) {
        super();
        this.tskDelay = delay;
        this.tskThread = null;
        this.tskLastExecutionDate = null;
    }

    /**
     * M�thode ex�cut�e lors de l'appel � la t�che.
     */
    public void execute() {
        Logger.defaultLogger().info("Scheduled task startup.", this.getClass().getName());
        this.tskLastExecutionDate = new GregorianCalendar();
    }

    /**
     * M�thode d�terminant si la t�che doit �tre ex�cut�e ou non
     */
    public boolean evaluateCondition() {
        return true;
    }

    /**
     * D�marre la t�che
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
     * Indique si la t�che fonctionne
     */
    public boolean isRunning() {
        return this.tskThread.isAlive();
    }

    /**
     * Arr�te la t�che
     */
    public void stopTask() {
        this.tskShallStop = true;
    }

    /**
     * M�thode mettant le thread courant en pause
     */
    public void pauseThread() {
        try {
            Thread.sleep(this.tskDelay * 1000);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * M�thode tournant en t�che de fond.
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