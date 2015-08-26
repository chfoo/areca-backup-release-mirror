package com.myJava.util.schedule;

import java.util.GregorianCalendar;

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

public abstract class ScheduledTask implements Runnable {

    protected GregorianCalendar tskLastExecutionDate;

    /**
     * Delai (secondes).
     * Il s'agit du delai de test de "evaluateCondition"
     */
    protected long tskDelay;


    protected Thread tskThread;
    protected boolean tskShallStop;

    /**
     * Constructeur.
     * Le delai est specifie en secondes.
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
    public synchronized void stopTask() {
        this.tskShallStop = true;
        this.notifyAll();
    }

    /**
     * M�thode mettant le thread courant en pause
     */
    public synchronized void pauseThread() {
        try {
        	this.wait(this.tskDelay * 1000);
        } catch (InterruptedException ignored) {
        }
    }

    public void run() {
        while (! this.tskShallStop) {
            if (this.evaluateCondition()) {
                this.execute();
            }
            this.pauseThread();
        }
    }
}