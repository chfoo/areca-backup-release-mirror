package com.myJava.util.taskmonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe définissant un moniteur d'avancement de tâche.
 * <BR>Permet d'enregistrer des sous tâches, de définir un état d'avancement global
 * <BR>et de lever des événements lors des changements d'état (@see TaskMonitorListener)
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -6307890396762748969
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

    // Taux de completion de la tâche (hors sous tâche) : double entre 0 et 1
    protected double currentCompletionRate;

    // Sous tâche courante
    protected TaskMonitor currentSubTask;
    
    // Part de la sous tâche dans l'avancement global de la tâche (double, entre 0 et 1)
    protected double currentSubTaskShare;
    
    // En cas de sous tâche, tâche parent
    protected TaskMonitor parentTask;   
    
    // Liste contenant des TaskMonitorListeners
    protected List listeners; 
    
    // Boolean telling wether the current tack can be cancelled or not
    protected boolean cancellable = true;
    
    // Tells wether a "cancel" has been requested by the user
    protected boolean cancelRequested = false;
    
    public TaskMonitor() {
        this.currentCompletionRate = 0;
        this.currentSubTaskShare = 0;
        this.currentSubTask = null;
        this.parentTask = null; 
        this.clearAllListeners();  
    }
    
    // Ajoute un listener à la liste
    public void addListener(TaskMonitorListener listener) {
        this.listeners.add(listener);
    }
    
    // Supprime tous les listeners.
    public void clearAllListeners() {
        this.listeners = new ArrayList();
    }
    
    // Retourne le taux d'avancement global de la tâche
    // (taux courant de la tâche + part de la sous tâche * taux global de la sous tâche.
    public double getGlobalCompletionRate() {
        if (currentSubTask == null) {
            return this.currentCompletionRate;
        } else {
            return this.currentCompletionRate 
                    + this.currentSubTaskShare * this.currentSubTask.getGlobalCompletionRate();
        }
    }
    
    // Initialise la sous tâche courante
    // subTaskShare représente la part de la sous tâche dans l'avancement global
    // de la tâche (exemple : 0.1 pour 10%)
    public void setCurrentSubTask(TaskMonitor subTask, double subTaskShare) {
        if (subTask.getGlobalCompletionRate() > 0) {
            throw new IllegalArgumentException("Illegal attempt to add a subTask which has already been started.");
        }
        
        this.currentSubTask = subTask;
        this.currentSubTaskShare = subTaskShare;
        subTask.parentTask = this;
    }
    
    public void addNewSubTask(double subTaskShare) {
        this.setCurrentSubTask(new TaskMonitor(), subTaskShare);
    }

    // Initialise le taux d'avancement global de la tâche.
    // Attention : ceci n'est faisable que si la sous tâche courante a été terminée.    
    public void setCurrentCompletion(double completion) {
        
        // On ne fait rien si le taux d'avancement ne change pas. (sauf si la complétion est égale à 0, auquel cas on force l'update quoi qu'il arrive)
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
        
        // Levée d'événement.
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
    
    // Retourne la sous tâche réellement active de la tâche courante
    // <BR>(c'est à dire la dernière sous tâche ayant été ajoutée dans le chaîne des sous tâches)
    // <BR>C'est la seule sous tâche dont le taux d'avancement peut être mis à jour par appel direct à 
    // 'setCurrentCompletion()'.
    public TaskMonitor getCurrentActiveSubTask() {
        if (this.currentSubTask == null) {
            return this;
        } else {
            return this.currentSubTask.getCurrentActiveSubTask();
        }
    }
    
    // Alerte ses listeners ainsi que la tâche parente que le taux d'avancement a changé.
    protected void completionChanged() {
        // nettoyage de la sous tâche
        if (this.currentSubTask != null && this.currentSubTask.getGlobalCompletionRate() >= 1.) {
            this.completeCurrentSubTask();
        }
        
        // appel des listeners
        for (int i=0; i<this.listeners.size(); i++) {
            ((TaskMonitorListener)listeners.get(i)).completionChanged(this);
        }
        
        // appel de la tâche parente
        if (this.parentTask != null) {
            this.parentTask.completionChanged();
        }
    }
    
    // Supprime la sous tâche courante et intègre son taux d'avancement dans le taux d'avancement
    // courant.
    // Cette sous tâche est supposée avoir été terminée (taux d'avancement de la sous tâche = 1);
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
}