package com.myJava.util.errors;

import java.util.Vector;


/**
 * Classe représentant un rapport d'erreur sur des données.
 *
 * Elle référence :
 * - une collection d'erreurs
 * - un booléen indiquant si tout est OK ou non
 * - Une information complémentaire, sous la forme d'une instance "Object"
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

public class ActionReport {

    /**
     * Liste des erreurs référencées par le QueryReport
     */
    private Vector errors;

    /**
     * Liste des informations retournées par l'action
     * Objet utile pour compléter les informations du rapprot d'erreur.
     * Il peut par exemple servir à stocker le résultat d'une exécution de requête
     * (identifiant de l'enregistrement inséré, nombre d'enregistrements modifiés
     * ou supprimés, etc.)
     */
    private Vector informations;

    /**
     * Constructeur
     */
    public ActionReport() {
        this.errors = new Vector();
        this.informations = new Vector();
    }

    /**
     * Indique le résultat de la vérification
     */
    public boolean isDataValid() {
        return (this.errors.size() == 0);
    }

    /**
     * Retourne le nombre d'erreurs
     */
    public int getErrorCount() {
        return this.errors.size();
    }

    /**
     * Retourne le nombre d'informations
     */
    public int getInformationCount() {
        return this.informations.size();
    }

    /**
     * Retourne le nombre d'informations du type spécifié
     */
    public int getInformationCount(String type) {
        return this.getInformationsByType(type).size();
    }

    /**
     * Retourne l'erreur à l'index spécifié
     */
    public ActionError getErrorAt(int i) {
        return (ActionError)this.errors.elementAt(i);
    }

    /**
     * Retourne l'information à l'index spécifié
     */
    public ActionInformation getInformationAt(int i) {
        return (ActionInformation)this.informations.elementAt(i);
    }

    /**
     * Retourne l'information à l'index spécifié. Seules les informations
     * du type demandé sont prises en compte.
     */
    public ActionInformation getInformationAt(String type, int i) {
        return (ActionInformation)this.getInformationsByType(type).elementAt(i);
    }

    /**
     * Ajout d'une erreur
     */
    public void addError(ActionError err) {
        this.errors.addElement(err);
    }

    /**
     * Ajout d'une erreur
     */
    public void addError(int code) {
        ActionError err = new ActionError(code);
        this.addError(err);
    }

    /**
     * Ajout d'une information
     */
    public void addInformation(ActionInformation inf) {
        this.informations.addElement(inf);
    }

    /**
     * Ajout d'une information
     */
    public void addInformation(String type, Object value) {
        ActionInformation inf = new ActionInformation(type, value);
        this.addInformation(inf);
    }

    /**
     * Ajout d'une information
     */
    public void addInformation(Object value) {
        ActionInformation inf = new ActionInformation(value);
        this.addInformation(inf);
    }

    /**
     * Retourne un vecteur contenant les informations d'un type donné
     */
    private Vector getInformationsByType(String type) {
        Vector infos = new Vector();
        int nbInf = this.getInformationCount();
        for (int i=0; i<nbInf; i++) {
            if (this.getInformationAt(i).getType().equalsIgnoreCase(type)) {
                infos.addElement(this.getInformationAt(i));
            }
        }

        return infos;
    }

    /**
     * Fusionne les deux resultats.
     * Les listes d'erreur et d'informations sont concaténées.
     */
    public void merge(ActionReport res) {
        if (res != null) {
            for (int i=0; i<res.getErrorCount(); i++) {
                this.addError(res.getErrorAt(i));
            }

            for (int i=0; i<res.getInformationCount(); i++) {
                this.addInformation(res.getInformationAt(i));
            }
        }
    }

    /**
     * Redéfinition toString
     */
    public String toString() {
        String str = "Nombre d'erreurs = [" + this.getErrorCount() + "], Nombre d'informations = [" + this.getInformationCount() + "]";
        str += "\nListe des erreurs : [";
        for (int i=0; i<this.getErrorCount(); i++) {
            str += " " + this.getErrorAt(i).toString();
        }
        str += "]";
        return str;
    }
}