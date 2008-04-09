package com.myJava.util.taskmonitor;

import java.util.ArrayList;
import java.util.List;

import com.myJava.object.ToStringHelper;

/**
 * Classe d�finissant un moniteur d'avancement de t�che.
 * <BR>Permet d'enregistrer des sous t�ches, de d�finir un �tat d'avancement global
 * <BR>et de lever des �v�nements lors des changements d'�tat (@see TaskMonitorListener)
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2380639557663016217
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
public class TaskMonitor {

    // Taux de completion de la t�che (hors sous t�che) : double entre 0 et 1
    protected double currentCompletionRate;

    // Sous t�che courante
    protected TaskMonitor currentSubTask;
    
    // Part de la sous t�che dans l'avancement global de la t�che (double, entre 0 et 1)
    protected double currentSubTaskShare;
    
    // En cas de sous t�che, t�che parent
    protected TaskMonitor parentTask;   
    
    // Liste contenant des TaskMonitorListeners
    protected List listeners; 
    
    // Boolean telling wether the current tack can be cancelled or not
    protected boolean cancellable = true;
    
    // Tells wether a "cancel" has been requested by the user
    protected boolean cancelRequested = false;
    
    protected String name = "";
    
    public TaskMonitor(String name) {
        this.currentCompletionRate = 0;
        this.currentSubTaskShare = 0;
        this.currentSubTask = null;
        this.parentTask = null; 
        this.name = name;
        this.clearAllListeners();  
    }
    
    // Ajoute un listener � la liste
    public void addListener(TaskMonitorListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        } else {
            this.listeners.add(listener);
        }
    }
    
    // Supprime tous les listeners.
    public void clearAllListeners() {
        this.listeners = new ArrayList();
    }
    
    // Retourne le taux d'avancement global de la t�che
    // (taux courant de la t�che + part de la sous t�che * taux global de la sous t�che.
    public double getGlobalCompletionRate() {
        if (currentSubTask == null) {
            return this.currentCompletionRate;
        } else {
            return this.currentCompletionRate 
                    + this.currentSubTaskShare * this.currentSubTask.getGlobalCompletionRate();
        }
    }
    
    // Initialise la sous t�che courante
    // subTaskShare repr�sente la part de la sous t�che dans l'avancement global
    // de la t�che (exemple : 0.1 pour 10%)
    public void setCurrentSubTask(TaskMonitor subTask, double subTaskShare) {
        if (subTask.getGlobalCompletionRate() > 0) {
            throw new IllegalArgumentException("Illegal attempt to add a subTask which has already been started.");
        }
        
        this.currentSubTask = subTask;
        this.currentSubTaskShare = subTaskShare;
        subTask.parentTask = this;
    }
    
    public void addNewSubTask(double subTaskShare, String name) {
        this.setCurrentSubTask(new TaskMonitor(name), subTaskShare);
    }

    // Initialise le taux d'avancement global de la t�che.
    // Attention : ceci n'est faisable que si la sous t�che courante a �t� termin�e.    
    public void setCurrentCompletion(double completion) {
        
        // On ne fait rien si le taux d'avancement ne change pas. (sauf si la compl�tion est �gale � 0, auquel cas on force l'update quoi qu'il arrive)
        if (this.currentCompletionRate == completion && completion != 0) {
            return;
        }
        
        if (this.currentSubTask != null) {
            throw new IllegalArgumentException("Illegal attempt to enforce the current task's completion while a subtask is pending");
        }
        
        if (completion < this.currentCompletionRate) {
            throw new IllegalArgumentException("Illegal Argument : the current completion rate is above the completion rate passed in argument");
        }
        
        // MAJ du taux d'avancement.
        this.currentCompletionRate = completion;
        
        // Lev�e d'�v�nement.
        this.completionChanged();
    }
    
    public void addCompletion(double completionStep) {
        this.setCurrentCompletion(this.currentCompletionRate + completionStep);
    }
    
    public void setCurrentCompletion(long numerator, long denominator) {
        if (denominator == numerator) {
            this.setCurrentCompletion(1.); // cas "0/0"
        } else {
            this.setCurrentCompletion(((double)numerator) / ((double)denominator));
        }
    }    
    
    /**
     * Enforces the task's completion.
     * <BR>All subtasks are deleted.
     * <BR>Since no checks are made, it must be used VERY carefully.
     */
    public void enforceCompletion() {
        this.currentSubTask = null;
        this.setCurrentCompletion(1.0);
    }
    
    // Retourne la sous t�che r�ellement active de la t�che courante
    // <BR>(c'est � dire la derni�re sous t�che ayant �t� ajout�e dans le cha�ne des sous t�ches)
    // <BR>C'est la seule sous t�che dont le taux d'avancement peut �tre mis � jour par appel direct � 
    // 'setCurrentCompletion()'.
    public TaskMonitor getCurrentActiveSubTask() {
        if (this.currentSubTask == null) {
            return this;
        } else {
            return this.currentSubTask.getCurrentActiveSubTask();
        }
    }
    
    // Alerte ses listeners ainsi que la t�che parente que le taux d'avancement a chang�.
    protected void completionChanged() {
        // nettoyage de la sous t�che
        if (this.currentSubTask != null && this.currentSubTask.getGlobalCompletionRate() >= 1.) {
            this.completeCurrentSubTask();
        }
        
        // appel des listeners
        for (int i=0; i<this.listeners.size(); i++) {
            ((TaskMonitorListener)listeners.get(i)).completionChanged(this);
        }
        
        // appel de la t�che parente
        if (this.parentTask != null) {
            this.parentTask.completionChanged();
        }
    }
    
    // Supprime la sous t�che courante et int�gre son taux d'avancement dans le taux d'avancement
    // courant.
    // Cette sous t�che est suppos�e avoir �t� termin�e (taux d'avancement de la sous t�che = 1);
    private void completeCurrentSubTask() {
        this.currentSubTask.clearAllListeners();
        this.currentSubTask.parentTask = null;
        this.currentSubTask = null;
        this.currentCompletionRate += currentSubTaskShare;
        this.currentSubTaskShare = 0;
    }    
    
    public synchronized boolean isCancellable() {
        return cancellable;
    }
    
    public synchronized boolean isCancelRequested() {
        return cancelRequested;
    }
    
    public void setCancelRequested() {
        if (cancellable) {
            synchronized(this) {
                this.cancelRequested = true;
                this.setCancellable(false);
            }
            
            // appel des listeners
            for (int i=0; i<this.listeners.size(); i++) {
                ((TaskMonitorListener)listeners.get(i)).cancelRequested(this);
            }
        }
    }
    
    public void resetCancellationState() {
        this.cancelRequested = false;
        this.setCancellable(true);
    }
    
    public void setCancellable(boolean cancellable) {
        synchronized(this) {
            this.cancellable = cancellable;
        }
        
        // appel des listeners
        for (int i=0; i<this.listeners.size(); i++) {
            ((TaskMonitorListener)listeners.get(i)).cancellableChanged(this);
        }
    }
    
    public void checkTaskCancellation() throws TaskCancelledException {
        if (this.isCancelRequested()) {
            throw new TaskCancelledException("The task has been cancelled");
        }
    }

    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("Name", this.name, sb);
        ToStringHelper.append("Child", this.currentSubTask, sb);
        ToStringHelper.append("ChildShare", this.currentSubTaskShare, sb);
        ToStringHelper.append("Completion", this.currentCompletionRate, sb);
        return ToStringHelper.close(sb);
    }
}