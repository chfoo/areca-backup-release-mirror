package com.application.areca;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import com.application.areca.context.ProcessContext;
import com.application.areca.indicator.IndicatorMap;
import com.application.areca.metadata.manifest.Manifest;
import com.application.areca.search.SearchCriteria;
import com.application.areca.search.TargetSearchResult;
import com.myJava.object.PublicClonable;
import com.myJava.util.errors.ActionReport;
import com.myJava.util.history.History;

/**
 * <BR>Interface définissant un support de stockage et pouvant héberger des archives.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 5653799526062900358
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
public interface ArchiveMedium extends PublicClonable {

    /**
     * Vérifie l'état du système avant l'action spécifiée (archivage, backup, fusion) 
     */
    public ActionReport checkMediumState(int action);
    
    /**
     * Stocke une entrée sur le support
     */
    public void store(RecoveryEntry entry, ProcessContext context) throws StoreException, ApplicationException;
    
    /**
     * Fusionne les archives jusqu'à la date donnée.
     * <BR>Le comportement de cette méthode varie avec l'implémentation.
     * <BR>Cette méthode sert à éliminer des informations redondantes éventuellement stockées au
     * fil des archivages. 
     */
    public void merge(
            GregorianCalendar fromDate, 
            GregorianCalendar toDate, 
            boolean keepDeletedEntries,
            Manifest manifest,
            ProcessContext context
    ) throws ApplicationException;

    /**
     * Supprime les archives comprises entre "fromDate" et la date courante
     */
    public void deleteArchives(
            GregorianCalendar fromDate,
            ProcessContext context            
    ) throws ApplicationException;
    
    /**
     * Restaure l'archive correspondant à la date. Les informations sont restaurées à l'emplacement
     * désigné par "destination". 
     */
    public void recover(
            Object destination, 
            String[] filter,
            GregorianCalendar date,
            boolean recoverDeletedEntries,
            ProcessContext context            
    ) throws ApplicationException;
    
    /**
     * Détruit toutes les archives et données les concernant.
     */
    public void destroyRepository() throws ApplicationException;
    
    /**
     * Ouvre le support afin qu'il puisse héberger par la suite des entrées.
     */    
    public void open(ProcessContext context) throws ApplicationException;
    
    /**
     * Ouvre le support avec le manifest spécifié.
     * Ceci signifie que les entrées ajoutées le seront avec ce manifest.
     * Selon le mode de traitement du support, ce manifest peut écraser un manifest éventuellement
     * existant, ou au contraire le compléter.
     */
    public void open(Manifest manifest, ProcessContext context) throws ApplicationException;
    
    /**
     * Valide le backup
     */
    public void commitBackup(ProcessContext context) throws ApplicationException;
    
    /**
     * Annule le backup
     */
    public void rollbackBackup(ProcessContext context) throws ApplicationException;
    
    /**
     * Valide le merge
     */
    public void commitMerge(ProcessContext context) throws ApplicationException;
    
    /**
     * Annule le merge
     */
    public void rollbackMerge(ProcessContext context) throws ApplicationException;
    
    /**
     * Retourne la cible à laquelle est affecté le support 
     */
    public AbstractRecoveryTarget getTarget();
    
    /**
     * Retourne une description du support de stockage 
     */
    public String getDescription();
    
    /**
     * Retourne l'historique des opérations effectuées sur le support 
     */
    public History getHistory();
    
    /**
     * Retourne le manifeste de l'archive correspondant à la date demandée. 
     */
    public Manifest getManifest(GregorianCalendar date) throws ApplicationException;
    
    /**
     * Retourne les entrées contenues dans l'archive pour la date donnée. (sous forme de RecoveryEntries)
     */
    public Set getEntries(GregorianCalendar date) throws ApplicationException;
    
    public Set getLogicalView() throws ApplicationException;
    
    /**
     * Retourne l'historique d'une entrée donnée
     */
    public EntryArchiveData[] getHistory(RecoveryEntry entry) throws ApplicationException;
    
    /**
     * Appelée avant la suppression de la target à laquelle appartient le medium
     */
    public void doBeforeDelete();
    
    /**
     * Appelée après la suppression de la target à laquelle appartient le medium
     */
    public void doAfterDelete();
    
    /**
     * Simule le traitement d'une RecoveryEntry durant un backup en mettant à jour son status.
     * Permet à la target de simuler un backup.
     */
    public void simulateEntryProcessing(RecoveryEntry entry, ProcessContext context) throws ApplicationException;
    
    /**
     * Closes the simulation and returns all unprocessed entries (ie entries which have been deleted). 
     */
    public List closeSimulation(ProcessContext context) throws ApplicationException;
    
    /**
     * Restaure la version demandée (identifiée par sa date) de l'entrée spécifiée à l'emplacement spécifié  
     */
    public void recoverEntry(
            GregorianCalendar date, 
            RecoveryEntry entryToRecover, 
            Object destination,
            ProcessContext context            
    ) throws ApplicationException;

    /**
     * Indique s'il est utile de faire une prévérification avant de déclencher le backup.
     * <BR>Cette méthode sera par exemple utile pour les supports de stockage incrémentaux : ceci
     * leur permet de vérifier qu'au moins un fichier a été modifié avant de lancer le backup.
     */
    public boolean isPreBackupCheckUseful();
    
    /**
     * Computes indicators on the archives stored by the medium.
     */
    public IndicatorMap computeIndicators() throws ApplicationException;
    
    /**
     * Searches entries within the archives 
     */
    public TargetSearchResult search(SearchCriteria criteria) throws ApplicationException;
    
    public void install() throws ApplicationException;
    
    public void setTarget(AbstractRecoveryTarget target, boolean revalidate);
}