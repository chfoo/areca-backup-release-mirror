package com.myJava.util.errors;

import java.util.Vector;


/**
 * Classe repr�sentant un rapport d'erreur sur des donn�es.
 *
 * Elle r�f�rence :
 * - une collection d'erreurs
 * - un bool�en indiquant si tout est OK ou non
 * - Une information compl�mentaire, sous la forme d'une instance "Object"
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

public class ActionReport {

    /**
     * Liste des erreurs r�f�renc�es par le QueryReport
     */
    private Vector errors;

    /**
     * Liste des informations retourn�es par l'action
     * Objet utile pour compl�ter les informations du rapprot d'erreur.
     * Il peut par exemple servir � stocker le r�sultat d'une ex�cution de requ�te
     * (identifiant de l'enregistrement ins�r�, nombre d'enregistrements modifi�s
     * ou supprim�s, etc.)
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
     * Indique le r�sultat de la v�rification
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
     * Retourne le nombre d'informations du type sp�cifi�
     */
    public int getInformationCount(String type) {
        return this.getInformationsByType(type).size();
    }

    /**
     * Retourne l'erreur � l'index sp�cifi�
     */
    public ActionError getErrorAt(int i) {
        return (ActionError)this.errors.elementAt(i);
    }

    /**
     * Retourne l'information � l'index sp�cifi�
     */
    public ActionInformation getInformationAt(int i) {
        return (ActionInformation)this.informations.elementAt(i);
    }

    /**
     * Retourne l'information � l'index sp�cifi�. Seules les informations
     * du type demand� sont prises en compte.
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
     * Retourne un vecteur contenant les informations d'un type donn�
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
     * Les listes d'erreur et d'informations sont concat�n�es.
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
     * Red�finition toString
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